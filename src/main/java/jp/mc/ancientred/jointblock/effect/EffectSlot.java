package jp.mc.ancientred.jointblock.effect;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;

public class EffectSlot extends AbstractEffectAttributes {
   public static final int EFFECT_NAME_MAX_LEN = 64;
   public static final byte MC_EFFECT_VERSION_1_7_10 = 5;
   public static final byte MC_EFFECT_VERSION_1_12_2 = 10;
   public static final String NBT_SOUNDITEM_ID = "effecItemId";
   public static final String NBT_SOUNDITEM_META = "effecItemMeta";
   public static final String NBT_EFFECT_FLAGS = "effectFlags";
   public static final String NBT_VOLUME = "volume";
   public static final String NBT_PITCH = "pitch";
   public static final String NBT_REPEAT_DELAY = "repeatDelay";
   public static final String NBT_KEEP_DURATION = "keepDuration";
   public static final String NBT_FADE_DURATION = "fadeDuration";
   public static final String NBT_FADE_BACK_DURATION = "fadeBackDuration";
   public static final String NBT_EFFECT_NAMESTR = "effectNameStr";
   public static final int SOUND_STEP_SLOT_NUM = 0;
   public static final int SOUND_FALL_SLOT_NUM = 1;
   public static final int SOUND_HURT_SLOT_NUM = 2;
   public static final int SOUND_DEATH_SLOT_NUM = 3;
   public static final int SOUND_SWIM_SLOT_NUM = 4;
   public int effectItemId;
   public short effectItemMeta;
   public boolean hasInit;
   public boolean isEnabled;
   public String displayStr = "";

   public EffectSlot() {
      this.setDefaultValues();
   }

   public void setDefaultValues() {
      this.volume = 10;
      this.pitch = 10;
      this.soundFlags = 1;
      this.repeatDelay = 0;
      this.keepDuration = 10;
      this.fadeDuration = 0;
      this.fadeBackDuration = 0;
      this.effectNameStr = "";
      this.displayStr = "";
   }

   public boolean checkEnabled() {
      if (!this.hasInit) {
         this.setUpReference();
      }

      return this.isEnabled;
   }

   private void setUpReference() {
      this.hasInit = true;
      this.isEnabled = false;
      if (!StringUtil.isNullOrEmpty(this.effectNameStr)) {
         this.soundNameLoc = new ResourceLocation(this.effectNameStr);
         this.isEnabled = true;
      } else {
         try {
            Item item = BuiltInRegistries.ITEM.byId(this.effectItemId);
            if (item instanceof IJBSound.IJBSoundHolder) {
               this.sound = ((IJBSound.IJBSoundHolder)item).getIJBSound(this.effectItemMeta);
               this.isEnabled = this.sound != null;
            }
         } catch (Exception var3) {
            var3.printStackTrace();
         }

      }
   }

   public void readFromBuffer(ByteBuf bbuf, boolean minimize) {
      this.effectItemId = bbuf.readInt();
      this.effectItemMeta = bbuf.readShort();
      this.soundFlags = bbuf.readByte();
      this.volume = bbuf.readByte();
      this.pitch = (byte)Mth.clamp(bbuf.readByte(), 0, 20);
      if (!minimize || this.isRepeat()) {
         this.repeatDelay = (byte)Mth.clamp(bbuf.readByte(), 0, 100);
         this.keepDuration = (byte)Mth.clamp(bbuf.readByte(), 1, 100);
         this.fadeDuration = (byte)Mth.clamp(bbuf.readByte(), 0, 100);
         this.fadeBackDuration = (byte)Mth.clamp(bbuf.readByte(), 0, 100);
      }

      this.effectNameStr = JBCommonUtil.readLimitedLengthStrFromBuf((ByteBuf)bbuf, (byte)64, "");
   }

   public void writeToBuffer(ByteBuf bbuf, boolean minimize) {
      bbuf.writeInt(this.effectItemId);
      bbuf.writeShort(this.effectItemMeta);
      bbuf.writeByte(this.soundFlags);
      bbuf.writeByte(this.volume);
      bbuf.writeByte(this.pitch);
      if (!minimize || this.isRepeat()) {
         bbuf.writeByte(this.repeatDelay);
         bbuf.writeByte(this.keepDuration);
         bbuf.writeByte(this.fadeDuration);
         bbuf.writeByte(this.fadeBackDuration);
      }

      JBCommonUtil.writeLimitedLengthStrToBuf(this.effectNameStr, (ByteBuf)bbuf, (byte)64);
   }

   public String readFromBufferForImport(ByteBuffer bbuf) {
      byte format = bbuf.get();
      this.effectItemMeta = bbuf.getShort();
      this.soundFlags = bbuf.get();
      this.volume = bbuf.get();
      this.pitch = (byte)Mth.clamp(bbuf.get(), 0, 20);
      if (this.isRepeat()) {
         this.repeatDelay = (byte)Mth.clamp(bbuf.get(), 0, 100);
         this.keepDuration = (byte)Mth.clamp(bbuf.get(), 1, 100);
         this.fadeDuration = (byte)Mth.clamp(bbuf.get(), 0, 100);
         this.fadeBackDuration = (byte)Mth.clamp(bbuf.get(), 0, 100);
      }

      this.effectNameStr = JBCommonUtil.readLimitedLengthStrFromBuf((ByteBuffer)bbuf, (byte)64, "");
      return JBCommonUtil.readLimitedLengthStrFromBuf((ByteBuffer)bbuf, (byte)64, "");
   }

