package jp.mc.ancientred.jointblock.assemble;

import java.util.Arrays;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.motion.MotionHelper;

public class JointDataSerializer {
   public static final int MAX_UBYTE_AS_UNUSED = 255;
   public static final int UBYTE_TO_BYTE_FIX = 128;
   public static final int MAX_MOTIONTEL_CNT = 255;
   private static final byte FORMAT_VER = 10;
   private static int[] dataWorkAry = new int[32767];
   private static final int EXTRADATA_TYPE_EXINIT = 10;
   private static final int EXTRADATA_TYPE_CARVING = 20;
   private static final int EXTRADATA_TYPE_EXTRAFLAGS = 30;
   private static final int EXTRADATA_TYPE_MEMOSTR = 40;
   private static final int EXTRADATA_TYPE_RINGHELPER = 50;

   public static final int[] serializeAll(JointParts[] refLists, int partsNum) {
      int index = 0;
      boolean retry = false;

      do {
         try {
            index = 0;
            int[] data = dataWorkAry;
            int partsIdCnt = 0;
            int motionPartsIdCnt = 0;
            int len = refLists.length;

            for(int i = 0; i < len; ++i) {
               JointParts target = refLists[i];
               target.id = (short)(partsIdCnt++);
               target.isMotionType = JBCommonUtil.jointTypeHasMotion(target.jointType);
               if (JBCommonUtil.jointNeedsMotionId(target.jointType, target.motionItemId, target.motionMeta, target.bindNum) && motionPartsIdCnt < 255) {
                  target.motionTelePartsId = motionPartsIdCnt++;
               } else {
                  target.motionTelePartsId = 255;
               }

               data[index++] = target.parentId;
               int extraDataCntsSaveIndex = index;
               data[index++] = target.connectSide << 24 | (255 & target.xCoord) << 16 | (255 & target.yCoord) << 8 | target.zCoord & 255;
               data[index++] = (target.isDestructed ? 1 : 0) << 31 | (127 & target.jointType) << 24 | (255 & target.modelMeta) << 16 | target.modelItemId & '\uffff';
               data[index++] = (target.requestDestruct ? 1 : 0) << 31 | (target.cancelParentRotate ? 1 : 0) << 30 | target.guiPage << 28 | target.rotatePoint << 24 | target.renderSide << 20 | target.facing << 16 | target.motionItemId & '\uffff';
               if (jointNeedsParam(target.jointType) || target.motionItemId != 0) {
                  data[index++] = target.extendRate << 16 | (255 & target.motionMeta) << 8 | 255 & target.bindNum;
                  data[index++] = pushThreeValuesIntoInt(target.params[0], target.params[1], target.params[2]);
                  data[index++] = pushThreeValuesIntoInt(target.params[3], target.params[4], target.params[5]);
                  data[index++] = pushFourValuesIntoInt(target.initValues[0][0], target.initValues[0][1], target.initValues[0][2], target.initValues[0][3]);
                  data[index++] = pushThreeValuesIntoIntWithExBitFlagA(target.rotateAngleX, target.rotateAngleY, target.rotateAngleZ, target.paramModifyDirFlg);
               }

               int extraDataCnts = 0;
               if (target.initValues.length > 1) {
                  data[index++] = 10;
                  index = setExInitLayers(data, index, target);
                  ++extraDataCnts;
               }

               if (target.carvingData != null && JBConfig.enableCarving) {
                  data[index++] = 20;
                  index = setExCarvingData(data, index, target);
                  ++extraDataCnts;
               }

               if (target.extraJointStateFlags != 0) {
                  data[index++] = 30 | target.extraJointStateFlags & -65536;
                  ++extraDataCnts;
               }

               if (target.memoStrHolder != null) {
                  data[index++] = 40;
                  index = setExMemoStr(data, index, target);
                  ++extraDataCnts;
               }

               if (target.motionHelper != null) {
                  data[index++] = 50;
                  index = setExRingHelper(data, index, target);
                  ++extraDataCnts;
               }

               data[extraDataCntsSaveIndex] |= extraDataCnts << 28;

               for(int k = 0; k < 6; ++k) {
                  if (target.child[k] != null) {
                     target.child[k].parentId = target.id;
                  }
               }
            }

            retry = false;
         } catch (IndexOutOfBoundsException var13) {
            retry = true;
            dataWorkAry = new int[dataWorkAry.length * 2];
         }
      } while(retry);

      return Arrays.copyOf(dataWorkAry, index);
   }

