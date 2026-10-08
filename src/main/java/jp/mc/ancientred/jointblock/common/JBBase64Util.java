package jp.mc.ancientred.jointblock.common;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

public class JBBase64Util {
   private static final byte[] longBytes = new byte[8];
   private static Base64 base64Mime = new Base64(false);
   private static Base64 base64Flat = new Base64(0, new byte[]{13, 10}, false);

   public static String longToBasd64(long value) {
      for(int i = 0; i < 8; ++i) {
         longBytes[i] = (byte)((int)(value >> (8 - i - 1 << 3)));
      }

      return StringUtils.newStringUtf8(base64Flat.encode(longBytes));
   }

   public static String toBase64FlatChunkString(byte[] byteAry) {
      return StringUtils.newStringUtf8(base64Flat.encode(byteAry));
   }

   public static String toBase64WrappedChunkString(byte[] byteAry) {
      return StringUtils.newStringUtf8(base64Mime.encode(byteAry));
   }

   public static byte[] base64StringToByteArray(String clipboardData) {
      return base64Flat.decode(clipboardData);
   }

   public static boolean checkBase64String(String clipboardData) {
      return Base64.isBase64(clipboardData);
   }
}
