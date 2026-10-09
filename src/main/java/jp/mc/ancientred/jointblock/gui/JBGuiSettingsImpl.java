package jp.mc.ancientred.jointblock.gui;

import java.text.ChoiceFormat;
import java.text.Format;
import jp.mc.ancientred.jointblock.api.IJBConsts;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.assemble.EntrySetting;
import jp.mc.ancientred.jointblock.common.CompatHelper;

/**
 * 1.12.2 {@code JBGuiSettingsImpl}, verbatim port (no Minecraft dependencies).
 */
public class JBGuiSettingsImpl implements IJBGuiSettings, IJBConsts {

    public final EntrySetting[] entrySettings;
    int textFromRow;
    public String text;
    public String localized;

    public JBGuiSettingsImpl() {
        this.entrySettings = new EntrySetting[6];

        for (int i = 0; i < this.entrySettings.length; ++i) {
            this.entrySettings[i] = new EntrySetting();
        }
    }

    public JBGuiSettingsImpl(int entryNums) {
        this.entrySettings = new EntrySetting[entryNums];

        for (int i = 0; i < this.entrySettings.length; ++i) {
            this.entrySettings[i] = new EntrySetting();
        }
    }

    public void resetAll() {
        for (int i = 0; i < this.entrySettings.length; ++i) {
            this.entrySettings[i].visible = false;
            this.entrySettings[i].enabled = false;
            this.setParamButtonToggleAryWithoutInitIndex(i, defaultButtonStates);
            this.entrySettings[i].isRefTogglable = false;
        }
    }

    public void setRefToggleable(int paramNum) {
        this.entrySettings[paramNum].isRefTogglable = true;
    }

    public void hideButton(int paramNum) {
        this.entrySettings[paramNum].visible = false;
    }

    public void disableButton(int paramNum) {
        this.entrySettings[paramNum].enabled = false;
    }

    public void setParamEntryState(int paramNum, ChoiceFormat title, Format format, int min, int max) {
        if (paramNum >= 0 && paramNum < this.entrySettings.length) {
            this.entrySettings[paramNum].title = title;
            this.entrySettings[paramNum].titleStr = null;
            this.entrySettings[paramNum].format = format;
            this.entrySettings[paramNum].formatStr = null;
            this.entrySettings[paramNum].min = min;
            this.entrySettings[paramNum].max = max;
            this.entrySettings[paramNum].visible = true;
            this.entrySettings[paramNum].enabled = true;
        }
    }

    public void setParamEntryState(int paramNum, ChoiceFormat title, String formatStr, int min, int max) {
        if (paramNum >= 0 && paramNum < this.entrySettings.length) {
            this.entrySettings[paramNum].title = title;
            this.entrySettings[paramNum].titleStr = null;
            this.entrySettings[paramNum].format = null;
            this.entrySettings[paramNum].formatStr = formatStr;
            this.entrySettings[paramNum].min = min;
            this.entrySettings[paramNum].max = max;
            this.entrySettings[paramNum].visible = true;
            this.entrySettings[paramNum].enabled = true;
        }
    }

    public void setParamEntryState(int paramNum, String titleStr, Format format, int min, int max) {
        if (paramNum >= 0 && paramNum < this.entrySettings.length) {
            this.entrySettings[paramNum].title = null;
            this.entrySettings[paramNum].titleStr = titleStr;
            this.entrySettings[paramNum].format = format;
            this.entrySettings[paramNum].formatStr = null;
            this.entrySettings[paramNum].min = min;
            this.entrySettings[paramNum].max = max;
            this.entrySettings[paramNum].visible = true;
            this.entrySettings[paramNum].enabled = true;
        }
    }

    public void setParamEntryState(int paramNum, String titleStr, String formatStr, int min, int max) {
        if (paramNum >= 0 && paramNum < this.entrySettings.length) {
            this.entrySettings[paramNum].title = null;
            this.entrySettings[paramNum].titleStr = titleStr;
            this.entrySettings[paramNum].format = null;
            this.entrySettings[paramNum].formatStr = formatStr;
            this.entrySettings[paramNum].min = min;
            this.entrySettings[paramNum].max = max;
            this.entrySettings[paramNum].visible = true;
            this.entrySettings[paramNum].enabled = true;
        }
    }

    public void setDescriptionText(String text, int textFromRow) {
        if (this.text != text) {
            this.text = text;
            this.textFromRow = textFromRow;
            this.localized = CompatHelper.formatLang(text).replaceAll("\\$BR\\$", "\n");
        }
    }

    public void setParamButtonToggleAryWithoutInitIndex(int paramNum, IJBGuiSettings.TriButtonState[] buttonStates) {
        if (paramNum >= 0 && paramNum < this.entrySettings.length) {
            this.entrySettings[paramNum].buttonStatesPrev = this.entrySettings[paramNum].buttonStates;
            this.entrySettings[paramNum].buttonStates = buttonStates;
        }
    }

    public void setParamButtonToggleAry(int paramNum, IJBGuiSettings.TriButtonState[] buttonStates, int aryInitIndex) {
        if (paramNum >= 0 && paramNum < this.entrySettings.length) {
            this.entrySettings[paramNum].buttonStates = buttonStates;
            this.entrySettings[paramNum].buttonStatesInitIndex = aryInitIndex;
            if (this.entrySettings[paramNum].buttonStatesPrev != buttonStates) {
                this.entrySettings[paramNum].buttonStatesIndex = aryInitIndex;
            }
        }
    }

    public static final int addIntInRange(long valA, long valB, int max) {
        long res = valA + valB;
        if (res > (long)max) {
            return max;
        } else {
            return res > 2147483647L ? Integer.MAX_VALUE : (int)res;
        }
    }

    public static final int subIntInRange(long valA, long valB, int min) {
        long res = valA + valB;
        if (res < (long)min) {
            return min;
        } else {
            return res < -2147483648L ? Integer.MIN_VALUE : (int)res;
        }
    }

    public static final int trySetValue(long val, int min, int max) {
        if (val < (long)min) {
            return min;
        } else if (val < -2147483648L) {
            return Integer.MIN_VALUE;
        } else if (val > (long)max) {
            return max;
        } else {
            return val > 2147483647L ? Integer.MAX_VALUE : (int)val;
        }
    }

    public static final double addDoubleInRange(double valA, double valB, double max) {
        double res = valA + valB;
        return res > max ? max : res;
    }

    public static final double subDoubleInRange(double valA, double valB, double min) {
        double res = valA + valB;
        return res < min ? min : res;
    }

    public static final double trySetDoubleValue(double val, double min, double max) {
        if (val < min) {
            return min;
        } else {
            return val > max ? max : val;
        }
    }
}
