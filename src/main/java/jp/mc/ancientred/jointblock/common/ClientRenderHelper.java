package jp.mc.ancientred.jointblock.common;

import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.entity.EntityJBClientCamera;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientRenderHelper {
   public static boolean setUpThirdPersonFarViewCamera(Minecraft mc, Player player, EntityJointed entityJointed, float partialTick) {
      Entity eCamera = entityJointed.entityCamera;
      if (!(eCamera instanceof EntityJBClientCamera)) {
         return false;
      } else {
         EntityJBClientCamera camEntity = (EntityJBClientCamera)eCamera;
         int farLv = entityJointed.getThirdPersonViewFarLevel();
         if (farLv < 0 || farLv >= JBConfig.TPV_FAR_ARY.length) {
            farLv = 0;
         }

         float far = JBConfig.TPV_FAR_ARY[farLv];
         Vec3 looking = player.getViewVector(partialTick);
         double dist = getDistanceConsiderBlock(mc, player, (double)far, partialTick, mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT);
         if (mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            looking.x *= -dist;
            looking.y *= -dist;
            looking.z *= -dist;
         } else {
            looking.x *= dist;
            looking.y *= dist;
            looking.z *= dist;
         }

         camEntity.setPos(player.getX() + looking.x, player.getY() + looking.y, player.getZ() + looking.z);
         camEntity.xo = player.xo + looking.x;
         camEntity.yo = player.yo + looking.y;
         camEntity.zo = player.zo + looking.z;
         camEntity.xOld = player.xOld + looking.x;
         camEntity.yOld = player.yOld + looking.y;
         camEntity.zOld = player.zOld + looking.z;
         camEntity.setXRot(player.getXRot());
         camEntity.xRotO = player.xRotO;
         camEntity.setYRot(player.getYRot());

         for(camEntity.yRotO = player.yRotO; camEntity.getYRot() - camEntity.yRotO < -180.0F; camEntity.yRotO -= 360.0F) {
         }

         while(camEntity.getYRot() - camEntity.yRotO >= 180.0F) {
            camEntity.yRotO += 360.0F;
         }

         mc.setCameraEntity(camEntity);
         return true;
      }
   }

   private static double getDistanceConsiderBlock(Minecraft mc, Entity player, double dist, float partialTicks, boolean reverse) {
      float f = player.getEyeHeight();
      double d0 = player.xo + (player.getX() - player.xo) * (double)partialTicks;
      double d1 = player.yo + (player.getY() - player.yo) * (double)partialTicks + (double)f;
      double d2 = player.zo + (player.getZ() - player.zo) * (double)partialTicks;
      float f1 = player.getYRot();
      float f2 = player.getXRot();
      if (reverse) {
         f2 += 180.0F;
      }

      double d4 = (double)(-Mth.sin(f1 * 0.017453292F) * Mth.cos(f2 * 0.017453292F)) * dist;
      double d5 = (double)(Mth.cos(f1 * 0.017453292F) * Mth.cos(f2 * 0.017453292F)) * dist;
      double d6 = (double)(-Mth.sin(f2 * 0.017453292F)) * dist;

      for(int i = 0; i < 8; ++i) {
         float f3 = (float)((i & 1) * 2 - 1);
         float f4 = (float)((i >> 1 & 1) * 2 - 1);
         float f5 = (float)((i >> 2 & 1) * 2 - 1);
         f3 *= 0.1F;
         f4 *= 0.1F;
         f5 *= 0.1F;
         BlockHitResult raytraceresult = mc.level.clip(new ClipContext(new Vec3(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
         if (raytraceresult.getType() != HitResult.Type.MISS) {
            double d7 = raytraceresult.getLocation().distanceTo(new Vec3(d0, d1, d2));
            if (d7 < dist) {
               dist = d7;
            }
         }
      }

      return dist;
   }
}
