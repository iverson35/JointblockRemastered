package jp.mc.ancientred.jointblock.modelpack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import jp.mc.ancientred.jbrobot.item.external.ExJBAddonInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBAddonItemInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBAddonLimitKeyInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBBluePrintInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBSound;
import jp.mc.ancientred.jbrobot.item.external.ExJBSoundInfo;
import jp.mc.ancientred.jbrobot.item.external.ExJBSoundItemInfo;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import net.minecraft.locale.Language;

/**
 * Reads extension packs out of {@code <config dir>/JointBlock/*.zip}.
 *
 * <p>1.12.2 {@code jp.mc.ancientred.jbrobot.item.external.ExtraPackageLoader}, called from the
 * addon's preInit. The port runs the same scan from the mod constructor, because 1.20.1 items have
 * to be registered before the registry events fire and one item per pack is created here.
 *
 * <p>A pack is a zip with
 * <ul>
 *   <li>{@code package.json} - {@code format:"package"}, {@code packageId}, {@code packageVersion},
 *       optional {@code packageName}/{@code packageIconFile};</li>
 *   <li>{@code model/*.json} - {@code format:"model"}, {@code modelId} (0..127), {@code modelName},
 *       optional {@code modelDesc}/{@code addonName}/{@code itemIconFile}/{@code renderType}/
 *       {@code renderExpand}/{@code action}, and a {@code layers} array of
 *       {@code {pass, modelFile, textureFile}};</li>
 *   <li>{@code blueprint/*.json} - {@code format:"blueprint"}, {@code blueprintId} (0..127),
 *       {@code blueprintName}, {@code blueprintData} (base64 blueprint), optional
 *       {@code blueprintDesc}/{@code capsule}/{@code useCoreName}/{@code merchant};</li>
 *   <li>{@code addon/*.json} - {@code format:"limitKey"|"addon"|"addonItem"} entries: limit keys
 *       (version, costLimit, ignoreModelOnExceed), addons (limitKeyName/limitKeyCost + one bonus
 *       value per {@link IJBAddon.BonusType}) and the addon items that hand the addons out;</li>
 *   <li>{@code sound/*.json} - {@code format:"sound"} (a name plus a weighted {@code sounds} array
 *       of {@code {soundFile, weight, volume, pitch}}) and {@code format:"soundItem"};</li>
 *   <li>{@code lang/*.lang} (or {@code *.json}) - language files of the pack.</li>
 * </ul>
 */
public class ExtraPackageLoader {
    private static final Pattern EXTRA_LANG = Pattern.compile("lang/(?:.+/|)([\\w_-]+)\\.(lang|json)");
    private static final int MAX_ENTRY_NUMS = 127;
    private static final String CHARSET = "UTF-8";

