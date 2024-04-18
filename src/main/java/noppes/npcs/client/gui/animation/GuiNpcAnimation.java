package noppes.npcs.client.gui.animation;

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.CustomNPCsScheduler;

public class GuiNpcAnimation
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener, IGuiData {

	public static int backColor = 0xFF000000;
	private GuiCustomScroll scrollType, scrollAnims;
	private final String[][] typeHovers;
	private final Map<String, AnimationKind> dataType;
	private final Map<String, AnimationConfig> dataAnims;
	private EntityNPCInterface npcAnim;
	private DataAnimation animation;
	private String selType, selAnim;
	private AnimationController aData;
	
	public GuiNpcAnimation(EntityCustomNpc npc) {
		super(npc, 4);
		this.closeOnEsc = true;
		this.animation = new DataAnimation(npc);
		this.setBackground("bgfilled.png");
		
		this.dataType = Maps.<String, AnimationKind>newLinkedHashMap();
		this.dataType.put("puppet."+AnimationKind.INIT.name().toLowerCase().replace("_", ""), AnimationKind.INIT);
		this.dataType.put("puppet."+AnimationKind.JUMP.name().toLowerCase().replace("_", ""), AnimationKind.JUMP);
		this.dataType.put("puppet."+AnimationKind.ATTACKING.name().toLowerCase().replace("_", ""), AnimationKind.ATTACKING);
		this.dataType.put("puppet."+AnimationKind.HIT.name().toLowerCase().replace("_", ""), AnimationKind.HIT);
		this.dataType.put("puppet."+AnimationKind.DIES.name().toLowerCase().replace("_", ""), AnimationKind.DIES);
		this.dataType.put("puppet."+AnimationKind.BASE.name().toLowerCase().replace("_", ""), AnimationKind.BASE);
		this.dataType.put("puppet."+AnimationKind.STANDING.name().toLowerCase().replace("_", ""), AnimationKind.STANDING);
		this.dataType.put("puppet."+AnimationKind.FLY_STAND.name().toLowerCase().replace("_", ""), AnimationKind.FLY_STAND);
		this.dataType.put("puppet."+AnimationKind.WATER_STAND.name().toLowerCase().replace("_", ""), AnimationKind.WATER_STAND);
		this.dataType.put("puppet."+AnimationKind.REVENGE_STAND.name().toLowerCase().replace("_", ""), AnimationKind.REVENGE_STAND);
		this.dataType.put("puppet."+AnimationKind.WALKING.name().toLowerCase().replace("_", ""), AnimationKind.WALKING);
		this.dataType.put("puppet."+AnimationKind.FLY_WALK.name().toLowerCase().replace("_", ""), AnimationKind.FLY_WALK);
		this.dataType.put("puppet."+AnimationKind.WATER_WALK.name().toLowerCase().replace("_", ""), AnimationKind.WATER_WALK);
		this.dataType.put("puppet."+AnimationKind.REVENGE_WALK.name().toLowerCase().replace("_", ""), AnimationKind.REVENGE_WALK);
		typeHovers = new String[this.dataType.size()][];
		int i = 0;
		for (AnimationKind ak : this.dataType.values()) {
			typeHovers[i] = new String[] { new TextComponentTranslation("animation.hover.anim." + ak.get()).getFormattedText() };
			i++;
		}
		this.dataAnims = Maps.<String, AnimationConfig>newHashMap();
		selType = "";
		selAnim = "";
		npcAnim = AdditionalMethods.copyToGUI(npc, mc.world, false);
		Client.sendData(EnumPacketServer.AnimationGet);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 8;
		int y = this.guiTop + 14;
		if (this.scrollType == null) {
			(this.scrollType = new GuiCustomScroll(this, 0)).setSize(120, 198);
			this.scrollType.setListNotSorted(Lists.<String>newArrayList(this.dataType.keySet()));
			this.scrollType.hoversTexts = typeHovers;
		}
		this.scrollType.guiLeft = x;
		this.scrollType.guiTop = y;
		this.addScroll(this.scrollType);
		if (selType.isEmpty()) {
			for (String key : this.dataType.keySet()) {
				if (this.dataType.get(key) == AnimationKind.STANDING) {
					selType = key;
					break;
				}
			}
		}
		this.scrollType.setSelected(selType);
		this.addLabel(new GuiNpcLabel(0, "animation.type", x + 1, y - 10));
		
		x += 125;
		dataAnims.clear();
		AnimationKind type = null;
		aData = AnimationController.getInstance();
		if (dataType.containsKey(selType)) {
			type = dataType.get(selType);
			for (AnimationConfig ac : aData.getAnimations(this.animation.data.get(type))) {
				dataAnims.put(ac.getSettingName(), ac);
			}
		}
		if (this.scrollAnims == null) { (this.scrollAnims = new GuiCustomScroll(this, 1)).setSize(120, 198); }
		this.scrollAnims.setListNotSorted(Lists.<String>newArrayList(this.dataAnims.keySet()));
		this.scrollAnims.guiLeft = x;
		this.scrollAnims.guiTop = y;
		this.addScroll(this.scrollAnims);
		if (selAnim.isEmpty() && !this.scrollAnims.getList().isEmpty()) {
			for (String key : this.scrollAnims.getList()) { selAnim = key; break; }
		}
		if (!selAnim.isEmpty()) {
			if (this.scrollAnims.getList().contains(selAnim)) { this.scrollAnims.setSelected(selAnim); }
			else { selAnim = ""; }
		}
		this.addLabel(new GuiNpcLabel(1, new TextComponentTranslation("movement.animation").getFormattedText() + ":", x + 1, y - 10));

		x += 125;
		this.addButton(new GuiNpcButton(0, x, y, 60, 20, "markov.generate"));
		this.getButton(0).enabled = type != AnimationKind.BASE || dataAnims.isEmpty();
		this.addButton(new GuiNpcButton(3, x, y += 22, 60, 20, "gui.load"));
		this.getButton(3).enabled = !aData.animations.isEmpty();
		this.addButton(new GuiNpcButton(1, x, y += 22, 60, 20, "gui.remove"));
		this.getButton(1).enabled = !selAnim.isEmpty();
		this.addButton(new GuiNpcButton(2, x, y += 22, 60, 20, "gui.edit"));
		this.getButton(2).enabled = !selAnim.isEmpty();
		this.resetAnims();
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		AnimationConfig anim = this.getAnim();
		switch(button.id) {
			case 0: { // add anim
				if (this.scrollType == null || !this.scrollType.hasSelected()) { return; }
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.instance.deleteColor(new TextComponentTranslation(this.scrollType.getSelected()).getFormattedText() + "_" + aData.getUnusedId())));
				break;
			}
			case 1: { // del anim
				if (anim==null || !dataType.containsKey(scrollType.getSelected())) { return; }
				if (this.animation.removeAnimation(dataType.get(scrollType.getSelected()).get(),  anim.id)) { this.selAnim = ""; }
				this.initGui();
				
				break;
			}
			case 2: { // edit
				this.setSubGui(new SubGuiEditAnimation(this.npc, anim, 4, this));
				break;
			}
			case 3: { // load anim
				this.setSubGui(new SubGuiLoadAnimation(2, this.npc));
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
		}
	}

	private AnimationConfig getAnim() {
		if (!this.dataAnims.containsKey(selAnim)) { selAnim = ""; }
		if (selAnim.isEmpty() || !this.dataAnims.containsKey(selAnim)) { return null; }
		return this.dataAnims.get(selAnim);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.subgui != null) {
			this.subgui.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		AnimationConfig anim = this.getAnim();
		if (anim != null && !this.hasSubGui() && this.npcAnim != null) {
			npcAnim.field_20061_w = this.npc.field_20061_w;
			npcAnim.field_20062_v = this.npc.field_20062_v;
			npcAnim.field_20063_u = this.npc.field_20063_u;
			npcAnim.field_20064_t = this.npc.field_20064_t;
			npcAnim.field_20065_s = this.npc.field_20065_s;
			npcAnim.field_20066_r = this.npc.field_20066_r;
			npcAnim.ticksExisted = this.npc.ticksExisted;
			npcAnim.deathTime = 0;
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + 342.0f, this.guiTop + 15.0f, 0.0f);
			Gui.drawRect(-1, -1, 56, 91, 0xFFF080F0);
			Gui.drawRect(0, 0, 55, 90, GuiNpcAnimation.backColor);
			GlStateManager.popMatrix();
			this.drawNpc(this.npcAnim, 369, 87, 1.0f, 0, 0, 0);
		}
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (this.scrollAnims!=null && this.scrollAnims.hover != -1) { // scroll anim
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.list").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.create").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.del").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.edit").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.load").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}
	
	@Override
	public void save() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("save", true);
		Client.sendData(EnumPacketServer.AnimationChange, nbt);
		CustomNPCsScheduler.runTack(() -> {
			Client.sendData(EnumPacketServer.AnimationSave, this.animation.save(new NBTTagCompound()));
		}, 500);
	}
	
	@Override
	public void close() {
		this.save();
		CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
	}
	
	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.animation.load(compound);
		this.initGui();
	}
	
	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (scroll.id == 0) { this.selType = scroll.getSelected(); } // animation Type
		if (scroll.id == 1) { this.selAnim = scroll.getSelected(); } // animation ID
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		AnimationConfig anim = this.getAnim();
		if (anim == null) { return; }
		this.setSubGui(new SubGuiEditText(3, anim.name));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		AnimationConfig anim = this.getAnim();
		AnimationKind type = this.dataType.get(this.scrollType.getSelected());
		if (subgui.id == 1) { // add new
			if (!(subgui instanceof SubGuiEditText) || ((SubGuiEditText) subgui).cancelled || !this.dataType.containsKey(this.scrollType.getSelected())) { return; }
			AnimationConfig newAnim = (AnimationConfig) AnimationController.getInstance().createNew();
			newAnim.name = ((SubGuiEditText) subgui).text[0];
			animation.data.get(type).add(newAnim.id);
			this.selAnim = newAnim.getSettingName();
			this.initGui();
			CustomNPCsScheduler.runTack(() -> { 
				this.setSubGui(new SubGuiEditAnimation(this.npc, newAnim, 4, this));
			}, 50);
		}
		else if (subgui.id == 2) { // load
			if (!(subgui instanceof SubGuiLoadAnimation) || ((SubGuiLoadAnimation) subgui).cancelled || ((SubGuiLoadAnimation) subgui).animation==null || !this.dataType.containsKey(this.scrollType.getSelected())) { return; }
			try {
				anim = ((SubGuiLoadAnimation) subgui).animation;
				type = dataType.get(scrollType.getSelected());
				animation.data.get(type).add(anim.id);
				selAnim = anim.getSettingName();
				dataAnims.put(selAnim, anim);
				initGui();
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		else if (subgui.id == 3) { // rename
			if (!(subgui instanceof SubGuiEditText) || anim == null) { return; }
			anim.name = ((SubGuiEditText) subgui).text[0];
			this.selAnim = anim.getSettingName();
			this.initGui();
		}
		else if (subgui.id == 4) { // new
			this.displayGuiScreen(this);
			if (!(subgui instanceof SubGuiEditAnimation) || ((SubGuiEditAnimation) subgui).anim == null) { return; }
			this.selAnim = ((SubGuiEditAnimation) subgui).anim.getSettingName();
			if (!this.animation.data.get(type).contains(anim.id)) { this.animation.data.get(type).add(anim.id); }
			Client.sendData(EnumPacketServer.AnimationChange, ((SubGuiEditAnimation) subgui).anim.writeToNBT(new NBTTagCompound()));
			this.initGui();
		}
	}
	
	private void resetAnims() {
		AnimationConfig anim = this.getAnim();
		if (anim == null || this.npcAnim == null) { return; }
		AnimationConfig ac = anim.copy();
		ac.isEdit = true;
		ac.type = AnimationKind.STANDING;
		NBTTagCompound npcNbt = new NBTTagCompound();
		this.npcAnim.animation.clear();
		this.npcAnim.writeEntityToNBT(npcNbt);
		this.npcAnim.writeToNBTOptional(npcNbt);
		this.npcAnim.display.setName("1_"+this.npc.getName());
		this.npcAnim.animation.activeAnim = ac;
		this.npcAnim.setHealth(this.npcAnim.getMaxHealth());
		npcAnim.deathTime = 0;
	}
	
}
