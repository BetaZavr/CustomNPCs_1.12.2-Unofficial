package noppes.npcs.client.gui.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.api.constants.AnimationKind;
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

	public static int backColor = 0xFF000000;
	private static AnimationKind type = AnimationKind.STANDING;
	private static int blockType = 0; // 0 - stone, 1-stairs, 2-stone_slab, 3-carpet, 4 - non
	private static int blockSize = 0; // 0 - x1, 1 - x3, 2 - x5

	// data
	private GuiScreen parent;
	private DataAnimation animation;
	private GuiCustomScroll scroll, scrollSet;
	private AnimationConfig anim;
	private AnimationFrameConfig frame;
	private PartConfig part;
	private final Map<String, AnimationConfig> dataAnim;
	private boolean onlyCurrentPart;
	private EntityNPCInterface npcAnim, npcPart;
	// display
	private final float[] dispRot, dispPos;
	private float dispScale;
	private final int[] mouseC;
	private ScaledResolution sw;
	private Framebuffer framebuffer;

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
		
		this.anim = null;
		this.frame = null;
		this.part = null;
		this.onlyCurrentPart = false;
		this.dataAnim = Maps.<String, AnimationConfig>newHashMap();
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
		AnimationConfig tempAnim = null;
		this.dataAnim.clear();
		int id = 0;
		for (AnimationConfig ac : this.animation.data.get(GuiNpcAnimation.type)) {
			ac.id = id;
			this.dataAnim.put(ac.getSettingName(), ac);
			if (this.anim==null || this.anim.equals(tempAnim)) { tempAnim = ac; }
			id++;
		}
		if (this.anim==null && tempAnim!=null) { this.anim = tempAnim; }
		
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(120, 156); }
		this.scroll.setList(new ArrayList<String>(this.dataAnim.keySet()));
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 36;
		this.addScroll(this.scroll);
		if (this.anim!=null) { this.scroll.setSelected(this.anim.getSettingName()); }
		
		this.addLabel(new GuiNpcLabel(0, "animation.type", this.guiLeft+4, this.guiTop+4));
		GuiNpcButton button = new GuiButtonBiDirectional(1, this.guiLeft+4, this.guiTop+14, 120, 20, AnimationKind.getNames(), GuiNpcAnimation.type.get());
		((GuiButtonBiDirectional) button).showShedow = false;
		this.addButton(button);

		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 17, this.guiTop + 5, 12, 12, "X"));
		
		this.addButton(new GuiNpcButton(2, this.guiLeft + 4, this.guiTop + 194, 59, 20, "gui.add"));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 64, this.guiTop + 194, 59, 20, "gui.remove"));
		this.getButton(3).enabled = this.anim!=null;
		this.addButton(new GuiNpcButton(4, this.guiLeft + 4, this.guiTop + 216, 59, 20, "gui.load"));
		this.getButton(4).enabled = aData!=null;
		this.addButton(new GuiNpcButton(5, this.guiLeft + 64, this.guiTop + 216, 59, 20, "gui.save"));
		this.getButton(5).layerColor = this.anim!=null && aData.getAnimation(this.anim.name)==null ? 0xFF00FF00 : 0xFFFFFFFF;
		
		if (this.anim!=null) {
			if (this.anim.frames.size()==0) {
				this.frame = new AnimationFrameConfig();
				this.anim.frames.put(0, this.frame);
			}
			if (!this.anim.frames.containsValue(this.frame)) { this.frame = this.anim.frames.get(0); }
			
			if (this.frame!=null) {
				if (this.part == null || this.part.id < 0 || this.part.id >= this.frame.parts.length) { this.part = this.frame.parts[0]; }
			}
			else { this.part = null; }
		}
		else {
			this.frame = null;
			this.part = null;
		}
		this.mouseC[3] = -1;
		this.mouseC[4] = -1;
		this.mouseC[5] = 0;
		if (this.anim!=null && this.frame!=null && this.part!=null) {

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
			this.addScroll(this.scrollSet);

			this.addButton(new GuiNpcButton(65, u + 53, v - 17, 50, 14, this.onlyCurrentPart ? "animation.frame" : "movement.animation"));
			
			v -= 74;
			u += 16;
			this.addButton(new GuiNpcButton(7, u, v, 18, 18, "")); // head
			this.addButton(new GuiNpcButton(8, u+19, v+19, 8, 24, "")); // left arm
			this.addButton(new GuiNpcButton(9, u-9, v+19, 8, 24, "")); // right arm
			this.addButton(new GuiNpcButton(10, u, v+19, 18, 24, "")); // body
			this.addButton(new GuiNpcButton(11, u+10, v+44, 8, 24, "")); // left leg
			this.getButton(11).visible = ((ModelPartData) ((EntityCustomNpc) this.npc).modelData.getPartData(EnumParts.LEGS)).type == (byte) 0;
			this.addButton(new GuiNpcButton(12, u, v+44, 8, 24, "")); // right leg
			this.getButton(12).visible = ((ModelPartData) ((EntityCustomNpc) this.npc).modelData.getPartData(EnumParts.LEGS)).type == (byte) 0;
			this.getButton(7+this.part.id).layerColor = 0xFF70FF70;
			
			// Work place
			u = this.scroll.guiLeft + this.scroll.width + 56;
			v = this.guiTop + 4;
			this.addLabel(new GuiNpcLabel(1, "type.name", u+2, v));
			GuiNpcTextField textField = new GuiNpcTextField(0, this, u+1, v += 10, 120, 15, this.anim.name);
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
			this.addButton(new GuiNpcButton(20, u + 123, v -= 1, 80, 17, blocks, GuiNpcAnimation.blockType));
			String[] sizes = new String[] { "x1", "x3", "x5" };
			this.addButton(new GuiNpcButton(21, u + 204, v, 25, 17, sizes, GuiNpcAnimation.blockSize));

			v += 17;
			button = new GuiNpcCheckBox(18, u, v, 65, 14, this.anim.isDisable() ? "Off" : "On");
			((GuiNpcCheckBox) button).setSelected(this.anim.isDisable());
			this.addButton(button);
			
			v += 180;
			if (this.scrollSet.selected>-1) {
				int s = scrollSet.selected;
				for (int i = 0; i < 3; i++) {
					this.addLabel(new GuiNpcLabel(7 + i, i==0 ? "X:" : i==1 ? "Y:" : "Z:", u, v + i * 10));
					float[] values = s == 0 ? this.part.rotation : s == 1 ? this.part.offset : this.part.scale;
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
					this.addSlider(new GuiNpcSlider(this, i, u + 8, v + i * 10, 168, 8, values[i]));
					
					textField = new GuiNpcTextField(i * 3 + 1, this, u + 179, v + i * 10, 51, 8, ""+datas[i]);
					textField.setDoubleNumbersOnly();
					switch(i) {
						case 1: textField.setMinMaxDoubleDefault(-5.0d, 5.0d, (double) datas[i]); break;
						case 2: textField.setMinMaxDoubleDefault(0.0d, 5.0d, (double) datas[i]); break;
						default: textField.setMinMaxDoubleDefault(0.0d, 360.0d, (double) datas[i]); break;
					}
					this.addTextField(textField);
					
					button = new GuiNpcButton(80 + i, u + 232, v - 1 + i * 10, 10, 10, "x");
					button.dropShadow = false;
					this.addButton(button);
				}
			}
			
			// display
			button = new GuiNpcButton(63, this.guiLeft + this.xSize - 17, this.guiTop + 19, 12, 12, "x");
			button.packedFGColour = 0xFFC0C0C0;
			button.dropShadow = false;
			button.layerColor = 0xFFF080F0;
			this.addButton(button);
			
			List<String> list = Lists.newArrayList();
			for (int i=0; i<this.anim.frames.size(); i++) { list.add(""+(i+1)+"/"+this.anim.frames.size()); }
		} else { this.clearDisplay(); }
		this.resetAnims();
	}


	@Override
	protected void actionPerformed(GuiButton button) {
		if (!(button instanceof GuiNpcButton)) { return; }
		GuiNpcButton npcButton = (GuiNpcButton) button;
		switch(npcButton.id) {
			case 1: { // set type
				GuiNpcAnimation.type = AnimationKind.values()[npcButton.getValue()];
				this.anim = null;
				this.frame = null;
				this.part = null;
				this.initGui();
				break;
			}
			case 2: { // add anim
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 3: { // del anim
				if (this.anim==null) { return; }
				boolean bo = this.animation.removeAnimation(GuiNpcAnimation.type.get(), this.anim.name);
				if (bo) {
					this.anim = null;
					this.frame = null;
					this.part = null;
				}
				this.initGui();
				break;
			}
			case 4: { // load
				this.setSubGui(new SubGuiLoadAnimation(2, this.npc));
				break;
			}
			case 5: { // save
				if (this.anim==null) { return; }
				AnimationController aData = AnimationController.getInstance();
				AnimationConfig ac = (AnimationConfig) aData.createNew(GuiNpcAnimation.type.get());
				ac.readFromNBT(this.anim.writeToNBT(new NBTTagCompound()));
				this.player.sendMessage(new TextComponentTranslation("animation.message.save", ac.name));
				Client.sendData(EnumPacketServer.AnimationGlobalSave, ac.writeToNBT(new NBTTagCompound()));
				this.initGui();
				break;
			}
			case 18: { // disable
				if (this.anim==null) { return; }
				this.anim.setDisable(((GuiNpcCheckBox) button).isSelected());
				((GuiNpcCheckBox) button).setText(this.anim.isDisable() ? "Off" : "On");
				break;
			}
			case 20: { // block type
				GuiNpcAnimation.blockType = npcButton.getValue();
				break;
			}
			case 21: { // block size
				GuiNpcAnimation.blockSize = npcButton.getValue();
				break;
			}
			case 63: { // reset now rotation
				this.dispScale = 1.0f;
				for (int i = 0; i < 3; i++) {
					this.dispRot[i] = 0.0f;
					this.dispPos[i] = 0.0f;
					this.mouseC[i] =  i == 0 ? -1 : 0;
				}
				break;
			}
			case 65: { // exit
				this.onlyCurrentPart = !this.onlyCurrentPart;
				this.initGui();
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
			case 80: { // reset part X
				if (this.dispRot[0]==0.0f) {
					this.dispRot[0] = 180.0f;
				} else if (this.dispRot[0]==180.0f) {
					this.dispRot[0] = 0.0f;
				} else {
					this.dispRot[0] = 0.0f;
				}
				this.dispRot[1] = 0.0f;
				
				if (this.part==null || this.scrollSet==null || this.scrollSet.selected<0 || this.scrollSet.selected>2) { return; }
				switch(this.scrollSet.selected) {
					case 0: this.part.rotation[0] = 0.5f; break;
					case 1: this.part.offset[0] = 0.5f; break;
					case 2: this.part.scale[0] = 0.2f; break;
				}
				this.initGui();
				break;
			}
			case 81: { // reset part Y
				if (this.part==null || this.scrollSet==null || this.scrollSet.selected<0 || this.scrollSet.selected>2) { return; }
				switch(this.scrollSet.selected) {
					case 0: this.part.rotation[1] = 0.5f; break;
					case 1: this.part.offset[1] = 0.5f; break;
					case 2: this.part.scale[1] = 0.2f; break;
				}
				this.initGui();
				break;
			}
			case 82: { // reset part Z
				if (this.part==null || this.scrollSet==null || this.scrollSet.selected<0 || this.scrollSet.selected>2) { return; }
				switch(this.scrollSet.selected) {
					case 0: this.part.rotation[2] = 0.5f; break;
					case 1: this.part.offset[2] = 0.5f; break;
					case 2: this.part.scale[2] = 0.2f; break;
				}
				this.initGui();
				break;
			}
			/*
			case 6: { // select frame
				if (this.anim==null) { return; }
				this.selectFrame = npcButton.getValue();
				this.initGui();
				break;
			}
			case 7: { // select head
				if (this.frame==null) { return; }
				this.selectPart = 0;
				this.initGui();
				break;
			}
			case 8: { // select left arm
				if (this.frame==null) { return; }
				this.selectPart = 1;
				this.initGui();
				break;
			}
			case 9: { // select right arm
				if (this.frame==null) { return; }
				this.selectPart = 2;
				this.initGui();
				break;
			}
			case 10: { // select body
				if (frame==null) { return; }
				this.selectPart = 3;
				this.initGui();
				break;
			}
			case 11: { // select left leg
				if (frame==null) { return; }
				this.selectPart = 4;
				this.initGui();
				break;
			}
			case 12: { // select right leg
				if (frame==null) { return; }
				this.selectPart = 5;
				this.initGui();
				break;
			}
			case 13: { // add frame
				if (this.anim==null) { return; }
				this.selectFrame = ((AnimationFrameConfig) this.anim.addFrame(frame)).id;
				this.initGui();
				break;
			}
			case 14: { // del frame
				if (this.anim==null || frame==null) { return; }
				int f = frame.id--;
				if (f<0) { f = 0; }
				if (!this.anim.removeFrame(frame)) { return; }
				this.selectFrame = 0;
				this.selectPart = 0;
				this.initGui();
				break;
			}
			case 15: { // disabled part
				if (anim==null || part==null) { return; }
				part.setDisable(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					for (AnimationFrameConfig f : anim.frames.values()) {
						f.parts[this.selectPart].setDisable(part.isDisable());
					}
				}
				((GuiNpcCheckBox) button).setText(part.isDisable() ? "gui.disabled" : "gui.enabled");
				this.resetAnims();
				break;
			}
			case 16: { // smooth
				if (frame==null) { return; }
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
			case 17: { // back color
				GuiNpcAnimation.backColor = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF: 0xFF000000;
				break;
			}
			case 19: { // load טפûף
				this.setSubGui(new SubGuiLoadAnimation(3, this.npc));
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
			case 78: { // reset All Parts
				if (frame==null) { return; }
				for (int j=0; j<6; j++) {
					for (int i=0; i<3; i++) {
						frame.parts[j].clear();
					}
				}
				this.initGui();
				break;
			}
			case 79: { // reset Part
				if (part==null) { return; }
				part.clear();
				this.initGui();
				break;
			}*/
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.getEventButton() != this.mouseC[0]) { this.mouseC[0] = -1; }
		if (this.anim != null) {
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
			this.sw = new ScaledResolution(this.mc);
			int u, v;
			// NPC
			EntityNPCInterface npc = this.onlyCurrentPart ? this.npcPart : this.npcAnim;
			if (npc!=null) {
				int current = GlStateManager.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
				if (this.framebuffer == null) {
					this.framebuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, false);
					this.framebuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);
				}
				if (this.mc.displayWidth != this.framebuffer.framebufferWidth || this.mc.displayHeight != this.framebuffer.framebufferHeight) {
					this.framebuffer.createBindFramebuffer(this.mc.displayWidth, this.mc.displayHeight);
				}
				this.framebuffer.bindFramebuffer(false);
				GlStateManager.bindTexture(-1);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.mc.getFramebuffer().bindFramebuffer(false);
				this.drawWork(npc);
				this.framebuffer.framebufferClear();
				//this.framebuffer.deleteFramebuffer();
				this.mc.getFramebuffer().bindFramebuffer(false);
				GlStateManager.bindTexture(current);
			}
			
			// GUI
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			if (this.scroll!=null && this.scrollSet!=null) {
				u = this.scroll.guiLeft + this.scroll.width + 1;
				v = this.guiTop + this.ySize - 1;
				this.drawVerticalLine(u, this.guiTop+3, v, 0xFF808080);
				v -= this.scrollSet.height + 5;
				this.drawHorizontalLine(u, u + 297, v, 0xFF808080);
				this.drawVerticalLine(u+53, v - 78, v, 0xFF808080);
				u += 3; v -= 74;
				Gui.drawRect(u, v, u + 48, v + 72, 0xFF202020);
				u += 1; v += 1;
				this.drawGradientRect(u, v, u + 46, v + 70, 0xFFC0C0C0, 0xFF808080);
				u -= 4; v -= 4;
				this.drawHorizontalLine(u, u + 52, v, 0xFF808080);
			}
			
			u = this.mouseC[3];
			v = this.mouseC[4];
			Gui.drawRect(u, v, u + this.mouseC[5], v + this.mouseC[5], 0xFFF080F0);
			u += 1; v += 1;
			Gui.drawRect(u, v, u + this.mouseC[5] - 2, v + this.mouseC[5] - 2, GuiNpcAnimation.backColor);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(u + 1, v + 1, 1.0f);
			this.drawString(this.fontRenderer, "text", 0, 0, 0xFFFFFFFF);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			String ts = "x"+this.dispScale;
			this.fontRenderer.drawString(ts, u + this.mouseC[5] - 3 - this.fontRenderer.getStringWidth(ts), v + 1, 0xFFFFFF, false);
			ts = (int) this.dispRot[0] + "" + ((char) 176) + "/" + (int) this.dispRot[1] + ((char) 176);
			this.fontRenderer.drawString(ts, u + this.mouseC[5] - 3 - this.fontRenderer.getStringWidth(ts), v + this.mouseC[5] - 11, 0xFFFFFF, false);
			ts = (int) this.dispPos[0] + "/" + (int) this.dispPos[1];
			this.fontRenderer.drawString(ts, u + 1, v + this.mouseC[5] - 11, 0xFFFFFF, false);
			GlStateManager.popMatrix();
		}
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
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
		} else if (this.getButton(19)!=null && this.getButton(19).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.base").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.anim!= null) {
			if (this.isMouseHover(mouseX, mouseY, this.mouseC[3], this.mouseC[4], this.mouseC[5], this.mouseC[5])) { // scroll anim
				this.setHoverText(new TextComponentTranslation("animation.hover.work."+this.onlyCurrentPart, ""+(this.frame!=null ? this.frame.id : -1))
						.appendSibling(new TextComponentTranslation("animation.hover.work"))
						.getFormattedText());
			} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.name").getFormattedText());
			} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "X").getFormattedText());
			} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "Y").getFormattedText());
			} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.rotation", "Z").getFormattedText());
			}
			else if (this.frame!=null && this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.frame", ""+this.frame.id).getFormattedText());
			} else if (this.part!=null && this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.part.id==0)).appendSibling(new TextComponentTranslation("model.head")).getFormattedText());
			} else if (this.part!=null && this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.part.id==1)).appendSibling(new TextComponentTranslation("model.larm")).getFormattedText());
			} else if (this.part!=null && this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.part.id==2)).appendSibling(new TextComponentTranslation("model.rarm")).getFormattedText());
			} else if (this.part!=null && this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.part.id==3)).appendSibling(new TextComponentTranslation("model.body")).getFormattedText());
			} else if (this.part!=null && this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.part.id==4)).appendSibling(new TextComponentTranslation("model.lleg")).getFormattedText());
			} else if (this.part!=null && this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part."+(this.part.id==5)).appendSibling(new TextComponentTranslation("model.rleg")).getFormattedText());
			} else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.frame.add").getFormattedText());
			} else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.frame.del").getFormattedText());
			} else if (this.part!=null && this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part.disabled."+this.part.isDisable()).appendSibling(new TextComponentTranslation("animation.hover.shift")).getFormattedText());
			} else if (this.frame!=null && this.getButton(16)!=null && this.getButton(16).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.part.smooth."+this.frame.isSmooth()).appendSibling(new TextComponentTranslation("animation.hover.shift")).getFormattedText());
			} else if (this.getButton(17)!=null && this.getButton(17).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.color").getFormattedText());
			} else if (this.getButton(18)!=null && this.getButton(18).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("animation.hover.disabled."+this.anim.isDisable()).getFormattedText());
			} else if ((this.getButton(60)!=null && this.getButton(60).isMouseOver()) ||
					(this.getButton(61)!=null && this.getButton(61).isMouseOver()) ||
					(this.getButton(62)!=null && this.getButton(62).isMouseOver()) ||
					(this.getButton(63)!=null && this.getButton(63).isMouseOver())) {
				this.setHoverText(new TextComponentTranslation("quest.reset").getFormattedText());
			}
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	private void drawWork(EntityNPCInterface npc) {
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
		switch(slider.id) {
			/*case 0: {
				if (part==null || this.getTextField(1)==null) { return; }
				part.rotation[0] = slider.sliderValue;
				float value = Math.round(3600.0f * slider.sliderValue) / 10.0f;
				this.getTextField(1).setText("" + value);
				this.resetAnims();
				break;
			}
			case 1: {
				if (part==null || this.getTextField(2)==null) { return; }
				part.rotation[1] = slider.sliderValue;
				float value = Math.round(3600.0f * slider.sliderValue) / 10.0f;
				this.getTextField(2).setText("" + value);
				this.resetAnims();
				break;
			}
			case 2: {
				if (part==null || this.getTextField(3)==null) { return; }
				part.rotation[2] = slider.sliderValue;
				float value = Math.round(3600.0f * slider.sliderValue) / 10.0f;
				this.getTextField(3).setText("" + value);
				this.resetAnims();
				break;
			}
			case 3: {
				if (part==null || this.getTextField(4)==null) { return; }
				part.offset[0] = slider.sliderValue;
				double value = Math.round((10.0f * slider.sliderValue - 5.0f) * 1000.0f) / 1000.0d;
				this.getTextField(4).setText("" + value);
				this.resetAnims();
				break;
			}
			case 4: {
				if (part==null || this.getTextField(5)==null) { return; }
				part.offset[1] = slider.sliderValue;
				double value = Math.round((10.0f * slider.sliderValue - 5.0f) * 1000.0f) / 1000.0d;
				this.getTextField(5).setText("" + value);
				this.resetAnims();
				break;
			}
			case 5: {
				if (part==null || this.getTextField(6)==null) { return; }
				part.offset[2] = slider.sliderValue;
				double value = Math.round((10.0f * slider.sliderValue - 5.0f) * 1000.0f) / 1000.0d;
				this.getTextField(6).setText("" + value);
				this.resetAnims();
				break;
			}
			case 6: {
				if (part==null || this.getTextField(7)==null) { return; }
				part.scale[0] = slider.sliderValue;
				float value = Math.round(5000.0f * slider.sliderValue) / 1000.0f;
				this.getTextField(7).setText("" + value);
				this.resetAnims();
				break;
			}
			case 7: {
				if (part==null || this.getTextField(8)==null) { return; }
				part.scale[1] = slider.sliderValue;
				float value = Math.round(5000.0f * slider.sliderValue) / 1000.0f;
				this.getTextField(8).setText("" + value);
				this.resetAnims();
				break;
			}
			case 8: {
				if (part==null || this.getTextField(9)==null) { return; }
				part.scale[2] = slider.sliderValue;
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
			}*/
		}
		this.npc.updateHitbox();
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
			if (!this.dataAnim.containsKey(scroll.getSelected())) { return; }
			this.anim = this.dataAnim.get(scroll.getSelected());
			this.frame = null;
			this.part = null;
			this.clearDisplay();
		} else if (scroll.id == 1) { // setting
			
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {  }

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui.id == 1) { // add new
			if (!(subgui instanceof SubGuiEditText) || ((SubGuiEditText) subgui).cancelled) { return; }
			String name = ((SubGuiEditText) subgui).text[0];
			this.frame = null;
			this.part = null;
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
			this.anim = this.animation.createAnimation(GuiNpcAnimation.type.get());
			this.anim.name = name;
			this.dataAnim.put(this.anim.getSettingName(), this.anim);
			this.initGui();
		}
		else if (subgui.id == 2) { // load
			if (!(subgui instanceof SubGuiLoadAnimation) || ((SubGuiLoadAnimation) subgui).cancelled || ((SubGuiLoadAnimation) subgui).animation==null) { return; }
			this.anim = ((SubGuiLoadAnimation) subgui).animation.copy();
			this.anim.type = GuiNpcAnimation.type;
			this.anim.id = this.animation.data.get(this.anim.type).size();
			this.animation.data.get(this.anim.type).add(this.anim);
			this.dataAnim.put(this.anim.getSettingName(), this.anim);
			this.frame = null;
			this.part = null;
			this.initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.hasSubGui() || this.anim==null) { return; }
		switch(textField.getId()) {
			case 0: { // new name
				String name = textField.getText();
				this.dataAnim.remove(this.anim.getSettingName());
				for (AnimationConfig ac : this.dataAnim.values()) {
					while (ac.name.equals(name)) { name += "_"; }
				}
				this.anim.name = name;
				this.dataAnim.put(this.anim.getSettingName(), this.anim);
				this.initGui();
				break;
			}
			/*case 1: { // rotation X
				if (part==null || this.getSlider(0)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.rotation[0] = (float) (textField.getDouble()) / 360.0f;
				this.getSlider(0).sliderValue = part.rotation[0];
				this.resetAnims();
				break;
			}
			case 2: { // rotation Y
				if (part==null || this.getSlider(1)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.rotation[1] = (float) (textField.getDouble()) / 360.0f;
				this.getSlider(1).sliderValue = part.rotation[1];
				this.resetAnims();
				break;
			}
			case 3: { // rotation Z
				if (part==null || this.getSlider(2)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.rotation[2] = (float) (textField.getDouble()) / 360.0f;
				this.getSlider(2).sliderValue = part.rotation[2];
				this.resetAnims();
				break;
			}
			case 4: { // offset X
				if (part==null || this.getSlider(3)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.offset[0] = 0.1f * (float) (textField.getDouble()) + 0.5f;
				this.getSlider(3).sliderValue = part.offset[0];
				this.resetAnims();
				break;
			}
			case 5: { // offset Y
				if (part==null || this.getSlider(4)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.offset[1] = 0.1f * (float) (textField.getDouble()) + 0.5f;
				this.getSlider(4).sliderValue = part.offset[1];
				this.resetAnims();
				break;
			}
			case 6: { // offset Z
				if (part==null || this.getSlider(5)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.offset[2] = 0.1f * (float) (textField.getDouble()) + 0.5f;
				this.getSlider(5).sliderValue = part.offset[2];
				this.resetAnims();
				break;
			}
			case 7: { // scale X
				if (part==null || this.getSlider(6)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.scale[0] = (float) (textField.getDouble()) / 5.0f;
				this.getSlider(6).sliderValue = part.scale[0];
				this.resetAnims();
				break;
			}
			case 8: { // scale Y
				if (part==null || this.getSlider(7)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.scale[1] = (float) (textField.getDouble()) / 5.0f;
				this.getSlider(7).sliderValue = part.scale[1];
				this.resetAnims();
				break;
			}
			case 9: { // scale Z
				if (part==null || this.getSlider(8)==null) { return; }
				float value = (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d);
				textField.setText(""+value);
				part.scale[2] = (float) (textField.getDouble()) / 5.0f;
				this.getSlider(8).sliderValue = part.scale[2];
				this.resetAnims();
				break;
			}
			case 10: { // speed
				if (frame==null) { return; }
				frame.setSpeed(textField.getInteger());
				this.resetAnims();
				break;
			}
			case 11: { // delay
				if (frame==null) { return; }
				frame.setEndDelay(textField.getInteger());
				this.resetAnims();
				break;
			}
			case 12: { // repeatLast
				anim.setRepeatLast(textField.getInteger());
				this.resetAnims();
				break;
			}*/
		}
	}
	
	private void resetAnims() {
		if (this.anim == null || this.frame==null || this.npcAnim == null) {
			this.npcPart = null;
			return;
		}
		AnimationConfig ac = this.anim.copy();
		ac.isEdit = true;
		ac.disable = false;
		ac.type = AnimationKind.STANDING;
		NBTTagCompound npcNbt = new NBTTagCompound();
		this.npcAnim.writeEntityToNBT(npcNbt);
		this.npcAnim.writeToNBTOptional(npcNbt);
		if (this.npcAnim==null) {
			Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
			if (animNpc instanceof EntityNPCInterface) {
				this.npcPart = (EntityNPCInterface) animNpc;
				this.npcPart.animation.clear();
			}
		}
		if (this.npcPart!=null) {
			((EntityNPCInterface) this.npcPart).display.setName("0_"+this.npc.getName());
			this.npcPart.animation.activeAnim = ac;
		}
		if (!ac.frames.containsKey(this.frame.id)) {
			this.npcPart = null;
			return;
		}
		if (this.npcPart!=null) {
			((EntityNPCInterface) this.npcPart).display.setName("0_"+this.npc.getName());
			if (this.npcPart.animation.activeAnim==null) {
				if (this.npcPart.animation.data.get(AnimationKind.STANDING).isEmpty()) {
					this.npcPart.animation.activeAnim = new AnimationConfig(AnimationKind.STANDING.get());
					this.npcPart.animation.data.get(AnimationKind.STANDING).add(this.npcPart.animation.activeAnim);
				}
				else { this.npcPart.animation.activeAnim = this.npcPart.animation.data.get(AnimationKind.STANDING).get(0); }
			}
			if (!this.npcPart.animation.activeAnim.frames.containsKey(0)) { this.npcPart.animation.activeAnim.addFrame(); }
			for (int i=0; i<6; i++) {
				this.npcPart.animation.activeAnim.frames.get(0).parts[i].readNBT(ac.frames.get(this.frame.id).parts[i].writeNBT());
			}
		}
	}
	
}
