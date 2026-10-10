package jp.mc.ancientred.jbrobot.item.addon;

import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.item.IColorHanldlerItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The basic addon item, ported from the 1.12.2
 * {@code jp.mc.ancientred.jbrobot.item.addon.ItemBasicAddon}.
 *
 * <p>Each damage-value subtype is one addon: nine plain stat addons
 * ({@link AbstractBasicAddon}, no limit key) and three sub-type addons
 * ({@link AbstractSubTypeAddon}, limit key {@code JBSUBTYPE}).
 *
 * <p>1.20.1 deviations: the model meta is the vanilla {@code Damage} tag read through
 * {@link ItemStack#getDamageValue()} (the same convention {@code modelpack.ExtraPackage} and
 * {@code TileEntityJoint#getModelItemDamage} use); 1.20.1 has no {@code func_150895_a} item hook, so
 * the subtype stacks are enumerated by the creative tab and sold by the model catalog;
 * {@code getColorFromItemStack} keeps its original
 * tint-0/tint-1 colour tables. The original's dead {@code complementary_color} helper (never called in
 * 1.12.2) was dropped.
 */
public class ItemBasicAddon extends Item implements IJBAddon.IJBAddonHolder, IColorHanldlerItem {
   public static final int BASIC_ADDON_PLUS_HEALTH_A = 0;
   public static final int BASIC_ADDON_PLUS_ENERGY_A = 2;
   public static final int BASIC_ADDON_PLUS_HEALTH_B = 4;
   public static final int BASIC_ADDON_PLUS_ENERGY_B = 6;
   public static final int BASIC_ADDON_PLUS_FWD_SPEED = 8;
   public static final int BASIC_ADDON_PLUS_SRF_SPEED = 10;
   public static final int BASIC_ADDON_PLUS_JUMP_POWER = 12;
   public static final int BASIC_ADDON_PLUS_STEP_HEIGHT = 14;
   public static final int BASIC_ADDON_PLUS_THRUSTER = 16;
   public static final int BASIC_ADDON_SUBTYPE_FLY = 30;
   public static final int BASIC_ADDON_SUBTYPE_HOVER = 32;
   public static final int BASIC_ADDON_SUBTYPE_EXFLY = 34;

   public AbstractBasicAddon addonPlusHealthA = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return 0.4F;
            case maxEnergy:
               return 0.2F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractBasicAddon addonPlusHealthB = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return 0.8F;
            case maxEnergy:
               return -0.4F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractBasicAddon addonPlusEnergyA = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return 0.2F;
            case maxEnergy:
               return 0.4F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractBasicAddon addonPlusEnergyB = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return -0.4F;
            case maxEnergy:
               return 0.8F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractBasicAddon addonPlusFwdSpeed = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return -0.1F;
            case maxEnergy:
            default:
               return 0.0F;
            case forwardSpeed:
               return 0.1F;
            case strafingSpeed:
               return -0.05F;
         }
      }
   };
   public AbstractBasicAddon addonPlusSrfSpeed = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return -0.1F;
            case maxEnergy:
            default:
               return 0.0F;
            case forwardSpeed:
               return -0.05F;
            case strafingSpeed:
               return 0.1F;
         }
      }
   };
   public AbstractBasicAddon addonPlusJumpPower = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxEnergy:
               return -0.05F;
            case forwardSpeed:
               return -0.02F;
            case strafingSpeed:
               return -0.02F;
            case jumpPower:
               return 0.2F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractBasicAddon addonPlusStepHeight = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return -0.1F;
            case maxEnergy:
               return -0.1F;
            case stepHeight:
               return 3.0F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractBasicAddon addonPlusThrusterPower = new AbstractBasicAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return -0.1F;
            case thrusterPower:
               return 0.3F;
            case energyHealInterval:
               return 3.0F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractSubTypeAddon addonSubTypeFly = new AbstractSubTypeAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return -0.4F;
            case maxEnergy:
               return -0.2F;
            case subTypeUnitId:
               return 3.0F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractSubTypeAddon addonSubTypeHover = new AbstractSubTypeAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return -0.4F;
            case maxEnergy:
               return -0.2F;
            case subTypeUnitId:
               return 4.0F;
            default:
               return 0.0F;
         }
      }
   };
   public AbstractSubTypeAddon addonSubTypeExFly = new AbstractSubTypeAddon() {
      @Override
      public float getBonusValue(IJBAddon.BonusType bonusType) {
         switch (bonusType) {
            case maxHealth:
               return -0.4F;
            case maxEnergy:
               return -0.2F;
            case subTypeUnitId:
               return 10.0F;
            default:
               return 0.0F;
         }
      }
   };

   public ItemBasicAddon() {
      super(new Item.Properties().stacksTo(1));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getColorFromItemStack(ItemStack itemStack, int tint) {
      if (tint == 0) {
         switch (itemStack.getDamageValue()) {
            case 0:
            case 4:
               return 16746530;
            case 2:
            case 6:
               return 6750139;
            case 8:
            case 10:
               return 5592575;
            case 12:
            case 14:
            case 16:
               return 6591981;
            case 30:
            case 32:
            case 34:
               return 13421772;
            default:
               return 16777215;
         }
      } else {
         switch (itemStack.getDamageValue()) {
            case 0:
               return 14540253;
            case 2:
               return 14540253;
            case 4:
               return 16776960;
            case 6:
               return 16776960;
            case 8:
               return 11184674;
            case 10:
               return 16776960;
            case 12:
               return 14540253;
            case 14:
               return 16776960;
            case 16:
               return 65280;
            case 30:
               return 16711680;
            case 32:
               return 65280;
            case 34:
               return 255;
            default:
               return 16777215;
         }
      }
   }

   @Override
   public Component getName(ItemStack stack) {
      String subNameKey;
      switch (stack.getDamageValue()) {
         case 0:
            subNameKey = "baddon.plushealtha";
            break;
         case 2:
            subNameKey = "baddon.plusenergya";
            break;
         case 4:
            subNameKey = "baddon.plushealthb";
            break;
         case 6:
            subNameKey = "baddon.plusenergyb";
            break;
         case 8:
            subNameKey = "baddon.plusfwdspeed";
            break;
         case 10:
            subNameKey = "baddon.plussrfspeed";
            break;
         case 12:
            subNameKey = "baddon.plusjumppower";
            break;
         case 14:
            subNameKey = "baddon.plusstepheight";
            break;
         case 16:
            subNameKey = "baddon.plusthruster";
            break;
         case 30:
            subNameKey = "baddon.subtypeheli";
            break;
         case 32:
            subNameKey = "baddon.subtypehover";
            break;
         case 34:
            subNameKey = "baddon.subtypeaircraft";
            break;
         default:
            return super.getName(stack);
      }

      return super.getName(stack).copy().append(Component.translatable(subNameKey));
   }

   @Override
   public IJBAddon getIJBAddon(int itemMeta) {
      switch (itemMeta) {
         case 0:
            return this.addonPlusHealthA;
         case 2:
            return this.addonPlusEnergyA;
         case 4:
            return this.addonPlusHealthB;
         case 6:
            return this.addonPlusEnergyB;
         case 8:
            return this.addonPlusFwdSpeed;
         case 10:
            return this.addonPlusSrfSpeed;
         case 12:
            return this.addonPlusJumpPower;
         case 14:
            return this.addonPlusStepHeight;
         case 16:
            return this.addonPlusThrusterPower;
         case 30:
            return this.addonSubTypeFly;
         case 32:
            return this.addonSubTypeHover;
         case 34:
            return this.addonSubTypeExFly;
         default:
            return null;
      }
   }

   private abstract class AbstractSubTypeAddon implements IJBAddon {
      private AbstractSubTypeAddon() {
      }

      @Override
      public IJBAddon.IJBAddonLimitKey getLimitKey() {
         return LimitKeySubType.instance;
      }

      @Override
      public int getCost() {
         return 1;
      }
   }

   private abstract class AbstractBasicAddon implements IJBAddon {
      private AbstractBasicAddon() {
      }

      @Override
      public IJBAddon.IJBAddonLimitKey getLimitKey() {
         return null;
      }

      @Override
      public int getCost() {
         return 1;
      }
   }
}
