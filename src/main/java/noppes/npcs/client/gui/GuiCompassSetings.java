package noppes.npcs.client.gui;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientGuiEventHandler;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.PlayerCompassHUDData;

public class GuiCompassSetings
extends GuiNPCInterface
implements ISliderListener, ITextfieldListener {
	
	private GuiScreen parent;
	private PlayerCompassHUDData compassData;

	public GuiCompassSetings(GuiScreen parent) {
		super();
		this.xSize = 256;
		this.ySize = 217;
		this.parent = parent;
		this.compassData = ClientProxy.playerData.hud.compassData;
		this.setBackground("menubg.png");
		this.closeOnEsc = true;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "quest.screen.pos", this.guiLeft + 4, this.guiTop + 4));
		
		//Screen Pos
		this.addLabel(new GuiNpcLabel(1, "U:", this.guiLeft + 4, this.guiTop + 102));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 14, this.guiTop + 99, 40, 15, ""+this.compassData.screenPos[0]));
		this.getTextField(0).setDoubleNumbersOnly();
		this.getTextField(0).setMinMaxDoubleDefault(0.0d, 1.0d, this.compassData.screenPos[0]);

		this.addLabel(new GuiNpcLabel(2, "V:", this.guiLeft + 60, this.guiTop + 102));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 70, this.guiTop + 99, 40, 15, ""+this.compassData.screenPos[1]));
		this.getTextField(1).setDoubleNumbersOnly();
		this.getTextField(1).setMinMaxDoubleDefault(0.0d, 1.0d, this.compassData.screenPos[1]);
		
		// Scale
		this.addLabel(new GuiNpcLabel(3, "model.scale", this.guiLeft + 4, this.guiTop + 118));
		float v = this.compassData.scale - 0.5f;
		this.addSlider(new GuiNpcSlider(this, 0, this.guiLeft + 4, this.guiTop + 127, 120, 20, v));
		this.getSlider(0).setString((""+this.compassData.scale).replace(".", ","));
		
		// Incline
		this.addLabel(new GuiNpcLabel(4, "model.incline", this.guiLeft + 4, this.guiTop + 150));
		v = this.compassData.incline * -0.022222f + 0.5f;
		this.addSlider(new GuiNpcSlider(this, 1, this.guiLeft + 4, this.guiTop + 160, 120, 20, v));
		this.getSlider(1).setString((""+(45.0f + this.compassData.incline*-1.0f)).replace(".", ","));
		
		// Rotation
		this.addLabel(new GuiNpcLabel(5, "movement.rotation", this.guiLeft + 4, this.guiTop + 183));
		v = this.compassData.rot * 0.016667f + 0.5f;
		this.addSlider(new GuiNpcSlider(this, 2, this.guiLeft + 4, this.guiTop + 192, 120, 20, v));
		this.getSlider(2).setString((""+this.compassData.rot).replace(".", ","));
		
		this.addButton(new GuiNpcCheckBox(0, this.guiLeft + 128, this.guiTop + 128, 128, 12, "quest.screen.show.quest"));
		((GuiNpcCheckBox) this.getButton(0)).setSelected(this.compassData.showQuestName);

		this.addButton(new GuiNpcCheckBox(1, this.guiLeft + 128, this.guiTop + 162, 128, 12, "quest.screen.show.task"));
		((GuiNpcCheckBox) this.getButton(1)).setSelected(this.compassData.showTaskProgress);
		
		this.addButton(new GuiNpcButton(66, this.guiLeft + 192, this.guiTop + 192, 60, 20, "gui.back"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id) {
			case 0: {
				if (!(guibutton instanceof GuiNpcCheckBox)) { return; }
				this.compassData.showQuestName = ((GuiNpcCheckBox) guibutton).isSelected();
				break;
			}
			case 1: {
				if (!(guibutton instanceof GuiNpcCheckBox)) { return; }
				this.compassData.showTaskProgress = ((GuiNpcCheckBox) guibutton).isSelected();
				break;
			}
			case 66: {
				this.close();
				break;
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.guiLeft + 5, this.guiTop + 14, 0);
		Gui.drawRect(-1, -1, 122, 82, 0xFF808080);
		Gui.drawRect(0, 0, 121, 81, 0xFFF0F0F0);
		Gui.drawRect(34, 66, 86, 82, 0xFF808080);
		Gui.drawRect(35, 67, 85, 81, 0xFFA0A0A0);
		GlStateManager.translate(this.compassData.screenPos[0]*120.0d, this.compassData.screenPos[1]*80.0d, 0.0d);
		Gui.drawRect(-3, -1, 4, 3, 0xFF0000FF);
		Gui.drawRect(-3, 3, 4, 5, 0xFFFF00FF);
		
		GlStateManager.popMatrix();

		GlStateManager.translate(this.guiLeft+188, this.guiTop+45, 20.0d);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 33.0f, 0.0f);
		int i = 0;
		if (this.compassData.showQuestName) {
			this.drawCenteredString(this.mc.fontRenderer, "Quest name", 0, 0, 0xFFFFFFFF);
			i = 12;
		}
		if (this.compassData.showTaskProgress) { this.drawCenteredString(this.mc.fontRenderer, "Tasck name", 0, i, 0xFFFFFFFF); }
		GlStateManager.popMatrix();

		float scale = -30.0f * this.compassData.scale;
		float incline = -45.0f + this.compassData.incline;
		
		this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.translate(0.0f, -32.85714f * this.compassData.scale + 32.42857f, 0.0f);
		GlStateManager.scale(scale , scale, scale);
		GlStateManager.rotate(incline, 1.0f, 0.0f, 0.0f);
		if (this.compassData.rot!=0.0f)  { GlStateManager.rotate(this.compassData.rot, 0.0f, 1.0f, 0.0f); }
		GlStateManager.enableDepth();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableLighting();
		RenderHelper.enableStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
		
		// Body
		GlStateManager.pushMatrix();
		GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("body", "dial", "arrow_1", "arrow_20", "fase"), null));
		GlStateManager.rotate((System.currentTimeMillis()%3500L) / (3500.0f / 360.0f), 0.0f, 1.0f, 0.0f);
		GlStateManager.callList(ModelBuffer.getDisplayList(ClientGuiEventHandler.RESOURCE_COMPASS, Lists.<String>newArrayList("arrow_0"), null));
		GlStateManager.popMatrix();
		
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}
	
	@Override
	public void close() {
		this.save();
		this.displayGuiScreen(this.parent);
		if (this.parent==null) {
			this.mc.setIngameFocus();
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (mouseX>=this.guiLeft+5 && mouseX<=this.guiLeft+125 && mouseY>=this.guiTop+14 && mouseY<=this.guiTop+94) {
			this.compassData.screenPos[0] = Math.round((double) (mouseX - this.guiLeft - 5) * 8.33333d)/1000.0d;
			this.compassData.screenPos[1] = Math.round((double) (mouseY - this.guiTop - 14) * 12.5d)/1000.0d;
			this.initGui();
		}
	}
	
	@Override
	public void save() {
		Client.sendDataDelayCheck(EnumPlayerPacket.SaveCompassData, 0, 0, this.compassData.getNbt());
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		switch(slider.id) {
			case 0: {
				this.compassData.scale = Math.round((slider.sliderValue + 0.5f)*100.0f)/100.0f;
				slider.setString((""+this.compassData.scale).replace(".", ","));
				break;
			}
			case 1: {
				this.compassData.incline = Math.round((-45.0f * slider.sliderValue + 22.5f)*100.0f)/100.0f;
				slider.setString((""+(45.0f + this.compassData.incline*-1.0f)).replace(".", ","));
				break;
			}
			case 2: {
				this.compassData.rot = Math.round((60.0f * slider.sliderValue - 30.0f)*100.0f)/100.0f;
				slider.setString((""+this.compassData.rot).replace(".", ","));
				break;
			}
		}
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch(textField.getId()) {
			case 0: {
				this.compassData.screenPos[0] = Math.round(textField.getDouble()*100.0d)/100.0d;
				break;
			}
			case 1: {
				this.compassData.screenPos[1] = Math.round(textField.getDouble()*100.0d)/100.0d;
				break;
			}
		}
	}

}
