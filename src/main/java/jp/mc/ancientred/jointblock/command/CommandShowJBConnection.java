package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Port of the 1.12.2 {@code CommandShowJBConnection} ({@code jbshowconnection} /
 * {@code jbscon}): sets the joint-connection debug overlay level. 1.12.2 routed the body
 * through the Forge {@code CommandEvent} hook
 * ({@code CommonProxyClient.processCommandJBShowConnection}).
 */
public final class CommandShowJBConnection extends AbstractClientCommand {

   private static final List<String> ALIASES = List.of("jbscon");
   private static final String USAGE = "commands.jbscon.usage";

   @Override
   public String getName() {
      return "jbshowconnection";
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
            int value = Integer.parseInt(params[0]);
            JBConfig.showJBConnection = value;
            JBCommands.message(sender, JBCommonUtil.makeNormalChat("commands.jbscon.succeed", String.valueOf(value)));
            return 1;
         } catch (Exception var6) {
         }
      }

      JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbscon.usage"));
      return 0;
   }
}
