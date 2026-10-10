package jp.mc.ancientred.jointblock.gui;

import net.minecraft.world.entity.player.Player;

/**
 * Receiver of a {@code PACKET_TYPE_GUIACT} button action, implemented by the ported
 * containers.  1.12.2: {@code void receiveButtonAction(EntityPlayer, int, int, int)}.
 */
public interface IContainerActionReceiver {

    void receiveButtonAction(Player player, int id, int startIndex, int amount);
}
