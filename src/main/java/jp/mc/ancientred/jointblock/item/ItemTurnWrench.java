package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

public class ItemTurnWrench extends Item {
   public static final int TYPE_FACEWRENCH = 0;
   public static final int TYPE_SIDEWRENCH = 1;
   public static final int TYPE_MAGNETWRENCH = 2;
   public static final int TYPE_HAMMER = 3;
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";
   private String[] SUB_NAMES = new String[]{"face", "side", "magnet", "hammer"};

   public ItemTurnWrench() {
      super((new Item.Properties()).stacksTo(1));
   }

   /**
    * 1.12 damage-value subtype, now stored as int NBT tag "subtype"
    * (flattening to separate items is a Phase 6 decision).
    */
   public static int getSubtype(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null ? tag.getInt("subtype") : 0;
   }

   public ItemStack createSubtypedStack(int subtype) {
      ItemStack stack = new ItemStack(this);
      stack.getOrCreateTag().putInt("subtype", subtype);
      return stack;
   }

   @Override
   public String getDescriptionId(ItemStack itemStack) {
      int i = getSubtype(itemStack);
      if (i < 0 || i >= this.SUB_NAMES.length) {
         i = 0;
      }

      return super.getDescriptionId() + "." + this.SUB_NAMES[i];
   }

   @Override
   public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
      return player != null && player.level().getBlockState(pos).getBlock() instanceof BlockJointBase ? true : super.onBlockStartBreak(itemstack, pos, player);
   }

   @Override
   public int getEnchantmentValue() {
      return 0;
   }

   @Override
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   public static boolean isFaceRotateWrench(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemTurnWrench && getSubtype(itemStack) == 0;
   }

   public static boolean isSideRotateWrench(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemTurnWrench && getSubtype(itemStack) == 1;
   }

   public static boolean isMagnetWrench(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemTurnWrench && getSubtype(itemStack) == 2;
   }

   public static boolean isHammer(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemTurnWrench && getSubtype(itemStack) == 3;
   }
}
