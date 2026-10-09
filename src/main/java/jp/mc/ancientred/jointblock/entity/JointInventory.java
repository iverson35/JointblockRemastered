package jp.mc.ancientred.jointblock.entity;

import java.lang.ref.WeakReference;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.assemble.AddonBonus;
import jp.mc.ancientred.jointblock.assemble.AssembleInfo;
import jp.mc.ancientred.jointblock.assemble.ring.RingProgram;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import jp.mc.ancientred.jointblock.registry.JBItems;

public class JointInventory extends SimpleContainer {
   public static final int FUEL_SLOT_NUM = 5;
   public static final int FUEL_SLOT_START = 45;
   public static final int RING_SLOT_NUM = 4;
   public static final int RING_SLOT_START = 50;
   public static final int ADDON_SLOT_NUM = 9;
   public static final int ADDON_SLOT_START = 54;
   public static final int UNIT_INVENTORY_NUM = 63;
   public static final String RING_P00 = "rp00";
   public static final String RING_P01 = "rp01";
   public static final String RING_P02 = "rp02";
   public static final String RING_P03 = "rp03";
   public static final String[] NBT_RING_PKEYS = new String[]{"rp00", "rp01", "rp02", "rp03"};
   private WeakReference<IJointInventoryHolder> inventoryHolderWeak;
   private float consumedCashe = 0.0F;

   public JointInventory(IJointInventoryHolder inventoryHolder, String inventoryTitle, int slotCount) {
      super(slotCount);
      this.inventoryHolderWeak = new WeakReference(inventoryHolder);
   }

   public void setNewHolder(IJointInventoryHolder inventoryHolder) {
      this.consumedCashe = 0.0F;
      this.inventoryHolderWeak = new WeakReference(inventoryHolder);
   }

   public float getConsumedCashe() {
      return this.consumedCashe;
   }

   public void setConsumedCashe(float value) {
      this.consumedCashe = value;
   }

   public boolean stillValid(Player entityPlayer) {
      if (entityPlayer.isRemoved()) {
         return false;
      } else {
         IJointInventoryHolder holder;
         return this.inventoryHolderWeak != null && (holder = (IJointInventoryHolder)this.inventoryHolderWeak.get()) != null ? holder.isInventoryCurrentlyUsable(entityPlayer) : false;
      }
   }

   public TileEntityJointCore getInventoryHolderTileEntityJointCore() {
      IJointInventoryHolder holder;
      return this.inventoryHolderWeak != null && (holder = (IJointInventoryHolder)this.inventoryHolderWeak.get()) != null && holder instanceof TileEntityJointCore ? (TileEntityJointCore)holder : null;
   }

   public EntityJointed getInventoryHolderEntityJointed() {
      IJointInventoryHolder holder;
      return this.inventoryHolderWeak != null && (holder = (IJointInventoryHolder)this.inventoryHolderWeak.get()) != null && holder instanceof EntityJointed ? (EntityJointed)holder : null;
   }

   public ListTag writeToNBT(ListTag tagList) {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         ItemStack itemstack = this.getItem(i);
         if (!CompatHelper.isStackEmptyOrNull(itemstack)) {
            CompoundTag tagSlot = new CompoundTag();
            tagSlot.putByte("Slot", (byte)i);
            itemstack.save(tagSlot);
            tagList.add(tagSlot);
         }
      }

