package jp.mc.ancientred.jointblock.motion.common;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractPendulumActHandler extends PendulumHelper implements IJBMotionBinderItem.IJBMotionHandler {
   public static final int TYPE_TICK = 0;
   public static final int TYPE_MOVE = 1;
   public static final int TYPE_KEY = 2;
   public static final int TYPE_ITEM = 3;
   public int type;
   public int axis;
   public int seatNum;
   public int keyItemEntryNum;
   public int amountEntryNum;
   public int minEntryNum;
   public int maxEntryNum;
   public int patternEntryNum;
   public int autoBackEntryNum;
   public boolean enableAutoBackOnKeyItemUpEntry = false;

   public void setCommonParamsInitialValue(int jointType, int[] params) {
      params[this.patternEntryNum] = 0;
      if (this.enableAutoBackOnKeyItemUpEntry) {
         params[this.autoBackEntryNum] = 0;
      }

   }

   public void setCommonGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      guiSettings.setParamEntryState(this.patternEntryNum, (String)"basicmot.pendulum.pattern.title", (Format)EntryFormats.pendulumPatternValueFormat, 0, 1);
      guiSettings.setParamButtonToggleAry(this.patternEntryNum, EntryFormats.selectLRButtonStates, 0);
      if (this.enableAutoBackOnKeyItemUpEntry) {
         guiSettings.setParamEntryState(this.autoBackEntryNum, (String)"basicmot.pendulum.autoback.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(this.autoBackEntryNum, EntryFormats.selectValueButtonStates, 0);
      }

   }

   public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
      boolean doMinMaxFlip = false;
      if (joint.getJointType() == 8) {
         if (flipWay == 2) {
            switch (this.axis) {
               case 0:
                  joint.setParam(this.amountEntryNum, joint.getParam(this.amountEntryNum));
                  break;
               case 1:
                  joint.setParam(this.amountEntryNum, -joint.getParam(this.amountEntryNum));
                  doMinMaxFlip = true;
                  break;
               default:
                  joint.setParam(this.amountEntryNum, -joint.getParam(this.amountEntryNum));
                  doMinMaxFlip = true;
            }
         } else if (flipWay == 1) {
            switch (this.axis) {
               case 0:
                  joint.setParam(this.amountEntryNum, -joint.getParam(this.amountEntryNum));
                  doMinMaxFlip = true;
                  break;
               case 1:
                  joint.setParam(this.amountEntryNum, joint.getParam(this.amountEntryNum));
                  break;
               default:
                  joint.setParam(this.amountEntryNum, -joint.getParam(this.amountEntryNum));
                  doMinMaxFlip = true;
            }
         }
      }

      if (joint.getJointType() == 14) {
         if (flipWay == 2) {
            switch (this.axis) {
               case 0:
                  joint.setParam(this.amountEntryNum, -joint.getParam(this.amountEntryNum));
                  doMinMaxFlip = true;
                  break;
               case 1:
                  joint.setParam(this.amountEntryNum, joint.getParam(this.amountEntryNum));
                  break;
               default:
                  joint.setParam(this.amountEntryNum, joint.getParam(this.amountEntryNum));
            }
         } else if (flipWay == 1) {
            switch (this.axis) {
               case 0:
                  joint.setParam(this.amountEntryNum, joint.getParam(this.amountEntryNum));
                  break;
               case 1:
                  joint.setParam(this.amountEntryNum, -joint.getParam(this.amountEntryNum));
                  doMinMaxFlip = true;
                  break;
               default:
                  joint.setParam(this.amountEntryNum, joint.getParam(this.amountEntryNum));
            }
         }
      }

      if (doMinMaxFlip) {
         int swp = joint.getParam(this.minEntryNum);
         joint.setParam(this.minEntryNum, -joint.getParam(this.maxEntryNum));
         joint.setParam(this.maxEntryNum, -swp);
      }

   }

   public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return this.detectOperate(entityLivingBase, joint) || this.detectAutoBack(entityLivingBase, joint);
   }

   private boolean detectAutoBack(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return this.enableAutoBackOnKeyItemUpEntry && joint.getParam(this.autoBackEntryNum) != 0;
   }

   private boolean detectOperate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      boolean onOperate = false;
      if (this.type == 1) {
         if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
            return true;
         }

         double d0 = entityLivingBase.getX() - entityLivingBase.xo;
         double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
         double d2 = entityLivingBase.getY() - entityLivingBase.yo;
         float distMoved = (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
         onOperate = (double)distMoved > 0.1;
      } else {
         EntitySeatPart entitySeats;
         if (this.type == 2) {
            entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
            if (entitySeats == null || !entitySeats.canKeyItemUsed()) {
               return false;
            }

            onOperate = entitySeats.isRiddenbyPressingExKey(joint.getParam(this.keyItemEntryNum));
         } else {
            if (this.type != 3) {
               return true;
            }

            entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
            if (entitySeats == null || !entitySeats.canKeyItemUsed()) {
               return false;
            }

            onOperate = entitySeats.isRiddenbyUsedItem(joint.getParam(this.keyItemEntryNum));
         }
      }

      return onOperate;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      if (this.type == 1 && !EntityJointed.isItMotIndependant(entityLivingBase)) {
         ((JointParts)joint).setToIninitalValue();
      } else if (this.detectAutoBack(entityLivingBase, joint) && !this.detectOperate(entityLivingBase, joint)) {
         ((JointParts)joint).setToIninitalValue();
      } else {
         int amount = joint.getParam(this.amountEntryNum);
         int min = joint.getParam(this.minEntryNum);
         int max = joint.getParam(this.maxEntryNum);
         if (joint.getParam(this.patternEntryNum) == 1) {
            sinePendulumMotion(joint, amount, min, max, this.axis);
         } else {
            simplePendulumMotion(joint, amount, min, max, this.axis);
         }

      }
   }

   public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
   }

   public boolean isIgnoreRotationPrevFix() {
      return true;
   }
}
