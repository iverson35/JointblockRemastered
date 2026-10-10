package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jp.mc.ancientred.jointblock.export.JBImportExportDataUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Port of the 1.12.2 {@code CommandImportCarving} ({@code jbimportdata} / {@code jbid}).
 * 1.12.2 left the command body empty and routed the work through the Forge
 * {@code CommandEvent} hook into {@link JBImportExportDataUtil#importJBData}; the 1.20.1
 * command calls it directly.
 */
public final class CommandImportCarving extends AbstractClientCommand {

   private static final List<String> ALIASES = List.of("jbid");
   private static final String USAGE = "commands.jbid.usage";

   @Override
   public String getName() {
      return "jbimportdata";
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
         .executes(ctx -> run(new String[0]))
         .then(Commands.argument("args", StringArgumentType.greedyString())
            .executes(ctx -> run(JBCommands.params(ctx.getArgument("args", String.class)))));
   }

   private static int run(String[] args) {
      LocalPlayer player = clientPlayer();
      if (player == null) {
         return 0;
      }
      JBImportExportDataUtil.importJBData(player, args);
      return 1;
   }
}
