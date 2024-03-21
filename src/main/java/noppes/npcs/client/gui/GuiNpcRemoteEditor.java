package noppes.npcs.client.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;

public class GuiNpcRemoteEditor
extends GuiNPCInterface
implements IGuiData, GuiYesNoCallback, ICustomScrollListener {
	
	private HashMap<String, Integer> dataIDs;
	private GuiCustomScroll scroll;
	private String search = "";
	private List<String> list;
	public Entity selectEntity;
	private static boolean all = false;
	private DecimalFormat df = new DecimalFormat("#.#");
	private char chr = Character.toChars(0x00A7)[0];

	public GuiNpcRemoteEditor() {
		this.dataIDs = new HashMap<String, Integer>();
		this.xSize = 256;
		this.setBackground("menubg.png");
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: {
				if (!this.dataIDs.containsKey(this.scroll.getSelected())) { return; }
				Entity entity = this.mc.world.getEntityByID(this.dataIDs.get(scroll.getSelected()));
				if (entity instanceof EntityNPCInterface) {
					Client.sendData(EnumPacketServer.RemoteMainMenu, this.dataIDs.get(this.scroll.getSelected()));
					return;
				} else {
					if (entity==null) { return; }
					GuiNbtBook gui = new GuiNbtBook(0, 0, 0);
					NBTTagCompound data = new NBTTagCompound();
					entity.writeToNBTAtomically(data);
					NBTTagCompound compound = new NBTTagCompound();
					compound.setInteger("EntityId", entity.getEntityId());
					compound.setTag("Data", data);
					gui.setGuiData(compound);
					this.mc.displayGuiScreen(gui);
				}
				break;
			}
			case 1: { // remove entity
				if (!this.dataIDs.containsKey(this.scroll.getSelected())) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.scroll.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 2: {
				if (!this.dataIDs.containsKey(this.scroll.getSelected())) { return; }
				Client.sendData(EnumPacketServer.RemoteReset, this.dataIDs.get(this.scroll.getSelected()));
				Entity entity2 = this.player.world.getEntityByID(this.dataIDs.get(this.scroll.getSelected()));
				if (entity2 != null && entity2 instanceof EntityNPCInterface) {
					((EntityNPCInterface) entity2).reset();
				}
				break;
			}
			case 3: {
				Client.sendData(EnumPacketServer.RemoteFreeze);
				break;
			}
			case 4: { // tp
				if (!this.dataIDs.containsKey(this.scroll.getSelected())) { return; }
				Client.sendData(EnumPacketServer.RemoteTpToNpc, this.dataIDs.get(this.scroll.getSelected()));
				CustomNPCsScheduler.runTack(() -> {
					Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
				}, 250);
				break;
			}
			case 5: {
				for (int ids : this.dataIDs.values()) {
					Client.sendData(EnumPacketServer.RemoteReset, ids);
					Entity entity = this.player.world.getEntityByID(ids);
					if (entity != null && entity instanceof EntityNPCInterface) {
						((EntityNPCInterface) entity).reset();
					}
				}
				break;
			}
			case 6: {
				GuiNpcRemoteEditor.all = ((GuiNpcCheckBox) button).isSelected();
				Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
				break;
			}
			default: {
				
			}
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			Client.sendData(EnumPacketServer.RemoteDelete, this.dataIDs.get(this.scroll.getSelected()), GuiNpcRemoteEditor.all);
			this.selectEntity = null;
			Entity e = this.player.world.getEntityByID(this.dataIDs.get(this.scroll.getSelected()));
			if (e != null) { this.player.world.removeEntity(e); }
		}
		CustomNPCsScheduler.runTack(() -> {
			NoppesUtil.openGUI((EntityPlayer) this.player, this);
			Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
		}, 250);
		
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(165, 191);
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 21;
		this.addScroll(this.scroll);
		
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 4, 165, 15, this.search));
		
		String title = new TextComponentTranslation("remote.title").getFormattedText();
		int x = (this.xSize - this.fontRenderer.getStringWidth(title)) / 2;
		this.addLabel(new GuiNpcLabel(0, title, this.guiLeft + x, this.guiTop - 8));
		GuiNpcButton button = new GuiNpcButton(0, this.guiLeft + 170, this.guiTop + 4, 82, 20, "selectServer.edit");
		button.enabled = this.selectEntity != null && !(this.selectEntity instanceof EntityPlayer);
		this.addButton(button);
		button = new GuiNpcButton(1, this.guiLeft + 170, this.guiTop + 24, 82, 20, "selectWorld.deleteButton");
		button.enabled = this.selectEntity != null;
		this.addButton(button);
		button = new GuiNpcButton(2, this.guiLeft + 170, this.guiTop + 44, 82, 20, "remote.reset");
		button.enabled = this.selectEntity instanceof EntityNPCInterface;
		this.addButton(button);
		this.addButton(new GuiNpcButton(3, this.guiLeft + 170, this.guiTop + 192, 82, 20, CustomNpcs.FreezeNPCs ? "remote.unfreeze" : "remote.freeze"));
		button = new GuiNpcButton(4, this.guiLeft + 170, this.guiTop + 64, 82, 20, "remote.tp");
		button.enabled = this.selectEntity != null;
		this.addButton(button);
		this.addButton(new GuiNpcButton(5, this.guiLeft + 170, this.guiTop + 172, 82, 20, "remote.resetall"));
		
		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(6, this.guiLeft + 174, this.guiTop + 86, 20, 20, "");
		checkBox.setSelected(GuiNpcRemoteEditor.all);
		this.addButton(checkBox);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
		super.keyTyped(c, i);
		if (i==200 || i==208 || i==ClientProxy.frontButton.getKeyCode() || i==ClientProxy.backButton.getKeyCode()) {
			this.resetEntity();
		}
		if (this.search.equals(this.getTextField(1).getText())) { return; }
		this.search = this.getTextField(1).getText().toLowerCase();
		this.scroll.setList(this.getSearchList());
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
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList nbtlist = compound.getTagList("Data", 10);
		List<String> list = new ArrayList<String>();
		String[][] hs = new String[nbtlist.tagCount()][];
		this.dataIDs.clear();
		for (int i = 0; i < nbtlist.tagCount(); ++i) {
			NBTTagCompound nbt = nbtlist.getCompoundTagAt(i);
			Entity entity = this.mc.world.getEntityByID(nbt.getInteger("Id"));
			if (entity==null) { continue; }
			String type = chr+"7";
			if (entity instanceof EntityNPCInterface) { type = chr+"a"; }
			else if (entity instanceof EntityPlayer) { type = chr+"c"; }
			else if (entity instanceof EntityAnimal) { type = chr+"e"; }
			else if (entity instanceof EntityMob) { type = chr+"b"; }
			float distance = this.player.getDistance(entity);
			hs[i] = new String[] { chr+"7Distance Of: "+chr+"6"+this.df.format(distance) };
			String key = type+"ID:"+nbt.getInteger("Id")+" "+chr+"r"+(new TextComponentTranslation(entity.getName()).getFormattedText())+" "+chr+"7"+this.df.format(distance);
			list.add(key);
			this.dataIDs.put(key, nbt.getInteger("Id"));
		}
		this.list = list;
		this.scroll.setListNotSorted(this.getSearchList());
		this.scroll.hoversTexts = hs;
		this.resetEntity();
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		if (this.mc.world.getTotalWorldTime()%40!=0) { return; }
		for (int id : this.dataIDs.values()) {
			Entity entity = this.mc.world.getEntityByID(id);
			if (entity!=null) {
				float distance = this.player.getDistance(entity);
				for (int i=0; i<this.scroll.getList().size(); i++) {
					if (this.scroll.getList().get(i).indexOf("ID:"+id+" ")!=-1) {
						this.scroll.hoversTexts[i] = new String[] {
								chr+"7Name: "+chr+"r"+new TextComponentTranslation(entity.getName()).getFormattedText(),
								chr+"7Distance Of: "+chr+"6"+this.df.format(distance),
								chr+"7Class Type: "+chr+"f"+entity.getClass().getSimpleName() };
						break;
					}
				}
			}
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.resetEntity();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {  }

	@Override
	public void drawScreen(int i, int j, float f) {
		if (this.subgui==null) {
			GlStateManager.pushMatrix();
			if (this.selectEntity!=null) {
				int r, p = 0, x = 221, y = 162;
				if (this.selectEntity instanceof EntityLivingBase) { r = (int) (3 * this.player.world.getTotalWorldTime() % 360); }
				else {
					r = 0;
					y -= 34;
					if (this.selectEntity instanceof EntityItem) { p = 30; y += 10; }
					if (this.selectEntity instanceof EntityItemFrame) { x += 16; }
				}
				this.drawNpc(this.selectEntity, x, y, 1.0f, r, p, 0);
			}
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(this.guiLeft + 191, this.guiTop + 85, this.guiLeft + 252, this.guiTop + 171, 0xFF808080);
			Gui.drawRect(this.guiLeft + 192, this.guiTop + 86, this.guiLeft + 251, this.guiTop + 170, 0xFF000000);
			GlStateManager.popMatrix();
		}
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("wand.hover.edit").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("wand.hover.del").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("wand.hover.reset").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("wand.hover.freeze").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("wand.hover.tp").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("wand.hover.resetall").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("wand.hover.showall").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 191, this.guiTop + 85, 61, 86)) {
			this.setHoverText(new TextComponentTranslation("wand.hover.entity").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	private List<String> getSearchList() {
		if (this.search.isEmpty()) { return new ArrayList<String>(this.list); }
		List<String> list = new ArrayList<String>();
		for (String name : this.list) {
			if (name.toLowerCase().contains(this.search)) {
				list.add(name);
			}
		}
		return list;
	}
	
	private void resetEntity() {
		this.selectEntity = null;
		if (this.dataIDs.containsKey(this.scroll.getSelected())) {
			Entity entity = this.mc.world.getEntityByID(this.dataIDs.get(scroll.getSelected()));
			if (entity == null) {
				Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
				return;
			}
			this.selectEntity = entity;
		}
	}
	
}
