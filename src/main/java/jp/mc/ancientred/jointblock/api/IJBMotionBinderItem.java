package jp.mc.ancientred.jointblock.api;

import net.minecraft.world.entity.LivingEntity;

public interface IJBMotionBinderItem extends IJBConsts {

    IJBMotionBinder getMotionBinder();

    interface IJBMotionHandler {

        int getBindNum();

        String getMotionHandlerName();

        void setParamsInitialValue(int index, int[] params);

        void setGuiEntryState(int index, IJBGuiSettings guiSettings);

        void onInitParts(IJBJointParameterHolder.IJBJoint joint, short[] vars);

        boolean needsJointUpdate(LivingEntity entity, IJBJointParameterHolder.IJBJoint joint);

        void doUpdateJointState(LivingEntity entity, IJBJointParameterHolder.IJBJoint joint);

        void onJointPositionUpdate(IJBJointParameterHolder.IJBJoint joint, LivingEntity entity, IJBVectorTransformer transformer);

        void onJointFlip(IJBJointParameterHolder holder, int flipWay);

        boolean isIgnoreRotationPrevFix();
    }

    interface IJBMotionBinder extends IJBConsts {

        String getBinderName();

        IJBMotionHandler getBindMotionHandler(int type, int index);

        int[] getBindNumberList(int type, int index);
    }
}
