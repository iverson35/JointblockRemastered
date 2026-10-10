package jp.mc.ancientred.jbrobot.item;

import java.util.List;
import jp.mc.ancientred.jbrobot.item.addon.LimitKeyHands;
import jp.mc.ancientred.jbrobot.model.technesample.ModelRobotHand;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/**
 * Robot hand model item with the 1.12.2 left / right damage-value subtypes, ported from
 * {@code jp.mc.ancientred.jbrobot.item.ItemModelHand}.
 *
 * <p>1.20.1 deviation: the model meta is the vanilla {@code Damage} tag read through
 * {@link ItemStack#getDamageValue()} (the same convention {@code modelpack.ExtraPackage} and
 * {@code TileEntityJoint#getModelItemDamage} use); {@link #getFlippedStack} therefore rewrites the
 * damage value in place, exactly where 1.12.2 did. The item also carries the "hands" addon (limit key
 * {@code JBHANDS}, +0.025 max energy).
 */
public class ItemModelHand extends Item implements IJBModelItem, IJBModelItem.IJBModelItemFlippable, IJBAddon.IJBAddonHolder {
   public static final int ITEM_META_LEFT = 0;
   public static final int ITEM_META_RIGHT = 1;
   private static final IJBAddon addonHead = new IJBAddon() {
      @Override
      public IJBAddon.IJBAddonLimitKey getLimitKey() {
         return LimitKeyHands.instance;
      }

      @Override
      public int getCost() {
         return 1;
      }

      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxEnergy:
               return 0.025F;
            default:
               return 0.0F;
         }
      }
   };

   public ItemModelHand() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return modelMeta == 1 ? ModelRobotHand.instanceRight : ModelRobotHand.instanceLeft;
   }

   @Override
   public IJBModelItem.IJBActionHandler getIJBActionHandler(int modelMeta) {
      return null;
   }

   @Override
   public double getRenderExpand(int modelMeta) {
      return 0.0;
   }

   @Override
   public Component getName(ItemStack stack) {
      return super.getName(stack).copy()
            .append(Component.translatable(stack.getDamageValue() == 1 ? "jbrobot.right" : "jbrobot.left"));
   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   @Override
   public ItemStack getFlippedStack(IJBJointParameterHolder joint, ItemStack srcStack, int flipWay) {
      if (CompatHelper.isStackEmptyOrNull(srcStack)) {
         return null;
      } else {
         if ((flipWay & 2) != 0) {
            if (srcStack.getDamageValue() == 1) {
               srcStack.setDamageValue(0);
            } else if (srcStack.getDamageValue() == 0) {
               srcStack.setDamageValue(1);
            }
         }

         return srcStack;
      }
   }

   @Override
   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("jbrobot.info.hasaddon"));
   }

   @Override
   public IJBAddon getIJBAddon(int itemMeta) {
      return addonHead;
   }
}
