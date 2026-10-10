package jp.mc.ancientred.jointblock.motion.common;

import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractSpringKeyItemThreeValuesHandler extends AbstractComplexKeyItemThreeValuesHandler {
   public String getMotionHandlerName() {
      if (this.keyOrItem == 0) {
         switch (this.axis) {
            case 0:
               return "mhndl.keyresponse.spring.name, [X]";
            case 1:
               return "mhndl.keyresponse.spring.name, [Y]";
            default:
               return "mhndl.keyresponse.spring.name, [Z]";
         }
      } else {
         switch (this.axis) {
            case 0:
               return "mhndl.itemresponse.spring.name, [X]";
            case 1:
               return "mhndl.itemresponse.spring.name, [Y]";
            default:
               return "mhndl.itemresponse.spring.name, [Z]";
         }
      }
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      if (this.keyOrItem == 0) {
         guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      } else {
         guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      }

      guiSettings.setParamEntryState(2, (String)"cmnmot.springbacktype.title", (Format)EntryFormats.springBackTypeFormat, 0, 1);
      guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      guiSettings.setParamEntryState(3, (String)"cmnmot.springbackamount.title", (String)"jbcommon.simplenum.valueformat", 0, 180);
      if (jointType == 8) {
         switch (this.axis) {
            case 0:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.rotateXTitleFormat, (String)"jbcommon.xangledegreese.format", -180, 180);
               break;
            case 1:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.rotateYTitleFormat, (String)"jbcommon.yangledegreese.format", -180, 180);
               break;
            default:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.rotateZTitleFormat, (String)"jbcommon.zangledegreese.format", -180, 180);
         }

         guiSettings.setParamEntryState(4, (String)"basicmot.rotate.min.tilte", (String)"jbcommon.angledegreese.format", -180, 180);
         guiSettings.setParamEntryState(5, (String)"basicmot.rotate.max.title", (String)"jbcommon.angledegreese.format", -180, 180);
      } else {
         switch (this.axis) {
            case 0:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.slideXTitleFormat, (String)"jbcommon.xslideramount.format", -128, 128);
               break;
            case 1:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.slideYTitleFormat, (String)"jbcommon.yslideramount.format", -128, 128);
               break;
            default:
               guiSettings.setParamEntryState(1, (ChoiceFormat)EntryFormats.slideZTitleFormat, (String)"jbcommon.zslideramount.format", -128, 128);
         }

         guiSettings.setParamButtonToggleAry(1, EntryFormats.sliderAmountButtonStates, 0);
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
                  break;
               case 1:
                  joint.setParam(1, -joint.getParam(1));
                  doMinMaxFlip = true;
                  break;
               default:
                  joint.setParam(1, -joint.getParam(1));
                  doMinMaxFlip = true;
            }
         } else if (flipWay == 1) {
            switch (this.axis) {
               case 0:
                  joint.setParam(1, -joint.getParam(1));
                  doMinMaxFlip = true;
                  break;
               case 1:
                  joint.setParam(1, joint.getParam(1));
                  break;
               default:
                  joint.setParam(1, -joint.getParam(1));
                  doMinMaxFlip = true;
            }
         }
      } else if (joint.getJointType() == 14) {
         if (flipWay == 2) {
            switch (this.axis) {
               case 0:
                  joint.setParam(1, -joint.getParam(1));
                  doMinMaxFlip = true;
                  break;
               case 1:
                  joint.setParam(1, joint.getParam(1));
                  break;
               default:
                  joint.setParam(1, joint.getParam(1));
            }
         } else if (flipWay == 1) {
            switch (this.axis) {
               case 0:
                  joint.setParam(1, joint.getParam(1));
                  break;
               case 1:
                  joint.setParam(1, -joint.getParam(1));
                  doMinMaxFlip = true;
                  break;
               default:
                  joint.setParam(1, joint.getParam(1));
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
      return true;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      EntitySeatPart entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
      if (entitySeats != null && entitySeats.canKeyItemUsed()) {
         if (this.keyOrItem == 0) {
            if (entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
               super.doUpdateJointState(entityLivingBase, joint);
               return;
            }
         } else if (entitySeats.isRiddenbyUsedItem(joint.getParam(0))) {
            super.doUpdateJointState(entityLivingBase, joint);
            return;
         }
      }

      JointParts jp = (JointParts)joint;
      int backAmount = jp.getParam(3);
      if (backAmount > 0) {
         int nowValue;
         int diff;
         int fixValue;
         if (jp.getParam(2) == 0) {
            nowValue = jp.getRotationX();
            if (jp.fixedInitVals[0] != nowValue) {
               diff = jp.fixedInitVals[0] - nowValue;
               if (diff > 0) {
                  if (backAmount >= diff) {
                     fixValue = diff;
                  } else {
                     fixValue = backAmount;
                  }
               } else if (backAmount >= -diff) {
                  fixValue = diff;
               } else {
                  fixValue = -backAmount;
               }

               jp.setNewRotationX(nowValue + fixValue);
            }

            nowValue = jp.getRotationY();
            if (jp.fixedInitVals[1] != nowValue) {
               diff = jp.fixedInitVals[1] - nowValue;
               if (diff > 0) {
                  if (backAmount >= diff) {
                     fixValue = diff;
                  } else {
                     fixValue = backAmount;
                  }
               } else if (backAmount >= -diff) {
                  fixValue = diff;
               } else {
                  fixValue = -backAmount;
               }

               jp.setNewRotationY(nowValue + fixValue);
            }

            nowValue = jp.getRotationZ();
            if (jp.fixedInitVals[2] != nowValue) {
               diff = jp.fixedInitVals[2] - nowValue;
               if (diff > 0) {
                  if (backAmount >= diff) {
                     fixValue = diff;
                  } else {
                     fixValue = backAmount;
                  }
               } else if (backAmount >= -diff) {
                  fixValue = diff;
               } else {
                  fixValue = -backAmount;
               }

               jp.setNewRotationZ(nowValue + fixValue);
            }
         } else {
            if (backAmount > 100) {
               backAmount = 100;
            }

            float backRateF = (float)backAmount / 100.0F;
            nowValue = jp.getRotationX();
            if (jp.fixedInitVals[0] != nowValue) {
               diff = jp.fixedInitVals[0] - nowValue;
               fixValue = Math.round((float)diff * backRateF);
               if (fixValue == 0) {
                  fixValue = diff > 0 ? 1 : -1;
               }

               jp.setNewRotationX(nowValue + fixValue);
            }

            nowValue = jp.getRotationY();
            if (jp.fixedInitVals[1] != nowValue) {
               diff = jp.fixedInitVals[1] - nowValue;
               fixValue = Math.round((float)diff * backRateF);
               if (fixValue == 0) {
                  fixValue = diff > 0 ? 1 : -1;
               }

               jp.setNewRotationY(nowValue + fixValue);
            }

            nowValue = jp.getRotationZ();
            if (jp.fixedInitVals[2] != nowValue) {
               diff = jp.fixedInitVals[2] - nowValue;
               fixValue = Math.round((float)diff * backRateF);
               if (fixValue == 0) {
                  fixValue = diff > 0 ? 1 : -1;
               }

               jp.setNewRotationZ(nowValue + fixValue);
            }
         }

      }
   }

   protected int addExtendForKeyItem(int keyActNum, int nowValue, IJBJointParameterHolder.IJBJoint joint, EntitySeatPart entitySeats, int min, int max) {
      return keyActNum != 0 ? nowValue : super.addExtendForKeyItem(keyActNum, nowValue, joint, entitySeats, min, max);
   }
}
