package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class RingNodeTGEntity extends AbstractRealTimeStateNode {
   protected static final int ENTRY_ENTITYID = 0;
   protected static final int ENTRY_DETECT_TYPE = 1;
   private static final int DETECT_TYPE_ISALIVE = 10;
   private static final int DETECT_TYPE_WPOSX = 20;
   private static final int DETECT_TYPE_WPOSY = 22;
   private static final int DETECT_TYPE_WPOSZ = 24;
   private static final int DETECT_TYPE_WPOSVEC = 26;
   private static final int DETECT_TYPE_PITCH = 30;
   private static final int DETECT_TYPE_YAW = 32;
   private static final int DETECT_TYPE_EYEPOSVEC = 36;
   private static final int DETECT_TYPE_LOOKVEC = 38;
   private static final int DETECT_TYPE_RIDINGENTITY = 40;
   private static final int DETECT_TYPE_RIDBYENTITY = 42;
   private static final int DETECT_TYPE_ISMOVINGA = 50;
   private static final int DETECT_TYPE_MOVINGUP = 54;
   private static final int DETECT_TYPE_MOVINGDW = 56;
   private static final int DETECT_TYPE_RIDING_EID = 60;
   private static final int DETECT_TYPE_RIDDEN_EID = 62;
   private static final int DETECT_TYPE_LATTACK_EID = 64;
   private static final int DETECT_TYPE_REVENGE_EID = 66;
   private static final int DETECT_TYPE_ENTITY_TYPE = 80;
   private static final int DETECT_TYPE_ENTITY_HASHA = 82;
   private static final int[] DETECT_TYPE_CONV = new int[]{10, 20, 22, 24, 26, 30, 32, 36, 38, 40, 42, 50, 54, 56, 60, 62, 64, 66, 80, 82};
   public ReffableIntValueEntry entityId = new ReffableIntValueEntry(0, false, 0);
   public byte detectType = 10;

   public AbstractRingNode newInstance() {
      return new RingNodeTGEntity();
   }

   public byte getNodeTypeID() {
      return 90;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.entityId.value;
         case 1:
            return (double)convertToTypeIndex(DETECT_TYPE_CONV, (short)this.detectType);
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.entityId.value = (int)value;
            break;
         case 1:
            this.detectType = (byte)convertToTypeValue(DETECT_TYPE_CONV, value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.entityId.write(out);
      out.writeByte(this.detectType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.entityId.read(inn);
      this.detectType = inn.readByte(this.detectType);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (!this.entityId.fetchActualValue(program, page)) {
         this.returnValue = Double.NaN;
      } else {
         Entity target = ej.level().getEntity(this.entityId.actualValue);
         if (target == null) {
            this.returnValue = Double.NaN;
         } else {
            switch (this.detectType) {
               case 10:
                  this.returnValue = (double)booleanValue(target.isAlive());
                  return;
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 16:
               case 17:
               case 18:
               case 19:
               case 21:
               case 23:
               case 25:
               case 27:
               case 28:
               case 29:
               case 31:
               case 33:
               case 34:
               case 35:
               case 37:
               case 39:
               case 41:
               case 43:
               case 44:
               case 45:
               case 46:
               case 47:
               case 48:
               case 49:
               case 51:
               case 52:
               case 53:
               case 55:
               case 57:
               case 58:
               case 59:
               case 61:
               case 63:
               case 65:
               case 67:
               case 68:
               case 69:
               case 70:
               case 71:
               case 72:
               case 73:
               case 74:
               case 75:
               case 76:
               case 77:
               case 78:
               case 79:
               case 81:
               default:
                  return;
               case 20:
                  this.returnValue = target.getX();
                  return;
               case 22:
                  this.returnValue = target.getY();
                  return;
               case 24:
                  this.returnValue = target.getZ();
                  return;
               case 26:
                  this.arrayValues = ensureArrayValues(this.arrayValues);
                  this.arrayValues[0] = target.getX();
                  this.arrayValues[1] = target.getY();
                  this.arrayValues[2] = target.getZ();
                  this.arrayValues[3] = 0.0;
                  this.returnValue = this.arrayValues[0];
                  return;
               case 30:
                  this.returnValue = (double)(target.getXRot() % 180.0F);
                  return;
               case 32:
                  this.returnValue = (double)RingUtils.range180YawValue(target.getYRot());
                  return;
               case 36:
                  this.arrayValues = ensureArrayValues(this.arrayValues);
                  this.arrayValues[0] = target.getX();
                  this.arrayValues[1] = target.getY() + (double)target.getEyeHeight();
                  this.arrayValues[2] = target.getZ();
                  this.arrayValues[3] = 0.0;
                  this.returnValue = this.arrayValues[0];
                  return;
               case 38:
                  this.arrayValues = ensureArrayValues(this.arrayValues);
                  Vec3 look = target.getLookAngle();
                  if (look != null) {
                     this.arrayValues[0] = look.x;
                     this.arrayValues[1] = look.y;
                     this.arrayValues[2] = look.z;
                     this.arrayValues[3] = 0.0;
                  } else {
                     this.arrayValues[0] = Double.NaN;
                     this.arrayValues[1] = Double.NaN;
                     this.arrayValues[2] = Double.NaN;
                     this.arrayValues[3] = Double.NaN;
                  }

                  this.returnValue = this.arrayValues[0];
                  return;
               case 40:
                  this.returnValue = (double)booleanValue(target.getVehicle() != null && target.getVehicle().hasPassenger(target));
                  return;
               case 42:
                  Entity riddenBy = CompatHelper.getRiddenByEntity(target);
                  this.returnValue = (double)booleanValue(riddenBy != null && riddenBy.getVehicle() == target);
                  return;
               case 50:
                  double d0 = target.getX() - target.xo;
                  double d1 = target.getZ() - target.zo;
                  double d2 = target.getY() - target.yo;
                  this.returnValue = (double)booleanValue((double)Mth.sqrt((float)(d0 * d0 + d1 * d1 + d2 * d2)) > 0.1);
                  return;
               case 54:
                  this.returnValue = (double)booleanValue(target.getY() - target.yo > 0.0);
                  return;
               case 56:
                  this.returnValue = (double)booleanValue(target.getY() - target.yo < 0.0);
                  return;
               case 60:
                  Entity riding = target.getVehicle();
                  if (riding != null && riding instanceof EntitySeatPart) {
                     riding = ((EntitySeatPart)riding).entityJointed;
                  }

                  if (riding != null) {
                     this.returnValue = (double)riding.getId();
                     return;
                  }

                  this.returnValue = Double.NaN;
                  return;
               case 62:
                  Entity riddenByEntity = CompatHelper.getRiddenByEntity(target);
                  if (riddenByEntity != null) {
                     this.returnValue = (double)riddenByEntity.getId();
                     return;
                  }

                  this.returnValue = Double.NaN;
                  return;
               case 64:
                  if (target instanceof LivingEntity) {
                     LivingEntity revenge = ((LivingEntity)target).getLastHurtMob();
                     if (revenge != null) {
                        this.returnValue = (double)revenge.getId();
                        return;
                     }
                  }

                  this.returnValue = Double.NaN;
                  return;
               case 66:
                  if (target instanceof LivingEntity) {
                     LivingEntity revengeTarget = ((LivingEntity)target).getLastHurtByMob();
                     if (revengeTarget != null) {
                        this.returnValue = (double)revengeTarget.getId();
                        return;
                     }
                  }

                  this.returnValue = Double.NaN;
                  return;
               case 80:
                  this.returnValue = (double)EntityAbstractType.getAbstractType(target);
                  return;
               case 82:
                  String eStr = EntityType.getKey(target.getType()).toString();
                  if (eStr == null) {
                     this.returnValue = Double.NaN;
                  } else {
                     int hashInt = eStr.hashCode();
                     this.returnValue = (double)((short)(hashInt ^ hashInt >> 16));
                  }
            }
         }
      }
   }

   public boolean hasVectorOutput(RingProgram program, RingProgramPage page) {
      return this.detectType == 26 || this.detectType == 36 || this.detectType == 38;
   }

   public int getRefCount() {
      return 1;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.entityId;
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
      return "jbring.cmn.innode.entityid.title";
   }

   public int getRefCharIndex(int refNum) {
      return 15;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.entityId.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.entityid.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.entityid.title", (Format)EntryFormats.simpleDecimalFormat, Integer.MIN_VALUE, Integer.MAX_VALUE);
         gs.setParamButtonToggleAry(0, EntryFormats.defaultButtonStates, 0);
      }

      gs.setRefToggleable(0);
      gs.setParamEntryState(1, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.entityDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 128.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgentity.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgentity.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
