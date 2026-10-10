package jp.mc.ancientred.jointblock.assemble;

import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import jp.mc.ancientred.jointblock.api.IJBConsts;
import jp.mc.ancientred.jointblock.api.IJBGuiSettings;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.tileentity.TileEntityJoint;

public class EntryFormats implements IJBConsts {
   public static final int ROAT_VAL_MIN = -359;
   public static final int ROAT_VAL_MAX = 359;
   public static final int SLID_VAL_MIN = -128;
   public static final int SLID_VAL_MAX = 128;
   public static final int EXTN_VAL_MIN = 0;
   public static final int EXTN_VAL_MAX = 300;
   public static final int SCAL_VAL_MIN = 0;
   public static final int SCAL_VAL_MAX = 300;
   public static final IJBGuiSettings.TriButtonState[] selectLRButtonStates = new IJBGuiSettings.TriButtonState[]{new IJBGuiSettings.TriButtonState("<<", ">>", -1.0, 1.0)};
   public static final IJBGuiSettings.TriButtonState[] defaultButtonStates = new IJBGuiSettings.TriButtonState[]{new IJBGuiSettings.TriButtonState("-10", "x10", "+10", -10.0, 10.0), new IJBGuiSettings.TriButtonState("-1", "x1", "+1", -1.0, 1.0), new IJBGuiSettings.TriButtonState("min", "<->", "MAX", -2.147483648E9, 2.147483647E9)};
   public static final IJBGuiSettings.TriButtonState[] sliderAmountButtonStates = new IJBGuiSettings.TriButtonState[]{new IJBGuiSettings.TriButtonState("-16", "x16", "+16", -16.0, 16.0), new IJBGuiSettings.TriButtonState("-1", "x1", "+1", -1.0, 1.0), new IJBGuiSettings.TriButtonState("min", "<->", "MAX", -2.147483648E9, 2.147483647E9)};
   public static final IJBGuiSettings.TriButtonState[] oneOnlyButtonStates = new IJBGuiSettings.TriButtonState[]{new IJBGuiSettings.TriButtonState("-1", "+1", -1.0, 1.0)};
   public static final IJBGuiSettings.TriButtonState[] selectValueButtonStates = new IJBGuiSettings.TriButtonState[]{new IJBGuiSettings.TriButtonState("<<", ">>", -1.0, 1.0)};
   public static final IJBGuiSettings.TriButtonState[] hasDecimalButtonStates = new IJBGuiSettings.TriButtonState[]{new IJBGuiSettings.TriButtonState("-.1", "x.1", "+.1", -0.1, 0.1), new IJBGuiSettings.TriButtonState("-1", "x1", "+1", -1.0, 1.0), new IJBGuiSettings.TriButtonState("-10", "x10", "+10", -10.0, 10.0), new IJBGuiSettings.TriButtonState("min", "<->", "MAX", -2.147483648E9, 2.147483647E9)};
   public static ChoiceFormat rotateZTitleFormat;
   public static ChoiceFormat rotateYTitleFormat;
   public static ChoiceFormat rotateXTitleFormat;
   public static ChoiceFormat slideZTitleFormat;
   public static ChoiceFormat slideYTitleFormat;
   public static ChoiceFormat slideXTitleFormat;
   public static MessageFormat rotateNumFormat;
   public static MessageFormat seatNumValueFormat;
   public static MessageFormat sensorNumValueFormat;
   public static MessageFormat targetNumValueFormat;
   public static MessageFormat seatNumValueFormatRing;
   public static ChoiceFormat keyActionTitleFormat;
   public static MessageFormat keyActionValueFormat;
   public static MessageFormat commonOnOffValueFormat;
   public static MessageFormat commonYRotValueFormat;
   public static MessageFormat commonRotAxisValueFormat;
   public static MessageFormat disableMotValueFormat;
   public static MessageFormat seatEMountTypeValueFormat;
   public static MessageFormat followLookTypeValueFormat;
   public static MessageFormat rotTwdTargetTypeValueFormat;
   public static MessageFormat commonLRSelectValueFormat;
   public static MessageFormat commonOnJumpValueFormat;
   public static MessageFormat insectsOnJumpValueFormat;
   public static MessageFormat limitRotFixValueFormat;
   public static MessageFormat keyValueFormat;
   public static MessageFormat itemValueFormat;
   public static MessageFormat shrinkWayValueFormat;
   public static MessageFormat pendulumPatternValueFormat;
   public static MessageFormat springBackTypeFormat;
   public static MessageFormat seatTypeFormat;
   public static MessageFormat attracterKeyFunctionTypeFormat;
   public static MessageFormat attracterKeyFunctionTypeFormatRing;
   public static MessageFormat modeChangeValueFormat;
   public static MessageFormat mainSubTypeValueFormat;
   public static ChoiceFormat commonOnJumpTitleFormat;
   public static ChoiceFormat insectsOnJumpTitleFormat;
   public static MessageFormat takesTimeValueFormat;
   public static MessageFormat takesTimeValueFormatSimple;
   public static MessageFormat jumpWhenValueFormat;
   public static MessageFormat refNodeTypeValueFormat;
   public static MessageFormat channelRuntypeValueFormat;
   public static MessageFormat moveWayLRValueFormat;
   public static MessageFormat moveWayFBValueFormat;
   public static MessageFormat moveWayUDValueFormat;
   public static MessageFormat followYawTypeValueFormat;
   public static ChoiceFormat yawRotSetTitleFormat;
   public static MessageFormat moveDirChnValueFormat;
   public static MessageFormat ringInputSlotValueFormat;
   public static MessageFormat ringMoveSlotValueFormat;
   public static MessageFormat varV2OprTypeValueFormat;
   public static MessageFormat varV1OprTypeValueFormat;
   public static MessageFormat channelTypeValueFormat;
   public static MessageFormat transWayValueFormat;
   public static MessageFormat entityDetectTypeValueFormat;
   public static MessageFormat blockDetectTypeValueFormat;
   public static MessageFormat playerDetectTypeValueFormat;
   public static MessageFormat targetDetectTypeValueFormat;
   public static MessageFormat sensorDetectTypeValueFormat;
   public static MessageFormat partsDetectTypeValueFormat;
   public static MessageFormat searchOrderValueFormat;
   public static MessageFormat iffTypeValueFormat;
   public static MessageFormat targetEntityTypeValueFormat;
   public static MessageFormat targetTypeValueFormat;
   public static MessageFormat motModifyTypeValueFormat;
   public static MessageFormat unitMoveDetectTypeValueFormat;
   public static MessageFormat unitStateDetectTypeValueFormat;
   public static MessageFormat linkOutsideTypeFormat;
   public static MessageFormat simpleDecimalFormat;
   public static MessageFormat yawDecimalAngleValueFormat;
   public static MessageFormat swingTypeValueFormat;
   public static MessageFormat changeModeTypeValueFormat;
   public static MessageFormat seatDetectTypeValueFormat;
   public static MessageFormat singleordoubleValueFormat;
   public static MessageFormat moveTaskTypeValueFormat;

