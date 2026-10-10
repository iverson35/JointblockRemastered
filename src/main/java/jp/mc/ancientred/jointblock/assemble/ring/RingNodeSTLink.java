package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTLink extends AbstractSyntaxRingNode implements INodeValueSettable, IOverPageLinker {
   private static final int ENTRY_LINKPAGEREF = 0;
   private static final int ENTRY_LINKNODEREF = 1;
   private static final int ENTRY_LINK_OUTSIDE_TYPE = 4;
   private static final int ENTRY_OTHERSLOTREF = 5;
   private static final byte LINK_OUTSIDE_TYPE_DONOT = 10;
   private static final byte LINK_OUTSIDE_TYPE_SELF = 20;
   private static final byte LINK_OUTSIDE_TYPE_MASTER = 30;
   private static final int[] LINK_OUTSIDE_TYPE_CONV = new int[]{10, 20, 30};
   private byte linkOutsideType = 10;
   private byte otherSlotNum = -1;
   private ReffableShortValueEntry linkPageRef = new ReffableShortValueEntry(-1, false, -1);
   private ReffableByteValueEntry linkNodeRef = new ReffableByteValueEntry(-1, false, -1);
   private long tickGenRTValueUpdate = -1L;
   public AbstractRingNode linkTargetNode = null;
   public RingProgramPage linkTargetPage = null;
   public RingProgram linkTargetProgram = null;

   public AbstractRingNode newInstance() {
      return new RingNodeSTLink();
   }

   public byte getNodeTypeID() {
      return 22;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.linkPageRef.value;
         case 1:
            return (double)this.linkNodeRef.value;
         case 2:
         case 3:
         default:
            return 0.0;
         case 4:
            return (double)convertToTypeIndex(LINK_OUTSIDE_TYPE_CONV, (short)this.linkOutsideType);
         case 5:
            return (double)this.otherSlotNum;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.linkPageRef.value = (short)((int)value);
            break;
         case 1:
            this.linkNodeRef.value = (byte)((int)value);
         case 2:
         case 3:
         default:
            break;
         case 4:
            byte convValue = (byte)convertToTypeValue(LINK_OUTSIDE_TYPE_CONV, value);
            if (convValue != this.linkOutsideType) {
               this.otherSlotNum = 0;
            }

            this.linkOutsideType = convValue;
            break;
         case 5:
            this.otherSlotNum = (byte)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.linkPageRef.write(out);
      this.linkNodeRef.write(out);
      out.writeByte(this.linkOutsideType);
      out.writeByte(this.otherSlotNum);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.linkPageRef.read(inn);
      this.linkNodeRef.read(inn);
      this.linkOutsideType = inn.readByte(this.otherSlotNum);
      this.otherSlotNum = inn.readByte(this.otherSlotNum);
   }

   private boolean isLinkOutside() {
      return this.linkOutsideType == 30 || this.linkOutsideType == 20;
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.updateTargetOnceInTick(program, page);
      return program.nextNodeIndex(page);
   }

   public double getArrayValue(RingProgram program, RingProgramPage page, int index) {
      this.updateTargetOnceInTick(program, page);
      return this.isLinkTargetBlank() ? Double.NaN : this.linkTargetNode.getArrayValue(this.linkTargetProgram, this.linkTargetPage, index);
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      this.updateTargetOnceInTick(program, page);
      return this.isLinkTargetBlank() ? Double.NaN : this.linkTargetNode.getReturnValue(this.linkTargetProgram, this.linkTargetPage);
   }

   public void setReturnValue(RingProgram program, RingProgramPage page, double newValue) {
      this.updateTargetOnceInTick(program, page);
      if (!this.isLinkTargetBlank()) {
         if (this.linkTargetNode instanceof INodeValueSettable) {
            ((INodeValueSettable)this.linkTargetNode).setReturnValue(this.linkTargetProgram, this.linkTargetPage, newValue);
         }

      }
   }

   private boolean isLinkTargetBlank() {
      return this.linkTargetNode == null || this.linkTargetProgram == null || this.linkTargetPage == null;
   }

   private void updateTargetOnceInTick(RingProgram program, RingProgramPage page) {
      if (program.tickGeneration != this.tickGenRTValueUpdate) {
         this.tickGenRTValueUpdate = program.tickGeneration;
         this.linkTargetNode = null;
         this.linkTargetPage = null;
         this.linkTargetProgram = null;
         this.updateLinkTarget(program, page);
      }

   }

   private void updateLinkTarget(RingProgram program, RingProgramPage page) {
      AbstractRingNode linkNode = this;
      int recursiveLimit = 5;

      do {
         RingNodeSTLink casted = (RingNodeSTLink)linkNode;
         if (!casted.linkPageRef.fetchActualValue(program, page) || !casted.linkNodeRef.fetchActualValue(program, page)) {
            return;
         }

         if (this.linkOutsideType == 20) {
            if (casted.otherSlotNum < 0 && casted.otherSlotNum >= 4) {
               return;
            }

            program = program.getSlefSlotPgm(casted.otherSlotNum);
            if (program == null || program.hadError) {
               return;
            }
         } else if (this.linkOutsideType == 30) {
            if (casted.otherSlotNum < 0 && casted.otherSlotNum >= 4) {
               return;
            }

            program = program.getMasterSlotPgm(casted.otherSlotNum);
            if (program == null || program.hadError) {
               return;
            }
         }

         page = program.getPage(casted.linkPageRef.actualValue);
         if (page == null) {
            return;
         }

         linkNode = page.getNode(casted.linkNodeRef.actualValue);
         if (linkNode == null) {
            return;
         }

         --recursiveLimit;
         if (recursiveLimit < 0) {
            return;
         }
      } while(linkNode instanceof RingNodeSTLink);

      this.linkTargetNode = (AbstractRingNode)linkNode;
      this.linkTargetPage = page;
      this.linkTargetProgram = program;
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

   public boolean canBeLinedWith(int refNum, AbstractRingNode node) {
      return !(node instanceof RingNodeSTLink);
   }

   public boolean hasOPPageLinkEntry() {
      return !this.linkPageRef.isNodeRefMode && !this.isLinkOutside();
   }

   public short getPageLinkNum() {
      return this.linkPageRef.value;
   }

   public void setPageLinkNum(int value) {
      this.linkPageRef.value = (short)value;
   }

   public boolean hasOPNodeLinkEntry() {
      return !this.linkNodeRef.isNodeRefMode && !this.isLinkOutside();
   }

   public int getNodeLinkNum() {
      return this.linkNodeRef.value;
   }

   public void setNodeLinkNum(int value) {
      this.linkNodeRef.value = (byte)value;
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
      gs.setParamEntryState(4, (String)"jbring.link.linkoutsidetype.title", (Format)EntryFormats.linkOutsideTypeFormat, 0, lastIdxOf(LINK_OUTSIDE_TYPE_CONV));
      gs.setParamButtonToggleAry(4, EntryFormats.selectLRButtonStates, 0);
      if (this.linkOutsideType == 20 || this.linkOutsideType == 30) {
         gs.setParamEntryState(5, (String)"jbring.link.otherslotnum.title", (Format)EntryFormats.simpleDecimalFormat, 0, 3);
         gs.setParamButtonToggleAry(5, EntryFormats.oneOnlyButtonStates, 0);
      }

   }

   public String getEntryValueFormattedStr(RingProgram program, RingProgramPage page, int entryNum) {
      if (this.isLinkOutside()) {
         return null;
      } else if (entryNum == 0 && !this.linkPageRef.isNodeRefMode) {
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

   public float getIconU() {
      return 352.0F;
   }

   public float getIconV() {
      return 192.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.link.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.link.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.link.rvaldesc");
   }
}
