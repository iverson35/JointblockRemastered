package jp.mc.ancientred.jointblock.modelpack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helpers of the model pack format.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.JBRModelUtils}, ported with the same
 * method names (including the original {@code Entiry}/{@code Extention} misspellings) and the same
 * fallback values, because the pack JSON contract depends on them. Only the parts reachable from the
 * ported pack sections are kept.
 *
 * <p>1.12.2 → 1.20.1 substitutions:
 * <ul>
 *   <li>logging went through launchwrapper's {@code LogWrapper} at INFO; here a log4j logger keeps
 *       the exact {@code [JBModel]} prefixes.</li>
 *   <li>{@code GameData.getWrapper(Item.class)} item/block lookups became
 *       {@link ForgeRegistries#ITEMS}/{@link ForgeRegistries#BLOCKS}. The original parsed
 *       {@code new ResourceLocation(name.toLowerCase())} which merely logged on a malformed name;
 *       1.20.1 throws, so the name goes through {@link ResourceLocation#tryParse} and a malformed
 *       name falls back to the default item exactly like a missing one.</li>
 *   <li>the not-found default was {@code Items.field_151166_bC}; in 1.20.1 the "nothing here" item
 *       is {@link Items#AIR}.</li>
 * </ul>
 */
public class JBRModelUtils {
    private static final Logger LOGGER = LogManager.getLogger("JBModel");
    private static final Pattern PACKAGE_NAME = Pattern.compile("^[a-zA-Z_]+$");

    public static boolean isInvalidFileLoaderEntiry(FileLoader.FileEntry entryModelFile) {
        return entryModelFile == null || entryModelFile.isDirectory() || entryModelFile.getSize() == 0L;
    }

    public static final String getFileExtention(String fileName) {
        int i = fileName.lastIndexOf(46);
        return i > 0 ? fileName.substring(i + 1).toLowerCase() : "";
    }

    public static void LogFailed(String str) {
        LOGGER.info("[JBModel] " + str);
    }

    public static void LogInfo(String str) {
        LOGGER.info("[JBModel] ----------" + str);
    }

    public static void LogFailedReadingModelFile(String str) {
        LOGGER.info("[JBModel] ----------Failed on reading model! [" + str + "]");
    }

    public static void LogFailedReadingTextureFile(String str) {
        LOGGER.info("[JBModel] ----------Failed on reading texture! [" + str + "]");
    }

    /** Resolves a pack's {@code item} name to an item; blocks resolve through their item form. */
    public static Item tryGetRegisteredItem(String name) {
        ResourceLocation loc = ResourceLocation.tryParse(name.toLowerCase());
        if (loc != null) {
            Item item = ForgeRegistries.ITEMS.getValue(loc);
            if (item != null) {
                return item;
            }

            Block block = ForgeRegistries.BLOCKS.getValue(loc);
            if (block != null) {
                return block.asItem();
            }
        }

        return Items.AIR;
    }

    public static int getIntegerFromJson(JsonObject jsonObj, String key) {
        try {
            return Integer.parseInt(getStringFromJson(jsonObj, key));
        } catch (Exception e) {
            return -1;
        }
    }

    public static float getFloatFromJson(JsonObject jsonObj, String key) {
        try {
            return Float.parseFloat(getStringFromJson(jsonObj, key));
        } catch (Exception e) {
            return -1.0F;
        }
    }

    public static float getFloatFromJsonNeg(JsonObject jsonObj, String key) {
        try {
            return Float.parseFloat(getStringFromJson(jsonObj, key));
        } catch (Exception e) {
            return Float.NaN;
        }
    }

    public static boolean getBooleanFromJson(JsonObject jsonObj, String key) {
        return "true".equals(getStringFromJson(jsonObj, key));
    }

    public static String getStringFromJson(JsonObject jsonObj, String key) {
        return getStringFromJson(jsonObj.get(key));
    }

    public static String getStringFromJson(JsonElement elem) {
        return elem != null && elem.isJsonPrimitive() ? elem.getAsString() : "";
    }

    public static boolean isValidPackageId(String s) {
        if (isStringEmpty(s)) {
            return false;
        }

        return s.length() <= 32 && PACKAGE_NAME.matcher(s).matches();
    }

    public static boolean isStringEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean containsNumber(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) {
            return false;
        }

        Object obj = map.get(key);
        return obj instanceof Double || obj instanceof Integer || obj instanceof Float;
    }

    public static double getAsNumber(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj instanceof Double) {
            return (Double) obj;
        }

        if (obj instanceof Float) {
            return (Float) obj;
        }

        if (obj instanceof Integer) {
            return (Integer) obj;
        }

        return 0.0;
    }

    public static boolean containsString(Map<String, Object> map, String key) {
        return map.get(key) instanceof String;
    }

    public static String getAsString(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        return obj instanceof String ? (String) obj : "";
    }

    /** Pairs {@code model/x.obj} with {@code model/x.mtl}; {@code null} when the pack has no mtl. */
    public static FileLoader.FileEntry getMtlFileFromModelEntry(FileLoader fileLoader, FileLoader.FileEntry fileEntryModel) {
        String objFileName = fileEntryModel.getName();
        String mtlPath = objFileName.substring(0, Math.max(0, objFileName.length() - 4)) + ".mtl";
        return fileLoader.getEntry(mtlPath);
    }

    /**
     * {@code BuiltInRegistries} keeps the registries reachable before {@code ForgeRegistries} are
     * wired; both point at the same objects once the game is up.
     */
    static boolean isRegistered(Item item) {
        return item != null && BuiltInRegistries.ITEM.getKey(item) != null;
    }
}
