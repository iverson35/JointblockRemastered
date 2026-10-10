package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class RingNodeTGPlayer extends AbstractRealTimeStateNode {
   protected static final int ENTRY_SEATNUM = 0;
   protected static final int ENTRY_DETECT_TYPE = 1;
   private static final int DETECT_TYPE_PITCH = 10;
   private static final int DETECT_TYPE_YAWREL = 20;
   private static final int DETECT_TYPE_YAWABS = 30;
   private static final int DETECT_TYPE_SWING = 40;
   private static final int[] DETECT_TYPE_CONV = new int[]{10, 20, 30, 40};
   public ReffableByteValueEntry seatNum = new ReffableByteValueEntry(0, false, 1);
   public byte detectType = 10;

   public AbstractRingNode newInstance() {
      return new RingNodeTGPlayer();
   }

   public byte getNodeTypeID() {
      return 76;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.seatNum.value;
         case 1:
            return (double)convertToTypeIndex(DETECT_TYPE_CONV, (short)this.detectType);
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
            this.detectType = (byte)convertToTypeValue(DETECT_TYPE_CONV, value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.seatNum.write(out);
      out.writeByte(this.detectType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.seatNum.read(inn);
      this.detectType = inn.readByte(this.detectType);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (!this.seatNum.fetchActualValue(program, page)) {
         this.returnValue = Double.NaN;
      } else {
         Player target = null;
         int seatNumFixed = this.seatNum.actualValue - 1;
         Entity temp;
         if (seatNumFixed == 0 && (temp = ej.getMasterEntityIfConnected()) instanceof Player) {
            target = (Player)temp;
         } else if (seatNumFixed >= 0 && seatNumFixed < ej.entitySeatPartArray.length && ej.entitySeatPartArray[seatNumFixed] != null && CompatHelper.getRiddenByEntity(ej.entitySeatPartArray[seatNumFixed]) instanceof Player) {
            target = (Player)CompatHelper.getRiddenByEntity(ej.entitySeatPartArray[seatNumFixed]);
         }

         if (target == null) {
            this.returnValue = Double.NaN;
         } else {
            switch (this.detectType) {
               case 10:
                  this.returnValue = (double)(target.getXRot() % 180.0F);
                  return;
               case 20:
                  this.returnValue = (double)RingUtils.range180YawValue(target.getYHeadRot() - ej.getYRot());
                  return;
               case 30:
                  this.returnValue = (double)RingUtils.range180YawValue(target.getYHeadRot());
                  return;
               case 40:
                  this.returnValue = (double)target.attackAnim;
                  return;
               default:
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
      gs.setParamEntryState(1, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.playerDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconU() {
      return 352.0F;
   }

   public float getIconV() {
      return 160.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgplayerstats.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgplayerstats.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
