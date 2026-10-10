package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Port of the 1.12.2 {@code CommandToggleIndicator} ({@code jbtoggleindicator} /
 * {@code jbti}): toggles the indicator window, its "detail" debug mode and its position.
 * 1.12.2 routed the body through the Forge {@code CommandEvent} hook
 * ({@code CommonProxyClient.processCommandToggleInidicator}).
 */
public final class CommandToggleIndicator extends AbstractClientCommand {

   private static final List<String> ALIASES = List.of("jbti");
   private static final String USAGE = "commands.jbti.usage";

   @Override
   public String getName() {
      return "jbtoggleindicator";
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
               JBConfig.indicatorWindowEnabled = false;
               succeedStr = "off";
            } else if ("on".equals(params[0].toLowerCase())) {
               JBConfig.indicatorWindowEnabled = true;
               JBConfig.enableDebugIndicates = false;
               succeedStr = "on";
            } else if ("don".equals(params[0].toLowerCase())) {
               JBConfig.indicatorWindowEnabled = true;
               JBConfig.enableDebugIndicates = true;
               succeedStr = "detail-on";
            } else {
               int value = Integer.parseInt(params[0]);
               if (value < 0 || value > 2) {
                  JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbti.usage"));
                  return 0;
               }

               JBConfig.indicatorWindowEnabled = true;
               JBConfig.indicatorWindowPosition = value;
               succeedStr = String.valueOf(value);
            }

            JBCommands.message(sender, JBCommonUtil.makeNormalChat("commands.jbti.succeed", succeedStr));
            return 1;
         } catch (Exception var6) {
         }
      }

      JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbti.usage"));
      return 0;
   }
}
