package noppes.npcs.client.gui.animation;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.client.model.ModelRendererAlt;
import noppes.npcs.client.model.animation.AddedPartConfig;
import noppes.npcs.constants.EnumParts;

public class SubGuiAddAnimationPart
extends SubGuiInterface
implements ISubGuiListener, ITextfieldListener {

	public boolean isCube;
	private int workU, workV, workS;
	public ModelRendererAlt msr;
	public AddedPartConfig part;

	public SubGuiAddAnimationPart(SubGuiEditAnimation gui) {
		super(gui.npc);
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.setBackground("menubg.png");

		isCube = true;
		msr = new ModelRendererAlt(new ModelNpcAlt(0.0f, false), EnumParts.CUSTOM, 64, 64, true);
		msr.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
		msr.setRotationPoint(0.0F, 0.0F, 0.0F);
		part = new AddedPartConfig(gui.frame.parts.size());
		part.parentPart = gui.part.id;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: {
			isCube = button.getValue() == 1;
			break;
		}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(workU, workV, 1.0f);
		int color = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFF080F0 : 0xFFF020F0;
		this.drawGradientRect(-1, -1, workS + 1, workS + 1, color, color);
		this.drawGradientRect(0, 0, workS, workS, GuiNpcAnimation.backColor, GuiNpcAnimation.backColor);
		GlStateManager.popMatrix();

		Render<?> rnpc = this.mc.getRenderManager().getEntityRenderObject(npc);
		if (rnpc instanceof RenderLivingBase) {
			// RenderLivingBase<?> r = (RenderLivingBase<?>) rnpc;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(workU, workV, 1.0f);
		this.msr.render(0.0625f);
		GlStateManager.popMatrix();

		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.hasSubGui() || !CustomNpcs.ShowDescriptions) {
			return;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		workU = this.guiLeft + this.xSize - 134;
		workV = this.guiTop + this.ySize - 134;
		workS = 128;
		int x = this.guiLeft + 5, y = this.guiTop + 5, lId = 0;
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.type").getFormattedText() + ":", x + 1,
				y + 5));
		this.addButton(
				new GuiNpcButton(0, x + 35, y, 80, 20, new String[] { "gui.cuboid", "gui.obj" }, isCube ? 0 : 1));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		// TODO Auto-generated method stub

	}

}
