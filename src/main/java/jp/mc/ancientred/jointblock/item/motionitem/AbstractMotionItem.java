package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.item.IColorHanldlerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractMotionItem extends Item implements IColorHanldlerItem {

   /**
    * 1.12 damage-value subtype (the seat index of the key/item binded motion items), now stored as int
    * NBT tag {@code subtype} (flattening to separate items is a Phase 6 decision).
    *
    * <p>Declared on the base class because {@code TileEntityJoint} holds an arbitrary motion stack and
    * has to read the subtype back: 1.12.2 asked {@code Item#getHasSubtypes} and returned
    * {@code ItemStack#getMetadata} from {@code TileEntityJoint#getMotionItemDamage}, and the only
    * motion items that declared subtypes were the two binded ones.
    */
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";

   public AbstractMotionItem() {
      super(new Item.Properties());
   }

   /** 1.12.2 damage value of the stack; stacks without the tag are subtype 0. */
   public int getSubtype(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null ? tag.getInt(NBT_TAGNAME_SUBTYPE) : 0;
   }

   public void setSubtype(ItemStack stack, int subtype) {
      stack.getOrCreateTag().putInt(NBT_TAGNAME_SUBTYPE, subtype);
   }

   public ItemStack createSubtypedStack(int subtype) {
      ItemStack stack = new ItemStack(this);
      this.setSubtype(stack, subtype);
      return stack;
   }
}
