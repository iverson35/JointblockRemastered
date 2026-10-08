package jp.mc.ancientred.jointblock.common;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CapabilityEntityCommonStorage {
   // 1.20.1: @CapabilityInject/CapabilityManager.register are gone; the token below is the
   // RegisterCapabilitiesEvent-compatible equivalent (register via RegisterCapabilitiesEvent#register).
   public static final Capability<IEntitCommonStorage> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
   public static final ResourceLocation EXTENDED_PROPERTY_CSTOREAGE_HOLDER = new ResourceLocation("modj_jointblock:capability_ecsc");

   public static class EntitCommonStorageImpl implements IEntitCommonStorage, ICapabilityProvider {
      public int targetSightTick = 0;
      public int targetSightTickStart = 0;
      public long targetSightRenderGen = -1L;
      private final LazyOptional<IEntitCommonStorage> holder = LazyOptional.of(() -> this);

      public int getTargetSightTick() {
         return this.targetSightTick;
      }

      public void setTargetSightTick(int targetSightTick) {
         this.targetSightTick = targetSightTick;
      }

      public int getTargetSightTickStart() {
         return this.targetSightTickStart;
      }

      public void setTargetSightTickStart(int targetSightTickStart) {
         this.targetSightTickStart = targetSightTickStart;
      }

      public long getTargetSightRenderGen() {
         return this.targetSightRenderGen;
      }

      public void setTargetSightRenderGen(long targetSightRenderGen) {
         this.targetSightRenderGen = targetSightRenderGen;
      }

      @NotNull
      public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
         return capability == CapabilityEntityCommonStorage.CAPABILITY ? this.holder.cast() : LazyOptional.empty();
      }
   }

   public interface IEntitCommonStorage {
      int getTargetSightTick();

      void setTargetSightTick(int var1);

      int getTargetSightTickStart();

      void setTargetSightTickStart(int var1);

      long getTargetSightRenderGen();

      void setTargetSightRenderGen(long var1);
   }
}
