package jp.mc.ancientred.jbrobot.item;

import java.util.List;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelBladeTopA;
import jp.mc.ancientred.jbrobot.model.wavefrontsample.ModelBladeTopB;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/**
 * Blade top model item with the 1.12.2 type-A / type-B damage-value subtypes.
 *
 * <p>1.20.1 deviation: the model meta is the vanilla {@code Damage} tag read through
 * {@link ItemStack#getDamageValue()} (the same convention {@code modelpack.ExtraPackage} and
 * {@code TileEntityJoint#getModelItemDamage} use); 1.20.1 has no {@code func_150895_a} item hook, so
 * the subtype stacks are enumerated by the creative tab and sold by the model catalog. The display
 * name still appends the {@code jbrobot.typea}/{@code jbrobot.typeb} suffix through {@link #getName}.
 */
public class ItemModelBladeTop extends Item implements IJBModelItem {
   private IJBModelItem.IJBActionHandler bladeAction;
   public static final int ITEM_META_TYPE_A = 0;
   public static final int ITEM_META_TYPE_B = 1;

   public ItemModelBladeTop() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return modelMeta == 0 ? ModelBladeTopA.instance : ModelBladeTopB.instance;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      if (this.bladeAction == null && JBModContainer.actionFactory != null) {
         this.bladeAction = JBModContainer.actionFactory.createAction("blade", null);
      }

      return this.bladeAction;
   }

   @Override
   public double getRenderExpand(int modelMeta) {
      return 0.0;
   }

   @Override
   public Component getName(ItemStack stack) {
      return super.getName(stack).copy()
            .append(Component.translatable(stack.getDamageValue() == 0 ? "jbrobot.typea" : "jbrobot.typeb"));
   }

   @Override
   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("jbrobot.info.hasaction"));
   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }
}
