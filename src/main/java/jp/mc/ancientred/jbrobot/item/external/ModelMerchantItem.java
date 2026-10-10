package jp.mc.ancientred.jbrobot.item.external;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Catalog entry model that draws a plain item stack (the merchant result / icon of a catalog entry).
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ModelMerchantItem}, which is a
 * {@link IJBModelItem.IJBModel} render helper - not an item - and is used by
 * {@link ExJBAddonItemInfo#getModel()} and the merchant entry classes. The 1.12.2
 * {@code glScalef(1,-1,-1) + glTranslatef(0,-1,0) + ItemRenderer.renderItem(stack, GROUND)} sequence
 * becomes the same PoseStack transform plus
 * {@link net.minecraft.client.renderer.entity.ItemRenderer#renderStatic} with
 * {@link ItemDisplayContext#GROUND}.
 */
@OnlyIn(Dist.CLIENT)
public class ModelMerchantItem implements IJBModelItem.IJBModel {
    private IJBRMerchantEntry merchantEntry;
    private ItemStack merchantEntryStack;

    public ModelMerchantItem(ItemStack argItemStack) {
        this.merchantEntryStack = argItemStack;
    }

    public ModelMerchantItem(IJBRMerchantEntry argMEntry) {
        this.merchantEntry = argMEntry;
    }

    @Override
    public void renderAll(int modelMeta, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay, int renderPass) {
        ItemStack stackToRender = this.merchantEntryStack;
        if (stackToRender == null) {
            stackToRender = this.merchantEntry.getEntryItemStack();
        }

        if (!CompatHelper.isStackEmptyOrNull(stackToRender)) {
            pose.scale(1.0F, -1.0F, -1.0F);
            pose.translate(0.0F, -1.0F, 0.0F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stackToRender, ItemDisplayContext.GROUND,
                    packedLight, packedOverlay, pose, bufferSource, null, 0);
        }
    }

    @Override
    public void setRenderState(PoseStack pose, IJBJointParameterHolder jointMayBeNull, int modelMetaMayBeMeaningLess,
                               float tickPartialMayBeMeaningLess, LivingEntity entityMayBeNull, int renderPass) {
        // 1.12.2 ModelMerchantItem#setRenderState was an empty no-op.
    }

    @Override
    public boolean shouldRenderInPass(int modelMeta, int renderPass) {
        return renderPass == 0;
    }
}
