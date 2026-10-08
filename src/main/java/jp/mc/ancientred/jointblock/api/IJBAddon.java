package jp.mc.ancientred.jointblock.api;

public interface IJBAddon {

    float getBonusValue(BonusType type);

    int getCost();

    IJBAddonLimitKey getLimitKey();

    interface IJBAddonLimitKey {
        String getApplyKeyName();

        String getApplyKey();

        int getVersion();

        int getApplyCostLimit();

        boolean ignoreModelOnExceed();
    }

    interface IJBAddonHolder {
        IJBAddon getIJBAddon(int index);
    }

    enum BonusType {
        maxHealth("jbcommon.addonbonus.maxhealth", "jbcommon.addonbonus.short.maxhealth"),
        maxEnergy("jbcommon.addonbonus.maxenergy", "jbcommon.addonbonus.short.maxenergy"),
        forwardSpeed("jbcommon.addonbonus.forwardspeed", "jbcommon.addonbonus.short.forwardspeed"),
        strafingSpeed("jbcommon.addonbonus.strafingspeed", "jbcommon.addonbonus.short.strafingspeed"),
        energyUseRate("jbcommon.addonbonus.energyuserate", "jbcommon.addonbonus.short.energyuserate"),
        energyHealInterval("jbcommon.addonbonus.ehealinterval", "jbcommon.addonbonus.short.ehealinterval"),
        stepHeight("jbcommon.addonbonus.stepheight", "jbcommon.addonbonus.short.stepheight"),
        jumpPower("jbcommon.addonbonus.jumppower", "jbcommon.addonbonus.short.jumppower"),
        thrusterPower("jbcommon.addonbonus.thrusterpower", "jbcommon.addonbonus.short.thrusterpower"),
        subTypeUnitId("jbcommon.addonbonus.subtype", "jbcommon.addonbonus.short.subtype");

        public final String nameKey;
        public final String nameKeyShort;

        BonusType(String argNameKey, String argNameKeyShort) {
            this.nameKey = argNameKey;
            this.nameKeyShort = argNameKeyShort;
        }
    }
}
