package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.carving.JBCarvingClientUtil;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractCarvingToolItem extends Item implements ICeramicModBoundItem {
   public static final String NBT_TAGNAME_PNUM = "jb:pnum";

   public AbstractCarvingToolItem() {
      super((new Item.Properties()).stacksTo(1).durability(120));
   }

   @Override
   public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
      TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(player.level(), pos);
      if (te != null && te.isValidCarveable()) {
         if (player != null && player.level().isClientSide) {
            JBCarvingClientUtil.editCarveDataOnClient(player, te, true);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public float getDestroySpeed(ItemStack stack, BlockState bState) {
      return bState.getBlock() instanceof BlockJointBase ? 1000.0F : super.getDestroySpeed(stack, bState);
   }

   public void onItemUsedToJointBlock(ItemStack stack, Player player, TileEntityJoint te) {
      if (te.getLevel().isClientSide && te.isValidCarveable()) {
         JBCarvingClientUtil.editCarveDataOnClient(player, te, false);
      }

   }

   public abstract void processEditOnServer(ItemStack var1, Player var2, TileEntityJoint var3, int var4, int var5, int var6, boolean var7);

   @Override
   public int getEnchantmentValue() {
      return 0;
   }
}
