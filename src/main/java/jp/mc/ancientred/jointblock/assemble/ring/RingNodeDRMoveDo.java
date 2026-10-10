package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;

public class RingNodeDRMoveDo extends AbstractActivatableRingNode {
   private static final int ENTRY_MOVEDIRCH = 0;
   private static final int ENTRY_MOVEWAY = 1;
   private static final int ENTRY_WHILETIME = 2;
   public byte moveDirCh;
   public byte moveWay;

   public AbstractRingNode newInstance() {
      return new RingNodeDRMoveDo();
   }

   public byte getNodeTypeID() {
      return 40;
   }

   public int getAuthNeeds() {
      return 4;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.moveDirCh;
         case 1:
            return (double)this.moveWay;
         case 2:
            return (double)this.whileTime.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            if ((byte)((int)value) != this.moveDirCh) {
               this.moveWay = 0;
            }

            this.moveDirCh = (byte)((int)value);
            break;
         case 1:
            this.moveWay = (byte)((int)value);
            break;
         case 2:
            this.whileTime.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.moveDirCh);
      out.writeByte(this.moveWay);
      this.whileTime.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.whileTime.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.moveDirCh = inn.readByte(this.moveDirCh);
      this.moveWay = inn.readByte(this.moveWay);
      this.whileTime.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.whileTime.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.move, (short)this.moveDirCh);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return !this.whileTime.fetchActualValue(program, page) ? 0 : this.whileTime.getActualValueShort();
   }

   public void doActiveEntityWork(EntityJointed ej) {
      short timeTick = this.whileTime.getActualValueShort();
      if (timeTick > 0) {
         if (ej.entitySeatPart0 != null && !ej.entitySeatPart0.lockMoveDR) {
            ej.entitySeatPart0.hasRINGMoveDR = true;
            boolean isPrimal = (this.moveWay & 1) == 0;
            byte isSingleOrDouble = (byte)((this.moveWay & 2) != 0 ? 2 : 1);
            switch (this.moveDirCh) {
               case 0:
                  ej.entitySeatPart0.moveForwardDR = isSingleOrDouble;
                  if (isPrimal) {
                     ej.entitySeatPart0.moveForwardAmount = 1.0F;
                  } else {
                     ej.entitySeatPart0.moveForwardAmount = -1.0F;
                  }
                  break;
               case 1:
                  ej.entitySeatPart0.moveStrafingDR = isSingleOrDouble;
                  if (isPrimal) {
                     ej.entitySeatPart0.moveStrafingAmount = 1.0F;
                  } else {
                     ej.entitySeatPart0.moveStrafingAmount = -1.0F;
                  }
                  break;
               case 2:
                  ej.entitySeatPart0.moveUpDownDR = isSingleOrDouble;
                  if (isPrimal) {
                     ej.entitySeatPart0.moveUp = true;
                  } else {
                     ej.entitySeatPart0.moveUp = false;
                  }
            }
         }

      }
   }

   public int getRefCount() {
      return 1;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 2:
            return this.whileTime;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      return 2;
   }

   public String getRefEntryName(int refNum) {
      return "jbring.cmn.innode.while.title";
   }

   public int getRefCharIndex(int refNum) {
      return 11;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, "jbring.movedo.movedirch.title", (Format)EntryFormats.moveDirChnValueFormat, 0, 2);
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      switch (this.moveDirCh) {
         case 0:
            gs.setParamEntryState(1, "jbring.movedo.way.title", (Format)EntryFormats.moveWayLRValueFormat, 0, 3);
            break;
         case 1:
            gs.setParamEntryState(1, "jbring.movedo.way.title", (Format)EntryFormats.moveWayFBValueFormat, 0, 3);
            break;
         case 2:
            gs.setParamEntryState(1, "jbring.movedo.way.title", (Format)EntryFormats.moveWayUDValueFormat, 0, 3);
      }

      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      super.setParamEntryWhileTime(2, gs);
   }

   public float getIconU() {
      return 352.0F;
   }

   public float getIconV() {
      return 64.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.movedo.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.movedo.nodedesc");
   }
}
