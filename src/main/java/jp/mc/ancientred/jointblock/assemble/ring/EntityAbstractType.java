package jp.mc.ancientred.jointblock.assemble.ring;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class EntityAbstractType {
   public static final int getAbstractType(Entity entity) {
      if (entity instanceof Player) {
         return 1;
      } else if (entity instanceof Animal) {
         return 2;
      } else if (entity instanceof Villager) {
         return 3;
      } else if (!(entity instanceof Monster) && !(entity instanceof Slime) && !(entity instanceof Ghast)) {
         if (entity instanceof AbstractGolem) {
            return 5;
         } else if (entity instanceof WaterAnimal) {
            return 6;
         } else {
            return entity instanceof Bat ? 7 : 99;
         }
      } else {
         return 4;
      }
   }
}
