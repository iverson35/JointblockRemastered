package jp.mc.ancientred.jointblock.assemble.ring;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.EntrySetting;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityJointed;
import jp.mc.ancientred.jointblock.gui.JBGuiSettingsImpl;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractRingNode {
   public static final int NODETYPE_ST = 0;
   public static final int NODETYPE_TG = 1;
   public static final int NODETYPE_DR = 2;
   protected static final byte RESERVED_B = 0;
   protected static final short RESERVED_S = 0;
   protected static final int RESERVED_I = 0;
   public static final int ARRAY_VALUES_LEN = 4;
   protected static final int KEY_TYPE_SINGLE = 0;
   protected static final int KEY_TYPE_DOUBLE = 1;
   protected static final double EPSILON = 1.0E-4;
   public static final AbstractRingNode[][] NODETYPELIST = new AbstractRingNode[][]{{new RingNodeSTEmpty(), new RingNodeSTJumpIf(), new RingNodeSTWait(), new RingNodeSTRingCall(), new RingNodeSTReturn(), new RingNodeSTVar(), new RingNodeSTVarOpr2V(), new RingNodeSTVarOpr1V(), new RingNodeSTLink(), new RingNodeSTVector()}, {new RingNodeTGKey(), new RingNodeTGItem(), new RingNodeTGSeat(), new RingNodeTGPlayer(), new RingNodeTGUnitMove(), new RingNodeTGUnitState(), new RingNodeTGChannelState(), new RingNodeTGSensor(), new RingNodeTGTarget(), new RingNodeTGParts(), new RingNodeTGEntity(), new RingNodeTGBlock()}, {new RingNodeDRMotion3VSet(8), new RingNodeDRMotion1VSet(10), new RingNodeDRMotion3VSet(14), new RingNodeDRMotion1VSet(12), new RingNodeDRAttracter(), new RingNodeDRActionDo(), new RingNodeDREffectDo(), new RingNodeDRRotTwdTarget(), new RingNodeDRMoveDo(), new RingNodeDRMoveTask(), new RingNodeDRYawSet(), new RingNodeDRYawFollow(), new RingNodeDRUseKey(), new RingNodeDRUseItem(), new RingNodeDRSearch(), new RingNodeDRTargetSet(), new RingNodeDRChannelClear(), new RingNodeDRChangeMode()}};
   protected static final int NODE_TYPE_ID_ST_EMPTY = 0;
   protected static final int NODE_TYPE_ID_ST_RETURN = 2;
   protected static final int NODE_TYPE_ID_ST_RINGCALL = 4;
   protected static final int NODE_TYPE_ID_ST_WAIT = 6;
   protected static final int NODE_TYPE_ID_ST_JUMPIF = 8;
   protected static final int NODE_TYPE_ID_ST_AND = 10;
   protected static final int NODE_TYPE_ID_ST_OR = 12;
   protected static final int NODE_TYPE_ID_ST_NOT = 14;
   protected static final int NODE_TYPE_ID_ST_VAR = 16;
   protected static final int NODE_TYPE_ID_ST_VAROPR1V = 18;
   protected static final int NODE_TYPE_ID_ST_VAROPR2V = 20;
   protected static final int NODE_TYPE_ID_ST_LINK = 22;
   protected static final int NODE_TYPE_ID_ST_VECTOR = 24;
   protected static final int NODE_TYPE_ID_DR_MOT1V_SET = 30;
   protected static final int NODE_TYPE_ID_DR_MOT3V_SET = 34;
   protected static final int NODE_TYPE_ID_DR_ROT_TWD_TARGET = 36;
   protected static final int NODE_TYPE_ID_DR_ACTIONDO = 38;
   protected static final int NODE_TYPE_ID_DR_MOVEDO = 40;
   protected static final int NODE_TYPE_ID_DR_YAWASET = 42;
   protected static final int NODE_TYPE_ID_DR_YAWAFOLLOW = 46;
   protected static final int NODE_TYPE_ID_DR_MOVETASK = 48;
   protected static final int NODE_TYPE_ID_DR_CHANNELCLEAR = 50;
   protected static final int NODE_TYPE_ID_DR_CHANGEMODE = 52;
   protected static final int NODE_TYPE_ID_DR_USEKEY = 54;
   protected static final int NODE_TYPE_ID_DR_USEITEM = 56;
   protected static final int NODE_TYPE_ID_DR_SEARCH = 58;
   protected static final int NODE_TYPE_ID_DR_TARGETSET = 60;
   protected static final int NODE_TYPE_ID_DR_ATTRACTER = 62;
   protected static final int NODE_TYPE_ID_DR_EFFECTDO = 64;
   protected static final int NODE_TYPE_ID_TG_KEY = 70;
   protected static final int NODE_TYPE_ID_TG_ITEM = 72;
   protected static final int NODE_TYPE_ID_TG_SEAT = 74;
   protected static final int NODE_TYPE_ID_TG_PLAYER = 76;
   protected static final int NODE_TYPE_ID_TG_UNITMOVE = 78;
   protected static final int NODE_TYPE_ID_TG_UNITSTATE = 80;
   protected static final int NODE_TYPE_ID_TG_UNITSWING = 82;
   protected static final int NODE_TYPE_ID_DR_CHANNELSTATE = 84;
   protected static final int NODE_TYPE_ID_TG_SENSOR = 86;
   protected static final int NODE_TYPE_ID_TG_TARGET = 88;
   protected static final int NODE_TYPE_ID_TG_ENTITY = 90;
   protected static final int NODE_TYPE_ID_TG_BLOCK = 92;
   protected static final int NODE_TYPE_ID_TG_PARTS = 94;
   public static final int RET_TYPE_BINARY = 0;
   public static final int RET_TYPE_NUMBER = 1;
   public static final int RET_STATE_FALSE = 0;
   public static final int RET_STATE_TRUE = 1;
   public static final double RET_STATE_ERROR = Double.NaN;
   protected static final int SLOTTYPE_MOT = 10;
   protected static final int SLOTTYPE_ACT = 20;
   protected static final int SLOTTYPE_MOV = 30;
   protected static final int SLOTTYPE_INP = 40;
   protected static final int[] SLOTTYPE_CONV = new int[]{10, 20, 30, 40};
   public static final int MAX_TIMETICK = 32767;
   public static final int ERROR_INDEX = -1;
   protected long tickGeneration;
   public byte nodeIndex;
   public static final int GUI_REFCHAR_INDEX_0 = 0;
   public static final int GUI_REFCHAR_INDEX_1 = 1;
   public static final int GUI_REFCHAR_INDEX_2 = 2;
   public static final int GUI_REFCHAR_INDEX_3 = 3;
   public static final int GUI_REFCHAR_INDEX_4 = 4;
   public static final int GUI_REFCHAR_INDEX_5 = 5;
   public static final int GUI_REFCHAR_INDEX_A = 6;
   public static final int GUI_REFCHAR_INDEX_B = 7;
   public static final int GUI_REFCHAR_INDEX_I = 8;
   public static final int GUI_REFCHAR_INDEX_O = 9;
   public static final int GUI_REFCHAR_INDEX_J = 10;
   public static final int GUI_REFCHAR_INDEX_T = 11;
   public static final int GUI_REFCHAR_INDEX_X = 12;
   public static final int GUI_REFCHAR_INDEX_Y = 13;
   public static final int GUI_REFCHAR_INDEX_Z = 14;
   public static final int GUI_REFCHAR_INDEX_E = 15;
   public static final int GUI_REFCHAR_INDEX_S = 16;
   public static final int GUI_REFCHAR_INDEX_L = 17;
   public static final int GUI_REFCHAR_INDEX_TG = 18;
   public static final int GUI_REFCHAR_INDEX_R = 19;
   public static final int GUI_REFCHAR_INDEX_H = 20;
   public static final int GUI_REFCHAR_INDEX_W = 21;
   public static final int LINE_COL_FLOW = 3407667;
   public static final int LINE_COL_SET = 7895295;
   public static final int LINE_COL_REF = 16734296;
   public static final int LINE_COL_SUB = 5789784;
   public RingGui gui;

   public abstract AbstractRingNode newInstance();

   public abstract byte getNodeTypeID();

   public double getValue(int entryNum) {
      return 0.0;
   }

   public void setValue(int entryNum, double value) {
   }

   public AbstractRingNode cloneNode(NodeCloneHelper helper) {
      AbstractRingNode node = this.newInstance();

      try {
         helper.reset();
         this.writeNodeData(helper.dBufOut);
         helper.transferBufferToBuffer();
         node.readNodeData(helper.dBufInn);
         return node;
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   protected void writeNodeData(DataBufferableOutput out) throws IOException {
   }

   protected void writeRunningData(DataBufferableOutput out) throws IOException {
   }

   public void write(DataBufferableOutput out) throws IOException {
      out.writeByte(this.getNodeTypeID());
      this.writeNodeData(out);
   }

   protected void readNodeData(DataBufferableInput inn) throws IOException {
   }

   protected void readRunningData(DataBufferableInput inn) throws IOException {
   }

   public static AbstractRingNode read(DataBufferableInput inn) throws IOException {
      byte nodeType = inn.readByte((byte)0);
      AbstractRingNode ringNode = null;

      try {
         switch (nodeType) {
            case 0:
            case 1:
            case 3:
            case 5:
            case 7:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 17:
            case 19:
            case 21:
            case 23:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 31:
            case 32:
            case 33:
            case 35:
            case 37:
            case 39:
            case 41:
            case 43:
            case 44:
            case 45:
            case 47:
            case 49:
            case 51:
            case 53:
            case 55:
            case 57:
            case 59:
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 71:
            case 73:
            case 75:
            case 77:
            case 79:
            case 81:
            case 82:
            case 83:
            case 85:
            case 87:
            case 89:
            case 91:
            case 93:
            default:
               ringNode = new RingNodeSTEmpty();
               break;
            case 2:
               ringNode = new RingNodeSTReturn();
               break;
            case 4:
               ringNode = new RingNodeSTRingCall();
               break;
            case 6:
               ringNode = new RingNodeSTWait();
               break;
            case 8:
               ringNode = new RingNodeSTJumpIf();
               break;
            case 16:
               ringNode = new RingNodeSTVar();
               break;
            case 18:
               ringNode = new RingNodeSTVarOpr1V();
               break;
            case 20:
               ringNode = new RingNodeSTVarOpr2V();
               break;
            case 22:
               ringNode = new RingNodeSTLink();
               break;
            case 24:
               ringNode = new RingNodeSTVector();
               break;
            case 30:
               ringNode = new RingNodeDRMotion1VSet();
               break;
            case 34:
               ringNode = new RingNodeDRMotion3VSet();
               break;
            case 36:
               ringNode = new RingNodeDRRotTwdTarget();
               break;
            case 38:
               ringNode = new RingNodeDRActionDo();
               break;
            case 40:
               ringNode = new RingNodeDRMoveDo();
               break;
            case 42:
               ringNode = new RingNodeDRYawSet();
               break;
            case 46:
               ringNode = new RingNodeDRYawFollow();
               break;
            case 48:
               ringNode = new RingNodeDRMoveTask();
               break;
            case 50:
               ringNode = new RingNodeDRChannelClear();
               break;
            case 52:
               ringNode = new RingNodeDRChangeMode();
               break;
            case 54:
               ringNode = new RingNodeDRUseKey();
               break;
            case 56:
               ringNode = new RingNodeDRUseItem();
               break;
            case 58:
               ringNode = new RingNodeDRSearch();
               break;
            case 60:
               ringNode = new RingNodeDRTargetSet();
               break;
            case 62:
               ringNode = new RingNodeDRAttracter();
               break;
            case 64:
               ringNode = new RingNodeDREffectDo();
               break;
            case 70:
               ringNode = new RingNodeTGKey();
               break;
            case 72:
               ringNode = new RingNodeTGItem();
               break;
            case 74:
               ringNode = new RingNodeTGSeat();
               break;
            case 76:
               ringNode = new RingNodeTGPlayer();
               break;
            case 78:
               ringNode = new RingNodeTGUnitMove();
               break;
            case 80:
               ringNode = new RingNodeTGUnitState();
               break;
            case 84:
               ringNode = new RingNodeTGChannelState();
               break;
            case 86:
               ringNode = new RingNodeTGSensor();
               break;
            case 88:
               ringNode = new RingNodeTGTarget();
               break;
            case 90:
               ringNode = new RingNodeTGEntity();
               break;
            case 92:
               ringNode = new RingNodeTGBlock();
               break;
            case 94:
               ringNode = new RingNodeTGParts();
         }

         ((AbstractRingNode)ringNode).readNodeData(inn);
      } catch (Exception var4) {
         var4.printStackTrace();
         if (ringNode == null) {
            ringNode = new RingNodeSTEmpty();
         }
      }

      return (AbstractRingNode)ringNode;
   }

   public int getAuthNeeds() {
      return 0;
   }

   protected static int lastIdxOf(int[] typeConvAry) {
      return typeConvAry.length - 1;
   }

   public static int convertToTypeValue(int[] typeConvAry, double indexValueDouble) {
      int indexValue = (int)indexValueDouble;
      return indexValue >= 0 && indexValue < typeConvAry.length ? typeConvAry[indexValue] : typeConvAry[0];
   }

   public static int convertToTypeIndex(int[] typeConvAry, short value) {
      for(int i = 0; i < typeConvAry.length; ++i) {
         if (value == typeConvAry[i]) {
            return i;
         }
      }

      return 0;
   }

   public static short booleanValue(boolean value) {
      return (short)(value ? 1 : 0);
   }

   protected ActiveRingSlots.SlotType slotTypeNumToEnum(int slotTypeNum) {
      switch (slotTypeNum) {
         case 10:
         default:
            return ActiveRingSlots.SlotType.motion;
         case 20:
            return ActiveRingSlots.SlotType.action;
         case 30:
            return ActiveRingSlots.SlotType.move;
         case 40:
            return ActiveRingSlots.SlotType.input;
      }
   }

   public void onProgramInit(RingProgram program, RingProgramPage page) {
   }

   public byte ringRun(EntityJointed ej, RingProgram program, RingProgramPage page) {
      return program.nextNodeIndex(page);
   }

   public double getReturnValue(RingProgram program, RingProgramPage page) {
      return 0.0;
   }

   public final boolean returnStateBool(RingProgram program, RingProgramPage page) {
      return (int)this.getReturnValue(program, page) != 0;
   }

   public boolean hasVectorOutput(RingProgram program, RingProgramPage page) {
      return false;
   }

   public double getArrayValue(RingProgram program, RingProgramPage page, int index) {
      return this.getReturnValue(program, page);
   }

   protected void writeArrayData(DataBufferableOutput out, double[] data) throws IOException {
      if (data == null) {
         out.writeBoolean(false);
      } else {
         out.writeBoolean(true);

         for(int i = 0; i < 4; ++i) {
            out.writeDouble(data[i]);
         }
      }

   }

   protected double[] readArrayData(DataBufferableInput inn) throws IOException {
      boolean hasArrayData = inn.readBoolean(false);
      if (!hasArrayData) {
         return null;
      } else {
         double[] arrayValues = ensureArrayValues((double[])null);

         for(int i = 0; i < 4; ++i) {
            arrayValues[i] = inn.readDouble(0.0);
         }

         return arrayValues;
      }
   }

   protected static double[] ensureArrayValues(double[] arrayValues) {
      if (arrayValues == null) {
         arrayValues = new double[4];
      }

      return arrayValues;
   }

   protected double fetchEachVectorValue(RingProgram program, RingProgramPage page, ReffableDoubleValueEntry valueEntry, int index) {
      if (valueEntry.isNodeRefMode) {
         AbstractRingNode node = valueEntry.getReferringNode(page, (int)valueEntry.value);
         return node == null ? Double.NaN : node.getArrayValue(program, page, index);
      } else {
         return valueEntry.value;
      }
   }

   protected double fetchEachVectorValue(RingProgram program, RingProgramPage page, ReffableIntValueEntry valueEntry, int index) {
      if (valueEntry.isNodeRefMode) {
         AbstractRingNode node = valueEntry.getReferringNode(page, valueEntry.value);
         return node == null ? Double.NaN : node.getArrayValue(program, page, index);
      } else {
         return (double)valueEntry.value;
      }
   }

   public boolean isPageTransition(RingProgram program, RingProgramPage curPage) {
      return false;
   }

   public short getTransitionPageIndex(RingProgram program, RingProgramPage curPage) {
      return 0;
   }

   public boolean processTransition(EntityJointed ej, RingProgram program, RingProgramPage curPage, RingProgramPage nextPage, byte cNodeIndex, short nextPageIndex) {
      program.setError(RingProgram.ErrorType.unKnownError);
      return false;
   }

   public boolean hasRefEntry() {
      return this.getRefCount() > 0;
   }

   public boolean isRefEntryDisabled() {
      return false;
   }

   public int getRefCount() {
      return 0;
   }

   public int getRefEntryNum(int refNum) {
      return -1;
   }

   public String getRefEntryName(int refNum) {
      return null;
   }

   public int getRefNodeIndex(int refNum) {
      int entryNum = this.getRefEntryNum(refNum);
      AbstractReffableValueEntry tglEntry = this.getTogglableRefEntry(entryNum);
      return tglEntry != null && !tglEntry.isNodeRefMode ? -1 : (int)this.getValue(entryNum);
   }

   public void setRefNodeIndex(int refNum, int value) {
      int entryNum = this.getRefEntryNum(refNum);
      AbstractReffableValueEntry tglEntry = this.getTogglableRefEntry(entryNum);
      if (tglEntry != null) {
         tglEntry.setToRefModeAndSetIndex(value);
      } else {
         this.setValue(entryNum, (double)value);
      }

   }

   public boolean isRangeRef() {
      return false;
   }

   public int getRefLineColor(int refNum) {
      return 16734296;
   }

   public boolean isRefLineTowardsThis(int refNum) {
      return true;
   }

   public boolean isCurvedLine(int refNum) {
      return false;
   }

   public boolean canBeLinedWith(int refNum, AbstractRingNode node) {
      return true;
   }

   public int getCurrentLayingRefNum() {
      return this.gui.refSetCnt % this.getRefCount();
   }

   public void clearRefSetCnt() {
      if (this.gui != null) {
         this.gui.refSetCnt = 0;
      }

   }

   public boolean isNotInRefMode(int refNum) {
      AbstractReffableValueEntry tglEntry = this.getTogglableRefEntry(this.getRefEntryNum(refNum));
      if (tglEntry != null) {
         return !tglEntry.isNodeRefMode;
      } else {
         return false;
      }
   }

   public boolean isTogglableEntryNodeRef(int entryNum) {
      AbstractReffableValueEntry tglEntry = this.getTogglableRefEntry(entryNum);
      return tglEntry != null ? tglEntry.isNodeRefMode : false;
   }

   public void toggleEntryType(int entryNum) {
      AbstractReffableValueEntry tglEntry = this.getTogglableRefEntry(entryNum);
      if (tglEntry != null) {
         tglEntry.toggleMode();
      }

   }

   public AbstractReffableValueEntry getTogglableRefEntry(int entryNum) {
      return null;
   }

   public void setGuiEntryState(JBGuiSettingsImpl gs) {
   }

   public AbstractRingNode withGui() {
      if (this.gui == null) {
         this.gui = new RingGui();
      }

      if (this.hasRefEntry()) {
         this.gui.refNodes = new AbstractRingNode[this.getRefCount()];
      }

      return this;
   }

   @OnlyIn(Dist.CLIENT)
   public void setGuiDraggingPos(int mouseX, int mouseY) {
      if (this.gui != null) {
         this.gui.draggingX = mouseX;
         this.gui.draggingY = mouseY;
         if (this instanceof GuiRingNodeMultiHandle) {
            GuiRingNodeMultiHandle thisMult = (GuiRingNodeMultiHandle)this;
            int mIdx = 0;

            for(int len = thisMult.multiNodes.size(); mIdx < len; ++mIdx) {
               AbstractRingNode each = (AbstractRingNode)thisMult.multiNodes.get(mIdx);
               if (each != this) {
                  each.setGuiDraggingPos(mouseX, mouseY);
               }
            }
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public void setGuiDrawPos(float argX, float argY) {
      if (this.gui != null) {
         this.gui.drawX = argX;
         this.gui.drawY = argY;
         if (this instanceof GuiRingNodeMultiHandle) {
            GuiRingNodeMultiHandle thisMult = (GuiRingNodeMultiHandle)this;
            int mIdx = 0;

            for(int len = thisMult.multiNodes.size(); mIdx < len; ++mIdx) {
               AbstractRingNode each = (AbstractRingNode)thisMult.multiNodes.get(mIdx);
               if (each != this) {
                  each.setGuiDrawPos(argX, argY);
               }
            }
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public boolean allowDecimalInput(int entryNum) {
      AbstractReffableValueEntry tglEntry = this.getTogglableRefEntry(entryNum);
      if (tglEntry instanceof ReffableDoubleValueEntry) {
         return !tglEntry.isNodeRefMode;
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public double getDisplayValue(int entryNum) {
      return this.allowDecimalInput(entryNum) ? this.getValue(entryNum) : (double)((int)this.getValue(entryNum));
   }

   @OnlyIn(Dist.CLIENT)
   public int getRefCharIndex(int refNum) {
      return refNum;
   }

   @OnlyIn(Dist.CLIENT)
   public float getRefCharPos(int refNum) {
      return 0.25F;
   }

   @OnlyIn(Dist.CLIENT)
   public float getIconU() {
      return 368.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public float getIconV() {
      return 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public int getIconSize() {
      return 16;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean hasOverlayIcon() {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public float getOverlayIconU() {
      return 368.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public float getOverlayIconV() {
      return 0.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public int getOverlayIconSize() {
      return 16;
   }

   @OnlyIn(Dist.CLIENT)
   public String getNodeName() {
      return null;
   }

   @OnlyIn(Dist.CLIENT)
   public String getNodeDesc() {
      return null;
   }

   @OnlyIn(Dist.CLIENT)
   public String getReturnValueDesc() {
      return CompatHelper.formatLang("jbring.cmn.rvaldesc.alwaysfalse");
   }

   @OnlyIn(Dist.CLIENT)
   public void getTip(List list, boolean showValues) {
      if (!showValues) {
         String nName = this.getNodeName();
         if (nName != null) {
            list.add(ChatFormatting.LIGHT_PURPLE + nName);
         }

         if ((this.getAuthNeeds() & 131072) != 0) {
            list.addAll(this.addPrefixToAll(Arrays.asList(CompatHelper.formatLang("jbring.cmn.overpower.desc").split(";;")), ChatFormatting.RED));
         }

         String nDesc = this.getNodeDesc();
         if (nDesc != null) {
            list.addAll(Arrays.asList(nDesc.split(";;")));
         }

         if (this instanceof AbstractActivatableRingNode) {
            list.addAll(Arrays.asList(CompatHelper.formatLang("jbring.cmn.activedesc").split(";;")));
         }

         if (this instanceof AbstractRealTimeStateNode) {
            list.addAll(Arrays.asList(CompatHelper.formatLang("jbring.cmn.realtimedesc").split(";;")));
         }

         if (this instanceof INodeValueSettable) {
            list.addAll(this.addPrefixToAll(Arrays.asList(CompatHelper.formatLang("jbring.cmn.inputdesc").split(";;")), ChatFormatting.GRAY));
         }

         String rDesc = this.getReturnValueDesc();
         if (rDesc != null) {
            list.addAll(this.addPrefixToAll(Arrays.asList(rDesc.split(";;")), ChatFormatting.GRAY));
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   private List addPrefixToAll(List<String> list, ChatFormatting prefix) {
      int i = 0;

      for(int len = list.size(); i < len; ++i) {
         list.set(i, prefix + (String)list.get(i));
      }

      return list;
   }

   @OnlyIn(Dist.CLIENT)
   public String getDisplayEntryTitleStr(RingProgram program, RingProgramPage currentPage, int entryNum, EntrySetting setting) {
      String str = this.getEntryTitleFormattedStr(program, currentPage, entryNum);
      if (str == null) {
         if (setting.title != null) {
            str = setting.title.format(this.getDisplayValue(entryNum));
         } else {
            str = CompatHelper.formatLang(setting.titleStr);
         }
      }

      return str;
   }

   @OnlyIn(Dist.CLIENT)
   public String getDisplayEntryValueStr(RingProgram program, RingProgramPage currentPage, int entryNum, EntrySetting setting) {
      String str = this.getEntryValueFormattedStr(program, currentPage, entryNum);
      if (str == null) {
         if (setting.format != null) {
            str = setting.format.format(new Object[]{this.getDisplayValue(entryNum)});
         } else {
            str = CompatHelper.formatLang(setting.formatStr, (int)this.getDisplayValue(entryNum));
         }
      }

      return str;
   }

   @OnlyIn(Dist.CLIENT)
   protected String getEntryTitleFormattedStr(RingProgram program, RingProgramPage page, int entryNum) {
      return null;
   }

   @OnlyIn(Dist.CLIENT)
   protected String getEntryValueFormattedStr(RingProgram program, RingProgramPage page, int entryNum) {
      return null;
   }

   public static class RingGui {
      public boolean hasInitAngle = false;
      public double angleOnRing = 0.0;
      public double angleOnRingPrev = 0.0;
      public int mouseDownX;
      public int mouseDownY;
      public int draggingX;
      public int draggingY;
      public int draggingOnRingIndex;
      public int originalIndex;
      public float drawX;
      public float drawY;
      public RingProgramPage overPagePageRef;
      public AbstractRingNode overPageNodeRef;
      public AbstractRingNode[] refNodes;
      public int refSetCnt;
      public int dbgActivated;
   }
}
