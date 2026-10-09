package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.assemble.AssembleFlagsUtil;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 1.12.2 {@code ContainerJointBlockExtra} port: the "extra" page of a joint block
 * (board view flags, assemble flags, attracter common flags, extra joint state flags and
 * the memo string).  It owns no slot in 1.12.2 either.
 */
public class ContainerJointBlockExtra extends AbstractContainerMenu implements IContainerActionReceiver {

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

    public ContainerJointBlockExtra(int containerId, Inventory par1Player, TileEntityJoint par2IInventory) {
        super(JBMenuTypes.JOINT_BLOCK_EXTRA, containerId);
        this.entityplayer = par1Player.player;
        this.te = par2IInventory;
        this.world = par2IInventory.getLevel();
        BlockPos pos = par2IInventory.getBlockPos();
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
    }

    @Override
    public boolean stillValid(Player par1EntityPlayer) {
        if (!(WorldHelper.getBlock(this.world, this.posX, this.posY, this.posZ) instanceof BlockJointBase)) {
            return false;
        } else {
            // 1.12.2: par1EntityPlayer.getDistance(cx, cy, cz) < 32.0
            return par1EntityPlayer.distanceToSqr((double)this.posX + 0.5, (double)this.posY + 0.5, (double)this.posZ + 0.5) < 32.0 * 32.0;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotNum) {
        return ItemStack.EMPTY;
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

            int jointType = te.getJointType();
            boolean hasChanged = false;
            if (te instanceof TileEntityJointCore) {
                TileEntityJointCore teCore = (TileEntityJointCore)te;
                switch (ID) {
                    case 20:
                        if (amount == 1) {
                            teCore.boardViewFlags = (byte)(teCore.boardViewFlags | 128);
                        } else {
                            teCore.boardViewFlags = (byte)(teCore.boardViewFlags & -129);
                        }

                        hasChanged = true;
                        // 1.12.2 falls through into case 22 (no break in the original switch)
                    case 22:
                        if (amount == 1) {
                            teCore.boardViewFlags = (byte)(teCore.boardViewFlags | 64);
                        } else {
                            teCore.boardViewFlags &= -65;
                        }

                        hasChanged = true;
                        break;
                    case 24:
                        teCore.boardViewFlags &= -16;
                        if (amount == 1) {
                            teCore.boardViewFlags = (byte)(teCore.boardViewFlags | 0);
                        }

                        hasChanged = true;
                        break;
                    case 25:
                        teCore.boardViewFlags &= -16;
                        if (amount == 1) {
                            teCore.boardViewFlags = (byte)(teCore.boardViewFlags | 1);
                        }

                        hasChanged = true;
                        break;
                    case 26:
                        teCore.boardViewFlags &= -16;
                        if (amount == 1) {
                            teCore.boardViewFlags = (byte)(teCore.boardViewFlags | 2);
                        }

                        hasChanged = true;
                        break;
                    case 27:
                        teCore.boardViewFlags &= -16;
                        if (amount == 1) {
                            teCore.boardViewFlags = (byte)(teCore.boardViewFlags | 3);
                        }

                        hasChanged = true;
                        break;
                    case 30:
                        teCore.assembleFlags = AssembleFlagsUtil.setSlaveMotIndependent(teCore.assembleFlags, amount == 1);
                        hasChanged = true;
                        break;
                    case 32:
                        teCore.assembleFlags = AssembleFlagsUtil.setSlaveOpIndependent(teCore.assembleFlags, amount == 1);
                        hasChanged = true;
                        break;
                    case 34:
                        teCore.assembleFlags = AssembleFlagsUtil.setManualSetLargeWeight(teCore.assembleFlags, amount == 1);
                        hasChanged = true;
                        break;
                    case 50:
                    case 52:
                    case 54:
                    case 56:
                        teCore.largeUnitWeightFixed = (short)Mth.clamp(amount, 1, 300);
                        hasChanged = true;
                    default:
                        break;
                }
            } else if (te instanceof TileEntityJointAttracter || te.jointType == 2) {
                switch (ID) {
                    case 41:
                        if (amount == 1) {
                            te.setExtraFlagsTypeSpecCommonA(true);
                        } else {
                            te.setExtraFlagsTypeSpecCommonA(false);
                        }

                        hasChanged = true;
                        break;
                    case 42:
                        if (amount == 1) {
                            te.setExtraFlagsTypeSpecCommonB(true);
                        } else {
                            te.setExtraFlagsTypeSpecCommonB(false);
                        }

                        hasChanged = true;
                        break;
                    case 43:
                        if (amount == 1) {
                            te.setExtraFlagsTypeSpecCommonC(true);
                        } else {
                            te.setExtraFlagsTypeSpecCommonC(false);
                        }

                        hasChanged = true;
                    default:
                        break;
                }
            }

            switch (ID) {
                case 10:
                case 12:
                case 14:
                case 16:
                    int mask = -503316480;
                    te.extraJointStateFlags &= ~mask;
                    amount &= mask;
                    te.extraJointStateFlags |= amount;
                    break;
                case 7221:
                    if (te instanceof TileEntityJointCore) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreId, player, this.world, new BlockPos(this.posX, this.posY, this.posZ));
                    } else {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockId, player, this.world, new BlockPos(this.posX, this.posY, this.posZ));
                    }

                    return;
                case 7223:
                    if (te instanceof TileEntityJointCore) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointChestFromBlockId, player, this.world, new BlockPos(this.posX, this.posY, this.posZ));
                    }

                    return;
                case 7226:
                    if (te instanceof TileEntityJointCore) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreFuelsFromBlockId, player, this.world, new BlockPos(this.posX, this.posY, this.posZ));
                    }

                    return;
                case 7227:
                    if (te instanceof TileEntityJointCore) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreAddonsId, player, this.world, new BlockPos(this.posX, this.posY, this.posZ));
                    }

                    return;
                case 7228:
                    if (te instanceof TileEntityJointCore) {
                        JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreSoundsId, player, this.world, new BlockPos(this.posX, this.posY, this.posZ));
                    }

                    return;
                default:
                    if (!hasChanged) {
                        return;
                    }
                    break;
            }

            te.markUpdateThis();
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

    /** 1.12.2 {@code receiveMemoStrUpdate(String)} (PACKET_SUBTYPE_STRINGCHNGED_MEMOSTR). */
    public void receiveMemoStrUpdate(String stringFromPacket) {
        if (stringFromPacket != null && stringFromPacket.length() > 5) {
            this.te.memoStr = stringFromPacket.substring(0, 5);
        } else {
            this.te.memoStr = stringFromPacket;
        }

        this.te.markUpdateThis();
    }
}
