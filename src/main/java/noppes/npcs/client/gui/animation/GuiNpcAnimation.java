package noppes.npcs.client.gui.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
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
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig.PartConfig;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.util.AdditionalMethods;

public class GuiNpcAnimation
extends GuiNPCInterface
implements ISubGuiListener, ISliderListener, ICustomScrollListener, ITextfieldListener, IGuiData {

	private static int backColor = 0xFF000000;
	private static EnumAnimationType type = EnumAnimationType.standing;

	private GuiScreen parent;
	private DataAnimation animation;
	private GuiCustomScroll scroll;
	
	// current
	private AnimationConfig currentAnim;
	private AnimationFrameConfig currentFrame;
	private PartConfig currentPart;
	// data
	private int[] rots;
	private long ticksExisted;
	private EntityNPCInterface[] npcs;

	public GuiNpcAnimation(GuiScreen parent, EntityCustomNpc npc) {
		super(npc);
		this.parent = parent;
		this.ySize = 240;
		this.xSize = 427;
		this.animation = npc.animation;
		this.closeOnEsc = true;
		
		this.currentAnim = null;
		this.currentFrame = null;
		this.currentPart = null;
		this.setBackground("bgfilled.png");
		this.rots = new int[] { 180, 180, 180, 180, 0, 0, 0, 0 };
		this.ticksExisted = this.mc.world.getTotalWorldTime();
		this.npcs = new EntityNPCInterface[5];
		NBTTagCompound npcNbt = new NBTTagCompound();
		npc.writeEntityToNBT(npcNbt);
		npc.writeToNBTOptional(npcNbt);
		Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
		if (animNpc instanceof EntityNPCInterface) {
			this.npcs[4] = (EntityNPCInterface) animNpc;
			this.npcs[4].display.setShowName(1);
			MarkData.get(this.npc).marks.clear();
		}
		Client.sendData(EnumPacketServer.AnimationGet);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		AnimationController aData = AnimationController.getInstance();
		Map<String, AnimationConfig> dataAnim = Maps.<String, AnimationConfig>newHashMap();
		if (this.currentAnim!=null && this.currentAnim.type != GuiNpcAnimation.type) { this.currentAnim = null; }
		for (EnumAnimationType t : this.animation.data.keySet()) {
			if (t!=GuiNpcAnimation.type) { continue; }
			List<AnimationConfig> list = this.animation.data.get(t);
			for (AnimationConfig ac : list) {
				dataAnim.put(ac.name, ac);
				if (this.currentAnim == null) { this.currentAnim = ac; }
				else if (this.currentAnim.name.equals(ac.name)) { this.currentAnim = ac; }
			}
		}
		if (this.scroll == null) { this.scroll = new GuiCustomScroll(this, 0); }
		this.scroll.setList(new ArrayList<String>(dataAnim.keySet()));
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 36;
		this.scroll.setSize(120, 156);
		this.addScroll(this.scroll);
		if (this.currentAnim!=null) { this.scroll.setSelected(this.currentAnim.name); }
		
		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 13, this.guiTop+3, 10, 10, "X"));
		
		this.addLabel(new GuiNpcLabel(0, "animation.type", this.guiLeft+4, this.guiTop+4));
		this.addButton(new GuiButtonBiDirectional(1, this.guiLeft+4, this.guiTop+14, 120, 20, EnumAnimationType.getNames(), GuiNpcAnimation.type.ordinal()));
		
		this.addButton(new GuiNpcButton(2, this.guiLeft + 4, this.guiTop + 194, 58, 20, "gui.add"));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 64, this.guiTop + 194, 58, 20, "gui.remove"));
		this.getButton(3).enabled = this.currentAnim!=null;
		this.addButton(new GuiNpcButton(4, this.guiLeft + 4, this.guiTop + 216, 58, 20, "gui.load"));
		this.getButton(4).enabled = aData!=null;
		this.addButton(new GuiNpcButton(5, this.guiLeft + 64, this.guiTop + 216, 58, 20, "gui.save"));
		this.getButton(5).layerColor = this.currentAnim!=null && aData.getAnimation(this.currentAnim.name)==null ? 0xFF00FF00 : 0xFFFFFFFF;
		
		if (this.currentAnim!=null && this.currentFrame!=null && this.currentPart!=null) {
			List<String> list = Lists.newArrayList();
			for (int i=0; i<this.currentAnim.frames.size(); i++) { list.add(""+i); }
			
			int u = this.guiLeft+230, v = this.guiTop+14, s = 65;
			// Display
			GuiNpcButton button;
			button = new GuiNpcCheckBox(18, u, v - 15, 65, 14, this.currentAnim.isDisable() ? "gui.disabled" : "gui.enabled");
			((GuiNpcCheckBox) button).setSelected(this.currentAnim.isDisable());
			this.addButton(button);
			
			for (int i=0; i<4; i++) {
				int un = i%4>2 ? s*2 : i*s, vn = i>2 ? 191 : 91;
				this.addSlider(new GuiNpcSlider(this, 70+i, u+un-1, v+vn, 56, 7, (float) this.rots[i] / 360.0f));
				this.addSlider(new GuiNpcSlider(this, 74+i, u+s+un-9, v+vn-91, 7, 90, 0.005556f * (float) this.rots[4+i] + 0.5f));
				button = new GuiNpcButton(60+i, u+s+un-10, v+vn-1, 8, 8, "");
				button.layerColor = i==0 ? 0xFFF08080 : i==1 ? 0xFF80F080 : i==2 ? 0xFF8080F0 : 0xFFF080F0;
				this.addButton(button);
			}
			u = this.guiLeft + 127;
			v = this.guiTop + 4;
			this.addLabel(new GuiNpcLabel(1, "type.name", u+2, v));
			GuiNpcTextField textField = new GuiNpcTextField(0, this, u+1, v += 10, 97, 15, this.currentAnim.name);
			this.addTextField(textField);
			
			this.addLabel(new GuiNpcLabel(3, "animation.frame", u+2, v += 17));
			this.addButton(new GuiButtonBiDirectional(6, u, v += 10, 99, 20, list.toArray(new String[list.size()]), this.currentFrame.id));
			
			int u1 = u + 186;
			this.addButton(new GuiNpcButton(7, u1, v+84, 18, 18, "")); // head
			this.addButton(new GuiNpcButton(8, u1+19, v+103, 8, 24, "")); // left arm
			this.addButton(new GuiNpcButton(9, u1-9, v+103, 8, 24, "")); // right arm
			this.addButton(new GuiNpcButton(10, u1, v+103, 18, 24, "")); // body
			this.addButton(new GuiNpcButton(11, u1+10, v+128, 8, 24, "")); // left leg
			this.getButton(11).visible = ((ModelPartData) ((EntityCustomNpc) this.npc).modelData.getPartData(EnumParts.LEGS)).type == (byte) 0;
			this.addButton(new GuiNpcButton(12, u1, v+128, 8, 24, "")); // right leg
			this.getButton(12).visible = ((ModelPartData) ((EntityCustomNpc) this.npc).modelData.getPartData(EnumParts.LEGS)).type == (byte) 0;
			this.getButton(7+this.currentPart.part).layerColor = 0xFF70FF70;
			this.addButton(new GuiNpcButton(13, u, v += 22, 48, 12, "gui.add"));
			this.addButton(new GuiNpcButton(14, u + 51, v, 48, 12, "gui.remove"));
			this.getButton(14).enabled = this.currentAnim.frames.size()>1;
			
			this.addLabel(new GuiNpcLabel(17, "gui.time", u+2, v += 14));
			textField = new GuiNpcTextField(10, this, u+1, v += 10, 45, 12, ""+this.currentFrame.getSpeed());
			textField.setNumbersOnly();
			textField.setMinMaxDefault(0, 3600, this.currentFrame.getSpeed());
			this.addTextField(textField);
			textField = new GuiNpcTextField(11, this, u + 52, v, 45, 12, ""+this.currentFrame.getEndDelay());
			textField.setNumbersOnly();
			textField.setMinMaxDefault(0, 3600, this.currentFrame.getEndDelay());
			this.addTextField(textField);
			
			v += 21;
			// rotation, offset, scale
			for (int i=0; i<3; i++) {
				this.addLabel(new GuiNpcLabel(4+i, i==0 ? "movement.rotation" : i==1 ? "type.offset" : "model.scale", u+2, v));
				v += 10;
				for (int j=0; j<3; j++) {
					float valueSlider = this.currentPart.rotation[j];
					float value = (float) (Math.round(3600.0f * valueSlider) / 10.0d);
					if (i==1) {
						valueSlider = this.currentPart.offset[j];
						value = (float) (Math.round((10.0f * valueSlider - 5.0f) * 1000.0f) / 1000.0d);
					}
					else if (i==2) {
						valueSlider = this.currentPart.scale[j];
						value = (float) (Math.round(5000.0f * valueSlider) / 1000.0d);
					}
					this.addSlider(new GuiNpcSlider(this, i * 3 + j, u, v+j*12, 98, 8, valueSlider));
					textField = new GuiNpcTextField(i * 3 + j + 1, this, u+115, v - 2 + j*12, 45, 10, ""+value);
					textField.setDoubleNumbersOnly();
					switch(i) {
						case 1: textField.setMinMaxDoubleDefault(-5.0d, 5.0d, (double) value); break;
						case 2: textField.setMinMaxDoubleDefault(0.0d, 5.0d, (double) value); break;
						default: textField.setMinMaxDoubleDefault(0.0d, 360.0d, (double) value); break;
					}
					this.addLabel(new GuiNpcLabel(7 + i * 3 + j, j==0 ? "X:" : j==1 ? "Y:" : "Z:", u+105, v + j*12));
					this.addTextField(textField);
				}
				v += 32;
			}
			u = this.guiLeft+296;
			v = this.guiTop+205;
			button = new GuiNpcCheckBox(15, u, v, 65, 12, this.currentAnim.isDisable() ? "gui.disabled" : "gui.enabled");
			((GuiNpcCheckBox) button).setSelected(this.currentAnim.isDisable());
			this.addButton(button);
			button = new GuiNpcCheckBox(16, u, v += 14, 65, 12, this.currentFrame.isSmooth() ? "gui.smooth" : "gui.linearly");
			((GuiNpcCheckBox) button).setSelected(this.currentFrame.isSmooth());
			this.addButton(button);
			this.addButton(new GuiNpcButton(17, u, v - 26, 10, 10, new String[] { "b", "w" }, GuiNpcAnimation.backColor==0xFF000000 ? 0 : 1));
			
			if (GuiNpcAnimation.type.isCyclical()) {
				this.addLabel(new GuiNpcLabel(16, "gui.repeat", (u += 60) + 2, v -= 6));
				if (this.currentAnim.repeatLast < 0) { this.currentAnim.repeatLast *= -1; }
				if (this.currentAnim.repeatLast > this.currentAnim.frames.size()) { this.currentAnim.repeatLast = this.currentAnim.frames.size(); }
				
				textField = new GuiNpcTextField(12, this, u, v += 10, 45, 12, ""+this.currentAnim.repeatLast);
				textField.setNumbersOnly();
				textField.setMinMaxDefault(0, this.currentAnim.frames.size(), this.currentAnim.repeatLast);
				this.addTextField(textField);
			}
		}
		this.resetAnims();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (!(button instanceof GuiNpcButton)) { return; }
		GuiNpcButton npcButton = (GuiNpcButton) button;
		switch(npcButton.id) {
			case 1: { // set type
				GuiNpcAnimation.type = EnumAnimationType.values()[npcButton.getValue()];
				this.currentAnim = null;
				this.currentFrame = null;
				this.currentPart = null;
				this.initGui();
				break;
			}
			case 2: { // add anim
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 3: { // del anim
				if (this.currentAnim==null) { return; }
				boolean bo = this.animation.removeAnimation(GuiNpcAnimation.type.ordinal(), this.currentAnim.name);
				if (bo) {
					this.currentAnim = null;
					this.currentFrame = null;
					this.currentPart = null;
				}
				this.initGui();
				break;
			}
			case 4: { // load
				this.setSubGui(new SubGuiLoadAnimation(2, this.npc));
				break;
			}
			case 5: { // save
				if (this.currentAnim==null) { return; }
				AnimationController aData = AnimationController.getInstance();
				AnimationConfig ac = (AnimationConfig) aData.createNew(GuiNpcAnimation.type.ordinal());
				ac.readFromNBT(this.currentAnim.writeToNBT(new NBTTagCompound()));
				this.player.sendMessage(new TextComponentTranslation("animation.message.save", ac.name));
				Client.sendData(EnumPacketServer.AnimationGlobalSave, ac.writeToNBT(new NBTTagCompound()));
				this.initGui();
				break;
			}
			case 6: { // select frame
				if (this.currentAnim==null || this.currentAnim.frames.get(npcButton.getValue()).equals(this.currentFrame)) { return; }
				this.currentFrame = this.currentAnim.frames.get(npcButton.getValue());
				this.currentPart = this.currentFrame.parts[this.currentPart!=null ? this.currentPart.part : 0];
				this.resetAnims();
				this.initGui();
				break;
			}
			case 7: { // select head
				if (this.currentFrame==null) { return; }
				this.currentPart = this.currentFrame.parts[0];
				this.resetAnims();
				this.initGui();
				break;
			}
			case 8: { // select left arm
				if (this.currentFrame==null) { return; }
				this.currentPart = this.currentFrame.parts[1];
				this.resetAnims();
				this.initGui();
				break;
			}
			case 9: { // select right arm
				if (this.currentFrame==null) { return; }
				this.currentPart = this.currentFrame.parts[2];
				this.resetAnims();
				this.initGui();
				break;
			}
			case 10: { // select body
				if (this.currentFrame==null) { return; }
				this.currentPart = this.currentFrame.parts[3];
				this.resetAnims();
				this.initGui();
				break;
			}
			case 11: { // select left leg
				if (this.currentFrame==null) { return; }
				this.currentPart = this.currentFrame.parts[4];
				this.resetAnims();
				this.initGui();
				break;
			}
			case 12: { // select right leg
				if (this.currentFrame==null) { return; }
				this.currentPart = this.currentFrame.parts[5];
				this.resetAnims();
				this.initGui();
				break;
			}
			case 13: { // add frame
				if (this.currentAnim==null) { return; }
				this.currentFrame = (AnimationFrameConfig) this.currentAnim.addFrame(this.currentFrame);
				this.resetAnims();
				this.initGui();
				break;
			}
			case 14: { // del frame
				if (this.currentAnim==null || !this.currentAnim.removeFrame(this.currentFrame)) { return; }
				this.currentFrame = (AnimationFrameConfig) this.currentAnim.getFrame(0);
				this.currentPart = this.currentFrame.parts[0];
				this.resetAnims();
				this.initGui();
				break;
			}
			case 15: { // disabled
				if (this.currentAnim==null) { return; }
				this.currentAnim.setDisable(((GuiNpcCheckBox) button).isSelected());
				((GuiNpcCheckBox) button).setText(this.currentAnim.isDisable() ? "gui.disabled" : "gui.enabled");
				this.resetAnims();
				break;
			}
			case 16: { // smooth
				if (this.currentFrame==null) { return; }
				this.currentFrame.setSmooth(((GuiNpcCheckBox) button).isSelected());
				((GuiNpcCheckBox) button).setText(this.currentFrame.isSmooth() ? "gui.smooth" : "gui.linearly");
				this.resetAnims();
				break;
			}
			case 17: { // back color
				GuiNpcAnimation.backColor = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF: 0xFF000000;
				break;
			}
			case 18: { // disable
				if (this.currentAnim==null) { return; }
				this.currentAnim.disable = ((GuiNpcCheckBox) button).isSelected();
				((GuiNpcCheckBox) button).setText(((GuiNpcCheckBox) button).isSelected() ? "gui.disabled" : "gui.enabled");
				this.resetAnims();
				break;
			}
			case 60: { // reset animation rotation
				if (this.getSlider(70)==null || this.getSlider(74)==null) { return; }
				this.rots[0] = 180;
				this.rots[4] = 0;
				this.getSlider(70).sliderValue = 0.5f;
				this.getSlider(74).sliderValue = 0.5f;
				break;
			}
			case 61: { // reset pre rotation
				if (this.getSlider(71)==null || this.getSlider(75)==null) { return; }
				this.rots[1] = 180;
				this.rots[5] = 0;
				this.getSlider(71).sliderValue = 0.5f;
				this.getSlider(75).sliderValue = 0.5f;
				break;
			}
			case 62: { // reset post rotation
				if (this.getSlider(72)==null || this.getSlider(76)==null) { return; }
				this.rots[2] = 180;
				this.rots[6] = 0;
				this.getSlider(72).sliderValue = 0.5f;
				this.getSlider(76).sliderValue = 0.5f;
				break;
			}
			case 63: { // reset now rotation
				if (this.getSlider(73)==null || this.getSlider(77)==null) { return; }
				this.rots[3] = 180;
				this.rots[7] = 0;
				this.getSlider(73).sliderValue = 0.5f;
				this.getSlider(77).sliderValue = 0.5f;
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.currentAnim!=null) {
			int x = this.guiLeft+230, y = this.guiTop+14;
			int xn = 27, yn = 70, s=65;
			for (int i=0; i<4; i++) {
				if (this.npcs[i]!=null) {
					this.npcs[i].ticksExisted = (int) (this.mc.world.getTotalWorldTime() - this.ticksExisted);
				}
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			for (int i=0; i<3; i++) {
				Gui.drawRect(x-1+i*s, y-1, x+56+i*s, y+91, i==0 ? 0xFFF08080 : i==1 ? 0xFF80F080 : 0xFF8080F0);
				Gui.drawRect(x+i*s, y, x+55+i*s, y+90, GuiNpcAnimation.backColor);
				if (this.npcs[i]!=null) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(xn+i*s+232.0f, yn-10.0f, 50.0f);
					GlStateManager.rotate((float) this.rots[i+4], 1.0f, 0.0f, 0.0f);
					GlStateManager.translate(0.0f, 25.0f, -50.0f);
					this.drawNpc(this.npcs[i], 0, 0, 1.0f, this.rots[i]-180, false);
					GlStateManager.popMatrix();
				}
			}
			x += s;
			y += 100;
			for (int i=0; i<2; i++) {
				Gui.drawRect(x-1+i*s, y-1, x+56+i*s, y+91, i==0 ? 0xFF808080 :  0xFFF080F0);
				Gui.drawRect(x+i*s, y, x+55+i*s, y+90, i==0 ? 0xFFF0F0F0 : GuiNpcAnimation.backColor);
				if (i==1 && this.npcs[3]!=null) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(xn+(i+1)*s+232.0f, yn+90.0f, 25.0f);
					GlStateManager.rotate((float) this.rots[7], 1.0f, 0.0f, 0.0f);
					GlStateManager.translate(0.0f, 25.0f, -50.0f);
					this.drawNpc(this.npcs[3], 0, 0, 1.0f, this.rots[3]-180, false);
					GlStateManager.popMatrix();
				}
			}
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			this.drawVerticalLine(this.guiLeft+125, this.guiTop+3, this.guiTop+this.ySize-3, 0xFF404040);
			this.drawVerticalLine(this.guiLeft+227, this.guiTop+3, this.guiTop+114, 0xFF404040);
			this.drawHorizontalLine(this.guiLeft+126, this.guiLeft+226, this.guiTop+101, 0xFF404040);
			this.drawHorizontalLine(this.guiLeft+227, this.guiLeft+291, this.guiTop+113, 0xFF404040);
			this.drawVerticalLine(this.guiLeft+292, this.guiTop+112, this.guiTop+this.ySize-3, 0xFF404040);
			this.drawHorizontalLine(this.guiLeft+126, this.guiLeft+226, this.guiTop+76, 0xFF404040);
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.isMouseHover(mouseX, mouseY, this.guiLeft+4, this.guiTop+36, 120, 156)) {
			this.setHoverText(new TextComponentTranslation("animation.hover.list").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.type").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.add").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.del").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.load").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.save").getFormattedText());
		} else if (this.currentAnim!=null) {
			if (this.isMouseHover(mouseX, mouseY, this.guiLeft+232, this.guiTop+10, 55, 90)) {
				this.setHoverText(new TextComponentTranslation("animation.hover.animated").getFormattedText());
			} else if (this.isMouseHover(mouseX, mouseY, this.guiLeft+297, this.guiTop+10, 55, 90)) {
				this.setHoverText(new TextComponentTranslation("animation.hover.pre.part").getFormattedText());
			} else if (this.isMouseHover(mouseX, mouseY, this.guiLeft+362, this.guiTop+10, 55, 90)) {
				this.setHoverText(new TextComponentTranslation("animation.hover.post.part").getFormattedText());
			} else if (this.isMouseHover(mouseX, mouseY, this.guiLeft+362, this.guiTop+110, 55, 90)) {
				this.setHoverText(new TextComponentTranslation("animation.hover.now.part").getFormattedText());
			} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.name").getFormattedText());
			} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "X").getFormattedText());
			} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "Y").getFormattedText());
			} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "Z").getFormattedText());
			} else if (this.getTextField(4)!=null && this.getTextField(4).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.offset", "X").getFormattedText());
			} else if (this.getTextField(5)!=null && this.getTextField(5).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.offset", "Y").getFormattedText());
			} else if (this.getTextField(6)!=null && this.getTextField(6).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.offset", "Z").getFormattedText());
			} else if (this.getTextField(7)!=null && this.getTextField(7).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.scale", "X").getFormattedText());
			} else if (this.getTextField(8)!=null && this.getTextField(8).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.scale", "Y").getFormattedText());
			} else if (this.getTextField(9)!=null && this.getTextField(9).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.scale", "Z").getFormattedText());
			} else if (this.getTextField(10)!=null && this.getTextField(10).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part.ticks").getFormattedText());
			} else if (this.getTextField(11)!=null && this.getTextField(11).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part.delay").getFormattedText());
			} else if (this.getTextField(12)!=null && this.getTextField(12).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.anim.repeat").getFormattedText());
			} else if (this.currentFrame!=null && this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.frame", ""+this.currentFrame.id).getFormattedText());
			} else if (this.currentPart!=null && this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.currentPart.part==0)).appendSibling(new TextComponentTranslation("model.head")).getFormattedText());
			} else if (this.currentPart!=null && this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.currentPart.part==1)).appendSibling(new TextComponentTranslation("model.larm")).getFormattedText());
			} else if (this.currentPart!=null && this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.currentPart.part==2)).appendSibling(new TextComponentTranslation("model.rarm")).getFormattedText());
			} else if (this.currentPart!=null && this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.currentPart.part==3)).appendSibling(new TextComponentTranslation("model.body")).getFormattedText());
			} else if (this.currentPart!=null && this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.currentPart.part==4)).appendSibling(new TextComponentTranslation("model.lleg")).getFormattedText());
			} else if (this.currentPart!=null && this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.currentPart.part==5)).appendSibling(new TextComponentTranslation("model.rleg")).getFormattedText());
			} else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.frame.add").getFormattedText());
			} else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.frame.del").getFormattedText());
			} else if (this.currentAnim!=null && this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.disabled."+this.currentAnim.isDisable()).getFormattedText());
			} else if (this.currentFrame!=null && this.getButton(16)!=null && this.getButton(16).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.smooth."+this.currentFrame.isSmooth()).getFormattedText());
			} else if (this.getButton(17)!=null && this.getButton(17).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.color").getFormattedText());
			} else if ((this.getButton(60)!=null && this.getButton(60).isMouseOver()) ||
					(this.getButton(61)!=null && this.getButton(61).isMouseOver()) ||
					(this.getButton(62)!=null && this.getButton(62).isMouseOver()) ||
					(this.getButton(63)!=null && this.getButton(63).isMouseOver())) {
				this.setHoverText(new TextComponentTranslation("quest.reset").getFormattedText());
			} else if (this.getSlider(0)!=null && this.getSlider(0).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "X").getFormattedText());
			} else if (this.getSlider(1)!=null && this.getSlider(1).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "Y").getFormattedText());
			} else if (this.getSlider(2)!=null && this.getSlider(2).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "Z").getFormattedText());
			} else if (this.getSlider(3)!=null && this.getSlider(3).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.offset", "X").getFormattedText());
			} else if (this.getSlider(4)!=null && this.getSlider(4).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.offset", "Y").getFormattedText());
			} else if (this.getSlider(5)!=null && this.getSlider(5).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.offset", "Z").getFormattedText());
			} else if (this.getSlider(6)!=null && this.getSlider(6).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.scale", "X").getFormattedText());
			} else if (this.getSlider(7)!=null && this.getSlider(7).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.scale", "Y").getFormattedText());
			} else if (this.getSlider(8)!=null && this.getSlider(8).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.scale", "Z").getFormattedText());
			} else if ((this.getSlider(70)!=null && this.getSlider(70).isMouseOver()) ||
					(this.getSlider(71)!=null && this.getSlider(71).isMouseOver()) ||
					(this.getSlider(72)!=null && this.getSlider(72).isMouseOver()) ||
					(this.getSlider(73)!=null && this.getSlider(73).isMouseOver())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.rotate").appendSibling(new TextComponentTranslation("animation.hover.hor")).getFormattedText());
			} else if ((this.getSlider(74)!=null && this.getSlider(74).isMouseOver()) ||
					(this.getSlider(75)!=null && this.getSlider(75).isMouseOver()) ||
					(this.getSlider(76)!=null && this.getSlider(76).isMouseOver()) ||
					(this.getSlider(77)!=null && this.getSlider(77).isMouseOver())) {
				this.setHoverText(new TextComponentTranslation("display.hover.part.rotate").appendSibling(new TextComponentTranslation("animation.hover.vert")).getFormattedText());
			}
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		switch(slider.id) {
			case 0: {
				if (this.currentPart==null || this.getTextField(1)==null) { return; }
				this.currentPart.rotation[0] = slider.sliderValue;
				float value = Math.round(3600.0f * slider.sliderValue) / 10.0f;
				this.getTextField(1).setText("" + value);
				this.resetAnims();
				break;
			}
			case 1: {
				if (this.currentPart==null || this.getTextField(2)==null) { return; }
				this.currentPart.rotation[1] = slider.sliderValue;
				float value = Math.round(3600.0f * slider.sliderValue) / 10.0f;
				this.getTextField(2).setText("" + value);
				this.resetAnims();
				break;
			}
			case 2: {
				if (this.currentPart==null || this.getTextField(3)==null) { return; }
				this.currentPart.rotation[2] = slider.sliderValue;
				float value = Math.round(3600.0f * slider.sliderValue) / 10.0f;
				this.getTextField(3).setText("" + value);
				this.resetAnims();
				break;
			}
			case 3: {
				if (this.currentPart==null || this.getTextField(4)==null) { return; }
				this.currentPart.offset[0] = slider.sliderValue;
				double value = Math.round((10.0f * slider.sliderValue - 5.0f) * 1000.0f) / 1000.0d;
				this.getTextField(4).setText("" + value);
				this.resetAnims();
				break;
			}
			case 4: {
				if (this.currentPart==null || this.getTextField(5)==null) { return; }
				this.currentPart.offset[1] = slider.sliderValue;
				double value = Math.round((10.0f * slider.sliderValue - 5.0f) * 1000.0f) / 1000.0d;
				this.getTextField(5).setText("" + value);
				this.resetAnims();
				break;
			}
			case 5: {
				if (this.currentPart==null || this.getTextField(6)==null) { return; }
				this.currentPart.offset[2] = slider.sliderValue;
				double value = Math.round((10.0f * slider.sliderValue - 5.0f) * 1000.0f) / 1000.0d;
				this.getTextField(6).setText("" + value);
				this.resetAnims();
				break;
			}
			case 6: {
				if (this.currentPart==null || this.getTextField(7)==null) { return; }
				this.currentPart.scale[0] = slider.sliderValue;
				float value = Math.round(5000.0f * slider.sliderValue) / 1000.0f;
				this.getTextField(7).setText("" + value);
				this.resetAnims();
				break;
			}
			case 7: {
				if (this.currentPart==null || this.getTextField(8)==null) { return; }
				this.currentPart.scale[1] = slider.sliderValue;
				float value = Math.round(5000.0f * slider.sliderValue) / 1000.0f;
				this.getTextField(8).setText("" + value);
				this.resetAnims();
				break;
			}
			case 8: {
				if (this.currentPart==null || this.getTextField(9)==null) { return; }
				this.currentPart.scale[2] = slider.sliderValue;
				float value = Math.round(5000.0f * slider.sliderValue) / 1000.0f;
				this.getTextField(9).setText("" + value);
				this.resetAnims();
				break;
			}
			case 70: {
				this.rots[0] = (int) (slider.sliderValue * 360.0f);
				break;
			}
			case 71: {
				this.rots[1] = (int) (slider.sliderValue * 360.0f);
				break;
			}
			case 72: {
				this.rots[2] = (int) (slider.sliderValue * 360.0f);
				break;
			}
			case 73: {
				this.rots[3] = (int) (slider.sliderValue * 360.0f);
				break;
			}
			case 74: {
				this.rots[4] = (int) (180.0f * slider.sliderValue - 90.0f);
				break;
			}
			case 75: {
				this.rots[5] = (int) (180.0f * slider.sliderValue - 90.0f);
				break;
			}
			case 76: {
				this.rots[6] = (int) (180.0f * slider.sliderValue - 90.0f);
				break;
			}
			case 77: {
				this.rots[7] = (int) (180.0f * slider.sliderValue - 90.0f);
				break;
			}
		}
		this.npc.updateHitbox();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.AnimationSave, this.animation.writeToNBT(new NBTTagCompound()));
		this.mc.displayGuiScreen(this.parent);
	}
	
	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.animation.readFromNBT(compound);
		this.initGui();
	}
	
	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (this.currentAnim!=null && this.currentAnim.name.equals(scroll.getSelected())) { return; }
		for (AnimationConfig ac : this.animation.data.get(GuiNpcAnimation.type)) {
			if (ac.name.equals(scroll.getSelected())) {
				this.currentAnim = ac;
				this.currentFrame = ac.frames.get(0);
				this.currentPart = this.currentFrame.parts[0];
				break;
			}
		}
		this.resetAnims();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {  }

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText && ((SubGuiEditText) subgui).cancelled) {
			return;
		}
		if (subgui.id == 1) {
			String name = ((SubGuiEditText) subgui).text[0];
			boolean next = true;
			while(next) {
				next = false;
				for (AnimationConfig acn : this.animation.data.get(GuiNpcAnimation.type)) {
					if (acn.name.equals(name)) {
						name += "_";
						next = true;
						break;
					}
				}
			}
			AnimationConfig ac = this.animation.createAnimation(GuiNpcAnimation.type.ordinal());
			ac.name = name;
			this.currentAnim = ac;
			this.currentFrame = ac.frames.get(0);
			this.currentPart = this.currentFrame.parts[0];
			for (int i = 0; i<4; i++) {
				this.rots[i] = 180;
				this.rots[i+4] = 0;
			}
			this.resetAnims();
			this.initGui();
		}
		if (subgui.id == 2) {
			if (((SubGuiLoadAnimation) subgui).animation==null) { return; }
			AnimationConfig ac = ((SubGuiLoadAnimation) subgui).animation.copy();
			ac.type = GuiNpcAnimation.type;
			this.animation.data.get(ac.type).add(ac);
			this.currentAnim = ac;
			this.currentFrame = ac.frames.get(0);
			this.currentPart = this.currentFrame.parts[0];
			this.resetAnims();
			this.initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.currentAnim==null) { return; }
		switch(textField.getId()) {
			case 0: { // name
				this.currentAnim.name = textField.getText();
				this.resetAnims();
				break;
			}
			case 1: { // rotation X
				if (this.currentPart==null || this.getSlider(0)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.rotation[0] = (float) (textField.getDouble()) / 360.0f;
				this.getSlider(0).sliderValue = this.currentPart.rotation[0];
				this.resetAnims();
				break;
			}
			case 2: { // rotation Y
				if (this.currentPart==null || this.getSlider(1)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.rotation[1] = (float) (textField.getDouble()) / 360.0f;
				this.getSlider(1).sliderValue = this.currentPart.rotation[1];
				this.resetAnims();
				break;
			}
			case 3: { // rotation Z
				if (this.currentPart==null || this.getSlider(2)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.rotation[2] = (float) (textField.getDouble()) / 360.0f;
				this.getSlider(2).sliderValue = this.currentPart.rotation[2];
				this.resetAnims();
				break;
			}
			case 4: { // offset X
				if (this.currentPart==null || this.getSlider(3)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.offset[0] = 0.1f * (float) (textField.getDouble()) + 0.5f;
				this.getSlider(3).sliderValue = this.currentPart.offset[0];
				this.resetAnims();
				break;
			}
			case 5: { // offset Y
				if (this.currentPart==null || this.getSlider(4)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.offset[1] = 0.1f * (float) (textField.getDouble()) + 0.5f;
				this.getSlider(4).sliderValue = this.currentPart.offset[1];
				this.resetAnims();
				break;
			}
			case 6: { // offset Z
				if (this.currentPart==null || this.getSlider(5)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.offset[2] = 0.1f * (float) (textField.getDouble()) + 0.5f;
				this.getSlider(5).sliderValue = this.currentPart.offset[2];
				this.resetAnims();
				break;
			}
			case 7: { // scale X
				if (this.currentPart==null || this.getSlider(6)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.scale[0] = (float) (textField.getDouble()) / 5.0f;
				this.getSlider(6).sliderValue = this.currentPart.scale[0];
				this.resetAnims();
				break;
			}
			case 8: { // scale Y
				if (this.currentPart==null || this.getSlider(7)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.scale[1] = (float) (textField.getDouble()) / 5.0f;
				this.getSlider(7).sliderValue = this.currentPart.scale[1];
				this.resetAnims();
				break;
			}
			case 9: { // scale Z
				if (this.currentPart==null || this.getSlider(8)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				this.currentPart.scale[2] = (float) (textField.getDouble()) / 5.0f;
				this.getSlider(8).sliderValue = this.currentPart.scale[2];
				this.resetAnims();
				break;
			}
			case 10: { // speed
				if (this.currentFrame==null) { return; }
				this.currentFrame.setSpeed(textField.getInteger());
				this.resetAnims();
				break;
			}
			case 11: { // delay
				if (this.currentFrame==null) { return; }
				this.currentFrame.setEndDelay(textField.getInteger());
				this.resetAnims();
				break;
			}
			case 12: { // repeatLast
				if (this.currentAnim==null) { return; }
				this.currentAnim.setRepeatLast(textField.getInteger());
				this.resetAnims();
				break;
			}
		}
	}
	
	private void resetAnims() {
		if (this.currentAnim == null || this.currentFrame==null || this.currentPart==null || this.npcs[4] == null) {
			this.npcs[0] = null;
			this.npcs[1] = null;
			this.npcs[2] = null;
			this.npcs[3] = null;
			return;
		}
		AnimationConfig ac = this.currentAnim.copy();
		ac.disable = false;
		ac.type = EnumAnimationType.standing;
		NBTTagCompound npcNbt = new NBTTagCompound();
		this.npcs[4].writeEntityToNBT(npcNbt);
		this.npcs[4].writeToNBTOptional(npcNbt);
		if (this.npcs[0]==null) {
			Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
			if (animNpc instanceof EntityNPCInterface) {
				this.npcs[0] = (EntityNPCInterface) animNpc;
				this.npcs[0].animation.clear();
			}
		}
		if (this.npcs[0]!=null) {
			((EntityNPCInterface) this.npcs[0]).display.setName("0_"+this.npc.getName());
			this.npcs[0].animation.activeAnim = ac;
		}
		
		// Pre, Post, Now
		for (int p=1; p<4; p++) {
			int frame = this.currentFrame.id;
			if (p==1) { frame -= 1; } else if (p==2) { frame += 1; }
			if (!ac.frames.containsKey(frame) && GuiNpcAnimation.type.isCyclical()) {
				if (frame==-1 || frame == ac.frames.size()) { frame = 0; }
			}
			if (!ac.frames.containsKey(frame)) {
				this.npcs[p] = null;
				continue;
			}
			if (this.npcs[p]==null) {
				Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
				if (animNpc instanceof EntityNPCInterface) {
					this.npcs[p] = (EntityNPCInterface) animNpc;
					this.npcs[p].animation.clear();
				}
			}
			if (this.npcs[p]!=null) {
				((EntityNPCInterface) this.npcs[p]).display.setName(p+"_"+this.npc.getName());
				if (this.npcs[p].animation.activeAnim==null) {
					if (this.npcs[p].animation.data.get(EnumAnimationType.standing).isEmpty()) {
						this.npcs[p].animation.activeAnim = new AnimationConfig(EnumAnimationType.standing.ordinal());
						this.npcs[p].animation.data.get(EnumAnimationType.standing).add(this.npcs[p].animation.activeAnim);
					}
					else { this.npcs[p].animation.activeAnim = this.npcs[p].animation.data.get(EnumAnimationType.standing).get(0); }
				}
				if (!this.npcs[p].animation.activeAnim.frames.containsKey(0)) { this.npcs[p].animation.activeAnim.addFrame(); }
				for (int i=0; i<6; i++) {
					this.npcs[p].animation.activeAnim.frames.get(0).parts[i].readNBT(ac.frames.get(frame).parts[i].writeNBT());
				}
			}
		}
	}
	
}
