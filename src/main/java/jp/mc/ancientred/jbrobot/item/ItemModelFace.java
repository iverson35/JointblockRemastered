package jp.mc.ancientred.jbrobot.item;

import java.util.List;
import jp.mc.ancientred.jbrobot.item.addon.LimitKeyHead;
import jp.mc.ancientred.jbrobot.model.technesample.ModelRobotFace;
import jp.mc.ancientred.jointblock.api.IJBAddon;
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
 * Robot face model item, ported from the 1.12.2 {@code jp.mc.ancientred.jbrobot.item.ItemModelFace}.
 * It also carries the "head" addon (limit key {@code JBHEAD}, +0.05 max health).
 */
public class ItemModelFace extends Item implements IJBModelItem, IJBAddon.IJBAddonHolder {
   private static final IJBAddon addonHead = new IJBAddon() {
      @Override
      public IJBAddon.IJBAddonLimitKey getLimitKey() {
         return LimitKeyHead.instance;
      }

      @Override
      public int getCost() {
         return 1;
      }

      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return 0.05F;
            default:
               return 0.0F;
         }
      }
   };

   public ItemModelFace() {
      super(new Item.Properties());
   }

   @Override
   public IJBModelItem.IJBModel getIJBModel(int modelMeta) {
      return ModelRobotFace.instance;
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
   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("jbrobot.info.hasaddon"));
   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   @Override
   public IJBAddon getIJBAddon(int itemMeta) {
      return addonHead;
   }
}
