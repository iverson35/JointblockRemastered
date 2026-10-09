package jp.mc.ancientred.jointblock.assemble;

import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.motion.MotionHelper;

public class SimpleUpdatedJointList {
   public int size = 300;
   public byte[] list;
   public int index;
   private static final byte EXTRA_MESSAGE_DESTRUCT = 4;
   private static final byte EXTRA_MESSAGE_CLIENTACTION = 6;
   private static final byte EXTRA_MESSAGE_TOWARDSTARGETROT = 8;
   private static final byte EXTRA_MESSAGE_CLIENTMOTION = 10;
   private static final byte EXTRA_MESSAGE_CLIENTEFFECT = 12;

   public SimpleUpdatedJointList() {
      this.list = new byte[this.size];
   }

   public void clear() {
      this.index = 0;
   }

   public void addAsCorelSynchronizeUpdate(EntityJointed ej) {
      if (ej.exFlyAttr != null) {
         JointParts elemCore = ej.jointParts.child[0];
         if (elemCore != null) {
            if (elemCore.motionTelePartsId < 255) {
               this.ensureList(17);
               this.list[this.index++] = (byte)(elemCore.motionTelePartsId - 128);
               Quaternion q = ej.exFlyAttr.coreRotQuats;
               int quatX = Float.floatToRawIntBits(q.x);
               this.list[this.index++] = JBCommonUtil.int2Byte3(quatX);
               this.list[this.index++] = JBCommonUtil.int2Byte2(quatX);
               this.list[this.index++] = JBCommonUtil.int2Byte1(quatX);
               this.list[this.index++] = JBCommonUtil.int2Byte0(quatX);
               int quatY = Float.floatToRawIntBits(q.y);
               this.list[this.index++] = JBCommonUtil.int2Byte3(quatY);
               this.list[this.index++] = JBCommonUtil.int2Byte2(quatY);
               this.list[this.index++] = JBCommonUtil.int2Byte1(quatY);
               this.list[this.index++] = JBCommonUtil.int2Byte0(quatY);
               int quatZ = Float.floatToRawIntBits(q.z);
               this.list[this.index++] = JBCommonUtil.int2Byte3(quatZ);
               this.list[this.index++] = JBCommonUtil.int2Byte2(quatZ);
               this.list[this.index++] = JBCommonUtil.int2Byte1(quatZ);
               this.list[this.index++] = JBCommonUtil.int2Byte0(quatZ);
               int quatW = Float.floatToRawIntBits(q.w);
               this.list[this.index++] = JBCommonUtil.int2Byte3(quatW);
               this.list[this.index++] = JBCommonUtil.int2Byte2(quatW);
               this.list[this.index++] = JBCommonUtil.int2Byte1(quatW);
               this.list[this.index++] = JBCommonUtil.int2Byte0(quatW);
            }
         }
      }
   }

   public static final int revertCoreSyncStateFromData(EntityJointed ej, byte[] data, int index) {
      if (ej.exFlyAttr == null) {
         return 17;
      } else {
         synchronized(ej.exFlyAttr) {
            Quaternion q = ej.exFlyAttr.updatedCoreRotQuats;
            q.x = Float.intBitsToFloat(JBCommonUtil.back2Int(data[index + 1], data[index + 2], data[index + 3], data[index + 4]));
            q.y = Float.intBitsToFloat(JBCommonUtil.back2Int(data[index + 5], data[index + 6], data[index + 7], data[index + 8]));
            q.z = Float.intBitsToFloat(JBCommonUtil.back2Int(data[index + 9], data[index + 10], data[index + 11], data[index + 12]));
            q.w = Float.intBitsToFloat(JBCommonUtil.back2Int(data[index + 13], data[index + 14], data[index + 15], data[index + 16]));
            ej.exFlyAttr.updateInc = 3;
            return 17;
         }
      }
   }