   public static final JointParts rebuildAll(int[] data, int partsCount, IJointListsHolder refHolder) {
      JointParts topLevelParts = null;
      if (partsCount > 32767) {
         return null;
      } else {
         JointParts[] referenceAry = new JointParts[partsCount];
         JointParts[] motionReferenceAry = new JointParts[255];
         short[] sWork = new short[4];
         int minY = 0;
         int maxY = 0;
         int partsIdCnt = 0;
         int motionTelePartsCount = 0;
         int len = data.length;
         int index = 0;

         while(index < len) {
            int refId = data[index++];
            int param = data[index++];
            int extraDataCnts = param >> 28;
            int cSide = param >> 24 & 15;
            int pSide = EnumDirectionOld.OPPOSITES[cSide];
            int relX = (byte)(param >> 16 & 255);
            int relY = (byte)(param >> 8 & 255);
            int relZ = (byte)(param & 255);
            JointParts jointParts = JBModContainer.proxy.creteSideBasedJointParts(relX, relY, relZ);
            jointParts.connectSide = (byte)cSide;
            jointParts.id = (short)(partsIdCnt++);
            referenceAry[jointParts.id] = jointParts;
            param = data[index++];
            jointParts.isDestructed = (param & Integer.MIN_VALUE) != 0;
            jointParts.jointType = (byte)((param & 2130706432) >> 24);
            jointParts.modelMeta = (byte)((param & 16711680) >> 16);
            jointParts.modelItemId = (short)(param & '\uffff');
            param = data[index++];
            jointParts.requestDestruct = (param & Integer.MIN_VALUE) != 0;
            jointParts.cancelParentRotate = (param & 1073741824) != 0;
            jointParts.guiPage = (byte)((param & 805306368) >> 28);
            jointParts.rotatePoint = (byte)((param & 251658240) >> 24);
            jointParts.renderSide = (byte)((param & 15728640) >> 20);
            jointParts.facing = (byte)((param & 983040) >> 16);
            jointParts.motionItemId = (short)(param & '\uffff');
            jointParts.isMotionType = JBCommonUtil.jointTypeHasMotion(jointParts.jointType);
            jointParts.motionTelePartsId = 255;
            if (jointNeedsParam(jointParts.jointType) || jointParts.motionItemId != 0) {
               param = data[index++];
               jointParts.extendRateNew = jointParts.prevExtendRate = jointParts.extendRate = (short)(param >> 16);
               jointParts.motionMeta = (byte)((param & '\uff00') >> 8);
               jointParts.bindNum = (short)(param & 255);
               if (JBCommonUtil.jointNeedsMotionId(jointParts.jointType, jointParts.motionItemId, jointParts.motionMeta, jointParts.bindNum) && motionTelePartsCount < 255) {
                  jointParts.motionTelePartsId = motionTelePartsCount++;
                  motionReferenceAry[jointParts.motionTelePartsId] = jointParts;
               }

               param = data[index++];
               sWork = getThreeValuesFromInt(sWork, param);
               jointParts.params[0] = sWork[0];
               jointParts.params[1] = sWork[1];
               jointParts.params[2] = sWork[2];
               param = data[index++];
               sWork = getThreeValuesFromInt(sWork, param);
               jointParts.params[3] = sWork[0];
               jointParts.params[4] = sWork[1];
               jointParts.params[5] = sWork[2];
               param = data[index++];
               sWork = getFourValuesFromInt(sWork, param);
               jointParts.initValues[0][0] = sWork[0];
               jointParts.initValues[0][1] = sWork[1];
               jointParts.initValues[0][2] = sWork[2];
               jointParts.initValues[0][3] = sWork[3];
               param = data[index++];
               sWork = getThreeValuesFromInt(sWork, param);
               jointParts.rotateAngleXNew = jointParts.prevRotateAngleX = jointParts.rotateAngleX = sWork[0];
               jointParts.rotateAngleYNew = jointParts.prevRotateAngleY = jointParts.rotateAngleY = sWork[1];
               jointParts.rotateAngleZNew = jointParts.prevRotateAngleZ = jointParts.rotateAngleZ = sWork[2];
               jointParts.paramModifyDirFlg = getExBitFlagAFromInt(param);
            }

            for(; extraDataCnts > 0; --extraDataCnts) {
               param = data[index++];
               if ((param & '\uffff') == 10) {
                  index = buildExInitLayers(data, sWork, index, jointParts);
               }

               if ((param & '\uffff') == 20) {
                  index = buildExCarvingData(data, index, jointParts);
                  jointParts.detectCarvingType();
               }

               if ((param & '\uffff') == 30) {
                  jointParts.extraJointStateFlags = param & -65536;
               }

               if ((param & '\uffff') == 40) {
                  index = buildExMemoStr(data, index, jointParts);
               }

               if ((param & '\uffff') == 50) {
                  index = buildExRingHelper(data, index, jointParts);
               }
            }

            if (refId == -1) {
               topLevelParts = JBModContainer.proxy.creteSideBasedJointParts(0, 0, 0);
               topLevelParts.child[0] = jointParts;
               topLevelParts.hasChild = true;
               jointParts.parent = topLevelParts;
               jointParts.depth = 0;
            } else if (0 <= refId && refId < partsCount && 0 <= pSide && pSide < 6) {
               JointParts parent = referenceAry[refId];
               parent.child[pSide] = jointParts;
               parent.hasChild = true;
               jointParts.parent = parent;
               jointParts.depth = parent.depth + 1;
               jointParts.renderOffsetX = (float)(jointParts.xCoord - parent.xCoord);
               jointParts.renderOffsetY = (float)(jointParts.yCoord - parent.yCoord);
               jointParts.renderOffsetZ = (float)(jointParts.zCoord - parent.zCoord);
               if (minY > jointParts.yCoord) {
                  minY = jointParts.yCoord;
               }

               if (maxY < jointParts.yCoord) {
                  maxY = jointParts.yCoord;
               }
            }
         }

         refHolder.setReferenceLists(referenceAry, motionReferenceAry);
         refHolder.setMinMaxY(minY, maxY);
         return topLevelParts;
      }
   }

