package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.JointInventory;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12.2 {@code ContainerJointChest} port.  The chest is reachable from a
 * {@code TileEntityJointCore} (9x8 slots) and from an {@code EntityJointed}
 * (which owns its own {@code JointInventory}), hence the {@link MenuType} parameter of
 * the main constructor: the two 1.12 GUI ids 4602/4603 map onto two menu types.
 */
public class ContainerJointChest extends AbstractContainerJBInventoryHolder implements IContainerActionReceiver {

    public static final int ROW_1_YPOS = 18;
    public static final int ROW_2_YPOS = 69;
    public static final int ROW_3_YPOS = 140;
    public static final int ROW_4_YPOS = 198;
    private int numRows;

    public ContainerJointChest(int containerId, Inventory playerInv, JointInventory jointChestInventoryIn) {
        this(JBMenuTypes.JOINT_CHEST_FROM_BLOCK, containerId, playerInv, jointChestInventoryIn);
    }

    public ContainerJointChest(MenuType<?> type, int containerId, Inventory playerInv, JointInventory jointChestInventoryIn) {
        super(type, containerId, playerInv.player, jointChestInventoryIn);
        Inventory playerInventory = playerInv;
        this.numRows = jointChestInventoryIn.getContainerSize() / 9;
        int yPos = 18;

        int j;
        int k;
        for (j = 0; j < 2; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(jointChestInventoryIn, k + j * 9, 8 + k * 18, yPos + j * 18));
            }
        }

        yPos = 69;

        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(jointChestInventoryIn, 18 + k + j * 9, 8 + k * 18, yPos + j * 18));
            }
        }

        yPos = 140;

        for (j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, yPos + j * 18));
            }
        }

        yPos = 198;

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, yPos));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotClicked) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotClicked);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotClicked < this.numRows * 9) {
                if (!this.moveItemStackTo(itemstack1, this.numRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.numRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void receiveButtonAction(Player player, int ID, int startIndex, int amount) {
        if (player != null) {
            TileEntityJointCore teCore = this.innerChestInventory.getInventoryHolderTileEntityJointCore();
            if (teCore != null) {
                BlockPos pos = teCore.getBlockPos();
                switch (ID) {
                    case 7221:
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreId, player, player.level(), pos);
                        return;
                    case 7222:
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockExtraId, player, player.level(), pos);
                        return;
                    case 7223:
                    case 7224:
                    case 7225:
                    default:
                        break;
                    case 7226:
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreFuelsFromBlockId, player, player.level(), pos);
                        return;
                    case 7227:
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreAddonsId, player, player.level(), pos);
                        return;
                    case 7228:
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreSoundsId, player, player.level(), pos);
                        return;
                }
            } else {
                EntityJointed ej = this.innerChestInventory.getInventoryHolderEntityJointed();
                if (ej != null) {
                    switch (ID) {
                        case 7226:
                            JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreFuelsFromEntityId, player, ej);
                            return;
                        default:
                            break;
                    }
                }
            }
        }
    }
}
