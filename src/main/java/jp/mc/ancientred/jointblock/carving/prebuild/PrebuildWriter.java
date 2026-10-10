package jp.mc.ancientred.jointblock.carving.prebuild;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import jp.mc.ancientred.jointblock.JBModContainer;

public class PrebuildWriter implements Runnable {
   private static char LINESEP = '\n';
   private static char SP = ' ';
   public static Prebuilder kickWriteQueue;
   public static Prebuilder doneWriteQueue;
   private Prebuilder builder;

   public PrebuildWriter(Prebuilder argBuilder) {
      this.builder = argBuilder;
   }

   public static void runWorkThread() {
      PrebuildWriter writer = new PrebuildWriter(kickWriteQueue);
      kickWriteQueue = null;
      synchronized(writer.builder) {
         Thread thread = new Thread(writer);
         thread.start();
      }
   }

   public void run() {
      try {
         this.builder.mergeDupeVertices();
         this.builder.mergeEdgeByEdgeFaces();
         writeOutPrebuildData(this.builder);
      } catch (Exception var2) {
         this.builder.addMessage(var2.getMessage(), true);
         var2.printStackTrace();
      }

      doneWriteQueue = this.builder;
   }

   public static String sanitizeFileName(String fileName) {
      return fileName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
   }

   public static void writeOutPrebuildData(Prebuilder prebuilder) {
      BufferedWriter outObj = null;
      BufferedWriter outMtl = null;

      try {
         Calendar c = Calendar.getInstance();
         String outDateFolderStr = (new SimpleDateFormat("yyyy_MM_dd_HHmmssSSS")).format(c.getTime());
         File outputDir = new File(JBModContainer.configDir, "JointBlock" + File.separator + "objout" + File.separator + outDateFolderStr);
         if (!outputDir.exists() && !outputDir.mkdirs()) {
            prebuilder.addMessage("commands.jbbo.iofailed", true);
            return;
         }

         if (!outputDir.isDirectory()) {
            prebuilder.addMessage("commands.jbbo.iofailed", true);
            return;
         }

         if (outputDir.canWrite()) {
            String outFileStr = sanitizeFileName(prebuilder.outputFolderStr);
            File outputFileObj = new File(outputDir, outFileStr + ".obj");
            File outputFileMtl = new File(outputDir, outFileStr + ".mtl");
            outObj = createFile(outputFileObj);
            outMtl = createFile(outputFileMtl);
            if (outObj != null && outMtl != null) {
               outObj.append("# Written by JointBlockMod(Minecraft FanMade Mod)").append(LINESEP);
               outObj.append("mtllib ").append(outFileStr + ".mtl").append(LINESEP);
               writeDataObj(outObj, outMtl, prebuilder);
               outObj.flush();
               outMtl.flush();
               prebuilder.addMessage("commands.jbbo.success", false, outputFileObj.getAbsolutePath());
               return;
            }

            prebuilder.addMessage("commands.jbbo.iofailed", true);
            return;
         }

         prebuilder.addMessage("commands.jbbo.iofailed", true);
      } catch (Exception var30) {
         prebuilder.addMessage(var30.getClass().getName(), true);
         return;
      } finally {
         if (outObj != null) {
            try {
               outObj.close();
            } catch (Exception var29) {
            }
         }

         if (outMtl != null) {
            try {
               outMtl.close();
            } catch (Exception var28) {
            }
         }

      }

   }

