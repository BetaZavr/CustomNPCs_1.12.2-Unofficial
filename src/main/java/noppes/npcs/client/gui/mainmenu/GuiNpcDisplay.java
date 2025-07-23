package noppes.npcs.client.gui.mainmenu;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcName;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataDisplay;

import java.awt.*;
import java.text.DecimalFormat;

public class GuiNpcDisplay
extends GuiNPCInterface2
implements ITextfieldListener, IGuiData, ISubGuiListener {

	private final DataDisplay display;
	private boolean enableInvisibleNpcs;
	private float baseHitBoxWidth;
	private float baseHitBoxHeight;
    private final DecimalFormat df = new DecimalFormat("#.#");
	
	public GuiNpcDisplay(EntityNPCInterface npc) {
		super(npc, 1);
		display = npc.display;
		Client.sendData(EnumPacketServer.MainmenuDisplayGet);
		
		baseHitBoxWidth = npc.baseWidth;
		baseHitBoxHeight = npc.baseHeight;

		if (npc instanceof EntityCustomNpc && display.getModel() != null) {
			ModelData modeldata = ((EntityCustomNpc) npc).modelData;
			baseHitBoxWidth = modeldata.entity.width;
			baseHitBoxHeight = modeldata.entity.height;
		}
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: display.setShowName(button.getValue()); break;
			case 1: NoppesUtil.requestOpenGUI(EnumGuiType.CreationParts); break;
			case 2: {
				display.setSkinUrl("");
				display.setSkinPlayer(null);
				display.skinType = (byte) button.getValue();
				initGui();
				break;
			}
			case 3: setSubGui(new GuiTextureSelection(npc, npc.display.getSkinTexture(), "png", 0)); break;
			case 4: display.setShadowType(button.getValue()); break;
			case 5: display.setHasLivingAnimation(button.getValue() == 0); break;
			case 7: {
				display.setVisible(button.getValue());
				initGui();
				break;
			}
			case 8: setSubGui(new GuiTextureSelection(npc, npc.display.getCapeTexture(), "png", 1)); break;
			case 9: setSubGui(new GuiTextureSelection(npc, npc.display.getOverlayTexture(), "png", 2)); break;
			case 10: display.setBossbar(button.getValue()); break;
			case 12: display.setBossColor(button.getValue()); break;
			case 13: display.setHitboxState((byte) button.getValue()); break;
			case 14: {
				String name = display.getRandomName();
				display.setName(name);
				getTextField(0).setFullText(name);
				break;
			}
			case 15: setSubGui(new SubGuiNpcName(display)); break;
			case 16: setSubGui(new SubGuiNpcAvailability(display.getAvailability(), this)); break;
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		initGui();
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 4;
		int lID = 0;
		addLabel(new GuiNpcLabel(lID++, "gui.name", guiLeft + 5, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, guiLeft + 50, y, 206, 20, display.getName());
		textField.setHoverText("display.hover.name");
		addTextField(textField);
		GuiNpcButton button = new GuiNpcButton(0, guiLeft + 253 + 52, y, 110, 20, new String[] { "display.show", "display.hide", "display.showAttacking" }, display.getShowName());
		button.setHoverText("display.hover.show.name");
		addButton(button);
		button = new GuiNpcButton(14, guiLeft + 259, y, 20, 20, Character.toString((char) 8635));
		button.setHoverText("display.hover.random.name");
		addButton(button);
		button = new GuiNpcButton(15, guiLeft + 259 + 22, y, 20, 20, Character.toString((char) 8942));
		button.setHoverText("display.hover.group.name");
		addButton(button);
		y += 23;
		addLabel(new GuiNpcLabel(lID++, "gui.title", guiLeft + 5, y + 5));
		textField = new GuiNpcTextField(11, this, fontRenderer, guiLeft + 50, y, 186, 20, display.getTitle());
		textField.setHoverText("display.hover.subname");
		addTextField(textField);
		y += 23;
		addLabel(new GuiNpcLabel(lID++, "display.model", guiLeft + 5, y + 5));
		button = new GuiNpcButton(1, guiLeft + 50, y, 110, 20, "selectServer.edit");
		button.setHoverText("display.hover.set.model");
		addButton(button);
		addLabel(new GuiNpcLabel(lID++, "display.size", guiLeft + 175, y + 5));
		textField = new GuiNpcTextField(2, this, fontRenderer, guiLeft + 203, y, 40, 20, display.getSize() + "");
		textField.setMinMaxDefault(1, 30, 5);
		textField.setHoverText("display.hover.size");
		addTextField(textField);
		addLabel(new GuiNpcLabel(lID++, "(1-30)", guiLeft + 246, y + 5));
		y += 23;
		addLabel(new GuiNpcLabel(lID++, "display.texture", guiLeft + 5, y + 5));
		textField = new GuiNpcTextField(3, this, fontRenderer, guiLeft + 80, y, 200, 20, (display.skinType == 0) ? display.getSkinTexture() : display.getSkinUrl());
		if (display.skinType == 1 && !display.getSkinPlayer().isEmpty()) { textField.setText(display.getSkinPlayer()); }
		textField.setHoverText("display.hover.skin." + display.skinType);
		addTextField(textField);
		button = new GuiNpcButton(3, guiLeft + 325, y, 38, 20, "mco.template.button.select");
		button.setHoverText("display.hover.texture.set");
		addButton(button);
		button = new GuiNpcButton(2, guiLeft + 283, y, 40, 20, new String[] { "display.texture", "display.player", "display.url" }, display.skinType);
		button.setHoverText("display.hover.texture.type." + display.skinType);
		addButton(button);
		getButton(3).setEnabled(display.skinType == 0);
		y += 23;
		addLabel(new GuiNpcLabel(lID++, "display.cape", guiLeft + 5, y + 5));
		textField = new GuiNpcTextField(8, this, fontRenderer, guiLeft + 80, y, 200, 20, display.getCapeTexture());
		textField.setHoverText("display.hover.cloak");
		addTextField(textField);
		button = new GuiNpcButton(8, guiLeft + 283, y, 80, 20, "display.selectTexture");
		button.setHoverText("display.hover.texture.cloak");
		addButton(button);
		y += 23;
		addLabel(new GuiNpcLabel(lID++, "display.overlay", guiLeft + 5, y + 5));
		textField = new GuiNpcTextField(9, this, fontRenderer, guiLeft + 80, y, 200, 20, display.getOverlayTexture());
		textField.setHoverText("display.hover.eyes");
		addTextField(textField);
		button = new GuiNpcButton(9, guiLeft + 283, y, 80, 20, "display.selectTexture");
		button.setHoverText("display.hover.texture.eyes");
		addButton(button);
		y += 23;
		addLabel(new GuiNpcLabel(lID++, "display.livingAnimation", guiLeft + 5, y + 5));
		button = new GuiNpcButton(5, guiLeft + 120, y, 50, 20, new String[] { "gui.yes", "gui.no" }, (display.getHasLivingAnimation() ? 0 : 1));
		button.setHoverText("display.hover.animation");
		addButton(button);
		addLabel(new GuiNpcLabel(lID++, "display.tint", guiLeft + 180, y + 5));
		StringBuilder color = new StringBuilder(Integer.toHexString(display.getTint()));
		while (color.length() < 6) { color.insert(0, "0"); }
		textField = new GuiNpcTextField(6, this, guiLeft + 220, y, 60, 20, color.toString());
		textField.setTextColor(display.getTint());
		textField.setHoverText("display.hover.color");
		addTextField(textField);

		addLabel(new GuiNpcLabel(lID++, "display.shadow", guiLeft + 285, y + 5));
		button = new GuiNpcButton(4, guiLeft + 325, y, 50, 20, new String[] { "0%%", "50%%", "100%%", "150%%" }, display.getShadowType());
		button.setHoverText("display.hover.shadow");
		addButton(button);
		y += 23;
		addLabel(new GuiNpcLabel(lID++, "display.visible", guiLeft + 5, y + 5));
		button = new GuiNpcButton(7, guiLeft + 40, y, 50, 20, new String[] { "gui.yes", "gui.no", "gui.partly" }, display.getVisible());
		button.setHoverText("display.hover.visible");
		addButton(button);
		button = new GuiNpcButton(16, guiLeft + 92, y, 78, 20, "availability.available");
		button.setHoverText("display.hover.visible." + (CustomNpcs.EnableInvisibleNpcs ? 1 : 0));
		addButton(button);
		getButton(16).setEnabled(enableInvisibleNpcs && display.getVisible() == 1);
		addLabel(new GuiNpcLabel(lID++, "display.interactable", guiLeft + 180, y + 5));

		int x = guiLeft + 240;
		button = new GuiNpcButton(13, x, y, 50, 20, new String[] { "gui.yes", "gui.no", "gui.solid"}, display.getHitboxState());
		button.setHoverText("display.hover.interactable");
		addButton(button);

		addLabel(new GuiNpcLabel(20, "W:", x += 54, y + 5));
		textField = new GuiNpcTextField(12, this, fontRenderer, x += 8, y + 1, 50, 18, df.format(display.width));
        float hitBoxWidth = npc.width;
		if (npc instanceof EntityCustomNpc) {
			float scaleHead = 1.0f, scaleBody = 1.0f;
			if (display.getModel() != null) {
				ModelData modeldata = ((EntityCustomNpc) npc).modelData;
				ModelPartConfig model = modeldata.getPartConfig(EnumParts.HEAD);
				scaleHead = Math.max(model.scale[0], model.scale[2]);
				model = modeldata.getPartConfig(EnumParts.BODY);
				scaleBody = Math.max(model.scale[0], model.scale[2]);
				hitBoxWidth = modeldata.entity.width;
			}
			hitBoxWidth *= Math.max(scaleHead, scaleBody);
			hitBoxWidth = hitBoxWidth / 5.0f * display.getSize();
		}
		textField.setMinMaxDoubleDefault(-1.0, 7.5, display.width);
		textField.setHoverText("display.hover.hitbox.width", ("" + Math.round(baseHitBoxWidth * 1000.0) / 1000.0).replace(".", ","), ("" + Math.round(hitBoxWidth * 1000.0) / 1000.0).replace(".", ","));
		addTextField(textField);
		addLabel(new GuiNpcLabel(21, "H:", x += 54, y + 5));
		textField = new GuiNpcTextField(13, this, fontRenderer, x + 8, y + 1, 50, 18, df.format(display.height));
        float hitBoxHeight = npc.height;
		if (npc instanceof EntityCustomNpc) {
			if (display.getModel() != null) { hitBoxHeight = ((EntityCustomNpc) npc).modelData.entity.height; }
			hitBoxHeight = hitBoxHeight / 5.0f * display.getSize();
		}
		textField.setMinMaxDoubleDefault(-1.0, 15.0, display.height);
		textField.setHoverText(new TextComponentTranslation("display.hover.hitbox.height", ("" + Math.round(baseHitBoxHeight * 1000.0) / 1000.0).replace(".", ","), ("" + Math.round(hitBoxHeight * 1000.0) / 1000.0).replace(".", ",")).getFormattedText());
		addTextField(textField);
				
		y += 23;
		addLabel(new GuiNpcLabel(lID++, "display.bossbar", guiLeft + 5, y + 5));
		button = new GuiNpcButton(10, guiLeft + 60, y, 110, 20, new String[] { "display.hide", "display.show", "display.showAttacking" }, display.getBossbar());
		button.setHoverText("display.hover.boss.bar");
		addButton(button);
		addLabel(new GuiNpcLabel(lID, "gui.color", guiLeft + 180, y + 5));
		button = new GuiNpcButton(12, guiLeft + 220, y, 110, 20, display.getBossColor(), "color.pink", "color.blue", "color.red", "color.green", "color.yellow", "color.purple", "color.white");
		button.setHoverText("display.hover.bar.color");
		addButton(button);
	}

	@Override
	public void save() {
		if (display.skinType == 1) {
			display.loadProfile();
		}
		npc.textureLocation = null;
		mc.renderGlobal.onEntityRemoved(npc);
		mc.renderGlobal.onEntityAdded(npc);
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, display.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		display.readToNBT(compound);
		baseHitBoxWidth = npc.baseWidth;
		baseHitBoxHeight = npc.baseHeight;
		enableInvisibleNpcs = compound.getBoolean("EnableInvisibleNpcs");
		initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 0: {
				if (!textfield.isEmpty()) { display.setName(textfield.getFullText()); }
				textfield.setFullText(display.getName());
				break;
			}
			case 2: display.setSize(textfield.getInteger()); break;
			case 3: {
				if (display.skinType == 2) { display.setSkinUrl(textfield.getFullText()); }
				else if (display.skinType == 1) { display.setSkinPlayer(textfield.getFullText()); }
				else { display.setSkinTexture(textfield.getFullText()); }
				break;
			}
			case 6: {
				int color;
				try { color = Integer.parseInt(textfield.getFullText(), 16); }
				catch (NumberFormatException e) { color = new Color(0xFFFFFF).getRGB(); }
				this.display.setTint(color);
				((GuiNpcTextField) textfield).setTextColor(this.display.getTint());
				break;
			}
			case 8: display.setCapeTexture(textfield.getFullText()); break;
			case 9: display.setOverlayTexture(textfield.getFullText()); break;
			case 11: display.setTitle(textfield.getFullText()); break;
			case 12: {
				float f = (float) textfield.getDouble();
				display.width = f < 0.0f ? baseHitBoxWidth : f;
				if (f < 0.0f) { textfield.setFullText(df.format(display.width)); }
				break;
			}
			case 13: {
				float f = (float) textfield.getDouble();
				display.height = f < 0.0f ? baseHitBoxHeight : f;
				if (f < 0.0f) { textfield.setFullText(df.format(display.height)); }
				break;
			}
		}
	}

}