   public static final void initEntryFormats() {
      DecimalFormat dFmt = new DecimalFormat("#.####");
      simpleDecimalFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      simpleDecimalFormat.setFormats(new Format[]{dFmt});
      yawDecimalAngleValueFormat = new MessageFormat(CompatHelper.formatLang("jbring.cmn.yawdec"));
      yawDecimalAngleValueFormat.setFormats(new Format[]{dFmt});
      slideXTitleFormat = new ChoiceFormat(new double[]{-1.0, 0.0}, new String[]{CompatHelper.formatLang("basicmot.slider.slidex.titleNeg"), CompatHelper.formatLang("basicmot.slider.slidex.titlePos")});
      slideYTitleFormat = new ChoiceFormat(new double[]{-1.0, 0.0}, new String[]{CompatHelper.formatLang("basicmot.slider.slidey.titleNeg"), CompatHelper.formatLang("basicmot.slider.slidey.titlePos")});
      slideZTitleFormat = new ChoiceFormat(new double[]{-1.0, 0.0}, new String[]{CompatHelper.formatLang("basicmot.slider.slidez.titleNeg"), CompatHelper.formatLang("basicmot.slider.slidez.titlePos")});
      rotateXTitleFormat = new ChoiceFormat(new double[]{-1.0, 0.0}, new String[]{CompatHelper.formatLang("basicmot.rotate.rotatez.titleNeg"), CompatHelper.formatLang("basicmot.rotate.rotatez.titlePos")});
      rotateYTitleFormat = new ChoiceFormat(new double[]{-1.0, 0.0}, new String[]{CompatHelper.formatLang("basicmot.rotate.rotatey.titleNeg"), CompatHelper.formatLang("basicmot.rotate.rotatey.titlePos")});
      rotateZTitleFormat = new ChoiceFormat(new double[]{-1.0, 0.0}, new String[]{CompatHelper.formatLang("basicmot.rotate.rotatex.titleNeg"), CompatHelper.formatLang("basicmot.rotate.rotatex.titlePos")});
      rotateNumFormat = new MessageFormat(CompatHelper.formatLang("basicmot.rotate.valueformat"));
      rotateNumFormat.setFormats(new Format[]{NumberFormat.getInstance()});
      seatNumValueFormat = new MessageFormat(CompatHelper.formatLang("basicmot.seat.seatnum.formatcp"));
      seatNumValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{1.0, 2.0}, new String[]{CompatHelper.formatLang("basicmot.seat.seatnum.cocpit"), ""}), NumberFormat.getInstance()});
      sensorNumValueFormat = new MessageFormat(CompatHelper.formatLang("jbring.cmn.sensornum.formatcp"));
      sensorNumValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.cmn.sensornum.unitorigin"), ""}), NumberFormat.getInstance()});
      targetNumValueFormat = new MessageFormat(CompatHelper.formatLang("jbring.cmn.targetnum.formatcp"));
      targetNumValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.cmn.targetnum.owner"), ""}), NumberFormat.getInstance()});
      seatNumValueFormatRing = new MessageFormat(CompatHelper.formatLang("basicmot.seat.seatnum.formatcp"));
      seatNumValueFormatRing.setFormats(new Format[]{new ChoiceFormat(new double[]{1.0, 2.0}, new String[]{CompatHelper.formatLang("jbring.cmn.seatnum.cocpit"), ""}), NumberFormat.getInstance()});
      keyActionTitleFormat = new ChoiceFormat(new double[]{0.0}, new String[]{CompatHelper.formatLang("basicmot.keyaction.title")});
      keyActionValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      keyActionValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbcommon.onoff.donot"), CompatHelper.formatLang("jbcommon.onoff.doit")}), NumberFormat.getInstance()});
      commonOnOffValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      commonOnOffValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbcommon.onoff.donot"), CompatHelper.formatLang("jbcommon.onoff.doit")}), NumberFormat.getInstance()});
      followLookTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      followLookTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("jbcommon.onoff.donot"), CompatHelper.formatLang("jbcommon.onoff.doit"), CompatHelper.formatLang("keymot.onpressedonly.backwithnokey")}), NumberFormat.getInstance()});
      disableMotValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      disableMotValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbcommon.onoff.donot"), CompatHelper.formatLang("jbcommon.disablemot.motonly"), CompatHelper.formatLang("jbcommon.disablemot.actonly"), CompatHelper.formatLang("jbcommon.disablemot.both")}), NumberFormat.getInstance()});
      rotTwdTargetTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      rotTwdTargetTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("keymot.rottwdaxis.xy"), CompatHelper.formatLang("keymot.rottwdaxis.y"), CompatHelper.formatLang("keymot.rottwdaxis.x")}), NumberFormat.getInstance()});
      seatEMountTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      seatEMountTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("keymot.seat.targettype.all"), CompatHelper.formatLang("keymot.seat.targettype.tagged")}), NumberFormat.getInstance()});
      commonYRotValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      commonYRotValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbcommon.yrot.clock"), CompatHelper.formatLang("jbcommon.yrot.cclock")}), NumberFormat.getInstance()});
      commonRotAxisValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      commonRotAxisValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("mechmot.rotaxis.x"), CompatHelper.formatLang("mechmot.rotaxis.y"), CompatHelper.formatLang("mechmot.rotaxis.z")}), NumberFormat.getInstance()});
      commonLRSelectValueFormat = new MessageFormat(CompatHelper.formatLang("cmnmot.selectlr.valueformat"));
      commonLRSelectValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("cmnmot.selectlr.left"), CompatHelper.formatLang("cmnmot.selectlr.right")}), NumberFormat.getInstance()});
      commonOnJumpTitleFormat = new ChoiceFormat(new double[]{3.0, 4.0}, new String[]{CompatHelper.formatLang("cmnmot.onjump.titleA"), CompatHelper.formatLang("cmnmot.onjump.titleB")});
      commonOnJumpValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      commonOnJumpValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0}, new String[]{CompatHelper.formatLang("cmnmot.onjump.type0"), CompatHelper.formatLang("cmnmot.onjump.type1"), CompatHelper.formatLang("cmnmot.onjump.type2"), CompatHelper.formatLang("cmnmot.onjump.type3"), CompatHelper.formatLang("cmnmot.onjump.type0"), CompatHelper.formatLang("cmnmot.onjump.type1"), CompatHelper.formatLang("cmnmot.onjump.type2"), CompatHelper.formatLang("cmnmot.onjump.type3")}), NumberFormat.getInstance()});
      insectsOnJumpTitleFormat = new ChoiceFormat(new double[]{4.0, 5.0}, new String[]{CompatHelper.formatLang("cmnmot.onjump.titleA"), CompatHelper.formatLang("cmnmot.onjump.titleB")});
      insectsOnJumpValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      insectsOnJumpValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0}, new String[]{CompatHelper.formatLang("insecmot.onjump.type0"), CompatHelper.formatLang("insecmot.onjump.type1"), CompatHelper.formatLang("insecmot.onjump.type2"), CompatHelper.formatLang("insecmot.onjump.type3"), CompatHelper.formatLang("insecmot.onjump.type4"), CompatHelper.formatLang("insecmot.onjump.type1"), CompatHelper.formatLang("insecmot.onjump.type2"), CompatHelper.formatLang("insecmot.onjump.type3"), CompatHelper.formatLang("insecmot.onjump.type4")}), NumberFormat.getInstance()});
      limitRotFixValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      limitRotFixValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("cmnmot.limitfix.stopatlimit"), CompatHelper.formatLang("cmnmot.limitfix.fixforward")}), NumberFormat.getInstance()});
      keyValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      keyValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{-1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0}, new String[]{CompatHelper.formatLang("key.jointkeybindN.desc"), CompatHelper.formatLang("key.jointkeybind0.desc"), CompatHelper.formatLang("key.jointkeybind1.desc"), CompatHelper.formatLang("key.jointkeybind2.desc"), CompatHelper.formatLang("key.jointkeybind3.desc"), CompatHelper.formatLang("key.jointkeybind4.desc"), CompatHelper.formatLang("key.jointkeybind5.desc"), CompatHelper.formatLang("key.jointkeybind6.desc"), CompatHelper.formatLang("key.jointkeybind7.desc"), CompatHelper.formatLang("key.jointkeybindF.desc"), CompatHelper.formatLang("key.jointkeybindB.desc"), CompatHelper.formatLang("key.jointkeybindL.desc"), CompatHelper.formatLang("key.jointkeybindR.desc"), CompatHelper.formatLang("key.jointkeybindU.desc"), CompatHelper.formatLang("key.jointkeybindD.desc"), CompatHelper.formatLang("key.jointkeybindMWU.desc"), CompatHelper.formatLang("key.jointkeybindMWD.desc")}), NumberFormat.getInstance()});
      itemValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      itemValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0}, new String[]{CompatHelper.formatLang("cmnmot.itemact.blueLclick"), CompatHelper.formatLang("cmnmot.itemact.blueRclick"), CompatHelper.formatLang("cmnmot.itemact.redLclick"), CompatHelper.formatLang("cmnmot.itemact.redRclick"), CompatHelper.formatLang("cmnmot.itemact.greenLclick"), CompatHelper.formatLang("cmnmot.itemact.greenRclick"), CompatHelper.formatLang("cmnmot.itemact.yellowLclick"), CompatHelper.formatLang("cmnmot.itemact.yellowRclick")}), NumberFormat.getInstance()});
      shrinkWayValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      shrinkWayValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("basicmot.scaler.axis.xyz"), CompatHelper.formatLang("basicmot.scaler.axis.x"), CompatHelper.formatLang("basicmot.scaler.axis.y"), CompatHelper.formatLang("basicmot.scaler.axis.z")}), NumberFormat.getInstance()});
      pendulumPatternValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      pendulumPatternValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("basicmot.pendulum.pattern.tri"), CompatHelper.formatLang("basicmot.pendulum.pattern.sin")}), NumberFormat.getInstance()});
      springBackTypeFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      springBackTypeFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("cmnmot.springbacktype.constant"), CompatHelper.formatLang("cmnmot.springbacktype.percentage")}), NumberFormat.getInstance()});
      seatTypeFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      seatTypeFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("basicmot.seat.type.type0"), CompatHelper.formatLang("basicmot.seat.type.type1"), CompatHelper.formatLang("basicmot.seat.type.type2")}), NumberFormat.getInstance()});
      attracterKeyFunctionTypeFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      attracterKeyFunctionTypeFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("keymot.attr.function.toggle"), CompatHelper.formatLang("keymot.attr.function.purge"), CompatHelper.formatLang("keymot.attr.function.disablete"), CompatHelper.formatLang("keymot.attr.function.purgeanddiste")}), NumberFormat.getInstance()});
      attracterKeyFunctionTypeFormatRing = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      attracterKeyFunctionTypeFormatRing.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0}, new String[]{CompatHelper.formatLang("keymot.attr.function.toggle"), CompatHelper.formatLang("keymot.attr.function.on"), CompatHelper.formatLang("keymot.attr.function.off"), CompatHelper.formatLang("keymot.attr.function.purge"), CompatHelper.formatLang("keymot.attr.function.disablete"), CompatHelper.formatLang("keymot.attr.function.purgeanddiste")}), NumberFormat.getInstance()});
      modeChangeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      modeChangeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{-1.0, 0.0}, new String[]{CompatHelper.formatLang("jbcommon.onoff.off"), CompatHelper.formatLang("jbcommon.valueformat")}), NumberFormat.getInstance()});
      mainSubTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      mainSubTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("jbcommon.mainsubtype.still"), CompatHelper.formatLang("jbcommon.mainsubtype.main"), CompatHelper.formatLang("jbcommon.mainsubtype.sub")}), NumberFormat.getInstance()});
      takesTimeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      takesTimeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{1.0, 2.0}, new String[]{CompatHelper.formatLang("jbring.cmn.immediate"), CompatHelper.formatLang("jbring.cmn.tick")}), NumberFormat.getInstance()});
      takesTimeValueFormatSimple = new MessageFormat(CompatHelper.formatLang("jbring.cmn.tick"));
      jumpWhenValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      jumpWhenValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.jump.jumpwhen.true"), CompatHelper.formatLang("jbring.jump.jumpwhen.false"), CompatHelper.formatLang("jbring.jump.jumpwhen.error"), CompatHelper.formatLang("jbring.jump.jumpwhen.noterror")}), NumberFormat.getInstance()});
      refNodeTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      refNodeTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.cmn.reftype.each"), CompatHelper.formatLang("jbring.cmn.reftype.range")}), NumberFormat.getInstance()});
      channelRuntypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      channelRuntypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.cmn.clear"), CompatHelper.formatLang("jbring.cmn.check")}), NumberFormat.getInstance()});
      changeModeTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      changeModeTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.changemode.mode.init"), CompatHelper.formatLang("jbring.changemode.mode.slave")}), NumberFormat.getInstance()});
      moveWayFBValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      moveWayFBValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.movedo.left"), CompatHelper.formatLang("jbring.movedo.right"), CompatHelper.formatLang("jbring.movedo.leftdbl"), CompatHelper.formatLang("jbring.movedo.rightdbl")}), NumberFormat.getInstance()});
      moveWayLRValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      moveWayLRValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.movedo.front"), CompatHelper.formatLang("jbring.movedo.back"), CompatHelper.formatLang("jbring.movedo.frontdbl"), CompatHelper.formatLang("jbring.movedo.backdbl")}), NumberFormat.getInstance()});
      moveWayUDValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      moveWayUDValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.movedo.up"), CompatHelper.formatLang("jbring.movedo.down"), CompatHelper.formatLang("jbring.movedo.updbl"), CompatHelper.formatLang("jbring.movedo.downdbl")}), NumberFormat.getInstance()});
      followYawTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      followYawTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("jbring.yawflw.const"), CompatHelper.formatLang("jbring.yawflw.accel"), CompatHelper.formatLang("jbring.yawflw.decel")}), NumberFormat.getInstance()});
      yawRotSetTitleFormat = new ChoiceFormat(new double[]{-1.0, 0.0}, new String[]{CompatHelper.formatLang("jbring.yawset.yaw.left"), CompatHelper.formatLang("jbring.yawset.yaw.right")});
      moveTaskTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      moveTaskTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.movetask.type.follow"), CompatHelper.formatLang("jbring.movetask.type.awayfrom"), CompatHelper.formatLang("jbring.movetask.type.wanderarownd"), CompatHelper.formatLang("jbring.movetask.type.wander")}), NumberFormat.getInstance()});
      moveDirChnValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      moveDirChnValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.cmn.movch.frontback"), CompatHelper.formatLang("jbring.cmn.movch.leftright"), CompatHelper.formatLang("jbring.cmn.movch.updown"), CompatHelper.formatLang("jbring.cmn.movch.yaw")}), NumberFormat.getInstance()});
      ringInputSlotValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      ringInputSlotValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0}, new String[]{CompatHelper.formatLang("key.jointkeybind0.desc"), CompatHelper.formatLang("key.jointkeybind1.desc"), CompatHelper.formatLang("key.jointkeybind2.desc"), CompatHelper.formatLang("key.jointkeybind3.desc"), CompatHelper.formatLang("key.jointkeybind4.desc"), CompatHelper.formatLang("key.jointkeybind5.desc"), CompatHelper.formatLang("key.jointkeybind6.desc"), CompatHelper.formatLang("key.jointkeybind7.desc"), CompatHelper.formatLang("cmnmot.itemact.blueLclick"), CompatHelper.formatLang("cmnmot.itemact.blueRclick"), CompatHelper.formatLang("cmnmot.itemact.redLclick"), CompatHelper.formatLang("cmnmot.itemact.redRclick"), CompatHelper.formatLang("cmnmot.itemact.greenLclick"), CompatHelper.formatLang("cmnmot.itemact.greenRclick"), CompatHelper.formatLang("cmnmot.itemact.yellowLclick"), CompatHelper.formatLang("cmnmot.itemact.yellowRclick")}), NumberFormat.getInstance()});
      ringMoveSlotValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      ringMoveSlotValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.movedo.movedirch.frontback"), CompatHelper.formatLang("jbring.movedo.movedirch.leftright"), CompatHelper.formatLang("jbring.movedo.movedirch.updown"), CompatHelper.formatLang("jbring.movedo.movedirch.yaw")}), NumberFormat.getInstance()});
      varV1OprTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      varV1OprTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0}, new String[]{CompatHelper.formatLang("jbring.varopr1v.oprtype.set"), CompatHelper.formatLang("jbring.varopr1v.oprtype.trunc"), CompatHelper.formatLang("jbring.varopr1v.oprtype.round"), CompatHelper.formatLang("jbring.varopr1v.oprtype.floor"), CompatHelper.formatLang("jbring.varopr1v.oprtype.ceil"), CompatHelper.formatLang("jbring.varopr1v.oprtype.not"), CompatHelper.formatLang("jbring.varopr1v.oprtype.abs"), CompatHelper.formatLang("jbring.varopr1v.oprtype.sqrt"), CompatHelper.formatLang("jbring.varopr1v.oprtype.sin"), CompatHelper.formatLang("jbring.varopr1v.oprtype.cos"), CompatHelper.formatLang("jbring.varopr1v.oprtype.tan"), CompatHelper.formatLang("jbring.varopr1v.oprtype.asin"), CompatHelper.formatLang("jbring.varopr1v.oprtype.acos"), CompatHelper.formatLang("jbring.varopr1v.oprtype.atan"), CompatHelper.formatLang("jbring.varopr1v.oprtype.torad"), CompatHelper.formatLang("jbring.varopr1v.oprtype.todeg"), CompatHelper.formatLang("jbring.varopr1v.oprtype.exp"), CompatHelper.formatLang("jbring.varopr1v.oprtype.vecx"), CompatHelper.formatLang("jbring.varopr1v.oprtype.vecy"), CompatHelper.formatLang("jbring.varopr1v.oprtype.vecz"), CompatHelper.formatLang("jbring.varopr1v.oprtype.vecw"), CompatHelper.formatLang("jbring.varopr1v.oprtype.vec3len"), CompatHelper.formatLang("jbring.varopr1v.oprtype.vec3norm"), CompatHelper.formatLang("jbring.varopr1v.oprtype.e"), CompatHelper.formatLang("jbring.varopr1v.oprtype.pi"), CompatHelper.formatLang("jbring.varopr1v.oprtype.err"), CompatHelper.formatLang("jbring.varopr1v.oprtype.rand")}), NumberFormat.getInstance()});
      varV2OprTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      varV2OprTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0}, new String[]{CompatHelper.formatLang("jbring.varopr2v.oprtype.add"), CompatHelper.formatLang("jbring.varopr2v.oprtype.sub"), CompatHelper.formatLang("jbring.varopr2v.oprtype.mul"), CompatHelper.formatLang("jbring.varopr2v.oprtype.div"), CompatHelper.formatLang("jbring.varopr2v.oprtype.mod"), CompatHelper.formatLang("jbring.varopr2v.oprtype.equals"), CompatHelper.formatLang("jbring.varopr2v.oprtype.equalsint"), CompatHelper.formatLang("jbring.varopr2v.oprtype.lesser"), CompatHelper.formatLang("jbring.varopr2v.oprtype.greater"), CompatHelper.formatLang("jbring.varopr2v.oprtype.lesseq"), CompatHelper.formatLang("jbring.varopr2v.oprtype.greateq"), CompatHelper.formatLang("jbring.varopr2v.oprtype.and"), CompatHelper.formatLang("jbring.varopr2v.oprtype.or"), CompatHelper.formatLang("jbring.varopr2v.oprtype.xor"), CompatHelper.formatLang("jbring.varopr2v.oprtype.pow"), CompatHelper.formatLang("jbring.varopr2v.oprtype.min"), CompatHelper.formatLang("jbring.varopr2v.oprtype.max"), CompatHelper.formatLang("jbring.varopr2v.oprtype.atan2"), CompatHelper.formatLang("jbring.varopr2v.oprtype.vec3dot"), CompatHelper.formatLang("jbring.varopr2v.oprtype.vec3dist"), CompatHelper.formatLang("jbring.varopr2v.oprtype.vec3add"), CompatHelper.formatLang("jbring.varopr2v.oprtype.vec3sub"), CompatHelper.formatLang("jbring.varopr2v.oprtype.vec3cross")}), NumberFormat.getInstance()});
      channelTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      channelTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.cmn.channeltype.mot"), CompatHelper.formatLang("jbring.cmn.channeltype.act"), CompatHelper.formatLang("jbring.cmn.channeltype.mov"), CompatHelper.formatLang("jbring.cmn.channeltype.inp")}), NumberFormat.getInstance()});
      transWayValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      transWayValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.cmn.transway.fwd"), CompatHelper.formatLang("jbring.cmn.transway.bwd")}), NumberFormat.getInstance()});
      entityDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      entityDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0}, new String[]{CompatHelper.formatLang("jbring.tgentity.detecttype.alive"), CompatHelper.formatLang("jbring.tgentity.detecttype.eposx"), CompatHelper.formatLang("jbring.tgentity.detecttype.eposy"), CompatHelper.formatLang("jbring.tgentity.detecttype.eposz"), CompatHelper.formatLang("jbring.tgentity.detecttype.eposve"), CompatHelper.formatLang("jbring.tgentity.detecttype.pitch"), CompatHelper.formatLang("jbring.tgentity.detecttype.yaw"), CompatHelper.formatLang("jbring.tgentity.detecttype.eyeposvec"), CompatHelper.formatLang("jbring.tgentity.detecttype.lookvec"), CompatHelper.formatLang("jbring.tgentity.detecttype.riding"), CompatHelper.formatLang("jbring.tgentity.detecttype.rideby"), CompatHelper.formatLang("jbring.tgentity.detecttype.mova"), CompatHelper.formatLang("jbring.tgentity.detecttype.movup"), CompatHelper.formatLang("jbring.tgentity.detecttype.movdw"), CompatHelper.formatLang("jbring.tgentity.detecttype.ridingeid"), CompatHelper.formatLang("jbring.tgentity.detecttype.riddenbyeid"), CompatHelper.formatLang("jbring.tgentity.detecttype.lattackeid"), CompatHelper.formatLang("jbring.tgentity.detecttype.revengeeid"), CompatHelper.formatLang("jbring.tgentity.detecttype.entitytype"), CompatHelper.formatLang("jbring.tgentity.detecttype.entityhasha")}), NumberFormat.getInstance()});
      blockDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      blockDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0}, new String[]{CompatHelper.formatLang("jbring.tgblock.detecttype.isair"), CompatHelper.formatLang("jbring.tgblock.detecttype.blockid"), CompatHelper.formatLang("jbring.tgblock.detecttype.blockmeta"), CompatHelper.formatLang("jbring.tgblock.detecttype.isnormalblock"), CompatHelper.formatLang("jbring.tgblock.detecttype.isopaque"), CompatHelper.formatLang("jbring.tgblock.detecttype.blocksmovement"), CompatHelper.formatLang("jbring.tgblock.detecttype.issolid"), CompatHelper.formatLang("jbring.tgblock.detecttype.isliquid"), CompatHelper.formatLang("jbring.tgblock.detecttype.hardness"), CompatHelper.formatLang("jbring.tgblock.detecttype.opacity"), CompatHelper.formatLang("jbring.tgblock.detecttype.lightvalue"), CompatHelper.formatLang("jbring.tgblock.detecttype.canprovidepower"), CompatHelper.formatLang("jbring.tgblock.detecttype.powerinput"), CompatHelper.formatLang("jbring.tgblock.detecttype.indirectinput")}), NumberFormat.getInstance()});
      playerDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      playerDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0}, new String[]{CompatHelper.formatLang("jbring.tgplayerstats.detecttype.pitch"), CompatHelper.formatLang("jbring.tgplayerstats.detecttype.yawrel"), CompatHelper.formatLang("jbring.tgplayerstats.detecttype.yawabs"), CompatHelper.formatLang("jbring.tgplayerstats.detecttype.swing")}), NumberFormat.getInstance()});
      iffTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      iffTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("jbring.search.iff.any"), CompatHelper.formatLang("jbring.search.iff.ally"), CompatHelper.formatLang("jbring.search.iff.enemy")}), NumberFormat.getInstance()});
      targetEntityTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      targetEntityTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0}, new String[]{CompatHelper.formatLang("jbring.search.etype.living"), CompatHelper.formatLang("jbring.search.etype.player"), CompatHelper.formatLang("jbring.search.etype.jointed"), CompatHelper.formatLang("jbring.search.etype.item"), CompatHelper.formatLang("jbring.search.etype.creachure"), CompatHelper.formatLang("jbring.search.etype.animal"), CompatHelper.formatLang("jbring.search.etype.villager"), CompatHelper.formatLang("jbring.search.etype.boss"), CompatHelper.formatLang("jbring.search.etype.any"), CompatHelper.formatLang("jbring.search.etype.byhash"), CompatHelper.formatLang("jbring.search.etype.clear")}), NumberFormat.getInstance()});
      targetTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      targetTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("jbring.targettype.entity"), CompatHelper.formatLang("jbring.targettype.worldpos"), CompatHelper.formatLang("jbring.targettype.clear")}), NumberFormat.getInstance()});
      targetDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      targetDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0}, new String[]{CompatHelper.formatLang("jbring.tgtarget.detecttype.dist"), CompatHelper.formatLang("jbring.tgtarget.detecttype.yawdiff"), CompatHelper.formatLang("jbring.tgtarget.detecttype.pitchdiff"), CompatHelper.formatLang("jbring.tgtarget.detecttype.enabled"), CompatHelper.formatLang("jbring.tgtarget.detecttype.xdiff"), CompatHelper.formatLang("jbring.tgtarget.detecttype.ydiff"), CompatHelper.formatLang("jbring.tgtarget.detecttype.zdiff"), CompatHelper.formatLang("jbring.tgtarget.detecttype.diffvec"), CompatHelper.formatLang("jbring.tgtarget.detecttype.tposx"), CompatHelper.formatLang("jbring.tgtarget.detecttype.tposy"), CompatHelper.formatLang("jbring.tgtarget.detecttype.tposz"), CompatHelper.formatLang("jbring.tgtarget.detecttype.tposvec"), CompatHelper.formatLang("jbring.tgtarget.detecttype.istargete"), CompatHelper.formatLang("jbring.tgtarget.detecttype.targeteid")}), NumberFormat.getInstance()});
      sensorDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      sensorDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0}, new String[]{CompatHelper.formatLang("jbring.tgsensor.detecttype.sposx"), CompatHelper.formatLang("jbring.tgsensor.detecttype.sposy"), CompatHelper.formatLang("jbring.tgsensor.detecttype.sposz"), CompatHelper.formatLang("jbring.tgsensor.detecttype.sposvec"), CompatHelper.formatLang("jbring.tgsensor.detecttype.swayvec"), CompatHelper.formatLang("jbring.tgsensor.detecttype.searchres0"), CompatHelper.formatLang("jbring.tgsensor.detecttype.searchres1"), CompatHelper.formatLang("jbring.tgsensor.detecttype.searchres2"), CompatHelper.formatLang("jbring.tgsensor.detecttype.searchres3"), CompatHelper.formatLang("jbring.tgsensor.detecttype.searchresvec"), CompatHelper.formatLang("jbring.tgsensor.detecttype.partsid"), CompatHelper.formatLang("jbring.tgsensor.detecttype.enabled")}), NumberFormat.getInstance()});
      partsDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      partsDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0}, new String[]{CompatHelper.formatLang("jbring.tgparts.detecttype.jtype"), CompatHelper.formatLang("jbring.tgparts.detecttype.depth"), CompatHelper.formatLang("jbring.tgparts.detecttype.value_x"), CompatHelper.formatLang("jbring.tgparts.detecttype.value_y"), CompatHelper.formatLang("jbring.tgparts.detecttype.value_z"), CompatHelper.formatLang("jbring.tgparts.detecttype.value_e"), CompatHelper.formatLang("jbring.tgparts.detecttype.value_vec"), CompatHelper.formatLang("jbring.tgparts.detecttype.initv_a"), CompatHelper.formatLang("jbring.tgparts.detecttype.initv_b"), CompatHelper.formatLang("jbring.tgparts.detecttype.initv_c"), CompatHelper.formatLang("jbring.tgparts.detecttype.initv_d"), CompatHelper.formatLang("jbring.tgparts.detecttype.initv_vec"), CompatHelper.formatLang("jbring.tgparts.detecttype.parent"), CompatHelper.formatLang("jbring.tgparts.detecttype.child_d"), CompatHelper.formatLang("jbring.tgparts.detecttype.child_u"), CompatHelper.formatLang("jbring.tgparts.detecttype.child_b"), CompatHelper.formatLang("jbring.tgparts.detecttype.child_f"), CompatHelper.formatLang("jbring.tgparts.detecttype.child_r"), CompatHelper.formatLang("jbring.tgparts.detecttype.child_l"), CompatHelper.formatLang("jbring.tgparts.detecttype.child_a")}), NumberFormat.getInstance()});
      searchOrderValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      searchOrderValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.search.orderby.dist"), CompatHelper.formatLang("jbring.search.orderby.wayvec")}), NumberFormat.getInstance()});
      seatDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      seatDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0}, new String[]{CompatHelper.formatLang("jbring.tgseat.detecttype.seattype"), CompatHelper.formatLang("jbring.tgseat.detecttype.ridbyplayer"), CompatHelper.formatLang("jbring.tgseat.detecttype.ridbyentity"), CompatHelper.formatLang("jbring.tgseat.detecttype.entitytype"), CompatHelper.formatLang("jbring.tgseat.detecttype.entityhasha"), CompatHelper.formatLang("jbring.tgseat.detecttype.entityid")}), NumberFormat.getInstance()});
      unitMoveDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      unitMoveDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0}, new String[]{CompatHelper.formatLang("jbring.tgunitmove.detecttype.yawabs"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.movea"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.moveb"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.moveup"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.movedw"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.inair"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.jumping"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.flying"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.flyhover"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.dashing"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.spinrate"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.limbamount"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.groundfar"), CompatHelper.formatLang("jbring.tgunitmove.detecttype.isgzero")}), NumberFormat.getInstance()});
      unitStateDetectTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      unitStateDetectTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0}, new String[]{CompatHelper.formatLang("jbring.tgunitstat.detecttype.initmode"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.isslave"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.slaveslot"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.masterecon"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.mastertcon"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.health"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.energy"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.efuelpool"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.healthrate"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.energyrate"), CompatHelper.formatLang("jbring.tgunitstat.detecttype.entityid")}), NumberFormat.getInstance()});
      linkOutsideTypeFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      linkOutsideTypeFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0, 2.0}, new String[]{CompatHelper.formatLang("jbring.link.linkoutsidetype.donot"), CompatHelper.formatLang("jbring.link.linkoutsidetype.self"), CompatHelper.formatLang("jbring.link.linkoutsidetype.master")}), NumberFormat.getInstance()});
      motModifyTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      motModifyTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.cmn.modtype.set"), CompatHelper.formatLang("jbring.cmn.modtype.add")}), NumberFormat.getInstance()});
      swingTypeValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      swingTypeValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.cmn.modtype.set"), CompatHelper.formatLang("jbring.cmn.modtype.add")}), NumberFormat.getInstance()});
      singleordoubleValueFormat = new MessageFormat(CompatHelper.formatLang("jbcommon.valueformat"));
      singleordoubleValueFormat.setFormats(new Format[]{new ChoiceFormat(new double[]{0.0, 1.0}, new String[]{CompatHelper.formatLang("jbring.cmn.single"), CompatHelper.formatLang("jbring.cmn.double")}), NumberFormat.getInstance()});
   }

   public static void setDefaultJointTypeButtonState(IJBGuiSettings settings, boolean asInit, int jointType) {
      setDefaultJointTypeButtonState(settings, jointType, asInit, 0);
   }

   public static void setDefaultJointTypeButtonState(IJBGuiSettings settings, int jointType, boolean asInit, int startIndex) {
      switch (jointType) {
         case 2:
            settings.setParamEntryState(startIndex + 0, "basicmot.seat.seatnum.title", seatNumValueFormat, 1, 4);
            settings.setParamButtonToggleAry(startIndex + 0, oneOnlyButtonStates, 0);
            settings.setParamEntryState(startIndex + 1, "basicmot.seat.seathight.title", "basicmot.seat.seathight.format", -16, 16);
            settings.setParamEntryState(startIndex + 2, "basicmot.seat.type.title", seatTypeFormat, 0, 2);
            settings.setParamButtonToggleAry(startIndex + 2, selectLRButtonStates, 0);
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 9:
         case 11:
         case 13:
         default:
            break;
         case 8:
            settings.setParamEntryState(startIndex + 0, rotateXTitleFormat, "jbcommon.xangledegreese.format", -359, 359);
            settings.setParamEntryState(startIndex + 1, rotateYTitleFormat, "jbcommon.yangledegreese.format", -359, 359);
            settings.setParamEntryState(startIndex + 2, rotateZTitleFormat, "jbcommon.zangledegreese.format", -359, 359);
            if (asInit) {
               setDisableMotButtonState(settings, startIndex + 3);
            }
            break;
         case 10:
            settings.setParamEntryState(startIndex + 0, "basicmot.extend.init.title", "jbcommon.percentage.valueformat", 0, 300);
            if (asInit) {
               setDisableMotButtonState(settings, startIndex + 3);
            }
            break;
         case 12:
            settings.setParamEntryState(startIndex + 0, "basicmot.scaler.init.title", "jbcommon.percentage.valueformat", 0, 300);
            if (asInit) {
               setDisableMotButtonState(settings, startIndex + 3);
            }
            break;
         case 14:
            settings.setParamEntryState(startIndex + 0, slideXTitleFormat, "jbcommon.xslideramount.format", -128, 128);
            settings.setParamButtonToggleAry(startIndex + 0, sliderAmountButtonStates, 0);
            settings.setParamEntryState(startIndex + 1, slideYTitleFormat, "jbcommon.yslideramount.format", -128, 128);
            settings.setParamButtonToggleAry(startIndex + 1, sliderAmountButtonStates, 0);
            settings.setParamEntryState(startIndex + 2, slideZTitleFormat, "jbcommon.zslideramount.format", -128, 128);
            settings.setParamButtonToggleAry(startIndex + 2, sliderAmountButtonStates, 0);
            if (asInit) {
               setDisableMotButtonState(settings, startIndex + 3);
            }
            break;
         case 15:
            settings.setParamEntryState(startIndex + 0, "basicmot.attracter.openslot.title", commonOnOffValueFormat, 0, 1);
            settings.setParamButtonToggleAry(startIndex + 0, selectLRButtonStates, 0);
            settings.setParamEntryState(startIndex + 1, "basicmot.attracter.slotnum.title", "jbcommon.simplenum.valueformat", 1, 10);
            settings.setParamButtonToggleAry(startIndex + 1, oneOnlyButtonStates, 0);
            settings.setParamEntryState(startIndex + 2, "basicmot.attracter.offset.title", "jbcommon.attacheramount.format", 0, 128);
            settings.setParamButtonToggleAry(startIndex + 2, sliderAmountButtonStates, 0);
      }

   }

   protected static void setDisableMotButtonState(IJBGuiSettings settings, int paramNum) {
      settings.setParamEntryState(paramNum, "cmnmot.disablemot.title", disableMotValueFormat, 0, 3);
      settings.setParamButtonToggleAry(paramNum, selectValueButtonStates, 0);
   }

   public static void setDefaultKeyActionButtonState(IJBGuiSettings settings, int paramNum) {
      settings.setParamEntryState(paramNum, keyActionTitleFormat, keyActionValueFormat, 0, 1);
      settings.setParamButtonToggleAry(paramNum, selectValueButtonStates, 0);
   }

   public static final void flipDefault(TileEntityJoint joint, int flipWay) {
      int i;
      if (joint.getJointType() == 8) {
         if (flipWay == 2) {
            for(i = 0; i < joint.initValues.length; ++i) {
               joint.initValues[i][1] = -joint.initValues[i][1];
               joint.initValues[i][2] = -joint.initValues[i][2];
            }
         } else if (flipWay == 1) {
            for(i = 0; i < joint.initValues.length; ++i) {
               joint.initValues[i][0] = -joint.initValues[i][0];
               joint.initValues[i][2] = -joint.initValues[i][2];
            }
         }
      } else if (joint.getJointType() == 14) {
         if (flipWay == 2) {
            for(i = 0; i < joint.initValues.length; ++i) {
               joint.initValues[i][0] = -joint.initValues[i][0];
            }
         } else if (flipWay == 1) {
            for(i = 0; i < joint.initValues.length; ++i) {
               joint.initValues[i][1] = -joint.initValues[i][1];
            }
         }
      }

   }
}
