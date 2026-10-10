package jp.mc.ancientred.jbrobot.registry;

import java.util.ArrayList;
import java.util.Map;
import jp.mc.ancientred.jbrobot.JBRobotMODContainer;
import jp.mc.ancientred.jointblock.modelpack.ExtraPackage;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The addon's own creative tab ({@code jbrobot}), the 1.20.1 replacement of the 1.12.2
 * {@code JBRobotMODContainer.jbRobotTab}.
 *
 * <p>1.12.2 added every addon item plus the per-pack model/addon items to the tab from
 * {@code setJBCreativeTabs()}; the items were all {@code modj_jbrobot} items. The port owns exactly
 * the {@code jbrobot} namespace, so the tab enumerates that namespace instead of listing twenty
 * fields, and then adds one stack per pack subtype (1.12.2's {@code getSubItems}).
 */
public final class JBRCreativeTab {

    private JBRCreativeTab() {
    }

    /**
     * 1.12.2 {@code func_150895_a} of the fixed items that enumerate more than one damage value, in
     * the original order ({@code ItemModelBladeTop}:49, {@code ItemModelBazookaHeadParts}:70,
     * {@code ItemModelHand}:62, {@code ItemBasicAddon}:198).
     */
    private static final Map<String, int[]> FIXED_SUBTYPES = Map.of(
            "model_blade_top", new int[]{0, 1},
            "model_bazooka_head", new int[]{0, 1},
            "model_robot_hand", new int[]{0, 1},
            "basic_addon", new int[]{0, 4, 2, 6, 8, 10, 12, 14, 16, 30, 32, 34});

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JBRobotMODContainer.ASSET_NAMESPACE);

    public static final RegistryObject<CreativeModeTab> JBR_TAB = CREATIVE_MODE_TABS.register(
            "jbrobot",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.jbrobot"))
                    .icon(JBRCreativeTab::icon)
                    .displayItems((params, output) -> {
                        for (Item item : ForgeRegistries.ITEMS.getValues()) {
                            ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                            if (key == null || !JBRobotMODContainer.ASSET_NAMESPACE.equals(key.getNamespace()) || isPackItem(item)) {
                                continue;
                            }

                            int[] metas = FIXED_SUBTYPES.get(key.getPath());
                            if (metas == null) {
                                output.accept(item);
                            } else {
                                for (int meta : metas) {
                                    output.accept(subtyped(item, meta));
                                }
                            }
                        }

                        for (ExtraPackage pack : JBRobotMODContainer.importedExPackageList) {
                            addSubtypes(output, JBRobotMODContainer.getModelItem(pack), pack.modelsIndexCompressed, pack.modelNums);
                            addSubtypes(output, JBRobotMODContainer.getAddonItem(pack), pack.addonItemsIndexCompressed, pack.addonItemNums);
                            addSubtypes(output, JBRobotMODContainer.getSoundItem(pack), pack.soundItemsIndexCompressed, pack.soundItemNums);
                        }
                    })
                    .build());

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }

    private static ItemStack subtyped(Item item, int meta) {
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(meta);
        return stack;
    }

    private static ItemStack icon() {
        Item face = ForgeRegistries.ITEMS.getValue(new ResourceLocation(JBRobotMODContainer.ASSET_NAMESPACE, "model_robot_face"));
        return face != null ? new ItemStack(face) : ItemStack.EMPTY;
    }

    /** Per-pack items carry damage subtypes, so they are enumerated instead of accepted once. */
    private static boolean isPackItem(Item item) {
        for (ExtraPackage pack : JBRobotMODContainer.importedExPackageList) {
            if (item == JBRobotMODContainer.getModelItem(pack) || item == JBRobotMODContainer.getAddonItem(pack)
                    || item == JBRobotMODContainer.getSoundItem(pack)) {
                return true;
            }
        }

        return false;
    }

    private static void addSubtypes(CreativeModeTab.Output output, Item item, int[] ids, int count) {
        if (item == null) {
            return;
        }

        ArrayList<ItemStack> stacks = JBRobotMODContainer.createSubtypeStacks(item, ids);
        for (int i = 0; i < count && i < stacks.size(); ++i) {
            output.accept(stacks.get(i));
        }
    }
}
