package noppes.npcs.client.gui.animation;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiLoadAnimation
extends SubGuiInterface
implements ICustomScrollListener {

	public boolean cancelled;
	private GuiCustomScroll scroll;
	private Map<String, Integer> data;
	private String selected;
	public AnimationConfig animation;
	private EntityNPCInterface showNpc;

	public SubGuiLoadAnimation(int id, EntityNPCInterface npc) {
		this.npc = npc;
		this.id = id;
		this.cancelled = true;
		this.setBackground("smallbg.png");
		this.closeOnEsc = true;
		this.xSize = 176;
		this.ySize = 222;
		
		this.data = Maps.<String, Integer>newTreeMap();
		this.animation = null;
		this.selected = "";
		
		this.showNpc = null;
		NBTTagCompound npcNbt = new NBTTagCompound();
		npc.writeEntityToNBT(npcNbt);
		npc.writeToNBTOptional(npcNbt);
		Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
		if (animNpc instanceof EntityNPCInterface) {
			this.showNpc = (EntityNPCInterface) animNpc;
			this.showNpc.display.setShowName(1);
			MarkData.get(this.showNpc).marks.clear();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		AnimationController aData = AnimationController.getInstance();
		this.data.clear();
		char c = ((char) 167);
		String[][] hts = new String[aData.animations.size()][];
		int i = 0;
		for (int id : aData.animations.keySet()) {
			AnimationConfig ac = (AnimationConfig) aData.animations.get(id);
			String  t = "";
			switch(ac.type) {
				case ATTACKING: t = c + "cAT"; break;
				case DIES: t = c + "4D"; break;
				case FLY_STAND: t = c + "eSF"; break;
				case FLY_WALK: t = c + "6WF"; break;
				case INIT: t = c + "aI"; break;
				case JUMP: t = c + "bJ"; break;
				case WALKING: t = c + "6W"; break;
				case WATER_STAND: t = c + "cAT"; break;
				case WATER_WALK: t = c + "cAT"; break;
				default: t = c + "eS"; break; // STANDING or any
			}
			this.data.put(c + "8ID:" + c + "7" + id + c + "r " + ac.getName() + c + "7[" + t + c + "7]" , id);
			hts[i] = new String[] { new TextComponentTranslation("animation.type").appendSibling(new TextComponentTranslation("puppet."+ac.type.name().toLowerCase())).getFormattedText() };
			i++;
		}
		if (this.scroll == null) { this.scroll = new GuiCustomScroll(this, 0); }
		this.scroll.setListNotSorted(new ArrayList<String>(this.data.keySet()));
		this.scroll.hoversTexts = hts;
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		this.scroll.setSize(110, 178);
		this.addScroll(this.scroll);
		
		this.addLabel(new GuiNpcLabel(0, "puppet.animation", this.guiLeft+4, this.guiTop+4));
		
		this.addButton( new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 194, 80, 20, "gui.done"));
		this.addButton( new GuiNpcButton(1, this.guiLeft + 90, this.guiTop + 194, 80, 20, "gui.cancel"));
		this.addButton( new GuiNpcButton(2, this.guiLeft + 115, this.guiTop + 110, 57, 20, "gui.remove"));
		this.addButton( new GuiNpcButton(3, this.guiLeft + 115, this.guiTop + 98, 10, 10, new String[] { "b", "w" }, GuiNpcAnimation.backColor==0xFF000000 ? 0 : 1));
		
		this.resetAnim();
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0:
				this.cancelled = false;
				this.close();
				break;
			case 1:
				this.animation = null;
				this.cancelled = true;
				this.close();
				break;
			case 2:
				if (!this.data.containsKey(this.selected)) { return; }
				if (AnimationController.getInstance().removeAnimation(this.data.get(this.selected))) { this.initGui(); }
				this.animation = null;
				this.resetAnim();
				break;
			case 3: GuiNpcAnimation.backColor = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF: 0xFF000000; break;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.animation!=null && this.showNpc!=null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + 116.0f, this.guiTop + 5.0f, 1.0f);
			Gui.drawRect(-1, -1, 56, 91, 0xFFF080F0);
			Gui.drawRect(0, 0, 55, 90, GuiNpcAnimation.backColor);
			GlStateManager.popMatrix();
			this.drawNpc(this.showNpc, 143, 77, 1.0f, 0, 0, false);
		}
		if (this.getButton(0)!=null) { this.getButton(0).enabled = this.animation!=null; }
		if (this.getButton(2)!=null) { this.getButton(2).enabled = this.animation!=null; }
		if (this.getButton(3)!=null) { this.getButton(3).setVisible(this.animation!=null); }
	}

	@Override
	public void save() { }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (this.selected.equals(scroll.getSelected()) || !this.data.containsKey(scroll.getSelected())) { return; }
		this.selected = scroll.getSelected();
		this.animation = (AnimationConfig) AnimationController.getInstance().getAnimation(this.data.get(scroll.getSelected()));
		this.resetAnim();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.cancelled = false;
		this.close();
	}

	private void resetAnim() {
		if (this.getButton(0)!=null) { this.getButton(0).enabled = this.animation != null; }
		if (this.getButton(2)!=null) { this.getButton(2).enabled = this.animation != null; }
		if (this.animation==null) { return; }
		AnimationConfig ac = this.animation.copy();
		ac.isEdit = true;
		ac.disable = false;
		ac.type = AnimationKind.STANDING;
		
		if (this.showNpc==null) {
			NBTTagCompound npcNbt = new NBTTagCompound();
			this.npc.writeEntityToNBT(npcNbt);
			this.npc.writeToNBTOptional(npcNbt);
			Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
			if (animNpc instanceof EntityNPCInterface) {
				this.showNpc = (EntityNPCInterface) animNpc;
				this.showNpc.animation.clear();
			}
		}
		if (this.showNpc!=null) {
			((EntityNPCInterface) this.showNpc).display.setName("0_"+this.npc.getName());
			this.showNpc.animation.activeAnim = ac;
		}
	}
	
}
