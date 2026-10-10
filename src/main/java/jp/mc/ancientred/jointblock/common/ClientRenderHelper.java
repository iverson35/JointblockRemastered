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
         // 1.12 created this camera in onEntityJBJoinedWorld, which only fires when the unit spawns
         // with the far-view flag already set. Creating it on demand also covers the unit that is
         // assembled first and gets the flag later (the core GUI toggles it while a unit exists).
         EntityJBClientCamera created = new EntityJBClientCamera(entityJointed.level());
         created.parentEntity = entityJointed;
         entityJointed.entityCamera = created;
         eCamera = created;
      }

      {
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

         // Camera#setup lerps xo -> x itself, so the previous-position fields must hold the player's
         // previous-tick position plus the *same* offset: that makes the lerp produce exactly
         // playerPos(partialTick) + offset, i.e. an orbit centred on the interpolated player.
         //
         // The eye height belongs in that base position: this camera is an entity whose own
         // getEyeHeight() is 0, so Camera#setup adds nothing, while the vanilla third-person camera
         // of the player starts from its eyes. 1.12 got the same result by translating the already
         // eye-positioned camera, so leaving it out drops the view by 1.62 blocks.
         double eye = (double)player.getEyeHeight();
         camEntity.setPos(player.getX() + looking.x, player.getY() + eye + looking.y, player.getZ() + looking.z);
         camEntity.xo = player.xo + looking.x;
         camEntity.yo = player.yo + eye + looking.y;
         camEntity.zo = player.zo + looking.z;
         camEntity.xOld = player.xOld + looking.x;
         camEntity.yOld = player.yOld + eye + looking.y;
         camEntity.zOld = player.zOld + looking.z;

         // The rotation must be exactly the one the offset above was built from: getViewVector uses
         // the player's getViewXRot/getViewYRot, and Camera#setup reads those same two accessors off
         // the camera entity before adding its own 4-block zoom. Writing player.getXRot()/xRotO here
         // instead hands Camera#setup an interpolated pitch, while a LocalPlayer reports its *raw*
         // pitch as its view pitch - one tick apart. The zoom then runs along a line the offset was
         // not computed for and the camera swings off the player-centred orbit while the view turns.
         float viewPitch = player.getViewXRot(partialTick);
         float viewYaw = player.getViewYRot(partialTick);
         camEntity.setXRot(viewPitch);
         camEntity.xRotO = viewPitch;
         camEntity.setYRot(viewYaw);
         camEntity.yRotO = viewYaw;

         mc.setCameraEntity(camEntity);
         return true;
      }
   }

   private static double getDistanceConsiderBlock(Minecraft mc, Entity player, double dist, float partialTicks, boolean reverse) {
      float f = player.getEyeHeight();
      double d0 = player.xo + (player.getX() - player.xo) * (double)partialTicks;
      double d1 = player.yo + (player.getY() - player.yo) * (double)partialTicks + (double)f;
      double d2 = player.zo + (player.getZ() - player.zo) * (double)partialTicks;
      // Cast the ray along the same view angles the offset it shortens was computed from
      // (getViewVector in setUpThirdPersonFarViewCamera) rather than the raw yRot/xRot: the two are
      // the same for a LocalPlayer today, but keeping both on the view accessors is what makes the
      // hit distance - and so the camera distance - stay in step with the offset it is limiting.
      float f1 = player.getViewYRot(partialTicks);
      float f2 = player.getViewXRot(partialTicks);
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
