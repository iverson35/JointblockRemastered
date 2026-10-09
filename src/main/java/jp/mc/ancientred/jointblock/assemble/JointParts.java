package jp.mc.ancientred.jointblock.assemble;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.api.IJBJointParameterHolder;
import jp.mc.ancientred.jointblock.api.IJBModelItem;
import jp.mc.ancientred.jointblock.api.IJBMotionBinderItem;
import jp.mc.ancientred.jointblock.api.IJBVectorTransformer;
import jp.mc.ancientred.jointblock.block.BlockJointBase;
import jp.mc.ancientred.jointblock.common.EnumDirectionOld;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.common.JBRotationUtil;
import jp.mc.ancientred.jointblock.common.WorldHelper;
import jp.mc.ancientred.jointblock.effect.EffectSlot;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import jp.mc.ancientred.jointblock.entity.masterslave.AbstractSlaveAttatcher;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlaveControl;
import jp.mc.ancientred.jointblock.entity.masterslave.MasterSlot;
import jp.mc.ancientred.jointblock.motion.BasicMotionBinder;
import jp.mc.ancientred.jointblock.motion.MotionHelper;
import jp.mc.ancientred.jointblock.registry.JBBlocks;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJointAttracter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class JointParts implements IJBJointParameterHolder.IJBJoint, IJBJointParameterHolder {
   protected static final float NEAR_ZERO = 0.001F;
   public static final float rotationPointX = 0.0F;
   public static final float rotationPointY = 1.0F;
   public static final float rotationPointZ = 0.0F;
   public static final int DISABLEMOT_NONE = 0;
   public static final int DISABLEMOT_MOT = 1;
   public static final int DISABLEMOT_ACT = 2;
   public static final int DISABLEMOT_BOTH = 3;
   public short id;
   public int motionTelePartsId;
   public boolean isMotionType;
   public int depth;
   public JointParts[] child = new JointParts[6];
   public boolean isDestructed;
   public boolean requestDestruct;
   public JointParts parent;
   public WeakReference<EntitySeatPart> seatWeakRef;
   public final byte xCoord;
   public final byte yCoord;
   public final byte zCoord;
   public byte jointType;
   public byte connectSide;
   public byte renderSide;
   public byte facing;
   public short modelItemId;
   public short motionItemId;
   public byte modelMeta;
   public byte motionMeta;
   public short bindNum;
   public int initValsLayer = -1;
   public short[][] initValues = new short[][]{new short[4]};
   public short[] params = new short[6];
   public int[] carvingData;
   public static final int EXTRA_FLAGS_TRANSPARENT_ALWAYS = Integer.MIN_VALUE;
   public static final int EXTRA_FLAGS_TRANSPARENT_ONBOARD = 1073741824;
   public static final int EXTRA_FLAGS_WIREFRAME_ONBOARD = 536870912;
   public static final int EXTRA_FLAGS_TYPE_SPEC_COMMON_A = 268435456;
   public static final int EXTRA_FLAGS_TYPE_SPEC_COMMON_B = 134217728;
   public static final int EXTRA_FLAGS_TYPE_SPEC_COMMON_C = 67108864;
   public static final int EXTRA_FLAGS_VISIBLEONLY_ONBOARD = 33554432;
   public int extraJointStateFlags;
   public short[] fixedInitVals = new short[4];
   public ExtraActionVarsHolder extraActionVarsHld;
   public byte guiPage;
   public static final byte NO_ROTATION_SHIFT = 15;
   public byte rotatePoint = 15;
   public boolean cancelParentRotate;
   public IJBMotionBinderItem.IJBMotionHandler motionHanlder;
   public boolean doMotion;
   public IJBModelItem.IJBActionHandler actionHanlder;
   public boolean hasAction;
   public boolean doAction;
   public LivingEntity actionConductor;
   public byte doEffect = -1;
   public boolean hasInit;
   public float renderOffsetX;
   public float renderOffsetY;
   public float renderOffsetZ;
   public short rotateAngleX;
   public short rotateAngleY;
   public short rotateAngleZ;
   public short prevRotateAngleX;
   public short prevRotateAngleY;
   public short prevRotateAngleZ;
   public short extendRate = 100;
   public short prevExtendRate = 100;
   public boolean paramModifyDirFlg;
   public boolean doSpecialRotFlg;
   public int extendRateNew;
   public int rotateAngleXNew;
   public int rotateAngleYNew;
   public int rotateAngleZNew;
   public boolean hasReceivedClient;
   protected boolean hasChild;
   protected int matrixIndex;
   protected ModelViewMatrix matrixPrivate;
   public JointParts linkPrev;
   public int parentId;
   public MemoStringHolder memoStrHolder;
   public MotionHelper motionHelper;

   public JointParts(int relX, int relY, int relZ) {
      this.extendRateNew = this.extendRate;
      this.parentId = -1;
      this.xCoord = (byte)relX;
      this.yCoord = (byte)relY;
      this.zCoord = (byte)relZ;
   }

   public int hashCode() {
      int result = 17;
      result = 37 * result + this.xCoord;
      result = 37 * result + this.yCoord;
      result = 37 * result + this.zCoord;
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         JointParts other = (JointParts)obj;
         return this.xCoord == other.xCoord && this.yCoord == other.yCoord && this.zCoord == other.zCoord;
      }
   }

   public void breakChane() {
      for(int i = 0; i < 6; ++i) {
         this.child[i] = null;
      }

   }

   public IJBJointParameterHolder.IJBJoint getParentJoint() {
      return this.parent;
   }

   public IJBJointParameterHolder.IJBJoint getChildJoint(int childNum0to5) {
      return this.child[childNum0to5];
   }

   public int getModelItemId() {
      return this.modelItemId;
   }

   public int getModelItemMeta() {
      return this.modelMeta;
   }

   public int getMotionItemId() {
      return this.motionItemId;
   }

   public int getMotionItemMeta() {
      return this.motionMeta;
   }

   public int getRotationX() {
      return this.rotateAngleX;
   }

   public int getRotationY() {
      return this.rotateAngleY;
   }

   public int getRotationZ() {
      return this.rotateAngleZ;
   }

   public int getExtendRate() {
      return this.extendRate;
   }

   public int getPrevRotationX() {
      return this.prevRotateAngleX;
   }

   public int getPrevRotationY() {
      return this.prevRotateAngleY;
   }

   public int getPrevRotationZ() {
      return this.prevRotateAngleZ;
   }

   public int getPrevExtendRate() {
      return this.prevExtendRate;
   }

   public int setNewRotationX(int value) {
      return this.rotateAngleXNew = value;
   }

   public int setNewRotationY(int value) {
      return this.rotateAngleYNew = value;
   }

   public int setNewRotationZ(int value) {
      return this.rotateAngleZNew = value;
   }

   public int setNewExtendRate(int value) {
      return this.extendRateNew = value;
   }

   public IJBVectorTransformer getTransformMatrix() {
      return this.matrixPrivate;
   }

   public boolean hasInitExtraActionVars() {
      return this.extraActionVarsHld != null;
   }

   public void initExtraActionVars(int valueNum, int tickModify, int min, int max) {
      if (this.extraActionVarsHld == null) {
         this.extraActionVarsHld = new ExtraActionVarsHolder();
         this.extraActionVarsHld.extraActionVars = new int[]{0, 0, 0, 0, 0, 0};
         this.extraActionVarsHld.extraActionVarsTickModify = new int[]{0, 0, 0, 0, 0, 0};
         this.extraActionVarsHld.extraActionVarsMin = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
         this.extraActionVarsHld.extraActionVarsMax = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
      }

      this.extraActionVarsHld.extraActionVarsTickModify[valueNum] = tickModify;
      this.extraActionVarsHld.extraActionVarsMin[valueNum] = min;
      this.extraActionVarsHld.extraActionVarsMax[valueNum] = max;
   }

   public int getExtraActionVars(int valueNum) {
      return this.extraActionVarsHld == null ? 0 : this.extraActionVarsHld.extraActionVars[valueNum];
   }

   public void setExtraActionVars(int valueNum, int value) {
      if (this.extraActionVarsHld != null) {
         this.extraActionVarsHld.extraActionVars[valueNum] = value;
      }

   }

   public void setRotatePoint(int side) {
      if (side < 0 || side >= 6) {
         side = -1;
      }

      this.rotatePoint = (byte)side;
   }

   public int getJointType() {
      return this.jointType;
   }

   public int getBindNum() {
      return this.bindNum;
   }

   public int getConnectSide() {
      return this.connectSide;
   }

   public int getRenderSide() {
      return this.connectSide;
   }

   public int getParam(int paramNum) {
      return this.params[paramNum];
   }

   public void setParam(int paramNum, int value) {
      this.params[paramNum] = (short)value;
   }

   public int getInitValue(int initValueNum) {
      return this.getCurrentInitVals()[initValueNum];
   }

   public short[] getCurrentInitVals() {
      return this.initValsLayer < 0 ? this.initValues[0] : this.initValues[this.initValsLayer];
   }

   private boolean setCurrentInitValsLayer(int initValueMode) {
      int old = this.initValsLayer;
      if (initValueMode >= 0 && initValueMode < this.initValues.length) {
         this.initValsLayer = initValueMode;
      } else {
         this.initValsLayer = 0;
      }

      if (old != this.initValsLayer) {
         this.hasInit = false;
         this.cancelParentRotate = false;
         this.setupFixedInitVals(this.getCurrentInitVals());
      }

      return isDisableMotValue(this.initValues[this.initValsLayer][3]);
   }

   public boolean updateAndForceInitValue(byte initLayer, byte initParamIndex, short newValue, int initLayerLimit) {
      if (this.initValues != null && initLayer >= 0) {
         if (initLayer >= this.initValues.length) {
            if (initLayer > initLayerLimit) {
               return false;
            }

            this.ensureInitValuesArySize(initLayer + 1);
         }

         short[] initVals = this.initValues[initLayer];
         if (initVals != null && initParamIndex >= 0 && initParamIndex < initVals.length) {
            initVals[initParamIndex] = newValue;
            this.setupFixedInitVals(initVals);
            if (this.jointType != 8 && this.jointType != 14) {
               if (this.jointType != 10 && this.jointType != 12) {
                  if (this.jointType == 15) {
                     this.extendRateNew = this.fixedInitVals[2];
                  }
               } else {
                  this.extendRateNew = this.fixedInitVals[0];
               }
            } else {
               this.rotateAngleXNew = this.fixedInitVals[0];
               this.rotateAngleYNew = this.fixedInitVals[1];
               this.rotateAngleZNew = this.fixedInitVals[2];
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void ensureInitValuesArySize(int initValuesLayerCount) {
      if (initValuesLayerCount > this.initValues.length) {
         short[][] newArr = new short[initValuesLayerCount][];

         for(int i = 0; i < newArr.length; ++i) {
            if (i < this.initValues.length && this.initValues[i] != null) {
               newArr[i] = Arrays.copyOf(this.initValues[i], 4);
            } else {
               newArr[i] = Arrays.copyOf(this.initValues[0], 4);
            }
         }

         this.initValues = newArr;
      }

   }

   public void setupFixedInitVals(short[] currentInitVals) {
      if (currentInitVals != null) {
         this.fixedInitVals[0] = currentInitVals[0];
         this.fixedInitVals[1] = currentInitVals[1];
         this.fixedInitVals[2] = currentInitVals[2];
         this.fixedInitVals[3] = currentInitVals[3];
         this.motionHanlder.onInitParts(this, this.fixedInitVals);
      }
   }

   public int[] getCarvinData() {
      return this.carvingData;
   }

   public static void updateAllParts(EntityJointed entityJointed, ModelViewMatrix matrix) {
      JointParts[] referenceList = entityJointed.refLists;
      matrix.pushMatrix();
      matrix.isRemote = entityJointed.level().isClientSide;
      int len = referenceList.length;

      for(int i = 0; i < len; ++i) {
         JointParts targetParts = referenceList[i];
         matrix.popBeforeIndexAndPushMatrix(targetParts.matrixIndex = targetParts.parent.matrixIndex);
         updateParts(targetParts, entityJointed, entityJointed.getInitLayerMode(), matrix);
         if (targetParts.hasChild) {
            targetParts.matrixIndex = matrix.pushMatrix();
         }
      }

   }

   private static void updateParts(JointParts target, EntityJointed entityJointed, byte initLayer, ModelViewMatrix matrix) {
      boolean isRemote = matrix.isRemote;
      target.matrixPrivate = matrix;
      ensureMotionHanlder(target);
      boolean disableMot = target.setCurrentInitValsLayer(initLayer);
      if (target.motionTelePartsId != 255) {
         if (entityJointed.disassemble) {
            target.handleDisassembleMotion(entityJointed);
            target.fixValuesInValidRange();
            if (!isRemote) {
               entityJointed.updateList.addAsFullSynchronizeUpdate(target);
            }
         } else if (entityJointed.isOnInitPhase()) {
            if (!target.hasReceivedClient && !entityJointed.isReInit) {
               target.handleInitMotion(entityJointed);
               target.fixValuesInValidRange();
               if (!entityJointed.level().isClientSide) {
                  entityJointed.updateList.addAsFullSynchronizeUpdate(target);
               }
            }
         } else if (!disableMot && !isRemote && target.motionHanlder.needsJointUpdate(entityJointed, target)) {
            target.motionHanlder.doUpdateJointState(entityJointed, target);
            target.fixValuesInValidRange();
            entityJointed.updateList.addAsFullSynchronizeUpdate(target);
         }
      } else if (target.isMotionType) {
         if (entityJointed.disassemble) {
            target.handleDisassembleMotion(entityJointed);
         } else if (entityJointed.isOnInitPhase()) {
            target.handleInitMotion(entityJointed);
         } else {
            target.setInittedMotionValues(entityJointed);
         }

         target.fixValuesInValidRange();
      }

      target.hasReceivedClient = false;
      target.updateExtraValues();
      float preOffsetX = target.renderOffsetX;
      float preOffsetY = target.renderOffsetY;
      float preOffsetZ = target.renderOffsetZ;
      if (!target.isMotionType) {
         matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
         target.onPositionThisPart(entityJointed, matrix);
      } else {
         target.savePrevValue();
         target.rotateAngleX = (short)target.rotateAngleXNew;
         target.rotateAngleY = (short)target.rotateAngleYNew;
         target.rotateAngleZ = (short)target.rotateAngleZNew;
         target.extendRate = (short)target.extendRateNew;
         target.fixPrevValues();
         if (target.jointType == 15) {
            matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
            target.onPositionThisPart(entityJointed, matrix);
            short slotNum = target.initValues[0][1];
            if (entityJointed.getMasterSlotEnableStateFlag(slotNum)) {
               MasterSlot masterSlot = MasterSlot.getFromEntity(entityJointed, slotNum);
               if (masterSlot != null) {
                  matrix.tempPushMatrix();
                  float offsetRate = (float)target.fixedInitVals[2] * ((float)target.extendRate / 100.0F) / 16.0F;
                  int sideOppositeIdx = EnumDirectionOld.OPPOSITES[target.connectSide];
                  EnumDirectionOld sideOpposite = EnumDirectionOld.VALID_DIRECTIONS[sideOppositeIdx];
                  matrix.translate((float)sideOpposite.offsetX * offsetRate, (float)(-sideOpposite.offsetY) * offsetRate, (float)(-sideOpposite.offsetZ) * offsetRate);
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
                  matrix.cmnVec.x = 0.0;
                  matrix.cmnVec.y = 1.0;
                  matrix.cmnVec.z = 0.0;
                  matrix.transform(matrix.cmnVec);
                  masterSlot.posX = matrix.baseEntityPosX + matrix.cmnVec.x;
                  masterSlot.posY = matrix.baseEntityPosY + matrix.cmnVec.y;
                  masterSlot.posZ = matrix.baseEntityPosZ + matrix.cmnVec.z;
                  masterSlot.isSlotValueSet = true;
                  matrix.tempPopMatrix();
               }
            }
         } else if (target.jointType == 10) {
            float extendRateX = 1.0F;
            float extendRateY = 1.0F;
            float extendRateZ = 1.0F;
            int extendAxis = JBRotationUtil.getExtenderAxis(target.connectSide);
            float extendRateCommon = Mth.sqrt((float)target.extendRate / 100.0F);
            if (extendAxis == 0) {
               extendRateX = extendRateCommon;
            }

            if (extendAxis == 1) {
               extendRateY = extendRateCommon;
            }

            if (extendAxis == 2) {
               extendRateZ = extendRateCommon;
            }

            float fix;
            if (target.renderOffsetX < 0.0F) {
               fix = (1.0F - extendRateX) / 2.0F;
               preOffsetX = target.renderOffsetX + fix;
               extendRateX = fix;
            } else if (target.renderOffsetX > 0.0F) {
               fix = (1.0F - extendRateX) / 2.0F;
               preOffsetX = target.renderOffsetX - fix;
               extendRateX = -fix;
            }

            float renderOffsetYShift = 0.5F * extendRateY - 0.5F + (fix = (1.0F * extendRateY - 1.0F) / 2.0F);
            preOffsetY += renderOffsetYShift;
            if (target.renderOffsetY < 0.0F) {
               preOffsetY = target.renderOffsetY + renderOffsetYShift - fix;
               extendRateY = -renderOffsetYShift - fix;
            } else if (target.renderOffsetY > 0.0F) {
               preOffsetY = target.renderOffsetY + renderOffsetYShift + fix;
               extendRateY = -renderOffsetYShift + fix;
            }

            if (target.renderOffsetZ < 0.0F) {
               fix = (1.0F - extendRateZ) / 2.0F;
               preOffsetZ = target.renderOffsetZ + fix;
               extendRateZ = fix;
            } else if (target.renderOffsetZ > 0.0F) {
               fix = (1.0F - extendRateZ) / 2.0F;
               preOffsetZ = target.renderOffsetZ - fix;
               extendRateZ = -fix;
            }

            if (isRemote) {
               target.tracePlayerLookRayScalerExtenderScaler(entityJointed, matrix, extendRateCommon, preOffsetX, preOffsetY, preOffsetZ);
            }

            matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
            if (target.extendRate == 0) {
               matrix.tempPushMatrix();
               matrix.scale(0.001F, 0.001F, 0.001F);
               target.onPositionThisPart(entityJointed, matrix);
               matrix.tempPopMatrix();
            } else {
               matrix.tempPushMatrix();
               matrix.scale(extendRateX, extendRateY, extendRateZ);
               target.onPositionThisPart(entityJointed, matrix);
               matrix.tempPopMatrix();
            }

            matrix.translate(extendRateX, -extendRateY, -extendRateZ);
         } else if (target.jointType == 12) {
            float extendRateX = 1.0F;
            float extendRateY = 1.0F;
            float extendRateZ = 1.0F;
            int extendAxis = target.getInitValue(1);
            extendRateY = Mth.sqrt((float)target.extendRate / 100.0F);
            if (extendRateY < 0.001F) {
               extendRateY = 0.001F;
            }

            if (extendAxis == 0) {
               extendRateZ = extendRateY;
               extendRateY = extendRateY;
               extendRateX = extendRateY;
            }

            if (extendAxis == 1) {
               extendRateX = extendRateY;
            }

            if (extendAxis == 2) {
               extendRateY = extendRateY;
            }

            if (extendAxis == 3) {
               extendRateZ = extendRateY;
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
            if (isRemote) {
               target.tracePlayerLookRayScalerExtenderScaler(entityJointed, matrix, extendRateX, preOffsetX, preOffsetY, preOffsetZ);
            }

            target.onPositionThisPart(entityJointed, matrix);
         } else if (target.jointType == 14) {
            float slideX = (float)target.rotateAngleX / 64.0F;
            float slideY = (float)target.rotateAngleY / 64.0F;
            float slideZ = (float)target.rotateAngleZ / 64.0F;
            matrix.translate(preOffsetX + slideX, -preOffsetY - slideY, -preOffsetZ - slideZ);
            if (isRemote) {
               target.tracePlayerLookRaySlider(entityJointed, matrix);
            }

            target.onPositionThisPart(entityJointed, matrix);
         } else {
            float rotX = (float)target.rotateAngleX;
            float rotY = (float)target.rotateAngleY;
            float rotZ = (float)target.rotateAngleZ;
            matrix.translate(preOffsetX, -preOffsetY, -preOffsetZ);
            float shiftX = 0.0F;
            float shiftY = 0.0F;
            float shiftZ = 0.0F;
            if (target.rotatePoint != 15) {
               shiftX = (float)JBRotationUtil.side_matrix[target.rotatePoint][0] * 0.5F;
               shiftY = (float)(-JBRotationUtil.side_matrix[target.rotatePoint][1]) * 0.5F;
               shiftZ = (float)(-JBRotationUtil.side_matrix[target.rotatePoint][2]) * 0.5F;
            }

            if (isRemote) {
               target.tracePlayerLookRayRotator(entityJointed, matrix, rotX, rotY, rotZ);
            }

            matrix.translate(0.0F + shiftX, 1.0F + shiftY, 0.0F + shiftZ);
            if (target.doSpecialRotFlg && !entityJointed.isOnInitPhase() && !entityJointed.disassemble) {
               if (calculateRotMatrixTwdTarget(entityJointed, target, matrix, false, 0.0, 0.0, 0.0, 0.0F)) {
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

               if (!matrix.isRemote) {
                  target.doSpecialRotFlg = false;
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
            target.onPositionThisPart(entityJointed, matrix);
            if (target.jointType == 0) {
               entityJointed.setFlyMoveDirectionFromCore(matrix);
               entityJointed.setMatrixToSensor(target, matrix, 0);
            }
         }
      }

      target.matrixPrivate = null;
   }

   protected static boolean calculateRotMatrixTwdTarget(EntityJointed entityJointed, JointParts target, ModelViewMatrix matrix, boolean isRender, double rX, double rY, double rZ, float pt) {
      matrix.rotTwdUpdateY = false;
      matrix.rotTwdUpdateX = false;
      if (target.motionHelper == null) {
         return false;
      } else {
         int args = target.motionHelper.commonUse;
         int targetNum = args >> 16 & 255;
         int mode = args >> 8 & 255;
         int maxAngle = (byte)(args & 255) + 128;
         EjTarget[] tList = entityJointed.targetList;
         if (targetNum >= 0 && targetNum < entityJointed.targetList.length && entityJointed.targetList[targetNum] != null) {
            double wpX;
            double wpY;
            double wpZ;
            if (tList[targetNum].isTargetEntity) {
               Entity entity = tList[targetNum].targetEntity;
               if (entity == null) {
                  return false;
               }

               wpX = entity.getX();
               wpY = entity.getY();
               wpZ = entity.getZ();
               wpY += (double)(entity.getBbHeight() / 2.0F);
            } else {
               wpX = (double)tList[targetNum].worldPosX + 0.5;
               wpY = (double)tList[targetNum].worldPosY + 0.5;
               wpZ = (double)tList[targetNum].worldPosZ + 0.5;
            }

            MatrixBase invMatrix;
            if ((invMatrix = matrix.createInvert(matrix.invMatrix)) == null) {
               return false;
            } else {
               if (isRender) {
                  matrix.cmnVec.x = wpX - rX;
                  matrix.cmnVec.y = wpY - rY;
                  matrix.cmnVec.z = wpZ - rZ;
               } else {
                  matrix.cmnVec.x = wpX - matrix.baseEntityPosX;
                  matrix.cmnVec.y = wpY - matrix.baseEntityPosY;
                  matrix.cmnVec.z = wpZ - matrix.baseEntityPosZ;
               }

               invMatrix.transform(matrix.cmnVec);
               double refX = matrix.cmnVec.x;
               double refY = matrix.cmnVec.y;
               double refZ = matrix.cmnVec.z;
               double rotX;
               if (mode == 0 || mode == 1) {
                  rotX = -90.0 - Math.atan2(refZ, refX) * 180.0 / Math.PI;
                  if (rotX > 180.0) {
                     rotX -= 360.0;
                  }

                  if (rotX < -180.0) {
                     rotX += 360.0;
                  }

                  matrix.cmnVec.y = Math.min(Math.max(rotX, (double)(-maxAngle)), (double)maxAngle);
                  matrix.rotTwdUpdateY = true;
               }

               if (mode == 0 || mode == 2) {
                  rotX = Math.atan2(refY, Math.sqrt(refX * refX + refZ * refZ)) * 180.0 / Math.PI;
                  if (rotX > 180.0) {
                     rotX -= 360.0;
                  }

                  if (rotX < -180.0) {
                     rotX += 360.0;
                  }

                  matrix.cmnVec.x = Math.min(Math.max(rotX, (double)(-maxAngle)), (double)maxAngle);
                  matrix.rotTwdUpdateX = true;
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   protected void updateExtraValues() {
      if (this.extraActionVarsHld != null) {
         ExtraActionVarsHolder hld = this.extraActionVarsHld;

         for(int i = 0; i < hld.extraActionVars.length; ++i) {
            int[] var10000 = hld.extraActionVars;
            var10000[i] += hld.extraActionVarsTickModify[i];
            if (hld.extraActionVars[i] < hld.extraActionVarsMin[i]) {
               hld.extraActionVars[i] = hld.extraActionVarsMin[i];
            }

            if (hld.extraActionVars[i] > hld.extraActionVarsMax[i]) {
               hld.extraActionVars[i] = hld.extraActionVarsMax[i];
            }
         }
      }

   }

   protected void handleInitMotion(EntityJointed entityJointed) {
      switch (this.jointType) {
         case 8:
         case 14:
            this.handleRotatorInit(entityJointed, entityJointed.initTick);
         case 9:
         case 11:
         case 13:
         default:
            break;
         case 10:
         case 12:
            this.handleExtenderInit(entityJointed, entityJointed.initTick);
            break;
         case 15:
            this.handleAttacherInit(entityJointed, entityJointed.initTick);
      }

   }

   protected void setInittedMotionValues(EntityJointed entityJointed) {
      switch (this.jointType) {
         case 8:
         case 14:
            this.setInittedRotatorValues();
         case 9:
         case 11:
         case 13:
         default:
            break;
         case 10:
         case 12:
            this.setInittedExtenderValues();
            break;
         case 15:
            this.setInittedAttacherValues();
      }

   }

   protected void handleRotatorInit(EntityJointed entityJointed, int entityInitTick) {
      if (!this.hasInit) {
         if (entityInitTick >= 30 && !this.hasInit) {
            this.setInittedRotatorValues();
         } else {
            this.rotateAngleXNew += (short)((int)((float)(this.fixedInitVals[0] - this.rotateAngleXNew) * ((float)entityInitTick / 30.0F)));
            this.rotateAngleYNew += (short)((int)((float)(this.fixedInitVals[1] - this.rotateAngleYNew) * ((float)entityInitTick / 30.0F)));
            this.rotateAngleZNew += (short)((int)((float)(this.fixedInitVals[2] - this.rotateAngleZNew) * ((float)entityInitTick / 30.0F)));
         }

      }
   }

   private void setInittedRotatorValues() {
      this.rotateAngleXNew = this.fixedInitVals[0];
      this.rotateAngleYNew = this.fixedInitVals[1];
      this.rotateAngleZNew = this.fixedInitVals[2];
      this.hasInit = true;
   }

   protected void handleExtenderInit(EntityJointed entityJointed, int entityInitTick) {
      if (!this.hasInit) {
         if (entityInitTick >= 30 && !this.hasInit) {
            this.setInittedExtenderValues();
         } else {
            this.extendRateNew += (short)((int)((float)(this.fixedInitVals[0] - this.extendRateNew) * ((float)entityInitTick / 30.0F)));
         }

      }
   }

   private void setInittedExtenderValues() {
      this.extendRateNew = this.fixedInitVals[0];
      this.hasInit = true;
   }

   protected void handleAttacherInit(EntityJointed entityJointed, int entityInitTick) {
      if (!this.hasInit) {
         if (entityInitTick >= 30 && !this.hasInit) {
            this.setInittedAttacherValues();
         } else {
            this.extendRateNew += (short)((int)((float)(0 - this.extendRateNew) * ((float)entityInitTick / 30.0F)));
         }

      }
   }

   private void setInittedAttacherValues() {
      this.extendRateNew = 0;
      this.hasInit = true;
   }

   protected void handleDisassembleMotion(EntityJointed entityJointed) {
      switch (this.jointType) {
         case 8:
         case 14:
            this.handleRotatorDisassemble(entityJointed, entityJointed.disassembleTick);
         case 9:
         case 11:
         case 13:
         default:
            break;
         case 10:
         case 12:
            this.handleExtenderDisassemble(entityJointed, entityJointed.disassembleTick);
            break;
         case 15:
            this.handleAttacherDisassemble(entityJointed, entityJointed.disassembleTick);
      }

   }

   protected void handleRotatorDisassemble(EntityJointed entityJointed, int entityTotalTick) {
      if (entityTotalTick >= 30) {
         this.rotateAngleXNew = 0;
         this.rotateAngleYNew = 0;
         this.rotateAngleZNew = 0;
      } else {
         this.rotateAngleXNew = (short)((int)((float)(0 - this.rotateAngleXNew) * ((float)entityTotalTick / 30.0F)));
         this.rotateAngleYNew = (short)((int)((float)(0 - this.rotateAngleYNew) * ((float)entityTotalTick / 30.0F)));
         this.rotateAngleZNew = (short)((int)((float)(0 - this.rotateAngleZNew) * ((float)entityTotalTick / 30.0F)));
      }

   }

   protected void handleExtenderDisassemble(EntityJointed entityJointed, int entityTotalTick) {
      if (entityTotalTick >= 30) {
         this.extendRateNew = 100;
      } else {
         this.extendRateNew += (short)((int)((float)(100 - this.extendRateNew) * ((float)entityTotalTick / 30.0F)));
      }

   }

   protected void handleAttacherDisassemble(EntityJointed entityJointed, int entityTotalTick) {
      if (entityTotalTick >= 30) {
         this.extendRateNew = 100;
      } else {
         this.extendRateNew += (short)((int)((float)(100 - this.extendRateNew) * ((float)entityTotalTick / 30.0F)));
      }

   }

   protected void fixValuesInValidRange() {
      while(this.rotateAngleXNew > 360) {
         this.rotateAngleXNew -= 360;
      }

      while(this.rotateAngleXNew < -360) {
         this.rotateAngleXNew += 360;
      }

      while(this.rotateAngleYNew > 360) {
         this.rotateAngleYNew -= 360;
      }

      while(this.rotateAngleYNew < -360) {
         this.rotateAngleYNew += 360;
      }

      while(this.rotateAngleZNew > 360) {
         this.rotateAngleZNew -= 360;
      }

      while(this.rotateAngleZNew < -360) {
         this.rotateAngleZNew += 360;
      }

   }

   public void savePrevValue() {
      this.prevRotateAngleX = this.rotateAngleX;
      this.prevRotateAngleY = this.rotateAngleY;
      this.prevRotateAngleZ = this.rotateAngleZ;
      this.prevExtendRate = this.extendRate;
   }

   public void fixPrevValues() {
      if (this.jointType != 14 && this.jointType != 15) {
         if (!this.motionHanlder.isIgnoreRotationPrevFix()) {
            while(this.rotateAngleX - this.prevRotateAngleX < -180) {
               this.prevRotateAngleX = (short)((int)((float)this.prevRotateAngleX - 360.0F));
            }

            while(this.rotateAngleX - this.prevRotateAngleX >= 180) {
               this.prevRotateAngleX = (short)((int)((float)this.prevRotateAngleX + 360.0F));
            }

            while(this.rotateAngleY - this.prevRotateAngleY < -180) {
               this.prevRotateAngleY = (short)((int)((float)this.prevRotateAngleY - 360.0F));
            }

            while(this.rotateAngleY - this.prevRotateAngleY >= 180) {
               this.prevRotateAngleY = (short)((int)((float)this.prevRotateAngleY + 360.0F));
            }

            while(this.rotateAngleZ - this.prevRotateAngleZ < -180) {
               this.prevRotateAngleZ = (short)((int)((float)this.prevRotateAngleZ - 360.0F));
            }

            while(this.rotateAngleZ - this.prevRotateAngleZ >= 180) {
               this.prevRotateAngleZ = (short)((int)((float)this.prevRotateAngleZ + 360.0F));
            }

         }
      }
   }

   public static final void ensureMotionHanlder(JointParts thiss) {
      Item item;
      if (thiss.motionHanlder == null) {
         if (thiss.motionItemId <= 0) {
            thiss.motionHanlder = BasicMotionBinder.instance.getBindMotionHandler(thiss.bindNum, thiss.motionMeta);
         } else {
            item = Item.byId(thiss.motionItemId);
            if (item != null && item instanceof IJBMotionBinderItem) {
               IJBMotionBinderItem.IJBMotionBinder binder = ((IJBMotionBinderItem)item).getMotionBinder();
               if (binder != null) {
                  thiss.motionHanlder = binder.getBindMotionHandler(thiss.bindNum, thiss.motionMeta);
               }
            }
         }

         if (thiss.motionHanlder == null) {
            thiss.motionHanlder = BasicMotionBinder.instance.getBindMotionHandler(0, 0);
            thiss.motionItemId = 0;
            thiss.bindNum = 0;
         }
      }

      if (thiss.actionHanlder == null) {
         item = Item.byId(thiss.modelItemId);
         if (item != null && item instanceof IJBModelItem) {
            thiss.actionHanlder = ((IJBModelItem)item).getIJBActionHandler(thiss.modelMeta);
         }

         if (thiss.actionHanlder != null) {
            thiss.hasAction = true;
         } else {
            thiss.hasAction = false;
         }
      }

   }

   protected void onPositionThisPart(EntityJointed entityJointed, ModelViewMatrix matrix) {
      matrix.updateMinMaxDirect(this.jointType, this.connectSide);
      this.motionHanlder.onJointPositionUpdate(this, entityJointed, matrix);
      if (this.jointType == 2) {
         int seatNum = this.initValues[0][0] - 1;
         if (0 <= seatNum && seatNum <= 3 && !entityJointed.entitySeatPartArray[seatNum].isPositionUpdated) {
            EntitySeatPart seatPart = entityJointed.entitySeatPartArray[seatNum];
            if (this.initValues[0][0] == 1) {
               seatPart.isOperationSeat = true;
            } else {
               seatPart.isOperationSeat = false;
            }

            if (this.initValues[0][2] == 1) {
               seatPart.isAsInteractableJoint = true;
               seatPart.isAsCameraJoint = false;
               seatPart.notAsSeat = true;
            } else if (this.initValues[0][2] == 2) {
               seatPart.isAsInteractableJoint = false;
               seatPart.isAsCameraJoint = true;
               seatPart.notAsSeat = true;
            } else {
               seatPart.isAsInteractableJoint = false;
               seatPart.isAsCameraJoint = false;
               seatPart.notAsSeat = false;
            }

            if (entityJointed.level().isClientSide) {
               if (seatPart.isAsCameraJoint) {
                  seatPart.attachAndCreateCamera();
               }

               if (this.isExtraFlagsTypeSpecCommonA()) {
                  seatPart.hidePlayer = true;
               }

               if (seatPart.cameraModeSeatNum == -3 && this.isExtraFlagsTypeSpecCommonB()) {
                  seatPart.cameraModeSeatNum = -2;
               }

               if (this.isExtraFlagsTypeSpecCommonC()) {
                  seatPart.updateRidersYaw = true;
               }
            }

            if (entityJointed.level().isClientSide && seatPart.entityCamera != null) {
               this.updateCamEntity(entityJointed, matrix, seatPart);
            }

            if (this.seatWeakRef == null) {
               this.seatWeakRef = new WeakReference(seatPart);
            }

            matrix.cmnVec.x = 0.0;
            matrix.cmnVec.y = 1.5 - (entityJointed.disassemble ? 0.0 : (double)this.initValues[0][1] / 16.0);
            matrix.cmnVec.z = 0.0;
            matrix.transform(matrix.cmnVec);
            seatPart.updatedJointPositionX = matrix.baseEntityPosX + matrix.cmnVec.x;
            seatPart.updatedJointPositionY = matrix.baseEntityPosY + matrix.cmnVec.y;
            seatPart.updatedJointPositionZ = matrix.baseEntityPosZ + matrix.cmnVec.z;
            seatPart.isPositionUpdated = true;
            if (seatPart.updateRidersYaw) {
               matrix.tempPushMatrix();
               matrix.translate(0.0F, 1.0F, 0.0F);
               float rotY = JBRotationUtil.roatYawEachSide[this.facing];
               matrix.rotateYY(-rotY * 0.017453292F);
               matrix.translate(-0.0F, -1.0F, -0.0F);
               seatPart.ridersYaw = 180.0F + (float)(-Math.toDegrees(Math.atan2((double)matrix.m02, (double)matrix.m22)));
               if (Float.isNaN(seatPart.ridersYaw) || Float.isInfinite(seatPart.ridersYaw)) {
                  seatPart.ridersYaw = 0.0F;
               }

               matrix.tempPopMatrix();
            }
         }
      }

      if (this.doAction) {
         if (!this.isDisableAction() && this.actionHanlder != null) {
            matrix.tempPushMatrix();
            matrix.translate(0.0F, 1.0F, 0.0F);
            if (JBCommonUtil.doSideRotateOnRender(this.jointType)) {
               float[] rotXZ = JBRotationUtil.rotXZ_matrix[this.renderSide];
               matrix.rotateXX(rotXZ[0] * 3.1415927F);
               matrix.rotateZZ(rotXZ[1] * 3.1415927F);
            }

            float rotY = JBRotationUtil.roatYawEachSide[this.facing];
            matrix.rotateYY(-rotY * 0.017453292F);
            matrix.translate(-0.0F, -1.0F, -0.0F);
            this.actionHanlder.action(entityJointed.level(), entityJointed, this.actionConductor, this, matrix);
            matrix.tempPopMatrix();
         }

         this.doAction = false;
         this.actionConductor = null;
      }

      if (this.doEffect != -1 && entityJointed.level().isClientSide) {
         int effectSlotNum = this.doEffect;
         this.doEffect = -1;
         if (effectSlotNum >= 0 && effectSlotNum < entityJointed.effectSlots.length) {
            EffectSlot effectSlot = entityJointed.effectSlots[effectSlotNum];
            if (effectSlot != null) {
               matrix.cmnVec.x = 0.0;
               matrix.cmnVec.y = 1.0;
               matrix.cmnVec.z = 0.0;
               matrix.transformAbsolute(matrix.cmnVec);
               this.updateEffectPosition(matrix.cmnVec);
               if (!effectSlot.isParticle() && effectSlot.checkEnabled()) {
                  JBModContainer.proxy.playPartsSoundEvent(this, entityJointed, effectSlot);
               } else if (effectSlot.isParticle()) {
                  matrix.cmnVec.x = 0.0;
                  matrix.cmnVec.y = (double)(-(effectSlot.volume + 128)) * 0.01;
                  matrix.cmnVec.z = 0.0;
                  matrix.transformOnlyRotation(matrix.cmnVec);
                  this.updateEffectVelocity(matrix.cmnVec);
                  JBModContainer.proxy.playPartsParticleEvent(this, entityJointed, effectSlot);
               }
            }
         }
      }

      if (matrix.isRemote && entityJointed.isTunbleEntityOnClient) {
         this.tracePlayerLookRayCommon(entityJointed, matrix);
      }

   }

   protected void tracePlayerLookRayRotator(EntityJointed entityJointed, ModelViewMatrix matrix, float rotAngleX, float rotAngleY, float rotAngleZ) {
   }

   protected void tracePlayerLookRaySlider(EntityJointed entityJointed, ModelViewMatrix matrix) {
   }

   protected void tracePlayerLookRayScalerExtenderScaler(EntityJointed entityJointed, ModelViewMatrix matrix, float scale, float preOffsetX, float preOffsetY, float preOffsetZ) {
   }

   protected void tracePlayerLookRayCommon(EntityJointed entityJointed, ModelViewMatrix matrix) {
   }

   public void detectCarvingType() {
   }

   public EntitySeatPart getRelatedSeatPart() {
      return this.seatWeakRef != null ? (EntitySeatPart)this.seatWeakRef.get() : null;
   }

   public boolean hasModelAction() {
      return this.hasAction;
   }

   public void dispatchModelAction(LivingEntity conducted) {
      if (conducted != null && !conducted.level().isClientSide) {
         this.doAction = true;
         this.actionConductor = conducted;
      }

   }

   public void turnIntoBlock(IJointListsHolder.BlockArranger arranger, EntityJointed entityJointedMayBeNull, int coreFacing, int corePosX, int corePosY, int corePosZ) {
      int[] ret = JBRotationUtil.rotateZFrontToFacing(coreFacing, new int[3], this.xCoord, this.yCoord, this.zCoord);
      int toPlaceX = corePosX + ret[0];
      int toPlaceY = corePosY + ret[1];
      int toPlaceZ = corePosZ + ret[2];
      arranger.placeBlockAt(this, entityJointedMayBeNull, toPlaceX, toPlaceY, toPlaceZ, coreFacing, this.caclFacingSide(coreFacing));
   }

   public Block getJBBlockType() {
      Block block = null;
      switch (this.jointType) {
         case 0:
            block = JBBlocks.JointCoreBlock;
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
            block = JBBlocks.JointSeatBlock;
            break;
         case 4:
            block = JBBlocks.JointNormalBlock;
            break;
         case 8:
            block = JBBlocks.JointRotatorBlock;
            break;
         case 10:
            block = JBBlocks.JointExtenderBlock;
            break;
         case 12:
            block = JBBlocks.JointScalerBlock;
            break;
         case 14:
            block = JBBlocks.JointSliderBlock;
            break;
         case 15:
            block = JBBlocks.JointAttracterBlock;
      }

      return block;
   }

   private int caclFacingSide(int coreFacing) {
      if (JBCommonUtil.doSideRotateOnRender(this.jointType) && this.carvingData == null) {
         return this.modelItemId == 0 ? JBRotationUtil.convertSideFacingFromFaceBasedToZpositive(coreFacing, this.facing, this.connectSide) : JBRotationUtil.convertSideFacingFromFaceBasedToZpositive(coreFacing, this.facing, this.renderSide);
      } else {
         return JBRotationUtil.convertSideFacingFromFaceBasedToZpositive(coreFacing, this.facing);
      }
   }

   public void reAttatchSlaveToTileEntity(TileEntityJoint te, EntityJointed entityJointed) {
      if (te instanceof TileEntityJointAttracter) {
         TileEntityJointAttracter teAttracter = (TileEntityJointAttracter)te;
         int slotNum = this.initValues[0][1];
         if (entityJointed.getMasterSlotEnableStateFlag(slotNum)) {
            MasterSlot masterSlot = MasterSlot.getFromEntity(entityJointed, slotNum);
            if (masterSlot != null && masterSlot.isConnectionAlive) {
               AbstractSlaveAttatcher slaveAttatcher = masterSlot.getConnectedSlaveAtattcher();
               EntityJointed slaveJointed = masterSlot.getConnectedSlaveEntity();
               if (slaveAttatcher == null || slaveJointed == null) {
                  return;
               }

               if (slaveAttatcher instanceof AbstractSlaveAttatcher.JointSlaveAttatcher) {
                  AbstractSlaveAttatcher.JointSlaveAttatcher jsa = (AbstractSlaveAttatcher.JointSlaveAttatcher)slaveAttatcher;
                  if (jsa.entityMaster == entityJointed) {
                     MasterSlaveControl.setupTileEntityAttatcher(slaveJointed, teAttracter);
                     slaveJointed.requestCoreUpdateForawhile();
                  }
               }
            }
         }

      }
   }

   public void setExtraFlagsTransparentAlways(boolean value) {
      if (value) {
         this.extraJointStateFlags |= Integer.MIN_VALUE;
      } else {
         this.extraJointStateFlags &= Integer.MAX_VALUE;
      }

   }

   public boolean isExtraFlagsTransparentAlways() {
      return (this.extraJointStateFlags & Integer.MIN_VALUE) != 0;
   }

   public void setExtraFlagsTransparentOnBoard(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 1073741824;
      } else {
         this.extraJointStateFlags &= -1073741825;
      }

   }

   public boolean isExtraFlagsTransparentOnBoard() {
      return (this.extraJointStateFlags & 1073741824) != 0;
   }

   public void setExtraFlagsWireframeOnBoard(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 536870912;
      } else {
         this.extraJointStateFlags &= -536870913;
      }

   }

   public boolean isExtraFlagsWireframeOnBoard() {
      return (this.extraJointStateFlags & 536870912) != 0;
   }

   public void setExtraFlagsVisibleOnlyOnBoard(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 33554432;
      } else {
         this.extraJointStateFlags &= -33554433;
      }

   }

   public boolean isExtraFlagsVisibleOnlyOnBoard() {
      return (this.extraJointStateFlags & 33554432) != 0;
   }

   public void setExtraFlagsTypeSpecCommonA(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 268435456;
      } else {
         this.extraJointStateFlags &= -268435457;
      }

   }

   public boolean isExtraFlagsTypeSpecCommonA() {
      return (this.extraJointStateFlags & 268435456) != 0;
   }

   public void setExtraFlagsTypeSpecCommonB(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 134217728;
      } else {
         this.extraJointStateFlags &= -134217729;
      }

   }

   public boolean isExtraFlagsTypeSpecCommonB() {
      return (this.extraJointStateFlags & 134217728) != 0;
   }

   public void setExtraFlagsTypeSpecCommonC(boolean value) {
      if (value) {
         this.extraJointStateFlags |= 67108864;
      } else {
         this.extraJointStateFlags &= -67108865;
      }

   }

   public boolean isExtraFlagsTypeSpecCommonC() {
      return (this.extraJointStateFlags & 67108864) != 0;
   }

   public TileEntityJoint getTileEntityFromCoord(Level world, int coreFacing, int corePosX, int corePosY, int corePosZ) {
      int[] ret = JBRotationUtil.rotateZFrontToFacing(coreFacing, new int[3], this.xCoord, this.yCoord, this.zCoord);
      return BlockJointBase.getTileEntityJointerBase(world, corePosX + ret[0], corePosY + ret[1], corePosZ + ret[2]);
   }

   public void destroyBlockAtCoords(Level world, int coreFacing, int corePosX, int corePosY, int corePosZ) {
      int[] ret = JBRotationUtil.rotateZFrontToFacing(coreFacing, new int[3], this.xCoord, this.yCoord, this.zCoord);
      int targetX = corePosX + ret[0];
      int targetY = corePosY + ret[1];
      int targetZ = corePosZ + ret[2];
      TileEntityJoint te = BlockJointBase.getTileEntityJointerBase(world, targetX, targetY, targetZ);
      if (te != null) {
         te.breakByAssemble = true;
      }

      WorldHelper.setBlockToAir(world, targetX, targetY, targetZ);
   }

   public int getRenderRotatedSide(int targetSide) {
      return JBRotationUtil.getSideWhenRotatedSideAndFace(this.renderSide, this.facing, targetSide);
   }

   public void setResetParentRotation() {
      this.cancelParentRotate = true;
   }

   public boolean getMotionCommonFlag() {
      return this.paramModifyDirFlg;
   }

   public void setMotionCommonFlag(boolean value) {
      this.paramModifyDirFlg = value;
   }

   public boolean isDisableMotion() {
      int disableMotVal = this.getCurrentInitVals()[3];
      return isDisableMotValue(disableMotVal);
   }

   protected static boolean isDisableMotValue(int disableMotVal) {
      return disableMotVal == 1 || disableMotVal == 3;
   }

   public boolean isDisableAction() {
      int disableMotVal = this.getCurrentInitVals()[3];
      return isDisableActValue(disableMotVal);
   }

   protected static boolean isDisableActValue(int disableMotVal) {
      return disableMotVal == 2 || disableMotVal == 3;
   }

   public double getAttracterSlaveOffsetDiv() {
      return this.initValues != null && this.initValues[0] != null ? (double)this.fixedInitVals[2] * ((double)this.extendRate / 100.0) / 16.0 : 1.0;
   }

   public void purgeAttatchedSlave(EntityJointed entityJointedMaster, short purgeTick) {
      if (this.initValues != null && this.initValues[0] != null) {
         short slotNum = this.initValues[0][1];
         if (entityJointedMaster.getMasterSlotEnableStateFlag(slotNum)) {
            MasterSlot masterSlot = MasterSlot.getFromEntity(entityJointedMaster, slotNum);
            if (masterSlot != null && masterSlot.isConnectionAlive) {
               EntityJointed slaveJointed = null;
               if (masterSlot.entityJointedWeak != null) {
                  slaveJointed = (EntityJointed)masterSlot.entityJointedWeak.get();
               }

               AbstractSlaveAttatcher slaveAttatcher = null;
               if (masterSlot.slaveAttatcherWeak != null) {
                  slaveAttatcher = (AbstractSlaveAttatcher)masterSlot.slaveAttatcherWeak.get();
               }

               if (slaveJointed == null || slaveAttatcher == null) {
                  return;
               }

               if (slaveAttatcher.connectedMasterSlotWeak != null && slaveAttatcher.connectedMasterSlotWeak.get() == masterSlot) {
                  slaveJointed.purgeSlaveAttatcher(purgeTick);
               }
            }
         }

      }
   }

   private void updateCamEntity(EntityJointed entityJointed, ModelViewMatrix matrix, EntitySeatPart seatPart) {
      matrix.tempPushMatrix();
      matrix.translate(0.0F, 1.0F, 0.0F);
      float rotY = JBRotationUtil.roatYawEachSide[this.facing];
      matrix.rotateYY(-rotY * 0.017453292F);
      matrix.translate(-0.0F, -1.0F, -0.0F);
      matrix.cmnVec.x = 0.0;
      matrix.cmnVec.y = 1.0;
      matrix.cmnVec.z = 0.0;
      matrix.transform(matrix.cmnVec);
      double camPositionX = matrix.baseEntityPosX + matrix.cmnVec.x;
      double camPositionY = matrix.baseEntityPosY + matrix.cmnVec.y;
      double camPositionZ = matrix.baseEntityPosZ + matrix.cmnVec.z;
      matrix.scale(1.0F, -1.0F, -1.0F);
      JBModContainer.proxy.setCameraMatrix(seatPart.entityCamera, camPositionX, camPositionY, camPositionZ, matrix);
      matrix.tempPopMatrix();
   }

   public JointParts tryGetSide(int side) {
      return this.jointType != 0 && this.connectSide == side && this.parent != null ? this.parent : this.child[side];
   }

   public void setToIninitalValue() {
      if (this.jointType != 8 && this.jointType != 14) {
         if (this.jointType == 10 || this.jointType == 12) {
            this.setNewExtendRate(this.getInitValue(0));
         }
      } else {
         this.setNewRotationX(this.getInitValue(0));
         this.setNewRotationY(this.getInitValue(1));
         this.setNewRotationZ(this.getInitValue(2));
      }

      this.paramModifyDirFlg = false;
   }

   public void updateEffectPosition(Vec3 soundPos) {
   }

   public void updateEffectVelocity(Vec3 soundPos) {
   }

   public static class ExtraActionVarsHolder {
      public int[] extraActionVars;
      public int[] extraActionVarsTickModify;
      public int[] extraActionVarsMin;
      public int[] extraActionVarsMax;
   }
}
