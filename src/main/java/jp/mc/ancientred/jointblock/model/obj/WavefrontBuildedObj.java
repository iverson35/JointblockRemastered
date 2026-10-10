package jp.mc.ancientred.jointblock.model.obj;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.mc.ancientred.jointblock.JointBlockRemastered;

public class WavefrontBuildedObj {
   public static final int NO_SMOOTHING_GROUP = 0;
   public static final int EMPTY_VERTEX_VALUE = Integer.MIN_VALUE;
   public boolean isWithMaterial;
   public String objectName = null;
   public String objFilename = null;
   public String predictedMtlFilename = null;
   public ArrayList<ObjFace.ObjVec3f> vertices = new ArrayList<>();
   public ArrayList<ObjFace.ObjVec3f> normals = new ArrayList<>();
   public ArrayList<ObjFace.TextureUV> texUVs = new ArrayList<>();
   public ArrayList<ObjFace> facesTris = new ArrayList<>();
   public ArrayList<ObjFace> facesQuads = new ArrayList<>();
   private ArrayList<ObjFace> currentSmoothingGroup = null;
   private int currentSmoothingGroupNumber = 0;
   public HashMap<Integer, ArrayList<ObjFace>> smoothingGroups = new HashMap<>();
   private ArrayList<String> currentGroups = new ArrayList<>();
   public HashMap<String, ArrayList<ObjFace>> groups = new HashMap<>();
   private ArrayList<ArrayList<ObjFace>> currentGroupFaceLists = new ArrayList<>();
   private ObjFace.Material currentMaterial = null;
   public HashMap<String, ObjFace.Material> materialLib = new HashMap<>();

   public WavefrontBuildedObj(String fileName, InputStream fileIs) throws IOException {
      WavefrontBuildedObj.Parser.doParse(this, fileName, fileIs, (InputStream)null);
   }

   public WavefrontBuildedObj(String fileName, InputStream fileIs, InputStream fileIsMtl) throws IOException {
      WavefrontBuildedObj.Parser.doParse(this, fileName, fileIs, fileIsMtl);
   }

   public void setObjFilename(String filename) {
      this.objFilename = filename;
      this.predictedMtlFilename = null;
      int spidx;
      if ((spidx = this.objFilename.lastIndexOf(47)) != -1) {
         String objName = this.objFilename.substring(spidx + 1, this.objFilename.length());
         if (objName.toLowerCase().endsWith(".obj")) {
            this.predictedMtlFilename = objName.substring(0, Math.max(0, objName.length() - 4)) + ".mtl";
         }
      }

   }

   public boolean isPredictedMtlName(String matlibname) {
      return this.predictedMtlFilename == null ? false : this.predictedMtlFilename.equalsIgnoreCase(matlibname);
   }

