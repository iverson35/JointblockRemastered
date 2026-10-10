package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTJumpIf extends AbstractSyntaxRingNode {
   private static final int ENTRY_REFNODE = 0;
   private static final int ENTRY_JUMPTO = 1;
   private static final int ENTRY_JUMPWHEN = 2;
   protected static final int JUMPWHEN_TRUE = 10;
   protected static final int JUMPWHEN_FALSE = 20;
   protected static final int JUMPWHEN_ERROR = 30;
   protected static final int JUMPWHEN_NOTERROR = 40;
   protected static final int[] JUMPWHEN_CONV = new int[]{10, 20, 30, 40};
   public byte refNode = -1;
   public byte jumpTo = -1;
   public byte jumpWhen;

   public AbstractRingNode newInstance() {
      return new RingNodeSTJumpIf();
   }

   public byte getNodeTypeID() {
      return 8;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.refNode;
         case 1:
            return (double)this.jumpTo;
         case 2:
            return (double)convertToTypeIndex(JUMPWHEN_CONV, (short)this.jumpWhen);
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.refNode = (byte)((int)value);
            break;
         case 1:
            this.jumpTo = (byte)((int)value);
            break;
         case 2:
            this.jumpWhen = (byte)convertToTypeValue(JUMPWHEN_CONV, value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.refNode);
      out.writeByte(this.jumpTo);
      out.writeByte(this.jumpWhen);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.refNode = inn.readByte(this.refNode);
      this.jumpTo = inn.readByte(this.jumpTo);
      this.jumpWhen = inn.readByte(this.jumpWhen);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      AbstractRingNode node = page.getNode(this.refNode);
      if (node == null) {
         return program.nextNodeIndex(page);
      } else {
         boolean doJump = false;
         double rv;
         switch (this.jumpWhen) {
            case 20:
               doJump = !node.returnStateBool(program, page);
               break;
            case 30:
               rv = node.getReturnValue(program, page);
               doJump = rv != rv;
               break;
            case 40:
               rv = node.getReturnValue(program, page);
               doJump = rv == rv;
               break;
            default:
               doJump = node.returnStateBool(program, page);
         }

         return doJump ? this.jumpTo : program.nextNodeIndex(page);
      }
   }

   public int getRefCount() {
      return 2;
   }

   public int getRefEntryNum(int refNum) {
      return refNum == 0 ? 0 : 1;
   }

   public String getRefEntryName(int refNum) {
      return refNum == 0 ? "jbring.cmn.innode.title" : "jbring.jump.jumpto.title";
   }

   public boolean isRefLineTowardsThis(int refNum) {
      return refNum == 0;
   }

   public boolean isCurvedLine(int refNum) {
      return refNum == 1;
   }

   public int getRefLineColor(int refNum) {
      return refNum == 0 ? 16734296 : 3407667;
   }

   public int getRefCharIndex(int refNum) {
      return refNum == 0 ? 8 : 10;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.cmn.innode.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      gs.setParamEntryState(1, (String)"jbring.jump.jumpto.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
      gs.setParamEntryState(2, (String)"jbring.jump.jumpwhen.title", (Format)EntryFormats.jumpWhenValueFormat, 0, lastIdxOf(JUMPWHEN_CONV));
      gs.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconV() {
      return 64.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.jump.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.jump.nodedesc");
   }
}
