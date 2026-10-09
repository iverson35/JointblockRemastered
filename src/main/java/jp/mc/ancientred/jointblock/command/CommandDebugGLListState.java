package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Port of the 1.12.2 {@code CommandDebugGLListState} ({@code jbdebugglist} /
 * {@code jbdebuggl}): flips {@code JBConfig.debugGLListState}. 1.12.2 handled it inline in
 * {@code CommonForgeEventHandler.handleOnClientCommand} (no arguments, no chat output) —
 * the toggle itself is kept; the GL display-list counter it feeds is part of the deferred
 * Phase 4 renderer (the overlay reads the flag, nothing else is needed here).
 */
public final class CommandDebugGLListState extends AbstractClientCommand {

   private static final List<String> ALIASES = List.of("jbdebuggl");
   private static final String USAGE = "commands.jbsf.usage";

   @Override
   public String getName() {
      return "jbdebugglist";
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
      return Commands.literal(getName()).executes(ctx -> {
         JBConfig.debugGLListState = !JBConfig.debugGLListState;
         return 1;
      });
   }
}
