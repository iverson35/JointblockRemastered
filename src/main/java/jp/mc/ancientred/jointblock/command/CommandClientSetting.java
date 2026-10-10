package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Port of the 1.12.2 {@code CommandClientSetting} ({@code jbclientsetting} /
 * {@code jbclient}): toggles the client side joint sound / particle effects. 1.12.2 routed
 * the body through the Forge {@code CommandEvent} hook
 * ({@code CommonProxyClient.processCommandClientSetting}).
 */
public final class CommandClientSetting extends AbstractClientCommand {

   private static final List<String> ALIASES = List.of("jbclient");
   private static final String USAGE = "commands.jbclient.usage";

   @Override
   public String getName() {
      return "jbclientsetting";
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
      if (params.length >= 2) {
         try {
            String arg00 = params[0].toLowerCase();
            int type;
            String succeedStrA;
            if (!"sound".equals(arg00) && !"sounds".equals(arg00)) {
               if (!"particle".equals(arg00) && !"particles".equals(arg00)) {
                  JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbclient.usage"));
                  return 0;
               }

               type = 1;
               succeedStrA = "particle";
            } else {
               type = 0;
               succeedStrA = "sound";
            }

            boolean setOn;
            String succeedStrB;
            if ("off".equals(params[1].toLowerCase())) {
               setOn = false;
               succeedStrB = "off";
            } else {
               if (!"on".equals(params[1].toLowerCase())) {
                  JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbclient.usage"));
                  return 0;
               }

               setOn = true;
               succeedStrB = "on";
            }

            switch (type) {
               case 0:
                  JBConfig.enableJointBlockSounds = setOn;
                  break;
               case 1:
                  JBConfig.enableJointBlockParticle = setOn;
            }

            JBCommands.message(sender, JBCommonUtil.makeNormalChat("commands.jbclient.success", succeedStrA, succeedStrB));
            return 1;
         } catch (Exception var11) {
         }
      }

      JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbclient.usage"));
      return 0;
   }
}
