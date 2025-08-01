package noppes.npcs.client.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import noppes.npcs.LogWriter;
import noppes.npcs.client.model.ModelPony;
import noppes.npcs.client.model.ModelPonyArmor;
import noppes.npcs.entity.EntityNpcPony;

public class RenderNPCPony<T extends EntityNpcPony>
extends RenderNPCInterface<T>
implements IRenderFactory<EntityNpcPony> {
	
	private final ModelPonyArmor modelArmor;
	private final ModelPonyArmor modelArmorChestPlate;
	private final ModelPony modelBipedMain;

	public RenderNPCPony() {
		super(new ModelPony(0.0f), 0.5f);
		this.modelBipedMain = (ModelPony) this.mainModel;
		this.modelArmorChestPlate = new ModelPonyArmor(1.0f);
		this.modelArmor = new ModelPonyArmor(0.5f);
	}

	@Override
	public Render<? super EntityNpcPony> createRenderFor(RenderManager manager) {
		return null;
	}

	@Override
	public void doRender(@Nonnull T pony, double d, double d1, double d2, float f, float f1) {
        ModelPonyArmor modelArmor = this.modelArmor;
		ModelPony modelBipedMain = this.modelBipedMain;
		int heldItemRight;
		int n = heldItemRight = 1;
		modelBipedMain.heldItemRight = n;
		modelArmor.heldItemRight = n;
		this.modelArmorChestPlate.heldItemRight = heldItemRight;
        ModelPonyArmor modelArmor2 = this.modelArmor;
		ModelPony modelBipedMain2 = this.modelBipedMain;
		boolean isSneaking = pony.isSneaking();
		modelBipedMain2.isSneak = isSneaking;
		modelArmor2.isSneak = isSneaking;
		this.modelArmorChestPlate.isSneak = isSneaking;
        ModelPonyArmor modelArmor3 = this.modelArmor;
		ModelPony modelBipedMain3 = this.modelBipedMain;
		boolean isRiding = false;
		modelBipedMain3.isRiding = isRiding;
		modelArmor3.isRiding = isRiding;
		this.modelArmorChestPlate.isRiding = isRiding;
        ModelPonyArmor modelArmor4 = this.modelArmor;
		ModelPony modelBipedMain4 = this.modelBipedMain;
		boolean isPlayerSleeping = pony.isPlayerSleeping();
		modelBipedMain4.isSleeping = isPlayerSleeping;
		modelArmor4.isSleeping = isPlayerSleeping;
		this.modelArmorChestPlate.isSleeping = isPlayerSleeping;
        ModelPonyArmor modelArmor5 = this.modelArmor;
		ModelPony modelBipedMain5 = this.modelBipedMain;
		boolean isUnicorn = pony.isUnicorn;
		modelBipedMain5.isUnicorn = isUnicorn;
		modelArmor5.isUnicorn = isUnicorn;
		this.modelArmorChestPlate.isUnicorn = isUnicorn;
        ModelPonyArmor modelArmor6 = this.modelArmor;
		ModelPony modelBipedMain6 = this.modelBipedMain;
		boolean isPegasus = pony.isPegasus;
		modelBipedMain6.isPegasus = isPegasus;
		modelArmor6.isPegasus = isPegasus;
		this.modelArmorChestPlate.isPegasus = isPegasus;
		if (pony.isSneaking()) {
			d1 -= 0.125;
		}
		super.doRender(pony, d, d1, d2, f, f1);
        ModelPonyArmor modelArmor7 = this.modelArmor;
		ModelPony modelBipedMain7 = this.modelBipedMain;
		boolean aimedBow = false;
		modelBipedMain7.aimedBow = aimedBow;
		modelArmor7.aimedBow = aimedBow;
		this.modelArmorChestPlate.aimedBow = aimedBow;
        ModelPonyArmor modelArmor8 = this.modelArmor;
		ModelPony modelBipedMain8 = this.modelBipedMain;
		boolean isRiding2 = false;
		modelBipedMain8.isRiding = isRiding2;
		modelArmor8.isRiding = isRiding2;
		this.modelArmorChestPlate.isRiding = isRiding2;
        ModelPonyArmor modelArmor9 = this.modelArmor;
		ModelPony modelBipedMain9 = this.modelBipedMain;
		boolean isSneak = false;
		modelBipedMain9.isSneak = isSneak;
		modelArmor9.isSneak = isSneak;
		this.modelArmorChestPlate.isSneak = isSneak;
        ModelPonyArmor modelArmor10 = this.modelArmor;
		ModelPony modelBipedMain10 = this.modelBipedMain;
		int heldItemRight2 = 0;
		modelBipedMain10.heldItemRight = heldItemRight2;
		modelArmor10.heldItemRight = heldItemRight2;
		this.modelArmorChestPlate.heldItemRight = heldItemRight2;
	}

	@Override
	public ResourceLocation getEntityTexture(@Nonnull T pony) {
		boolean check = pony.textureLocation == null || pony.textureLocation != pony.checked;
		ResourceLocation loc = super.getEntityTexture(pony);
		if (check && loc != null) {
			try {
				IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(loc);
				BufferedImage bufferedimage = ImageIO.read(resource.getInputStream());
				pony.isPegasus = false;
				pony.isUnicorn = false;
				Color color = new Color(bufferedimage.getRGB(0, 0), true);
                Color color3 = new Color(136, 202, 240, 255);
				Color color4 = new Color(209, 159, 228, 255);
				Color color5 = new Color(254, 249, 252, 255);
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
			} catch (IOException e) { LogWriter.error(e); }
		}
		return loc;
	}
}
