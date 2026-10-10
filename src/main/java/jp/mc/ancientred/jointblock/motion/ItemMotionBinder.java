package jp.mc.ancientred.jointblock.motion;

import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.motion.common.AbstractComplexKeyItemExtenderScalerHandler;
import jp.mc.ancientred.jointblock.motion.common.AbstractComplexKeyItemThreeValuesHandler;
import jp.mc.ancientred.jointblock.motion.common.AbstractPendulumActHandler;
import jp.mc.ancientred.jointblock.motion.common.EMountKeyItemBindedHandler;
import jp.mc.ancientred.jointblock.motion.common.EffectKeyItemBindedHandler;
import jp.mc.ancientred.jointblock.motion.common.ModeChangeKeyItemBindedHandler;
import jp.mc.ancientred.jointblock.motion.common.PendulumHelper;
import jp.mc.ancientred.jointblock.motion.common.SlavePurgeKeyItemBindedHandler;
import jp.mc.ancientred.jointblock.motion.common.SlavePurgeKeyItemComplexHandler;
import net.minecraft.world.entity.LivingEntity;

public class ItemMotionBinder implements IJBMotionBinderItem.IJBMotionBinder {
   public static final int MAX_ITEM_INDEX = 7;
   public static final int BIND_TYPE_SIMPLE_ITEM = 10;
   public static final int BIND_TYPE_COMPLEX_EXTENDER_SCALER_ITEM = 20;
   public static final int BIND_TYPE_COMPLEX_ROTATOR_X_ITEM = 30;
   public static final int BIND_TYPE_COMPLEX_ROTATOR_Y_ITEM = 31;
   public static final int BIND_TYPE_COMPLEX_ROTATOR_Z_ITEM = 32;
   public static final int BIND_TYPE_SLAVE_PURGE_ITEM = 42;
   public static final int BIND_TYPE_SLAVE_PURGE_COMPLEX_ITEM = 43;
   public static final int BIND_TYPE_INITMODE_CHANGE_ITEM = 44;
   public static final int BIND_TYPE_SLAVEMODE_CHANGE_ITEM = 45;
   public static final int BIND_TYPE_EMOUNT_ITEM = 46;
   public static final int BIND_TYPE_SOUND_ITEM = 47;
   public static final int BIND_TYPE_COMPLEX_SLIDER_X_ITEM = 50;
   public static final int BIND_TYPE_COMPLEX_SLIDER_Y_ITEM = 51;
   public static final int BIND_TYPE_COMPLEX_SLIDER_Z_ITEM = 52;
   public static final int BIND_TYPE_PENDULUM_ROTATOR_X_ITEM = 60;
   public static final int BIND_TYPE_PENDULUM_ROTATOR_Y_ITEM = 61;
   public static final int BIND_TYPE_PENDULUM_ROTATOR_Z_ITEM = 62;
   public static final int BIND_TYPE_PENDULUM_SLIDER_X_ITEM = 70;
   public static final int BIND_TYPE_PENDULUM_SLIDER_Y_ITEM = 71;
   public static final int BIND_TYPE_PENDULUM_SLIDER_Z_ITEM = 72;
   public static final int BIND_TYPE_SPRING_EXTENDER_SCALER_ITEM = 80;
   public static final String[] seatNames = new String[]{"jbcommon.jbseat0.name", "jbcommon.jbseat1.name", "jbcommon.jbseat2.name", "jbcommon.jbseat3.name"};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SIMPLEITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new SimpleItemActBindedHandler(0), new SimpleItemActBindedHandler(1), new SimpleItemActBindedHandler(2), new SimpleItemActBindedHandler(3)};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] COMP_R_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new ComplexItemActBindedRotatorHandler(0, 0), new ComplexItemActBindedRotatorHandler(0, 1), new ComplexItemActBindedRotatorHandler(0, 2)}, {new ComplexItemActBindedRotatorHandler(1, 0), new ComplexItemActBindedRotatorHandler(1, 1), new ComplexItemActBindedRotatorHandler(1, 2)}, {new ComplexItemActBindedRotatorHandler(2, 0), new ComplexItemActBindedRotatorHandler(2, 1), new ComplexItemActBindedRotatorHandler(2, 2)}, {new ComplexItemActBindedRotatorHandler(3, 0), new ComplexItemActBindedRotatorHandler(3, 1), new ComplexItemActBindedRotatorHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] COMP_Sl_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new ComplexItemBindedSliderHandler(0, 0), new ComplexItemBindedSliderHandler(0, 1), new ComplexItemBindedSliderHandler(0, 2)}, {new ComplexItemBindedSliderHandler(1, 0), new ComplexItemBindedSliderHandler(1, 1), new ComplexItemBindedSliderHandler(1, 2)}, {new ComplexItemBindedSliderHandler(2, 0), new ComplexItemBindedSliderHandler(2, 1), new ComplexItemBindedSliderHandler(2, 2)}, {new ComplexItemBindedSliderHandler(3, 0), new ComplexItemBindedSliderHandler(3, 1), new ComplexItemBindedSliderHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] PENDULUM_R_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new PendulumItemBindedRotatorHandler(0, 0), new PendulumItemBindedRotatorHandler(0, 1), new PendulumItemBindedRotatorHandler(0, 2)}, {new PendulumItemBindedRotatorHandler(1, 0), new PendulumItemBindedRotatorHandler(1, 1), new PendulumItemBindedRotatorHandler(1, 2)}, {new PendulumItemBindedRotatorHandler(2, 0), new PendulumItemBindedRotatorHandler(2, 1), new PendulumItemBindedRotatorHandler(2, 2)}, {new PendulumItemBindedRotatorHandler(3, 0), new PendulumItemBindedRotatorHandler(3, 1), new PendulumItemBindedRotatorHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] PENDULUM_Sl_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new PendulumItemBindedSliderHandler(0, 0), new PendulumItemBindedSliderHandler(0, 1), new PendulumItemBindedSliderHandler(0, 2)}, {new PendulumItemBindedSliderHandler(1, 0), new PendulumItemBindedSliderHandler(1, 1), new PendulumItemBindedSliderHandler(1, 2)}, {new PendulumItemBindedSliderHandler(2, 0), new PendulumItemBindedSliderHandler(2, 1), new PendulumItemBindedSliderHandler(2, 2)}, {new PendulumItemBindedSliderHandler(3, 0), new PendulumItemBindedSliderHandler(3, 1), new PendulumItemBindedSliderHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[] COMP_E_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new ComplexItemActBindedExtenderScalerHandler(0), new ComplexItemActBindedExtenderScalerHandler(1), new ComplexItemActBindedExtenderScalerHandler(2), new ComplexItemActBindedExtenderScalerHandler(3)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SLAVE_PURGE_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new SlavePurgeKeyItemBindedHandler(0, 1), new SlavePurgeKeyItemBindedHandler(1, 1), new SlavePurgeKeyItemBindedHandler(2, 1), new SlavePurgeKeyItemBindedHandler(3, 1)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SLAVE_PURGE_COMPLEX_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new SlavePurgeKeyItemComplexHandler(0, 1), new SlavePurgeKeyItemComplexHandler(1, 1), new SlavePurgeKeyItemComplexHandler(2, 1), new SlavePurgeKeyItemComplexHandler(3, 1)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] EMOUNT_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new EMountKeyItemBindedHandler(0, 1), new EMountKeyItemBindedHandler(1, 1), new EMountKeyItemBindedHandler(2, 1), new EMountKeyItemBindedHandler(3, 1)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] INITMODE_CHANGE_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new ModeChangeKeyItemBindedHandler(0, 0, 1), new ModeChangeKeyItemBindedHandler(1, 0, 1), new ModeChangeKeyItemBindedHandler(2, 0, 1), new ModeChangeKeyItemBindedHandler(3, 0, 1)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SLAVEMODE_CHANGE_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new ModeChangeKeyItemBindedHandler(0, 1, 1), new ModeChangeKeyItemBindedHandler(1, 1, 1), new ModeChangeKeyItemBindedHandler(2, 1, 1), new ModeChangeKeyItemBindedHandler(3, 1, 1)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] EFFECT_ITEMBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new EffectKeyItemBindedHandler(0, 1), new EffectKeyItemBindedHandler(1, 1), new EffectKeyItemBindedHandler(2, 1), new EffectKeyItemBindedHandler(3, 1)};
   private static final int[] motionNBindList = new int[]{10, 44, 45, 47};
   private static final int[] motionAtBindList = new int[]{42, 43};
   private static final int[] motionSeatBindList = new int[]{46};
   private static final int[] motionRBindList = new int[]{10, 30, 31, 32, 60, 61, 62};
   private static final int[] motionSlBindList = new int[]{10, 50, 51, 52, 70, 71, 72};
   private static final int[] motionEBindList = new int[]{10, 20};

   public String getBinderName() {
      return "mbinder.itemresponse.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      if (itemDamage > 3) {
         itemDamage = 3;
      }

      if (bindNum == 44) {
         return INITMODE_CHANGE_ITEMBIND_LIST[itemDamage];
      } else if (bindNum == 45) {
         return SLAVEMODE_CHANGE_ITEMBIND_LIST[itemDamage];
      } else if (bindNum == 42) {
         return SLAVE_PURGE_ITEMBIND_LIST[itemDamage];
      } else if (bindNum == 43) {
         return SLAVE_PURGE_COMPLEX_ITEMBIND_LIST[itemDamage];
      } else if (bindNum == 46) {
         return EMOUNT_ITEMBIND_LIST[itemDamage];
      } else if (bindNum == 47) {
         return EFFECT_ITEMBIND_LIST[itemDamage];
      } else if (bindNum == 20) {
         return COMP_E_ITEMBIND_LIST[itemDamage];
      } else if (bindNum == 10) {
         return SIMPLEITEMBIND_LIST[itemDamage];
      } else {
         int sub;
         if (30 <= bindNum && bindNum <= 32) {
            sub = bindNum - 30;
            if (sub >= 0 && sub < 3) {
               return COMP_R_ITEMBIND_LIST[itemDamage][sub];
            }
         } else if (50 <= bindNum && bindNum <= 52) {
            sub = bindNum - 50;
            if (sub >= 0 && sub < 3) {
               return COMP_Sl_ITEMBIND_LIST[itemDamage][sub];
            }
         } else if (60 <= bindNum && bindNum <= 62) {
            sub = bindNum - 60;
            if (sub >= 0 && sub < 3) {
               return PENDULUM_R_ITEMBIND_LIST[itemDamage][sub];
            }
         } else if (70 <= bindNum && bindNum <= 72) {
            sub = bindNum - 70;
            if (sub >= 0 && sub < 3) {
               return PENDULUM_Sl_ITEMBIND_LIST[itemDamage][sub];
            }
         }

         return null;
      }
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      switch (jointType) {
         case 2:
            return motionSeatBindList;
         case 3:
         case 5:
         case 6:
         case 7:
         case 9:
         case 11:
         case 13:
         default:
            return null;
         case 4:
            return motionNBindList;
         case 8:
            return motionRBindList;
         case 10:
         case 12:
            return motionEBindList;
         case 14:
            return motionSlBindList;
         case 15:
            return motionAtBindList;
      }
   }

   private static class PendulumItemBindedSliderHandler extends AbstractPendulumActHandler {
      public PendulumItemBindedSliderHandler(int seatNum, int axis) {
         this.type = 3;
         this.seatNum = seatNum;
         this.axis = axis;
         this.keyItemEntryNum = 0;
         this.amountEntryNum = 1;
         this.minEntryNum = 2;
         this.maxEntryNum = 3;
         this.patternEntryNum = 4;
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[this.keyItemEntryNum] = 0;
         params[this.amountEntryNum] = 0;
         params[this.minEntryNum] = -64;
         params[this.maxEntryNum] = 64;
         super.setCommonParamsInitialValue(jointType, params);
      }

      public int getBindNum() {
         switch (this.axis) {
            case 0:
               return 70;
            case 1:
               return 71;
            default:
               return 72;
         }
      }

      public String getMotionHandlerName() {
         switch (this.axis) {
            case 0:
               return "mhndl.keyresponse.pendulum.name, [X]";
            case 1:
               return "mhndl.keyresponse.pendulum.name, [Y]";
            default:
               return "mhndl.keyresponse.pendulum.name, [Z]";
         }
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(this.keyItemEntryNum, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(this.keyItemEntryNum, EntryFormats.selectLRButtonStates, 0);
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

   private static class PendulumItemBindedRotatorHandler extends AbstractPendulumActHandler {
      public PendulumItemBindedRotatorHandler(int seatNum, int axis) {
         this.type = 3;
         this.seatNum = seatNum;
         this.axis = axis;
         this.keyItemEntryNum = 0;
         this.amountEntryNum = 1;
         this.minEntryNum = 2;
         this.maxEntryNum = 3;
         this.patternEntryNum = 4;
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[this.keyItemEntryNum] = 0;
         params[this.amountEntryNum] = 0;
         params[this.minEntryNum] = -90;
         params[this.maxEntryNum] = 90;
         super.setCommonParamsInitialValue(jointType, params);
      }

      public int getBindNum() {
         switch (this.axis) {
            case 0:
               return 60;
            case 1:
               return 61;
            default:
               return 62;
         }
      }

      public String getMotionHandlerName() {
         switch (this.axis) {
            case 0:
               return "mhndl.keyresponse.pendulum.name, [X]";
            case 1:
               return "mhndl.keyresponse.pendulum.name, [Y]";
            default:
               return "mhndl.keyresponse.pendulum.name, [Z]";
         }
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(this.keyItemEntryNum, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
         guiSettings.setParamButtonToggleAry(this.keyItemEntryNum, EntryFormats.selectLRButtonStates, 0);
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

   private static class ComplexItemBindedSliderHandler extends AbstractComplexKeyItemThreeValuesHandler {
      public ComplexItemBindedSliderHandler(int seatNum, int axis) {
         this.keyOrItem = 1;
         this.seatNum = seatNum;
         this.axis = axis;
      }

      public int getBindNum() {
         switch (this.axis) {
            case 0:
               return 50;
            case 1:
               return 51;
            default:
               return 52;
         }
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 0;
         params[2] = 0;
         params[3] = 0;
         params[4] = -64;
         params[5] = 64;
      }
   }

   private static class ComplexItemActBindedRotatorHandler extends AbstractComplexKeyItemThreeValuesHandler {
      public ComplexItemActBindedRotatorHandler(int seatNum, int axis) {
         this.keyOrItem = 1;
         this.seatNum = seatNum;
         this.axis = axis;
      }

      public int getBindNum() {
         switch (this.axis) {
            case 0:
               return 30;
            case 1:
               return 31;
            default:
               return 32;
         }
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 0;
         params[2] = 0;
         params[3] = 0;
         params[4] = -359;
         params[5] = 359;
      }
   }

   private static class ComplexItemActBindedExtenderScalerHandler extends AbstractComplexKeyItemExtenderScalerHandler {
      public ComplexItemActBindedExtenderScalerHandler(int seatNum) {
         this.keyOrItem = 1;
         this.seatNum = seatNum;
      }

      public int getBindNum() {
         return 20;
      }
   }

   private static class SimpleItemActBindedHandler implements IJBMotionBinderItem.IJBMotionHandler {
      private int seatNum;

      public SimpleItemActBindedHandler(int seatNum) {
         this.seatNum = seatNum;
      }

      public int getBindNum() {
         return 10;
      }

      public String getMotionHandlerName() {
         return "mhndl.itemresponse.simple.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         switch (jointType) {
            case 4:
            case 14:
               params[0] = 0;
               params[1] = 0;
            case 5:
            case 6:
            case 7:
            case 9:
            case 11:
            case 13:
            default:
               return;
            case 8:
               params[0] = 0;
               params[1] = params[2] = params[3] = 0;
               params[4] = 0;
               return;
            case 10:
               params[0] = 0;
               params[1] = 0;
               params[2] = 100;
               params[3] = 200;
               params[4] = 0;
               return;
            case 12:
               params[0] = 0;
               params[1] = 0;
               params[2] = 0;
               params[3] = 100;
               params[4] = 0;
         }
      }

      public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
         if (joint.getJointType() == 8) {
            if (flipWay == 2) {
               joint.setParam(1, joint.getParam(1));
               joint.setParam(2, -joint.getParam(2));
               joint.setParam(3, -joint.getParam(3));
            } else if (flipWay == 1) {
               joint.setParam(1, -joint.getParam(1));
               joint.setParam(2, joint.getParam(2));
               joint.setParam(3, -joint.getParam(3));
            }
         }

      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         switch (jointType) {
            case 4:
            case 14:
               guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
               guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
               EntryFormats.setDefaultKeyActionButtonState(guiSettings, 1);
            case 5:
            case 6:
            case 7:
            case 9:
            case 11:
            case 13:
            default:
               break;
            case 8:
               guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
               guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
               EntryFormats.setDefaultJointTypeButtonState(guiSettings, jointType, false, 1);
               EntryFormats.setDefaultKeyActionButtonState(guiSettings, 4);
               break;
            case 10:
               guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
               guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
               guiSettings.setParamEntryState(1, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
               guiSettings.setParamEntryState(2, (String)"basicmot.extend.min.tilte", (String)"jbcommon.percentage.valueformat", 0, 300);
               guiSettings.setParamEntryState(3, (String)"basicmot.extend.max.title", (String)"jbcommon.percentage.valueformat", 0, 300);
               EntryFormats.setDefaultKeyActionButtonState(guiSettings, 4);
               break;
            case 12:
               guiSettings.setParamEntryState(0, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
               guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
               guiSettings.setParamEntryState(1, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
               guiSettings.setParamEntryState(2, (String)"basicmot.extend.min.tilte", (String)"jbcommon.percentage.valueformat", 0, 300);
               guiSettings.setParamEntryState(3, (String)"basicmot.extend.max.title", (String)"jbcommon.percentage.valueformat", 0, 300);
               EntryFormats.setDefaultKeyActionButtonState(guiSettings, 4);
         }

      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         if (joint.getJointType() != 4 && joint.getJointType() != 14) {
            EntitySeatPart entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
            return entitySeats != null && entitySeats.canKeyItemUsed() ? entitySeats.isRiddenbyUsedItem(joint.getParam(0)) : false;
         } else {
            return false;
         }
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
               int amount = joint.getParam(1);
               int min = joint.getParam(2);
               int max = joint.getParam(3);
               PendulumHelper.handleExtenderMotion(entityLivingBase, joint, amount, min, max);
         }
      }

      private void handleRotatorMotion(LivingEntity entity, IJBJointParameterHolder.IJBJoint joint) {
         joint.setNewRotationX(joint.getRotationX() + joint.getParam(1));
         joint.setNewRotationY(joint.getRotationY() + joint.getParam(2));
         joint.setNewRotationZ(joint.getRotationZ() + joint.getParam(3));
      }

      public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
         EntitySeatPart entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
         if (joint.hasModelAction() && entitySeats != null && entitySeats.canKeyItemUsed()) {
            boolean doAction = false;
            switch (joint.getJointType()) {
               case 4:
               case 14:
                  doAction = joint.getParam(1) == 1;
               case 5:
               case 6:
               case 7:
               case 9:
               case 11:
               case 13:
               default:
                  break;
               case 8:
                  doAction = joint.getParam(4) == 1;
                  break;
               case 10:
               case 12:
                  doAction = joint.getParam(4) == 1;
            }

            if (doAction && entitySeats.isRiddenbyUsedItem(joint.getParam(0))) {
               joint.dispatchModelAction(entitySeats.getActionDispatcher());
            }

         }
      }

      public boolean isIgnoreRotationPrevFix() {
         return false;
      }
   }
}
