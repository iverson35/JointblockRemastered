package jp.mc.ancientred.jointblock.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Port of the 1.12.2 {@code CommandChangeSetting} ({@code jbchangesetting} / {@code jbcset},
 * permission level 3). Setting names, ranges and defaults are the originals.
 */
public final class CommandChangeSetting {

   public static final String NAME = "jbchangesetting";
   public static final List<String> ALIASES = List.of("jbcset");
   public static final String USAGE = "commands.jbcset.usage";

   private static final SimpleCommandExceptionType USAGE_EXCEPTION =
      new SimpleCommandExceptionType(Component.translatable(USAGE));

   private static final String[] COMMAND_LIST = new String[]{
      "disableDisassemble <true|false>",
      "disableMasterConnectedDisassemble <true|false>",
      "allowAnyBlockForModel <true|false>",
      "enableDataImport <true|false>",
      "enableDataExport <true|false>",
      "enableOverpowerNode <true|false>",
      "maxAssembleCount <number>",
      "maxAssembleMotionCount <number>",
      "maxAssembleXRange <number>",
      "maxAssembleYRange <number>",
      "maxAssembleZRange <number>",
      "maxCarvingDivide <number>",
      "maxImportablePartsCount <number>",
      "slaveModeEnergyHealMult <float>"
   };

   private CommandChangeSetting() {
   }

   public static LiteralArgumentBuilder<CommandSourceStack> build() {
      return Commands.literal(NAME)
         .requires(source -> source.hasPermission(3))
         .executes(ctx -> run(ctx, new String[0]))
         .then(Commands.argument("args", StringArgumentType.greedyString())
            .executes(ctx -> run(ctx, JBCommands.params(ctx.getArgument("args", String.class)))));
   }

