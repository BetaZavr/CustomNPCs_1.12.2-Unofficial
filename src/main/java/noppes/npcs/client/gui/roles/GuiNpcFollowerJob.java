package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobFollower;

public class GuiNpcFollowerJob extends GuiNPCInterface2 implements ICustomScrollListener {

	protected final JobFollower job;

    public GuiNpcFollowerJob(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

		job = (JobFollower) npc.advanced.jobInterface;
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(1, "gui.name", guiLeft + 6, guiTop + 110));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 50, guiTop + 105, 200, 20, job.name));
        GuiCustomScroll scroll = new GuiCustomScroll(this, 0).setSize(143, 208);
		scroll.guiLeft = guiLeft + 268;
		scroll.guiTop = guiTop + 4;
		addScroll(scroll);
		List<String> names = new ArrayList<>();
		List<EntityNPCInterface> list = new ArrayList<>();
		try { list = npc.world.getEntitiesWithinAABB(EntityNPCInterface.class, npc.getEntityBoundingBox().grow(40.0, 40.0, 40.0)); } catch (Exception ignored) { }
		for (EntityNPCInterface npcEntity : list) {
			if (npcEntity.equals(npc) || names.contains(npcEntity.display.getName())) { continue; }
			names.add(npcEntity.display.getName());
		}
		scroll.setList(names);
	}

	@Override
	public void save() {
		job.name = getTextField(1).getText();
		Client.sendData(EnumPacketServer.JobSave, job.save(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		getTextField(1).setText(scroll.getSelected());
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

}
