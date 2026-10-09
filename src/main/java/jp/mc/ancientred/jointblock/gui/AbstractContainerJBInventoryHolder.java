package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.entity.JointInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * 1.12.2 {@code AbstractContainerJBInventoryHolder}: a container whose slots live in a
 * {@link JointInventory} (the joint core's inner chest, or the one carried by an
 * {@code EntityJointed}).  The {@code MenuType} is passed through because the chest/fuel
 * screens are reachable both from a block and from an entity.
 */
public abstract class AbstractContainerJBInventoryHolder extends AbstractContainerMenu {

    protected JointInventory innerChestInventory;
    protected Player entityplayer;

    public AbstractContainerJBInventoryHolder(MenuType<?> type, int containerId, Player playerIn, JointInventory jointChestInventoryIn) {
        super(type, containerId);
        this.innerChestInventory = jointChestInventoryIn;
        this.entityplayer = playerIn;
    }

    @Override
    public boolean stillValid(Player entityPlayer) {
        return this.innerChestInventory.stillValid(entityPlayer);
    }
}
