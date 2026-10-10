package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * <p>Port of the 1.12.2 {@code CommandRingDebug} ({@code jbringdebug} / {@code jbrd} /
 * {@code jbdr}): toggles the ring-program debug overlay (the HUD ring window and the navigator path
 * boxes) and its target ring number. 1.12.2 routed the body through the Forge {@code CommandEvent}
 * hook ({@code CommonProxyClient.processCommandRingDebug}); the toggled state and the renderer are
 * both wired in 1.20.1 ({@code JBClientSetup#onRegisterGuiOverlays} for the HUD window,
 * {@code ClientEventhandler#onRenderLevelStage} for the world-space path boxes).
 */
public final class CommandRingDebug extends AbstractClientCommand {

   private static final List<String> ALIASES = List.of("jbrd", "jbdr");
   private static final String USAGE = "commands.jbrd.usage";

   @Override
   public String getName() {
      return "jbringdebug";
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
      if (params.length == 1 && params[0].length() > 0) {
         try {
            String succeedStr;
            if ("off".equals(params[0].toLowerCase())) {
               JBConfig.enableDebugRing = false;
               succeedStr = "off";
            } else if ("on".equals(params[0].toLowerCase())) {
               JBConfig.enableDebugRing = true;
               succeedStr = "on";
            } else {
               int value = Integer.parseInt(params[0]);
               if (value < 0 || value > 4) {
                  JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbrd.usage"));
                  return 0;
               }

               JBConfig.enableDebugRing = true;
               JBConfig.debugRingTargetNum = value;
               succeedStr = String.valueOf(value);
            }

            JBCommands.message(sender, JBCommonUtil.makeNormalChat("commands.jbrd.succeed", succeedStr));
            return 1;
         } catch (Exception var6) {
         }
      }

      JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbrd.usage"));
      return 0;
   }
}
