package noppes.npcs.client.gui;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.Gui;
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
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;

public class GuiNpcRemoteEditor
extends GuiNPCInterface
implements IGuiData, GuiYesNoCallback, ICustomScrollListener {

	private static boolean all = false;
	private final HashMap<String, Integer> dataIDs = new HashMap<>();
	private GuiCustomScroll scroll;
	public Entity selectEntity;
	private final DecimalFormat df = new DecimalFormat("#.#");

	public GuiNpcRemoteEditor() {
		super();
        xSize = 256;
		setBackground("menubg.png");
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: {
				if (!dataIDs.containsKey(scroll.getSelected())) {
					return;
				}
				Entity entity = mc.world.getEntityByID(dataIDs.get(scroll.getSelected()));
				if (entity instanceof EntityNPCInterface) {
					Client.sendData(EnumPacketServer.RemoteMainMenu, dataIDs.get(scroll.getSelected()));
					return;
				} else {
					if (entity == null) {
						return;
					}
					GuiNbtBook gui = new GuiNbtBook(0, 0, 0);
					NBTTagCompound data = new NBTTagCompound();
					entity.writeToNBTAtomically(data);
					NBTTagCompound compound = new NBTTagCompound();
					compound.setInteger("EntityId", entity.getEntityId());
					compound.setTag("Data", data);
					gui.setGuiData(compound);
					mc.displayGuiScreen(gui);
				}
				break;
			}
			case 1: { // remove entity
				if (!dataIDs.containsKey(scroll.getSelected())) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this, scroll.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 2: {
				if (!dataIDs.containsKey(scroll.getSelected())) {
					return;
				}
				Client.sendData(EnumPacketServer.RemoteReset, dataIDs.get(scroll.getSelected()));
				Entity entity2 = player.world.getEntityByID(dataIDs.get(scroll.getSelected()));
				if (entity2 instanceof EntityNPCInterface) {
					((EntityNPCInterface) entity2).reset();
				}
				break;
			}
			case 3: {
				Client.sendData(EnumPacketServer.RemoteFreeze);
				break;
			}
			case 4: { // tp
				if (!dataIDs.containsKey(scroll.getSelected())) {
					return;
				}
				Client.sendData(EnumPacketServer.RemoteTpToNpc, dataIDs.get(scroll.getSelected()));
				CustomNPCsScheduler.runTack(() -> Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all), 250);
				break;
			}
			case 5: {
				for (int ids : dataIDs.values()) {
					Client.sendData(EnumPacketServer.RemoteReset, ids);
					Entity entity = player.world.getEntityByID(ids);
					if (entity instanceof EntityNPCInterface) {
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
			case 7: {
				Client.sendData(EnumPacketServer.RemoveNpcEdit);
				CustomNpcs.proxy.openGui(null, EnumGuiType.MainMenuGlobal, 0, 0, 0);
				break;
			}
			default: {
	
			}
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			Client.sendData(EnumPacketServer.RemoteDelete, dataIDs.get(scroll.getSelected()), GuiNpcRemoteEditor.all);
			selectEntity = null;
			Entity e = player.world.getEntityByID(dataIDs.get(scroll.getSelected()));
			if (e != null) {
				player.world.removeEntity(e);
			}
		}
		CustomNPCsScheduler.runTack(() -> {
			NoppesUtil.openGUI(player, this);
			Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
		}, 250);

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui == null) {
			GlStateManager.pushMatrix();
			if (selectEntity != null) {
				int r, p = 0, x = 221, y = 162;
				if (selectEntity instanceof EntityLivingBase) {
					r = (int) (3 * player.world.getTotalWorldTime() % 360);
				} else {
					r = 0;
					y -= 34;
					if (selectEntity instanceof EntityItem) {
						p = 30;
						y += 10;
					}
					if (selectEntity instanceof EntityItemFrame) {
						x += 16;
					}
				}
				drawNpc(selectEntity, x, y, 1.0f, r, p, 0);
			}
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(guiLeft + 191, guiTop + 85, guiLeft + 252, guiTop + 171, new Color(0xFF808080).getRGB());
			Gui.drawRect(guiLeft + 192, guiTop + 86, guiLeft + 251, guiTop + 170, new Color(0xFF000000).getRGB());
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (isMouseHover(mouseX, mouseY, guiLeft + 191, guiTop + 85, 61, 86)) {
			drawHoverText("wand.hover.entity");
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(165, 191); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 21;
		addScroll(scroll);
		// title
		String title = new TextComponentTranslation("remote.title").getFormattedText();
		int x = (xSize - fontRenderer.getStringWidth(title)) / 2;
		addLabel(new GuiNpcLabel(0, title, guiLeft + x, guiTop - 8));
		// edit
		GuiNpcButton button = new GuiNpcButton(0, guiLeft + 170, guiTop + 4, 82, 20, "selectServer.edit");
		button.setEnabled(selectEntity != null && !(selectEntity instanceof EntityPlayer));
		button.setHoverText("wand.hover.edit");
		addButton(button);
		// del
		button = new GuiNpcButton(1, guiLeft + 170, guiTop + 24, 82, 20, "selectWorld.deleteButton");
		button.setEnabled(selectEntity != null);
		button.setHoverText("wand.hover.del");
		addButton(button);
		// reset
		button = new GuiNpcButton(2, guiLeft + 170, guiTop + 44, 82, 20, "remote.reset");
		button.setEnabled(selectEntity instanceof EntityNPCInterface);
		button.setHoverText("wand.hover.reset");
		addButton(button);
		// freeze
		button = new GuiNpcButton(3, guiLeft + 170, guiTop + 192, 82, 20,  CustomNpcs.FreezeNPCs ? "remote.unfreeze" : "remote.freeze");
		button.setHoverText("wand.hover.freeze");
		addButton(button);
		// tp
		button = new GuiNpcButton(4, guiLeft + 170, guiTop + 64, 82, 20, "remote.tp");
		button.setEnabled(selectEntity != null);
		button.setHoverText("wand.hover.tp");
		addButton(button);
		// reset all
		button = new GuiNpcButton(5, guiLeft + 170, guiTop + 172, 82, 20, "remote.resetall");
		button.setHoverText("wand.hover.resetall");
		addButton(button);
		// all entities
		button = new GuiNpcCheckBox(6, guiLeft + 174, guiTop + 86, 20, 20, "", null, GuiNpcRemoteEditor.all);
		button.setHoverText("wand.hover.showall");
		addButton(button);
		// global
		button = new GuiMenuSideButton(7, guiLeft + xSize, guiTop + 8, "menu.global");
		((GuiMenuSideButton) button).setIsLeft(false);
		button.setHoverText("display.hover.menu.global");
		addButton(button);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || isInventoryKey(i)) { close(); }
		super.keyTyped(c, i);
		if (i == 200 || i == 208 || i == ClientProxy.frontButton.getKeyCode() || i == ClientProxy.backButton.getKeyCode()) { resetEntity(); }
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		scroll.mouseClicked(i, j, k);
	}

	private void resetEntity() {
		selectEntity = null;
		if (dataIDs.containsKey(scroll.getSelected())) {
			Entity entity = mc.world.getEntityByID(dataIDs.get(scroll.getSelected()));
			if (entity == null) {
				Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
				return;
			}
			selectEntity = entity;
		}
	}

	@Override
	public void save() {
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		resetEntity();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		NBTTagList nbtList = compound.getTagList("Data", 10);
		dataIDs.clear();
		List<String> list = new ArrayList<>();
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		for (int i = 0; i < nbtList.tagCount(); ++i) {
			NBTTagCompound nbt = nbtList.getCompoundTagAt(i);
			Entity entity = mc.world.getEntityByID(nbt.getInteger("Id"));
			if (entity == null) {
				continue;
			}
			String type = ((char) 167) + "7";
			if (entity instanceof EntityNPCInterface) {
				type = ((char) 167) + "a";
			} else if (entity instanceof EntityPlayer) {
				type = ((char) 167) + "c";
			} else if (entity instanceof EntityAnimal) {
				type = ((char) 167) + "e";
			} else if (entity instanceof EntityMob) {
				type = ((char) 167) + "b";
			}
			float distance = player.getDistance(entity);
			String key = type + "ID:" + nbt.getInteger("Id") + " " + ((char) 167) + "r" + (new TextComponentTranslation(entity.getName()).getFormattedText()) + " " + ((char) 167) + "7" + df.format(distance);
			list.add(key);
			dataIDs.put(key, nbt.getInteger("Id"));
			hts.put(i, Collections.singletonList(((char) 167) + "7Distance Of: " + ((char) 167) + "6" + df.format(distance)));
		}
		scroll.setListNotSorted(list);
		scroll.setHoverTexts(hts);
		resetEntity();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (mc.world.getTotalWorldTime() % 40 != 0) { return; }
		for (int id : dataIDs.values()) {
			Entity entity = mc.world.getEntityByID(id);
			if (entity != null) {
				float distance = player.getDistance(entity);
				for (int i = 0; i < scroll.getList().size(); i++) {
					if (scroll.getList().get(i).contains("ID:" + id + " ")) {
						List<String> l = new ArrayList<>();
						l.add(((char) 167) + "7Name: " + ((char) 167) + "r");
						l.add(new TextComponentTranslation(entity.getName()).getFormattedText());
						l.add(((char) 167) + "7Distance Of: " + ((char) 167) + "6" + df.format(distance));
						l.add(((char) 167) + "7Class Type: " + ((char) 167) + "f" + entity.getClass().getSimpleName());
						scroll.getHoversTexts().put(i, l);
						break;
					}
				}
			}
		}
	}

}
