package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTRingCall extends AbstractRingNode implements INodeValueSettable, IOverPageLinker {
   private static final int ENTRY_LINKPAGEREF = 0;
   private static final int ENTRY_LINKNODEREF = 1;
   private ReffableShortValueEntry linkPageRef = new ReffableShortValueEntry(-1, false, -1);
   private ReffableShortValueEntry linkNodeRef = new ReffableShortValueEntry(-1, false, -1);
   private double returnValue = 0.0;

   public AbstractRingNode newInstance() {
      return new RingNodeSTRingCall();
   }

   public byte getNodeTypeID() {
      return 4;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.linkPageRef.value;
         case 1:
            return (double)this.linkNodeRef.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.linkPageRef.value = (short)((int)value);
            break;
         case 1:
            this.linkNodeRef.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.linkPageRef.write(out);
      this.linkNodeRef.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeDouble(this.returnValue);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.linkPageRef.read(inn);
      this.linkNodeRef.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.returnValue = inn.readDouble(this.returnValue);
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return this.returnValue;
   }

   public void setReturnValue(RingProgram program, RingProgramPage page, double value) {
      this.returnValue = value;
   }

   public boolean isPageTransition(RingProgram program, RingProgramPage curPage) {
      return true;
   }

   public short getTransitionPageIndex(RingProgram program, RingProgramPage curPage) {
      return !this.linkPageRef.fetchActualValue(program, curPage) ? -1 : this.linkPageRef.actualValue;
   }

   public boolean processTransition(EntityJointed ej, RingProgram program, RingProgramPage curPage, RingProgramPage nextPage, byte cNodeIndex, short nextPageIndex) {
      if (!this.linkNodeRef.fetchActualValue(program, curPage)) {
         program.setError(RingProgram.ErrorType.fetchValueError);
         return false;
      } else {
         byte nextPageNodeIndex = (byte)this.linkNodeRef.actualValue;
         if (nextPageNodeIndex >= 0 && nextPageNodeIndex < nextPage.nodeList.size()) {
            program.callStack.push(program.currentPageIndex, cNodeIndex);
            program.currentPageIndex = nextPageIndex;
            program.currentNodeIndex = nextPageNodeIndex;
            return true;
         } else {
            program.setError(RingProgram.ErrorType.invalidNodeIndexError);
            return false;
         }
      }
   }

   public int getRefCount() {
      return 2;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.linkPageRef;
         case 1:
            return this.linkNodeRef;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      return refNum == 0 ? 0 : 1;
   }

   public String getRefEntryName(int refNum) {
      return refNum == 0 ? "jbring.cmn.innode.pagenum.title" : "jbring.cmn.innode.nodenum.title";
   }

   public int getRefLineColor(int refNum) {
      return 16734296;
   }

   public boolean hasOPPageLinkEntry() {
      return !this.linkPageRef.isNodeRefMode;
   }

   public short getPageLinkNum() {
      return this.linkPageRef.value;
   }

   public void setPageLinkNum(int value) {
      this.linkPageRef.value = (short)value;
   }

   public boolean hasOPNodeLinkEntry() {
      return !this.linkNodeRef.isNodeRefMode;
   }

   public int getNodeLinkNum() {
      return this.linkNodeRef.value;
   }

   public void setNodeLinkNum(int value) {
      this.linkNodeRef.value = (short)value;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.linkPageRef.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.pagenum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.pagenum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 32);
         gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      }

      gs.setRefToggleable(0);
      if (this.linkNodeRef.isNodeRefMode) {
         gs.setParamEntryState(1, (String)"jbring.cmn.innode.nodenum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(1, (String)"jbring.cmn.nodenum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
      }

      gs.setRefToggleable(1);
   }

   public String getEntryValueFormattedStr(RingProgram program, RingProgramPage page, int entryNum) {
      if (entryNum == 0 && !this.linkPageRef.isNodeRefMode) {
         if (this.linkPageRef.value < 0) {
            return String.valueOf(-1);
         } else {
            RingProgramPage pageTarget = program.getPage(this.linkPageRef.value);
            return pageTarget != null ? pageTarget.getPageName() + "(P:" + this.linkPageRef.value + ")" : String.format(CompatHelper.formatLang("jbring.cmn.invalidcallpage"), this.linkPageRef.value);
         }
      } else {
         return null;
      }
   }

   public float getIconV() {
      return 32.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.ringcall.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.ringcall.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.var");
   }
}
