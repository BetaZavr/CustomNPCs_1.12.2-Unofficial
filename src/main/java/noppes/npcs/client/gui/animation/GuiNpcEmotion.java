package noppes.npcs.client.gui.animation;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.DataAnimation;

public class GuiNpcEmotion
extends GuiNPCInterface {

	private DataAnimation animation;
	
	public GuiNpcEmotion(EntityCustomNpc npc) {
		super(npc);
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
	public void buttonEvent(GuiNpcButton button) {
		
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.AnimationSave, this.animation.save(new NBTTagCompound()));
	}
	
}
