package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.item.ItemJBController;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Port of the 1.12.2 {@code CommandSlaveMode} ({@code jbslavemode} / {@code jbslave},
 * permission level 0, always allowed).
 */
public final class CommandSlaveMode {

   public static final String NAME = "jbslavemode";
   public static final List<String> ALIASES = List.of("jbslave");
   public static final String USAGE = "commands.jbslave.usage";

   private static final SimpleCommandExceptionType USAGE_EXCEPTION =
      new SimpleCommandExceptionType(Component.translatable(USAGE));

   private CommandSlaveMode() {
   }

   public static LiteralArgumentBuilder<CommandSourceStack> build() {
      return Commands.literal(NAME)
         .executes(ctx -> run(ctx, new String[0]))
         .then(Commands.argument("args", StringArgumentType.greedyString())
            .executes(ctx -> run(ctx, JBCommands.params(ctx.getArgument("args", String.class)))));
   }

   private static int run(CommandContext<CommandSourceStack> ctx, String[] args) throws CommandSyntaxException {
      if (args.length == 1 && args[0].length() > 0) {
         int value = -1;
         boolean setOn = false;
         if ("off".equals(args[0].toLowerCase())) {
            setOn = false;
         } else {
            setOn = true;

            try {
               value = Math.abs(Integer.parseInt(args[0]));
            } catch (Exception var7) {
            }
         }

         if (!setOn || value >= 0 && value <= 10) {
            changeTargetEntityMode(ctx.getSource(), setOn, (byte)value);
            return 1;
         }
      }

      throw USAGE_EXCEPTION.create();
   }

   private static void changeTargetEntityMode(CommandSourceStack commandSender, boolean setOn, byte value) {
      ServerPlayer entityPlayer = commandSender.getPlayer();
      if (entityPlayer != null) {
         EntityJointed targetEntity = null;
         InteractionHand[] var6 = InteractionHand.values();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            InteractionHand hand = var6[var8];
            ItemStack currentItemStack = entityPlayer.getItemInHand(hand);
            if (ItemJBController.isItemStackJBController(currentItemStack)) {
               targetEntity = ItemJBController.getRelatedEntityJointedServer(currentItemStack, entityPlayer.level(), entityPlayer);
               if (targetEntity != null) {
                  break;
               }
            }
         }

         if (targetEntity == null) {
            Entity ridingEntity = entityPlayer.getVehicle();
            if (ridingEntity != null && ridingEntity instanceof EntitySeatPart) {
               EntitySeatPart seatPart = (EntitySeatPart)ridingEntity;
               targetEntity = seatPart.entityJointed;
            }
         }

         if (targetEntity != null) {
            targetEntity.setSlaveMode(setOn, value);
            if (!setOn) {
               JBCommands.message(commandSender, JBCommonUtil.makeNormalChat("commands.jbslave.success", "off"));
            } else {
               JBCommands.message(commandSender, JBCommonUtil.makeNormalChat("commands.jbslave.success", value));
            }

            return;
         }
      }

      JBCommands.message(commandSender, JBCommonUtil.makeErrorChat("commands.jbslave.fail", value));
   }
}
