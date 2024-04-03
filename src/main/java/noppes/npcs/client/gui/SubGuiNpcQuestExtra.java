package noppes.npcs.client.gui;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.player.GuiLog;
import noppes.npcs.client.gui.select.GuiNPCSelection;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class SubGuiNpcQuestExtra
extends SubGuiInterface
implements ITextfieldListener, ISubGuiListener {

	private static ResourceLocation sheet = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_3.png");
	private static ResourceLocation tabs = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_4.png");
	private EntityNPCInterface showNpc;
	public Quest quest;
	private ScaledResolution sw;
	
	public SubGuiNpcQuestExtra(int id, Quest q) {
		this.quest = q;
		this.id = id;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;

		showNpc = AdditionalMethods.copyToGUI(quest.completer, mc.world, false);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		sw = new ScaledResolution(this.mc);
		int y = this.guiTop + 5;
		int lId = 0;
		this.addLabel(new GuiNpcLabel(lId++, "quest.icon", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 149, y, 60, 20, "availability.select"));

		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 5, y += 22, 203, 20, this.quest.icon.toString()));

		this.addLabel(new GuiNpcLabel(lId++, "quest.texture", this.guiLeft + 5, (y += 22) + 5 ));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 149, y, 60, 20, "availability.select"));
		
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 5, y += 22, 203, 20, this.quest.texture==null ? "" : this.quest.texture.toString()));
		
		int x = this.guiLeft + 110;
		this.addButton(new GuiNpcButton(1, this.guiLeft + 5, y += 32, 100, 20, new String[] { "quest.npc", "quest.instant" }, this.quest.completion.ordinal()));
		this.addButton(new GuiNpcButton(2, x, y, 60, 20, "availability.select"));
		
		this.addLabel(new GuiNpcLabel(lId++, "quest.questrewardtext", this.guiLeft + 5, (y += 22) + 5));
		this.addButton(new GuiNpcButton(4, x, y, 60, 20, this.quest.rewardText.isEmpty() ? "selectServer.edit" : "advanced.editingmode"));

		this.addLabel(new GuiNpcLabel(lId++, "quest.extra.button.type", this.guiLeft + 5, (y += 22) + 5));
		this.addButton(new GuiButtonBiDirectional(5, x, y, 60, 20, new String[] { "gui.none", "1", "2", "3", "4", "5"}, quest.extraButton));
		
		this.addLabel(new GuiNpcLabel(lId++, "quest.extra.button.text", this.guiLeft + 5, (y += 22) + 5));
		this.addButton(new GuiNpcButton(6, x, y, 60, 20, "selectServer.edit"));
		this.getButton(6).enabled = quest.extraButton > 0;
		
		this.addButton(new GuiNpcButton(66, this.guiLeft + 5, this.guiTop + this.ySize - 25, 60, 20, "gui.done"));
		
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: { // icon select
				GuiTextureSelection subGui = new GuiTextureSelection(showNpc, this.quest.icon.toString(), "png", 3);
				subGui.id = 0;
				this.setSubGui(subGui);
				break;
			}
			case 1: { // completion type
				this.quest.completion = EnumQuestCompletion.values()[button.getValue()];
				break;
			}
			case 2: { // select npc
				this.setSubGui(new GuiNPCSelection(this.quest.completer));
				break;
			}
			case 3: { // texture select
				GuiTextureSelection subGui = new GuiTextureSelection(showNpc, this.quest.texture==null ? "" : this.quest.texture.toString(), "png", 3);
				subGui.id = 1;
				this.setSubGui(subGui);
				break;
			}
			case 4: { // reward text
				this.setSubGui(new SubGuiNpcTextArea(0, this.quest.rewardText));
				break;
			}
			case 5: { // extra button type
				this.quest.extraButton = button.getValue();
				this.initGui();
				break;
			}
			case 6: { // extra button hover text
				this.setSubGui(new SubGuiNpcTextArea(1, this.quest.extraButtonText));
				break;
			}
			case 66: { 
				this.close();
				break;
			}
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (this.subgui != null) { return; }
		if (this.isMouseHover(mouseX, mouseY, this.guiLeft + 214, this.guiTop + 8, 32, 32)) {
			GuiTextureSelection subGui = new GuiTextureSelection(showNpc, this.quest.icon.toString(), "png", 3);
			subGui.id = 0;
			this.setSubGui(subGui);
			return;
		}
		if (this.isMouseHover(mouseX, mouseY, this.guiLeft + 214, this.guiTop + 52, 32, 32)) {
			GuiTextureSelection subGui = new GuiTextureSelection(showNpc, this.quest.texture==null ? "" : this.quest.texture.toString(), "png", 3);
			subGui.id = 1;
			this.setSubGui(subGui);
			return;
		}
		if (this.isMouseHover(mouseX, mouseY, guiLeft + 182, guiTop + 95, 65, 65)) {
			this.setSubGui(new GuiNPCSelection(this.quest.completer));
			return;
		}
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		if (this.subgui!=null) {
			super.drawScreen(i, j, f);
			return;
		}
		String[] temp = this.hoverText;
		this.hoverText = null;
		super.drawScreen(i, j, f);
		int u = this.guiLeft + 182;
		int v = this.guiTop + 97;
		// Back
		GlStateManager.pushMatrix();
		GlStateManager.translate(u + 5.0f, v + 3.0f, 0.0f);
		GlStateManager.enableBlend();
		GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
		this.mc.renderEngine.bindTexture(SubGuiNpcQuestExtra.sheet);
		this.drawTexturedModalRect(-5, -5, 34, 54, 65, 65);
		GlStateManager.popMatrix();
		if (showNpc!=null) {
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = sw.getScaledWidth() < mc.displayWidth ? (int) Math.round((double) mc.displayWidth / (double) sw.getScaledWidth()) : 1;
			GL11.glScissor((u + 4) * c, (v - 25) * c, (56) * c, (44) * c);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawNpc(showNpc);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
			// Fase
			GlStateManager.pushMatrix();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 100.0f);
			GlStateManager.enableBlend();
			GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
			this.mc.renderEngine.bindTexture(SubGuiNpcQuestExtra.tabs);
			this.drawTexturedModalRect(0, 0, 193, 0, 63, 52);
			String name = ((char) 167)+"l"+this.quest.completer.getName();
			this.mc.fontRenderer.drawString(name, 32 - this.mc.fontRenderer.getStringWidth(name) / 2, 50, CustomNpcs.questLogColor, false);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (quest.extraButton > 0) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(u - 83.0f, v + 55.5f, 100.0f);
			GlStateManager.enableBlend();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.mc.renderEngine.bindTexture(SubGuiNpcQuestExtra.sheet);
			this.drawTexturedModalRect(-1, -1, 34, 20, 11, 11);
			this.mc.renderEngine.bindTexture(SubGuiNpcQuestExtra.tabs);
			this.drawTexturedModalRect(0, 0, 116 + quest.extraButton * 9, 0, 9, 9);
			GlStateManager.popMatrix();
		}
		
		u = this.guiLeft + 214;
		v = this.guiTop + 8;
		this.drawGradientRect(u, v, u + 34, v + 34, 0xFF404040, 0xFF404040);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
		this.mc.renderEngine.bindTexture(SubGuiNpcQuestExtra.sheet);
		this.drawTexturedModalRect(0, 0, 34, 54, 32, 32);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		if (this.quest.icon!=null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
			GlStateManager.scale(0.125f, 0.125f, 1.0f);
			this.mc.renderEngine.bindTexture(this.quest.icon);
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		
		u = this.guiLeft + 214;
		v = this.guiTop + 52;
		this.drawGradientRect(u, v, u + 34, v + 34, 0xFF404040, 0xFF404040);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
		this.mc.renderEngine.bindTexture(SubGuiNpcQuestExtra.sheet);
		this.drawTexturedModalRect(0, 0, 34, 54, 32, 32);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		if (this.quest.texture!=null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
			GlStateManager.scale(0.125f, 0.125f, 1.0f);
			try {
				this.mc.renderEngine.bindTexture(this.quest.texture);
				this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			} catch (Exception e) {}
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (temp != null) { this.drawHoveringText(Arrays.asList(temp), mouseX, mouseY, this.fontRenderer); }
		if (this.subgui!=null || !CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.icon.path").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.texture.path").getFormattedText());
		} else  if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.icon.sel").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.completion").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.completion.npc").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.texture.sel").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.reward.text").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.extra.button.type", EnumScriptType.EXTRA_BUTTON.function).getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.extra.button.text").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	private void drawNpc(EntityNPCInterface npc) {
		if (npc == null) { return; }
		GlStateManager.translate((sw.getScaledWidth_double() + 170.0d) / 2.0d, (sw.getScaledHeight_double() + 90.0d) / 2.0d, 10.0d);
		String modelName = "";
		if (npc.display.getModel() != null) { modelName = npc.display.getModel(); }
		boolean canUpdate = GuiLog.preDrawEntity(modelName);
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		this.mc.getRenderManager().playerViewY = 180.0f;
		GlStateManager.scale(25.0f, 25.0f, 25.0f);
		npc.ticksExisted = 100;
		if (canUpdate) { npc.onUpdate(); }
		this.mc.getRenderManager().renderEntity(npc, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	
	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getId()==0) {
			if (textField.getText().isEmpty()) { this.quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png"); }
			else { this.quest.icon = new ResourceLocation(textField.getText()); }
			textField.setText(this.quest.icon.toString());
		}
		else if (textField.getId()==1) {
			if (textField.getText().isEmpty()) { this.quest.texture = null; }
			else { this.quest.texture = new ResourceLocation(textField.getText()); }
			textField.setText(this.quest.texture==null ? "" : this.quest.texture.toString());
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			if (subgui.id == 0) { this.quest.rewardText = ((SubGuiNpcTextArea) subgui).text; }
			else if (subgui.id == 1) { this.quest.extraButtonText = ((SubGuiNpcTextArea) subgui).text; }
			this.initGui();
		}
		else if (subgui instanceof GuiTextureSelection) {
			if (((GuiTextureSelection) subgui).id==0) {
				this.quest.icon = ((GuiTextureSelection) subgui).resource;
				if (this.quest.icon == null) { this.quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png"); }
			} else {
				this.quest.texture = ((GuiTextureSelection) subgui).resource;
			}
		}
		else if (subgui instanceof GuiNPCSelection) {
			if (((GuiNPCSelection) subgui).selectEntity==null) { return; }
			Entity entity = this.mc.world.getEntityByID(((GuiNPCSelection) subgui).selectEntity.getEntityId());
			if (!(entity instanceof EntityNPCInterface)) { return; }
			quest.completer = AdditionalMethods.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			showNpc = AdditionalMethods.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			this.initGui();
		}
	}

}
