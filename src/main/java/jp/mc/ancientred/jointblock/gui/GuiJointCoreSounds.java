package jp.mc.ancientred.jointblock.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jointblock.api.IJBSound;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.effect.EffectHelper;
import jp.mc.ancientred.jointblock.effect.EffectInventory;
import jp.mc.ancientred.jointblock.effect.EffectSlot;
import jp.mc.ancientred.jointblock.effect.SoundHandlerHook;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.GuiCheckBoxNoShadow;
import jp.mc.ancientred.jointblock.gui.widget.GuiSliderShrink;
import jp.mc.ancientred.jointblock.gui.widget.GuiTabSetJoint;
import jp.mc.ancientred.jointblock.gui.widget.GuiTextFieldEx;
import jp.mc.ancientred.jointblock.gui.widget.GuiVerticalSlider;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.item.ItemJBEffect;
import jp.mc.ancientred.jointblock.packet.JBNetwork;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiJointCoreSounds extends GuiContainerWithTip<ContainerJointCoreSounds>
      implements GuiSliderShrink.GuiResponder, GuiSliderShrink.FormatHelper, ContainerJointCoreSounds.OnSlotItemChangedListener {
   private static final ResourceLocation resMainbg = new ResourceLocation("jointblock:textures/gui/guijointblock.png");
   private static final String STRMINUS = "-";
   private static final String STRPLUS = "+";
   private static DecimalFormat dFormat01 = new DecimalFormat("0.0");
   private static DecimalFormat dFormat001 = new DecimalFormat("0.00");
   private static final int MAXRESULTROWCOUNT = 8;
   public static final int ButtonBindNumPrevID = 2;
   public static final int ButtonBindNumNextID = 4;
   public static final int ButtonTestPlaySoundID = 6;
   public static final int CheckBoxAsDefaultID = 10;
   public static final int CheckBoxRepeatID = 12;
   public static final int CheckBoxDriverOnlyID = 14;
   public static final int CheckBoxUnManagedID = 16;
   public static final int SliderVolumeID = 20;
   public static final int SliderPitchID = 22;
   public static final int SliderDelayID = 24;
   public static final int SliderKeepID = 26;
   public static final int SliderFadeID = 28;
   public static final int SliderFadeBackID = 30;
   public static final int SliderVelocityID = 32;
   public static final int TextFieldSoundNameID = 40;
   public static final int SliderSoundListID = 99;
   private GuiTabSetJoint tabSetJoint;
   private GuiButtonJB ButtonBindNumPrev;
   private GuiButtonJB ButtonBindNumNext;
   private GuiButtonJB ButtonTestPlaySound;
   private GuiCheckBoxNoShadow CheckBoxAsDefault;
   private GuiCheckBoxNoShadow CheckBoxRepeat;
   private GuiCheckBoxNoShadow CheckBoxDriverOnly;
   private GuiCheckBoxNoShadow CheckBoxUnManaged;
   private GuiSliderShrink SliderVolume;
   private GuiSliderShrink SliderPitch;
   private GuiSliderShrink SliderDelay;
   private GuiSliderShrink SliderKeep;
   private GuiSliderShrink SliderFade;
   private GuiSliderShrink SliderFadeBack;
   private GuiSliderShrink SliderVelocity;
   private GuiTextFieldEx TextFieldSoundName;
   private boolean textFieldSoundNameEnabled = false;
   private GuiVerticalSlider SliderSoundList;
   private EffectHelper effectHelper = new EffectHelper();
   TileEntityJointCore te;
   private Player player;
   private EffectSlot currentSlotData = new EffectSlot();
   private EffectSlot[] slotsForTestPlay = new EffectSlot[16];

   public GuiJointCoreSounds(ContainerJointCoreSounds menu, Inventory playerInv) {
      super(menu, playerInv);
      this.te = menu.teCore;
      this.player = playerInv.player;
      this.imageHeight = 222;

      for(int i = 0; i < this.slotsForTestPlay.length; ++i) {
         this.slotsForTestPlay[i] = new EffectSlot();
      }

      this.getContainerSound().setSlotChangeListener(this);
   }

   public ContainerJointCoreSounds getContainerSound() {
      return this.menu;
   }

   @Override
   public void removed() {
      super.removed();
      // 1.12 disabled GLFW key repeat here: Keyboard.enableRepeatEvents(false). 1.20.1 removed that
      // API (key repeat is always delivered to screens), so there is nothing to turn off.
   }

   @Override
   protected void init() {
      super.init();
      int xLeft = (this.width - this.imageWidth) / 2;
      int yTop = (this.height - this.imageHeight) / 2;
      int dMax = 100;
      List<AbstractWidget> tabButtons = new ArrayList<>();
      this.tabSetJoint = new GuiTabSetJoint(tabButtons, this::actionPerformed, xLeft - 22, yTop, GuiTabSetJoint.TabSetType.core, 5, false);
      tabButtons.forEach(this::addRenderableWidget);
      this.CheckBoxAsDefault = new GuiCheckBoxNoShadow(CheckBoxAsDefaultID, xLeft + 90, yTop + 24, "", true, 0, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxAsDefault);
      int buttonWidth = 26;
      int buttonHeight = 20;
      int buttonShiftH = buttonWidth + 2;
      this.ButtonBindNumPrev = new GuiButtonJB(ButtonBindNumPrevID, xLeft + 112, yTop + 43, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.bindprev"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonBindNumPrev);
      this.ButtonBindNumNext = new GuiButtonJB(ButtonBindNumNextID, this.ButtonBindNumPrev.getX() + buttonShiftH, yTop + 43, buttonWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.bindnext"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonBindNumNext);
      this.TextFieldSoundName = new GuiTextFieldEx(TextFieldSoundNameID, this.font, xLeft + 11, yTop + 48, 90, 18);
      this.TextFieldSoundName.setTextColor(-1);
      this.TextFieldSoundName.setBordered(true);
      this.TextFieldSoundName.setMaxLength(64);
      this.TextFieldSoundName.setEditable(this.textFieldSoundNameEnabled);
      this.addRenderableWidget(this.TextFieldSoundName);
      int detailRowY = yTop + 70;
      int sliderWith = 74;
      int sliderHeight = 14;
      int sliderShiftW = sliderWith + 4;
      this.SliderVolume = new GuiSliderShrink(this, SliderVolumeID, xLeft + 11, detailRowY, sliderWith, sliderHeight, "volume", 0.0F, 1.0F, 1.0F, this);
      this.addRenderableWidget(this.SliderVolume);
      this.SliderPitch = new GuiSliderShrink(this, SliderPitchID, xLeft + 11 + sliderShiftW, detailRowY, sliderWith, sliderHeight, "pitch", 0.5F, 2.0F, 1.0F, this);
      this.addRenderableWidget(this.SliderPitch);
      this.SliderVelocity = new GuiSliderShrink(this, SliderVelocityID, xLeft + 11, detailRowY, sliderWith + sliderShiftW, sliderHeight, "velocity", 0.0F, 2.0F, 0.0F, this);
      this.addRenderableWidget(this.SliderVelocity);
      this.SliderVolume.leftClickModifyDigit = 0.1F;
      this.SliderPitch.leftClickModifyDigit = 0.1F;
      this.SliderVelocity.leftClickModifyDigit = 0.01F;
      detailRowY += sliderHeight + 2;
      buttonWidth = 60;
      this.CheckBoxDriverOnly = new GuiCheckBoxNoShadow(CheckBoxDriverOnlyID, xLeft + 12, detailRowY, CompatHelper.formatLang("jointGui.info.driveronly"), false, -1, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxDriverOnly);
      this.ButtonTestPlaySound = new GuiButtonJB(ButtonTestPlaySoundID, xLeft + 11 + sliderShiftW + sliderWith - buttonWidth, detailRowY, buttonWidth, 12, CompatHelper.formatLang("jointGui.info.testplaysound"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonTestPlaySound);
      detailRowY += 11;
      this.CheckBoxRepeat = new GuiCheckBoxNoShadow(CheckBoxRepeatID, xLeft + 12, detailRowY, CompatHelper.formatLang("jointGui.info.soundrepeat"), false, -1, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxRepeat);
      this.CheckBoxUnManaged = new GuiCheckBoxNoShadow(CheckBoxUnManagedID, xLeft + 11 + sliderShiftW, detailRowY, CompatHelper.formatLang("jointGui.info.unmanaged"), false, -1, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxUnManaged);
      detailRowY += 11;
      sliderShiftW = sliderWith + 4;
      this.SliderDelay = new GuiSliderShrink(this, SliderDelayID, xLeft + 11, detailRowY, sliderWith, sliderHeight, "delay", 0.0F, (float)dMax, 0.0F, this);
      this.addRenderableWidget(this.SliderDelay);
      this.SliderKeep = new GuiSliderShrink(this, SliderKeepID, xLeft + 11 + sliderShiftW, detailRowY, sliderWith, sliderHeight, "keep", 1.0F, (float)dMax, 10.0F, this);
      this.addRenderableWidget(this.SliderKeep);
      detailRowY += sliderHeight + 1;
      this.SliderFade = new GuiSliderShrink(this, SliderFadeID, xLeft + 11, detailRowY, sliderWith, sliderHeight, "fade", 0.0F, 100.0F, 0.0F, this);
      this.addRenderableWidget(this.SliderFade);
      this.SliderFadeBack = new GuiSliderShrink(this, SliderFadeBackID, xLeft + 11 + sliderShiftW, detailRowY, sliderWith, sliderHeight, "fadeback", 0.0F, (float)dMax, 0.0F, this);
      this.addRenderableWidget(this.SliderFadeBack);
      this.SliderSoundList = new GuiVerticalSlider(SliderSoundListID, xLeft + 151, yTop + 144 - 1, 10, 72, 0.0, 0.0, 0.0);
      this.addRenderableWidget(this.SliderSoundList);
      this.updateEffectSlotDataFromCurrentSlot(this.currentSlotData);
      this.setWidgetValuesFromCurrentSlot();
      this.updateSoundNameTextState(true);
      this.updateInputStates();
   }

   public void updateEffectSlotDataFromCurrentSlot(EffectSlot effectSlot) {
      Slot slot = this.menu.getSlot(0);
      ItemStack stack = slot.getItem();
      if (EffectInventory.isEffectItem(stack)) {
         CompoundTag tag = stack.getTag();
         if (tag != null) {
            EffectSlot.readFromNBT(tag, effectSlot, true);
         } else {
            effectSlot.setDefaultValues();
         }

         IJBSound.IJBSoundHolder soundHolder = EffectInventory.getJBSoundHolder(stack);
         if (soundHolder != null) {
            effectSlot.displayStr = stack.getHoverName().getString();
            effectSlot.effectNameStr = "";
         } else {
            effectSlot.displayStr = effectSlot.effectNameStr;
         }
      } else {
         effectSlot.setDefaultValues();
      }

   }

   public void updateSoundNameTextState(boolean forceCursorReset) {
      Slot slot = this.menu.getSlot(0);
      ItemStack stack = slot.getItem();
      boolean enabledOld = this.textFieldSoundNameEnabled;
      this.textFieldSoundNameEnabled = !CompatHelper.isStackEmptyOrNull(stack) && EffectInventory.getJBSoundHolder(stack) == null;
      boolean enabledChanged = enabledOld != this.textFieldSoundNameEnabled;
      if (enabledChanged) {
         this.TextFieldSoundName.setEditable(this.textFieldSoundNameEnabled);
      }

      if (forceCursorReset || enabledChanged || !this.textFieldSoundNameEnabled || !this.TextFieldSoundName.isFocused()) {
         this.TextFieldSoundName.setValue(this.currentSlotData.displayStr);
         this.TextFieldSoundName.setCursorPosition(0);
         this.TextFieldSoundName.setFocused(false);
      }

   }

   private void setWidgetValuesFromCurrentSlot() {
      this.SliderVolume.setSliderValue((float)this.currentSlotData.volume * 0.1F);
      this.SliderPitch.setSliderValue((float)this.currentSlotData.pitch * 0.1F);
      this.CheckBoxAsDefault.setIsChecked(this.currentSlotData.isAsDefault());
      this.CheckBoxDriverOnly.setIsChecked(this.currentSlotData.isDriverrOnly());
      this.CheckBoxRepeat.setIsChecked(this.currentSlotData.isRepeat());
      this.CheckBoxUnManaged.setIsChecked(this.currentSlotData.isUnManaged());
      this.SliderDelay.setSliderValue((float)this.currentSlotData.repeatDelay);
      this.SliderKeep.setSliderValue((float)this.currentSlotData.keepDuration);
      this.SliderFade.setSliderValue((float)this.currentSlotData.fadeDuration);
      this.SliderFadeBack.setSliderValue((float)this.currentSlotData.fadeBackDuration);
      this.SliderVelocity.setSliderValue((float)(this.currentSlotData.volume + 128) * 0.01F);
   }

   @Override
   public void onSlotItemChanged() {
      this.updateEffectSlotDataFromCurrentSlot(this.currentSlotData);
      this.setWidgetValuesFromCurrentSlot();
      this.updateSoundNameTextState(false);
      this.effectHelper.clearPlayingSounds();
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      // 1.12 advanced the text field cursor counter inside drawTextBox(); 1.20.1 moves that to
      // EditBox.tick().
      this.TextFieldSoundName.tick();
      this.updateInputStates();
   }

   private void updateInputStates() {
      this.SliderVolume.active = false;
      this.SliderVolume.visible = false;
      this.SliderPitch.active = false;
      this.SliderPitch.visible = false;
      this.SliderVelocity.active = false;
      this.SliderVelocity.visible = false;
      this.CheckBoxAsDefault.visible = false;
      this.CheckBoxAsDefault.active = false;
      this.CheckBoxDriverOnly.active = false;
      this.CheckBoxDriverOnly.visible = false;
      this.CheckBoxRepeat.active = false;
      this.CheckBoxRepeat.visible = false;
      this.CheckBoxUnManaged.active = false;
      this.CheckBoxUnManaged.visible = false;
      this.SliderDelay.active = false;
      this.SliderDelay.visible = false;
      this.SliderKeep.active = false;
      this.SliderKeep.visible = false;
      this.SliderFade.active = false;
      this.SliderFade.visible = false;
      this.SliderFadeBack.active = false;
      this.SliderFadeBack.visible = false;
      this.ButtonTestPlaySound.active = false;
      this.ButtonTestPlaySound.visible = false;
      int slotNum = this.getContainerSound().getDummySlotPointerIndex();
      if (EffectSlot.isSlotNumDefault(slotNum)) {
         this.CheckBoxAsDefault.visible = true;
         this.CheckBoxAsDefault.setMessage(Component.literal(CompatHelper.formatLang("jointGui.info.effectslotas", this.getAsDefaultDisplayString(slotNum))));
      }

      Slot slot = this.menu.getSlot(0);
      ItemStack stack = slot.getItem();
      if (EffectInventory.isSoundItem(stack)) {
         this.SliderVolume.active = true;
         this.SliderVolume.visible = true;
         this.SliderPitch.active = true;
         this.SliderPitch.visible = true;
         this.CheckBoxAsDefault.active = true;
         this.CheckBoxDriverOnly.visible = true;
         this.CheckBoxRepeat.visible = true;
         this.CheckBoxUnManaged.visible = true;
         if (this.CheckBoxAsDefault.visible) {
            this.CheckBoxDriverOnly.active = !this.CheckBoxAsDefault.isChecked();
            this.CheckBoxRepeat.active = !this.CheckBoxAsDefault.isChecked();
            this.CheckBoxUnManaged.active = !this.CheckBoxAsDefault.isChecked();
         } else {
            this.CheckBoxDriverOnly.active = true;
            this.CheckBoxRepeat.active = true;
            this.CheckBoxUnManaged.active = true;
         }

         if (this.CheckBoxRepeat.active && this.CheckBoxRepeat.isChecked()) {
            this.SliderDelay.active = true;
            this.SliderDelay.visible = true;
            this.SliderKeep.active = true;
            this.SliderKeep.visible = true;
            this.SliderFade.active = true;
            this.SliderFade.visible = true;
            this.SliderFadeBack.active = true;
            this.SliderFadeBack.visible = true;
         }

         this.ButtonTestPlaySound.active = true;
         this.ButtonTestPlaySound.visible = true;
      } else if (EffectInventory.isParticleItem(stack)) {
         this.SliderVelocity.active = true;
         this.SliderVelocity.visible = true;
         this.CheckBoxAsDefault.visible = false;
      }

      int maxRowIndex = this.getContainerSound().getMaxRowIndex();
      maxRowIndex -= 3;
      if (maxRowIndex < 0) {
         this.SliderSoundList.maxValue = 0.0;
         this.SliderSoundList.active = false;
      } else {
         this.SliderSoundList.active = true;
         this.SliderSoundList.maxValue = (double)maxRowIndex / 4.0;
         this.SliderSoundList.minValue = 0.0;
      }

      int soundListStartIdx = (int)(this.SliderSoundList.getValue() * 4.0) * 8;
      this.getContainerSound().setDummyListPointerOffset(soundListStartIdx);
      this.effectHelper.posX = this.player.getX();
      this.effectHelper.posY = this.player.getY();
      this.effectHelper.posZ = this.player.getZ();
   }

   @Override
   public void onToggle(int id, boolean value) {
   }

   @Override
   public void setEntryValue(int id, String value) {
   }

   @Override
   public void onTick(int id, float value) {
      switch (id) {
         case SliderVolumeID:
         case SliderPitchID:
         case SliderDelayID:
         case SliderKeepID:
         case SliderFadeID:
         case SliderFadeBackID:
         case SliderVelocityID:
            this.sendGUIValuesToServer(id);
         case 21:
         case 23:
         case 25:
         case 27:
         case 29:
         case 31:
         default:
      }
   }

   private void sendGUIValuesToServer(int id) {
      if (this.player != null) {
         EffectSlot slotData = this.collectEffectSlotDataFromGUI();
         JBNetwork.sendToServer(JBPacketSender.createSoundGUIActPacket(this.menu.containerId, id, this.getContainerSound().getDummySlotPointerIndex(), slotData));
      }

   }

   private EffectSlot collectEffectSlotDataFromGUI() {
      if (this.SliderVelocity.active) {
         this.currentSlotData.volume = (byte)(Math.round(this.SliderVelocity.getSliderValue() * 100.0F) - 128);
      } else {
         this.currentSlotData.volume = (byte)Math.round(this.SliderVolume.getSliderValue() * 10.0F);
      }

      this.currentSlotData.pitch = (byte)Math.round(this.SliderPitch.getSliderValue() * 10.0F);
      this.currentSlotData.setAsDefault(this.CheckBoxAsDefault.visible && this.CheckBoxAsDefault.isChecked());
      this.currentSlotData.setDriverrOnly(this.CheckBoxDriverOnly.isChecked() && (!this.CheckBoxAsDefault.visible || !this.CheckBoxAsDefault.isChecked()));
      this.currentSlotData.setRepeat(this.CheckBoxRepeat.isChecked() && (!this.CheckBoxAsDefault.visible || !this.CheckBoxAsDefault.isChecked()));
      this.currentSlotData.setUnManaged(this.CheckBoxUnManaged.isChecked() && (!this.CheckBoxAsDefault.visible || !this.CheckBoxAsDefault.isChecked()));
      this.currentSlotData.repeatDelay = (byte)Math.round(this.SliderDelay.getSliderValue());
      this.currentSlotData.keepDuration = (byte)Math.round(this.SliderKeep.getSliderValue());
      this.currentSlotData.fadeDuration = (byte)Math.round(this.SliderFade.getSliderValue());
      this.currentSlotData.fadeBackDuration = (byte)Math.round(this.SliderFadeBack.getSliderValue());
      return this.currentSlotData;
   }

   @Override
   public String getText(int id, String name, float value) {
      switch (id) {
         case SliderVolumeID:
            return CompatHelper.formatLang("jointGui.info.soundvolume", dFormat01.format((double)value));
         case 21:
         case 23:
         case 25:
         case 27:
         case 29:
         case 31:
         default:
            return null;
         case SliderPitchID:
            return CompatHelper.formatLang("jointGui.info.soundpitch", dFormat01.format((double)value));
         case SliderDelayID:
            return CompatHelper.formatLang("jointGui.info.sounddelay", Math.round(value));
         case SliderKeepID:
            return CompatHelper.formatLang("jointGui.info.soundkeep", Math.round(value));
         case SliderFadeID:
            return CompatHelper.formatLang("jointGui.info.soundfade", Math.round(value));
         case SliderFadeBackID:
            return CompatHelper.formatLang("jointGui.info.soundfadeback", Math.round(value));
         case SliderVelocityID:
            return CompatHelper.formatLang("jointGui.info.effectvelocity", dFormat001.format((double)value));
      }
   }

   private String getAsDefaultDisplayString(int slotNum) {
      switch (slotNum) {
         case 0:
            return CompatHelper.formatLang("jointGui.info.soundstep");
         case 1:
            return CompatHelper.formatLang("jointGui.info.soundfall");
         case 2:
            return CompatHelper.formatLang("jointGui.info.soundhurt");
         case 3:
            return CompatHelper.formatLang("jointGui.info.sounddeath");
         case 4:
            return CompatHelper.formatLang("jointGui.info.soundswim");
         default:
            return "";
      }
   }

   @Override
   protected void actionPerformed(IJBGuiWidget guiButton) {
      int id = guiButton.getId();
      if (id != SliderVolumeID && id != SliderPitchID && id != SliderDelayID && id != SliderKeepID && id != SliderFadeID && id != SliderFadeBackID) {
         if (id == ButtonBindNumPrevID) {
            this.getContainerSound().setDummySlotPointerToPrev();
            this.updateEffectSlotDataFromCurrentSlot(this.currentSlotData);
            this.setWidgetValuesFromCurrentSlot();
            this.updateSoundNameTextState(true);
         } else if (id == ButtonBindNumNextID) {
            this.getContainerSound().setDummySlotPointerToNext();
            this.updateEffectSlotDataFromCurrentSlot(this.currentSlotData);
            this.setWidgetValuesFromCurrentSlot();
            this.updateSoundNameTextState(true);
         } else if (id == ButtonTestPlaySoundID) {
            this.testPlaySound();
         } else if (id != CheckBoxAsDefaultID && id != CheckBoxRepeatID && id != CheckBoxUnManagedID && id != CheckBoxDriverOnlyID) {
            if (this.player != null) {
               JBNetwork.sendToServer(JBPacketSender.createGUIActPacket(id, 0, 0, null));
            }

         } else {
            if (id == CheckBoxRepeatID && this.CheckBoxRepeat.isChecked()) {
               this.CheckBoxUnManaged.setIsChecked(false);
            }

            if (id == CheckBoxUnManagedID && this.CheckBoxUnManaged.isChecked()) {
               this.CheckBoxRepeat.setIsChecked(false);
            }

            this.sendGUIValuesToServer(id);
            this.effectHelper.clearPlayingSounds();
         }
      } else {
         this.effectHelper.clearPlayingSounds();
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      boolean handled = super.mouseScrolled(mouseX, mouseY, delta);
      this.handleMouseWheelInput(mouseX, mouseY, (int)delta);
      return handled;
   }

   private void handleMouseWheelInput(double mouseX, double mouseY, int dWheel) {
      if (this.SliderSoundList.visible && this.SliderSoundList.maxValue != 0.0) {
         int Lft = (this.width - this.imageWidth) / 2;
         int Top = (this.height - this.imageHeight) / 2;
         int yPos = 144;
         if (mouseX >= Lft + 7 && mouseX < Lft + this.imageWidth - 7 && mouseY >= Top + yPos && mouseY < Top + yPos + 72) {
            this.SliderSoundList.handleSliderWheelScroll(dWheel, 8.0);
         }
      }

   }

   private Slot getSlotAtPosition(int x, int y) {
      for(int i = 0; i < this.menu.slots.size(); ++i) {
         Slot slot = this.menu.slots.get(i);
         if (this.isMouseOverSlot(slot, x, y) && slot.isActive()) {
            if (!(slot instanceof ContainerJointCoreSounds.PointerSlot)) {
               return slot;
            }

            if (((ContainerJointCoreSounds.PointerSlot)slot).isValidPointer()) {
               return ((ContainerJointCoreSounds.PointerSlot)slot).getSlot();
            }
         }
      }

      return null;
   }

   private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
      return this.isHovering(slotIn.x, slotIn.y, 16, 16, (double)mouseX, (double)mouseY);
   }

   @Override
   protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
      if (slotIn != null && slotIn instanceof ContainerJointCoreSounds.PointerSlot && ((ContainerJointCoreSounds.PointerSlot)slotIn).isValidPointer()) {
         slotId = ((ContainerJointCoreSounds.PointerSlot)slotIn).getSlot().index;
      }

      Minecraft.getInstance().gameMode.handleInventoryMouseClick(this.menu.containerId, slotId, mouseButton, type, Minecraft.getInstance().player);
   }

   private boolean isSliderLeftClick(int x, int y, int mouseButton) {
      if (mouseButton != 1) {
         return false;
      } else {
         return this.SliderVolume.mouseLeftPressed((double)x, (double)y) || this.SliderPitch.mouseLeftPressed((double)x, (double)y) || this.SliderDelay.mouseLeftPressed((double)x, (double)y) || this.SliderKeep.mouseLeftPressed((double)x, (double)y) || this.SliderFade.mouseLeftPressed((double)x, (double)y) || this.SliderFadeBack.mouseLeftPressed((double)x, (double)y) || this.SliderVelocity.mouseLeftPressed((double)x, (double)y);
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
      if (this.isSliderLeftClick((int)mouseX, (int)mouseY, mouseButton)) {
         return true;
      } else {
         // 1.12 additionally forwarded the click to TextFieldSoundName when it was enabled; the
         // 1.20.1 edit box is a registered widget, so it receives the click itself and only focuses
         // while it is editable.
         return super.mouseClicked(mouseX, mouseY, mouseButton);
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      String oldName = this.TextFieldSoundName.getValue();
      boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
      this.sendSoundNameChangedIfNeeded(oldName, handled);
      return handled;
   }

   @Override
   public boolean charTyped(char typedChar, int modifiers) {
      String oldName = this.TextFieldSoundName.getValue();
      boolean handled = super.charTyped(typedChar, modifiers);
      this.sendSoundNameChangedIfNeeded(oldName, handled);
      return handled;
   }

   private void sendSoundNameChangedIfNeeded(String oldName, boolean textFieldHandled) {
      if (textFieldHandled) {
         String newName = this.TextFieldSoundName.getValue();
         if (!oldName.equals(newName)) {
            JBPacketSender.sendJointGuiStringChanged(newName, (byte)30, this.getContainerSound().getDummySlotPointerIndex());
         }
      }

   }

   private void testPlaySound() {
      Slot slot = this.menu.getSlot(0);
      WeighedSoundEvents accessor = null;
      if (slot != null && EffectInventory.isSoundItem(slot.getItem())) {
         ItemStack stack = slot.getItem();
         Item item = stack.getItem();
         if (item instanceof IJBSound.IJBSoundHolder) {
            IJBSound.IJBSoundHolder soundHolder = (IJBSound.IJBSoundHolder)item;
            IJBSound sound = soundHolder.getIJBSound(stack.getDamageValue());
            if (sound != null) {
               accessor = sound.getSoundAccessor();
            }
         } else if (item instanceof ItemJBEffect) {
            String soundNameStr = ItemJBEffect.getSoundNameStrFromStackNBT(stack);
            if (!StringUtil.isNullOrEmpty(soundNameStr)) {
               accessor = Minecraft.getInstance().getSoundManager().getSoundEvent(new ResourceLocation(soundNameStr));
            }
         }
      }

      if (accessor != null) {
         int idx = this.getContainerSound().getDummySlotPointerIndex();
         if (idx >= 0 && idx < this.slotsForTestPlay.length) {
            EffectSlot soundSlot = this.slotsForTestPlay[idx];
            EffectHelper helper = this.effectHelper;
            this.updateEffectSlotDataFromCurrentSlot(soundSlot);
            if (soundSlot.isUnManaged()) {
               helper = new EffectHelper(helper);
            }

            SoundHandlerHook.playJBSound(soundSlot, accessor, helper);
         }
      }
   }

   /** 1.12.2 {@code func_146976_a} (drawGuiContainerBackgroundLayer). */
   @Override
   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int lft = this.leftPos;
      int top = this.topPos;
      guiGraphics.blit(resMainbg, lft, top, 0, 0, this.imageWidth, this.imageHeight);
      guiGraphics.blit(resMainbg, lft + 148, top + 21, 176, 0, 20, 20);
      guiGraphics.blit(resMainbg, lft + 106, top + 41, 176, 20, 63, 28);
      guiGraphics.blit(resMainbg, lft + 63, top + 18, 68, 18, 4, 24);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int yPos = 144;
      guiGraphics.fill(lft + 7, top + yPos - 5, lft + 169, top + 215, -3750202);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.blit(resMainbg, lft + 7, top + 144 - 5, 7, 175, 162, 1);

      for(int i = 0; i < 4; ++i) {
         guiGraphics.blit(resMainbg, lft + 7, top + yPos - 1 + i * 18, 7, 197, 144, 18);
      }

   }

   /** 1.12.2 {@code func_146979_b} (drawGuiContainerForegroundLayer). */
   @Override
   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderLabels(guiGraphics, mouseX, mouseY);
      int slotNum = this.getContainerSound().getDummySlotPointerIndex();
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.jointcoreeffects"), 8, 6, 4210752, false);
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.effectslot", slotNum), 29, 26, 4210752, false);
   }

   /**
    * 1.12.2 {@code func_73863_a} (drawScreen) override.  1.12 did
    * {@code super.func_73863_a} (which already draws the background, widgets, labels and the hover
    * tip), then disabled the standard item lighting, drew {@code TextFieldSoundName.drawTextBox()},
    * re-rendered the hovered tooltip and re-enabled the lighting.
    *
    * <p>In 1.20.1 {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen#render}
    * already draws the background layers, widgets, slot highlight, labels and the hover tip, and the
    * sound-name edit box is a registered widget, so it is drawn by {@code super.render}.  The only
    * remaining 1.12 extra is the second hover-tip render, which is reproduced here; the
    * item-lighting toggles have no 1.20.1 counterpart and are dropped.
    */
   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }
}
