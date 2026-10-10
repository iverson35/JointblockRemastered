package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeDRActionDo extends AbstractActivatableRingNode {
   private static final int ENTRY_ACTIONCH = 0;
   private static final int ENTRY_WHILETIME = 1;
   public short actionChannel;

   public AbstractRingNode newInstance() {
      return new RingNodeDRActionDo();
   }

   public byte getNodeTypeID() {
      return 38;
   }

   public int getAuthNeeds() {
      return 2;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.actionChannel;
         case 1:
            return (double)this.whileTime.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.actionChannel = (short)((int)value);
            break;
         case 1:
            this.whileTime.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeShort(this.actionChannel);
      out.writeByte(0);
      this.whileTime.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.whileTime.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.actionChannel = inn.readShort(this.actionChannel);
      inn.readByte((byte)0);
      this.whileTime.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.whileTime.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.action, this.actionChannel);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return !this.whileTime.fetchActualValue(program, page) ? 0 : this.whileTime.getActualValueShort();
   }

   public void doActiveChannelWork(EntityJointed ej, JointParts jp, ActiveRingSlots.ActiveSlot slot) {
      short timeTick = this.whileTime.getActualValueShort();
      if (timeTick > 0) {
         int phase = this.calcPhaseUpdateTiming(timeTick);
         if (phase > 0) {
            jp.dispatchModelAction(ej);
         }
      }
   }

   public int getRefCount() {
      return 1;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 1:
            return this.whileTime;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      return 1;
   }

   public String getRefEntryName(int refNum) {
      return "jbring.cmn.innode.while.title";
   }

   public int getRefCharIndex(int refNum) {
      return 11;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, "jbring.cmn.channel.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
      gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      super.setParamEntryWhileTime(1, gs);
   }

   public float getIconU() {
      return 352.0F;
   }

   public float getIconV() {
      return 96.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.actdo.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.actdo.nodedesc");
   }
}
