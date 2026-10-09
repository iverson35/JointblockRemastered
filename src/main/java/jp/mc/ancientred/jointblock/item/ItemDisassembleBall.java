package jp.mc.ancientred.jointblock.item;

import jp.mc.ancientred.jointblock.common.CompatHelper;
import jp.mc.ancientred.jointblock.entity.EntityDisassembleBall;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemDisassembleBall extends Item {
   public static final int TYPE_DISASSEMBLE = 0;
   public static final int TYPE_MOUNT = 1;
   public static final int TYPE_CAPSULE = 2;
   public static final String NBT_TAGNAME_SUBTYPE = "subtype";
   private String[] SUB_NAMES = new String[]{"disas", "mount"};

   public ItemDisassembleBall() {
      super((new Item.Properties()).stacksTo(64));
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
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }

         world.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
         if (!world.isClientSide) {
            EntityDisassembleBall dab = new EntityDisassembleBall(world, player);
            if (getSubtype(stack) == 1) {
               dab.type = EntityDisassembleBall.BallType.mount;
            }

            dab.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(dab);
         }

         return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
      }
   }

   @Override
   public String getDescriptionId(ItemStack itemStack) {
      int i = getSubtype(itemStack);
      if (i < 0 || i >= this.SUB_NAMES.length) {
         i = 0;
      }

      return super.getDescriptionId() + "." + this.SUB_NAMES[i];
   }
}
