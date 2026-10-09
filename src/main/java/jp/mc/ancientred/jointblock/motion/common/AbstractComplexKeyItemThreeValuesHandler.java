package jp.mc.ancientred.jointblock.motion.common;

import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractComplexKeyItemThreeValuesHandler implements IJBMotionBinderItem.IJBMotionHandler {
   public static final int FOR_KEY = 0;
   public static final int FOR_ITEM = 1;
   public static final int AXIS_X = 0;
   public static final int AXIS_Y = 1;
   public static final int AXIS_Z = 2;
   public int keyOrItem;
   public int seatNum;
   public int axis;

   public String getMotionHandlerName() {
      if (this.keyOrItem == 0) {
         switch (this.axis) {
            case 0:
               return "mhndl.keyresponse.complex.name, [X]";
            case 1:
               return "mhndl.keyresponse.complex.name, [Y]";
            default:
               return "mhndl.keyresponse.complex.name, [Z]";
         }
      } else {
         switch (this.axis) {
            case 0:
               return "mhndl.itemresponse.complex.name, [X]";
            case 1:
               return "mhndl.itemresponse.complex.name, [Y]";
            default:
               return "mhndl.itemresponse.complex.name, [Z]";
         }
      }
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      if (this.keyOrItem == 0) {
         guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(2, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      } else {
         guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(2, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      }

      if (jointType == 8) {
         switch (this.axis) {
            case 0:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.rotateXTitleFormat, (String)"jbcommon.xangledegreese.format", -180, 180);
               guiSettings.setParamEntryState(3, (ChoiceFormat)EntryFormats.rotateXTitleFormat, (String)"jbcommon.xangledegreese.format", -180, 180);
               break;
            case 1:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.rotateYTitleFormat, (String)"jbcommon.yangledegreese.format", -180, 180);
               guiSettings.setParamEntryState(3, (ChoiceFormat)EntryFormats.rotateYTitleFormat, (String)"jbcommon.yangledegreese.format", -180, 180);
               break;
            default:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.rotateZTitleFormat, (String)"jbcommon.zangledegreese.format", -180, 180);
               guiSettings.setParamEntryState(3, (ChoiceFormat)EntryFormats.rotateZTitleFormat, (String)"jbcommon.zangledegreese.format", -180, 180);
         }

         guiSettings.setParamEntryState(4, (String)"basicmot.rotate.min.tilte", (String)"jbcommon.angledegreese.format", -359, 359);
         guiSettings.setParamEntryState(5, (String)"basicmot.rotate.max.title", (String)"jbcommon.angledegreese.format", -359, 359);
      } else {
         switch (this.axis) {
            case 0:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.slideXTitleFormat, (String)"jbcommon.xslideramount.format", -128, 128);
               guiSettings.setParamEntryState(3, (ChoiceFormat)EntryFormats.slideXTitleFormat, (String)"jbcommon.xslideramount.format", -128, 128);
               break;
            case 1:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.slideYTitleFormat, (String)"jbcommon.yslideramount.format", -128, 128);
               guiSettings.setParamEntryState(3, (ChoiceFormat)EntryFormats.slideYTitleFormat, (String)"jbcommon.yslideramount.format", -128, 128);
               break;
            default:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.slideZTitleFormat, (String)"jbcommon.zslideramount.format", -128, 128);
               guiSettings.setParamEntryState(3, (ChoiceFormat)EntryFormats.slideZTitleFormat, (String)"jbcommon.zslideramount.format", -128, 128);
         }

         guiSettings.setParamButtonToggleAry(1, EntryFormats.sliderAmountButtonStates, 0);
         guiSettings.setParamButtonToggleAry(3, EntryFormats.sliderAmountButtonStates, 0);
         guiSettings.setParamEntryState(4, (String)"basicmot.slider.min.tilte", (String)"jbcommon.slideramount.format", -128, 128);
         guiSettings.setParamButtonToggleAry(4, EntryFormats.sliderAmountButtonStates, 0);
         guiSettings.setParamEntryState(5, (String)"basicmot.slider.max.tilte", (String)"jbcommon.slideramount.format", -128, 128);
         guiSettings.setParamButtonToggleAry(5, EntryFormats.sliderAmountButtonStates, 0);
      }

   }

   public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
      boolean doMinMaxFlip = false;
      if (joint.getJointType() == 8) {
         if (flipWay == 2) {
            switch (this.axis) {
               case 0:
                  joint.setParam(1, joint.getParam(1));
                  joint.setParam(3, joint.getParam(3));
                  break;
               case 1:
                  joint.setParam(1, -joint.getParam(1));
                  joint.setParam(3, -joint.getParam(3));
                  doMinMaxFlip = true;
                  break;
               default:
                  joint.setParam(1, -joint.getParam(1));
                  joint.setParam(3, -joint.getParam(3));
                  doMinMaxFlip = true;
            }
         } else if (flipWay == 1) {
            switch (this.axis) {
               case 0:
                  joint.setParam(1, -joint.getParam(1));
                  joint.setParam(3, -joint.getParam(3));
                  doMinMaxFlip = true;
                  break;
               case 1:
                  joint.setParam(1, joint.getParam(1));
                  joint.setParam(3, joint.getParam(3));
                  break;
               default:
                  joint.setParam(1, -joint.getParam(1));
                  joint.setParam(3, -joint.getParam(3));
                  doMinMaxFlip = true;
            }
         }
      } else if (joint.getJointType() == 14) {
         if (flipWay == 2) {
            switch (this.axis) {
               case 0:
                  joint.setParam(1, -joint.getParam(1));
                  joint.setParam(3, -joint.getParam(3));
                  doMinMaxFlip = true;
                  break;
               case 1:
                  joint.setParam(1, joint.getParam(1));
                  joint.setParam(3, joint.getParam(3));
                  break;
               default:
                  joint.setParam(1, joint.getParam(1));
                  joint.setParam(3, joint.getParam(3));
            }
         } else if (flipWay == 1) {
            switch (this.axis) {
               case 0:
                  joint.setParam(1, joint.getParam(1));
                  joint.setParam(3, joint.getParam(3));
                  break;
               case 1:
                  joint.setParam(1, -joint.getParam(1));
                  joint.setParam(3, -joint.getParam(3));
                  doMinMaxFlip = true;
                  break;
               default:
                  joint.setParam(1, joint.getParam(1));
                  joint.setParam(3, joint.getParam(3));
            }
         }
      }

      if (doMinMaxFlip) {
         int swp = joint.getParam(4);
         joint.setParam(4, -joint.getParam(5));
         joint.setParam(5, -swp);
      }

   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntitySeatPart entitySeats;
      if (this.keyOrItem == 0) {
         entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
         if (entitySeats != null && entitySeats.canKeyItemUsed()) {
            return entitySeats.isRiddenbyPressingExKey(joint.getParam(0)) || entitySeats.isRiddenbyPressingExKey(joint.getParam(2));
         } else {
            return false;
         }
      } else {
         entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
         if (entitySeats != null && entitySeats.canKeyItemUsed()) {
            return entitySeats.isRiddenbyUsedItem(joint.getParam(0)) || entitySeats.isRiddenbyUsedItem(joint.getParam(2));
         } else {
            return false;
         }
      }
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntitySeatPart entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
      if (entitySeats != null && entitySeats.canKeyItemUsed()) {
         int min = joint.getParam(4);
         int max = joint.getParam(5);
         int minmaxDiff = max - min;
         if (minmaxDiff <= 0) {
            switch (this.axis) {
               case 0:
                  joint.setNewRotationX(min);
                  break;
               case 1:
                  joint.setNewRotationY(min);
                  break;
               default:
                  joint.setNewRotationZ(min);
            }

         } else {
            int nowValue;
            switch (this.axis) {
               case 0:
                  nowValue = joint.getRotationX();
                  break;
               case 1:
                  nowValue = joint.getRotationY();
                  break;
               default:
                  nowValue = joint.getRotationZ();
            }

            nowValue = this.addExtendForKeyItem(0, nowValue, joint, entitySeats, min, max);
            nowValue = this.addExtendForKeyItem(2, nowValue, joint, entitySeats, min, max);
            switch (this.axis) {
               case 0:
                  joint.setNewRotationX(nowValue);
                  break;
               case 1:
                  joint.setNewRotationY(nowValue);
                  break;
               default:
                  joint.setNewRotationZ(nowValue);
            }

         }
      }
   }

   protected int addExtendForKeyItem(int keyActNum, int nowValue, IJBJointParameterHolder.IJBJoint joint, EntitySeatPart entitySeats, int min, int max) {
      int amount;
      if (this.keyOrItem == 0) {
         if (entitySeats.isRiddenbyPressingExKey(joint.getParam(keyActNum))) {
            amount = joint.getParam(keyActNum + 1);
            nowValue += amount;
            if (nowValue < min) {
               nowValue = min;
            }

            if (nowValue > max) {
               nowValue = max;
            }
         }

         return nowValue;
      } else {
         if (entitySeats.isRiddenbyUsedItemList[joint.getParam(keyActNum)]) {
            amount = joint.getParam(keyActNum + 1);
            nowValue += amount;
            if (nowValue < min) {
               nowValue = min;
            }

            if (nowValue > max) {
               nowValue = max;
            }
         }

         return nowValue;
      }
   }

   public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
   }

   public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
   }

   public boolean isIgnoreRotationPrevFix() {
      return true;
   }
}
