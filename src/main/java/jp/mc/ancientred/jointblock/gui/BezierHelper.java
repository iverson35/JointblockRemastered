package jp.mc.ancientred.jointblock.gui;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import org.joml.Matrix4f;

/**
 * 1.12.2 {@code BezierHelper}, maths/state port.
 *
 * <p>The 1.12.2 method {@code addBezierLineStrips(BufferBuilder bb, double z, float steps, int r,
 * int g, int b, int a)} walked the curve and, for every sample, emitted the two
 * <em>GL_LINE_STRIP</em> vertices {@code (p1, p2)} in the immediate-mode {@code BufferBuilder}
 * before advancing {@code p1 = p2}.  It also derived {@link #midLine} / {@link #reqPosLine} from
 * that walk.  The 1.20.1 port keeps the identical walk and colour/offset derivation and emits into
 * the {@code VertexConsumer} supplied by the caller (the caller obtains it from
 * {@code GuiGraphics.bufferSource().getBuffer(RenderType.debugLineStrip(width))}, which is the
 * {@code POSITION_COLOR} / {@code DEBUG_LINE_STRIP} equivalent of the 1.12.2 {@code BufferBuilder}
 * start mode {@code 3} + {@code DefaultVertexFormats.field_181706_f}).
 */
public class BezierHelper {

    public double zValue = 0.0;
    private int ctrlSize = 0;
    private float[][] ctrlPts;
    private float[] workA = new float[2];
    private float[] workB = new float[2];
    public float[] midLine = new float[4];
    public float[] reqPosLine = new float[4];
    public boolean gotMidLine;
    public boolean gotRequestPosLine;
    public float requestPos = 0.0F;

    public BezierHelper(int initCap) {
        if (initCap <= 0) {
            initCap = 1;
        }

        this.ctrlPts = new float[initCap][];
    }

    public void clear(float requestPos) {
        this.ctrlSize = 0;
        this.gotMidLine = false;
        this.gotRequestPosLine = false;
        this.requestPos = requestPos;
    }

    public void addVerts2f(float x, float y) {
        this.ensureCapacity(this.ctrlSize + 1);
        if (this.ctrlPts[this.ctrlSize] == null) {
            this.ctrlPts[this.ctrlSize] = new float[2];
        }

        this.ctrlPts[this.ctrlSize][0] = x;
        this.ctrlPts[this.ctrlSize][1] = y;
        ++this.ctrlSize;
    }

    /**
     * 1.12.2 {@code addBezierLineStrips}: walks the bezier and emits one {@code GL_LINE_STRIP}
     * vertex pair per sample into {@code vc} ({@code POSITION_COLOR} format - do NOT add uv/uv2/
     * normal/overlay calls), deriving {@link #midLine} / {@link #reqPosLine} from the same walk.
     */
    public void addBezierLineStrips(VertexConsumer vc, Matrix4f matrix, double z, float steps, int r, int g, int b, int a) {
        if (this.ctrlSize > 0) {
            float[] p2 = this.workB;
            float[] p1 = this.workA;
            p1[0] = this.ctrlPts[0][0];
            p1[1] = this.ctrlPts[0][1];

            for (float t = 0.0F; t <= 1.0F; t += steps) {
                p2 = bernstein(this.ctrlPts, p2, t, this.ctrlSize - 1);
                vc.vertex(matrix, p1[0], p1[1], (float)z).color(r, g, b, a).endVertex();
                vc.vertex(matrix, p2[0], p2[1], (float)z).color(r, g, b, a).endVertex();
                if (!this.gotMidLine && t > 0.5F) {
                    this.midLine[0] = p1[0];
                    this.midLine[1] = p1[1];
                    this.midLine[2] = p2[0];
                    this.midLine[3] = p2[1];
                    this.gotMidLine = true;
                }

                if (!this.gotRequestPosLine && t > this.requestPos) {
                    this.reqPosLine[0] = p1[0];
                    this.reqPosLine[1] = p1[1];
                    this.reqPosLine[2] = p2[0];
                    this.reqPosLine[3] = p2[1];
                    this.gotRequestPosLine = true;
                }

                p1[0] = p2[0];
                p1[1] = p2[1];
            }
        }
    }

    private void ensureCapacity(int minCap) {
        int oldCap = this.ctrlPts.length;
        if (minCap > oldCap) {
            int newCap = oldCap + (oldCap >> 1);
            this.ctrlPts = Arrays.copyOf(this.ctrlPts, newCap < minCap ? minCap : newCap);
        }
    }

    private static float[] bernstein(float[][] ctrlPoints, float[] work, float t, int n) {
        work[1] = work[0] = 0.0F;

        for (int i = 0; i <= n; ++i) {
            work[0] += ctrlPoints[i][0] * binomial(i, n) * (float)Math.pow((double)t, (double)((float)i)) * (float)Math.pow((double)(1.0F - t), (double)((float)(n - i)));
            work[1] += ctrlPoints[i][1] * binomial(i, n) * (float)Math.pow((double)t, (double)((float)i)) * (float)Math.pow((double)(1.0F - t), (double)((float)(n - i)));
        }

        return work;
    }

    private static float binomial(int f, int n) {
        return factorial(n) / (factorial(f) * factorial(n - f));
    }

    private static float factorial(int x) {
        if (x != 0 && x != 1) {
            int fact = 1;

            for (int i = 1; i <= x; ++i) {
                fact *= i;
            }

            return (float)fact;
        } else {
            return 1.0F;
        }
    }
}
