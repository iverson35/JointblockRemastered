package jp.mc.ancientred.jointblock.gui;

import jp.mc.ancientred.jointblock.common.JBUnicodeUtil;

/**
 * 1.12.2 {@code AllowedCharJP} (a copy of {@code ChatAllowedCharacters} widened to accept
 * Japanese characters).  Verbatim port; only the {@code __OBFID} field was dropped.
 */
public class AllowedCharJP {

    public static final char[] allowedCharacters = new char[]{
        '/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'
    };

    public static boolean isAllowedCharacter(char c) {
        return c != 167 && c >= ' ' && c != 127 || JBUnicodeUtil.isJPChar(c);
    }

    public static String filerAllowedCharacters(String text) {
        StringBuilder stringbuilder = new StringBuilder();
        char[] achar = text.toCharArray();
        int i = achar.length;

        for (int j = 0; j < i; ++j) {
            char c0 = achar[j];
            if (isAllowedCharacter(c0)) {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }
}