   private static int run(CommandContext<CommandSourceStack> ctx, String[] args) throws CommandSyntaxException {
      CommandSourceStack commandSender = ctx.getSource();
      if (args.length == 1 && "help".equals(args[0])) {
         for(int i = 0; i < COMMAND_LIST.length; i += 2) {
            if (i + 1 < COMMAND_LIST.length) {
               JBCommands.message(commandSender, JBCommonUtil.makeNormalChat("[" + COMMAND_LIST[i] + "] [" + COMMAND_LIST[i + 1] + "]"));
            } else {
               JBCommands.message(commandSender, JBCommonUtil.makeNormalChat("[" + COMMAND_LIST[i] + "]"));
            }
         }

         return 1;
      } else if (args.length == 2 && args[0].length() > 0 && args[1].length() > 0) {
         boolean succeed = false;
         if (isBooleanValue(args[1])) {
            succeed = setBoolaenSettingValue(args[0], toBooleanValue(args[1]));
         } else if (isIntegerValue(args[1])) {
            succeed = setIntegerSettingValue(args[0], toIntegerValue(args[1]));
         } else if (isFloatValue(args[1])) {
            succeed = setFloatSettingValue(args[0], toFloatValue(args[1]));
         }

         if (succeed) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            builder.append(args[0]);
            builder.append(":->:");
            builder.append(args[1]);
            builder.append("]");
            JBCommands.notifySource(commandSender, Component.translatable("commands.jbcset.success", builder.toString()));
         } else {
            JBCommands.message(commandSender, JBCommonUtil.makeErrorChat("commands.jbcset.invalid"));
         }

         return 1;
      } else {
         throw USAGE_EXCEPTION.create();
      }
   }

   private static boolean setBoolaenSettingValue(String targetSettingsName, boolean valueToSet) {
      if ("disableDisassemble".equals(targetSettingsName)) {
         JBConfig.disableDisassemble = valueToSet;
         return true;
      } else if ("disableMasterConnectedDisassemble".equals(targetSettingsName)) {
         JBConfig.disableMasterConnectedDisassemble = valueToSet;
         return true;
      } else if ("allowAnyBlockForModel".equals(targetSettingsName)) {
         JBConfig.allowAnyBlockForModel = valueToSet;
         return true;
      } else if ("enableDataImport".equals(targetSettingsName)) {
         JBConfig.enableDataImport = valueToSet;
         return true;
      } else if ("enableDataExport".equals(targetSettingsName)) {
         JBConfig.enableDataExport = valueToSet;
         return true;
      } else if ("enableOverpowerNode".equals(targetSettingsName)) {
         JBConfig.enableOverpowerNode = valueToSet;
         return true;
      } else {
         return false;
      }
   }

   private static boolean setIntegerSettingValue(String targetSettingsName, int valueToSet) {
      if ("maxAssembleCount".equals(targetSettingsName) && isInValidRange(1, 32767, valueToSet)) {
         JBConfig.maxAssembleCount = valueToSet;
         return true;
      } else if ("maxAssembleMotionCount".equals(targetSettingsName) && isInValidRange(1, 255, valueToSet)) {
         JBConfig.maxAssembleMotionCount = valueToSet;
         return true;
      } else if ("maxAssembleXRange".equals(targetSettingsName) && isInValidRange(0, 64, valueToSet)) {
         JBConfig.maxAssembleXRange = valueToSet;
         return true;
      } else if ("maxAssembleYRange".equals(targetSettingsName) && isInValidRange(0, 64, valueToSet)) {
         JBConfig.maxAssembleYRange = valueToSet;
         return true;
      } else if ("maxAssembleZRange".equals(targetSettingsName) && isInValidRange(0, 64, valueToSet)) {
         JBConfig.maxAssembleZRange = valueToSet;
         return true;
      } else if ("maxCarvingDivide".equals(targetSettingsName) && isInValidRange(2, 16, valueToSet)) {
         JBConfig.maxCarvingDivide = valueToSet;
         return true;
      } else if ("maxImportablePartsCount".equals(targetSettingsName) && isInValidRange(0, 32767, valueToSet)) {
         JBConfig.maxImportablePartsCount = valueToSet;
         return true;
      } else {
         return setFloatSettingValue(targetSettingsName, (float)valueToSet);
      }
   }

   private static boolean setFloatSettingValue(String targetSettingsName, float valueToSet) {
      if ("slaveModeEnergyHealMult".equals(targetSettingsName) && isInValidRange(0.01F, 1000.0F, valueToSet)) {
         JBConfig.slaveModeEnergyHealMult = valueToSet;
         return true;
      } else {
         return false;
      }
   }

   public static boolean isInValidRange(int min, int max, int value) {
      if (value < min) {
         return false;
      } else {
         return value <= max;
      }
   }

   public static boolean isInValidRange(float min, float max, float value) {
      if (value < min) {
         return false;
      } else {
         return !(value > max);
      }
   }

   public static boolean isBooleanValue(String arg) {
      if (arg == null) {
         return false;
      } else {
         return "true".equals(arg.trim().toLowerCase()) || "false".equals(arg.trim().toLowerCase());
      }
   }

   public static boolean toBooleanValue(String arg) {
      return "true".equals(arg.trim().toLowerCase());
   }

   public static boolean isIntegerValue(String arg) {
      if (arg == null) {
         return false;
      } else {
         int value = -1;

         try {
            value = Math.abs(Integer.parseInt(arg));
         } catch (Exception var4) {
         }

         return value >= 0 && value <= 32767;
      }
   }

   public static int toIntegerValue(String arg) throws CommandSyntaxException {
      int value = -1;

      try {
         value = Math.abs(Integer.parseInt(arg));
      } catch (Exception var4) {
      }

      if (value >= 0 && value <= 32767) {
         return value;
      } else {
         throw USAGE_EXCEPTION.create();
      }
   }

   public static boolean isFloatValue(String arg) {
      if (arg == null) {
         return false;
      } else {
         float value = -1.0F;

         try {
            value = Float.parseFloat(arg);
         } catch (Exception var4) {
            return false;
         }

         return value >= 0.0F && !Float.isInfinite(value) && !Float.isNaN(value);
      }
   }

   public static float toFloatValue(String arg) throws CommandSyntaxException {
      float value = 0.0F;

      try {
         value = Float.parseFloat(arg);
      } catch (Exception var4) {
         throw USAGE_EXCEPTION.create();
      }

      if (value >= 0.0F && !Float.isInfinite(value) && !Float.isNaN(value)) {
         return value;
      } else {
         throw USAGE_EXCEPTION.create();
      }
   }
}