   private static int setExInitLayers(int[] data, int index, JointParts target) {
      data[index++] = target.initValues.length;

      for(int j = 1; j < target.initValues.length; ++j) {
         data[index++] = pushFourValuesIntoInt(target.initValues[j][0], target.initValues[j][1], target.initValues[j][2], target.initValues[j][3]);
      }

      return index;
   }

   private static int buildExInitLayers(int[] data, short[] sWork, int index, JointParts jointParts) {
      int initValuesLayerNum = data[index++];
      if (initValuesLayerNum > 127) {
         throw new RuntimeException("invalid initvalue data size");
      } else {
         short[] saved = jointParts.initValues[0];
         jointParts.initValues = new short[initValuesLayerNum][];
         jointParts.initValues[0] = saved;

         for(int j = 1; j < initValuesLayerNum; ++j) {
            sWork = getFourValuesFromInt(sWork, data[index++]);
            jointParts.initValues[j] = new short[4];
            jointParts.initValues[j][0] = sWork[0];
            jointParts.initValues[j][1] = sWork[1];
            jointParts.initValues[j][2] = sWork[2];
            jointParts.initValues[j][3] = sWork[3];
         }

         return index;
      }
   }

   private static int setExCarvingData(int[] data, int index, JointParts jointParts) {
      int[] carvingData = jointParts.carvingData;
      data[index++] = carvingData.length;
      System.arraycopy(carvingData, 0, data, index, carvingData.length);
      return index + carvingData.length;
   }

   private static int buildExCarvingData(int[] data, int index, JointParts jointParts) {
      int carvingDataLength = data[index++];
      if (carvingDataLength > 32767) {
         throw new RuntimeException("invalid carving data size");
      } else {
         int[] carvingData = new int[carvingDataLength];
         System.arraycopy(data, index, carvingData, 0, carvingDataLength);
         jointParts.carvingData = carvingData;
         return index + carvingDataLength;
      }
   }

   private static int setExRingHelper(int[] data, int index, JointParts jointParts) {
      MotionHelper helper = jointParts.motionHelper;
      data[index++] = helper.version;
      data[index++] = helper.start0 & '\uffff' | helper.start1 << 16 & -65536;
      data[index++] = helper.start2 & '\uffff' | 0;
      data[index++] = helper.commonUse;
      return index;
   }

   private static int buildExRingHelper(int[] data, int index, JointParts jointParts) {
      MotionHelper helper = jointParts.motionHelper = new MotionHelper();
      int version = data[index++];
      int value = data[index++];
      helper.start0 = (short)(value & '\uffff');
      helper.start1 = (short)(value >> 16 & '\uffff');
      value = data[index++];
      helper.start2 = (short)(value & '\uffff');
      helper.commonUse = data[index++];
      return index;
   }

   private static int setExMemoStr(int[] data, int index, JointParts jointParts) {
      byte[] memoStrByteAry = jointParts.memoStrHolder.memoStrByteAry;
      data[index++] = memoStrByteAry.length;
      int targetIdx = 0;

      for(int i = 0; i < memoStrByteAry.length; ++i) {
         int mod = i & 3;
         if (mod == 0) {
            targetIdx = index++;
            data[targetIdx] = 0;
         }

         data[targetIdx] |= (memoStrByteAry[i] & 255) << mod * 8;
      }

      return index;
   }