   public void addFace(int[] vertexIndices, ObjFace.TrisCalculationHelper triangulationHelper, ArrayList<ObjFace.ObjVertex> temporaryVertList) throws Exception {
      float averageU = 0.0F;
      float averageV = 0.0F;
      int texCount = 0;
      temporaryVertList.clear();
      int index = 0;

      while(true) {
         if (index < vertexIndices.length) {
            int vertexIndex = vertexIndices[index++];
            if (vertexIndex < 0) {
               vertexIndex += this.vertices.size();
            }

            if (vertexIndex > 0 && vertexIndex <= this.vertices.size()) {
               ObjFace.ObjVertex fv = new ObjFace.ObjVertex((ObjFace.ObjVec3f)this.vertices.get(vertexIndex - 1));
               if ((vertexIndex = vertexIndices[index++]) != Integer.MIN_VALUE) {
                  if (vertexIndex < 0) {
                     vertexIndex += this.texUVs.size();
                  }

                  if (vertexIndex > 0 && vertexIndex <= this.texUVs.size()) {
                     fv.t = (ObjFace.TextureUV)this.texUVs.get(vertexIndex - 1);
                     averageU += fv.t.u;
                     averageV += fv.t.v;
                     ++texCount;
                  }
               }

               if ((vertexIndex = vertexIndices[index++]) != Integer.MIN_VALUE) {
                  if (vertexIndex < 0) {
                     vertexIndex += this.normals.size();
                  }

                  if (vertexIndex > 0 && vertexIndex <= this.normals.size()) {
                     fv.n = (ObjFace.ObjVec3f)this.normals.get(vertexIndex - 1);
                  }
               }

               temporaryVertList.add(fv);
               continue;
            }

            throw new Exception("Error constructing face. Vertex must have at least coord index.");
         }

         if (temporaryVertList.size() == 0) {
            throw new Exception("Error constructing face. No vertex.");
         }

         ObjFace face = new ObjFace(temporaryVertList);
         if (this.currentMaterial != null) {
            face.material = this.currentMaterial;
            this.isWithMaterial = true;
         }

         if (texCount > 0) {
            this.fixTexutureUV(face, averageU, averageV, texCount);
         }

         if (face.vertIdxCnt == 3) {
            this.facesTris.add(face);
            if (this.currentSmoothingGroup != null) {
               this.currentSmoothingGroup.add(face);
            }

            for(index = 0; index < this.currentGroupFaceLists.size(); ++index) {
               ((ArrayList<ObjFace>)this.currentGroupFaceLists.get(index)).add(face);
            }
         } else if (face.vertIdxCnt == 4) {
            this.facesQuads.add(face);
            if (this.currentSmoothingGroup != null) {
               this.currentSmoothingGroup.add(face);
            }

            for(index = 0; index < this.currentGroupFaceLists.size(); ++index) {
               ((ArrayList<ObjFace>)this.currentGroupFaceLists.get(index)).add(face);
            }
         } else {
            if (face.vertIdxCnt <= 4) {
               throw new Exception("Error constructing face. Too few vertex count.");
            }

            if (!face.breakDownToTris(triangulationHelper)) {
               throw new Exception("Error constructing face. Failed ngons to tris conversion.");
            }

            this.facesTris.addAll(triangulationHelper.triangleList);
            if (this.currentSmoothingGroup != null) {
               this.currentSmoothingGroup.addAll(triangulationHelper.triangleList);
            }

            for(index = 0; index < this.currentGroupFaceLists.size(); ++index) {
               ((ArrayList<ObjFace>)this.currentGroupFaceLists.get(index)).addAll(triangulationHelper.triangleList);
            }
         }

         return;
      }
   }

   private void fixTexutureUV(ObjFace face, float averageU, float averageV, int texCount) {
      averageU /= (float)texCount;
      averageV /= (float)texCount;

      for(int i = 0; i < face.vertIdxCnt; ++i) {
         ObjFace.ObjVertex fv = face.vertices[i];
         if (fv != null && fv.t != null) {
            float offsetU = 5.0E-4F;
            float offsetV = 5.0E-4F;
            if (fv.t.u > averageU) {
               offsetU = -offsetU;
            }

            if (fv.t.v > averageV) {
               offsetV = -offsetV;
            }

            fv.t.u += offsetU;
            fv.t.v += offsetV;
         }
      }

   }

   public void setCurrentGroupNames(String[] names) {
      this.currentGroups.clear();
      this.currentGroupFaceLists.clear();
      if (names != null) {
         for(int loopi = 0; loopi < names.length; ++loopi) {
            String group = names[loopi].trim();
            this.currentGroups.add(group);
            if (null == this.groups.get(group)) {
               this.groups.put(group, new ArrayList<>());
            }

            this.currentGroupFaceLists.add(this.groups.get(group));
         }

      }
   }

   public void setCurrentSmoothingGroup(int groupNumber) {
      this.currentSmoothingGroupNumber = groupNumber;
      if (this.currentSmoothingGroupNumber != NO_SMOOTHING_GROUP) {
         if (this.smoothingGroups.get(this.currentSmoothingGroupNumber) == null) {
            this.currentSmoothingGroup = new ArrayList<>();
            this.smoothingGroups.put(this.currentSmoothingGroupNumber, this.currentSmoothingGroup);
         }

      }
   }

