package de.jumpingpxl.labymod.customhitboxes.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;

public enum EntityType {
	UNKNOWN,
	PLAYER,
	ANIMAL,
	MOB,
	DROP,
	THROWABLE;

	public static EntityType fromEntity(Entity entity) {
		if (entity instanceof EntityPlayer) {
			return PLAYER;
		}

		if (entity instanceof EntityAnimal || entity instanceof EntityAmbientCreature
				|| entity instanceof EntityWaterMob || entity instanceof EntityVillager) {
			return ANIMAL;
		}

		if (entity instanceof EntityMob) {
			return MOB;
		}

		if (entity instanceof EntityItem || entity instanceof EntityExpBottle) {
			return DROP;
		}

		if (entity instanceof EntityThrowable) {
			return THROWABLE;
		}

		return UNKNOWN;
	}
}
