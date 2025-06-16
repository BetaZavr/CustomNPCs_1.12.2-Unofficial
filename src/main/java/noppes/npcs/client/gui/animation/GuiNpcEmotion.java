package noppes.npcs.client.gui.animation;

import java.util.*;

import noppes.npcs.client.gui.util.*;
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

	public EntityNPCInterface npcEmtn;
	public EmotionFrame frame;
	public int toolType = 0; // 0 - rotation, 1 - offset, 2 - scale
	public int elementType = 0; // 0 - eye, 1 - pupil, 2 - brow, 3 - mouth
	public boolean isRight = true;

	protected final Map<String, EmotionConfig> dataEmtns = new TreeMap<>();
	protected String selEmtn;
	protected boolean onlyPart = false;
	protected final DataAnimation animation;
	protected GuiCustomScroll scroll;
	protected AnimationController aData;
	protected ScaledResolution sw;
	protected ModelEyeData modelEye;
	protected final String[] types = new String[] { "gui.small", "gui.normal", "gui.select" };

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
	public void buttonEvent(IGuiNpcButton button) {
		EmotionConfig emtn = getEmtn();
		switch (button.getID()) {
			case 0: {
				setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			} // create new
			case 1: {
				if (emtn == null || !dataEmtns.containsKey(scroll.getSelected())) { return; }
				if (aData.removeEmotion(emtn.id)) { selEmtn = ""; }
				initGui();
				break;
			} // del
			case 2: {
				GuiNpcAnimation.backColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000);
				button.setLayerColor(GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
				break;
			} // back color
			case 3: {
				if (emtn == null || !emtn.frames.containsKey(button.getValue())) { return; }
				frame = emtn.frames.get(button.getValue());
				initGui();
				break;
			} // select frame
			case 4: {
				if (emtn == null) { return; }
				frame = (EmotionFrame) emtn.addFrame(frame);
				initGui();
				break;
			} // add frame
			case 5: {
				if (frame == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			} // del frame
			case 6: {
				if (frame == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.clearMessage").getFormattedText(),
						GuiScreen.isShiftKeyDown() ? 4 : 1);
				displayGuiScreen(guiyesno);
				break;
			} // clear frame
			case 11: {
				if (emtn == null || frame == null) { return; }
				frame.setSmooth(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					for (EmotionFrame f : emtn.frames.values()) {
						f.setSmooth(frame.isSmooth());
					}
				}
				resetEmtns();
				break;
			} // smooth
			case 22: {
				if (elementType == 3) { return; }
				elementType = 3;
				initGui();
				break;
			} // element eye
			case 23: {
				if (toolType == 1) { return; }
				toolType = 1;
				initGui();
				break;
			} // tool pos
			case 24: {
				if (toolType == 0) { return; }
				toolType = 0;
				initGui();
				break;
			} // tool rot
			case 25: {
				if (toolType == 2) { return; }
				toolType = 2;
				initGui();
				break;
			} // tool scale
			case 26: {
				if (elementType == 0) { return; }
				elementType = 0;
				initGui();
				break;
			} // element eye
			case 27: {
				if (elementType == 1) { return; }
				elementType = 1;
				initGui();
				break;
			}// element pupil
			case 28: {
				if (elementType == 2) { return; }
				elementType = 2;
				initGui();
				break;
			} // element brow
			case 29: {
				isRight = button.getValue() == 0;
				initGui();
				break;
			} // element brow
			case 30: {
				if (frame == null) { return; }
				switch (toolType) {
					case 0: {
						switch (elementType) {
							case 0: frame.rotEye[isRight ? 0 : 1] = 0.0f; break;
							case 1: frame.rotPupil[isRight ? 0 : 1] = 0.0f; break;
							case 2: frame.rotBrow[isRight ? 0 : 1] = 0.0f; break;
							case 3: frame.rotMouth = 0.0f; break;
						}
						break;
					}
					case 1: {
						switch (elementType) {
							case 0: frame.offsetEye[isRight ? 0 : 2] = 0.0f; break;
							case 1: frame.offsetPupil[isRight ? 0 : 2] = 0.0f; break;
							case 2: frame.offsetBrow[isRight ? 0 : 2] = 0.0f; break;
							case 3: frame.offsetMouth[0] = 0.0f; break;
						}
						break;
					}
					case 2: {
						switch (elementType) {
							case 0: frame.scaleEye[isRight ? 0 : 2] = 1.0f; break;
							case 1: frame.scalePupil[isRight ? 0 : 2] = 1.0f; break;
							case 2: frame.scaleBrow[isRight ? 0 : 2] = 1.0f; break;
							case 3: frame.scaleMouth[0] = 1.0f; break;
						}
						break;
					}
				}
				initGui();
				break;
			} // reset X
			case 31: {
				if (frame == null) { return; }
				switch (toolType) {
					case 1:
						switch (elementType) {
							case 0: frame.offsetEye[isRight ? 1 : 3] = 0.0f; break;
							case 1: frame.offsetPupil[isRight ? 1 : 3] = 0.0f; break;
							case 2: frame.offsetBrow[isRight ? 1 : 3] = 0.0f; break;
							case 3: frame.offsetMouth[1] = 0.0f; break;
						}
						break;
					case 2:
						switch (elementType) {// 0 - eye, 1 - pupil, 2 - brow
							case 0: frame.scaleEye[isRight ? 1 : 3] = 1.0f; break;
							case 1: frame.scalePupil[isRight ? 1 : 3] = 1.0f; break;
							case 2: frame.scaleBrow[isRight ? 1 : 3] = 1.0f; break;
							case 3: frame.scaleMouth[1] = 1.0f; break;
						}
						break;
				}
				initGui();
				break;
			} // reset part set Y
			case 32: {
				if (modelEye == null) { return; }
				modelEye.type = (byte) button.getValue();
				break;
			} // type
			case 33: {
				if (animation == null || !dataEmtns.containsKey(selEmtn)) { return; }
				animation.setBaseEmotionId(dataEmtns.get(selEmtn).id);
				initGui();
				break;
			} // set base emotion
			case 34: {
				if (animation == null) { return; }
				animation.setBaseEmotionId(-1);
				initGui();
				break;
			} // del base emotion
			case 35: {
				if (frame == null) { return; }
				frame.setBlink(((GuiNpcCheckBox) button).isSelected());
				initGui();
				break;
			} // start blink
			case 36: {
				if (frame == null) { return; }
				frame.setEndBlink(((GuiNpcCheckBox) button).isSelected());
				initGui();
				break;
			} // end blink
			case 37: {
				if (emtn == null) { return; }
				emtn.setCanBlink(((GuiNpcCheckBox) button).isSelected());
				initGui();
				break;
			} // can blink
			case 38: {
				if (frame == null) { return; }
				frame.setDisable(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown() && emtn != null) { // Shift pressed
					for (EmotionFrame f : emtn.frames.values()) {
						f.setDisable(frame.isDisabled());
					}
				}
				initGui();
				break;
			} // disable pupil
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

	@SuppressWarnings("all")
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
		getButton(0).setEnabled(dataEmtns.isEmpty());
		addButton(new GuiNpcButton(1, x + 62, y + scroll.height + 1, 59, 20, "gui.remove"));
		getButton(1).setEnabled(!selEmtn.isEmpty());

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

		int xx = sw.getScaledWidth() / 2 - wX;
		int yy = sw.getScaledHeight() / 2 - wY;
//System.out.println("CNPCs: ["+sw.getScaledWidth()+", "+sw.getScaledHeight()+"]; ["+wX+", "+wY+"]; ["+xx+", "+yy+"]");

		/*button = new GuiNpcButton(21, workU + workS / 2 - 11, workV + workS - 12, 18, 10, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = onlyCurrentPart ? 144 : 188;
		button.txrW = 44;
		button.txrH = 24;
		button.setHoverText(new TextComponentTranslation("animation.hover.work." + onlyCurrentPart, ((char) 167) + "6" + (frame != null ? frame.id + 1 : -1)).getFormattedText());
		addButton(button);*/


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
		float[] values;
		switch(elementType) {
			case 1: {
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
			} // pupil
			case 2: {
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
			} // brow
			case 3: {
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
			} // mouth
			default: {
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
			} // eye
		}
		for (int i = 0; i < 2; i++) {
			if (toolType == 0 && i == 1) { break; }
			addLabel(new GuiNpcLabel(lId++, i == 0 ? "X:" : "Y:", x, y + i * f + 4));
			float sliderData;
			double m, n;
			if (toolType == 1) { // offset
				m = isRight ? -1.0d : -1.5d;
				n = isRight ? 1.5d : 1.0d;
				sliderData = values[i] * 0.4f + (isRight ? 0.4f : 0.6f);
			} else if (toolType == 2) { // scale
				m = 0.0d;
				n = 2.0d;
				sliderData = values[i] * 0.5f;
			} else { // rotation
				m = -180.0d;
				n = 180.0d;
				sliderData = values[i] * 0.0027778f + 0.5f;
			}
			addSlider(new GuiNpcSlider(this, i, x + 8, y + i * f, 128, 8, sliderData));
			textField = new GuiNpcTextField(i + 5, this, x + 9, y + 9 + i * f, 56, 8, "" + values[i]);
			textField.setMinMaxDoubleDefault(m, n, sliderData);
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

		addLabel(new GuiNpcLabel(lId, new TextComponentTranslation("ai.movement").getFormattedText() + ":", x, (y += 20) + 1));
		textField = new GuiNpcTextField(7, this, x += 45,  y, 40, 12, "" + emtn.scaleMoveX);
		textField.setMinMaxDoubleDefault(0.05d, 1.25d, emtn.scaleMoveX);
		textField.setHoverText("emotion.hover.scale.move", "X");
		addTextField(textField);
		textField = new GuiNpcTextField(8, this, x + 45,  y, 40, 12, "" + emtn.scaleMoveY);
		textField.setMinMaxDoubleDefault(0.05d, 1.25d, emtn.scaleMoveY);
		textField.setHoverText("emotion.hover.scale.move", "Y");
		addTextField(textField);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui != null) {
			subgui.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		npcEmtn.animation.updateTime();
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
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			if (selEmtn.equals(scroll.getSelected())) { return; }
			save();
			selEmtn = scroll.getSelected();
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) { initGui(); }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		animation.load(compound);
		initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui.getId() == 1) { // add new
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
		emtn.editFrame = onlyPart ? frame.id : -1;
		emtn.isEdit = true;
		npcEmtn.display.setName(npc.getName()+"_edit_emotion");
		npcEmtn.setHealth(npcEmtn.getMaxHealth());
		npcEmtn.deathTime = 0;
		npcEmtn.animation.tryRunEmotion(emtn);
	}

	@Override
	public void mouseDragged(IGuiNpcSlider slider) {
		float value;
		int pos = slider.getID() + (isRight ? 0 : 2);
		switch(elementType) {
			case 1: { // pupil
				switch(toolType) { // 0 - rotation, 1 - offset, 2 - scale
					case 1: {
						frame.offsetPupil[pos] = (value = slider.getSliderValue() * 2.5f - (isRight ? 1.0f : 1.5f));
						break;
					}
					case 2: {
						frame.offsetPupil[pos] = (value = slider.getSliderValue() * 2.0f);
						break;
					}
					default: {
						frame.rotPupil[isRight ? 0 : 1] = (value = slider.getSliderValue() * 360.0f - 180.0f);
						break;
					}
				}
				break;
			}
			case 2: { // brow
				switch(toolType) {
					case 1: {
						frame.offsetBrow[pos] = (value = slider.getSliderValue() * 2.5f - (isRight ? 1.0f : 1.5f));
						break;
					}
					case 2: {
						frame.scaleBrow[pos] = (value = slider.getSliderValue() * 2.0f);
						break;
					}
					default: {
						frame.rotBrow[isRight ? 0 : 1] = (value = slider.getSliderValue() * 360.0f - 180.0f);
						break;
					}
				}
				break;
			}
			case 3: { // mouth
				switch(toolType) {
					case 1: {
						frame.offsetMouth[slider.getID()] = (value = slider.getSliderValue() * 2.5f - (isRight ? 1.0f : 1.5f));
						break;
					}
					case 2: {
						frame.scaleMouth[slider.getID()] = (value = slider.getSliderValue() * 2.0f);
						break;
					}
					default: {
						frame.rotMouth = (value = slider.getSliderValue() * 360.0f - 180.0f);
						break;
					}
				}
				break;
			}
			default: { // eye
				elementType = 0;
				switch(toolType) {
					case 1: {
						frame.offsetEye[pos] = (value = slider.getSliderValue() * 2.5f - (isRight ? 1.0f : 1.5f));
						break;
					}
					case 2: {
						frame.scaleEye[pos] = (value = slider.getSliderValue() * 2.0f);
						break;
					}
					default: {
						frame.rotEye[isRight ? 0 : 1] = (value = slider.getSliderValue() * 360.0f - 180.0f);
						break;
					}
				}
				break;
			}
		}
		if (getTextField(5 + slider.getID()) != null) { getTextField(5 + slider.getID()).setFullText("" + (Math.round(value * 1000.0f) / 1000.0f)); }
		resetEmtns();
	}

	@Override
	public void mousePressed(IGuiNpcSlider slider) { }

	@Override
	public void mouseReleased(IGuiNpcSlider slider) { }

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		EmotionConfig emtn = getEmtn();
		if (hasSubGui() || emtn == null) { return; }
		switch (textField.getID()) {
			case 0: {
				emtn.name = textField.getFullText();
				selEmtn = emtn.getSettingName();
				initGui();
				break;
			} // renames
			case 1: {
				if (frame == null) { return; }
				frame.setSpeed(textField.getInteger());
				resetEmtns();
				break;
			} // speed
			case 2: {
				if (frame == null) { return; }
				frame.setEndDelay(textField.getInteger());
				resetEmtns();
				break;
			} // delay
			case 3: {

				break;
			} // repeat
			case 5: {
				float value = 0.0f;
				float data = (float) textField.getDouble();
				switch(elementType) {
					case 0: {
						switch (toolType) {
							case 1: {
								value = data / 3.0f + 0.5f;
								frame.offsetEye[isRight ? 0 : 2] = data;
								break;
							}
							case 2: {
								value = data * 0.5f;
								frame.scaleEye[isRight ? 0 : 2] = data;
								break;
							}
							default: {
								value = data * 0.0027778f + 0.5f;
								frame.rotEye[isRight ? 0 : 1] = data;
								break;
							}
						}
						break;
					} // Eye
					case 1: {
						switch (toolType) {
							case 1: {
								value = data / 3.0f + 0.5f;
								frame.offsetPupil[isRight ? 0 : 2] = data;
								break;
							}
							case 2: {
								value = data * 0.5f;
								frame.scalePupil[isRight ? 0 : 2] = data;
								break;
							}
							default: {
								value = data * 0.0027778f + 0.5f;
								frame.rotPupil[isRight ? 0 : 1] = data;
								break;
							}
						}
						break;
					} // Pupil
					case 2: {
						switch (toolType) {
							case 1: {
								value = data / 3.0f + 0.5f;
								frame.offsetBrow[isRight ? 0 : 2] = data;
								break;
							}
							case 2: {
								value = data * 0.5f;
								frame.scaleBrow[isRight ? 0 : 2] = data;
								break;
							}
							default: {
								value = data * 0.0027778f + 0.5f;
								frame.rotBrow[isRight ? 0 : 1] = data;
								break;
							}
						}
						break;
					} // Brow
					case 3: {
						switch (toolType) {
							case 1: {
								value = data / 3.0f + 0.5f;
								frame.offsetMouth[0] = data;
								break;
							}
							case 2: {
								value = data * 0.5f;
								frame.scaleMouth[0] = data;
								break;
							}
							default: {
								value = data * 0.0027778f + 0.5f;
								frame.rotMouth = data;
								break;
							}
						}
						break;
					} // Mouth
				}
				textField.setFullText("" + (Math.round(data * 1000.0f) / 1000.0f));
				if (getSlider(0) != null) { getSlider(0).setSliderValue(value); }
				resetEmtns();
				break;
			} // X
			case 6: {
				float value = 0.0f;
				float data = (float) textField.getDouble();
				switch(elementType) {
					case 0: {
                        if (toolType == 1) {
                            value = data / 3.0f + 0.5f;
                            frame.offsetEye[isRight ? 1 : 3] = data;
                        } else {
                            value = data * 0.5f;
                            frame.scaleEye[isRight ? 1 : 3] = data;
                        }
						break;
					} // Eye
					case 1: {
						if (toolType == 1) {
							value = data / 3.0f + 0.5f;
							frame.offsetPupil[isRight ? 1 : 3] = data;
						} else {
							value = data * 0.5f;
							frame.scalePupil[isRight ? 1 : 3] = data;
						}
						break;
					} // Pupil
					case 2: {
						if (toolType == 1) {
							value = data / 3.0f + 0.5f;
							frame.offsetBrow[isRight ? 1 : 3] = data;
						} else {
							value = data * 0.5f;
							frame.scaleBrow[isRight ? 1 : 3] = data;
						}
						break;
					} // Brow
					case 3: {
						if (toolType == 1) {
							value = data / 3.0f + 0.5f;
							frame.offsetMouth[1] = data;
						} else {
							value = data * 0.5f;
							frame.scaleMouth[1] = data;
						}
						break;
					} // Mouth
				}
				textField.setFullText("" + (Math.round(data * 1000.0f) / 1000.0f));
				if (getSlider(1) != null) { getSlider(1).setSliderValue(value); }
				resetEmtns();
				break;
			} // Y
			case 7: {
				emtn.scaleMoveX = (float) textField.getDouble();
				break;
			} // scale move x
			case 8: {
				emtn.scaleMoveY = (float) textField.getDouble();
				break;
			} // scale move y
		}
	}

}
