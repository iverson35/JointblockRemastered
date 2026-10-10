package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.common.JBUnicodeUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code JBGuiStringHelper}, verbatim port.  The only change is the widget type:
 * 1.12.2 {@code GuiTextField} is {@link EditBox} in 1.20.1, so
 * {@code func_146179_b()} -&gt; {@code getValue()} and {@code func_146180_a(x)} -&gt; {@code setValue(x)}.
 */
@OnlyIn(Dist.CLIENT)
public class JBGuiStringHelper {

    public static boolean isEmptyOrNull(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static int fixToValidNumString(EditBox textField, String oldStr) {
        return fixToValidNumString(textField, oldStr, -999, 999);
    }

    public static int fixToValidNumString(EditBox textField, String oldStr, int min, int max) {
        String str = textField.getValue();
        if (isEmptyOrNull(str)) {
            textField.setValue("");
            return 0;
        } else {
            if (!isValidNumber(str, min, max)) {
                if (isEmptyOrNull(oldStr) || !isValidNumber(oldStr, min, max)) {
                    textField.setValue("");
                    return 0;
                }

                textField.setValue(oldStr);
            }

            if (str.equals("-")) {
                return 0;
            } else {
                try {
                    return Integer.parseInt(str);
                } catch (NumberFormatException var6) {
                    textField.setValue("");
                    return 0;
                }
            }
        }
    }

    public static boolean isValidNumber(String str, int min, int max) {
        try {
            if (str.equals("-")) {
                return true;
            } else {
                int i;
                for (i = 0; i < str.length(); ++i) {
                    if (!JBUnicodeUtil.isNumChar(str.charAt(i)) && str.charAt(i) != '-') {
                        return false;
                    }
                }

                i = Integer.parseInt(str);
                return i >= min && i <= max;
            }
        } catch (NumberFormatException var4) {
            return false;
        }
    }

    public static double fixToValidDecimalString(EditBox textField, String oldStr, int min, int max) {
        String str = textField.getValue();
        if (isEmptyOrNull(str)) {
            textField.setValue("");
            return 0.0;
        } else {
            if (!isValidDecimal(str, min, max)) {
                if (isEmptyOrNull(oldStr) || !isValidDecimal(oldStr, min, max)) {
                    textField.setValue("");
                    return 0.0;
                }

                textField.setValue(oldStr);
            }

            if (str.equals("-")) {
                return 0.0;
            } else {
                try {
                    double num = Double.parseDouble(str);
                    if (!Double.isInfinite(num) && !Double.isNaN(num)) {
                        return num;
                    } else {
                        return 0.0;
                    }
                } catch (NumberFormatException var7) {
                    textField.setValue("");
                    return 0.0;
                }
            }
        }
    }

    public static boolean isValidDecimal(String str, int min, int max) {
        try {
            if (str.equals("-")) {
                return true;
            } else {
                for (int i = 0; i < str.length(); ++i) {
                    if (!JBUnicodeUtil.isNumChar(str.charAt(i)) && str.charAt(i) != '-' && str.charAt(i) != '.') {
                        return false;
                    }
                }

                double num = Double.parseDouble(str);
                if (!Double.isInfinite(num) && !Double.isNaN(num)) {
                    return num >= (double)min && num <= (double)max;
                } else {
                    return false;
                }
            }
        } catch (NumberFormatException var5) {
            return false;
        }
    }
}
