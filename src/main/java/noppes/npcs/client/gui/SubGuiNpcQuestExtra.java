package noppes.npcs.client.gui;

import java.util.Arrays;
import java.util.UUID;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.select.GuiNPCSelection;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiNpcQuestExtra
extends SubGuiInterface
implements ITextfieldListener, ISubGuiListener {

	private static ResourceLocation sheet = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_3.png");
	private static ResourceLocation tabs = new ResourceLocation(CustomNpcs.MODID, "textures/quest log/q_log_4.png");
	private EntityNPCInterface npc;
	public Quest quest;
	
	public SubGuiNpcQuestExtra(int id, Quest q) {
		this.quest = q;
		this.id = id;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 5;
		this.addLabel(new GuiNpcLabel(0, "quest.icon", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 149, y, 60, 20, "availability.select"));
		y += 22;
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 5, y, 203, 20, this.quest.icon.toString()));
		y += 22;
		this.addLabel(new GuiNpcLabel(1, "quest.texture", this.guiLeft + 5, y + 5 ));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 149, y, 60, 20, "availability.select"));
		y += 22;
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 5, y, 203, 20, this.quest.texture==null ? "" : this.quest.texture.toString()));
		y += 48;
		this.addButton(new GuiNpcButton(1, this.guiLeft + 5, y, 100, 20, new String[] { "quest.npc", "quest.instant" }, this.quest.completion.ordinal()));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 110, y, 60, 20, "availability.select"));
		y += 38;
		this.addLabel(new GuiNpcLabel(2, "quest.questrewardtext", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 110, y, 60, 20, this.quest.rewardText.isEmpty() ? "selectServer.edit" : "advanced.editingmode"));
		
		this.addButton(new GuiNpcButton(66, this.guiLeft + 5, this.guiTop + this.ySize - 25, 60, 20, "gui.done"));
		
		this.npc = (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), this.mc.world);
		if (this.quest.completer!=null) {
			NBTTagCompound compound = new NBTTagCompound();
			this.quest.completer.writeToNBTOptional(compound);
			compound.setUniqueId("UUID", UUID.randomUUID());
			Entity e = EntityList.createEntityFromNBT(compound, this.quest.completer.world);
			if (e instanceof EntityNPCInterface) { this.npc = (EntityNPCInterface) e; }
			else { this.npc.readEntityFromNBT(compound); }
		}
		else {
			this.quest.completer = this.npc;
			this.quest.completerPos[0] = (int) this.npc.posX;
			this.quest.completerPos[1] = (int) (this.npc.posY + 0.5d);
			this.quest.completerPos[2] = (int) this.npc.posZ;
			this.quest.completerPos[3] = this.npc.world.provider.getDimension();
		}
		MarkData data = MarkData.get(this.npc);
		if(data!=null) { data.marks.clear(); }
		this.npc.display.setShowName(1);
		this.npc.rotationYaw = npc.rotationYaw;
		this.npc.rotationPitch = npc.rotationPitch;
		this.npc.ais.orientation = npc.ais.orientation;
		this.npc.ais.setStandingType(1);
		this.npc.animation.clear();
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: { // icon select
				GuiTextureSelection subGui = new GuiTextureSelection(this.npc, this.quest.icon.toString(), "png", 3);
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
				GuiTextureSelection subGui = new GuiTextureSelection(this.npc, this.quest.texture==null ? "" : this.quest.texture.toString(), "png", 3);
				subGui.id = 1;
				this.setSubGui(subGui);
				break;
			}
			case 4: { // reward text
				this.setSubGui(new SubGuiNpcTextArea(0, this.quest.rewardText));
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
		if (this.isMouseHover(mouseX, mouseY, this.guiLeft + 214, this.guiTop + 8, 32, 32)) {
			GuiTextureSelection subGui = new GuiTextureSelection(this.npc, this.quest.icon.toString(), "png", 3);
			subGui.id = 0;
			this.setSubGui(subGui);
			return;
		}
		if (this.isMouseHover(mouseX, mouseY, this.guiLeft + 214, this.guiTop + 52, 32, 32)) {
			GuiTextureSelection subGui = new GuiTextureSelection(this.npc, this.quest.texture==null ? "" : this.quest.texture.toString(), "png", 3);
			subGui.id = 1;
			this.setSubGui(subGui);
			return;
		}
		if (this.isMouseHover(mouseX, mouseY, this.guiLeft + 177, this.guiTop + 91, 66, 88)) {
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
		int u, v;
		if (this.npc!=null) {
			GlStateManager.pushMatrix();
			float size = (float) this.npc.display.getSize() * 0.38f;
			float s = 0.6f / this.npc.width;
			int h = 0;
			if (this.npc.height != size || this.npc.height < 1.9f) {
				h = (int) (24.76190f * this.npc.height - 47.04762f);
			}
			h -= (int) (-36.0f * s + 36.0f);
			this.drawNpc(this.npc, 212, 168 + h, s, 30, 15, 0);
			GlStateManager.popMatrix();
			
			u = this.guiLeft + 182;
			v = this.guiTop + 97;
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.translate(u + 1.0f, v + 1.0f, 100.0f);
			// Fase
			this.mc.renderEngine.bindTexture(SubGuiNpcQuestExtra.tabs);
			this.drawTexturedModalRect(0, 0, 0, 0, 56, 43);
			// Back
			GlStateManager.translate(0.0f, 0.0f, -100.0f);
			this.mc.renderEngine.bindTexture(SubGuiNpcQuestExtra.sheet);
			this.drawTexturedModalRect(-5, -5, 34, 54, 65, 85);
			// Front
			GlStateManager.translate(0.0f, 0.0f, 99.0f);
			this.drawTexturedModalRect(-5, -5, 34, 54, 32, 12);
			this.drawTexturedModalRect(27, -5, 66, 54, 33, 10);
			this.drawTexturedModalRect(-5, 7, 34, 66, 10, 29);
			this.drawTexturedModalRect(50, 5, 34, 64, 10, 31);
			this.drawTexturedModalRect(-5, 36, 34, 83, 65, 44);
			String name = ((char) 167)+"l"+this.quest.completer.getName();
			this.mc.fontRenderer.drawString(name, 61 - this.mc.fontRenderer.getStringWidth(name), 44, CustomNpcs.questLogColor, false);
			GlStateManager.disableBlend();
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
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
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
		if (subgui instanceof GuiTextureSelection) {
			if (((GuiTextureSelection) subgui).id==0) {
				this.quest.icon = ((GuiTextureSelection) subgui).resource;
				if (this.quest.icon == null) { this.quest.icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png"); }
			} else {
				this.quest.texture = ((GuiTextureSelection) subgui).resource;
			}
			this.initGui();
		}
		if (subgui instanceof GuiNPCSelection) {
			if (((GuiNPCSelection) subgui).selectEntity==null) { return; }
			Entity entity = this.mc.world.getEntityByID(((GuiNPCSelection) subgui).selectEntity.getEntityId());
			if (entity==null) { return; }
			NBTTagCompound compound = new NBTTagCompound();
			entity.writeToNBTOptional(compound);
			compound.setUniqueId("UUID", UUID.randomUUID());
			Entity e = EntityList.createEntityFromNBT(compound, this.mc.world);
			if (e instanceof EntityNPCInterface) {
				this.quest.completer = (EntityNPCInterface) e;
				this.quest.completerPos[0] = (int) e.posX;
				this.quest.completerPos[1] = (int) (e.posY + 0.5d);
				this.quest.completerPos[2] = (int) e.posZ;
				this.quest.completerPos[3] = e.world.provider.getDimension();
			}
			MarkData data = MarkData.get(this.quest.completer);
			if(data!=null) { data.marks.clear(); }
			this.quest.completer.display.setShowName(1);
			this.quest.completer.animation.clear();
			this.initGui();
		}
		if (subgui instanceof SubGuiNpcTextArea) {
			this.quest.rewardText = ((SubGuiNpcTextArea) subgui).text;
		}
	}

}
