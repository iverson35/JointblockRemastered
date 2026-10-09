package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeDRUseKey extends AbstractActivatableRingNode {
   protected static final int ENTRY_SEATNUM = 0;
   protected static final int ENTRY_KEYORITEM = 1;
   protected static final int ENTRY_WHILETIME = 2;
   protected static final int ENTRY_SINGLEDOUBLE = 3;
   public byte keyOrItem = 0;
   public byte singleOrDouble = 0;
   public ReffableByteValueEntry seatNum = new ReffableByteValueEntry(0, false, 1);

   public AbstractRingNode newInstance() {
      return new RingNodeDRUseKey();
   }

   public byte getNodeTypeID() {
      return 54;
   }

   public int getAuthNeeds() {
      return 8;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.seatNum.value;
         case 1:
            return (double)this.keyOrItem;
         case 2:
            return (double)this.whileTime.value;
         case 3:
            return (double)this.singleOrDouble;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.seatNum.value = (byte)((int)value);
            break;
         case 1:
            this.keyOrItem = (byte)((int)value);
            break;
         case 2:
            this.whileTime.value = (short)((int)value);
            break;
         case 3:
            this.singleOrDouble = (byte)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.seatNum.write(out);
      out.writeByte(this.keyOrItem);
      this.whileTime.write(out);
      out.writeByte(this.singleOrDouble);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.seatNum.writeRunningData(out);
      this.whileTime.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.seatNum.read(inn);
      this.keyOrItem = inn.readByte(this.keyOrItem);
      this.whileTime.read(inn);
      this.singleOrDouble = inn.readByte(this.singleOrDouble);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.seatNum.readRunningData(inn);
      this.whileTime.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (!this.seatNum.preFetchActualValue(program, page)) {
         return program.nextNodeIndex(page);
      } else {
         int seatNumFixed = this.seatNum.prefetchValue - 1;
         if (seatNumFixed >= 0 && seatNumFixed < ej.entitySeatPartArray.length) {
            short channel = (short)(seatNumFixed * 16 + 0 + this.keyOrItem);
            this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.input, channel);
         }

         return program.nextNodeIndex(page);
      }
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return this.seatNum.fetchActualValue(program, page) && this.whileTime.fetchActualValue(program, page) ? this.whileTime.getActualValueShort() : 0;
   }

   public void doActiveEntityWork(EntityJointed ej) {
      short timeTick = this.whileTime.getActualValueShort();
      if (timeTick > 0) {
         int seatNumFixed = this.seatNum.actualValue - 1;
         if (seatNumFixed >= 0 && seatNumFixed < ej.entitySeatPartArray.length && ej.entitySeatPartArray[seatNumFixed] != null) {
            if (this.singleOrDouble == 0) {
               ej.entitySeatPartArray[seatNumFixed].ringKeyDirections |= 1 << this.keyOrItem;
            } else {
               ej.entitySeatPartArray[seatNumFixed].ringDblKeyDirections |= 1 << this.keyOrItem;
            }
         }
      }
   }

   public int getRefCount() {
      return 2;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.seatNum;
         case 2:
            return this.whileTime;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 0;
         case 1:
            return 2;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      return refNum == 0 ? "jbring.cmn.innode.seat.title" : "jbring.cmn.innode.while.title";
   }

   public int getRefCharIndex(int refNum) {
      return refNum == 0 ? 16 : 11;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.seatNum.isNodeRefMode) {
         gs.setParamEntryState(0, "jbring.cmn.innode.seat.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, "jbring.cmn.seat.title", (Format)EntryFormats.seatNumValueFormatRing, 1, 4);
         gs.setParamButtonToggleAry(0, EntryFormats.oneOnlyButtonStates, 0);
      }

      gs.setRefToggleable(0);
      gs.setParamEntryState(1, "keymot.key.title", (Format)EntryFormats.keyValueFormat, 0, 7);
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      super.setParamEntryWhileTime(2, gs);
      gs.setParamEntryState(3, "jbring.cmn.singleordouble.title", (Format)EntryFormats.singleordoubleValueFormat, 0, 1);
      gs.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 16.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.usekey.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.usekey.nodedesc");
   }
}
