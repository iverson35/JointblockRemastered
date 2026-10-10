package jp.mc.ancientred.jointblock.motion;

import java.text.ChoiceFormat;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.motion.common.AbstractPendulumActHandler;
import jp.mc.ancientred.jointblock.motion.common.CommonDescriptionDummyHandler;
import jp.mc.ancientred.jointblock.motion.common.PendulumHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BasicMotionBinder implements IJBMotionBinderItem.IJBMotionBinder {
   public static BasicMotionBinder instance;
   public static final int BIND_TYPE_STILL = 0;
   public static final int BIND_TYPE_ACTIONTICK = 1;
   public static final int BIND_TYPE_ACTIONMOVE = 2;
   public static final int BIND_TYPE_SCALER_ACTIONTICK_X = 30;
   public static final int BIND_TYPE_SCALER_ACTIONTICK_Y = 31;
   public static final int BIND_TYPE_SCALER_ACTIONTICK_Z = 32;
   public static final int BIND_TYPE_SCALER_ACTIONMOVE_X = 40;
   public static final int BIND_TYPE_SCALER_ACTIONMOVE_Y = 41;
   public static final int BIND_TYPE_SCALER_ACTIONMOVE_Z = 42;
   public static final int BIND_TYPE_ROTATOR_ACTIONTICK_EX_X = 50;
   public static final int BIND_TYPE_ROTATOR_ACTIONTICK_EX_Y = 51;
   public static final int BIND_TYPE_ROTATOR_ACTIONTICK_EX_Z = 52;
   public static final int BIND_TYPE_ROTATOR_ACTIONMOVE_EX_X = 60;
   public static final int BIND_TYPE_ROTATOR_ACTIONMOVE_EX_Y = 61;
   public static final int BIND_TYPE_ROTATOR_ACTIONMOVE_EX_Z = 62;
   private static final int[] motionJointBindList = new int[]{0, 1, 2};
   private static final int[] sliderJointBlindList = new int[]{0, 30, 31, 32, 40, 41, 42};
   private static final int[] rotatorJointBlindList = new int[]{0, 1, 2, 50, 51, 52, 60, 61, 62};
   private static final int[] staticJointBindList = new int[]{0};
   private static final CommonDescriptionDummyHandler bindStillHandler = new CommonDescriptionDummyHandler() {
      public String getMotionHandlerName() {
         return "mhndl.basic.bindstill.name";
      }

      public int getBindNum() {
         return 0;
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings buttonSettings) {
         switch (jointType) {
            case 2:
               buttonSettings.setDescriptionText("jointtype.seat.desc", 0);
            case 3:
            case 5:
            case 6:
            case 7:
            case 9:
            case 11:
            case 13:
            default:
               break;
            case 4:
               buttonSettings.setDescriptionText("jointtype.normal.desc", 0);
               break;
            case 8:
               buttonSettings.setDescriptionText("jointtype.rotator.desc", 0);
               break;
            case 10:
               buttonSettings.setDescriptionText("jointtype.extender.desc", 0);
               break;
            case 12:
               buttonSettings.setDescriptionText("jointtype.scaler.desc", 0);
               break;
            case 14:
               buttonSettings.setDescriptionText("jointtype.slider.desc", 0);
               break;
            case 15:
               buttonSettings.setDescriptionText("jointtype.attracter.desc", 0);
         }

      }
   };
   private static final IJBMotionBinderItem.IJBMotionHandler bindActOnTickHandler = new CommonActHandler() {
      public int getBindNum() {
         return 1;
      }

      public String getMotionHandlerName() {
         return "mhndl.basic.bindactontick.name";
      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         return true;
      }
   };
   private static final IJBMotionBinderItem.IJBMotionHandler bindActOnMoveHandler = new CommonActHandler() {
      public int getBindNum() {
         return 2;
      }

      public String getMotionHandlerName() {
         return "mhndl.basic.bindactonmove.name";
      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
            return true;
         } else {
            double d0 = entityLivingBase.getX() - entityLivingBase.xo;
            double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
            double d2 = entityLivingBase.getY() - entityLivingBase.yo;
            float distMoved = (float)Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            return (double)distMoved > 0.1;
         }
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
            ((JointParts)joint).setToIninitalValue();
         } else {
            super.doUpdateJointState(entityLivingBase, joint);
         }
      }
   };
   private static final IJBMotionBinderItem.IJBMotionHandler[] BASIC_SLIDER_TICK_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new BasicSliderActHandler(0, 0), new BasicSliderActHandler(0, 1), new BasicSliderActHandler(0, 2)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] BASIC_SLIDER_MOVE_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new BasicSliderActHandler(1, 0), new BasicSliderActHandler(1, 1), new BasicSliderActHandler(1, 2)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] BASIC_ROTATOR_TICK_EX_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new BasicRotatorActHandlerEx(0, 0), new BasicRotatorActHandlerEx(0, 1), new BasicRotatorActHandlerEx(0, 2)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] BASIC_ROTATOR_MOVE_EX_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new BasicRotatorActHandlerEx(1, 0), new BasicRotatorActHandlerEx(1, 1), new BasicRotatorActHandlerEx(1, 2)};

   public String getBinderName() {
      return "mbinder.basic.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      switch (bindNum) {
         case 0:
            return bindStillHandler;
         case 1:
            return bindActOnTickHandler;
         case 2:
            return bindActOnMoveHandler;
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         default:
            return bindStillHandler;
         case 30:
            return BASIC_SLIDER_TICK_LIST[0];
         case 31:
            return BASIC_SLIDER_TICK_LIST[1];
         case 32:
            return BASIC_SLIDER_TICK_LIST[2];
         case 40:
            return BASIC_SLIDER_MOVE_LIST[0];
         case 41:
            return BASIC_SLIDER_MOVE_LIST[1];
         case 42:
            return BASIC_SLIDER_MOVE_LIST[2];
         case 50:
            return BASIC_ROTATOR_TICK_EX_LIST[0];
         case 51:
            return BASIC_ROTATOR_TICK_EX_LIST[1];
         case 52:
            return BASIC_ROTATOR_TICK_EX_LIST[2];
         case 60:
            return BASIC_ROTATOR_MOVE_EX_LIST[0];
         case 61:
            return BASIC_ROTATOR_MOVE_EX_LIST[1];
         case 62:
            return BASIC_ROTATOR_MOVE_EX_LIST[2];
      }
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      if (jointType == 14) {
         return sliderJointBlindList;
      } else if (jointType == 8) {
         return rotatorJointBlindList;
      } else {
         return jointType != 10 && jointType != 12 ? staticJointBindList : motionJointBindList;
      }
   }

   private static class BasicRotatorActHandlerEx extends AbstractPendulumActHandler {
      public BasicRotatorActHandlerEx(int type, int axis) {
         this.type = type;
         this.axis = axis;
         this.amountEntryNum = 0;
         this.minEntryNum = 1;
         this.maxEntryNum = 2;
         this.patternEntryNum = 3;
         if (this.type == 1) {
            this.enableAutoBackOnKeyItemUpEntry = true;
            this.autoBackEntryNum = 4;
         }

      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[this.amountEntryNum] = 0;
         params[this.minEntryNum] = -90;
         params[this.maxEntryNum] = 90;
         super.setCommonParamsInitialValue(jointType, params);
      }

      public int getBindNum() {
         if (this.type == 0) {
            switch (this.axis) {
               case 0:
                  return 50;
               case 1:
                  return 51;
               default:
                  return 52;
            }
         } else {
            switch (this.axis) {
               case 0:
                  return 60;
               case 1:
                  return 61;
               default:
                  return 62;
            }
         }
      }

      public String getMotionHandlerName() {
         if (this.type == 0) {
            switch (this.axis) {
               case 0:
                  return "mhndl.basic.pendulumtick.name, [X]";
               case 1:
                  return "mhndl.basic.pendulumtick.name, [Y]";
               default:
                  return "mhndl.basic.pendulumtick.name, [Z]";
            }
         } else {
            switch (this.axis) {
               case 0:
                  return "mhndl.basic.pendulummove.name, [X]";
               case 1:
                  return "mhndl.basic.pendulummove.name, [Y]";
               default:
                  return "mhndl.basic.pendulummove.name, [Z]";
            }
         }
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         switch (this.axis) {
            case 0:
               guiSettings.setParamEntryState(this.amountEntryNum, (ChoiceFormat)EntryFormats.rotateXTitleFormat, (String)"jbcommon.xangledegreese.format", -180, 180);
               break;
            case 1:
               guiSettings.setParamEntryState(this.amountEntryNum, (ChoiceFormat)EntryFormats.rotateYTitleFormat, (String)"jbcommon.yangledegreese.format", -180, 180);
               break;
            default:
               guiSettings.setParamEntryState(this.amountEntryNum, (ChoiceFormat)EntryFormats.rotateZTitleFormat, (String)"jbcommon.zangledegreese.format", -180, 180);
         }

         guiSettings.setParamEntryState(this.minEntryNum, (String)"basicmot.rotate.min.tilte", (String)"jbcommon.angledegreese.format", -359, 359);
         guiSettings.setParamEntryState(this.maxEntryNum, (String)"basicmot.rotate.max.title", (String)"jbcommon.angledegreese.format", -359, 359);
         super.setCommonGuiEntryState(jointType, guiSettings);
      }
   }

   private static class BasicSliderActHandler extends AbstractPendulumActHandler {
      public BasicSliderActHandler(int type, int axis) {
         this.type = type;
         this.axis = axis;
         this.amountEntryNum = 0;
         this.minEntryNum = 1;
         this.maxEntryNum = 2;
         this.patternEntryNum = 3;
         if (this.type == 1) {
            this.enableAutoBackOnKeyItemUpEntry = true;
            this.autoBackEntryNum = 4;
         }

      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[this.amountEntryNum] = 0;
         params[this.minEntryNum] = -64;
         params[this.maxEntryNum] = 64;
         super.setCommonParamsInitialValue(jointType, params);
      }

      public int getBindNum() {
         if (this.type == 0) {
            switch (this.axis) {
               case 0:
                  return 30;
               case 1:
                  return 31;
               default:
                  return 32;
            }
         } else {
            switch (this.axis) {
               case 0:
                  return 40;
               case 1:
                  return 41;
               default:
                  return 42;
            }
         }
      }

      public String getMotionHandlerName() {
         if (this.type == 0) {
            switch (this.axis) {
               case 0:
                  return "mhndl.basic.bindactontick.name, [X]";
               case 1:
                  return "mhndl.basic.bindactontick.name, [Y]";
               default:
                  return "mhndl.basic.bindactontick.name, [Z]";
            }
         } else {
            switch (this.axis) {
               case 0:
                  return "mhndl.basic.bindactonmove.name, [X]";
               case 1:
                  return "mhndl.basic.bindactonmove.name, [Y]";
               default:
                  return "mhndl.basic.bindactonmove.name, [Z]";
            }
         }
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         switch (this.axis) {
            case 0:
               guiSettings.setParamEntryState(this.amountEntryNum, (ChoiceFormat)EntryFormats.slideXTitleFormat, (String)"jbcommon.xslideramount.format", -128, 128);
               break;
            case 1:
               guiSettings.setParamEntryState(this.amountEntryNum, (ChoiceFormat)EntryFormats.slideYTitleFormat, (String)"jbcommon.yslideramount.format", -128, 128);
               break;
            default:
               guiSettings.setParamEntryState(this.amountEntryNum, (ChoiceFormat)EntryFormats.slideZTitleFormat, (String)"jbcommon.zslideramount.format", -128, 128);
         }

         guiSettings.setParamButtonToggleAry(this.amountEntryNum, EntryFormats.sliderAmountButtonStates, 0);
         guiSettings.setParamEntryState(this.minEntryNum, (String)"basicmot.slider.min.tilte", (String)"jbcommon.slideramount.format", -128, 128);
         guiSettings.setParamButtonToggleAry(this.minEntryNum, EntryFormats.sliderAmountButtonStates, 0);
         guiSettings.setParamEntryState(this.maxEntryNum, (String)"basicmot.slider.max.tilte", (String)"jbcommon.slideramount.format", -128, 128);
         guiSettings.setParamButtonToggleAry(this.maxEntryNum, EntryFormats.sliderAmountButtonStates, 0);
         super.setCommonGuiEntryState(jointType, guiSettings);
      }
   }

   private abstract static class CommonActHandler implements IJBMotionBinderItem.IJBMotionHandler {
      private CommonActHandler() {
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         switch (jointType) {
            case 8:
               params[0] = params[1] = params[2] = 0;
               return;
            case 9:
            case 11:
            default:
               return;
            case 10:
               params[0] = 0;
               params[1] = 100;
               params[2] = 200;
               return;
            case 12:
               params[0] = 0;
               params[1] = 0;
               params[2] = 100;
         }
      }

      public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
         if (joint.getJointType() == 8) {
            if (flipWay == 2) {
               joint.setParam(0, joint.getParam(0));
               joint.setParam(1, -joint.getParam(1));
               joint.setParam(2, -joint.getParam(2));
            } else if (flipWay == 1) {
               joint.setParam(0, -joint.getParam(0));
               joint.setParam(1, joint.getParam(1));
               joint.setParam(2, -joint.getParam(2));
            }
         }

      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         switch (joint.getJointType()) {
            case 8:
               this.handleRotatorMotion(entityLivingBase, joint);
               return;
            case 9:
            case 11:
            default:
               return;
            case 10:
            case 12:
               int amount = joint.getParam(0);
               int min = joint.getParam(1);
               int max = joint.getParam(2);
               PendulumHelper.handleExtenderMotion(entityLivingBase, joint, amount, min, max);
         }
      }

      private void handleRotatorMotion(Entity entity, IJBJointParameterHolder.IJBJoint joint) {
         joint.setNewRotationX(joint.getRotationX() + joint.getParam(0));
         joint.setNewRotationY(joint.getRotationY() + joint.getParam(1));
         joint.setNewRotationZ(joint.getRotationZ() + joint.getParam(2));
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         switch (jointType) {
            case 8:
               EntryFormats.setDefaultJointTypeButtonState(guiSettings, false, jointType);
            case 9:
            case 11:
            default:
               break;
            case 10:
               guiSettings.setParamEntryState(0, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
               guiSettings.setParamEntryState(1, (String)"basicmot.extend.min.tilte", (String)"jbcommon.percentage.valueformat", 0, 300);
               guiSettings.setParamEntryState(2, (String)"basicmot.extend.max.title", (String)"jbcommon.percentage.valueformat", 0, 300);
               break;
            case 12:
               guiSettings.setParamEntryState(0, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
               guiSettings.setParamEntryState(1, (String)"basicmot.extend.min.tilte", (String)"jbcommon.percentage.valueformat", 0, 300);
               guiSettings.setParamEntryState(2, (String)"basicmot.extend.max.title", (String)"jbcommon.percentage.valueformat", 0, 300);
         }

      }

      public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
      }

      public boolean isIgnoreRotationPrevFix() {
         return false;
      }
   }
}