   private static int buildExMemoStr(int[] data, int index, JointParts jointParts) {
      int memoStrIntAryLength = data[index++];
      if (memoStrIntAryLength > 32767) {
         throw new RuntimeException("invalid memostr data size");
      } else {
         byte[] memoStrByteAry = new byte[memoStrIntAryLength];
         int targetIdx = 0;

         for(int i = 0; i < memoStrIntAryLength; ++i) {
            int mod = i & 3;
            if (mod == 0) {
               targetIdx = index++;
            }

            memoStrByteAry[i] = (byte)(data[targetIdx] >> mod * 8 & 255);
         }

         try {
            jointParts.memoStrHolder = new MemoStringHolder(memoStrByteAry);
         } catch (Exception var8) {
            jointParts.memoStrHolder = null;
         }

         return index;
      }
   }

   private static boolean jointNeedsParam(int jointType) {
      return jointType == 0 || jointType == 2 || jointType == 8 || jointType == 10 || jointType == 12 || jointType == 14 || jointType == 15;
   }

   private static final int pushFourValuesIntoInt(short valueA, short valueB, short valueC, short valueD) {
      return pushThreeValuesIntoInt(valueA, valueB, valueC) | valueD << 30;
   }

   private static final short[] getFourValuesFromInt(short[] ret, int value) {
      ret[3] = (short)(3 & value >>> 30);
      return getThreeValuesFromInt(ret, value);
   }

   private static final int pushThreeValuesIntoInt(short valueA, short valueB, short valueC) {
      int absValA = valueA < 0 ? -valueA : valueA;
      int absValB = valueB < 0 ? -valueB : valueB;
      int absValC = valueC < 0 ? -valueC : valueC;
      byte flgs = 0;
      flgs = (byte)(flgs | (valueA < 0 ? 32 : 0));
      flgs = (byte)(flgs | (valueB < 0 ? 16 : 0));
      flgs = (byte)(flgs | (valueC < 0 ? 8 : 0));
      flgs = (byte)(flgs | ((absValA & 256) != 0 ? 4 : 0));
      flgs = (byte)(flgs | ((absValB & 256) != 0 ? 2 : 0));
      flgs = (byte)(flgs | ((absValC & 256) != 0 ? 1 : 0));
      return flgs << 24 | (255 & absValA) << 16 | (255 & absValB) << 8 | 255 & absValC;
   }

   private static final short[] getThreeValuesFromInt(short[] ret, int value) {
      byte flgs = (byte)(255 & value >> 24);
      int additionalValABit = (flgs & 4) != 0 ? 256 : 0;
      int additionalValBBit = (flgs & 2) != 0 ? 256 : 0;
      int additionalValCBit = (flgs & 1) != 0 ? 256 : 0;
      int absedValA = 255 & value >> 16 | additionalValABit;
      int absedValB = 255 & value >> 8 | additionalValBBit;
      int absedValC = 255 & value | additionalValCBit;
      if ((flgs & 32) != 0) {
         ret[0] = (short)(-absedValA);
      } else {
         ret[0] = (short)absedValA;
      }

      if ((flgs & 16) != 0) {
         ret[1] = (short)(-absedValB);
      } else {
         ret[1] = (short)absedValB;
      }

      if ((flgs & 8) != 0) {
         ret[2] = (short)(-absedValC);
      } else {
         ret[2] = (short)absedValC;
      }

      return ret;
   }

   private static final int pushThreeValuesIntoIntWithExBitFlagA(short valueA, short valueB, short valueC, boolean flag) {
      int absValA = valueA < 0 ? -valueA : valueA;
      int absValB = valueB < 0 ? -valueB : valueB;
      int absValC = valueC < 0 ? -valueC : valueC;
      byte flgs = 0;
      flgs = (byte)(flgs | (flag ? 64 : 0));
      flgs = (byte)(flgs | (valueA < 0 ? 32 : 0));
      flgs = (byte)(flgs | (valueB < 0 ? 16 : 0));
      flgs = (byte)(flgs | (valueC < 0 ? 8 : 0));
      flgs = (byte)(flgs | ((absValA & 256) != 0 ? 4 : 0));
      flgs = (byte)(flgs | ((absValB & 256) != 0 ? 2 : 0));
      flgs = (byte)(flgs | ((absValC & 256) != 0 ? 1 : 0));
      return flgs << 24 | (255 & absValA) << 16 | (255 & absValB) << 8 | 255 & absValC;
   }

   private static final boolean getExBitFlagAFromInt(int value) {
      return (value & 1073741824) != 0;
   }
}
