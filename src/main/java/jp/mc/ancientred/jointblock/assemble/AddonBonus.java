package jp.mc.ancientred.jointblock.assemble;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import jp.mc.ancientred.jointblock.api.IJBAddon;
import jp.mc.ancientred.jointblock.common.IntArray;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.unit.UnitAbility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public class AddonBonus {
   public static final int SEARCH_RESULT_ARY_SPAN = 2;
   public static final int SEARCH_RESULT_SEND_LIMIT = 20;
   public static final String NBT_SUB_TAG = "jbaddonbonus";
   public float maxHealthMult;
   public float maxEnergyMult;
   public float forwardSpeedMult;
   public float strafingSpeedMult;
   public float energyUseRateMult;
   public float jumpPowerMult;
   public float thrusterPowerMult;
   public int stepHeightModify;
   public int energyHealIntervalModify;
   public boolean hasSubTypeSet;
   public int subTypeUnitId;
   public short foundCount;
   public short arrayLimit;
   public short ignoredCount;
   public IntArray addonSearchResult = new IntArray();
   public int[] addonSearchResultAry;
   public ArrayList<Object> sResultStrList;
   public ArrayList<Object> sResultStrSummary;
   public String strResultHeader;
   public String strMaxListCount;
   public boolean checkLimitResultExceedLimit = false;
   public HashMap<String, AddonKeyState> addonKeyStateMap = new HashMap();

   public void applyAddonBonusHealthAndEnergy(EntityJointed ej) {
      ej.maxJBHealth += (short)((int)((float)ej.maxJBHealth * this.maxHealthMult));
      ej.maxJBEnergy += (short)((int)((float)ej.maxJBEnergy * this.maxEnergyMult));
   }

   public void applyAddonBonusMovement(UnitAbility unitAbility) {
      unitAbility.energyUseRate += this.energyUseRateMult;
      unitAbility.energyHealSpeed = (byte)(unitAbility.energyHealSpeed + this.energyHealIntervalModify);
      unitAbility.moveForwardSpeedRate += (short)((int)((float)unitAbility.moveForwardSpeedRate * this.forwardSpeedMult));
      unitAbility.moveStrafingSpeedRate += (short)((int)((float)unitAbility.moveStrafingSpeedRate * this.strafingSpeedMult));
      unitAbility.stepHight = (byte)(unitAbility.stepHight + this.stepHeightModify);
      unitAbility.jumpPower += this.jumpPowerMult;
      unitAbility.thrusterPower += this.thrusterPowerMult;
      if (unitAbility.moveForwardSpeedRate <= 0) {
         unitAbility.moveForwardSpeedRate = 0;
      }

      if (unitAbility.moveStrafingSpeedRate <= 0) {
         unitAbility.moveStrafingSpeedRate = 0;
      }

      if (unitAbility.energyHealSpeed <= 0) {
         unitAbility.energyHealSpeed = 1;
      }

      if (unitAbility.energyUseRate <= 0.0F) {
         unitAbility.energyUseRate = 0.0F;
      }

      if (unitAbility.stepHight <= 0) {
         unitAbility.stepHight = 0;
      }

      if (unitAbility.stepHight > 32) {
         unitAbility.stepHight = 32;
      }

      if (unitAbility.jumpPower < 0.0F) {
         unitAbility.jumpPower = 0.0F;
      }

      if (unitAbility.thrusterPower < 0.0F) {
         unitAbility.thrusterPower = 0.0F;
      }

   }

   public void createAddonSearchResultAry() {
      if (this.addonSearchResult != null) {
         this.addonSearchResultAry = this.addonSearchResult.toIntArrayPadded(2, 40);
         this.addonSearchResultAry[0] = (this.foundCount & '\uffff') << 16 | 20;
         this.addonSearchResultAry[1] = this.ignoredCount;
      }

   }

   public void readFromNBT(CompoundTag tag) {
      if (tag.contains("jbaddonbonus", 10)) {
         CompoundTag subTag = tag.getCompound("jbaddonbonus");
         this.maxHealthMult = subTag.getFloat("maxHealthMult");
         this.maxEnergyMult = subTag.getFloat("maxEnergyMult");
         this.forwardSpeedMult = subTag.getFloat("forwardSpeedMult");
         this.strafingSpeedMult = subTag.getFloat("strafingSpeedMult");
         this.energyUseRateMult = subTag.getFloat("energyUseRateMult");
         this.jumpPowerMult = subTag.getFloat("jumpPowerMult");
         this.thrusterPowerMult = subTag.getFloat("thrusterPowerMult");
         this.stepHeightModify = subTag.getInt("stepHeightModify");
         this.energyHealIntervalModify = subTag.getInt("energyHealSpeedModify");
         this.subTypeUnitId = subTag.getInt("subTypeUnitId");
         if (subTag.contains("searchRes", 11)) {
            this.addonSearchResultAry = subTag.getIntArray("searchRes");
         } else {
            this.addonSearchResultAry = null;
         }

      }
   }

   public void writeToNBT(CompoundTag tag) {
      CompoundTag subTag;
      if (!tag.contains("jbaddonbonus", 10)) {
         tag.put("jbaddonbonus", subTag = new CompoundTag());
      } else {
         subTag = tag.getCompound("jbaddonbonus");
      }

      subTag.putFloat("maxHealthMult", this.maxHealthMult);
      subTag.putFloat("maxEnergyMult", this.maxEnergyMult);
      subTag.putFloat("forwardSpeedMult", this.forwardSpeedMult);
      subTag.putFloat("strafingSpeedMult", this.strafingSpeedMult);
      subTag.putFloat("energyUseRateMult", this.energyUseRateMult);
      subTag.putFloat("jumpPowerMult", this.jumpPowerMult);
      subTag.putFloat("thrusterPowerMult", this.thrusterPowerMult);
      subTag.putInt("stepHeightModify", this.stepHeightModify);
      subTag.putInt("energyHealSpeedModify", this.energyHealIntervalModify);
      subTag.putInt("subTypeUnitId", this.subTypeUnitId);
      if (this.addonSearchResultAry != null && this.addonSearchResultAry.length > 0) {
         subTag.putIntArray("searchRes", this.addonSearchResultAry);
      }

   }

   public void readSpawnData(ByteBuf additionalData) {
      this.maxHealthMult = additionalData.readFloat();
      this.maxEnergyMult = additionalData.readFloat();
      this.forwardSpeedMult = additionalData.readFloat();
      this.strafingSpeedMult = additionalData.readFloat();
      this.energyUseRateMult = additionalData.readFloat();
      this.jumpPowerMult = additionalData.readFloat();
      this.thrusterPowerMult = additionalData.readFloat();
      this.stepHeightModify = additionalData.readInt();
      this.energyHealIntervalModify = additionalData.readInt();
      this.subTypeUnitId = additionalData.readInt();
   }

   public void writeSpawnData(ByteBuf buffer) {
      buffer.writeFloat(this.maxHealthMult);
      buffer.writeFloat(this.maxEnergyMult);
      buffer.writeFloat(this.forwardSpeedMult);
      buffer.writeFloat(this.strafingSpeedMult);
      buffer.writeFloat(this.energyUseRateMult);
      buffer.writeFloat(this.jumpPowerMult);
      buffer.writeFloat(this.thrusterPowerMult);
      buffer.writeInt(this.stepHeightModify);
      buffer.writeInt(this.energyHealIntervalModify);
      buffer.writeInt(this.subTypeUnitId);
   }

   public IJBAddon checkAndGetAddon(short smodelItemId, byte bmodelMeta) {
      IJBAddon addon = null;
      this.checkLimitResultExceedLimit = false;
      if (smodelItemId > 0) {
         Item modelItem = Item.byId(smodelItemId);
         if (modelItem instanceof IJBAddon.IJBAddonHolder) {
            addon = ((IJBAddon.IJBAddonHolder)modelItem).getIJBAddon(bmodelMeta);
            if (addon != null) {
               ++this.foundCount;
               IJBAddon.IJBAddonLimitKey limitKey = addon.getLimitKey();
               if (limitKey != null && !this.canApplyModelAddon(addon, limitKey)) {
                  ++this.ignoredCount;
                  if (limitKey.ignoreModelOnExceed()) {
                     this.checkLimitResultExceedLimit = true;
                     return null;
                  }

                  addon = null;
               }
            }
         }
      }

      return addon;
   }

   public boolean canApplyModelAddon(IJBAddon addon, IJBAddon.IJBAddonLimitKey limitKey) {
      int allowdTotalCost = limitKey.getApplyCostLimit();
      if (allowdTotalCost <= 0) {
         return true;
      } else {
         int slingleCost = addon.getCost();
         if (allowdTotalCost < slingleCost) {
            return false;
         } else {
            String key = limitKey.getApplyKey();
            AddonKeyState keyState = (AddonKeyState)this.addonKeyStateMap.get(key);
            if (keyState == null) {
               keyState = new AddonKeyState();
               keyState.appliedNum = slingleCost;
               this.addonKeyStateMap.put(key, keyState);
               return true;
            } else if (keyState.appliedNum + slingleCost <= allowdTotalCost) {
               keyState.appliedNum += slingleCost;
               return true;
            } else {
               return false;
            }
         }
      }
   }

   public void applyAddon(IJBAddon addon, int itemId, int itemMeta) {
      float maxHealthMultBonus = addon.getBonusValue(IJBAddon.BonusType.maxHealth);
      this.maxHealthMult += maxHealthMultBonus;
      float maxEnergyMultBonus = addon.getBonusValue(IJBAddon.BonusType.maxEnergy);
      this.maxEnergyMult += maxEnergyMultBonus;
      float forwardSpeedMultBonus = addon.getBonusValue(IJBAddon.BonusType.forwardSpeed);
      this.forwardSpeedMult += forwardSpeedMultBonus;
      float strafingSpeedMultBonus = addon.getBonusValue(IJBAddon.BonusType.strafingSpeed);
      this.strafingSpeedMult += strafingSpeedMultBonus;
      float energyUseRateMultBonus = addon.getBonusValue(IJBAddon.BonusType.energyUseRate);
      this.energyUseRateMult += energyUseRateMultBonus;
      int energyHealIntervalBonus = (int)addon.getBonusValue(IJBAddon.BonusType.energyHealInterval);
      this.energyHealIntervalModify += energyHealIntervalBonus;
      int stepHeightModifyBonus = (int)addon.getBonusValue(IJBAddon.BonusType.stepHeight);
      this.stepHeightModify += stepHeightModifyBonus;
      float jumpPowerModifyBonus = addon.getBonusValue(IJBAddon.BonusType.jumpPower);
      this.jumpPowerMult += jumpPowerModifyBonus;
      float thrusterPowerModifyBonus = addon.getBonusValue(IJBAddon.BonusType.thrusterPower);
      this.thrusterPowerMult += thrusterPowerModifyBonus;
      if (!this.hasSubTypeSet) {
         int subTypeUnitId = (int)addon.getBonusValue(IJBAddon.BonusType.subTypeUnitId);
         if (subTypeUnitId != 0) {
            this.subTypeUnitId = subTypeUnitId;
            this.hasSubTypeSet = true;
         }
      }

      if (this.addonSearchResult != null) {
         this.addonSearchResult.add(itemId);
         this.addonSearchResult.add(itemMeta);
      }

   }

   private class AddonKeyState {
      public int appliedNum;

      private AddonKeyState() {
         this.appliedNum = 0;
      }
   }
}
