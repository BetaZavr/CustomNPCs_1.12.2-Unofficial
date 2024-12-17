package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionTalents;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

public class GuiNpcCompanion
extends GuiNPCInterface2
implements ITextfieldListener, ISliderListener {

	private final List<GuiNpcCompanionTalents.GuiTalent> talents = new ArrayList<>();
	private final RoleCompanion role;

	public GuiNpcCompanion(EntityNPCInterface npc) {
		super(npc);
		role = (RoleCompanion) npc.advanced.roleInterface;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			role.matureTo(EnumCompanionStage.values()[button.getValue()]);
			if (role.canAge) {
				role.ticksActive = role.stage.matureAge;
			}
			initGui();
		}
		if (button.id == 1) {
			Client.sendData(EnumPacketServer.RoleCompanionUpdate, role.stage.ordinal());
		}
		if (button.id == 2) {
			role.canAge = (button.getValue() == 1);
			initGui();
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		for (GuiNpcCompanionTalents.GuiTalent talent : new ArrayList<>(talents)) {
			talent.drawScreen(i, j, f);
		}
	}

	@Override
	public void elementClicked() {
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 4;
		addButton(new GuiNpcButton(0, guiLeft + 70, y, 90, 20, new String[] { EnumCompanionStage.BABY.name, EnumCompanionStage.CHILD.name, EnumCompanionStage.TEEN.name, EnumCompanionStage.ADULT.name, EnumCompanionStage.FULL_GROWN.name }, role.stage.ordinal()));
		addLabel(new GuiNpcLabel(0, "companion.stage", guiLeft + 4, y + 5));
		addButton(new GuiNpcButton(1, guiLeft + 162, y, 90, 20, "gui.update"));

		y += 22;
		addButton(new GuiNpcButton(2, guiLeft + 70, y, 90, 20, new String[] { "gui.no", "gui.yes" }, (role.canAge ? 1 : 0)));
		addLabel(new GuiNpcLabel(2, "companion.age", guiLeft + 4, y + 5));
		if (role.canAge) {
			GuiNpcTextField textField = new GuiNpcTextField(2, this, guiLeft + 162, y, 140, 20, role.ticksActive + "");
			textField.setMinMaxDefault(0, Integer.MAX_VALUE, 0);
			addTextField(textField);
		}

		EnumCompanionTalent inventory = EnumCompanionTalent.INVENTORY;
		talents.clear();

		y += 26;
		talents.add(new GuiNpcCompanionTalents.GuiTalent(role, inventory, guiLeft + 4, y));
		addSlider(new GuiNpcSlider(this, 10, guiLeft + 30, y + 2, 100, 20, role.getExp(EnumCompanionTalent.INVENTORY) / 5000.0f));

		EnumCompanionTalent armor = EnumCompanionTalent.ARMOR;
		y += 26;
		talents.add(new GuiNpcCompanionTalents.GuiTalent(role, armor, guiLeft + 4, y));
		addSlider(new GuiNpcSlider(this, 11, guiLeft + 30, y + 2, 100, 20, role.getExp(EnumCompanionTalent.ARMOR) / 5000.0f));

		EnumCompanionTalent sword = EnumCompanionTalent.SWORD;
		y += 26;
		talents.add(new GuiNpcCompanionTalents.GuiTalent(role, sword, guiLeft + 4, y));
		addSlider(new GuiNpcSlider(this, 12, guiLeft + 30, y + 2, 100, 20, role.getExp(EnumCompanionTalent.SWORD) / 5000.0f));

		for (GuiNpcCompanionTalents.GuiTalent gui : talents) { gui.setWorldAndResolution(mc, width, height); }
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		if (slider.sliderValue <= 0.0f) {
			slider.setString("gui.disabled");
			role.talents.remove(EnumCompanionTalent.values()[slider.id - 10]);
		} else {
			slider.displayString = (int) Math.floor(slider.sliderValue * 5000.0f) + "/5000 exp";
			role.setExp(EnumCompanionTalent.values()[slider.id - 10], (int) (slider.sliderValue * 5000.0f));
		}
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.RoleSave, role.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 2) {
			role.ticksActive = textfield.getInteger();
		}
	}
}