   private static BufferedWriter createFile(File outputFileObj) throws Exception {
      if (outputFileObj.exists()) {
         return null;
      } else {
         return !outputFileObj.createNewFile() ? null : new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileObj, false), "UTF-8"));
      }
   }

   private static void writeDataObj(Writer outObj, Writer outMtl, Prebuilder prebuilder) throws Exception {
      StringBuilder matNameBld = new StringBuilder();
      DecimalFormat matDColFmt = new DecimalFormat("0.0##");
      DecimalFormat vertFmt = new DecimalFormat("0.0#####");
      ArrayList<PbCarvingCell.PbCellVert> vertices = prebuilder.vertices;
      int m = 0;
      int vCnt = 1;

      for(int len = vertices.size(); m < len; ++m) {
         PbCarvingCell.PbCellVert vert = (PbCarvingCell.PbCellVert)vertices.get(m);
         vert.holderFaces.clear();
         if (vert.vertIdx_and_flg != -2) {
            vert.vertIdx_and_flg = vCnt++;
            outObj.append("v");
            outObj.append(SP).append(vertFmt.format((double)vert.tx));
            outObj.append(SP).append(vertFmt.format((double)vert.ty));
            outObj.append(SP).append(vertFmt.format((double)vert.tz)).append(LINESEP);
         }
      }

      outObj.append("s off").append(LINESEP);

      for(m = 0; m < 2; ++m) {
         PbCarvingCell.PbWFObjGroup[] groupList = m == 0 ? prebuilder.groupListF3 : prebuilder.groupListF4;
         String groupSuffix = m == 0 ? "F3" : "F4";

         for(int i = 0; i < groupList.length; ++i) {
            PbCarvingCell.PbWFObjGroup wfGroup = groupList[i];
            if (wfGroup != null && wfGroup.faceFirst != null) {
               matNameBld.setLength(0);
               matNameBld.append("mtlPal").append(String.valueOf(wfGroup.palIdx)).append("Col").append(String.valueOf(wfGroup.colIdx));
               outObj.append("usemtl ").append(matNameBld).append(LINESEP);

               for(PbCarvingCell.PbCellFace targetFace = wfGroup.faceFirst; targetFace != null; targetFace = targetFace.nextFaceRef) {
                  if (!targetFace.isDecimated) {
                     writeFace(outObj, targetFace, 0);
                  }
               }
            }
         }
      }

      for(m = 0; m < prebuilder.matList.length; ++m) {
         PbCarvingCell.PbWFMaterial material = prebuilder.matList[m];
         if (material != null) {
            matNameBld.setLength(0);
            matNameBld.append("mtlPal").append(String.valueOf(material.palIdx)).append("Col").append(String.valueOf(material.colIdx));
            outMtl.append("newmtl ").append(matNameBld).append(LINESEP);
            outMtl.append("Kd");
            outMtl.append(SP).append(matDColFmt.format((double)material.colorR / 255.0));
            outMtl.append(SP).append(matDColFmt.format((double)material.colorG / 255.0));
            outMtl.append(SP).append(matDColFmt.format((double)material.colorB / 255.0)).append(LINESEP);
         }
      }

   }

   private static void writeFace(Writer outObj, PbCarvingCell.PbCellFace targetFace, int depth) throws IOException {
      PbCarvingCell.PbCellFace actualFace = targetFace.getActualFaceFirst();

      int fv;
      for(fv = 0; fv < depth; ++fv) {
         outObj.append(" ");
      }

      outObj.append("f");

      for(fv = 0; fv < actualFace.vertIdxCnt; ++fv) {
         PbCarvingCell.PbCellVert targetVert = actualFace.vertices[fv];
         outObj.append(SP).append(String.valueOf(targetVert.vertIdx_and_flg));
      }

      outObj.append(LINESEP);
   }

   private static void writeFaceDbg(Writer outObj, PbCarvingCell.PbCellFace targetFace, int depth) throws IOException {
      if (targetFace != null) {
         PbCarvingCell.PbCellFace actualFace = targetFace.getActualFaceFirst();

         for(PbCarvingCell.PbCellFace holes = actualFace; holes != null; holes = holes.nextSubLinkRef) {
            outObj.append(LINESEP);
            outObj.append("f");

            for(int fv = 0; fv < holes.vertIdxCnt; ++fv) {
               PbCarvingCell.PbCellVert targetVert = holes.vertices[fv];
               outObj.append(SP).append(String.valueOf(targetVert.vertIdx_and_flg));
            }
         }

         outObj.append(LINESEP);
      }
   }
}
