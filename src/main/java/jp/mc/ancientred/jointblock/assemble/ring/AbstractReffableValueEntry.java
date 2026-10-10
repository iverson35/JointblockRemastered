package jp.mc.ancientred.jointblock.assemble.ring;

public abstract class AbstractReffableValueEntry {
   public boolean isNodeRefMode;

   public abstract void reset();

   public abstract void toggleMode();

   public abstract void setToRefModeAndSetIndex(int var1);

   protected AbstractRingNode getReferringNode(RingProgramPage page, int nodeIndex) {
      return nodeIndex >= 0 && nodeIndex < page.nodeList.size() ? (AbstractRingNode)page.nodeList.get(nodeIndex) : null;
   }

   public boolean preFetchActualValue(RingProgram program, RingProgramPage page) {
      return this.fetchActual(program, page, true);
   }

   public boolean fetchActualValue(RingProgram program, RingProgramPage page) {
      return this.fetchActual(program, page, false);
   }

   protected abstract boolean fetchActual(RingProgram var1, RingProgramPage var2, boolean var3);

   public abstract short getActualValueShort();

   public abstract int getActualValueInt();

   public abstract boolean getActualValueBool();
}
