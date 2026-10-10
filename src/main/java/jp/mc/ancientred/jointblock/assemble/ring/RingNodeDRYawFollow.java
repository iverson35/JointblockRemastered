package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class RingNodeDRYawFollow extends AbstractActivatableRingNode {
   private static final int ENTRY_SEATNUM = 0;
   private static final int ENTRY_WHILETIME = 1;
   private static final int ENTRY_FOLLOWTYPE = 2;
   private static final int ENTRY_YAWSPEED = 3;
   private static final int FOLLOW_TYPE_CONST = 10;
   private static final int FOLLOW_TYPE_ACCEL = 20;
   private static final int FOLLOW_TYPE_DECEL = 30;
   private static final int[] FOLLOW_TYPE_CONV = new int[]{10, 20, 30};
   public ReffableByteValueEntry seatNum = new ReffableByteValueEntry(0, false, 1);
   public byte followType = 10;
   public ReffableDoubleValueEntry yawSpeed = new ReffableDoubleValueEntry(0.0, false, 0.0);

   public AbstractRingNode newInstance() {
      return new RingNodeDRYawFollow();
   }

   public byte getNodeTypeID() {
      return 46;
   }

   public int getAuthNeeds() {
      return 4;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.seatNum.value;
         case 1:
            return (double)this.whileTime.value;
         case 2:
            return (double)convertToTypeIndex(FOLLOW_TYPE_CONV, (short)this.followType);
         case 3:
            return this.yawSpeed.value;
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
            this.whileTime.value = (short)((int)value);
            break;
         case 2:
            this.followType = (byte)convertToTypeValue(FOLLOW_TYPE_CONV, value);
            break;
         case 3:
            this.yawSpeed.value = value;
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.seatNum.write(out);
      out.writeByte(this.followType);
      this.whileTime.write(out);
      this.yawSpeed.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.seatNum.writeRunningData(out);
      this.whileTime.writeRunningData(out);
      this.yawSpeed.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.seatNum.read(inn);
      this.followType = inn.readByte(this.followType);
      this.whileTime.read(inn);
      this.yawSpeed.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.seatNum.readRunningData(inn);
      this.whileTime.readRunningData(inn);
      this.yawSpeed.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.move, (short)3);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return this.seatNum.fetchActualValue(program, page) && this.whileTime.fetchActualValue(program, page) && this.yawSpeed.fetchActualValue(program, page) ? this.whileTime.getActualValueShort() : 0;
   }

   public void doActiveEntityWork(EntityJointed ej) {
      short timeTick = this.whileTime.getActualValueShort();
      float yawSpeed = (float)this.yawSpeed.actualValue;
      if (timeTick > 0) {
         if (!(yawSpeed <= 0.0F)) {
            if (ej.entitySeatPart0 != null && !ej.entitySeatPart0.lockMoveDR) {
               Player player = ej.getRidingPlayer(this.seatNum.actualValue - 1);
               if (player != null) {
                  float ejYaw = ej.getYRot() % 360.0F;

                  float plYaw;
                  for (plYaw = player.getYRot() % 360.0F; plYaw - ejYaw < -180.0F; ejYaw -= 360.0F) {
                  }

                  while (plYaw - ejYaw >= 180.0F) {
                     ejYaw += 360.0F;
                  }

                  float diff = plYaw - ejYaw;
                  float absDiff = Math.abs(diff);
                  if (absDiff < 1.0E-4F) {
                     return;
                  }

                  ej.entitySeatPart0.hasRINGMoveDR = true;
                  ej.entitySeatPart0.yawChange = true;
                  ej.entitySeatPart0.yawAdd = true;
                  switch (this.followType) {
                     case 10:
                     default:
                        break;
                     case 20:
                        yawSpeed *= Mth.clamp(1.0F - absDiff / 180.0F, 0.1F, 1.0F);
                        break;
                     case 30:
                        yawSpeed *= Mth.clamp(absDiff / 180.0F, 0.1F, 1.0F);
                  }

                  if (diff > 0.0F) {
                     ej.entitySeatPart0.yawValue = Math.min(diff, yawSpeed) % 360.0F;
                  } else {
                     ej.entitySeatPart0.yawValue = Math.max(diff, yawSpeed * -1.0F) % 360.0F;
                  }
               }
            }

         }
      }
   }

   public int getRefCount() {
      return 3;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.seatNum;
         case 1:
            return this.whileTime;
         case 2:
         default:
            return null;
         case 3:
            return this.yawSpeed;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 0;
         case 1:
            return 1;
         case 2:
            return 3;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 0:
            return "jbring.cmn.innode.seat.title";
         case 1:
         default:
            return "jbring.cmn.innode.while.title";
         case 2:
            return "jbring.yawflw.innode.yawspeed.title";
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 0:
            return 16;
         case 1:
         default:
            return 11;
         case 2:
            return 13;
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
      super.setParamEntryWhileTime(1, gs);
      gs.setParamEntryState(2, "jbring.cmn.type.title", (Format)EntryFormats.followYawTypeValueFormat, 0, lastIdxOf(FOLLOW_TYPE_CONV));
      gs.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      if (this.yawSpeed.isNodeRefMode) {
         gs.setParamEntryState(3, "jbring.yawflw.innode.yawspeed.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(3, "jbring.yawflw.yawspeed.title", (Format)EntryFormats.yawDecimalAngleValueFormat, 0, 180);
         gs.setParamButtonToggleAry(3, EntryFormats.hasDecimalButtonStates, 1);
      }

      gs.setRefToggleable(3);
   }

   public float getIconV() {
      return 112.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.yawflw.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.yawflw.nodedesc");
   }
}
