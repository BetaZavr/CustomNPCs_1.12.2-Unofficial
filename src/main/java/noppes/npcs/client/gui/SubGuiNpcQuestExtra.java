package noppes.npcs.client.gui;

import java.awt.*;
import java.util.List;

import noppes.npcs.client.gui.util.*;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.player.GuiLog;
import noppes.npcs.client.gui.select.SubGuiNPCSelection;
import noppes.npcs.client.gui.select.SubGuiTextureSelection;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class SubGuiNpcQuestExtra extends SubGuiInterface implements ITextfieldListener {

	protected static final ResourceLocation sheet = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_3.png");
	protected static final ResourceLocation tabs = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_4.png");
	protected EntityNPCInterface showNpc;
	protected ScaledResolution sw;
	public Quest quest;

	public SubGuiNpcQuestExtra(int id, Quest questIn) {
		super(id);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 217;
		closeOnEsc = true;

		quest = questIn;
		showNpc = Util.instance.copyToGUI(quest.completer, mc.world, false);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: setSubGui(new SubGuiTextureSelection(0, showNpc, quest.icon.toString(), "png", 3)); break;// icon select
			case 1: {
				quest.completion = EnumQuestCompletion.values()[button.getValue()];
				break;
			} // completion type
			case 2: {
				setSubGui(new SubGuiNPCSelection(quest.completer));
				break;
			} // select npc
			case 3: setSubGui(new SubGuiTextureSelection(1, showNpc, quest.texture == null ? "" : quest.texture.toString(), "png", 3)); break; // texture select
			case 4: {
				setSubGui(new SubGuiNpcTextArea(0, quest.rewardText));
				break;
			} // reward text
			case 5: {
				quest.extraButton = button.getValue();
				initGui();
				break;
			} // extra button type
			case 6: {
				setSubGui(new SubGuiNpcTextArea(1, quest.extraButtonText));
				break;
			} // extra button hover text
			case 7: {
				quest.showProgressInChat = ((GuiNpcCheckBox) button).isSelected();
				break;
			} // progress in chat
			case 8: {
				quest.showProgressInWindow = ((GuiNpcCheckBox) button).isSelected();
				break;
			} // progress in window
			case 66: {
				onClosed();
				break;
			}
		}
	}

	private void drawNpc(EntityNPCInterface npc) {
		if (npc == null) { return; }
		GlStateManager.translate((sw.getScaledWidth() + 170.0f) / 2.0f, (sw.getScaledHeight() + 60.0f) / 2.0f, 10.0f);
		String modelName = "";
		if (npc.display.getModel() != null) { modelName = npc.display.getModel(); }
		boolean canUpdate = GuiLog.preDrawEntity(modelName);
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		mc.getRenderManager().playerViewY = 180.0f;
		GlStateManager.scale(25.0f, 25.0f, 25.0f);
		npc.ticksExisted = 100;
		if (canUpdate) { npc.onUpdate(); }
		mc.getRenderManager().renderEntity(npc, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		List<String> tempHoverText = getHoverText();
		putHoverText(null);
		super.drawScreen(mouseX, mouseY, partialTicks);
		int u = guiLeft + 182;
		int v = guiTop + 97;
		if (getButton(2) != null) {
			u = getButton(2).x + getButton(2).width + 9;
			v = getButton(2).y + 2;
		}
		// Back
		GlStateManager.pushMatrix();
		GlStateManager.translate(u + 5.0f, v + 3.0f, 1.0f);
		GlStateManager.enableBlend();
		GlStateManager.color(3.0f, 3.0f, 3.0f, 1.0f);
		mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		drawTexturedModalRect(-5, -5, 34, 54, 65, 65);
		GlStateManager.popMatrix();

		if (showNpc != null && subgui == null) {
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
		if (quest.extraButton > 0 && subgui == null) {
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

		// quest icon
		u = guiLeft + 214;
		v = guiTop + 4;
		if (getButton(0) != null) {
			u = getButton(0).x + getButton(0).width + 5;
			v = getButton(0).y - 1;
		}
		int color = new Color(0xFF404040).getRGB();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(u + 1.0f, v + 1.0f, 1.0f);
		drawGradientRect(-1, -1,  33, 33, color, color);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		drawTexturedModalRect(0, 0, 34, 54, 32, 32);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		if (quest.icon != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 1.0f);
			GlStateManager.scale(0.125f, 0.125f, 1.0f);
			mc.getTextureManager().bindTexture(quest.icon);
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		// quest texture
		u = guiLeft + 214;
		v = guiTop + 38;
		if (getButton(3) != null) {
			u = getButton(3).x + getButton(3).width + 5;
			v = getButton(3).x - 1;
		}
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.translate(u + 1.0f, v + 1.0f, 1.0f);
		drawGradientRect(-1, -1, 33, 33, color, color);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(SubGuiNpcQuestExtra.sheet);
		drawTexturedModalRect(0, 0, 34, 54, 32, 32);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		if (quest.texture != null) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 1.0f);
			GlStateManager.scale(0.125f, 0.125f, 1.0f);
			mc.getTextureManager().bindTexture(quest.texture);
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (tempHoverText != null) { putHoverText(tempHoverText); }
		super.drawScreen(mouseX, mouseY, partialTicks);
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
		addButton(new GuiNpcButton(0, x + 144, y, 60, 14, "availability.select")
				.setHoverText("quest.hover.edit.quest.icon.sel"));
		addTextField(new GuiNpcTextField(0, this, x, y += 16, 203, 16, quest.icon.toString())
				.setHoverText("quest.hover.edit.quest.icon.path"));
		// texture description
		addLabel(new GuiNpcLabel(lId++, "quest.texture", x + 1, (y += 18) + 2));
		addButton(new GuiNpcButton(3, x + 144, y, 60, 14, "availability.select")
				.setHoverText("quest.hover.edit.quest.texture.sel"));
		addTextField(new GuiNpcTextField(1, this, x, y += 16, 203, 16, quest.texture == null ? "" : quest.texture.toString())
				.setHoverText("quest.hover.edit.quest.texture.path"));
		// completion npc
		addButton(new GuiNpcButton(1, x, y += 18, 100, 14, new String[] { "quest.npc", "quest.instant" }, quest.completion.ordinal())
				.setHoverText("quest.hover.edit.quest.completion"));
		addButton(new GuiNpcButton(2, x + 105, y, 60, 14, "availability.select")
				.setHoverText("quest.hover.edit.quest.completion.npc"));
		// reward text
		addLabel(new GuiNpcLabel(lId++, "quest.questrewardtext", guiLeft + 5, (y += 16) + 2));
		addButton(new GuiNpcButton(4, x + 105, y, 60, 14, quest.rewardText.isEmpty() ? "selectServer.edit" : "advanced.editing mode")
				.setHoverText("quest.hover.edit.reward.text"));
		// extra button
		addLabel(new GuiNpcLabel(lId++, "quest.extra.button.type", guiLeft + 5, (y += 16) + 2));
		addButton(new GuiButtonBiDirectional(5, x + 105, y, 60, 14, new String[] { "gui.none", "1", "2", "3", "4", "5" }, quest.extraButton)
				.setHoverText("quest.hover.extra.button.type", EnumScriptType.QUEST_LOG_BUTTON.function));
		// extra button text
		addLabel(new GuiNpcLabel(lId, "quest.extra.button.text", guiLeft + 5, (y += 16) + 2));
		addButton(new GuiNpcButton(6, x + 105, y, 60, 14, "selectServer.edit")
				.setIsEnable(quest.extraButton > 0)
				.setHoverText("quest.hover.extra.button.text"));
		// progress in chat / window
		addButton(new GuiNpcCheckBox(7, x, (y += 17), 239, 14, "quest.show.progress.in.chat", "", quest.showProgressInChat)
				.setHoverText("quest.hover.show.in.chat"));
		addButton(new GuiNpcCheckBox(8, x, y + 16, 239, 14, "quest.show.progress.in.window", "", quest.showProgressInWindow)
				.setHoverText("quest.hover.show.in.window"));
		// exit
		addButton(new GuiNpcButton(66, x, guiTop + ySize - 19, 60, 14, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (subgui == null) {
			int u = guiLeft + 214, v = guiTop + 5;
			if (getButton(0) != null) {
				u = getButton(0).x + getButton(0).width + 6;
				v = getButton(0).y;
			}
			if (isMouseHover(mouseX, mouseY, u, v, 32, 32)) {
				setSubGui(new SubGuiTextureSelection(0, showNpc, quest.icon.toString(), "png", 3));
				return true;
			}
			v = guiTop + 37;
			if (getButton(3) != null) {
				u = getButton(3).x + getButton(3).width + 6;
				v = getButton(3).y;
			}
			if (isMouseHover(mouseX, mouseY, u, v, 32, 32)) {
				setSubGui(new SubGuiTextureSelection(1, showNpc, quest.texture == null ? "" : quest.texture.toString(), "png", 3));
				return true;
			}
			if (isMouseHover(mouseX, mouseY, guiLeft + 182, guiTop + 95, 65, 65)) { setSubGui(new SubGuiNPCSelection(quest.completer)); }
		}
		return super.mouseCnpcsPressed(mouseX, mouseY, mouseButton);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			if (subgui.getId() == 0) { quest.rewardText = ((SubGuiNpcTextArea) subgui).text; }
			else if (subgui.getId() == 1) { quest.extraButtonText = ((SubGuiNpcTextArea) subgui).text; }
			initGui();
		}
		else if (subgui instanceof SubGuiTextureSelection) {
			if (subgui.getId() == 0) {
				quest.icon = ((SubGuiTextureSelection) subgui).resource;
				if (quest.icon == null) { quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png"); }
			}
			else { quest.texture = ((SubGuiTextureSelection) subgui).resource; }
			initGui();
		}
		else if (subgui instanceof SubGuiNPCSelection) {
			if (((SubGuiNPCSelection) subgui).selectEntity == null) { return; }
			Entity entity = mc.world.getEntityByID(((SubGuiNPCSelection) subgui).selectEntity.getEntityId());
			if (!(entity instanceof EntityNPCInterface)) { return; }
			quest.completer = Util.instance.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			showNpc = Util.instance.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getID() == 0) {
			if (textField.getText().isEmpty()) { quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png"); }
			else { quest.icon = new ResourceLocation(textField.getText()); }
			textField.setText(quest.icon.toString());
		}
		else if (textField.getID() == 1) {
			if (textField.getText().isEmpty()) { quest.texture = null; }
			else { quest.texture = new ResourceLocation(textField.getText()); }
			textField.setText(quest.texture == null ? "" : quest.texture.toString());
		}
	}

}
