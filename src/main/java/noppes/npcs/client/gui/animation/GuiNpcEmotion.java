package noppes.npcs.client.gui.animation;

import java.util.*;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.EmotionFrame;
import noppes.npcs.client.model.part.ModelEyeData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.util.Util;
import noppes.npcs.util.CustomNPCsScheduler;

public class GuiNpcEmotion
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener, IGuiData, ITextfieldListener, ISliderListener, GuiYesNoCallback {

	public static final ResourceLocation etns = new ResourceLocation(CustomNpcs.MODID, "textures/gui/emotion/buttons.png");

	private final DataAnimation animation;
	private GuiCustomScroll scroll;
	private final Map<String, EmotionConfig> dataEmtns = new TreeMap<>();
	private String selEmtn;
	public EntityNPCInterface npcEmtn;
	private AnimationController aData;
	private ScaledResolution sw;
	public EmotionFrame frame;
	public int toolType = 0; // 0 - rotation, 1 - offset, 2 - scale
	public int elementType = 0; // 0 - eye, 1 - pupil, 2 - brow, 3 - mouth
	public boolean isRight = true;
	private ModelEyeData modelEye;
	private final String[] types = new String[] { "gui.small", "gui.normal", "gui.select" };

	public GuiNpcEmotion(EntityCustomNpc npc) {
		super(npc, 4);
		closeOnEsc = true;
		setBackground("bgfilled.png");

		animation = new DataAnimation(npc);
		animation.setBaseEmotionId(npc.animation.getBaseEmotionId());

		selEmtn = "";
		npcEmtn = Util.instance.copyToGUI(npc, mc.world, false);
		Client.sendData(EnumPacketServer.AnimationGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		EmotionConfig emtn = getEmtn();
		switch (button.id) {
			case 0: { // create new
				setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 1: { // del
				if (emtn == null || !dataEmtns.containsKey(scroll.getSelected())) { return; }
				if (aData.removeEmotion(emtn.id)) { selEmtn = ""; }
				initGui();
				break;
			}
			case 2: { // back color
				GuiNpcAnimation.backColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000);
				button.layerColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
				break;
			}
			case 3: { // select frame
				if (emtn == null || !emtn.frames.containsKey(button.getValue())) { return; }
				frame = emtn.frames.get(button.getValue());
				initGui();
				break;
			}
			case 4: { // add frame
				if (emtn == null) { return; }
				frame = (EmotionFrame) emtn.addFrame(frame);
				initGui();
				break;
			}
			case 5: { // del frame
				if (frame == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 6: { // clear frame
				if (frame == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.clearMessage").getFormattedText(),
						GuiScreen.isShiftKeyDown() ? 4 : 1);
				displayGuiScreen(guiyesno);
				break;
			}
			case 11: { // smooth
				if (emtn == null || frame == null) { return; }
				frame.setSmooth(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					for (EmotionFrame f : emtn.frames.values()) {
						f.setSmooth(frame.isSmooth());
					}
				}
				resetEmtns();
				break;
			}
			case 22: { // element eye
				if (elementType == 3) { return; }
				elementType = 3;
				initGui();
				break;
			}
			case 23: { // tool pos
				if (toolType == 1) { return; }
				toolType = 1;
				initGui();
				break;
			}
			case 24: { // tool rot
				if (toolType == 0) { return; }
				toolType = 0;
				initGui();
				break;
			}
			case 25: { // tool scale
				if (toolType == 2) { return; }
				toolType = 2;
				initGui();
				break;
			}
			case 26: { // element eye
				if (elementType == 0) { return; }
				elementType = 0;
				initGui();
				break;
			}
			case 27: { // element pupil
				if (elementType == 1) { return; }
				elementType = 1;
				initGui();
				break;
			}
			case 28: { // element brow
				if (elementType == 2) { return; }
				elementType = 2;
				initGui();
				break;
			}
			case 29: { // element brow
				isRight = button.getValue() == 0;
				initGui();
				break;
			}
			case 30: { // reset X
				if (frame == null) { return; }
				switch (toolType) {
					case 0: {
						switch (elementType) {
							case 0: frame.rotEye[isRight ? 0 : 1] = 0.5f; break;
							case 1: frame.rotPupil[isRight ? 0 : 1] = 0.5f; break;
							case 2: frame.rotBrow[isRight ? 0 : 1] = 0.5f; break;
							case 3: frame.rotMouth = 0.5f; break;
						}
						break;
					}
					case 1: {
						switch (elementType) {
							case 0: frame.offsetEye[isRight ? 0 : 2] = 0.5f; break;
							case 1: frame.offsetPupil[isRight ? 0 : 2] = 0.5f; break;
							case 2: frame.offsetBrow[isRight ? 0 : 2] = 0.5f; break;
							case 3: frame.offsetMouth[0] = 0.5f; break;
						}
						break;
					}
					case 2: {
						switch (elementType) {
							case 0: frame.scaleEye[isRight ? 0 : 2] = 0.5f; break;
							case 1: frame.scalePupil[isRight ? 0 : 2] = 0.5f; break;
							case 2: frame.scaleBrow[isRight ? 0 : 2] = 0.5f; break;
							case 3: frame.scaleMouth[0] = 0.5f; break;
						}
						break;
					}
				}
				initGui();
				break;
			}
			case 31: { // reset part set Y
				if (frame == null) { return; }
				switch (toolType) {
					case 1:
						switch (elementType) {
							case 0: frame.offsetEye[isRight ? 1 : 3] = 0.5f; break;
							case 1: frame.offsetPupil[isRight ? 1 : 3] = 0.5f; break;
							case 2: frame.offsetBrow[isRight ? 1 : 3] = 0.5f; break;
							case 3: frame.offsetMouth[1] = 0.5f; break;
						}
						break;
					case 2:
						switch (elementType) {// 0 - eye, 1 - pupil, 2 - brow
							case 0: frame.scaleEye[isRight ? 1 : 3] = 0.5f; break;
							case 1: frame.scalePupil[isRight ? 1 : 3] = 0.5f; break;
							case 2: frame.scaleBrow[isRight ? 1 : 3] = 0.5f; break;
							case 3: frame.scaleMouth[1] = 0.5f; break;
						}
						break;
				}
				initGui();
				break;
			}
			case 32: { // reset part set Y
				if (modelEye == null) { return; }
				modelEye.type = (byte) button.getValue();
				break;
			}
			case 33: { // set base emotion
				if (animation == null || !dataEmtns.containsKey(selEmtn)) { return; }
				animation.setBaseEmotionId(dataEmtns.get(selEmtn).id);
				initGui();
				break;
			}
			case 34: { // del base emotion
				if (animation == null) { return; }
				animation.setBaseEmotionId(-1);
				initGui();
				break;
			}
			case 35: { // start blink
				if (frame == null) { return; }
				frame.setBlink(((GuiNpcCheckBox) button).isSelected());
				initGui();
				break;
			}
			case 36: { // end blink
				if (frame == null) { return; }
				frame.setEndBlink(((GuiNpcCheckBox) button).isSelected());
				initGui();
				break;
			}
			case 37: { // can blink
				if (emtn == null) { return; }
				emtn.setCanBlink(((GuiNpcCheckBox) button).isSelected());
				initGui();
				break;
			}
			case 38: { // disable pupil
				if (frame == null) { return; }
				frame.setDisable(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown() && emtn != null) { // Shift pressed
					for (EmotionFrame f : emtn.frames.values()) {
						f.setDisable(frame.isDisabled());
					}
				}
				initGui();
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		displayGuiScreen(this);
		if (!result) {
			return;
		}
		EmotionConfig emtn = getEmtn();
		switch (id) {
			case 0: { // remove frame
				if (emtn == null || frame == null || emtn.frames.size() <= 1) {
					return;
				}
				int f = frame.id - 1;
				if (f < 0) {
					f = 0;
				}
				emtn.removeFrame(frame);
				frame = emtn.frames.get(f);
				initGui();
				break;
			}
			case 1: { // clear frame
				if (frame == null) { return; }
				frame.clear();
				initGui();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		sw = new ScaledResolution(mc);
		int lId = 0;
		int x = guiLeft + 8;
		int y = guiTop + 14;
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(120, 177); }
		scroll.guiLeft = x;
		scroll.guiTop = y;
		addScroll(scroll);

		addLabel(new GuiNpcLabel(lId++, "emotion.list", x + 1, y - 10));

		dataEmtns.clear();
		aData = AnimationController.getInstance();
		for (EmotionConfig ec : aData.emotions.values()) { dataEmtns.put(ec.getSettingName(), ec); }
		scroll.setListNotSorted(new ArrayList<>(dataEmtns.keySet()));
		if (!selEmtn.isEmpty()) {
			if (scroll.getList().contains(selEmtn)) { scroll.setSelected(selEmtn); }
			else { selEmtn = ""; }
		}
		if (selEmtn.isEmpty() && !dataEmtns.isEmpty()) {
			for (String key : dataEmtns.keySet()) {
				selEmtn = key;
				scroll.setSelected(selEmtn);
				frame = dataEmtns.get(key).frames.get(0);
				break;
			}
		}
		addButton(new GuiNpcButton(0, x, y + scroll.height + 1, 59, 20, "markov.generate"));
		getButton(0).enabled = dataEmtns.isEmpty();
		addButton(new GuiNpcButton(1, x + 62, y + scroll.height + 1, 59, 20, "gui.remove"));
		getButton(1).enabled = !selEmtn.isEmpty();

		EmotionConfig emtn = getEmtn();
		if (emtn == null) { return; }
		if (frame == null) {
			frame = emtn.frames.get(0);
			if (frame == null) { return; }
		}
		int wX = guiLeft + 274;
		int wY = guiTop + 71;

		// Back color
		GuiNpcButton button = new GuiNpcButton(2, wX, wY - 13, 8, 8, "");
		button.layerColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		// Tool type
		button = new GuiNpcButton(23, wX + 10, wY - 16, 14, 14, ""); // tool pos
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 1 ? 0xFFFF8080 : 0xFFFFFFFF;
		button.setHoverText("animation.hover.tool.0");
		addButton(button);
		button = new GuiNpcButton(24, wX + 26, wY - 16, 14, 14, ""); // tool rot
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 24;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 0 ? 0xFF80FF80 : 0xFFFFFFFF;
		button.setHoverText("animation.hover.tool.1");
		addButton(button);
		button = new GuiNpcButton(25, wX + 42, wY - 16, 14, 14, ""); // tool scale
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 48;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 2 ? 0xFF8080FF : 0xFFFFFFFF;
		button.setHoverText("animation.hover.tool.2");
		addButton(button);

		button = new GuiNpcButton(26, wX + 78, wY - 16, 14, 14, ""); // element eye
		button.texture = etns;
		button.hasDefBack = false;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = elementType == 0 ? 0xFFFF8080 : 0xFFFFFFFF;
		addButton(button);
		button = new GuiNpcButton(27, wX + 94, wY - 16, 14, 14, ""); // element pupil
		button.texture = etns;
		button.hasDefBack = false;
		button.txrX = 24;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = elementType == 1 ? 0xFF80FF80 : 0xFFFFFFFF;
		addButton(button);
		button = new GuiNpcButton(28, wX + 110, wY - 16, 14, 14, ""); // element brow
		button.texture = etns;
		button.hasDefBack = false;
		button.txrX = 48;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = elementType == 2 ? 0xFF8080FF : 0xFFFFFFFF;
		addButton(button);
		button = new GuiNpcButton(22, wX + 126, wY - 16, 14, 14, ""); // element mouth
		button.texture = etns;
		button.hasDefBack = false;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = elementType == 3 ? 0xFFFFFF80 : 0xFFFFFFFF;
		addButton(button);

		button = new GuiButtonBiDirectional(32, wX + 58, wY - 48, 82, 14, types, (modelEye == null) ? 0 : modelEye.type);
		addButton(button);

		button = new GuiNpcButton(33, wX, y - 10, 70, 14, "gui.set");
		button.setEnabled(animation.getBaseEmotionId() != emtn.id);
		addButton(button);
		button = new GuiNpcButton(34, wX + 72, y - 10, 70, 14, "gui.remove");
		button.setEnabled(animation.getBaseEmotionId() >= 0);
		addButton(button);

		button = new GuiNpcButton(29, wX + 58, wY - 32, 82, 14, new String[] { "gui.right", "gui.left" }, isRight ? 0 : 1);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		// Name
		x += scroll.width + 2;
		y = guiTop + 14;
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.name").getFormattedText() + ":", x + 1, y - 10));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, x + 1, y, 135, 12, emtn.name);
		addTextField(textField);

		// Frame
		addLabel(new GuiNpcLabel(lId++, "animation.frames", x, (y += 26) - 10));
		List<String> lFrames = new ArrayList<>();
		for (int i = 0; i < emtn.frames.size(); i++) { lFrames.add((i + 1) + "/" + emtn.frames.size()); }
		button = new GuiButtonBiDirectional(3, x, y, 60, 14, lFrames.toArray(new String[0]), emtn.id);
		button.setEnabled(emtn.frames.size() > 1);
		addButton(button);
		button = new GuiNpcButton(4, x + 62, y + 2, 10, 10, ""); // add frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		button = new GuiNpcButton(5, x + 74, y + 2, 10, 10, ""); // del frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = emtn.frames.size() > 1;
		addButton(button);
		button = new GuiNpcButton(6, x + 126, y + 2, 10, 10, ""); // clear frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);

		y += 17;
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.time").getFormattedText() + ":", x, y + 2));
		textField = new GuiNpcTextField(1, this, x + 45, y, 43, 12, "" + frame.getSpeed());
		textField.setMinMaxDefault(0, 3600, frame.getSpeed());
		addTextField(textField);
		textField = new GuiNpcTextField(2, this, x + 92, y, 43, 12, "" + frame.getEndDelay());
		textField.setMinMaxDefault(0, 3600, frame.getEndDelay());
		addTextField(textField);

		y += 13;
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.repeat").getFormattedText() + ":", x, y + 5));
		if (emtn.repeatLast < 0) { emtn.repeatLast *= -1; }
		if (emtn.repeatLast < 0 || emtn.repeatLast > emtn.frames.size()) { emtn.repeatLast = emtn.frames.size(); }
		textField = new GuiNpcTextField(3, this, x + 45, y + 3, 43, 12, "" + emtn.repeatLast);
		textField.setMinMaxDefault(0, emtn.frames.size(), emtn.repeatLast);
		addTextField(textField);
		addButton(new GuiNpcCheckBox(11, x + 92, y + 1, 45, 14, "gui.smooth", "gui.linearly", frame.isSmooth()));

		y += 16;
		addButton(new GuiNpcCheckBox(38, x, y, 140, 14, "emotion.part.disable", null, frame.isDisabled()));
		addButton(button);
		resetEmtns();

		y += 15;
		addButton(new GuiNpcCheckBox(35, x, y, 140, 14, "emotion.blink", "emotion.no.blink", frame.isBlink()));

		y += 15;
		if (frame.isBlink()) {
			addButton(new GuiNpcCheckBox(36, x, y, 140, 14, "emotion.end.blink", null, frame.isEndBlink()));
		}

		// Tool sliders
		y += 19;
		int f = 18;
		for (int i = 0; i < 2; i++) {
			if (toolType == 0 && i == 1) { break; }
			addLabel(new GuiNpcLabel(lId++, i == 0 ? "X:" : "Y:", x, y + i * f + 4));
			float[] values;
			switch(elementType) {
				case 1: { // pupil
					switch(toolType) { // 0 - rotation, 1 - offset, 2 - scale
						case 1: {
							values = new float[] { frame.offsetPupil[isRight ? 0 : 2], frame.offsetPupil[isRight ? 1 : 3] };
							break;
						}
						case 2: {
							values = new float[] { frame.scalePupil[isRight ? 0 : 2], frame.scalePupil[isRight ? 1 : 3] };
							break;
						}
						default: {
							values = new float[] { frame.rotPupil[isRight ? 0 : 1] };
							break;
						}
					}
					break;
				}
				case 2: { // brow
					switch(toolType) {
						case 1: {
							values = new float[] { frame.offsetBrow[isRight ? 0 : 2], frame.offsetBrow[isRight ? 1 : 3] };
							break;
						}
						case 2: {
							values = new float[] { frame.scaleBrow[isRight ? 0 : 2], frame.scaleBrow[isRight ? 1 : 3] };
							break;
						}
						default: {
							values = new float[] { frame.rotBrow[isRight ? 0 : 1] };
							break;
						}
					}
					break;
				}
				case 3: { // mouth
					switch(toolType) {
						case 1: {
							values = frame.offsetMouth;
							break;
						}
						case 2: {
							values = frame.scaleMouth;
							break;
						}
						default: {
							values = new float[] { frame.rotMouth };
							break;
						}
					}
					break;
				}
				default: { // eye
					switch(toolType) {
						case 1: {
							values = new float[] { frame.offsetEye[isRight ? 0 : 2], frame.offsetEye[isRight ? 1 : 3] };
							break;
						}
						case 2: {
							values = new float[] { frame.scaleEye[isRight ? 0 : 2], frame.scaleEye[isRight ? 1 : 3] };
							break;
						}
						default: {
							values = new float[] { frame.rotEye[isRight ? 0 : 1] };
							break;
						}
					}
					break;
				}
			}

			float[] datas = new float[2];
			double m, n;
			if (toolType == 1) { // offset
				m = -0.5d;
				n = 0.5d;
				for (int j = 0; j < 2; j++) {
					datas[j] = (float) (Math.round((values[i] - 0.5f) * 1000.0f) / 1000.0d);
				}
			} else if (toolType == 2) { // scale
				m = 0.5d;
				n = 1.5d;
				for (int j = 0; j < 2; j++) {
					datas[j] = (float) (Math.round((values[i] + 0.5f) * 1000.0f) / 1000.0d);
				}
			} else { // rotation
				m = -180.0d;
				n = 180.0d;
				for (int j = 0; j < 2; j++) {
					datas[j] = (float) (Math.round((360.0f * values[i] - 180.0f) * 1000.0f) / 1000.0d);
				}
			}
			addSlider(new GuiNpcSlider(this, i, x + 8, y + i * f, 128, 8, values[i]));
			textField = new GuiNpcTextField(i + 5, this, x + 9, y + 9 + i * f, 56, 8, "" + datas[i]);
			textField.setMinMaxDoubleDefault(m, n, datas[i]);
			addTextField(textField);
			button = new GuiNpcButton(30 + i, x + 67, y + 9 + i * f, 8, 8, "X");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			addButton(button);
		}

		y += 38;
		addButton(new GuiNpcCheckBox(37, x, y, 140, 14, "emotion.can.blink", "emotion.can.no.blink", emtn.canBlink()));
		resetEmtns();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui != null) {
			subgui.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		EmotionConfig emtn = getEmtn();
		if (emtn != null) {
			if (sw == null) { sw = new ScaledResolution(mc); }
			int wX = guiLeft + 274;
			int wY = guiTop + 71;
			int x = guiLeft + scroll.width + 10;
			drawGradientRect(wX - 1, wY - 1, wX + 141, wY + 141, 0xFF808080, 0xFF808080);

			drawVerticalLine(wX - 5, guiTop + 4, guiTop + ySize + 12, 0xFF808080);

			drawHorizontalLine(x - 2, x + 138, guiTop + 28, 0xFF808080);
			drawHorizontalLine(x - 2, x + 138, guiTop + 133, 0xFF808080);
			drawHorizontalLine(x - 2, x + 138, guiTop + 172, 0xFF808080);

			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = sw.getScaledWidth() < mc.displayWidth ? (int) Math.round((double) mc.displayWidth / (double) sw.getScaledWidth()) : 1;
			GL11.glScissor(wX * c, mc.displayHeight - (wY + 140) * c, 140 * c, 140 * c);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

			drawGradientRect(wX, wY, wX + 140, wY + 140, GuiNpcAnimation.backColor, GuiNpcAnimation.backColor);
			drawNpc(npcEmtn, 344, 544, 8.2f, 0, 0, 0);

			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void close() {
		save();
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("save", true);
		Client.sendData(EnumPacketServer.EmotionChange, nbt);
		CustomNPCsScheduler.runTack(() -> Client.sendData(EnumPacketServer.AnimationSave, animation.save(new NBTTagCompound())), 500);
		CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void save() {
		EmotionConfig emtn = getEmtn();
		if (emtn != null) { Client.sendData(EnumPacketServer.EmotionChange, emtn.save()); }
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			if (selEmtn.equals(scroll.getSelected())) { return; }
			save();
			selEmtn = scroll.getSelected();
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { initGui(); }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		animation.load(compound);
		initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui.id == 1) { // add new
			if (!(subgui instanceof SubGuiEditText) || ((SubGuiEditText) subgui).cancelled) { return; }
			EmotionConfig newEmtn = (EmotionConfig) aData.createNewEmtn();
			newEmtn.name = ((SubGuiEditText) subgui).text[0];
			selEmtn = newEmtn.getSettingName();
			Client.sendData(EnumPacketServer.EmotionChange, newEmtn.save());
			initGui();
		}
	}

	private EmotionConfig getEmtn() {
		if (!dataEmtns.containsKey(selEmtn)) { selEmtn = ""; }
		if (selEmtn.isEmpty() || !dataEmtns.containsKey(selEmtn)) { return null; }
		return dataEmtns.get(selEmtn);
	}

	private void resetEmtns() {
		EmotionConfig emtn = getEmtn();
		ModelPartData model = ((EntityCustomNpc) npcEmtn).modelData.getPartData(EnumParts.EYES);
		if (model instanceof ModelEyeData) { modelEye = (ModelEyeData) model; }
		if (emtn == null || npcEmtn == null) { return; }
		EmotionConfig ec = emtn.copy();
		NBTTagCompound npcNbt = new NBTTagCompound();
		npcEmtn.animation.reset();
		npcEmtn.writeEntityToNBT(npcNbt);
		npcEmtn.writeToNBTOptional(npcNbt);
		npcEmtn.display.setName("1_" + npc.getName());
		npcEmtn.animation.setActiveEmotion(ec);
		npcEmtn.setHealth(npcEmtn.getMaxHealth());
		npcEmtn.deathTime = 0;
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		float value;
		int pos = slider.id + (isRight ? 0 : 2);
		switch(elementType) {
			case 1: { // pupil
				switch(toolType) { // 0 - rotation, 1 - offset, 2 - scale
					case 1: {
						frame.offsetPupil[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue - 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					case 2: {
						frame.scalePupil[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue + 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					default: {
						frame.rotPupil[isRight ? 0 : 1] = slider.sliderValue;
						value = Math.round((360.0f * slider.sliderValue - 180.0f) * 1000.0f) / 1000.0f;
						break;
					}
				}
				break;
			}
			case 2: { // brow
				switch(toolType) {
					case 1: {
						frame.offsetBrow[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue - 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					case 2: {
						frame.scaleBrow[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue + 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					default: {
						frame.rotBrow[isRight ? 0 : 1] = slider.sliderValue;
						value = Math.round((360.0f * slider.sliderValue - 180.0f) * 1000.0f) / 1000.0f;
						break;
					}
				}
				break;
			}
			case 3: { // mouth
				switch(toolType) {
					case 1: {
						frame.offsetMouth[slider.id] = slider.sliderValue;
						value = Math.round((slider.sliderValue - 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					case 2: {
						frame.scaleMouth[slider.id] = slider.sliderValue;
						value = Math.round((slider.sliderValue + 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					default: {
						frame.rotMouth = slider.sliderValue;
						value = Math.round((360.0f * slider.sliderValue - 180.0f) * 1000.0f) / 1000.0f;
						break;
					}
				}
				break;
			}
			default: { // eye
				elementType = 0;
				switch(toolType) {
					case 1: {
						frame.offsetEye[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue - 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					case 2: {
						frame.scaleEye[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue + 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					default: {
						frame.rotEye[isRight ? 0 : 1] = slider.sliderValue;
						value = Math.round((360.0f * slider.sliderValue - 180.0f) * 1000.0f) / 1000.0f;
						break;
					}
				}
				break;
			}
		}
		if (getTextField(5 + slider.id) != null) { getTextField(5 + slider.id).setText("" + value); }
		resetEmtns();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		EmotionConfig emtn = getEmtn();
		if (hasSubGui() || emtn == null) { return; }
		switch (textField.getId()) {
			case 0: { // renames
				emtn.name = textField.getText();
				selEmtn = emtn.getSettingName();
				initGui();
				break;
			}
			case 1: { // speed
				if (frame == null) { return; }
				frame.setSpeed(textField.getInteger());
				resetEmtns();
				break;
			}
			case 2: { // delay
				if (frame == null) { return; }
				frame.setEndDelay(textField.getInteger());
				resetEmtns();
				break;
			}
			case 3: { // repeat

				break;
			}
			case 5: { // X
				float value = 0.0f;
				String text = "";
				switch(elementType) {
					case 0: {
						switch (toolType) {
							case 1: {
								frame.offsetEye[isRight ? 0 : 2] = (value = (float) (textField.getDouble() + 0.5D));
								text = "" + Math.round((value - 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleEye[isRight ? 0 : 2] = (value = (float) (textField.getDouble() - 0.5D));
								text = "" + Math.round((value + 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							default: {
								frame.rotEye[isRight ? 0 : 1] = (value = (float) (textField.getDouble() + 180.0D) / 360.0f);
								text = "" + Math.round((360.0f * value - 180.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 1: {
						switch (toolType) {
							case 1: {
								frame.offsetPupil[isRight ? 0 : 2] = (value = (float) (textField.getDouble() + 0.5D));
								text = "" + Math.round((value - 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scalePupil[isRight ? 0 : 2] = (value = (float) (textField.getDouble() - 0.5D));
								text = "" + Math.round((value + 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							default: {
								frame.rotPupil[isRight ? 0 : 1] = (value = (float) (textField.getDouble() + 180.0D) / 360.0f);
								text = "" + Math.round((360.0f * value - 180.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 2: {
						switch (toolType) {
							case 1: {
								frame.offsetBrow[isRight ? 0 : 2] = (value = (float) (textField.getDouble() + 0.5D));
								text = "" + Math.round((value - 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleBrow[isRight ? 0 : 2] = (value = (float) (textField.getDouble() - 0.5D));
								text = "" + Math.round((value + 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							default: {
								frame.rotBrow[isRight ? 0 : 1] = (value = (float) (textField.getDouble() + 180.0D) / 360.0f);
								text = "" + Math.round((360.0f * value - 180.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 3: {
						switch (toolType) {
							case 1: {
								frame.offsetMouth[0] = (value = (float) (textField.getDouble() + 0.5D));
								text = "" + Math.round((value - 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleMouth[0] = (value = (float) (textField.getDouble() - 0.5D));
								text = "" + Math.round((value + 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							default: {
								frame.rotMouth = (value = (float) (textField.getDouble() + 180.0D) / 360.0f);
								text = "" + Math.round((360.0f * value - 180.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
				}
				textField.setText(text);
				if (getSlider(0) != null) { getSlider(0).sliderValue = value; }
				resetEmtns();
				break;
			}
			case 6: { // Y
				float value = 0.0f;
				String text = "";
				switch(elementType) {
					case 0: {
						switch (toolType) {
							case 1: {
								frame.offsetEye[isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleEye[isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 1: {
						switch (toolType) {
							case 1: {
								frame.offsetPupil[isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scalePupil[isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 2: {
						switch (toolType) {
							case 1: {
								frame.offsetBrow[isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleBrow[isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 3: {
						switch (toolType) {
							case 1: {
								frame.offsetMouth[1] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleMouth[1] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
				}
				textField.setText(text);
				if (getSlider(0) != null) { getSlider(0).sliderValue = value; }
				resetEmtns();
				break;
			}
		}
	}

}
