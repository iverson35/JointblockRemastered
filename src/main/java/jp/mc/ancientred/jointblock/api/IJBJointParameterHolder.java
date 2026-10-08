package jp.mc.ancientred.jointblock.api;

import net.minecraft.world.entity.LivingEntity;

public interface IJBJointParameterHolder {

    int getJointType();

    int getBindNum();

    int getConnectSide();

    int getRenderSide();

    int getParam(int index);

    void setParam(int index, int value);

    int getInitValue(int index);

    int[] getCarvinData();

    interface IJBJoint extends IJBJointParameterHolder {

        int getRotationX();

        int getPrevRotationX();

        int getRotationY();

        int getPrevRotationY();

        int getRotationZ();

        int getPrevRotationZ();

        int getExtendRate();

        int getPrevExtendRate();

        int setNewRotationX(int value);

        int setNewRotationY(int value);

        int setNewRotationZ(int value);

        int setNewExtendRate(int value);

        void setRotatePoint(int point);

        void setResetParentRotation();

        boolean getMotionCommonFlag();

        void setMotionCommonFlag(boolean flag);

        IJBJoint getParentJoint();

        IJBJoint getChildJoint(int index);

        int getModelItemId();

        int getModelItemMeta();

        int getMotionItemId();

        int getMotionItemMeta();

        boolean hasModelAction();

        void dispatchModelAction(LivingEntity entity);

        IJBVectorTransformer getTransformMatrix();

        boolean hasInitExtraActionVars();

        void initExtraActionVars(int var1, int var2, int var3, int var4);

        int getExtraActionVars(int index);

        void setExtraActionVars(int index, int value);
    }
}
