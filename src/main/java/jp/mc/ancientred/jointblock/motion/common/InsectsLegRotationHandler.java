package jp.mc.ancientred.jointblock.motion.common;

import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public abstract class InsectsLegRotationHandler extends AbstractCommonMotionHandler {
   protected static final int LR_PARAM_NUM = 0;
   protected static final int SWING_COEFF_PARAM_NUM = 1;
   protected static final int CYCLE_DELAY_PARAM_NUM = 2;
   protected static final int ONJUMP_PARAM_NUM = 3;
   protected static final int JUMPDEGREES_PARAM_NUM = 4;
   public static final int TYPE_TROCHANTER = 4;
   public static final int TYPE_FEMUR = 6;
   private int type;
   private int subType;

   public InsectsLegRotationHandler(int type, int subType) {
      this.type = type;
      this.subType = subType;
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      guiSettings.setParamEntryState(0, (String)"cmnmot.selectlr.title", (Format)EntryFormats.commonLRSelectValueFormat, 0, 1);
      guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      guiSettings.setParamEntryState(1, (String)"cmnmot.swingcoeff.title", (String)"jbcommon.percentage.valueformat", -500, 500);
      guiSettings.setParamEntryState(2, (String)"cmnmot.cycledelay.title", (String)"jbcommon.percentage.valueformat", -100, 100);
      guiSettings.setParamEntryState(3, (ChoiceFormat)EntryFormats.insectsOnJumpTitleFormat, (Format)EntryFormats.insectsOnJumpValueFormat, 0, 8);
      guiSettings.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
      guiSettings.setParamEntryState(4, (String)"cmnmot.onjumpangle.title", (String)"jbcommon.angledegreese.format", -180, 180);
   }

   public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
      if ((flipWay & 2) != 0 && joint instanceof TileEntityJoint) {
         joint.setParam(0, joint.getParam(0) == 1 ? 0 : 1);
      }

   }

   public void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] initValues) {
      initValues[2] = this.getFixedInitZ(joint, initValues[2]);
   }

   private short getFixedInitZ(IJBJointParameterHolder.IJBJoint joint, int initZ) {
      int newInitZ;
      if (this.type == 4) {
         if (joint.getParam(0) == 0) {
            newInitZ = initZ + 30;
         } else {
            newInitZ = initZ - 30;
         }

         newInitZ %= 360;
         return (short)newInitZ;
      } else if (this.type == 6) {
         if (joint.getParam(0) == 0) {
            newInitZ = initZ + 45;
         } else {
            newInitZ = initZ - 45;
         }

         newInitZ %= 360;
         return (short)newInitZ;
      } else {
         return 0;
      }
   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return true;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
         ((JointParts)joint).setToIninitalValue();
      } else {
         boolean movingBack = false;
         double d0 = entityLivingBase.getX() - entityLivingBase.xo;
         double d1 = entityLivingBase.getZ() - entityLivingBase.zo;
         double yaw = -90.0 + Math.atan2(d1, d0) * 180.0 / Math.PI;
         float yRot = (float)yaw - entityLivingBase.getYRot() % 360.0F;
         if (yRot < -180.0F) {
            yRot += 360.0F;
         }

         if (yRot > 180.0F) {
            yRot -= 360.0F;
         }

         if (yRot > 120.0F || yRot < -120.0F) {
            movingBack = true;
         }

         float limbSwingAmountF = entityLivingBase.walkAnimation.speed();
         float limbSwing = entityLivingBase.walkAnimation.position();
         if (limbSwingAmountF > 1.0F) {
            limbSwingAmountF = 1.0F;
         }

         float radSpeed = limbSwing * 0.32F;
         boolean isLeftCycle = joint.getParam(0) == 0;
         int reverse = 1;
         if (isLeftCycle) {
            reverse = -1;
         }

         if (this.subType == 1) {
            reverse = -reverse;
         }

         double swingCoeff = (double)joint.getParam(1) / 100.0;
         double cycleDelay = (double)joint.getParam(2) / 100.0;
         float radiansValY = 0.0F;
         float radiansValZ = 0.0F;
         switch (this.type) {
            case 4:
               if (isLeftCycle) {
                  radSpeed = (float)((double)radSpeed + 3.1415927410125732 + 3.1415927410125732 * cycleDelay);
               }

               if (this.subType == 1) {
                  radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               }

               radiansValY = Mth.cos(radSpeed) * (float)Math.toRadians(-50.0 * swingCoeff * (double)reverse) * limbSwingAmountF;
               radSpeed = (float)((double)radSpeed + 1.5707963705062866);
               if (movingBack) {
                  radSpeed += 3.1415927F;
               }

               radiansValZ = Mth.cos(radSpeed) * (float)Math.toRadians(30.0 * swingCoeff * (double)reverse) * limbSwingAmountF;
               if (isLeftCycle) {
                  radiansValZ = radiansValZ > 0.0F ? 0.0F : radiansValZ;
               } else {
                  radiansValZ = radiansValZ < 0.0F ? 0.0F : radiansValZ;
               }
               break;
            case 6:
               if (isLeftCycle) {
                  radSpeed += 3.1415927F;
               }

               if (this.subType == 1) {
                  radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               }

               radSpeed = (float)((double)radSpeed + 1.5707963705062866);
               if (movingBack) {
                  radSpeed += 3.1415927F;
               }

               radiansValZ = Mth.cos(radSpeed) * (float)Math.toRadians(30.0 * swingCoeff * (double)reverse) * limbSwingAmountF;
               if (isLeftCycle) {
                  radiansValZ = radiansValZ > 0.0F ? 0.0F : radiansValZ;
               } else {
                  radiansValZ = radiansValZ < 0.0F ? 0.0F : radiansValZ;
               }
         }

         int degreeY = (int)Math.toDegrees((double)radiansValY);
         int degreeZ = (int)Math.toDegrees((double)radiansValZ);
         int onAirBend = joint.getParam(3);
         if (onAirBend > 4) {
            onAirBend -= 4;
            if (JBCommonUtil.isAirUp(entityLivingBase)) {
               if ((onAirBend - 1 & 2) != 0) {
                  degreeZ = isLeftCycle ? joint.getParam(4) : -joint.getParam(4);
               } else {
                  degreeZ = 0;
               }

               degreeY = 0;
            }

            if (JBCommonUtil.isAirDown(entityLivingBase)) {
               if ((onAirBend - 1 & 1) != 0) {
                  degreeZ = isLeftCycle ? joint.getParam(4) : -joint.getParam(4);
               } else {
                  degreeZ = 0;
               }

               degreeY = 0;
            }
         } else {
            if (JBCommonUtil.isJumpingUp(entityLivingBase)) {
               if (onAirBend > 0) {
                  if ((onAirBend - 1 & 2) != 0) {
                     degreeZ = isLeftCycle ? joint.getParam(4) : -joint.getParam(4);
                  } else {
                     degreeZ = 0;
                  }
               } else if (this.type == 4) {
                  degreeZ = isLeftCycle ? 110 : -110;
               } else {
                  degreeZ = isLeftCycle ? -90 : 90;
               }

               degreeY = 0;
            }

            if (JBCommonUtil.isJumpingDown(entityLivingBase)) {
               if (onAirBend > 0) {
                  if ((onAirBend - 1 & 1) != 0) {
                     degreeZ = isLeftCycle ? joint.getParam(4) : -joint.getParam(4);
                  } else {
                     degreeZ = 0;
                  }
               } else if (this.type == 4) {
                  degreeZ = isLeftCycle ? 110 : -110;
               } else {
                  degreeZ = isLeftCycle ? -90 : 90;
               }

               degreeY = 0;
            }
         }

         joint.setNewRotationZ(this.getFixedInitZ(joint, joint.getInitValue(2)) + degreeZ);
         joint.setNewRotationY(joint.getInitValue(1) + degreeY);
      }
   }
}
