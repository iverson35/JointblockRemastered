package jp.mc.ancientred.jointblock.registry;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Mod attributes.
 *
 * <p>1.12.2 had no attribute registry on this path - {@code EntityJointed} tracked energy in a
 * plain field. The port models it as a real {@link Attribute} so it syncs and can be modified by
 * attribute modifiers, which requires it to exist in {@code ForgeRegistries.ATTRIBUTES} before any
 * {@code AttributeSupplier} referencing it is built.
 */
public final class JBAttributes {

    private JBAttributes() {
    }

    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(ForgeRegistries.ATTRIBUTES, JointBlockRemastered.MOD_ID);

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register("max_energy", () -> EntityJointed.MAX_ENERGY);
        ATTRIBUTES.register(modBus);
    }
}
