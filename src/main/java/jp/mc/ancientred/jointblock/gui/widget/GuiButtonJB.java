package jp.mc.ancientred.jointblock.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.20.1 replacement for the removed vanilla 1.12 {@code GuiButton} / Forge {@code GuiButtonExt}.
 * Keeps the 1.12 id and the 1.12 "actionPerformed" style callback.
 */
@OnlyIn(Dist.CLIENT)
public class GuiButtonJB extends Button implements IJBGuiWidget {
   public final int id;
   public int packedFGColour = 14737632;
   protected final Consumer<IJBGuiWidget> onPressCallback;

   public GuiButtonJB(int id, int x, int y, int width, int height, Component message) {
      this(id, x, y, width, height, message, (Consumer<IJBGuiWidget>)null);
   }

   public GuiButtonJB(int id, int x, int y, int width, int height, String message) {
      this(id, x, y, width, height, messageOf(message), (Consumer<IJBGuiWidget>)null);
   }

   public GuiButtonJB(int id, int x, int y, int width, int height, Component message, Consumer<IJBGuiWidget> onPress) {
      super(x, y, width, height, message, button -> {}, DEFAULT_NARRATION);
      this.id = id;
      this.onPressCallback = onPress;
   }

   public GuiButtonJB(int id, int x, int y, int width, int height, String message, Consumer<IJBGuiWidget> onPress) {
      this(id, x, y, width, height, messageOf(message), onPress);
   }

   @Override
   public int getId() {
      return this.id;
   }

   @Override
   public void onPress() {
      if (this.onPressCallback != null) {
         this.onPressCallback.accept(this);
      }
   }

   private static Component messageOf(String message) {
      return message == null ? Component.empty() : Component.literal(message);
   }
}
