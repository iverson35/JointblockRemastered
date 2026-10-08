package jp.mc.ancientred.jointblock.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IJBModelItem extends IJBConsts {

    IJBModel getIJBModel(int index);

    IJBActionHandler getIJBActionHandler(int index);

    double getRenderExpand(int index);

    interface IJBModelItemFlippable {
        ItemStack getFlippedStack(IJBJointParameterHolder holder, ItemStack stack, int flipWay);
    }

    interface IJBModel extends IJBConsts {

        void renderAll(int pass, float partialTicks, int index);

        void setRenderState(IJBJointParameterHolder holder, int index, float partialTicks, LivingEntity entity, int pass);

        boolean shouldRenderInPass(int pass, int index);
    }

    interface IJBActionHandler {

        String getActionName();

        void action(Level level, LivingEntity user, LivingEntity target, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer transformer);
    }
}