   public void addAsFullSynchronizeUpdate(JointParts elem) {
      if (elem.motionTelePartsId < 255) {
         if (elem.jointType != 10 && elem.jointType != 12 && elem.jointType != 15) {
            if (elem.jointType == 8 || elem.jointType == 14) {
               this.ensureList(5);
               this.list[this.index++] = (byte)(elem.motionTelePartsId - 128);
               int a = elem.rotateAngleXNew;
               int absRotX = a < 0 ? -a : a;
               int a1 = elem.rotateAngleYNew;
               int absRotY = a1 < 0 ? -a1 : a1;
               int a2 = elem.rotateAngleZNew;
               int absRotZ = a2 < 0 ? -a2 : a2;
               byte flgs = 0;
               flgs = (byte)(flgs | (elem.rotateAngleXNew < 0 ? 128 : 0));
               flgs = (byte)(flgs | (elem.rotateAngleYNew < 0 ? 64 : 0));
               flgs = (byte)(flgs | (elem.rotateAngleZNew < 0 ? 32 : 0));
               flgs = (byte)(flgs | ((absRotX & 256) != 0 ? 16 : 0));
               flgs = (byte)(flgs | ((absRotY & 256) != 0 ? 8 : 0));
               flgs = (byte)(flgs | ((absRotZ & 256) != 0 ? 4 : 0));
               flgs = (byte)(flgs | (elem.paramModifyDirFlg ? 2 : 0));
               flgs = (byte)(flgs | (elem.doSpecialRotFlg ? 1 : 0));
               this.list[this.index++] = flgs;
               this.list[this.index++] = (byte)(255 & absRotX);
               this.list[this.index++] = (byte)(255 & absRotY);
               this.list[this.index++] = (byte)(255 & absRotZ);
            }
         } else {
            this.ensureList(3);
            this.list[this.index++] = (byte)(elem.motionTelePartsId - 128);
            this.list[this.index++] = (byte)(255 & elem.extendRateNew);
            this.list[this.index++] = (byte)(elem.extendRateNew >> 8);
         }

      }
   }

   public static final int revertFullExtendSyncStateFromData(JointParts elem, byte[] data, int index) {
      elem.extendRateNew = (short)(255 & data[index + 1] | data[index + 2] << 8 & '\uff00');
      return 3;
   }

   public static final int revertFullRotateSyncStateFromData(JointParts elem, byte[] data, int index) {
      byte flgs = data[index + 1];
      int additionalXBit = (flgs & 16) != 0 ? 256 : 0;
      int additionalYBit = (flgs & 8) != 0 ? 256 : 0;
      int additionalZBit = (flgs & 4) != 0 ? 256 : 0;
      int absedRotX = 255 & data[index + 2] | additionalXBit;
      int absedRotY = 255 & data[index + 3] | additionalYBit;
      int absedRotZ = 255 & data[index + 4] | additionalZBit;
      if ((flgs & 128) != 0) {
         elem.rotateAngleXNew = (short)(-absedRotX);
      } else {
         elem.rotateAngleXNew = (short)absedRotX;
      }

      if ((flgs & 64) != 0) {
         elem.rotateAngleYNew = (short)(-absedRotY);
      } else {
         elem.rotateAngleYNew = (short)absedRotY;
      }

      if ((flgs & 32) != 0) {
         elem.rotateAngleZNew = (short)(-absedRotZ);
      } else {
         elem.rotateAngleZNew = (short)absedRotZ;
      }

      elem.doSpecialRotFlg = (flgs & 1) != 0;
      return 5;
   }

   public void ensureList(int dataSize) {
      while(this.index >= this.list.length || this.index + dataSize >= this.list.length) {
         byte[] newList = new byte[this.size *= 2];
         System.arraycopy(this.list, 0, newList, 0, this.list.length);
         this.list = newList;
      }

   }

   public static void updateReceivedAmount(EntityJointed entity, byte[] data) {
      int index = 0;

      while(true) {
         while(index < data.length) {
            int refMotId = data[index] + 128;
            if (refMotId < 255) {
               JointParts partsToUpdate = entity.motionRefLists[refMotId];
               if (partsToUpdate == null) {
                  return;
               }

               JointParts.ensureMotionHanlder(partsToUpdate);
               if (partsToUpdate.jointType != 10 && partsToUpdate.jointType != 12 && partsToUpdate.jointType != 15) {
                  if (partsToUpdate.jointType != 8 && partsToUpdate.jointType != 14) {
                     if (partsToUpdate.jointType != 0) {
                        return;
                     }

                     index += revertCoreSyncStateFromData(entity, data, index);
                  } else {
                     index += revertFullRotateSyncStateFromData(partsToUpdate, data, index);
                  }
               } else {
                  index += revertFullExtendSyncStateFromData(partsToUpdate, data, index);
               }

               partsToUpdate.hasReceivedClient = true;
               partsToUpdate.fixValuesInValidRange();
            } else if (refMotId == 255) {
               switch (data[index + 1]) {
                  case 4:
                     index += processExtraDestructSyncMessage(entity, data, index);
                     break;
                  case 5:
                  case 7:
                  case 9:
                  case 11:
                  default:
                     return;
                  case 6:
                     index += processExtraClientActionSyncMessage(entity, data, index);
                     break;
                  case 8:
                     index += processExtraTowardsTargetRotSyncMessage(entity, data, index);
                     break;
                  case 10:
                     index += processExtraClientMotionSyncMessage(entity, data, index);
                     break;
                  case 12:
                     index += processExtraClientEffectSyncMessage(entity, data, index);
               }
            }
         }

         return;
      }
   }

