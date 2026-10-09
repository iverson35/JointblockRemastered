package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeDRChannelClear extends AbstractRingNode {
   protected static final int ENTRY_SLOTTYPE = 0;
   protected static final int ENTRY_CHANNEL = 1;
   protected static final int ENTRY_SEATNUM = 2;
   public byte slotType = 10;
   public short channel = 0;
   public ReffableByteValueEntry seatNum = new ReffableByteValueEntry(0, false, 1);

   public AbstractRingNode newInstance() {
      return new RingNodeDRChannelClear();
   }

   public byte getNodeTypeID() {
      return 50;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)convertToTypeIndex(SLOTTYPE_CONV, (short)this.slotType);
         case 1:
            return (double)this.channel;
         case 2:
            return (double)this.seatNum.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            byte convValue = (byte)convertToTypeValue(SLOTTYPE_CONV, value);
            if (convValue != this.slotType) {
               this.channel = 0;
               this.seatNum.reset();
            }

            this.slotType = convValue;
            break;
         case 1:
            this.channel = (short)((int)value);
            break;
         case 2:
            this.seatNum.value = (byte)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.slotType);
      out.writeShort(this.channel);
      this.seatNum.write(out);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.slotType = inn.readByte(this.slotType);
      this.channel = inn.readShort(this.channel);
      this.seatNum.read(inn);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (ej.activeRingSlots == null) {
         return program.nextNodeIndex(page);
      } else {
         if (this.slotType == 40) {
            if (!this.seatNum.fetchActualValue(program, page)) {
               return program.nextNodeIndex(page);
            }

            int channelNum = this.channel + (this.seatNum.actualValue - 1) * 16;
            int seatNumFixed = this.seatNum.actualValue - 1;
            if (seatNumFixed >= 0 && seatNumFixed < ej.entitySeatPartArray.length) {
               ej.activeRingSlots.clearChannelSlot(this.slotTypeNumToEnum(this.slotType), (short)channelNum);
            }
         } else {
            ej.activeRingSlots.clearChannelSlot(this.slotTypeNumToEnum(this.slotType), this.channel);
         }

         return program.nextNodeIndex(page);
      }
   }

   protected short fixChannelNum() {
      short channelNum = this.channel;
      if (this.slotType == 40) {
         channelNum = (short)(channelNum + (this.seatNum.actualValue - 1) * 16);
      }

      return channelNum;
   }

   public int getRefCount() {
      return 1;
   }

   public boolean isRefEntryDisabled() {
      return this.slotType != 40;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 2:
            return this.seatNum;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 2;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      return "jbring.cmn.innode.seat.title";
   }

   public int getRefCharIndex(int refNum) {
      return 16;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, "jbring.cmn.channeltype.title", (Format)EntryFormats.channelTypeValueFormat, 0, lastIdxOf(SLOTTYPE_CONV));
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      switch (this.slotType) {
         case 10:
         default:
            gs.setParamEntryState(1, "jbring.cmn.channel.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
            gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
            break;
         case 20:
            gs.setParamEntryState(1, "jbring.cmn.channel.title", (Format)EntryFormats.simpleDecimalFormat, 0, 127);
            gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
            break;
         case 30:
            gs.setParamEntryState(1, "jbring.cmn.channel.title", (Format)EntryFormats.moveDirChnValueFormat, 0, 3);
            gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
            break;
         case 40:
            gs.setParamEntryState(1, "jbring.cmn.channel.title", (Format)EntryFormats.ringInputSlotValueFormat, 0, 15);
            gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 1);
            if (this.seatNum.isNodeRefMode) {
               gs.setParamEntryState(2, "jbring.cmn.innode.seat.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
            } else {
               gs.setParamEntryState(2, "jbring.cmn.seat.title", (Format)EntryFormats.seatNumValueFormatRing, 1, 4);
               gs.setParamButtonToggleAry(2, EntryFormats.oneOnlyButtonStates, 0);
            }

            gs.setRefToggleable(2);
      }

   }

   public float getIconU() {
      return 352.0F;
   }

   public float getIconV() {
      return 144.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.channelclear.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.channelclear.nodedesc");
   }
}
