package jp.mc.ancientred.jointblock.common;

import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.world.entity.player.Player;

public class CommonProxyServer extends CommonProxy {
   @Override
   public void registerRenderHelper() {
   }

   @Override
   public JointParts creteSideBasedJointParts(int partsX, int partsY, int partsZ) {
      return new JointParts(partsX, partsY, partsZ);
   }

   @Override
   public void onPlayerAttachedToSeat(Player riddenByEntity, EntityJointed entityJointed) {
   }
}
