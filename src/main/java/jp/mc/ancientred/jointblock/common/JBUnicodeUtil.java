package jp.mc.ancientred.jointblock.common;

public class JBUnicodeUtil {
   public static final int C_TYPE_ELSE = 0;
   public static final int C_TYPE_KANJI = 1;
   public static final int C_TYPE_ZEN_HIRAGANA = 2;
   public static final int C_TYPE_ZEN_KATAKANA = 3;
   public static final int C_TYPE_HAN_KATAKANA = 4;
   public static final int C_TYPE_HANKAKU = 5;
   public static final int C_TYPE_SPECIAL = 7;
   public static final int C_TYPE_COMBINE_PREV = 9;
   public static int[] U_C0X30_CHAR_ATTRS = new int[]{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 9, 9, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3};

   public static final boolean isNumChar(char chr) {
      return chr >= '0' && chr <= '9';
   }

   public static final boolean isJPChar(char chr) {
      if (chr < 12288) {
         if (chr == 8213) {
            return true;
         }

         if (chr == 8251) {
            return true;
         }

         if (chr == 8254) {
            return true;
         }

         if (chr == 9675) {
            return true;
         }
      } else {
         if (chr <= 12543) {
            return U_C0X30_CHAR_ATTRS[chr & 255] != 0;
         }

         if (chr <= 65376) {
            if (chr <= 12703) {
               if (chr >= 12688) {
                  return true;
               }
            } else if (chr <= 19903) {
               if (chr >= 13312) {
                  return true;
               }
            } else if (chr <= 40959) {
               if (chr >= 19968) {
                  return true;
               }
            } else if (chr > '\udfff') {
               if (chr <= '\ufadf') {
                  if (chr >= 63744) {
                     return true;
                  }
               } else if (chr == 65339 || chr == 65372) {
                  return true;
               }
            }
         } else if (chr <= 65439) {
            return true;
         }
      }

      return false;
   }
}
