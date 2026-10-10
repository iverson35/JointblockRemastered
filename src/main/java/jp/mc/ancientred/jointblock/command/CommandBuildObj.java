package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.tuning.StaticClientTuningStates;
import jp.mc.ancientred.jointblock.carving.prebuild.Prebuilder;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Port of the 1.12.2 {@code CommandBuildObj} ({@code jbbuildobj} / {@code jbbo} /
 * {@code jbobj}). 1.12.2 routed the work through the Forge {@code CommandEvent} hook
 * ({@code CommonProxyClient.processCommandBuildObj}); the body is ported here and starts a
 * {@link Prebuilder} on the currently ray-traced joint of the tuning target.
 */
public final class CommandBuildObj extends AbstractClientCommand {

   private static final List<String> ALIASES = List.of("jbbo", "jbobj");
   private static final String USAGE = "commands.jbbo.usage";

   @Override
   public String getName() {
      return "jbbuildobj";
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
      LocalPlayer player = clientPlayer();
      if (player == null) {
         return 0;
      }

      if (!StaticClientTuningStates.checkClientPlayerHoldingTuneItem() || !StaticClientTuningStates.targetSelected.isSetectionEnabled || StaticClientTuningStates.targetSelected.rayTracedHitEntity == null) {
         JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbbo.noselect"));
         return 0;
      }

      if (params.length >= 1 && params[0].length() > 0) {
         String outGp = params[0];
         boolean triangulate = false;
         boolean coreBase = false;

         for(int i = 1; i < params.length; ++i) {
            if ("t".equalsIgnoreCase(params[i])) {
               triangulate = true;
            } else {
               if (!"c".equalsIgnoreCase(params[i])) {
                  JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbbo.usage"));
                  return 0;
               }

               coreBase = true;
            }
         }

         StaticClientTuningStates.targetSelected.rayTracedHitEntity.prebuilder = new Prebuilder(player, outGp.substring(0, Math.min(20, outGp.length())), triangulate, coreBase);
         return 1;
      }

      JBCommands.message(sender, JBCommonUtil.makeErrorChat("commands.jbbo.usage"));
      return 0;
   }
}
