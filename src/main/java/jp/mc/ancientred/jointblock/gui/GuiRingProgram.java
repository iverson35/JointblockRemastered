package jp.mc.ancientred.jointblock.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jp.mc.ancientred.jointblock.assemble.EntrySetting;
import jp.mc.ancientred.jointblock.assemble.ring.AbstractRingNode;
import jp.mc.ancientred.jointblock.assemble.ring.GuiRingNodeMultiHandle;
import jp.mc.ancientred.jointblock.assemble.ring.IOverPageLinker;
import jp.mc.ancientred.jointblock.assemble.ring.NodeCloneHelper;
import jp.mc.ancientred.jointblock.assemble.ring.RingNodeSTRingCall;
import jp.mc.ancientred.jointblock.assemble.ring.RingProgram;
import jp.mc.ancientred.jointblock.assemble.ring.RingProgramPage;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.common.JBCommonUtil;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonJB;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonRed;
import jp.mc.ancientred.jointblock.gui.widget.GuiButtonVector;
import jp.mc.ancientred.jointblock.gui.widget.GuiCheckBoxNoShadow;
import jp.mc.ancientred.jointblock.gui.widget.GuiScreenClosable;
import jp.mc.ancientred.jointblock.gui.widget.GuiTextFieldEx;
import jp.mc.ancientred.jointblock.gui.widget.GuiTextFieldJP;
import jp.mc.ancientred.jointblock.gui.widget.GuiVerticalSlider;
import jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget;
import jp.mc.ancientred.jointblock.item.ItemRingProgram;
import jp.mc.ancientred.jointblock.packet.JBPacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;

