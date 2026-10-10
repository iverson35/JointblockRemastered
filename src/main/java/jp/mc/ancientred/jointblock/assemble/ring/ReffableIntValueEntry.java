package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;

public class ReffableIntValueEntry extends AbstractReffableValueEntry {
   private final int valueInit;
   private final int fixedValueInit;
   private final boolean modeInit;
   public int value;
   public int prefetchValue;
   public int actualValue;

   public ReffableIntValueEntry(int initValue, boolean isRefMode) {
      this(initValue, isRefMode, 0);
   }

   public ReffableIntValueEntry(int initValue, boolean isRefMode, int fvInit) {
      this.modeInit = isRefMode;
      this.valueInit = initValue;
      this.fixedValueInit = fvInit;
      this.reset();
   }

   public void reset() {
      this.value = this.fixedValueInit;
      this.isNodeRefMode = this.modeInit;
   }

   public void setToRefModeAndSetIndex(int refNodeIndex) {
      this.isNodeRefMode = true;
      this.value = refNodeIndex;
   }

   public void toggleMode() {
      this.isNodeRefMode = !this.isNodeRefMode;
      this.value = this.isNodeRefMode ? -1 : this.fixedValueInit;
   }

   protected boolean fetchActual(RingProgram program, RingProgramPage page, boolean isPrefetch) {
      int value = this.value;
      if (this.isNodeRefMode) {
         AbstractRingNode node = this.getReferringNode(page, value);
         if (node == null) {
            return false;
         }

         double dValue = node.getReturnValue(program, page);
         if (dValue != dValue) {
            return false;
         }

         value = (int)dValue;
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
      out.writeInt(this.value);
      out.writeBoolean(this.isNodeRefMode);
   }

   public void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeInt(this.actualValue);
   }

   public void read(DataBufferableInput inn) throws IOException {
      this.value = inn.readInt(this.value);
      this.isNodeRefMode = inn.readBoolean(this.isNodeRefMode);
   }

   public void readRunningData(DataBufferableInput inn) throws IOException {
      this.actualValue = inn.readInt(this.actualValue);
   }
}
