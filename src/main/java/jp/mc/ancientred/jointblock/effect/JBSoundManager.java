package jp.mc.ancientred.jointblock.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.api.IJBSoundManager;
import jp.mc.ancientred.jointblock.registry.JBItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public class JBSoundManager implements IJBSoundManager {
   public static final SoundSource JB_SOUND_CATEGORY;
   public final Map<String, IJBSound> soundMap = new HashMap();
   public final ArrayList<ItemStack> defaultEffectItemList = new ArrayList();
   public final ArrayList<ItemStack> importedSoundItemList = new ArrayList();

   public void registerSound(String name, IJBSound sound) {
      this.soundMap.put(name, sound);
   }

   public ArrayList<ItemStack> getImportedSoundItemList() {
      return this.importedSoundItemList;
   }

   public void setupDefaultItemList() {
      EffectSlot slot = new EffectSlot();
      this.defaultEffectItemList.add(this.createDefaultItem(slot, "block.stone.break", 0));
      this.defaultEffectItemList.add(this.createDefaultItem(slot, "entity.cat.ambient", 0));
      this.defaultEffectItemList.add(this.createDefaultItem(slot, "entity.chicken.ambient", 0));
      this.defaultEffectItemList.add(this.createDefaultItem(slot, "angryVillager", 1));
      this.defaultEffectItemList.add(this.createDefaultItem(slot, "largesmoke", 1));
      this.defaultEffectItemList.add(this.createDefaultItem(slot, "spell", 1));
   }

   private ItemStack createDefaultItem(EffectSlot slot, String soundNameStr, int meta) {
      ItemStack stack = new ItemStack(JBItems.JBEffectItem);
      stack.setDamageValue(meta);
      CompoundTag tag = new CompoundTag();
      slot.setDefaultValues();
      if (meta == 1) {
         slot.volume = -128;
      }

      EffectSlot.writeToNBT(tag, slot, true);
      tag.putString("effectNameStr", soundNameStr);
      stack.setTag(tag);
      return stack;
   }

   static {
      JB_SOUND_CATEGORY = SoundSource.HOSTILE;
   }
}
