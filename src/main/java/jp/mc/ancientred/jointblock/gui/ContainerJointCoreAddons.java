package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12.2 {@code ContainerJointCoreAddons} port: the addon part of the core's inner chest
 * (slots 54..62) plus the player hotbar.
 */
public class ContainerJointCoreAddons extends AbstractContainerJointCore implements IContainerActionReceiver {

    public static final int USER_INV_STARTSLOT = 9;
    public static final int ADDON_SLOT_LEFT = 7;
    public static final int INV_YPOS = 140;

    public ContainerJointCoreAddons(int containerId, Inventory playerIn, TileEntityJointCore teCoreIn) {
        super(JBMenuTypes.JOINT_CORE_ADDONS, containerId, playerIn.player, teCoreIn);

        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(teCoreIn.innerChest, 54 + j, 7 + j * 18 + 1, 36));
        }

        int i = 37;

        for (int j = 0; j < 9; ++j) {
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
            } else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
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
        switch (ID) {
            case 10:
                this.teCore.searchParts(false, true);
                return;
            case 16:
                if (this.teCore.addonBonus != null) {
                    this.teCore.addonBonus = null;
                    this.teCore.markUpdateThis();
                }

                return;
            case 7221:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreId, player, this.world, this.teCore.getBlockPos());
                return;
            case 7222:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockExtraId, this.entityplayer, this.world, this.teCore.getBlockPos());
                return;
            case 7223:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointChestFromBlockId, this.entityplayer, this.world, this.teCore.getBlockPos());
                return;
            case 7226:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreFuelsFromBlockId, this.entityplayer, this.world, this.teCore.getBlockPos());
                return;
            case 7228:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreSoundsId, this.entityplayer, this.world, this.teCore.getBlockPos());
                return;
            default:
                break;
        }
    }
}
