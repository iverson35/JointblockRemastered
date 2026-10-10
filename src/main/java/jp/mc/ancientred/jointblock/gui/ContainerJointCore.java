package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import jp.mc.ancientred.jointblock.unit.AbstractUnitBase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12.2 {@code ContainerJointCore} port: the core's single unit slot plus the player
 * hotbar, and the unit-page/unit-name/assemble button protocol.
 */
public class ContainerJointCore extends AbstractContainerJointCore {

    public static final int USER_INV_STARTSLOT = 1;
    public static final int GUIPAGE_STANDARD = 0;
    public static final int GUIPAGE_UNITRESULT = 1;
    public static final int UNITNAME_MAX = 15;

    public ContainerJointCore(int containerId, Inventory playerIn, TileEntityJointCore teCoreIn) {
        super(JBMenuTypes.JOINT_CORE, containerId, playerIn.player, teCoreIn);
        this.addSlot(new Slot(teCoreIn, 0, 11, 22));
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
            itemstack = itemstack1.copy();
            if (slotNum < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                Slot slotTarget = this.slots.get(0);
                if (!slotTarget.hasItem() && slotTarget.mayPlace(itemstack1)) {
                    slotTarget.set(itemstack1.copy());
                    slotTarget.setChanged();
                    itemstack1.shrink(1);
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

    /**
     * 1.12.2 {@code receiveButtonAction(int ID, int startIndex, int flags, String unitName)}
     * (PACKET_TYPE_GUIACT for this container + PACKET_TYPE_STRINGCHNGED unitname delivery).
     */
    public void receiveButtonAction(int ID, int startIndex, int flags, String unitName) {
        if (!this.world.isClientSide) {
            TileEntityJointCore te = (TileEntityJointCore)BlockJointBase.getTileEntityJointerBase(this.world, this.posX, this.posY, this.posZ);
            if (te == null) {
                return;
            }

            if (this.teCore == null) {
                return;
            }

            if (te != this.teCore) {
                return;
            }

            if (unitName != null) {
                this.receiveUnitNameUpdate(unitName);
            }

            switch (ID) {
                case 2:
                    te.clearUnitTestResult();
                    te.unitId = AbstractUnitBase.getPrevUnitBaseId(te.unitId);
                    if (te.guiPage == 1) {
                        te.guiPage = 0;
                    }

                    te.addonBonus = null;
                    break;
                case 4:
                    te.clearUnitTestResult();
                    te.unitId = AbstractUnitBase.getNextUnitBaseId(te.unitId);
                    if (te.guiPage == 1) {
                        te.guiPage = 0;
                    }

                    te.addonBonus = null;
                    break;
                case 6:
                    te.clearUnitTestResult();
                    EntityJointed ej = te.assemble(te.getLevel(), te.getLevel(), this.entityplayer);
                    if (ej != null) {
                        te.getLevel().addFreshEntity(ej);
                    }
                    break;
                case 8:
                    if (te.guiPage == 0) {
                        te.clearUnitTestResult();
                        te.searchParts(true, false);
                        te.guiPage = 1;
                    } else {
                        te.guiPage = 0;
                    }
                    break;
                case 12:
                case 14:
                case 16:
                    int mask = 0;
                    mask |= 1;
                    mask |= 2;
                    mask |= 4;
                    mask |= 8;
                    te.assembleFlags &= ~mask;
                    te.assembleFlags |= flags;
                    break;
                case 20:
                    if (++te.slaveSlotNum > 10) {
                        te.slaveSlotNum = 10;
                    }
                    break;
                case 21:
                    if (--te.slaveSlotNum < 0) {
                        te.slaveSlotNum = 0;
                    }
                    break;
                case 7222:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockExtraId, this.entityplayer, this.world, te.getBlockPos());
                    return;
                case 7223:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointChestFromBlockId, this.entityplayer, this.world, te.getBlockPos());
                    return;
                case 7226:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreFuelsFromBlockId, this.entityplayer, this.world, te.getBlockPos());
                    return;
                case 7227:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreAddonsId, this.entityplayer, this.world, te.getBlockPos());
                    return;
                case 7228:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreSoundsId, this.entityplayer, this.world, te.getBlockPos());
                    return;
                default:
                    break;
            }

            te.markUpdateThis();
        }
    }

    public void receiveUnitNameUpdate(String unitNameFromPacket) {
        if (unitNameFromPacket != null && unitNameFromPacket.length() > UNITNAME_MAX) {
            this.teCore.unitName = unitNameFromPacket.substring(0, UNITNAME_MAX);
        } else {
            this.teCore.unitName = unitNameFromPacket;
        }

        this.teCore.markUpdateThis();
    }
}
