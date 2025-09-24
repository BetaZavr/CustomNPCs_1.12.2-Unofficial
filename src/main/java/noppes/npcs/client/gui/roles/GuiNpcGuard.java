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
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;

import javax.annotation.Nonnull;

public class GuiNpcGuard extends GuiNPCInterface2 implements ICustomScrollListener {

	protected final JobGuard role;
	protected GuiCustomScroll scrollAllEntities;
	protected GuiCustomScroll scrollTargetEntities;
	protected final Map<String, String> data = new HashMap<>();

	public GuiNpcGuard(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

		role = (JobGuard) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
					Class<? extends Entity> cl = ent.getEntityClass();
					String name = "entity." + ent.getName() + ".name";
					if (EntityAnimal.class.isAssignableFrom(cl) && !role.targets.contains(name)) { role.targets.add(name); }
				}
				scrollAllEntities.setSelect(-1);
				scrollTargetEntities.setSelect(-1);
				initGui();
				break;
			}
			case 1: {
				for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
					Class<? extends Entity> cl = ent.getEntityClass();
					String name = "entity." + ent.getName() + ".name";
					if (EntityMob.class.isAssignableFrom(cl) && !EntityCreeper.class.isAssignableFrom(cl)  && !role.targets.contains(name)) { role.targets.add(name); }
				}
				scrollAllEntities.setSelect(-1);
				scrollTargetEntities.setSelect(-1);
				initGui();
				break;
			}
			case 2: {
				for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
					Class<? extends Entity> cl = ent.getEntityClass();
					String name = "entity." + ent.getName() + ".name";
					if (EntityCreeper.class.isAssignableFrom(cl) && !role.targets.contains(name)) {
						role.targets.add(name);
					}
				}
				scrollAllEntities.setSelect(-1);
				scrollTargetEntities.setSelect(-1);
				initGui();
				break;
			}
			case 11: {
				if (!scrollAllEntities.hasSelected()) { return; }
				role.targets.add(data.get(scrollAllEntities.getSelected()));
				scrollAllEntities.setSelect(-1);
				scrollTargetEntities.setSelect(-1);
				initGui();
				break;
			} // >
			case 12: {
				if (!scrollTargetEntities.hasSelected()) { return; }
				role.targets.remove(data.get(scrollTargetEntities.getSelected()));
				scrollTargetEntities.setSelect(-1);
				initGui();
				break;
			} // <
			case 13: {
				role.targets.clear();
				for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
					Class<? extends Entity> cl = ent.getEntityClass();
					String name = "entity." + ent.getName() + ".name";
					if (EntityLivingBase.class.isAssignableFrom(cl) && !EntityNPCInterface.class.isAssignableFrom(cl)) {
						role.targets.add(name);
					}
				}
				scrollAllEntities.setSelect(-1);
				scrollTargetEntities.setSelect(-1);
				initGui();
				break;
			} // >>
			case 14: {
				role.targets.clear();
				scrollAllEntities.setSelect(-1);
				scrollTargetEntities.setSelect(-1);
				initGui();
				break;
			} // <<
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addButton(new GuiNpcButton(0, guiLeft + 10, guiTop + 4, 100, 20, "guard.animals"));
		addButton(new GuiNpcButton(1, guiLeft + 140, guiTop + 4, 100, 20, "guard.mobs"));
		addButton(new GuiNpcButton(2, guiLeft + 275, guiTop + 4, 100, 20, "guard.creepers"));
		if (scrollAllEntities == null) { scrollAllEntities = new GuiCustomScroll(this, 0).setSize(175, 154); }
		scrollAllEntities.guiLeft = guiLeft + 4;
		scrollAllEntities.guiTop = guiTop + 58;
		addScroll(scrollAllEntities);
		addLabel(new GuiNpcLabel(11, "guard.availableTargets", guiLeft + 4, guiTop + 48));
		if (scrollTargetEntities == null) { scrollTargetEntities = new GuiCustomScroll(this, 1).setSize(175, 154); }
		scrollTargetEntities.guiLeft = guiLeft + 235;
		scrollTargetEntities.guiTop = guiTop + 58;
		addScroll(scrollTargetEntities);
		addLabel(new GuiNpcLabel(12, "guard.currentTargets", guiLeft + 235, guiTop + 48));
		List<String> all = new ArrayList<>();
		data.clear();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> cl = ent.getEntityClass();
			String name = "entity." + ent.getName() + ".name";
			if (!role.targets.contains(name)) {
				if (EntityNPCInterface.class.isAssignableFrom(cl) || !EntityLivingBase.class.isAssignableFrom(cl)) { continue; }
				String key = new TextComponentTranslation(name).getFormattedText();
				all.add(key);
				data.put(key, name);
			}
		}
		scrollAllEntities.setList(all);
		List<String> targets = new ArrayList<>();
		for (String name : role.targets) {
			String key = new TextComponentTranslation(name).getFormattedText();
			targets.add(key);
			data.put(key, name);
		}
		scrollTargetEntities.setList(targets);
		addButton(new GuiNpcButton(11, guiLeft + 180, guiTop + 80, 55, 20, ">")
				.setHoverText("beacon.hover.add.element"));
		addButton(new GuiNpcButton(12, guiLeft + 180, guiTop + 102, 55, 20, "<")
				.setHoverText("beacon.hover.del.element"));
		addButton(new GuiNpcButton(13, guiLeft + 180, guiTop + 130, 55, 20, ">>")
				.setHoverText("beacon.hover.add.all.element"));
		addButton(new GuiNpcButton(14, guiLeft + 180, guiTop + 152, 55, 20, "<<")
				.setHoverText("hover.del.all.element"));
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.JobSave, role.save(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			if (!scrollAllEntities.hasSelected()) { return; }
			role.targets.add(data.get(scrollAllEntities.getSelected()));
			scrollAllEntities.setSelect(-1);
			scrollTargetEntities.setSelect(-1);
		} // >
		if (scroll.getID() == 0) {
			if (!scrollTargetEntities.hasSelected()) { return; }
			role.targets.remove(data.get(scrollTargetEntities.getSelected()));
			scrollTargetEntities.setSelect(-1);
		} // <
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
