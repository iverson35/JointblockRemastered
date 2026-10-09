package jp.mc.ancientred.jointblock.item;

import java.util.List;
import jp.mc.ancientred.jointblock.JBModContainer;
import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.gui.JBGuiIds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemRingProgram extends Item {
   public static final String NBT_TAGNAME_RINGPROGRAM = "jb:ringp";
   public static final String NBT_TAGNAME_RINGZERONAME = "jb:ringnm";

   public ItemRingProgram() {
      super((new Item.Properties()).stacksTo(1));
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (world.isClientSide) {
         JBModContainer.proxy.openGui(JBGuiIds.guiRingProgramId, player, world, BlockPos.ZERO);
      }

      return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
   }

   @Override
   public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
      CompoundTag nbttagcompound = getRingProgramTag(stack);
      if (nbttagcompound != null) {
         list.add(Component.translatable("jbInfo.hascopydata"));
         list.add(Component.literal(nbttagcompound.getString("jb:ringnm")));
      }

   }

   public static boolean isCurrentItemRing(Player player) {
      return player != null && player.getInventory() != null ? isRingProgramItem(player.getInventory().getSelected()) : false;
   }

   public static boolean isRingProgramItem(ItemStack itemStack) {
      return !CompatHelper.isStackEmptyOrNull(itemStack) && itemStack.getItem() instanceof ItemRingProgram;
   }

   public static byte[] getRingProgramData(ItemStack itemStack) {
      CompoundTag tag;
      return (tag = getRingProgramTag(itemStack)) != null ? tag.getByteArray("jb:ringp") : null;
   }

   public static CompoundTag getRingProgramTag(ItemStack itemStack) {
      if (itemStack.hasTag()) {
         CompoundTag tag = itemStack.getTag();
         if (tag.contains("jb:ringp", 7)) {
            return tag;
         }
      }

      return null;
   }

   public static void setRingProgramFromTag(ItemStack itemStack, CompoundTag tag) {
      if (tag != null && tag.contains("jb:ringp", 7)) {
         CompoundTag tagStack;
         if (itemStack.hasTag()) {
            tagStack = itemStack.getTag();
         } else {
            itemStack.setTag(tagStack = new CompoundTag());
         }

         tagStack.putByteArray("jb:ringp", tag.getByteArray("jb:ringp"));
         if (tag.contains("jb:ringnm", 8)) {
            tagStack.putString("jb:ringnm", tag.getString("jb:ringnm"));
         }
      }

   }
}
