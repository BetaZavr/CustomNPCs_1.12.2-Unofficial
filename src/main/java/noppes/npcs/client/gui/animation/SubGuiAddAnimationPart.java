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
import noppes.npcs.client.model.ModelNPCAlt;
import noppes.npcs.client.model.ModelScaleRenderer;
import noppes.npcs.client.model.animation.AddedPartConfig;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiAddAnimationPart
extends SubGuiInterface
implements ISubGuiListener, ITextfieldListener{

	public boolean isCube;
	private int workU, workV, workS;
	public ModelScaleRenderer msr;
	public AddedPartConfig part;
	
	public SubGuiAddAnimationPart(EntityNPCInterface npc, int id, int partId) {
		super(npc);
		this.id = id;
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.setBackground("menubg.png");
		
		isCube = true;
		msr = new ModelScaleRenderer(new ModelNPCAlt(1.0f, false), 64, 64, EnumParts.CUSTOM);
		msr.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F);
		msr.setRotationPoint(0.0F, 0.0F, 0.0F);
		part = new AddedPartConfig(partId);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		workU = this.guiLeft + this.xSize - 134;
		workV = this.guiTop+ this.ySize - 134;
		workS = 128;
		int x = this.guiLeft + 5, y = this.guiTop + 5, lId = 0;
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.type").getFormattedText()+":", x + 1, y + 5));
		this.addButton(new GuiNpcButton(0, x + 35, y , 80, 20, new String[] { "gui.cuboid", "gui.obj" }, isCube ? 0 : 1));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(workU, workV, 1.0f);
		int color = GuiNpcAnimation.backColor==0xFF000000 ? 0xFFF080F0 : 0xFFF020F0;
		this.drawGradientRect(-1, -1, workS + 1, workS + 1, color, color);
		this.drawGradientRect(0, 0, workS, workS, GuiNpcAnimation.backColor, GuiNpcAnimation.backColor);
		GlStateManager.popMatrix();
		
		Render<?> rnpc = this.mc.getRenderManager().getEntityRenderObject(npc);
		if (rnpc instanceof RenderLivingBase) {
			RenderLivingBase<?> r = (RenderLivingBase<?>) rnpc;
			System.out.println("CNPCs: "+r.getMainModel());
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(workU, workV, 1.0f);
		this.msr.render(0.0625f);
		GlStateManager.popMatrix();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.hasSubGui() || !CustomNpcs.showDescriptions) { return; }
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		System.out.println("buttonID: "+button.id);
		switch(button.id) {
			case 0: {
				isCube = button.getValue() == 1;
				break;
			}
		}
	}
		
	@Override
	public void unFocused(GuiNpcTextField textField) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		// TODO Auto-generated method stub
		
	}

}
