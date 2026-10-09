package jp.mc.ancientred.jointblock.assemble.ring;

import com.google.common.base.Predicate;
import java.io.IOException;
import java.text.Format;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.EjSensor;
import jp.mc.ancientred.jointblock.assemble.EntryFormats;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RingNodeDRSearch extends AbstractActivatableRingNode {
   private static final int SEARCH_RANGE_MIN = 1;
   private static final int SEARCH_RANGE_MAX = 64;
   protected static final int ENTRY_SENSORNUM = 0;
   protected static final int ENTRY_SEARCHRANGE = 1;
   protected static final int ENTRY_SORT_ORDER = 2;
   protected static final int ENTRY_IFF_TYPE = 3;
   protected static final int ENTRY_ENTITY_TYPE = 4;
   protected static final int ENTRY_OPTIONAL = 5;
   private static final int SORT_ORDER_DISTANCE = 10;
   private static final int SORT_ORDER_WAYVECT = 20;
   private static final int[] SORT_ORDER_CONV = new int[]{10, 20};
   private static final int IFF_TYPE_ANY = 10;
   private static final int IFF_TYPE_ALLY = 20;
   private static final int IFF_TYPE_ENEMY = 30;
   private static final int[] IFF_TYPE_CONV = new int[]{10, 20, 30};
   private static final int ENTITY_TYPE_LIVING = 10;
   private static final int ENTITY_TYPE_PLAYER = 20;
   private static final int ENTITY_TYPE_JOINTED = 30;
   private static final int ENTITY_TYPE_ITEM = 40;
   private static final int ENTITY_TYPE_CREACHURE = 50;
   private static final int ENTITY_TYPE_ANIMAL = 60;
   private static final int ENTITY_TYPE_VILLAGER = 70;
   private static final int ENTITY_TYPE_BOSS = 80;
   private static final int ENTITY_TYPE_ANY = 90;
   private static final int ENTITY_TYPE_BYHASH = 95;
   private static final int ENTITY_TYPE_CLEAR = 99;
   private static final int[] ENTITY_TYPE_CONV = new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 99};
   public ReffableByteValueEntry sensorNum = new ReffableByteValueEntry(0, false, 1);
   public ReffableByteValueEntry searchRange = new ReffableByteValueEntry(32, false, 32);
   public byte sortOrder = 10;
   public byte iffType = 10;
   public byte entityType = 10;
   public ReffableShortValueEntry optionalVal = new ReffableShortValueEntry(0, false, 0);
   private AABB aabbForSearch;
   private Vec3 vecWorkA;
   private Vec3 vecWorkB;
   private static final Predicate<Entity> selectorLiving = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         return entity instanceof LivingEntity;
      }
   };
   private static final Predicate<Entity> selectorPlayer = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         return entity instanceof Player;
      }
   };
   private static final Predicate<Entity> selectorJointed = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         return entity instanceof EntityJointed;
      }
   };
   private static final Predicate<Entity> selectorItem = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         return entity instanceof ItemEntity;
      }
   };
   private static final Predicate<Entity> selectorCreature = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         return entity instanceof Monster || entity instanceof Slime;
      }
   };
   private static final Predicate<Entity> selectorAnimal = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         return entity instanceof Animal;
      }
   };
   private static final Predicate<Entity> selectorVillager = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         return entity instanceof Villager;
      }
   };
   private static final Predicate<Entity> selectorBoss = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         // 1.12.2 used !entity.isNonBoss(); that method no longer exists in 1.20.1, the vanilla bosses are:
         return entity instanceof EnderDragon || entity instanceof WitherBoss;
      }
   };
   private static final Predicate<Entity> selectorAny = new Predicate<Entity>() {
      public boolean apply(Entity entity) {
         return true;
      }
   };
   private static final EntitySelectorHash selectorHash = new EntitySelectorHash() {
      public boolean apply(Entity entity) {
         String eStr = EntityType.getKey(entity.getType()).toString();
         if (eStr == null) {
            return false;
         } else {
            int hashInt = eStr.hashCode();
            short targetHash = (short)(hashInt ^ hashInt >> 16);
            return targetHash == this.hash;
         }
      }
   };

   public AbstractRingNode newInstance() {
      return new RingNodeDRSearch();
   }

   public byte getNodeTypeID() {
      return 58;
   }

   public double getValue(int entryNum) {
      switch (entryNum) {
         case 0:
            return (double)this.sensorNum.value;
         case 1:
            return (double)this.searchRange.value;
         case 2:
            return (double)convertToTypeIndex(SORT_ORDER_CONV, (short)this.sortOrder);
         case 3:
            return (double)convertToTypeIndex(IFF_TYPE_CONV, (short)this.iffType);
         case 4:
            return (double)convertToTypeIndex(ENTITY_TYPE_CONV, (short)this.entityType);
         case 5:
            return (double)this.optionalVal.value;
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
            this.searchRange.value = (byte)((int)value);
            break;
         case 2:
            this.sortOrder = (byte)convertToTypeValue(SORT_ORDER_CONV, value);
            break;
         case 3:
            this.iffType = (byte)convertToTypeValue(IFF_TYPE_CONV, value);
            break;
         case 4:
            byte convValue = (byte)convertToTypeValue(ENTITY_TYPE_CONV, value);
            if (convValue != this.entityType) {
               this.optionalVal.reset();
               this.clearRefSetCnt();
            }

            this.entityType = convValue;
            break;
         case 5:
            this.optionalVal.value = (short)((int)value);
      }

   }

   public void writeNodeData(DataBufferableOutput out) throws IOException {
      this.sensorNum.write(out);
      this.searchRange.write(out);
      this.optionalVal.write(out);
      out.writeByte(this.sortOrder);
      out.writeByte(this.iffType);
      out.writeByte(this.entityType);
   }

   public void readNodeData(DataBufferableInput inn) throws IOException {
      this.sensorNum.read(inn);
      this.searchRange.read(inn);
      this.optionalVal.read(inn);
      this.sortOrder = inn.readByte(this.sortOrder);
      this.iffType = inn.readByte(this.iffType);
      this.entityType = inn.readByte(this.entityType);
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      this.trySetRingNodeToParts(ej, program, page, ActiveRingSlots.SlotType.search, (short)0);
      return program.nextNodeIndex(page);
   }

   public short onBeforeAddedToSlot(EntityJointed ej, RingProgram program, RingProgramPage page) {
      if (this.sensorNum.fetchActualValue(program, page) && this.searchRange.fetchActualValue(program, page)) {
         return (short)(this.entityType == 95 && !this.optionalVal.fetchActualValue(program, page) ? 0 : 1);
      } else {
         return 0;
      }
   }

   public void doActiveEntityWork(EntityJointed ej) {
      int sensorNum = this.sensorNum.getActualValueShort();
      if (ej.sensorList != null && sensorNum >= 0 && sensorNum <= ej.sensorList.length) {
         EjSensor sensor = ej.sensorList[sensorNum];
         if (sensor != null && sensor.joint != null) {
            if (this.entityType == 99) {
               sensor.ensureAndCleanSearchResult();
            } else {
               double sRange = (double)Math.max(Math.min(this.searchRange.getActualValueInt(), 64), 1);
               boolean sortByWayvec = this.sortOrder == 20;
               boolean useIff = this.iffType != 10;
               boolean onlyFriend = this.iffType == 20;
               if (this.aabbForSearch == null) {
                  this.aabbForSearch = JBCommonUtil.getBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                  this.vecWorkA = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
                  this.vecWorkB = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
               }

               AABB aabb = this.aabbForSearch;
               sensor.cmnVec.x = 0.0;
               sensor.cmnVec.y = 0.0;
               sensor.cmnVec.z = 0.0;
               sensor.transform(sensor.cmnVec);
               aabb.minX = sensor.cmnVec.x - sRange;
               aabb.maxX = sensor.cmnVec.x + sRange;
               aabb.minY = sensor.cmnVec.y - sRange;
               aabb.maxY = sensor.cmnVec.y + sRange;
               aabb.minZ = sensor.cmnVec.z - sRange;
               aabb.maxZ = sensor.cmnVec.z + sRange;
               sensor.ensureAndCleanSearchResult();
               Predicate<Entity> selector = null;
               switch (this.entityType) {
                  case 10:
                     selector = selectorLiving;
                     break;
                  case 20:
                     selector = selectorPlayer;
                     break;
                  case 30:
                     selector = selectorJointed;
                     break;
                  case 40:
                     selector = selectorItem;
                     break;
                  case 50:
                     selector = selectorCreature;
                     break;
                  case 60:
                     selector = selectorAnimal;
                     break;
                  case 70:
                     selector = selectorVillager;
                     break;
                  case 80:
                     selector = selectorBoss;
                     break;
                  case 90:
                     selector = selectorAny;
                     break;
                  case 95:
                     selectorHash.hash = this.optionalVal.getActualValueShort();
                     selector = selectorHash;
               }

               if (selector != null) {
                  searchLiving(ej, aabb, sensor, selector, this.vecWorkA, this.vecWorkB, sortByWayvec, useIff, onlyFriend);
               }

            }
         }
      }
   }

   private static void searchLiving(EntityJointed ej, AABB aabb, EjSensor sensor, Predicate<Entity> selector, Vec3 vecWorkA, Vec3 vecWorkB, boolean sortByWayvec, boolean useIff, boolean onlyFriend) {
      Level world = ej.level();
      List<Entity> list = world.getEntities(ej, aabb);
      double sX = sensor.cmnVec.x;
      double sY = sensor.cmnVec.y;
      double sZ = sensor.cmnVec.z;
      sensor.cmnVec.x = 0.0;
      sensor.cmnVec.y = 0.0;
      sensor.cmnVec.z = -1.0;
      sensor.transformOnlyRotation(sensor.cmnVec);
      Vec3 vecWayN = JBCommonUtil.normalizeVec3(sensor.cmnVec);
      int i = 0;

      for(int len = list.size(); i < len; ++i) {
         Entity entity = list.get(i);
         if (entity != null && !entity.isRemoved() && !ej.isRelatedToThisEntity(entity) && !(entity instanceof EntitySeatPart) && (!(entity instanceof EntityJointed) || !((EntityJointed)entity).isMasterEntityConnected()) && selector.apply(entity) && (!useIff || onlyFriend == ej.isFriendlyEntity(entity))) {
            vecWorkA.x = entity.getX();
            vecWorkA.y = entity.getY() + (double)entity.getBbHeight() / 2.0;
            vecWorkA.z = entity.getZ();
            vecWorkB.x = sX;
            vecWorkB.y = sY;
            vecWorkB.z = sZ;
            if (!JBCommonUtil.rayTraceWorldBlockHitSimple(world, vecWorkB, vecWorkA)) {
               double dX = vecWorkA.x - sX;
               double dY = vecWorkA.y - sY;
               double dZ = vecWorkA.z - sZ;
               double dist;
               if (sortByWayvec) {
                  vecWorkA.x = dX;
                  vecWorkA.y = dY;
                  vecWorkA.z = dZ;
                  vecWorkA = JBCommonUtil.crossProductToVec(vecWayN, vecWorkA, vecWorkA);
                  dist = vecWorkA.length();
               } else {
                  dist = dX * dX + dY * dY + dZ * dZ;
               }

               for(int s = 0; s < 4; ++s) {
                  if (sensor.searchResultValue[s] > dist) {
                     for(int fw = 2; fw >= s; --fw) {
                        sensor.searchResult[fw + 1] = sensor.searchResult[fw];
                        sensor.searchResultValue[fw + 1] = sensor.searchResultValue[fw];
                     }

                     sensor.searchResult[s] = entity.getId();
                     sensor.searchResultValue[s] = dist;
                     break;
                  }
               }
            }
         }
      }

   }

   public int getRefCount() {
      return 3;
   }

   public int getCurrentLayingRefNum() {
      return this.entityType != 95 ? this.gui.refSetCnt % 2 : super.getCurrentLayingRefNum();
   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      switch (entryNum) {
         case 0:
            return this.sensorNum;
         case 1:
            return this.searchRange;
         case 5:
            return this.optionalVal;
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
         case 2:
            return 5;
         default:
            return -1;
      }
   }

   public String getRefEntryName(int refNum) {
      switch (refNum) {
         case 1:
            return "jbring.cmn.innode.range.title";
         case 2:
            return "jbring.cmn.innode.hash.title";
         default:
            return "jbring.cmn.innode.sensornum.title";
      }
   }

   public int getRefCharIndex(int refNum) {
      switch (refNum) {
         case 1:
            return 19;
         case 2:
            return 20;
         default:
            return 16;
      }
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
      if (this.sensorNum.isNodeRefMode) {
         gs.setParamEntryState(0, (String)"jbring.cmn.innode.sensornum.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(0, (String)"jbring.cmn.sensornum.title", (Format)EntryFormats.sensorNumValueFormat, 0, 10);
         gs.setParamButtonToggleAry(0, EntryFormats.selectLRButtonStates, 0);
      }

      gs.setRefToggleable(0);
      if (this.searchRange.isNodeRefMode) {
         gs.setParamEntryState(1, (String)"jbring.cmn.innode.range.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
      } else {
         gs.setParamEntryState(1, (String)"jbring.search.range.title", (Format)EntryFormats.simpleDecimalFormat, 1, 64);
         gs.setParamButtonToggleAry(1, EntryFormats.defaultButtonStates, 0);
      }

      gs.setRefToggleable(1);
      gs.setParamEntryState(2, (String)"jbring.search.orderby.title", (Format)EntryFormats.searchOrderValueFormat, 0, lastIdxOf(SORT_ORDER_CONV));
      gs.setParamButtonToggleAry(2, EntryFormats.selectLRButtonStates, 0);
      gs.setParamEntryState(3, (String)"jbring.search.iff.title", (Format)EntryFormats.iffTypeValueFormat, 0, lastIdxOf(IFF_TYPE_CONV));
      gs.setParamButtonToggleAry(3, EntryFormats.selectLRButtonStates, 0);
      gs.setParamEntryState(4, (String)"jbring.cmn.detecttype.title", (Format)EntryFormats.targetEntityTypeValueFormat, 0, lastIdxOf(ENTITY_TYPE_CONV));
      gs.setParamButtonToggleAry(4, EntryFormats.selectLRButtonStates, 0);
      if (this.entityType == 95) {
         if (this.searchRange.isNodeRefMode) {
            gs.setParamEntryState(5, (String)"jbring.cmn.innode.hash.title", (Format)EntryFormats.simpleDecimalFormat, -1, 23);
         } else {
            gs.setParamEntryState(5, (String)"jbring.search.hash.title", (Format)EntryFormats.simpleDecimalFormat, 0, lastIdxOf(ENTITY_TYPE_CONV));
            gs.setParamButtonToggleAry(5, EntryFormats.defaultButtonStates, 0);
         }

         gs.setRefToggleable(5);
      }

   }

   public float getIconU() {
      return 320.0F;
   }

   public float getIconV() {
      return 96.0F;
   }

   public String getNodeName() {
      return CompatHelper.formatLang("jbring.search.nodename");
   }

   public String getNodeDesc() {
      return CompatHelper.formatLang("jbring.search.nodedesc");
   }

   private abstract static class EntitySelectorHash implements Predicate<Entity> {
      public short hash;
   }
}
