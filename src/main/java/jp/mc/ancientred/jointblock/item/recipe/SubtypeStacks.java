package jp.mc.ancientred.jointblock.item.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Shared subtype access for the recipe classes.
 *
 * <p>1.12.2 encoded the sub-kinds of an item in its damage value. The port stores them in the int NBT
 * tag {@code subtype} instead - exactly the convention of the item classes, which each expose their own
 * {@code NBT_TAGNAME_SUBTYPE}/{@code getSubtype}/{@code createSubtypedStack} trio over the same key.
 * Recipes have to read that key generically (they hold an {@link Item}, not one of those classes), so
 * the tag name and the two operations live here.
 */
final class SubtypeStacks {

   /** Mirrors {@code NBT_TAGNAME_SUBTYPE} of the subtyped item classes. */
   static final String NBT_TAGNAME_SUBTYPE = "subtype";

   private SubtypeStacks() {
   }

   /** 1.12.2 damage value of the stack; stacks without the tag are subtype 0, like the item classes. */
   static int getSubtype(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null ? tag.getInt(NBT_TAGNAME_SUBTYPE) : 0;
   }

   /** Canonical subtype stack: the item plus the {@code subtype} tag, whether or not the subtype is 0. */
   static ItemStack create(Item item, int subtype) {
      ItemStack stack = new ItemStack(item);
      stack.getOrCreateTag().putInt(NBT_TAGNAME_SUBTYPE, subtype);
      return stack;
   }
}
