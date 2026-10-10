package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeDREffectDo extends AbstractActivatableRingNode {
   private static final int ENTRY_ACTIONCH = 0;
   private static final int ENTRY_WHILETIME = 1;
   private static final int ENTRY_EFFECTSLOT = 2;
   public short actionChannel;
   public ReffableByteValueEntry effectSlot = new ReffableByteValueEntry(0, false, 0);

   public AbstractRingNode newInstance() {
      return new RingNodeDREffectDo();
   }

   public byte getNodeTypeID() {
      return 64;
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
         case 2:
            return (double)this.effectSlot.value;
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
            break;
         case 2:
            this.effectSlot.value = (byte)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeShort(this.actionChannel);
      out.writeByte(0);
      this.whileTime.write(out);
      this.effectSlot.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.whileTime.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.actionChannel = inn.readShort(this.actionChannel);
      inn.readByte((byte)0);
      this.whileTime.read(inn);
      this.effectSlot.read(inn);
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
      return this.whileTime.fetchActualValue(program, page) && this.effectSlot.fetchActualValue(program, page) ? this.whileTime.getActualValueShort() : 0;
   }

   public void doActiveChannelWork(EntityJointed ej, JointParts jp, ActiveRingSlots.ActiveSlot slot) {
      short timeTick = this.whileTime.getActualValueShort();
      byte effectSlotNum = this.effectSlot.actualValue;
      if (timeTick > 0) {
         int phase = this.calcPhaseUpdateTiming(timeTick);
         if (phase > 0) {
            ej.notifyClientEffect(jp, effectSlotNum);
         }
      }
   }

   public int getRefCount() {
      return 2;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 1:
            return this.whileTime;
         case 2:
            return this.effectSlot;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 1;
         case 1:
            return 2;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 0:
            return "jbring.cmn.innode.while.title";
         case 1:
            return "jbring.cmn.innode.slot.title";
         default:
            return null;
      }
   }

   public int getRefCharIndex(int refNum) {
      return refNum == 0 ? 11 : 16;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, "jbring.cmn.channel.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
      gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 1);
      gs.setParamEntryState(2, "keymot.effectslot.title", (Format)EntryFormats.simpleDecimalFormat, 0, 15);
      gs.setParamButtonToggleAry(2, EntryFormats.defaultButtonStates, 1);
      super.setParamEntryWhileTime(1, gs);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 192.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.effectdo.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.effectdo.nodedesc");
   }
}
