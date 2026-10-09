package jp.mc.ancientred.jointblock.assemble.ring;

import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeDRUseItem extends RingNodeDRUseKey {
   public AbstractRingNode newInstance() {
      return new RingNodeDRUseItem();
   }

   public byte getNodeTypeID() {
      return 56;
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (!this.seatNum.preFetchActualValue(program, page)) {
         return program.nextNodeIndex(page);
      } else {
         int seatNumFixed = this.seatNum.prefetchValue - 1;
         if (seatNumFixed >= 0 && seatNumFixed < ej.entitySeatPartArray.length) {
            short channel = (short)(seatNumFixed * 16 + 8 + this.keyOrItem);
            this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.input, channel);
         }

         return program.nextNodeIndex(page);
      }
   }

   public void doActiveEntityWork(EntityJointed ej) {
      short timeTick = this.whileTime.getActualValueShort();
      if (timeTick > 0) {
         int seatNumFixed = this.seatNum.actualValue - 1;
         if (seatNumFixed >= 0 && seatNumFixed < ej.entitySeatPartArray.length && ej.entitySeatPartArray[seatNumFixed] != null) {
            ej.entitySeatPartArray[seatNumFixed].ringItemDirections |= 1 << this.keyOrItem;
         }
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.seatNum.isNodeRefMode) {
         gs.setParamEntryState(0, "jbring.cmn.innode.seat.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, "jbring.cmn.seat.title", (Format)EntryFormats.seatNumValueFormatRing, 1, 4);
         gs.setParamButtonToggleAry(0, EntryFormats.oneOnlyButtonStates, 0);
      }

      gs.setRefToggleable(0);
      gs.setParamEntryState(1, "itemmot.act.title", (Format)EntryFormats.itemValueFormat, 0, 7);
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      super.setParamEntryWhileTime(2, gs);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 0.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.useitem.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.useitem.nodedesc");
   }
}
