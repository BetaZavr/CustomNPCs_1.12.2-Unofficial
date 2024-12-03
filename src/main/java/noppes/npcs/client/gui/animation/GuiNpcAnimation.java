package noppes.npcs.client.gui.animation;

import java.util.*;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
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
import noppes.npcs.util.Util;
import noppes.npcs.util.CustomNPCsScheduler;

public class GuiNpcAnimation
		extends GuiNPCInterface2
		implements ISubGuiListener, ICustomScrollListener, IGuiData, GuiYesNoCallback {

	public static int backColor = 0xFF000000;

	private GuiCustomScroll scrollType;
	private GuiCustomScroll scrollAnimations;
	private GuiCustomScroll scrollAllAnimations;

	private boolean isChanged = true;
	private final String[][] typeHovers;
	private final Map<String, AnimationKind> dataType = new LinkedHashMap<>();
	private final List<String> dataAnimations = new ArrayList<>();
	private final Map<String, AnimationConfig> dataAllAnimations = new TreeMap<>();

	private final EntityNPCInterface npcAnim;
	private final DataAnimation animation;
	private String selType = "";
	private String selAnim = "";
	private String selBaseAnim = "";
	private AnimationController aData;

	public GuiNpcAnimation(EntityCustomNpc npc) {
		super(npc, 4);
		closeOnEsc = true;
		animation = new DataAnimation(npc);
		setBackground("bgfilled.png");

		dataType.clear();
		dataType.put("puppet." + AnimationKind.INIT.name().toLowerCase().replace("_", ""), AnimationKind.INIT);
		dataType.put("puppet." + AnimationKind.JUMP.name().toLowerCase().replace("_", ""), AnimationKind.JUMP);
		dataType.put("puppet." + AnimationKind.ATTACKING.name().toLowerCase().replace("_", ""), AnimationKind.ATTACKING);
		dataType.put("puppet." + AnimationKind.SHOOT.name().toLowerCase().replace("_", ""), AnimationKind.SHOOT);
		dataType.put("puppet." + AnimationKind.AIM.name().toLowerCase().replace("_", ""), AnimationKind.AIM);
		dataType.put("puppet." + AnimationKind.SWING.name().toLowerCase().replace("_", ""), AnimationKind.SWING);
		dataType.put("puppet." + AnimationKind.HIT.name().toLowerCase().replace("_", ""), AnimationKind.HIT);
		dataType.put("puppet." + AnimationKind.DIES.name().toLowerCase().replace("_", ""), AnimationKind.DIES);
		dataType.put("puppet." + AnimationKind.BASE.name().toLowerCase().replace("_", ""), AnimationKind.BASE);
		dataType.put("puppet." + AnimationKind.INTERACT.name().toLowerCase().replace("_", ""), AnimationKind.INTERACT);
		dataType.put("puppet." + AnimationKind.BLOCKED.name().toLowerCase().replace("_", ""), AnimationKind.BLOCKED);
		dataType.put("puppet." + AnimationKind.STANDING.name().toLowerCase().replace("_", ""), AnimationKind.STANDING);
		dataType.put("puppet." + AnimationKind.FLY_STAND.name().toLowerCase().replace("_", ""), AnimationKind.FLY_STAND);
		dataType.put("puppet." + AnimationKind.WATER_STAND.name().toLowerCase().replace("_", ""), AnimationKind.WATER_STAND);
		dataType.put("puppet." + AnimationKind.REVENGE_STAND.name().toLowerCase().replace("_", ""), AnimationKind.REVENGE_STAND);
		dataType.put("puppet." + AnimationKind.WALKING.name().toLowerCase().replace("_", ""), AnimationKind.WALKING);
		dataType.put("puppet." + AnimationKind.FLY_WALK.name().toLowerCase().replace("_", ""), AnimationKind.FLY_WALK);
		dataType.put("puppet." + AnimationKind.WATER_WALK.name().toLowerCase().replace("_", ""), AnimationKind.WATER_WALK);
		dataType.put("puppet." + AnimationKind.REVENGE_WALK.name().toLowerCase().replace("_", ""), AnimationKind.REVENGE_WALK);
		typeHovers = new String[dataType.size()][];
		int i = 0;
		for (AnimationKind ak : dataType.values()) {
			String hoverText = new TextComponentTranslation("animation.hover.anim." + ak.get()).getFormattedText();
			typeHovers[i] = hoverText.split("<br>");
			i++;
		}

		npcAnim = Util.instance.copyToGUI(npc, mc.world, false);
		npcAnim.display.setName(npc.getName()+"_animation");

		Client.sendData(EnumPacketServer.AnimationGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		AnimationConfig anim = getAnim();
		switch (button.id) {
			case 0: { // add anim
				if (scrollType == null || !scrollType.hasSelected()) { return; }
				AnimationConfig newAnim = (AnimationConfig) aData.createNewAnim();
				newAnim.name = Util.instance.deleteColor(new TextComponentTranslation(selType).getFormattedText().replaceAll(" ", "_")+ "_" + newAnim.id);
				newAnim.type = dataType.get(selType);
				animation.data.get(newAnim.type).add(newAnim.id);
				selAnim = newAnim.getSettingName();
				isChanged = true;
				initGui();
				CustomNPCsScheduler.runTack(() -> setSubGui(new SubGuiEditAnimation(npc, newAnim, 4, this)), 50);
				break;
			}
			case 1: { // copy anim
				if (anim == null) { return; }
				AnimationController aData = AnimationController.getInstance();
				anim = anim.copy();
				anim.id = aData.getUnusedAnimId();
				anim.immutable = false;
				anim.type = dataType.get(selType);
				aData.animations.put(anim.id, anim);
				selAnim = anim.getSettingName();
				selBaseAnim = anim.getSettingName();
				if (dataType.containsKey(selType)) {
					animation.data.get(dataType.get(selType)).add(anim.id);
				}
				isChanged = true;
				initGui();
				break;
			}
			case 2: { // del anim
				if (anim == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this, anim.getSettingName(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 3: { // edit
				if (anim == null || !dataType.containsKey(selType)) { return; }
				setSubGui(new SubGuiEditAnimation(npc, anim, 4, this));
				break;
			}
			case 4: { // back color
				GuiNpcAnimation.backColor = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000;
				break;
			}
		}
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result) {
			return;
		}
		AnimationConfig anim = getAnim();
		if (id == 0) {
			if (anim == null) { return; }
			if (dataType.containsKey(selType)) {
				animation.data.get(dataType.get(selType)).remove(Integer.valueOf(anim.id));
			}
			AnimationController.getInstance().removeAnimation(anim.id);
			isChanged = true;
			initGui();
		}
	}

	@Override
	public void close() {
		save();
		CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void save() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("save", true);
		Client.sendData(EnumPacketServer.EmotionChange, nbt);
		Client.sendData(EnumPacketServer.AnimationSave, animation.save(new NBTTagCompound()));
		CustomNPCsScheduler.runTack(() -> Client.sendData(EnumPacketServer.AnimationSave, animation.save(new NBTTagCompound())), 500);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui != null) {
			subgui.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		AnimationConfig anim = getAnim();
		if (anim != null && !hasSubGui() && npcAnim != null) {
			npcAnim.animation.updateTime();
			npcAnim.field_20061_w = npc.field_20061_w;
			npcAnim.field_20062_v = npc.field_20062_v;
			npcAnim.field_20063_u = npc.field_20063_u;
			npcAnim.field_20064_t = npc.field_20064_t;
			npcAnim.field_20065_s = npc.field_20065_s;
			npcAnim.field_20066_r = npc.field_20066_r;
			npcAnim.ticksExisted = npc.ticksExisted;
			npcAnim.deathTime = 0;
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft + 342.0f, guiTop + 9.0f, 0.0f);
			Gui.drawRect(-1, -1, 56, 91, 0xFFF080F0);
			Gui.drawRect(0, 0, 55, 90, GuiNpcAnimation.backColor);
			GlStateManager.popMatrix();
			drawNpc(npcAnim, 369, 81, 1.0f, 0, 0, 0);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft + 315.0f, guiTop - 2.0f, 0.0f);
		int color = 0xA0000000;
		int hoverButton = -1;
		for (int i = 0; i < 5; i++) {
			GlStateManager.translate(0.0f, 22.0f, 0.0f);
			int c = color;
			if (getButton(i) != null && getButton(i).isMouseOver()) {
				c = 0xA0FFFF00;
				hoverButton = i;
			}
			drawHorizontalLine(0, 0, 0, color);
			drawHorizontalLine(0, 1, 1, color);
			drawHorizontalLine(0, 2, 2, color);
			drawHorizontalLine(0, 3, 3, color);
			drawHorizontalLine(0, 0, 4, color);
			drawHorizontalLine(1, 14, 4, c);
			drawHorizontalLine(0, 3, 5, color);
			drawHorizontalLine(0, 2, 6, color);
			drawHorizontalLine(0, 1, 7, color);
			drawHorizontalLine(0, 0, 8, color);
		}
		GlStateManager.translate(11.0f, -10.0f, 0.0f);
		drawVerticalLine(0, 0, 2, color);
		drawVerticalLine(1, -1, 2, color);
		drawVerticalLine(2, -2, 2, color);
		drawVerticalLine(3, -3, 2, color);
		drawVerticalLine(4, 0, 2, color);
		if (hoverButton != -1) {
			drawVerticalLine(4, -75 + hoverButton * 22, 1, 0xA0FFFF00);
			if (hoverButton != 0) {
				drawVerticalLine(4, -75, -74 + hoverButton * 22, color);
			}
		}
		else { drawVerticalLine(4, -75, 1, color); }
		drawVerticalLine(5, -3, 2, color);
		drawVerticalLine(6, -2, 2, color);
		drawVerticalLine(7, -1, 2, color);
		drawVerticalLine(8, 0, 2, color);
		GlStateManager.popMatrix();

		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (scrollAnimations != null && scrollAnimations.hover != -1) { // scroll anim
			setHoverText(new TextComponentTranslation("animation.hover.anim.list").getFormattedText());
		} else if (getButton(1) != null && getButton(1).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.anim.copy").getFormattedText());
		} else if (getButton(0) != null && getButton(0).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.anim.create").getFormattedText());
		} else if (getButton(2) != null && getButton(2).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.anim.del").getFormattedText());
		} else if (getButton(3) != null && getButton(3).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.anim.edit").getFormattedText());
		}
		if (hoverText != null) {
			drawHoveringText(Arrays.asList(hoverText), mouseX, mouseY, fontRenderer);
			hoverText = null;
		}
	}

	private AnimationConfig getAnim() {
		if (!dataAnimations.contains(selAnim) && !selAnim.isEmpty()) { selAnim = ""; }
		if (!selAnim.isEmpty() && dataAllAnimations.containsKey(selAnim)) {
			return dataAllAnimations.get(selAnim);
		}
		if (selBaseAnim.isEmpty() || !dataAllAnimations.containsKey(selBaseAnim)) {
			return null;
		}
		return dataAllAnimations.get(selBaseAnim);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 8;
		int y = guiTop + 14;
		if (scrollType == null) {
			(scrollType = new GuiCustomScroll(this, 0)).setSize(120, 198);
			scrollType.setListNotSorted(new ArrayList<>(dataType.keySet()));
			scrollType.hoversTexts = typeHovers;
		}
		scrollType.guiLeft = x;
		scrollType.guiTop = y;
		addScroll(scrollType);

		if (selType.isEmpty()) {
			for (String key : dataType.keySet()) {
				if (dataType.get(key) == AnimationKind.STANDING) {
					selType = key;
					break;
				}
			}
		}
		scrollType.setSelected(selType);
		addLabel(new GuiNpcLabel(0, "animation.type", x + 1, y - 10));

		x += 123;
		dataAnimations.clear();
		dataAllAnimations.clear();
		aData = AnimationController.getInstance();
		ArrayList<String> allAnimations = Lists.newArrayList();
		String[][] hts = new String[aData.animations.size()][];

		int i = 0;
		AnimationKind type = dataType.get(selType);
		if (!selAnim.isEmpty() && !selBaseAnim.isEmpty() && !Util.instance.deleteColor(selAnim).equals(Util.instance.deleteColor(selBaseAnim))) { selAnim = ""; }
		for (AnimationConfig ac : aData.animations.values()) {
			String key = ((char) 167) + (type == ac.type ? "a" : "7") + ac.getSettingName();
			if (animation.data.get(type).contains(ac.id)) { dataAnimations.add(key); }
			dataAllAnimations.put(key, ac);
			if (!selAnim.isEmpty() && Util.instance.deleteColor(selAnim).equals(Util.instance.deleteColor(key))) { selAnim = key; }
			if (!selBaseAnim.isEmpty() && Util.instance.deleteColor(selBaseAnim).equals(Util.instance.deleteColor(key))) { selBaseAnim = key; }
			allAnimations.add(key);
			hts[i] = new String[] { new TextComponentTranslation(ac.name).getFormattedText(), ((char) 167) + "7" + new TextComponentTranslation("gui.type").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + ac.type.name() };
			i++;
		}

		if (scrollAnimations == null) {
			(scrollAnimations = new GuiCustomScroll(this, 1)).setSize(120, 198);
		}
		scrollAnimations.setListNotSorted(dataAnimations);
		scrollAnimations.guiLeft = x;
		scrollAnimations.guiTop = y;
		addScroll(scrollAnimations);

		if (selAnim.isEmpty() && selBaseAnim.isEmpty() && !scrollAnimations.getList().isEmpty()) {
			for (String key : scrollAnimations.getList()) {
				selAnim = key;
				selBaseAnim = key;
				break;
			}
		}
		if (!selAnim.isEmpty()) {
			scrollAnimations.setSelected(selAnim);
			if (!scrollAnimations.hasSelected()) { selAnim = ""; }
			else { selAnim = scrollAnimations.getSelected(); }
		} else {
			scrollAnimations.setSelected(null);
		}
		if (selBaseAnim.isEmpty()) { selBaseAnim = selAnim; }

		x += 123;
		if (scrollAllAnimations == null) {
			(scrollAllAnimations = new GuiCustomScroll(this, 2)).setSize(160, 110);
		}
		scrollAllAnimations.setListNotSorted(allAnimations);
		scrollAllAnimations.hoversTexts = hts;
		scrollAllAnimations.guiLeft = x;
		scrollAllAnimations.guiTop = y + 88;
		if (!selBaseAnim.isEmpty()) {
			scrollAllAnimations.setSelected(selBaseAnim);
		}
		this.addScroll(scrollAllAnimations);

		AnimationConfig anim = getAnim();
		boolean isOP = anim != null && (!anim.immutable || player.getName().equals("BetaZavr"));
		addLabel(new GuiNpcLabel(1, new TextComponentTranslation("movement.animation").getFormattedText() + ":", x + 1, y - 10));
		addButton(new GuiNpcButton(0, x, y, 60, 20, "markov.generate"));
		addButton(new GuiNpcButton(4, x + 148, y, 10, 10, new String[] { "b", "w" }, backColor == 0xFF000000 ? 0 : 1));
		addButton(new GuiNpcButton(1, x, y += 22, 60, 20, "gui.copy"));
		getButton(1).enabled = anim != null;
		addButton(new GuiNpcButton(2, x, y += 22, 60, 20, "gui.remove"));
		getButton(2).enabled = isOP;
		addButton(new GuiNpcButton(3, x, y + 22, 60, 20, "gui.edit"));
		getButton(3).enabled = isOP;
		resetAnimation();
	}

	private void resetAnimation() {
		if (!isChanged) { return; }
		AnimationConfig anim = getAnim();
		if (anim == null || npcAnim == null) { return; }
		anim = anim.copy();
		anim.type = dataType.get(selType);
		npcAnim.animation.clear();
		npcAnim.animation.setAnimation(anim, AnimationKind.EDITING_All);
		npcAnim.setHealth(npcAnim.getMaxHealth());
		npcAnim.deathTime = 0;
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.id == 0) { // animation Type
			if (selType.equals(scroll.getSelected())) { return; }
			selType = scroll.getSelected();
			isChanged = true;
		}
		else if (scroll.id == 1) { // animation in type
			if (selAnim.equals(scroll.getSelected())) { return; }
			selAnim = scroll.getSelected();
			scrollAllAnimations.setSelected(selAnim);
			selBaseAnim = scrollAllAnimations.getSelected();
			isChanged = true;
		}
		else if (scroll.id == 2) { // animation in base
			if (selBaseAnim.equals(scroll.getSelected())) { return; }
			selBaseAnim = scroll.getSelected();
			if (scrollAnimations.hasSelected(selBaseAnim)) {
				scrollAnimations.setSelected(selBaseAnim);
				selAnim = scrollAnimations.getSelected();
			}
			isChanged = true;
		}
		if (isChanged) { initGui(); }
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		AnimationConfig anim;
		if (scroll.id == 1) {
			anim = getAnim();
			if (anim == null) { return; }
			AnimationKind type = dataType.get(selType);
			if (animation.data.get(type).contains(anim.id)) {
				for (Integer id : animation.data.get(type)) {
					if (id == anim.id) {
						animation.data.get(type).remove(id);
						isChanged = true;
						initGui();
						return;
					}
				}
			}
		}
		else if (scroll.id == 2) {
			if (scrollAnimations.hasSelected(selBaseAnim)) { return; }
			anim = dataAllAnimations.get(selBaseAnim);
			if (anim == null) { return; }
			selAnim = anim.getSettingName();
			selBaseAnim = anim.getSettingName();
			AnimationKind type = dataType.get(selType);
			if (!animation.data.get(type).contains(anim.id)) { animation.data.get(type).add(anim.id); }
			isChanged = true;
			initGui();
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		animation.load(compound);
		isChanged = true;
		initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		AnimationConfig anim = getAnim();
		AnimationKind type = dataType.get(selType);
		if (subgui.id == 4) { // new
			displayGuiScreen(this);
			if (!(subgui instanceof SubGuiEditAnimation) || ((SubGuiEditAnimation) subgui).anim == null) {
				return;
			}
			selAnim = ((SubGuiEditAnimation) subgui).anim.getSettingName();
			if (!animation.data.containsKey(type)) { animation.data.put(type, new ArrayList<>()); }
			if (anim != null && !animation.data.get(type).contains(anim.id)) {
				animation.data.get(type).add(anim.id);
			}
			Client.sendData(EnumPacketServer.AnimationChange, ((SubGuiEditAnimation) subgui).anim.save());
			isChanged = true;
			initGui();
		}
	}

}
