package jp.mc.ancientred.jointblock.api;

import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IJBEntityState {

    byte getUnitId();

    byte getUnitFacing();

    float getEntityEnergy();

    boolean useEntityEnergy(float amount);

    float getEntityMaxEnergy();

    float getEntityCoreHeight();

    double getSpinRate();

    boolean isEntityJumping();

    boolean isEntityFlying();

    boolean isEntityDashing();

    Player getRidingPlayer(int index);

    boolean isRidingPlayerGenericKeyPressed(int index, int key, boolean flag);

    boolean isRidingThisEntity(Entity entity);

    boolean isRelatedToThisEntity(Entity entity);

    boolean isFriendlyEntity(Entity entity);

    byte getInitLayerMode();

    boolean isSlaveMode();

    boolean isMasterConnected();

    IJBEntityState getMasterIfConnected();

    boolean isMasterEntityConnected();

    Entity getMasterEntityIfConnected();

    Entity getCameraForNum(int index, boolean flag);

    boolean isMasterTeConnected();

    BlockEntity getMasterTeIfConnected();

    int getSlaveSlotNum();

    boolean getMasterSlotEnableStateFlag(int index);

    void setMasterSlotEnableStateFlag(int index, boolean state, boolean flag);

    void notifyClientAction(IJBJointParameterHolder.IJBJoint joint);

    void notifyClientMotion(IJBJointParameterHolder.IJBJoint joint);

    void notifyClientEffect(IJBJointParameterHolder.IJBJoint joint, byte effect);

    Container getInnerInventory();

    Entity getNearestTargetEntityUnused(double x, double y, double z);
}
