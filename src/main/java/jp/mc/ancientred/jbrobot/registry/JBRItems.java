package jp.mc.ancientred.jbrobot.registry;

import jp.mc.ancientred.jbrobot.item.ItemModelBazookaGripParts;
import jp.mc.ancientred.jbrobot.item.ItemModelBazookaHeadParts;
import jp.mc.ancientred.jbrobot.item.ItemModelBazookaTailParts;
import jp.mc.ancientred.jbrobot.item.ItemModelBazookaTubeParts;
import jp.mc.ancientred.jbrobot.item.ItemModelBladeBody;
import jp.mc.ancientred.jbrobot.item.ItemModelBladeGrip;
import jp.mc.ancientred.jbrobot.item.ItemModelBladeTop;
import jp.mc.ancientred.jbrobot.item.ItemModelDrill;
import jp.mc.ancientred.jbrobot.item.ItemModelFace;
import jp.mc.ancientred.jbrobot.item.ItemModelFlamer;
import jp.mc.ancientred.jbrobot.item.ItemModelHand;
import jp.mc.ancientred.jbrobot.item.ItemModelHarvester;
import jp.mc.ancientred.jbrobot.item.ItemModelLazerGatling;
import jp.mc.ancientred.jbrobot.item.ItemModelLazerGun;
import jp.mc.ancientred.jbrobot.item.ItemModelMissilePod;
import jp.mc.ancientred.jbrobot.item.ItemModelPlanter;
import jp.mc.ancientred.jbrobot.item.ItemModelThruster;
import jp.mc.ancientred.jbrobot.item.ItemModelWheel;
import jp.mc.ancientred.jbrobot.item.addon.ItemBasicAddon;
import jp.mc.ancientred.jbrobot.item.external.ItemCatalog;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The 20 fixed JBRobotModel items, registered in the {@code jbrobot} namespace so the ported assets
 * ({@code assets/jbrobot/models/item/*.json}, {@code item.jbrobot.<name>} lang keys) resolve.
 *
 * <p>1.12.2 registered these from {@code JBRobotMODContainer.preInit}
 * ({@code ModelFaceItem = new ItemModelFace()...} etc.); 1.20.1 uses a {@link DeferredRegister}. The
 * registry names, field names and the registration order are the originals. The dynamically loaded
 * model-pack items ({@code m_<packageId>}/{@code a_<packageId>}/{@code s_<packageId>}) are registered
 * by the pack subsystem with its own register, not here.
 *
 * <p>Subtype model metas are the vanilla {@code Damage} tag ({@code ItemStack#getDamageValue}), the
 * same convention {@code modelpack.ExtraPackage#subtypedStack} and
 * {@code TileEntityJoint#getModelItemDamage} use; the original damage-value enumeration of
 * {@code func_150895_a} is done by the creative tab.
 */
public final class JBRItems {

   private JBRItems() {
   }

   /** 1.12.2 {@code JBRobotMODContainer.MODID_LC} (port namespace). */
   public static final String MODID_LC = "jbrobot";

   public static final DeferredRegister<Item> ITEMS =
         DeferredRegister.create(ForgeRegistries.ITEMS, MODID_LC);

   public static RegistryObject<Item> ModelFaceItem;
   public static RegistryObject<Item> ModelHandItem;
   public static RegistryObject<Item> ModelWheelItem;
   public static RegistryObject<Item> ModelBazookaTubePartsItem;
   public static RegistryObject<Item> ModelBazookaHeadPartsItem;
   public static RegistryObject<Item> ModelBazookaTailPartsItem;
   public static RegistryObject<Item> ModelBazookaGripPartsItem;
   public static RegistryObject<Item> ModelLazerGunItem;
   public static RegistryObject<Item> ModelLazerGatlingItem;
   public static RegistryObject<Item> ModelDrillItem;
   public static RegistryObject<Item> ModelMissilePodItem;
   public static RegistryObject<Item> ModelThrusterItem;
   public static RegistryObject<Item> ModelHarvesterItem;
   public static RegistryObject<Item> ModelPlanterItem;
   public static RegistryObject<Item> ModelBladeTopItem;
   public static RegistryObject<Item> ModelBladeBodyItem;
   public static RegistryObject<Item> ModelBladeGripItem;
   public static RegistryObject<Item> ModelFlamerItem;
   public static RegistryObject<Item> CatalogItem;
   public static RegistryObject<Item> BasicAddonItem;

   public static void register(IEventBus modBus) {
      ModelFaceItem = ITEMS.register("model_robot_face", ItemModelFace::new);
      ModelHandItem = ITEMS.register("model_robot_hand", ItemModelHand::new);
      ModelWheelItem = ITEMS.register("model_wheel", ItemModelWheel::new);
      ModelBazookaTubePartsItem = ITEMS.register("model_bazooka_tube", ItemModelBazookaTubeParts::new);
      ModelBazookaHeadPartsItem = ITEMS.register("model_bazooka_head", ItemModelBazookaHeadParts::new);
      ModelBazookaTailPartsItem = ITEMS.register("model_bazooka_tail", ItemModelBazookaTailParts::new);
      ModelBazookaGripPartsItem = ITEMS.register("model_bazooka_grip", ItemModelBazookaGripParts::new);
      ModelLazerGunItem = ITEMS.register("model_lazergun", ItemModelLazerGun::new);
      ModelLazerGatlingItem = ITEMS.register("model_lazergatling", ItemModelLazerGatling::new);
      ModelDrillItem = ITEMS.register("model_drill", ItemModelDrill::new);
      ModelMissilePodItem = ITEMS.register("model_mpod", ItemModelMissilePod::new);
      ModelThrusterItem = ITEMS.register("model_thruster", ItemModelThruster::new);
      ModelHarvesterItem = ITEMS.register("model_harvester", ItemModelHarvester::new);
      ModelPlanterItem = ITEMS.register("model_planter", ItemModelPlanter::new);
      ModelBladeTopItem = ITEMS.register("model_blade_top", ItemModelBladeTop::new);
      ModelBladeBodyItem = ITEMS.register("model_blade_body", ItemModelBladeBody::new);
      ModelBladeGripItem = ITEMS.register("model_blade_grip", ItemModelBladeGrip::new);
      ModelFlamerItem = ITEMS.register("model_flamer", ItemModelFlamer::new);
      CatalogItem = ITEMS.register("jbrobot_catalog", ItemCatalog::new);
      BasicAddonItem = ITEMS.register("basic_addon", ItemBasicAddon::new);

      ITEMS.register(modBus);
   }
}
