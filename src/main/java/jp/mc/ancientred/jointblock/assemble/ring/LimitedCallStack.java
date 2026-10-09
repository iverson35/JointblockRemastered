package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;

public class LimitedCallStack {
   public static final int LIMITED_SIZE = 8;
   public static final int ISBLANKDATA = -1;
   public static final int INIT_INDEX = 0;
   public byte pointer = 0;
   public int[] limitedStack = new int[8];

   public void pop() {
      --this.pointer;
      this.limitedStack[this.fixPointerInRange()] = 0;
   }

   public void push(short pageIdx, byte nodeIdx) {
      int v = pageIdx << 16 | (nodeIdx & 255) << 8 | 1;
      this.limitedStack[this.fixPointerInRange()] = v;
      ++this.pointer;
   }

   private int fixPointerInRange() {
      if (this.pointer < 0) {
         this.pointer = 7;
      } else if (this.pointer >= 8) {
         this.pointer = 0;
      }

      return this.pointer;
   }

   public boolean hasStack() {
      int v = this.limitedStack[this.getPrevInRange()];
      return (v & 1) != 0;
   }

   public short getCurrentCallerPage() {
      int v = this.limitedStack[this.getPrevInRange()];
      return (v & 1) == 0 ? -1 : (short)(v >> 16 & '\uffff');
   }

   public byte getCurrentCallerNode() {
      int v = this.limitedStack[this.getPrevInRange()];
      return (v & 1) == 0 ? -1 : (byte)(v >> 8 & 255);
   }

   private int getPrevInRange() {
      int prevIdx = this.pointer - 1;
      if (prevIdx < 0) {
         return 7;
      } else {
         return prevIdx >= 8 ? 0 : prevIdx;
      }
   }

   public void initForShotMode() {
      this.limitedStack[7] = 0;
      this.pointer = 0;
   }

   public void writeStack(DataBufferableOutput out) throws IOException {
      out.writeByte(this.pointer);

      for(int i = 0; i < 8; ++i) {
         out.writeInt(this.limitedStack[i]);
      }

   }

   public void readStack(DataBufferableInput inn) throws IOException {
      this.pointer = inn.readByte(this.pointer);

      for(int i = 0; i < 8; ++i) {
         this.limitedStack[i] = inn.readInt(0);
      }

   }
}
