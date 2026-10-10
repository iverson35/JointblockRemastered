package jp.mc.ancientred.jointblock.motion;

import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.motion.common.AbstractCommonMotionHandler;
import net.minecraft.world.entity.LivingEntity;

public class SensorMotionBinder extends AbstractCommonMotionHandler implements IJBMotionBinderItem.IJBMotionBinder, IJBMotionBinderItem.IJBMotionHandler {
   private static final int[] SINGLE_SELF_BIND_LIST = new int[]{0};
   protected static final int SENSOR_PARAM_NUM = 0;

   public String getBinderName() {
      return "mbinder.sensor.name";
   }

   public IJBMotionBinderItem.IJBMotionHandler getBindMotionHandler(int bindNum, int itemDamage) {
      return this;
   }

   public int[] getBindNumberList(int jointType, int itemDamage) {
      return SINGLE_SELF_BIND_LIST;
   }

   public int getBindNum() {
      return 0;
   }

   public String getMotionHandlerName() {
      return "mhndl.sensor.name";
   }

   public void setParamsInitialValue(int jointType, int[] params) {
      params[0] = 1;
   }

   public void setGuiEntryState(int jointType, IJBGuiSettings guiSettings) {
      guiSettings.setParamEntryState(0, (String)"jbring.cmn.sensornum.title", (Format)EntryFormats.targetNumValueFormat, 0, 10);
      guiSettings.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
   }

   public boolean needsJointUpdate(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
      return false;
   }

   public void doUpdateJointState(LivingEntity entityLivingBase, IJBJointParameterHolder.IJBJoint joint) {
   }

   public void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entityLivingBase, IJBVectorTransformer matrixUtil) {
      EntityJointed ej = (EntityJointed)entityLivingBase;
      short sensorNum = (short)joint.getParam(0);
      ej.setMatrixToSensor((JointParts)joint, (ModelViewMatrix)matrixUtil, sensorNum);
   }
}
