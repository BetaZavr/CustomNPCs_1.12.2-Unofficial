package noppes.npcs.client.gui;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.data.SpawnNPCData;
import noppes.npcs.util.AdditionalMethods;

public class GuiNpcMobSpawnerSelector
extends SubGuiInterface
implements IGuiData, ICustomScrollListener, ITextfieldListener {
	
	private static String search = "";
	public int activeTab = 1;
	public int showingClones = 0;
	private List<String> list;
	private GuiCustomScroll scroll;
	// new
	public EntityLivingBase selectNpc;
	public SpawnNPCData spawnData;
	
	public GuiNpcMobSpawnerSelector() {
		this.activeTab = 1;
		this.showingClones = 0;
		this.xSize = 256;
		this.closeOnEsc = true;
		this.setBackground("menubg.png");
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiTop += 10;
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(165, 188);
		} else {
			this.scroll.clear();
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 26;
		this.addScroll(this.scroll);
		
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 4, 165, 20,
				GuiNpcMobSpawnerSelector.search));
		GuiMenuTopButton button;
		this.addTopButton(button = new GuiMenuTopButton(3, this.guiLeft + 4, this.guiTop - 17, "spawner.clones"));
		button.active = this.showingClones==0;
		this.addTopButton(button = new GuiMenuTopButton(4, button, "spawner.entities"));
		button.active = this.showingClones==1;
		this.addTopButton(button = new GuiMenuTopButton(5, button, "gui.server"));
		button.active = this.showingClones==2;
		if (this.showingClones == 0 || this.showingClones == 2) {
			this.addSideButton(new GuiMenuSideButton(21, this.guiLeft - 69, this.guiTop + 2, 70, 22, "Tab 1"));
			this.addSideButton(new GuiMenuSideButton(22, this.guiLeft - 69, this.guiTop + 23, 70, 22, "Tab 2"));
			this.addSideButton(new GuiMenuSideButton(23, this.guiLeft - 69, this.guiTop + 44, 70, 22, "Tab 3"));
			this.addSideButton(new GuiMenuSideButton(24, this.guiLeft - 69, this.guiTop + 65, 70, 22, "Tab 4"));
			this.addSideButton(new GuiMenuSideButton(25, this.guiLeft - 69, this.guiTop + 86, 70, 22, "Tab 5"));
			this.addSideButton(new GuiMenuSideButton(26, this.guiLeft - 69, this.guiTop + 107, 70, 22, "Tab 6"));
			this.addSideButton(new GuiMenuSideButton(27, this.guiLeft - 69, this.guiTop + 128, 70, 22, "Tab 7"));
			this.addSideButton(new GuiMenuSideButton(28, this.guiLeft - 69, this.guiTop + 149, 70, 22, "Tab 8"));
			this.addSideButton(new GuiMenuSideButton(29, this.guiLeft - 69, this.guiTop + 170, 70, 22, "Tab 9"));
			this.getSideButton(20 + this.activeTab).active = true;
			this.showClones();
		} else {
			this.showEntities();
		}
		this.addButton(new GuiNpcButton(0, this.guiLeft + 171, this.guiTop + 170, 80, 20, "gui.done"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 171, this.guiTop + 192, 80, 20, "gui.cancel"));
		
		if (this.spawnData==null) { return; }
		
		this.addLabel(new GuiNpcLabel(5, new TextComponentTranslation("type.count").getFormattedText()+":", this.guiLeft + 170, this.guiTop + 153));
		GuiNpcTextField tf = new GuiNpcTextField(2, this, this.guiLeft + 216, this.guiTop + 148, 35, 20, ""+this.spawnData.count);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0, 7, this.spawnData.count);
		this.addTextField(tf);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: {
				this.close();
				break;
			}
			case 1: {
				this.scroll.clear();
				this.close();
				break;
			}
			case 3: {
				this.selectNpc = null;
				this.showingClones = 0;
				this.initGui();
				break;
			}
			case 4: {
				this.selectNpc = null;
				this.showingClones = 1;
				this.initGui();
				break;
			}
			case 5: {
				this.selectNpc = null;
				this.showingClones = 2;
				this.initGui();
				break;
			}
		}
		if (button.id > 20) {
			this.activeTab = button.id - 20;
			this.initGui();
		}
	}

	public NBTTagCompound getCompound() {
		String sel = this.scroll.getSelected();
		if (sel == null) { return null; }
		NBTTagCompound nbtEntity = null;
		if (this.showingClones==0) {
			nbtEntity = ClientCloneController.Instance.getCloneData(this.player, sel, this.activeTab);
		} else if (this.showingClones==1) {
			nbtEntity = this.spawnData.compound;
		}
		return nbtEntity;
	}

	private List<String> getSearchList() {
		if (GuiNpcMobSpawnerSelector.search.isEmpty()) {
			return new ArrayList<String>(this.list);
		}
		List<String> list = new ArrayList<String>();
		for (String name : this.list) {
			if (name.toLowerCase().contains(GuiNpcMobSpawnerSelector.search)) {
				list.add(name);
			}
		}
		return list;
	}

	public String getSelected() {
		return this.scroll.getSelected();
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (!GuiNpcMobSpawnerSelector.search.equals(this.getTextField(1).getText())) { // filter
			GuiNpcMobSpawnerSelector.search = this.getTextField(1).getText().toLowerCase();
			this.scroll.setList(this.getSearchList());
		}
		if (i==200 || i==208 || i==ClientProxy.frontButton.getKeyCode() || i==ClientProxy.backButton.getKeyCode()) {
			this.resetEntity();
		}
	}

	protected NBTTagList newDoubleNBTList(double... par1ArrayOfDouble) {
		NBTTagList nbttaglist = new NBTTagList();
		double[] adouble = par1ArrayOfDouble;
		for (int i = par1ArrayOfDouble.length, j = 0; j < i; ++j) {
			double d1 = adouble[j];
			nbttaglist.appendTag(new NBTTagDouble(d1));
		}
		return nbttaglist;
	}

	@Override
	public void save() {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NPCData", 10)) {
			this.selectNpc = (EntityNPCInterface) EntityList.createEntityFromNBT(compound.getCompoundTag("NPCData"), this.player.world);
			return;
		}
		NBTTagList nbtlist = compound.getTagList("List", 8);
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < nbtlist.tagCount(); ++i) {
			list.add(nbtlist.getStringTagAt(i));
		}
		this.list = list;
		this.scroll.setList(this.getSearchList());
		if (this.spawnData!=null) {
			this.scroll.setSelected(AdditionalMethods.instance.deleteColor(this.spawnData.getTitle()));
			if (this.selectNpc == null) {
				String name = new TextComponentTranslation("type.empty").getFormattedText();
				if (this.spawnData.compound!= null) {
					if (this.spawnData.compound.hasKey("ClonedName")) {
						name = new TextComponentTranslation(this.spawnData.compound.getString("ClonedName")).getFormattedText();
					} else if (this.spawnData.compound.hasKey("Name")) {
						name = new TextComponentTranslation(this.spawnData.compound.getString("Name")).getFormattedText();
					}
				}
				Client.sendData(EnumPacketServer.GetClone, false, name, this.activeTab);
			}
		}
	}

	private void showClones() {
		if (this.showingClones==2) {
			Client.sendData(EnumPacketServer.CloneList, this.activeTab);
			return;
		}
		this.list = new ArrayList<String>(ClientCloneController.Instance.getClones(this.activeTab));
		this.scroll.setList(this.getSearchList());
	}

	private void showEntities() {
		ArrayList<String> list = new ArrayList<String>();
		List<Class<? extends Entity>> classes = new ArrayList<Class<? extends Entity>>();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			if (ent.getRegistryName().getResourceDomain().equals(CustomNpcs.MODID)) { continue; }
			Class<? extends Entity> c = (Class<? extends Entity>) ent.getEntityClass();
			String name = ent.getName();
			try {
				if (classes.contains(c) || !EntityLiving.class.isAssignableFrom(c)
						|| c.getConstructor(World.class) == null || Modifier.isAbstract(c.getModifiers())) {
					continue;
				}
				Entity entity = EntityList.createEntityByIDFromName(ent.getRegistryName(), Minecraft.getMinecraft().world);
				if (!(entity instanceof EntityMob)) {
					continue;
				}
				list.add(name.toString());
				classes.add(c);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException ex) {
			}
		}
		this.list = list;
		this.scroll.setList(this.getSearchList());
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.selectNpc!=null) {
			this.drawNpc(this.selectNpc, 210, 80, 1.0f, (int) (3 * this.player.world.getTotalWorldTime() % 360), 0, false);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, 1.0f);
		Gui.drawRect(this.guiLeft + 181, this.guiTop + 4, this.guiLeft + 242, this.guiTop + 90, 0xFF808080);
		Gui.drawRect(this.guiLeft + 182, this.guiTop + 5, this.guiLeft + 241, this.guiTop + 89, 0xFF000000);
		GlStateManager.popMatrix();
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		String sel = this.scroll.getSelected();
		if (sel == null) { return; }
		this.resetEntity();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.close();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.spawnData==null || textField.getId()!=2) { return; }
		this.spawnData.count = textField.getInteger();
	}
	
	private void resetEntity() {
		String sel = this.scroll.getSelected();
		if (this.showingClones==0) { // client
			NBTTagCompound npcNbt = ClientCloneController.Instance.getCloneData(this.player, sel, this.activeTab);
			if (npcNbt==null) { return; }
			Entity entity = EntityList.createEntityFromNBT(npcNbt, Minecraft.getMinecraft().world);
			if (entity instanceof EntityLivingBase) {
				if (this.spawnData!=null) {
					this.spawnData.typeClones = 0;
					this.spawnData.compound = npcNbt;
				}
				this.selectNpc = (EntityLivingBase) entity;
			}
		}
		else if (this.showingClones==1) { // mob
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				if (ent.getName().equals(sel)) {
					Entity entity = EntityList.createEntityByIDFromName(ent.getRegistryName(), Minecraft.getMinecraft().world);
					if (entity instanceof EntityLivingBase) {
						this.selectNpc = (EntityLivingBase) entity;
						if (this.spawnData!=null) {
							this.spawnData.typeClones =1;
							this.spawnData.compound = entity.writeToNBT(new NBTTagCompound());
							this.spawnData.compound.setString("id", ent.getRegistryName().toString());
						}
					}
					return;
				}
			}
		}
		else { // server
			if (this.spawnData!=null) {
				this.spawnData.typeClones = 2;
				this.spawnData.compound = new NBTTagCompound();
				this.spawnData.compound.setString("Name", sel);
			}
			Client.sendData(EnumPacketServer.GetClone, false, sel, this.activeTab);
		}
	}
	
}
