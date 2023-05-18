package noppes.npcs.client.gui.animation;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.DataAnimation;

public class GuiNpcEmotion
extends GuiNPCInterface {

	private GuiScreen parent;
	private DataAnimation animation;
	
	public GuiNpcEmotion(GuiScreen parent, EntityCustomNpc npc) {
		super(npc);
		this.parent = parent;
		this.setBackground("smallbg.png");
		this.closeOnEsc = true;
		this.ySize = 240;
		this.xSize = 427;
		
		this.animation = npc.animation;
	}
	
	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	public void buttonEvent(GuiButton button) {
		
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.AnimationSave, this.animation.writeToNBT(new NBTTagCompound()));
		this.mc.displayGuiScreen(this.parent);
	}
	
}