/**
 * 1.12.2 {@code GuiRingProgram} - the ring program editor (pages / ring nodes / entry values).
 *
 * <p>Rendering: the 1.12.2 screen emitted immediate-mode {@code POSITION_COLOR} geometry with
 * texturing disabled ({@code GlStateManager.func_179090_x}) through the {@code Tessellator}.  This
 * port keeps the same geometry, constants, state-selection and draw order and maps each GL call to
 * the closest 1.20.1 API:
 * <ul>
 *   <li>{@code GL_LINE_STRIP} / {@code GL_LINES} with {@code GL11.glLineWidth(w)} -&gt;
 *       {@code RenderType.debugLineStrip(w)} (a {@code POSITION_COLOR}/{@code DEBUG_LINE_STRIP}
 *       render type whose {@code LineStateShard} carries the width) plus a {@code VertexConsumer}
 *       taken from {@code GuiGraphics.bufferSource()} and a {@code GuiGraphics.flush()} where the
 *       1.12.2 code called {@code Tessellator.func_78381_a()}.  {@code debugLineStrip} is a
 *       <em>connected</em> strip, so the 1.12.2 {@code GL_LINES} batches
 *       ({@link #drawSimpleLine}, {@link #drawArrowedLine}) are emitted as one 2-vertex strip per
 *       segment with a flush in between (the same lines, without the connecting segments a strip
 *       would add).</li>
 *   <li>{@code GL_TRIANGLE_STRIP} / {@code GL_TRIANGLE_FAN} fills ({@link #drawFilledRing},
 *       {@link #drawFilledCircle}) -&gt; {@code RenderType.debugQuads()}, emitting the same
 *       vertices as quads (the strip's degenerate vertices collapse to identical triangles).</li>
 *   <li>textured {@code Tessellator} quads -&gt; {@code GuiGraphics.blit(ResourceLocation, ...)}
 *       with the same 384x256 atlas sub-rect and destination size; the float source positions are
 *       kept sub-pixel exact via a fractional pose translate.</li>
 *   <li>{@code GlStateManager.func_179147_l}/{@code func_179084_k} (blend) -&gt;
 *       {@code RenderSystem.enableBlend()} / {@code defaultBlendFunc()} /
 *       {@code disableBlend()}.</li>
 *   <li>the 1.12.2 z values (308/310) collapse to 0: 1.20.1 draws these in screen space and orders
 *       them by flush order (the debug render types use LEQUAL depth).</li>
 *   <li>the 1.12.2 lighting/rescale-normal/disable-texture/enable-texture calls and
 *       {@code RenderHelper.func_74518_a}/{@code func_74519_b} have no 1.20.1 equivalent and are
 *       dropped; widgets and render types manage their own state.</li>
 *   <li>1.20.1 (Forge 47.4.0) {@code Screen.render} draws only the renderables and no longer
 *       renders the default background, so {@link #render} calls
 *       {@code renderBackground(guiGraphics)} explicitly for the 1.12.2
 *       {@code func_146276_q_()} step; the tooltips use
 *       {@code GuiGraphics.renderComponentTooltip} instead of {@code drawHoveringText}.</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class GuiRingProgram extends GuiScreenClosable {
   public static final ResourceLocation resource = new ResourceLocation("jointblock:textures/gui/guiringprogram.png");
   public static final double PI_X2 = 6.283185307179586;
   public static final double PI_D2 = 1.5707963267948966;
   private static final float BEZIER_STEPS = 0.001F;
   private static final float TEX_XSIZE = 384.0F;
   private static final float TEX_YSIZE = 256.0F;
   public static final int RING_RADIUS = 80;
   public static final int NODE_ICON_SIZE = 16;
   private static final int ON_RING_TORELANCE = 16;
   private static final int NODETYPELIST_COLS = 2;
   private static final int NODETYPELIST_SPAN = 16;
   private static final int NODETYPELIST_TB_MERGIN = 1;
   private static final int NODETYPELIST_VIEW_ROW_MAX = 8;
   private static final int NODETYPELIST_SLIDER_SHIFT = 13;
   private static final int MAX_VIEW_ENTRY_NUM = 6;
   private static final int AVAILABLE_ENTRY_NUM = 10;
   private static final int PAGE_JUMP_HISTORY_MAX = 20;
   private static final int COPY_DEL_LIST_CPY1IDX = 0;
   private static final int COPY_DEL_LIST_DEL1IDX = 1;
   private static final int[] COPY_DEL_LIST_YTOP = new int[]{46, 69};
   private static int SET_LARGE_AMOUNT = 100;
   private static final int NUMTEXT_MIN = -32768;
   private static final int NUMTEXT_MAX = 32767;
   private static final int LEFT_MOUSE_BTN = 0;
   private static final int RIGHT_MOUSE_BTN = 1;
   private static final double NODE_RENDER_Z = 310.0;
   private static final double NODE_LINE_RENDER_Z = 308.0;
   public static final double BG_RING_WIDTH = 10.0;
   public static final int BG_RING_SEGMENTS = 20;
   public static final int BG_RING_COLOR = 5916487;
   public static final int CENTER_PAGEBTN_RADIUS = 15;
   public static final int NODE_HIGHLIGHT_COLOR = 8355635;
   public static final int NODE_ERROR_COLOR = 13120307;
   private static final int NODE_INDEX_COLOR = 10066329;
   private static final int VECTOR_BTN_NCOLOR = 8421504;
   private static final int VECTOR_BTN_SCOLOR = 8421576;
   private static final int VECTOR_BTN_DCOLOR = 5916487;
   private static final int ENTRY_EDIT_TITLE_TOP = 24;
   private static final int ENTRY_TOP = 42;
   private static final int ENTRY_LEFT = 243;
   private static final int ENTRY_BTN_LEFT = 325;
   private static final int ENTRY_ROW_HIGHT = 25;
   private static final int ENTRY_AREA_WIDTH_A = 132;
   private static final int RING_SETTING_TITLE_TOP = 90;
   private static final String REF = "R";
   private static final int REF_COLOR = 8947967;
   private static final String FIX = "F";
   private static final int FIX_COLOR = 8947848;
   /** GLFW key codes of the 1.12.2 LWJGL codes this screen used (1.12: 28, 30, 46, 45, 211). */
   private static final int KEY_ENTER = 257;
   private static final int KEY_KP_ENTER = 335;
   private static final int KEY_A = 65;
   private static final int KEY_C = 67;
   private static final int KEY_X = 88;
   private static final int KEY_DELETE = 261;
   protected int xSize = 404;
   protected int ySize = 222;
   protected int guiLeft;
   protected int guiTop;
   private double[] TIPWORK = new double[4];
   public static final int ButtonAddPageID = 1;
   public static final int ButtonWithoutSaveID = 2;
   public static final int ButtonDeletePageID = 3;
   public static final int ButtonConfirmYesID = 4;
   public static final int ButtonConfirmNoID = 5;
   public static final int ButtonPagePrevID = 6;
   public static final int ButtonPageNextID = 7;
   public static final int ButtonPageBackID = 8;
   public static final int ButtonCenterID = 9;
   public static final int ButtonRModAddID = 20;
   public static final int ButtonRModSubID = 22;
   public static final int ButtonRTglID = 24;
   public static final int ButtonREditID = 26;
   public static final int CheckBoxRToggleID = 28;
   public static final int TexEditID = 30;
   public static final int CheckBoxTypeCycleID = 40;
   public static final int CheckBoxTypeShotID = 42;
   public static final int CheckBoxRefInnLineID = 44;
   public static final int CheckBoxRefOutLineID = 46;
   public static final int CheckBoxNodeIndexID = 47;
   public static final int CheckBoxLockDelID = 48;
   public static final int CheckBoxAuthMotID = 60;
   public static final int CheckBoxAuthActID = 62;
   public static final int CheckBoxAuthMovID = 64;
   public static final int CheckBoxAuthInpID = 66;
   public static final int CheckBoxAuthItmID = 68;
   public static final int CheckBoxOpCutID = 80;
   public static final int ButtonRMODNUM = 1000;
   public static final int TexJPID = 97;
   public static final int SliderEntryID = 98;
   public static final int SliderNodeListID = 99;
   private GuiButtonJB ButtonAddPage;
   private GuiButtonJB ButtonWithoutSave;
   private GuiButtonJB ButtonDeletePage;
   private GuiButtonJB ButtonConfirmYes;
   private GuiButtonJB ButtonConfirmNo;
   private GuiButtonJB ButtonPagePrev;
   private GuiButtonJB ButtonPageNext;
   private GuiButtonJB ButtonPageBack;
   private GuiButtonJB ButtonCenter;
   private GuiTextFieldJP TextFieldPageName;
   private GuiCheckBoxNoShadow CheckBoxTypeCycle;
   private GuiCheckBoxNoShadow CheckBoxTypeShot;
   private GuiCheckBoxNoShadow CheckBoxRefInnLine;
   private GuiCheckBoxNoShadow CheckBoxRefOutLine;
   private GuiCheckBoxNoShadow CheckBoxNodeIndex;
   private GuiCheckBoxNoShadow CheckBoxLockDel;
   private GuiVerticalSlider SliderEntry;
   private GuiVerticalSlider SliderNodeList;
   private GuiCheckBoxNoShadow CheckBoxAuthMot;
   private GuiCheckBoxNoShadow CheckBoxAuthAct;
   private GuiCheckBoxNoShadow CheckBoxAuthMov;
   private GuiCheckBoxNoShadow CheckBoxAuthInp;
   private GuiCheckBoxNoShadow CheckBoxOpCut;
   /** 1.12.2 {@code GuiButton[][]}; the ref/state toggle cell (index 4) is a GuiCheckBoxNoShadow. */
   private AbstractWidget[][] buttonAry;
   private GuiTextFieldEx[] textFiledAry;
   private boolean[] showTextFieldFlgAry;
   private boolean[] bypathTextFieldClick;
   private String buttonPagePrevAStr;
   private String buttonPageNextAStr;
   private String buttonPagePrevBStr;
   private String buttonPageNextBStr;
   private JBGuiSettingsImpl guiEntrySettings;
   private boolean isRingSettingEdtMode = true;
   private Player player;
   private int itemIndex;
   private boolean closeWithoutSave = false;
   private RingProgram program;
   private int currentPageIndex = -1;
   private String currentPageIndexStr;
   private RingProgramPage currentPage;
   public ArrayList<AbstractRingNode> nodeTypeList;
   public static AbstractRingNode[] copy_DelList = new AbstractRingNode[2];
   public int[] copyDelAnimTick = new int[2];
   private static final int COPY_DEL_ANIM_TICK_START = 3;
   private static boolean delLock;
   private boolean showNodeTip = false;
   public int ringRadius = 80;
   public int initAnimTick;
   public int initAnimTickPrev;
   private static final int TOTAL_INITANIM_TICK = 5;
   public AbstractRingNode mouseDownNode = null;
   public AbstractRingNode draggingNode = null;
   public AbstractRingNode selectedNodePrev = null;
   public AbstractRingNode lineFromNode = null;
   public ArrayList<String> listTooltip = new ArrayList<>();
   public LinkedList<RingProgramPage> pageJumpHistory = new LinkedList<>();
   private BezierHelper bezierHelper = new BezierHelper(1);
   /**
    * Stand-in for the 1.12.2 GL line width state ({@code GL11.glLineWidth}); it is baked into the
    * {@code RenderType.debugLineStrip(width)} of every line draw.
    */
   private float currentLineWidth = 0.5F;
   private NodeCloneHelper cloneHelper = new NodeCloneHelper(50);
   private DecimalFormat formatNumText;
   public int selectedFirst = -1;
   public int selectedFrom = -1;
   public int selectedTo = -1;
   private int confirmBtnId = -1;
   private int confirmBtnIdPend = -1;
   private String confirmMessage;
   private String errorMessage;

   private int getRingCenterX() {
      return (this.width - this.xSize) / 2 + 148;
   }

   private int getRingCenterY() {
      return (this.height - this.ySize) / 2 + 123;
   }

   private int getTypeListLeftX() {
      return (this.width - this.xSize) / 2 + 21;
   }

   private int getTypeListTopY() {
      return (this.height - this.ySize) / 2 + 86;
   }

   public GuiRingProgram(Player par1Player, ItemStack itemStack, int itemIndex) {
      this.player = par1Player;
      this.itemIndex = itemIndex;
      // 1.12.2 set GuiScreen.field_146291_p (allowUserInput) = false here; that flag no longer
      // exists in 1.20.1 (screen input is always dispatched to the widgets first), same note as
      // GuiContainerWithTip.
      this.formatNumText = new DecimalFormat("#.####");
      this.initAnimTickPrev = this.initAnimTick = 5;
      byte[] rpData = ItemRingProgram.getRingProgramData(itemStack);
      if (rpData != null) {
         this.program = RingProgram.readFromByteArray(rpData);
      } else {
         this.program = RingProgram.createNewRingProgram(true);
      }

      if (this.program.hadError) {
         this.program = RingProgram.createNewRingProgram(true);
         this.errorMessage = CompatHelper.formatLang("jbring.saveres.rebuilderr");
      }

      if (this.program.pageList.isEmpty()) {
         this.program.pageList.add(RingProgramPage.createNewRingProgramPage(true));
      }

      for (int p = 0; p < this.program.pageList.size(); ++p) {
         this.program.pageList.get(p).ensureGUI();
      }

      this.setCurrentPage(0);
      this.nodeTypeList = new ArrayList<>();
      this.setupNodeTypeList(this.nodeTypeList, 2);
      this.setupNodeTypeList(this.nodeTypeList, 0);
      this.setupNodeTypeList(this.nodeTypeList, 1);
      this.guiEntrySettings = new JBGuiSettingsImpl(10);
   }

   private boolean setCurrentPage(int pageIndex) {
      if (pageIndex >= 0 && pageIndex < this.program.pageList.size()) {
         boolean pageChanged = pageIndex != this.currentPageIndex;
         this.currentPage = this.program.pageList.get(pageIndex);
         this.currentPageIndex = pageIndex;
         this.currentPageIndexStr = "P:" + this.currentPageIndex;
         if (pageChanged) {
            this.isRingSettingEdtMode = true;
            this.clearSelection();
            if (this.TextFieldPageName != null) {
               this.TextFieldPageName.setValue(this.currentPage.getPageName());
            }

            return true;
         }
      }

      return false;
   }

   private void setupNodeTypeList(ArrayList<AbstractRingNode> list, int nodeType) {
      AbstractRingNode[] nodetypelist = AbstractRingNode.NODETYPELIST[nodeType];
      int added = 0;

      int i;
      for (i = 0; i < nodetypelist.length; ++i) {
         list.add(nodetypelist[i].newInstance().withGui());
         ++added;
      }

      added = 2 - added % 2;
      if (added != 2) {
         for (i = 0; i < added; ++i) {
            list.add(null);
         }
      }
   }

   @Override
   public void removed() {
      // 1.12.2 Keyboard.enableRepeatEvents(false): the API is gone in 1.20.1 (screens always get
      // repeated key events), so there is no call to make here.
      if (!this.closeWithoutSave) {
         this.saveAndSendRingData();
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void init() {
      super.init();
      int xLeft = this.guiLeft = (this.width - this.xSize) / 2;
      int yTop = this.guiTop = (this.height - this.ySize) / 2;
      int buttonWidth = 20;
      int buttonHeight = 20;
      String empty = "";
      int ringCenterX = this.getRingCenterX();
      int ringCenterY = this.getRingCenterY();
      this.ButtonAddPage = new GuiButtonJB(1, xLeft + 7, yTop + 18, 48, buttonHeight, CompatHelper.formatLang("jointGui.info.newpage"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonAddPage);
      int widthRem = 152;
      int padBetween = 2;
      int downRightBtnsY = yTop + this.ySize - buttonHeight - 5;
      this.ButtonConfirmYes = new GuiButtonRed(4, xLeft + 256 - 12, downRightBtnsY, 32, buttonHeight, CompatHelper.formatLang("jointGui.info.confirmyes"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonConfirmYes);
      widthRem -= this.ButtonConfirmYes.getWidth() + padBetween;
      this.ButtonConfirmNo = new GuiButtonRed(5, this.ButtonConfirmYes.getX() + this.ButtonConfirmYes.getWidth() + padBetween, downRightBtnsY, 32, buttonHeight, CompatHelper.formatLang("jointGui.info.confirmno"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonConfirmNo);
      padBetween = 15;
      widthRem -= this.ButtonConfirmNo.getWidth() + padBetween;
      this.ButtonWithoutSave = new GuiButtonRed(2, this.ButtonConfirmNo.getX() + this.ButtonConfirmNo.getWidth() + padBetween, downRightBtnsY, widthRem, buttonHeight, CompatHelper.formatLang("jointGui.info.canceledit"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonWithoutSave);
      int delBtnWidth = 60;
      this.ButtonDeletePage = new GuiButtonRed(3, xLeft + this.xSize - delBtnWidth - 9, yTop + 20, delBtnWidth, buttonHeight, CompatHelper.formatLang("jointGui.info.delpage"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonDeletePage);
      this.buttonPagePrevAStr = CompatHelper.formatLang("jointGui.info.bindprev");
      this.buttonPageNextAStr = CompatHelper.formatLang("jointGui.info.bindnext");
      this.buttonPagePrevBStr = CompatHelper.formatLang("jointGui.info.toFirst");
      this.buttonPageNextBStr = CompatHelper.formatLang("jointGui.info.toLast");
      this.ButtonPagePrev = new GuiButtonVector(6, ringCenterX - this.ringRadius - 9, yTop + 18, 26, buttonHeight, 8421504, 8421576, 5916487, this.createArrowL(), this.buttonPagePrevAStr, this::actionPerformed);
      this.addRenderableWidget(this.ButtonPagePrev);
      this.ButtonPageNext = new GuiButtonVector(7, ringCenterX + this.ringRadius + 9 - 26, yTop + 18, 26, buttonHeight, 8421504, 8421576, 5916487, this.createArrowR(), this.buttonPageNextAStr, this::actionPerformed);
      this.addRenderableWidget(this.ButtonPageNext);
      this.ButtonPageBack = new GuiButtonVector(8, ringCenterX - this.ringRadius - 9, yTop + 18 + 22, 26, buttonHeight, 8421504, 8421576, 5916487, this.createArrowL(), CompatHelper.formatLang("jointGui.info.pageback"), this::actionPerformed);
      this.addRenderableWidget(this.ButtonPageBack);
      this.ButtonCenter = new GuiButtonVector(9, ringCenterX - 15, ringCenterY - 15, 30, 30, 8421504, 8421576, 5916487, this.createRingC(15.0F, 20), empty, this::actionPerformed);
      this.addRenderableWidget(this.ButtonCenter);
      int checkBoxLeft = xLeft + 243 + 15;
      this.CheckBoxTypeCycle = new GuiCheckBoxNoShadow(40, checkBoxLeft, yTop + 90 + 25, CompatHelper.formatLang("jointGui.info.typecycle"), this.program.isExecuteTypeCycle(), 15658734, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxTypeCycle);
      this.CheckBoxTypeShot = new GuiCheckBoxNoShadow(42, xLeft + 243 + 75, yTop + 90 + 25, CompatHelper.formatLang("jointGui.info.typeshot"), this.program.isExecuteTypeShot(), 15658734, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxTypeShot);
      int yPos = yTop + 90 + 50;
      int xSpan = 40;
      this.CheckBoxAuthMot = new GuiCheckBoxNoShadow(60, checkBoxLeft + 0 * xSpan, yPos, "Mot", this.program.isAutMotOK(), 15658734, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxAuthMot);
      this.CheckBoxAuthAct = new GuiCheckBoxNoShadow(62, checkBoxLeft + 1 * xSpan, yPos, "Act", this.program.isAutActOK(), 15658734, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxAuthAct);
      yPos += 10;
      this.CheckBoxAuthMov = new GuiCheckBoxNoShadow(64, checkBoxLeft + 0 * xSpan, yPos, "Mov", this.program.isAutMovOK(), 15658734, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxAuthMov);
      this.CheckBoxAuthInp = new GuiCheckBoxNoShadow(66, checkBoxLeft + 1 * xSpan, yPos, "Key", this.program.isAutInpOK(), 15658734, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxAuthInp);
      this.CheckBoxOpCut = new GuiCheckBoxNoShadow(80, checkBoxLeft, yTop + 90 + 85, CompatHelper.formatLang("jbring.cmn.opcut"), this.program.isOPCut(), 15658734, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxOpCut);
      this.CheckBoxNodeIndex = new GuiCheckBoxNoShadow(47, xLeft + 56, yTop + this.ySize - 11 - 7, "IDX", false, 10066329, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxNodeIndex);
      this.CheckBoxRefOutLine = new GuiCheckBoxNoShadow(46, xLeft + 56, this.CheckBoxNodeIndex.getY() - this.CheckBoxNodeIndex.getHeight(), "OUT", true, 3407667, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxRefOutLine);
      this.CheckBoxRefInnLine = new GuiCheckBoxNoShadow(44, xLeft + 56, this.CheckBoxRefOutLine.getY() - this.CheckBoxRefOutLine.getHeight(), "IN", true, 16734296, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxRefInnLine);
      this.CheckBoxLockDel = new GuiCheckBoxNoShadow(48, xLeft + 8, yTop + 74, "", delLock, 0, this::actionPerformed);
      this.addRenderableWidget(this.CheckBoxLockDel);
      this.SliderNodeList = new GuiVerticalSlider(99, this.getTypeListLeftX() - 13, this.getTypeListTopY(), 10, 130, 0.0, 0.0, 0.0, this::actionPerformed);
      this.addRenderableWidget(this.SliderNodeList);
      int eSliderTop = 41;
      int eSliderBottom = 194;
      this.SliderEntry = new GuiVerticalSlider(98, xLeft + this.xSize - 8 - 10, yTop + eSliderTop, 10, eSliderBottom - eSliderTop + 1, 0.0, 0.0, 0.0, this::actionPerformed);
      this.addRenderableWidget(this.SliderEntry);
      int maxEnabledRow = (this.nodeTypeList.size() + 1) / 2 - 1;
      maxEnabledRow -= 7;
      if (maxEnabledRow < 0) {
         maxEnabledRow = 0;
      }

      this.SliderNodeList.maxValue = (double)maxEnabledRow / 8.0;
      this.SliderNodeList.minValue = 0.0;
      int buttonShiftH = buttonWidth + 1;
      int buttonXLeft = xLeft + 325;
      int buttonRowY = yTop + 42 + 5;
      this.buttonAry = new AbstractWidget[6][];
      this.textFiledAry = new GuiTextFieldEx[6];

      int i;
      for (i = 0; i < this.buttonAry.length; ++i) {
         int idAdd = i * 1000;
         this.buttonAry[i] = new AbstractWidget[5];
         this.buttonAry[i][0] = new GuiButtonJB(22 + idAdd, buttonXLeft + buttonShiftH * 0, buttonRowY, buttonWidth, buttonHeight, empty, this::actionPerformed);
         this.addRenderableWidget(this.buttonAry[i][0]);
         this.buttonAry[i][2] = new GuiButtonJB(24 + idAdd, buttonXLeft + buttonShiftH * 1, buttonRowY - 2, buttonWidth, buttonHeight - 9, empty, this::actionPerformed);
         this.addRenderableWidget(this.buttonAry[i][2]);
         this.buttonAry[i][3] = new GuiButtonJB(26 + idAdd, buttonXLeft + buttonShiftH * 1, buttonRowY + 10, buttonWidth, buttonHeight - 9, "Edit", this::actionPerformed);
         this.addRenderableWidget(this.buttonAry[i][3]);
         this.buttonAry[i][1] = new GuiButtonJB(20 + idAdd, buttonXLeft + buttonShiftH * 2, buttonRowY, buttonWidth, buttonHeight, empty, this::actionPerformed);
         this.addRenderableWidget(this.buttonAry[i][1]);
         this.buttonAry[i][4] = new GuiCheckBoxNoShadow(28 + idAdd, xLeft + 243, buttonRowY + 5, "R", true, 8947967, this::actionPerformed);
         this.addRenderableWidget(this.buttonAry[i][4]);

         for (int b = 0; b < 4; ++b) {
            this.buttonAry[i][b].visible = false;
         }

         this.textFiledAry[i] = new GuiTextFieldEx(30 + idAdd, this.font, buttonXLeft + buttonShiftH * 0, buttonRowY, 40, 18);
         buttonRowY += 25;
      }

      this.showTextFieldFlgAry = new boolean[this.textFiledAry.length];
      this.bypathTextFieldClick = new boolean[this.textFiledAry.length];

      for (i = 0; i < this.textFiledAry.length; ++i) {
         this.textFiledAry[i].setTextColor(-1);
         this.textFiledAry[i].setTextColorUneditable(-1);
         this.textFiledAry[i].setBordered(true);
         this.textFiledAry[i].setMaxLength(7);
      }

      this.updateEachRingAngle();
      this.TextFieldPageName = new GuiTextFieldJP(97, this.font, xLeft + 243 + 10, yTop + 42 + 10, 120, 18);
      this.TextFieldPageName.setTextColor(-1);
      this.TextFieldPageName.setTextColorUneditable(-1);
      this.TextFieldPageName.setBordered(true);
      this.TextFieldPageName.setMaxLength(15);
      this.TextFieldPageName.setValue(this.currentPage.getPageName());
      this.initAllNodeReferences();
      this.updateInputStates();
   }

   /**
    * 1.12.2 rebuilt the whole layout when the window was resized ({@code GuiScreen} re-ran
    * {@code initGui}); the 1.20.1 {@link Screen#resize} default only repositions elements, which
    * would leave this screen's {@code guiLeft}/{@code guiTop} based layout stale.
    */
   @Override
   public void resize(Minecraft minecraft, int width, int height) {
      this.width = width;
      this.height = height;
      this.rebuildWidgets();
   }

   private float[][][] createArrowL() {
      int arrowFX = 26;
      int arrowFY = 10;
      int arrowTX = 0;
      int arrowW = 10;
      int arrowWd2 = arrowW / 2;
      calcArrowTip((double)arrowFX, (double)arrowFY, (double)arrowTX, (double)arrowFY, 10.0, 10.0, this.TIPWORK);
      float arrowAX = (float)this.TIPWORK[0];
      float arrowAY = (float)this.TIPWORK[1];
      float arrowBX = (float)this.TIPWORK[2];
      float arrowBY = (float)this.TIPWORK[3];
      float[][][] trisB = new float[][][]{{{(float)arrowTX, (float)arrowFY}, {arrowBX, arrowBY}, {arrowAX, arrowAY}}, {{(float)arrowFX, (float)(arrowFY + arrowWd2)}, {(float)arrowFX, (float)(arrowFY - arrowWd2)}, {arrowAX, (float)(arrowFY + arrowWd2)}}, {{(float)arrowFX, (float)(arrowFY - arrowWd2)}, {arrowAX, (float)(arrowFY - arrowWd2)}, {arrowAX, (float)(arrowFY + arrowWd2)}}};
      return trisB;
   }

   private float[][][] createArrowR() {
      int arrowFX = 0;
      int arrowFY = 10;
      int arrowTX = arrowFX + 26;
      int arrowW = 10;
      int arrowWd2 = arrowW / 2;
      calcArrowTip((double)arrowFX, (double)arrowFY, (double)arrowTX, (double)arrowFY, 10.0, 10.0, this.TIPWORK);
      float arrowAX = (float)this.TIPWORK[0];
      float arrowAY = (float)this.TIPWORK[1];
      float arrowBX = (float)this.TIPWORK[2];
      float arrowBY = (float)this.TIPWORK[3];
      float[][][] trisB = new float[][][]{{{(float)arrowTX, (float)arrowFY}, {arrowBX, arrowBY}, {arrowAX, arrowAY}}, {{(float)arrowFX, (float)(arrowFY + arrowWd2)}, {arrowAX, (float)(arrowFY + arrowWd2)}, {(float)arrowFX, (float)(arrowFY - arrowWd2)}}, {{(float)arrowFX, (float)(arrowFY - arrowWd2)}, {arrowAX, (float)(arrowFY + arrowWd2)}, {arrowAX, (float)(arrowFY - arrowWd2)}}};
      return trisB;
   }

   private float[][][] createRingC(float radius, int segments) {
      float[][][] trisB = new float[segments][][];
      float centerX = radius;
      float centerY = radius;
      double div = (double)(segments - 1);

      for (int i = 0; i < segments; ++i) {
         trisB[i] = new float[3][];
         double angle = 6.283185307179586 * (double)i / div + 1.5707963267948966;
         trisB[i][0] = new float[]{centerX + (float)(Math.cos(angle) * (double)radius), centerY + (float)(Math.sin(angle) * (double)radius)};
         trisB[i][1] = new float[]{centerX, centerY};
         angle = 6.283185307179586 * (double)(i + 1) / div + 1.5707963267948966;
         trisB[i][2] = new float[]{centerX + (float)(Math.cos(angle) * (double)radius), centerY + (float)(Math.sin(angle) * (double)radius)};
      }

      return trisB;
   }

   @Override
   public void tick() {
      super.tick();
      this.updateInputStates();
   }

   private void updateInputStates() {
      int xLeft = (this.width - this.xSize) / 2;
      int yTop = (this.height - this.ySize) / 2;
      this.guiEntrySettings.resetAll();
      this.guiEntrySettings.text = null;
      this.ButtonCenter.setMessage(Component.literal(this.currentPageIndexStr));
      this.ButtonAddPage.active = this.program.pageList.size() < 32;
      this.ButtonPagePrev.active = this.currentPageIndex > 0;
      this.ButtonPageNext.active = this.currentPageIndex < this.program.pageList.size() - 1;
      if (Screen.hasControlDown()) {
         this.ButtonPagePrev.setMessage(Component.literal(this.buttonPagePrevBStr));
         this.ButtonPageNext.setMessage(Component.literal(this.buttonPageNextBStr));
      } else {
         this.ButtonPagePrev.setMessage(Component.literal(this.buttonPagePrevAStr));
         this.ButtonPageNext.setMessage(Component.literal(this.buttonPageNextAStr));
      }

      this.ButtonPageBack.visible = !this.pageJumpHistory.isEmpty();
      this.ButtonConfirmNo.visible = this.ButtonConfirmYes.visible = this.confirmBtnId != -1;
      AbstractRingNode selectedNode = this.getSelectedOnRingNode();
      boolean hasNodeEntryStateChanged = this.selectedNodePrev != selectedNode;
      this.selectedNodePrev = selectedNode;
      this.CheckBoxTypeCycle.setIsChecked(this.program.isExecuteTypeCycle());
      this.CheckBoxTypeShot.setIsChecked(this.program.isExecuteTypeShot());
      this.CheckBoxAuthMot.setIsChecked(this.program.isAutMotOK());
      this.CheckBoxAuthAct.setIsChecked(this.program.isAutActOK());
      this.CheckBoxAuthMov.setIsChecked(this.program.isAutMovOK());
      this.CheckBoxAuthInp.setIsChecked(this.program.isAutInpOK());
      this.CheckBoxOpCut.setIsChecked(this.program.isOPCut());
      if (this.isRingSettingEdtMode) {
         this.CheckBoxTypeCycle.visible = true;
         this.CheckBoxTypeShot.visible = true;
         this.ButtonDeletePage.visible = this.program.pageList.size() > 1;
         this.TextFieldPageName.setVisible(true);
         this.CheckBoxAuthMot.visible = true;
         this.CheckBoxAuthAct.visible = true;
         this.CheckBoxAuthMov.visible = true;
         this.CheckBoxAuthInp.visible = true;
         this.CheckBoxOpCut.visible = true;
      } else {
         this.CheckBoxTypeCycle.visible = false;
         this.CheckBoxTypeShot.visible = false;
         this.ButtonDeletePage.visible = false;
         this.TextFieldPageName.setVisible(false);
         this.CheckBoxAuthMot.visible = false;
         this.CheckBoxAuthAct.visible = false;
         this.CheckBoxAuthMov.visible = false;
         this.CheckBoxAuthInp.visible = false;
         this.CheckBoxOpCut.visible = false;
         if (selectedNode != null) {
            selectedNode.setGuiEntryState(this.guiEntrySettings);
         }
      }

      this.updateSliderState(this.guiEntrySettings);
      if (hasNodeEntryStateChanged) {
         this.SliderEntry.sliderValue = 0.0;
         this.disableAllTextField();
      }

      int startIndex = (int)(this.SliderEntry.getValue() * 6.0);
      int b = 0;

      for (int e = startIndex; b < this.buttonAry.length; ++e) {
         EntrySetting setting = this.guiEntrySettings.entrySettings[e];
         if (setting != null) {
            int i;
            if (this.showTextFieldFlgAry[b]) {
               for (i = 0; i < this.buttonAry[b].length; ++i) {
                  this.buttonAry[b][i].visible = false;
                  this.buttonAry[b][i].active = false;
               }

               this.buttonAry[b][1].visible = setting.visible;
               this.buttonAry[b][1].active = setting.visible;
               this.textFiledAry[b].setVisible(setting.visible);
               this.textFiledAry[b].setEditable(setting.enabled);
            } else {
               for (i = 0; i < this.buttonAry[b].length; ++i) {
                  this.buttonAry[b][i].visible = setting.visible;
                  this.buttonAry[b][i].active = setting.enabled;
               }

               this.textFiledAry[b].setVisible(false);
               this.textFiledAry[b].setEditable(false);
            }

            this.buttonAry[b][4].active = this.buttonAry[b][4].visible = setting.isRefTogglable;
            if (selectedNode != null) {
               GuiCheckBoxNoShadow checkBoxRefFix = (GuiCheckBoxNoShadow)this.buttonAry[b][4];
               if (selectedNode.isTogglableEntryNodeRef(b)) {
                  checkBoxRefFix.setIsChecked(true);
                  checkBoxRefFix.setMessage(Component.literal("R"));
                  checkBoxRefFix.packedFGColour = 8947967;
               } else {
                  checkBoxRefFix.setIsChecked(false);
                  checkBoxRefFix.setMessage(Component.literal("F"));
                  checkBoxRefFix.packedFGColour = 8947848;
               }
            }

            if (hasNodeEntryStateChanged) {
               setting.buttonStatesIndex = setting.buttonStatesInitIndex;
            }

            if (this.showTextFieldFlgAry[b]) {
               this.buttonAry[b][1].setMessage(Component.literal("Set"));
            } else {
               if (setting.buttonStatesIndex >= setting.buttonStates.length) {
                  setting.buttonStatesIndex = 0;
               }

               this.buttonAry[b][0].setMessage(Component.literal(setting.buttonStates[setting.buttonStatesIndex].left));
               this.buttonAry[b][1].setMessage(Component.literal(setting.buttonStates[setting.buttonStatesIndex].right));
               if (setting.buttonStates[setting.buttonStatesIndex].center != null) {
                  this.buttonAry[b][2].setMessage(Component.literal(setting.buttonStates[setting.buttonStatesIndex].center));
               } else {
                  this.buttonAry[b][2].visible = false;
                  this.buttonAry[b][2].active = false;
                  this.buttonAry[b][3].visible = false;
                  this.buttonAry[b][3].active = false;
               }
            }
         }

         ++b;
      }

      this.initAnimTickPrev = this.initAnimTick;
      if (this.initAnimTick > 0) {
         --this.initAnimTick;
      }

      this.updateListNodeIcons(this.currentPage.nodeList);
      this.updateEachRingAngle();

      for (b = 0; b < this.copyDelAnimTick.length; ++b) {
         if (this.copyDelAnimTick[b] > 0) {
            --this.copyDelAnimTick[b];
         }
      }
   }

   protected void updateSliderState(JBGuiSettingsImpl gs) {
      int maxEnabledRow = 0;

      for (int j = 0; j < gs.entrySettings.length; ++j) {
         EntrySetting setting = gs.entrySettings[j];
         if (setting.visible) {
            maxEnabledRow = j;
         }
      }

      maxEnabledRow -= 5;
      if (maxEnabledRow < 0) {
         maxEnabledRow = 0;
      }

      this.SliderEntry.maxValue = (double)maxEnabledRow / 6.0;
      this.SliderEntry.minValue = 0.0;
   }

   private AbstractRingNode getSelectedOnRingNode() {
      return this.selectedFrom == this.selectedTo && this.selectedTo >= 0 && this.selectedTo < this.currentPage.nodeList.size() ? this.currentPage.nodeList.get(this.selectedTo) : null;
   }

   private void updateEachRingAngle() {
      int insertIndex = -1;
      int insertCount = 1;
      if (this.draggingNode != null) {
         insertIndex = this.draggingNode.gui.draggingOnRingIndex;
      }

      int addSize = 0;
      if (insertIndex >= 0) {
         addSize = 1;
      }

      int nodeIdx = 0;

      for (int nodeCnt = this.currentPage.nodeList.size(); nodeIdx < nodeCnt; ++nodeIdx) {
         AbstractRingNode node = this.currentPage.nodeList.get(nodeIdx);
         int indexForAngle = nodeIdx;
         if (insertIndex != -1 && insertIndex <= nodeIdx) {
            indexForAngle = nodeIdx + insertCount;
         }

         node.gui.angleOnRing = (double)((float)(1.5707963267948966 - 6.283185307179586 / (double)(nodeCnt + addSize) * (double)indexForAngle));
         if (!node.gui.hasInitAngle) {
            node.gui.angleOnRingPrev = node.gui.angleOnRing;
            node.gui.hasInitAngle = true;
         }
      }
   }

   private void updateListNodeIcons(ArrayList<AbstractRingNode> list) {
      int nodeCnt = list.size();

      for (int nodeIdx = 0; nodeIdx < nodeCnt; ++nodeIdx) {
         AbstractRingNode node = list.get(nodeIdx);
         if (node != null) {
            node.gui.angleOnRingPrev = node.gui.angleOnRing;
         }
      }
   }

   /**
    * 1.12.2 {@code func_73863_a} tooltip selection: decides which tooltip is active, fills
    * {@link #listTooltip} and draws it (the 1.12.2 {@code drawHoveringText} call, mapped to
    * {@code GuiGraphics.renderComponentTooltip}).
    */
   private void collectTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (this.confirmBtnId != -1) {
         String confirmTip = this.confirmMessage;
         this.listTooltip.clear();
         this.listTooltip.add(confirmTip);
         int tipX = this.ButtonConfirmYes.getX() + this.ButtonConfirmYes.getWidth() / 2 - this.font.width(confirmTip) / 2;
         int tipY = this.ButtonConfirmYes.getY();
         this.renderTooltip(guiGraphics, tipX - this.guiLeft, tipY - this.guiTop);
      } else if (this.lineFromNode != null) {
         this.listTooltip.clear();
         this.listTooltip.add(CompatHelper.formatLang(this.lineFromNode.getRefEntryName(this.lineFromNode.getCurrentLayingRefNum())));
         this.renderTooltip(guiGraphics, mouseX, mouseY);
      } else if (this.isMouseOver(this.CheckBoxLockDel, mouseX, mouseY)) {
         this.listTooltip.clear();
         this.listTooltip.add("Del Lock");
         this.renderTooltip(guiGraphics, this.CheckBoxLockDel.getX(), this.CheckBoxLockDel.getY());
      } else if (this.draggingNode == null) {
         int mouseOnIndex = this.isOnNodeTypeListNode(mouseX, mouseY);
         if (mouseOnIndex != -1) {
            this.renderNodeTip(guiGraphics, this.nodeTypeList.get(mouseOnIndex), mouseX, mouseY, false);
         } else {
            mouseOnIndex = this.isOnRingNode(mouseX, mouseY);
            if (mouseOnIndex != -1) {
               this.renderNodeTip(guiGraphics, this.currentPage.nodeList.get(mouseOnIndex), mouseX, mouseY, true);
            }
         }
      }
   }

   /**
    * 1.12.2 {@code GuiScreen.drawHoveringText(listTooltip, x, y, font)}: draws {@link #listTooltip}
    * through the 1.20.1 component tooltip renderer.
    */
   private void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
      List<Component> lines = new ArrayList<>(this.listTooltip.size());

      for (String line : this.listTooltip) {
         lines.add(Component.literal(line));
      }

      guiGraphics.renderComponentTooltip(this.font, lines, x, y);
   }

   /**
    * 1.12.2 {@code func_73863_a} {@code showNodeTip} block: keeps the very same ring geometry walk
    * (interpolated node angle / ring relative position), collects every visible entry string into
    * {@link #listTooltip} and draws it through {@link #drawHoveringTextExEx}.
    */
   private void collectAllNodeTips(GuiGraphics guiGraphics, float partialTick) {
      float ringCenterX = (float)this.getRingCenterX();
      float ringCenterY = (float)this.getRingCenterY();
      JBGuiSettingsImpl stTemp = new JBGuiSettingsImpl(10);
      int nodeIdx = 0;

      for (int nodeCnt = this.currentPage.nodeList.size(); nodeIdx < nodeCnt; ++nodeIdx) {
         AbstractRingNode node = this.currentPage.nodeList.get(nodeIdx);
         double angleRad = node.gui.angleOnRingPrev + (node.gui.angleOnRing - node.gui.angleOnRingPrev) * (double)partialTick;
         float nodeRelCenterX = (float)Math.cos(angleRad) * (float)this.ringRadius;
         float nodeRelCenterY = -((float)Math.sin(angleRad)) * (float)this.ringRadius;
         stTemp.resetAll();
         node.setGuiEntryState(stTemp);
         this.listTooltip.clear();

         for (int e = 0; e < stTemp.entrySettings.length; ++e) {
            EntrySetting setting = stTemp.entrySettings[e];
            if (setting != null && setting.visible) {
               this.listTooltip.add(node.getDisplayEntryValueStr(this.program, this.currentPage, e, setting));
            }
         }

         this.drawHoveringTextExEx(guiGraphics, this.listTooltip, ringCenterX, ringCenterY, nodeRelCenterX, nodeRelCenterY);
      }
   }

   private boolean isMouseOver(AbstractWidget btn, int mouseX, int mouseY) {
      return btn.active && btn.visible && mouseX >= btn.getX() && mouseY >= btn.getY() && mouseX < btn.getX() + btn.getWidth() && mouseY < btn.getY() + btn.getHeight();
   }

   /**
    * The ring node draw positions of the 1.12.2 {@code drawGuiContainerBackgroundLayer2} (including
    * the init-animation interpolation and the page-change offset) and the dragged node position,
    * i.e. the {@code RingGui.drawX}/{@code drawY} state {@link #drawGuiContainerBackgroundLayer2}
    * and the connect-line drawing consume.
    */
   private void updateNodeDrawPositions(float partialTick, int mouseX, int mouseY) {
      float ringCenterX = (float)this.getRingCenterX();
      float ringCenterY = (float)this.getRingCenterY();
      float animIp = (5.0F - ((float)this.initAnimTickPrev + (float)(this.initAnimTick - this.initAnimTickPrev) * partialTick)) / 5.0F;
      int targetRefNum = 0;

      for (int nodeCnt = this.currentPage.nodeList.size(); targetRefNum < nodeCnt; ++targetRefNum) {
         AbstractRingNode nodeFrom = this.currentPage.nodeList.get(targetRefNum);
         double angleRad = nodeFrom.gui.angleOnRingPrev + (nodeFrom.gui.angleOnRing - nodeFrom.gui.angleOnRingPrev) * (double)partialTick;
         angleRad += (double)((1.0F - animIp) * 3.1415927F);
         float nodeCenterX = ringCenterX + (float)Math.cos(angleRad) * (float)this.ringRadius * animIp;
         float nodeCenterY = ringCenterY - (float)Math.sin(angleRad) * (float)this.ringRadius * animIp;
         nodeFrom.setGuiDrawPos(nodeCenterX, nodeCenterY);
      }

      if (this.draggingNode != null) {
         this.draggingNode.setGuiDrawPos((float)mouseX, (float)mouseY);
      }
   }

   /**
    * 1.12.2 {@code func_73863_a} (drawScreen).  Order: default background, texture layer 1, widgets
    * ({@code super.render}), texture layer 2, then the (guiLeft, guiTop) translated foreground,
    * the text fields, the hovered tooltip and finally the node tips.  See the class javadoc for the
    * GL substitutions.
    */
   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics);
      int guiLeft = this.guiLeft;
      int guiTop = this.guiTop;
      this.drawGuiContainerBackgroundLayer(guiGraphics, partialTick, mouseX, mouseY);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.drawGuiContainerBackgroundLayer2(guiGraphics, partialTick, mouseX, mouseY);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate((float)guiLeft, (float)guiTop, 0.0F);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.drawGuiContainerForegroundLayer2(guiGraphics, partialTick, mouseX, mouseY);
      guiGraphics.pose().popPose();

      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         if (this.showTextFieldFlgAry[i]) {
            this.textFiledAry[i].render(guiGraphics, mouseX, mouseY, partialTick);
         }
      }

      if (this.TextFieldPageName.isVisible()) {
         this.TextFieldPageName.render(guiGraphics, mouseX, mouseY, partialTick);
      }

      this.collectTooltips(guiGraphics, mouseX, mouseY);

      if (this.showNodeTip) {
         this.collectAllNodeTips(guiGraphics, partialTick);
      }

      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   /**
    * 1.12.2 {@code drawGuiContainerForegroundLayer2}.  Drawn with the pose already translated by
    * (guiLeft, guiTop), so all coordinates are gui-relative exactly as in 1.12.2.
    */
   protected void drawGuiContainerForegroundLayer2(GuiGraphics guiGraphics, float partialTick, int par1, int par2) {
      guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.editring"), 8, 6, 4210752, false);
      int strh = this.font.lineHeight;
      int strw;
      if (this.errorMessage != null) {
         strw = this.font.width(this.errorMessage);
         guiGraphics.drawString(this.font, this.errorMessage, this.xSize - strw - 10, 6, 16711680, false);
      }

      String pageNumsStr = "/" + this.program.pageList.size();
      strw = this.font.width(pageNumsStr);
      guiGraphics.drawString(this.font, pageNumsStr, 240 - strw - 1, 216 - strh - 1, 16777215, false);
      int lft = (this.width - this.xSize) / 2;
      int top = (this.height - this.ySize) / 2;
      int ringCenterXForFr = this.getRingCenterX() - lft;
      int ringCenterYForFr = this.getRingCenterY() - top;
      AbstractRingNode selectedNode = this.getSelectedOnRingNode();
      String pageName = this.currentPage.getPageName();
      if (pageName != null) {
         pageName = this.font.plainSubstrByWidth(pageName, 120);
         strw = this.font.width(pageName);
         guiGraphics.drawString(this.font, pageName, ringCenterXForFr - strw / 2, ringCenterYForFr - this.ringRadius - strh - 10 - 2, 8947848, false);
      }

      if (this.isRingSettingEdtMode) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.pagesettings"), 243, 24, 16777113, false);
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.ringsettings"), 243, 90, 16777113, false);
      } else if (selectedNode != null) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang(selectedNode.getNodeName()), 243, 24, 16777113, false);
      }

      int rowY;
      int nodeCnt;
      if (this.isRingSettingEdtMode) {
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.pagename"), 253, 42, 15658734, false);
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.executetype"), 253, 105, 15658734, false);
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.authority"), 253, 130, 15658734, false);
         guiGraphics.drawString(this.font, CompatHelper.formatLang("jointGui.info.else"), 253, 165, 15658734, false);
      } else if (selectedNode != null) {
         rowY = 42;
         nodeCnt = (int)(this.SliderEntry.getValue() * 6.0);
         int b = 0;

         for (int e = nodeCnt; b < this.buttonAry.length; ++e) {
            EntrySetting setting = this.guiEntrySettings.entrySettings[e];
            if (setting != null) {
               if (setting.visible) {
                  String str = selectedNode.getDisplayEntryTitleStr(this.program, this.currentPage, e, setting);
                  guiGraphics.drawString(this.font, str, 243, rowY, 15658734, false);
                  str = selectedNode.getDisplayEntryValueStr(this.program, this.currentPage, e, setting);
                  if (str != null) {
                     if (selectedNode.getTogglableRefEntry(e) != null) {
                        str = this.font.plainSubstrByWidth(str, 65);
                     } else {
                        str = this.font.plainSubstrByWidth(str, 85);
                     }
                  }

                  int righten = 325 - this.font.width(str);
                  guiGraphics.drawString(this.font, str, righten, rowY + 10, 15658734, false);
               }

               rowY += 25;
            }

            ++b;
         }
      }

      if (this.initAnimTick == 0 && this.CheckBoxNodeIndex.isChecked()) {
         float ringCenterX = (float)this.getRingCenterX() - (float)this.guiLeft;
         float ringCenterY = (float)this.getRingCenterY() - (float)this.guiTop;
         rowY = 0;

         for (nodeCnt = this.currentPage.nodeList.size(); rowY < nodeCnt; ++rowY) {
            AbstractRingNode node = this.currentPage.nodeList.get(rowY);
            double angleRad = node.gui.angleOnRingPrev + (node.gui.angleOnRing - node.gui.angleOnRingPrev) * (double)partialTick;
            float nodeCenterX = ringCenterX + (float)Math.cos(angleRad) * (float)this.ringRadius * 0.8F;
            float nodeCenterY = ringCenterY - (float)Math.sin(angleRad) * (float)this.ringRadius * 0.8F;
            String str = this.formatNumText.format((long)rowY);
            int strW = this.font.width(str);
            int strL = (int)(nodeCenterX - (float)strW / 2.0F);
            int strT = (int)(nodeCenterY - (float)this.font.lineHeight / 2.0F);
            guiGraphics.drawString(this.font, str, (float)strL, (float)strT, 10066329, true);
         }
      }
   }

   /**
    * 1.12.2 {@code drawHoveringTextExEx}: node tip placed on the ring-to-screen bounding edge,
    * including the connecting line and the hand-drawn bordered box.  The 1.12.2
    * {@code GuiUtils.drawGradientRect(zLevel, ...)} calls map to
    * {@code GuiGraphics.fillGradient(x0, y0, x1, y1, from, to)} (the zLevel is dropped: 1.20.1
    * orders these by flush order at z 0).
    */
   protected void drawHoveringTextExEx(GuiGraphics guiGraphics, List<String> textLines, float ringCenterX, float ringCenterY, float nodeRelCenterX, float nodeRelCenterY) {
      if (!textLines.isEmpty()) {
         int xPad = 2;
         int yPad = 2;
         int maxWidth = 46;
         float scale = 1.0F;
         int tooltipTextWidth = 0;

         for (String textLine : textLines) {
            int textLineWidth = this.font.width(textLine);
            if (textLineWidth > tooltipTextWidth) {
               tooltipTextWidth = textLineWidth;
            }
         }

         tooltipTextWidth = Math.min(tooltipTextWidth, maxWidth - xPad);
         int titleLinesCount = 1;
         int tooltipHeight = 8;
         if (textLines.size() > 1) {
            tooltipHeight += (textLines.size() - 1) * 10;
            if (textLines.size() > titleLinesCount) {
               tooltipHeight += 2;
            }
         }

         float nodeCenterX = ringCenterX + nodeRelCenterX;
         float nodeCenterY = ringCenterY + nodeRelCenterY;
         float tipAreaX = (float)tooltipTextWidth + (float)xPad * 2.0F;
         float tipAreaY = (float)tooltipHeight + (float)yPad * 2.0F;
         float boundMarginX = tipAreaX / 2.0F + (float)xPad;
         float boundMarginY = tipAreaY / 2.0F + (float)yPad;
         boundMarginX *= scale;
         boundMarginY *= scale;
         float screenHalfBoundedX = (float)this.width / 2.0F - boundMarginX;
         float screenHalfBoundedY = (float)this.height / 2.0F - boundMarginY;
         float aspectRatio = (float)this.height / (float)(this.width - (this.width - this.height) / 2);
         float ellipsedNodeCenterY = nodeRelCenterY * aspectRatio;
         float intersectX_A;
         float intersectY_A;
         if (ellipsedNodeCenterY != 0.0F) {
            if (ellipsedNodeCenterY > 0.0F) {
               intersectX_A = screenHalfBoundedY * (nodeRelCenterX / ellipsedNodeCenterY);
               intersectY_A = screenHalfBoundedY;
            } else {
               intersectX_A = -screenHalfBoundedY * (nodeRelCenterX / ellipsedNodeCenterY);
               intersectY_A = -screenHalfBoundedY;
            }
         } else {
            intersectY_A = Float.MAX_VALUE;
            intersectX_A = Float.MAX_VALUE;
         }

         float a_Len = intersectX_A * intersectX_A + intersectY_A * intersectY_A;
         float intersectX_B;
         float intersectY_B;
         if (nodeRelCenterX != 0.0F) {
            if (nodeRelCenterX > 0.0F) {
               intersectY_B = screenHalfBoundedX * (ellipsedNodeCenterY / nodeRelCenterX);
               intersectX_B = screenHalfBoundedX;
            } else {
               intersectY_B = -screenHalfBoundedX * (ellipsedNodeCenterY / nodeRelCenterX);
               intersectX_B = -screenHalfBoundedX;
            }
         } else {
            intersectY_B = Float.MAX_VALUE;
            intersectX_B = Float.MAX_VALUE;
         }

         float b_Len = intersectX_B * intersectX_B + intersectY_B * intersectY_B;
         if (a_Len > b_Len) {
            intersectX_A = intersectX_B;
            intersectY_A = intersectY_B;
         }

         float intersectX = intersectX_A + (float)(this.width / 2);
         float intersectY = intersectY_A + (float)(this.height / 2);
         int tooltipLineX = (int)(intersectX - tipAreaX / 2.0F);
         int tooltipLineY = (int)(intersectY - tipAreaY / 2.0F);
         this.drawSimpleLine(guiGraphics, nodeCenterX, nodeCenterY, (float)tooltipLineX + tipAreaX / 2.0F, (float)tooltipLineY + tipAreaY / 2.0F, 3407667);
         intersectX *= 1.0F / scale;
         intersectY *= 1.0F / scale;
         int tooltipX = (int)(intersectX - tipAreaX / 2.0F);
         int tooltipY = (int)(intersectY - tipAreaY / 2.0F);
         guiGraphics.pose().pushPose();
         guiGraphics.pose().scale(scale, scale, 1.0F);
         guiGraphics.fillGradient(tooltipX - xPad, tooltipY - yPad, tooltipX + tooltipTextWidth + xPad, tooltipY + tooltipHeight + yPad, -1, -1);
         guiGraphics.fillGradient(tooltipX - xPad, tooltipY - (yPad - 1), tooltipX - (xPad - 1), tooltipY + tooltipHeight + (yPad - 1), 1347420415, 1344798847);
         guiGraphics.fillGradient(tooltipX + tooltipTextWidth + (xPad - 1), tooltipY - (xPad - 1), tooltipX + tooltipTextWidth + xPad, tooltipY + tooltipHeight + (yPad - 1), 1347420415, 1344798847);
         guiGraphics.fillGradient(tooltipX - xPad, tooltipY - yPad, tooltipX + tooltipTextWidth + xPad, tooltipY - (yPad - 1), 1347420415, 1347420415);
         guiGraphics.fillGradient(tooltipX - xPad, tooltipY + tooltipHeight + (yPad - 1), tooltipX + tooltipTextWidth + xPad, tooltipY + tooltipHeight + yPad, 1344798847, 1344798847);

         for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
            String line = this.font.plainSubstrByWidth(textLines.get(lineNumber), maxWidth - xPad);
            guiGraphics.drawString(this.font, line, tooltipX, tooltipY, -16777216, false);
            if (lineNumber + 1 == titleLinesCount) {
               tooltipY += 2;
            }

            tooltipY += 10;
         }

         guiGraphics.pose().popPose();
      }
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer}: the three-piece panel texture strip, drawn in
    * absolute screen coordinates.
    */
   protected void drawGuiContainerBackgroundLayer(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int lft = (this.width - this.xSize) / 2;
      int top = (this.height - this.ySize) / 2;
      float texLft = 0.0F;
      int width = 256;
      drawTexturedModalRect(guiGraphics, (float)lft, (float)top, texLft, 0.0F, width, this.ySize, 384.0F, 256.0F);
      int bgLft = lft + width;
      texLft += (float)width;
      width = 48;
      drawTexturedModalRect(guiGraphics, (float)bgLft, (float)top, texLft, 0.0F, width, this.ySize, 132.0F, (float)this.ySize, 384.0F, 256.0F);
      bgLft += 132;
      texLft += (float)width;
      width = 16;
      drawTexturedModalRect(guiGraphics, (float)bgLft, (float)top, texLft, 0.0F, width, this.ySize, 384.0F, 256.0F);
   }

   /**
    * 1.12.2 {@code drawGuiContainerBackgroundLayer2}: copy/del list icons, node type list icons, the
    * background ring, the ring nodes (via {@link #updateNodeDrawPositions}), the dragged node and
    * all connect lines.
    */
   protected void drawGuiContainerBackgroundLayer2(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
      int listLft = this.getTypeListLeftX();
      int listTop = this.getTypeListTopY() + 1;
      int nTypeStartRow = 0;

      int startIndex;
      float nodePosX;
      float nodePosY;
      for (startIndex = copy_DelList.length; nTypeStartRow < startIndex; ++nTypeStartRow) {
         AbstractRingNode node = copy_DelList[nTypeStartRow];
         if (node != null) {
            nodePosX = (float)(listLft + 16);
            nodePosY = (float)(this.guiTop + COPY_DEL_LIST_YTOP[nTypeStartRow] - this.copyDelAnimTick[nTypeStartRow]);
            drawNode(guiGraphics, node, nodePosX, nodePosY, 16, false, false, 0, 0, true);
         }
      }

      nTypeStartRow = (int)(this.SliderNodeList.getValue() * 8.0);
      startIndex = nTypeStartRow * 2;
      int endIndexExclude = (nTypeStartRow + 8) * 2;

      for (int nodeIdx = startIndex; nodeIdx < endIndexExclude && nodeIdx < this.nodeTypeList.size(); ++nodeIdx) {
         AbstractRingNode node = this.nodeTypeList.get(nodeIdx);
         if (node != null) {
            nodePosX = (float)(listLft + nodeIdx % 2 * 16);
            nodePosY = (float)(listTop + (nodeIdx / 2 - nTypeStartRow) * 16);
            drawNode(guiGraphics, node, nodePosX, nodePosY, 16, false, false, 0, 0, true);
         }
      }

      float ringCenterX = (float)this.getRingCenterX();
      float ringCenterY = (float)this.getRingCenterY();
      drawFilledRing(guiGraphics, ringCenterX, ringCenterY, (float)this.ringRadius, 20, 10.0, 5916487);
      this.updateNodeDrawPositions(partialTick, mouseX, mouseY);
      int targetRefNum = 0;

      int nodeCnt;
      for (nodeCnt = this.currentPage.nodeList.size(); targetRefNum < nodeCnt; ++targetRefNum) {
         AbstractRingNode nodeFrom = this.currentPage.nodeList.get(targetRefNum);
         drawNode(guiGraphics, nodeFrom, nodeFrom.gui.drawX, nodeFrom.gui.drawY, 16, true, this.isInSelection(targetRefNum), 8355635, 127, true);
      }

      if (this.draggingNode != null) {
         drawNode(guiGraphics, this.draggingNode, (float)mouseX, (float)mouseY, 16, true, false, 0, 0, true);
         if (this.draggingNode.hasRefEntry()) {
            this.drawConnectLine(guiGraphics, this.draggingNode);
         }
      }

      targetRefNum = 0;

      for (nodeCnt = this.currentPage.nodeList.size(); targetRefNum < nodeCnt; ++targetRefNum) {
         AbstractRingNode nodeFrom = this.currentPage.nodeList.get(targetRefNum);
         if (nodeFrom.hasRefEntry()) {
            this.drawConnectLine(guiGraphics, nodeFrom);
         }
      }

      if (this.lineFromNode != null) {
         int refNum = this.lineFromNode.getCurrentLayingRefNum();
         this.drawSimpleLine(guiGraphics, this.lineFromNode.gui.drawX, this.lineFromNode.gui.drawY, (float)mouseX, (float)mouseY, this.lineFromNode.getRefLineColor(refNum));
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
   }

   /**
    * 1.12.2 {@code drawNode}.
    *
    * @param guiGraphics the 1.20.1 render context (the 1.12.2 implicit GL state)
    */
   public static void drawNode(GuiGraphics guiGraphics, AbstractRingNode node, float nodePosX, float nodePosY, int showIconSize, boolean alignCenter, boolean isHighlighted, int highlightColor, int highlightAlpha, boolean isABlendOff) {
      float showH;
      float showW = showH = isHighlighted ? (float)showIconSize * 1.5F : (float)showIconSize;
      float nodeX;
      float nodeY;
      if (alignCenter) {
         nodeX = nodePosX - showW / 2.0F;
         nodeY = nodePosY - showH / 2.0F;
      } else {
         nodeX = nodePosX;
         nodeY = nodePosY;
      }

      float iconU = node.getIconU();
      float iconV = node.getIconV();
      int iconWH = node.getIconSize();
      if (isHighlighted) {
         if (isABlendOff) {
            RenderSystem.enableBlend();
         }

         drawFilledCircle(guiGraphics, nodePosX, nodePosY, showW * 1.1F / 2.0F, 9, highlightColor, highlightAlpha);
         if (isABlendOff) {
            RenderSystem.disableBlend();
         }
      }

      drawTexturedModalRect(guiGraphics, nodeX, nodeY, iconU, iconV, iconWH, iconWH, showW, showH, 384.0F, 256.0F, 310.0);
      if (node.hasOverlayIcon()) {
         iconU = node.getOverlayIconU();
         iconV = node.getOverlayIconV();
         iconWH = node.getOverlayIconSize();
         drawTexturedModalRect(guiGraphics, nodeX, nodeY, iconU, iconV, iconWH, iconWH, showW, showH, 384.0F, 256.0F, 310.0);
      }
   }

   /**
    * 1.12.2 {@code drawConnectLine}: range refs, self refs (loop circle) and single refs, with the
    * per-segment line width of the 1.12.2 {@code GL11.glLineWidth} calls kept in
    * {@link #currentLineWidth}.
    */
   private void drawConnectLine(GuiGraphics guiGraphics, AbstractRingNode nodeRefFrom) {
      boolean doShowTypeChar = nodeRefFrom == this.draggingNode || nodeRefFrom == this.getSelectedOnRingNode();
      int refNum;
      int refTypeChar;
      int ringSize;
      float avgX;
      float dX;
      if (nodeRefFrom.isRangeRef() && nodeRefFrom != this.draggingNode) {
         if (!this.CheckBoxRefInnLine.isChecked()) {
            return;
         }

         refNum = -1;
         float charAt = 0.25F;
         if (doShowTypeChar) {
            refNum = nodeRefFrom.getRefCharIndex(0);
            charAt = nodeRefFrom.getRefCharPos(0);
            this.currentLineWidth = 3.0F;
         } else {
            this.currentLineWidth = 0.5F;
         }

         refTypeChar = 0;
         int toRefNum = nodeRefFrom.getRefCount() - 1;
         int rangeNodeCnt = this.currentPage.nodeList.size();
         if (nodeRefFrom.gui.refNodes[refTypeChar] != null && nodeRefFrom.gui.refNodes[toRefNum] != null && nodeRefFrom.gui.refNodes[refTypeChar] != this.draggingNode && nodeRefFrom.gui.refNodes[toRefNum] != this.draggingNode && nodeRefFrom.gui.refNodes[refTypeChar] != nodeRefFrom.gui.refNodes[toRefNum]) {
            int fromIdx = nodeRefFrom.getRefNodeIndex(refTypeChar);
            int toIdx = nodeRefFrom.getRefNodeIndex(toRefNum);
            if (fromIdx >= 0 && fromIdx < rangeNodeCnt && toIdx >= 0 && toIdx < rangeNodeCnt && fromIdx != toIdx) {
               ringSize = 1;
               avgX = nodeRefFrom.gui.drawX;
               dX = nodeRefFrom.gui.drawY;
               int lineCol = fromIdx;
               int idx = fromIdx;

               while (true) {
                  AbstractRingNode nodeInRange = this.currentPage.nodeList.get(idx);
                  avgX += nodeInRange.gui.drawX;
                  dX += nodeInRange.gui.drawY;
                  ++ringSize;
                  if (idx == toIdx) {
                     avgX /= (float)ringSize;
                     dX /= (float)ringSize;
                     lineCol = nodeRefFrom.getRefLineColor(refTypeChar);
                     this.drawMidArrowedLine(guiGraphics, avgX, dX, nodeRefFrom.gui.drawX, nodeRefFrom.gui.drawY, lineCol, refNum, charAt);
                     idx = fromIdx;
                     int idxMod = fromIdx;

                     while (true) {
                        AbstractRingNode nodeInRangeMod = this.currentPage.nodeList.get(idxMod);
                        if (idxMod == fromIdx) {
                           lineCol = nodeRefFrom.getRefLineColor(refTypeChar);
                        } else if (idxMod == toIdx) {
                           lineCol = nodeRefFrom.getRefLineColor(toRefNum);
                        } else {
                           lineCol = 5789784;
                        }

                        this.drawMidArrowedLine(guiGraphics, nodeInRangeMod.gui.drawX, nodeInRangeMod.gui.drawY, avgX, dX, lineCol, refNum, charAt);
                        if (idxMod == toIdx) {
                           this.currentLineWidth = 0.5F;
                           return;
                        }

                        ++idx;
                        idxMod = idx % ringSize;
                     }
                  }

                  ++lineCol;
                  idx = lineCol % ringSize;
               }
            }

            return;
         }
      }

      refNum = 0;

      for (int len = nodeRefFrom.getRefCount(); refNum < len; ++refNum) {
         refTypeChar = -1;
         float charAt = 0.25F;
         if (doShowTypeChar) {
            refTypeChar = nodeRefFrom.getRefCharIndex(refNum);
            charAt = nodeRefFrom.getRefCharPos(refNum);
            this.currentLineWidth = 3.0F;
         } else {
            this.currentLineWidth = 0.5F;
         }

         if (nodeRefFrom.gui.refNodes[refNum] != null) {
            AbstractRingNode nodeTo = nodeRefFrom.gui.refNodes[refNum];
            if (nodeRefFrom == nodeTo) {
               float ringCenterX = (float)this.getRingCenterX();
               float ringCenterY = (float)this.getRingCenterY();
               ringSize = this.currentPage.nodeList.size();
               if (ringSize < 8) {
                  ringSize = 8;
               }

               avgX = 2.0F / (float)ringSize;
               dX = (ringCenterX - nodeRefFrom.gui.drawX) * avgX;
               float dY = (ringCenterY - nodeRefFrom.gui.drawY) * avgX;
               float loopCenterX = nodeRefFrom.gui.drawX + dX;
               float loopCenterY = nodeRefFrom.gui.drawY + dY;
               float loopRadius = Mth.sqrt(dX * dX + dY * dY);
               if (loopRadius <= 0.01F) {
                  return;
               }

               if (nodeRefFrom.isRefLineTowardsThis(refNum)) {
                  if (!this.CheckBoxRefInnLine.isChecked()) {
                     continue;
                  }
               } else if (!this.CheckBoxRefOutLine.isChecked()) {
                  continue;
               }

               this.drawLinedCircle(guiGraphics, loopCenterX, loopCenterY, loopRadius, 20, nodeRefFrom.getRefLineColor(refNum));
               float tangentX = nodeRefFrom.gui.drawX + dX * 2.0F;
               float tangentY = nodeRefFrom.gui.drawY + dY * 2.0F;
               if (nodeRefFrom.isRefLineTowardsThis(refNum)) {
                  this.drawArrowedLine(guiGraphics, tangentX - dY, tangentY + dX, tangentX, tangentY, nodeRefFrom.getRefLineColor(refNum), false, true, refTypeChar, charAt);
               } else {
                  this.drawArrowedLine(guiGraphics, tangentX + dY, tangentY - dX, tangentX, tangentY, nodeRefFrom.getRefLineColor(refNum), false, true, refTypeChar, 1.0F - charAt);
               }
            } else if (nodeRefFrom.isRefLineTowardsThis(refNum)) {
               if (!this.CheckBoxRefInnLine.isChecked()) {
                  continue;
               }

               if (nodeRefFrom.isCurvedLine(refNum)) {
                  this.drawCurvedLine(guiGraphics, nodeTo.gui.drawX, nodeTo.gui.drawY, nodeRefFrom.gui.drawX, nodeRefFrom.gui.drawY, nodeRefFrom.getRefLineColor(refNum), refTypeChar, charAt);
               } else {
                  this.drawMidArrowedLine(guiGraphics, nodeTo.gui.drawX, nodeTo.gui.drawY, nodeRefFrom.gui.drawX, nodeRefFrom.gui.drawY, nodeRefFrom.getRefLineColor(refNum), refTypeChar, charAt);
               }
            } else {
               if (!this.CheckBoxRefOutLine.isChecked()) {
                  continue;
               }

               if (nodeRefFrom.isCurvedLine(refNum)) {
                  this.drawCurvedLine(guiGraphics, nodeRefFrom.gui.drawX, nodeRefFrom.gui.drawY, nodeTo.gui.drawX, nodeTo.gui.drawY, nodeRefFrom.getRefLineColor(refNum), refTypeChar, 1.0F - charAt);
               } else {
                  this.drawMidArrowedLine(guiGraphics, nodeRefFrom.gui.drawX, nodeRefFrom.gui.drawY, nodeTo.gui.drawX, nodeTo.gui.drawY, nodeRefFrom.getRefLineColor(refNum), refTypeChar, 1.0F - charAt);
               }
            }
         }

         this.currentLineWidth = 0.5F;
      }
   }

   /**
    * 1.12.2 {@code drawCurvedLine}: lays out the bezier control points and emits the curve through
    * {@link BezierHelper#addBezierLineStrips}, then draws the mid arrow tip and the ref type char of
    * the sampled request position.
    */
   private void drawCurvedLine(GuiGraphics guiGraphics, float fromX, float fromY, float toX, float toY, int col, int refTypeChar, float charAt) {
      this.bezierHelper.clear(charAt);
      this.bezierHelper.addVerts2f(fromX, fromY);
      float rCX = (float)this.getRingCenterX();
      float rCY = (float)this.getRingCenterY();
      float cX = (fromX + toX) / 2.0F;
      float cY = (fromY + toY) / 2.0F;
      cX += (rCX - cX) / 5.0F;
      cY += (rCY - cY) / 5.0F;
      this.bezierHelper.addVerts2f(cX, cY);
      this.bezierHelper.addVerts2f(toX, toY);
      int r = col >> 16 & 255;
      int g = col >> 8 & 255;
      int b = col & 255;
      int a = 127;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugLineStrip((double)this.currentLineWidth));
      this.bezierHelper.addBezierLineStrips(vc, guiGraphics.pose().last().pose(), 0.0, BEZIER_STEPS, r, g, b, a);
      guiGraphics.flush();
      if (this.bezierHelper.gotMidLine) {
         this.drawArrowedLine(guiGraphics, this.bezierHelper.midLine[0], this.bezierHelper.midLine[1], this.bezierHelper.midLine[2], this.bezierHelper.midLine[3], col, true, true, -1, 0.0F);
      }

      if (refTypeChar != -1 && this.bezierHelper.gotRequestPosLine) {
         drawTexturedModalRect(guiGraphics, this.bezierHelper.reqPosLine[0] - 4.0F, this.bezierHelper.reqPosLine[1] - 4.0F, (float)refTypeChar * 8.0F, 248.0F, 8, 8, 8.0F, 8.0F, 384.0F, 256.0F, 308.0);
      }
   }

   /** 1.12.2 {@code drawMidArrowedLine}. */
   private void drawMidArrowedLine(GuiGraphics guiGraphics, float fromX, float fromY, float toX, float toY, int col, int refTypeChar, float charAt) {
      this.drawArrowedLine(guiGraphics, fromX, fromY, toX, toY, col, true, false, refTypeChar, charAt);
   }

   /**
    * 1.12.2 {@code drawArrowedLine}.  The 1.12.2 batch was a {@code GL_LINES} draw (vertex pairs);
    * {@code RenderType.debugLineStrip} is a connected strip, so each pair is emitted as its own
    * 2-vertex strip with a flush in between (identical lines, no extra connecting segments).
    */
   private void drawArrowedLine(GuiGraphics guiGraphics, float fromX, float fromY, float toX, float toY, int col, boolean arrowAtMid, boolean onlyArrowTip, int refTypeChar, float charAt) {
      float arrowX = arrowAtMid ? (fromX + toX) / 2.0F : toX;
      float arrowY = arrowAtMid ? (fromY + toY) / 2.0F : toY;
      if (calcArrowTip((double)fromX, (double)fromY, (double)arrowX, (double)arrowY, 2.0, 2.0, this.TIPWORK)) {
         double xa = this.TIPWORK[0];
         double ya = this.TIPWORK[1];
         double xb = this.TIPWORK[2];
         double yb = this.TIPWORK[3];
         int r = col >> 16 & 255;
         int g = col >> 8 & 255;
         int b = col & 255;
         int a = 127;
         if (!onlyArrowTip) {
            this.drawLineSegment(guiGraphics, fromX, fromY, toX, toY, r, g, b, a);
         }

         this.drawLineSegment(guiGraphics, (float)xa, (float)ya, arrowX, arrowY, r, g, b, a);
         this.drawLineSegment(guiGraphics, (float)xb, (float)yb, arrowX, arrowY, r, g, b, a);
         if (refTypeChar != -1) {
            float thirdQuarterX = fromX + (toX - fromX) * charAt;
            float thirdQuarterY = fromY + (toY - fromY) * charAt;
            drawTexturedModalRect(guiGraphics, thirdQuarterX - 4.0F, thirdQuarterY - 4.0F, (float)refTypeChar * 8.0F, 248.0F, 8, 8, 8.0F, 8.0F, 384.0F, 256.0F, 308.0);
         }
      }
   }

   /** 1.12.2 {@code drawSimpleLine} (a single {@code GL_LINES} segment). */
   private void drawSimpleLine(GuiGraphics guiGraphics, float fromX, float fromY, float toX, float toY, int col) {
      int r = col >> 16 & 255;
      int g = col >> 8 & 255;
      int b = col & 255;
      int a = 127;
      this.drawLineSegment(guiGraphics, fromX, fromY, toX, toY, r, g, b, a);
   }

   /**
    * Emits one 2-vertex {@code DEBUG_LINE_STRIP} batch (a single line segment) with the current line
    * width and flushes it, standing in for one 1.12.2 {@code GL_LINES} pair.
    */
   private void drawLineSegment(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int r, int g, int b, int a) {
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugLineStrip((double)this.currentLineWidth));
      Matrix4f matrix = guiGraphics.pose().last().pose();
      vc.vertex(matrix, x1, y1, 0.0F).color(r, g, b, a).endVertex();
      vc.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a).endVertex();
      guiGraphics.flush();
   }

   /** 1.12.2 {@code drawLinedCircle} (a {@code GL_LINE_STRIP} circle outline). */
   private void drawLinedCircle(GuiGraphics guiGraphics, float centerX, float centerY, float radius, int segments, int col) {
      int r = col >> 16 & 255;
      int g = col >> 8 & 255;
      int b = col & 255;
      int a = 127;
      double circleW = 10.0;
      double minRadius = (double)radius - 10.0;
      double maxRadius = (double)radius + 10.0;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugLineStrip((double)this.currentLineWidth));
      Matrix4f matrix = guiGraphics.pose().last().pose();

      for (int i = 0; i < segments; ++i) {
         double angle = PI_X2 * (double)i / (double)(segments - 1) + PI_D2;
         double sinAngle = Math.sin(angle);
         double cosAngle = Math.cos(angle);
         vc.vertex(matrix, (float)((double)centerX + cosAngle * (double)radius), (float)((double)centerY + sinAngle * (double)radius), 0.0F).color(r, g, b, a).endVertex();
      }

      guiGraphics.flush();
   }

   /**
    * 1.12.2 {@code drawFilledRing}.  The 1.12.2 {@code GL_TRIANGLE_STRIP} vertex pairs are emitted
    * as {@code RenderType.debugQuads()} quads (the same band geometry).
    *
    * @param guiGraphics the 1.20.1 render context (the 1.12.2 implicit GL state)
    */
   public static void drawFilledRing(GuiGraphics guiGraphics, float centerX, float centerY, float radius, int segments, double ringWidth, int col) {
      int r = col >> 16 & 255;
      int g = col >> 8 & 255;
      int b = col & 255;
      int a = 127;
      double minRadius = (double)radius - ringWidth;
      double maxRadius = (double)radius + ringWidth;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugQuads());
      Matrix4f matrix = guiGraphics.pose().last().pose();
      double prevMaxX = 0.0;
      double prevMaxY = 0.0;
      double prevMinX = 0.0;
      double prevMinY = 0.0;

      for (int i = 0; i < segments; ++i) {
         double angle = PI_X2 * (double)i / (double)(segments - 1) + PI_D2;
         double sinAngle = Math.sin(angle);
         double cosAngle = Math.cos(angle);
         double maxX = (double)centerX + cosAngle * maxRadius;
         double maxY = (double)centerY + sinAngle * maxRadius;
         double minX = (double)centerX + cosAngle * minRadius;
         double minY = (double)centerY + sinAngle * minRadius;
         if (i > 0) {
            vc.vertex(matrix, (float)prevMaxX, (float)prevMaxY, 0.0F).color(r, g, b, a).endVertex();
            vc.vertex(matrix, (float)prevMinX, (float)prevMinY, 0.0F).color(r, g, b, a).endVertex();
            vc.vertex(matrix, (float)maxX, (float)maxY, 0.0F).color(r, g, b, a).endVertex();
            vc.vertex(matrix, (float)minX, (float)minY, 0.0F).color(r, g, b, a).endVertex();
         }

         prevMaxX = maxX;
         prevMaxY = maxY;
         prevMinX = minX;
         prevMinY = minY;
      }

      guiGraphics.flush();
   }

   /**
    * 1.12.2 {@code drawFilledCircle}.  The 1.12.2 {@code GL_TRIANGLE_STRIP} fan is emitted as
    * degenerate {@code RenderType.debugQuads()} quads ({@code center, prev, current, current}).
    * As in 1.12.2 the {@code aplha} argument is ignored (the original hardcoded alpha 127).
    */
   private static void drawFilledCircle(GuiGraphics guiGraphics, float centerX, float centerY, float radius, int segments, int col, int aplha) {
      int r = col >> 16 & 255;
      int g = col >> 8 & 255;
      int b = col & 255;
      int a = 127;
      VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.debugQuads());
      Matrix4f matrix = guiGraphics.pose().last().pose();
      double prevX = 0.0;
      double prevY = 0.0;

      for (int i = 0; i < segments; ++i) {
         double angle = PI_X2 * (double)i / (double)(segments - 1) + PI_D2;
         double sinAngle = Math.sin(angle);
         double cosAngle = Math.cos(angle);
         double pointX = (double)centerX + cosAngle * (double)radius;
         double pointY = (double)centerY + sinAngle * (double)radius;
         if (i > 0) {
            vc.vertex(matrix, centerX, centerY, 0.0F).color(r, g, b, a).endVertex();
            vc.vertex(matrix, (float)prevX, (float)prevY, 0.0F).color(r, g, b, a).endVertex();
            vc.vertex(matrix, (float)pointX, (float)pointY, 0.0F).color(r, g, b, a).endVertex();
            vc.vertex(matrix, (float)pointX, (float)pointY, 0.0F).color(r, g, b, a).endVertex();
         }

         prevX = pointX;
         prevY = pointY;
      }

      guiGraphics.flush();
   }

   /** 1.12.2 {@code drawTexturedModalRect} (unscaled destination, z 0). */
   public static void drawTexturedModalRect(GuiGraphics guiGraphics, float fx, float fy, float u, float v, int twidth, int theight, float texXSize, float texYSize) {
      drawTexturedModalRect(guiGraphics, fx, fy, u, v, twidth, theight, texXSize, texYSize, 0.0);
   }

   /** 1.12.2 {@code drawTexturedModalRect} (unscaled destination with the 1.12.2 z). */
   public static void drawTexturedModalRect(GuiGraphics guiGraphics, float fx, float fy, float u, float v, int twidth, int theight, float texXSize, float texYSize, double z) {
      blitTexturedModalRect(guiGraphics, fx, fy, u, v, twidth, theight, (float)twidth, (float)theight, texXSize, texYSize);
   }

   /** 1.12.2 {@code drawTexturedModalRect} (scaled destination, z 0). */
   public static void drawTexturedModalRect(GuiGraphics guiGraphics, float fx, float fy, float u, float v, int twidth, int theight, float scaledWidth, float scaledHeight, float texXSize, float texYSize) {
      drawTexturedModalRect(guiGraphics, fx, fy, u, v, twidth, theight, scaledWidth, scaledHeight, texXSize, texYSize, 0.0);
   }

   /** 1.12.2 {@code drawTexturedModalRect} (scaled destination with the 1.12.2 z). */
   public static void drawTexturedModalRect(GuiGraphics guiGraphics, float fx, float fy, float u, float v, int twidth, int theight, float scaledWidth, float scaledHeight, float texXSize, float texYSize, double z) {
      blitTexturedModalRect(guiGraphics, fx, fy, u, v, twidth, theight, scaledWidth, scaledHeight, texXSize, texYSize);
   }

   /**
    * The shared body of the {@code drawTexturedModalRect} overloads: one atlas quad from
    * ({@code u}, {@code v}, {@code srcWidth}, {@code srcHeight}) of the 384x256
    * {@link #resource} texture into the ({@code fx}, {@code fy}, {@code destWidth},
    * {@code destHeight}) destination.  Because {@code GuiGraphics.blit} takes integer destinations,
    * the fractional part of {@code fx}/{@code fy} is preserved with a temporary pose translate.
    */
   private static void blitTexturedModalRect(GuiGraphics guiGraphics, float fx, float fy, float u, float v, int srcWidth, int srcHeight, float destWidth, float destHeight, float texXSize, float texYSize) {
      int ix = Mth.floor(fx);
      int iy = Mth.floor(fy);
      float fracX = fx - (float)ix;
      float fracY = fy - (float)iy;
      boolean shifted = fracX != 0.0F || fracY != 0.0F;
      if (shifted) {
         guiGraphics.pose().pushPose();
         guiGraphics.pose().translate(fracX, fracY, 0.0F);
      }

      guiGraphics.blit(resource, ix, iy, (int)destWidth, (int)destHeight, u, v, srcWidth, srcHeight, (int)texXSize, (int)texYSize);
      if (shifted) {
         guiGraphics.pose().popPose();
      }
   }

   public static boolean calcArrowTip(double fromX, double fromY, double arrowX, double arrowY, double ARROW_W, double ARROW_H, double[] res) {
      double dx = arrowX - fromX;
      double dy = arrowY - fromY;
      double lenD = Math.sqrt(dx * dx + dy * dy);
      if (lenD < 0.01) {
         return false;
      } else {
         double xla = lenD - ARROW_W;
         double ylb = -ARROW_H;
         double sin = dy / lenD;
         double cos = dx / lenD;
         res[0] = xla * cos - ARROW_H * sin + fromX;
         res[1] = xla * sin + ARROW_H * cos + fromY;
         res[2] = xla * cos - ylb * sin + fromX;
         res[3] = xla * sin + ylb * cos + fromY;
         return true;
      }
   }

   protected void actionPerformed(IJBGuiWidget par1GuiButton) {
      if (par1GuiButton != this.SliderNodeList && par1GuiButton != this.SliderEntry) {
         int startIndex = (int)(this.SliderEntry.getValue() * 6.0);
         int fixedId = par1GuiButton.getId() % 1000;
         int entryBtnsRowNum = par1GuiButton.getId() / 1000;
         int entryNum = startIndex + entryBtnsRowNum;
         double amount = 0.0;
         boolean modVal = false;
         boolean setDirect = false;
         int toPage;
         switch (fixedId) {
            case 1:
               this.confirmBtnIdPend = fixedId;
               this.confirmMessage = CompatHelper.formatLang("jointGui.info.confirm.insertrpage");
               return;
            case 2:
               this.confirmBtnIdPend = fixedId;
               this.confirmMessage = CompatHelper.formatLang("jointGui.info.confirm.withoutsave");
               return;
            case 3:
               this.confirmBtnIdPend = fixedId;
               this.confirmMessage = CompatHelper.formatLang("jointGui.info.confirm.deleterpage");
               return;
            case 4:
               this.confirmPerformed(this.confirmBtnId);
               return;
            case 5:
               this.confirmBtnId = -1;
               return;
            case 6:
               toPage = Screen.hasControlDown() ? 0 : this.currentPageIndex - 1;
               if (this.setCurrentPage(toPage)) {
                  this.pageJumpHistory.clear();
               }

               return;
            case 7:
               toPage = Screen.hasControlDown() ? this.program.pageList.size() - 1 : this.currentPageIndex + 1;
               if (this.setCurrentPage(toPage)) {
                  this.pageJumpHistory.clear();
               }

               return;
            case 8:
               if (!this.pageJumpHistory.isEmpty()) {
                  RingProgramPage page = this.pageJumpHistory.removeLast();
                  int backIndex = this.program.pageList.indexOf(page);
                  this.setCurrentPage(backIndex);
               }

               return;
            case 9:
               this.isRingSettingEdtMode = true;
               this.clearSelection();
               return;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 23:
            case 25:
            case 27:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 41:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 61:
            case 63:
            case 65:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            default:
               break;
            case 20:
               if (this.showTextFieldFlgAry[entryBtnsRowNum]) {
                  modVal = true;
                  setDirect = true;
                  amount = this.getValueFromTextField(entryBtnsRowNum);
               } else {
                  modVal = true;
                  amount = this.getButtonRightValue(entryNum);
               }
               break;
            case 22:
               modVal = true;
               amount = this.getButtonLeftValue(entryNum);
               break;
            case 24:
               this.setNextButtonStateIndex(this.guiEntrySettings.entrySettings[entryNum]);
               return;
            case 26:
               this.enableTargetTextField(entryBtnsRowNum);
               return;
            case 28:
               AbstractRingNode selectedNode = this.getSelectedOnRingNode();
               if (selectedNode != null) {
                  selectedNode.toggleEntryType(entryNum);
                  this.updateRefRingIdx2Ref(this.currentPage.nodeList, selectedNode);
               }
               break;
            case 40:
               this.program.setExecuteType(0);
               return;
            case 42:
               this.program.setExecuteType(1);
               return;
            case 48:
               delLock = this.CheckBoxLockDel.isChecked();
               return;
            case 60:
            case 62:
            case 64:
            case 66:
            case 80:
               this.program.setAutMotOK(this.CheckBoxAuthMot.isChecked());
               this.program.setAutActOK(this.CheckBoxAuthAct.isChecked());
               this.program.setAutMovOK(this.CheckBoxAuthMov.isChecked());
               this.program.setAutInpOK(this.CheckBoxAuthInp.isChecked());
               this.program.setOPCut(this.CheckBoxOpCut.isChecked());
               return;
         }

         if (modVal) {
            this.setValueToSelectedNode(entryNum, amount, setDirect);
         }
      }
   }

   private void setValueToSelectedNode(int entryNum, double amount, boolean setDirect) {
      AbstractRingNode selectedNode = this.getSelectedOnRingNode();
      if (!selectedNode.allowDecimalInput(entryNum)) {
         amount = (double)((int)amount);
      }

      if (selectedNode != null) {
         if (setDirect) {
            this.setDirectValue(selectedNode, entryNum, amount);
         } else if (amount > 0.0) {
            selectedNode.setValue(entryNum, JBGuiSettingsImpl.addDoubleInRange(selectedNode.getValue(entryNum), amount, (double)this.guiEntrySettings.entrySettings[entryNum].max));
         } else if (amount < 0.0) {
            selectedNode.setValue(entryNum, JBGuiSettingsImpl.subDoubleInRange(selectedNode.getValue(entryNum), amount, (double)this.guiEntrySettings.entrySettings[entryNum].min));
         }

         this.updateRefRingIdx2Ref(this.currentPage.nodeList, selectedNode);
      }
   }

   private void setDirectValue(AbstractRingNode selectedNode, int entryNum, double amount) {
      if (!selectedNode.allowDecimalInput(entryNum)) {
         amount = (double)((int)amount);
      }

      selectedNode.setValue(entryNum, JBGuiSettingsImpl.trySetDoubleValue(amount, (double)this.guiEntrySettings.entrySettings[entryNum].min, (double)this.guiEntrySettings.entrySettings[entryNum].max));
   }

   private double getButtonLeftValue(int rowNum) {
      int index = this.guiEntrySettings.entrySettings[rowNum].buttonStatesIndex;
      return this.guiEntrySettings.entrySettings[rowNum].buttonStates[index].amountLeft;
   }

   private double getButtonRightValue(int rowNum) {
      int index = this.guiEntrySettings.entrySettings[rowNum].buttonStatesIndex;
      return this.guiEntrySettings.entrySettings[rowNum].buttonStates[index].amountRight;
   }

   private void setNextButtonStateIndex(EntrySetting entrySetting) {
      if (++entrySetting.buttonStatesIndex >= entrySetting.buttonStates.length) {
         entrySetting.buttonStatesIndex = 0;
      }
   }

   protected void confirmPerformed(int srcButtonId) {
      switch (srcButtonId) {
         case 1:
            this.insertPageAfterCurrent();
            return;
         case 2:
            this.closeWithoutSave = true;
            Minecraft.getInstance().player.closeContainer();
            return;
         case 3:
            this.removeCurrentPage();
            return;
         default:
      }
   }

   private void removeCurrentPage() {
      ArrayList<RingProgramPage> pageList = this.program.pageList;
      if (pageList.size() > 1) {
         int indexOfCurrent = pageList.indexOf(this.currentPage);
         pageList.remove(indexOfCurrent);
         this.fixProgramsPagePointer(pageList, indexOfCurrent, true);
         if (indexOfCurrent >= pageList.size()) {
            --indexOfCurrent;
         }

         this.setCurrentPage(indexOfCurrent);
      }
   }

   protected void insertPageAfterCurrent() {
      ArrayList<RingProgramPage> pageList = this.program.pageList;
      if (pageList.size() < 32) {
         int indexOfCurrent = pageList.indexOf(this.currentPage);
         pageList.add(indexOfCurrent + 1, RingProgramPage.createNewRingProgramPage(true));
         this.fixProgramsPagePointer(pageList, indexOfCurrent, false);
         this.setCurrentPage(indexOfCurrent + 1);
      }
   }

   private void fixProgramsPagePointer(ArrayList<RingProgramPage> pageList, int indexOfCurrent, boolean isRemove) {
      int p = 0;

      for (int pLen = pageList.size(); p < pLen; ++p) {
         RingProgramPage page = pageList.get(p);
         int n = 0;

         for (int nLen = page.nodeList.size(); n < nLen; ++n) {
            AbstractRingNode node = page.nodeList.get(n);
            if (node instanceof IOverPageLinker) {
               IOverPageLinker overPageLinkNode = (IOverPageLinker)node;
               if (overPageLinkNode.hasOPPageLinkEntry()) {
                  int pIndex = overPageLinkNode.getPageLinkNum();
                  if (pIndex >= 0) {
                     if (isRemove) {
                        if (pIndex == indexOfCurrent) {
                           overPageLinkNode.setPageLinkNum(-1);
                           if (overPageLinkNode.hasOPNodeLinkEntry()) {
                              overPageLinkNode.setNodeLinkNum(-1);
                              node.gui.overPagePageRef = null;
                              node.gui.overPageNodeRef = null;
                           }
                        } else if (pIndex > indexOfCurrent) {
                           overPageLinkNode.setPageLinkNum(pIndex - 1);
                        }
                     } else if (pIndex > indexOfCurrent) {
                        overPageLinkNode.setPageLinkNum(pIndex + 1);
                     }
                  }
               }
            }
         }
      }
   }

   private void actionPerformedMouseRightClick(AbstractWidget guiButton) {
      int startIndex = (int)(this.SliderEntry.getValue() * 6.0);
      int guiButtonId = ((IJBGuiWidget)guiButton).getId();
      int fixedId = guiButtonId % 1000;
      int entryBtnsRowNum = guiButtonId / 1000;
      int entryNum = startIndex + entryBtnsRowNum;
      int amount = 0;
      boolean modVal = false;
      boolean setDirect = false;
      switch (fixedId) {
         case 20:
            amount = SET_LARGE_AMOUNT;
            modVal = true;
         case 21:
         case 23:
         case 25:
         default:
            break;
         case 22:
            amount = -SET_LARGE_AMOUNT;
            modVal = true;
            break;
         case 24:
         case 26:
            amount = 0;
            setDirect = true;
            modVal = true;
      }

      if (modVal) {
         this.setValueToSelectedNode(entryNum, (double)amount, setDirect);
      }

      this.playClickSound();
   }

   private void clearManipulateObj() {
      if (this.draggingNode != null) {
         int insertIndex = this.draggingNode.gui.originalIndex;
         if (insertIndex >= 0 && insertIndex <= this.currentPage.nodeList.size()) {
            this.insertNodeToRing(this.draggingNode, insertIndex, true);
         }

         this.draggingNode = null;
      }

      this.mouseDownNode = null;
      this.lineFromNode = null;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int x = (int)mouseX;
      int y = (int)mouseY;
      if (this.lineFromNode != null && button == 0) {
         ++this.lineFromNode.gui.refSetCnt;
         return true;
      }

      this.clearManipulateObj();
      this.processBtnRightClickFotTF(x, y, button);
      boolean handled = super.mouseClicked(mouseX, mouseY, button);
      this.processTextFieldsFocusChange(x, y, button);
      this.confirmBtnId = -1;
      if (this.confirmBtnIdPend != -1) {
         this.confirmBtnId = this.confirmBtnIdPend;
         this.confirmBtnIdPend = -1;
      }

      if (this.initAnimTick == 0) {
         int mouseDownNodeIdx;
         AbstractRingNode node;
         if (button == 0) {
            mouseDownNodeIdx = this.isOnRingNode(x, y);
            if (mouseDownNodeIdx >= 0 && mouseDownNodeIdx < this.currentPage.nodeList.size()) {
               node = this.currentPage.nodeList.get(mouseDownNodeIdx);
               if (Screen.hasControlDown() && node instanceof RingNodeSTRingCall) {
                  RingNodeSTRingCall rcNode = (RingNodeSTRingCall)node;
                  int pageToJump = rcNode.getTransitionPageIndex(this.program, this.currentPage);
                  RingProgramPage nowPage = this.currentPage;
                  if (this.setCurrentPage(pageToJump)) {
                     this.pageJumpHistory.addLast(nowPage);
                     if (this.pageJumpHistory.size() > PAGE_JUMP_HISTORY_MAX) {
                        this.pageJumpHistory.removeFirst();
                     }
                  }

                  return true;
               }

               this.mouseDownNode = node;
            } else {
               mouseDownNodeIdx = this.isOnNodeTypeListNode(x, y);
               if (mouseDownNodeIdx >= 0 && mouseDownNodeIdx < this.nodeTypeList.size()) {
                  this.mouseDownNode = this.nodeTypeList.get(mouseDownNodeIdx);
               } else {
                  mouseDownNodeIdx = this.isOnCopyDelListNode(x, y);
                  if (mouseDownNodeIdx >= 0 && mouseDownNodeIdx < copy_DelList.length) {
                     this.mouseDownNode = copy_DelList[mouseDownNodeIdx];
                  }
               }
            }

            if (this.mouseDownNode != null) {
               this.mouseDownNode.gui.mouseDownX = x;
               this.mouseDownNode.gui.mouseDownY = y;
               handled = true;
            }
         } else if (button == 1) {
            mouseDownNodeIdx = this.isOnRingNode(x, y);
            if (mouseDownNodeIdx >= 0 && mouseDownNodeIdx < this.currentPage.nodeList.size()) {
               node = this.currentPage.nodeList.get(mouseDownNodeIdx);
               if (node.hasRefEntry() && !node.isRefEntryDisabled()) {
                  this.lineFromNode = node;
                  handled = true;
               }
            }
         }
      }

      return handled;
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      // 1.12.2 GuiScreen.mouseClickMove was empty, but 1.20.1 delivers drags to the registered
      // widget (needed by GuiVerticalSlider), so the super call is kept here.
      boolean handled = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      int x = (int)mouseX;
      int y = (int)mouseY;
      if (this.draggingNode != null) {
         this.draggingNode.setGuiDraggingPos(x, y);
         if (this.canAddNodeToRing(this.draggingNode) && this.isNearRing(x, y, 16)) {
            this.draggingNode.gui.draggingOnRingIndex = this.calculateRingInsertIndex(x, y, this.currentPage.nodeList.size() + 1);
         } else {
            this.draggingNode.gui.draggingOnRingIndex = -1;
         }
      } else if (this.mouseDownNode != null) {
         int mouseMovedX = this.mouseDownNode.gui.mouseDownX - x;
         int mouseMovedY = this.mouseDownNode.gui.mouseDownY - y;
         if (Math.abs(mouseMovedX) >= 3 || Math.abs(mouseMovedY) >= 3) {
            this.draggingNode = this.mouseDownNode;
            this.mouseDownNode = null;
            this.draggingNode.setGuiDraggingPos(x, y);
            this.draggingNode.gui.draggingOnRingIndex = -1;
            ArrayList<AbstractRingNode> ringNodeList = this.currentPage.nodeList;
            int index = ringNodeList.indexOf(this.draggingNode);
            if (index != -1) {
               if (this.isInSelection(index) && this.countSelected(this.currentPage.nodeList, this.selectedFrom, this.selectedTo) > 1) {
                  GuiRingNodeMultiHandle multiDrag = this.collectSelectedToMultiHandleNode(false);
                  if (multiDrag == null) {
                     return handled;
                  }

                  multiDrag.originalListCopy = (ArrayList<AbstractRingNode>)ringNodeList.clone();
                  multiDrag.originalFrom = this.selectedFrom;
                  multiDrag.originalTo = this.selectedTo;
                  multiDrag.originalListInst = ringNodeList;
                  int mIdx = 0;

                  for (int len = multiDrag.multiNodes.size(); mIdx < len; ++mIdx) {
                     AbstractRingNode each = multiDrag.multiNodes.get(mIdx);
                     int nodeIdx = ringNodeList.indexOf(each);
                     if (nodeIdx != -1) {
                        ringNodeList.remove(each);
                     }
                  }

                  multiDrag.gui.originalIndex = 0;
                  this.draggingNode = multiDrag;
                  this.draggingNode.setGuiDraggingPos(x, y);
               } else {
                  ringNodeList.remove(this.draggingNode);
                  this.draggingNode.gui.originalIndex = index;
               }

               this.onAfterRingElementsIndexChanged();
               this.draggingNode.gui.draggingOnRingIndex = index;
               this.clearSelection();
            } else {
               index = this.nodeTypeList.indexOf(this.draggingNode);
               if (index != -1) {
                  this.draggingNode = this.draggingNode.newInstance().withGui();
                  this.draggingNode.gui.originalIndex = -1;
                  this.draggingNode.gui.draggingOnRingIndex = -1;
                  this.draggingNode.setGuiDraggingPos(x, y);
               } else {
                  index = ArrayUtils.indexOf(copy_DelList, this.draggingNode);
                  if (index != -1) {
                     this.draggingNode = this.draggingNode.cloneNode(this.cloneHelper).withGui();
                     this.draggingNode.gui.originalIndex = -1;
                     this.draggingNode.gui.draggingOnRingIndex = -1;
                     this.draggingNode.setGuiDraggingPos(x, y);
                  }
               }
            }

            handled = true;
         }
      }

      return handled;
   }

   private GuiRingNodeMultiHandle collectSelectedToMultiHandleNode(boolean asClone) {
      ArrayList<AbstractRingNode> ringNodeList = this.currentPage.nodeList;
      int ringSize = ringNodeList.size();
      int fromIdx = this.selectedFrom;
      int toIdx = this.selectedTo;
      if (fromIdx < ringSize && toIdx < ringSize) {
         GuiRingNodeMultiHandle multiDrag = (GuiRingNodeMultiHandle)(new GuiRingNodeMultiHandle()).withGui();
         int idx = fromIdx;
         int idxMod = fromIdx;

         while (true) {
            AbstractRingNode node = ringNodeList.get(idxMod);
            multiDrag.multiNodes.add(node);
            if (idxMod == toIdx) {
               if (asClone) {
                  multiDrag = (GuiRingNodeMultiHandle)multiDrag.cloneNode(this.cloneHelper).withGui();
               }

               return multiDrag;
            }

            ++idx;
            idxMod = idx % ringSize;
         }
      } else {
         return null;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      boolean handled = super.mouseReleased(mouseX, mouseY, button);
      int x = (int)mouseX;
      int y = (int)mouseY;
      int insertIndex;
      int idxSetFromCnt;
      if (button == 0) {
         if (this.mouseDownNode != null) {
            insertIndex = this.currentPage.nodeList.indexOf(this.mouseDownNode);
            if (insertIndex >= 0) {
               if (Screen.hasShiftDown() && this.selectedFrom != -1 && this.selectedTo != -1) {
                  int idxSetToCnt;
                  if (this.selectedFrom != this.selectedTo && !this.isInSelection(insertIndex)) {
                     idxSetFromCnt = this.countSelected(this.currentPage.nodeList, this.selectedFrom, this.selectedTo);
                     idxSetToCnt = this.countSelected(this.currentPage.nodeList, insertIndex, this.selectedTo) - idxSetFromCnt;
                     int idxSetToDiff = this.countSelected(this.currentPage.nodeList, this.selectedFrom, insertIndex) - idxSetFromCnt;
                     if (idxSetToCnt <= idxSetToDiff) {
                        this.selectedFrom = insertIndex;
                     } else {
                        this.selectedTo = insertIndex;
                     }
                  } else {
                     idxSetFromCnt = this.countSelected(this.currentPage.nodeList, insertIndex, this.selectedFirst);
                     idxSetToCnt = this.countSelected(this.currentPage.nodeList, this.selectedFirst, insertIndex);
                     if (idxSetFromCnt < idxSetToCnt) {
                        this.selectedFrom = insertIndex;
                        this.selectedTo = this.selectedFirst;
                     } else {
                        this.selectedFrom = this.selectedFirst;
                        this.selectedTo = insertIndex;
                     }
                  }
               } else {
                  this.setSingleSelection(insertIndex);
               }

               this.playClickSound();
               this.isRingSettingEdtMode = false;
            }
         } else if (this.draggingNode != null) {
            if (this.canAddNodeToRing(this.draggingNode) && this.isNearRing(x, y, 16)) {
               if (this.currentPage.nodeList.size() == 0) {
                  insertIndex = 0;
               } else {
                  insertIndex = this.calculateRingInsertIndex(x, y, this.currentPage.nodeList.size() + 1);
               }

               if (insertIndex >= 0 && insertIndex <= this.currentPage.nodeList.size()) {
                  this.insertNodeToRing(this.draggingNode, insertIndex, false);
               }
            } else {
               this.onNodeRemovedFromList(this.draggingNode);
               if (this.draggingNode.gui.originalIndex != -1) {
                  this.putNodeToCopyDelList(1, this.draggingNode);
               }
            }
         }

         this.mouseDownNode = null;
         this.draggingNode = null;
         handled = true;
      } else if (button == 1) {
         if (this.lineFromNode != null && this.isNearRing(x, y, 10)) {
            insertIndex = this.calculateRingInsertIndex(x, y, this.currentPage.nodeList.size());
            if (insertIndex >= 0 && insertIndex <= this.currentPage.nodeList.size()) {
               idxSetFromCnt = this.lineFromNode.getCurrentLayingRefNum();
               AbstractRingNode lineToNode = this.currentPage.nodeList.get(insertIndex);
               if (this.lineFromNode.canBeLinedWith(idxSetFromCnt, lineToNode)) {
                  this.lineFromNode.setRefNodeIndex(idxSetFromCnt, insertIndex);
                  this.lineFromNode.gui.refNodes[idxSetFromCnt] = this.currentPage.nodeList.get(insertIndex);
                  ++this.lineFromNode.gui.refSetCnt;
               }
            }
         }

         this.lineFromNode = null;
         handled = true;
      }

      return handled;
   }

   private void playClickSound() {
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   private int setSingleSelection(int idx) {
      return this.selectedFirst = this.selectedFrom = this.selectedTo = idx;
   }

   private void setMultiSelection(int fromIdx, int toIdx) {
      this.selectedFirst = this.selectedFrom = fromIdx;
      this.selectedTo = toIdx;
   }

   private int clearSelection() {
      return this.setSingleSelection(-1);
   }

   private void setAllSelected() {
      this.selectedFirst = this.selectedFrom = 0;
      this.selectedTo = this.currentPage.nodeList.size() - 1;
      this.isRingSettingEdtMode = false;
   }

   private int countSelected(ArrayList<AbstractRingNode> nodeList, int from, int to) {
      if (from == to) {
         return 1;
      } else {
         return from < to ? to - from + 1 : to + 1 + (nodeList.size() - from);
      }
   }

   private int calculateRingInsertIndex(int mouseX, int mouseY, int nodeCnt) {
      float diffX = (float)mouseX - (float)this.getRingCenterX();
      float diffY = (float)mouseY - (float)this.getRingCenterY();
      double angleUnit = 6.283185307179586 / (double)nodeCnt;
      double angleRad = 1.5707963267948966 - Math.atan2((double)(-diffY), (double)diffX);
      angleRad += angleUnit / 2.0;
      if (angleRad < 0.0) {
         angleRad += 6.283185307179586;
      }

      return (int)(angleRad / angleUnit);
   }

   private boolean canAddNodeToRing(AbstractRingNode nodeToAdd) {
      int nodeNums = 1;
      if (nodeToAdd instanceof GuiRingNodeMultiHandle) {
         nodeNums = ((GuiRingNodeMultiHandle)nodeToAdd).multiNodes.size();
      }

      return this.currentPage.nodeList.size() + nodeNums <= 24;
   }

   private boolean isInSelection(int nodeIdx) {
      if (this.selectedFrom != -1 && this.selectedTo != -1) {
         if (this.selectedFrom <= this.selectedTo) {
            return nodeIdx >= this.selectedFrom && nodeIdx <= this.selectedTo;
         } else {
            return nodeIdx >= this.selectedFrom || nodeIdx <= this.selectedTo;
         }
      } else {
         return false;
      }
   }

   private void insertNodeToRing(AbstractRingNode node, int insertIndex, boolean backToOriginal) {
      if (node instanceof GuiRingNodeMultiHandle) {
         GuiRingNodeMultiHandle multiNode = (GuiRingNodeMultiHandle)node;
         if (backToOriginal && multiNode.originalListInst == this.currentPage.nodeList) {
            this.currentPage.nodeList = multiNode.originalListCopy;
            this.setMultiSelection(multiNode.originalFrom, multiNode.originalTo);
         } else {
            int numsToInsert = multiNode.multiNodes.size();

            for (int mIdx = numsToInsert - 1; mIdx >= 0; --mIdx) {
               AbstractRingNode each = multiNode.multiNodes.get(mIdx);
               this.insertSinge(each, insertIndex);
            }

            this.setMultiSelection(insertIndex, insertIndex + numsToInsert - 1);
         }
      } else {
         this.insertSinge(node, insertIndex);
         this.setSingleSelection(insertIndex);
      }

      this.onAfterRingElementsIndexChanged();
      this.isRingSettingEdtMode = false;
   }

   private void insertSinge(AbstractRingNode node, int insertIndex) {
      node.gui.hasInitAngle = false;
      this.currentPage.nodeList.add(insertIndex, node);
   }

   private void initAllNodeReferences() {
      int p = 0;

      for (int pLen = this.program.pageList.size(); p < pLen; ++p) {
         ArrayList<AbstractRingNode> ringNodeList = this.program.pageList.get(p).nodeList;
         int nodeIdx = 0;

         for (int nodeCnt = ringNodeList.size(); nodeIdx < nodeCnt; ++nodeIdx) {
            AbstractRingNode nodeFrom = ringNodeList.get(nodeIdx);
            this.updateRefRingIdx2Ref(ringNodeList, nodeFrom);
         }
      }
   }

   private void updateRefRingIdx2Ref(ArrayList<AbstractRingNode> ringNodeList, AbstractRingNode nodeRefFrom) {
      if (nodeRefFrom.hasRefEntry()) {
         int refNum = 0;

         for (int len = nodeRefFrom.getRefCount(); refNum < len; ++refNum) {
            if (nodeRefFrom.isNotInRefMode(refNum)) {
               nodeRefFrom.gui.refNodes[refNum] = null;
            } else if (nodeRefFrom.getRefNodeIndex(refNum) >= 0 && nodeRefFrom.getRefNodeIndex(refNum) < ringNodeList.size()) {
               AbstractRingNode nodeTo = ringNodeList.get(nodeRefFrom.getRefNodeIndex(refNum));
               nodeRefFrom.gui.refNodes[refNum] = nodeTo;
            } else {
               nodeRefFrom.gui.refNodes[refNum] = null;
            }
         }
      }

      if (nodeRefFrom instanceof IOverPageLinker) {
         nodeRefFrom.gui.overPagePageRef = null;
         nodeRefFrom.gui.overPageNodeRef = null;
         IOverPageLinker overPageLinkNode = (IOverPageLinker)nodeRefFrom;
         if (overPageLinkNode.hasOPPageLinkEntry() && overPageLinkNode.hasOPNodeLinkEntry()) {
            RingProgramPage overLinkTargetPage = this.program.getPage(overPageLinkNode.getPageLinkNum());
            if (overLinkTargetPage != null) {
               int nodeIdx = overPageLinkNode.getNodeLinkNum();
               if (nodeIdx >= 0 && nodeIdx < 24) {
                  AbstractRingNode nodeToLink = overLinkTargetPage.getNode((byte)nodeIdx);
                  if (nodeToLink != null) {
                     nodeRefFrom.gui.overPagePageRef = overLinkTargetPage;
                     nodeRefFrom.gui.overPageNodeRef = nodeToLink;
                  }
               }
            }
         }
      }
   }

   private void onAfterRingElementsIndexChanged() {
      int p = 0;

      for (int pLen = this.program.pageList.size(); p < pLen; ++p) {
         ArrayList<AbstractRingNode> ringNodeList = this.program.pageList.get(p).nodeList;
         int nodeIdx = 0;

         for (int nodeCnt = ringNodeList.size(); nodeIdx < nodeCnt; ++nodeIdx) {
            AbstractRingNode nodeFrom = ringNodeList.get(nodeIdx);
            this.updateRefRingRef2Idx(ringNodeList, nodeFrom);
         }
      }
   }

   private void updateRefRingRef2Idx(ArrayList<AbstractRingNode> ringNodeList, AbstractRingNode nodeRefFrom) {
      int pIndex;
      if (nodeRefFrom.hasRefEntry()) {
         int refNum = 0;

         for (pIndex = nodeRefFrom.getRefCount(); refNum < pIndex; ++refNum) {
            if (!nodeRefFrom.isNotInRefMode(refNum)) {
               if (nodeRefFrom.gui.refNodes[refNum] != null) {
                  AbstractRingNode nodeTo = nodeRefFrom.gui.refNodes[refNum];
                  int indexTo = ringNodeList.indexOf(nodeTo);
                  nodeRefFrom.setRefNodeIndex(refNum, indexTo);
               } else {
                  nodeRefFrom.setRefNodeIndex(refNum, -1);
               }
            }
         }
      }

      if (nodeRefFrom instanceof IOverPageLinker) {
         IOverPageLinker overPageLinkNode = (IOverPageLinker)nodeRefFrom;
         if (overPageLinkNode.hasOPPageLinkEntry() && overPageLinkNode.hasOPNodeLinkEntry()) {
            if (nodeRefFrom.gui.overPagePageRef != null && nodeRefFrom.gui.overPageNodeRef != null) {
               pIndex = this.program.pageList.indexOf(nodeRefFrom.gui.overPagePageRef);
               int nIndex = nodeRefFrom.gui.overPagePageRef.nodeList.indexOf(nodeRefFrom.gui.overPageNodeRef);
               if (pIndex != -1 && nIndex != -1) {
                  // 1.12.2 called setNodeLinkNum twice here (verbatim port).
                  overPageLinkNode.setNodeLinkNum(pIndex);
                  overPageLinkNode.setNodeLinkNum(nIndex);
               }
            } else {
               overPageLinkNode.setPageLinkNum(-1);
               overPageLinkNode.setNodeLinkNum(-1);
            }
         }
      }
   }

   private void onNodeRemovedFromList(AbstractRingNode nodeToRemoveFormPgm) {
      if (nodeToRemoveFormPgm instanceof GuiRingNodeMultiHandle) {
         GuiRingNodeMultiHandle multiNode = (GuiRingNodeMultiHandle)nodeToRemoveFormPgm;
         int mIdx = 0;

         for (int len = multiNode.multiNodes.size(); mIdx < len; ++mIdx) {
            AbstractRingNode each = multiNode.multiNodes.get(mIdx);
            this.clearIfReferringNodeConnection(this.currentPage.nodeList, each);
            this.clearIfOverPageReferringNodeConnection(each);
         }
      } else {
         this.clearIfReferringNodeConnection(this.currentPage.nodeList, nodeToRemoveFormPgm);
         this.clearIfOverPageReferringNodeConnection(nodeToRemoveFormPgm);
      }
   }

   private void clearIfReferringNodeConnection(ArrayList<AbstractRingNode> ringNodeList, AbstractRingNode nodeToRemoveFormPgm) {
      int nodeIdx = 0;

      for (int nodeCnt = ringNodeList.size(); nodeIdx < nodeCnt; ++nodeIdx) {
         AbstractRingNode nodeFrom = ringNodeList.get(nodeIdx);
         if (nodeFrom.hasRefEntry()) {
            int refNum = 0;

            for (int len = nodeFrom.getRefCount(); refNum < len; ++refNum) {
               if (nodeFrom.gui.refNodes[refNum] == nodeToRemoveFormPgm) {
                  nodeFrom.gui.refNodes[refNum] = null;
                  nodeFrom.setRefNodeIndex(refNum, -1);
               }
            }
         }
      }
   }

   private void clearIfOverPageReferringNodeConnection(AbstractRingNode nodeToRemoveFormPgm) {
      int p = 0;

      for (int pLen = this.program.pageList.size(); p < pLen; ++p) {
         ArrayList<AbstractRingNode> ringNodeList = this.program.pageList.get(p).nodeList;
         int nodeIdx = 0;

         for (int nodeCnt = ringNodeList.size(); nodeIdx < nodeCnt; ++nodeIdx) {
            AbstractRingNode nodeFrom = ringNodeList.get(nodeIdx);
            if (nodeFrom instanceof IOverPageLinker) {
               IOverPageLinker overPageLinkNode = (IOverPageLinker)nodeFrom;
               if (overPageLinkNode.hasOPPageLinkEntry() && overPageLinkNode.hasOPNodeLinkEntry() && nodeFrom.gui.overPagePageRef == this.currentPage && nodeFrom.gui.overPageNodeRef == nodeToRemoveFormPgm) {
                  overPageLinkNode.setPageLinkNum(-1);
                  overPageLinkNode.setNodeLinkNum(-1);
                  nodeFrom.gui.overPagePageRef = null;
                  nodeFrom.gui.overPageNodeRef = null;
               }
            }
         }
      }
   }

   private void copyAllSelected() {
      if (this.selectedFrom != -1 && this.selectedTo != -1) {
         ArrayList<AbstractRingNode> ringNodeList = this.currentPage.nodeList;
         if (this.countSelected(this.currentPage.nodeList, this.selectedFrom, this.selectedTo) > 1) {
            GuiRingNodeMultiHandle multiNode = this.collectSelectedToMultiHandleNode(true);
            if (multiNode == null) {
               return;
            }

            this.putNodeToCopyDelList(0, multiNode);
         } else {
            AbstractRingNode node = this.getSelectedOnRingNode();
            if (node == null) {
               return;
            }

            this.putNodeToCopyDelList(0, node.cloneNode(this.cloneHelper).withGui());
         }

         this.onAfterRingElementsIndexChanged();
         this.clearSelection();
      }
   }

   private void removeAllSelected(boolean isCutDel) {
      if (this.selectedFrom != -1 && this.selectedTo != -1) {
         ArrayList<AbstractRingNode> ringNodeList = this.currentPage.nodeList;
         AbstractRingNode node;
         if (this.countSelected(this.currentPage.nodeList, this.selectedFrom, this.selectedTo) > 1) {
            GuiRingNodeMultiHandle multiNode = this.collectSelectedToMultiHandleNode(false);
            if (multiNode == null) {
               return;
            }

            int mIdx = 0;

            for (int len = multiNode.multiNodes.size(); mIdx < len; ++mIdx) {
               AbstractRingNode each = multiNode.multiNodes.get(mIdx);
               int nodeIdx = ringNodeList.indexOf(each);
               if (nodeIdx != -1) {
                  ringNodeList.remove(each);
               }
            }

            node = multiNode;
         } else {
            node = this.getSelectedOnRingNode();
            if (node == null) {
               return;
            }

            ringNodeList.remove(node);
         }

         this.onNodeRemovedFromList(node);
         this.putNodeToCopyDelList(isCutDel ? 0 : 1, node);
         this.onAfterRingElementsIndexChanged();
         this.clearSelection();
      }
   }

   private void putNodeToCopyDelList(int cdSlotIndex, AbstractRingNode node) {
      if (cdSlotIndex != 1 || !this.CheckBoxLockDel.isChecked() || copy_DelList[1] == null) {
         if (node instanceof GuiRingNodeMultiHandle) {
            GuiRingNodeMultiHandle multiNode = (GuiRingNodeMultiHandle)node;
            int mIdx = 0;

            for (int len = multiNode.multiNodes.size(); mIdx < len; ++mIdx) {
               AbstractRingNode each = multiNode.multiNodes.get(mIdx);
               if (each.hasRefEntry()) {
                  this.crealReffingNodesIfNotInList(each, multiNode.multiNodes);
               }
            }

            multiNode.clearOriginalListPos();
         } else if (node.hasRefEntry()) {
            this.crealReffingNodesIfNotInList(node, (List<AbstractRingNode>)null);
         }

         node.gui.originalIndex = -1;
         copy_DelList[cdSlotIndex] = node;
         this.copyDelAnimTick[cdSlotIndex] = COPY_DEL_ANIM_TICK_START;
      }
   }

   private void crealReffingNodesIfNotInList(AbstractRingNode refNode, List<AbstractRingNode> list) {
      for (int ref = 0; ref < refNode.getRefCount(); ++ref) {
         if (!refNode.isNotInRefMode(ref)) {
            AbstractRingNode refToNode = refNode.gui.refNodes[ref];
            if (list == null || refToNode != null && !list.contains(refToNode)) {
               refNode.gui.refNodes[ref] = null;
               refNode.setRefNodeIndex(ref, -1);
            }
         }
      }

      if (refNode instanceof IOverPageLinker) {
         IOverPageLinker overPageLinkNode = (IOverPageLinker)refNode;
         if (overPageLinkNode.hasOPPageLinkEntry() && overPageLinkNode.hasOPNodeLinkEntry()) {
            overPageLinkNode.setPageLinkNum(-1);
            overPageLinkNode.setNodeLinkNum(-1);
         }

         refNode.gui.overPagePageRef = null;
         refNode.gui.overPageNodeRef = null;
      }
   }

   private int isOnCopyDelListNode(int mouseX, int mouseY) {
      int listLft = this.getTypeListLeftX();
      int nodeIdx = 0;

      for (int nodeCnt = copy_DelList.length; nodeIdx < nodeCnt; ++nodeIdx) {
         AbstractRingNode node = copy_DelList[nodeIdx];
         if (node != null) {
            float nodeLftX = (float)(listLft + 16);
            float nodeTopY = (float)(this.guiTop + COPY_DEL_LIST_YTOP[nodeIdx]);
            float nodeCenterX = nodeLftX + 8.0F;
            float nodeCenterY = nodeTopY + 8.0F;
            if (this.isOnNode(mouseX, mouseY, nodeCenterX, nodeCenterY)) {
               return nodeIdx;
            }
         }
      }

      return -1;
   }

   private boolean isInNodeTypeList(int mouseX, int mouseY) {
      int listLft = this.getTypeListLeftX();
      int listTop = this.getTypeListTopY();
      int listRht = listLft + 32;
      int listBtm = listTop + 128 + 2;
      return mouseX >= listLft - 13 && mouseX <= listRht && mouseY >= listTop && mouseY <= listBtm;
   }

   private int isOnNodeTypeListNode(int mouseX, int mouseY) {
      int nTypeStartRow = (int)(this.SliderNodeList.getValue() * 8.0);
      int startIndex = nTypeStartRow * 2;
      int endIndexExclude = (nTypeStartRow + 8) * 2;
      int listLft = this.getTypeListLeftX();
      int listTop = this.getTypeListTopY() + 1;

      for (int nodeIdx = startIndex; nodeIdx < endIndexExclude && nodeIdx < this.nodeTypeList.size(); ++nodeIdx) {
         AbstractRingNode node = this.nodeTypeList.get(nodeIdx);
         if (node != null) {
            float nodeLftX = (float)(listLft + nodeIdx % 2 * 16);
            float nodeTopY = (float)(listTop + (nodeIdx / 2 - nTypeStartRow) * 16);
            float nodeCenterX = nodeLftX + 8.0F;
            float nodeCenterY = nodeTopY + 8.0F;
            if (this.isOnNode(mouseX, mouseY, nodeCenterX, nodeCenterY)) {
               return nodeIdx;
            }
         }
      }

      return -1;
   }

   private int isOnRingNode(int mouseX, int mouseY) {
      int ringCenterX = this.getRingCenterX();
      int ringCenterY = this.getRingCenterY();
      int nodeIdx = 0;

      for (int nodeCnt = this.currentPage.nodeList.size(); nodeIdx < nodeCnt; ++nodeIdx) {
         AbstractRingNode node = this.currentPage.nodeList.get(nodeIdx);
         float nodeCenterX = (float)ringCenterX + (float)Math.cos(node.gui.angleOnRing) * (float)this.ringRadius;
         float nodeCenterY = (float)ringCenterY - (float)Math.sin(node.gui.angleOnRing) * (float)this.ringRadius;
         if (this.isOnNode(mouseX, mouseY, nodeCenterX, nodeCenterY)) {
            return nodeIdx;
         }
      }

      return -1;
   }

   protected boolean isOnNode(int mouseX, int mouseY, float nodeCenterX, float nodeCenterY) {
      int diffX = (int)nodeCenterX - mouseX;
      int diffY = (int)nodeCenterY - mouseY;
      return Math.abs(diffX) < 8 && Math.abs(diffY) < 8;
   }

   private boolean isNearRing(int mouseX, int mouseY, int tolerance) {
      int diffX = this.getRingCenterX() - mouseX;
      int diffY = this.getRingCenterY() - mouseY;
      double distFromC = Math.sqrt((double)(diffX * diffX + diffY * diffY));
      return Math.abs(distFromC - (double)this.ringRadius) < (double)tolerance;
   }

   public void saveAndSendRingData() {
      byte[] data = this.program.writeToByteArray();
      if (data != null) {
         CompoundTag tagForSave = new CompoundTag();
         tagForSave.putByteArray("jb:ringp", data);
         if (!JBPacketSender.sendSavedRingDataToServer(tagForSave, this.itemIndex, true)) {
            this.player.displayClientMessage(JBCommonUtil.makeErrorChat("jbring.saveres.maybetoolarge"), false);
         }
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      boolean result = super.mouseScrolled(mouseX, mouseY, delta);
      this.handleMouseWheelInput(mouseX, mouseY, delta);
      return result;
   }

   private void handleMouseWheelInput(double mouseX, double mouseY, double delta) {
      int x = (int)mouseX;
      int y = (int)mouseY;
      int dWheel = delta > 0.0 ? 120 : (delta < 0.0 ? -120 : 0);
      if (dWheel != 0) {
         this.disableAllTextField();
      }

      if (this.isInNodeTypeList(x, y)) {
         this.doWheelSliderWork(dWheel, this.SliderNodeList, 8);
      }

      if (this.isInEntryArea(x, y)) {
         this.doWheelSliderWork(dWheel, this.SliderEntry, 6);
      }
   }

   public boolean isInEntryArea(int mouseX, int mouseY) {
      int Lft = (this.width - this.xSize) / 2;
      int Top = (this.height - this.ySize) / 2;
      return mouseX >= Lft + 243 && mouseX <= this.SliderEntry.getX() + this.SliderEntry.getWidth() && mouseY >= this.SliderEntry.getY() && mouseY <= this.SliderEntry.getY() + this.SliderEntry.getHeight();
   }

   private void doWheelSliderWork(int dWheel, GuiVerticalSlider slider, int sliderRowLimit) {
      int nTypeStartRow;
      double newValue;
      if (dWheel < 0) {
         nTypeStartRow = (int)(slider.getValue() * (double)sliderRowLimit);
         newValue = (double)(nTypeStartRow + 1) / (double)sliderRowLimit;
         if (newValue <= slider.maxValue) {
            slider.setValue(newValue);
         }
      } else if (dWheel > 0) {
         nTypeStartRow = (int)(slider.getValue() * (double)sliderRowLimit);
         newValue = (double)(nTypeStartRow - 1) / (double)sliderRowLimit;
         if (newValue >= slider.minValue) {
            slider.setValue(newValue);
         }
      }
   }

   /**
    * 1.12.2 {@code renderNodeTip}: collects the node tip into {@link #listTooltip} and draws it at
    * (mouseX, mouseY) through {@link #renderTooltip}.
    */
   private void renderNodeTip(GuiGraphics guiGraphics, AbstractRingNode node, int mouseX, int mouseY, boolean showValues) {
      this.listTooltip.clear();
      node.getTip(this.listTooltip, showValues);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   @Override
   public boolean charTyped(char typedChar, int modifiers) {
      String[] oldStrs = new String[this.textFiledAry.length];
      boolean hasKeyTyped = false;

      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         oldStrs[i] = this.textFiledAry[i].getValue();
         hasKeyTyped |= this.textFiledAry[i].charTyped(typedChar, modifiers);
      }

      if (this.TextFieldPageName.charTyped(typedChar, modifiers)) {
         hasKeyTyped = true;
         this.currentPage.setPageName(this.TextFieldPageName.getValue());
      }

      boolean result = hasKeyTyped;
      if (!hasKeyTyped) {
         result = super.charTyped(typedChar, modifiers);
      }

      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         JBGuiStringHelper.fixToValidDecimalString(this.textFiledAry[i], oldStrs[i], -32768, 32767);
      }

      return result;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      String[] oldStrs = new String[this.textFiledAry.length];
      boolean hasKeyTyped = false;

      int i;
      for (i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         oldStrs[i] = this.textFiledAry[i].getValue();
         hasKeyTyped |= this.textFiledAry[i].keyPressed(keyCode, scanCode, modifiers);
         if (this.showTextFieldFlgAry[i] && (keyCode == KEY_ENTER || keyCode == KEY_KP_ENTER)) {
            hasKeyTyped = true;
            double amount = this.getValueFromTextField(i);
            AbstractRingNode selectedNode = this.getSelectedOnRingNode();
            this.setDirectValue(selectedNode, i, amount);
            this.updateRefRingIdx2Ref(this.currentPage.nodeList, selectedNode);
            this.disableAllTextField();
         }
      }

      if (this.TextFieldPageName.keyPressed(keyCode, scanCode, modifiers)) {
         hasKeyTyped = true;
         this.currentPage.setPageName(this.TextFieldPageName.getValue());
      }

      boolean result = hasKeyTyped;
      if (!hasKeyTyped) {
         result = super.keyPressed(keyCode, scanCode, modifiers);
         if (keyCode == Minecraft.getInstance().options.keyJump.getKey().getValue()) {
            this.showNodeTip = !this.showNodeTip;
         }

         boolean isCtrlDwn = Screen.hasControlDown();
         if (keyCode == KEY_A && isCtrlDwn) {
            this.setAllSelected();
         } else if (keyCode == KEY_C && isCtrlDwn) {
            this.copyAllSelected();
         } else if (keyCode == KEY_X && isCtrlDwn) {
            this.removeAllSelected(true);
         } else if (keyCode == KEY_DELETE) {
            this.removeAllSelected(false);
         }
      }

      for (i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         JBGuiStringHelper.fixToValidDecimalString(this.textFiledAry[i], oldStrs[i], -32768, 32767);
      }

      return result;
   }

   private void processBtnRightClickFotTF(int mouseX, int mouseY, int which) {
      if (which == 1) {
         for (int i = 0; i < this.buttonAry.length; ++i) {
            if (!this.showTextFieldFlgAry[i]) {
               if (this.isButtonMousePressed(this.buttonAry[i][0], mouseX, mouseY)) {
                  this.actionPerformedMouseRightClick(this.buttonAry[i][0]);
               } else if (this.isButtonMousePressed(this.buttonAry[i][1], mouseX, mouseY)) {
                  this.actionPerformedMouseRightClick(this.buttonAry[i][1]);
               } else if (this.isButtonMousePressed(this.buttonAry[i][2], mouseX, mouseY)) {
                  this.actionPerformedMouseRightClick(this.buttonAry[i][2]);
               } else if (this.isButtonMousePressed(this.buttonAry[i][3], mouseX, mouseY)) {
                  this.actionPerformedMouseRightClick(this.buttonAry[i][3]);
               }
            }
         }
      }
   }

   private void processTextFieldsFocusChange(int mouseX, int mouseY, int which) {
      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         if (!this.bypathTextFieldClick[i]) {
            this.textFiledAry[i].mouseClicked((double)mouseX, (double)mouseY, which);
            if (!this.textFiledAry[i].isFocused()) {
               this.showTextFieldFlgAry[i] = false;
            }
         } else {
            String str = this.textFiledAry[i].getValue();
            if (!JBGuiStringHelper.isEmptyOrNull(str)) {
               this.textFiledAry[i].moveCursorToStart();
               this.textFiledAry[i].setHighlightPos(str.length());
            }
         }

         this.bypathTextFieldClick[i] = false;
      }

      boolean isTextFocused = this.TextFieldPageName.isFocused();
      this.TextFieldPageName.mouseClicked((double)mouseX, (double)mouseY, which);
      if (isTextFocused && !this.TextFieldPageName.isFocused()) {
         this.currentPage.setPageName(this.TextFieldPageName.getValue());
      }
   }

   public boolean isButtonMousePressed(AbstractWidget button, int mouseX, int mouseY) {
      return button.active && button.visible && mouseX >= button.getX() && mouseY >= button.getY() && mouseX < button.getX() + button.getWidth() && mouseY < button.getY() + button.getHeight();
   }

   private void enableTargetTextField(int idx) {
      this.disableAllTextField();
      this.setValueFormCurrrentValueAry(idx);
      this.showTextFieldFlgAry[idx] = true;
      this.textFiledAry[idx].setFocusedIgnoreVisible(true);
      this.bypathTextFieldClick[idx] = true;
   }

   private void setValueFormCurrrentValueAry(int idx) {
      AbstractRingNode nodeSelected = this.getSelectedOnRingNode();
      if (nodeSelected != null && idx >= 0 && idx < this.textFiledAry.length) {
         this.textFiledAry[idx].setValue(this.formatNumText.format(nodeSelected.getValue(idx)));
      }
   }

   private void disableAllTextField() {
      for (int i = 0; i < this.showTextFieldFlgAry.length; ++i) {
         this.showTextFieldFlgAry[i] = false;
      }
   }

   private double getValueFromTextField(int i) {
      return i >= 0 && i <= this.textFiledAry.length ? JBGuiStringHelper.fixToValidDecimalString(this.textFiledAry[i], "0", -32768, 32767) : 0.0;
   }
}
