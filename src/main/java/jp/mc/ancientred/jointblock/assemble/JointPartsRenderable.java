package jp.mc.ancientred.jointblock.assemble;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jp.mc.ancientred.jointblock.JointBlockRemastered;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.assemble.tuning.StaticClientTuningStates;
import jp.mc.ancientred.jointblock.assemble.tuning.TuningBarInf;
import jp.mc.ancientred.jointblock.carving.JBCarvingDataUtil;
import jp.mc.ancientred.jointblock.carving.mcube.MCubeGathered;
import jp.mc.ancientred.jointblock.carving.prebuild.PbCarvingCell;
import jp.mc.ancientred.jointblock.carving.prebuild.Prebuilder;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.config.JBConfig;
import jp.mc.ancientred.jointblock.effect.EffectHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlot;
import jp.mc.ancientred.jointblock.gui.OverlayRendeler;
import jp.mc.ancientred.jointblock.model.BasicJointRenderer;
import jp.mc.ancientred.jointblock.model.render.CarvedMeshCache;
import jp.mc.ancientred.jointblock.model.render.CarvingRenderer;
import jp.mc.ancientred.jointblock.model.render.JBPoseHelper;
import jp.mc.ancientred.jointblock.model.render.JBRenderTypes;
import jp.mc.ancientred.jointblock.model.tuning.ModelTuningBladeExtend;
import jp.mc.ancientred.jointblock.model.tuning.ModelTuningBladeRotateX;
import jp.mc.ancientred.jointblock.model.tuning.ModelTuningBladeRotateY;
import jp.mc.ancientred.jointblock.model.tuning.ModelTuningBladeRotateZ;
import jp.mc.ancientred.jointblock.model.tuning.ModelTuningBladeShrink;
import jp.mc.ancientred.jointblock.model.tuning.ModelTuningBladeSlideX;
import jp.mc.ancientred.jointblock.model.tuning.ModelTuningBladeSlideY;
import jp.mc.ancientred.jointblock.model.tuning.ModelTuningBladeSlideZ;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Render side subclass of {@link JointParts} for one assembled {@link EntityJointed} unit: it walks
 * the part list, rebuilds each part's transform on the shared {@link ModelViewMatrix} stack and
 * draws the joint geometry, the carving/prebuild data and the tuning/selection helpers.
 *
 * <p><b>Where the matrix comes from.</b> 1.12.2 passed the prepared {@code ModelViewMatrix} into
 * {@link #renderAllPartsWithPrebuild} / {@link #renderAllPartsWithOwnMatrix}. The 1.20.1 entry
 * points take the {@link PoseStack} instead, so the per-frame matrix is read from
 * {@link ModelViewMatrix#tHreadMvMatrix}. The caller (the entity renderer) must
 * {@code reset()} it, apply {@code rotateYY}/{@code setRotation} + {@code scale(1,-1,-1)} +
 * {@code translate(0,-1.5-coreHeight,0)} + {@code saveBaseRotation()} before calling, and must not
 * translate it by the entity position - the dispatcher {@link PoseStack} is already at the entity
 * origin.
 *
 * <p><b>1.12.2 -> 1.20.1 substitutions</b> (a GL call with no 1.20.1 counterpart):
 * <ul>
 *   <li>{@code pushMatrix/popMatrix} -> {@link PoseStack#pushPose()}/{@link PoseStack#popPose()},
 *       {@code multMatrix(mv.getMatrixTransferredFBuf())} -> {@link JBPoseHelper#multiply}.</li>
 *   <li>{@code translate/scale/rotate} -> {@code pose.translate/pose.scale/pose.mulPose}
 *       (all right-multiplications, so the order is kept verbatim).</li>
 *   <li>{@code bindTexture(whiteResTexId)} + {@code GlStateManager.color(...)} + lighting/cull
 *       toggles -> {@link JBRenderTypes#solidColor()} (unlit POSITION_COLOR quads), which is the
 *       same "white texture times vertex colour" result.</li>
 *   <li>Multi pass ({@code MinecraftForgeClient.getRenderPass()}) does not exist: everything is
 *       drawn in one pass and {@code matrix.renderPass} is left at 0. The pass gates therefore all
 *       take their pass-0 branch, and the 1.12 pass-1-only attracter beam is drawn in that same
 *       pass. {@code getShouldRender} (a pass/layer split) is consequently always true.</li>
 *   <li>{@code GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)} (wireframe highlight of the selected
 *       joint) has no 1.20.1 equivalent: a {@link VertexConsumer} can only emit the primitive mode
 *       its RenderType carries (all joint models are QUADS) and polygon mode is GL state owned by
 *       the pipeline. The part is drawn normally instead; the same selection is still indicated by
 *       the tuning bars and the selection box, which are drawn for exactly the same conditions.</li>
 *   <li>The displayed block model: 1.12.2 {@code Block.getBlockById(id).getStateFromMeta(modelMeta)}
 *       is impossible in 1.20.1 (block states have no numeric metadata), so the block's
 *       {@code defaultBlockState()} is used and {@code modelMeta} is only kept for the item-model
 *       path.</li>
 *   <li>Nested block-entity model: {@code ITileEntityProvider} ->
 *       {@link EntityBlock#newBlockEntity} + {@code BlockEntityRenderDispatcher.render}.</li>
 *   <li>{@code RenderGlobal.drawBoundingBox} -> {@link LevelRenderer#renderLineBox}.</li>
 *   <li>GL display lists are gone, so the {@code finalize()} override that freed
 *       {@code carvingCompiledListNum} through {@code GLCompiledListManager} is dropped, and the
 *       {@code Tessellator} flush in the failure handler has no counterpart (no open immediate-mode
 *       batch exists any more). {@link #isCarvingCompiled}/{@link #carvingCompiledListNum} are kept
 *       as data only, mirroring the ported fields on {@code TileEntityJoint}; the compiled mesh
 *       itself now lives in {@link #carvedMeshCache}.</li>
 * </ul>
 *
 * <p>The rotator/pivot branch reproduces the local-variable layout that the ported
 * {@code JointParts.updateParts} uses (real interpolated rotations + separate pivot shifts); the
 * 1.12.2 decompiler had collapsed both into one reused local slot.
 */
public class JointPartsRenderable extends JointParts {
   private IJBModelItem.IJBModel modelToRender;
   private BlockState blockStateToRender;
   private boolean checkdTileEntity;
   private BlockEntity tileEntityToRender;
   private boolean failedOnRender = false;
   public int carvingType = 0;
   public MCubeGathered mCubeGatheredData;
   /** Local-space carved mesh of this part, replayed per frame instead of re-emitting every voxel. */
   public final CarvedMeshCache carvedMeshCache = new CarvedMeshCache();
   /** 1.12.2 display-list bookkeeping; the 1.20.1 equivalent is {@link #carvedMeshCache}. */
   public boolean isCarvingCompiled = false;
   /** 1.12.2 display-list handle; always 0 in the 1.20.1 port. */
   public int carvingCompiledListNum = 0;
   public short group;
   public JBRAdditional additional;
   private static final double SHRINK_PADDING = 0.01;

   public JointPartsRenderable(int posX, int posY, int posZ) {
      super(posX, posY, posZ);
   }

   @Override
   public void detectCarvingType() {
      if (this.carvingData != null && this.carvingData.length > 0) {
         this.carvingType = JBCarvingDataUtil.getCarvingType(this.carvingData);
      }
   }

   public static void renderAllPartsWithPrebuild(EntityJointed entityJointed, float partial, PoseStack pose,
                                                 MultiBufferSource bufferSource, int packedLight) {
      ModelViewMatrix matrix = ModelViewMatrix.tHreadMvMatrix.get();
      JointParts[] referenceList = entityJointed.refLists;

      try {
         matrix.pushMatrix();
         Prebuilder prebuilder = (Prebuilder)entityJointed.prebuilder;
         short groupNums = 1;
         int len = referenceList.length;

         JointPartsRenderable targetParts;
         int i;
         for (i = 0; i < len; ++i) {
            targetParts = (JointPartsRenderable)referenceList[i];
            if (i == 0 || targetParts.isMotionType) {
               targetParts.group = groupNums++;
            }

            for (int c = 0; c < 6; ++c) {
               if (targetParts.child[c] != null) {
                  ((JointPartsRenderable)targetParts.child[c]).group = targetParts.group;
               }
            }

            matrix.popBeforeIndexAndPushMatrix(targetParts.matrixIndex = targetParts.parent.matrixIndex);
            renderPartsWithOwnMatrix(targetParts, entityJointed, partial, matrix, pose, bufferSource, packedLight);
            if (targetParts.hasChild) {
               targetParts.matrixIndex = matrix.pushMatrix();
            }

            prebuilder.blockedPartsMap.put(targetParts.xCoord, targetParts.yCoord, targetParts.zCoord, targetParts);
         }

         len = referenceList.length;

         for (i = 0; i < len; ++i) {
            targetParts = (JointPartsRenderable)referenceList[i];
            prebuilder.processBlockPolys(targetParts);
         }

         prebuilder.collectPallete(entityJointed);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void renderAllPartsWithOwnMatrix(EntityJointed entityJointed, float partial, PoseStack pose,
                                                  MultiBufferSource bufferSource, int packedLight) {
      ModelViewMatrix matrix = ModelViewMatrix.tHreadMvMatrix.get();
      JointParts[] referenceList = entityJointed.refLists;
      matrix.pushMatrix();
      int len = referenceList.length;

      for (int i = 0; i < len; ++i) {
         JointPartsRenderable targetParts = (JointPartsRenderable)referenceList[i];
         matrix.popBeforeIndexAndPushMatrix(targetParts.matrixIndex = targetParts.parent.matrixIndex);
         renderPartsWithOwnMatrix(targetParts, entityJointed, partial, matrix, pose, bufferSource, packedLight);
         if (targetParts.hasChild) {
            targetParts.matrixIndex = matrix.pushMatrix();
         }
      }
   }

   private static void renderPartsWithOwnMatrix(JointPartsRenderable target, EntityJointed entityJointed, float partial,
                                                ModelViewMatrix matrix, PoseStack pose, MultiBufferSource bufferSource,
                                                int packedLight) {
      float preOffsetX = target.renderOffsetX;
      float preOffsetY = target.renderOffsetY;
      float preOffsetZ = target.renderOffsetZ;
      if (!target.isMotionType) {
         matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
         target.renderThisPartWithOwnMatrix(entityJointed, partial, matrix, pose, bufferSource, packedLight);
      } else if (target.jointType == 15) {
         matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
         target.renderThisPartWithOwnMatrix(entityJointed, partial, matrix, pose, bufferSource, packedLight);
         short slotNum = target.initValues[0][1];
         if (entityJointed.getMasterSlotEnableStateFlag(slotNum)) {
            MasterSlot masterSlot = MasterSlot.getFromEntity(entityJointed, slotNum);
            if (masterSlot != null) {
               matrix.tempPushMatrix();
               int sideOppositeIdx = EnumDirectionOld.OPPOSITES[target.connectSide];
               float yawRot;
               if (target.connectSide == 0) {
                  yawRot = JBRotationUtil.entityYawEachSide[target.facing];
               } else if (target.connectSide == 1) {
                  yawRot = JBRotationUtil.entityYawEachSideNagateZ[target.facing];
               } else {
                  yawRot = JBRotationUtil.entityYawEachSide[sideOppositeIdx];
               }

               matrix.rotateYY(yawRot * 0.017453292F);
               matrix.setRotationToMasterAttatcher(masterSlot);
               matrix.setBaseRotationToMasterAttatcher(masterSlot);
               matrix.tempPopMatrix();
            }
         }
      } else if (target.jointType == 10) {
         // 1.12.2 keeps two triples here: the part's scale (1.0F defaults, replaced on the
         // extension axis) and the child-frame push (0.0F defaults, written by the renderOffset
         // fixups). The decompiled source merged both into extendRateX/Y/Z, so the push used the
         // scale values and moved the child frame a full block on both axes normal to the
         // extension whenever the child sits straight on the extension axis.
         float pushX = 0.0F;
         float pushY = 0.0F;
         float pushZ = 0.0F;
         float extendRateX = 1.0F;
         float extendRateY = 1.0F;
         float extendRateZ = 1.0F;
         int extendAxis = JBRotationUtil.getExtenderAxis(target.connectSide);
         float extendRateCommon = Mth.abs((float)target.extendRate / 100.0F);
         float extendRateCommonPrev = Mth.abs((float)target.prevExtendRate / 100.0F);
         float extendRatePartial = extendRateCommonPrev + (extendRateCommon - extendRateCommonPrev) * partial;
         if (extendAxis == 0) {
            extendRateX = extendRatePartial;
         }

         if (extendAxis == 1) {
            extendRateY = extendRatePartial;
         }

         if (extendAxis == 2) {
            extendRateZ = extendRatePartial;
         }

         float fix;
         if (target.renderOffsetX < 0.0F) {
            fix = (1.0F - extendRateX) / 2.0F;
            preOffsetX = target.renderOffsetX + fix;
            pushX = fix;
         } else if (target.renderOffsetX > 0.0F) {
            fix = (1.0F - extendRateX) / 2.0F;
            preOffsetX = target.renderOffsetX - fix;
            pushX = -fix;
         }

         float renderOffsetYShift = 0.5F * extendRateY - 0.5F + (fix = (1.0F * extendRateY - 1.0F) / 2.0F);
         preOffsetY += renderOffsetYShift;
         if (target.renderOffsetY < 0.0F) {
            preOffsetY = target.renderOffsetY + renderOffsetYShift - fix;
            pushY = -renderOffsetYShift - fix;
         } else if (target.renderOffsetY > 0.0F) {
            preOffsetY = target.renderOffsetY + renderOffsetYShift + fix;
            pushY = -renderOffsetYShift + fix;
         }

         if (target.renderOffsetZ < 0.0F) {
            fix = (1.0F - extendRateZ) / 2.0F;
            preOffsetZ = target.renderOffsetZ + fix;
            pushZ = fix;
         } else if (target.renderOffsetZ > 0.0F) {
            fix = (1.0F - extendRateZ) / 2.0F;
            preOffsetZ = target.renderOffsetZ - fix;
            pushZ = -fix;
         }

         if (StaticClientTuningStates.targetSelected.isSetectionEnabled
                 && StaticClientTuningStates.targetSelected.rayTracedHitJoint == target && matrix.renderPass == 0) {
            target.renderExtenderScalerTuningStates(matrix, extendRatePartial, preOffsetX, preOffsetY, preOffsetZ,
                    pose, bufferSource, packedLight);
         }

         matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
         if (extendRatePartial > 0.001F) {
            matrix.tempPushMatrix();
            matrix.scale(extendRateX, extendRateY, extendRateZ);
            target.renderThisPartWithOwnMatrix(entityJointed, partial, matrix, pose, bufferSource, packedLight);
            matrix.tempPopMatrix();
         }

         matrix.translate(pushX, -pushY, -pushZ);
      } else if (target.jointType == 12) {
         float extendRateX = 1.0F;
         float extendRateY = 1.0F;
         float extendRateZ = 1.0F;
         int extendAxis = target.getInitValue(1);
         extendRateY = Mth.abs((float)target.extendRate / 100.0F);
         extendRateZ = Mth.abs((float)target.prevExtendRate / 100.0F);
         float extendRatePartial = extendRateZ + (extendRateY - extendRateZ) * partial;
         if (extendRatePartial < 0.001F) {
            extendRatePartial = 0.001F;
         }

         if (extendAxis == 0) {
            extendRateZ = extendRatePartial;
            extendRateY = extendRatePartial;
            extendRateX = extendRatePartial;
         }

         if (extendAxis == 1) {
            extendRateX = extendRatePartial;
         }

         if (extendAxis == 2) {
            extendRateY = extendRatePartial;
         }

         if (extendAxis == 3) {
            extendRateZ = extendRatePartial;
         }

         extendRateX *= 1.0F / matrix.getScaleX();
         extendRateY *= 1.0F / matrix.getScaleY();
         extendRateZ *= 1.0F / matrix.getScaleZ();
         float fix;
         if (target.renderOffsetX < 0.0F) {
            fix = (1.0F - extendRateX) / 2.0F;
            preOffsetX = target.renderOffsetX + fix;
         } else if (target.renderOffsetX > 0.0F) {
            fix = (1.0F - extendRateX) / 2.0F;
            preOffsetX = target.renderOffsetX - fix;
         }

         float renderOffsetYShift = 0.5F * extendRateY - 0.5F + (fix = (1.0F * extendRateY - 1.0F) / 2.0F);
         preOffsetY += renderOffsetYShift;
         if (target.renderOffsetY < 0.0F) {
            preOffsetY = target.renderOffsetY + renderOffsetYShift - fix;
         } else if (target.renderOffsetY > 0.0F) {
            preOffsetY = target.renderOffsetY + renderOffsetYShift + fix;
         }

         if (target.renderOffsetZ < 0.0F) {
            fix = (1.0F - extendRateZ) / 2.0F;
            preOffsetZ = target.renderOffsetZ + fix;
         } else if (target.renderOffsetZ > 0.0F) {
            fix = (1.0F - extendRateZ) / 2.0F;
            preOffsetZ = target.renderOffsetZ - fix;
         }

         matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
         matrix.scale(extendRateX, extendRateY, extendRateZ);
         if (StaticClientTuningStates.targetSelected.isSetectionEnabled
                 && StaticClientTuningStates.targetSelected.rayTracedHitJoint == target && matrix.renderPass == 0) {
            target.renderExtenderScalerTuningStates(matrix, extendRatePartial, preOffsetX, preOffsetY, preOffsetZ,
                    pose, bufferSource, packedLight);
         }

         target.renderThisPartWithOwnMatrix(entityJointed, partial, matrix, pose, bufferSource, packedLight);
      } else if (target.jointType == 14) {
         float slideX = ((float)target.prevRotateAngleX
                 + (float)(target.rotateAngleX - target.prevRotateAngleX) * partial) / 64.0F;
         float slideY = ((float)target.prevRotateAngleY
                 + (float)(target.rotateAngleY - target.prevRotateAngleY) * partial) / 64.0F;
         float slideZ = ((float)target.prevRotateAngleZ
                 + (float)(target.rotateAngleZ - target.prevRotateAngleZ) * partial) / 64.0F;
         matrix.translate(preOffsetX + slideX, -preOffsetY - slideY, -preOffsetZ - slideZ);
         if (StaticClientTuningStates.targetSelected.isSetectionEnabled
                 && StaticClientTuningStates.targetSelected.rayTracedHitJoint == target && matrix.renderPass == 0) {
            target.renderSliderTuningStates(matrix, pose, bufferSource, packedLight);
         }

         target.renderThisPartWithOwnMatrix(entityJointed, partial, matrix, pose, bufferSource, packedLight);
      } else {
         float rotX = (float)target.prevRotateAngleX
                 + (float)(target.rotateAngleX - target.prevRotateAngleX) * partial;
         float rotY = (float)target.prevRotateAngleY
                 + (float)(target.rotateAngleY - target.prevRotateAngleY) * partial;
         float rotZ = (float)target.prevRotateAngleZ
                 + (float)(target.rotateAngleZ - target.prevRotateAngleZ) * partial;
         matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
         float shiftX = 0.0F;
         float shiftY = 0.0F;
         float shiftZ = 0.0F;
         if (target.rotatePoint != 15) {
            shiftX = (float)JBRotationUtil.side_matrix[target.rotatePoint][0] * 0.5F;
            shiftY = (float)(-JBRotationUtil.side_matrix[target.rotatePoint][1]) * 0.5F;
            shiftZ = (float)(-JBRotationUtil.side_matrix[target.rotatePoint][2]) * 0.5F;
         }

         if (StaticClientTuningStates.targetSelected.isSetectionEnabled
                 && StaticClientTuningStates.targetSelected.rayTracedHitJoint == target && matrix.renderPass == 0) {
            target.renderRotationTuningStates(matrix, rotX, rotY, rotZ, pose, bufferSource, packedLight);
         }

         matrix.translate(0.0F + shiftX, 1.0F + shiftY, 0.0F + shiftZ);
         if (target.doSpecialRotFlg && !entityJointed.isOnInitPhase() && !entityJointed.disassemble) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            double renderPosX = entity.xo + (entity.getX() - entity.xo) * (double)partial;
            double renderPosY = entity.yo + (entity.getY() - entity.yo) * (double)partial;
            double renderPosZ = entity.zo + (entity.getZ() - entity.zo) * (double)partial;
            if (calculateRotMatrixTwdTarget(entityJointed, target, matrix, true, renderPosX, renderPosY, renderPosZ, partial)) {
               if (matrix.rotTwdUpdateX) {
                  rotX = (float)matrix.cmnVec.x;
               }

               if (matrix.rotTwdUpdateY) {
                  rotY = (float)matrix.cmnVec.y;
               }

               rotZ = 0.0F;
               target.rotateAngleXNew = (short)Math.round(rotX);
               target.rotateAngleYNew = (short)Math.round(rotY);
               target.rotateAngleZNew = (short)Math.round(rotZ);
            }
         } else if (target.cancelParentRotate) {
            matrix.resetRotate();
         }

         if (rotZ != 0.0F) {
            matrix.rotateZZ(rotZ * 0.017453292F);
         }

         if (rotY != 0.0F) {
            matrix.rotateYY(rotY * 0.017453292F);
         }

         if (rotX != 0.0F) {
            matrix.rotateXX(rotX * 0.017453292F);
         }

         matrix.translate(-(0.0F + shiftX), -(1.0F + shiftY), -(0.0F + shiftZ));
         target.renderThisPartWithOwnMatrix(entityJointed, partial, matrix, pose, bufferSource, packedLight);
      }
   }

   public void renderThisPartWithOwnMatrix(EntityJointed entityJointed, float partial, ModelViewMatrix matrix,
                                           PoseStack pose, MultiBufferSource bufferSource, int packedLight) {
      if (!this.failedOnRender) {
         try {
            if (StaticClientTuningStates.isClientPlayerHoldingTuneItem
                    && StaticClientTuningStates.targetSelected.isSetectionEnabled
                    && (StaticClientTuningStates.targetSelected.rayTracedHitJoint == this)
                       == !entityJointed.reverseSelectedWireframe) {
               if (JBConfig.doShowJBConnection && this.jointType != 0) {
                  if (0 != matrix.renderPass) {
                     return;
                  }

                  this.renderAbstractConnection(entityJointed, partial, matrix, pose, bufferSource, packedLight);
                  return;
               }
            } else if (this.extraJointStateFlags != 0) {
               if (this.isExtraFlagsTransparentAlways()) {
                  return;
               }

               if (this.isExtraFlagsTransparentOnBoard()) {
                  if (entityJointed.isClientPlayerOnBoard) {
                     return;
                  }
               } else if (this.isExtraFlagsVisibleOnlyOnBoard()) {
                  if (!entityJointed.isClientPlayerOnBoard) {
                     return;
                  }
               }
               // 1.12.2 also flipped this part to GL wireframe when isExtraFlagsWireframeOnBoard()
               // held and the client player was on board; polygon mode has no 1.20.1 equivalent
               // (see the class javadoc), so the part is drawn normally.
            }

            if (this.carvingData != null) {
               if (0 != matrix.renderPass) {
                  return;
               }

               int[] paletteData = null;
               int paletteIndex = JBCarvingDataUtil.getPaletteIndex(this.carvingData);
               if (entityJointed.boFixedPaletteDataList != null) {
                  paletteData = entityJointed.boFixedPaletteDataList[paletteIndex];
               }

               if (paletteData == null) {
                  paletteData = TileEntityJointCore.BOFIXED_DEFAULT_PALETTE_DATA[paletteIndex];
               }

               pose.pushPose();
               JBPoseHelper.multiply(pose, matrix);
               pose.translate(0.0F, 1.0F, 0.0F);
               pose.scale(1.0F, -1.0F, -1.0F);
               BasicJointRenderer.addAdditonalFacingRotatoin(pose, this.facing, false);
               pose.translate(-0.0F, -1.0F, -0.0F);
               pose.translate(-0.5F, 0.5F, -0.5F);
               if (JBConfig.disableCarvingGLCompile) {
                  CarvingRenderer.renderJointCarvedModelByType(this, paletteData, pose, bufferSource, packedLight);
               } else {
                  BatchRenderHelper.renderer.compileOrRenderCarving(this, paletteData, pose, bufferSource, packedLight);
               }

               pose.popPose();
            } else if (this.modelItemId < 0) {
               if (this.blockStateToRender == null) {
                  Block blockForModel = BuiltInRegistries.BLOCK.byId(-this.modelItemId);
                  if (blockForModel == null) {
                     this.failedOnRender = true;
                     return;
                  }

                  this.blockStateToRender = blockForModel.defaultBlockState();
               }

               Block blockToRender = this.blockStateToRender.getBlock();
               if (!this.getShouldRender(blockToRender, matrix.renderPass)) {
                  return;
               }

               pose.pushPose();
               JBPoseHelper.multiply(pose, matrix);
               pose.translate(0.0F, 1.0F, 0.0F);
               pose.scale(1.0F, -1.0F, -1.0F);
               if (JBCommonUtil.doSideRotateOnRender(this.jointType)) {
                  BasicJointRenderer.addAdditonalSideRotatoin(pose, this.renderSide, true);
               }

               BasicJointRenderer.addAdditonalFacingRotatoin(pose, this.facing, false);
               pose.translate(-0.0F, -1.0F, -0.0F);
               pose.translate(-0.5F, 0.5F, -0.5F);
               boolean renderedTe = false;
               if (matrix.renderPass == 0 && blockToRender instanceof EntityBlock) {
                  if (!this.checkdTileEntity) {
                     this.checkdTileEntity = true;
                     BlockEntity te = ((EntityBlock)blockToRender).newBlockEntity(BlockPos.ZERO, this.blockStateToRender);
                     if (te != null && Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(te) != null) {
                        this.tileEntityToRender = te;
                     }
                  }

                  if (this.tileEntityToRender != null) {
                     Minecraft.getInstance().getBlockEntityRenderDispatcher()
                             .render(this.tileEntityToRender, partial, pose, bufferSource);
                     renderedTe = true;
                  }
               }

               if (!renderedTe) {
                  BasicJointRenderer.renderNormalBlock(this.blockStateToRender, pose, bufferSource, packedLight,
                          OverlayTexture.NO_OVERLAY);
               }

               pose.popPose();
            } else if (this.modelItemId > 0) {
               if (this.modelToRender == null) {
                  Item item = Item.byId(this.modelItemId);
                  if (!(item instanceof IJBModelItem)) {
                     this.failedOnRender = true;
                     return;
                  }

                  this.modelToRender = ((IJBModelItem)item).getIJBModel(this.modelMeta);
                  if (this.modelToRender == null) {
                     this.failedOnRender = true;
                     return;
                  }
               }

               if (!this.modelToRender.shouldRenderInPass(this.modelMeta, matrix.renderPass)) {
                  return;
               }

               pose.pushPose();
               JBPoseHelper.multiply(pose, matrix);
               pose.translate(0.0F, 1.0F, 0.0F);
               if (JBCommonUtil.doSideRotateOnRender(this.jointType)) {
                  BasicJointRenderer.addAdditonalSideRotatoin(pose, this.renderSide);
               }

               BasicJointRenderer.addAdditonalFacingRotatoin(pose, this.facing);
               this.modelToRender.setRenderState(pose, this, this.modelMeta, partial, entityJointed, matrix.renderPass);
               pose.translate(-0.0F, -1.0F, -0.0F);
               BasicJointRenderer.renderIJBModel(this.modelToRender, pose, bufferSource, packedLight,
                       OverlayTexture.NO_OVERLAY, this.modelMeta, matrix.renderPass);
               pose.popPose();
            } else if (matrix.renderPass == 0) {
               pose.pushPose();
               JBPoseHelper.multiply(pose, matrix);
               pose.translate(0.0F, 1.0F, 0.0F);
               if (JBCommonUtil.doSideRotateOnRender(this.jointType)) {
                  BasicJointRenderer.addAdditonalSideRotatoin(pose, this.connectSide);
               }

               BasicJointRenderer.addAdditonalFacingRotatoin(pose, this.facing);
               BasicJointRenderer.setAdditonalRenderState(entityJointed, this, partial);
               pose.translate(-0.0F, -1.0F, -0.0F);
               BasicJointRenderer.renderBasicModel(pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY,
                       this.jointType, false);
               pose.popPose();
            }

            // 1.12.2 drew the attracter beam in the translucent pass (renderPass == 1). There is a
            // single pass in 1.20.1, so it is drawn here in the same pass.
            if (this.jointType == 15 && this.extendRate > 0 && !this.isExtraFlagsTypeSpecCommonA()) {
               pose.pushPose();
               JBPoseHelper.multiply(pose, matrix);
               pose.translate(0.0F, 1.0F, 0.0F);
               if (JBCommonUtil.doSideRotateOnRender(this.jointType)) {
                  BasicJointRenderer.addAdditonalSideRotatoin(pose, this.connectSide);
               }

               BasicJointRenderer.addAdditonalFacingRotatoin(pose, this.facing);
               pose.translate(-0.0F, -1.0F, -0.0F);
               BasicJointRenderer.renderBeam(pose, bufferSource, this.getAttracterSlaveOffsetDiv());
               pose.popPose();
            }
         } catch (Exception e) {
            JointBlockRemastered.LOGGER.debug("[JointBlock]Failed at JointParts render: {}", e.toString());
            JointBlockRemastered.LOGGER.debug("[JointBlock]Failed at JointParts render", e);
            this.failedOnRender = true;
            // 1.12.2 also flushed an in-flight Tessellator batch here; 1.20.1 has no immediate-mode
            // batch to close, the buffer source owns the batches.
         }
      } else {
         pose.pushPose();
         JBPoseHelper.multiply(pose, matrix);
         BasicJointRenderer.renderErrorModel(pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
         pose.popPose();
      }

      if (StaticClientTuningStates.isClientPlayerHoldingTuneItem) {
         if (entityJointed.prebuilder instanceof Prebuilder) {
            this.buildupPrebuildData(entityJointed, matrix, (Prebuilder)entityJointed.prebuilder);
         }

         this.renderAdditionalTuningStates(matrix, pose, bufferSource);
      }
   }

   /**
    * 1.12.2 split the block model between the solid and the translucent pass by looking at
    * {@code BlockRenderLayer}. 1.20.1 renders everything in one pass, so every layer is drawn here.
    */
   private boolean getShouldRender(Block block, int renderPass) {
      return true;
   }

   private void renderAbstractConnection(EntityJointed entityJointed, float partial, ModelViewMatrix matrix,
                                         PoseStack pose, MultiBufferSource bufferSource, int packedLight) {
      pose.pushPose();
      JBPoseHelper.multiply(pose, matrix);
      pose.translate(0.0F, 1.0F, 0.0F);
      if (JBCommonUtil.doSideRotateOnRender(this.jointType)) {
         BasicJointRenderer.addAdditonalSideRotatoin(pose, this.connectSide);
      }

      pose.translate(-0.0F, -1.0F, -0.0F);
      BasicJointRenderer.renderAbstractConnection(pose, bufferSource, false);
      pose.popPose();
   }

   public void buildupPrebuildData(EntityJointed entityJointed, ModelViewMatrix matrix, Prebuilder prebuilder) {
      try {
         if (this.carvingData != null) {
            prebuilder.palletIdxTemp = (byte)JBCarvingDataUtil.getPaletteIndex(this.carvingData);
            prebuilder.mulMatrixFrom(matrix);
            prebuilder.translate(0.0F, 1.0F, 0.0F);
            prebuilder.scale(1.0F, -1.0F, -1.0F);
            prebuilder.addAdditonalFacingRotatoinOwn(this.facing, false);
            prebuilder.translate(-0.0F, -1.0F, -0.0F);
            prebuilder.translate(-0.5F, 0.5F, -0.5F);
            prebuilder.prebuildJointCarvedModelByType(this);
         }
      } catch (Exception e) {
         e.printStackTrace();
         JointBlockRemastered.LOGGER.debug("[JointBlock]Failed at JointParts prebuild: {}", e.toString());
         JointBlockRemastered.LOGGER.debug("[JointBlock]Failed at JointParts prebuild", e);
      }
   }

   private void renderExtenderScalerTuningStates(ModelViewMatrix matrix, float scaleRate, float preOffsetX,
                                                 float preOffsetY, float preOffsetZ, PoseStack pose,
                                                 MultiBufferSource bufferSource, int packedLight) {
      TuningBarInf[] tuningBarInfs = TuningBarInf.getTuningBarInfsByJointType(this.jointType);
      if (tuningBarInfs != null) {
         pose.pushPose();
         JBPoseHelper.multiply(pose, matrix);

         for (int i = 0; i < tuningBarInfs.length; ++i) {
            TuningBarInf inf = tuningBarInfs[i];
            pose.pushPose();
            if (this.jointType == 10) {
               if (i == 0) {
                  pose.translate(this.renderOffsetX * scaleRate, -this.renderOffsetY * scaleRate,
                          -this.renderOffsetZ * scaleRate);
               } else {
                  pose.translate(this.renderOffsetX, -this.renderOffsetY, -this.renderOffsetZ);
               }
            }

            pose.translate(0.0F, 1.0F, 0.0F);
            BasicJointRenderer.addAdditonalSideRotatoin(pose, this.connectSide);
            if (this.jointType == 12) {
               if (i == 0) {
                  pose.translate(-0.0F / 2.0F, -1.0F / 2.0F, -0.0F / 2.0F);
                  pose.scale(1.0F / matrix.getScaleX(), 1.0F / matrix.getScaleY(), 1.0F / matrix.getScaleZ());
                  pose.translate(-0.0F / 2.0F, -1.0F / 2.0F, -0.0F / 2.0F);
               } else {
                  pose.translate(0.0F, -1.5F / matrix.getScaleY(), 0.0F);
                  pose.translate(-0.0F / 2.0F, -1.0F / 2.0F, -0.0F / 2.0F);
                  pose.scale(1.0F / matrix.getScaleX(), 1.0F / matrix.getScaleY(), 1.0F / matrix.getScaleZ());
                  pose.translate(0.0F, matrix.getScaleY(), 0.0F);
               }
            } else {
               pose.translate(-0.0F, -1.0F, -0.0F);
            }

            this.renderTuningBarWithOffset(inf, pose, bufferSource);
            if (i == 0) {
               ModelTuningBladeExtend.instance.renderAll(0, pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            if (i == 1) {
               ModelTuningBladeShrink.instance.renderAll(0, pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            pose.popPose();
         }

         pose.popPose();
      }
   }

   @Override
   protected void tracePlayerLookRayScalerExtenderScaler(EntityJointed entityJointed, ModelViewMatrix matrix,
                                                         float scaleRate, float preOffsetX, float preOffsetY,
                                                         float preOffsetZ) {
      if (StaticClientTuningStates.targetSelected.isSetectionEnabled
              && StaticClientTuningStates.targetSelected.rayTracedHitJoint == this) {
         TuningBarInf[] tuningBarInfs = TuningBarInf.getTuningBarInfsByJointType(this.jointType);
         if (tuningBarInfs != null) {
            for (int i = 0; i < tuningBarInfs.length; ++i) {
               TuningBarInf inf = tuningBarInfs[i];
               matrix.tempPushMatrix();
               if (this.jointType == 10) {
                  if (i == 0) {
                     matrix.translate(this.renderOffsetX * scaleRate, -this.renderOffsetY * scaleRate,
                             -this.renderOffsetZ * scaleRate);
                  } else {
                     matrix.translate(this.renderOffsetX, -this.renderOffsetY, -this.renderOffsetZ);
                  }
               }

               matrix.translate(0.0F, 1.0F, 0.0F);
               BasicJointRenderer.addAdditonalSideRotatoinOwnMatrix(this.connectSide, matrix);
               if (this.jointType == 12) {
                  if (i == 0) {
                     matrix.translate(-0.0F / 2.0F, -1.0F / 2.0F, -0.0F / 2.0F);
                     matrix.scale(1.0F / matrix.getScaleX(), 1.0F / matrix.getScaleY(), 1.0F / matrix.getScaleZ());
                     matrix.translate(-0.0F / 2.0F, -1.0F / 2.0F, -0.0F / 2.0F);
                  } else {
                     float scy = matrix.getScaleY();
                     matrix.translate(0.0F, -1.5F / scy, 0.0F);
                     matrix.translate(-0.0F / 2.0F, -1.0F / 2.0F, -0.0F / 2.0F);
                     matrix.scale(1.0F / matrix.getScaleX(), 1.0F / scy, 1.0F / matrix.getScaleZ());
                     matrix.translate(0.0F, scy, 0.0F);
                  }
               } else {
                  matrix.translate(-0.0F, -1.0F, -0.0F);
               }

               this.checkRayHit(entityJointed, matrix, inf);
               matrix.tempPopMatrix();
            }
         }
      }
   }

   private void renderRotationTuningStates(ModelViewMatrix matrix, float rotAngleX, float rotAngleY, float rotAngleZ,
                                           PoseStack pose, MultiBufferSource bufferSource, int packedLight) {
      TuningBarInf[] tuningBarInfs = TuningBarInf.getTuningBarInfsByJointType(this.jointType);
      if (tuningBarInfs != null) {
         pose.pushPose();
         JBPoseHelper.multiply(pose, matrix);
         pose.translate(0.0F, 1.0F, 0.0F);

         for (int i = 0; i < tuningBarInfs.length; ++i) {
            TuningBarInf inf = tuningBarInfs[i];
            pose.pushPose();
            if (i == 0 || i == 1) {
               if (rotAngleZ != 0.0F) {
                  pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotAngleZ));
               }

               if (rotAngleY != 0.0F) {
                  pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotAngleY));
               }

               if (rotAngleX != 0.0F) {
                  pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotAngleX));
               }
            }

            if (i == 2 || i == 3) {
               if (rotAngleZ != 0.0F) {
                  pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotAngleZ));
               }

               if (rotAngleY != 0.0F) {
                  pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotAngleY));
               }
            }

            if ((i == 4 || i == 5) && rotAngleZ != 0.0F) {
               pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotAngleZ));
            }

            pose.translate(-0.0F, -1.0F, -0.0F);
            this.renderTuningBarWithOffset(inf, pose, bufferSource);
            if (i == 1) {
               ModelTuningBladeRotateX.instance.renderAll(0, pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            if (i == 3) {
               ModelTuningBladeRotateY.instance.renderAll(0, pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            if (i == 5) {
               ModelTuningBladeRotateZ.instance.renderAll(0, pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            pose.popPose();
         }

         pose.popPose();
      }
   }

   @Override
   protected void tracePlayerLookRayRotator(EntityJointed entityJointed, ModelViewMatrix matrix, float rotAngleX,
                                            float rotAngleY, float rotAngleZ) {
      if (StaticClientTuningStates.targetSelected.isSetectionEnabled
              && StaticClientTuningStates.targetSelected.rayTracedHitJoint == this) {
         TuningBarInf[] tuningBarInfs = TuningBarInf.getTuningBarInfsByJointType(this.jointType);
         if (tuningBarInfs != null) {
            for (int i = 0; i < tuningBarInfs.length; ++i) {
               TuningBarInf inf = tuningBarInfs[i];
               matrix.tempPushMatrix();
               matrix.translate(0.0F, 1.0F, 0.0F);
               if (i == 0 || i == 1) {
                  if (rotAngleZ != 0.0F) {
                     matrix.rotateZZ(rotAngleZ * 0.017453292F);
                  }

                  if (rotAngleY != 0.0F) {
                     matrix.rotateYY(rotAngleY * 0.017453292F);
                  }

                  if (rotAngleX != 0.0F) {
                     matrix.rotateXX(rotAngleX * 0.017453292F);
                  }
               }

               if (i == 2 || i == 3) {
                  if (rotAngleZ != 0.0F) {
                     matrix.rotateZZ(rotAngleZ * 0.017453292F);
                  }

                  if (rotAngleY != 0.0F) {
                     matrix.rotateYY(rotAngleY * 0.017453292F);
                  }
               }

               if ((i == 4 || i == 5) && rotAngleZ != 0.0F) {
                  matrix.rotateZZ(rotAngleZ * 0.017453292F);
               }

               matrix.translate(-0.0F, -1.0F, -0.0F);
               this.checkRayHit(entityJointed, matrix, inf);
               matrix.tempPopMatrix();
            }
         }
      }
   }

   private void renderSliderTuningStates(ModelViewMatrix matrix, PoseStack pose, MultiBufferSource bufferSource,
                                         int packedLight) {
      TuningBarInf[] tuningBarInfs = TuningBarInf.getTuningBarInfsByJointType(this.jointType);
      if (tuningBarInfs != null) {
         pose.pushPose();
         JBPoseHelper.multiply(pose, matrix);

         for (int i = 0; i < tuningBarInfs.length; ++i) {
            TuningBarInf inf = tuningBarInfs[i];
            pose.pushPose();
            this.renderTuningBarWithOffset(inf, pose, bufferSource);
            if (i == 1) {
               ModelTuningBladeSlideX.instance.renderAll(0, pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            if (i == 3) {
               ModelTuningBladeSlideY.instance.renderAll(0, pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            if (i == 5) {
               ModelTuningBladeSlideZ.instance.renderAll(0, pose, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            pose.popPose();
         }

         pose.popPose();
      }
   }

   @Override
   protected void tracePlayerLookRaySlider(EntityJointed entityJointed, ModelViewMatrix matrix) {
      if (StaticClientTuningStates.targetSelected.isSetectionEnabled
              && StaticClientTuningStates.targetSelected.rayTracedHitJoint == this) {
         TuningBarInf[] tuningBarInfs = TuningBarInf.getTuningBarInfsByJointType(this.jointType);
         if (tuningBarInfs != null) {
            for (int i = 0; i < tuningBarInfs.length; ++i) {
               TuningBarInf inf = tuningBarInfs[i];
               matrix.tempPushMatrix();
               this.checkRayHit(entityJointed, matrix, inf);
               matrix.tempPopMatrix();
            }
         }
      }
   }

   private void renderAdditionalTuningStates(ModelViewMatrix matrix, PoseStack pose, MultiBufferSource bufferSource) {
      if (StaticClientTuningStates.target.rayTracedHitJoint == this
              || StaticClientTuningStates.targetSelected.isSetectionEnabled
                 && StaticClientTuningStates.targetSelected.rayTracedHitJoint == this) {
         pose.pushPose();
         JBPoseHelper.multiply(pose, matrix);
         VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
         // 1.12.2 RenderGlobal.drawBoundingBox(bb, 0.0F, 0.0F, 0.0F, 255.0F): the alpha literal is
         // on the 0..255 scale there and clamps to opaque on the way to the vertex byte.
         LevelRenderer.renderLineBox(pose, consumer, matrix.selectBoxBB, 0.0F, 0.0F, 0.0F, 255.0F);
         pose.popPose();
      }
   }

   @Override
   protected void tracePlayerLookRayCommon(EntityJointed entityJointed, ModelViewMatrix matrix) {
      if (StaticClientTuningStates.isClientPlayerHoldingTuneItem && !StaticClientTuningStates.targetSelected.isSetectionEnabled) {
         MatrixBase invMatrix = matrix.createInvert(matrix.invMatrix);
         if (invMatrix != null) {
            matrix.rayBecStCnv.x = matrix.rayBecSt.x;
            matrix.rayBecStCnv.y = matrix.rayBecSt.y;
            matrix.rayBecStCnv.z = matrix.rayBecSt.z;
            matrix.rayBecEdCnv.x = matrix.rayBecEd.x;
            matrix.rayBecEdCnv.y = matrix.rayBecEd.y;
            matrix.rayBecEdCnv.z = matrix.rayBecEd.z;
            invMatrix.transform(matrix.rayBecStCnv);
            invMatrix.transform(matrix.rayBecEdCnv);
            if (JBCommonUtil.calculateFixedBoxIntercept(-0.5, 0.5, 0.5, 1.5, -0.5, 0.5,
                    matrix.rayBecStCnv, matrix.rayBecEdCnv, matrix)) {
               matrix.transform(ModelViewMatrix.rayHitVecWork);
               double distSq = matrix.rayBecSt.distanceToSqr(ModelViewMatrix.rayHitVecWork);
               if (distSq < StaticClientTuningStates.targetPri.rayTracedHitDistSq) {
                  StaticClientTuningStates.targetPri.rayTracedHitDistSq = distSq;
                  StaticClientTuningStates.targetPri.isAdditionalBarHit = false;
                  StaticClientTuningStates.targetPri.rayTracedHitJoint = this;
                  StaticClientTuningStates.targetPri.rayTracedHitEntity = entityJointed;
                  StaticClientTuningStates.targetPri.rayTracedHitSide = matrix.rayHitSide;
                  StaticClientTuningStates.PlayerTuningTarget.rayTracedHitVecAbs.x = ModelViewMatrix.rayHitVecWork.x + matrix.baseEntityPosX;
                  StaticClientTuningStates.PlayerTuningTarget.rayTracedHitVecAbs.y = ModelViewMatrix.rayHitVecWork.y + matrix.baseEntityPosY;
                  StaticClientTuningStates.PlayerTuningTarget.rayTracedHitVecAbs.z = ModelViewMatrix.rayHitVecWork.z + matrix.baseEntityPosZ;
               }
            }
         }
      }
   }

   /**
    * One tuning bar segment. 1.12.2 bound the white 4x4 texture and pushed a {@code POSITION_TEX_NORMAL}
    * quad batch; the white texel makes that identical to an unlit {@code POSITION_COLOR} quad, so this
    * emits through {@link JBRenderTypes#solidColor()} and drops the UV/normal (the shader has neither).
    */
   public static void renderSimpleOffsetWithTexture(PoseStack pose, MultiBufferSource bufferSource,
                                                    double minX, double maxX, double minY, double maxY,
                                                    double minZ, double maxZ, boolean hit) {
      VertexConsumer consumer = bufferSource.getBuffer(JBRenderTypes.solidColor());
      Matrix4f mat = pose.last().pose();
      float r;
      float g;
      float b;
      if (hit) {
         r = 0.2F;
         g = 1.0F;
         b = 0.5F;
      } else {
         r = 1.0F;
         g = 1.0F;
         b = 1.0F;
      }

      float a = 1.0F;
      emitQuad(consumer, mat, r, g, b, a, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ);
      emitQuad(consumer, mat, r, g, b, a, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ);
      emitQuad(consumer, mat, r, g, b, a, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ);
      emitQuad(consumer, mat, r, g, b, a, minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ);
      emitQuad(consumer, mat, r, g, b, a, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ);
      emitQuad(consumer, mat, r, g, b, a, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ);
   }

   private static void emitQuad(VertexConsumer consumer, Matrix4f mat, float r, float g, float b, float a,
                                double x0, double y0, double z0, double x1, double y1, double z1,
                                double x2, double y2, double z2, double x3, double y3, double z3) {
      consumer.vertex(mat, (float)x0, (float)y0, (float)z0).color(r, g, b, a).endVertex();
      consumer.vertex(mat, (float)x1, (float)y1, (float)z1).color(r, g, b, a).endVertex();
      consumer.vertex(mat, (float)x2, (float)y2, (float)z2).color(r, g, b, a).endVertex();
      consumer.vertex(mat, (float)x3, (float)y3, (float)z3).color(r, g, b, a).endVertex();
   }

   private void renderTuningBarWithOffset(TuningBarInf inf, PoseStack pose, MultiBufferSource bufferSource) {
      for (int j = 0; j < inf.repeatCount; ++j) {
         double offsetX = inf.offset[0] * (double)j;
         double offsetY = inf.offset[1] * (double)j;
         double offsetZ = inf.offset[2] * (double)j;
         boolean isHit = StaticClientTuningStates.target.isAdditionalBarHit
                 && StaticClientTuningStates.target.rayTracedHitJoint == this
                 && StaticClientTuningStates.target.additionalBarNum == inf.barSideNum
                 && StaticClientTuningStates.target.additionalBarOffset == j;
         renderSimpleOffsetWithTexture(pose, bufferSource,
                 inf.minX + offsetX + SHRINK_PADDING, inf.maxX + offsetX - SHRINK_PADDING,
                 inf.minY + offsetY + SHRINK_PADDING, inf.maxY + offsetY - SHRINK_PADDING,
                 inf.minZ + offsetZ + SHRINK_PADDING, inf.maxZ + offsetZ - SHRINK_PADDING, isHit);
      }
   }

   private void checkRayHit(EntityJointed entityJointed, ModelViewMatrix matrix, TuningBarInf inf) {
      MatrixBase invMatrix = matrix.createInvert(matrix.invMatrix);
      if (invMatrix != null) {
         matrix.rayBecStCnv.x = matrix.rayBecSt.x;
         matrix.rayBecStCnv.y = matrix.rayBecSt.y;
         matrix.rayBecStCnv.z = matrix.rayBecSt.z;
         matrix.rayBecEdCnv.x = matrix.rayBecEd.x;
         matrix.rayBecEdCnv.y = matrix.rayBecEd.y;
         matrix.rayBecEdCnv.z = matrix.rayBecEd.z;
         invMatrix.transform(matrix.rayBecStCnv);
         invMatrix.transform(matrix.rayBecEdCnv);

         for (int j = 0; j < inf.repeatCount; ++j) {
            double offsetX = inf.offset[0] * (double)j;
            double offsetY = inf.offset[1] * (double)j;
            double offsetZ = inf.offset[2] * (double)j;
            if (JBCommonUtil.calculateFixedBoxIntercept(inf.minX + offsetX, inf.maxX + offsetX,
                    inf.minY + offsetY, inf.maxY + offsetY, inf.minZ + offsetZ, inf.maxZ + offsetZ,
                    matrix.rayBecStCnv, matrix.rayBecEdCnv, matrix)) {
               matrix.transform(ModelViewMatrix.rayHitVecWork);
               double distSq = matrix.rayBecSt.distanceToSqr(ModelViewMatrix.rayHitVecWork);
               if (distSq < StaticClientTuningStates.targetPri.rayTracedHitDistSq) {
                  StaticClientTuningStates.targetPri.rayTracedHitDistSq = distSq;
                  StaticClientTuningStates.targetPri.isAdditionalBarHit = true;
                  StaticClientTuningStates.targetPri.rayTracedHitJoint = this;
                  StaticClientTuningStates.targetPri.rayTracedHitEntity = entityJointed;
                  StaticClientTuningStates.targetPri.rayTracedHitSide = matrix.rayHitSide;
                  StaticClientTuningStates.targetPri.additionalBarNum = inf.barSideNum;
                  StaticClientTuningStates.targetPri.additionalBarOffset = j;
                  StaticClientTuningStates.PlayerTuningTarget.rayTracedHitVecAbs.x = ModelViewMatrix.rayHitVecWork.x + matrix.baseEntityPosX;
                  StaticClientTuningStates.PlayerTuningTarget.rayTracedHitVecAbs.y = ModelViewMatrix.rayHitVecWork.y + matrix.baseEntityPosY;
                  StaticClientTuningStates.PlayerTuningTarget.rayTracedHitVecAbs.z = ModelViewMatrix.rayHitVecWork.z + matrix.baseEntityPosZ;
               }
            }
         }
      }
   }

   public void addOverlayIndicator() {
      switch (this.jointType) {
         case 0:
            OverlayRendeler.addIndicate("Type:", "Core");
         case 1:
         case 3:
         case 5:
         case 6:
         case 7:
         case 9:
         case 11:
         case 13:
         default:
            break;
         case 2:
            OverlayRendeler.addIndicate("Type:", "Seat");
            break;
         case 4:
            OverlayRendeler.addIndicate("Type:", "Normal");
            break;
         case 8:
            OverlayRendeler.addIndicate("Type:", "Rotator");
            OverlayRendeler.addIndicate("RotX:", String.valueOf(this.getInitValue(0)));
            OverlayRendeler.addIndicate("RotY:", String.valueOf(this.getInitValue(1)));
            OverlayRendeler.addIndicate("RotZ:", String.valueOf(this.getInitValue(2)));
            OverlayRendeler.addIndicate("Mode:", String.valueOf(this.initValsLayer));
            break;
         case 10:
            OverlayRendeler.addIndicate("Type:", "Extender");
            OverlayRendeler.addIndicate("Rate:", String.valueOf(this.getInitValue(0)));
            OverlayRendeler.addIndicate("Mode:", String.valueOf(this.initValsLayer));
            break;
         case 12:
            OverlayRendeler.addIndicate("Type:", "Scaler");
            OverlayRendeler.addIndicate("Rate:", String.valueOf(this.getInitValue(0)));
            OverlayRendeler.addIndicate("Mode:", String.valueOf(this.initValsLayer));
            break;
         case 14:
            OverlayRendeler.addIndicate("Type:", "Slider");
            OverlayRendeler.addIndicate("MovX:", String.valueOf(this.getInitValue(0)));
            OverlayRendeler.addIndicate("MovY:", String.valueOf(this.getInitValue(1)));
            OverlayRendeler.addIndicate("MovZ:", String.valueOf(this.getInitValue(2)));
            OverlayRendeler.addIndicate("Mode:", String.valueOf(this.initValsLayer));
            break;
         case 15:
            OverlayRendeler.addIndicate("Type:", "Attracter");
      }

      if (this.memoStrHolder != null) {
         OverlayRendeler.addIndicate("Memo:", this.memoStrHolder.memoStr);
      }

      if (JBConfig.enableDebugIndicates) {
         OverlayRendeler.addIndicate("Lyr:", String.valueOf(this.initValues.length - 1));
         OverlayRendeler.addIndicate("Act:", String.valueOf(this.hasAction));
         OverlayRendeler.addIndicate("Mot:", String.valueOf(this.motionTelePartsId != 255));
      }
   }

   public JBRAdditional getAdditional() {
      if (this.additional == null) {
         this.additional = new JBRAdditional();
      }

      return this.additional;
   }

   public EffectHelper getEffectHelper() {
      JBRAdditional add = this.getAdditional();
      if (add.effectHelper == null) {
         add.effectHelper = new EffectHelper();
      }

      return add.effectHelper;
   }

   @Override
   public void updateEffectPosition(Vec3 soundPos) {
      EffectHelper effectHelper = this.getEffectHelper();
      effectHelper.posX = soundPos.x;
      effectHelper.posY = soundPos.y;
      effectHelper.posZ = soundPos.z;
   }

   @Override
   public void updateEffectVelocity(Vec3 soundPos) {
      EffectHelper effectHelper = this.getEffectHelper();
      effectHelper.velX = soundPos.x;
      effectHelper.velY = soundPos.y;
      effectHelper.velZ = soundPos.z;
   }

   public static class JBRAdditional {
      public PbCarvingCell.PbBoxMatrix boxMatrix;
      public EffectHelper effectHelper;
   }
}
