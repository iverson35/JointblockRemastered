package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

public class RingNodeTGSeat extends AbstractRealTimeStateNode {
   private static final String ENTITY_PLAYER_STR = "EntityPlayer";
   protected static final int ENTRY_SEATNUM = 0;
   protected static final int ENTRY_DETECT_TYPE = 1;
   private static final int DETECT_TYPE_SEATTYPE = 10;
   private static final int DETECT_TYPE_RIDBYPLAYER = 20;
   private static final int DETECT_TYPE_RIDBYENTITY = 30;
   private static final int DETECT_TYPE_ENTITY_TYPE = 40;
   private static final int DETECT_TYPE_ENTITY_HASHA = 50;
   private static final int DETECT_TYPE_ENTITY_EID = 60;
   private static final int[] DETECT_TYPE_CONV = new int[]{10, 20, 30, 40, 50, 60};
   private static final int RET_SEATTYPE_SEAT = 1;
   private static final int RET_SEATTYPE_INTERFACE = 2;
   private static final int RET_SEATTYPE_CAM = 3;
   public ReffableByteValueEntry seatNum = new ReffableByteValueEntry(0, false, 1);
   public byte detectType = 10;

   public AbstractRingNode newInstance() {
      return new RingNodeTGSeat();
   }

   public byte getNodeTypeID() {
      return 74;
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
         EntitySeatPart target = null;
         int seatNumFixed = this.seatNum.actualValue - 1;
         if (seatNumFixed >= 0 && seatNumFixed < ej.entitySeatPartArray.length) {
            target = ej.entitySeatPartArray[seatNumFixed];
         }

         if (target != null && target.seatEnabled) {
            switch (this.detectType) {
               case 10:
                  if (target.isAsInteractableJoint) {
                     this.returnValue = 2.0;
                  } else if (target.isAsCameraJoint) {
                     this.returnValue = 3.0;
                  } else {
                     this.returnValue = 1.0;
                  }

                  return;
               case 20:
                  this.returnValue = (double)booleanValue(CompatHelper.getRiddenByEntity(target) != null && CompatHelper.getRiddenByEntity(target) instanceof Player);
                  return;
               case 30:
                  this.returnValue = (double)booleanValue(CompatHelper.getRiddenByEntity(target) != null);
                  return;
               case 40:
                  if (CompatHelper.getRiddenByEntity(target) == null) {
                     this.returnValue = Double.NaN;
                     return;
                  }

                  this.returnValue = (double)EntityAbstractType.getAbstractType(CompatHelper.getRiddenByEntity(target));
                  return;
               case 50:
                  if (CompatHelper.getRiddenByEntity(target) == null) {
                     this.returnValue = Double.NaN;
                     return;
                  } else {
                     String eStr;
                     if (CompatHelper.getRiddenByEntity(target) instanceof Player) {
                        eStr = ENTITY_PLAYER_STR;
                     } else {
                        ResourceLocation entityId = EntityType.getKey(CompatHelper.getRiddenByEntity(target).getType());
                        eStr = entityId == null ? null : entityId.toString();
                     }

                     if (eStr == null) {
                        this.returnValue = Double.NaN;
                        return;
                     }

                     int hashInt = eStr.hashCode();
                     this.returnValue = (double)((short)(hashInt ^ hashInt >> 16));
                     return;
                  }
               case 60:
                  if (CompatHelper.getRiddenByEntity(target) == null) {
                     this.returnValue = Double.NaN;
                     return;
                  }

                  this.returnValue = (double)CompatHelper.getRiddenByEntity(target).getId();
                  return;
               default:
            }
         } else {
            this.returnValue = Double.NaN;
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
      gs.setParamEntryState(1, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.seatDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconV() {
      return 192.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgseat.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgseat.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
