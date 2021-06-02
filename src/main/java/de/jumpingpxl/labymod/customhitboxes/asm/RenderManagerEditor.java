package de.jumpingpxl.labymod.customhitboxes.asm;

import de.jumpingpxl.labymod.customhitboxes.CustomHitboxes;
import de.jumpingpxl.labymod.customhitboxes.util.Color;
import de.jumpingpxl.labymod.customhitboxes.util.EntityType;
import de.jumpingpxl.labymod.customhitboxes.util.Settings;
import net.labymod.api.permissions.Permissions;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.core.asm.LabyModTransformer;
import net.labymod.core.asm.global.ClassEditor;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;

public class RenderManagerEditor extends ClassEditor {

	public static final String RENDER_MANAGER_CLASS = (LabyModCoreMod.isObfuscated() ? "biu"
			: "net/minecraft/client/renderer/entity/RenderManager");
	private static final String RENDER_MANAGER_EDITOR_CLASS =
			"de/jumpingpxl/labymod/customhitboxes" + "/asm/RenderManagerEditor";
	private static final String ENTITY_CLASS = LabyModTransformer.getMappingImplementation()
			.getEntityClassName();
	private static final String DO_RENDER_ENTITY_METHOD =
			LabyModTransformer.getMappingImplementation()
			.getDoRenderEntityName();
	private static final String RENDER_DEBUG_BOUNDING_BOX_METHOD =
			LabyModTransformer.getMappingImplementation()
			.getRenderDebugBoundingBoxName();
	private static final String DEBUG_BOUNDING_BOX_FIELD = (LabyModCoreMod.isObfuscated() ? "t"
			: "debugBoundingBox");

	public RenderManagerEditor() {
		super(ClassEditor.ClassEditorType.CLASS_VISITOR);
	}

	public static boolean debugBoundingBox() {
		return Minecraft.getMinecraft().getRenderManager().isDebugBoundingBox()
				|| CustomHitboxes.getSettings().isEnabled();
	}

	public static void renderDebugBoundingBox(Entity entity, double x, double y, double z,
	                                          float entityYaw, float partialTicks) {
		Settings settings = CustomHitboxes.getSettings();
		Color color = getColor(settings, entity);
		if (Objects.isNull(color)) {
			if (Minecraft.getMinecraft().getRenderManager().isDebugBoundingBox()) {
				color = settings.getColor();
			} else {
				return;
			}
		}

		GlStateManager.depthMask(false);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
		AxisAlignedBB axisAlignedBB = new AxisAlignedBB(entityBoundingBox.minX - entity.posX + x,
				entityBoundingBox.minY - entity.posY + y, entityBoundingBox.minZ - entity.posZ + z,
				entityBoundingBox.maxX - entity.posX + x, entityBoundingBox.maxY - entity.posY + y,
				entityBoundingBox.maxZ - entity.posZ + z);
		RenderGlobal.drawOutlinedBoundingBox(axisAlignedBB, color.getRed(), color.getGreen(),
				color.getBlue(), color.getAlpha());

		if (entity instanceof EntityLivingBase && settings.isEyeHeightBoxEnabled()) {
			float f = entity.width / 2.0F;
			Color eyeHeightBoxColor = settings.getEyeHeightBoxColor();
			AxisAlignedBB eyeHeightBox = new AxisAlignedBB(x - f, y + entity.getEyeHeight() - 0.01F,
					z - f, x + f, y + entity.getEyeHeight() + 0.01F, z + f);
			RenderGlobal.drawOutlinedBoundingBox(eyeHeightBox, eyeHeightBoxColor.getRed(),
					eyeHeightBoxColor.getGreen(), eyeHeightBoxColor.getBlue(), eyeHeightBoxColor.getAlpha());
		}

		if (!(LabyMod.getSettings().playerAnimation && LabyMod.getSettings().oldHitbox
				&& Permissions.isAllowed(Permissions.Permission.ANIMATIONS))) {
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			Vec3 vec3 = entity.getLook(partialTicks);
			worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos(x, y + entity.getEyeHeight(), z).color(0, 0, 255, 255).endVertex();
			worldrenderer.pos(x + vec3.xCoord * 2.0D, y + entity.getEyeHeight() + vec3.yCoord * 2.0D,
					z + vec3.zCoord * 2.0D).color(0, 0, 255, 255).endVertex();
			tessellator.draw();
		}

		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	private static Color getColor(Settings settings, Entity entity) {
		Color color = null;
		switch (EntityType.fromEntity(entity)) {
			case PLAYER:
				if (settings.isPlayersEnabled() && !(Minecraft.getMinecraft().thePlayer == entity
						&& !settings.isSelfEnabled())) {
					color = settings.isOwnColorPlayers() ? settings.getPlayerColor() : settings.getColor();
				}

				break;
			case ANIMAL:
				if (settings.isAnimalsEnabled()) {
					color = settings.isOwnColorAnimals() ? settings.getAnimalColor() : settings.getColor();
				}

				break;
			case MOB:
				if (settings.isMobsEnabled()) {
					color = settings.isOwnColorMobs() ? settings.getMobColor() : settings.getColor();
				}

				break;
			case DROP:
				if (settings.isDropsEnabled()) {
					color = settings.isOwnColorDrops() ? settings.getDropColor() : settings.getColor();
				}

				break;
			case THROWABLE:
				if (settings.isThrowablesEnabled()) {
					color =
							settings.isOwnColorThrowables() ? settings.getThrowableColor() : settings.getColor();
				}

				break;
			default:

				break;
		}

		return color;
	}

	@Override
	public void accept(String name, ClassNode node) {
		for (MethodNode method : node.methods) {
			if (method.name.equals(DO_RENDER_ENTITY_METHOD) && method.desc.startsWith(
					"(L" + ENTITY_CLASS + ";DDDFFZ)")) {
				for (int i = 0; i < method.instructions.size(); i++) {
					AbstractInsnNode abstractInsnNode = method.instructions.get(i);
					if (abstractInsnNode.getOpcode() == Opcodes.GETFIELD) {
						FieldInsnNode field = (FieldInsnNode) abstractInsnNode;
						if (field.owner.equals(RENDER_MANAGER_CLASS) && field.name.equals(
								DEBUG_BOUNDING_BOX_FIELD) && field.desc.equals("Z")) {
							method.instructions.insert(abstractInsnNode,
									new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_MANAGER_EDITOR_CLASS,
											"debugBoundingBox", "()Z", false));
							method.instructions.remove(abstractInsnNode.getPrevious());
							method.instructions.remove(abstractInsnNode);
						}
					} else if (abstractInsnNode.getOpcode() == Opcodes.INVOKESPECIAL) {
						MethodInsnNode methodNode = (MethodInsnNode) abstractInsnNode;
						if (methodNode.owner.equals(RENDER_MANAGER_CLASS) && methodNode.name.equals(
								RENDER_DEBUG_BOUNDING_BOX_METHOD) && methodNode.desc.equals(
								"(L" + ENTITY_CLASS + ";DDDFF)V")) {
							method.instructions.insert(abstractInsnNode,
									new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_MANAGER_EDITOR_CLASS,
											"renderDebugBoundingBox", "(L" + ENTITY_CLASS + ";DDDFF)V", false));
							method.instructions.remove(method.instructions.get(i - 7));
							method.instructions.remove(abstractInsnNode);
						}
					}
				}

				break;
			}
		}

		super.accept(name, node);
	}
}