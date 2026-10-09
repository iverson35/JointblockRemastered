package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;

/**
 * Port of the 1.12.2 {@code AbstractClientCommand} ({@code extends CommandBase}).
 *
 * <p>The 1.12.2 class only overrode {@code func_184882_a} (always allowed),
 * {@code func_82362_a} (permission level 0) and left {@code func_184881_a} empty — the
 * client commands were execute-less shells whose real work happened in the Forge
 * {@code CommandEvent} hook ({@code CommonForgeEventHandler.handleOnClientCommand}).
 *
 * <p>1.20.1 has no command event, so each ported client command supplies its own Brigadier
 * body through {@link #build()}. The "always allowed, permission 0" policy is preserved by
 * simply not adding a {@code requires(...)} gate.
 *
 * <p>The client source of a client command is built from a {@code LocalPlayer}
 * ({@code ClientCommandSourceStack}), so {@code CommandSourceStack.getPlayer()} is null —
 * use {@link #clientPlayer()} exactly as the 1.12.2 bodies used
 * {@code CommandEvent.getSender()}.
 */
public abstract class AbstractClientCommand {

   /** 1.12.2 {@code func_71517_b()}. */
   public abstract String getName();

   /** 1.12.2 {@code func_71514_a()}. */
   public abstract List<String> getAliases();

   /** 1.12.2 {@code func_71518_a(ICommandSender)}. */
   public abstract String getUsage();

   /** 1.12.2 {@code func_184881_a(MinecraftServer, ICommandSender, String[])}. */
   public abstract LiteralArgumentBuilder<CommandSourceStack> build();

   protected static LocalPlayer clientPlayer() {
      return Minecraft.getInstance().player;
   }
}
