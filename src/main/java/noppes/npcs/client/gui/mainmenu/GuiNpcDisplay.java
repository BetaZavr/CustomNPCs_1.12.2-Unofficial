package noppes.npcs.client.gui.mainmenu;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcName;
import noppes.npcs.client.gui.model.GuiCreationParts;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataDisplay;

import java.awt.*;

public class GuiNpcDisplay
extends GuiNPCInterface2
implements ITextfieldListener, IGuiData, ISubGuiListener {

	private final DataDisplay display;
	private boolean enableInvisibleNpcs;
	private float baseHitBoxWidth, baseHitBoxHeight;
	
	public GuiNpcDisplay(EntityNPCInterface npc) {
		super(npc, 1);
		this.display = npc.display;
		Client.sendData(EnumPacketServer.MainmenuDisplayGet);
		
		this.baseHitBoxWidth = 0.8f;
		this.baseHitBoxHeight = npc.baseHeight;

		if (npc instanceof EntityCustomNpc && this.display.getModel() != null) {
			ModelData modeldata = ((EntityCustomNpc) npc).modelData;
			this.baseHitBoxWidth = modeldata.entity.width;
			this.baseHitBoxHeight = modeldata.entity.height;
		}
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: {
				this.display.setShowName(button.getValue());
				break;
			}
			case 1: {
				NoppesUtil.openGUI(this.player, new GuiCreationParts(this.npc));
				break;
			}
			case 2: {
				this.display.setSkinUrl("");
				this.display.setSkinPlayer(null);
				this.display.skinType = (byte) button.getValue();
				this.initGui();
				break;
			}
			case 3: {
				this.setSubGui(new GuiTextureSelection(this.npc, this.npc.display.getSkinTexture(), "png", 0));
				break;
			}
			case 4: {
				this.display.setShadowType(button.getValue());
				break;
			}
			case 5: {
				this.display.setHasLivingAnimation(button.getValue() == 0);
				break;
			}
			case 7: {
				this.display.setVisible(button.getValue());
				this.initGui();
				break;
			}
			case 8: {
				this.setSubGui(new GuiTextureSelection(this.npc, this.npc.display.getCapeTexture(), "png", 1));
				break;
			}
			case 9: {
				this.setSubGui(new GuiTextureSelection(this.npc, this.npc.display.getOverlayTexture(), "png", 2));
				break;
			}
			case 10: {
				this.display.setBossbar(button.getValue());
				break;
			}
			case 12: {
				this.display.setBossColor(button.getValue());
				break;
			}
			case 13: {
				this.display.setHasHitbox(button.getValue() != 1);
				this.getLabel(20).setEnabled(button.getValue() == 2);
				this.getLabel(21).setEnabled(button.getValue() == 2);
				this.getTextField(12).setVisible(button.getValue() == 2);
				this.getTextField(13).setVisible(button.getValue() == 2);
				break;
			}
			case 14: {
				String name = this.display.getRandomName();
				this.display.setName(name);
				this.getTextField(0).setText(name);
				break;
			}
			case 15: {
				this.setSubGui(new SubGuiNpcName(this.display));
				break;
			}
			case 16: {
				this.setSubGui(new SubGuiNpcAvailability(display.getAvailability(), this));
				break;
			}
			default: break;
		}
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		initGui();
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 4;
		int lID = 0;
		this.addLabel(new GuiNpcLabel(lID++, "gui.name", this.guiLeft + 5, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 50, y, 206, 20, this.display.getName());
		textField.setHoverText("display.hover.name");
		addTextField(textField);
		GuiNpcButton button = new GuiNpcButton(0, this.guiLeft + 253 + 52, y, 110, 20, new String[] { "display.show", "display.hide", "display.showAttacking" }, this.display.getShowName());
		button.setHoverText("display.hover.show.name");
		addButton(button);
		button = new GuiNpcButton(14, this.guiLeft + 259, y, 20, 20, Character.toString((char) 8635));
		button.setHoverText("display.hover.random.name");
		addButton(button);
		button = new GuiNpcButton(15, this.guiLeft + 259 + 22, y, 20, 20, Character.toString((char) 8942));
		button.setHoverText("display.hover.group.name");
		addButton(button);
		y += 23;
		this.addLabel(new GuiNpcLabel(lID++, "gui.title", this.guiLeft + 5, y + 5));
		textField = new GuiNpcTextField(11, this, this.fontRenderer, this.guiLeft + 50, y, 186, 20, this.display.getTitle());
		textField.setHoverText("display.hover.subname");
		addTextField(textField);
		y += 23;
		this.addLabel(new GuiNpcLabel(lID++, "display.model", this.guiLeft + 5, y + 5));
		button = new GuiNpcButton(1, this.guiLeft + 50, y, 110, 20, "selectServer.edit");
		button.setHoverText("display.hover.set.model");
		addButton(button);
		this.addLabel(new GuiNpcLabel(lID++, "display.size", this.guiLeft + 175, y + 5));
		textField = new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 203, y, 40, 20, this.display.getSize() + "");
		textField.setMinMaxDefault(1, 30, 5);
		textField.setHoverText("display.hover.size");
		addTextField(textField);
		this.addLabel(new GuiNpcLabel(lID++, "(1-30)", this.guiLeft + 246, y + 5));
		y += 23;
		this.addLabel(new GuiNpcLabel(lID++, "display.texture", this.guiLeft + 5, y + 5));
		textField = new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 80, y, 200, 20, (this.display.skinType == 0) ? this.display.getSkinTexture() : this.display.getSkinUrl());
		if (this.display.skinType == 1 && !this.display.getSkinPlayer().isEmpty()) { textField.setText(this.display.getSkinPlayer()); }
		textField.setHoverText("display.hover.skin." + display.skinType);
		addTextField(textField);
		button = new GuiNpcButton(3, this.guiLeft + 325, y, 38, 20, "mco.template.button.select");
		button.setHoverText("display.hover.texture.set");
		addButton(button);
		button = new GuiNpcButton(2, this.guiLeft + 283, y, 40, 20, new String[] { "display.texture", "display.player", "display.url" }, this.display.skinType);
		button.setHoverText("display.hover.texture.type." + display.skinType);
		addButton(button);
		this.getButton(3).setEnabled(this.display.skinType == 0);
		y += 23;
		this.addLabel(new GuiNpcLabel(lID++, "display.cape", this.guiLeft + 5, y + 5));
		textField = new GuiNpcTextField(8, this, this.fontRenderer, this.guiLeft + 80, y, 200, 20, this.display.getCapeTexture());
		textField.setHoverText("display.hover.cloak");
		addTextField(textField);
		button = new GuiNpcButton(8, this.guiLeft + 283, y, 80, 20, "display.selectTexture");
		button.setHoverText("display.hover.texture.cloak");
		addButton(button);
		y += 23;
		this.addLabel(new GuiNpcLabel(lID++, "display.overlay", this.guiLeft + 5, y + 5));
		textField = new GuiNpcTextField(9, this, this.fontRenderer, this.guiLeft + 80, y, 200, 20, this.display.getOverlayTexture());
		textField.setHoverText("display.hover.eyes");
		addTextField(textField);
		button = new GuiNpcButton(9, this.guiLeft + 283, y, 80, 20, "display.selectTexture");
		button.setHoverText("display.hover.texture.eyes");
		addButton(button);
		y += 23;
		this.addLabel(new GuiNpcLabel(lID++, "display.livingAnimation", this.guiLeft + 5, y + 5));
		button = new GuiNpcButton(5, this.guiLeft + 120, y, 50, 20, new String[] { "gui.yes", "gui.no" }, (this.display.getHasLivingAnimation() ? 0 : 1));
		button.setHoverText("display.hover.animation");
		addButton(button);
		this.addLabel(new GuiNpcLabel(lID++, "display.tint", this.guiLeft + 180, y + 5));
		StringBuilder color = new StringBuilder(Integer.toHexString(this.display.getTint()));
		while (color.length() < 6) { color.insert(0, "0"); }
		textField = new GuiNpcTextField(6, this, this.guiLeft + 220, y, 60, 20, color.toString());
		textField.setTextColor(this.display.getTint());
		textField.setHoverText("display.hover.color");
		addTextField(textField);

		this.addLabel(new GuiNpcLabel(lID++, "display.shadow", this.guiLeft + 285, y + 5));
		button = new GuiNpcButton(4, this.guiLeft + 325, y, 50, 20, new String[] { "0%%", "50%%", "100%%", "150%%" }, this.display.getShadowType());
		button.setHoverText("display.hover.shadow");
		addButton(button);
		y += 23;
		this.addLabel(new GuiNpcLabel(lID++, "display.visible", this.guiLeft + 5, y + 5));
		button = new GuiNpcButton(7, this.guiLeft + 40, y, 50, 20, new String[] { "gui.yes", "gui.no", "gui.partly" }, this.display.getVisible());
		button.setHoverText("display.hover.visible");
		addButton(button);
		button = new GuiNpcButton(16, this.guiLeft + 92, y, 78, 20, "availability.available");
		button.setHoverText("display.hover.visible." + (CustomNpcs.EnableInvisibleNpcs ? 1 : 0));
		addButton(button);
		this.getButton(16).setEnabled(this.enableInvisibleNpcs && this.display.getVisible() == 1);
		this.addLabel(new GuiNpcLabel(lID++, "display.interactable", this.guiLeft + 180, y + 5));
		
		int hb = this.display.getHasHitbox() ? 0 : 1;
		if (this.display.getHasHitbox() && this.display.width != 0.0f && this.display.height != 0.0f) { hb = 2; }
		int x = this.guiLeft + 240;
		button = new GuiNpcButton(13, x, y, 50, 20, new String[] { "gui.yes", "gui.no", "gui.set"}, hb);
		button.setHoverText("display.hover.interactable");
		addButton(button);

		this.addLabel(new GuiNpcLabel(20, "W:", x += 54, y + 5));
		textField = new GuiNpcTextField(12, this, this.fontRenderer, x += 8, y + 1, 50, 18, "" + this.display.width);
		float w = this.npc.width;
		if (this.npc instanceof EntityCustomNpc) {
			float scaleHead = 1.0f, scaleBody = 1.0f;
			if (this.display.getModel() != null) {
				ModelData modeldata = ((EntityCustomNpc) npc).modelData;
				ModelPartConfig model = modeldata.getPartConfig(EnumParts.HEAD);
				scaleHead = Math.max(model.scale[0], model.scale[2]);
				model = modeldata.getPartConfig(EnumParts.BODY);
				scaleBody = Math.max(model.scale[0], model.scale[2]);
				w = modeldata.entity.width;
			}
			w *= Math.max(scaleHead, scaleBody);
			w = w / 5.0f * this.display.getSize();
		}
		textField.setVisible(hb == 2);
		textField.setMinMaxDoubleDefault(0.0, 7.5, this.display.width);
		textField.setHoverText("display.hover.hitbox.width", ("" + Math.round(this.baseHitBoxWidth * 1000.0) / 1000.0).replace(".", ","), ("" + Math.round(w * 1000.0) / 1000.0).replace(".", ","));
		addTextField(textField);
		this.addLabel(new GuiNpcLabel(21, "H:", x += 54, y + 5));
		textField = new GuiNpcTextField(13, this, this.fontRenderer, x + 8, y + 1, 50, 18, "" + this.display.height);
		float h = this.npc.height;
		if (this.npc instanceof EntityCustomNpc) {
			if (this.display.getModel() != null) { h = ((EntityCustomNpc) npc).modelData.entity.height; }
			h = h / 5.0f * this.display.getSize();
		}
		textField.setVisible(hb == 2);
		textField.setMinMaxDoubleDefault(0.0, 15.0, this.display.height);
		textField.setHoverText(new TextComponentTranslation("display.hover.hitbox.height", ("" + Math.round(this.baseHitBoxHeight * 1000.0) / 1000.0).replace(".", ","), ("" + Math.round(h * 1000.0) / 1000.0).replace(".", ",")).getFormattedText());
		addTextField(textField);
		this.getLabel(20).setEnabled(hb == 2);
		this.getLabel(21).setEnabled(hb == 2);
				
		y += 23;
		this.addLabel(new GuiNpcLabel(lID++, "display.bossbar", this.guiLeft + 5, y + 5));
		button = new GuiNpcButton(10, this.guiLeft + 60, y, 110, 20, new String[] { "display.hide", "display.show", "display.showAttacking" }, this.display.getBossbar());
		button.setHoverText("display.hover.boss.bar");
		addButton(button);
		this.addLabel(new GuiNpcLabel(lID, "gui.color", this.guiLeft + 180, y + 5));
		button = new GuiNpcButton(12, this.guiLeft + 220, y, 110, 20, this.display.getBossColor(), "color.pink", "color.blue", "color.red", "color.green", "color.yellow", "color.purple", "color.white");
		button.setHoverText("display.hover.bar.color");
		addButton(button);
	}

	@Override
	public void save() {
		if (this.display.skinType == 1) {
			this.display.loadProfile();
		}
		this.npc.textureLocation = null;
		this.mc.renderGlobal.onEntityRemoved(this.npc);
		this.mc.renderGlobal.onEntityAdded(this.npc);
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, this.display.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.display.readToNBT(compound);
		this.enableInvisibleNpcs = compound.getBoolean("EnableInvisibleNpcs");
		this.initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 0: {
				if (!textfield.isEmpty()) { display.setName(textfield.getText()); }
				textfield.setText(display.getName());
				break;
			}
			case 2: display.setSize(textfield.getInteger()); break;
			case 3: {
				if (display.skinType == 2) { display.setSkinUrl(textfield.getText()); }
				else if (display.skinType == 1) { display.setSkinPlayer(textfield.getText()); }
				else { display.setSkinTexture(textfield.getText()); }
				break;
			}
			case 6: {
				int color;
				try { color = Integer.parseInt(textfield.getText(), 16); }
				catch (NumberFormatException e) { color = new Color(0xFFFFFF).getRGB(); }
				this.display.setTint(color);
				textfield.setTextColor(this.display.getTint());
				break;
			}
			case 8: display.setCapeTexture(textfield.getText()); break;
			case 9: display.setOverlayTexture(textfield.getText()); break;
			case 11: display.setTitle(textfield.getText()); break;
			case 12: display.width = (float) textfield.getDouble(); break;
			case 13: display.height = (float) textfield.getDouble(); break;
		}
	}

}
