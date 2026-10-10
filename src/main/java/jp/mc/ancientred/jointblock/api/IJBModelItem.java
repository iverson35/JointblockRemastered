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

        /**
         * Draws the model for {@code modelMeta}. 1.12.2 passed {@code (modelMeta, 0.0625F, pass)} and
         * drew through the fixed-function pipeline bound to the GL texture; the port hands over the
         * pose and the buffer source so the implementation picks its own
         * {@link net.minecraft.client.renderer.RenderType} (textures travel with the render type in
         * 1.20.1).
         */
        void renderAll(int modelMeta, com.mojang.blaze3d.vertex.PoseStack pose,
                       net.minecraft.client.renderer.MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay, int renderPass);

        /**
         * Applies the per-frame transform/visibility state for {@code modelMeta}. 1.12.2 did this
         * with {@code GlStateManager} calls on the shared stack, so the 1.20.1 form receives the
         * {@link com.mojang.blaze3d.vertex.PoseStack} the caller renders into. Pushing is the
         * implementation's responsibility only if it also pops; the caller resets state per part.
         */
        void setRenderState(com.mojang.blaze3d.vertex.PoseStack pose, IJBJointParameterHolder holder, int modelMeta,
                            float partialTicks, LivingEntity entityMayBeNull, int renderPass);

        boolean shouldRenderInPass(int modelMeta, int renderPass);
    }

    interface IJBActionHandler {

        String getActionName();

        void action(Level level, LivingEntity user, LivingEntity target, IJBJointParameterHolder.IJBJoint joint, IJBVectorTransformer transformer);
    }
}
