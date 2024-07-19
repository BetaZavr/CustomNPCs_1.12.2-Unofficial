package noppes.npcs.client.gui.mainmenu;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcMovement;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAI;

public class GuiNpcAI extends GuiNPCInterface2 implements ITextfieldListener, IGuiData {

	private final DataAI ai;
	private final String[] tactics;

	public GuiNpcAI(EntityNPCInterface npc) {
		super(npc, 6);
		this.tactics = new String[] { "aitactics.rush", "aitactics.stagger", "aitactics.orbit", "aitactics.hitandrun", "aitactics.commander", "aitactics.stalk", "gui.none" };
		this.ai = npc.ais;
		Client.sendData(EnumPacketServer.MainmenuAIGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			this.ai.onAttack = button.getValue();
			this.initGui();
		} else if (button.id == 1) {
			this.ai.doorInteract = button.getValue();
		} else if (button.id == 2) {
			this.setSubGui(new SubGuiNpcMovement(this.ai));
		} else if (button.id == 5) {
			this.npc.ais.setAvoidsWater(button.getValue() == 1);
		} else if (button.id == 6) {
			this.ai.returnToStart = (button.getValue() == 1);
		} else if (button.id == 7) {
			this.ai.canSwim = (button.getValue() == 1);
		} else if (button.id == 9) {
			this.ai.findShelter = button.getValue();
		} else if (button.id == 10) {
			this.ai.directLOS = (button.getValue() == 1);
		} else if (button.id == 15) {
			this.ai.canLeap = (button.getValue() == 1);
		} else if (button.id == 16) {
			this.ai.canSprint = (button.getValue() == 1);
		} else if (button.id == 17) {
			this.ai.tacticalVariant = button.getValue();
			this.ai.directLOS = (button.getValue() != 5 && this.ai.directLOS);
			this.initGui();
		} else if (button.id == 23) {
			this.ai.attackInvisible = ((GuiNpcButtonYesNo) button).getBoolean();
		} else if (button.id == 25) {
			this.ai.aiDisabled = (button.getValue() == 1);
			button.layerColor = this.ai.aiDisabled ? 0xFFF02020 : 0xFF20F020;
			this.initGui();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(3) != null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.attack.range").getFormattedText());
		} else if (this.getTextField(4) != null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.hurt.resistant.time").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			ITextComponent mess = new TextComponentTranslation("ai.hover.if.see")
					.appendSibling(new TextComponentTranslation("ai.hover.if.see." + this.getButton(0).getValue()));
			if (this.ai.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
			this.setHoverText(mess.getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.door").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.set.walking").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.non.water").getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.back.home").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.water").getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.found.refuge").getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.found.target").getFormattedText());
		} else if (this.getButton(15) != null && this.getButton(15).isMouseOver()) {
			ITextComponent mess = new TextComponentTranslation("ai.hover.jump");
			if (this.ai.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
			this.setHoverText(mess.getFormattedText());
		} else if (this.getButton(16) != null && this.getButton(16).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("ai.hover.run", "" + (int) ((double) this.npc.stats.aggroRange / 3.0d))
							.getFormattedText());
		} else if (this.getButton(17) != null && this.getButton(17).isMouseOver()) {
			ITextComponent mess = new TextComponentTranslation("ai.hover.attack.type", this.getButton(17).displayString).appendSibling(new TextComponentTranslation("ai.hover.attack.type." + this.ai.tacticalVariant));
			if (this.ai.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
			this.setHoverText(mess.getFormattedText());
		} else if (this.getButton(23) != null && this.getButton(23).isMouseOver()) {
			ITextComponent mess = new TextComponentTranslation("ai.hover.stealth");
			if (this.ai.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
			this.setHoverText(mess.getFormattedText());
		} else if (this.getButton(25) != null && this.getButton(25).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("ai.hover.disabled").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int lId = 0;
		int x = this.guiLeft + 7;
		int y = this.guiTop + 10;
		this.addLabel(new GuiNpcLabel(lId++, "ai.enemyresponse", x, y + 7));
		this.addButton(new GuiNpcButton(0, x + 111, y, 60, 20, new String[] { "gui.retaliate", "gui.panic", "gui.retreat", "gui.nothing" }, this.npc.ais.onAttack));
		this.getButton(0).setEnabled(!this.ai.aiDisabled);
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.door", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(1, x + 111, y, 60, 20, new String[] { "gui.break", "gui.open", "gui.disabled" }, this.npc.ais.doorInteract));
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.swim", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(7, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (this.npc.ais.canSwim ? 1 : 0)));
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.shelter", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(9, x + 111, y, 60, 20, new String[] { "gui.darkness", "gui.sunlight", "gui.disabled" }, this.npc.ais.findShelter));
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.clearlos", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(10, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (this.npc.ais.directLOS ? 1 : 0)));
		
		this.addLabel(new GuiNpcLabel(lId++, "stats.attackInvisible", x, (y += 25) + 7));
		this.addButton(new GuiNpcButtonYesNo(23, x + 111, y, 60, 20, this.ai.attackInvisible));
		this.getButton(23).setEnabled(!this.ai.aiDisabled);
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.movement", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(2, x + 111, y, 60, 20, "selectServer.edit"));
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.disabled", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(25, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (this.ai.aiDisabled ? 1 : 0)));
		this.getButton(25).layerColor = this.ai.aiDisabled ? 0xFFF02020 : 0xFF20F020;
		
		x = this.guiLeft + 190;
		y = this.guiTop + 10;
		this.addLabel(new GuiNpcLabel(lId++, "ai.avoidwater", x, y + 7));
		this.addButton(new GuiNpcButton(5, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (this.ai.avoidsWater ? 1 : 0)));
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.return", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(6, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (this.ai.returnToStart ? 1 : 0)));
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.leapattarget", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(15, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (this.ai.canLeap ? 1 : 0)));
		
		this.addLabel(new GuiNpcLabel(lId++, "ai.cansprint", x, (y += 25) + 7));
		this.addButton(new GuiNpcButton(16, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (this.ai.canSprint ? 1 : 0)));

		this.addLabel(new GuiNpcLabel(lId++, "ai.hurtresistanttime", x, (y += 25) + 7));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, x + 112, y + 1, 58, 18, (this.ai.getMaxHurtResistantTime() / 2) + ""));
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(0, 100, this.ai.getMaxHurtResistantTime() / 2);

		this.addLabel(new GuiNpcLabel(lId++, "ai.tacticalvariant", x, (y += 50) + 7));
		this.addButton(new GuiNpcButton(17, x + 111, y, 60, 20, this.tactics, this.ai.tacticalVariant));
		
		if (this.ai.tacticalVariant != 0 && this.ai.tacticalVariant != 6) {
			String label;
			switch (this.ai.tacticalVariant) {
			case 1:
				label = "gui.dodgedistance";
				break;
			case 2:
				label = "gui.orbitdistance";
				break;
			case 3:
				label = "gui.fightifthisclose";
				break;
			case 4:
				label = "gui.searchdistance";
				break;
			case 5:
				label = "gui.proximity";
				break;
			default:
				label = "gui.engagedistance";
				break;
			}
			this.addLabel(new GuiNpcLabel(lId, label, x, (y += 25) + 7));
			this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, x + 112, y + 1, 58, 18,
					this.ai.getTacticalRange() + ""));
			this.getTextField(3).setNumbersOnly();
			this.getTextField(3).setMinMaxDefault(1, this.npc.stats.aggroRange, 5);
		}
		this.getButton(15).setEnabled(!this.ai.aiDisabled && this.ai.onAttack == 0);
		this.getButton(17).setEnabled(!this.ai.aiDisabled && this.ai.onAttack == 0);
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAISave, this.ai.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.ai.readToNBT(compound);
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 3) {
			this.ai.setTacticalRange(textfield.getInteger());
		} else if (textfield.getId() == 4) {
			this.ai.setMaxHurtResistantTime(textfield.getInteger() * 2);
			if (textfield.getInteger() * 2 != this.ai.getMaxHurtResistantTime()) {
				textfield.setText("" + this.ai.getMaxHurtResistantTime());
			}
		}
	}

}
