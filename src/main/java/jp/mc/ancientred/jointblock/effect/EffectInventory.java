package jp.mc.ancientred.jointblock.effect;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBLegacyNames;
import jp.mc.ancientred.jointblock.item.ItemJBEffect;
import jp.mc.ancientred.jointblock.modelpack.ModelPackResources;
import jp.mc.ancientred.jointblock.registry.JBItems;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EffectInventory extends SimpleContainer {
   public static final int EFFECT_SLOT_NUM = 16;
   private WeakReference<TileEntityJointCore> inventoryHolderWeak;
   private static final String NBT_TAGNAME_EFFECTSLOTS_EXPORT = "jb:efcslt";
   private static final String NBT_TAGNAME_EFFECTBARY_EXPORT = "bary";

   public EffectInventory(TileEntityJointCore inventoryHolder, String inventoryTitle) {
      super(16);
      this.inventoryHolderWeak = new WeakReference(inventoryHolder);
   }

   public boolean stillValid(Player entityPlayer) {
      if (entityPlayer.isRemoved()) {
         return false;
      } else {
         TileEntityJointCore holder;
         return this.inventoryHolderWeak != null && (holder = (TileEntityJointCore)this.inventoryHolderWeak.get()) != null ? holder.isInventoryCurrentlyUsable(entityPlayer) : false;
      }
   }

   public boolean canPlaceItem(int index, ItemStack stack) {
      return stack.isEmpty() || stack.getItem() instanceof IJBSound.IJBSoundHolder;
   }

   public ListTag writeToNBT(ListTag tagList) {
      for(int idx = 0; idx < this.getContainerSize(); ++idx) {
         ItemStack itemstack = this.getItem(idx);
         if (!CompatHelper.isStackEmptyOrNull(itemstack)) {
            CompoundTag tagSlot = new CompoundTag();
            tagSlot.putByte("Slot", (byte)idx);
            itemstack.save(tagSlot);
            tagList.add(tagSlot);
         }
      }

      return tagList;
   }

   public void readFromNBT(ListTag tagList) {
      for(int i = 0; i < tagList.size(); ++i) {
         CompoundTag tagSlot = tagList.getCompound(i);
         int idx = tagSlot.getByte("Slot") & 255;
         if (idx >= 0 && idx < this.getContainerSize()) {
            this.setItem(idx, ItemStack.of(tagSlot));
         }
      }

   }

   public void writeEffectSlotDatasToExportNBT(CompoundTag tag) {
      EffectSlot[] effectSlots = new EffectSlot[16];
      this.inventoryItemToEffectSlot(effectSlots);

      try {
         byte[] bAryBuf = new byte[256];
         ByteBuffer bbuf = ByteBuffer.wrap(bAryBuf);
         ListTag tagList = new ListTag();

         for(int idx = 0; idx < effectSlots.length; ++idx) {
            EffectSlot effectSlot = effectSlots[idx];
            if (effectSlot != null) {
               CompoundTag tagSlot = new CompoundTag();
               tagSlot.putByte("Slot", (byte)idx);
               Item item = Item.byId(effectSlot.effectItemId);
               if (item != null) {
                  String namespacedItemName;
                  if (item == JBItems.JBEffectItem) {
                     namespacedItemName = "";
                  } else {
                     ResourceLocation namespaced = BuiltInRegistries.ITEM.getKey(item);
                     if (namespaced == null || StringUtil.isNullOrEmpty(namespacedItemName = namespaced.toString())) {
                        continue;
                     }

                     // 1.12.2 payloads stored addon items with the namespace stripped (":m_<pack>"),
                     // so keep that convention for the current addon namespace.
                     String addonPrefix = ModelPackResources.ITEM_NAMESPACE + ":";
                     if (namespacedItemName.startsWith(addonPrefix)) {
                        namespacedItemName = ":" + namespacedItemName.substring(addonPrefix.length());
                     }
                  }

                  bbuf.clear();
                  effectSlot.writeToBufferForExport(namespacedItemName, bbuf);
                  tagSlot.putByteArray("bary", Arrays.copyOf(bAryBuf, bbuf.position()));
                  tagList.add(tagSlot);
               }
            }
         }

         tag.put("jb:efcslt", tagList);
      } catch (Throwable var13) {
         var13.printStackTrace();
      }

   }

   public void readEffectSlotDatasFromImportNBT(CompoundTag tag) {
      if (tag.contains("jb:efcslt", 9)) {
         try {
            EffectSlot[] effectSlots = new EffectSlot[16];
            ListTag tagList = tag.getList("jb:efcslt", 10);
            new EffectSlot();

            for(int i = 0; i < tagList.size(); ++i) {
               CompoundTag tagSlot = tagList.getCompound(i);
               if (tagSlot.contains("bary", 7)) {
                  int idx = tagSlot.getByte("Slot") & 255;
                  if (idx >= 0 && idx < effectSlots.length) {
                     EffectSlot effectSlot = new EffectSlot();

                     try {
                        String namespacedItemName = effectSlot.readFromBufferForImport(ByteBuffer.wrap(tagSlot.getByteArray("bary")));
                        Item objItem;
                        if (StringUtil.isNullOrEmpty(namespacedItemName)) {
                           objItem = JBItems.JBEffectItem;
                        } else {
                           // 1.12.2 stored addon items namespace-stripped (":m_<pack>") and everything
                           // else under the old mod ids; JBLegacyNames expands both to this port's ids.
                           objItem = JBLegacyNames.itemByName(namespacedItemName);
                        }

                        if (objItem != null) {
                           effectSlot.effectItemId = BuiltInRegistries.ITEM.getId(objItem);
                           if (effectSlot.effectItemId != -1) {
                              effectSlots[idx] = effectSlot;
                           }
                        }
                     } catch (Throwable var12) {
                        var12.printStackTrace();
                     }
                  }
               }
            }

            this.effectSlotToInventoryItem(effectSlots);
         } catch (Throwable var13) {
            var13.printStackTrace();
         }

      }
   }

   public void inventoryItemToEffectSlot(EffectSlot[] effectSlots) {
      int i = 0;

      for(int len = this.getContainerSize(); i < len; ++i) {
         ItemStack stack = this.getItem(i);
         if (!CompatHelper.isStackEmptyOrNull(stack) && i < effectSlots.length) {
            Item item = stack.getItem();
            EffectSlot slot;
            if (item instanceof IJBSound.IJBSoundHolder) {
               slot = effectSlots[i] = new EffectSlot();
               slot.effectItemId = Item.getId(item);
               slot.effectItemMeta = (short)stack.getDamageValue();
               if (stack.hasTag()) {
                  EffectSlot.readFromNBT(stack.getTag(), slot, true);
               }

               slot.effectNameStr = "";
            } else if (item instanceof ItemJBEffect) {
               slot = effectSlots[i] = new EffectSlot();
               slot.effectItemId = Item.getId(item);
               slot.effectItemMeta = (short)stack.getDamageValue();
               if (stack.hasTag()) {
                  EffectSlot.readFromNBT(stack.getTag(), slot, true);
               }

               if (slot.effectItemMeta == 1) {
                  slot.setParticle(true);
               }
            }
         }
      }

   }

   public void effectSlotToInventoryItem(EffectSlot[] effectSlots) {
      int invSize = this.getContainerSize();

      for(int i = 0; i < effectSlots.length; ++i) {
         if (effectSlots[i] != null && i < invSize) {
            EffectSlot slot = effectSlots[i];
            Item item = Item.byId(slot.effectItemId);
            ItemStack stack;
            CompoundTag tag;
            if (item instanceof IJBSound.IJBSoundHolder) {
               stack = new ItemStack(item, 1);
               stack.setDamageValue(slot.effectItemMeta);
               tag = new CompoundTag();
               EffectSlot.writeToNBT(tag, slot, true);
               stack.setTag(tag);
               this.setItem(i, stack);
            } else if (item instanceof ItemJBEffect) {
               stack = new ItemStack(item, 1);
               stack.setDamageValue(slot.effectItemMeta);
               tag = new CompoundTag();
               EffectSlot.writeToNBT(tag, slot, true);
               stack.setTag(tag);
               this.setItem(i, stack);
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

   public TileEntityJointCore getInventoryHolderTileEntityJointCore() {
      TileEntityJointCore holder;
      return this.inventoryHolderWeak != null && (holder = (TileEntityJointCore)this.inventoryHolderWeak.get()) != null ? holder : null;
   }

   public static boolean isEffectItem(ItemStack stack) {
      if (CompatHelper.isStackEmptyOrNull(stack)) {
         return false;
      } else {
         Item item = stack.getItem();
         return item instanceof IJBSound.IJBSoundHolder || item instanceof ItemJBEffect;
      }
   }

   public static boolean isSoundItem(ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         return item instanceof IJBSound.IJBSoundHolder || item instanceof ItemJBEffect && stack.getDamageValue() == 0;
      }
   }

   public static boolean isParticleItem(ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         return item instanceof ItemJBEffect && stack.getDamageValue() == 1;
      }
   }

   public static IJBSound.IJBSoundHolder getJBSoundHolder(ItemStack stack) {
      return !CompatHelper.isStackEmptyOrNull(stack) && stack.getItem() instanceof IJBSound.IJBSoundHolder ? (IJBSound.IJBSoundHolder)stack.getItem() : null;
   }
}
