package jp.mc.ancientred.jbrobot.registry;

import jp.mc.ancientred.jbrobot.gui.ContainerModelCatalog;
import jp.mc.ancientred.jbrobot.gui.GuiModelCatalog;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Menu types of the JBRobotModel addon (now part of the {@code jointblock} mod).
 *
 * <p>1.12.2 opened the catalog by {@code player.openGui(JBRobotMODContainer.instance, 5596, ...)}
 * and the addon's own {@code IGuiHandler} built the {@code ContainerModelCatalog} directly.  In
 * 1.20.1 the menu is built by this factory and the screen is registered separately; the catalog is
 * player-bound, so no extra payload is written to the open-screen buffer.
 *
 * <p>Exposes the hook the mod constructor must call: {@link #register(IEventBus)} on the mod event
 * bus and {@link #registerScreens()} from the client setup.
 */
public final class JBRMenuTypes {

    private JBRMenuTypes() {
    }

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, JointBlockRemastered.MOD_ID);

    public static MenuType<ContainerModelCatalog> MODEL_CATALOG;

    public static void register(IEventBus modBus) {
        MENU_TYPES.register("model_catalog", () -> MODEL_CATALOG = menu(JBRMenuTypes::createModelCatalog));
        MENU_TYPES.register(modBus);
    }

    /**
     * Client-side {@code MenuScreens.register} wiring (1.12.2 built the screen from the container
     * instance in the addon's client {@code IGuiHandler}, so there was no equivalent step).
     */
    @OnlyIn(Dist.CLIENT)
    public static void registerScreens() {
        MenuScreens.<ContainerModelCatalog, GuiModelCatalog>register(MODEL_CATALOG, (menu, inv, title) -> new GuiModelCatalog(menu, inv));
    }

    public static ContainerModelCatalog createModelCatalog(int containerId, Inventory inv, FriendlyByteBuf data) {
        return new ContainerModelCatalog(containerId, inv);
    }

    /**
     * Forge's {@link IContainerFactory} also inherits {@code MenuType.MenuSupplier.create(int,
     * Inventory)} as a second abstract method, so it cannot be used as a lambda target directly.
     * Defaulting the two-argument form (never called for the catalog, which is always opened with the
     * payload) leaves a single abstract method and makes the factories lambdas again.
     */
    private interface JBRMenuFactory<T extends AbstractContainerMenu> extends IContainerFactory<T> {
        @Override
        default T create(int windowId, Inventory playerInv) {
            return null;
        }
    }

    private static <T extends AbstractContainerMenu> MenuType<T> menu(JBRMenuFactory<T> factory) {
        return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
    }
}
