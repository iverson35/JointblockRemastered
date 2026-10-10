package jp.mc.ancientred.jointblock.registry;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.entity.EntityJBClientCamera;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Client-only entity types.
 *
 * <p>{@link EntityJBClientCamera} is {@code @OnlyIn(Dist.CLIENT)} (it is the 1.12.2 fake camera
 * entity the seat camera rides), so Forge's {@code RuntimeDistCleaner} refuses to load it on a
 * dedicated server: registering its type from the common {@link JBEntityTypes} crashed the server
 * with {@code Attempted to load class ... for invalid dist DEDICATED_SERVER}. The registration
 * therefore lives here and is only invoked when {@code FMLEnvironment.dist} is the client.
 *
 * <p>1.12.2 never registered this entity at all; it was created directly with
 * {@code new EntityJBClientCamera(world)}. 1.20.1's {@code Entity} constructor requires a type.
 */
public final class JBClientEntityTypes {

    private JBClientEntityTypes() {
    }

    public static final DeferredRegister<EntityType<?>> CLIENT_ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JointBlockRemastered.MOD_ID);

    public static EntityType<EntityJBClientCamera> CLIENT_CAMERA;

    public static void register(IEventBus modBus) {
        CLIENT_ENTITY_TYPES.register("entityclientcamera", () -> CLIENT_CAMERA = EntityType.Builder
            .<EntityJBClientCamera>of(EntityJBClientCamera::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(64)
            .updateInterval(1)
            .build("entityclientcamera"));
        CLIENT_ENTITY_TYPES.register(modBus);
    }
}
