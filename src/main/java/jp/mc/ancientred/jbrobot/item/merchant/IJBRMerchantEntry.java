package jp.mc.ancientred.jbrobot.item.merchant;

import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * One purchasable catalog entry of a {@link IJBRMerchantPackage}, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.merchant.IJBRMerchantEntry}.
 *
 * <p>The 1.12.2 client-only members keep their {@code @SideOnly(CLIENT)} split as
 * {@link OnlyIn}({@link Dist#CLIENT}); {@code World} became {@link Level}.
 */
public interface IJBRMerchantEntry {
   String getEntryName();

   ItemStack getMerchantCost();

   ItemStack getMerchantResult(Level world);

   boolean hasAction();

   IJBAddon getAddon();

   @OnlyIn(Dist.CLIENT)
   ItemStack getEntryItemStack();

   @OnlyIn(Dist.CLIENT)
   IJBModelItem.IJBModel getModel();

   @OnlyIn(Dist.CLIENT)
   int getModelMeta();

   @OnlyIn(Dist.CLIENT)
   float getCatalogScale();

   @OnlyIn(Dist.CLIENT)
   String getEntryDesc();

   @OnlyIn(Dist.CLIENT)
   String getActionName();
}
