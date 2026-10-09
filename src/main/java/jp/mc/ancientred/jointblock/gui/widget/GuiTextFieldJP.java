package jp.mc.ancientred.jointblock.gui.widget;

import java.util.function.Consumer;
import jp.mc.ancientred.jointblock.gui.AllowedCharJP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiTextFieldJP}: a text field which also accepts Japanese characters
 * ({@link AllowedCharJP}) and reports value changes to a 1.12 {@code GuiPageButtonList.GuiResponder}
 * (now a plain {@link Consumer}).
 * <p>1.20.1 substitutions: the 1.12 class carried its own copy of {@code GuiTextField.drawTextBox} /
 * {@code drawSelectionBox}; in 1.20.1 those bodies are exactly what {@link EditBox#renderWidget} and
 * EditBox's private {@code renderHighlight} do (same border/background rectangles and colours, same
 * {@code trimStringToWidth}/{@code plainSubstrByWidth} scroll window, same shadowed text, same
 * {@code OR_REVERSE} selection logic), so the field inherits them instead of re-implementing them —
 * re-implementing would also lose {@code EditBox}'s private {@code displayPos}/{@code cursorPos}
 * scroll offset, which the 1.12 copy kept in its own fields.
 * <p>Two inherited vanilla 1.20.1 details therefore stay 1.20.1 rather than 1.12: the focused border
 * is {@code -1} where the 1.12 copy always used {@code -6250336}, and the highlight is fed
 * {@code 0xFF000000} where the 1.12 copy fed opaque blue (both still drawn through
 * {@code COLOR_LOGIC_OR_REVERSE}).  The enabled/disabled colour choice matches the 1.12 one because
 * {@link #setEnabled} drives {@code setEditable}, which selects {@code textColorUneditable} just like
 * {@code isEnabled ? enabledColor : disabledColor} did.
 */
@OnlyIn(Dist.CLIENT)
public class GuiTextFieldJP extends EditBox {
   private final int id;
   private boolean isEnabled = true;
   private Consumer<String> guiResponder;

   public GuiTextFieldJP(int componentId, Font font, int x, int y, int width, int height) {
      super(font, x, y, width, height, Component.empty());
      this.id = componentId;
   }

   public int getId() {
      return this.id;
   }

   public void setGuiResponder(Consumer<String> guiResponderIn) {
      this.guiResponder = guiResponderIn;
   }

   public void setEnabled(boolean enabled) {
      this.isEnabled = enabled;
      this.setEditable(enabled);
   }

   @Override
   public void insertText(String textToWrite) {
      String before = this.getValue();
      super.insertText(textToWrite);
      this.setResponderEntryValue(before);
   }

   @Override
   public void deleteChars(int num) {
      String before = this.getValue();
      super.deleteChars(num);
      this.setResponderEntryValue(before);
   }

   private void setResponderEntryValue(String textBefore) {
      if (this.guiResponder != null && !textBefore.equals(this.getValue())) {
         this.guiResponder.accept(this.getValue());
      }
   }

   public boolean textboxKeyTyped(char typedChar, int keyCode) {
      if (!this.isFocused()) {
         return false;
      } else if (Screen.isSelectAll(keyCode)) {
         this.moveCursorToEnd();
         this.setHighlightPos(0);
         return true;
      } else if (Screen.isCopy(keyCode)) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
         return true;
      } else if (Screen.isPaste(keyCode)) {
         if (this.isEnabled) {
            this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
         }

         return true;
      } else if (Screen.isCut(keyCode)) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
         if (this.isEnabled) {
            this.insertText("");
         }

         return true;
      } else {
         switch (keyCode) {
            case 259:
            case 261:
            case 262:
            case 263:
            case 268:
            case 269:
               return super.keyPressed(keyCode, 0, 0);
            default:
               if (AllowedCharJP.isAllowedCharacter(typedChar)) {
                  if (this.isEnabled) {
                     this.insertText(Character.toString(typedChar));
                  }

                  return true;
               } else {
                  return false;
               }
         }
      }
   }

   @Override
   public boolean charTyped(char typedChar, int modifiers) {
      if (!this.canConsumeInput()) {
         return false;
      } else if (AllowedCharJP.isAllowedCharacter(typedChar)) {
         if (this.isEnabled) {
            this.insertText(Character.toString(typedChar));
         }

         return true;
      } else {
         return false;
      }
   }
}
