package jp.mc.ancientred.jointblock.registry;

import jp.mc.ancientred.jointblock.JointBlockRemastered;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative tab. The 1.12.2 mod had an anonymous {@code CreativeTabs} whose icon was
 * the joint-normal block; the 1.20.1 equivalent is a registered
 * {@link CreativeModeTab} filled from item suppliers.
 *
 * <p>Subtype enumeration: in 1.12.2 items that had damage-value subtypes overrode
 * {@code func_150895_a} to add one {@code ItemStack} per damage value. The port stores
 * those subtypes in the int NBT tag {@code "subtype"} instead, so the same variants are
 * enumerated here by calling {@code createSubtypedStack(i)} once per subtype, in ascending
 * subtype order. Items without a 1.12.2 enumeration are still added as a single plain stack.
 * The per-item subtype count comes from the port item class (its {@code SUB_NAMES} array,
 * {@code mouseNames}/{@code seatNames} array, or the enumerations hard-coded in the original
 * {@code func_150895_a}); each block below cites its source.
 */
public final class JBCreativeTab {

    private JBCreativeTab() {
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JointBlockRemastered.MOD_ID);

    public static final RegistryObject<CreativeModeTab> JOINT_BLOCK_TAB = CREATIVE_MODE_TABS.register(
        "jointblock",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + JointBlockRemastered.MOD_ID))
            .icon(() -> new ItemStack(JBItems.JointNormalItem))
            .displayItems((params, output) -> {
                output.accept(JBItems.JointCoreItem);
                output.accept(JBItems.JointSeatItem);
                output.accept(JBItems.JointNormalItem);
                output.accept(JBItems.JointRotatorItem);
                output.accept(JBItems.JointExtenderItem);
                output.accept(JBItems.JointScalerItem);
                output.accept(JBItems.JointSliderItem);
                output.accept(JBItems.JointAttracterItem);
                output.accept(JBItems.BlueprintBuilderItem);
                output.accept(JBItems.JBControllerItem);
                // ItemJBMouse: mouseNames 4 entries (ItemJBMouse.java:17); original func_150895_a loops i < 4
                for (int i = 0; i < 4; i++) {
                    output.accept(JBItems.JBMouseItem.createSubtypedStack(i));
                }
                // ItemDisassembleBall: SUB_NAMES length 2 (ItemDisassembleBall.java:21)
                for (int i = 0; i < 2; i++) {
                    output.accept(JBItems.DisassembleBallItem.createSubtypedStack(i));
                }
                output.accept(JBItems.AssemblyTunerItem);
                // ItemTurnWrench: SUB_NAMES length 4 (ItemTurnWrench.java:18)
                for (int i = 0; i < 4; i++) {
                    output.accept(JBItems.TurnWrenchItem.createSubtypedStack(i));
                }
                output.accept(JBItems.UnitPartsItem);
                // ItemCeramicPutty: original func_150895_a adds damage 0 and 2 only
                output.accept(JBItems.CeramicPuttyItem.createSubtypedStack(0));
                output.accept(JBItems.CeramicPuttyItem.createSubtypedStack(2));
                output.accept(JBItems.CeramicChiselItem);
                output.accept(JBItems.PaintGunItem);
                output.accept(JBItems.PatternCutterItem);
                // ItemJBCopyTool: SUB_NAMES length 4 (ItemJBCopyTool.java:31)
                for (int i = 0; i < 4; i++) {
                    output.accept(JBItems.CopyToolItem.createSubtypedStack(i));
                }
                // ItemJBIFFTag: SUB_NAMES length 2 (ItemJBIFFTag.java:38)
                for (int i = 0; i < 2; i++) {
                    output.accept(JBItems.JBIFFTagItem.createSubtypedStack(i));
                }
                output.accept(JBItems.JBMirrorToolItem);
                // ItemJBBlueprint: original func_150895_a adds damage 0 and 1 only
                output.accept(JBItems.BluePrintItem.createSubtypedStack(0));
                output.accept(JBItems.BluePrintItem.createSubtypedStack(1));
                output.accept(JBItems.RingProgramItem);
                output.accept(JBItems.TargetSightItem);
                output.accept(JBItems.JBEffectItem);
                // ItemKeyBindedMotion: seatNames 4 entries (KeyMotionBinder.java:61); original loops i < 4
                for (int i = 0; i < 4; i++) {
                    output.accept(JBItems.KeyBindedMotionItem.createSubtypedStack(i));
                }
                // ItemItemBindedMotion: seatNames 4 entries (ItemMotionBinder.java:46); original loops i < 4
                for (int i = 0; i < 4; i++) {
                    output.accept(JBItems.ItemBindedMotionItem.createSubtypedStack(i));
                }
                output.accept(JBItems.HumanUpperMotionItem);
                output.accept(JBItems.HumanLowerMotionItem);
                output.accept(JBItems.BeastLowerMotionItem);
                output.accept(JBItems.InsectsMotionItem);
                output.accept(JBItems.MachineMotionItem);
                output.accept(JBItems.RingMotionItem);
                output.accept(JBItems.SensorMotionItem);
            })
            .build());

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
