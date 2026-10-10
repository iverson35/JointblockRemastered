package jp.mc.ancientred.jointblock.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiScreenClosable}: ESC (or the inventory key) closes the container the player is
 * currently looking at, instead of the plain screen close.
 */
@OnlyIn(Dist.CLIENT)
public class GuiScreenClosable extends Screen {
   protected GuiScreenClosable() {
      super(Component.empty());
   }

   /**
    * 1.12.2 {@code GuiScreen.actionPerformed(GuiButton)} hook.  The 1.20.1 widgets report a press
    * through {@link IJBGuiWidget} (see {@code GuiButtonJB}); subclasses override this.
    */
   protected void actionPerformed(IJBGuiWidget par1GuiButton) {
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256 || keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
         Minecraft.getInstance().player.closeContainer();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