   public static class Parser {
      private static final int ERROR_EXCEED_CNT = 5;
      private static final String MTL_KA = "Ka";
      private static final String MTL_KD = "Kd";
      private static final String MTL_KS = "Ks";
      private static final String MTL_TF = "Tf";
      private static final String MTL_D = "d";
      private static final String MTL_TR = "Tr";
      private static ObjFace.TrisCalculationHelper triangulationHelper = new ObjFace.TrisCalculationHelper();
      private static ArrayList<ObjFace.ObjVertex> temporaryVertList = new ArrayList<>();
      private static ObjFace.Material currentMaterialBeingParsed = null;
      private static final Integer EMPTY_INT = Integer.valueOf(Integer.MIN_VALUE);
      private static final String DUMMY_STR = "";
      private static Pattern SPACE_DELIMIT = Pattern.compile("\\s+");
      private static ArrayList<Integer> faceStoreList = new ArrayList<>();
      private static Matcher FACE_LIST_MT = Pattern.compile("\\s(-?[0-9]*)/?(-?[0-9]*)?/?(-?[0-9]*)?/?").matcher("");
      private static float[][] floatAryAry = new float[30][];
      private static int[][] intAryAry = new int[30][];
      private static Matcher FLOAT_LIST_MT = Pattern.compile("-?[0-9]+\\.?[0-9]*").matcher("");
      private static Matcher INT_LIST_MT = Pattern.compile("-?[0-9]*").matcher("");

      public static synchronized void doParse(WavefrontBuildedObj builder, String fileName, InputStream fileIs, InputStream fileIsMtl) throws FileNotFoundException, IOException {
         builder.setObjFilename(fileName);
         InputStreamReader reader = new InputStreamReader(fileIs);
         BufferedReader bufferedReader = new BufferedReader(reader);
         int lineCount = 0;
         int errorCount = 0;
         String line = null;

         while(true) {
            label95:
            while(true) {
               do {
                  if ((line = bufferedReader.readLine()) == null) {
                     bufferedReader.close();
                     JointBlockRemastered.LOGGER.info("Loaded obj file:" + fileName + "[faces of " + builder.facesTris.size() + "Tris and " + builder.facesQuads.size() + "Quads.]");
                     return;
                  }
               } while(isEmptyStr(line = line.trim()));

               try {
                  if (!line.startsWith("#")) {
                     float[] values;
                     if (line.startsWith("vt")) {
                        values = parseFloatNums(2, line);
                        builder.texUVs.add(new ObjFace.TextureUV(values[0], -values[1]));
                        break;
                     }

                     if (line.startsWith("vn")) {
                        values = parseFloatNums(3, line);
                        builder.normals.add(new ObjFace.ObjVec3f(values[0], values[1], values[2]));
                        break;
                     }

                     if (line.startsWith("v")) {
                        values = parseFloatNums(3, line);
                        builder.vertices.add(new ObjFace.ObjVec3f(values[0], values[1], values[2]));
                        break;
                     }

                     if (line.startsWith("f")) {
                        builder.addFace(processFace(line), triangulationHelper, temporaryVertList);
                        break;
                     }

                     String pref = "g";
                     String[] matlibnames;
                     if (line.startsWith("g")) {
                        matlibnames = splitToTokens(removeHead(line, pref));
                        builder.setCurrentGroupNames(matlibnames);
                        break;
                     }

                     pref = "o";
                     if (line.startsWith("o")) {
                        builder.objectName = removeHead(line, pref);
                        break;
                     }

                     pref = "s";
                     if (line.startsWith("s")) {
                        int groupNumber = 0;
                        if (!(line = removeHead(line, pref)).equalsIgnoreCase("off")) {
                           groupNumber = Integer.parseInt(line);
                        }

                        builder.setCurrentSmoothingGroup(groupNumber);
                        break;
                     }

                     pref = "usemtl";
                     if (line.startsWith("usemtl")) {
                        builder.currentMaterial = (ObjFace.Material)builder.materialLib.get(removeHead(line, pref));
                        break;
                     }

                     pref = "mtllib";
                     if (!line.startsWith("mtllib")) {
                        break;
                     }

                     if (fileIsMtl != null) {
                        matlibnames = splitToTokens(removeHead(line, pref));
                        if (null == matlibnames) {
                           break;
                        }

                        int i = 0;

                        while(true) {
                           if (i >= matlibnames.length) {
                              break label95;
                           }

                           String matlibname = matlibnames[i];
                           if (builder.isPredictedMtlName(matlibname)) {
                              try {
                                 parseMtlFile(builder, fileIsMtl);
                              } catch (FileNotFoundException var15) {
                                 JointBlockRemastered.LOGGER.info("Failed opening mtl file.");
                              }
                           }

                           ++i;
                        }
                     }
                  }
               } catch (Exception var16) {
                  if (errorCount >= ERROR_EXCEED_CNT) {
                     break;
                  }

                  JointBlockRemastered.LOGGER.info("Obj parse error at line:" + lineCount + ":[" + var16.getMessage() + "]");
                  ++errorCount;
                  if (errorCount >= ERROR_EXCEED_CNT) {
                     JointBlockRemastered.LOGGER.info(" ### Obj parse error count exceed. Further error log wil be ignored.");
                  }
                  break;
               }
            }

            ++lineCount;
         }
      }

