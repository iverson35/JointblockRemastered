package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemCeramicChisel extends AbstractCarvingToolItem {
   public static final int PATTERN_NUM_MAX = 16;
   public static final int PATTERN_NUM_FILL = 0;

   public void processEditOnServer(ItemStack stack, Player player, TileEntityJoint te, int boxIndex, int subHitValue, int hitSide, boolean leftClick) {
      int carvingType = JBCarvingDataUtil.getCarvingType(te.carvingData);
      switch (carvingType) {
         case 2:
            this.processTypePattern(player, te, boxIndex, hitSide, leftClick);
            break;
         case 4:
            this.processTypeMCube(stack, player, te, boxIndex, hitSide, leftClick);
            break;
         default:
            this.processTypeBox(player, te, boxIndex, hitSide, leftClick);
      }

   }

   private void processTypeBox(Player player, TileEntityJoint te, int boxIndex, int hitSide, boolean leftClick) {
      if (leftClick) {
         JBCarvingDataUtil.modifyBoxedCarveData(te, boxIndex, 0, player);
      } else {
         JBCarvingDataUtil.fillCarveData(0, te, boxIndex, hitSide, player);
      }

   }

   private void processTypeMCube(ItemStack stack, Player player, TileEntityJoint te, int boxIndex, int hitSide, boolean leftClick) {
      byte patternNum;
      if (!stack.hasTag()) {
         patternNum = 0;
      } else {
         patternNum = stack.getTag().getByte("jb:pnum");
         if (patternNum < 1 || patternNum > 16) {
            patternNum = 0;
         }
      }

      boolean fill = patternNum == 0;
      if (patternNum == 0) {
         patternNum = 1;
      }

      if (leftClick) {
         JBCarvingDataUtil.modifyMCubeCarveData(te, boxIndex, -patternNum, fill, player);
      } else if (fill) {
         JBCarvingDataUtil.fillCarveData(4, te, boxIndex, hitSide, player);
      } else {
         JBCarvingDataUtil.modifyMCubeCarveData(te, boxIndex, patternNum, false, player);
      }

   }

   private void processTypePattern(Player player, TileEntityJoint te, int boxIndex, int hitSide, boolean leftClick) {
      if (leftClick) {
         JBCarvingDataUtil.modifyPatternCarveData(te, boxIndex, 0, 0, true, false, player);
      } else {
         JBCarvingDataUtil.fillCarveData(2, te, boxIndex, hitSide, player);
      }

   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (player.isShiftKeyDown()) {
         ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
         if (CompatHelper.isStackEmptyOrNull(stack)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
         }

         CompoundTag tag = stack.getOrCreateTag();
         byte patternNum;
         if (tag.contains("jb:pnum")) {
            patternNum = tag.getByte("jb:pnum");
            if (patternNum >= 1 && patternNum <= 16) {
               patternNum = (byte)(patternNum * 2);
               if (patternNum < 1 || patternNum > 16) {
                  patternNum = 0;
               }
            } else {
               patternNum = 1;
            }
         } else {
            patternNum = 1;
         }

         tag.putByte("jb:pnum", patternNum);
      }

      return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
   }
}
