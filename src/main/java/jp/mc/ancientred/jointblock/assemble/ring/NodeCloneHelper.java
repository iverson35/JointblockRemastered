package jp.mc.ancientred.jointblock.assemble.ring;

public class NodeCloneHelper {
   public DataBufferableInput dBufInn;
   public DataBufferableOutput dBufOut = new DataBufferableOutput();

   public NodeCloneHelper(int byteArySize) {
      this.dBufInn = new DataBufferableInput(this.dBufOut.baosBuffer.toByteArray());
   }

   public void reset() {
      this.dBufOut.baosBuffer.reset();
      this.dBufInn.baisMain.reset();
      this.dBufInn.baisBuffer.reset();
   }

   public void transferBufferToBuffer() {
      this.dBufInn.baisMain.bufferSegmentTo(this.dBufInn.baisBuffer, this.dBufOut.baosBuffer.getCount());
   }
}
