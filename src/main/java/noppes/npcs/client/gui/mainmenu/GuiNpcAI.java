package noppes.npcs.client.gui.mainmenu;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcMovement;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAI;

import java.awt.*;

public class GuiNpcAI
extends GuiNPCInterface2
implements ITextfieldListener, IGuiData {

	private final DataAI ai;
	private final String[] tactics = new String[] { "aitactics.rush", "aitactics.stagger", "aitactics.orbit", "aitactics.hitandrun", "aitactics.commander", "aitactics.stalk", "gui.none" };

	public GuiNpcAI(EntityNPCInterface npc) {
		super(npc, 6);
		ai = npc.ais;
		Client.sendData(EnumPacketServer.MainmenuAIGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: {
				ai.onAttack = button.getValue();
				initGui();
				break;
			}
			case 1: ai.doorInteract = button.getValue(); break;
			case 2: setSubGui(new SubGuiNpcMovement(ai)); break;
			case 5: npc.ais.setAvoidsWater(button.getValue() == 1); break;
			case 6: ai.returnToStart = (button.getValue() == 1); break;
			case 7: ai.canSwim = (button.getValue() == 1); break;
			case 9: ai.findShelter = button.getValue(); break;
			case 10: ai.directLOS = (button.getValue() == 1); break;
			case 15: ai.canLeap = (button.getValue() == 1); break;
			case 16: ai.canSprint = (button.getValue() == 1); break;
			case 17: {
				ai.tacticalVariant = button.getValue();
				ai.directLOS = (button.getValue() != 5 && ai.directLOS);
				initGui();
				break;
			}
			case 18: ai.canBeCollide = (button.getValue() == 1); break;
			case 23: ai.attackInvisible = ((GuiNpcButtonYesNo) button).getBoolean(); break;
			case 25: {
				ai.aiDisabled = (button.getValue() == 1);
				button.setLayerColor(ai.aiDisabled ?
						new Color(0xFFF02020).getRGB() :
						new Color(0xFF20F020).getRGB());
				initGui();
				break;
			}
			case 66: close(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int lId = 0;
		int x = guiLeft + 7;
		int y = guiTop + 10;
		addLabel(new GuiNpcLabel(lId++, "ai.enemyresponse", x, y + 7));
		GuiNpcButton button = new GuiNpcButton(0, x + 111, y, 60, 20, new String[] { "gui.retaliate", "gui.panic", "gui.retreat", "gui.nothing" }, npc.ais.onAttack);
		button.setEnabled(!ai.aiDisabled);
		ITextComponent mess = new TextComponentTranslation("ai.hover.if.see").appendSibling(new TextComponentTranslation("ai.hover.if.see." + npc.ais.onAttack));
		if (ai.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		button.setHoverText(mess.getFormattedText());
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.door", x, (y += 25) + 7));
		button = new GuiNpcButton(1, x + 111, y, 60, 20, new String[] { "gui.break", "gui.open", "gui.disabled" }, npc.ais.doorInteract);
		button.setHoverText("ai.hover.door");
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.swim", x, (y += 25) + 7));
		button = new GuiNpcButton(7, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (npc.ais.canSwim ? 1 : 0));
		button.setHoverText("ai.hover.water");
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.shelter", x, (y += 25) + 7));
		button = new GuiNpcButton(9, x + 111, y, 60, 20, new String[] { "gui.darkness", "gui.sunlight", "gui.disabled" }, npc.ais.findShelter);
		button.setHoverText("ai.hover.found.refuge");
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.clearlos", x, (y += 25) + 7));
		button = new GuiNpcButton(10, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (npc.ais.directLOS ? 1 : 0));
		button.setHoverText("ai.hover.found.target");
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "stats.attackInvisible", x, (y += 25) + 7));
		button = new GuiNpcButtonYesNo(23, x + 111, y, 60, 20, ai.attackInvisible);
		button.setEnabled(!ai.aiDisabled);
		mess = new TextComponentTranslation("ai.hover.stealth");
		if (ai.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		button.setHoverText(mess.getFormattedText());
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.movement", x, (y += 25) + 7));
		button = new GuiNpcButton(2, x + 111, y, 60, 20, "selectServer.edit");
		button.setHoverText("ai.hover.set.walking");
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.disabled", x, (y += 25) + 7));
		button = new GuiNpcButton(25, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (ai.aiDisabled ? 1 : 0));
		button.layerColor = ai.aiDisabled ?
				new Color(0xFFF02020).getRGB() :
				new Color(0xFF20F020).getRGB();
		button.setHoverText("ai.hover.disabled");
		addButton(button);
		
		x = guiLeft + 190;
		y = guiTop + 10;
		addLabel(new GuiNpcLabel(lId++, "ai.avoidwater", x, y + 7));
		button = new GuiNpcButton(5, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (ai.avoidsWater ? 1 : 0));
		button.setHoverText("ai.hover.non.water");
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.return", x, (y += 25) + 7));
		button = new GuiNpcButton(6, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (ai.returnToStart ? 1 : 0));
		button.setHoverText("ai.hover.back.home");
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.leapattarget", x, (y += 25) + 7));
		button = new GuiNpcButton(15, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (ai.canLeap ? 1 : 0));
		button.setEnabled(!ai.aiDisabled && ai.onAttack == 0);
		mess = new TextComponentTranslation("ai.hover.jump");
		if (ai.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		button.setHoverText(mess.getFormattedText());
		addButton(button);
		
		addLabel(new GuiNpcLabel(lId++, "ai.cansprint", x, (y += 25) + 7));
		button = new GuiNpcButton(16, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (ai.canSprint ? 1 : 0));
		button.setHoverText("ai.hover.run", "" + (int) ((double) npc.stats.aggroRange / 3.0d));
		addButton(button);

		addLabel(new GuiNpcLabel(lId++, "ai.hurt.resistant.time", x, (y += 25) + 7));
		GuiNpcTextField textField = new GuiNpcTextField(4, this, fontRenderer, x + 112, y + 1, 58, 18, (ai.getMaxHurtResistantTime() / 2) + "");
		textField.setMinMaxDefault(0, 100, ai.getMaxHurtResistantTime() / 2);
		textField.setHoverText("ai.hover.hurt.resistant.time");
		addTextField(textField);

		addLabel(new GuiNpcLabel(lId++, "ai.can.be.collide", x, (y += 25) + 7));
		button = new GuiNpcButton(18, x + 111, y, 60, 20, new String[] { "gui.no", "gui.yes" }, (ai.canBeCollide ? 1 : 0));
		button.setHoverText("ai.hover.can.be.collide");
		addButton(button);

		addLabel(new GuiNpcLabel(lId++, "ai.tacticalvariant", x, (y += 25) + 7));
		button = new GuiNpcButton(17, x + 111, y, 60, 20, tactics, ai.tacticalVariant);
		button.setEnabled(!ai.aiDisabled && ai.onAttack == 0);
		mess = new TextComponentTranslation("ai.hover.attack.type", button.displayString).appendSibling(new TextComponentTranslation("ai.hover.attack.type." + ai.tacticalVariant));
		if (ai.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		button.setHoverText(mess.getFormattedText());
		addButton(button);
		
		if (ai.tacticalVariant != 0 && ai.tacticalVariant != 6) {
			String label;
			switch (ai.tacticalVariant) {
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
			addLabel(new GuiNpcLabel(lId, label, x, (y += 25) + 7));
			textField = new GuiNpcTextField(3, this, fontRenderer, x + 112, y + 1, 58, 18, ai.getTacticalRange() + "");
			textField.setMinMaxDefault(1, npc.stats.aggroRange, 5);
			textField.setHoverText("ai.hover.attack.range");
			addTextField(textField);
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAISave, ai.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		ai.readToNBT(compound);
		initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getID() == 3) {
			ai.setTacticalRange(textfield.getInteger());
		}
		else if (textfield.getID() == 4) {
			ai.setMaxHurtResistantTime(textfield.getInteger() * 2);
			if (textfield.getInteger() * 2 != ai.getMaxHurtResistantTime()) {
				textfield.setText("" + ai.getMaxHurtResistantTime());
			}
		}
	}

}
