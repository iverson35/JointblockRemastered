package jp.mc.ancientred.jointblock.motion;

import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.motion.common.AbstractComplexKeyItemExtenderScalerHandler;
import jp.mc.ancientred.jointblock.motion.common.AbstractComplexKeyItemThreeValuesHandler;
import jp.mc.ancientred.jointblock.motion.common.AbstractPendulumActHandler;
import jp.mc.ancientred.jointblock.motion.common.AbstractSpringKeyItemExtenderScalerHandler;
import jp.mc.ancientred.jointblock.motion.common.AbstractSpringKeyItemThreeValuesHandler;
import jp.mc.ancientred.jointblock.motion.common.EMountKeyItemBindedHandler;
import jp.mc.ancientred.jointblock.motion.common.EffectKeyItemBindedHandler;
import jp.mc.ancientred.jointblock.motion.common.ModeChangeKeyItemBindedHandler;
import jp.mc.ancientred.jointblock.motion.common.PendulumHelper;
import jp.mc.ancientred.jointblock.motion.common.SlavePurgeKeyItemBindedHandler;
import jp.mc.ancientred.jointblock.motion.common.SlavePurgeKeyItemComplexHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class KeyMotionBinder implements IJBMotionBinderItem.IJBMotionBinder {
   public static final int MAX_KEY_INDEX = 15;
   public static final int BIND_TYPE_SIMPLE_KEY = 10;
   public static final int BIND_TYPE_COMPLEX_EXTENDER_SCALER_KEY = 20;
   public static final int BIND_TYPE_COMPLEX_ROTATOR_X_KEY = 30;
   public static final int BIND_TYPE_COMPLEX_ROTATOR_Y_KEY = 31;
   public static final int BIND_TYPE_COMPLEX_ROTATOR_Z_KEY = 32;
   public static final int BIND_TYPE_FOLLOWLOOK = 40;
   public static final int BIND_TYPE_FOLLOWTARGET = 41;
   public static final int BIND_TYPE_SLAVE_PURGE_KEY = 42;
   public static final int BIND_TYPE_SLAVE_PURGE_COMPLEX_KEY = 43;
   public static final int BIND_TYPE_INITMODE_CHANGE_KEY = 44;
   public static final int BIND_TYPE_SLAVEMODE_CHANGE_KEY = 45;
   public static final int BIND_TYPE_EMOUNT_KEY = 46;
   public static final int BIND_TYPE_SOUND_KEY = 47;
   public static final int BIND_TYPE_COMPLEX_SLIDER_X_KEY = 50;
   public static final int BIND_TYPE_COMPLEX_SLIDER_Y_KEY = 51;
   public static final int BIND_TYPE_COMPLEX_SLIDER_Z_KEY = 52;
   public static final int BIND_TYPE_PENDULUM_ROTATOR_X_KEY = 60;
   public static final int BIND_TYPE_PENDULUM_ROTATOR_Y_KEY = 61;
   public static final int BIND_TYPE_PENDULUM_ROTATOR_Z_KEY = 62;
   public static final int BIND_TYPE_PENDULUM_SLIDER_X_KEY = 70;
   public static final int BIND_TYPE_PENDULUM_SLIDER_Y_KEY = 71;
   public static final int BIND_TYPE_PENDULUM_SLIDER_Z_KEY = 72;
   public static final int BIND_TYPE_SPRING_EXTENDER_SCALER_KEY = 80;
   public static final int BIND_TYPE_SPRING_ROTATOR_X_KEY = 90;
   public static final int BIND_TYPE_SPRING_ROTATOR_Y_KEY = 91;
   public static final int BIND_TYPE_SPRING_ROTATOR_Z_KEY = 92;
   public static final int BIND_TYPE_SPRING_SLIDER_X_KEY = 100;
   public static final int BIND_TYPE_SPRING_SLIDER_Y_KEY = 101;
   public static final int BIND_TYPE_SPRING_SLIDER_Z_KEY = 102;
   public static final String[] seatNames = new String[]{"jbcommon.jbseat0.name", "jbcommon.jbseat1.name", "jbcommon.jbseat2.name", "jbcommon.jbseat3.name"};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SIMPLEKEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new SimpleKeyBindedHandler(0), new SimpleKeyBindedHandler(1), new SimpleKeyBindedHandler(2), new SimpleKeyBindedHandler(3)};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] COMP_R_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new ComplexKeyBindedRotatorHandler(0, 0), new ComplexKeyBindedRotatorHandler(0, 1), new ComplexKeyBindedRotatorHandler(0, 2)}, {new ComplexKeyBindedRotatorHandler(1, 0), new ComplexKeyBindedRotatorHandler(1, 1), new ComplexKeyBindedRotatorHandler(1, 2)}, {new ComplexKeyBindedRotatorHandler(2, 0), new ComplexKeyBindedRotatorHandler(2, 1), new ComplexKeyBindedRotatorHandler(2, 2)}, {new ComplexKeyBindedRotatorHandler(3, 0), new ComplexKeyBindedRotatorHandler(3, 1), new ComplexKeyBindedRotatorHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] COMP_Sl_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new ComplexKeyBindedSliderHandler(0, 0), new ComplexKeyBindedSliderHandler(0, 1), new ComplexKeyBindedSliderHandler(0, 2)}, {new ComplexKeyBindedSliderHandler(1, 0), new ComplexKeyBindedSliderHandler(1, 1), new ComplexKeyBindedSliderHandler(1, 2)}, {new ComplexKeyBindedSliderHandler(2, 0), new ComplexKeyBindedSliderHandler(2, 1), new ComplexKeyBindedSliderHandler(2, 2)}, {new ComplexKeyBindedSliderHandler(3, 0), new ComplexKeyBindedSliderHandler(3, 1), new ComplexKeyBindedSliderHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] PENDULUM_R_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new PendulumKeyBindedRotatorHandler(0, 0), new PendulumKeyBindedRotatorHandler(0, 1), new PendulumKeyBindedRotatorHandler(0, 2)}, {new PendulumKeyBindedRotatorHandler(1, 0), new PendulumKeyBindedRotatorHandler(1, 1), new PendulumKeyBindedRotatorHandler(1, 2)}, {new PendulumKeyBindedRotatorHandler(2, 0), new PendulumKeyBindedRotatorHandler(2, 1), new PendulumKeyBindedRotatorHandler(2, 2)}, {new PendulumKeyBindedRotatorHandler(3, 0), new PendulumKeyBindedRotatorHandler(3, 1), new PendulumKeyBindedRotatorHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] PENDULUM_Sl_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new PendulumKeyBindedSliderHandler(0, 0), new PendulumKeyBindedSliderHandler(0, 1), new PendulumKeyBindedSliderHandler(0, 2)}, {new PendulumKeyBindedSliderHandler(1, 0), new PendulumKeyBindedSliderHandler(1, 1), new PendulumKeyBindedSliderHandler(1, 2)}, {new PendulumKeyBindedSliderHandler(2, 0), new PendulumKeyBindedSliderHandler(2, 1), new PendulumKeyBindedSliderHandler(2, 2)}, {new PendulumKeyBindedSliderHandler(3, 0), new PendulumKeyBindedSliderHandler(3, 1), new PendulumKeyBindedSliderHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] SPRING_R_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new SpringKeyBindedRotatorHandler(0, 0), new SpringKeyBindedRotatorHandler(0, 1), new SpringKeyBindedRotatorHandler(0, 2)}, {new SpringKeyBindedRotatorHandler(1, 0), new SpringKeyBindedRotatorHandler(1, 1), new SpringKeyBindedRotatorHandler(1, 2)}, {new SpringKeyBindedRotatorHandler(2, 0), new SpringKeyBindedRotatorHandler(2, 1), new SpringKeyBindedRotatorHandler(2, 2)}, {new SpringKeyBindedRotatorHandler(3, 0), new SpringKeyBindedRotatorHandler(3, 1), new SpringKeyBindedRotatorHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[][] SPRING_Sl_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[][]{{new SpringKeyBindedSliderHandler(0, 0), new SpringKeyBindedSliderHandler(0, 1), new SpringKeyBindedSliderHandler(0, 2)}, {new SpringKeyBindedSliderHandler(1, 0), new SpringKeyBindedSliderHandler(1, 1), new SpringKeyBindedSliderHandler(1, 2)}, {new SpringKeyBindedSliderHandler(2, 0), new SpringKeyBindedSliderHandler(2, 1), new SpringKeyBindedSliderHandler(2, 2)}, {new SpringKeyBindedSliderHandler(3, 0), new SpringKeyBindedSliderHandler(3, 1), new SpringKeyBindedSliderHandler(3, 2)}};
   private static final IJBMotionBinderItem.IJBMotionHandler[] COMP_E_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new ComplexKeyBindedExtenderScalerHandler(0), new ComplexKeyBindedExtenderScalerHandler(1), new ComplexKeyBindedExtenderScalerHandler(2), new ComplexKeyBindedExtenderScalerHandler(3)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SPRING_E_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new SpringKeyBindedExtenderScalerHandler(0), new SpringKeyBindedExtenderScalerHandler(1), new SpringKeyBindedExtenderScalerHandler(2), new SpringKeyBindedExtenderScalerHandler(3)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] FOLLOW_LOOK_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new FolowLookKeyBindedHandler(0), new FolowLookKeyBindedHandler(1), new FolowLookKeyBindedHandler(2), new FolowLookKeyBindedHandler(3)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] FOLLOW_TARGET_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new FolowTargetKeyBindedHandler(0), new FolowTargetKeyBindedHandler(1), new FolowTargetKeyBindedHandler(2), new FolowTargetKeyBindedHandler(3)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SLAVE_PURGE_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new SlavePurgeKeyItemBindedHandler(0, 0), new SlavePurgeKeyItemBindedHandler(1, 0), new SlavePurgeKeyItemBindedHandler(2, 0), new SlavePurgeKeyItemBindedHandler(3, 0)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SLAVE_PURGE_COMPLEX_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new SlavePurgeKeyItemComplexHandler(0, 0), new SlavePurgeKeyItemComplexHandler(1, 0), new SlavePurgeKeyItemComplexHandler(2, 0), new SlavePurgeKeyItemComplexHandler(3, 0)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] EMOUNT_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new EMountKeyItemBindedHandler(0, 0), new EMountKeyItemBindedHandler(1, 0), new EMountKeyItemBindedHandler(2, 0), new EMountKeyItemBindedHandler(3, 0)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] INITMODE_CHANGE_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new ModeChangeKeyItemBindedHandler(0, 0, 0), new ModeChangeKeyItemBindedHandler(1, 0, 0), new ModeChangeKeyItemBindedHandler(2, 0, 0), new ModeChangeKeyItemBindedHandler(3, 0, 0)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] SLAVEMODE_CHANGE_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new ModeChangeKeyItemBindedHandler(0, 1, 0), new ModeChangeKeyItemBindedHandler(1, 1, 0), new ModeChangeKeyItemBindedHandler(2, 1, 0), new ModeChangeKeyItemBindedHandler(3, 1, 0)};
   private static final IJBMotionBinderItem.IJBMotionHandler[] EFFECT_KEYBIND_LIST = new IJBMotionBinderItem.IJBMotionHandler[]{new EffectKeyItemBindedHandler(0, 0), new EffectKeyItemBindedHandler(1, 0), new EffectKeyItemBindedHandler(2, 0), new EffectKeyItemBindedHandler(3, 0)};
   private static final int[] motionNBindList = new int[]{10, 44, 45, 47};
   private static final int[] motionAtBindList = new int[]{42, 43};
   private static final int[] motionSeatBindList = new int[]{46};
   private static final int[] motionRBindList = new int[]{10, 30, 31, 32, 40, 41, 60, 61, 62, 90, 91, 92};
   private static final int[] motionSlBindList = new int[]{10, 50, 51, 52, 70, 71, 72, 100, 101, 102};
   private static final int[] motionEBindList = new int[]{10, 20, 80};

   public String getBinderName() {
      return "mbinder.keyresponse.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      if (itemDamage > 3) {
         itemDamage = 3;
      }

      if (bindNum == 44) {
         return INITMODE_CHANGE_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 45) {
         return SLAVEMODE_CHANGE_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 20) {
         return COMP_E_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 80) {
         return SPRING_E_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 10) {
         return SIMPLEKEYBIND_LIST[itemDamage];
      } else if (bindNum == 40) {
         return FOLLOW_LOOK_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 41) {
         return FOLLOW_TARGET_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 42) {
         return SLAVE_PURGE_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 43) {
         return SLAVE_PURGE_COMPLEX_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 46) {
         return EMOUNT_KEYBIND_LIST[itemDamage];
      } else if (bindNum == 47) {
         return EFFECT_KEYBIND_LIST[itemDamage];
      } else {
         int sub;
         if (30 <= bindNum && bindNum <= 32) {
            sub = bindNum - 30;
            if (sub >= 0 && sub < 3) {
               return COMP_R_KEYBIND_LIST[itemDamage][sub];
            }
         } else if (50 <= bindNum && bindNum <= 52) {
            sub = bindNum - 50;
            if (sub >= 0 && sub < 3) {
               return COMP_Sl_KEYBIND_LIST[itemDamage][sub];
            }
         } else if (60 <= bindNum && bindNum <= 62) {
            sub = bindNum - 60;
            if (sub >= 0 && sub < 3) {
               return PENDULUM_R_KEYBIND_LIST[itemDamage][sub];
            }
         } else if (70 <= bindNum && bindNum <= 72) {
            sub = bindNum - 70;
            if (sub >= 0 && sub < 3) {
               return PENDULUM_Sl_KEYBIND_LIST[itemDamage][sub];
            }
         } else if (90 <= bindNum && bindNum <= 92) {
            sub = bindNum - 90;
            if (sub >= 0 && sub < 3) {
               return SPRING_R_KEYBIND_LIST[itemDamage][sub];
            }
         } else if (100 <= bindNum && bindNum <= 102) {
            sub = bindNum - 100;
            if (sub >= 0 && sub < 3) {
               return SPRING_Sl_KEYBIND_LIST[itemDamage][sub];
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

   private static class FolowTargetKeyBindedHandler implements IJBMotionBinderItem.IJBMotionHandler {
      private int seatNum;

      public FolowTargetKeyBindedHandler(int seatNum) {
         this.seatNum = seatNum;
      }

      public int getBindNum() {
         return 41;
      }

      public String getMotionHandlerName() {
         return "mhndl.keyresponse.rottwdtarget.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 1;
         params[2] = 0;
         params[3] = 0;
         params[4] = 180;
      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
         if (((JointParts)joint).motionHelper == null) {
            ((JointParts)joint).motionHelper = new MotionHelper();
         }

      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"jbring.cmn.targetnum.title", (Format)EntryFormats.targetNumValueFormat, 0, 5);
         guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(2, (String)"keymot.onpressedonly.title", (Format)EntryFormats.followLookTypeValueFormat, 0, 2);
         guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(3, (String)"keymot.rottwdaxis.title", (Format)EntryFormats.rotTwdTargetTypeValueFormat, 0, 2);
         guiSettings.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(4, (String)"cmnmot.limitrot.title", (String)"jbcommon.angledegreese.format", 0, 180);
         guiSettings.setParamButtonToggleAry(4, EntryFormats.defaultButtonStates, 0);
      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         EntityJointed targetEJ = (EntityJointed)entityLivingBase;
         Entity targetEJOrMaster = targetEJ.getMasterEntityCheckSlaveOpIndependent();
         int responseType = joint.getParam(2);
         EntitySeatPart entitySeats;
         if (targetEJOrMaster instanceof EntityJointed) {
            targetEJ = (EntityJointed)targetEJOrMaster;
            entitySeats = targetEJ.getSeatEntity(this.seatNum);
            if (entitySeats == null) {
               return false;
            } else if (responseType == 1) {
               return CompatHelper.getRiddenByEntity(entitySeats) == null ? false : entitySeats.isRiddenbyPressingExKey(joint.getParam(0));
            } else {
               return responseType == 2 ? true : true;
            }
         } else if (targetEJOrMaster instanceof Player) {
            entitySeats = targetEJ.getSeatEntity(this.seatNum);
            if (entitySeats == null) {
               return false;
            } else if (responseType == 1) {
               return !entitySeats.isAsInteractableJoint ? false : entitySeats.isRiddenbyPressingExKey(joint.getParam(0));
            } else {
               return responseType == 2 ? true : true;
            }
         } else {
            return false;
         }
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         EntityJointed targetEJ = (EntityJointed)entityLivingBase;
         Entity targetEJOrMaster = targetEJ.getMasterEntityCheckSlaveOpIndependent();
         JointParts jp = (JointParts)joint;
         EntitySeatPart entitySeats;
         if (targetEJOrMaster instanceof EntityJointed) {
            targetEJ = (EntityJointed)targetEJOrMaster;
            if (joint.getParam(2) == 2) {
               entitySeats = targetEJ.getSeatEntity(this.seatNum);
               if (entitySeats == null || CompatHelper.getRiddenByEntity(entitySeats) == null || !entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
                  JBCommonUtil.xyzBackToInitialWithAmount(jp, 1000);
                  return;
               }
            }

            jp.doSpecialRotFlg = true;
         } else if (targetEJOrMaster instanceof Player) {
            if (joint.getParam(2) == 2) {
               entitySeats = targetEJ.getSeatEntity(this.seatNum);
               if (entitySeats == null || !entitySeats.notAsSeat || !entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
                  JBCommonUtil.xyzBackToInitialWithAmount(jp, 1000);
                  return;
               }
            }

            jp.doSpecialRotFlg = true;
         }

         if (jp.doSpecialRotFlg) {
            if (jp.motionHelper == null) {
               jp.motionHelper = new MotionHelper();
            }

            byte targetNum = (byte)joint.getParam(1);
            byte mode = (byte)joint.getParam(3);
            byte limitAngle = (byte)(joint.getParam(4) - 128);
            targetEJ.updateList.addAsExtraTowardsTargetRotSyncMessage(jp, targetNum, mode, limitAngle);
            jp.motionHelper.commonUse = (255 & targetNum) << 16 | (255 & mode) << 8 | 255 & limitAngle;
         }

      }

      public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
      }

      public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
      }

      public boolean isIgnoreRotationPrevFix() {
         return false;
      }
   }

   private static class FolowLookKeyBindedHandler implements IJBMotionBinderItem.IJBMotionHandler {
      private int seatNum;

      public FolowLookKeyBindedHandler(int seatNum) {
         this.seatNum = seatNum;
      }

      public int getBindNum() {
         return 40;
      }

      public String getMotionHandlerName() {
         return "mhndl.keyresponse.followlook.name";
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 1;
         params[2] = 0;
         params[3] = 0;
         params[4] = 0;
      }

      public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
         if (initValues[3] != 1 && joint.getParam(1) == 1) {
            joint.setResetParentRotation();
         }

      }

      public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
         guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
         guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(1, (String)"cmnmot.resetrot.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(2, (String)"cmnmot.reverse.title", (Format)EntryFormats.commonOnOffValueFormat, 0, 1);
         guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
         guiSettings.setParamEntryState(3, (String)"keymot.onpressedonly.title", (Format)EntryFormats.followLookTypeValueFormat, 0, 2);
         guiSettings.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
         EntryFormats.setDefaultKeyActionButtonState(guiSettings, 4);
      }

      public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         EntityJointed targetEJ = (EntityJointed)entityLivingBase;
         Entity targetEJOrMaster = targetEJ.getMasterEntityCheckSlaveOpIndependent();
         EntitySeatPart entitySeats;
         if (targetEJOrMaster instanceof EntityJointed) {
            targetEJ = (EntityJointed)targetEJOrMaster;
            entitySeats = targetEJ.getSeatEntity(this.seatNum);
            if (entitySeats != null && CompatHelper.getRiddenByEntity(entitySeats) != null) {
               return joint.getParam(3) == 1 ? entitySeats.isRiddenbyPressingExKey(joint.getParam(0)) : true;
            } else {
               return false;
            }
         } else if (targetEJOrMaster instanceof Player) {
            entitySeats = targetEJ.getSeatEntity(this.seatNum);
            if (entitySeats != null && entitySeats.isAsInteractableJoint) {
               return joint.getParam(3) == 1 ? entitySeats.isRiddenbyPressingExKey(joint.getParam(0)) : true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
         EntityJointed targetEJ = (EntityJointed)entityLivingBase;
         Entity targetEJOrMaster = targetEJ.getMasterEntityCheckSlaveOpIndependent();
         EntitySeatPart entitySeats;
         boolean reverse;
         Player entityPlayer;
         if (targetEJOrMaster instanceof EntityJointed) {
            targetEJ = (EntityJointed)targetEJOrMaster;
            if (joint.getParam(3) == 2) {
               entitySeats = targetEJ.getSeatEntity(this.seatNum);
               if (entitySeats == null || CompatHelper.getRiddenByEntity(entitySeats) == null || !entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
                  JBCommonUtil.xyzBackToInitialWithAmount((JointParts)joint, 1000);
                  return;
               }
            }

            entityPlayer = targetEJ.getRidingPlayer(this.seatNum);
            if (entityPlayer == null) {
               return;
            }

            reverse = joint.getParam(2) == 1;
            if (targetEJ.isMasterTeConnected() && !targetEJ.isMasterConnected() && this.seatNum == 0) {
               if (reverse) {
                  joint.setNewRotationX(joint.getInitValue(0) + (int)(-entityPlayer.getXRot()));
                  joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.getYRot() - entityPlayer.yHeadRot) + 180);
               } else {
                  joint.setNewRotationX(joint.getInitValue(0) + (int)entityPlayer.getXRot());
                  joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.getYRot() - entityPlayer.yHeadRot));
               }
            } else if (reverse) {
               joint.setNewRotationX(joint.getInitValue(0) + (int)(-entityPlayer.getXRot()));
               joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.yHeadRot - targetEJ.getYRot()) + 180);
            } else {
               joint.setNewRotationX(joint.getInitValue(0) + (int)entityPlayer.getXRot());
               joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.yHeadRot - targetEJ.getYRot()));
            }
         } else if (targetEJOrMaster instanceof Player) {
            if (joint.getParam(3) == 2) {
               entitySeats = targetEJ.getSeatEntity(this.seatNum);
               if (entitySeats == null || !entitySeats.notAsSeat || !entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
                  JBCommonUtil.xyzBackToInitialWithAmount((JointParts)joint, 1000);
                  return;
               }
            }

            entityPlayer = (Player)targetEJOrMaster;
            reverse = joint.getParam(2) == 1;
            if (reverse) {
               joint.setNewRotationX(joint.getInitValue(0) + (int)(-entityPlayer.getXRot()));
               joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.getYRot() - entityPlayer.yHeadRot) + 180);
            } else {
               joint.setNewRotationX(joint.getInitValue(0) + (int)entityPlayer.getXRot());
               joint.setNewRotationY(joint.getInitValue(1) + (int)(entityPlayer.getYRot() - entityPlayer.yHeadRot));
            }
         }

      }

      public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
         EntitySeatPart entitySeats = ((EntityJointed)entityLivingBase).getSeatEntity(this.seatNum);
         if (joint.hasModelAction() && entitySeats != null && entitySeats.canKeyItemUsed()) {
            if (joint.getParam(4) == 1 && entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
               joint.dispatchModelAction(entitySeats.getActionDispatcher());
            }

         }
      }

      public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
      }

      public boolean isIgnoreRotationPrevFix() {
         return false;
      }
   }

   private static class PendulumKeyBindedSliderHandler extends AbstractPendulumActHandler {
      public PendulumKeyBindedSliderHandler(int seatNum, int axis) {
         this.type = 2;
         this.seatNum = seatNum;
         this.axis = axis;
         this.keyItemEntryNum = 0;
         this.amountEntryNum = 1;
         this.minEntryNum = 2;
         this.maxEntryNum = 3;
         this.patternEntryNum = 4;
         this.enableAutoBackOnKeyItemUpEntry = true;
         this.autoBackEntryNum = 5;
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
         guiSettings.setParamEntryState(this.keyItemEntryNum, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
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

   private static class PendulumKeyBindedRotatorHandler extends AbstractPendulumActHandler {
      public PendulumKeyBindedRotatorHandler(int seatNum, int axis) {
         this.type = 2;
         this.seatNum = seatNum;
         this.axis = axis;
         this.keyItemEntryNum = 0;
         this.amountEntryNum = 1;
         this.minEntryNum = 2;
         this.maxEntryNum = 3;
         this.patternEntryNum = 4;
         this.enableAutoBackOnKeyItemUpEntry = true;
         this.autoBackEntryNum = 5;
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
         guiSettings.setParamEntryState(this.keyItemEntryNum, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
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

   private static class SpringKeyBindedSliderHandler extends AbstractSpringKeyItemThreeValuesHandler {
      public SpringKeyBindedSliderHandler(int seatNum, int axis) {
         this.keyOrItem = 0;
         this.seatNum = seatNum;
         this.axis = axis;
      }

      public int getBindNum() {
         switch (this.axis) {
            case 0:
               return 100;
            case 1:
               return 101;
            default:
               return 102;
         }
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 0;
         params[2] = 0;
         params[3] = 10;
         params[4] = -64;
         params[5] = 64;
      }
   }

   private static class SpringKeyBindedRotatorHandler extends AbstractSpringKeyItemThreeValuesHandler {
      public SpringKeyBindedRotatorHandler(int seatNum, int axis) {
         this.keyOrItem = 0;
         this.seatNum = seatNum;
         this.axis = axis;
      }

      public int getBindNum() {
         switch (this.axis) {
            case 0:
               return 90;
            case 1:
               return 91;
            default:
               return 92;
         }
      }

      public void setParamsInitialValue(int jointType, int[] params) {
         params[0] = 0;
         params[1] = 0;
         params[2] = 0;
         params[3] = 10;
         params[4] = -180;
         params[5] = 180;
      }
   }

   private static class SpringKeyBindedExtenderScalerHandler extends AbstractSpringKeyItemExtenderScalerHandler {
      public SpringKeyBindedExtenderScalerHandler(int seatNum) {
         this.keyOrItem = 0;
         this.seatNum = seatNum;
      }

      public int getBindNum() {
         return 80;
      }
   }

   private static class ComplexKeyBindedSliderHandler extends AbstractComplexKeyItemThreeValuesHandler {
      public ComplexKeyBindedSliderHandler(int seatNum, int axis) {
         this.keyOrItem = 0;
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

   private static class ComplexKeyBindedRotatorHandler extends AbstractComplexKeyItemThreeValuesHandler {
      public ComplexKeyBindedRotatorHandler(int seatNum, int axis) {
         this.keyOrItem = 0;
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

   private static class ComplexKeyBindedExtenderScalerHandler extends AbstractComplexKeyItemExtenderScalerHandler {
      public ComplexKeyBindedExtenderScalerHandler(int seatNum) {
         this.keyOrItem = 0;
         this.seatNum = seatNum;
      }

      public int getBindNum() {
         return 20;
      }
   }

   private static class SimpleKeyBindedHandler implements IJBMotionBinderItem.IJBMotionHandler {
      private int seatNum;

      public SimpleKeyBindedHandler(int seatNum) {
         this.seatNum = seatNum;
      }

      public int getBindNum() {
         return 10;
      }

      public String getMotionHandlerName() {
         return "mhndl.keyresponse.simple.name";
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
               guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
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
               guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
               guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
               EntryFormats.setDefaultJointTypeButtonState(guiSettings, jointType, false, 1);
               EntryFormats.setDefaultKeyActionButtonState(guiSettings, 4);
               break;
            case 10:
               guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
               guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
               guiSettings.setParamEntryState(1, (String)"basicmot.extend.move.title", (String)"jbcommon.percentage.valueformat", -100, 100);
               guiSettings.setParamEntryState(2, (String)"basicmot.extend.min.tilte", (String)"jbcommon.percentage.valueformat", 0, 300);
               guiSettings.setParamEntryState(3, (String)"basicmot.extend.max.title", (String)"jbcommon.percentage.valueformat", 0, 300);
               EntryFormats.setDefaultKeyActionButtonState(guiSettings, 4);
               break;
            case 12:
               guiSettings.setParamEntryState(0, (String)"keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 15);
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
            return entitySeats != null && entitySeats.canKeyItemUsed() ? entitySeats.isRiddenbyPressingExKey(joint.getParam(0)) : false;
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

            if (doAction && entitySeats.isRiddenbyPressingExKey(joint.getParam(0))) {
               joint.dispatchModelAction(entitySeats.getActionDispatcher());
            }

         }
      }

      public boolean isIgnoreRotationPrevFix() {
         return false;
      }
   }
}
