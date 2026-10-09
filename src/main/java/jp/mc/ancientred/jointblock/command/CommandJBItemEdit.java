package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.item.ICommandEditableItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Port of the 1.12.2 {@code CommandJBItemEdit} ({@code jbitemedit} / {@code jbitem},
 * permission level 2): hands the arguments to the held
 * {@link ICommandEditableItem}.
 */
public final class CommandJBItemEdit {

   public static final String NAME = "jbitemedit";
   public static final List<String> ALIASES = List.of("jbitem");
   public static final String USAGE = "commands.jbitem.usage";

   private static final SimpleCommandExceptionType USAGE_EXCEPTION =
      new SimpleCommandExceptionType(Component.translatable(USAGE));

   private CommandJBItemEdit() {
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
      ServerPlayer player = commandSender.getPlayer();
      if (player != null) {
         ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
         if (!CompatHelper.isStackEmptyOrNull(stack) && stack.getItem() instanceof ICommandEditableItem && ((ICommandEditableItem)stack.getItem()).editByCommand(player, stack, args)) {
            JBCommands.message(commandSender, JBCommonUtil.makeNormalChat("commands.jbitem.success", 0));
            return 1;
         }
      }

      throw USAGE_EXCEPTION.create();
   }
}
