package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.assemble.tuning.StaticClientTuningStates;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemJBAssemblyTuner extends Item {
   public ItemJBAssemblyTuner() {
      super((new Item.Properties()).stacksTo(1));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (world.isClientSide) {
         EntityJointed entityJointed;
         JointParts joint;
         if (!StaticClientTuningStates.targetSelected.isSetectionEnabled) {
            entityJointed = StaticClientTuningStates.target.rayTracedHitEntity;
            joint = StaticClientTuningStates.target.rayTracedHitJoint;
            if (joint != null && entityJointed != null && !joint.isDestructed && !entityJointed.isRemoved()) {
               StaticClientTuningStates.targetSelected.copyFrom(StaticClientTuningStates.target);
               StaticClientTuningStates.targetSelected.isSetectionEnabled = true;
            }
         } else if (StaticClientTuningStates.target.isAdditionalBarHit) {
            entityJointed = StaticClientTuningStates.target.rayTracedHitEntity;
            joint = StaticClientTuningStates.target.rayTracedHitJoint;
            if (joint != null && entityJointed != null && !joint.isDestructed && !entityJointed.isRemoved()) {
               JBPacketSender.sendAssemblyTuningActionToServer(entityJointed.getId(), joint.id, StaticClientTuningStates.target.additionalBarNum, StaticClientTuningStates.target.additionalBarOffset, StaticClientTuningStates.target.rayTracedHitSide);
            }
         }
      }

      return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
   }
}
