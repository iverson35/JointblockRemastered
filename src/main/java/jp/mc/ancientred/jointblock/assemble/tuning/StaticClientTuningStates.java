package jp.mc.ancientred.jointblock.assemble.tuning;

import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.assemble.JointParts;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.item.ItemJBAssemblyTuner;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class StaticClientTuningStates {
   public static boolean isClientPlayerHoldingTuneItem;
   public static final PlayerTuningTarget targetPri = new PlayerTuningTarget();
   public static final PlayerTuningTarget target = new PlayerTuningTarget();
   public static final PlayerTuningTarget targetSelected = new PlayerTuningTarget();

   public static boolean checkClientPlayerHoldingTuneItem() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.player != null) {
            Player player = mc.player;
            ItemStack itemStack = CompatHelper.getCurrentEquippedItem(player);
            return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemJBAssemblyTuner;
         } else {
            return false;
         }
      } catch (Exception var3) {
         return false;
      }
   }

   public static void shiftSelectedToChild() {
      if (targetSelected.rayTracedHitJoint != null) {
         for(int i = 0; i < 6; ++i) {
            IJBJointParameterHolder.IJBJoint child = targetSelected.rayTracedHitJoint.getChildJoint(i);
            if (child != null && child instanceof JointParts) {
               JointParts jp = (JointParts)child;
               if (!jp.isDestructed) {
                  targetSelected.rayTracedHitJoint = jp;
                  return;
               }
            }
         }

      }
   }

   public static void shiftSelectedToParent() {
      if (targetSelected.rayTracedHitJoint != null) {
         if (targetSelected.rayTracedHitJoint.jointType != 0) {
            IJBJointParameterHolder.IJBJoint parent = targetSelected.rayTracedHitJoint.getParentJoint();
            if (parent != null && parent instanceof JointParts) {
               JointParts jp = (JointParts)parent;
               if (!jp.isDestructed) {
                  targetSelected.rayTracedHitJoint = jp;
               }
            }

         }
      }
   }

   public static void shiftSelectedWhithinBrothers(boolean way) {
      if (targetSelected.rayTracedHitJoint != null) {
         if (targetSelected.rayTracedHitJoint.jointType != 0) {
            IJBJointParameterHolder.IJBJoint parent = targetSelected.rayTracedHitJoint.getParentJoint();
            if (parent != null && parent instanceof JointParts) {
               JointParts jpParent = (JointParts)parent;
               if (!jpParent.isDestructed && jpParent.child != null) {
                  int selfIndex = -1;

                  int i;
                  for(i = 0; i < 6; ++i) {
                     IJBJointParameterHolder.IJBJoint child = jpParent.child[i];
                     if (child != null && child == targetSelected.rayTracedHitJoint) {
                        selfIndex = i;
                        break;
                     }
                  }

                  if (selfIndex != -1) {
                     for(i = 1; i < 6; ++i) {
                        int x = (selfIndex + (way ? -i : i)) % 6;
                        if (x < 0) {
                           x += 6;
                        }

                        IJBJointParameterHolder.IJBJoint child = jpParent.child[x];
                        if (child != null && child instanceof JointParts) {
                           JointParts jp = (JointParts)child;
                           if (!jp.isDestructed) {
                              targetSelected.rayTracedHitJoint = jp;
                              return;
                           }
                        }
                     }
                  }
               }
            }

         }
      }
   }

   public static class PlayerTuningTarget {
      public boolean isSetectionEnabled = false;
      public boolean isAdditionalBarHit = false;
      public int selectionEnabledTick;
      public JointParts rayTracedHitJoint;
      public EntityJointed rayTracedHitEntity;
      public double rayTracedHitDistSq;
      public int rayTracedHitSide;
      public int additionalBarNum;
      public int additionalBarOffset;
      public static Vec3 rayTracedHitVecAbs = JBCommonUtil.createVectorHelper(0.0, 0.0, 0.0);

      public void copyFrom(PlayerTuningTarget src) {
         this.rayTracedHitJoint = src.rayTracedHitJoint;
         this.rayTracedHitEntity = src.rayTracedHitEntity;
         this.rayTracedHitDistSq = src.rayTracedHitDistSq;
         this.rayTracedHitSide = src.rayTracedHitSide;
         this.isAdditionalBarHit = src.isAdditionalBarHit;
         this.additionalBarNum = src.additionalBarNum;
         this.additionalBarOffset = src.additionalBarOffset;
         rayTracedHitVecAbs.x = src.rayTracedHitVecAbs.x;
         rayTracedHitVecAbs.y = src.rayTracedHitVecAbs.y;
         rayTracedHitVecAbs.z = src.rayTracedHitVecAbs.z;
      }

      public void clearWork() {
         this.isSetectionEnabled = false;
         this.isAdditionalBarHit = false;
         this.rayTracedHitJoint = null;
         this.rayTracedHitEntity = null;
         this.rayTracedHitDistSq = Double.MAX_VALUE;
         this.rayTracedHitSide = -1;
         this.additionalBarNum = -1;
         this.additionalBarOffset = 0;
         rayTracedHitVecAbs.x = 0.0;
         rayTracedHitVecAbs.y = 0.0;
         rayTracedHitVecAbs.z = 0.0;
      }

      public void checkSelectionEnabled() {
         if (this.isSetectionEnabled) {
            try {
               if (this.rayTracedHitJoint == null || this.rayTracedHitEntity == null) {
                  this.clearWork();
                  return;
               }

               if (this.rayTracedHitJoint.isDestructed) {
                  this.clearWork();
                  return;
               }

               if (this.rayTracedHitEntity.isRemoved()) {
                  this.clearWork();
                  return;
               }
            } catch (Exception var2) {
            }
         }

      }
   }
}