   public void addAsExtraDestructSyncMessage(JointParts elem) {
      this.ensureList(4);
      this.list[this.index++] = 127;
      this.list[this.index++] = 4;
      this.list[this.index++] = (byte)(elem.id >> 8 & 255);
      this.list[this.index++] = (byte)(elem.id & 255);
   }

   private static int processExtraDestructSyncMessage(EntityJointed entity, byte[] data, int index) {
      int refId = (short)(data[index + 2] << 8 & '\uff00' | 255 & data[index + 3]);
      JointParts elem = entity.refLists[refId];
      if (elem != null) {
      }

      return 4;
   }

   public void addAsExtraClientActionSyncMessage(JointParts elem) {
      this.ensureList(4);
      this.list[this.index++] = 127;
      this.list[this.index++] = 6;
      this.list[this.index++] = (byte)(elem.id >> 8 & 255);
      this.list[this.index++] = (byte)(elem.id & 255);
   }

   public void addAsExtraClientMotionSyncMessage(JointParts elem) {
      this.ensureList(4);
      this.list[this.index++] = 127;
      this.list[this.index++] = 10;
      this.list[this.index++] = (byte)(elem.id >> 8 & 255);
      this.list[this.index++] = (byte)(elem.id & 255);
   }

   public void addAsExtraClientEffectSyncMessage(JointParts elem, byte effectNum) {
      this.ensureList(5);
      this.list[this.index++] = 127;
      this.list[this.index++] = 12;
      this.list[this.index++] = (byte)(elem.id >> 8 & 255);
      this.list[this.index++] = (byte)(elem.id & 255);
      this.list[this.index++] = effectNum;
   }

   private static int processExtraClientActionSyncMessage(EntityJointed entity, byte[] data, int index) {
      int refId = (short)(data[index + 2] << 8 & '\uff00' | 255 & data[index + 3]);
      JointParts elem = entity.refLists[refId];
      if (elem != null) {
         elem.doAction = true;
      }

      return 4;
   }

   private static int processExtraClientMotionSyncMessage(EntityJointed entity, byte[] data, int index) {
      int refId = (short)(data[index + 2] << 8 & '\uff00' | 255 & data[index + 3]);
      JointParts elem = entity.refLists[refId];
      if (elem != null) {
         elem.doMotion = true;
      }

      return 4;
   }

   private static int processExtraClientEffectSyncMessage(EntityJointed entity, byte[] data, int index) {
      int refId = (short)(data[index + 2] << 8 & '\uff00' | 255 & data[index + 3]);
      JointParts elem = entity.refLists[refId];
      if (elem != null) {
         elem.doEffect = data[index + 4];
      }

      return 5;
   }

   public void addAsExtraTowardsTargetRotSyncMessage(JointParts elem, byte targetNum, byte mode, byte limitAngle) {
      if (elem.motionTelePartsId < 255) {
         this.ensureList(6);
         this.list[this.index++] = 127;
         this.list[this.index++] = 8;
         this.list[this.index++] = (byte)(elem.motionTelePartsId - 128);
         this.list[this.index++] = targetNum;
         this.list[this.index++] = mode;
         this.list[this.index++] = limitAngle;
      }
   }

   private static int processExtraTowardsTargetRotSyncMessage(EntityJointed entity, byte[] data, int index) {
      int refMotId = data[index + 2] + 128;
      if (refMotId >= 255) {
         return 6;
      } else {
         JointParts partsToUpdate = entity.motionRefLists[refMotId];
         if (partsToUpdate == null) {
            return 6;
         } else {
            byte targetNum = data[index + 3];
            byte mode = data[index + 4];
            byte limitAngle = data[index + 5];
            if (partsToUpdate.motionHelper == null) {
               partsToUpdate.motionHelper = new MotionHelper();
            }

            partsToUpdate.motionHelper.commonUse = (255 & targetNum) << 16 | (255 & mode) << 8 | 255 & limitAngle;
            return 6;
         }
      }
   }
}
