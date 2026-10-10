package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class ItemPaintGun extends AbstractCarvingToolItem {
   public static final int PATTERN_NUM_MAX = 7;

   @Override
   public void processEditOnServer(ItemStack stack, Player player, TileEntityJoint te, int boxIndex, int subHitValue, int hitSide, boolean leftClick) {
      if (!leftClick) {
         int patternNum = getColorNumFromStack(stack);
         int carvingType = JBCarvingDataUtil.getCarvingType(te.carvingData);
         switch (carvingType) {
            case 2:
               JBCarvingDataUtil.modifyPatternCarveData(te, boxIndex, patternNum, 0, true, false, player);
               break;
            case 4:
               return;
            default:
               JBCarvingDataUtil.modifyBoxedCarveData(te, boxIndex, patternNum, player);
         }
      }

   }

   public static int getColorNumFromStack(ItemStack stack) {
      int patternNum;
      if (!stack.hasTag()) {
         patternNum = 1;
      } else {
         CompoundTag tag = stack.getTag();
         patternNum = tag.getByte("jb:pnum");
         if (patternNum < 1 || patternNum > 7) {
            patternNum = 1;
         }
      }

      return patternNum;
   }

   @Override
   public boolean onEntitySwing(ItemStack stack, LivingEntity entityLiving) {
      return true;
   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
      if (CompatHelper.isStackEmptyOrNull(stack)) {
         return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
      } else {
         if (player.isShiftKeyDown()) {
            if (!stack.hasTag()) {
               stack.setTag(new CompoundTag());
            }

            CompoundTag tag = stack.getTag();
            byte patternNum;
            if (tag.contains("jb:pnum")) {
               patternNum = tag.getByte("jb:pnum");
               if (patternNum >= 1 && patternNum <= 7) {
                  ++patternNum;
                  if (patternNum < 1 || patternNum > 7) {
                     patternNum = 1;
                  }
               } else {
                  patternNum = 2;
               }
            } else {
               patternNum = 2;
            }

            tag.putByte("jb:pnum", patternNum);
         }

         return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
      }
   }
}
