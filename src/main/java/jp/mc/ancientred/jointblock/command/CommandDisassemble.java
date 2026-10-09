package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Port of the 1.12.2 {@code CommandDisassemble} ({@code jbdisassemble} / {@code jbdis},
 * permission level 2).
 */
public final class CommandDisassemble {

   public static final String NAME = "jbdisassemble";
   public static final List<String> ALIASES = List.of("jbdis");
   public static final String USAGE = "commands.jbdis.usage";

   private static final SimpleCommandExceptionType USAGE_EXCEPTION =
      new SimpleCommandExceptionType(Component.translatable(USAGE));

   private CommandDisassemble() {
   }

   public static LiteralArgumentBuilder<CommandSourceStack> build() {
      return Commands.literal(NAME)
         .requires(source -> source.hasPermission(2))
         .executes(ctx -> run(ctx, new String[0]))
         .then(Commands.argument("args", StringArgumentType.greedyString())
            .executes(ctx -> run(ctx, JBCommands.params(ctx.getArgument("args", String.class)))));
   }

   private static int run(CommandContext<CommandSourceStack> ctx, String[] args) throws CommandSyntaxException {
      CommandSourceStack commandSender = ctx.getSource();
      int value = 0;
      boolean force = false;
      if (args.length == 0) {
         value = 64;
      } else if (args.length >= 1 && args[0].length() > 0) {
         try {
            value = Integer.parseInt(args[0]);
         } catch (Exception var7) {
         }
      }

      if (args.length >= 2 && "force".equalsIgnoreCase(args[1])) {
         force = true;
      }

      if (value > 0 && value <= 64) {
         EntityJointed.disassembleEntityJointedInRange(commandSender, value, force);
         JBCommands.message(commandSender, JBCommonUtil.makeNormalChat("commands.jbdis.success", value));
         return 1;
      } else {
         throw USAGE_EXCEPTION.create();
      }
   }
}
