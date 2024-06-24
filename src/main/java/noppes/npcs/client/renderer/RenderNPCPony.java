package noppes.npcs.client.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import noppes.npcs.client.model.ModelPony;
import noppes.npcs.client.model.ModelPonyArmor;
import noppes.npcs.entity.EntityNpcPony;

public class RenderNPCPony<T extends EntityNpcPony>
extends RenderNPCInterface<T>
implements IRenderFactory<EntityNpcPony> {
	
	private ModelPonyArmor modelArmor;
	private ModelPonyArmor modelArmorChestplate;
	private ModelPony modelBipedMain;

	public RenderNPCPony() {
		super(new ModelPony(0.0f), 0.5f);
		this.modelBipedMain = (ModelPony) this.mainModel;
		this.modelArmorChestplate = new ModelPonyArmor(1.0f);
		this.modelArmor = new ModelPonyArmor(0.5f);
	}

	@Override
	public Render<? super EntityNpcPony> createRenderFor(RenderManager manager) {
		return null;
	}

	@Override
	public void doRender(T pony, double d, double d1, double d2, float f, float f1) {
		ItemStack itemstack = pony.getHeldItemMainhand();
		ModelPonyArmor modelArmorChestplate = this.modelArmorChestplate;
		ModelPonyArmor modelArmor = this.modelArmor;
		ModelPony modelBipedMain = this.modelBipedMain;
		int heldItemRight;
		int n = heldItemRight = ((itemstack == null) ? 0 : 1);
		modelBipedMain.heldItemRight = n;
		modelArmor.heldItemRight = n;
		modelArmorChestplate.heldItemRight = heldItemRight;
		ModelPonyArmor modelArmorChestplate2 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor2 = this.modelArmor;
		ModelPony modelBipedMain2 = this.modelBipedMain;
		boolean isSneaking = pony.isSneaking();
		modelBipedMain2.isSneak = isSneaking;
		modelArmor2.isSneak = isSneaking;
		modelArmorChestplate2.isSneak = isSneaking;
		ModelPonyArmor modelArmorChestplate3 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor3 = this.modelArmor;
		ModelPony modelBipedMain3 = this.modelBipedMain;
		boolean isRiding = false;
		modelBipedMain3.isRiding = isRiding;
		modelArmor3.isRiding = isRiding;
		modelArmorChestplate3.isRiding = isRiding;
		ModelPonyArmor modelArmorChestplate4 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor4 = this.modelArmor;
		ModelPony modelBipedMain4 = this.modelBipedMain;
		boolean isPlayerSleeping = pony.isPlayerSleeping();
		modelBipedMain4.isSleeping = isPlayerSleeping;
		modelArmor4.isSleeping = isPlayerSleeping;
		modelArmorChestplate4.isSleeping = isPlayerSleeping;
		ModelPonyArmor modelArmorChestplate5 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor5 = this.modelArmor;
		ModelPony modelBipedMain5 = this.modelBipedMain;
		boolean isUnicorn = pony.isUnicorn;
		modelBipedMain5.isUnicorn = isUnicorn;
		modelArmor5.isUnicorn = isUnicorn;
		modelArmorChestplate5.isUnicorn = isUnicorn;
		ModelPonyArmor modelArmorChestplate6 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor6 = this.modelArmor;
		ModelPony modelBipedMain6 = this.modelBipedMain;
		boolean isPegasus = pony.isPegasus;
		modelBipedMain6.isPegasus = isPegasus;
		modelArmor6.isPegasus = isPegasus;
		modelArmorChestplate6.isPegasus = isPegasus;
		if (pony.isSneaking()) {
			d1 -= 0.125;
		}
		super.doRender(pony, d, d1, d2, f, f1);
		ModelPonyArmor modelArmorChestplate7 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor7 = this.modelArmor;
		ModelPony modelBipedMain7 = this.modelBipedMain;
		boolean aimedBow = false;
		modelBipedMain7.aimedBow = aimedBow;
		modelArmor7.aimedBow = aimedBow;
		modelArmorChestplate7.aimedBow = aimedBow;
		ModelPonyArmor modelArmorChestplate8 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor8 = this.modelArmor;
		ModelPony modelBipedMain8 = this.modelBipedMain;
		boolean isRiding2 = false;
		modelBipedMain8.isRiding = isRiding2;
		modelArmor8.isRiding = isRiding2;
		modelArmorChestplate8.isRiding = isRiding2;
		ModelPonyArmor modelArmorChestplate9 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor9 = this.modelArmor;
		ModelPony modelBipedMain9 = this.modelBipedMain;
		boolean isSneak = false;
		modelBipedMain9.isSneak = isSneak;
		modelArmor9.isSneak = isSneak;
		modelArmorChestplate9.isSneak = isSneak;
		ModelPonyArmor modelArmorChestplate10 = this.modelArmorChestplate;
		ModelPonyArmor modelArmor10 = this.modelArmor;
		ModelPony modelBipedMain10 = this.modelBipedMain;
		int heldItemRight2 = 0;
		modelBipedMain10.heldItemRight = heldItemRight2;
		modelArmor10.heldItemRight = heldItemRight2;
		modelArmorChestplate10.heldItemRight = heldItemRight2;
	}

	@Override
	public ResourceLocation getEntityTexture(T pony) {
		boolean check = pony.textureLocation == null || pony.textureLocation != pony.checked;
		ResourceLocation loc = super.getEntityTexture(pony);
		if (check) {
			try {
				IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(loc);
				BufferedImage bufferedimage = ImageIO.read(resource.getInputStream());
				pony.isPegasus = false;
				pony.isUnicorn = false;
				Color color = new Color(bufferedimage.getRGB(0, 0), true);
				Color color2 = new Color(249, 177, 49, 255);
				Color color3 = new Color(136, 202, 240, 255);
				Color color4 = new Color(209, 159, 228, 255);
				Color color5 = new Color(254, 249, 252, 255);
				if (color.equals(color2)) {
				}
				if (color.equals(color3)) {
					pony.isPegasus = true;
				}
				if (color.equals(color4)) {
					pony.isUnicorn = true;
				}
				if (color.equals(color5)) {
					pony.isPegasus = true;
					pony.isUnicorn = true;
				}
				pony.checked = loc;
			} catch (IOException ex) {
			}
		}
		return loc;
	}
}
