package jp.mc.ancientred.jointblock.effect;

import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IJBRelatedSound extends TickableSoundInstance {
   Sound getSoundPoolEntry();

   boolean isSoundSlotObjEquals(AbstractEffectAttributes var1);

   void dispose();
}
