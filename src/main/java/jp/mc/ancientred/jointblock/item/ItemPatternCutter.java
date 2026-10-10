package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingUtil;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemPatternCutter extends AbstractCarvingToolItem {
   public static final int PATTERN_INDEX_ARGS = 1;
   public static final int PATTERN_INDEX_TURN = 2;
   public static final int PATTERN_INDEX_FLIP = 3;
   public static final int PATTERN_INDEX_COPY = 4;
   public static final int PATTERN_INDEX_SIDE_FACE = 5;
   public static final int PATTERN_INDEX_MAX = 5;
   public static final String NBT_TAGNAME_COPYVAL = "jb:cpv";

   @Override
   public void processEditOnServer(ItemStack stack, Player player, TileEntityJoint te, int boxIndex, int subHitValue, int hitSide, boolean leftClick) {
      int carvingType = JBCarvingDataUtil.getCarvingType(te.carvingData);
      int patternNum = getPatternNumFromStackNBT(stack);
      int facing;
      if (carvingType == 2) {
         if (patternNum == 0) {
            if (leftClick) {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -1, 1, player);
            } else {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -1, -1, player);
            }
         } else if (patternNum == 2) {
            if (leftClick) {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -5, 1, player);
            } else {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -4, 1, player);
            }
         } else if (patternNum == 5) {
            if (leftClick) {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -2, 1, player);
            } else {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -3, 1, player);
            }
         } else if (patternNum == 3) {
            if (leftClick) {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -7, 1, player);
            } else {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -6, 1, player);
            }
         } else if (patternNum == 4) {
            int valuePattern;
            if (leftClick) {
               if (stack.hasTag()) {
                  CompoundTag tag = stack.getTag();
                  if (tag.contains("jb:cpv")) {
                     valuePattern = tag.getInt("jb:cpv") & 131071;
                     valuePattern = JBPatternCarvingUtil.fixFacingForValueCopyDst(valuePattern, te.getJointFacing(), player);
                     JBCarvingDataUtil.modifyPatternCarveData(te, boxIndex, 0, valuePattern, false, true, player);
                  }
               }
            } else {
               facing = te.getJointFacing();
               valuePattern = JBCarvingDataUtil.getPatternValueForBoxIndex(te.carvingData, boxIndex);
               if (valuePattern != -1) {
                  if (!stack.hasTag()) {
                     stack.setTag(new CompoundTag());
                  }

                  CompoundTag tag = stack.getTag();
                  valuePattern = JBPatternCarvingUtil.fixFacingForValueCopySrc(valuePattern, te.getJointFacing(), player);
                  tag.putInt("jb:cpv", valuePattern);
               }
            }
         } else if (patternNum == 1) {
            if (subHitValue < 0) {
               return;
            }

            if (leftClick) {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, subHitValue, -1, player);
            } else {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, subHitValue, 1, player);
            }
         }
      } else if (carvingType == 0 && patternNum == 0) {
         facing = JBCarvingDataUtil.getCarvingDivide(te.carvingData);
         if (facing <= 5) {
            te.carvingData = JBCarvingDataUtil.convertTypeCubeToTypePattern(te.carvingData);
            if (leftClick) {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -1, 1, player);
            } else {
               JBCarvingDataUtil.togglePatternCarveData(te, boxIndex, -1, -1, player);
            }
         }
      }

   }

   private static int getPatternNumFromStackNBT(ItemStack stack) {
      int patternNum = 0;
      if (stack.hasTag()) {
         patternNum = stack.getTag().getByte("jb:pnum");
         if (patternNum < 1 || patternNum > 5) {
            patternNum = 0;
         }
      }

      return patternNum;
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
               if (patternNum >= 1 && patternNum <= 5) {
                  ++patternNum;
                  if (patternNum < 1 || patternNum > 5) {
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

   public static boolean isPatternArgsCutter(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemPatternCutter && getPatternNumFromStackNBT(itemStack) == 1;
   }

   public static boolean isPatternSideFaceCutter(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemPatternCutter && getPatternNumFromStackNBT(itemStack) == 5;
   }
}
