package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeTGItem extends AbstractRealTimeStateNode {
   protected static final int ENTRY_SEATNUM = 0;
   protected static final int ENTRY_KEYORITEM = 1;
   public byte keyOrItem = 0;
   public ReffableByteValueEntry seatNum = new ReffableByteValueEntry(0, false, 1);

   public AbstractRingNode newInstance() {
      return new RingNodeTGItem();
   }

   public byte getNodeTypeID() {
      return 72;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.seatNum.value;
         case 1:
            return (double)this.keyOrItem;
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
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.seatNum.write(out);
      out.writeByte(this.keyOrItem);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.seatNum.read(inn);
      this.keyOrItem = inn.readByte(this.keyOrItem);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (!this.seatNum.fetchActualValue(program, page)) {
         this.returnValue = Double.NaN;
      } else {
         EntitySeatPart entitySeats = null;
         int seatNumFixed = this.seatNum.actualValue - 1;
         if (seatNumFixed >= 0 && seatNumFixed < ej.entitySeatPartArray.length) {
            entitySeats = ej.entitySeatPartArray[seatNumFixed];
         }

         if (entitySeats == null) {
            this.returnValue = Double.NaN;
         } else {
            if (entitySeats.isRiddenbyUsedItem(this.keyOrItem)) {
               this.returnValue = 1.0;
            } else {
               this.returnValue = 0.0;
            }

         }
      }
   }

   public int getRefCount() {
      return 1;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.seatNum;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 0;
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
      if (this.seatNum.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.seat.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.seat.title", (Format)EntryFormats.seatNumValueFormatRing, 1, 4);
         gs.setParamButtonToggleAry(0, EntryFormats.oneOnlyButtonStates, 0);
      }

      gs.setRefToggleable(0);
      gs.setParamEntryState(1, (String)"itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconU() {
      return 352.0F;
   }

   public float getIconV() {
      return 112.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgitem.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgitem.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.tgitem.rvaldesc");
   }
}
