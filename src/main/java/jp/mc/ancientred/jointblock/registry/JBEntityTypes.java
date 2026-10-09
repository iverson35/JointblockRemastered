package jp.mc.ancientred.jointblock.registry;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityBullet;
import jp.mc.ancientred.jointblock.entity.EntityDisassembleBall;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntityTempWorker;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Entity types. Registry names and client tracking ranges are the originals from
 * {@code JBModContainer} ({@code name("entityjointed")} etc. with the mod id).
 *
 * <p>{@code EntitySeatPart} was created directly in 1.12.2 without ever being registered; it is a
 * {@link net.minecraftforge.entity.PartEntity} of {@code EntityJointed} and needs no type.
 * The client-only camera entity's type lives in {@link JBClientEntityTypes}.
 */
public final class JBEntityTypes {

    private JBEntityTypes() {
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JointBlockRemastered.MOD_ID);

    // Concrete element types so subclass constructors can forward them without casts.
    public static EntityType<EntityJointed> JOINTED;
    public static EntityType<EntityDisassembleBall> DISASSEMBLE_BALL;
    public static EntityType<EntityBullet> BULLET;
    public static EntityType<EntityTempWorker> TEMP_WORKER;

    public static void register(IEventBus modBus) {
        // The assembled unit resizes itself at runtime (4.0 wide, dynamic height).
        ENTITY_TYPES.register("entityjointed", () -> JOINTED = EntityType.Builder
            .<EntityJointed>of(EntityJointed::new, MobCategory.MISC)
            .sized(4.0F, 1.0F)
            .clientTrackingRange(JBConfig.entityJointedTrackingRange)
            .updateInterval(1)
            .build("entityjointed"));
        ENTITY_TYPES.register("entitydisasmball", () -> DISASSEMBLE_BALL = EntityType.Builder
            .<EntityDisassembleBall>of(EntityDisassembleBall::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(40)
            .updateInterval(1)
            .build("entitydisasmball"));
        ENTITY_TYPES.register("entitybeamlazer", () -> BULLET = EntityType.Builder
            .<EntityBullet>of(EntityBullet::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(200)
            .updateInterval(1)
            .build("entitybeamlazer"));
        ENTITY_TYPES.register("entitytempworker", () -> TEMP_WORKER = EntityType.Builder
            .<EntityTempWorker>of(EntityTempWorker::new, MobCategory.MISC)
            .sized(0.6F, 1.8F)
            .clientTrackingRange(32)
            .updateInterval(1)
            .build("entitytempworker"));
        ENTITY_TYPES.register(modBus);
    }
}
