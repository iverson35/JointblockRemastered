package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * Port of the 1.12.2 command registration (the {@code registerServerCommand} calls in
 * {@code JBModContainer.preInit} plus the client {@code ClientCommandHandler.instance.register}
 * calls in {@code CommonProxyClient.registerCommands}).
 *
 * <p>1.12.2 registered the client commands through Forge's {@code CommandEvent} hook
 * ({@code CommonForgeEventHandler.handleOnClientCommand}); 1.20.1 has no such event, so the
 * client commands are Brigadier commands registered on {@link RegisterClientCommandsEvent}
 * and their bodies live in the command classes themselves. Names, aliases and argument
 * handling are the originals.
 */
public final class JBCommands {

   private JBCommands() {
   }

   public static void register() {
      MinecraftForge.EVENT_BUS.addListener(JBCommands::onRegisterCommands);
      MinecraftForge.EVENT_BUS.addListener(JBCommands::onRegisterClientCommands);
   }

   /** 1.12.2 {@code JBModContainer.preInit}: registerServerCommand(...). */
   private static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
      register(dispatcher, CommandDisassemble.NAME, CommandDisassemble.build(), CommandDisassemble.ALIASES);
      register(dispatcher, CommandChangeMode.NAME, CommandChangeMode.build(), CommandChangeMode.ALIASES);
      register(dispatcher, CommandSlaveMode.NAME, CommandSlaveMode.build(), CommandSlaveMode.ALIASES);
      register(dispatcher, CommandChangeSetting.NAME, CommandChangeSetting.build(), CommandChangeSetting.ALIASES);
      register(dispatcher, CommandJBItemEdit.NAME, CommandJBItemEdit.build(), CommandJBItemEdit.ALIASES);
   }

   /** 1.12.2 {@code CommonProxyClient.registerCommands}: ClientCommandHandler.instance.register(...). */
   private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
      registerClient(dispatcher, new CommandImportCarving());
      registerClient(dispatcher, new CommandExportCarving());
      registerClient(dispatcher, new CommandBuildObj());
      registerClient(dispatcher, new CommandShowJBFrame());
      registerClient(dispatcher, new CommandShowJBBounds());
      registerClient(dispatcher, new CommandShowJBConnection());
      registerClient(dispatcher, new CommandSeatCamera());
      registerClient(dispatcher, new CommandToggleIndicator());
      registerClient(dispatcher, new CommandRingDebug());
      registerClient(dispatcher, new CommandClientSetting());
      registerClient(dispatcher, new CommandDebugGLListState());
   }

   private static void registerClient(CommandDispatcher<CommandSourceStack> dispatcher, AbstractClientCommand command) {
      register(dispatcher, command.getName(), command.build(), command.getAliases());
   }

   private static void register(CommandDispatcher<CommandSourceStack> dispatcher, String name, LiteralArgumentBuilder<CommandSourceStack> builder, List<String> aliases) {
      LiteralCommandNode<CommandSourceStack> node = dispatcher.register(builder);
      for (String alias : aliases) {
         dispatcher.register(Commands.literal(alias).redirect(node));
      }
   }

   /**
    * 1.12.2 handed the raw {@code String[] args} to every command; the ported commands keep
    * that shape by taking one greedy string and splitting it the way the 1.12.2 command
    * tokenizer did.
    */
   static String[] params(String greedy) {
      String trimmed = greedy.trim();
      return trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
   }

   /** 1.12.2 {@code ICommandSender.func_145747_a(ITextComponent)}. */
   static void message(CommandSourceStack source, Component component) {
      source.sendSuccess(() -> component, false);
   }

   /** 1.12.2 {@code CommandBase.func_152373_a(sender, command, key, args)}. */
   static void notifySource(CommandSourceStack source, Component component) {
      source.sendSuccess(() -> component, true);
   }
}
