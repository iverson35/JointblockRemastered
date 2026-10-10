package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.ByteArrayInputStream;

public class DataBufferableInput {
   public static final int BUFFER_LEN = 128;
   public ASyncByteArrayInputStream baisMain;
   public ASyncByteArrayInputStream baisBuffer;
   private byte[] bytearr = new byte[127];
   private char[] chararr = new char[127];
   private byte[] readBuffer = new byte[8];

   public DataBufferableInput(byte[] data) {
      this.baisMain = new ASyncByteArrayInputStream(data);
      this.baisBuffer = new ASyncByteArrayInputStream(new byte[128]);
   }

   public boolean bufferNextSegment(byte segmentId) {
      int segId = this.baisMain.readPos();
      int segLen = this.baisMain.readPosNext();
      if ((segId | segLen) < 0) {
         return false;
      } else if (segId != segmentId) {
         return false;
      } else if (segLen == 0) {
         return false;
      } else if (segLen > 128) {
         return false;
      } else if (!this.baisMain.checkAvailable(segLen + 2)) {
         return false;
      } else {
         this.baisMain.skip(2L);
         this.baisMain.bufferSegmentTo(this.baisBuffer, segLen);
         return true;
      }
   }

   public boolean readBoolean(boolean defaultValue) {
      int ch = this.baisBuffer.read();
      if (ch < 0) {
         return defaultValue;
      } else {
         return ch != 0;
      }
   }

   public byte readByte(byte defaultValue) {
      int ch = this.baisBuffer.read();
      return ch < 0 ? defaultValue : (byte)ch;
   }

   public short readShort(short defaultValue) {
      int ch1 = this.baisBuffer.read();
      int ch2 = this.baisBuffer.read();
      return (ch1 | ch2) < 0 ? defaultValue : (short)((ch1 << 8) + (ch2 << 0));
   }

   public char readChar(char defaultValue) {
      int ch1 = this.baisBuffer.read();
      int ch2 = this.baisBuffer.read();
      return (ch1 | ch2) < 0 ? defaultValue : (char)((ch1 << 8) + (ch2 << 0));
   }

