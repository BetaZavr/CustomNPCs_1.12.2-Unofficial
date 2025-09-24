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
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.ServerEventsHandler;
import noppes.npcs.api.wrapper.VillagerWrapper;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class GuiNpcRemoteEditor extends GuiNPCInterface
		implements IGuiData, GuiYesNoCallback, ICustomScrollListener {

	protected static boolean all = false;
	protected final HashMap<String, Integer> dataIDs = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected final DecimalFormat df = new DecimalFormat("#.#");
	public Entity selectEntity;

	public GuiNpcRemoteEditor() {
		super();
		setBackground("menubg.png");
        xSize = 256;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				if (!dataIDs.containsKey(scroll.getSelected())) { return; }
				Entity entity = mc.world.getEntityByID(dataIDs.get(scroll.getSelected()));
				if (entity instanceof EntityNPCInterface) {
					Client.sendData(EnumPacketServer.RemoteMainMenu, dataIDs.get(scroll.getSelected()));
					return;
				}
				if (entity instanceof EntityVillager) {
					ServerEventsHandler.Merchant = (EntityVillager) entity;
					MerchantRecipeList merchantrecipelist = ServerEventsHandler.Merchant.getRecipes(player);
					if (merchantrecipelist != null) {
						player.openGui(CustomNpcs.instance, EnumGuiType.MerchantAdd.ordinal(), player.world, entity.getEntityId(), 0, 0);
						return;
					}
				}
				if (entity == null) { return; }
				GuiNbtBook gui = new GuiNbtBook(0, 0, 0);
				NBTTagCompound data = new NBTTagCompound();
				entity.writeToNBTAtomically(data);
				NBTTagCompound compound = new NBTTagCompound();
				compound.setInteger("EntityId", entity.getEntityId());
				compound.setTag("Data", data);
				gui.setGuiData(compound);
				displayGuiScreen(gui);
				break;
			} // edit
			case 1: {
				if (!dataIDs.containsKey(scroll.getSelected())) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this, scroll.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			} // remove entity
			case 2: {
				if (!dataIDs.containsKey(scroll.getSelected())) { return; }
				Client.sendData(EnumPacketServer.RemoteReset, dataIDs.get(scroll.getSelected()));
				Entity entity2 = player.world.getEntityByID(dataIDs.get(scroll.getSelected()));
				if (entity2 instanceof EntityNPCInterface) { ((EntityNPCInterface) entity2).reset(); }
				break;
			} // reset
			case 3: Client.sendData(EnumPacketServer.RemoteFreeze); break; // freeze
			case 4: {
				if (dataIDs.containsKey(scroll.getSelected())) {
					Client.sendData(EnumPacketServer.RemoteTpToNpc, true, dataIDs.get(scroll.getSelected()));
					CustomNPCsScheduler.runTack(() -> Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all), 250);
				}
				break;
			} // tp
			case 5: {
				for (int ids : dataIDs.values()) {
					Client.sendData(EnumPacketServer.RemoteReset, ids);
					Entity entity = player.world.getEntityByID(ids);
					if (entity instanceof EntityNPCInterface) { ((EntityNPCInterface) entity).reset(); }
				}
				break;
			} // reset all
			case 6: {
				GuiNpcRemoteEditor.all = ((GuiNpcCheckBox) button).isSelected();
				Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
				break;
			} // all entities
			case 7: {
				Client.sendData(EnumPacketServer.RemoveNpcEdit);
				CustomNpcs.proxy.openGui(null, EnumGuiType.MainMenuGlobal, 0, 0, 0);
				break;
			} // global
		}
	}

	public void confirmClicked(boolean flag, int i) {
		if (flag) {
			Client.sendData(EnumPacketServer.RemoteDelete, dataIDs.get(scroll.getSelected()), GuiNpcRemoteEditor.all);
			selectEntity = null;
			Entity e = player.world.getEntityByID(dataIDs.get(scroll.getSelected()));
			if (e != null) { player.world.removeEntity(e); }
		}
		CustomNPCsScheduler.runTack(() -> {
			NoppesUtil.openGUI(player, this);
			Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
		}, 250);
	}

	@Override
	public void postDrawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui == null) {
			GlStateManager.pushMatrix();
			if (selectEntity != null) {
				int yaw;
				int pitch = 0;
				int x = 221;
				int y = 162;
				if (selectEntity instanceof EntityLivingBase) { yaw = (int) (3 * player.world.getTotalWorldTime() % 360); }
				else {
					yaw = 0;
					y -= 34;
					if (selectEntity instanceof EntityItem) {
						pitch = 30;
						y += 10;
					}
					if (selectEntity instanceof EntityItemFrame) { x += 16; }
				}
				drawNpc(selectEntity, x, y, 1.0f, yaw, pitch, 0);
			}
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(guiLeft + 191, guiTop + 85, guiLeft + 252, guiTop + 171, new Color(0xFF808080).getRGB());
			Gui.drawRect(guiLeft + 192, guiTop + 86, guiLeft + 251, guiTop + 170, new Color(0xFF000000).getRGB());
			GlStateManager.popMatrix();
		}
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (isMouseHover(mouseX, mouseY, guiLeft + 191, guiTop + 85, 61, 86)) { drawHoverText("wand.hover.entity"); }
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
		addButton(new GuiNpcButton(0, guiLeft + 170, guiTop + 4, 82, 20, "selectServer.edit")
				.setIsEnable(selectEntity != null && !(selectEntity instanceof EntityPlayer))
				.setHoverText("wand.hover.edit"));
		// del
		addButton(new GuiNpcButton(1, guiLeft + 170, guiTop + 24, 82, 20, "selectWorld.deleteButton")
				.setIsEnable(selectEntity != null)
				.setHoverText("wand.hover.del"));
		// reset
		addButton(new GuiNpcButton(2, guiLeft + 170, guiTop + 44, 82, 20, "remote.reset")
				.setIsEnable(selectEntity instanceof EntityNPCInterface)
				.setHoverText("wand.hover.reset"));
		// freeze
		addButton(new GuiNpcButton(3, guiLeft + 170, guiTop + 192, 82, 20,  CustomNpcs.FreezeNPCs ? "remote.unfreeze" : "remote.freeze")
				.setHoverText("wand.hover.freeze"));
		// tp
		addButton(new GuiNpcButton(4, guiLeft + 170, guiTop + 64, 82, 20, "remote.tp")
				.setIsEnable(scroll.hasSelected())
				.setHoverText("wand.hover.tp"));
		// reset all
		addButton(new GuiNpcButton(5, guiLeft + 170, guiTop + 172, 82, 20, "remote.resetall")
				.setHoverText("wand.hover.resetall"));
		// all entities
		addButton(new GuiNpcCheckBox(6, guiLeft + 174, guiTop + 86, 13, 14, "", null, GuiNpcRemoteEditor.all)
				.setHoverText("wand.hover.showall"));
		// global
		addButton(new GuiMenuSideButton(7, guiLeft + xSize, guiTop + 8, "menu.global")
				.setIsLeft(false)
				.setHoverText("display.hover.menu.global"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.RemoteNpcsGet, GuiNpcRemoteEditor.all);
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (subgui != null) { return subgui.keyCnpcsPressed(typedChar, keyCode); }
		if (keyCode == Keyboard.KEY_ESCAPE || isInventoryKey(keyCode)) {
			onClosed();
			return true;
		}
		boolean bo = super.keyCnpcsPressed(typedChar, keyCode);
		if (keyCode == Keyboard.KEY_UP ||
				keyCode == Keyboard.KEY_DOWN ||
				keyCode == mc.gameSettings.keyBindForward.getKeyCode() ||
				keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
			resetEntity();
		}
		return bo;
	}

	private void resetEntity() {
		selectEntity = null;
		if (dataIDs.containsKey(scroll.getSelected())) {
			Entity entity = mc.world.getEntityByID(dataIDs.get(scroll.getSelected()));
			if (entity == null) { return; }
			selectEntity = entity;
		}
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
			String type = ((char) 167) + "7";
			if (entity == null) { type = ((char) 167) + "4"; }
			else if (entity instanceof EntityNPCInterface) { type = ((char) 167) + "a"; }
			else if (entity instanceof EntityPlayer) { type = ((char) 167) + "c"; }
			else if (entity instanceof EntityAnimal) { type = ((char) 167) + "e"; }
			else if (entity instanceof EntityMob) { type = ((char) 167) + "b"; }
			float distance;
			String name;
			if (entity != null) {
				distance = player.getDistance(entity);
				name = new TextComponentTranslation(entity.getName()).getFormattedText();
				hts.put(i, Collections.singletonList(((char) 167) + "7Distance Of: " + ((char) 167) + "6" + df.format(distance)));
			}
			else {
				distance = nbt.getFloat("Distance");
				name = new TextComponentTranslation(nbt.getString("Name")).getFormattedText();
				List<String> hl = new ArrayList<>();
				hl.add(((char) 167) + "cNot load in client world");
				NBTTagList posList = nbt.getTagList("Pos", 6);
				hl.add(((char) 167) + "7Distance to: " + ((char) 167) + "6" + df.format(distance));
				hl.add(((char) 167) + "7Position X:" + ((char) 167) + "6" + (df.format(posList.getDoubleAt(0))) +
						((char) 167) + "7, Y:" + ((char) 167) + "6" + (df.format(posList.getDoubleAt(1))) +
						((char) 167) + "7, Z:" + ((char) 167) + "6" + (df.format(posList.getDoubleAt(2))));
				hl.add(((char) 167) + "7Class Type: " + ((char) 167) + "f" + nbt.getString("Class"));
				hts.put(i, hl);
			}
			String key = type + "ID:" + nbt.getInteger("Id") + " " + ((char) 167) + "r" + (name) + " " + ((char) 167) + "7" + df.format(distance);
			list.add(key);
			dataIDs.put(key, nbt.getInteger("Id"));
		}
		scroll.setUnsortedList(list).setHoverTexts(hts);
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
						List<String> hl = new ArrayList<>();
						hl.add(((char) 167) + "7Distance to: " + ((char) 167) + "6" + df.format(distance));
						hl.add(((char) 167) + "7Position X:" + ((char) 167) + "6" + (df.format(entity.posX)) +
								((char) 167) + "7, Y:" + ((char) 167) + "6" + (df.format(entity.posY)) +
								((char) 167) + "7, Z:" + ((char) 167) + "6" + (df.format(entity.posZ)));
						hl.add(((char) 167) + "7Class Type: " + ((char) 167) + "f" + entity.getClass().getSimpleName());
						scroll.getHoversTexts().put(i, hl);
						break;
					}
				}
			}
		}
	}

}
