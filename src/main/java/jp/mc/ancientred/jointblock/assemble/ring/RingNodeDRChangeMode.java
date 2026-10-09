package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeDRChangeMode extends AbstractRingNode {
   protected static final int ENTRY_MODETYPE = 0;
   protected static final int ENTRY_TARGET_NUM = 1;
   protected static final int ENTRY_TOGGLE_MSUB = 2;
   protected static final int MODETYPE_INIT = 10;
   protected static final int MODETYPE_SLAVE = 20;
   protected static final int[] MODETYPE_CONV = new int[]{10, 20};
   public byte modeType = 10;
   public byte targetNum = 0;
   public byte mainSubType = 0;
   public byte returnValue = 0;

   public AbstractRingNode newInstance() {
      return new RingNodeDRChangeMode();
   }

   public byte getNodeTypeID() {
      return 52;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)convertToTypeIndex(MODETYPE_CONV, (short)this.modeType);
         case 1:
            return (double)this.targetNum;
         case 2:
            return (double)this.mainSubType;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            byte convValue = (byte)convertToTypeValue(MODETYPE_CONV, value);
            if (convValue != this.modeType) {
               this.targetNum = 0;
               this.mainSubType = 0;
            }

            this.modeType = convValue;
            break;
         case 1:
            this.targetNum = (byte)((int)value);
            break;
         case 2:
            this.mainSubType = (byte)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.modeType);
      out.writeByte(this.targetNum);
      out.writeByte(this.mainSubType);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.returnValue);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.modeType = inn.readByte(this.modeType);
      this.targetNum = inn.readByte(this.targetNum);
      this.mainSubType = inn.readByte(this.mainSubType);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.returnValue = inn.readByte(this.returnValue);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.returnValue = 0;
      if (this.modeType == 20) {
         if (ej.changeSlaveSlotNum(this.targetNum)) {
            this.returnValue = 1;
         }
      } else if (ej.changeInitValueMode(this.targetNum, this.mainSubType)) {
         this.returnValue = 1;
      }

      return program.nextNodeIndex(page);
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return (double)this.returnValue;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, "jbring.cmn.type.title", (Format)EntryFormats.changeModeTypeValueFormat, 0, lastIdxOf(MODETYPE_CONV));
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      switch (this.modeType) {
         case 10:
         default:
            gs.setParamEntryState(1, "jbring.changemode.initto.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
            gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
            gs.setParamEntryState(2, "keymot.toggletype.title", (Format)EntryFormats.mainSubTypeValueFormat, 0, 2);
            gs.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
            break;
         case 20:
            gs.setParamEntryState(1, "jbring.changemode.slaveto.title", (Format)EntryFormats.modeChangeValueFormat, -1, 10);
            gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
      }

   }

   public float getIconV() {
      return 160.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.changemode.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.changemode.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.changemode.rvaldesc");
   }
}
