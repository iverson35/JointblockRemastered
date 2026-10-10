package jp.mc.ancientred.jointblock.gui;

import java.util.Arrays;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.motion.BasicMotionBinder;
import jp.mc.ancientred.jointblock.registry.JBItems;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 1.12.2 {@code ContainerJointBlock} port: joint motion / unit-parts slots, the init-value
 * layers and the whole 2/4/10..98 motion parameter button protocol.
 */
public class ContainerJointBlock extends AbstractContainerMenu implements IContainerActionReceiver {

    public static final double DISTANCE_MAX = 32.0;
    public static final int USER_INV_STARTSLOT = 2;
    public static final int GUIPAGE_INIT = 0;
    public static final int GUIPAGE_MOTION = 1;
    public static final int ROW_MAX = 4;
    public TileEntityJoint te;
    public Player entityplayer;
    private Level world;
    private int posX;
    private int posY;
    private int posZ;
    public JBGuiSettingsImpl guiButtonSettings;
    int[] copyInitVal;

    public ContainerJointBlock(int containerId, Inventory par1Player, TileEntityJoint par2IInventory) {
        super(JBMenuTypes.JOINT_BLOCK, containerId);
        this.entityplayer = par1Player.player;
        this.te = par2IInventory;
        this.world = par2IInventory.getLevel();
        BlockPos pos = par2IInventory.getBlockPos();
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.guiButtonSettings = new JBGuiSettingsImpl();
        IJBMotionBinderItem.IJBMotionHandler motHndl = this.te.getBindOrFixBindStatus();
        if (this.te.guiPage == 0) {
            EntryFormats.setDefaultJointTypeButtonState(this.guiButtonSettings, true, this.te.getJointType());
        } else {
            motHndl.setGuiEntryState(this.te.getJointType(), this.guiButtonSettings);
        }

        this.addSlot(new Slot(par2IInventory, 0, 11, 22));
        this.addSlot(new Slot(par2IInventory, 1, 149, 22));
        int i = 37;

        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(par1Player, j, 8 + j * 18, 161 + i));
        }
    }

    @Override
    public boolean stillValid(Player par1EntityPlayer) {
        if (!(WorldHelper.getBlock(this.world, this.posX, this.posY, this.posZ) instanceof BlockJointBase)) {
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
            if (slotNum < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                Slot slotTarget;
                if (!(itemstack.getItem() instanceof IJBMotionBinderItem) && itemstack.getItem() != JBItems.UnitPartsItem) {
                    slotTarget = this.slots.get(0);
                    if (!slotTarget.hasItem() && slotTarget.mayPlace(itemstack1)) {
                        slotTarget.set(itemstack1.copy());
                        itemstack1.shrink(1);
                    }
                } else {
                    slotTarget = this.slots.get(1);
                    if (!slotTarget.hasItem()) {
                        slotTarget.set(itemstack1.copy());
                        itemstack1.shrink(1);
                    }
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
    public void receiveButtonAction(Player player, int ID, int startIndex, int amount) {
        if (!this.world.isClientSide) {
            TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(this.world, this.posX, this.posY, this.posZ);
            if (te == null) {
                return;
            }

            if (this.te == null) {
                return;
            }

            if (te != this.te) {
                return;
            }

            if (startIndex < 0 || startIndex > 2) {
                return;
            }

            int jointType = te.getJointType();
            BlockPos pos;
            switch (ID) {
                case 6:
                    te.guiPage = 1;
                    te.markUpdateThis();
                    return;
                case 8:
                    te.guiPage = 0;
                    te.markUpdateThis();
                    return;
                case 96:
                    if (te.initValues.length > 1 && te.initValuesLayer == te.initValues.length - 1) {
                        int[][] newInitVals = new int[te.initValues.length - 1][];
                        System.arraycopy(te.initValues, 0, newInitVals, 0, newInitVals.length);
                        te.initValues = newInitVals;
                        --te.initValuesLayer;
                        te.markUpdateThisWithMirrorProcess(player);
                    }

                    return;
                case 97:
                    if (te.initValuesLayer > te.initValues.length - 1) {
                        return;
                    }

                    this.copyInitVal = Arrays.copyOf(te.initValues[te.initValuesLayer], ROW_MAX);
                    return;
                case 98:
                    if (te.initValuesLayer > te.initValues.length - 1) {
                        return;
                    }

                    if (this.copyInitVal != null) {
                        te.initValues[te.initValuesLayer] = Arrays.copyOf(this.copyInitVal, ROW_MAX);
                        te.markUpdateThisWithMirrorProcess(player);
                    }

                    return;
                case 7222:
                    JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockExtraId, player, this.world, new BlockPos(this.posX, this.posY, this.posZ));
                    return;
                case 7224:
                    TileEntityJoint teConnectedSide = te.getConnectedSideTileEntityJointed();
                    pos = teConnectedSide.getBlockPos();
                    if (teConnectedSide instanceof TileEntityJointCore) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreId, player, this.world, pos);
                    } else if (teConnectedSide instanceof TileEntityJoint) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockId, player, this.world, pos);
                    }

                    return;
                case 7225:
                    if (player == null) {
                        return;
                    }

                    EnumDirectionOld dir = EnumDirectionOld.SOUTH;
                    int d = ((Mth.floor((double)(player.getYRot() * 4.0F / 360.0F) + 0.5) & 3) + 2) % 4;
                    if (d == 0) {
                        dir = EnumDirectionOld.VALID_DIRECTIONS[2];
                    }

                    if (d == 1) {
                        dir = EnumDirectionOld.VALID_DIRECTIONS[5];
                    }

                    if (d == 2) {
                        dir = EnumDirectionOld.VALID_DIRECTIONS[3];
                    }

                    if (d == 3) {
                        dir = EnumDirectionOld.VALID_DIRECTIONS[4];
                    }

                    TileEntityJoint tePlayerFacingSide = BlockJointBase.getTileEntityJointerBase(
                        this.world,
                        te.getBlockPos().getX() + dir.offsetX,
                        te.getBlockPos().getY() + dir.offsetY,
                        te.getBlockPos().getZ() + dir.offsetZ);
                    if (tePlayerFacingSide instanceof TileEntityJointCore) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreId, player, this.world, tePlayerFacingSide.getBlockPos());
                    } else if (tePlayerFacingSide instanceof TileEntityJoint) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockId, player, this.world, tePlayerFacingSide.getBlockPos());
                    }

                    return;
                default:
                    IJBMotionBinderItem.IJBMotionBinder bindHndl = te.getMotionBinder();
                    if (bindHndl == null) {
                        bindHndl = BasicMotionBinder.instance;
                    }

                    IJBMotionBinderItem.IJBMotionHandler motHndl = bindHndl.getBindMotionHandler(te.bindNum, te.getMotionItemDamage());
                    if (motHndl == null) {
                        return;
                    }

                    int[] valuesToset;
                    if (te.guiPage == 0) {
                        if (te.initValuesLayer > te.initValues.length - 1) {
                            return;
                        }

                        valuesToset = te.initValues[te.initValuesLayer];
                        EntryFormats.setDefaultJointTypeButtonState(this.guiButtonSettings, true, jointType);
                    } else {
                        valuesToset = te.params;
                        motHndl.setGuiEntryState(jointType, this.guiButtonSettings);
                    }

                    boolean hasBindChanded = false;
                    int targetIndex = 0;
                    boolean modVal = false;
                    boolean valueSet = false;
                    switch (ID) {
                        case 2:
                            if (te.guiPage == 0) {
                                te.setInitValuesLayerPrev(this.entityplayer);
                            } else {
                                te.validateBindNumOrReset(bindHndl, true);
                            }
                            break;
                        case 4:
                            if (te.guiPage == 0) {
                                te.setInitValuesLayerNext(this.entityplayer);
                            } else {
                                te.validateBindNumOrReset(bindHndl, false);
                            }
                            break;
                        case 10:
                        case 14:
                            modVal = true;
                            targetIndex = startIndex + 0;
                            break;
                        case 20:
                        case 24:
                            modVal = true;
                            targetIndex = startIndex + 1;
                            break;
                        case 30:
                        case 34:
                            modVal = true;
                            targetIndex = startIndex + 2;
                            break;
                        case 40:
                        case 44:
                            modVal = true;
                            targetIndex = startIndex + 3;
                            break;
                        case 81:
                            valueSet = true;
                            targetIndex = startIndex + 0;
                            break;
                        case 82:
                            valueSet = true;
                            targetIndex = startIndex + 1;
                            break;
                        case 83:
                            valueSet = true;
                            targetIndex = startIndex + 2;
                            break;
                        case 84:
                            valueSet = true;
                            targetIndex = startIndex + 3;
                            break;
                        default:
                            return;
                    }

                    if (modVal && targetIndex < valuesToset.length) {
                        if (amount > 0) {
                            valuesToset[targetIndex] = JBGuiSettingsImpl.addIntInRange((long)valuesToset[targetIndex], (long)amount, this.guiButtonSettings.entrySettings[targetIndex].max);
                        } else if (amount < 0) {
                            valuesToset[targetIndex] = JBGuiSettingsImpl.subIntInRange((long)valuesToset[targetIndex], (long)amount, this.guiButtonSettings.entrySettings[targetIndex].min);
                        }
                    } else if (valueSet && targetIndex < valuesToset.length) {
                        valuesToset[targetIndex] = JBGuiSettingsImpl.trySetValue((long)amount, this.guiButtonSettings.entrySettings[targetIndex].min, this.guiButtonSettings.entrySettings[targetIndex].max);
                    }

                    te.checkMotionItemChange();
                    te.markUpdateThis();
            }
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        Level world = this.te.getLevel();
        if (!world.isClientSide && player != null) {
            BlockPos pos = this.te.getBlockPos();
            TileEntityJoint.setMirrorBlock(world, pos.getX(), pos.getY(), pos.getZ(), this.te.getJointFacing(), player, false);
        }
    }
}
