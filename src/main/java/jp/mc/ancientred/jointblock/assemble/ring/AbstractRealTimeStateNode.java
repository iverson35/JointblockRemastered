package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import jp.mc.ancientred.jointblock.entity.EntityJointed;

public abstract class AbstractRealTimeStateNode extends AbstractRingNode {
   public double returnValue;
   public double[] arrayValues;
   protected long tickGenRTValueUpdate = -1L;

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.updateOnceInTick(program, page);
      return program.nextNodeIndex(page);
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      this.updateOnceInTick(program, page);
      return this.returnValue;
   }

   public double getArrayValue(RingProgram program, RingProgramPage page, int index) {
      this.updateOnceInTick(program, page);
      if (this.arrayValues != null) {
         return index >= 0 && index < this.arrayValues.length ? this.arrayValues[index] : Double.NaN;
      } else {
         return super.getArrayValue(program, page, index);
      }
   }

   private void updateOnceInTick(RingProgram program, RingProgramPage page) {
      if (program.tickGeneration != this.tickGenRTValueUpdate) {
         this.tickGenRTValueUpdate = program.tickGeneration;
         this.updateReturnValue(program.getEntityJointed(), program, page);
      }

   }

   protected abstract void updateReturnValue(EntityJointed var1, RingProgram var2, RingProgramPage var3);

   protected final void writeRunningData(DataBufferableOutput out) throws IOException {
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
   }
}
