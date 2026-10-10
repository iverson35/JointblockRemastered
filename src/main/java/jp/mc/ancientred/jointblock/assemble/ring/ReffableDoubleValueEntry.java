package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;

public class ReffableDoubleValueEntry extends AbstractReffableValueEntry {
   private final short valueInit;
   private final short fixedValueInit;
   private final boolean modeInit;
   public double value;
   public double prefetchValue;
   public double actualValue;

   public ReffableDoubleValueEntry(double initValue, boolean isRefMode) {
      this(initValue, isRefMode, 0.0);
   }

   public ReffableDoubleValueEntry(double initValue, boolean isRefMode, double fvInit) {
      this.modeInit = isRefMode;
      this.valueInit = (short)((int)initValue);
      this.fixedValueInit = (short)((int)fvInit);
      this.reset();
   }

   public void reset() {
      this.value = (double)this.fixedValueInit;
      this.isNodeRefMode = this.modeInit;
   }

   public void setToRefModeAndSetIndex(int refNodeIndex) {
      this.isNodeRefMode = true;
      this.value = (double)((short)refNodeIndex);
   }

   public void toggleMode() {
      this.isNodeRefMode = !this.isNodeRefMode;
      this.value = (double)(this.isNodeRefMode ? -1 : this.fixedValueInit);
   }

   protected boolean fetchActual(RingProgram program, RingProgramPage page, boolean isPrefetch) {
      double value = this.value;
      if (this.isNodeRefMode) {
         AbstractRingNode node = this.getReferringNode(page, (int)value);
         if (node == null) {
            return false;
         }

         double dValue = node.getReturnValue(program, page);
         if (dValue != dValue) {
            return false;
         }

         value = dValue;
      }

      if (isPrefetch) {
         this.prefetchValue = value;
      } else {
         this.actualValue = value;
      }

      return true;
   }

   public short getActualValueShort() {
      return (short)((int)this.actualValue);
   }

   public int getActualValueInt() {
      return (int)this.actualValue;
   }

   public boolean getActualValueBool() {
      return (int)this.actualValue != 0;
   }

   public void write(DataBufferableOutput out) throws IOException {
      out.writeDouble(this.value);
      out.writeBoolean(this.isNodeRefMode);
   }

   public void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeDouble(this.actualValue);
   }

   public void read(DataBufferableInput inn) throws IOException {
      this.value = inn.readDouble(this.value);
      this.isNodeRefMode = inn.readBoolean(this.isNodeRefMode);
   }

   public void readRunningData(DataBufferableInput inn) throws IOException {
      this.actualValue = inn.readDouble(this.actualValue);
   }
}
