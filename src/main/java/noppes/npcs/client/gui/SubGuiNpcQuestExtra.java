package noppes.npcs.client.gui;

import java.awt.*;
import java.util.List;

import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.*;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.player.GuiLog;
import noppes.npcs.client.gui.select.GuiNPCSelection;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class SubGuiNpcQuestExtra
extends SubGuiInterface
implements ITextfieldListener, ISubGuiListener {

	private static final ResourceLocation sheet = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_3.png");
	private static final ResourceLocation tabs = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_4.png");
	private EntityNPCInterface showNpc;
	public Quest quest;
	private ScaledResolution sw;

	public SubGuiNpcQuestExtra(int i, Quest q) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 217;
		closeOnEsc = true;

		quest = q;
		id = i;
		showNpc = Util.instance.copyToGUI(quest.completer, mc.world, false);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: { // icon select
				GuiTextureSelection subGui = new GuiTextureSelection(showNpc, quest.icon.toString(), "png", 3);
				subGui.id = 0;
				setSubGui(subGui);
				break;
			}
			case 1: { // completion type
				quest.completion = EnumQuestCompletion.values()[button.getValue()];
				break;
			}
			case 2: { // select npc
				setSubGui(new GuiNPCSelection(quest.completer));
				break;
			}
			case 3: { // texture select
				GuiTextureSelection subGui = new GuiTextureSelection(showNpc, quest.texture == null ? "" : quest.texture.toString(), "png", 3);
				subGui.id = 1;
				setSubGui(subGui);
				break;
			}
			case 4: { // reward text
				setSubGui(new SubGuiNpcTextArea(0, quest.rewardText));
				break;
			}
			case 5: { // extra button type
				quest.extraButton = button.getValue();
				initGui();
				break;
			}
			case 6: { // extra button hover text
				setSubGui(new SubGuiNpcTextArea(1, quest.extraButtonText));
				break;
			}
			case 7: {
				quest.showProgressInChat = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 8: {
				quest.showProgressInWindow = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 66: {
				close();
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
			u = getButton(2).getLeft() - 25;
			v = getButton(2).getTop() - 26;
		}
		GlStateManager.translate((sw.getScaledWidth_double() + u) / 2.0d, (sw.getScaledHeight_double() + v) / 2.0d, 10.0d);
		String modelName = "";
		if (npc.display.getModel() != null) {
			modelName = npc.display.getModel();
		}
		boolean canUpdate = GuiLog.preDrawEntity(modelName);
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		mc.getRenderManager().playerViewY = 180.0f;
		GlStateManager.scale(25.0f, 25.0f, 25.0f);
		npc.ticksExisted = 100;
		if (canUpdate) {
			npc.onUpdate();
		}
		mc.getRenderManager().renderEntity(npc, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);

		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui != null) {
			super.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}
		List<String> tempHoverText = getHoverText();
		setHoverText(null);
		super.drawScreen(mouseX, mouseY, partialTicks);
		int u = guiLeft + 182;
		int v = guiTop + 97;
		if (getButton(2) != null) {
			u = getButton(2).getLeft() + getButton(2).getWidth() + 9;
			v = getButton(2).getTop() + 2;
		}
		// Back
		GlStateManager.pushMatrix();
		GlStateManager.translate(u + 5.0f, v + 3.0f, 0.0f);
		GlStateManager.enableBlend();
		GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
		mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		drawTexturedModalRect(-5, -5, 34, 54, 65, 65);
		GlStateManager.popMatrix();
		if (showNpc != null) {
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = sw.getScaledWidth() < mc.displayWidth
					? (int) Math.round((double) mc.displayWidth / (double) sw.getScaledWidth())
					: 1;
			GL11.glScissor((u + 4) * c, (v + 17) * c, (56) * c, (44) * c);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawNpc(showNpc);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
			// Fase
			GlStateManager.pushMatrix();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 100.0f);
			GlStateManager.enableBlend();
			GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
			mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.tabs);
			drawTexturedModalRect(0, 0, 193, 0, 63, 52);
			String name = ((char) 167) + "l" + (quest.completer != null ? quest.completer.getName() : "Empty");
			mc.fontRenderer.drawString(name, 32 - (float) mc.fontRenderer.getStringWidth(name) / 2, 50, CustomNpcs.QuestLogColor.getRGB(), false);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (quest.extraButton > 0) {
			u = guiLeft + 98;
			v = guiTop + 134;
			if (getButton(5) != null) {
				u = getButton(5).getLeft() - 12;
				v = getButton(5).getTop() + 3;
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
			u = getButton(0).getLeft() + getButton(0).getWidth() + 5;
			v = getButton(0).getTop() - 1;
		}
		int color = new Color(0xFF404040).getRGB();
		drawGradientRect(u, v, u + 34, v + 34, color, color);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
		mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		drawTexturedModalRect(0, 0, 34, 54, 32, 32);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		if (quest.icon != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
			GlStateManager.scale(0.125f, 0.125f, 1.0f);
			mc.getTextureManager().bindTexture(quest.icon);
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		u = guiLeft + 214;
		v = guiTop + 38;
		if (getButton(3) != null) {
			u = getButton(3).getLeft() + getButton(3).getWidth() + 5;
			v = getButton(3).getTop() - 1;
		}
		drawGradientRect(u, v, u + 34, v + 34, color, color);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
		mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		drawTexturedModalRect(0, 0, 34, 54, 32, 32);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		if (quest.texture != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 0.0f);
			GlStateManager.scale(0.125f, 0.125f, 1.0f);
			try {
				mc.getTextureManager().bindTexture(quest.texture);
				drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			}
			catch (Exception e) { LogWriter.error("Error:", e); }
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (tempHoverText != null) {
			drawHoveringText(tempHoverText, mouseX, mouseY, fontRenderer);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		sw = new ScaledResolution(mc);
		int x = guiLeft + 5;
		int y = guiTop + 5;
		int lId = 0;
		// icon
		addLabel(new GuiNpcLabel(lId++, "quest.icon", x + 1, y + 2));
		GuiNpcButton button = new GuiNpcButton(0, x + 144, y, 60, 14, "availability.select");
		button.setHoverText("quest.hover.edit.quest.icon.sel");
		addButton(button);
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, x, y += 16, 203, 16, quest.icon.toString());
		textField.setHoverText("quest.hover.edit.quest.icon.path");
		addTextField(textField);
		// texture description
		addLabel(new GuiNpcLabel(lId++, "quest.texture", x + 1, (y += 18) + 2));
		button = new GuiNpcButton(3, x + 144, y, 60, 14, "availability.select");
		button.setHoverText("quest.hover.edit.quest.texture.sel");
		addButton(button);
		textField = new GuiNpcTextField(1, this, fontRenderer, x, y += 16, 203, 16, quest.texture == null ? "" : quest.texture.toString());
		textField.setHoverText("quest.hover.edit.quest.texture.path");
		addTextField(textField);
		// completion npc
		button = new GuiNpcButton(1, x, y += 18, 100, 14, new String[] { "quest.npc", "quest.instant" }, quest.completion.ordinal());
		button.setHoverText("quest.hover.edit.quest.completion");
		addButton(button);
		button = new GuiNpcButton(2, x + 105, y, 60, 14, "availability.select");
		button.setHoverText("quest.hover.edit.quest.completion.npc");
		addButton(button);
		// reward text
		addLabel(new GuiNpcLabel(lId++, "quest.questrewardtext", guiLeft + 5, (y += 16) + 2));
		button = new GuiNpcButton(4, x + 105, y, 60, 14, quest.rewardText.isEmpty() ? "selectServer.edit" : "advanced.editing mode");
		button.setHoverText("quest.hover.edit.reward.text");
		addButton(button);
		// extra button
		addLabel(new GuiNpcLabel(lId++, "quest.extra.button.type", guiLeft + 5, (y += 16) + 2));
		button = new GuiButtonBiDirectional(5, x + 105, y, 60, 14, new String[] { "gui.none", "1", "2", "3", "4", "5" }, quest.extraButton);
		button.setHoverText("quest.hover.extra.button.type", EnumScriptType.QUEST_LOG_BUTTON.function);
		addButton(button);
		// extra button text
		addLabel(new GuiNpcLabel(lId, "quest.extra.button.text", guiLeft + 5, (y += 16) + 2));
		button = new GuiNpcButton(6, x + 105, y, 60, 14, "selectServer.edit");
		button.setEnabled(quest.extraButton > 0);
		button.setHoverText("quest.hover.extra.button.text");
		addButton(button);
		// progress in chat / window
		button = new GuiNpcCheckBox(7, x, (y += 17), 239, 14, "quest.show.progress.in.chat", "", quest.showProgressInChat);
		button.setHoverText("quest.hover.show.in.chat");
		addButton(button);
		button = new GuiNpcCheckBox(8, x, y + 16, 239, 14, "quest.show.progress.in.window", "", quest.showProgressInWindow);
		button.setHoverText("quest.hover.show.in.window");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, x, guiTop + ySize - 19, 60, 14, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (subgui != null) {
			return;
		}
		int u = guiLeft + 214, v = guiTop + 5;
		if (getButton(0) != null) {
			u = getButton(0).getLeft() + getButton(0).getWidth() + 6;
			v = getButton(0).getTop();
		}
		if (isMouseHover(mouseX, mouseY, u, v, 32, 32)) {
			GuiTextureSelection subGui = new GuiTextureSelection(showNpc, quest.icon.toString(), "png", 3);
			subGui.id = 0;
			setSubGui(subGui);
			return;
		}
		v = guiTop + 37;
		if (getButton(3) != null) {
			u = getButton(3).getLeft() + getButton(3).getWidth() + 6;
			v = getButton(3).getTop();
		}
		if (isMouseHover(mouseX, mouseY, u, v, 32, 32)) {
			GuiTextureSelection subGui = new GuiTextureSelection(showNpc, quest.texture == null ? "" : quest.texture.toString(), "png", 3);
			subGui.id = 1;
			setSubGui(subGui);
			return;
		}
		if (isMouseHover(mouseX, mouseY, guiLeft + 182, guiTop + 95, 65, 65)) {
			setSubGui(new GuiNPCSelection(quest.completer));
		}
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			if (subgui.getId() == 0) {
				quest.rewardText = ((SubGuiNpcTextArea) subgui).text;
			} else if (subgui.getId() == 1) {
				quest.extraButtonText = ((SubGuiNpcTextArea) subgui).text;
			}
			initGui();
		} else if (subgui instanceof GuiTextureSelection) {
			if (subgui.getId() == 0) {
				quest.icon = ((GuiTextureSelection) subgui).resource;
				if (quest.icon == null) {
					quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png");
				}
			} else {
				quest.texture = ((GuiTextureSelection) subgui).resource;
			}
			initGui();
		} else if (subgui instanceof GuiNPCSelection) {
			if (((GuiNPCSelection) subgui).selectEntity == null) {
				return;
			}
			Entity entity = mc.world.getEntityByID(((GuiNPCSelection) subgui).selectEntity.getEntityId());
			if (!(entity instanceof EntityNPCInterface)) {
				return;
			}
			quest.completer = Util.instance.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			showNpc = Util.instance.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			initGui();
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (textField.getID() == 0) {
			if (textField.getFullText().isEmpty()) {
				quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png");
			} else {
				quest.icon = new ResourceLocation(textField.getFullText());
			}
			textField.setFullText(quest.icon.toString());
		}
		else if (textField.getID() == 1) {
			if (textField.getFullText().isEmpty()) {
				quest.texture = null;
			} else {
				quest.texture = new ResourceLocation(textField.getFullText());
			}
			textField.setFullText(quest.texture == null ? "" : quest.texture.toString());
		}
	}

}
