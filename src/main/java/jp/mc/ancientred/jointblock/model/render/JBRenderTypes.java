package jp.mc.ancientred.jointblock.model.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Render types for the joint geometry.
 *
 * <p>{@link RenderType} equality is identity based and a {@code BufferSource} starts a new batch
 * whenever the instance changes, so the per-texture instances are cached instead of being built per
 * draw call.
 */
@OnlyIn(Dist.CLIENT)
public final class JBRenderTypes {

    private static final Map<ResourceLocation, RenderType> MODEL_TYPES = new HashMap<>();

    private JBRenderTypes() {
    }

    /**
     * Entity cutout, no back-face culling. The joint parts are openable shells drawn from the
     * inside, and {@code ModelPart} mirroring reverses quad winding, so an unculled cutout is the
     * only variant that cannot drop a face. Alpha is a hard cutout, matching 1.12.2, which drew
     * these with GL_ALPHA_TEST/GL_CULL_FACE and no blending.
     */
    public static RenderType jointModel(ResourceLocation texture) {
        return MODEL_TYPES.computeIfAbsent(texture, RenderType::entityCutoutNoCull);
    }

    /**
     * Untextured, unculled, opaque quads that write depth.
     *
     * <p>1.12.2 drew the carved voxel geometry by binding a white 4x4 texture and letting the vertex
     * colour through with GL lighting on (the shading is baked into the vertex colour here, see
     * {@code JBCarvingClientUtil#faceLight}), culling and blending off.
     *
     * <p>{@link RenderType#debugQuads()} is <em>not</em> usable for this: it is a debug state set
     * with translucent blending and {@code sortOnUpload = true}, so every frame the whole vertex
     * buffer was re-sorted by distance and blended - a several-hundred-thousand-quad batch makes
     * that the single most expensive thing in the frame. This state set is the same
     * {@code position_color} shader without blending, without sorting and with a large buffer.
     */
    private static final RenderType SOLID_COLOR = solid("jointblock_solid_color", VertexFormat.Mode.QUADS);

    /**
     * Same state as {@link #SOLID_COLOR}, but a {@code TRIANGLES} buffer.
     *
     * <p>The carving emitters ({@code JBCarvingClientUtil}/{@code JBPatternCarvingClientUtil}) write
     * explicit triangles: two triangles per face for the box/M-cube path and one per pattern cell.
     * A {@code QUADS} buffer consumes four vertices at a time, so those six-vertex faces lost their
     * second half and every face degraded to a single diagonal triangle.
     */
    private static final RenderType SOLID_COLOR_TRIANGLES = solid("jointblock_solid_color_triangles", VertexFormat.Mode.TRIANGLES);

    private static RenderType solid(String name, VertexFormat.Mode mode) {
        return RenderType.create(
            name,
            DefaultVertexFormat.POSITION_COLOR,
            mode,
            2097152,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, true))
                .setDepthTestState(new RenderStateShard.DepthTestStateShard("jointblock_lequal_depth_test", 515))
                .setCullState(new RenderStateShard.CullStateShard(false))
                .setLightmapState(new RenderStateShard.LightmapStateShard(false))
                .setOverlayState(new RenderStateShard.OverlayStateShard(false))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("jointblock_no_transparency",
                    RenderSystem::disableBlend, () -> {}))
                .createCompositeState(false));
    }

    public static RenderType solidColor() {
        return SOLID_COLOR;
    }

    /** The {@code TRIANGLES} variant of {@link #solidColor()}, for the carving emitters. */
    public static RenderType solidColorTriangles() {
        return SOLID_COLOR_TRIANGLES;
    }
}
