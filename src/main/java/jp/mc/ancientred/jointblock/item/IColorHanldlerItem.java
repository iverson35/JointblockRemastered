package jp.mc.ancientred.jointblock.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IColorHanldlerItem {
   IColorHanldlerItem DEFAULT = new IColorHanldlerItem() {
      public int getColorFromItemStack(ItemStack itemStack, int pass) {
         return 16777215;
      }
   };

   int getColorFromItemStack(ItemStack var1, int var2);

   static IColorHanldlerItem safeCast(Item instance) {
      return instance instanceof IColorHanldlerItem ? (IColorHanldlerItem)instance : DEFAULT;
   }
}
