package jp.mc.ancientred.jointblock.carving;

import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingUtil;
import jp.mc.ancientred.jointblock.carving.pattern.PatternValueCopyType;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;

public class JBCarvingDataUtil {
   private static final boolean isByteOrderLE;
   public static final int MIN_CARVING_DIV = 1;
   public static final int CARVING_TYPE_BOXES = 0;
   public static final int CARVING_TYPE_PATRN = 2;
   public static final int CARVING_TYPE_MCUBE = 4;
   public static final int MIN_MCUBE_DIM = 2;
   public static final int MAX_MCUBE_DIM = 5;
   public static final int MAX_PATRN_DIM = 5;
   public static final int TOGGLE_PATTERN_ID = -1;
   public static final int TOGGLE_PATTERN_SIDE = -2;
   public static final int TOGGLE_PATTERN_FACE = -3;
   public static final int TOGGLE_PATTERN_TURN3to4 = -4;
   public static final int TOGGLE_PATTERN_TURN0toX = -5;
   public static final int TOGGLE_PATTERN_FLIPXZ = -6;
   public static final int TOGGLE_PATTERN_FLIPY = -7;

   public static boolean isCarvingBoxes(int[] carvingData) {
      return carvingData != null && getCarvingType(carvingData) == 0;
   }

   public static boolean isCarvingMCube(int[] carvingData) {
      return carvingData != null && getCarvingType(carvingData) == 4;
   }

   public static boolean isCarvingPattern(int[] carvingData) {
      return carvingData != null && getCarvingType(carvingData) == 2;
   }

   public static int getCVDataLenFromDiv(int type, int carvingDiv) {
      switch (type) {
         case 2:
            return (carvingDiv * carvingDiv * carvingDiv * 8 + 31) / 32 + 1;
         case 4:
            return (carvingDiv * carvingDiv * carvingDiv * 8 + 31) / 32 + 1;
         default:
            return (carvingDiv * carvingDiv * carvingDiv * 4 + 31) / 32 + 1;
      }
   }

   public static int[] createCarvingData(int type, int carvingDiv) {
      switch (type) {
         case 2:
            return createCarvingDataPattern(carvingDiv);
         case 4:
            return createCarvingDataMcube(carvingDiv);
         default:
            return createCarvingDataBoxes(carvingDiv);
      }
   }

   public static int[] createCarvingDataBoxes(int carvingDiv) {
      int len = getCVDataLenFromDiv(0, carvingDiv);
      int[] carvingData = new int[len];
      carvingData[len - 1] = 0 | carvingDiv;
      --len;

      for(int i = 0; i < len; ++i) {
         carvingData[i] = 2004318071;
      }

      organizeBoxesCarveData(carvingData);
      return carvingData;
   }

   public static int[] createCarvingDataMcube(int carvingDiv) {
      int len = getCVDataLenFromDiv(4, carvingDiv);
      int[] carvingData = new int[len];
      carvingData[len - 1] = 16384 | carvingDiv;
      --len;

      for(int i = 0; i < len; ++i) {
         carvingData[i] = 522133279;
      }

      return carvingData;
   }

   public static int[] createCarvingDataPattern(int carvingDiv) {
      int len = getCVDataLenFromDiv(2, carvingDiv);
      ++len;
      int[] carvingData = new int[len];
      carvingData[len - 1] = 8192 | carvingDiv;
      len -= 2;

      for(int i = 0; i < len; ++i) {
         carvingData[i] = 1886417008;
      }

      return organizePatternCarveData(carvingData);
   }

   public static int getCarvingType(int[] carvingData) {
      if (carvingData != null && carvingData.length > 0) {
         int type = (carvingData[carvingData.length - 1] & '\uf000') >> 12;
         switch (type) {
            case 2:
               return 2;
            case 4:
               return 4;
            default:
               return 0;
         }
      } else {
         return 0;
      }
   }

   public static int getCarvingDivide(int[] carvingData) {
      return carvingData != null && carvingData.length > 0 ? carvingData[carvingData.length - 1] & 255 : 0;
   }

