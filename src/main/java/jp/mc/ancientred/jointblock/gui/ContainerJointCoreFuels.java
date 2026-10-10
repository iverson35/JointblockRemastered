package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.assemble.AssembleFlagsUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.JointInventory;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 1.12.2 {@code ContainerJointCoreFuels} port: the 5 fuel slots (45..49) and 4 ring
 * program slots (50..53) of the core's inner chest, with the fuel/ring side effects
 * performed on {@code setChanged}.
 */
public class ContainerJointCoreFuels extends AbstractContainerJBInventoryHolder implements IContainerActionReceiver {

    public static final int USER_INV_STARTSLOT = 9;
    public static final int GUIPAGE_INIT = 0;
    public static final int GUIPAGE_MOTION = 1;
    public static final int ROW_MAX = 4;
    public static final int FUEL_SLOT_LEFT = 7;
    public static final int RING_SLOT_TOP = 65;
    public static final int RING_SLOT_LEFT = 41;
    public static final int INV_YPOS = 140;

    public ContainerJointCoreFuels(int containerId, Inventory playerIn, JointInventory jointChestInventoryIn) {
        this(JBMenuTypes.JOINT_CORE_FUELS_FROM_BLOCK, containerId, playerIn, jointChestInventoryIn);
    }

    public ContainerJointCoreFuels(MenuType<?> type, int containerId, Inventory playerIn, JointInventory jointChestInventoryIn) {
        super(type, containerId, playerIn.player, jointChestInventoryIn);

        int yPos;
        for (yPos = 0; yPos < 5; ++yPos) {
            this.addSlot(new Slot(jointChestInventoryIn, 45 + yPos, 7 + yPos * 18 + 1, 36) {
                @Override
                public void setChanged() {
                    EntityJointed ej = ContainerJointCoreFuels.this.innerChestInventory.getInventoryHolderEntityJointed();
                    if (ej != null && !ej.level().isClientSide) {
                        float fuelNew = ContainerJointCoreFuels.this.innerChestInventory.getFuelValue(true);
                        ej.setEFuelPool(fuelNew);
                    }
                }
            });
        }

        yPos = RING_SLOT_TOP;
        int ringCol = 2;

        int j;
        for (j = 0; j < 4; ++j) {
            if (j == ringCol) {
                yPos += 19;
            }

            this.addSlot(new Slot(jointChestInventoryIn, 50 + j, RING_SLOT_LEFT + j % ringCol * 19 + 1, yPos + 1) {
                @Override
                public void setChanged() {
                    EntityJointed ej = ContainerJointCoreFuels.this.innerChestInventory.getInventoryHolderEntityJointed();
                    if (ej != null && !ej.level().isClientSide) {
                        ej.clearRingPrograms();
                    }
                }
            });
        }

        yPos = INV_YPOS;

        int k;
        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerIn, k + j * 9 + 9, 8 + k * 18, yPos + j * 18));
            }
        }

        int i = 37;

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerIn, j, 8 + j * 18, 161 + i));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotNum) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotNum);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            int stackSizeSave = itemstack1.getCount();
            if (slotNum < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemRingProgram.isRingProgramItem(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 5, 9, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 5, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == stackSizeSave) {
                return ItemStack.EMPTY;
            }
        }

        return itemstack;
    }

    @Override
    public void receiveButtonAction(Player player, int ID, int startIndex, int amount) {
        TileEntityJointCore teCore = this.innerChestInventory.getInventoryHolderTileEntityJointCore();
        if (teCore != null) {
            Level world = teCore.getLevel();
            BlockPos tePos = teCore.getBlockPos();
            int posX = tePos.getX();
            int posY = tePos.getY();
            int posZ = tePos.getZ();
            switch (ID) {
                case 10:
                    if (player != null && player.getAbilities().instabuild) {
                        teCore.assembleFlags = AssembleFlagsUtil.setInfiniteFuel(teCore.assembleFlags, amount != 0);
                        teCore.markUpdateThis();
                        return;
                    }

                    return;
                case 7221:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreId, player, world, tePos);
                    return;
                case 7222:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockExtraId, player, world, tePos);
                    return;
                case 7223:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointChestFromBlockId, player, world, tePos);
                    return;
                case 7227:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreAddonsId, player, world, tePos);
                    return;
                case 7228:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreSoundsId, player, world, tePos);
                    return;
                default:
                    break;
            }
        } else {
            EntityJointed ej = this.innerChestInventory.getInventoryHolderEntityJointed();
            if (ej != null) {
                switch (ID) {
                    case 7223:
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointChestFromEntityId, player, ej);
                        return;
                    default:
                        break;
                }
            }
        }
    }
}
