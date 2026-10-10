package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeSTWait extends AbstractSyntaxRingNode {
   private static final int ENTRY_WHILETIME = 0;
   public ReffableShortValueEntry whileTime = new ReffableShortValueEntry(1, false, 1);
   public short remainTick = -1;

   public AbstractRingNode newInstance() {
      return new RingNodeSTWait();
   }

   public byte getNodeTypeID() {
      return 6;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.whileTime.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.whileTime.value = (short)((int)value);
         default:
      }
   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.whileTime.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.whileTime.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.whileTime.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.whileTime.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public void onProgramInit(RingProgram program, RingProgramPage page) {
      this.remainTick = -1;
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (program.isExecuteTypeShot()) {
         return program.currentNodeIndex;
      } else {
         if (this.remainTick < 0) {
            if (!this.whileTime.fetchActualValue(program, page)) {
               return program.nextNodeIndex(page);
            }

            this.remainTick = this.whileTime.getActualValueShort();
         }

         return --this.remainTick >= 0 ? program.currentNodeIndex : program.nextNodeIndex(page);
      }
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return this.remainTick < 0 ? 0.0 : (double)this.remainTick;
   }

   public int getRefCount() {
      return 1;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.whileTime;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      return 0;
   }

   public String getRefEntryName(int refNum) {
      return "jbring.cmn.innode.while.title";
   }

   public int getRefCharIndex(int refNum) {
      return 11;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.whileTime.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.while.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.timetick.title", (Format)EntryFormats.takesTimeValueFormat, 1, 32767);
      }

      gs.setRefToggleable(0);
   }

   public float getIconV() {
      return 48.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.wait.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.wait.nodedesc");
   }
}