   public void writeToBufferForExport(String namespacedItemName, ByteBuffer bbuf) {
      bbuf.put((byte)10);
      bbuf.putShort(this.effectItemMeta);
      bbuf.put(this.soundFlags);
      bbuf.put(this.volume);
      bbuf.put(this.pitch);
      if (this.isRepeat()) {
         bbuf.put(this.repeatDelay);
         bbuf.put(this.keepDuration);
         bbuf.put(this.fadeDuration);
         bbuf.put(this.fadeBackDuration);
      }

      JBCommonUtil.writeLimitedLengthStrToBuf(this.effectNameStr, (ByteBuffer)bbuf, (byte)64);
      JBCommonUtil.writeLimitedLengthStrToBuf(namespacedItemName, (ByteBuffer)bbuf, (byte)64);
   }

   public static final void readEffectSlotsDataFromBBuf(EffectSlot[] soundSlots, ByteBuf bbuf) {
      for(int j = 0; j < soundSlots.length; ++j) {
         soundSlots[j] = null;
      }

      int count = bbuf.readByte();
      if (count > 16) {
         throw new RuntimeException("Illegal Packet");
      } else {
         for(int i = 0; i < count; ++i) {
            int index = bbuf.readByte();
            if (index >= 0 && index < soundSlots.length) {
               EffectSlot slot = new EffectSlot();
               slot.readFromBuffer(bbuf, true);
               soundSlots[index] = slot;
            }
         }

      }
   }

   public static final void writeEffectSlotsDataToBBuf(EffectSlot[] soundSlots, ByteBuf bbuf) {
      int count = 0;

      int i;
      for(i = 0; i < soundSlots.length; ++i) {
         if (soundSlots[i] != null) {
            ++count;
         }
      }

      bbuf.writeByte(count);

      for(i = 0; i < soundSlots.length; ++i) {
         if (soundSlots[i] != null) {
            bbuf.writeByte((byte)i);
            soundSlots[i].writeToBuffer(bbuf, true);
         }
      }

   }

   public static void readFromNBT(CompoundTag tag, EffectSlot slot, boolean ignoreItem) {
      if (!ignoreItem) {
         slot.effectItemId = tag.getInt("effecItemId");
         slot.effectItemMeta = tag.getShort("effecItemMeta");
      }

      slot.soundFlags = tag.getByte("effectFlags");
      slot.volume = tag.getByte("volume");
      slot.pitch = (byte)Mth.clamp(tag.getByte("pitch"), 0, 20);
      slot.repeatDelay = (byte)Mth.clamp(tag.getByte("repeatDelay"), 0, 100);
      slot.keepDuration = (byte)Mth.clamp(tag.getByte("keepDuration"), 1, 100);
      slot.fadeDuration = (byte)Mth.clamp(tag.getByte("fadeDuration"), 0, 100);
      slot.fadeBackDuration = (byte)Mth.clamp(tag.getByte("fadeBackDuration"), 0, 100);
      slot.effectNameStr = tag.getString("effectNameStr");
   }

   public static void writeToNBT(CompoundTag tag, EffectSlot slot, boolean ignoreItem) {
      if (!ignoreItem) {
         tag.putInt("effecItemId", slot.effectItemId);
         tag.putShort("effecItemMeta", slot.effectItemMeta);
      }

      tag.putByte("effectFlags", slot.soundFlags);
      tag.putByte("volume", slot.volume);
      tag.putByte("pitch", slot.pitch);
      tag.putByte("repeatDelay", slot.repeatDelay);
      tag.putByte("keepDuration", slot.keepDuration);
      tag.putByte("fadeDuration", slot.fadeDuration);
      tag.putByte("fadeBackDuration", slot.fadeBackDuration);
      if (!StringUtil.isNullOrEmpty(slot.effectNameStr)) {
         tag.putString("effectNameStr", slot.effectNameStr);
      }

   }

   public static void readEffectSlotsDataFromNBT(EffectSlot[] soundSlots, ListTag tagList) {
      for(int i = 0; i < tagList.size(); ++i) {
         CompoundTag tagSlot = tagList.getCompound(i);
         int j = tagSlot.getByte("Slot") & 255;
         if (j >= 0 && j < soundSlots.length) {
            soundSlots[j] = new EffectSlot();
            readFromNBT(tagSlot, soundSlots[j], false);
         }
      }

   }

   public static ListTag writeEffectSlotsDataToNBT(EffectSlot[] soundSlots, ListTag tagList) {
      for(int i = 0; i < soundSlots.length; ++i) {
         if (soundSlots[i] != null) {
            CompoundTag tagSlot = new CompoundTag();
            tagSlot.putByte("Slot", (byte)i);
            writeToNBT(tagSlot, soundSlots[i], false);
            tagList.add(tagSlot);
         }
      }

      return tagList;
   }

   public static boolean isSlotNumDefault(int slotNum) {
      switch (slotNum) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
            return true;
         default:
            return false;
      }
   }
}
