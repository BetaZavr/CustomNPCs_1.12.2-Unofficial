package noppes.npcs.client.gui;

import java.util.Arrays;

import noppes.npcs.LogWriter;
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
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class SubGuiNpcQuestExtra extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {

	private static final ResourceLocation sheet = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_3.png");
	private static final ResourceLocation tabs = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_4.png");
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

		showNpc = Util.instance.copyToGUI(quest.completer, mc.world, false);
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
			GuiTextureSelection subGui = new GuiTextureSelection(showNpc,
					this.quest.texture == null ? "" : this.quest.texture.toString(), "png", 3);
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
		case 7: {
			this.quest.showProgressInChat = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 8: {
			this.quest.showProgressInWindow = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 66: {
			this.close();
			break;
		}
		}
	}

	private void drawNpc(EntityNPCInterface npc) {
		if (npc == null) {
			return;
		}
		double u = 170.0d;
		double v = 90.0d;
		if (getButton(2) != null) {
			u = getButton(2).x - 25;
			v = getButton(2).y - 26;
		}
		GlStateManager.translate((sw.getScaledWidth_double() + u) / 2.0d, (sw.getScaledHeight_double() + v) / 2.0d,
				10.0d);
		String modelName = "";
		if (npc.display.getModel() != null) {
			modelName = npc.display.getModel();
		}
		boolean canUpdate = GuiLog.preDrawEntity(modelName);
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		this.mc.getRenderManager().playerViewY = 180.0f;
		GlStateManager.scale(25.0f, 25.0f, 25.0f);
		npc.ticksExisted = 100;
		if (canUpdate) {
			npc.onUpdate();
		}
		this.mc.getRenderManager().renderEntity(npc, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);

		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		if (this.subgui != null) {
			super.drawScreen(i, j, f);
			return;
		}
		String[] temp = this.hoverText;
		this.hoverText = null;
		super.drawScreen(i, j, f);
		int u = guiLeft + 182;
		int v = guiTop + 97;
		if (getButton(2) != null) {
			u = getButton(2).x + getButton(2).width + 9;
			v = getButton(2).y + 2;
		}
		// Back
		GlStateManager.pushMatrix();
		GlStateManager.translate(u + 5.0f, v + 3.0f, 0.0f);
		GlStateManager.enableBlend();
		GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		this.drawTexturedModalRect(-5, -5, 34, 54, 65, 65);
		GlStateManager.popMatrix();
		if (showNpc != null) {
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = sw.getScaledWidth() < mc.displayWidth
					? (int) Math.round((double) mc.displayWidth / (double) sw.getScaledWidth())
					: 1;
			GL11.glScissor((u + 4) * c, (v + 17) * c, (56) * c, (44) * c);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawNpc(showNpc);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
			// Fase
			GlStateManager.pushMatrix();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 100.0f);
			GlStateManager.enableBlend();
			GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
			this.mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.tabs);
			this.drawTexturedModalRect(0, 0, 193, 0, 63, 52);
			String name = ((char) 167) + "l" + this.quest.completer.getName();
			this.mc.fontRenderer.drawString(name, 32 - (float) this.mc.fontRenderer.getStringWidth(name) / 2, 50, CustomNpcs.QuestLogColor.getRGB(), false);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (quest.extraButton > 0) {
			u = guiLeft + 98;
			v = guiTop + 134;
			if (getButton(5) != null) {
				u = getButton(5).x - 12;
				v = getButton(5).y + 3;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(u, v, 100.0f);
			GlStateManager.enableBlend();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
			drawTexturedModalRect(-1, -1, 34, 20, 11, 11);
			mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.tabs);
			drawTexturedModalRect(0, 0, 116 + quest.extraButton * 9, 0, 9, 9);
			GlStateManager.popMatrix();
		}

		u = guiLeft + 214;
		v = guiTop + 4;
		if (getButton(0) != null) {
			u = getButton(0).x + getButton(0).width + 5;
			v = getButton(0).y - 1;
		}
		this.drawGradientRect(u, v, u + 34, v + 34, 0xFF404040, 0xFF404040);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
		this.mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		this.drawTexturedModalRect(0, 0, 34, 54, 32, 32);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		if (this.quest.icon != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
			GlStateManager.scale(0.125f, 0.125f, 1.0f);
			this.mc.getTextureManager().bindTexture(this.quest.icon);
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		u = guiLeft + 214;
		v = guiTop + 38;
		if (getButton(3) != null) {
			u = getButton(3).x + getButton(3).width + 5;
			v = getButton(3).y - 1;
		}
		this.drawGradientRect(u, v, u + 34, v + 34, 0xFF404040, 0xFF404040);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
		this.mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		this.drawTexturedModalRect(0, 0, 34, 54, 32, 32);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		if (this.quest.texture != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
			GlStateManager.scale(0.125f, 0.125f, 1.0f);
			try {
				this.mc.getTextureManager().bindTexture(this.quest.texture);
				this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			}
			catch (Exception e) { LogWriter.error("Error:", e); }
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (temp != null) {
			this.drawHoveringText(Arrays.asList(temp), mouseX, mouseY, this.fontRenderer);
		}
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.icon.path").getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.texture.path").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.icon.sel").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.completion").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.completion.npc").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.texture.sel").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.reward.text").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("quest.hover.extra.button.type", EnumScriptType.QUEST_LOG_BUTTON.function)
							.getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.extra.button.text").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.show.in.chat").getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.show.in.window").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		sw = new ScaledResolution(this.mc);
		int x = this.guiLeft + 5;
		int y = this.guiTop + 5;
		int lId = 0;
		this.addLabel(new GuiNpcLabel(lId++, "quest.icon", x + 1, y + 2));
		this.addButton(new GuiNpcButton(0, x + 144, y, 60, 14, "availability.select"));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, x, y += 16, 203, 16, this.quest.icon.toString()));

		this.addLabel(new GuiNpcLabel(lId++, "quest.texture", x + 1, (y += 18) + 2));
		this.addButton(new GuiNpcButton(3, x + 144, y, 60, 14, "availability.select"));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, x, y += 16, 203, 16, this.quest.texture == null ? "" : this.quest.texture.toString()));

		this.addButton(new GuiNpcButton(1, x, y += 18, 100, 14, new String[] { "quest.npc", "quest.instant" }, this.quest.completion.ordinal()));
		this.addButton(new GuiNpcButton(2, x + 105, y, 60, 14, "availability.select"));

		this.addLabel(new GuiNpcLabel(lId++, "quest.questrewardtext", this.guiLeft + 5, (y += 16) + 2));
		this.addButton(new GuiNpcButton(4, x + 105, y, 60, 14, this.quest.rewardText.isEmpty() ? "selectServer.edit" : "advanced.editing mode"));

		this.addLabel(new GuiNpcLabel(lId++, "quest.extra.button.type", this.guiLeft + 5, (y += 16) + 2));
		this.addButton(new GuiButtonBiDirectional(5, x + 105, y, 60, 14, new String[] { "gui.none", "1", "2", "3", "4", "5" }, quest.extraButton));

		this.addLabel(new GuiNpcLabel(lId, "quest.extra.button.text", this.guiLeft + 5, (y += 16) + 2));
		this.addButton(new GuiNpcButton(6, x + 105, y, 60, 14, "selectServer.edit"));
		this.getButton(6).enabled = quest.extraButton > 0;

		this.addButton(new GuiNpcCheckBox(7, x, (y += 17), 239, 14, "quest.show.progress.in.chat", "", quest.showProgressInChat));
		this.addButton(new GuiNpcCheckBox(8, x, y + 16, 239, 14, "quest.show.progress.in.window", "", quest.showProgressInWindow));

		this.addButton(new GuiNpcButton(66, x, this.guiTop + this.ySize - 19, 60, 14, "gui.done"));
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (this.subgui != null) {
			return;
		}
		int u = guiLeft + 214, v = guiTop + 5;
		if (getButton(0) != null) {
			u = getButton(0).x + getButton(0).width + 6;
			v = getButton(0).y;
		}
		if (this.isMouseHover(mouseX, mouseY, u, v, 32, 32)) {
			GuiTextureSelection subGui = new GuiTextureSelection(showNpc, this.quest.icon.toString(), "png", 3);
			subGui.id = 0;
			this.setSubGui(subGui);
			return;
		}
		v = guiTop + 37;
		if (getButton(3) != null) {
			u = getButton(3).x + getButton(3).width + 6;
			v = getButton(3).y;
		}
		if (this.isMouseHover(mouseX, mouseY, u, v, 32, 32)) {
			GuiTextureSelection subGui = new GuiTextureSelection(showNpc,
					this.quest.texture == null ? "" : this.quest.texture.toString(), "png", 3);
			subGui.id = 1;
			this.setSubGui(subGui);
			return;
		}
		if (this.isMouseHover(mouseX, mouseY, guiLeft + 182, guiTop + 95, 65, 65)) {
			this.setSubGui(new GuiNPCSelection(this.quest.completer));
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			if (((SubGuiNpcTextArea) subgui).getId() == 0) {
				this.quest.rewardText = ((SubGuiNpcTextArea) subgui).text;
			} else if (((SubGuiNpcTextArea) subgui).getId() == 1) {
				this.quest.extraButtonText = ((SubGuiNpcTextArea) subgui).text;
			}
			this.initGui();
		} else if (subgui instanceof GuiTextureSelection) {
			if (subgui.id == 0) {
				this.quest.icon = ((GuiTextureSelection) subgui).resource;
				if (this.quest.icon == null) {
					this.quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png");
				}
			} else {
				this.quest.texture = ((GuiTextureSelection) subgui).resource;
			}
			this.initGui();
		} else if (subgui instanceof GuiNPCSelection) {
			if (((GuiNPCSelection) subgui).selectEntity == null) {
				return;
			}
			Entity entity = this.mc.world.getEntityByID(((GuiNPCSelection) subgui).selectEntity.getEntityId());
			if (!(entity instanceof EntityNPCInterface)) {
				return;
			}
			quest.completer = Util.instance.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			showNpc = Util.instance.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			this.initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getId() == 0) {
			if (textField.getText().isEmpty()) {
				this.quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png");
			} else {
				this.quest.icon = new ResourceLocation(textField.getText());
			}
			textField.setText(this.quest.icon.toString());
		} else if (textField.getId() == 1) {
			if (textField.getText().isEmpty()) {
				this.quest.texture = null;
			} else {
				this.quest.texture = new ResourceLocation(textField.getText());
			}
			textField.setText(this.quest.texture == null ? "" : this.quest.texture.toString());
		}
	}

}
