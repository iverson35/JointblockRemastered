package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntitySeatPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemJBMouse extends Item implements IColorHanldlerItem {
   public static final String[] mouseNames = new String[]{"blue", "red", "green", "yellow"};
   public static final int[] mouseColors = new int[]{5592507, 12277077, 5618517, 16777045};
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";

   public ItemJBMouse() {
      super((new Item.Properties()).stacksTo(1));
   }

   /**
    * 1.12 damage-value subtype, now stored as int NBT tag "subtype"
    * (flattening to separate items is a Phase 6 decision).
    */
   public static int getSubtype(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null ? tag.getInt("subtype") : 0;
   }

   public ItemStack createSubtypedStack(int subtype) {
      ItemStack stack = new ItemStack(this);
      stack.getOrCreateTag().putInt("subtype", subtype);
      return stack;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = CompatHelper.getCurrentEquippedItem(player);
      if (CompatHelper.isStackEmptyOrNull(stack)) {
         return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
      } else {
         if (player.getVehicle() instanceof EntitySeatPart) {
            int damage = getSubtype(stack);
            this.onMouseItemClicked(player, (EntitySeatPart)player.getVehicle(), damage * 2 + 1);
         }

         return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
      }
   }

   @Override
   public boolean onEntitySwing(ItemStack itemStack, LivingEntity entityLiving) {
      if (entityLiving instanceof Player && entityLiving.getVehicle() instanceof EntitySeatPart) {
         int damage = getSubtype(itemStack);
         this.onMouseItemClicked((Player)entityLiving, (EntitySeatPart)entityLiving.getVehicle(), damage * 2 + 0);
      }

      return true;
   }

   private void onMouseItemClicked(Player player, EntitySeatPart seatPart, int act) {
      Level world = player.level();
      if (!world.isClientSide) {
         seatPart.isRiddenbyUsedItemList[act] = true;
      }

   }

   @Override
   public int getColorFromItemStack(ItemStack itemStack, int pass) {
      int i = getSubtype(itemStack);
      return i >= 0 && i < mouseColors.length ? mouseColors[i] : DEFAULT.getColorFromItemStack(itemStack, pass);
   }

   @Override
   public String getDescriptionId(ItemStack itemStack) {
      int i = getSubtype(itemStack);
      if (i < 0 || i >= mouseNames.length) {
         i = 0;
      }

      return super.getDescriptionId() + "." + mouseNames[i];
   }
}
