package jp.mc.ancientred.jointblock.gui;

import java.util.ArrayList;
import java.util.List;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.effect.EffectInventory;
import jp.mc.ancientred.jointblock.effect.EffectSlot;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12.2 {@code ContainerJointCoreSounds} port.
 *
 * <p>The sound page keeps 16 "effect slots" of the core's {@code effectChest} and a
 * 32-entry list of the sound items offered by the sound manager.  All of those are
 * pinned in place by {@link PointerSlot}/{@link DisabledSlot} wrappers that forward to
 * the real slot, which is how the 1.12.2 screen addressed them by index.
 *
 * <p>Omitted: the 1.12.2 {@code detectAndSendChanges()}/{@code func_75142_b()} override.
 * It re-implemented vanilla's slot-change detection/broadcast for the effect+sound slots;
 * in 1.20.1 the fields it needed ({@code lastSlots}, {@code remoteSlots},
 * {@code containerListeners}) are private, and the public
 * {@link #broadcastChanges()} performs the same detection and listener notification for
 * every slot, so the call sites use {@code broadcastChanges()} instead (the exact
 * partial-range optimisation is not reproduced).
 */
public class ContainerJointCoreSounds extends AbstractContainerJointCore implements IContainerActionReceiver {

    public static final int SOUND_SLOT_LEFT = 11;
    public static final int INV_YPOS = 144;
    public static final int INV_XPOS = 7;
    public static final int DUMMY_INV_LIST_ROWS = 4;
    public static final int DUMMY_INV_LIST_COLS = 8;
    private static final int DUMMY_LIST_NUM = 32;
    private static final int DUMMY_INV_TOTAL_NUM = 33;
    private static final SimpleContainer dummyInventory = new SimpleContainer(DUMMY_INV_TOTAL_NUM);
    private static SimpleContainer soundListInventory;
    private int[] dummySlotPointer = new int[1];
    private int[] dummyListPointerOffset = new int[1];
    private int actualEffectSlotStart;
    private int actualSoundListStart;

    public ContainerJointCoreSounds(int containerId, Inventory playerIn, TileEntityJointCore teCoreIn) {
        super(JBMenuTypes.JOINT_CORE_SOUNDS, containerId, playerIn.player, teCoreIn);
        this.addSlot(new PointerSlot(dummyInventory, new int[]{33}, this.dummySlotPointer, SOUND_SLOT_LEFT, 22));

        int i;
        for (int j = 0; j < DUMMY_INV_LIST_ROWS; ++j) {
            for (i = 0; i < DUMMY_INV_LIST_COLS; ++i) {
                this.addSlot(new PointerSlot(dummyInventory, this.dummyListPointerOffset, new int[]{i + j * 8}, 8 + i * 18, INV_YPOS + j * 18));
            }
        }

        List<ItemStack> soundList = new ArrayList<>();
        this.addToAllSoundItemFromList(JBModContainer.soundManager.defaultEffectItemList, soundList);
        this.addToAllSoundItemFromList(JBModContainer.soundManager.importedSoundItemList, soundList);

        while (soundList.size() < DUMMY_LIST_NUM) {
            soundList.add(ItemStack.EMPTY);
        }

        soundListInventory = new SimpleContainer(soundList.size());
        this.actualEffectSlotStart = this.slots.size();

        for (i = 0; i < 16; ++i) {
            this.addSlot(new DisabledSlot(teCoreIn.effectChest, i, i * 18 + 1, -15, false));
        }

        this.dummyListPointerOffset[0] = this.actualSoundListStart = this.slots.size();

        for (i = 0; i < soundListInventory.getContainerSize(); ++i) {
            soundListInventory.setItem(i, soundList.get(i));
            this.addSlot(new DisabledSlot(soundListInventory, i, 0, 0, true));
        }
    }

    public void setSlotChangeListener(OnSlotItemChangedListener listenerIn) {
        for (int i = this.actualEffectSlotStart; i < this.actualEffectSlotStart + 16; ++i) {
            Slot slot = this.getSlot(i);
            if (slot instanceof DisabledSlot) {
                ((DisabledSlot)slot).setListener(listenerIn);
            }
        }
    }

    private void addToAllSoundItemFromList(List<ItemStack> list, List<ItemStack> soundList) {
        if (list != null) {
            int i = 0;

            for (int len = list.size(); i < len; ++i) {
                ItemStack stack = list.get(i);
                if (EffectInventory.isEffectItem(stack)) {
                    soundList.add(stack.copy());
                }
            }
        }
    }

    public int getDummySlotPointerIndex() {
        return this.dummySlotPointer[0];
    }

    public void setDummySlotPointerToPrev() {
        if (this.dummySlotPointer[0] > 0) {
            this.dummySlotPointer[0]--;
        }
    }

    public void setDummySlotPointerToNext() {
        if (this.dummySlotPointer[0] < 15) {
            this.dummySlotPointer[0]++;
        }
    }

    public int getMaxRowIndex() {
        return (this.slots.size() - this.actualSoundListStart - 1) / 8;
    }

    public void setDummyListPointerOffset(int offset) {
        this.dummyListPointerOffset[0] = this.actualSoundListStart + offset;
    }

    /**
     * 1.12.2 {@code func_190896_a(List)} ({@code Container.setAll}).  In 1.20.1 the client
     * packet path is {@code initializeContents(int, List, ItemStack)}; the 1.12.2 body
     * clamped the list to the slot count, which {@code subList} preserves here.
     */
    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        super.initializeContents(stateId, items.subList(0, Math.min(items.size(), this.slots.size())), carried);
    }

    /**
     * 1.12.2 {@code func_184996_a(int, int, ClickType, EntityPlayer)}: swallow clicks on
     * out-of-range slot ids (the dummy/pointer rows) instead of letting them crash.
     */
    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        try {
            if (slotId < this.slots.size()) {
                super.clicked(slotId, dragType, clickTypeIn, player);
            }
        } catch (Exception var6) {
        }
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotNum) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotNum);
        if (slot != null && slot.hasItem() && slot instanceof DisabledSlot) {
            slot.set(ItemStack.EMPTY);
        }

        return itemstack;
    }

    /** 1.12.2 {@code updateSlotFromPacket(int, EffectSlot)} (PACKET_TYPE_SOUND_GUIACT). */
    public void updateSlotFromPacket(int slotIndex, EffectSlot effectSlot) {
        if (slotIndex >= 0 && slotIndex < 16) {
            int actualSlot = this.actualEffectSlotStart + slotIndex;
            Slot slot = this.getSlot(actualSlot);
            ItemStack stack = slot.getItem();
            if (EffectInventory.isEffectItem(stack)) {
                stack = stack.copy();
                CompoundTag tag = stack.getTag();
                if (tag == null) {
                    tag = new CompoundTag();
                    stack.setTag(tag);
                }

                EffectSlot.writeToNBT(tag, effectSlot, true);
                this.setItem(actualSlot, this.getStateId(), stack);
                this.broadcastChanges();
            }
        }
    }

    @Override
    public void receiveButtonAction(Player player, int ID, int startIndex, int amount) {
        switch (ID) {
            case 7221:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreId, player, this.world, this.teCore.getBlockPos());
                return;
            case 7222:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointBlockExtraId, this.entityplayer, this.world, this.teCore.getBlockPos());
                return;
            case 7223:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointChestFromBlockId, this.entityplayer, this.world, this.teCore.getBlockPos());
                return;
            case 7224:
            case 7225:
            default:
                return;
            case 7226:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreFuelsFromBlockId, this.entityplayer, this.world, this.teCore.getBlockPos());
                return;
            case 7227:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreAddonsId, this.entityplayer, this.world, this.teCore.getBlockPos());
                return;
            case 7228:
                JBModContainer.proxy.openGui(JBGuiIds.guiJointCoreSoundsId, this.entityplayer, this.world, this.teCore.getBlockPos());
                break;
        }
    }

    /** 1.12.2 {@code receiveSoundNameStrUpdate(int, String)} (PACKET_SUBTYPE_STRINGCHNGED_SOUNDNAME). */
    public void receiveSoundNameStrUpdate(int actualSlotNum, String stringFromPacket) {
        if (actualSlotNum >= 0 && actualSlotNum < 16) {
            if (stringFromPacket != null && stringFromPacket.length() > 64) {
                stringFromPacket = stringFromPacket.substring(0, 64);
            }

            Slot targetSlot = this.getSlot(this.actualEffectSlotStart + actualSlotNum);
            ItemStack stack = targetSlot.getItem();
            if (!CompatHelper.isStackEmptyOrNull(stack)) {
                CompoundTag tag;
                if (stack.hasTag()) {
                    tag = stack.getTag();
                } else {
                    tag = new CompoundTag();
                    stack.setTag(tag);
                }

                tag.putString("effectNameStr", stringFromPacket);
            }
        }
    }

    /** Slot wrapper that redirects every access to an index of the real slot list. */
    public class PointerSlot extends Slot {
        private int[] pontOffset;
        private int[] pointTo;

        public PointerSlot(net.minecraft.world.Container inventoryIn, int[] pontOffsetIn, int[] pointToIn, int xPosition, int yPosition) {
            super(inventoryIn, -1, xPosition, yPosition);
            this.pontOffset = pontOffsetIn;
            this.pointTo = pointToIn;
        }

        public boolean isValidPointer() {
            return ContainerJointCoreSounds.this.slots.size() > this.pontOffset[0] + this.pointTo[0];
        }

        public Slot getSlot() {
            return ContainerJointCoreSounds.this.slots.get(this.pontOffset[0] + this.pointTo[0]);
        }

        @Override
        public void onTake(Player thePlayer, ItemStack stack) {
            this.getSlot().onTake(thePlayer, stack);
        }

        @Override
        public void set(ItemStack stack) {
            this.getSlot().set(stack);
        }

        @Override
        public void setChanged() {
            this.getSlot().setChanged();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.getSlot().mayPlace(stack);
        }

        @Override
        public ItemStack getItem() {
            return this.getSlot().getItem();
        }

        @Override
        public boolean hasItem() {
            return this.getSlot().hasItem();
        }

        @Override
        public int getMaxStackSize() {
            return this.getSlot().getMaxStackSize();
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return this.getSlot().getMaxStackSize(stack);
        }

        @Override
        public com.mojang.datafixers.util.Pair<net.minecraft.resources.ResourceLocation, net.minecraft.resources.ResourceLocation> getNoItemIcon() {
            return this.getSlot().getNoItemIcon();
        }

        @Override
        public ItemStack remove(int amount) {
            return this.getSlot().remove(amount);
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return this.getSlot().mayPickup(playerIn);
        }

        @Override
        public boolean isActive() {
            if (!this.isValidPointer()) {
                return false;
            } else {
                Slot slot = this.getSlot();
                return slot != null;
            }
        }

        @Override
        public Slot setBackground(net.minecraft.resources.ResourceLocation atlas, net.minecraft.resources.ResourceLocation sprite) {
            this.getSlot().setBackground(atlas, sprite);
            return this;
        }

        @Override
        public int getSlotIndex() {
            return this.getSlot().getSlotIndex();
        }

        @Override
        public boolean isSameInventory(Slot other) {
            return this.getSlot().isSameInventory(other);
        }
    }

    public static class DisabledSlot extends Slot {
        private OnSlotItemChangedListener listener;
        public boolean lockStack;

        public DisabledSlot(net.minecraft.world.Container inventoryIn, int index, int xPosition, int yPosition, boolean lockStackIn) {
            super(inventoryIn, index, xPosition, yPosition);
            this.lockStack = lockStackIn;
        }

        public void setListener(OnSlotItemChangedListener listenerIn) {
            this.listener = listenerIn;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (this.listener != null) {
                this.listener.onSlotItemChanged();
            }
        }

        @Override
        public void set(ItemStack stack) {
            if (!this.lockStack) {
                super.set(stack);
            }
        }

        @Override
        public ItemStack getItem() {
            return this.lockStack ? super.getItem().copy() : super.getItem();
        }

        @Override
        public ItemStack remove(int amount) {
            return this.lockStack ? this.getItem().copy() : super.remove(amount);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }

    public interface OnSlotItemChangedListener {
        void onSlotItemChanged();
    }
}
