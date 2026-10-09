package jp.mc.ancientred.jointblock.model.render;

import net.minecraft.resources.ResourceLocation;

/**
 * Model textures.
 *
 * <p>1.12.2 preloaded these through a {@code TextureManager} and passed the resulting GL id around
 * ({@code TexResLoc.texId}, {@code whiteResTexId}), plus a runtime 4x4 white {@code DynamicTexture}.
 * A 1.20.1 {@link net.minecraft.client.renderer.RenderType} carries the {@link ResourceLocation}
 * itself and the bake happens on demand, so all of the preload/id machinery and both the white
 * texture and the four 1.12.2 textures that nothing referenced ({@code normalRes},
 * {@code simpleRes}, {@code rotatorRes}, {@code simpleJB2Res}) are gone.
 */
public final class LoadedTextures {

    public static final ResourceLocation coreNormalRes = new ResourceLocation("jointblock:textures/entity/core.png");
    public static final ResourceLocation coreSlaveRes = new ResourceLocation("jointblock:textures/entity/core_slave.png");
    public static final ResourceLocation seatRes = new ResourceLocation("jointblock:textures/entity/seat.png");
    public static final ResourceLocation scalerRes = new ResourceLocation("jointblock:textures/entity/scaler.png");
    public static final ResourceLocation sliderRes = new ResourceLocation("jointblock:textures/entity/slider.png");
    public static final ResourceLocation extenderRes = new ResourceLocation("jointblock:textures/entity/extender.png");
    public static final ResourceLocation attracter_interfaceRes = new ResourceLocation("jointblock:textures/entity/attracter_interface.png");
    public static final ResourceLocation cameraRes = new ResourceLocation("jointblock:textures/entity/camera.png");
    public static final ResourceLocation errRes = new ResourceLocation("jointblock:textures/entity/error.png");
    public static final ResourceLocation simpleJBRes = new ResourceLocation("jointblock:textures/entity/simplejb.png");

    private LoadedTextures() {
    }
}
