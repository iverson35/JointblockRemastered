package jp.mc.ancientred.jointblock.model.render;

import com.mojang.blaze3d.vertex.PoseStack;
import jp.mc.ancientred.jointblock.assemble.ModelViewMatrix;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

/**
 * Bridges the mod's own {@link ModelViewMatrix} stack to a 1.20.1 {@link PoseStack}.
 *
 * <p>1.12.2 built these matrices CPU side and then called
 * {@code GlStateManager.multMatrix(matrix.getMatrixTransferredFBuf())} right before drawing a part.
 * {@code multMatrix} is a right-multiply of the current GL model-view matrix, which is exactly what
 * {@link Matrix4f#mul(Matrix4f)} does on {@code PoseStack.last().pose()}.
 *
 * <p>Ordering: {@code ModelViewMatrix.pushInListWithGLOrder} emits {@code m00, m10, m20, m30, m01,
 * ...} - i.e. the mod field {@code mRC} is GL column {@code R}, row {@code C} - and that is the same
 * column-major order the {@link Matrix4f} constructor expects, so the 16 values are passed straight
 * through.
 */
@OnlyIn(Dist.CLIENT)
public final class JBPoseHelper {

    private JBPoseHelper() {
    }

    /** 1.12.2 {@code GlStateManager.multMatrix(mv.getMatrixTransferredFBuf())}. */
    public static void multiply(PoseStack pose, ModelViewMatrix mv) {
        pose.last().pose().mul(toMatrix4f(mv));
    }

    public static Matrix4f toMatrix4f(ModelViewMatrix m) {
        return new Matrix4f(
            m.m00, m.m10, m.m20, m.m30,
            m.m01, m.m11, m.m21, m.m31,
            m.m02, m.m12, m.m22, m.m32,
            m.m03, m.m13, m.m23, m.m33);
    }
}
