package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

/**
 * 1.12.2 {@code AbstractContainerJointCore}: shared state/validation for every container
 * whose block entity is a {@link TileEntityJointCore}.
 */
public abstract class AbstractContainerJointCore extends AbstractContainerMenu {

    public TileEntityJointCore teCore;
    public Player entityplayer;
    protected Level world;
    protected int posX;
    protected int posY;
    protected int posZ;

    public AbstractContainerJointCore(MenuType<?> type, int containerId, Player playerIn, TileEntityJointCore teCoreIn) {
        super(type, containerId);
        this.entityplayer = playerIn;
        this.teCore = teCoreIn;
        this.world = teCoreIn.getLevel();
        this.posX = teCoreIn.getBlockPos().getX();
        this.posY = teCoreIn.getBlockPos().getY();
        this.posZ = teCoreIn.getBlockPos().getZ();
    }

    @Override
    public boolean stillValid(Player par1EntityPlayer) {
        if (!(WorldHelper.getBlock(this.world, this.posX, this.posY, this.posZ) instanceof BlockJointCore)) {
            return false;
        } else {
            // 1.12.2: par1EntityPlayer.getDistance(cx, cy, cz) < 32.0
            return par1EntityPlayer.distanceToSqr((double)this.posX + 0.5, (double)this.posY + 0.5, (double)this.posZ + 0.5) < 32.0 * 32.0;
        }
    }
}
