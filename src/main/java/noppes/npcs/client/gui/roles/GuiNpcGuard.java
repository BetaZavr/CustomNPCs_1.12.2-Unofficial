package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;

public class GuiNpcGuard extends GuiNPCInterface2 {

	private final JobGuard role;
	private GuiCustomScroll scroll1;
	private GuiCustomScroll scroll2;
	private final Map<String, String> data = new HashMap<>();

	public GuiNpcGuard(EntityNPCInterface npc) {
		super(npc);
		this.role = (JobGuard) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityAnimal.class.isAssignableFrom(cl) && !this.role.targets.contains(name)) {
					this.role.targets.add(name);
				}
			}
			this.scroll1.selected = -1;
			this.scroll2.selected = -1;
			this.initGui();
		}
		if (button.id == 1) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityMob.class.isAssignableFrom(cl) && !EntityCreeper.class.isAssignableFrom(cl)
						&& !this.role.targets.contains(name)) {
					this.role.targets.add(name);
				}
			}
			this.scroll1.selected = -1;
			this.scroll2.selected = -1;
			this.initGui();
		}
		if (button.id == 2) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityCreeper.class.isAssignableFrom(cl) && !this.role.targets.contains(name)) {
					this.role.targets.add(name);
				}
			}
			this.scroll1.selected = -1;
			this.scroll2.selected = -1;
			this.initGui();
		}
		if (button.id == 11 && this.scroll1.hasSelected()) {
			this.role.targets.add(this.data.get(this.scroll1.getSelected()));
			this.scroll1.selected = -1;
			this.scroll2.selected = -1;
			this.initGui();
		}
		if (button.id == 12 && this.scroll2.hasSelected()) {
			this.role.targets.remove(this.data.get(this.scroll2.getSelected()));
			this.scroll2.selected = -1;
			this.initGui();
		}
		if (button.id == 13) {
			role.targets.clear();
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityLivingBase.class.isAssignableFrom(cl) && !EntityNPCInterface.class.isAssignableFrom(cl)) {
					role.targets.add(name);
				}
			}
			scroll1.selected = -1;
			scroll2.selected = -1;
			initGui();
		}
		if (button.id == 14) {
			role.targets.clear();
			scroll1.selected = -1;
			scroll2.selected = -1;
			initGui();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(0, this.guiLeft + 10, this.guiTop + 4, 100, 20, "guard.animals"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 140, this.guiTop + 4, 100, 20, "guard.mobs"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 275, this.guiTop + 4, 100, 20, "guard.creepers"));
		if (this.scroll1 == null) {
			(this.scroll1 = new GuiCustomScroll(this, 0)).setSize(175, 154);
		}
		this.scroll1.guiLeft = this.guiLeft + 4;
		this.scroll1.guiTop = this.guiTop + 58;
		this.addScroll(this.scroll1);
		this.addLabel(new GuiNpcLabel(11, "guard.availableTargets", this.guiLeft + 4, this.guiTop + 48));
		if (this.scroll2 == null) {
			(this.scroll2 = new GuiCustomScroll(this, 1)).setSize(175, 154);
		}
		this.scroll2.guiLeft = this.guiLeft + 235;
		this.scroll2.guiTop = this.guiTop + 58;
		this.addScroll(this.scroll2);
		this.addLabel(new GuiNpcLabel(12, "guard.currentTargets", this.guiLeft + 235, this.guiTop + 48));
		List<String> all = new ArrayList<>();
		this.data.clear();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> cl = ent.getEntityClass();
			String name = "entity." + ent.getName() + ".name";
			if (!this.role.targets.contains(name)) {
				if (EntityNPCInterface.class.isAssignableFrom(cl)) {
					continue;
				}
				if (!EntityLivingBase.class.isAssignableFrom(cl)) {
					continue;
				}
				String key = new TextComponentTranslation(name).getFormattedText();
				all.add(key);
				this.data.put(key, name);
			}
		}
		this.scroll1.setList(all);
		List<String> targets = new ArrayList<>();
		for (String name : this.role.targets) {
			String key = new TextComponentTranslation(name).getFormattedText();
			targets.add(key);
			this.data.put(key, name);
		}
		this.scroll2.setList(targets);
		this.addButton(new GuiNpcButton(11, this.guiLeft + 180, this.guiTop + 80, 55, 20, ">"));
		this.addButton(new GuiNpcButton(12, this.guiLeft + 180, this.guiTop + 102, 55, 20, "<"));
		this.addButton(new GuiNpcButton(13, this.guiLeft + 180, this.guiTop + 130, 55, 20, ">>"));
		this.addButton(new GuiNpcButton(14, this.guiLeft + 180, this.guiTop + 152, 55, 20, "<<"));
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.JobSave, this.role.writeToNBT(new NBTTagCompound()));
	}
}
