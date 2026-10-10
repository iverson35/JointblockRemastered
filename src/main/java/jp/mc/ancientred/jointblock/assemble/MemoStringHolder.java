package jp.mc.ancientred.jointblock.assemble;

import java.io.UnsupportedEncodingException;

public class MemoStringHolder {
   private static final String TOBYTE_CHARSET = "UTF-16LE";
   public final String memoStr;
   public final byte[] memoStrByteAry;

   public MemoStringHolder(String memoStr) throws UnsupportedEncodingException {
      this.memoStr = memoStr;
      this.memoStrByteAry = memoStr.getBytes("UTF-16LE");
   }

   public MemoStringHolder(byte[] memoStrByteAry) throws UnsupportedEncodingException {
      this.memoStrByteAry = memoStrByteAry;
      this.memoStr = new String(memoStrByteAry, "UTF-16LE");
   }
}
