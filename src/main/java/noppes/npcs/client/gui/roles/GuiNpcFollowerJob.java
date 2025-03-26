package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobFollower;

public class GuiNpcFollowerJob
extends GuiNPCInterface2
implements ICustomScrollListener {

	private final JobFollower job;

    public GuiNpcFollowerJob(EntityNPCInterface npc) {
		super(npc);
		job = (JobFollower) npc.advanced.jobInterface;
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(1, "gui.name", guiLeft + 6, guiTop + 110));
		addTextField(new GuiNpcTextField(1, this, fontRenderer, guiLeft + 50, guiTop + 105, 200, 20, job.name));
        GuiCustomScroll scroll;
        (scroll = new GuiCustomScroll(this, 0)).setSize(143, 208);
		scroll.guiLeft = guiLeft + 268;
		scroll.guiTop = guiTop + 4;
		addScroll(scroll);
		List<String> names = new ArrayList<>();
		List<EntityNPCInterface> list = npc.world.getEntitiesWithinAABB(EntityNPCInterface.class, npc.getEntityBoundingBox().grow(40.0, 40.0, 40.0));
		for (EntityNPCInterface npcEntity : list) {
			if (npcEntity.equals(npc) || names.contains(npcEntity.display.getName())) { continue; }
			names.add(npcEntity.display.getName());
		}
		scroll.setList(names);
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
		job.name = getTextField(1).getFullText();
		Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		getTextField(1).setFullText(scroll.getSelected());
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) { }

}
