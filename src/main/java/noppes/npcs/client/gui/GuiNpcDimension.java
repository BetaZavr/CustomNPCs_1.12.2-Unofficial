package noppes.npcs.client.gui;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcDimension extends GuiNPCInterface implements IScrollData {
	private HashMap<String, Integer> data;
	private GuiCustomScroll scroll;

	public GuiNpcDimension() {
		this.data = new HashMap<String, Integer>();
		this.xSize = 256;
		this.setBackground("menubg.png");
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		if (!this.data.containsKey(this.scroll.getSelected())) {
			return;
		}
		if (id == 4) {
			Client.sendData(EnumPacketServer.DimensionTeleport, this.data.get(this.scroll.getSelected()));
			this.close();
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			Client.sendData(EnumPacketServer.RemoteDelete, this.data.get(this.scroll.getSelected()));
		}
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(165, 208);
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 4;
		this.addScroll(this.scroll);
		String title = new TextComponentTranslation("Dimensions").getFormattedText();
		int x = (this.xSize - this.fontRenderer.getStringWidth(title)) / 2;
		this.addLabel(new GuiNpcLabel(0, title, this.guiLeft + x, this.guiTop - 8));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 170, this.guiTop + 72, 82, 20, "remote.tp"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.DimensionsGet, new Object[0]);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		this.scroll.mouseClicked(i, j, k);
	}

	@Override
	public void save() {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.scroll.setList(list);
		this.data = data;
	}

	@Override
	public void setSelected(String selected) {
		this.getButton(3).setDisplayText(selected);
	}
}
