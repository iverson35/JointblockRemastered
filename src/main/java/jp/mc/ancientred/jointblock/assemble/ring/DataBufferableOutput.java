package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class DataBufferableOutput {
   public ASyncByteArrayOutputStream baosMain = new ASyncByteArrayOutputStream(128);
   public ASyncByteArrayOutputStream baosBuffer = new ASyncByteArrayOutputStream(128);
   public DataOutputStream dos;
   private byte[] bytearr = null;

   public DataBufferableOutput() {
      this.dos = new DataOutputStream(this.baosBuffer);
   }

   public byte[] getByteArray() {
      return Arrays.copyOf(this.baosMain.toByteArray(), this.baosMain.getCount());
   }

   public void commitSegment(byte segmentId) {
      int segLen = this.baosBuffer.getCount();
      if (segLen > 0) {
         if (segLen > 128) {
            throw new RuntimeException("Segment too large!!");
         } else {
            this.baosMain.write(segmentId);
            this.baosMain.write(segLen);
            this.baosMain.write(this.baosBuffer.toByteArray(), 0, segLen);
            this.baosBuffer.reset();
         }
      }
   }

   public void write(int b) throws IOException {
      this.dos.writeByte(b);
   }

   public void write(byte[] b) throws IOException {
      this.dos.write(b);
   }

   public void write(byte[] b, int off, int len) throws IOException {
      this.dos.write(b, off, len);
   }

   public void writeBoolean(boolean v) throws IOException {
      this.dos.writeBoolean(v);
   }

   public void writeByte(int v) throws IOException {
      this.dos.writeByte(v);
   }

   public void writeShort(int v) throws IOException {
      this.dos.writeShort(v);
   }

   public void writeChar(int v) throws IOException {
      this.dos.writeChar(v);
   }

   public void writeInt(int v) throws IOException {
      this.dos.writeInt(v);
   }

   public void writeLong(long v) throws IOException {
      this.dos.writeLong(v);
   }

   public void writeFloat(float v) throws IOException {
      this.dos.writeFloat(v);
   }

   public void writeDouble(double v) throws IOException {
      this.dos.writeDouble(v);
   }

   public void writeBytes(String s) throws IOException {
      this.dos.writeBytes(s);
   }

   public void writeChars(String s) throws IOException {
      this.dos.writeChars(s);
   }

   public void writeCommitUTFSegment(String str, byte segId) {
      if (str != null) {
         int strlen = str.length();
         if (strlen != 0) {
            int utflen = 0;
            int count = 0;

            char c;
            for(int i = 0; i < strlen; ++i) {
               c = str.charAt(i);
               if (c >= 1 && c <= 127) {
                  ++utflen;
               } else if (c > 2047) {
                  utflen += 3;
               } else {
                  utflen += 2;
               }
            }

            if (utflen <= 127) {
               if (this.bytearr == null || this.bytearr.length < utflen + 2) {
                  this.bytearr = new byte[utflen * 2 + 2];
               }

               byte[] bytearr = this.bytearr;
               bytearr[count++] = segId;
               bytearr[count++] = (byte)utflen;
               int i = 0;

               for(; i < strlen; ++i) {
                  c = str.charAt(i);
                  if (c < 1 || c > 127) {
                     break;
                  }

                  bytearr[count++] = (byte)c;
               }

               for(; i < strlen; ++i) {
                  c = str.charAt(i);
                  if (c >= 1 && c <= 127) {
                     bytearr[count++] = (byte)c;
                  } else if (c > 2047) {
                     bytearr[count++] = (byte)(224 | c >> 12 & 15);
                     bytearr[count++] = (byte)(128 | c >> 6 & 63);
                     bytearr[count++] = (byte)(128 | c >> 0 & 63);
                  } else {
                     bytearr[count++] = (byte)(192 | c >> 6 & 31);
                     bytearr[count++] = (byte)(128 | c >> 0 & 63);
                  }
               }

               this.baosMain.write(bytearr, 0, utflen + 2);
            }
         }
      }
   }

   public static class ASyncByteArrayOutputStream extends ByteArrayOutputStream {
      public ASyncByteArrayOutputStream(int bufSize) {
         super(bufSize);
      }

      public byte[] toByteArray() {
         return this.buf;
      }

      public void reset() {
         this.count = 0;
      }

      public int getCount() {
         return this.count;
      }
   }
}
