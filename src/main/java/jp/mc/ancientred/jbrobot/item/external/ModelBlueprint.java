package jp.mc.ancientred.jbrobot.item.external;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.registry.JBItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Catalog entry model that draws the merchant-blueprint dummy item (the blueprint catalog icon).
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ModelBlueprint}. The 1.12.2
 * {@code glScalef(1,-1,-1) + glTranslatef(0,-1,0) + ItemRenderer.renderItem(GROUND)} sequence becomes
 * the same PoseStack transform plus {@code ItemRenderer.renderStatic}; the dummy stack is the
 * blueprint item with the merchant subtype (1.12.2 damage value 2, the port's {@code subtype} tag).
 */
@OnlyIn(Dist.CLIENT)
public class ModelBlueprint implements IJBModelItem.IJBModel {
    public static ModelBlueprint instance = new ModelBlueprint();
    public static ItemStack stackForRender;

    @Override
    public void renderAll(int modelMeta, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int renderPass) {
        pose.scale(1.0F, -1.0F, -1.0F);
        pose.translate(0.0F, -1.0F, 0.0F);
        if (stackForRender == null) {
            stackForRender = JBItems.BluePrintItem.createSubtypedStack(ItemJBBlueprint.TYPE_MERCHANT);
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(stackForRender, ItemDisplayContext.GROUND,
                packedLight, packedOverlay, pose, bufferSource, null, 0);
    }

    @Override
    public void setRenderState(PoseStack pose, IJBJointParameterHolder jointMayBeNull, int modelMetaMayBeMeaningLess,
                               float tickPartialMayBeMeaningLess, LivingEntity entityMayBeNull, int renderPass) {
        // 1.12.2 ModelBlueprint#setRenderState was an empty no-op.
    }

    @Override
    public boolean shouldRenderInPass(int modelMeta, int renderPass) {
        return renderPass == 0;
    }
}
