package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTReturn extends AbstractRingNode {
   private static final int ENTRY_REF_INPUT = 0;
   public ReffableDoubleValueEntry inputARef = new ReffableDoubleValueEntry(0.0, false);

   public AbstractRingNode newInstance() {
      return new RingNodeSTReturn();
   }

   public byte getNodeTypeID() {
      return 2;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.inputARef.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.inputARef.value = value;
         default:
      }
   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.inputARef.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.inputARef.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
   }

   public boolean isPageTransition(RingProgram program, RingProgramPage curPage) {
      return program.callStack.hasStack();
   }

   public short getTransitionPageIndex(RingProgram program, RingProgramPage curPage) {
      return program.callStack.getCurrentCallerPage();
   }

   public boolean processTransition(EntityJointed ej, RingProgram program, RingProgramPage curPage, RingProgramPage nextPage, byte cNodeIndex, short nextPageIndex) {
      byte callerNodeIndex = program.callStack.getCurrentCallerNode();
      if (callerNodeIndex >= 0 && callerNodeIndex < nextPage.nodeList.size()) {
         byte nextPageNodeIndex = nextPage.incrementNodeIndex(callerNodeIndex);
         double valueA;
         if (!this.inputARef.fetchActualValue(program, curPage)) {
            valueA = Double.NaN;
         } else {
            valueA = this.inputARef.actualValue;
         }

         AbstractRingNode outNode = nextPage.getNode(callerNodeIndex);
         if (outNode != null && outNode instanceof INodeValueSettable) {
            INodeValueSettable toSetVarNode = (INodeValueSettable)outNode;
            toSetVarNode.setReturnValue(program, nextPage, valueA);
            program.callStack.pop();
            program.currentPageIndex = nextPageIndex;
            program.currentNodeIndex = nextPageNodeIndex;
            return true;
         } else {
            program.setError(RingProgram.ErrorType.invalidNodeIndexError);
            return false;
         }
      } else {
         program.setError(RingProgram.ErrorType.invalidNodeIndexError);
         return false;
      }
   }

   public int getRefCount() {
      return 1;
   }

   public int getRefEntryNum(int refNum) {
      return 0;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      return entryNum == 0 ? this.inputARef : null;
   }

   public String getRefEntryName(int refNum) {
      return "jbring.cmn.innode.title";
   }

   public int getRefLineColor(int refNum) {
      return 16734296;
   }

   public int getRefCharIndex(int refNum) {
      return 8;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.inputARef.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.fixed.title", (Format)EntryFormats.simpleDecimalFormat, -32768, 32767);
         gs.setParamButtonToggleAry(0, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(0);
   }

   public float getIconV() {
      return 80.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.return.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.return.nodedesc");
   }
}