   public int readInt(int defaultValue) {
      int ch1 = this.baisBuffer.read();
      int ch2 = this.baisBuffer.read();
      int ch3 = this.baisBuffer.read();
      int ch4 = this.baisBuffer.read();
      return (ch1 | ch2 | ch3 | ch4) < 0 ? defaultValue : (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
   }

   private boolean tryBuffer(int byteSize) {
      if (this.baisBuffer.checkAvailable(byteSize)) {
         this.baisBuffer.read(this.readBuffer, 0, byteSize);
         return true;
      } else {
         return false;
      }
   }

   public long readLong(long defaultValue) {
      return !this.tryBuffer(8) ? defaultValue : ((long)this.readBuffer[0] << 56) + ((long)(this.readBuffer[1] & 255) << 48) + ((long)(this.readBuffer[2] & 255) << 40) + ((long)(this.readBuffer[3] & 255) << 32) + ((long)(this.readBuffer[4] & 255) << 24) + (long)((this.readBuffer[5] & 255) << 16) + (long)((this.readBuffer[6] & 255) << 8) + (long)((this.readBuffer[7] & 255) << 0);
   }

   public float readFloat(float defaultValue) {
      int ch1 = this.baisBuffer.read();
      int ch2 = this.baisBuffer.read();
      int ch3 = this.baisBuffer.read();
      int ch4 = this.baisBuffer.read();
      if ((ch1 | ch2 | ch3 | ch4) < 0) {
         return defaultValue;
      } else {
         int res = (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
         return Float.intBitsToFloat(res);
      }
   }

   public double readDouble(double defaultValue) {
      if (!this.tryBuffer(8)) {
         return defaultValue;
      } else {
         long res = ((long)this.readBuffer[0] << 56) + ((long)(this.readBuffer[1] & 255) << 48) + ((long)(this.readBuffer[2] & 255) << 40) + ((long)(this.readBuffer[3] & 255) << 32) + ((long)(this.readBuffer[4] & 255) << 24) + (long)((this.readBuffer[5] & 255) << 16) + (long)((this.readBuffer[6] & 255) << 8) + (long)((this.readBuffer[7] & 255) << 0);
         return Double.longBitsToDouble(res);
      }
   }

   public String readUTFSegmentSafe(byte argSegId) {
      int segId = this.baisMain.readPos();
      int utflen = this.baisMain.readPosNext();
      if ((segId | utflen) < 0) {
         return null;
      } else if (segId != argSegId) {
         return null;
      } else if (utflen > 0 && this.baisMain.checkAvailable(utflen + 2)) {
         this.baisMain.skip(2L);
         if (this.bytearr.length < utflen) {
            this.bytearr = new byte[utflen * 2];
            this.chararr = new char[utflen * 2];
         }

         char[] chararr = this.chararr;
         byte[] bytearr = this.bytearr;
         int count = 0;
         int chararr_count = 0;
         this.baisMain.read(bytearr, 0, utflen);

         int c;
         while(count < utflen) {
            c = bytearr[count] & 255;
            if (c > 127) {
               break;
            }

            ++count;
            chararr[chararr_count++] = (char)c;
         }

         while(count < utflen) {
            c = bytearr[count] & 255;
            byte char2;
            switch (c >> 4) {
               case 0:
               case 1:
               case 2:
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
                  ++count;
                  chararr[chararr_count++] = (char)c;
                  break;
               case 8:
               case 9:
               case 10:
               case 11:
               default:
                  return null;
               case 12:
               case 13:
                  count += 2;
                  if (count > utflen) {
                     return null;
                  }

                  char2 = bytearr[count - 1];
                  if ((char2 & 192) != 128) {
                     return null;
                  }

                  chararr[chararr_count++] = (char)((c & 31) << 6 | char2 & 63);
                  break;
               case 14:
                  count += 3;
                  if (count > utflen) {
                     return null;
                  }

                  char2 = bytearr[count - 2];
                  int char3 = bytearr[count - 1];
                  if ((char2 & 192) != 128 || (char3 & 192) != 128) {
                     return null;
                  }

                  chararr[chararr_count++] = (char)((c & 15) << 12 | (char2 & 63) << 6 | (char3 & 63) << 0);
            }
         }

         return new String(chararr, 0, chararr_count);
      } else {
         return null;
      }
   }

   public static class ASyncByteArrayInputStream extends ByteArrayInputStream {
      public ASyncByteArrayInputStream(byte[] buf) {
         super(buf);
      }

      public int read() {
         return this.pos < this.count ? this.buf[this.pos++] & 255 : -1;
      }

      public int readPos() {
         return this.pos < this.count ? this.buf[this.pos] & 255 : -1;
      }

      public int readPosNext() {
         return this.pos + 1 < this.count ? this.buf[this.pos + 1] & 255 : -1;
      }

      public int read(byte[] b, int off, int len) {
         return super.read(b, off, len);
      }

      public boolean checkAvailable(int byteSize) {
         int avail = this.count - this.pos;
         return byteSize <= avail;
      }

      public void bufferSegmentTo(ASyncByteArrayInputStream dst, int byteLen) {
         System.arraycopy(this.buf, this.pos, dst.buf, 0, byteLen);
         this.pos += byteLen;
         dst.count = byteLen;
         dst.mark = 0;
         dst.pos = 0;
      }

      public void reset() {
         this.count = this.buf.length;
         this.mark = 0;
         this.pos = 0;
      }

      public long skip(long n) {
         long k = (long)(this.count - this.pos);
         if (n < k) {
            k = n < 0L ? 0L : n;
         }

         this.pos = (int)((long)this.pos + k);
         return k;
      }
   }
}
