package jp.mc.ancientred.jointblock.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiTextFieldEx}: {@code GuiTextField} which can either be focused while staying
 * invisible (setFocusedIgnoreVisible) or refuses focus while invisible.
 */
@OnlyIn(Dist.CLIENT)
public class GuiTextFieldEx extends EditBox {
   private final int id;

   public GuiTextFieldEx(int componentId, Font font, int posX, int posY, int width, int height) {
      super(font, posX, posY, width, height, Component.empty());
      this.id = componentId;
   }

   public int getId() {
      return this.id;
   }

   public void setFocusedIgnoreVisible(boolean value) {
      super.setFocused(value);
   }

   @Override
   public void setFocused(boolean value) {
      if (!this.isVisible()) {
         value = false;
      }

      super.setFocused(value);
   }
}
