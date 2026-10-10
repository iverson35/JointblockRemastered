package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeTGUnitState extends AbstractRealTimeStateNode {
   protected static final int ENTRY_DETECT_TYPE = 0;
   private static final int DETECT_TYPE_INITMODE = 5;
   private static final int DETECT_TYPE_ISSLAVE = 10;
   private static final int DETECT_TYPE_SLAVESLOT = 15;
   private static final int DETECT_TYPE_MSE_CONN = 20;
   private static final int DETECT_TYPE_MST_CONN = 25;
   private static final int DETECT_TYPE_HEALTH = 30;
   private static final int DETECT_TYPE_ENERGY = 35;
   private static final int DETECT_TYPE_ENPOOL = 40;
   private static final int DETECT_TYPE_HPRATE = 45;
   private static final int DETECT_TYPE_ENRATE = 50;
   private static final int DETECT_TYPE_ENTITY_EID = 60;
   private static final int[] DETECT_TYPE_CONV = new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60};
   public byte detectType = 5;

   public AbstractRingNode newInstance() {
      return new RingNodeTGUnitState();
   }

   public byte getNodeTypeID() {
      return 80;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)convertToTypeIndex(DETECT_TYPE_CONV, (short)this.detectType);
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.detectType = (byte)convertToTypeValue(DETECT_TYPE_CONV, value);
         default:
      }
   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.detectType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.detectType = inn.readByte(this.detectType);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      switch (this.detectType) {
         case 5:
            this.returnValue = (double)ej.getInitLayerMode();
            return;
         case 10:
            this.returnValue = (double)booleanValue(ej.isSlaveMode());
            return;
         case 15:
            this.returnValue = (double)ej.getSlaveSlotNum();
            return;
         case 20:
            this.returnValue = (double)booleanValue(ej.isMasterEntityConnected());
            return;
         case 25:
            this.returnValue = (double)booleanValue(ej.isMasterTeConnected());
            return;
         case 30:
            this.returnValue = (double)ej.getHealth();
            return;
         case 35:
            this.returnValue = (double)ej.getEnergy();
            return;
         case 40:
            this.returnValue = (double)ej.getEFuelPool();
            return;
         case 45:
            if (ej.getMaxHealth() <= 0.0F) {
               this.returnValue = 0.0;
               return;
            }

            this.returnValue = (double)(ej.getHealth() / ej.getMaxHealth());
            return;
         case 50:
            if (ej.getMaxEnergy() <= 0.0F) {
               this.returnValue = 0.0;
               return;
            }

            this.returnValue = (double)(ej.getEnergy() / ej.getMaxEnergy());
            return;
         case 60:
            this.returnValue = (double)ej.getId();
            return;
         default:
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.unitStateDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconV() {
      return 128.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgunitstat.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgunitstat.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
