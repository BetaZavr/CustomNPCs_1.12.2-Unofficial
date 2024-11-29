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
		this.closeOnEsc = true;
		this.animation = new DataAnimation(npc);
		this.animation.baseEmotionId = this.npc.animation.baseEmotionId;
		this.setBackground("bgfilled.png");

		selEmtn = "";
		npcEmtn = Util.instance.copyToGUI(npc, mc.world, false);
		Client.sendData(EnumPacketServer.AnimationGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		EmotionConfig emtn = this.getEmtn();
		switch (button.id) {
			case 0: { // create new
				this.setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 1: { // del
				if (emtn == null || !dataEmtns.containsKey(scroll.getSelected())) { return; }
				if (this.aData.removeEmotion(emtn.id)) { this.selEmtn = ""; }
				this.initGui();
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
				this.initGui();
				break;
			}
			case 4: { // add frame
				if (emtn == null) { return; }
				frame = (EmotionFrame) emtn.addFrame(frame);
				this.initGui();
				break;
			}
			case 5: { // del frame
				if (frame == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				this.displayGuiScreen(guiyesno);
				break;
			}
			case 6: { // clear frame
				if (frame == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.clearMessage").getFormattedText(),
						GuiScreen.isShiftKeyDown() ? 4 : 1);
				this.displayGuiScreen(guiyesno);
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
				this.resetEmtns();
				break;
			}
			case 22: { // element eye
				if (elementType == 3) { return; }
				elementType = 3;
				this.initGui();
				break;
			}
			case 23: { // tool pos
				if (toolType == 1) { return; }
				toolType = 1;
				this.initGui();
				break;
			}
			case 24: { // tool rot
				if (toolType == 0) { return; }
				toolType = 0;
				this.initGui();
				break;
			}
			case 25: { // tool scale
				if (toolType == 2) { return; }
				toolType = 2;
				this.initGui();
				break;
			}
			case 26: { // element eye
				if (elementType == 0) { return; }
				elementType = 0;
				this.initGui();
				break;
			}
			case 27: { // element pupil
				if (elementType == 1) { return; }
				elementType = 1;
				this.initGui();
				break;
			}
			case 28: { // element brow
				if (elementType == 2) { return; }
				elementType = 2;
				this.initGui();
				break;
			}
			case 29: { // element brow
				isRight = button.getValue() == 0;
				this.initGui();
				break;
			}
			case 30: { // reset X
				if (frame == null) { return; }
				switch (toolType) {
					case 0: {
						switch (elementType) {
							case 0: frame.rotEye[this.isRight ? 0 : 1] = 0.5f; break;
							case 1: frame.rotPupil[this.isRight ? 0 : 1] = 0.5f; break;
							case 2: frame.rotBrow[this.isRight ? 0 : 1] = 0.5f; break;
							case 3: frame.rotMouth = 0.5f; break;
						}
						break;
					}
					case 1: {
						switch (elementType) {
							case 0: frame.offsetEye[this.isRight ? 0 : 2] = 0.5f; break;
							case 1: frame.offsetPupil[this.isRight ? 0 : 2] = 0.5f; break;
							case 2: frame.offsetBrow[this.isRight ? 0 : 2] = 0.5f; break;
							case 3: frame.offsetMouth[0] = 0.5f; break;
						}
						break;
					}
					case 2: {
						switch (elementType) {
							case 0: frame.scaleEye[this.isRight ? 0 : 2] = 0.5f; break;
							case 1: frame.scalePupil[this.isRight ? 0 : 2] = 0.5f; break;
							case 2: frame.scaleBrow[this.isRight ? 0 : 2] = 0.5f; break;
							case 3: frame.scaleMouth[0] = 0.5f; break;
						}
						break;
					}
				}
				this.initGui();
				break;
			}
			case 31: { // reset part set Y
				if (frame == null) { return; }
				switch (toolType) {
					case 1:
						switch (elementType) {
							case 0: frame.offsetEye[this.isRight ? 1 : 3] = 0.5f; break;
							case 1: frame.offsetPupil[this.isRight ? 1 : 3] = 0.5f; break;
							case 2: frame.offsetBrow[this.isRight ? 1 : 3] = 0.5f; break;
							case 3: frame.offsetMouth[1] = 0.5f; break;
						}
						break;
					case 2:
						switch (elementType) {// 0 - eye, 1 - pupil, 2 - brow
							case 0: frame.scaleEye[this.isRight ? 1 : 3] = 0.5f; break;
							case 1: frame.scalePupil[this.isRight ? 1 : 3] = 0.5f; break;
							case 2: frame.scaleBrow[this.isRight ? 1 : 3] = 0.5f; break;
							case 3: frame.scaleMouth[1] = 0.5f; break;
						}
						break;
				}
				this.initGui();
				break;
			}
			case 32: { // reset part set Y
				if (this.modelEye == null) { return; }
				this.modelEye.type = (byte) button.getValue();
				break;
			}
			case 33: { // set base emotion
				if (this.animation == null || !this.dataEmtns.containsKey(this.selEmtn)) { return; }
				this.animation.baseEmotionId = this.dataEmtns.get(this.selEmtn).id;
				this.initGui();
				break;
			}
			case 34: { // del base emotion
				if (this.animation == null) { return; }
				this.animation.baseEmotionId = -1;
				this.initGui();
				break;
			}
			case 35: { // start blink
				if (this.frame == null) { return; }
				this.frame.setBlink(((GuiNpcCheckBox) button).isSelected());
				this.initGui();
				break;
			}
			case 36: { // end blink
				if (this.frame == null) { return; }
				this.frame.setEndBlink(((GuiNpcCheckBox) button).isSelected());
				this.initGui();
				break;
			}
			case 37: { // can blink
				if (emtn == null) { return; }
				emtn.setCanBlink(((GuiNpcCheckBox) button).isSelected());
				this.initGui();
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
				this.initGui();
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		this.displayGuiScreen(this);
		if (!result) {
			return;
		}
		EmotionConfig emtn = this.getEmtn();
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
				this.initGui();
				break;
			}
			case 1: { // clear frame
				if (frame == null) { return; }
				frame.clear();
				this.initGui();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.sw = new ScaledResolution(this.mc);
		int lId = 0;
		int x = this.guiLeft + 8;
		int y = this.guiTop + 14;
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(120, 177); }
		this.scroll.guiLeft = x;
		this.scroll.guiTop = y;
		this.addScroll(this.scroll);

		this.addLabel(new GuiNpcLabel(lId++, "emotion.list", x + 1, y - 10));

		dataEmtns.clear();
		aData = AnimationController.getInstance();
		for (EmotionConfig ec : aData.emotions.values()) { dataEmtns.put(ec.getSettingName(), ec); }
		this.scroll.setListNotSorted(new ArrayList<>(dataEmtns.keySet()));
		if (!selEmtn.isEmpty()) {
			if (this.scroll.getList().contains(selEmtn)) { this.scroll.setSelected(selEmtn); }
			else { selEmtn = ""; }
		}
		if (selEmtn.isEmpty() && !dataEmtns.isEmpty()) {
			for (String key : dataEmtns.keySet()) {
				this.selEmtn = key;
				this.scroll.setSelected(selEmtn);
				frame = dataEmtns.get(key).frames.get(0);
				break;
			}
		}
		this.addButton(new GuiNpcButton(0, x, y + this.scroll.height + 1, 59, 20, "markov.generate"));
		this.getButton(0).enabled = dataEmtns.isEmpty();
		this.addButton(new GuiNpcButton(1, x + 62, y + this.scroll.height + 1, 59, 20, "gui.remove"));
		this.getButton(1).enabled = !selEmtn.isEmpty();

		EmotionConfig emtn = this.getEmtn();
		if (emtn == null) { return; }
		if (frame == null) {
			frame = emtn.frames.get(0);
			if (frame == null) { return; }
		}
		int wX = this.guiLeft + 274;
		int wY = this.guiTop + 71;

		// Back color
		GuiNpcButton button = new GuiNpcButton(2, wX, wY - 13, 8, 8, "");
		button.layerColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);

		// Tool type
		button = new GuiNpcButton(23, wX + 10, wY - 16, 14, 14, ""); // tool pos
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 1 ? 0xFFFF8080 : 0xFFFFFFFF;
		this.addButton(button);
		button = new GuiNpcButton(24, wX + 26, wY - 16, 14, 14, ""); // tool rot
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 24;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 0 ? 0xFF80FF80 : 0xFFFFFFFF;
		this.addButton(button);
		button = new GuiNpcButton(25, wX + 42, wY - 16, 14, 14, ""); // tool scale
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 48;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 2 ? 0xFF8080FF : 0xFFFFFFFF;
		this.addButton(button);

		button = new GuiNpcButton(26, wX + 78, wY - 16, 14, 14, ""); // element eye
		button.texture = etns;
		button.hasDefBack = false;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = elementType == 0 ? 0xFFFF8080 : 0xFFFFFFFF;
		this.addButton(button);
		button = new GuiNpcButton(27, wX + 94, wY - 16, 14, 14, ""); // element pupil
		button.texture = etns;
		button.hasDefBack = false;
		button.txrX = 24;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = elementType == 1 ? 0xFF80FF80 : 0xFFFFFFFF;
		this.addButton(button);
		button = new GuiNpcButton(28, wX + 110, wY - 16, 14, 14, ""); // element brow
		button.texture = etns;
		button.hasDefBack = false;
		button.txrX = 48;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = elementType == 2 ? 0xFF8080FF : 0xFFFFFFFF;
		this.addButton(button);
		button = new GuiNpcButton(22, wX + 126, wY - 16, 14, 14, ""); // element mouth
		button.texture = etns;
		button.hasDefBack = false;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = elementType == 3 ? 0xFFFFFF80 : 0xFFFFFFFF;
		this.addButton(button);

		button = new GuiButtonBiDirectional(32, wX + 58, wY - 48, 82, 14, this.types, (this.modelEye == null) ? 0 : this.modelEye.type);
		this.addButton(button);

		button = new GuiNpcButton(33, wX, y - 10, 70, 14, "gui.set");
		button.setEnabled(this.animation.baseEmotionId != emtn.id);
		this.addButton(button);
		button = new GuiNpcButton(34, wX + 72, y - 10, 70, 14, "gui.remove");
		button.setEnabled(this.animation.baseEmotionId >= 0);
		this.addButton(button);

		button = new GuiNpcButton(29, wX + 58, wY - 32, 82, 14, new String[] { "gui.right", "gui.left" }, this.isRight ? 0 : 1);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);

		// Name
		x += this.scroll.width + 2;
		y = this.guiTop + 14;
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.name").getFormattedText() + ":", x + 1, y - 10));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, this.fontRenderer, x + 1, y, 135, 12, emtn.name);
		this.addTextField(textField);

		// Frame
		this.addLabel(new GuiNpcLabel(lId++, "animation.frames", x, (y += 26) - 10));
		List<String> lFrames = new ArrayList<>();
		for (int i = 0; i < emtn.frames.size(); i++) { lFrames.add((i + 1) + "/" + emtn.frames.size()); }
		button = new GuiButtonBiDirectional(3, x, y, 60, 14, lFrames.toArray(new String[0]), emtn.id);
		button.setEnabled(emtn.frames.size() > 1);
		this.addButton(button);
		button = new GuiNpcButton(4, x + 62, y + 2, 10, 10, ""); // add frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(5, x + 74, y + 2, 10, 10, ""); // del frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = emtn.frames.size() > 1;
		this.addButton(button);
		button = new GuiNpcButton(6, x + 126, y + 2, 10, 10, ""); // clear frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);

		y += 17;
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.time").getFormattedText() + ":", x, y + 2));
		textField = new GuiNpcTextField(1, this, x + 45, y, 43, 12, "" + frame.getSpeed());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 3600, frame.getSpeed());
		this.addTextField(textField);
		textField = new GuiNpcTextField(2, this, x + 92, y, 43, 12, "" + frame.getEndDelay());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 3600, frame.getEndDelay());
		this.addTextField(textField);

		y += 13;
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.repeat").getFormattedText() + ":", x, y + 5));
		if (emtn.repeatLast < 0) { emtn.repeatLast *= -1; }
		if (emtn.repeatLast < 0 || emtn.repeatLast > emtn.frames.size()) { emtn.repeatLast = emtn.frames.size(); }
		textField = new GuiNpcTextField(3, this, x + 45, y + 3, 43, 12, "" + emtn.repeatLast);
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, emtn.frames.size(), emtn.repeatLast);
		this.addTextField(textField);
		addButton(new GuiNpcCheckBox(11, x + 92, y + 1, 45, 14, "gui.smooth", "gui.linearly", frame.isSmooth()));

		y += 16;
		addButton(new GuiNpcCheckBox(38, x, y, 140, 14, "emotion.part.disable", null, frame.isDisabled()));
		this.addButton(button);
		this.resetEmtns();

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
			if (this.toolType == 0 && i == 1) { break; }
			this.addLabel(new GuiNpcLabel(lId++, i == 0 ? "X:" : "Y:", x, y + i * f + 4));
			float[] values;
			switch(this.elementType) {
				case 1: { // pupil
					switch(this.toolType) { // 0 - rotation, 1 - offset, 2 - scale
						case 1: {
							values = new float[] { this.frame.offsetPupil[this.isRight ? 0 : 2], this.frame.offsetPupil[this.isRight ? 1 : 3] };
							break;
						}
						case 2: {
							values = new float[] { this.frame.scalePupil[this.isRight ? 0 : 2], this.frame.scalePupil[this.isRight ? 1 : 3] };
							break;
						}
						default: {
							values = new float[] { this.frame.rotPupil[this.isRight ? 0 : 1] };
							break;
						}
					}
					break;
				}
				case 2: { // brow
					switch(this.toolType) {
						case 1: {
							values = new float[] { this.frame.offsetBrow[this.isRight ? 0 : 2], this.frame.offsetBrow[this.isRight ? 1 : 3] };
							break;
						}
						case 2: {
							values = new float[] { this.frame.scaleBrow[this.isRight ? 0 : 2], this.frame.scaleBrow[this.isRight ? 1 : 3] };
							break;
						}
						default: {
							values = new float[] { this.frame.rotBrow[this.isRight ? 0 : 1] };
							break;
						}
					}
					break;
				}
				case 3: { // mouth
					switch(this.toolType) {
						case 1: {
							values = this.frame.offsetMouth;
							break;
						}
						case 2: {
							values = this.frame.scaleMouth;
							break;
						}
						default: {
							values = new float[] { this.frame.rotMouth };
							break;
						}
					}
					break;
				}
				default: { // eye
					switch(this.toolType) {
						case 1: {
							values = new float[] { this.frame.offsetEye[this.isRight ? 0 : 2], this.frame.offsetEye[this.isRight ? 1 : 3] };
							break;
						}
						case 2: {
							values = new float[] { this.frame.scaleEye[this.isRight ? 0 : 2], this.frame.scaleEye[this.isRight ? 1 : 3] };
							break;
						}
						default: {
							values = new float[] { this.frame.rotEye[this.isRight ? 0 : 1] };
							break;
						}
					}
					break;
				}
			}

			float[] datas = new float[2];
			double m, n;
			if (this.toolType == 1) { // offset
				m = -0.5d;
				n = 0.5d;
				for (int j = 0; j < 2; j++) {
					datas[j] = (float) (Math.round((values[i] - 0.5f) * 1000.0f) / 1000.0d);
				}
			} else if (this.toolType == 2) { // scale
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
			this.addSlider(new GuiNpcSlider(this, i, x + 8, y + i * f, 128, 8, values[i]));
			textField = new GuiNpcTextField(i + 5, this, x + 9, y + 9 + i * f, 56, 8, "" + datas[i]);
			textField.setDoubleNumbersOnly();
			textField.setMinMaxDoubleDefault(m, n, datas[i]);
			this.addTextField(textField);
			button = new GuiNpcButton(30 + i, x + 67, y + 9 + i * f, 8, 8, "X");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			this.addButton(button);
		}

		y += 38;
		addButton(new GuiNpcCheckBox(37, x, y, 140, 14, "emotion.can.blink", "emotion.can.no.blink", emtn.canBlink()));
		this.resetEmtns();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.subgui != null) {
			this.subgui.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		EmotionConfig emtn = this.getEmtn();
		if (emtn != null) {
			if (this.sw == null) { this.sw = new ScaledResolution(this.mc); }
			int wX = this.guiLeft + 274;
			int wY = this.guiTop + 71;
			int x = this.guiLeft + this.scroll.width + 10;
			this.drawGradientRect(wX - 1, wY - 1, wX + 141, wY + 141, 0xFF808080, 0xFF808080);

			this.drawVerticalLine(wX - 5, this.guiTop + 4, this.guiTop + this.ySize + 12, 0xFF808080);

			this.drawHorizontalLine(x - 2, x + 138, this.guiTop + 28, 0xFF808080);
			this.drawHorizontalLine(x - 2, x + 138, this.guiTop + 133, 0xFF808080);
			this.drawHorizontalLine(x - 2, x + 138, this.guiTop + 172, 0xFF808080);

			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = sw.getScaledWidth() < this.mc.displayWidth ? (int) Math.round((double) this.mc.displayWidth / (double) sw.getScaledWidth()) : 1;
			GL11.glScissor(wX * c, this.mc.displayHeight - (wY + 140) * c, 140 * c, 140 * c);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

			this.drawGradientRect(wX, wY, wX + 140, wY + 140, GuiNpcAnimation.backColor, GuiNpcAnimation.backColor);
			this.drawNpc(this.npcEmtn, 344, 544, 8.2f, 0, 0, 0);

			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
		}

		if (!CustomNpcs.ShowDescriptions) { return; }

		if (this.getButton(23) != null && this.getButton(23).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.0").getFormattedText());
		} else if (this.getButton(24) != null && this.getButton(24).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.1").getFormattedText());
		} else if (this.getButton(25) != null && this.getButton(25).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.2").getFormattedText());
		}

		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void close() {
		this.save();
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("save", true);
		Client.sendData(EnumPacketServer.EmotionChange, nbt);
		CustomNPCsScheduler.runTack(() -> Client.sendData(EnumPacketServer.AnimationSave, this.animation.save(new NBTTagCompound())), 500);
		CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void save() {
		EmotionConfig emtn = this.getEmtn();
		if (emtn != null) { Client.sendData(EnumPacketServer.EmotionChange, emtn.save()); }
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			if (this.selEmtn.equals(scroll.getSelected())) { return; }
			this.save();
			this.selEmtn = scroll.getSelected();
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { this.initGui(); }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.animation.load(compound);
		this.initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		//EmotionConfig emtn = this.getEmtn();
		if (subgui.id == 1) { // add new
			if (!(subgui instanceof SubGuiEditText) || ((SubGuiEditText) subgui).cancelled) { return; }
			EmotionConfig newEmtn = (EmotionConfig) aData.createNewEmtn();
			newEmtn.name = ((SubGuiEditText) subgui).text[0];
			this.selEmtn = newEmtn.getSettingName();
			Client.sendData(EnumPacketServer.EmotionChange, newEmtn.save());
			this.initGui();
		}
	}

	private EmotionConfig getEmtn() {
		if (!this.dataEmtns.containsKey(selEmtn)) { selEmtn = ""; }
		if (selEmtn.isEmpty() || !this.dataEmtns.containsKey(selEmtn)) { return null; }
		return this.dataEmtns.get(selEmtn);
	}

	private void resetEmtns() {
		EmotionConfig emtn = this.getEmtn();
		ModelPartData model = ((EntityCustomNpc) this.npcEmtn).modelData.getPartData(EnumParts.EYES);
		if (model instanceof ModelEyeData) { this.modelEye = (ModelEyeData) model; }
		if (emtn == null || this.npcEmtn == null) { return; }
		EmotionConfig ec = emtn.copy();
		NBTTagCompound npcNbt = new NBTTagCompound();
		this.npcEmtn.animation.clear();
		this.npcEmtn.writeEntityToNBT(npcNbt);
		this.npcEmtn.writeToNBTOptional(npcNbt);
		this.npcEmtn.display.setName("1_" + this.npc.getName());
		this.npcEmtn.animation.activeEmotion = ec;
		this.npcEmtn.setHealth(this.npcEmtn.getMaxHealth());
		this.npcEmtn.deathTime = 0;
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		float value;
		int pos = slider.id + (this.isRight ? 0 : 2);
		switch(this.elementType) {
			case 1: { // pupil
				switch(this.toolType) { // 0 - rotation, 1 - offset, 2 - scale
					case 1: {
						this.frame.offsetPupil[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue - 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					case 2: {
						this.frame.scalePupil[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue + 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					default: {
						this.frame.rotPupil[this.isRight ? 0 : 1] = slider.sliderValue;
						value = Math.round((360.0f * slider.sliderValue - 180.0f) * 1000.0f) / 1000.0f;
						break;
					}
				}
				break;
			}
			case 2: { // brow
				switch(this.toolType) {
					case 1: {
						this.frame.offsetBrow[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue - 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					case 2: {
						this.frame.scaleBrow[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue + 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					default: {
						this.frame.rotBrow[this.isRight ? 0 : 1] = slider.sliderValue;
						value = Math.round((360.0f * slider.sliderValue - 180.0f) * 1000.0f) / 1000.0f;
						break;
					}
				}
				break;
			}
			case 3: { // mouth
				switch(this.toolType) {
					case 1: {
						this.frame.offsetMouth[slider.id] = slider.sliderValue;
						value = Math.round((slider.sliderValue - 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					case 2: {
						this.frame.scaleMouth[slider.id] = slider.sliderValue;
						value = Math.round((slider.sliderValue + 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					default: {
						this.frame.rotMouth = slider.sliderValue;
						value = Math.round((360.0f * slider.sliderValue - 180.0f) * 1000.0f) / 1000.0f;
						break;
					}
				}
				break;
			}
			default: { // eye
				this.elementType = 0;
				switch(this.toolType) {
					case 1: {
						this.frame.offsetEye[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue - 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					case 2: {
						this.frame.scaleEye[pos] = slider.sliderValue;
						value = Math.round((slider.sliderValue + 0.5f) * 1000.0f) / 1000.0f;
						break;
					}
					default: {
						this.frame.rotEye[this.isRight ? 0 : 1] = slider.sliderValue;
						value = Math.round((360.0f * slider.sliderValue - 180.0f) * 1000.0f) / 1000.0f;
						break;
					}
				}
				break;
			}
		}
		if (this.getTextField(5 + slider.id) != null) { this.getTextField(5 + slider.id).setText("" + value); }
		this.resetEmtns();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		EmotionConfig emtn = this.getEmtn();
		if (this.hasSubGui() || emtn == null) { return; }
		switch (textField.getId()) {
			case 0: { // renames
				emtn.name = textField.getText();
				this.selEmtn = emtn.getSettingName();
				this.initGui();
				break;
			}
			case 1: { // speed
				if (frame == null) { return; }
				frame.setSpeed(textField.getInteger());
				this.resetEmtns();
				break;
			}
			case 2: { // delay
				if (frame == null) { return; }
				frame.setEndDelay(textField.getInteger());
				this.resetEmtns();
				break;
			}
			case 3: { // repeat

				break;
			}
			case 5: { // X
				float value = 0.0f;
				String text = "";
				switch(this.elementType) {
					case 0: {
						switch (toolType) {
							case 1: {
								frame.offsetEye[this.isRight ? 0 : 2] = (value = (float) (textField.getDouble() + 0.5D));
								text = "" + Math.round((value - 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleEye[this.isRight ? 0 : 2] = (value = (float) (textField.getDouble() - 0.5D));
								text = "" + Math.round((value + 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							default: {
								frame.rotEye[this.isRight ? 0 : 1] = (value = (float) (textField.getDouble() + 180.0D) / 360.0f);
								text = "" + Math.round((360.0f * value - 180.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 1: {
						switch (toolType) {
							case 1: {
								frame.offsetPupil[this.isRight ? 0 : 2] = (value = (float) (textField.getDouble() + 0.5D));
								text = "" + Math.round((value - 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scalePupil[this.isRight ? 0 : 2] = (value = (float) (textField.getDouble() - 0.5D));
								text = "" + Math.round((value + 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							default: {
								frame.rotPupil[this.isRight ? 0 : 1] = (value = (float) (textField.getDouble() + 180.0D) / 360.0f);
								text = "" + Math.round((360.0f * value - 180.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 2: {
						switch (toolType) {
							case 1: {
								frame.offsetBrow[this.isRight ? 0 : 2] = (value = (float) (textField.getDouble() + 0.5D));
								text = "" + Math.round((value - 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleBrow[this.isRight ? 0 : 2] = (value = (float) (textField.getDouble() - 0.5D));
								text = "" + Math.round((value + 0.5f) * 1000.0f) / 1000.0f;
								break;
							}
							default: {
								frame.rotBrow[this.isRight ? 0 : 1] = (value = (float) (textField.getDouble() + 180.0D) / 360.0f);
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
				if (this.getSlider(0) != null) { this.getSlider(0).sliderValue = value; }
				this.resetEmtns();
				break;
			}
			case 6: { // Y
				float value = 0.0f;
				String text = "";
				switch(this.elementType) {
					case 0: {
						switch (toolType) {
							case 1: {
								frame.offsetEye[this.isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleEye[this.isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 1: {
						switch (toolType) {
							case 1: {
								frame.offsetPupil[this.isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scalePupil[this.isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
						}
						break;
					}
					case 2: {
						switch (toolType) {
							case 1: {
								frame.offsetBrow[this.isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
								text = "" + Math.round((200.0f * value - 100.0f) * 1000.0f) / 1000.0f;
								break;
							}
							case 2: {
								frame.scaleBrow[this.isRight ? 1 : 3] = (value = (float) (textField.getDouble() + 100.0D) / 200.0f);
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
				if (this.getSlider(0) != null) { this.getSlider(0).sliderValue = value; }
				this.resetEmtns();
				break;
			}
		}
	}

}
