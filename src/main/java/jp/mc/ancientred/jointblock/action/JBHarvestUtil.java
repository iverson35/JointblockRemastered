package jp.mc.ancientred.jointblock.action;

import java.util.List;
import jp.mc.ancientred.jointblock.api.IJBEntityState;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class JBHarvestUtil {
   public static void harvestBlockToInventoryDropItem(Level world, LivingEntity entityConductor, IJBEntityState entityState, int targetX, int targetY, int targetZ, Block blockHit, int slotLimit) {
      List<ItemStack> list = Block.getDrops(WorldHelper.getBlockState(world, targetX, targetY, targetZ), (ServerLevel)world, new BlockPos(targetX, targetY, targetZ), null);
      sendItemListToInventory(world, entityState, list, targetX, targetY, targetZ, slotLimit);
      WorldHelper.setBlockToAir(world, targetX, targetY, targetZ);
      world.levelEvent(2001, new BlockPos(targetX, targetY, targetZ), Block.getId(WorldHelper.getBlockState(world, targetX, targetY, targetZ)));
   }

   private static void sendItemListToInventory(Level world, IJBEntityState entityState, List<ItemStack> list, int targetX, int targetY, int targetZ, int slotLimit) {
      Container innerInventory = entityState.getInnerInventory();

      for(int i = 0; i < list.size(); ++i) {
         ItemStack itemStack = (ItemStack)list.get(i);
         if (!CompatHelper.isStackEmptyOrNull(itemStack)) {
            itemStack = sendItemToInventory(innerInventory, (ItemStack)list.get(i), slotLimit);
            if (!CompatHelper.isStackEmptyOrNull(itemStack)) {
               float f = 0.7F;
               double randX = (double)(world.random.nextFloat() * f) + (double)(1.0F - f) * 0.5;
               double randY = (double)(world.random.nextFloat() * f) + (double)(1.0F - f) * 0.5;
               double randZ = (double)(world.random.nextFloat() * f) + (double)(1.0F - f) * 0.5;
               ItemEntity entityitem = new ItemEntity(world, (double)targetX + randX, (double)targetY + randY, (double)targetZ + randZ, itemStack);
               entityitem.setDefaultPickUpDelay();
               world.addFreshEntity(entityitem);
            }
         }
      }

   }

   private static ItemStack sendItemToInventory(Container targetInventory, ItemStack itemStackToPut, int slotLimit) {
      for(int i = 0; i < targetInventory.getContainerSize() && i < slotLimit && !CompatHelper.isStackEmptyOrNull(itemStackToPut); ++i) {
         ItemStack itemStackInSlot = targetInventory.getItem(i);
         if (targetInventory.canPlaceItem(i, itemStackToPut)) {
            boolean slotChanged = false;
            int max;
            if (CompatHelper.isStackEmptyOrNull(itemStackInSlot)) {
               max = Math.min(itemStackToPut.getMaxStackSize(), targetInventory.getMaxStackSize());
               if (max >= itemStackToPut.getCount()) {
                  targetInventory.setItem(i, itemStackToPut);
                  itemStackToPut = ItemStack.EMPTY;
               } else {
                  targetInventory.setItem(i, itemStackToPut.split(max));
               }

               slotChanged = true;
            } else if (isSameItemStack(itemStackInSlot, itemStackToPut)) {
               max = Math.min(itemStackToPut.getMaxStackSize(), targetInventory.getMaxStackSize());
               if (max > itemStackInSlot.getCount()) {
                  int l = Math.min(itemStackToPut.getCount(), max - itemStackInSlot.getCount());
                  itemStackToPut.shrink(1);
                  itemStackInSlot.grow(1);
                  slotChanged = l > 0;
               }
            }

            if (slotChanged) {
               targetInventory.setChanged();
            }
         }
      }

      return CompatHelper.isStackEmptyOrNull(itemStackToPut) ? ItemStack.EMPTY : itemStackToPut;
   }

   private static boolean isSameItemStack(ItemStack compareA, ItemStack compareB) {
      return compareA.getItem() != compareB.getItem() ? false : (compareA.getDamageValue() != compareB.getDamageValue() ? false : (compareA.getCount() > compareA.getMaxStackSize() ? false : ItemStack.isSameItemSameTags(compareA, compareB)));
   }
}
