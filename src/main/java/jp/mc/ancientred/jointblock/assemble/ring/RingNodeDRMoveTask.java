package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.text.Format;
import jp.mc.ancientred.jointblock.assemble.EjTarget;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.assemble.ai.JBPathNavigate;
import jp.mc.ancientred.jointblock.assemble.ai.JBRandomPosGen;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.util.Mth;

public class RingNodeDRMoveTask extends AbstractActivatableRingNode {
   private static final int ENTRY_MOVETYPE = 0;
   private static final int ENTRY_TARGETNUM = 1;
   private static final int ENTRY_WHILETIME = 2;
   protected static final int MOVETYPE_FOLLOW = 10;
   protected static final int MOVETYPE_AWAY = 20;
   protected static final int MOVETYPE_W_AROUND = 30;
   protected static final int MOVETYPE_WANDER = 40;
   protected static final int[] MOVETYPE_CONV = new int[]{10, 20, 30, 40};
   public byte moveType = 10;
   public ReffableByteValueEntry targetNum = new ReffableByteValueEntry(0, false, 1);
   private EjTarget randomGenPos = null;

   public AbstractRingNode newInstance() {
      return new RingNodeDRMoveTask();
   }

   public byte getNodeTypeID() {
      return 48;
   }

   public int getAuthNeeds() {
      return 4;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)convertToTypeIndex(MOVETYPE_CONV, (short)this.moveType);
         case 1:
            return (double)this.targetNum.value;
         case 2:
            return (double)this.whileTime.value;
         default:
            return 0.0;
      }
   }

   public void setValue(int entryNum, double value) {
      switch (entryNum) {
         case 0:
            this.moveType = (byte)convertToTypeValue(MOVETYPE_CONV, value);
            break;
         case 1:
            this.targetNum.value = (byte)((int)value);
            break;
         case 2:
            this.whileTime.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      out.writeByte(this.moveType);
      this.targetNum.write(out);
      this.whileTime.write(out);
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
      this.targetNum.writeRunningData(out);
      this.whileTime.writeRunningData(out);
      out.writeShort(this.remainTick);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.moveType = inn.readByte(this.moveType);
      this.targetNum.read(inn);
      this.whileTime.read(inn);
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
      this.targetNum.readRunningData(inn);
      this.whileTime.readRunningData(inn);
      this.remainTick = inn.readShort(this.remainTick);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.move, (short)4);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return this.targetNum.fetchActualValue(program, page) && this.whileTime.fetchActualValue(program, page) ? this.whileTime.getActualValueShort() : 0;
   }

   public void doActiveEntityWork(EntityJointed ej) {
      short timeTick = this.whileTime.getActualValueShort();
      if (timeTick > 0) {
         // StarMiner zero-gravity interop removed during the 1.20.1 port (see MIGRATION_PLAN.md, Phase 3).
         final boolean isInZeroGravityWorld = false;
         if (!isInZeroGravityWorld) {
            if (!ej.unitAbility.isExFlyable() && !ej.unitAbility.isFloatOnly()) {
               if (ej.entitySeatPart0 != null) {
                  ej.entitySeatPart0.resetMoveDR();
                  ej.entitySeatPart0.lockMoveDR = true;
                  JBPathNavigate navi = ej.getJBNavigator();
                  if (navi != null) {
                     double posX = 0.0;
                     double posY = 0.0;
                     double posZ = 0.0;
                     EjTarget target = null;
                     switch (this.moveType) {
                        case 10:
                        case 20:
                        case 30:
                           int targetNumInt = this.targetNum.getActualValueInt();
                           if (targetNumInt < 0 || targetNumInt >= ej.targetList.length) {
                              return;
                           }

                           target = ej.targetList[targetNumInt];
                           if (target == null) {
                              return;
                           }

                           if (target.isTargetEntity) {
                              if (target.targetEntity == null) {
                                 return;
                              }

                              posX = target.targetEntity.getX();
                              posY = target.targetEntity.getY();
                              posZ = target.targetEntity.getZ();
                           } else {
                              posX = (double)target.worldPosX + 0.5;
                              posY = (double)target.worldPosY + 0.5;
                              posZ = (double)target.worldPosZ + 0.5;
                           }
                     }

                     switch (this.moveType) {
                        case 10:
                           if (!this.inNavigatableRange(posX, posY, posZ, ej, 31.0)) {
                              if (this.randomGenPos == null) {
                                 this.randomGenPos = JBRandomPosGen.findRandomTargetBlockTowards(ej, 16, 7, posX, posY, posZ);
                              }

                              target = this.randomGenPos;
                              if (target != null) {
                                 posX = (double)target.worldPosX + 0.5;
                                 posY = (double)target.worldPosY + 0.5;
                                 posZ = (double)target.worldPosZ + 0.5;
                              }
                           }
                           break;
                        case 20:
                           if (this.randomGenPos == null) {
                              this.randomGenPos = JBRandomPosGen.findRandomTargetBlockAwayFrom(ej, 16, 7, posX, posY, posZ);
                           }

                           target = this.randomGenPos;
                           if (target != null) {
                              posX = (double)target.worldPosX + 0.5;
                              posY = (double)target.worldPosY + 0.5;
                              posZ = (double)target.worldPosZ + 0.5;
                           }
                           break;
                        case 30:
                           if (this.randomGenPos == null) {
                              this.randomGenPos = JBRandomPosGen.findRandomTarget(ej.level(), posX, posY, posZ, 10, 7);
                           }

                           target = this.randomGenPos;
                           if (target != null) {
                              posX = (double)target.worldPosX + 0.5;
                              posY = (double)target.worldPosY + 0.5;
                              posZ = (double)target.worldPosZ + 0.5;
                           }
                           break;
                        case 40:
                           if (this.randomGenPos == null) {
                              this.randomGenPos = JBRandomPosGen.findRandomTarget(ej, 10, 7);
                           }

                           target = this.randomGenPos;
                           if (target != null) {
                              posX = (double)target.worldPosX + 0.5;
                              posY = (double)target.worldPosY + 0.5;
                              posZ = (double)target.worldPosZ + 0.5;
                           }
                     }

                     if (target != null && (!target.isTargetEntity || target.targetEntity != null)) {
                        double diffX = posX - ej.getX();
                        double diffZ = posZ - ej.getZ();
                        double diffY = posY - (double)Mth.floor(ej.getBoundingBox().minY) + 0.5;
                        double xzRange = 0.16000000000000003;
                        if (target.isTargetEntity || ej.unitAbility.isHoverable()) {
                           xzRange = 16.0;
                        }

                        double yRange = 4.0;
                        if (ej.unitAbility.isFlyable() || ej.unitAbility.isHoverable()) {
                           yRange = 25.0;
                        }

                        if (diffX * diffX + diffZ * diffZ < xzRange && diffY * diffY < yRange) {
                           if (!navi.noPath()) {
                              navi.clearPathEntity();
                           }

                           this.randomGenPos = null;
                        } else {
                           this.processMoveForWalker(ej, navi, target);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   protected void processMoveForWalker(EntityJointed ej, JBPathNavigate navi, EjTarget target) {
      if (!navi.noPath() && !navi.checkDestinationExceed(target) && target == this.randomGenPos) {
         this.randomGenPos = null;
      } else {
         if (navi.noPath()) {
            navi.tryMoveToTarget(target);
         }

         if (navi.noPath()) {
            if (ej.unitAbility.isFlyable()) {
               ej.entitySeatPart0.hasRINGMoveDR = true;
               ej.entitySeatPart0.moveUpDownDR = 1;
               ej.entitySeatPart0.moveUp = false;
            }

            if (target == this.randomGenPos) {
               this.randomGenPos = null;
            }
         } else {
            navi.onUpdateNavigation();
            if (navi.destinationUpdated) {
               navi.destinationUpdated = false;
               double diffX = navi.curdestX - ej.getX();
               double diffZ = navi.curdestZ - ej.getZ();
               double diffY = navi.curdestY - (double)((int)ej.getBoundingBox().minY);
               double dist = diffX * diffX + diffY * diffY + diffZ * diffZ;
               if (dist >= 0.001) {
                  if (ej.unitAbility.isFlyable()) {
                     if (!ej.onGround() && ej.detectAirBlockBelow(2)) {
                        ej.entitySeatPart0.hasRINGMoveDR = true;
                        ej.entitySeatPart0.moveUpDownDR = 1;
                        ej.entitySeatPart0.moveUp = false;
                     }

                     ej.spinningRate = 1.0F;
                     ej.setAirBorneHovering(true);
                     if (ej.onGround() || diffY + 0.5 > 0.0) {
                        ej.entitySeatPart0.hasRINGMoveDR = true;
                        ej.entitySeatPart0.moveUpDownDR = 1;
                        ej.entitySeatPart0.moveUp = true;
                     }
                  }

                  ej.entitySeatPart0.hasRINGMoveDR = true;
                  if (ej.unitAbility.isFlyable() || ej.unitAbility.isHoverable() || ej.onGround()) {
                     ej.entitySeatPart0.moveForwardDR = 1;
                     ej.entitySeatPart0.moveForwardAmount = 1.0F;
                  }

                  float angleUpdatelimit = 120.0F;
                  float yawNewValue = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0F;
                  float diff = Mth.wrapDegrees(yawNewValue - ej.getYRot());
                  yawNewValue = ej.getYRot() + Math.max(Math.min(diff, angleUpdatelimit), -angleUpdatelimit);
                  ej.entitySeatPart0.yawChange = true;
                  ej.entitySeatPart0.yawAdd = false;
                  ej.entitySeatPart0.yawValue = yawNewValue % 360.0F;
               }
            }
         }

      }
   }

   private boolean inNavigatableRange(double posX, double posY, double posZ, EntityJointed ej, double dist) {
      posX -= ej.getX();
      posY -= ej.getY();
      posZ -= ej.getZ();
      return dist * dist > posX * posX + posY * posY + posZ * posZ;
   }

   public int getRefCount() {
      return 2;
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 1:
            return this.targetNum;
         case 2:
            return this.whileTime;
         default:
            return null;
      }
   }

   public int getRefEntryNum(int refNum) {
      switch (refNum) {
         case 0:
            return 1;
         case 1:
            return 2;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 0:
            return "jbring.cmn.innode.targetnum.title";
         case 1:
            return "jbring.cmn.innode.while.title";
         default:
            return null;
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 0:
            return 18;
         case 1:
            return 11;
         default:
            return super.getRefCharIndex(refNum);
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      gs.setParamEntryState(0, (String)"jbring.cmn.type.title", (Format)EntryFormats.moveTaskTypeValueFormat, 0, lastIdxOf(MOVETYPE_CONV));
      gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      if (this.targetNum.isNodeRefMode) {
         gs.setParamEntryState(1, (String)"jbring.cmn.innode.targetnum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(1, (String)"jbring.cmn.targetnum.title", (Format)EntryFormats.targetNumValueFormat, 0, 5);
         gs.setParamButtonToggleAry(1, EntryFormats.selectLRButtonStates, 0);
      }

      gs.setRefToggleable(1);
      super.setParamEntryWhileTime(2, gs);
   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 48.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.movetask.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.movetask.nodedesc");
   }
}
