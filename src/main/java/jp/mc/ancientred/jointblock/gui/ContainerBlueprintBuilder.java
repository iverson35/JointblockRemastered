package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.assemble.JointDataSerializer;
import jp.mc.ancientred.jointblock.assemble.JointListsHolderImpl;
import jp.mc.ancientred.jointblock.block.BlockBlueprintBuilder;
import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.export.JBEntityJBFactory;
import jp.mc.ancientred.jointblock.item.ItemJBBlueprint;
import jp.mc.ancientred.jointblock.item.ItemJBController;
import jp.mc.ancientred.jointblock.tileentity.TileEntityBlueprintBuilder;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * 1.12.2 {@code ContainerBlueprintBuilder} port: the blueprint slot plus the player
 * inventory, and the "build from blueprint" button whose execution replaces the
 * builder block by the assembled joint structure.
 */
public class ContainerBlueprintBuilder extends AbstractContainerMenu implements IContainerActionReceiver {

    public static final int USER_INV_STARTSLOT = 1;
    public static final double DISTANCE_MAX = 32.0;
    public TileEntityBlueprintBuilder te;
    public Player entityplayer;
    private Level world;
    private int posX;
    private int posY;
    private int posZ;

    public ContainerBlueprintBuilder(int containerId, Inventory par1Player, TileEntityBlueprintBuilder par2IInventory) {
        super(JBMenuTypes.BLUEPRINT_BUILDER, containerId);
        this.entityplayer = par1Player.player;
        this.te = par2IInventory;
        this.world = par2IInventory.getLevel();
        this.posX = par2IInventory.getBlockPos().getX();
        this.posY = par2IInventory.getBlockPos().getY();
        this.posZ = par2IInventory.getBlockPos().getZ();
        this.addSlot(new Slot(par2IInventory, 0, 11, 22));
        int i = 37;

        int j;
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(par1Player, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(par1Player, j, 8 + j * 18, 161 + i));
        }
    }

    @Override
    public boolean stillValid(Player par1EntityPlayer) {
        if (!(WorldHelper.getBlock(this.world, this.posX, this.posY, this.posZ) instanceof BlockBlueprintBuilder)) {
            return false;
        } else {
            // 1.12.2: par1EntityPlayer.getDistance(cx, cy, cz) < 32.0
            return par1EntityPlayer.distanceToSqr((double)this.posX + 0.5, (double)this.posY + 0.5, (double)this.posZ + 0.5) < DISTANCE_MAX * DISTANCE_MAX;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotNum) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotNum);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotNum < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                Slot slotZero = this.slots.get(0);
                if (itemstack.getItem() instanceof ItemJBBlueprint && !slotZero.hasItem()) {
                    slotZero.set(itemstack1.copy());
                    itemstack1.shrink(1);
                } else if (slotNum < 28) {
                    if (!this.moveItemStackTo(itemstack1, 28, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
        }

        return itemstack;
    }

    @Override
    public void receiveButtonAction(Player player, int ID, int checkedId, int unused) {
        if (!this.world.isClientSide) {
            TileEntityBlueprintBuilder te = getTileEntityBlueprintBuilder(this.world, this.posX, this.posY, this.posZ);
            if (te != null) {
                if (this.te != null) {
                    if (te == this.te) {
                        CompoundTag dataTag = this.getBlueprintDataFromSlotZero();
                        if (dataTag != null && !dataTag.getBoolean("jb:outdate")) {
                            try {
                                int facing = te.getFacing();
                                JointListsHolderImpl listHolder = JBEntityJBFactory.getSerializedFromDataTag(dataTag);
                                if (listHolder.serialized == null) {
                                    this.setOutdatedToNBT(dataTag);
                                    return;
                                }

                                int partsCount = dataTag.getInt("tnm");
                                JointDataSerializer.rebuildAll(listHolder.serialized, partsCount, listHolder);
                                int fixedY = this.posY;
                                if (checkedId == 10) {
                                    fixedY += -listHolder.minY;
                                } else if (checkedId == 12) {
                                    fixedY -= listHolder.maxY;
                                }

                                te.breakByBuild = true;
                                WorldHelper.setBlockToAir(this.world, this.posX, this.posY, this.posZ);
                                listHolder.createBlocksFromRefLists(new JointListsHolderImpl.DefaultBlockArranger(this.world), facing, this.posX, fixedY, this.posZ);
                                TileEntityJointCore teCore = BlockJointCore.getTileEntityJointerCore(this.world, this.posX, fixedY, this.posZ);
                                if (teCore != null && listHolder.coreSubTag != null) {
                                    teCore.readOriginalValuesExcept(listHolder.coreSubTag, true);
                                    if (teCore.innerChest != null) {
                                        teCore.innerChest.readRINGDatasFromImportNBT(listHolder.coreSubTag);
                                    }

                                    if (teCore.effectChest != null) {
                                        teCore.effectChest.readEffectSlotDatasFromImportNBT(listHolder.coreSubTag);
                                    }
                                }

                                if (this.entityplayer != null) {
                                    ItemJBController.removeReferringPositionTag(this.entityplayer, this.posX, this.posY, this.posZ);
                                }
                            } catch (Exception var12) {
                                var12.printStackTrace();
                                player.displayClientMessage(JBCommonUtil.makeErrorChat("Error during construction."), false);
                                if (dataTag != null) {
                                    this.setOutdatedToNBT(dataTag);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setOutdatedToNBT(CompoundTag dataTag) {
        dataTag.putBoolean("jb:outdate", true);
        this.slots.get(0).setChanged();
        this.te.markUpdateThis();
    }

    private CompoundTag getBlueprintDataFromSlotZero() {
        Slot slotZero = this.slots.get(0);
        return ItemJBBlueprint.getBlueprintDataFromStack(slotZero.getItem());
    }

    private static TileEntityBlueprintBuilder getTileEntityBlueprintBuilder(BlockGetter world, int par2, int par3, int par4) {
        net.minecraft.world.level.block.entity.BlockEntity te = WorldHelper.getTileEntity(world, par2, par3, par4);
        return te instanceof TileEntityBlueprintBuilder ? (TileEntityBlueprintBuilder)te : null;
    }
}
