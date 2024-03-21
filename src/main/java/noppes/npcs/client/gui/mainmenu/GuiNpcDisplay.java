package noppes.npcs.client.gui.mainmenu;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcName;
import noppes.npcs.client.gui.model.GuiCreationParts;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataDisplay;

public class GuiNpcDisplay
extends GuiNPCInterface2
implements ITextfieldListener, IGuiData {
	
	private DataDisplay display;
	private boolean enableInvisibleNpcs;

	public GuiNpcDisplay(EntityNPCInterface npc) {
		super(npc, 1);
		this.display = npc.display;
		Client.sendData(EnumPacketServer.MainmenuDisplayGet);
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 4;
		this.addLabel(new GuiNpcLabel(0, "gui.name", this.guiLeft + 5, y + 5));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 50, y, 206, 20, this.display.getName()));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 253 + 52, y, 110, 20, new String[] { "display.show", "display.hide", "display.showAttacking" }, this.display.getShowName()));
		this.addButton(new GuiNpcButton(14, this.guiLeft + 259, y, 20, 20, Character.toString('\u21bb')));
		this.addButton(new GuiNpcButton(15, this.guiLeft + 259 + 22, y, 20, 20, Character.toString('\u22ee')));
		y += 23;
		this.addLabel(new GuiNpcLabel(1, "gui.title", this.guiLeft + 5, y + 5));
		this.addTextField(new GuiNpcTextField(11, this, this.fontRenderer, this.guiLeft + 50, y, 186, 20, this.display.getTitle()));
		y += 23;
		this.addLabel(new GuiNpcLabel(2, "display.model", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 50, y, 110, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(3, "display.size", this.guiLeft + 175, y + 5));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 203, y, 40, 20, this.display.getSize() + ""));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(1, 30, 5);
		this.addLabel(new GuiNpcLabel(4, "(1-30)", this.guiLeft + 246, y + 5));
		y += 23;
		this.addLabel(new GuiNpcLabel(5, "display.texture", this.guiLeft + 5, y + 5));
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 80, y, 200, 20, (this.display.skinType == 0) ? this.display.getSkinTexture() : this.display.getSkinUrl()));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 325, y, 38, 20, "mco.template.button.select"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 283, y, 40, 20, new String[] { "display.texture", "display.player", "display.url" }, this.display.skinType));
		this.getButton(3).setEnabled(this.display.skinType == 0);
		if (this.display.skinType == 1 && !this.display.getSkinPlayer().isEmpty()) {
			this.getTextField(3).setText(this.display.getSkinPlayer());
		}
		y += 23;
		this.addLabel(new GuiNpcLabel(6, "display.cape", this.guiLeft + 5, y + 5));
		this.addTextField(new GuiNpcTextField(8, this, this.fontRenderer, this.guiLeft + 80, y, 200, 20, this.display.getCapeTexture()));
		this.addButton(new GuiNpcButton(8, this.guiLeft + 283, y, 80, 20, "display.selectTexture"));
		y += 23;
		this.addLabel(new GuiNpcLabel(7, "display.overlay", this.guiLeft + 5, y + 5));
		this.addTextField(new GuiNpcTextField(9, this, this.fontRenderer, this.guiLeft + 80, y, 200, 20, this.display.getOverlayTexture()));
		this.addButton(new GuiNpcButton(9, this.guiLeft + 283, y, 80, 20, "display.selectTexture"));
		y += 23;
		this.addLabel(new GuiNpcLabel(8, "display.livingAnimation", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 120, y, 50, 20, new String[] { "gui.yes", "gui.no" }, (this.display.getHasLivingAnimation() ? 0 : 1)));
		this.addLabel(new GuiNpcLabel(9, "display.tint", this.guiLeft + 180, y + 5));
		String color;
		for (color = Integer.toHexString(this.display.getTint()); color.length() < 6; color = "0" + color) { }
		this.addTextField(new GuiNpcTextField(6, this, this.guiLeft + 220, y, 60, 20, color));
		this.getTextField(6).setTextColor(this.display.getTint());
		
		this.addLabel(new GuiNpcLabel(10, "display.shadow", this.guiLeft + 285, y + 5));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 325, y, 50, 20, new String[] { "0%%", "50%%", "100%%", "150%%" }, this.display.getShadowType()));

		y += 23;
		this.addLabel(new GuiNpcLabel(11, "display.visible", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(7, this.guiLeft + 40, y, 50, 20,
				new String[] { "gui.yes", "gui.no", "gui.partly" }, this.display.getVisible()));
		this.addButton(new GuiNpcButton(16, this.guiLeft + 92, y, 78, 20, "availability.available"));
		this.getButton(16).enabled = (this.enableInvisibleNpcs && this.display.getVisible() == 1); // Changed
		this.addLabel(new GuiNpcLabel(12, "display.interactable", this.guiLeft + 180, y + 5));
		this.addButton(new GuiNpcButtonYesNo(13, this.guiLeft + 280, y, this.display.getHasHitbox()));
		y += 23;
		this.addLabel(new GuiNpcLabel(13, "display.bossbar", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(10, this.guiLeft + 60, y, 110, 20,
				new String[] { "display.hide", "display.show", "display.showAttacking" }, this.display.getBossbar()));
		this.addLabel(new GuiNpcLabel(14, "gui.color", this.guiLeft + 180, y + 5));
		this.addButton(new GuiNpcButton(12, this.guiLeft + 220, y, 110, 20, this.display.getBossColor(),
				new String[] { "color.pink", "color.blue", "color.red", "color.green", "color.yellow", "color.purple",
						"color.white" }));
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: {
				this.display.setShowName(button.getValue());
				break;
			}
			case 1: {
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiCreationParts(this.npc));
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
				this.display.setHasHitbox(((GuiNpcButtonYesNo) button).getBoolean());
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
				this.setSubGui(new SubGuiNpcAvailability(this.display.getAvailability()));
				break;
			}
			default: {
				
			}
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface subgui) {
		super.closeSubGui(subgui);
		this.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.name").getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.size").getFormattedText());
		} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.skin").getFormattedText());
		} else if (this.getTextField(6)!=null && this.getTextField(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.color").getFormattedText());
		} else if (this.getTextField(8)!=null && this.getTextField(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.cloak").getFormattedText());
		} else if (this.getTextField(9)!=null && this.getTextField(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.eyes").getFormattedText());
		} else if (this.getTextField(11)!=null && this.getTextField(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.subname").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.show.name").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.set.model").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.texture.type").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.texture.set").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.shadow").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.animation").getFormattedText());
		} else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.visible").getFormattedText());
		} else if (this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.texture.cloak").getFormattedText());
		} else if (this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.texture.eyes").getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.boss.bar").getFormattedText());
		} else if (this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.bar.color").getFormattedText());
		} else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.interactable").getFormattedText());
		} else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.random.name").getFormattedText());
		} 	else if (this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.group.name").getFormattedText());
		} else if (this.getButton(16)!=null && this.getButton(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.visible."+(CustomNpcs.EnableInvisibleNpcs ? 1 : 0)).getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
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
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 0) {
			if (!textfield.isEmpty()) {
				this.display.setName(textfield.getText());
			}
			textfield.setText(this.display.getName());
		} else if (textfield.getId() == 2) {
			this.display.setSize(textfield.getInteger());
		} else if (textfield.getId() == 3) {
			if (this.display.skinType == 2) {
				this.display.setSkinUrl(textfield.getText());
			} else if (this.display.skinType == 1) {
				this.display.setSkinPlayer(textfield.getText());
			} else {
				this.display.setSkinTexture(textfield.getText());
			}
		} else if (textfield.getId() == 6) {
			int color = 0;
			try {
				color = Integer.parseInt(textfield.getText(), 16);
			} catch (NumberFormatException e) {
				color = 16777215;
			}
			this.display.setTint(color);
			textfield.setTextColor(this.display.getTint());
		} else if (textfield.getId() == 8) {
			this.display.setCapeTexture(textfield.getText());
		} else if (textfield.getId() == 9) {
			this.display.setOverlayTexture(textfield.getText());
		} else if (textfield.getId() == 11) {
			this.display.setTitle(textfield.getText());
		}
	}

}
