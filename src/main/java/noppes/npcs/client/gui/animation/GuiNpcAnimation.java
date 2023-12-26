package noppes.npcs.client.gui.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
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
import noppes.npcs.client.model.part.ModelPartData;
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
implements ISubGuiListener, ISliderListener, ICustomScrollListener, ITextfieldListener, IGuiData, GuiYesNoCallback {

	public static int backColor = 0xFF000000;
	private static AnimationKind type = AnimationKind.STANDING;
	private static int blockType = 0; // 0 - stone, 1-stairs, 2-stone_slab, 3-carpet, 4 - non
	private static int blockSize = 0; // 0 - x1, 1 - x3, 2 - x5

	// data
	private GuiScreen parent;
	private GuiCustomScroll scroll, scrollSet;
	private String selAnim;
	private int selFrame, selPart;
	private final Map<String, Integer> data;
	private boolean onlyCurrentPart;
	private EntityNPCInterface npcAnim, npcPart;
	// display
	private final float[] dispRot, dispPos;
	private float dispScale;
	private final int[] mouseC;
	// private Framebuffer framebuffer;
	private DataAnimation animation;

	public GuiNpcAnimation(GuiScreen parent, EntityCustomNpc npc) {
		super(npc);
		this.parent = parent;
		this.ySize = 240;
		this.xSize = 427;
		this.animation = npc.animation;
		this.closeOnEsc = true;
		
		// Display
		this.dispScale = 1.0f;
		this.dispRot = new float[] { 0.0f, 0.0f, 0.0f };
		this.dispPos = new float[] { 0.0f, 0.0f, 0.0f };
		this.mouseC = new int[] { -1, 0, 0, -1, -1, 0 };
		
		this.selAnim = "";
		this.selFrame = 0;
		this.selPart = 0;
		this.onlyCurrentPart = false;
		this.data = Maps.<String, Integer>newHashMap();
		this.setBackground("bgfilled.png");
		NBTTagCompound npcNbt = new NBTTagCompound();
		npc.writeEntityToNBT(npcNbt);
		npc.writeToNBTOptional(npcNbt);
		Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
		if (animNpc instanceof EntityNPCInterface) {
			this.npcAnim = (EntityNPCInterface) animNpc;
			this.npcAnim.display.setShowName(1);
			MarkData.get(this.npc).marks.clear();
		}
		Client.sendData(EnumPacketServer.AnimationGet);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		AnimationController aData = AnimationController.getInstance();
		this.data.clear();
		int id = 0;
		for (AnimationConfig ac : this.animation.data.get(GuiNpcAnimation.type)) {
			ac.id = id;
			this.data.put(ac.getSettingName(), ac.id);
			id++;
		}
		AnimationConfig anim = this.getAnim();
		AnimationFrameConfig frame = this.getFrame(anim);
		PartConfig part = this.getPart(frame);
		
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(120, 156); }
		this.scroll.setList(new ArrayList<String>(this.data.keySet()));
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 36;
		this.addScroll(this.scroll);
		if (!this.selAnim.isEmpty()) { this.scroll.setSelected(this.selAnim); }
		
		this.addLabel(new GuiNpcLabel(0, "animation.type", this.guiLeft+4, this.guiTop+4));
		GuiNpcButton button = new GuiButtonBiDirectional(1, this.guiLeft+4, this.guiTop+14, 120, 20, AnimationKind.getNames(), GuiNpcAnimation.type.get());
		((GuiButtonBiDirectional) button).showShedow = false;
		this.addButton(button);

		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 17, this.guiTop + 5, 12, 12, "X"));
		
		this.addButton(new GuiNpcButton(2, this.guiLeft + 4, this.guiTop + 194, 59, 20, "gui.add"));
		this.getButton(2).enabled = GuiNpcAnimation.type == AnimationKind.BASE ? this.data.isEmpty() : true;
		this.addButton(new GuiNpcButton(3, this.guiLeft + 64, this.guiTop + 194, 59, 20, "gui.remove"));
		this.getButton(3).enabled = anim != null;
		this.addButton(new GuiNpcButton(4, this.guiLeft + 4, this.guiTop + 216, 59, 20, "gui.load"));
		this.getButton(4).enabled = aData!=null;
		this.addButton(new GuiNpcButton(5, this.guiLeft + 64, this.guiTop + 216, 59, 20, "gui.save"));
		this.getButton(5).layerColor = anim != null ? 0xFF00FF00 : 0xFFFFFFFF;
		
		this.mouseC[3] = -1;
		this.mouseC[4] = -1;
		this.mouseC[5] = 0;
		if (anim!=null && frame!=null && part!=null) {

			this.mouseC[3] = this.scroll.guiLeft + this.scroll.width + 141;
			this.mouseC[4] = this.guiTop + 33;
			this.mouseC[5] = 155;
			
			// Mini
			int u = this.scroll.guiLeft + this.scroll.width + 3;
			int v = this.guiTop + this.ySize - 47;
			int sel = 0;
			
			if (this.scrollSet == null) { (this.scrollSet = new GuiCustomScroll(this, 1)).setSize(50, 43); }
			else { sel = this.scrollSet.selected; }
			this.scrollSet.setListNotSorted(Lists.<String>newArrayList(new String[] { "movement.rotation", "type.offset", "model.scale" }));
			this.scrollSet.guiLeft = u;
			this.scrollSet.guiTop = v;
			this.scrollSet.selected = sel;
			this.scrollSet.hoverText = new String[] { "animation.hover.setting.type" };
			this.addScroll(this.scrollSet);

			this.addButton(new GuiNpcButton(6, u + 53, v - 17, 50, 14, this.onlyCurrentPart ? "animation.frame" : "movement.animation"));
			button = new GuiNpcCheckBox(7, u + 105, v - 18, 32, 14, anim.isDisable() ? "Off" : "On");
			((GuiNpcCheckBox) button).setSelected(anim.isDisable());
			this.addButton(button);
			
			v -= 74;
			u += 16;
			this.addButton(new GuiNpcButton(8, u, v, 18, 18, "")); // head
			this.addButton(new GuiNpcButton(9, u+19, v+19, 8, 24, "")); // left arm
			this.addButton(new GuiNpcButton(10, u-9, v+19, 8, 24, "")); // right arm
			this.addButton(new GuiNpcButton(11, u, v+19, 18, 24, "")); // body
			this.addButton(new GuiNpcButton(12, u+10, v+44, 8, 24, "")); // left leg
			this.getButton(12).visible = ((ModelPartData) ((EntityCustomNpc) this.npc).modelData.getPartData(EnumParts.LEGS)).type == (byte) 0;
			this.addButton(new GuiNpcButton(13, u, v+44, 8, 24, "")); // right leg
			this.getButton(13).visible = ((ModelPartData) ((EntityCustomNpc) this.npc).modelData.getPartData(EnumParts.LEGS)).type == (byte) 0;
			for (int i = 0; i < 6; i++) {
				if (frame.parts[i].isDisable()) { this.getButton(8 + i).layerColor = 0xFFFF7070; }
			}
			this.getButton(8+part.id).layerColor = 0xFF70FF70;
			
			// Work place
			u -= 16;
			v = this.guiTop + 4;
			this.addLabel(new GuiNpcLabel(1, new TextComponentTranslation("type.name").getFormattedText() + ":", u+2, v));
			GuiNpcTextField textField = new GuiNpcTextField(0, this, u+1, v += 10, 134, 15, anim.name);
			this.addTextField(textField);
			
			String[] blocks = new String[5];
			blocks[4] = "gui.none";
			for (int i = 0; i<4; i++) {
				Block block;
				switch(i) {
					case 1: block = Blocks.STONE_STAIRS; break;
					case 2: block = Blocks.STONE_SLAB; break;
					case 3: block = Blocks.CARPET; break;
					default: block = Blocks.STONE; break;
				}
				blocks[i] = new ItemStack(block).getDisplayName();
			}
			this.addButton(new GuiNpcButton(14, u + 139, v -= 1, 17, 17, new String[] { "b", "w" }, GuiNpcAnimation.backColor==0xFF000000 ? 0 : 1));
			this.addButton(new GuiNpcButton(15, u + 166, v, 80, 17, blocks, GuiNpcAnimation.blockType));
			String[] sizes = new String[] { "x1", "x3", "x5" };
			this.addButton(new GuiNpcButton(16, this.getButton(15).x + 82, v, 25, 17, sizes, GuiNpcAnimation.blockSize));
			this.getButton(16).setEnabled(GuiNpcAnimation.blockType != 4);

			this.addLabel(new GuiNpcLabel(2, new TextComponentTranslation("animation.frame", ":").getFormattedText(), u+2, v += 17));
			List<String> list = Lists.newArrayList();
			for (int i = 0; i < anim.frames.size(); i++) { list.add("" + (i + 1) + "/" + anim.frames.size()); }
			this.addButton(new GuiButtonBiDirectional(17, u, v += 10, 76, 20, list.toArray(new String[list.size()]), frame.id));
			
			this.addButton(new GuiNpcButton(18, u + 78, v - 4, 56, 12, "gui.add"));
			this.addButton(new GuiNpcButton(19, u + 78, v + 9, 56, 12, "gui.remove"));
			this.getButton(19).enabled = anim.frames.size() > 1;
			
			this.addLabel(new GuiNpcLabel(2, new TextComponentTranslation("gui.time").getFormattedText() + ":", u+2, v += 23));
			textField = new GuiNpcTextField(10, this, u + 39, v, 45, 12, ""+frame.getSpeed());
			textField.setNumbersOnly();
			textField.setMinMaxDefault(0, 3600, frame.getSpeed());
			this.addTextField(textField);
			textField = new GuiNpcTextField(3, this, u + 88, v, 45, 12, ""+frame.getEndDelay());
			textField.setNumbersOnly();
			textField.setMinMaxDefault(0, 3600, frame.getEndDelay());
			this.addTextField(textField);
			
			// Clears
			button = new GuiNpcButton(20, u, v += 17 , 50, 14, "movement.animation");
			button.layerColor = 0xFFFF0000;
			this.addButton(button);
			button = new GuiNpcButton(21, u, v += 18, 50, 14, "animation.frame");
			button.layerColor = 0xFFFFFF00;
			this.addButton(button);
			
			// Part sets
			button = new GuiNpcCheckBox(22, u += 54, v += 18, 80, 14, part.isDisable() ? "gui.disabled" : "gui.enabled");
			((GuiNpcCheckBox) button).setSelected(part.isDisable());
			this.addButton(button);
			button = new GuiNpcCheckBox(23, u, v += 14, 80, 14, frame.isSmooth() ? "gui.smooth" : "gui.linearly");
			((GuiNpcCheckBox) button).setSelected(frame.isSmooth());
			this.addButton(button);
			
			if (GuiNpcAnimation.type == AnimationKind.DIES) {
				this.addLabel(new GuiNpcLabel(4, new TextComponentTranslation("gui.repeat").getFormattedText() + ":", u, v += 18));
				if (anim.repeatLast < 0) { anim.repeatLast *= -1; }
				if (anim.repeatLast > anim.frames.size()) { anim.repeatLast = anim.frames.size(); }
				
				textField = new GuiNpcTextField(4, this, u, v += 12, 45, 12, ""+anim.repeatLast);
				textField.setNumbersOnly();
				textField.setMinMaxDefault(0, anim.frames.size(), anim.repeatLast);
				this.addTextField(textField);
			}
			
			// Sliders
			u = this.scroll.guiLeft + this.scroll.width + 57;
			v = this.guiTop + this.ySize - 43;
			if (this.scrollSet.selected > -1) {
				int s = this.scrollSet.selected, f = 14;
				for (int i = 0; i < 3; i++) {
					this.addLabel(new GuiNpcLabel(5 + i, i==0 ? "X:" : i==1 ? "Y:" : "Z:", u, v + i * f));
					float[] values = s == 0 ? part.rotation : s == 1 ? part.offset : part.scale;
					float[] datas = new float[3];
					switch(s) {
						case 1: {
							for (int j = 0; j < 3; j++) { datas[j] = (float) (Math.round((10.0f * values[i] - 5.0f) * 1000.0f) / 1000.0d); }
							break;
						}
						case 2: {
							for (int j = 0; j < 3; j++) { datas[j] = (float) (Math.round(5000.0f * values[i]) / 1000.0d); }
							break;
						}
						default: {
							for (int j = 0; j < 3; j++) { datas[j] = (float) (Math.round(3600.0f * values[i]) / 10.0d); }
							break;
						}
					}
					this.addSlider(new GuiNpcSlider(this, i, u + 8, v + i * f, 166, 8, values[i]));
					
					textField = new GuiNpcTextField(i + 5, this, u + 177, v + i * f, 51, 8, ""+datas[i]);
					textField.setDoubleNumbersOnly();
					double m = 0.0d, n = 360.0d;
					if (this.scrollSet.selected == 1) { m = -5.0d; n = 5.0d; }
					else if (this.scrollSet.selected == 1) { m = 0.0d; n = 5.0d; }
					switch(i) {
						case 1: textField.setMinMaxDoubleDefault(m, n, (double) datas[i]); break;
						case 2: textField.setMinMaxDoubleDefault(m, n, (double) datas[i]); break;
						default: textField.setMinMaxDoubleDefault(m, n, (double) datas[i]); break;
					}
					this.addTextField(textField);
					button = new GuiNpcButton(30 + i, u + 231, v - 1 + i * f, 10, 10, "x");
					button.dropShadow = false;
					this.addButton(button);
				}
			}
			// display
			button = new GuiNpcButton(24, this.guiLeft + this.xSize - 17, this.guiTop + 19, 12, 12, "x");
			button.packedFGColour = 0xFFC0C0C0;
			button.dropShadow = false;
			button.layerColor = 0xFFF080F0;
			this.addButton(button);
		} else { this.clearDisplay(); }
		this.resetAnims();
	}

	private AnimationConfig getAnim() {
		AnimationConfig anim = null;
		if (this.selAnim.isEmpty()) {
			if (!this.animation.data.get(GuiNpcAnimation.type).isEmpty()) {
				anim = this.animation.data.get(GuiNpcAnimation.type).get(0);
				this.selAnim = anim.getSettingName();
			}
		}
		else if (this.data.containsKey(this.selAnim) && this.data.get(this.selAnim) < this.animation.data.get(GuiNpcAnimation.type).size()) {
			anim = this.animation.data.get(GuiNpcAnimation.type).get(this.data.get(this.selAnim));
		} else {
			this.selAnim = "";
		}
		return anim;
	}
	
	private AnimationFrameConfig getFrame(AnimationConfig anim) {
		if (anim == null) { return null; }
		if (this.getButton(17) != null) { this.selFrame = this.getButton(17).getValue(); }
		if (this.selFrame < 0) { this.selFrame = 0; }
		else if (this.selFrame >= anim.frames.size()) { this.selFrame = anim.frames.size() - 1; }
		return anim.frames.get(this.selFrame);
	}

	private PartConfig getPart(AnimationFrameConfig frame) {
		if (frame == null) { return null; }
		if (this.selPart < 0) { this.selPart = 0; }
		else if (this.selPart > frame.parts.length) { this.selPart = 0; }
		return frame.parts[this.selPart];
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (!(button instanceof GuiNpcButton)) { return; }
		GuiNpcButton npcButton = (GuiNpcButton) button;
		AnimationConfig anim = this.getAnim();
		AnimationFrameConfig frame = this.getFrame(anim);
		PartConfig part = this.getPart(frame);
		switch(npcButton.id) {
			case 1: { // set type
				GuiNpcAnimation.type = AnimationKind.values()[npcButton.getValue()];
				this.selAnim = "";
				this.selFrame = 0;
				this.selPart = 0;
				this.initGui();
				break;
			}
			case 2: { // add anim
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 3: { // del anim
				if (anim==null) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("movement.animation").getFormattedText() + "\"" + anim.getName() + "\"", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 4: { // load anim
				this.setSubGui(new SubGuiLoadAnimation(2, this.npc));
				break;
			}
			case 5: { // save anim
				if (anim==null) { return; }
				Client.sendData(EnumPacketServer.AnimationGlobalSave, anim.writeToNBT(new NBTTagCompound()));
				this.initGui();
				break;
			}
			case 6: { // npc show
				this.onlyCurrentPart = !this.onlyCurrentPart;
				this.initGui();
				break;
			}
			case 7: { // disable
				if (anim==null) { return; }
				anim.setDisable(((GuiNpcCheckBox) button).isSelected());
				((GuiNpcCheckBox) button).setText(anim.isDisable() ? "Off" : "On");
				break;
			}
			case 8: { // select head
				if (frame==null) { return; }
				this.selPart = 0;
				this.initGui();
				break;
			}
			case 9: { // select left arm
				if (frame==null) { return; }
				this.selPart = 1;
				this.initGui();
				break;
			}
			case 10: { // select right arm
				if (frame==null) { return; }
				this.selPart = 2;
				this.initGui();
				break;
			}
			case 11: { // select body
				if (frame==null) { return; }
				this.selPart = 3;
				this.initGui();
				break;
			}
			case 12: { // select left leg
				if (frame==null) { return; }
				this.selPart = 4;
				this.initGui();
				break;
			}
			case 13: { // select right leg
				if (frame==null) { return; }
				this.selPart = 5;
				this.initGui();
				break;
			}
			case 14: { // back color
				GuiNpcAnimation.backColor = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF: 0xFF000000;
				break;
			}
			case 15: { // block type
				GuiNpcAnimation.blockType = npcButton.getValue();
				if (this.getButton(16)!=null) { this.getButton(16).setVisible(GuiNpcAnimation.blockType != 4); }
				break;
			}
			case 16: { // block size
				GuiNpcAnimation.blockSize = npcButton.getValue();
				break;
			}
			case 17: { // select frame
				if (anim==null) { return; }
				this.selFrame = npcButton.getValue();
				frame = this.getFrame(anim);
				if (frame==null) { this.selPart = 0; }
				this.initGui();
				break;
			}
			case 18: { // add frame
				if (anim==null) { return; }
				frame = (AnimationFrameConfig) anim.addFrame(frame);
				if (frame==null) {
					this.selFrame = 0;
					this.selPart = 0;
				}
				else { this.selFrame = frame.id; }
				this.initGui();
				break;
			}
			case 19: { // del frame
				if (anim==null || frame==null) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("movement.animation").getFormattedText() + "\"" + anim.getName() + "\": " + new TextComponentTranslation("animation.frame", ""+(frame.id+1)).getFormattedText(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 1);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 20: { // clear All Parts in frame
				if (frame==null) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("animation.clear.parts", ""+(frame.id+1)).getFormattedText(), new TextComponentTranslation("gui.clearMessage").getFormattedText(), 0);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 21: { // clear set Part
				if (part==null) { return; }
				part.clear();
				this.initGui();
				break;
			}
			case 22: { // disabled part
				if (anim==null || part==null) { return; }
				part.setDisable(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					for (AnimationFrameConfig f : anim.frames.values()) {
						f.parts[this.selPart].setDisable(part.isDisable());
					}
				}
				((GuiNpcCheckBox) button).setText(part.isDisable() ? "gui.disabled" : "gui.enabled");
				this.resetAnims();
				break;
			}
			case 23: { // smooth
				if (anim==null || frame==null) { return; }
				frame.setSmooth(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					for (AnimationFrameConfig f : anim.frames.values()) {
						f.setSmooth(frame.isSmooth());
					}
				}
				((GuiNpcCheckBox) button).setText(frame.isSmooth() ? "gui.smooth" : "gui.linearly");
				this.resetAnims();
				break;
			}
			case 24: { // reset now rotation
				this.dispScale = 1.0f;
				for (int i = 0; i < 3; i++) {
					this.dispRot[i] = 0.0f;
					this.dispPos[i] = 0.0f;
				}
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
			case 30: { // reset part set X
				if (part==null) { return; }
				switch(this.scrollSet.selected) {
					case 0: part.rotation[0] = 0.5f; break;
					case 1: part.offset[0] = 0.5f; break;
					case 2: part.scale[0] = 0.2f; break;
				}
				this.initGui();
				break;
			}
			case 31: { // reset part set Y
				if (part==null) { return; }
				switch(this.scrollSet.selected) {
					case 0: part.rotation[1] = 0.5f; break;
					case 1: part.offset[1] = 0.5f; break;
					case 2: part.scale[1] = 0.2f; break;
				}
				this.initGui();
				break;
			}
			case 32: { // reset part set Z
				if (part==null) { return; }
				switch(this.scrollSet.selected) {
					case 0: part.rotation[2] = 0.5f; break;
					case 1: part.offset[2] = 0.5f; break;
					case 2: part.scale[2] = 0.2f; break;
				}
				this.initGui();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.getEventButton() != this.mouseC[0]) { this.mouseC[0] = -1; }
		AnimationConfig anim = this.getAnim();
		AnimationFrameConfig frame = this.getFrame(anim);
		PartConfig part = this.getPart(frame);
		if (anim != null) {
			if (this.mouseC[0] != -1) {
				int x = mouseX - this.mouseC[1];
				int y = mouseY - this.mouseC[2];
				if (x!=0 || y!=0) {
					if (this.mouseC[0]==0) { this.displayOffset(x, y); }// LMB
					else if (this.mouseC[0]==1) { this.displayRotate(x, y); } // RMB
					this.mouseC[1] = mouseX;
					this.mouseC[2] = mouseY;
				}
			}
			int wheel = Mouse.getDWheel();
			if (wheel!=0) {
				this.dispScale += this.dispScale * (wheel < 0 ? 0.1f : -0.1f);
				if (this.dispScale < 0.5f) { this.dispScale = 0.5f; }
				else if (this.dispScale > 5.0f) { this.dispScale = 5.0f; }
				this.dispScale = (float) (Math.round(this.dispScale * 20.0d) / 20.0d);
				if (this.dispScale == 0.95f || this.dispScale == 1.05f) { this.dispScale = 1.0f; }
			}
			// Lines
			if (this.scroll!=null && this.scrollSet!=null) {
				int u = this.scroll.guiLeft + this.scroll.width + 1;
				int v = this.guiTop + this.ySize - 1;
				GlStateManager.pushMatrix();
				GlStateManager.translate(0.0f, 0.0f, 1.0f);
				this.drawVerticalLine(u, this.guiTop+3, v, 0xFF808080);
				v -= this.scrollSet.height + 5;
				this.drawHorizontalLine(u, u + 297, v, 0xFF808080);
				this.drawHorizontalLine(u, u + 137, v - 114, 0xFF808080);
				this.drawVerticalLine(u+53, v - 115, v + 48, 0xFF808080);
				u += 3; v -= 74;
				Gui.drawRect(u, v, u + 48, v + 72, 0xFF202020);
				u += 1; v += 1;
				this.drawGradientRect(u, v, u + 46, v + 70, 0xFFC0C0C0, 0xFF808080);
				u -= 4; v -= 4;
				this.drawHorizontalLine(u, u + 52, v, 0xFF808080);
				GlStateManager.popMatrix();
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (anim != null && this.subgui == null) {
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			int u = this.mouseC[3];
			int v = this.mouseC[4];
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(u, v, u + this.mouseC[5], v + this.mouseC[5], GuiNpcAnimation.backColor==0xFF000000 ? 0xFFF080F0 : 0xFFF020F0);
			GlStateManager.popMatrix();
			// NPC
			EntityNPCInterface npc = this.onlyCurrentPart ? this.npcPart : this.npcAnim;
			if (npc!=null) {
				/*GlStateManager.pushMatrix();
				int current = GlStateManager.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
				if (this.framebuffer == null) {
					this.framebuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, false);
				}
				if (this.mc.displayWidth != this.framebuffer.framebufferWidth || this.mc.displayHeight != this.framebuffer.framebufferHeight) {
					this.framebuffer.createBindFramebuffer(this.mc.displayWidth, this.mc.displayHeight);
				}
				this.framebuffer.bindFramebuffer(false);
				this.mc.getFramebuffer().bindFramebuffer(false);
				GlStateManager.bindTexture(0);
				this.drawWork(npc);
				this.framebuffer.framebufferClear();
				this.mc.getFramebuffer().bindFramebuffer(false);
				GlStateManager.bindTexture(current);
				GlStateManager.popMatrix();*/

				GlStateManager.pushMatrix();
				this.drawWork(npc);
				GlStateManager.popMatrix();
				
				u = this.mouseC[3];
				v = this.mouseC[4];
				// axis xyz
				GlStateManager.pushMatrix();
				GlStateManager.translate(u + 12.5f, v + 12, 120.0f * this.dispScale);
				if (this.dispRot[0]!=0.0f) { GlStateManager.rotate(this.dispRot[0], 0.0f, 1.0f, 0.0f); }
				this.drawCRect(-10.5d, -0.5d, -1.0d, 0.5d, 0xFFFF0000);
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.translate(u + 12.5f, v + 12.0f, 120.0f * this.dispScale);
				GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
				if (this.dispRot[0]!=0.0f) { GlStateManager.rotate(this.dispRot[0], 0.0f, 1.0f, 0.0f); }
				if (this.dispRot[1]!=0.0f) { GlStateManager.rotate(this.dispRot[1], 0.0f, 0.0f, 1.0f); }
				this.drawCRect(-10.5, -0.5d, -0.5d, 0.5d, 0xFF0000FF);
				this.drawCRect(-0.5d, -10.5, 0.5d, -0.5d, 0xFF00D000);
				this.drawCRect(-0.5d, -0.5d, 0.5d, 0.5d, GuiNpcAnimation.backColor==0xFF000000 ? 0xFFFFFFFF : 0xFF000000);
				GlStateManager.popMatrix();
				
				// display info
				GlStateManager.pushMatrix();
				GlStateManager.translate(u, v, 120.0f * this.dispScale);
				String ts = "x"+this.dispScale;
				this.fontRenderer.drawString(ts, this.mouseC[5] - 2 - this.fontRenderer.getStringWidth(ts), 1, GuiNpcAnimation.backColor==0xFF000000 ? 0xFFFFFFFF : 0xFF000000, false);
				ts = (int) this.dispRot[0] + "" + ((char) 176) + "/" + (int) this.dispRot[1] + ((char) 176);
				this.fontRenderer.drawString(ts, this.mouseC[5] - 2 - this.fontRenderer.getStringWidth(ts), this.mouseC[5] - 10, GuiNpcAnimation.backColor==0xFF000000 ? 0xFFFFFFFF : 0xFF000000, false);
				ts = (int) this.dispPos[0] + "/" + (int) this.dispPos[1];
				this.fontRenderer.drawString(ts, 2, this.mouseC[5] - 10, GuiNpcAnimation.backColor==0xFF000000 ? 0xFFFFFFFF : 0xFF000000, false);
				GlStateManager.popMatrix();
			}
		}
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.scroll!=null && this.isMouseHover(mouseX, mouseY, this.scroll.guiLeft, this.scroll.guiTop, this.scroll.width, this.scroll.height)) { // scroll anim
			this.setHoverText(new TextComponentTranslation("animation.hover.list").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.type", "\""+AdditionalMethods.instance.deleteColor(this.getButton(1).displayString)+"\"").
					appendSibling(new TextComponentTranslation("animation.hover.type."+GuiNpcAnimation.type.get())).
					appendSibling(new TextComponentTranslation("animation.hover.type.15"))
					.getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.add").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.del").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.load").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.save").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.work."+this.onlyCurrentPart, ""+this.selFrame).getFormattedText());
		}  else if (anim!=null && this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.disabled."+anim.isDisable()).getFormattedText());
		} else if (part!=null && this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part."+(part.id==0)).appendSibling(new TextComponentTranslation("model.head")).getFormattedText());
		} else if (part!=null && this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part."+(part.id==1)).appendSibling(new TextComponentTranslation("model.larm")).getFormattedText());
		} else if (part!=null && this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part."+(part.id==2)).appendSibling(new TextComponentTranslation("model.rarm")).getFormattedText());
		} else if (part!=null && this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part."+(part.id==3)).appendSibling(new TextComponentTranslation("model.body")).getFormattedText());
		} else if (part!=null && this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part."+(part.id==4)).appendSibling(new TextComponentTranslation("model.lleg")).getFormattedText());
		} else if (part!=null && this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part."+(part.id==5)).appendSibling(new TextComponentTranslation("model.rleg")).getFormattedText());
		} else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.color").getFormattedText());
		} else if (this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.block.type").getFormattedText());
		} else if (this.getButton(16)!=null && this.getButton(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.block.size").getFormattedText());
		} else if (frame!=null && this.getButton(17)!=null && this.getButton(17).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.frame", ""+(frame.id + 1)).getFormattedText());
		} else if (this.getButton(18)!=null && this.getButton(18).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.frame.add").getFormattedText());
		} else if (this.getButton(19)!=null && this.getButton(19).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.frame.del").getFormattedText());
		} else if (this.getButton(20)!=null && this.getButton(20).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.parts").getFormattedText());
		} else if (this.getButton(21)!=null && this.getButton(21).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.part").getFormattedText());
		} else if (part!=null && this.getButton(22)!=null && this.getButton(22).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.disabled."+part.isDisable()).appendSibling(new TextComponentTranslation("animation.hover.shift")).getFormattedText());
		} else if (frame!=null && this.getButton(23)!=null && this.getButton(23).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.smooth."+frame.isSmooth()).appendSibling(new TextComponentTranslation("animation.hover.shift")).getFormattedText());
		} else if (this.getButton(24)!=null && this.getButton(24).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.reset").getFormattedText());
		} else if (this.scrollSet!=null && this.getButton(30)!=null && this.getButton(30).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.patr."+this.scrollSet.selected, "X").getFormattedText());
		} else if (this.scrollSet!=null && this.getButton(31)!=null && this.getButton(31).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.patr."+this.scrollSet.selected, "Y").getFormattedText());
		} else if (this.scrollSet!=null && this.getButton(32)!=null && this.getButton(32).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.patr."+this.scrollSet.selected, "Z").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (anim != null && this.isMouseHover(mouseX, mouseY, this.mouseC[3], this.mouseC[4], this.mouseC[5], this.mouseC[5])) { // scroll anim
				this.setHoverText(new TextComponentTranslation("animation.hover.work."+this.onlyCurrentPart, ((char) 167) + "6" + (frame!=null ? frame.id + 1 : -1))
						.appendSibling(new TextComponentTranslation("animation.hover.work"))
						.getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.name").getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.ticks").getFormattedText());
		} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.delay").getFormattedText());
		} else if (this.getTextField(4)!=null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.repeat").getFormattedText());
		} else if (this.scrollSet != null && (this.getTextField(5)!=null && this.getTextField(5).isMouseOver()) || (this.getSlider(0)!=null && this.getSlider(0).isMouseOver())) {
			this.setHoverText(new TextComponentTranslation("animation.hover."+(this.scrollSet.selected==0 ? "rotation" : this.scrollSet.selected==1 ? "offset" : "scale"), "X").getFormattedText());
		} else if (this.scrollSet != null && (this.getTextField(6)!=null && this.getTextField(6).isMouseOver()) || (this.getSlider(1)!=null && this.getSlider(1).isMouseOver())) {
			this.setHoverText(new TextComponentTranslation("animation.hover."+(this.scrollSet.selected==0 ? "rotation" : this.scrollSet.selected==1 ? "offset" : "scale"), "Y").getFormattedText());
		} else if (this.scrollSet != null && (this.getTextField(7)!=null && this.getTextField(7).isMouseOver()) || (this.getSlider(2)!=null && this.getSlider(2).isMouseOver())) {
			this.setHoverText(new TextComponentTranslation("animation.hover."+(this.scrollSet.selected==0 ? "rotation" : this.scrollSet.selected==1 ? "offset" : "scale"), "Z").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	private void drawCRect(double left, double top, double right, double bottom, int color) {
		float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        // front
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        // left
        bufferbuilder.pos(left, bottom, -1.0D).endVertex();
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, -1.0D).endVertex();
        // back
        bufferbuilder.pos(right, bottom, -1.0D).endVertex();
        bufferbuilder.pos(left, bottom, -1.0D).endVertex();
        bufferbuilder.pos(left, top, -1.0D).endVertex();
        bufferbuilder.pos(right, top, -1.0D).endVertex();
        // right
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, -1.0D).endVertex();
        bufferbuilder.pos(right, top, -1.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        // top
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(right, top, -1.0D).endVertex();
        bufferbuilder.pos(left, top, -1.0D).endVertex();
        //bottom
        bufferbuilder.pos(left, bottom, -1.0D).endVertex();
        bufferbuilder.pos(right, bottom, -1.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
	}

	private void drawWork(EntityNPCInterface npc) {
		int u = this.mouseC[3] + 1;
		int v = this.mouseC[4] + 1;
		// work place
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, 1.0f);
		Gui.drawRect(u, v, u + this.mouseC[5] - 2, v + this.mouseC[5] - 2, GuiNpcAnimation.backColor);
		GlStateManager.popMatrix();
		// npc
		GlStateManager.pushMatrix();
		this.postRender(true);
		this.drawNpc(npc);
		GlStateManager.popMatrix();
		// blocks
		if (GuiNpcAnimation.blockType < 4) {
			Block block = Blocks.STONE;
			switch(GuiNpcAnimation.blockType) {
				case 1: block = Blocks.STONE_STAIRS; break;
				case 2: block = Blocks.STONE_SLAB; break;
				case 3: block = Blocks.CARPET; break;
				default: block = Blocks.STONE; break;
			}
			ItemStack stack = new ItemStack(block);
			if (!stack.isEmpty()) {
				GlStateManager.pushMatrix();
				this.postRender(false);
				RenderHelper.enableGUIStandardItemLighting();
				this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
				GlStateManager.enableRescaleNormal();
				GlStateManager.enableAlpha();
				GlStateManager.alphaFunc(516, 0.1F);
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				IBakedModel bakedmodel = this.mc.getRenderItem().getItemModelWithOverrides(stack, this.mc.world, this.mc.player);
				GlStateManager.translate(0.0f, 7.95f, 0.0f);
				if (GuiNpcAnimation.blockType==1) {
					GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
				} else if (GuiNpcAnimation.blockType==2) {
					GlStateManager.translate(0.0f, -8.0f, 0.0f);
				} else if (GuiNpcAnimation.blockType==3) {
					GlStateManager.translate(0.0f, -15.0f, 0.0f);
				}
				GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
				GlStateManager.scale(-16.0f, -16.0f, -16.0f);
				this.mc.getRenderItem().renderItem(stack, bakedmodel);
				float s = 1.0f;
				if (GuiNpcAnimation.blockSize > 0) {
					GlStateManager.translate(s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(-s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(-s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, -s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, -s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
				}
				if (GuiNpcAnimation.blockSize > 1) {
					GlStateManager.translate(s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(-s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(-s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(-s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(-s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, -s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, -s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, -s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(0.0f, 0.0f, -s);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
					GlStateManager.translate(s, 0.0f, 0.0f);
					this.mc.getRenderItem().renderItem(stack, bakedmodel);
				}
				GlStateManager.disableAlpha();
				GlStateManager.disableRescaleNormal();
				GlStateManager.disableLighting();
				this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
				RenderHelper.disableStandardItemLighting();
				GlStateManager.popMatrix();
			}
		}
	}

	private void drawNpc(EntityNPCInterface npc) {
		if (npc == null) { return; }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		
		float scale = 1.0f;
		if (npc.height > 2.4) {
			scale = 2.0f / npc.height;
		}
		GlStateManager.scale(-30.0f * scale, 30.0f * scale, 30.0f * scale);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		RenderHelper.enableStandardItemLighting();
		float f2 = npc.renderYawOffset;
		float f3 = npc.rotationYaw;
		float f4 = npc.rotationPitch;
		float f5 = npc.rotationYawHead;
		npc.ais.orientation = 0;
		npc.renderYawOffset = 0;
		npc.rotationYaw = 0.0f;
		npc.rotationPitch = 0.0f;
		npc.rotationYawHead = npc.rotationYaw;
		this.mc.getRenderManager().playerViewY = 180.0f;
		this.mc.getRenderManager().renderEntity(npc, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		npc.renderYawOffset = f2;
		npc.prevRenderYawOffset = f2;
		npc.rotationYaw = f3;
		npc.prevRotationYaw = f3;
		npc.rotationPitch = f4;
		npc.prevRotationPitch = f4;
		npc.rotationYawHead = f5;
		npc.prevRotationYawHead = f5;
		npc.ais.orientation = 0;
		
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	
	private void postRender(boolean forNpc) {
		float u = (float) this.mouseC[3];
		float v = (float) this.mouseC[4];
		GlStateManager.translate(u + 81.5f, v + 73.0f, 100.0f * this.dispScale); // center
		GlStateManager.translate(this.dispPos[0], this.dispPos[1], 0.0f);
		GlStateManager.rotate(this.dispRot[0], 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(this.dispRot[1], 1.0f, 0.0f, 0.0f);
		GlStateManager.scale(this.dispScale, this.dispScale, this.dispScale);
		if (GuiNpcAnimation.blockType == 4) { GlStateManager.translate(0.0f, 29.0f, 0.0f); }
		else { GlStateManager.translate(0.0f, 25.0f, 0.0f); }
	}

	private void clearDisplay() {
		this.dispScale = 1.0f;
		for (int i = 0; i < 3; i++) {
			this.dispRot[i] = 0.0f;
			this.dispPos[i] = 0.0f;
		}
	}
	
	private void displayOffset(int x, int y) {
		for (int i = 0; i < 2; i++) {
			this.dispPos[i] += (i == 0 ? x : y);
			if (this.dispPos[i] > 75.0f * this.dispScale) { this.dispPos[i] = 75.0f * this.dispScale; }
			else if (this.dispPos[i] < -75.0f * this.dispScale) { this.dispPos[i] = -75.0f * this.dispScale; }
		}
	}

	private void displayRotate(int x, int y) {
		for (int i = 0; i < 2; i++) {
			this.dispRot[i] += (i == 0 ? x : y);
			if (this.dispRot[i] > 360.0f) { this.dispRot[i] -= 360.0f; }
			else if (this.dispRot[i] < 0.0f) { this.dispRot[i] += 360.0f; }
		}
	}
	
	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		PartConfig part = this.getPart(this.getFrame(this.getAnim()));
		if (part==null || this.scrollSet==null) { return; }
		float value = 0.0f;
		switch(this.scrollSet.selected) {
			case 0: { // r
				part.rotation[slider.id] = slider.sliderValue;
				value = Math.round(360000.0f * slider.sliderValue) / 1000.0f;
				break;
			}
			case 1: { // o
				part.offset[slider.id] = slider.sliderValue;
				value = Math.round((10.0f * slider.sliderValue - 5.0f) * 100000.0f) / 100000.0f;
				break;
			}
			case 2: { // s
				part.scale[slider.id] = slider.sliderValue;
				value = Math.round(5000.0f * slider.sliderValue) / 1000.0f;
				break;
			}
		}
		if (this.getTextField(5 + slider.id)!=null) { this.getTextField(5 + slider.id).setText("" + value); }
		this.resetAnims();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if ((mouseBottom==0 || mouseBottom==1) && this.isMouseHover(mouseX, mouseY, this.mouseC[3], this.mouseC[4], this.mouseC[5], this.mouseC[5])) {
			this.mouseC[0] = mouseBottom;
			this.mouseC[1] = mouseX;
			this.mouseC[2] = mouseY;
		}
		else { this.mouseC[0] = -1; }
		super.mouseClicked(mouseX, mouseY, mouseBottom);
	}
	
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
		if (scroll.id == 0) { // animation
			if (!this.data.containsKey(scroll.getSelected())) { return; }
			this.selAnim = scroll.getSelected();
			this.selFrame = 0;
			this.selPart = 0;
			this.clearDisplay();
		} else if (scroll.id == 1) { // setting
			
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {  }

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		AnimationConfig anim;
		if (subgui.id == 1) { // add new
			if (!(subgui instanceof SubGuiEditText) || ((SubGuiEditText) subgui).cancelled) { return; }
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
			anim = this.animation.createAnimation(GuiNpcAnimation.type.get());
			anim.name = name;
			this.selAnim = anim.getSettingName();
			this.data.put(this.selAnim, anim.id);
			this.selFrame = 0;
			this.selPart = 0;
			this.initGui();
		}
		else if (subgui.id == 2) { // load
			if (!(subgui instanceof SubGuiLoadAnimation) || ((SubGuiLoadAnimation) subgui).cancelled || ((SubGuiLoadAnimation) subgui).animation==null) { return; }
			anim = ((SubGuiLoadAnimation) subgui).animation.copy();
			anim.type = GuiNpcAnimation.type;
			anim.id = this.animation.data.get(anim.type).size();
			this.animation.data.get(anim.type).add(anim);
			this.selAnim = anim.getSettingName();
			this.data.put(this.selAnim, anim.id);
			this.selFrame = 0;
			this.selPart = 0;
			this.initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		AnimationConfig anim = this.getAnim();
		AnimationFrameConfig frame = this.getFrame(anim);
		PartConfig part = this.getPart(frame);
		if (this.hasSubGui() || anim==null) { return; }
		switch(textField.getId()) {
			case 1: { // new name
				String name = textField.getText();
				this.data.remove(anim.getSettingName());
				for (AnimationConfig acn : this.animation.data.get(GuiNpcAnimation.type)) {
					while (acn.name.equals(name)) { name += "_"; }
				}
				anim.name = name;
				this.selAnim = anim.getSettingName();
				this.data.put(this.selAnim, anim.id);
				this.initGui();
				break;
			}
			case 2: { // speed
				if (frame==null) { return; }
				frame.setSpeed(textField.getInteger());
				this.resetAnims();
				break;
			}
			case 3: { // delay
				if (frame==null) { return; }
				frame.setEndDelay(textField.getInteger());
				this.resetAnims();
				break;
			}
			case 4: { // repeatLast
				if (anim != null) {
					anim.setRepeatLast(textField.getInteger());
					this.resetAnims();
				}
				break;
			}
			case 5: { // rotation X
				if (part==null || this.scrollSet==null) { return; }
				float value = 0.0f;
				switch(this.scrollSet.selected) {
					case 0: { // r
						part.rotation[0] = (value = (float) (textField.getDouble()) / 360.0f);
						break;
					}
					case 1: { // o
						part.offset[0] = (value = 0.1f * (float) (textField.getDouble()) + 0.5f);
						break;
					}
					case 2: { // s
						part.scale[0] = (value = (float) (textField.getDouble()) / 5.0f);
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (this.getSlider(0)!=null) { this.getSlider(0).sliderValue = value; }
				this.resetAnims();
				break;
			}
			case 6: { // rotation Y
				if (part==null || this.scrollSet==null) { return; }
				float value = 0.0f;
				switch(this.scrollSet.selected) {
					case 0: { // r
						part.rotation[1] = (value = (float) (textField.getDouble()) / 360.0f);
						break;
					}
					case 1: { // o
						part.offset[1] = (value = 0.1f * (float) (textField.getDouble()) + 0.5f);
						break;
					}
					case 2: { // s
						part.scale[1] = (value = (float) (textField.getDouble()) / 5.0f);
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (this.getSlider(1)!=null) { this.getSlider(1).sliderValue = value; }
				this.resetAnims();
				break;
			}
			case 7: { // rotation Z
				if (part==null || this.scrollSet==null) { return; }
				float value = 0.0f;
				switch(this.scrollSet.selected) {
					case 0: { // r
						part.rotation[2] = (value = (float) (textField.getDouble()) / 360.0f);
						break;
					}
					case 1: { // o
						part.offset[2] = (value = 0.1f * (float) (textField.getDouble()) + 0.5f);
						break;
					}
					case 2: { // s
						part.scale[2] = (value = (float) (textField.getDouble()) / 5.0f);
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (this.getSlider(2)!=null) { this.getSlider(2).sliderValue = value; }
				this.resetAnims();
				break;
			}
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		AnimationConfig anim = this.getAnim();
		if (anim!=null && this.scrollSet!=null) {
			boolean needInit = false;
			if (i==200 || i==ClientProxy.frontButton.getKeyCode()) { // up
				if (this.scrollSet.selected > 0) {
					this.scrollSet.selected--;
					if (this.scrollSet.maxScrollY>0) {
						this.scrollSet.scrollY -= 14;
						if (this.scrollSet.scrollY<0) { this.scrollSet.scrollY = 0; }
					}
					needInit = true;
				}
			}
			else if (i==208 || i==ClientProxy.backButton.getKeyCode()) { // down
				if (this.scrollSet.selected < this.scrollSet.getList().size()-1) {
					this.scrollSet.selected++;
					if (this.scrollSet.maxScrollY>0) {
						this.scrollSet.scrollY += 14;
						if (this.scrollSet.scrollY>this.scrollSet.maxScrollY) { this.scrollSet.scrollY = this.scrollSet.maxScrollY; }
					}
					needInit = true;
				}
			}
			if (needInit) {
				this.initGui();
				return;
			}
		}
		super.keyTyped(c, i);
	}
	
	@Override
	public void confirmClicked(boolean result, int id) {
		this.displayGuiScreen(this);
		if (!result) { return; }
		AnimationConfig anim = this.getAnim();
		AnimationFrameConfig frame = this.getFrame(anim);
		switch(id) {
			case 0: { // clear All Parts in frame
				if (frame==null) { return; }
				for (int j=0; j<6; j++) {
					for (int i=0; i<3; i++) {
						frame.parts[j].clear();
					}
				}
				this.initGui();
				break;
			}
			case 1: { // remove frame
				if (anim==null || frame==null) { return; }
				int f = frame.id - 1;
				if (f<0) { f = 0; }
				if (!anim.removeFrame(frame)) { return; }
				if (!anim.frames.containsKey(f)) { f = 0; }
				frame = anim.frames.get(0);
				if (frame==null) {
					this.selFrame = 0;
					this.selPart = 0;
				}
				this.initGui();
				break;
			}
			case 2: { // remove anim
				if (anim==null) { return; }
				boolean bo = this.animation.removeAnimation(GuiNpcAnimation.type.get(), anim.name);
				if (bo) {
					this.selAnim = "";
					this.selFrame = 0;
					this.selPart = 0;
				}
				this.initGui();
				break;
			}
		}
	}
	
	private void resetAnims() {
		AnimationConfig anim = this.getAnim();
		AnimationFrameConfig frame = this.getFrame(anim);
		if (anim == null || frame == null || this.npcAnim == null) {
			this.npcPart = null;
			return;
		}
		AnimationConfig ac = anim.copy();
		ac.isEdit = true;
		ac.disable = false;
		ac.type = AnimationKind.STANDING;
		NBTTagCompound npcNbt = new NBTTagCompound();
		this.npcAnim.animation.clear();
		this.npcAnim.writeEntityToNBT(npcNbt);
		this.npcAnim.writeToNBTOptional(npcNbt);
		if (this.onlyCurrentPart) {
			if (this.npcPart==null) {
				Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
				if (animNpc instanceof EntityNPCInterface) {
					this.npcPart = (EntityNPCInterface) animNpc;
					this.npcPart.animation.clear();
				}
			}
			if (this.npcPart == null || frame == null) {
				this.npcPart = null;
				return;
			}
			ac.frames.clear();
			ac.frames.put(0, frame);
			this.npcPart.display.setName("0_"+this.npc.getName());
			this.npcPart.animation.activeAnim = ac;
		} else {
			if (this.npcAnim == null) { return; }
			this.npcAnim.display.setName("1_"+this.npc.getName());
			this.npcAnim.animation.activeAnim = ac;
		}
	}
	
}
