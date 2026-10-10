package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EjSensor;
import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.MatrixBase;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.world.entity.Entity;

public class RingNodeTGTarget extends AbstractRealTimeStateNode {
   protected static final int ENTRY_SENSORNUM = 0;
   protected static final int ENTRY_TARGETNUM = 1;
   protected static final int ENTRY_DETECT_TYPE = 2;
   private static final int DETECT_TYPE_DIST = 10;
   private static final int DETECT_TYPE_YAWDIFF = 20;
   private static final int DETECT_TYPE_PITCHDIFF = 22;
   private static final int DETECT_TYPE_READY = 30;
   private static final int DETECT_TYPE_XDIFF = 40;
   private static final int DETECT_TYPE_YDIFF = 42;
   private static final int DETECT_TYPE_ZDIFF = 44;
   private static final int DETECT_TYPE_DIFFVEC = 46;
   private static final int DETECT_TYPE_TARGETX = 50;
   private static final int DETECT_TYPE_TARGETY = 52;
   private static final int DETECT_TYPE_TARGETZ = 54;
   private static final int DETECT_TYPE_TARGETVEC = 56;
   private static final int DETECT_TYPE_ISTARGETE = 60;
   private static final int DETECT_TYPE_TARGETEID = 62;
   private static final int[] DETECT_TYPE_CONV = new int[]{10, 20, 22, 30, 40, 42, 44, 46, 50, 52, 54, 56, 60, 62};
   public ReffableByteValueEntry sensorNum = new ReffableByteValueEntry(0, false, 1);
   public ReffableByteValueEntry targetNum = new ReffableByteValueEntry(1, false, 1);
   public byte detectType = 10;

   public AbstractRingNode newInstance() {
      return new RingNodeTGTarget();
   }