      private static void parseMtlFile(WavefrontBuildedObj builder, InputStream fileIsMtl) throws FileNotFoundException, IOException {
         InputStreamReader reader = new InputStreamReader(fileIsMtl);
         BufferedReader bufferedReader = new BufferedReader(reader);
         int lineCount = 0;
         int errorCount = 0;
         String line = null;
         currentMaterialBeingParsed = null;

         while(true) {
            while(true) {
               do {
                  if ((line = bufferedReader.readLine()) == null) {
                     bufferedReader.close();
                     return;
                  }
               } while(isEmptyStr(line = line.trim()));

               try {
                  if (!line.startsWith("#")) {
                     String pref = "newmtl";
                     if (line.startsWith("newmtl")) {
                        String newMatName = removeHead(line, pref);
                        currentMaterialBeingParsed = new ObjFace.Material(newMatName);
                        builder.materialLib.put(newMatName, currentMaterialBeingParsed);
                     } else if (line.startsWith(MTL_KA)) {
                        processReflection(builder, MTL_KA, line);
                     } else if (line.startsWith(MTL_KD)) {
                        processReflection(builder, MTL_KD, line);
                     } else if (line.startsWith(MTL_KS)) {
                        processReflection(builder, MTL_KS, line);
                     } else if (line.startsWith(MTL_D)) {
                        processD(builder, MTL_D, line);
                     } else if (line.startsWith(MTL_TR)) {
                        processD(builder, MTL_TR, line);
                     }
                     break;
                  }
               } catch (Exception var9) {
                  if (errorCount < ERROR_EXCEED_CNT) {
                     JointBlockRemastered.LOGGER.info("Mtl parse error at line:" + lineCount + ":[" + var9.getMessage() + "]");
                     ++errorCount;
                     if (errorCount >= ERROR_EXCEED_CNT) {
                        JointBlockRemastered.LOGGER.info(" ### Mtl parse error count exceed. Further error log wil be ignored.");
                     }
                  }
                  break;
               }
            }

            ++lineCount;
         }
      }

      private static void processReflection(WavefrontBuildedObj builder, String fieldName, String line) {
         if (currentMaterialBeingParsed != null) {
            ObjFace.Reflection refl = currentMaterialBeingParsed.ka;
            if (fieldName.equals(MTL_KD)) {
               refl = currentMaterialBeingParsed.kd;
            } else if (fieldName.equals(MTL_KS)) {
               refl = currentMaterialBeingParsed.ks;
            } else if (fieldName.equals(MTL_TF)) {
               refl = currentMaterialBeingParsed.tf;
            }

            String[] tokens = splitToTokens(line.substring(fieldName.length()));
            if (tokens != null && tokens.length > 0) {
               if (!tokens[0].equals("spectral")) {
                  if (tokens[0].equals("xyz")) {
                     if (tokens.length < 2) {
                        return;
                     }

                     refl.bz = refl.gy = refl.rx = (double)Float.parseFloat(tokens[1]);
                     if (tokens.length > 2) {
                        refl.gy = (double)Float.parseFloat(tokens[2]);
                     }

                     if (tokens.length > 3) {
                        refl.bz = (double)Float.parseFloat(tokens[3]);
                     }

                     refl.isXYZ = true;
                     refl.isRGB = false;
                  } else {
                     refl.bz = refl.gy = refl.rx = (double)Float.parseFloat(tokens[0]);
                     if (tokens.length > 1) {
                        refl.gy = (double)Float.parseFloat(tokens[1]);
                     }

                     if (tokens.length > 2) {
                        refl.bz = (double)Float.parseFloat(tokens[2]);
                     }

                     refl.isRGB = true;
                     refl.isXYZ = false;
                  }

               }
            }
         }
      }