   public static boolean validateCarvingData(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount >= 1 && divideCount <= 16) {
         int len = getCVDataLenFromDiv(type, divideCount);
         switch (type) {
            case 2:
               if (divideCount > 5) {
                  return false;
               }

               return carvingData.length >= len && carvingData.length <= len + 8;
            case 4:
               if (divideCount > 5) {
                  return false;
               }

               return carvingData.length == len;
            default:
               return carvingData.length == len;
         }
      } else {
         return false;
      }
   }

   public static int getPaletteIndex(int[] carvingData) {
      if (carvingData != null && carvingData.length > 0) {
         int pdataIndex = (carvingData[carvingData.length - 1] & 3840) >> 8;
         if (pdataIndex >= 0 && pdataIndex < 6) {
            return pdataIndex;
         }
      }

      return 0;
   }

   public static void setPaletteIndexNext(int[] carvingData) {
      if (carvingData != null && carvingData.length > 0) {
         int pdataIndex = ((carvingData[carvingData.length - 1] & 3840) >> 8) + 1;
         if (pdataIndex < 0 || pdataIndex >= 6) {
            pdataIndex = 0;
         }

         carvingData[carvingData.length - 1] &= -3841;
         carvingData[carvingData.length - 1] |= pdataIndex << 8;
      }

   }

   public static void setPaletteIndexPrev(int[] carvingData) {
      if (carvingData != null && carvingData.length > 0) {
         int pdataIndex = ((carvingData[carvingData.length - 1] & 3840) >> 8) - 1;
         if (pdataIndex < 0 || pdataIndex >= 6) {
            pdataIndex = 5;
         }

         carvingData[carvingData.length - 1] &= -3841;
         carvingData[carvingData.length - 1] |= pdataIndex << 8;
      }

   }

   public static void modifyBoxedCarveData(TileEntityJoint te, int boxIndex, int value, Player player) {
      int[] carvingData = te.carvingData;
      int index = boxIndex >> 3;
      if (index >= 0 && index < carvingData.length) {
         int mod = (boxIndex & 7) << 2;
         int data = carvingData[index] & ~(15 << mod);
         data |= (value & 15) << mod;
         carvingData[index] = data;
         organizeBoxesCarveData(carvingData);
         te.markUpdateForCarvingDataMod(player);
         te.isCarvingCompiled = false;
      }

   }

   public static void modifyStickerCarveData(TileEntityJoint te, int boxIndex, int value, boolean toggleEnable, Player player) {
      modifyMCubeCarveData(te, boxIndex, value, toggleEnable, player);
   }

   public static void modifyMCubeCarveData(TileEntityJoint te, int boxIndex, int value, boolean toggleEnable, Player player) {
      int[] carvingData = te.carvingData;
      int index = boxIndex >> 2;
      if (index >= 0 && index < carvingData.length) {
         int mod = (boxIndex & 3) << 3;
         int data = carvingData[index] & ~(255 << mod);
         int nowDensity = carvingData[index] >> mod & 31;
         int nowColor = carvingData[index] >> mod & 192;
         int nowDisplay = carvingData[index] >> mod & 32;
         if (toggleEnable) {
            if (value > 0) {
               nowDisplay = 0;
               nowDensity = 31;
            } else {
               nowDisplay = 32;
               nowDensity = 0;
            }
         } else {
            if (nowDensity == 0 && value < 0) {
               nowDisplay = 32;
            } else if (value > 0) {
               nowDisplay = 0;
            }

            nowDensity += value;
            nowDensity = Math.min(31, nowDensity);
            nowDensity = Math.max(0, nowDensity);
         }

         data |= (nowDisplay | nowColor | nowDensity & 31) << mod;
         carvingData[index] = data;
         te.markUpdateForCarvingDataMod(player);
         te.isCarvingCompiled = false;
      }

   }

   public static void fillCarveData(int type, TileEntityJoint te, int boxIndex, int hitSide, Player player) {
      if (hitSide >= 0 && hitSide <= 5) {
         int facing = te.getJointFacing();
         int divideCount = getCarvingDivide(te.carvingData);
         int unit;
         switch (hitSide) {
            case 0:
               unit = divideCount * divideCount;
               if (boxIndex / unit % divideCount != 0) {
                  boxIndex -= unit;
               }
               break;
            case 1:
               unit = divideCount * divideCount;
               if (boxIndex / unit % divideCount != divideCount - 1) {
                  boxIndex += unit;
               }
               break;
            case 2:
               if (boxIndex / divideCount % divideCount != 0) {
                  boxIndex -= divideCount;
               }
               break;
            case 3:
               if (boxIndex / divideCount % divideCount != divideCount - 1) {
                  boxIndex += divideCount;
               }
               break;
            case 4:
               unit = 1;
               if (boxIndex / unit % divideCount != 0) {
                  boxIndex -= unit;
               }
               break;
            case 5:
               unit = 1;
               if (boxIndex / unit % divideCount != divideCount - 1) {
                  boxIndex += unit;
               }
               break;
            default:
               return;
         }

         if (boxIndex >= 0 && boxIndex < divideCount * divideCount * divideCount) {
            switch (type) {
               case 2:
                  modifyPatternCarveData(te, boxIndex, 7, 0, true, true, player);
                  break;
               case 4:
                  modifyMCubeCarveData(te, boxIndex, 1, true, player);
                  break;
               default:
                  modifyBoxedCarveData(te, boxIndex, 7, player);
            }
         }

      }
   }

   public static void organizeBoxesCarveData(int[] carvingData) {
      int carvingDiv = getCarvingDivide(carvingData);
      if (carvingDiv >= 1) {
         int divCountPow = carvingDiv * carvingDiv;
         int lastCnt = divCountPow * carvingDiv - 1;
         int lastMod = lastCnt & 7;
         int boxIndex;
         if (lastMod != 7) {
            int lastIndex = lastCnt >> 3;

            for(boxIndex = lastMod + 1; boxIndex < 8; ++boxIndex) {
               carvingData[lastIndex] &= ~(15 << (boxIndex << 2));
            }
         }

         boolean hasAtLeastOne = false;
         boxIndex = carvingData.length - 1;

         int yCalc;
         for(yCalc = 0; yCalc < boxIndex; ++yCalc) {
            carvingData[yCalc] &= 2004318071;
            hasAtLeastOne |= carvingData[yCalc] != 0;
         }

         if (!hasAtLeastOne) {
            boxIndex = carvingData.length - 1;

            for(yCalc = 0; yCalc < boxIndex; ++yCalc) {
               carvingData[yCalc] = 2004318071;
            }
         }

         int divideCountSub1 = carvingDiv - 1;

         for(int y = 1; y < divideCountSub1; ++y) {
            yCalc = divCountPow * y;

            for(int z = 1; z < divideCountSub1; ++z) {
               int zCalc = carvingDiv * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 1; x < divideCountSub1; ++x) {
                  boxIndex = x + yPzCalc;
                  int boxIndexCntNeighbour = boxIndex + divCountPow;
                  int col = carvingData[boxIndexCntNeighbour >> 3] >>> ((boxIndexCntNeighbour & 7) << 2) & 7;
                  if (col != 0) {
                     boxIndexCntNeighbour = boxIndex - divCountPow;
                     col = carvingData[boxIndexCntNeighbour >> 3] >>> ((boxIndexCntNeighbour & 7) << 2) & 7;
                     if (col != 0) {
                        boxIndexCntNeighbour = boxIndex + carvingDiv;
                        col = carvingData[boxIndexCntNeighbour >> 3] >>> ((boxIndexCntNeighbour & 7) << 2) & 7;
                        if (col != 0) {
                           boxIndexCntNeighbour = boxIndex - carvingDiv;
                           col = carvingData[boxIndexCntNeighbour >> 3] >>> ((boxIndexCntNeighbour & 7) << 2) & 7;
                           if (col != 0) {
                              boxIndexCntNeighbour = boxIndex + 1;
                              col = carvingData[boxIndexCntNeighbour >> 3] >>> ((boxIndexCntNeighbour & 7) << 2) & 7;
                              if (col != 0) {
                                 boxIndexCntNeighbour = boxIndex - 1;
                                 col = carvingData[boxIndexCntNeighbour >> 3] >>> ((boxIndexCntNeighbour & 7) << 2) & 7;
                                 if (col != 0) {
                                    carvingData[boxIndex >> 3] |= 8 << ((boxIndex & 7) << 2);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public static int getPatternValueForBoxIndex(int[] carvingData, int boxIndex) {
      int index = boxIndex >> 2;
      if (index >= 0 && index < carvingData.length) {
         int mod = (boxIndex & 3) << 3;
         int pattern = carvingData[index] >> mod & 15;
         return getPatternValueFromTable(carvingData, pattern);
      } else {
         return -1;
      }
   }

   public static int getPatternValueFromTable(int[] carvingData, int patternIdx) {
      if (patternIdx == 0) {
         return 0;
      } else {
         int dataLen = getCVDataLenFromDiv(2, getCarvingDivide(carvingData));
         int actualLen = dataLen - 1;

         for(int idx = actualLen; idx > 0 && idx < carvingData.length - 1; ++idx) {
            int combined = carvingData[idx];
            int firstTableIdx = idx - actualLen << 1;
            int first = combined & '\uffff' | (carvingData[actualLen] >> firstTableIdx & 1) << 16;
            int secondTabelIdx = (idx - actualLen << 1) + 1;
            int second = combined >> 16 & '\uffff' | (carvingData[actualLen] >> secondTabelIdx & 1) << 16;
            if (patternIdx == firstTableIdx) {
               return first;
            }

            if (patternIdx == secondTabelIdx) {
               return second;
            }
         }

         return -1;
      }
   }

   public static void togglePatternCarveData(TileEntityJoint te, int boxIndex, int argsNum, int toggleDir, Player player) {
      int[] carvingData = te.carvingData;
      int index = boxIndex >> 2;
      if (index >= 0 && index < carvingData.length) {
         int mod = (boxIndex & 3) << 3;
         int pattern = carvingData[index] >> mod & 15;
         int valuePattern = getPatternValueFromTable(carvingData, pattern);
         if (valuePattern != -1) {
            int facingTwdPlayer;
            if (argsNum == -1) {
               facingTwdPlayer = getFixedPlayerTwardsFace(te, player);
               valuePattern = JBPatternCarvingUtil.getNextPatternDefaultValue(valuePattern, toggleDir, facingTwdPlayer);
            } else if (argsNum == -2) {
               valuePattern = JBPatternCarvingUtil.turnPatternSide(valuePattern, 1);
            } else if (argsNum == -3) {
               valuePattern = JBPatternCarvingUtil.turnPatternFace(valuePattern, 1);
            } else if (argsNum == -4) {
               valuePattern = JBPatternCarvingUtil.copyValue(valuePattern, PatternValueCopyType.rot3to4);
            } else if (argsNum == -5) {
               facingTwdPlayer = getFixedPlayerTwardsFace(te, player);
               switch (facingTwdPlayer) {
                  case 2:
                     valuePattern = JBPatternCarvingUtil.copyValue(valuePattern, PatternValueCopyType.rot0to3);
                     break;
                  case 3:
                     valuePattern = JBPatternCarvingUtil.copyValue(valuePattern, PatternValueCopyType.rot0to2);
                     break;
                  case 4:
                     valuePattern = JBPatternCarvingUtil.copyValue(valuePattern, PatternValueCopyType.rot0to5);
                     break;
                  case 5:
                     valuePattern = JBPatternCarvingUtil.copyValue(valuePattern, PatternValueCopyType.rot0to4);
               }
            } else if (argsNum == -6) {
               facingTwdPlayer = getFixedPlayerTwardsFace(te, player);
               switch (facingTwdPlayer) {
                  case 2:
                  case 3:
                     valuePattern = JBPatternCarvingUtil.copyValue(valuePattern, PatternValueCopyType.flipX);
                     break;
                  case 4:
                  case 5:
                     valuePattern = JBPatternCarvingUtil.copyValue(valuePattern, PatternValueCopyType.flipZ);
               }
            } else if (argsNum == -7) {
               valuePattern = JBPatternCarvingUtil.copyValue(valuePattern, PatternValueCopyType.flipY);
            } else {
               valuePattern = JBPatternCarvingUtil.modifyPatternArgs(argsNum, valuePattern, toggleDir);
            }

            modifyPatternCarveData(te, boxIndex, 0, valuePattern, false, true, player);
         } else {
            modifyPatternCarveData(te, boxIndex, 0, 0, false, true, player);
         }
      }

   }

   private static int getFixedPlayerTwardsFace(TileEntityJoint te, Player player) {
      int facingTwdPlayer = JBRotationUtil.getPlayerFacingOpposite(player);
      int baseFacing = te.getJointFacing();
      if (baseFacing <= 1 || baseFacing >= 6) {
         baseFacing = 3;
      }

      facingTwdPlayer = JBRotationUtil.rotateSideOrFacingBasedOnBaseFacing(baseFacing, facingTwdPlayer);
      return facingTwdPlayer;
   }

   public static void modifyPatternCarveData(TileEntityJoint te, int boxIndex, int valueColor, int valuePattern, boolean updateColor, boolean updatePattern, Player player) {
      int[] carvingData = te.carvingData;
      int index = boxIndex >> 2;
      if (index >= 0 && index < carvingData.length) {
         int mod = (boxIndex & 3) << 3;
         int data = carvingData[index];
         if (updateColor) {
            data &= ~(240 << mod);
            data |= (valueColor & 15) << 4 << mod;
         }

         if (updatePattern) {
            data &= ~(15 << mod);
            valuePattern &= 131071;
            if (valuePattern != 0) {
               int matchRes = getMatchedPatternIndex(carvingData, valuePattern);
               if (matchRes >= 0) {
                  data |= (matchRes & 15) << mod;
               } else {
                  int usedTableNum = getUsedPatternTableNum(carvingData);
                  if (usedTableNum <= 15) {
                     carvingData = addNewPatternToCarveData(carvingData, usedTableNum, valuePattern);
                     data |= (usedTableNum & 15) << mod;
                  }
               }
            }
         }

         carvingData[index] = data;
         te.carvingData = organizePatternCarveData(carvingData);
         te.markUpdateForCarvingDataMod(player);
         te.isCarvingCompiled = false;
      }

   }

   private static int getMatchedPatternIndex(int[] carvingData, int valuePattern) {
      if (valuePattern == 0) {
         return 0;
      } else {
         int dataLen = getCVDataLenFromDiv(2, getCarvingDivide(carvingData));
         int actualLen = dataLen - 1;

         for(int idx = actualLen; idx > 0 && idx < carvingData.length - 1; ++idx) {
            int combined = carvingData[idx];
            int firstTableIdx = idx - actualLen << 1;
            int first = combined & '\uffff' | (carvingData[actualLen] >> firstTableIdx & 1) << 16;
            int secondTabelIdx = (idx - actualLen << 1) + 1;
            int second = combined >> 16 & '\uffff' | (carvingData[actualLen] >> secondTabelIdx & 1) << 16;
            if (first != 0 && valuePattern == first) {
               return firstTableIdx;
            }

            if (second != 0 && valuePattern == second) {
               return secondTabelIdx;
            }
         }

         return -1;
      }
   }

   private static int[] addNewPatternToCarveData(int[] carvingData, int newPatternIndex, int valuePattern) {
      int dataLen = getCVDataLenFromDiv(2, getCarvingDivide(carvingData));
      int actualLen = dataLen - 1;
      carvingData = reCreatePCarvingDataIfNeed(carvingData, actualLen + newPatternIndex / 2 + 1 + 1);
      int targetIndex = actualLen + newPatternIndex / 2;
      int mod = (newPatternIndex & 1) << 4;
      carvingData[targetIndex] &= ~('\uffff' << mod);
      carvingData[targetIndex] |= (valuePattern & '\uffff') << mod;
      carvingData[actualLen] &= ~(1 << newPatternIndex);
      carvingData[actualLen] |= (valuePattern >> 16 & 1) << newPatternIndex;
      return carvingData;
   }

   private static int[] reCreatePCarvingDataIfNeed(int[] carvingData, int newTotalLen) {
      if (carvingData.length != newTotalLen) {
         int[] newCarvingData = new int[newTotalLen];
         System.arraycopy(carvingData, 0, newCarvingData, 0, newTotalLen - 1);
         newCarvingData[newCarvingData.length - 1] = carvingData[carvingData.length - 1];
         carvingData = newCarvingData;
      }

      return carvingData;
   }

   public static int getUsedPatternTableNum(int[] carvingData) {
      int dataLen = getCVDataLenFromDiv(2, getCarvingDivide(carvingData));
      int actualLen = dataLen - 1;
      int patternTableLen = carvingData.length - dataLen;
      if (patternTableLen <= 0) {
         return 0;
      } else {
         int tableLastIndex = actualLen + patternTableLen - 1;
         if (tableLastIndex >= 0 && tableLastIndex < carvingData.length - 1) {
            int secondTabelIdx = (tableLastIndex - actualLen << 1) + 1;
            int second = carvingData[tableLastIndex] >> 16 & '\uffff' | (carvingData[actualLen] >> secondTabelIdx & 1) << 16;
            return second != 0 ? patternTableLen << 1 : (patternTableLen << 1) - 1;
         } else {
            return 1;
         }
      }
   }

   public static int[] organizePatternCarveData(int[] carvingData) {
      int carvingDiv = getCarvingDivide(carvingData);
      int dataLen = getCVDataLenFromDiv(2, carvingDiv);
      int actualLen = dataLen - 1;
      if (carvingDiv >= 1 && actualLen <= carvingData.length) {
         int divCountPow = carvingDiv * carvingDiv;
         int lastCnt = divCountPow * carvingDiv - 1;
         int lastMod = lastCnt & 3;
         int divideCountSub1;
         if (lastMod != 3) {
            int lastIndex = lastCnt >> 2;

            for(divideCountSub1 = lastMod + 1; divideCountSub1 < 4; ++divideCountSub1) {
               carvingData[lastIndex] &= ~(255 << (divideCountSub1 << 3));
            }
         }

         boolean hasAtLeastOne = false;
         divideCountSub1 = actualLen;

         int y;
         for(y = 0; y < divideCountSub1; ++y) {
            carvingData[y] &= 2139062143;
            hasAtLeastOne |= (carvingData[y] & -252645136) != 0;
         }

         if (!hasAtLeastOne) {
            divideCountSub1 = getCVDataLenFromDiv(2, carvingDiv);
            ++divideCountSub1;
            carvingData = new int[divideCountSub1];
            carvingData[divideCountSub1 - 1] = 8192 | carvingDiv;
            divideCountSub1 -= 2;

            for(y = 0; y < divideCountSub1; ++y) {
               carvingData[y] = 1886417008;
            }
         } else {
            carvingData = shrinkPatternTable(carvingData);
         }

         divideCountSub1 = carvingDiv - 1;

         for(y = 1; y < divideCountSub1; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 1; z < divideCountSub1; ++z) {
               int zCalc = carvingDiv * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 1; x < divideCountSub1; ++x) {
                  int boxIndex = x + yPzCalc;
                  int boxIndexCntNeighbour = boxIndex + divCountPow;
                  int dat = carvingData[boxIndexCntNeighbour >> 2] >>> ((boxIndexCntNeighbour & 3) << 3) & 255;
                  int col = dat & 112;
                  int pat = dat & 15;
                  if (dat != 0 && pat == 0) {
                     boxIndexCntNeighbour = boxIndex - divCountPow;
                     dat = carvingData[boxIndexCntNeighbour >> 2] >>> ((boxIndexCntNeighbour & 3) << 3) & 255;
                     col = dat & 112;
                     pat = dat & 15;
                     if (dat != 0 && pat == 0) {
                        boxIndexCntNeighbour = boxIndex + carvingDiv;
                        dat = carvingData[boxIndexCntNeighbour >> 2] >>> ((boxIndexCntNeighbour & 3) << 3) & 255;
                        col = dat & 112;
                        pat = dat & 15;
                        if (dat != 0 && pat == 0) {
                           boxIndexCntNeighbour = boxIndex - carvingDiv;
                           dat = carvingData[boxIndexCntNeighbour >> 2] >>> ((boxIndexCntNeighbour & 3) << 3) & 255;
                           col = dat & 112;
                           pat = dat & 15;
                           if (dat != 0 && pat == 0) {
                              boxIndexCntNeighbour = boxIndex + 1;
                              dat = carvingData[boxIndexCntNeighbour >> 2] >>> ((boxIndexCntNeighbour & 3) << 3) & 255;
                              col = dat & 112;
                              pat = dat & 15;
                              if (dat != 0 && pat == 0) {
                                 boxIndexCntNeighbour = boxIndex - 1;
                                 dat = carvingData[boxIndexCntNeighbour >> 2] >>> ((boxIndexCntNeighbour & 3) << 3) & 255;
                                 col = dat & 112;
                                 pat = dat & 15;
                                 if (dat != 0 && pat == 0) {
                                    carvingData[boxIndex >> 2] |= 128 << ((boxIndex & 3) << 3);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return carvingData;
      } else {
         return carvingData;
      }
   }

   private static int[] shrinkPatternTable(int[] carvingData) {
      int carvingDiv = getCarvingDivide(carvingData);
      int divCountPow = carvingDiv * carvingDiv;
      int dataLen = getCVDataLenFromDiv(2, carvingDiv);
      int actualLen = dataLen - 1;
      int patternUsed = 0;

      int usedPatternsCnt;
      int y;
      int yCalc;
      int z;
      int yPzCalc;
      int x;
      int boxIndex;
      for(usedPatternsCnt = 0; usedPatternsCnt < carvingDiv; ++usedPatternsCnt) {
         yCalc = divCountPow * usedPatternsCnt;

         for(z = 0; z < carvingDiv; ++z) {
            yPzCalc = yCalc + carvingDiv * z;

            for(x = 0; x < carvingDiv; ++x) {
               boxIndex = x + yPzCalc;
               int dat = carvingData[boxIndex >> 2] >>> ((boxIndex & 3) << 3) & 255;
               int col = dat & 112;
               int pat = dat & 15;
               if (col != 0) {
                  patternUsed |= 1 << pat;
               }
            }
         }
      }

      usedPatternsCnt = 1;
      int[] usedPatterns = new int[16];
      int[] beforeAfter = new int[16];

      for(y = actualLen; y > 0 && y < carvingData.length - 1; ++y) {
         yCalc = y - actualLen << 1;
         if (yCalc != 0 && (patternUsed & 1 << yCalc) != 0) {
            usedPatterns[usedPatternsCnt] = carvingData[y] & '\uffff';
            beforeAfter[yCalc] = usedPatternsCnt++;
         }

         yCalc = (y - actualLen << 1) + 1;
         if ((patternUsed & 1 << yCalc) != 0) {
            usedPatterns[usedPatternsCnt] = carvingData[y] >> 16 & '\uffff';
            beforeAfter[yCalc] = usedPatternsCnt++;
         }
      }

      for(y = 1; y < usedPatternsCnt; ++y) {
         yCalc = actualLen + (y >> 1);
         if ((y & 1) == 0) {
            carvingData[yCalc] &= -65536;
            carvingData[yCalc] |= usedPatterns[y] & '\uffff';
         } else {
            carvingData[yCalc] &= 65535;
            carvingData[yCalc] |= (usedPatterns[y] & '\uffff') << 16;
         }

         carvingData[actualLen] &= ~(1 << y);
         carvingData[actualLen] |= (usedPatterns[y] >> 16 & 1) << y;
      }

      if ((usedPatternsCnt & 1) != 0) {
         carvingData[actualLen + (usedPatternsCnt - 1 >> 1)] &= 65535;
      }

      for(y = 0; y < carvingDiv; ++y) {
         yCalc = divCountPow * y;

         for(z = 0; z < carvingDiv; ++z) {
            yPzCalc = yCalc + carvingDiv * z;

            for(x = 0; x < carvingDiv; ++x) {
               boxIndex = x + yPzCalc;
               int mod = (boxIndex & 3) << 3;
               int bodIndexMod = boxIndex >> 2;
               int dat = carvingData[bodIndexMod] >> mod & 255;
               int col = dat & 112;
               int pat = dat & 15;
               pat = beforeAfter[pat];
               carvingData[bodIndexMod] &= ~(255 << mod);
               carvingData[bodIndexMod] |= (col | pat) << mod;
            }
         }
      }

      return reCreatePCarvingDataIfNeed(carvingData, actualLen + (usedPatternsCnt - 1) / 2 + 1 + 1);
   }

   public static String getPatternShortName(int valuePattern) {
      return JBPatternCarvingUtil.getPatternShortName(valuePattern);
   }

   public static void copyPatternTable(int[] carvingData, int[] copyData, PatternValueCopyType copyType) {
      int actualLen = getCVDataLenFromDiv(2, getCarvingDivide(carvingData)) - 1;

      for(int idx = actualLen; idx > 0 && idx < carvingData.length - 1; ++idx) {
         int combined = carvingData[idx];
         int firstTableIdx = idx - actualLen << 1;
         int first = combined & '\uffff' | (carvingData[actualLen] >> firstTableIdx & 1) << 16;
         first = JBPatternCarvingUtil.copyValue(first, copyType);
         copyData[idx] |= first & '\uffff';
         carvingData[actualLen] &= ~(1 << firstTableIdx);
         carvingData[actualLen] |= (first >> 16 & 1) << firstTableIdx;
         int secondTabelIdx = (idx - actualLen << 1) + 1;
         int second = combined >> 16 & '\uffff' | (carvingData[actualLen] >> secondTabelIdx & 1) << 16;
         second = JBPatternCarvingUtil.copyValue(second, copyType);
         copyData[idx] |= (second & '\uffff') << 16;
         carvingData[actualLen] &= ~(1 << secondTabelIdx);
         carvingData[actualLen] |= (second >> 16 & 1) << secondTabelIdx;
      }

   }

   private static void copyByType(int type, int[] carvingData, int[] copyData, int boxIndexSrc, int boxIndexDst) {
      int dat;
      switch (type) {
         case 2:
            dat = carvingData[boxIndexSrc >> 2] >>> ((boxIndexSrc & 3) << 3) & 255;
            copyData[boxIndexDst >> 2] |= dat << ((boxIndexDst & 3) << 3);
            break;
         case 4:
            dat = carvingData[boxIndexSrc >> 2] >> ((boxIndexSrc & 3) << 3) & 255;
            copyData[boxIndexDst >> 2] |= dat << ((boxIndexDst & 3) << 3);
            break;
         default:
            dat = carvingData[boxIndexSrc >> 3] >>> ((boxIndexSrc & 7) << 2) & 15;
            copyData[boxIndexDst >> 3] |= dat << ((boxIndexDst & 7) << 2);
      }

   }

   public static int[] flipCarveDataX(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int boxIndexCopy = divideCountSub1 - x + yPzCalc;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.flipX);
         }

         return copyData;
      }
   }

   public static int[] flipCarveDataZ(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int boxIndexCopy = x + (divideCountSub1 - z) * divideCount + yCalc;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.flipZ);
         }

         return copyData;
      }
   }

   public static int[] flipCarveDataY(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int boxIndexCopy = x + zCalc + (divideCountSub1 - y) * divCountPow;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.flipY);
         }

         return copyData;
      }
   }

   public static int[] flipCarveDataYofXYReversed(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int boxIndexCopy = divideCountSub1 - x + (divideCountSub1 - z) * divideCount + (divideCountSub1 - y) * divCountPow;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.flipYofXYZReversed);
         }

         return copyData;
      }
   }

   public static void rotateCarvingDataClockwize(TileEntityJoint te) {
      te.carvingData = rotateCarveData3to4(te.carvingData);
      te.isCarvingCompiled = false;
   }

   public static void rotateCarvingData(TileEntityJoint te, Player entityPlayer) {
      int facing = te.getJointFacing();
      int yawDiv = ((Mth.floor((double)(entityPlayer.getYRot() * 4.0F / 360.0F) + 0.5) & 3) + 2) % 4;
      int playerOpposite = 3;
      if (yawDiv == 0) {
         playerOpposite = 3;
      }

      if (yawDiv == 1) {
         playerOpposite = 4;
      }

      if (yawDiv == 2) {
         playerOpposite = 2;
      }

      if (yawDiv == 3) {
         playerOpposite = 5;
      }

      boolean flipX = false;
      label49:
      switch (facing) {
         case 2:
            switch (playerOpposite) {
               case 2:
                  te.carvingData = rotateCarveData0to2(te.carvingData);
                  break label49;
               case 3:
                  te.carvingData = rotateCarveData0to3(te.carvingData);
                  break label49;
               case 4:
                  te.carvingData = rotateCarveData0to4(te.carvingData);
                  break label49;
               case 5:
                  te.carvingData = rotateCarveData0to5(te.carvingData);
               default:
                  break label49;
            }
         case 3:
            switch (playerOpposite) {
               case 2:
                  te.carvingData = rotateCarveData0to3(te.carvingData);
                  break label49;
               case 3:
                  te.carvingData = rotateCarveData0to2(te.carvingData);
                  break label49;
               case 4:
                  te.carvingData = rotateCarveData0to5(te.carvingData);
                  break label49;
               case 5:
                  te.carvingData = rotateCarveData0to4(te.carvingData);
               default:
                  break label49;
            }
         case 4:
            switch (playerOpposite) {
               case 2:
                  te.carvingData = rotateCarveData0to5(te.carvingData);
                  break label49;
               case 3:
                  te.carvingData = rotateCarveData0to4(te.carvingData);
                  break label49;
               case 4:
                  te.carvingData = rotateCarveData0to2(te.carvingData);
                  break label49;
               case 5:
                  te.carvingData = rotateCarveData0to3(te.carvingData);
               default:
                  break label49;
            }
         case 5:
            switch (playerOpposite) {
               case 2:
                  te.carvingData = rotateCarveData0to4(te.carvingData);
                  break;
               case 3:
                  te.carvingData = rotateCarveData0to5(te.carvingData);
                  break;
               case 4:
                  te.carvingData = rotateCarveData0to3(te.carvingData);
                  break;
               case 5:
                  te.carvingData = rotateCarveData0to2(te.carvingData);
            }
      }

      te.markUpdateForCarvingDataMod(entityPlayer);
      te.isCarvingCompiled = false;
   }

   public static int[] rotateCarveData3to4(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int tX = divideCountSub1 - z;
                  int boxIndexCopy = tX + x * divideCount + y * divCountPow;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.rot3to4);
         }

         return copyData;
      }
   }

   public static int[] rotateCarveData0to2(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int tY = divideCountSub1 - z;
                  int boxIndexCopy = x + y * divideCount + tY * divCountPow;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.rot0to2);
         }

         return copyData;
      }
   }

   public static int[] rotateCarveData0to3(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int tZ = divideCountSub1 - y;
                  int boxIndexCopy = x + tZ * divideCount + z * divCountPow;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.rot0to3);
         }

         return copyData;
      }
   }

   public static int[] rotateCarveData0to4(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int tY = divideCountSub1 - x;
                  int boxIndexCopy = y + z * divideCount + tY * divCountPow;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.rot0to4);
         }

         return copyData;
      }
   }

   public static int[] rotateCarveData0to5(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;
         int[] copyData = new int[carvingData.length];
         copyData[copyData.length - 1] = carvingData[carvingData.length - 1];

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int tX = divideCountSub1 - y;
                  int boxIndexCopy = tX + z * divideCount + x * divCountPow;
                  copyByType(type, carvingData, copyData, boxIndex, boxIndexCopy);
               }
            }
         }

         if (type == 2) {
            copyPatternTable(carvingData, copyData, PatternValueCopyType.rot0to5);
         }

         return copyData;
      }
   }

   public static int[] convertTypeCubeToTypePattern(int[] carvingData) {
      int type = getCarvingType(carvingData);
      int divideCount = getCarvingDivide(carvingData);
      if (divideCount < 1) {
         return carvingData;
      } else {
         int[] newPatternCV = createCarvingData(2, divideCount);
         int divCountPow = divideCount * divideCount;
         int divideCountSub1 = divideCount - 1;

         for(int y = 0; y < divideCount; ++y) {
            int yCalc = divCountPow * y;

            for(int z = 0; z < divideCount; ++z) {
               int zCalc = divideCount * z;
               int yPzCalc = yCalc + zCalc;

               for(int x = 0; x < divideCount; ++x) {
                  int boxIndex = x + yPzCalc;
                  int dat = carvingData[boxIndex >> 3] >>> ((boxIndex & 7) << 2) & 15;
                  newPatternCV[boxIndex >> 2] &= ~(255 << ((boxIndex & 3) << 3));
                  newPatternCV[boxIndex >> 2] |= (dat & 7) << 4 << ((boxIndex & 3) << 3);
               }
            }
         }

         return organizePatternCarveData(newPatternCV);
      }
   }

   public static final void writePaletteDataToBBuf(int[][] paletteData, ByteBuf bbuf) {
      int sendCount = 0;

      int i;
      for(i = 0; i < paletteData.length; ++i) {
         if (paletteData[i] != null && paletteData[i].length == 8) {
            ++sendCount;
         }
      }

      bbuf.writeByte(sendCount);

      for(i = 0; i < paletteData.length; ++i) {
         if (paletteData[i] != null && paletteData[i].length == 8) {
            bbuf.writeByte((byte)i);

            for(int j = 0; j < 8; ++j) {
               bbuf.writeInt(paletteData[i][j]);
            }
         }
      }

   }

   public static final void readPaletteDataFromBBuf(int[][] paletteData, ByteBuf bbuf) {
      for(int j = 0; j < paletteData.length; ++j) {
         paletteData[j] = null;
      }

      int sendCount = bbuf.readByte();
      if (sendCount > 6) {
         throw new RuntimeException("Illegal Packet");
      } else {
         for(int i = 0; i < sendCount; ++i) {
            int index = bbuf.readByte();
            int j;
            if (index >= 0 && index < paletteData.length) {
               paletteData[index] = new int[8];

               for(j = 0; j < 8; ++j) {
                  paletteData[index][j] = bbuf.readInt();
               }
            } else {
               for(j = 0; j < 8; ++j) {
                  bbuf.readInt();
               }
            }
         }

      }
   }

   public static final void setPaletteDataToNBT(int[][] paletteData, String tagName, CompoundTag targetTagCompound) {
      boolean hasData = false;

      for(int j = 0; j < paletteData.length; ++j) {
         hasData |= paletteData[j] != null;
      }

      if (hasData) {
         ListTag nbttaglist = new ListTag();

         for(int i = 0; i < paletteData.length; ++i) {
            if (paletteData[i] != null && paletteData[i].length == 8) {
               CompoundTag each = new CompoundTag();
               each.putByte("index", (byte)i);
               each.putIntArray("paletteData", paletteData[i]);
               nbttaglist.add(each);
            }
         }

         targetTagCompound.put(tagName, nbttaglist);
      }

   }

   public static final void getPaletteDataFromNBT(int[][] paletteData, String tagName, CompoundTag targetTagCompound) {
      for(int j = 0; j < paletteData.length; ++j) {
         paletteData[j] = null;
      }

      if (targetTagCompound.contains(tagName)) {
         ListTag nbttaglist = targetTagCompound.getList(tagName, 10);
         int count = nbttaglist.size();

         for(int i = 0; i < count; ++i) {
            CompoundTag eachTag = nbttaglist.getCompound(i);
            int index = eachTag.getByte("index");
            if (index >= 0 && index < paletteData.length) {
               paletteData[index] = eachTag.getIntArray("paletteData");
            }
         }
      }

   }

   public static final boolean doesNBTHavePaletteData(String tagName, CompoundTag targetTagCompound) {
      if (targetTagCompound.contains(tagName)) {
         ListTag nbttaglist = targetTagCompound.getList(tagName, 10);
         int count = nbttaglist.size();
         if (count > 0) {
            return true;
         }
      }

      return false;
   }

   public static void fixPaletteByteOrder(int[][] srcDataList, int[][] fixDataList) {
      if (fixDataList == null) {
         JointBlockRemastered.LOGGER.warn("[JointBlock]UnExpected Error In:fixPaletteByteOrder:This should not be happened!");
      } else {
         for(int i = 0; i < srcDataList.length; ++i) {
            if (srcDataList[i] != null && srcDataList[i].length == 8) {
               fixDataList[i] = boFixlPalette(srcDataList[i], fixDataList[i]);
            } else {
               fixDataList[i] = TileEntityJointCore.BOFIXED_DEFAULT_PALETTE_DATA[i];
            }
         }

      }
   }

   public static int[] boFixlPalette(int[] src, int[] dst) {
      if (dst == null) {
         dst = new int[8];
      }

      int c;
      if (isByteOrderLE) {
         c = src[1];
         dst[1] = -16777216 | (c & 255) << 16 | c & '\uff00' | c >> 16 & 255;
         c = src[2];
         dst[2] = -16777216 | (c & 255) << 16 | c & '\uff00' | c >> 16 & 255;
         c = src[3];
         dst[3] = -16777216 | (c & 255) << 16 | c & '\uff00' | c >> 16 & 255;
         c = src[4];
         dst[4] = -16777216 | (c & 255) << 16 | c & '\uff00' | c >> 16 & 255;
         c = src[5];
         dst[5] = -16777216 | (c & 255) << 16 | c & '\uff00' | c >> 16 & 255;
         c = src[6];
         dst[6] = -16777216 | (c & 255) << 16 | c & '\uff00' | c >> 16 & 255;
         c = src[7];
         dst[7] = -16777216 | (c & 255) << 16 | c & '\uff00' | c >> 16 & 255;
      } else {
         c = src[1];
         dst[1] = (c >> 16 & 255) << 24 | (c >> 8 & 255) << 16 | (c & 255) << 8 | 255;
         c = src[2];
         dst[2] = (c >> 16 & 255) << 24 | (c >> 8 & 255) << 16 | (c & 255) << 8 | 255;
         c = src[3];
         dst[3] = (c >> 16 & 255) << 24 | (c >> 8 & 255) << 16 | (c & 255) << 8 | 255;
         c = src[4];
         dst[4] = (c >> 16 & 255) << 24 | (c >> 8 & 255) << 16 | (c & 255) << 8 | 255;
         c = src[5];
         dst[5] = (c >> 16 & 255) << 24 | (c >> 8 & 255) << 16 | (c & 255) << 8 | 255;
         c = src[6];
         dst[6] = (c >> 16 & 255) << 24 | (c >> 8 & 255) << 16 | (c & 255) << 8 | 255;
         c = src[7];
         dst[7] = (c >> 16 & 255) << 24 | (c >> 8 & 255) << 16 | (c & 255) << 8 | 255;
      }

      return dst;
   }

   public static int boFixedToUnFixed(int src) {
      int a;
      int r;
      int g;
      int b;
      if (isByteOrderLE) {
         a = src & 255;
         r = src >> 8 & 255;
         g = src >> 16 & 255;
         b = src >> 24 & 255;
         return b << 24 | a << 16 | r << 8 | g;
      } else {
         a = src & 255;
         r = src >> 24 & 255;
         g = src >> 16 & 255;
         b = src >> 8 & 255;
         return a << 24 | r << 16 | g << 8 | b;
      }
   }

   static {
      isByteOrderLE = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
   }
}
