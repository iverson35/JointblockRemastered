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

public abstract class CommonSwingRotationHandler extends AbstractCommonMotionHandler {
   protected static final int LR_PARAM_NUM = 0;
   protected static final int SWING_COEFF_PARAM_NUM = 1;
   protected static final int ONJUMP_PARAM_NUM = 2;
   protected static final int JUMPDEGREES_PARAM_NUM = 3;
   public static final int TYPE_SHOLDER = 0;
   public static final int TYPE_ELBOW = 2;
   public static final int TYPE_COXA = 4;
   public static final int TYPE_KNEE = 6;
   public static final int TYPE_ANKLE = 8;
   public static final int TYPE_BEAST_COXA = 10;
   public static final int TYPE_BEAST_KNEE = 12;
   public static final int TYPE_BEAST_ANKLE = 14;
   private int type;
   private int subType;

   public CommonSwingRotationHandler(int type) {
      this(type, 0);
   }

   public CommonSwingRotationHandler(int type, int subType) {
      this.type = type;
      this.subType = subType;
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      guiSettings.setParamEntryState(0, (String)"cmnmot.selectlr.title", (Format)EntryFormats.commonLRSelectValueFormat, 0, 1);
      guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      guiSettings.setParamEntryState(1, (String)"cmnmot.swingcoeff.title", (String)"jbcommon.percentage.valueformat", -500, 500);
      guiSettings.setParamEntryState(2, (ChoiceFormat)EntryFormats.commonOnJumpTitleFormat, (Format)EntryFormats.commonOnJumpValueFormat, 0, 7);
      guiSettings.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      guiSettings.setParamEntryState(3, (String)"cmnmot.onjumpangle.title", (String)"jbcommon.angledegreese.format", -180, 180);
   }

   public void onJointFlip(IJBJointParameterHolder joint, int flipWay) {
      if ((flipWay & 2) != 0 && joint instanceof TileEntityJoint) {
         joint.setParam(0, joint.getParam(0) == 1 ? 0 : 1);
      }

   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return true;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      if (!EntityJointed.isItMotIndependant(entityLivingBase)) {
         ((JointParts)joint).setToIninitalValue();
      } else {
         float limbSwingAmountF = entityLivingBase.walkAnimation.speed();
         float limbSwing = entityLivingBase.walkAnimation.position();
         if (limbSwingAmountF > 1.0F) {
            limbSwingAmountF = 1.0F;
         }

         float radSpeed = limbSwing * 0.32F;
         boolean isLeftCycle = joint.getParam(0) == 0;
         double swingCoeff = (double)joint.getParam(1) / 100.0;
         float radiansVal = 0.0F;
         switch (this.type) {
            case 0:
               if (isLeftCycle) {
                  radSpeed += 3.1415927F;
               }

               radiansVal = Mth.cos(radSpeed) * (float)Math.toRadians(-60.0 * swingCoeff) * limbSwingAmountF;
            case 1:
            case 3:
            case 5:
            case 7:
            case 9:
            case 11:
            case 13:
            default:
               break;
            case 2:
               radiansVal = (float)Math.toRadians(-90.0 * swingCoeff) * limbSwingAmountF;
               break;
            case 4:
               if (!isLeftCycle) {
                  radSpeed += 3.1415927F;
               }

               radiansVal = Mth.cos(radSpeed) * (float)Math.toRadians(50.0 * swingCoeff) * limbSwingAmountF;
               break;
            case 6:
               if (isLeftCycle) {
                  radSpeed += 3.1415927F;
               }

               radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               radSpeed /= 2.0F;
               radiansVal = (Mth.abs(Mth.cos(radSpeed)) / -2.0F + 0.5F) * 2.0F * (float)Math.toRadians(80.0 * swingCoeff) * limbSwingAmountF;
               break;
            case 8:
               if (isLeftCycle) {
                  radSpeed += 3.1415927F;
               }

               radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               radSpeed /= 2.0F;
               radiansVal = (Mth.abs(Mth.cos(radSpeed)) / -2.0F + 0.5F) * 2.0F * (float)Math.toRadians(90.0 * swingCoeff) * limbSwingAmountF;
               break;
            case 10:
               if (!isLeftCycle) {
                  radSpeed += 3.1415927F;
               }

               if (this.subType == 1) {
                  radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               }

               radiansVal = Mth.cos(radSpeed) * (float)Math.toRadians(60.0 * swingCoeff) * limbSwingAmountF;
               break;
            case 12:
               if (isLeftCycle) {
                  radSpeed += 3.1415927F;
               }

               if (this.subType == 1) {
                  radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               }

               radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               radSpeed /= 2.0F;
               radiansVal = (Mth.abs(Mth.cos(radSpeed)) / -2.0F + 0.5F) * 2.0F * (float)Math.toRadians(80.0 * swingCoeff) * limbSwingAmountF;
               break;
            case 14:
               if (isLeftCycle) {
                  radSpeed += 3.1415927F;
               }

               if (this.subType == 1) {
                  radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               }

               radSpeed = (float)((double)radSpeed - 1.5707963705062866);
               radSpeed /= 2.0F;
               radiansVal = (Mth.abs(Mth.cos(radSpeed)) / -2.0F + 0.5F) * 2.0F * (float)Math.toRadians(90.0 * swingCoeff) * limbSwingAmountF;
         }

         int degree = (int)Math.toDegrees((double)radiansVal);
         int onAirBend = joint.getParam(2);
         if (onAirBend > 3) {
            onAirBend -= 4;
            if (JBCommonUtil.isAirUp(entityLivingBase)) {
               if ((onAirBend & 2) != 0) {
                  degree = joint.getParam(3);
               } else {
                  degree = 0;
               }
            }

            if (JBCommonUtil.isAirDown(entityLivingBase)) {
               if ((onAirBend & 1) != 0) {
                  degree = joint.getParam(3);
               } else {
                  degree = 0;
               }
            }
         } else {
            if (JBCommonUtil.isJumpingUp(entityLivingBase)) {
               if ((onAirBend & 2) != 0) {
                  degree = joint.getParam(3);
               } else {
                  degree = 0;
               }
            }

            if (JBCommonUtil.isJumpingDown(entityLivingBase)) {
               if ((onAirBend & 1) != 0) {
                  degree = joint.getParam(3);
               } else {
                  degree = 0;
               }
            }
         }

         joint.setNewRotationX(joint.getInitValue(0) + degree);
      }
   }
}
