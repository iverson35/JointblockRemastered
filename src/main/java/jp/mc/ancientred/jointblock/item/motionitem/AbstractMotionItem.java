package jp.mc.ancientred.jointblock.item.motionitem;

import jp.mc.ancientred.jointblock.item.IColorHanldlerItem;
import net.minecraft.world.item.Item;

public abstract class AbstractMotionItem extends Item implements IColorHanldlerItem {
   public AbstractMotionItem() {
      super(new Item.Properties());
   }
}