      private static void processD(WavefrontBuildedObj builder, String fieldName, String line) {
         if (currentMaterialBeingParsed != null) {
            String[] tokens = splitToTokens(line.substring(fieldName.length()));
            if (tokens != null && tokens.length >= 1) {
               currentMaterialBeingParsed.dFactor = (double)Float.parseFloat(tokens[0]);
            }

         }
      }

      private static void processTr(WavefrontBuildedObj builder, String fieldName, String line) {
         if (currentMaterialBeingParsed != null) {
            String[] tokens = splitToTokens(line.substring(fieldName.length()));
            if (tokens != null && tokens.length >= 1) {
               currentMaterialBeingParsed.dFactor = 1.0 - (double)Float.parseFloat(tokens[0]);
            }

         }
      }

      private static final boolean isEmptyStr(String str) {
         return str == null || DUMMY_STR.equals(str);
      }

      private static String[] splitToTokens(String line) {
         return SPACE_DELIMIT.split(line.trim(), 0);
      }

      private static String removeHead(String line, String prefix) {
         return line.substring(prefix.length()).trim();
      }

      private static int[] processFace(String faceStr) {
         if (isEmptyStr(faceStr)) {
            return null;
         } else {
            faceStoreList.clear();
            FACE_LIST_MT.reset(faceStr);

            while(FACE_LIST_MT.find()) {
               for(int g = 1; g < 4; ++g) {
                  String strMatched;
                  if (!isEmptyStr(strMatched = FACE_LIST_MT.group(g))) {
                     faceStoreList.add(Integer.parseInt(strMatched));
                  } else {
                     faceStoreList.add(EMPTY_INT);
                  }
               }
            }

            int[] result = new int[faceStoreList.size()];
            int i = 0;

            for(int len = faceStoreList.size(); i < len; ++i) {
               result[i] = (Integer)faceStoreList.get(i);
            }

            return result;
         }
      }

      @SuppressWarnings("unchecked")
      private static <T> T getNumArrayOf(T[] pAry, Class<?> tc, int num) {
         return pAry[num] == null ? (pAry[num] = (T)Array.newInstance(tc.getComponentType(), num)) : pAry[num];
      }

      public static float[] parseFloatNums(int expectedNum, String floatNumsStr) {
         if (isEmptyStr(floatNumsStr)) {
            return null;
         } else {
            float[] returnArray = (float[])getNumArrayOf(floatAryAry, float[].class, expectedNum);
            int returnArrayCount = 0;
            FLOAT_LIST_MT.reset(floatNumsStr);

            while(FLOAT_LIST_MT.find()) {
               returnArray[returnArrayCount++] = Float.parseFloat(FLOAT_LIST_MT.group());
               if (returnArrayCount >= expectedNum) {
                  break;
               }
            }

            return returnArray;
         }
      }

      public static int[] parseIntNums(int expectedNum, String intNumsStr) {
         if (isEmptyStr(intNumsStr)) {
            return null;
         } else {
            int[] returnArray = (int[])getNumArrayOf(intAryAry, int[].class, expectedNum);
            int returnArrayCount = 0;
            INT_LIST_MT.reset(intNumsStr);

            while(INT_LIST_MT.find()) {
               returnArray[returnArrayCount++] = Integer.parseInt(INT_LIST_MT.group());
               if (returnArrayCount >= expectedNum) {
                  break;
               }
            }

            return returnArray;
         }
      }
   }
}
