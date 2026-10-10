package jp.mc.ancientred.jointblock.entity.fx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
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

    /**
     * 1.12.2 {@code EntityFX.renderParticle} multiplied {@code particleScale} by 0.1F before
     * emitting the quad, so its scale of 1.0 was a tenth of a block. 1.20.1's
     * {@code SingleQuadParticle.quadSize} is already measured in blocks, so the same look is
     * {@code 0.1F}; using 1.0F directly made the thruster smoke ten times too large.
     */
    private static final float BASE_QUAD_SIZE = 0.1F;

    private static final ResourceLocation[] FRAMES = new ResourceLocation[FRAME_COUNT];

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            FRAMES[i] = new ResourceLocation("minecraft", "generic_" + i);
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
        this.quadSize = BASE_QUAD_SIZE;
        this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
        // 1.12.2: this.field_190017_n = false (canCollide)
        this.hasPhysics = false;
        this.setFrame(FRAME_COUNT - 1);
    }

    private void setFrame(int frame) {
        // 1.12.2 asked the block atlas for "minecraft:particle/generic_N" (its particle sheet was
        // stitched into it). 1.20.1 keeps the particle sheet in its own atlas owned by the
        // ParticleEngine (assets/minecraft/atlases/particles.json, a directory source over
        // "particle" with an empty prefix, so the sprite ids carry no "particle/" segment - the
        // vanilla smoke.json lists them as minecraft:generic_7..generic_0). A "particle/..."
        // lookup therefore missed and produced the missing-texture magenta/black quad.
        this.setSprite(Minecraft.getInstance().particleEngine.textureAtlas
            .getSprite(FRAMES[Mth.clamp(frame, 0, FRAME_COUNT - 1)]));
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // 1.12.2 func_180434_a: scale the particle by progress through its lifetime * 32, in the
        // original's tenth-of-a-block units.
        float scale = ((float) this.age + partialTicks) / (float) this.lifetime * 32.0F;
        if (scale < 0.0F) {
            scale = 0.0F;
        }
        if (scale > 1.0F) {
            scale = 1.0F;
        }
        this.quadSize = scale * BASE_QUAD_SIZE;
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
