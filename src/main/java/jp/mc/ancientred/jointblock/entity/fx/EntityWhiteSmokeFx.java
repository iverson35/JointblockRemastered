package jp.mc.ancientred.jointblock.entity.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * White thruster smoke: a short-lived particle that grows from 0 to 1 over its life and
 * walks the eight frames of the vanilla generic particle sheet, exactly as the 1.12.2
 * original did via {@code setParticleTextureIndex(7 - age * 8 / lifetime)}.
 *
 * <p>Client-only: spawned from the thruster action, bullet and temporary worker effects.
 */
@OnlyIn(Dist.CLIENT)
public class EntityWhiteSmokeFx extends TextureSheetParticle {

    /** Frame count of the vanilla {@code particle/generic_N} sheet. */
    private static final int FRAME_COUNT = 8;

    private static final ResourceLocation[] FRAMES = new ResourceLocation[FRAME_COUNT];

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            FRAMES[i] = new ResourceLocation("minecraft", "particle/generic_" + i);
        }
    }

    public EntityWhiteSmokeFx(Level level, double x, double y, double z) {
        this((ClientLevel) level, x, y, z);
    }

    public EntityWhiteSmokeFx(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.xd = Math.random() * 0.1 - 0.05;
        this.yd = Math.random() * 0.1 - 0.05;
        this.zd = Math.random() * 0.1 - 0.05;
        float shade = 0.8F + (float) (Math.random() * 0.2);
        this.rCol = shade;
        this.gCol = shade;
        this.bCol = shade;
        this.quadSize = 1.0F;
        this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
        // 1.12.2: this.field_190017_n = false (canCollide)
        this.hasPhysics = false;
        this.setFrame(FRAME_COUNT - 1);
    }

    private void setFrame(int frame) {
        this.setSprite(Minecraft.getInstance()
            .getTextureAtlas(TextureAtlas.LOCATION_PARTICLES)
            .apply(FRAMES[Mth.clamp(frame, 0, FRAME_COUNT - 1)]));
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // 1.12.2 func_180434_a: scale the particle by progress through its lifetime * 32.
        float scale = ((float) this.age + partialTicks) / (float) this.lifetime * 32.0F;
        if (scale < 0.0F) {
            scale = 0.0F;
        }
        if (scale > 1.0F) {
            scale = 1.0F;
        }
        this.quadSize = scale;
        super.render(buffer, camera, partialTicks);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        this.setFrame(FRAME_COUNT - 1 - this.age * FRAME_COUNT / this.lifetime);
        this.move(this.xd, this.yd, this.zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
