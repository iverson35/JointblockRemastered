package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemCeramicPutty extends Item implements ICeramicModBoundItem, IColorHanldlerItem {
   public static final int TYPE_BOXES = 0;
   public static final int TYPE_PATRN = 2;
   public static final int TYPE_MCUBE = 4;
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";

   public ItemCeramicPutty() {
      super(new Item.Properties());
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
   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   public void onItemUsedToJointBlock(ItemStack stack, Player player, TileEntityJoint te) {
      if (!te.getLevel().isClientSide) {
         if (getSubtype(stack) == 4) {
            te.appendCarvingDivide(4, player);
         } else if (getSubtype(stack) == 2) {
            te.appendCarvingDivide(2, player);
         } else {
            te.appendCarvingDivide(0, player);
         }

         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
      }

   }

   @Override
   public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
      return player != null && player.level().getBlockState(pos).getBlock() instanceof BlockJointBase ? true : super.onBlockStartBreak(itemstack, pos, player);
   }

   @Override
   public int getEnchantmentValue() {
      return 0;
   }

   @OnlyIn(Dist.CLIENT)
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      if (getSubtype(itemStack) == 4) {
         return 10062079;
      } else {
         return getSubtype(itemStack) == 2 ? 14509602 : DEFAULT.getColorFromItemStack(itemStack, pass);
      }
   }

   @Override
   public String getDescriptionId(ItemStack itemStack) {
      int damage = getSubtype(itemStack);
      if (damage == 4) {
         return super.getDescriptionId() + ".mcube";
      } else {
         return damage == 2 ? super.getDescriptionId() + ".pattern" : super.getDescriptionId() + ".box";
      }
   }
}