   public byte getNodeTypeID() {
      return 88;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.sensorNum.value;
         case 1:
            return (double)this.targetNum.value;
         case 2:
            return (double)convertToTypeIndex(DETECT_TYPE_CONV, (short)this.detectType);
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.sensorNum.value = (byte)((int)value);
            break;
         case 1:
            this.targetNum.value = (byte)((int)value);
            break;
         case 2:
            this.detectType = (byte)convertToTypeValue(DETECT_TYPE_CONV, value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.sensorNum.write(out);
      this.targetNum.write(out);
      out.writeByte(this.detectType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.sensorNum.read(inn);
      this.targetNum.read(inn);
      this.detectType = inn.readByte(this.detectType);
   }

   public void updateReturnValue(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (this.sensorNum.fetchActualValue(program, page) && this.targetNum.fetchActualValue(program, page)) {
         int targetNum = this.targetNum.getActualValueShort();
         if (ej.targetList != null && targetNum >= 0 && targetNum <= ej.targetList.length) {
            EjTarget target = ej.targetList[targetNum];
            if (target == null || target.isTargetEntity && (target.targetEntity == null || target.targetEntity.isRemoved())) {
               if (this.detectType == 30) {
                  this.returnValue = 0.0;
               } else {
                  this.returnValue = Double.NaN;
               }
            } else {
               int sensorNum = this.sensorNum.getActualValueShort();
               if (ej.sensorList != null && sensorNum >= 0 && sensorNum <= ej.sensorList.length) {
                  EjSensor sensor = ej.sensorList[sensorNum];
                  if (sensor != null && sensor.joint != null) {
                     Entity entity = null;
                     double wpX;
                     double wpY;
                     double wpZ;
                     if (target.isTargetEntity) {
                        entity = target.targetEntity;
                        wpX = entity.getX();
                        wpY = entity.getY() + (double)(entity.getBbHeight() / 2.0F);
                        wpZ = entity.getZ();
                     } else {
                        wpX = (double)target.worldPosX + 0.5;
                        wpY = (double)target.worldPosY + 0.5;
                        wpZ = (double)target.worldPosZ + 0.5;
                     }

                     MatrixBase invMatrix;
                     switch (this.detectType) {
                        case 10:
                           sensor.setPosToCmnVec();
                           wpX -= sensor.cmnVec.x;
                           wpY -= sensor.cmnVec.y;
                           wpZ -= sensor.cmnVec.z;
                           this.returnValue = Math.sqrt(wpX * wpX + wpY * wpY + wpZ * wpZ);
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
                        case 24:
                        case 25:
                        case 26:
                        case 27:
                        case 28:
                        case 29:
                        case 31:
                        case 32:
                        case 33:
                        case 34:
                        case 35:
                        case 36:
                        case 37:
                        case 38:
                        case 39:
                        case 41:
                        case 43:
                        case 45:
                        case 47:
                        case 48:
                        case 49:
                        case 51:
                        case 53:
                        case 55:
                        case 57:
                        case 58:
                        case 59:
                        case 61:
                        default:
                           return;
                        case 20:
                           if ((invMatrix = sensor.createInvert(sensor.inv)) == null) {
                              this.returnValue = Double.NaN;
                              return;
                           }

                           sensor.cmnVec.x = wpX;
                           sensor.cmnVec.y = wpY;
                           sensor.cmnVec.z = wpZ;
                           invMatrix.transform(sensor.cmnVec);
                           double rotY = -90.0 - Math.atan2(sensor.cmnVec.z, sensor.cmnVec.x) * 180.0 / Math.PI;
                           if (rotY > 180.0) {
                              rotY -= 360.0;
                           }

                           if (rotY < -180.0) {
                              rotY += 360.0;
                           }

                           this.returnValue = rotY;
                           return;
                        case 22:
                           if ((invMatrix = sensor.createInvert(sensor.inv)) == null) {
                              this.returnValue = Double.NaN;
                              return;
                           }

                           sensor.cmnVec.x = wpX;
                           sensor.cmnVec.y = wpY;
                           sensor.cmnVec.z = wpZ;
                           invMatrix.transform(sensor.cmnVec);
                           double rotX = Math.atan2(sensor.cmnVec.y, Math.sqrt(sensor.cmnVec.x * sensor.cmnVec.x + sensor.cmnVec.z * sensor.cmnVec.z)) * 180.0 / Math.PI;
                           if (rotX > 180.0) {
                              rotX -= 360.0;
                           }

                           if (rotX < -180.0) {
                              rotX += 360.0;
                           }

                           this.returnValue = rotX;
                           return;
                        case 30:
                           this.returnValue = 1.0;
                           return;
                        case 40:
                           sensor.setPosToCmnVec();
                           this.returnValue = wpX - sensor.cmnVec.x;
                           return;
                        case 42:
                           sensor.setPosToCmnVec();
                           this.returnValue = wpY - sensor.cmnVec.y;
                           return;
                        case 44:
                           sensor.setPosToCmnVec();
                           this.returnValue = wpZ - sensor.cmnVec.z;
                           return;
                        case 46:
                           sensor.setPosToCmnVec();
                           this.arrayValues = ensureArrayValues(this.arrayValues);
                           this.arrayValues[0] = wpX - sensor.cmnVec.x;
                           this.arrayValues[1] = wpY - sensor.cmnVec.y;
                           this.arrayValues[2] = wpZ - sensor.cmnVec.z;
                           this.arrayValues[3] = 0.0;
                           this.returnValue = this.arrayValues[0];
                           return;
                        case 50:
                           this.returnValue = wpX;
                           return;
                        case 52:
                           this.returnValue = wpY;
                           return;
                        case 54:
                           this.returnValue = wpZ;
                           return;
                        case 56:
                           this.arrayValues = ensureArrayValues(this.arrayValues);
                           this.arrayValues[0] = wpX;
                           this.arrayValues[1] = wpY;
                           this.arrayValues[2] = wpZ;
                           this.arrayValues[3] = 0.0;
                           this.returnValue = this.arrayValues[0];
                           return;
                        case 60:
                           this.returnValue = target.isTargetEntity ? 1.0 : 0.0;
                           return;
                        case 62:
                           if (entity == null) {
                              this.returnValue = Double.NaN;
                           } else {
                              this.returnValue = (double)entity.getId();
                           }

                     }
                  } else if (this.detectType == 30) {
                     this.returnValue = 0.0;
                  } else {
                     this.returnValue = Double.NaN;
                  }
               } else if (this.detectType == 30) {
                  this.returnValue = 0.0;
               } else {
                  this.returnValue = Double.NaN;
               }
            }
         } else if (this.detectType == 30) {
            this.returnValue = 0.0;
         } else {
            this.returnValue = Double.NaN;
         }
      } else {
         this.returnValue = Double.NaN;
      }
   }

   public boolean hasVectorOutput(RingProgram program, RingProgramPage page) {
      return this.detectType == 46 || this.detectType == 56;
   }

   public int getRefCount() {
      return 2;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.sensorNum;
         case 1:
            return this.targetNum;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 0;
         case 1:
            return 1;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      return refNum == 0 ? "jbring.cmn.innode.sensornum.title" : "jbring.cmn.innode.targetnum.title";
   }

   public int getRefCharIndex(int refNum) {
      return refNum == 0 ? 16 : 18;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.sensorNum.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.sensornum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.sensornum.title", (Format)EntryFormats.sensorNumValueFormat, 0, 10);
         gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      }

      gs.setRefToggleable(0);
      if (this.targetNum.isNodeRefMode) {
         gs.setParamEntryState(1, (String)"jbring.cmn.innode.targetnum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(1, (String)"jbring.cmn.targetnum.title", (Format)EntryFormats.targetNumValueFormat, 0, 5);
         gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      }

      gs.setRefToggleable(1);
      gs.setParamEntryState(2, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.targetDetectTypeValueFormat, 0, lastIdxOf(DETECT_TYPE_CONV));
      gs.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 32.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.tgtarget.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.tgtarget.nodedesc");
   }

   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.bydetecttype");
   }
}