      return tagList;
   }

   public void readFromNBT(ListTag tagList) {
      for(int i = 0; i < tagList.size(); ++i) {
         CompoundTag each = tagList.getCompound(i);
         if (each.contains("Slot", 1)) {
            int j = each.getByte("Slot") & 255;
            if (j >= 0 && j < this.getContainerSize()) {
               this.setItem(j, ItemStack.of(each));
            }
         }
      }

   }

   public static boolean isTileEntityInventory(Container lowerChestInventory) {
      return lowerChestInventory instanceof JointInventory && ((JointInventory)lowerChestInventory).getInventoryHolderTileEntityJointCore() != null;
   }

   public float getFuelValue(boolean withCache) {
      int fuelValueSum = 0;

      for(int i = 45; i < 50; ++i) {
         ItemStack stack = this.getItem(i);
         if (!CompatHelper.isStackEmptyOrNull(stack)) {
            fuelValueSum += ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) * stack.getCount();
         }
      }

      return (float)fuelValueSum * JBConfig.unitFuelEfficiencyMult + (withCache ? this.consumedCashe : 0.0F);
   }

   public void consumespendFuelUntil(float eFuelPool) {
      float invSumValue = this.getFuelValue(true);
      float consumeNeeded = invSumValue - eFuelPool;
      consumeNeeded -= this.consumedCashe;

      label34:
      while(consumeNeeded > 0.0F) {
         int i = 49;

         while(true) {
            if (i < 45) {
               break label34;
            }

            ItemStack stack = this.getItem(i);
            if (!CompatHelper.isStackEmptyOrNull(stack)) {
               int burnTime = ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
               if (burnTime > 0 && stack.getCount() > 0) {
                  consumeNeeded -= (float)burnTime * JBConfig.unitFuelEfficiencyMult;
                  if (stack.getItem() == Items.LAVA_BUCKET && stack.getCount() == 1) {
                     this.setItem(i, new ItemStack(Items.BUCKET));
                     break;
                  }

                  stack.shrink(1);
                  if (stack.isEmpty()) {
                     this.setItem(i, ItemStack.EMPTY);
                  }
                  break;
               }
            }

            --i;
         }
      }

      this.consumedCashe = Math.max(0.0F, -consumeNeeded);
   }

   public boolean hasRINGInSlots() {
      for(int i = 50; i < 54; ++i) {
         if (ItemRingProgram.isRingProgramItem(this.getItem(i))) {
            return true;
         }
      }

      return false;
   }

   public void collectAddonInvItems(AssembleInfo asmInf) {
      AddonBonus addonBonus = asmInf.addonBonus;

      for(int i = 54; i < 63; ++i) {
         ItemStack stack = this.getItem(i);
         if (!CompatHelper.isStackEmptyOrNull(stack)) {
            Item item = stack.getItem();
            if (item != null && item instanceof IJBAddon.IJBAddonHolder && !(item instanceof IJBModelItem)) {
               int itemMeta = stack.getDamageValue();
               IJBAddon addon = ((IJBAddon.IJBAddonHolder)item).getIJBAddon(itemMeta);
               if (addon != null) {
                  ++addonBonus.foundCount;
                  IJBAddon.IJBAddonLimitKey limitKey = addon.getLimitKey();
                  if (limitKey != null) {
                     if (addonBonus.canApplyModelAddon(addon, limitKey)) {
                        addonBonus.applyAddon(addon, Item.getId(item), itemMeta);
                     } else {
                        ++addonBonus.ignoredCount;
                     }
                  } else {
                     addonBonus.applyAddon(addon, Item.getId(item), itemMeta);
                  }
               }
            }
         }
      }

   }

   public void writeRINGDatasToExportNBT(CompoundTag tag) {
      for(int i = 50; i < 54; ++i) {
         if (ItemRingProgram.isRingProgramItem(this.getItem(i))) {
            byte[] ringData = ItemRingProgram.getRingProgramData(this.getItem(i));
            int ringNum = i - 50;
            if (ringData != null && ringData.length > 0 && ringNum >= 0 && ringNum < NBT_RING_PKEYS.length) {
               tag.putByteArray(NBT_RING_PKEYS[ringNum], ringData);
            }
         }
      }

   }

   public void readRINGDatasFromImportNBT(CompoundTag tag) {
      for(int i = 50; i < 54; ++i) {
         int ringNum = i - 50;
         if (ringNum >= 0 && ringNum < NBT_RING_PKEYS.length && tag.contains(NBT_RING_PKEYS[ringNum], 7)) {
            byte[] ringData = tag.getByteArray(NBT_RING_PKEYS[ringNum]);
            if (ringData != null && ringData.length != 0) {
               RingProgram pgmTest = RingProgram.readFromByteArray(ringData);
               if (!pgmTest.hadError) {
                  ItemStack itemStack = new ItemStack(JBItems.RingProgramItem);
                  CompoundTag newTag = new CompoundTag();
                  newTag.putByteArray("jb:ringp", ringData);
                  String pageZeroName = pgmTest.getRingZeroName();
                  if (pageZeroName != null && pageZeroName.length() > 0) {
                     newTag.putString("jb:ringnm", pageZeroName);
                  }

                  itemStack.setTag(newTag);
                  this.setItem(i, itemStack);
               }
            }
         }
      }

   }

   public void setItem(int slotNum, ItemStack itemStack) {
      super.setItem(slotNum, itemStack);
      TileEntityJointCore jbCore = this.getInventoryHolderTileEntityJointCore();
      if (jbCore != null) {
         jbCore.setChanged();
      }

   }

   public interface IJointInventoryHolder {
      boolean isInventoryCurrentlyUsable(Player player);
   }
}
