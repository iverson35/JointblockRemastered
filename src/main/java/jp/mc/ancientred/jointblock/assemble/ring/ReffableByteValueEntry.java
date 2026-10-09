package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;

public class ReffableByteValueEntry extends AbstractReffableValueEntry {
   private final byte valueInit;
   private final byte fixedValueInit;
   private final boolean modeInit;
   public byte value;
   public byte prefetchValue;
   public byte actualValue;

   public ReffableByteValueEntry(int initValue, boolean isRefMode) {
      this(initValue, isRefMode, 0);
   }

   public ReffableByteValueEntry(int initValue, boolean isRefMode, int fvInit) {
      this.modeInit = isRefMode;
      this.valueInit = (byte)initValue;
      this.fixedValueInit = (byte)fvInit;
      this.reset();
   }

   public void reset() {
      this.value = this.fixedValueInit;
      this.isNodeRefMode = this.modeInit;
   }

   public void setToRefModeAndSetIndex(int refNodeIndex) {
      this.isNodeRefMode = true;
      this.value = (byte)refNodeIndex;
   }

   public void toggleMode() {
      this.isNodeRefMode = !this.isNodeRefMode;
      this.value = this.isNodeRefMode ? -1 : this.fixedValueInit;
   }

   protected boolean fetchActual(RingProgram program, RingProgramPage page, boolean isPrefetch) {
      byte value = this.value;
      if (this.isNodeRefMode) {
         AbstractRingNode node = this.getReferringNode(page, value);
         if (node == null) {
            return false;
         }

         double dValue = node.getReturnValue(program, page);
         if (dValue != dValue) {
            return false;
         }

         value = (byte)((int)dValue);
      }

      if (isPrefetch) {
         this.prefetchValue = value;
      } else {
         this.actualValue = value;
      }

      return true;
   }

   public short getActualValueShort() {
      return (short)this.actualValue;
   }

   public int getActualValueInt() {
      return this.actualValue;
   }

   public boolean getActualValueBool() {
      return this.actualValue != 0;
   }

   public void write(DataBufferableOutput out) throws IOException {
      out.writeByte(this.value);
      out.writeBoolean(this.isNodeRefMode);
   }

   public void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.actualValue);
   }

   public void read(DataBufferableInput inn) throws IOException {
      this.value = inn.readByte(this.value);
      this.isNodeRefMode = inn.readBoolean(this.isNodeRefMode);
   }

   public void readRunningData(DataBufferableInput inn) throws IOException {
      this.actualValue = inn.readByte(this.actualValue);
   }
}
