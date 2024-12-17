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

public class GuiNpcGuard
extends GuiNPCInterface2 {

	private final JobGuard role;
	private GuiCustomScroll scroll1;
	private GuiCustomScroll scroll2;
	private final Map<String, String> data = new HashMap<>();

	public GuiNpcGuard(EntityNPCInterface npc) {
		super(npc);
		role = (JobGuard) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityAnimal.class.isAssignableFrom(cl) && !role.targets.contains(name)) { role.targets.add(name); }
			}
			scroll1.selected = -1;
			scroll2.selected = -1;
			initGui();
		}
		if (button.id == 1) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityMob.class.isAssignableFrom(cl) && !EntityCreeper.class.isAssignableFrom(cl)  && !role.targets.contains(name)) { role.targets.add(name); }
			}
			scroll1.selected = -1;
			scroll2.selected = -1;
			initGui();
		}
		if (button.id == 2) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityCreeper.class.isAssignableFrom(cl) && !role.targets.contains(name)) {
					role.targets.add(name);
				}
			}
			scroll1.selected = -1;
			scroll2.selected = -1;
			initGui();
		}
		if (button.id == 11 && scroll1.hasSelected()) {
			role.targets.add(data.get(scroll1.getSelected()));
			scroll1.selected = -1;
			scroll2.selected = -1;
			initGui();
		}
		if (button.id == 12 && scroll2.hasSelected()) {
			role.targets.remove(data.get(scroll2.getSelected()));
			scroll2.selected = -1;
			initGui();
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
		addButton(new GuiNpcButton(0, guiLeft + 10, guiTop + 4, 100, 20, "guard.animals"));
		addButton(new GuiNpcButton(1, guiLeft + 140, guiTop + 4, 100, 20, "guard.mobs"));
		addButton(new GuiNpcButton(2, guiLeft + 275, guiTop + 4, 100, 20, "guard.creepers"));
		if (scroll1 == null) {
			(scroll1 = new GuiCustomScroll(this, 0)).setSize(175, 154);
		}
		scroll1.guiLeft = guiLeft + 4;
		scroll1.guiTop = guiTop + 58;
		addScroll(scroll1);
		addLabel(new GuiNpcLabel(11, "guard.availableTargets", guiLeft + 4, guiTop + 48));
		if (scroll2 == null) {
			(scroll2 = new GuiCustomScroll(this, 1)).setSize(175, 154);
		}
		scroll2.guiLeft = guiLeft + 235;
		scroll2.guiTop = guiTop + 58;
		addScroll(scroll2);
		addLabel(new GuiNpcLabel(12, "guard.currentTargets", guiLeft + 235, guiTop + 48));
		List<String> all = new ArrayList<>();
		data.clear();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> cl = ent.getEntityClass();
			String name = "entity." + ent.getName() + ".name";
			if (!role.targets.contains(name)) {
				if (EntityNPCInterface.class.isAssignableFrom(cl)) {
					continue;
				}
				if (!EntityLivingBase.class.isAssignableFrom(cl)) {
					continue;
				}
				String key = new TextComponentTranslation(name).getFormattedText();
				all.add(key);
				data.put(key, name);
			}
		}
		scroll1.setList(all);
		List<String> targets = new ArrayList<>();
		for (String name : role.targets) {
			String key = new TextComponentTranslation(name).getFormattedText();
			targets.add(key);
			data.put(key, name);
		}
		scroll2.setList(targets);
		addButton(new GuiNpcButton(11, guiLeft + 180, guiTop + 80, 55, 20, ">"));
		addButton(new GuiNpcButton(12, guiLeft + 180, guiTop + 102, 55, 20, "<"));
		addButton(new GuiNpcButton(13, guiLeft + 180, guiTop + 130, 55, 20, ">>"));
		addButton(new GuiNpcButton(14, guiLeft + 180, guiTop + 152, 55, 20, "<<"));
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
	public void save() {
		Client.sendData(EnumPacketServer.JobSave, role.writeToNBT(new NBTTagCompound()));
	}

}
