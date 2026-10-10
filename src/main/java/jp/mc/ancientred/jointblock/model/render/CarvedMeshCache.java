package jp.mc.ancientred.jointblock.model.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.carving.JBCarvingClientUtil;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeGathered;
import jp.mc.ancientred.jointblock.carving.pattern.JBPatternCarvingClientUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

/**
 * Per-part copy of one carved part's local-space mesh, so the carving emitters run once per carving
 * data change instead of once per frame.
 *
 * <p>1.12.2 compiled each carved part into a display list ({@code glNewList}/{@code glCallList}) and
 * only re-compiled it when the carving data or the palette changed. The first 1.20.1 port dropped
 * that and re-emitted every carved voxel of every part on every frame - for a machine whose parts
 * use {@code divide = 8} that is 512 cell iterations and, for a solid part, up to 384 faces (2304
 * vertices) per part per frame, all of it thrown away at the end of the frame.
 *
 * <p>This class restores the display-list behaviour without touching GL: the emitters write into a
 * {@link Capture} consumer with an identity matrix, the resulting local-space positions and vertex
 * colours are kept, and every frame they are replayed through the live {@link Matrix4f}. The
 * captured geometry is only rebuilt when the carving data (identity), the carve type, the gathered
 * M-cube density (identity) or the palette contents changed.
 *
 * <p>Palette contents are compared instead of a version counter on purpose: the port's palettes are
 * mutated in place in several places (e.g. {@code EntityJointed#boFixedPaletteDataList} is filled by
 * {@code JBCarvingDataUtil#readPaletteDataFromBBuf} + {@code fixPaletteByteOrder} after the outer
 * array was created), so an identity check would silently keep stale colours. Comparing the eight
 * ints a part's palette actually has costs a handful of comparisons per part per frame.
 *
 * <p>The colours stored here are the final shaded vertex colours the emitters produced, i.e. the
 * geometry is pixel-identical to what the emitters write directly - only the number of frames on
 * which they run changes.
 */
@OnlyIn(Dist.CLIENT)
public final class CarvedMeshCache {
    /** Identity matrix the emitters are captured with; the captured positions are part-local. */
    private static final Matrix4f IDENTITY = new Matrix4f();

    private float[] positions = new float[0];
    private int[] colors = new int[0];
    private int vertexCount;

    private int[] builtCarving;
    private int builtType = -1;
    private Object builtGathered;
    private final int[] builtPalette = new int[16];
    private boolean built;

    private final Capture capture = new Capture();

    /**
     * Replays the part's carved mesh, rebuilding it first when anything it was built from changed.
     *
     * @param bb          the buffer to write to (a {@code position_color} triangle buffer)
     * @param matrix      the part's model-view matrix
     * @param carvingData the part's carving data
     * @param type        the carve type ({@code 0}/boxes, {@code 2}/pattern, {@code 4}/M-cube)
     * @param paletteData the palette the part is drawn with
     * @param gathered    the gathered M-cube density, {@code null} for the other carve types
     */
    public void render(VertexConsumer bb, Matrix4f matrix, int[] carvingData, int type, int[] paletteData,
                       MCubeGathered gathered) {
        if (!isValid(carvingData, type, paletteData, gathered)) {
            rebuild(carvingData, type, paletteData, gathered);
        }

        if (this.vertexCount == 0) {
            return;
        }

        float[] positions = this.positions;
        int[] colors = this.colors;

        for (int i = 0, v = 0; i < this.vertexCount; ++i, v += 3) {
            int color = colors[i];
            bb.vertex(matrix, positions[v], positions[v + 1], positions[v + 2])
                .color(color & 255, color >> 8 & 255, color >> 16 & 255, color >>> 24)
                .endVertex();
        }
    }

    /**
     * Marks the cached mesh stale, for owners that know a carve-data write happened without the
     * array being replaced (the joint core clears {@code isCarvingCompiled} at every such write,
     * exactly as the 1.12.2 compile step did).
     */
    public void invalidate() {
        this.built = false;
    }

    /** True when the cached mesh was built from exactly these inputs. */
    private boolean isValid(int[] carvingData, int type, int[] paletteData, MCubeGathered gathered) {
        if (!this.built || carvingData != this.builtCarving || type != this.builtType || gathered != this.builtGathered) {
            return false;
        }

        if (paletteData == null) {
            return this.builtPalette[0] == 0;
        }

        int len = Math.min(paletteData.length, this.builtPalette.length);

        for (int i = 0; i < len; ++i) {
            if (paletteData[i] != this.builtPalette[i]) {
                return false;
            }
        }

        return true;
    }

    /** Re-runs the carving emitters into the capture consumer, with an identity matrix. */
    private void rebuild(int[] carvingData, int type, int[] paletteData, MCubeGathered gathered) {
        this.capture.reset();

        switch (type) {
            case 2:
                JBPatternCarvingClientUtil.renderPatternCarvedModel(this.capture, IDENTITY, carvingData, paletteData);
                break;
            case 4:
                JBCarvingClientUtil.renderMCubesCarvedModel(this.capture, IDENTITY, carvingData, gathered, paletteData);
                break;
            default:
                JBCarvingClientUtil.renderBoxesCarvedModelGreedy(this.capture, IDENTITY, carvingData, paletteData);
        }

        this.vertexCount = this.capture.count;
        this.positions = this.capture.pos;
        this.colors = this.capture.col;

        this.builtCarving = carvingData;
        this.builtType = type;
        this.builtGathered = gathered;
        this.built = true;

        java.util.Arrays.fill(this.builtPalette, 0);

        if (paletteData != null) {
            System.arraycopy(paletteData, 0, this.builtPalette, 0, Math.min(paletteData.length, this.builtPalette.length));
        }
    }

    /**
     * Swallows the emit calls and keeps the local-space position plus the shaded vertex colour of
     * every vertex. The carving emitters only ever call {@code vertex}, {@code color},
     * {@code normal} and {@code endVertex}, so the remaining methods are no-ops.
     */
    @OnlyIn(Dist.CLIENT)
    private static final class Capture implements VertexConsumer {
        private float[] pos = new float[3 * 4096];
        private int[] col = new int[4096];
        private int count;

        private float x;
        private float y;
        private float z;
        private int red = 255;
        private int green = 255;
        private int blue = 255;
        private int alpha = 255;

        private void reset() {
            this.count = 0;
        }

        private void ensureRoom() {
            if (this.count >= this.col.length) {
                int grown = this.col.length * 2;
                float[] pos = new float[grown * 3];
                System.arraycopy(this.pos, 0, pos, 0, this.pos.length);
                this.pos = pos;
                int[] col = new int[grown];
                System.arraycopy(this.col, 0, col, 0, this.col.length);
                this.col = col;
            }
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.ensureRoom();
            this.x = (float)x;
            this.y = (float)y;
            this.z = (float)z;
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void endVertex() {
            int i = this.count++;
            int v = i * 3;
            this.pos[v] = this.x;
            this.pos[v + 1] = this.y;
            this.pos[v + 2] = this.z;
            this.col[i] = this.alpha << 24 | this.blue << 16 | this.green << 8 | this.red;
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
        }

        @Override
        public void unsetDefaultColor() {
        }
    }
}
