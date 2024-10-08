package noppes.npcs.client.gui.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class SubGuiLoadAnimation extends SubGuiInterface implements ICustomScrollListener {

	public boolean cancelled;
	private GuiCustomScroll scroll;
	private final Map<String, Integer> data;
	private String selected;
	public AnimationConfig animation;
	private final EntityNPCInterface npcAnim;
	private final List<String> blackList;
	private final AnimationKind parentType;

	public SubGuiLoadAnimation(int id, EntityNPCInterface npc, Set<String> blackList, AnimationKind parentType) {
		this.npc = npc;
		this.id = id;
		this.cancelled = true;
		this.setBackground("smallbg.png");
		this.closeOnEsc = true;
		this.xSize = 176;
		this.ySize = 222;

		this.blackList = Lists.newArrayList();
		for (String key : blackList) {
			this.blackList.add(Util.instance.deleteColor(key));
		}
		this.parentType = parentType;
		this.data = Maps.newTreeMap();
		this.animation = null;
		this.selected = "";

		this.npcAnim = Util.instance.copyToGUI(npc, mc.world, false);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: {
				this.cancelled = false;
				this.close();
				break;
			}
			case 1: {
				this.animation = null;
				this.cancelled = true;
				this.close();
				break;
			}
			case 2: {
				if (!this.data.containsKey(this.selected)) {
					return;
				}
				if (AnimationController.getInstance().removeAnimation(this.data.get(this.selected))) {
					this.initGui();
				}
				this.animation = null;
				this.initGui();
				this.resetAnim();
				break;
			}
			case 3: {
				GuiNpcAnimation.backColor = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000;
				break;
			}
			case 4: {
				AnimationController aData = AnimationController.getInstance();
				this.animation = this.animation.copy();
				this.animation.id = aData.getUnusedAnimId();
				this.animation.immutable = false;
				this.animation.type = this.parentType;
				aData.animations.put(this.animation.id, this.animation);
				this.cancelled = false;
				this.close();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.animation != null && this.npcAnim != null) {
			npcAnim.field_20061_w = this.npc.field_20061_w;
			npcAnim.field_20062_v = this.npc.field_20062_v;
			npcAnim.field_20063_u = this.npc.field_20063_u;
			npcAnim.field_20064_t = this.npc.field_20064_t;
			npcAnim.field_20065_s = this.npc.field_20065_s;
			npcAnim.field_20066_r = this.npc.field_20066_r;
			npcAnim.ticksExisted = this.npc.ticksExisted;
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + 116.0f, this.guiTop + 5.0f, 1.0f);
			Gui.drawRect(-1, -1, 56, 91, 0xFFF080F0);
			Gui.drawRect(0, 0, 55, 90, GuiNpcAnimation.backColor);
			GlStateManager.popMatrix();
			this.drawNpc(this.npcAnim, 143, 77, 1.0f, 0, 0, 0);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.getButton(0) != null) {
			this.getButton(0).enabled = this.animation != null;
		}
		if (this.getButton(2) != null) {
			this.getButton(2).enabled = animation != null && !animation.immutable;
		}
		if (this.getButton(3) != null) {
			this.getButton(3).setVisible(this.animation != null);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		AnimationController aData = AnimationController.getInstance();
		this.data.clear();
		char c = ((char) 167);
		ArrayList<String> list = Lists.newArrayList();
		String[][] hts = new String[aData.animations.size()][];
		int i = 0;
		for (int id : aData.animations.keySet()) {
			AnimationConfig ac = aData.animations.get(id);
			String key = c + (parentType == ac.type ? "a" : "7") + ac.getSettingName();
			if (this.animation == null) {
				animation = ac;
				selected = key;
			}
			if (this.blackList.contains(Util.instance.deleteColor(key))) { continue; }
			this.data.put(key, id);
			list.add(key);
			hts[i] = new String[] { new TextComponentTranslation(ac.name).getFormattedText(), ((char) 167) + "7" + new TextComponentTranslation("gui.type").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + ac.type.name() };
			i++;
		}
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		this.scroll.setListNotSorted(list);
		this.scroll.hoversTexts = hts;
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		this.scroll.setSize(110, 178);
		if (!selected.isEmpty()) {
			this.scroll.setSelected(selected);
		}
		this.addScroll(this.scroll);

		this.addLabel(new GuiNpcLabel(0, "puppet.animation", this.guiLeft + 4, this.guiTop + 4));

		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 194, 80, 20, "gui.done"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 90, this.guiTop + 194, 80, 20, "gui.cancel"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 115, this.guiTop + 110, 57, 20, "gui.remove"));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 115, this.guiTop + 132, 57, 20, "gui.copy"));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 115, this.guiTop + 98, 10, 10, new String[] { "b", "w" }, GuiNpcAnimation.backColor == 0xFF000000 ? 0 : 1));
		this.resetAnim();
	}

	private void resetAnim() {
		if (this.animation == null) {
			return;
		}
		if (this.npcAnim != null) {
			AnimationConfig ac = this.animation.copy();

			this.npcAnim.display.setName("0_" + this.npc.getName());
			this.npcAnim.animation.setAnimation(ac, AnimationKind.EDITING);
			this.npcAnim.setHealth(this.npcAnim.getMaxHealth());
			this.npcAnim.deathTime = 0;
		}
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (this.selected.equals(scroll.getSelected()) || !this.data.containsKey(scroll.getSelected())) {
			return;
		}
		this.selected = scroll.getSelected();
		this.animation = (AnimationConfig) AnimationController.getInstance().getAnimation(this.data.get(scroll.getSelected()));
		this.resetAnim();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.cancelled = false;
		this.close();
	}

}
