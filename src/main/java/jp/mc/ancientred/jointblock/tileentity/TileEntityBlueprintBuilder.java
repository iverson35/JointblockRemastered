package jp.mc.ancientred.jointblock.tileentity;

import jp.mc.ancientred.jointblock.block.BlockBlueprintBuilder;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.registry.JBBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBlueprintBuilder extends BlockEntity implements Container {
   public static final int INV_SLOT_BLUEPRINT_ITEM = 0;
   public boolean breakByBuild;
   private Direction facing;
   private NonNullList<ItemStack> itemStacks;

   public TileEntityBlueprintBuilder(BlockPos pos, BlockState state) {
      super(JBBlockEntityTypes.BLUEPRINT_BUILDER, pos, state);
      this.itemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
   }

   public int getFacing() {
      if (this.facing == null) {
         BlockState iblockstate = this.level.getBlockState(this.worldPosition);
         this.facing = iblockstate.getValue(BlockBlueprintBuilder.FACING);
      }

      return this.facing.get3DDataValue() & 7;
   }

   @Override
   public void load(CompoundTag par1NBTTagCompound) {
      super.load(par1NBTTagCompound);
      ListTag nbttaglist = par1NBTTagCompound.getList("Items", 10);

      for(int i = 0; i < nbttaglist.size(); ++i) {
         CompoundTag nbttagcompound1 = nbttaglist.getCompound(i);
         int j = nbttagcompound1.getByte("Slot") & 255;
         if (j >= 0 && j < this.itemStacks.size()) {
            this.itemStacks.set(j, ItemStack.of(nbttagcompound1));
         }
      }

   }

   @Override
   protected void saveAdditional(CompoundTag par1NBTTagCompound) {
      super.saveAdditional(par1NBTTagCompound);
      ListTag nbttaglist = new ListTag();
      int i = 0;

      for(int len = this.itemStacks.size(); i < len; ++i) {
         if (!this.itemStacks.get(i).isEmpty()) {
            CompoundTag nbttagcompound1 = new CompoundTag();
            nbttagcompound1.putByte("Slot", (byte)i);
            this.itemStacks.get(i).save(nbttagcompound1);
            nbttaglist.add(nbttagcompound1);
         }
      }

      par1NBTTagCompound.put("Items", nbttaglist);
   }

   @Override
   public CompoundTag getUpdateTag() {
      return this.saveWithFullMetadata();
   }

   @Override
   public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
      CompoundTag tag = pkt.getTag();
      if (tag != null) {
         this.load(tag);
      }
   }

   @Override
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      CompoundTag tag = this.getUpdateTag();
      return ClientboundBlockEntityDataPacket.create(this, (be) -> tag);
   }

   public void markUpdateThis() {
      WorldHelper.markBlockForUpdate(this.level, this.worldPosition);
      this.setChanged();
   }

   @Override
   public int getContainerSize() {
      return this.itemStacks.size();
   }

   @Override
   public ItemStack getItem(int slot) {
      return this.itemStacks.get(slot);
   }

   @Override
   public ItemStack removeItem(int slot, int num) {
      return ContainerHelper.removeItem(this.itemStacks, slot, num);
   }

   @Override
   public void setItem(int slotIndex, ItemStack stack) {
      ItemStack oldStack = this.itemStacks.get(slotIndex);
      this.itemStacks.set(slotIndex, stack);
      if (!CompatHelper.isStackEmptyOrNull(stack) && stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }

   }

   public String getName() {
      return "";
   }

   public boolean hasCustomName() {
      return true;
   }

   @Override
   public int getMaxStackSize() {
      return 1;
   }

   @Override
   public boolean stillValid(Player par1EntityPlayer) {
      int xCoordi = this.worldPosition.getX();
      int yCoordi = this.worldPosition.getY();
      int zCoordi = this.worldPosition.getZ();
      // 1.12.2: par1EntityPlayer.getDistance(cx, cy, cz) < 10.0 (getDistance is not squared)
      return WorldHelper.getTileEntity(this.level, xCoordi, yCoordi, zCoordi) != this ? false : par1EntityPlayer.distanceToSqr((double)xCoordi + 0.5, (double)yCoordi + 0.5, (double)zCoordi + 0.5) < 100.0;
   }

   @Override
   public void startOpen(Player player) {
   }

   @Override
   public void stopOpen(Player player) {
   }

   @Override
   public boolean canPlaceItem(int slot, ItemStack itemStack) {
      return true;
   }

   @Override
   public ItemStack removeItemNoUpdate(int index) {
      return ContainerHelper.takeItem(this.itemStacks, index);
   }

   @Override
   public boolean isEmpty() {
      int i = 0;

      for(int len = this.itemStacks.size(); i < len; ++i) {
         if (!this.itemStacks.get(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void clearContent() {
      this.itemStacks.clear();
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }
}
