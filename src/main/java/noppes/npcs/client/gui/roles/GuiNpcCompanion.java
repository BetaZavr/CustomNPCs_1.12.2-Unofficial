package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
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

public class GuiNpcCompanion extends GuiNPCInterface2 implements ITextfieldListener, ISliderListener {
	private RoleCompanion role;
	private List<GuiNpcCompanionTalents.GuiTalent> talents;

	public GuiNpcCompanion(EntityNPCInterface npc) {
		super(npc);
		this.talents = new ArrayList<GuiNpcCompanionTalents.GuiTalent>();
		this.role = (RoleCompanion) npc.roleInterface;
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		if (guibutton.id == 0) {
			GuiNpcButton button = (GuiNpcButton) guibutton;
			this.role.matureTo(EnumCompanionStage.values()[button.getValue()]);
			if (this.role.canAge) {
				this.role.ticksActive = this.role.stage.matureAge;
			}
			this.initGui();
		}
		if (guibutton.id == 1) {
			Client.sendData(EnumPacketServer.RoleCompanionUpdate, this.role.stage.ordinal());
		}
		if (guibutton.id == 2) {
			GuiNpcButton button = (GuiNpcButton) guibutton;
			this.role.canAge = (button.getValue() == 1);
			this.initGui();
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		for (GuiNpcCompanionTalents.GuiTalent talent : new ArrayList<GuiNpcCompanionTalents.GuiTalent>(this.talents)) {
			talent.drawScreen(i, j, f);
		}
	}

	@Override
	public void elementClicked() {
	}

	@Override
	public void initGui() {
		super.initGui();
		this.talents = new ArrayList<GuiNpcCompanionTalents.GuiTalent>();
		int y = this.guiTop + 4;
		this.addButton(new GuiNpcButton(0, this.guiLeft + 70, y, 90, 20,
				new String[] { EnumCompanionStage.BABY.name, EnumCompanionStage.CHILD.name,
						EnumCompanionStage.TEEN.name, EnumCompanionStage.ADULT.name,
						EnumCompanionStage.FULLGROWN.name },
				this.role.stage.ordinal()));
		this.addLabel(new GuiNpcLabel(0, "companion.stage", this.guiLeft + 4, y + 5));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 162, y, 90, 20, "gui.update"));
		int i = 2;
		int j = this.guiLeft + 70;
		y += 22;
		this.addButton(
				new GuiNpcButton(i, j, y, 90, 20, new String[] { "gui.no", "gui.yes" }, (this.role.canAge ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(2, "companion.age", this.guiLeft + 4, y + 5));
		if (this.role.canAge) {
			this.addTextField(new GuiNpcTextField(2, this, this.guiLeft + 162, y, 140, 20, this.role.ticksActive + ""));
			this.getTextField(2).numbersOnly = true;
			this.getTextField(2).setMinMaxDefault(0, Integer.MAX_VALUE, 0);
		}
		List<GuiNpcCompanionTalents.GuiTalent> talents = this.talents;
		RoleCompanion role = this.role;
		EnumCompanionTalent inventory = EnumCompanionTalent.INVENTORY;
		int x = this.guiLeft + 4;
		y += 26;
		talents.add(new GuiNpcCompanionTalents.GuiTalent(role, inventory, x, y));
		this.addSlider(new GuiNpcSlider(this, 10, this.guiLeft + 30, y + 2, 100, 20,
				this.role.getExp(EnumCompanionTalent.INVENTORY) / 5000.0f));
		List<GuiNpcCompanionTalents.GuiTalent> talents2 = this.talents;
		RoleCompanion role2 = this.role;
		EnumCompanionTalent armor = EnumCompanionTalent.ARMOR;
		int x2 = this.guiLeft + 4;
		y += 26;
		talents2.add(new GuiNpcCompanionTalents.GuiTalent(role2, armor, x2, y));
		this.addSlider(new GuiNpcSlider(this, 11, this.guiLeft + 30, y + 2, 100, 20,
				this.role.getExp(EnumCompanionTalent.ARMOR) / 5000.0f));
		List<GuiNpcCompanionTalents.GuiTalent> talents3 = this.talents;
		RoleCompanion role3 = this.role;
		EnumCompanionTalent sword = EnumCompanionTalent.SWORD;
		int x3 = this.guiLeft + 4;
		y += 26;
		talents3.add(new GuiNpcCompanionTalents.GuiTalent(role3, sword, x3, y));
		this.addSlider(new GuiNpcSlider(this, 12, this.guiLeft + 30, y + 2, 100, 20,
				this.role.getExp(EnumCompanionTalent.SWORD) / 5000.0f));
		for (GuiNpcCompanionTalents.GuiTalent gui : this.talents) {
			gui.setWorldAndResolution(this.mc, this.width, this.height);
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		if (slider.sliderValue <= 0.0f) {
			slider.setString("gui.disabled");
			this.role.talents.remove(EnumCompanionTalent.values()[slider.id - 10]);
		} else {
			slider.displayString = (slider.sliderValue * 50.0f) * 100 + " exp";
			this.role.setExp(EnumCompanionTalent.values()[slider.id - 10], (int) ((slider.sliderValue * 50.0f) * 100));
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
		Client.sendData(EnumPacketServer.RoleSave, this.role.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 2) {
			this.role.ticksActive = textfield.getInteger();
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
	}
}
