package jp.mc.ancientred.jointblock.assemble;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class TargetSelect {
   public static final int SELECT_MODE_ENTITY_LIVING = 0;
   public static final int SELECT_MODE_ENTITY_ANY = 1;
   public static final int SELECT_MODE_WORLD_BLOCK = 2;
   private static final Vec3 vecWorkA = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   private static final Vec3 vecWorkB = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);
   public static long selectModeLastToggled;
   public static int curerntSelectMode;
   public static boolean isSelectBtnPressed = false;
   public static EjTarget[] selectedList = new EjTarget[5];
   public static AABB aabbForSearch = JBCommonUtil.getBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   public static ArrayList<Entity> candidatesList = new ArrayList();
   public static ArrayList<Entity> selectedEntityList = new ArrayList();
   public static BlockHitResult objectMouseOver;
   private static EntitySelectorEx eSelectorAny = new EntitySelectorEx() {
      public boolean apply(Entity entity) {
         if (entity == null) {
            return false;
         } else {
            TargetSelect.vecWorkA.x = this.lookX;
            TargetSelect.vecWorkA.y = this.lookY;
            TargetSelect.vecWorkA.z = this.lookZ;
            TargetSelect.vecWorkB.x = entity.getX();
            TargetSelect.vecWorkB.y = entity.getY();
            TargetSelect.vecWorkB.z = entity.getZ();
            return !JBCommonUtil.rayTraceWorldBlockHitSimple(entity.level(), TargetSelect.vecWorkA, TargetSelect.vecWorkB);
         }
      }
   };
   private static EntitySelectorEx eSelectorLiving = new EntitySelectorEx() {
      public boolean apply(Entity entity) {
         if (entity == null) {
            return false;
         } else {
            TargetSelect.vecWorkA.x = this.lookX;
            TargetSelect.vecWorkA.y = this.lookY;
            TargetSelect.vecWorkA.z = this.lookZ;
            TargetSelect.vecWorkB.x = entity.getX();
            TargetSelect.vecWorkB.y = entity.getY() + (double)entity.getBbHeight() / 2.0;
            TargetSelect.vecWorkB.z = entity.getZ();
            return JBCommonUtil.rayTraceWorldBlockHitSimple(entity.level(), TargetSelect.vecWorkA, TargetSelect.vecWorkB) ? false : entity instanceof LivingEntity;
         }
      }
   };

   public static boolean isEntityLivingSelectMode() {
      return curerntSelectMode % 3 == 0;
   }

   public static boolean isEntityAnySelectMode() {
      return curerntSelectMode % 3 == 1;
   }

   public static boolean isPointSelectMode() {
      return curerntSelectMode % 3 == 2;
   }

   public static boolean storeSelectedFromCandidates() {
      boolean addedNewEntity = false;
      int i = 0;

      for(int len = selectedEntityList.size(); i < len; ++i) {
         Entity entity = (Entity)selectedEntityList.get(i);
         if (entity != null && !entity.isRemoved()) {
            addedNewEntity |= addEntityIFNotInSelectedList(entity);
         }
      }

      return addedNewEntity;
   }

   public static boolean addEntityIFNotInSelectedList(Entity entity) {
      int emptyIdx = -1;

      for(int i = 0; i < selectedList.length; ++i) {
         if (selectedList[i] == null) {
            if (emptyIdx == -1) {
               emptyIdx = i;
            }
         } else if (selectedList[i].isTargetEntity && selectedList[i].targetEntity == entity) {
            return false;
         }
      }

      if (emptyIdx != -1) {
         selectedList[emptyIdx] = new EjTarget(entity);
         return true;
      } else {
         return false;
      }
   }

   public static void clearTargetSelection() {
      for(int i = 0; i < selectedList.length; ++i) {
         selectedList[i] = null;
      }

   }

   public static void commitTargetSelection(boolean isPointSelect) {
      int sendCnt;
      if (isPointSelect && objectMouseOver != null && objectMouseOver.getType() == Type.BLOCK) {
         for(sendCnt = 0; sendCnt < selectedList.length; ++sendCnt) {
            selectedList[sendCnt] = null;
         }

         BlockPos blockPos = objectMouseOver.getBlockPos();
         selectedList[0] = new EjTarget(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      }

      sendCnt = 0;

      for(int i = 0; i < selectedList.length; ++i) {
         if (selectedList[i] != null) {
            if (!selectedList[i].isTargetEntity || selectedList[i].targetEntity != null && !selectedList[i].targetEntity.isRemoved()) {
               ++sendCnt;
            } else {
               selectedList[i] = null;
            }
         }
      }

      JBPacketSender.sendTargetSelectPacketToServer(sendCnt, selectedList);
      clearTargetSelection();
   }

   public static void collectCandidates(Player player, boolean isLivingOnly) {
      Level world = player.level();
      double boxRange = 32.0;
      aabbForSearch.minX = player.getX() - boxRange;
      aabbForSearch.minY = player.getY() - boxRange;
      aabbForSearch.minZ = player.getZ() - boxRange;
      aabbForSearch.maxX = player.getX() + boxRange;
      aabbForSearch.maxY = player.getY() + boxRange;
      aabbForSearch.maxZ = player.getZ() + boxRange;
      candidatesList.clear();
      EntitySelectorEx selector = isLivingOnly ? eSelectorLiving : eSelectorAny;
      selector.lookX = player.getX();
      selector.lookY = player.getY();
      selector.lookZ = player.getZ();
      getEntitiesWithinAABBExcludingEntity(world, player, aabbForSearch, selector);
   }

   public static void getEntitiesWithinAABBExcludingEntity(Level world, Entity p_94576_1_, AABB aabb, Predicate<Entity> selector) {
      candidatesList.addAll(world.getEntities(p_94576_1_, aabb, selector));
   }

   public static void clearCandidates() {
      candidatesList.clear();
   }

   public abstract static class EntitySelectorEx implements Predicate<Entity> {
      protected double lookX;
      protected double lookY;
      protected double lookZ;
   }
}
