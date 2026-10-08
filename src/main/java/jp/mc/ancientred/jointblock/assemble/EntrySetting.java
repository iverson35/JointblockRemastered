package jp.mc.ancientred.jointblock.assemble;

import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;

public class EntrySetting {
   public ChoiceFormat title;
   public String titleStr;
   public Format format;
   public String formatStr;
   public int min;
   public int max;
   public boolean visible;
   public boolean enabled;
   public boolean isRefTogglable;
   public int buttonStatesIndex;
   public int buttonStatesInitIndex;
   public IJBGuiSettings.TriButtonState[] buttonStates;
   public IJBGuiSettings.TriButtonState[] buttonStatesPrev;
}
