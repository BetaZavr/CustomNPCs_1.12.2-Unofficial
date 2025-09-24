package noppes.npcs.client.gui.mainmenu;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcName;
import noppes.npcs.client.gui.select.SubGuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataDisplay;

import javax.annotation.Nonnull;
import java.awt.*;
import java.text.DecimalFormat;

public class GuiNpcDisplay extends GuiNPCInterface2 implements ITextfieldListener, IGuiData {

	protected final DecimalFormat df = new DecimalFormat("#.#");
	protected final DataDisplay display;
	protected boolean enableInvisibleNpcs;
	protected float baseHitBoxWidth;
	protected float baseHitBoxHeight;
	
	public GuiNpcDisplay(EntityNPCInterface npc) {
		super(npc, 1);
		parentGui = EnumGuiType.MainMenuAdvanced;

		display = npc.display;
		baseHitBoxWidth = npc.baseWidth;
		baseHitBoxHeight = npc.baseHeight;
		if (npc instanceof EntityCustomNpc && display.getModel() != null) {
			ModelData modeldata = ((EntityCustomNpc) npc).modelData;
			baseHitBoxWidth = modeldata.entity.width;
			baseHitBoxHeight = modeldata.entity.height;
		}
		Client.sendData(EnumPacketServer.MainmenuDisplayGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
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
			case 3: setSubGui(new SubGuiTextureSelection(0, npc, npc.display.getSkinTexture(), "png", 0)); break;
			case 4: display.setShadowType(button.getValue()); break;
			case 5: display.setHasLivingAnimation(button.getValue() == 0); break;
			case 7: {
				display.setVisible(button.getValue());
				initGui();
				break;
			}
			case 8: setSubGui(new SubGuiTextureSelection(1, npc, npc.display.getCapeTexture(), "png", 1)); break;
			case 9: setSubGui(new SubGuiTextureSelection(2, npc, npc.display.getOverlayTexture(), "png", 2)); break;
			case 10: display.setBossbar(button.getValue()); break;
			case 12: display.setBossColor(button.getValue()); break;
			case 13: display.setHitboxState((byte) button.getValue()); break;
			case 14: {
				String name = display.getRandomName();
				display.setName(name);
				getTextField(0).setText(name);
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
		addTextField(new GuiNpcTextField(0, this, guiLeft + 50, y, 206, 20, display.getName())
				.setHoverText("display.hover.name"));
		addButton(new GuiNpcButton(0, guiLeft + 253 + 52, y, 110, 20, new String[] { "display.show", "display.hide", "display.showAttacking" }, display.getShowName())
				.setHoverText("display.hover.show.name"));
		addButton(new GuiNpcButton(14, guiLeft + 259, y, 20, 20, Character.toString((char) 0x21BB))
				.setHoverText("display.hover.random.name"));
		addButton(new GuiNpcButton(15, guiLeft + 259 + 22, y, 20, 20, Character.toString((char) 0x22EE))
				.setHoverText("display.hover.group.name"));
		addLabel(new GuiNpcLabel(lID++, "gui.title", guiLeft + 5, (y += 23) + 5));
		addTextField(new GuiNpcTextField(11, this, guiLeft + 50, y, 186, 20, display.getTitle())
				.setHoverText("display.hover.subname"));
		addLabel(new GuiNpcLabel(lID++, "display.model", guiLeft + 5, (y += 23) + 5));
		addButton(new GuiNpcButton(1, guiLeft + 50, y, 110, 20, "selectServer.edit")
				.setHoverText("display.hover.set.model"));
		addLabel(new GuiNpcLabel(lID++, "display.size", guiLeft + 175, y + 5));
		addTextField(new GuiNpcTextField(2, this, guiLeft + 203, y, 40, 20, display.getSize() + "")
				.setMinMaxDefault(1, 30, 5)
				.setHoverText("display.hover.size"));
		addLabel(new GuiNpcLabel(lID++, "(1-30)", guiLeft + 246, y + 5));
		addLabel(new GuiNpcLabel(lID++, "display.texture", guiLeft + 5, (y += 23) + 5));
		addTextField(new GuiNpcTextField(3, this, guiLeft + 80, y, 200, 20, (display.skinType == 0) ? display.getSkinTexture() : display.getSkinUrl())
				.setHoverText("display.hover.skin." + display.skinType));
		if (display.skinType == 1 && !display.getSkinPlayer().isEmpty()) { getTextField(3).setText(display.getSkinPlayer()); }
		addButton(new GuiNpcButton(3, guiLeft + 325, y, 38, 20, "mco.template.button.select")
				.setIsEnable(display.skinType == 0)
				.setHoverText("display.hover.texture.set"));
		addButton(new GuiNpcButton(2, guiLeft + 283, y, 40, 20, new String[] { "display.texture", "display.player", "display.url" }, display.skinType)
				.setHoverText("display.hover.texture.type." + display.skinType));
		addLabel(new GuiNpcLabel(lID++, "display.cape", guiLeft + 5, (y += 23) + 5));
		addTextField(new GuiNpcTextField(8, this, guiLeft + 80, y, 200, 20, display.getCapeTexture())
				.setHoverText("display.hover.cloak"));
		addButton(new GuiNpcButton(8, guiLeft + 283, y, 80, 20, "display.selectTexture")
				.setHoverText("display.hover.texture.cloak"));
		addLabel(new GuiNpcLabel(lID++, "display.overlay", guiLeft + 5, (y += 23) + 5));
		addTextField(new GuiNpcTextField(9, this, guiLeft + 80, y, 200, 20, display.getOverlayTexture())
				.setHoverText("display.hover.eyes"));
		addButton(new GuiNpcButton(9, guiLeft + 283, y, 80, 20, "display.selectTexture")
				.setHoverText("display.hover.texture.eyes"));
		addLabel(new GuiNpcLabel(lID++, "display.livingAnimation", guiLeft + 5, (y += 23) + 5));
		addButton(new GuiNpcButton(5, guiLeft + 120, y, 50, 20, new String[] { "gui.yes", "gui.no" }, (display.getHasLivingAnimation() ? 0 : 1))
				.setHoverText("display.hover.animation"));
		addLabel(new GuiNpcLabel(lID++, "display.tint", guiLeft + 180, y + 5));
		StringBuilder color = new StringBuilder(Integer.toHexString(display.getTint()));
		while (color.length() < 6) { color.insert(0, "0"); }
		addTextField(new GuiNpcTextField(6, this, guiLeft + 220, y, 60, 20, color.toString())
				.setHoverText("display.hover.color"));
		getTextField(6).setTextColor(display.getTint());
		addLabel(new GuiNpcLabel(lID++, "display.shadow", guiLeft + 285, y + 5));
		addButton(new GuiNpcButton(4, guiLeft + 325, y, 50, 20, new String[] { "0%%", "50%%", "100%%", "150%%" }, display.getShadowType())
				.setHoverText("display.hover.shadow"));
		addLabel(new GuiNpcLabel(lID++, "display.visible", guiLeft + 5, (y += 23) + 5));
		addButton(new GuiNpcButton(7, guiLeft + 40, y, 50, 20, new String[] { "gui.yes", "gui.no", "gui.partly" }, display.getVisible())
				.setHoverText("display.hover.visible"));
		addButton(new GuiNpcButton(16, guiLeft + 92, y, 78, 20, "availability.available")
				.setHoverText("display.hover.visible." + (CustomNpcs.EnableInvisibleNpcs ? 1 : 0)));
		getButton(16).setIsEnable(enableInvisibleNpcs && display.getVisible() == 1);
		addLabel(new GuiNpcLabel(lID++, "display.interactable", guiLeft + 180, y + 5));
		int x = guiLeft + 240;
		addButton(new GuiNpcButton(13, x, y, 50, 20, new String[] { "gui.yes", "gui.no", "gui.solid"}, display.getHitboxState())
				.setHoverText("display.hover.interactable"));
		addLabel(new GuiNpcLabel(20, "W:", x += 54, y + 5));
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
		addTextField(new GuiNpcTextField(12, this, x += 8, y + 1, 50, 18, df.format(display.width))
				.setMinMaxDoubleDefault(-1.0, 7.5, display.width)
				.setHoverText("display.hover.hitbox.width", ("" + Math.round(baseHitBoxWidth * 1000.0) / 1000.0).replace(".", ","), ("" + Math.round(hitBoxWidth * 1000.0) / 1000.0).replace(".", ",")));
		addLabel(new GuiNpcLabel(21, "H:", x += 54, y + 5));
		float hitBoxHeight = npc.height;
		if (npc instanceof EntityCustomNpc) {
			if (display.getModel() != null) { hitBoxHeight = ((EntityCustomNpc) npc).modelData.entity.height; }
			hitBoxHeight = hitBoxHeight / 5.0f * display.getSize();
		}
		addTextField(new GuiNpcTextField(13, this, x + 8, y + 1, 50, 18, df.format(display.height))
				.setMinMaxDoubleDefault(-1.0, 15.0, display.height)
				.setHoverText(new TextComponentTranslation("display.hover.hitbox.height", ("" + Math.round(baseHitBoxHeight * 1000.0) / 1000.0).replace(".", ","), ("" + Math.round(hitBoxHeight * 1000.0) / 1000.0).replace(".", ",")).getFormattedText()));
		addLabel(new GuiNpcLabel(lID++, "display.bossbar", guiLeft + 5, (y += 23) + 5));
		addButton(new GuiNpcButton(10, guiLeft + 60, y, 110, 20, new String[] { "display.hide", "display.show", "display.showAttacking" }, display.getBossbar())
				.setHoverText("display.hover.boss.bar"));
		addLabel(new GuiNpcLabel(lID, "gui.color", guiLeft + 180, y + 5));
		addButton(new GuiNpcButton(12, guiLeft + 220, y, 110, 20, display.getBossColor(), "color.pink", "color.blue", "color.red", "color.green", "color.yellow", "color.purple", "color.white")
				.setHoverText("display.hover.bar.color"));
	}

	@Override
	public void save() {
		if (display.skinType == 1) { display.loadProfile(); }
		npc.textureLocation = null;
		mc.renderGlobal.onEntityRemoved(npc);
		mc.renderGlobal.onEntityAdded(npc);
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, display.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("MarkovGeneratorId", 3)) {
			display.readToNBT(compound);
			baseHitBoxWidth = npc.baseWidth;
			baseHitBoxHeight = npc.baseHeight;
			enableInvisibleNpcs = compound.getBoolean("EnableInvisibleNpcs");
			initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
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
				display.setTint(color);
				textfield.setTextColor(display.getTint());
				break;
			}
			case 8: display.setCapeTexture(textfield.getText()); break;
			case 9: display.setOverlayTexture(textfield.getText()); break;
			case 11: display.setTitle(textfield.getText()); break;
			case 12: {
				float f = (float) textfield.getDouble();
				display.width = f < 0.0f ? baseHitBoxWidth : f;
				if (f < 0.0f) { textfield.setText(df.format(display.width)); }
				break;
			}
			case 13: {
				float f = (float) textfield.getDouble();
				display.height = f < 0.0f ? baseHitBoxHeight : f;
				if (f < 0.0f) { textfield.setText(df.format(display.height)); }
				break;
			}
		}
	}

}
