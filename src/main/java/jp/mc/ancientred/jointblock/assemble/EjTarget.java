package jp.mc.ancientred.jointblock.assemble;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EjTarget {
   public static final int FOLLOW_MODE_XY = 0;
   public static final int FOLLOW_MODE_Y = 1;
   public static final int FOLLOW_MODE_X = 2;
   public static final int SENDFLAG_ENTITY = 0;
   public static final int SENDFLAG_BLOCK = 1;
   public boolean isTargetEntity;
   public Entity targetEntity;
   public int worldPosX;
   public int worldPosY;
   public int worldPosZ;
   public int usedCountInTick;

   public EjTarget(Entity entity) {
      this.isTargetEntity = true;
      this.targetEntity = entity;
   }

   public EjTarget(int wpX, int wpY, int wpZ) {
      this.isTargetEntity = false;
      this.worldPosX = wpX;
      this.worldPosY = wpY;
      this.worldPosZ = wpZ;
   }

   public boolean isTargetOwnerPlayer(UUID ownerUUID) {
      return this.targetEntity != null && this.targetEntity instanceof Player ? ownerUUID.equals(this.targetEntity.getUUID()) : false;
   }

   public void writeToBuffer(FriendlyByteBuf buffer) {
      if (this.isTargetEntity) {
         buffer.writeInt(0);
         if (this.targetEntity == null) {
            buffer.writeInt(-1);
         } else {
            buffer.writeInt(this.targetEntity.getId());
         }

         buffer.writeInt(0);
         buffer.writeInt(0);
      } else {
         buffer.writeInt(1);
         buffer.writeInt(this.worldPosX);
         buffer.writeInt(this.worldPosY);
         buffer.writeInt(this.worldPosZ);
      }

   }

   public void writeToBuffer2(FriendlyByteBuf buffef) throws Exception {
      if (this.isTargetEntity) {
         buffef.writeInt(0);
         if (this.targetEntity == null) {
            buffef.writeInt(-1);
         } else {
            buffef.writeInt(this.targetEntity.getId());
         }

         buffef.writeInt(0);
         buffef.writeInt(0);
      } else {
         buffef.writeInt(1);
         buffef.writeInt(this.worldPosX);
         buffef.writeInt(this.worldPosY);
         buffef.writeInt(this.worldPosZ);
      }

   }

   public static EjTarget readFromBuffer(Level world, ByteBuf buffef) {
      int flag = buffef.readInt();
      int va = buffef.readInt();
      int vb = buffef.readInt();
      int vc = buffef.readInt();
      EjTarget ret = null;
      if (flag == 0) {
         Entity entity = world.getEntity(va);
         if (entity != null) {
            ret = new EjTarget(entity);
         }
      } else if (flag == 1) {
         ret = new EjTarget(va, vb, vc);
      }

      return ret;
   }
}
