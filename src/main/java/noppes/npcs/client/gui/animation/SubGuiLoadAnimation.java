package noppes.npcs.client.gui.animation;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiLoadAnimation
extends SubGuiInterface
implements ICustomScrollListener {

	public boolean cancelled;
	private GuiCustomScroll scroll;
	private Map<String, AnimationConfig> data;
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
		this.ySize = 200;
		
		this.data = Maps.<String, AnimationConfig>newTreeMap();
		this.animation = null;
		
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
				case attacking: t = c + "cAT"; break;
				case dies: t = c + "4D"; break;
				case flystand: t = c + "eSF"; break;
				case flywalk: t = c + "6WF"; break;
				case init: t = c + "aI"; break;
				case jump: t = c + "bJ"; break;
				case walking: t = c + "6W"; break;
				case waterstand: t = c + "cAT"; break;
				case waterwalk: t = c + "cAT"; break;
				default: t = c + "eS"; break; // standing or any
			}
			this.data.put(c + "8ID:" + c + "7" + id + c + "r " + ac.getName() + c + "7[" + t + c + "7]" , ac);
			hts[i] = new String[] { new TextComponentTranslation("animation.type").appendSibling(new TextComponentTranslation("puppet."+ac.type.name())).getFormattedText() };
			i++;
		}
		if (this.scroll == null) { this.scroll = new GuiCustomScroll(this, 0); }
		this.scroll.setListNotSorted(new ArrayList<String>(this.data.keySet()));
		this.scroll.hoversTexts = hts;
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		this.scroll.setSize(120, 156);
		this.addScroll(this.scroll);
		
		this.addLabel(new GuiNpcLabel(0, "puppet.animation", this.guiLeft+4, this.guiTop+4));
		
		this.addButton( new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 22, 80, 20, "gui.done"));
		this.addButton( new GuiNpcButton(1, this.guiLeft + 90, this.guiTop + 44, 80, 20, "gui.cancel"));
		this.resetAnim();
	}

	@Override
	public void buttonEvent(GuiButton button) {
		if (button.id == 0) { this.cancelled = false; }
		this.close();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void save() { }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (this.selected.equals(scroll.getSelected()) || !this.data.containsKey(scroll.getSelected())) { return; }
		this.selected = scroll.getSelected();
		this.animation = this.data.get(scroll.getSelected());
		this.resetAnim();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.cancelled = false;
		this.close();
	}

	private void resetAnim() {
		if (this.animation==null) { return; }
		AnimationConfig ac = this.animation.copy();
		ac.type = EnumAnimationType.standing;
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