    /** Scans {@code <config dir>/JointBlock} in name order and returns every valid pack. */
    public static List<ExtraPackage> loadExtraModels(File modelRoot) {
        List<ExtraPackage> importedPackageList = new ArrayList<>();
        try {
            if (!isFileReadable(modelRoot) || !modelRoot.isDirectory()) {
                return importedPackageList;
            }

            File[] files = modelRoot.listFiles();
            if (files == null) {
                return importedPackageList;
            }

            Arrays.sort(files, Comparator.comparing(File::getName));
            Hashtable<String, ExtraPackage> importedPackageMap = new Hashtable<>();

            for (File modelFile : files) {
                if (isFileReadable(modelFile) && "zip".equals(JBRModelUtils.getFileExtention(modelFile.getName()))) {
                    ResourceDupeCheckeHolder dupeCheckeHolder = new ResourceDupeCheckeHolder();
                    ExtraPackage modelPackage = readPackageFromZip(modelFile, importedPackageMap, dupeCheckeHolder);
                    if (modelPackage != null) {
                        modelPackage.resources = dupeCheckeHolder;
                        importedPackageList.add(modelPackage);
                        importedPackageMap.put(modelPackage.packageId, modelPackage);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return importedPackageList;
    }

    public static ExtraPackage readPackageFromZip(File file, Hashtable<String, ExtraPackage> importedPackageMap,
                                                  ResourceDupeCheckeHolder dupeCheckeHolder) {
        ArrayList<ExJBModelInfo> modelListCommited = new ArrayList<>();
        FileLoader fileLoader = null;

        try {
            JBRModelUtils.LogInfo("Found zip " + file.getName() + ". Seeking in...");
            fileLoader = file.isDirectory() ? new FileLoader(file) : new FileLoader(new ZipFile(file));

            FileLoader.FileEntry packageInfoEntry = fileLoader.getEntry("package.json");
            if (packageInfoEntry == null) {
                JBRModelUtils.LogInfo(",but could't find 'package.json'.");
                return null;
            }

            ExtraPackage exPackage = processPackageInfoFiles(fileLoader.getInputStream(packageInfoEntry));
            if (exPackage == null) {
                JBRModelUtils.LogInfo(",but 'package.json' was invalid. Package ignored.");
                return null;
            }

            if (importedPackageMap.containsKey(exPackage.packageId)) {
                JBRModelUtils.LogInfo(",but already has same packageId. Package ignored.");
                return null;
            }

            exPackage.fileLoader = fileLoader;

            for (Enumeration<FileLoader.FileEntry> enumeration = fileLoader.entries(); enumeration.hasMoreElements(); ) {
                FileLoader.FileEntry entry = enumeration.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String entryName = entry.getName();
                String extension = JBRModelUtils.getFileExtention(entryName);
                if (!"json".equals(extension)) {
                    continue;
                }

                try {
                    if (entryName.startsWith("sound/")) {
                        processSoundDirFilesSound(exPackage, dupeCheckeHolder, fileLoader.getInputStream(entry));
                        processSoundDirFilesSoundItem(exPackage, dupeCheckeHolder, fileLoader.getInputStream(entry));
                    } else if (entryName.startsWith("addon/")) {
                        processAddonDirFilesLimitKey(exPackage, dupeCheckeHolder, fileLoader.getInputStream(entry));
                        processAddonDirFilesAddon(exPackage, dupeCheckeHolder, fileLoader.getInputStream(entry));
                        processAddonDirFilesAddonItem(exPackage, dupeCheckeHolder, fileLoader.getInputStream(entry));
                    } else if (entryName.startsWith("model/")) {
                        processModelDirFiles(exPackage, dupeCheckeHolder, fileLoader.getInputStream(entry));
                    } else if (entryName.startsWith("blueprint/")) {
                        processBlueprintDirFiles(exPackage, dupeCheckeHolder, fileLoader.getInputStream(entry));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (dupeCheckeHolder.modelListWork.isEmpty() && dupeCheckeHolder.blueprintListWork.isEmpty()
                    && dupeCheckeHolder.addonItemListWork.isEmpty() && dupeCheckeHolder.soundListWork.isEmpty()
                    && dupeCheckeHolder.soundItemListWork.isEmpty()) {
                JBRModelUtils.LogInfo("----------Found no Model nor Blueprint nor Addon nor Sound in package. Package ignored.");
                return null;
            }

            JBRModelUtils.LogInfo("----------Found " + dupeCheckeHolder.modelListWork.size() + "  Models in package.");
            JBRModelUtils.LogInfo("----------Found " + dupeCheckeHolder.blueprintListWork.size() + "  Blueprints in package.");
            JBRModelUtils.LogInfo("----------Found " + dupeCheckeHolder.addonLimitKeyListWork.size() + "  LimitKey infos in package.");
            JBRModelUtils.LogInfo("----------Found " + dupeCheckeHolder.addonListWork.size() + "  Addon infos in package.");
            JBRModelUtils.LogInfo("----------Found " + dupeCheckeHolder.addonItemListWork.size() + "  Addon Items in package.");
            JBRModelUtils.LogInfo("----------Found " + dupeCheckeHolder.soundListWork.size() + "  Sound infos in package.");
            JBRModelUtils.LogInfo("----------Found " + dupeCheckeHolder.soundItemListWork.size() + "  Sound Items in package.");

            for (ExJBSoundInfo soundInf : dupeCheckeHolder.soundListWork) {
                if (exPackage.soundMap.containsKey(soundInf.soundName)) {
                    JBRModelUtils.LogInfo("----------Sound name duplicated in package! [" + soundInf.soundName + "]. Ignored.");
                } else {
                    exPackage.soundMap.put(soundInf.soundName, soundInf);
                }
            }

            ArrayList<ExJBSoundItemInfo> soundItemListCommited = new ArrayList<>();
            for (ExJBSoundItemInfo soundItemInf : dupeCheckeHolder.soundItemListWork) {
                if (soundItemInf.soundName != null) {
                    soundItemInf.sound = exPackage.soundMap.get(soundItemInf.soundName);
                }

                soundItemListCommited.add(soundItemInf);
            }

            for (ExJBAddonLimitKeyInfo limitKeyInf : dupeCheckeHolder.addonLimitKeyListWork) {
                exPackage.limitKeyListCommited.add(limitKeyInf);
            }

            for (ExJBAddonInfo addonInf : dupeCheckeHolder.addonListWork) {
                if (exPackage.addonMap.containsKey(addonInf.addonName)) {
                    JBRModelUtils.LogInfo("----------Addon name duplicated in package! [" + addonInf.addonName + "]. Ignored.");
                } else {
                    exPackage.addonMap.put(addonInf.addonName, addonInf);
                }
            }

            ArrayList<ExJBAddonItemInfo> addonItemListCommited = new ArrayList<>();
            for (ExJBAddonItemInfo addonItemInf : dupeCheckeHolder.addonItemListWork) {
                if (addonItemInf.addonName != null) {
                    addonItemInf.addon = exPackage.addonMap.get(addonItemInf.addonName);
                }

                addonItemListCommited.add(addonItemInf);
            }

            for (ExJBModelInfo modelInf : dupeCheckeHolder.modelListWork) {
                if (modelInf.addonName != null) {
                    modelInf.addon = exPackage.addonMap.get(modelInf.addonName);
                }

                modelListCommited.add(modelInf);
            }

            ArrayList<ExJBBluePrintInfo> blueprintListCommited = new ArrayList<>(dupeCheckeHolder.blueprintListWork);

            processExtraLangFiles(fileLoader, exPackage);
            exPackage.setModelListCommited(modelListCommited);
            exPackage.setBlueprintListCommited(blueprintListCommited);
            exPackage.setAddonItemListCommited(addonItemListCommited);
            exPackage.setSoundItemListCommited(soundItemListCommited);
            return exPackage;
        } catch (ZipException e) {
            JBRModelUtils.LogInfo("OOOps! [" + file.getName() + "] is not zip.");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ExtraPackage processPackageInfoFiles(InputStream istream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream, CHARSET));
            JsonObject gsonParsed = new Gson().fromJson(reader, JsonObject.class);
            if (gsonParsed == null) {
                return null;
            }

            String format = JBRModelUtils.getStringFromJson(gsonParsed, "format");
            String packageId = JBRModelUtils.getStringFromJson(gsonParsed, "packageId");
            int packageVersion = JBRModelUtils.getIntegerFromJson(gsonParsed, "packageVersion");
            if (!"package".equals(format) || packageVersion < 0 || packageVersion > 32767
                    || !JBRModelUtils.isValidPackageId(packageId)) {
                return null;
            }

            JBRModelUtils.LogInfo("-----Found Package! [" + packageId + "]");
            String packageName = JBRModelUtils.getStringFromJson(gsonParsed, "packageName");
            if (JBRModelUtils.isStringEmpty(packageName)) {
                packageName = packageId;
            }

            ExtraPackage ret = new ExtraPackage(packageId, packageName, (short) packageVersion);
            if (gsonParsed.has("packageIconFile")) {
                ret.packageIconFileName = JBRModelUtils.getStringFromJson(gsonParsed, "packageIconFile");
            }

            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeQuietly(istream);
        }
    }

    private static void processModelDirFiles(ExtraPackage exPackage, ResourceDupeCheckeHolder dupeCheckeHolder, InputStream istream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(istream, CHARSET));
            JsonObject gsonParsed = new Gson().fromJson(reader, JsonObject.class);
            if (gsonParsed == null) {
                return;
            }

            String format = JBRModelUtils.getStringFromJson(gsonParsed, "format");
            int modelId = JBRModelUtils.getIntegerFromJson(gsonParsed, "modelId");
            String modelName = JBRModelUtils.getStringFromJson(gsonParsed, "modelName");
            ExJBModelLayer[] modelLayers = processModelLayerInfs(gsonParsed, dupeCheckeHolder);
            if ("model".equals(format) && modelId >= 0 && modelId <= MAX_ENTRY_NUMS && !JBRModelUtils.isStringEmpty(modelName)
                    && modelLayers != null && modelLayers.length > 0) {
                ExJBModelInfo modelInfo = new ExJBModelInfo(exPackage, modelId, modelName, modelLayers, gsonParsed);
                String addonName = JBRModelUtils.getStringFromJson(gsonParsed, "addonName");
                if (!JBRModelUtils.isStringEmpty(addonName)) {
                    modelInfo.addonName = addonName;
                }

                String descriptionText = JBRModelUtils.getStringFromJson(gsonParsed, "modelDesc");
                if (!JBRModelUtils.isStringEmpty(descriptionText)) {
                    modelInfo.descriptionText = descriptionText;
                }

                dupeCheckeHolder.modelListWork.add(modelInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(istream);
        }
    }

    private static ExJBModelLayer[] processModelLayerInfs(JsonObject gsonParsed, ResourceDupeCheckeHolder dupeCheckeHolder) {
        if (!gsonParsed.has("layers")) {
            return null;
        }

        JsonElement jsonElem = gsonParsed.get("layers");
        if (!jsonElem.isJsonArray()) {
            return null;
        }

        JsonArray jsonArray = jsonElem.getAsJsonArray();
        dupeCheckeHolder.layerListWork.clear();

        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonElement each = jsonArray.get(i);
            if (each.isJsonObject()) {
                JsonObject eachObj = each.getAsJsonObject();
                int pass = JBRModelUtils.getIntegerFromJson(eachObj, "pass");
                String modelFileName = JBRModelUtils.getStringFromJson(eachObj, "modelFile");
                String textureFileName = JBRModelUtils.getStringFromJson(eachObj, "textureFile");
                if ((pass == 0 || pass == 1) && !JBRModelUtils.isStringEmpty(modelFileName)
                        && !JBRModelUtils.isStringEmpty(textureFileName)) {
                    dupeCheckeHolder.layerListWork.add(new ExJBModelLayer(pass, modelFileName, textureFileName));
                }
            }
        }

        return dupeCheckeHolder.layerListWork.isEmpty()
                ? null
                : dupeCheckeHolder.layerListWork.toArray(new ExJBModelLayer[0]);
    }

    private static void processBlueprintDirFiles(ExtraPackage exPackage, ResourceDupeCheckeHolder dupeCheckeHolder, InputStream istream) {
        try {
            JsonObject gsonParsed = parseJson(istream);
            if (gsonParsed == null) {
                return;
            }

            String format = JBRModelUtils.getStringFromJson(gsonParsed, "format");
            int blueprintId = JBRModelUtils.getIntegerFromJson(gsonParsed, "blueprintId");
            String blueprintName = JBRModelUtils.getStringFromJson(gsonParsed, "blueprintName");
            if ("blueprint".equals(format) && blueprintId >= 0 && blueprintId <= MAX_ENTRY_NUMS
                    && !JBRModelUtils.isStringEmpty(blueprintName) && gsonParsed.has("blueprintData")) {
                ExJBBluePrintInfo blueprintInfo = new ExJBBluePrintInfo(exPackage, blueprintId, blueprintName, gsonParsed);
                String descriptionText = JBRModelUtils.getStringFromJson(gsonParsed, "blueprintDesc");
                if (!JBRModelUtils.isStringEmpty(descriptionText)) {
                    blueprintInfo.descriptionText = descriptionText;
                }

                dupeCheckeHolder.blueprintListWork.add(blueprintInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(istream);
        }
    }

    private static void processAddonDirFilesLimitKey(ExtraPackage exPackage, ResourceDupeCheckeHolder dupeCheckeHolder, InputStream istream) {
        try {
            JsonObject gsonParsed = parseJson(istream);
            if (gsonParsed == null) {
                return;
            }

            String format = JBRModelUtils.getStringFromJson(gsonParsed, "format");
            String limitKeyName = JBRModelUtils.getStringFromJson(gsonParsed, "limitKeyName");
            if ("limitKey".equals(format) && !JBRModelUtils.isStringEmpty(limitKeyName)) {
                ExJBAddonLimitKeyInfo limitKeyInfo = new ExJBAddonLimitKeyInfo(exPackage, limitKeyName, gsonParsed);
                int version = JBRModelUtils.getIntegerFromJson(gsonParsed, "version");
                if (version > 0) {
                    limitKeyInfo.version = version;
                }

                int costLimit = JBRModelUtils.getIntegerFromJson(gsonParsed, "costLimit");
                if (costLimit > 0) {
                    limitKeyInfo.costLimit = costLimit;
                }

                limitKeyInfo.ignoreModelOnExceed = JBRModelUtils.getBooleanFromJson(gsonParsed, "ignoreModelOnExceed");
                limitKeyInfo.limitKeyDisplayName = JBRModelUtils.getStringFromJson(gsonParsed, "limitKeyDisplayName");
                dupeCheckeHolder.addonLimitKeyListWork.add(limitKeyInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(istream);
        }
    }

    private static void processAddonDirFilesAddon(ExtraPackage exPackage, ResourceDupeCheckeHolder dupeCheckeHolder, InputStream istream) {
        try {
            JsonObject gsonParsed = parseJson(istream);
            if (gsonParsed == null) {
                return;
            }

            String format = JBRModelUtils.getStringFromJson(gsonParsed, "format");
            String addonName = JBRModelUtils.getStringFromJson(gsonParsed, "addonName");
            if ("addon".equals(format) && !JBRModelUtils.isStringEmpty(addonName)) {
                ExJBAddonInfo addonInfo = new ExJBAddonInfo(exPackage, addonName, gsonParsed);
                String limitKeyName = JBRModelUtils.getStringFromJson(gsonParsed, "limitKeyName");
                if (!JBRModelUtils.isStringEmpty(limitKeyName)) {
                    addonInfo.limitKeyName = limitKeyName;
                }

                int limitKeyCost = JBRModelUtils.getIntegerFromJson(gsonParsed, "limitKeyCost");
                if (limitKeyCost > 0) {
                    addonInfo.limitKeyCost = limitKeyCost;
                }

                for (IJBAddon.BonusType bt : IJBAddon.BonusType.values()) {
                    addonInfo.bonusValues[bt.ordinal()] = 0.0F;
                    float bonusValue = JBRModelUtils.getFloatFromJsonNeg(gsonParsed, bt.name());
                    if (!Float.isNaN(bonusValue) && !Float.isInfinite(bonusValue)) {
                        addonInfo.bonusValues[bt.ordinal()] = bonusValue;
                    }
                }

                dupeCheckeHolder.addonListWork.add(addonInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(istream);
        }
    }

    private static void processAddonDirFilesAddonItem(ExtraPackage exPackage, ResourceDupeCheckeHolder dupeCheckeHolder, InputStream istream) {
        try {
            JsonObject gsonParsed = parseJson(istream);
            if (gsonParsed == null) {
                return;
            }

            String format = JBRModelUtils.getStringFromJson(gsonParsed, "format");
            int addonItemId = JBRModelUtils.getIntegerFromJson(gsonParsed, "addonItemId");
            String addonName = JBRModelUtils.getStringFromJson(gsonParsed, "addonName");
            if ("addonItem".equals(format) && addonItemId >= 0 && addonItemId <= MAX_ENTRY_NUMS
                    && !JBRModelUtils.isStringEmpty(addonName)) {
                ExJBAddonItemInfo addonItemInfo = new ExJBAddonItemInfo(exPackage, addonItemId, addonName, gsonParsed);
                String addonItemName = JBRModelUtils.getStringFromJson(gsonParsed, "addonItemName");
                if (!JBRModelUtils.isStringEmpty(addonItemName)) {
                    addonItemInfo.addonItemName = addonItemName;
                }

                String descriptionText = JBRModelUtils.getStringFromJson(gsonParsed, "addonItemDesc");
                if (!JBRModelUtils.isStringEmpty(descriptionText)) {
                    addonItemInfo.descriptionText = descriptionText;
                }

                dupeCheckeHolder.addonItemListWork.add(addonItemInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(istream);
        }
    }

    private static void processSoundDirFilesSound(ExtraPackage exPackage, ResourceDupeCheckeHolder dupeCheckeHolder, InputStream istream) {
        try {
            JsonObject gsonParsed = parseJson(istream);
            if (gsonParsed == null) {
                return;
            }

            String format = JBRModelUtils.getStringFromJson(gsonParsed, "format");
            String soundName = JBRModelUtils.getStringFromJson(gsonParsed, "soundName");
            ExJBSound[] soundFiles = processSoundFiles(exPackage, gsonParsed, dupeCheckeHolder);
            if ("sound".equals(format) && !JBRModelUtils.isStringEmpty(soundName) && soundFiles != null && soundFiles.length > 0) {
                dupeCheckeHolder.soundListWork.add(new ExJBSoundInfo(exPackage, soundName, soundFiles, gsonParsed));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(istream);
        }
    }

    private static ExJBSound[] processSoundFiles(ExtraPackage exPackage, JsonObject gsonParsed, ResourceDupeCheckeHolder dupeCheckeHolder) {
        if (!gsonParsed.has("sounds")) {
            return null;
        }

        JsonElement jsonElem = gsonParsed.get("sounds");
        if (!jsonElem.isJsonArray()) {
            return null;
        }

        JsonArray jsonArray = jsonElem.getAsJsonArray();
        dupeCheckeHolder.soundeFileListWork.clear();

        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonElement each = jsonArray.get(i);
            if (each.isJsonObject()) {
                JsonObject eachObj = each.getAsJsonObject();
                String soundFileName = JBRModelUtils.getStringFromJson(eachObj, "soundFile");
                if (!JBRModelUtils.isStringEmpty(soundFileName)) {
                    // 1.12.2 read weight/volume/pitch from the outer object only; per-sound values are
                    // what the format documents, so they win and the outer object stays the fallback.
                    int weight = eachObj.has("weight")
                            ? JBRModelUtils.getIntegerFromJson(eachObj, "weight")
                            : JBRModelUtils.getIntegerFromJson(gsonParsed, "weight");
                    if (weight <= 0) {
                        weight = 1;
                    }

                    float volume = eachObj.has("volume")
                            ? JBRModelUtils.getFloatFromJson(eachObj, "volume")
                            : JBRModelUtils.getFloatFromJson(gsonParsed, "volume");
                    if (volume < 0.0F) {
                        volume = 1.0F;
                    }

                    float pitch = eachObj.has("pitch")
                            ? JBRModelUtils.getFloatFromJson(eachObj, "pitch")
                            : JBRModelUtils.getFloatFromJson(gsonParsed, "pitch");
                    if (pitch < 0.0F) {
                        pitch = 1.0F;
                    }

                    dupeCheckeHolder.soundeFileListWork.add(new ExJBSound(exPackage, soundFileName, weight, volume, pitch));
                }
            }
        }

        return dupeCheckeHolder.soundeFileListWork.isEmpty()
                ? null
                : dupeCheckeHolder.soundeFileListWork.toArray(new ExJBSound[0]);
    }

    private static void processSoundDirFilesSoundItem(ExtraPackage exPackage, ResourceDupeCheckeHolder dupeCheckeHolder, InputStream istream) {
        try {
            JsonObject gsonParsed = parseJson(istream);
            if (gsonParsed == null) {
                return;
            }

            String format = JBRModelUtils.getStringFromJson(gsonParsed, "format");
            int soundItemId = JBRModelUtils.getIntegerFromJson(gsonParsed, "soundItemId");
            String soundName = JBRModelUtils.getStringFromJson(gsonParsed, "soundName");
            if ("soundItem".equals(format) && soundItemId >= 0 && soundItemId <= MAX_ENTRY_NUMS
                    && !JBRModelUtils.isStringEmpty(soundName)) {
                ExJBSoundItemInfo soundItemInfo = new ExJBSoundItemInfo(exPackage, soundItemId, soundName, gsonParsed);
                String soundItemName = JBRModelUtils.getStringFromJson(gsonParsed, "soundItemName");
                if (!JBRModelUtils.isStringEmpty(soundItemName)) {
                    soundItemInfo.soundItemName = soundItemName;
                }

                String descriptionText = JBRModelUtils.getStringFromJson(gsonParsed, "soundItemDesc");
                if (!JBRModelUtils.isStringEmpty(descriptionText)) {
                    soundItemInfo.descriptionText = descriptionText;
                }

                dupeCheckeHolder.soundItemListWork.add(soundItemInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(istream);
        }
    }

    private static JsonObject parseJson(InputStream istream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream, CHARSET));
        return new Gson().fromJson(reader, JsonObject.class);
    }

    /**
     * Loads {@code lang/<code>.lang} (legacy {@code key=value}) and {@code lang/<code>.json} files
     * into {@link ExtraPackage#langMap}. 1.12.2 used {@code LanguageMap.parseLangFile}; 1.20.1 kept
     * {@link Language#loadFromJson} for the JSON form and dropped the {@code .lang} parser, so the
     * legacy form is parsed here (same {@code key=value} / {@code #} comment rules).
     */
    private static void processExtraLangFiles(FileLoader fileLoader, ExtraPackage exPackage) {
        try {
            for (Enumeration<FileLoader.FileEntry> enumeration = fileLoader.entries(); enumeration.hasMoreElements(); ) {
                FileLoader.FileEntry entry = enumeration.nextElement();
                Matcher matcher = EXTRA_LANG.matcher(entry.getName());
                if (!matcher.matches()) {
                    continue;
                }

                String lang = matcher.group(1).toLowerCase(Locale.ROOT);
                Map<String, String> langMap = new Hashtable<>();
                try (InputStream is = fileLoader.getInputStream(entry)) {
                    if (is == null) {
                        continue;
                    }

                    if ("json".equals(matcher.group(2))) {
                        Language.loadFromJson(is, langMap::put);
                    } else {
                        parseLegacyLangFile(is, langMap::put);
                    }
                }

                if (exPackage.langMap == null) {
                    exPackage.langMap = new Hashtable<>();
                }

                exPackage.langMap.put(lang, langMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 1.12.2 {@code LanguageMap.parseLangFile}: {@code key=value} lines, {@code #} comments. */
    private static void parseLegacyLangFile(InputStream is, java.util.function.BiConsumer<String, String> output) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }

            int eq = line.indexOf('=');
            if (eq != -1) {
                output.accept(line.substring(0, eq), line.substring(eq + 1));
            }
        }
    }

    public static final boolean isFileReadable(File file) {
        return file != null && file.exists() && file.canRead();
    }

    private static void closeQuietly(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ignored) {
        }
    }

    /** Working lists of one pack load; the dupe maps dedupe models/textures shared between packs. */
    public static class ResourceDupeCheckeHolder {
        public ArrayList<ExJBModelLayer> layerListWork = new ArrayList<>();
        public ArrayList<ExJBModelInfo> modelListWork = new ArrayList<>();
        public ArrayList<ExJBBluePrintInfo> blueprintListWork = new ArrayList<>();
        public ArrayList<ExJBAddonInfo> addonListWork = new ArrayList<>();
        public ArrayList<ExJBAddonItemInfo> addonItemListWork = new ArrayList<>();
        public ArrayList<ExJBAddonLimitKeyInfo> addonLimitKeyListWork = new ArrayList<>();
        public ArrayList<ExJBSound> soundeFileListWork = new ArrayList<>();
        public ArrayList<ExJBSoundInfo> soundListWork = new ArrayList<>();
        public ArrayList<ExJBSoundItemInfo> soundItemListWork = new ArrayList<>();
        public Hashtable<String, Object> dupeCheckModel = new Hashtable<>();
        public Hashtable<String, Object> dupeCheckTexture = new Hashtable<>();
    }
}
