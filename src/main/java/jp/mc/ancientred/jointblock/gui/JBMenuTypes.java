package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.block.BlockJointCore;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.tileentity.TileEntityBlueprintBuilder;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Menu types for the 14 JointBlock GUI ids ({@link JBGuiIds}).
 *
 * <p>1.12.2 opened a GUI by {@code player.openGui(mod, intGuiId, world, x, y, z)} and the
 * common proxy built the container in {@code getServerGuiElement}.  In 1.20.1 the menu is
 * created by one of these factories (server side, through
 * {@code NetworkHooks.openScreen}/{@code player.openMenu}) and the screen is registered
 * separately by {@code MenuScreens.register}.  The extra data payload is the
 * {@code BlockPos} of the block for the block-bound GUIs and the entity id for the
 * entity-bound ones, mirroring the 1.12.2 {@code IGuiHandler} arguments.
 */
public final class JBMenuTypes {

    private JBMenuTypes() {
    }

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, JointBlockRemastered.MOD_ID);

    public static MenuType<ContainerJointBlock> JOINT_BLOCK;
    public static MenuType<ContainerJointBlockExtra> JOINT_BLOCK_EXTRA;
    public static MenuType<ContainerJointChest> JOINT_CHEST_FROM_BLOCK;
    public static MenuType<ContainerJointChest> JOINT_CHEST_FROM_ENTITY;
    public static MenuType<ContainerJointCore> JOINT_CORE;
    public static MenuType<ContainerJointCoreAddons> JOINT_CORE_ADDONS;
    public static MenuType<ContainerJointCoreSounds> JOINT_CORE_SOUNDS;
    public static MenuType<ContainerJointCoreFuels> JOINT_CORE_FUELS_FROM_BLOCK;
    public static MenuType<ContainerJointCoreFuels> JOINT_CORE_FUELS_FROM_ENTITY;
    public static MenuType<ContainerBlueprintBuilder> BLUEPRINT_BUILDER;

    public static void register(IEventBus modBus) {
        MENU_TYPES.register("joint_block", () -> JOINT_BLOCK = menu(JBMenuTypes::createJointBlock));
        MENU_TYPES.register("joint_block_extra", () -> JOINT_BLOCK_EXTRA = menu(JBMenuTypes::createJointBlockExtra));
        MENU_TYPES.register("joint_chest_from_block", () -> JOINT_CHEST_FROM_BLOCK = menu(JBMenuTypes::createJointChestFromBlock));
        MENU_TYPES.register("joint_chest_from_entity", () -> JOINT_CHEST_FROM_ENTITY = menu(JBMenuTypes::createJointChestFromEntity));
        MENU_TYPES.register("joint_core", () -> JOINT_CORE = menu(JBMenuTypes::createJointCore));
        MENU_TYPES.register("joint_core_addons", () -> JOINT_CORE_ADDONS = menu(JBMenuTypes::createJointCoreAddons));
        MENU_TYPES.register("joint_core_sounds", () -> JOINT_CORE_SOUNDS = menu(JBMenuTypes::createJointCoreSounds));
        MENU_TYPES.register("joint_core_fuels_from_block", () -> JOINT_CORE_FUELS_FROM_BLOCK = menu(JBMenuTypes::createJointCoreFuelsFromBlock));
        MENU_TYPES.register("joint_core_fuels_from_entity", () -> JOINT_CORE_FUELS_FROM_ENTITY = menu(JBMenuTypes::createJointCoreFuelsFromEntity));
        MENU_TYPES.register("blueprint_builder", () -> BLUEPRINT_BUILDER = menu(JBMenuTypes::createBlueprintBuilder));
        MENU_TYPES.register(modBus);
    }

    /**
     * Forge's {@link IContainerFactory} carries the extra {@code FriendlyByteBuf} payload, but it
     * also inherits {@code MenuType.MenuSupplier.create(int, Inventory)} as a second abstract
     * method, so it cannot be used as a lambda target directly.  Defaulting the two-argument form
     * (never called for the menus here, which are always opened with the block pos / entity id)
     * leaves a single abstract method and makes the factories lambdas again.
     */
    private interface JBMenuFactory<T extends net.minecraft.world.inventory.AbstractContainerMenu> extends IContainerFactory<T> {
        @Override
        default T create(int windowId, Inventory playerInv) {
            return null;
        }
    }

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> menu(JBMenuFactory<T> factory) {
        return new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
    }

    /**
     * Client-side {@code MenuScreens.register} wiring.  1.12.2 had no equivalent step (the client
     * {@code IGuiHandler} built the {@code GuiContainer} directly from the container instance), so
     * in 1.20.1 every menu type needs its screen registered here.  It is called from
     * {@code common.JBClientSetup} through the {@code RegisterMenuScreensEvent}; the GUI-open path
     * itself is {@code JBModContainer.proxy.openGui(...)}.
     */
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static void registerScreens() {
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointBlock, GuiJointBlock>register(JOINT_BLOCK, (menu, inv, title) -> new GuiJointBlock(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointBlockExtra, GuiJointBlockExtra>register(JOINT_BLOCK_EXTRA, (menu, inv, title) -> new GuiJointBlockExtra(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointChest, GuiJointChest>register(JOINT_CHEST_FROM_BLOCK, (menu, inv, title) -> new GuiJointChest(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointChest, GuiJointChest>register(JOINT_CHEST_FROM_ENTITY, (menu, inv, title) -> new GuiJointChest(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointCore, GuiJointCore>register(JOINT_CORE, (menu, inv, title) -> new GuiJointCore(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointCoreAddons, GuiJointCoreAddons>register(JOINT_CORE_ADDONS, (menu, inv, title) -> new GuiJointCoreAddons(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointCoreSounds, GuiJointCoreSounds>register(JOINT_CORE_SOUNDS, (menu, inv, title) -> new GuiJointCoreSounds(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointCoreFuels, GuiJointCoreFuels>register(JOINT_CORE_FUELS_FROM_BLOCK, (menu, inv, title) -> new GuiJointCoreFuels(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerJointCoreFuels, GuiJointCoreFuels>register(JOINT_CORE_FUELS_FROM_ENTITY, (menu, inv, title) -> new GuiJointCoreFuels(menu, inv));
        net.minecraft.client.gui.screens.MenuScreens.<ContainerBlueprintBuilder, GuiBlueprintBuilder>register(BLUEPRINT_BUILDER, (menu, inv, title) -> new GuiBlueprintBuilder(menu, inv));
    }

    private static TileEntityJoint getJointBlockEntity(Inventory inv, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        return BlockJointBase.getTileEntityJointerBase(inv.player.level(), pos);
    }

    private static TileEntityJointCore getJointCoreEntity(Inventory inv, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        return BlockJointCore.getTileEntityJointerCore(inv.player.level(), pos);
    }

    private static EntityJointed getJointedEntity(Inventory inv, FriendlyByteBuf data) {
        int entityId = data.readVarInt();
        Entity entity = inv.player.level().getEntity(entityId);
        return entity instanceof EntityJointed ? (EntityJointed)entity : null;
    }

    public static ContainerJointBlock createJointBlock(int containerId, Inventory inv, FriendlyByteBuf data) {
        TileEntityJoint te = getJointBlockEntity(inv, data);
        return te == null ? null : new ContainerJointBlock(containerId, inv, te);
    }

    public static ContainerJointBlockExtra createJointBlockExtra(int containerId, Inventory inv, FriendlyByteBuf data) {
        TileEntityJoint te = getJointBlockEntity(inv, data);
        return te == null ? null : new ContainerJointBlockExtra(containerId, inv, te);
    }

    public static ContainerJointChest createJointChestFromBlock(int containerId, Inventory inv, FriendlyByteBuf data) {
        TileEntityJointCore teCore = getJointCoreEntity(inv, data);
        return teCore == null ? null : new ContainerJointChest(JOINT_CHEST_FROM_BLOCK, containerId, inv, teCore.innerChest);
    }

    public static ContainerJointChest createJointChestFromEntity(int containerId, Inventory inv, FriendlyByteBuf data) {
        EntityJointed ej = getJointedEntity(inv, data);
        return ej == null ? null : new ContainerJointChest(JOINT_CHEST_FROM_ENTITY, containerId, inv, ej.innerChest);
    }

    public static ContainerJointCore createJointCore(int containerId, Inventory inv, FriendlyByteBuf data) {
        TileEntityJointCore teCore = getJointCoreEntity(inv, data);
        return teCore == null ? null : new ContainerJointCore(containerId, inv, teCore);
    }

    public static ContainerJointCoreAddons createJointCoreAddons(int containerId, Inventory inv, FriendlyByteBuf data) {
        TileEntityJointCore teCore = getJointCoreEntity(inv, data);
        return teCore == null ? null : new ContainerJointCoreAddons(containerId, inv, teCore);
    }

    public static ContainerJointCoreSounds createJointCoreSounds(int containerId, Inventory inv, FriendlyByteBuf data) {
        TileEntityJointCore teCore = getJointCoreEntity(inv, data);
        return teCore == null ? null : new ContainerJointCoreSounds(containerId, inv, teCore);
    }

    public static ContainerJointCoreFuels createJointCoreFuelsFromBlock(int containerId, Inventory inv, FriendlyByteBuf data) {
        TileEntityJointCore teCore = getJointCoreEntity(inv, data);
        return teCore == null ? null : new ContainerJointCoreFuels(JOINT_CORE_FUELS_FROM_BLOCK, containerId, inv, teCore.innerChest);
    }

    public static ContainerJointCoreFuels createJointCoreFuelsFromEntity(int containerId, Inventory inv, FriendlyByteBuf data) {
        EntityJointed ej = getJointedEntity(inv, data);
        return ej == null ? null : new ContainerJointCoreFuels(JOINT_CORE_FUELS_FROM_ENTITY, containerId, inv, ej.innerChest);
    }

    public static ContainerBlueprintBuilder createBlueprintBuilder(int containerId, Inventory inv, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        net.minecraft.world.level.block.entity.BlockEntity te = WorldHelper.getTileEntity(inv.player.level(), pos);
        return te instanceof TileEntityBlueprintBuilder ? new ContainerBlueprintBuilder(containerId, inv, (TileEntityBlueprintBuilder)te) : null;
    }
}
