package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;

/**
 * Port of the 1.12.2 {@code CommandSeatCamera} ({@code jbcamera} / {@code jbcam}):
 * switches the camera seat number of the seat the player currently rides. 1.12.2 routed
 * the body through the Forge {@code CommandEvent} hook
 * ({@code CommonProxyClient.processCommandJBSeatCamera}).
 */
public final class CommandSeatCamera extends AbstractClientCommand {

   private static final List<String> ALIASES = List.of("jbcam");
   private static final String USAGE = "commands.jbcam.usage";

   @Override
   public String getName() {
      return "jbcamera";
   }

   @Override
   public List<String> getAliases() {
      return ALIASES;
   }

   @Override
   public String getUsage() {
      return USAGE;
   }

   @Override
   public LiteralArgumentBuilder<CommandSourceStack> build() {
      return Commands.literal(getName())
         .executes(ctx -> run(ctx.getSource(), new String[0]))
         .then(Commands.argument("args", StringArgumentType.greedyString())
            .executes(ctx -> run(ctx.getSource(), JBCommands.params(ctx.getArgument("args", String.class)))));
   }

   private static int run(CommandSourceStack sender, String[] params) {
      Minecraft mc = Minecraft.getInstance();
      EntitySeatPart seat = null;
      if (mc.player != null) {
         Entity ridingEntity = mc.player.getVehicle();
         if (ridingEntity != null && ridingEntity instanceof EntitySeatPart) {
            seat = (EntitySeatPart)ridingEntity;
         }
      }

      if (seat == null) {
         JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbcam.usage"));
         return 0;
      }

      if (params.length == 0) {
         if (seat.autoDetectCameraNum()) {
            JBCommands.message(sender, JBCommonUtil.makeNormalChat("commands.jbcam.succeed", String.valueOf(seat.cameraModeSeatNum + 1)));
            return 1;
         }

         JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbcam.notfound"));
         return 0;
      }

      if (params.length == 1 && params[0].length() > 0) {
         try {
            int value;
            if ("off".equalsIgnoreCase(params[0])) {
               value = -1;
            } else {
               value = Integer.parseInt(params[0]);
            }

            if (value == -1 || value >= 1 && value <= 4) {
               seat.setCamModeNum(value == -1 ? value : value - 1);
               JBCommands.message(sender, JBCommonUtil.makeNormalChat("commands.jbcam.succeed", value == -1 ? "off" : String.valueOf(value)));
               return 1;
            }
         } catch (Exception var8) {
         }
      }

      JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbcam.usage"));
      return 0;
   }
}
