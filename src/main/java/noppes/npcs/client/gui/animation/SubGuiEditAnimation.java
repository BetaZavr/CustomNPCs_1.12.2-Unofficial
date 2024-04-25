package noppes.npcs.client.gui.animation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.PartConfig;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class SubGuiEditAnimation extends SubGuiInterface
		implements ISubGuiListener, ISliderListener, ICustomScrollListener, ITextfieldListener, GuiYesNoCallback {

	public static final ResourceLocation btns = new ResourceLocation(CustomNpcs.MODID,
			"textures/gui/animation/buttons.png");

	// data
	public static int meshType = 1;
	public static boolean showHitBox = true;
	public AnimationConfig anim;
	public AnimationFrameConfig frame;
	public PartConfig part;
	private GuiCustomScroll scrollParts;
	private int blockType, blockSize, toolType, waitKey, waitKeyID;
	private boolean onlyCurrentPart, hovered;
	private EntityNPCInterface npcAnim, npcPart;
	private final String[] blockNames, blockSizes;
	// display
	private int workU, workV, workS, winU, winV, winW, winH, mousePressId, mousePressX, mousePressY;
	private final float[] dispRot, dispPos;
	private float dispScale, winScale, offsetY;
	private final Map<String, PartConfig> dataParts;
	private final List<Entity> environmentEntitys;
	private final Map<BlockPos, IBlockState> environmentStates;
	private final Map<BlockPos, TileEntity> environmentTiles;
	private ScaledResolution sw;
	public boolean showArmor;

	public SubGuiEditAnimation(EntityNPCInterface npc, AnimationConfig anim, int id, GuiNpcAnimation parent) {
		super(npc);
		this.id = id;
		this.ySize = 240;
		this.xSize = 427;
		this.parent = parent;
		this.anim = anim;
		frame = anim.frames.get(0);
		part = frame.parts.get(0);
		waitKey = 0;

		// Display
		toolType = 0; // 0 - rotation, 1 - offset, 2 - scale
		blockType = 0; // 0 - environment, 1 - non, 2 - stone, 3 - stairs, 4 - stone_slab, 5 - carpet
		blockSize = 2; // 0 - x1, 1 - x3, 2 - x5, 3 - x7, 4 - x9
		dispScale = 1.0f;
		winScale = 1.0f;
		dispRot = new float[] { 45.0f, 345.0f, 345.0f };
		dispPos = new float[] { 0.0f, 0.0f, 0.0f };
		dataParts = Maps.<String, PartConfig>newLinkedHashMap();
		environmentEntitys = Lists.<Entity>newArrayList();
		environmentStates = Maps.<BlockPos, IBlockState>newHashMap();
		environmentTiles = Maps.<BlockPos, TileEntity>newHashMap();
		setEnvironment();
		mousePressId = -1;
		mousePressX = 0;
		mousePressY = 0;
		onlyCurrentPart = false;
		showArmor = true;

		npcAnim = AdditionalMethods.copyToGUI(npc, mc.world, true);
		npcPart = AdditionalMethods.copyToGUI(npc, mc.world, true);

		blockNames = new String[6];
		blockNames[0] = "gui.environment";
		blockNames[1] = "gui.none";
		for (int i = 0; i < 4; i++) {
			Block block;
			switch (i) {
			case 1:
				block = Blocks.STONE_STAIRS;
				break;
			case 2:
				block = Blocks.STONE_SLAB;
				break;
			case 3:
				block = Blocks.CARPET;
				break;
			default:
				block = Blocks.STONE;
				break;
			}
			blockNames[i + 2] = new ItemStack(block).getDisplayName();
		}
		blockSizes = new String[] { "x1", "x3", "x5", "x7", "x9" };
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: { // block place
			blockType = button.getValue();
			if (this.getButton(16) != null) {
				this.getButton(16).setEnabled(blockType != 1);
			}
			break;
		}
		case 1: { // block size
			blockSize = button.getValue();
			break;
		}
		case 2: { // back color
			GuiNpcAnimation.backColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000);
			button.layerColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
			break;
		}
		case 3: { // select frame
			if (anim == null || !anim.frames.containsKey(button.getValue())) {
				return;
			}
			frame = anim.frames.get(button.getValue());
			part = frame.parts.get(part.id);
			this.initGui();
			break;
		}
		case 4: { // add frame
			if (anim == null) {
				return;
			}
			frame = (AnimationFrameConfig) anim.addFrame(frame);
			part = frame.parts.get(part.id);
			this.initGui();
			break;
		}
		case 5: { // del frame
			if (frame == null) {
				return;
			}
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this,
					new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
					new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
			this.displayGuiScreen((GuiScreen) guiyesno);
			break;
		}
		case 6: { // clear frame
			if (frame == null) {
				return;
			}
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this,
					new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
					new TextComponentTranslation("gui.clearMessage").getFormattedText(),
					GuiScreen.isShiftKeyDown() ? 4 : 1);
			this.displayGuiScreen((GuiScreen) guiyesno);
			break;
		}
		case 7: { // add part
			// this.setSubGui(new SubGuiAddAnimationPart(this.npc, 1, frame.parts.size()));
			// this.initGui();
			break;
		}
		case 8: { // del part
			if (part == null) {
				return;
			}
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this,
					new TextComponentTranslation("animation.clear.part", "" + (part.id + 1),
							this.scrollParts.getSelected()).getFormattedText(),
					new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
			this.displayGuiScreen((GuiScreen) guiyesno);
			break;
		}
		case 9: { // clear part
			if (part == null) {
				return;
			}
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this,
					new TextComponentTranslation("animation.clear.part", "" + (part.id + 1),
							this.scrollParts.getSelected()).getFormattedText(),
					new TextComponentTranslation("gui.clearMessage").getFormattedText(),
					GuiScreen.isShiftKeyDown() ? 5 : 3);
			this.displayGuiScreen((GuiScreen) guiyesno);
			break;
		}
		case 10: { // disabled part
			if (anim == null || part == null) {
				return;
			}
			part.setDisable(!((GuiNpcCheckBox) button).isSelected());
			if (GuiScreen.isShiftKeyDown()) { // Shift pressed
				for (AnimationFrameConfig f : anim.frames.values()) {
					f.parts.get(part.id).setDisable(part.isDisable());
				}
			}
			((GuiNpcCheckBox) button).setText(part.isDisable() ? "gui.disabled" : "gui.enabled");
			this.resetAnims();
			break;
		}
		case 11: { // smooth
			if (anim == null || frame == null) {
				return;
			}
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
		case 12: { // color hover
			this.setSubGui(new SubGuiColorSelector(CustomNpcs.colorAnimHoverPart));
			break;
		}
		case 13: { // reset mesh
			if (meshType == 0) {
				meshType = -1;
				button.layerColor = 0xFF360C1C;
			} else {
				meshType = 0;
				button.layerColor = 0xFFD93070;
			}
			if (this.getButton(14) != null) {
				this.getButton(14).layerColor = 0xFF1A0C36;
			}
			if (this.getButton(15) != null) {
				this.getButton(15).layerColor = 0xFF0C3620;
			}
			if (this.getButton(16) != null) {
				this.getButton(16).layerColor = 0xFF35360C;
			}
			break;
		}
		case 14: { // xz mesh
			if (meshType == 1) {
				meshType = -1;
				button.layerColor = 0xFF1A0C36;
			} else {
				meshType = 1;
				button.layerColor = 0xFF6830D9;
			}
			if (this.getButton(13) != null) {
				this.getButton(13).layerColor = 0xFF360C1C;
			}
			if (this.getButton(15) != null) {
				this.getButton(15).layerColor = 0xFF0C3620;
			}
			if (this.getButton(16) != null) {
				this.getButton(16).layerColor = 0xFF35360C;
			}
			break;
		}
		case 15: { // xy mesh
			if (meshType == 2) {
				meshType = -1;
				button.layerColor = 0xFF0C3620;
			} else {
				meshType = 2;
				button.layerColor = 0xFF30D980;
			}
			if (this.getButton(13) != null) {
				this.getButton(13).layerColor = 0xFF360C1C;
			}
			if (this.getButton(14) != null) {
				this.getButton(14).layerColor = 0xFF1A0C36;
			}
			if (this.getButton(16) != null) {
				this.getButton(16).layerColor = 0xFF35360C;
			}
			break;
		}
		case 16: { // xy mesh
			if (meshType == 3) {
				meshType = -1;
				button.layerColor = 0xFF35360C;
			} else {
				meshType = 3;
				button.layerColor = 0xFFD7D930;
			}
			if (this.getButton(13) != null) {
				this.getButton(13).layerColor = 0xFF360C1C;
			}
			if (this.getButton(14) != null) {
				this.getButton(14).layerColor = 0xFF1A0C36;
			}
			if (this.getButton(15) != null) {
				this.getButton(15).layerColor = 0xFF0C3620;
			}
			break;
		}
		case 17: { // show hit box
			showHitBox = !showHitBox;
			button.layerColor = showHitBox ? 0 : 0xFF808080;
			break;
		}
		case 18: { // reset scale
			this.dispScale = 1.0f;
			break;
		}
		case 19: { // reset pos
			for (int i = 0; i < 3; i++) {
				this.dispPos[i] = 0.0f;
			}
			break;
		}
		case 20: { // reset rot
			dispRot[0] = 45.0f;
			dispRot[1] = 345.0f;
			dispRot[2] = 345.0f;
			break;
		}
		case 21: { // npc show
			onlyCurrentPart = !onlyCurrentPart;
			button.txrX = onlyCurrentPart ? 144 : 188;
			break;
		}
		case 22: { // show part
			if (anim == null || part == null) {
				return;
			}
			part.setShow(((GuiNpcCheckBox) button).isSelected());
			if (GuiScreen.isShiftKeyDown()) { // Shift pressed
				for (AnimationFrameConfig f : anim.frames.values()) {
					f.parts.get(part.id).setShow(part.isShow());
				}
			}
			((GuiNpcCheckBox) button).setText(part.isShow() ? "gui.show" : "gui.noshow");
			this.resetAnims();
			break;
		}
		case 23: { // tool pos
			if (toolType == 1) {
				return;
			}
			toolType = 1;
			this.initGui();
			break;
		}
		case 24: { // tool rot
			if (toolType == 0) {
				return;
			}
			toolType = 0;
			this.initGui();
			break;
		}
		case 25: { // tool scale
			if (toolType == 2) {
				return;
			}
			toolType = 2;
			this.initGui();
			break;
		}
		case 26: { // show armor
			this.showArmor = !this.showArmor;
			button.layerColor = (this.showArmor ? 0xFFFF7200 : 0xFF6F3200);
			break;
		}
		case 30: { // reset part set X
			if (part == null) {
				return;
			}
			switch (toolType) {
			case 0:
				part.rotation[0] = 0.5f;
				break;
			case 1:
				part.offset[0] = 0.5f;
				break;
			case 2:
				part.scale[0] = 0.2f;
				break;
			}
			this.initGui();
			break;
		}
		case 31: { // reset part set Y
			if (part == null) {
				return;
			}
			switch (toolType) {
			case 0:
				part.rotation[1] = 0.5f;
				break;
			case 1:
				part.offset[1] = 0.5f;
				break;
			case 2:
				part.scale[1] = 0.2f;
				break;
			}
			this.initGui();
			break;
		}
		case 32: { // reset part set Z
			if (part == null) {
				return;
			}
			switch (toolType) {
			case 0:
				part.rotation[2] = 0.5f;
				break;
			case 1:
				part.offset[2] = 0.5f;
				break;
			case 2:
				part.scale[2] = 0.2f;
				break;
			}
			this.initGui();
			break;
		}
		case 66: { // exit
			this.close();
			break;
		}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		this.displayGuiScreen(this.parent);
		if (!result) {
			return;
		}
		switch (id) {
		case 0: { // remove frame
			if (anim == null || frame == null || anim.frames.size() <= 1) {
				return;
			}
			int f = frame.id - 1;
			if (f < 0) {
				f = 0;
			}
			anim.removeFrame(frame);
			frame = anim.frames.get(f);
			part = frame.parts.get(part.id);

			this.initGui();
			break;
		}
		case 1: { // clear frame
			if (frame == null) {
				return;
			}
			for (PartConfig p : frame.parts.values()) {
				p.clear();
			}
			this.initGui();
			break;
		}
		case 2: { // remove part
			if (frame == null || part == null || frame.parts.size() <= 6) {
				return;
			}
			int f = part.id - 1;
			if (f < 0) {
				f = 0;
			}
			frame.removePart(part);
			part = frame.parts.get(f);
			this.initGui();
			break;
		}
		case 3: { // clear part
			if (part == null) {
				return;
			}
			part.clear();
			this.initGui();
			break;
		}
		case 4: { // clear all frames
			if (anim == null) {
				return;
			}
			for (AnimationFrameConfig f : anim.frames.values()) {
				for (PartConfig p : f.parts.values()) {
					p.clear();
				}
			}
			this.initGui();
			break;
		}
		case 5: { // clear all part
			if (anim == null || part == null) {
				return;
			}
			for (AnimationFrameConfig f : anim.frames.values()) {
				f.parts.get(part.id).clear();
			}
			this.initGui();
			break;
		}
		}
	}

	private void displayOffset(int x, int y) {
		for (int i = 0; i < 2; i++) {
			this.dispPos[i] += (i == 0 ? x : y);
			if (this.dispPos[i] > workS * this.dispScale) {
				this.dispPos[i] = workS * this.dispScale;
			} else if (this.dispPos[i] < -workS * this.dispScale) {
				this.dispPos[i] = -workS * this.dispScale;
			}
		}
	}

	private void displayRotate(int x, int y) {
		this.dispRot[0] += x;
		this.dispRot[1] += Math.cos(this.dispRot[0] * Math.PI / 180.0f) * (float) y;
		this.dispRot[2] += Math.sin(this.dispRot[0] * Math.PI / 180.0f) * (float) y;
		for (int i = 0; i < 3; i++) {
			if (this.dispRot[i] > 360.0f) {
				this.dispRot[i] -= 360.0f;
			} else if (this.dispRot[i] < 0.0f) {
				this.dispRot[i] += 360.0f;
			}
		}
	}

	private void drawCRect(double left, double top, double right, double bottom, int color) {
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
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
		// bottom
		bufferbuilder.pos(left, bottom, -1.0D).endVertex();
		bufferbuilder.pos(right, bottom, -1.0D).endVertex();
		bufferbuilder.pos(right, bottom, 0.0D).endVertex();
		bufferbuilder.pos(left, bottom, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	private void drawLine(double x, double y, double z, double dist, int type, float red, float green, float blue) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		if (type == 0 || type < 0) {
			buffer.pos(x - dist, y, z).color(red, green, blue, 1.0f).endVertex();
			buffer.pos(x + dist, y, z).color(red, green, blue, 1.0f).endVertex();
		}
		if (type == 1 || type < 0) {
			buffer.pos(x, y - dist, z).color(red, green, blue, 1.0f).endVertex();
			buffer.pos(x, y + dist, z).color(red, green, blue, 1.0f).endVertex();
		}
		if (type == 2 || type < 0) {
			buffer.pos(x, y, z - dist).color(red, green, blue, 1.0f).endVertex();
			buffer.pos(x, y, z + dist).color(red, green, blue, 1.0f).endVertex();
		}
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void drawNpc(EntityNPCInterface showNPC) {
		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.translate(0.5f, 0.0f, -0.5f);
		this.mc.getRenderManager().playerViewY = 180.0f;
		if (showHitBox) {
			GlStateManager.glLineWidth(1.0F);
			GlStateManager.disableTexture2D();
			RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(showNPC.width / -2.0d, 0.0d, showNPC.width / -2.0d,
					showNPC.width / 2.0d, showNPC.height, showNPC.width / 2.0d), 1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableTexture2D();
		}
		this.mc.getRenderManager().renderEntity(showNPC, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		for (Entity e : environmentEntitys) {
			int x = Math.abs((int) Math.round(e.posX));
			int y = Math.abs((int) Math.round(e.posX));
			int z = Math.abs((int) Math.round(e.posX));
			int d = x > y ? x : x > z ? x : y > z ? y : z;
			if (d > blockSize) {
				continue;
			}
			this.mc.getRenderManager().renderEntity(e, e.posX, e.posY, e.posZ, 0.0f, 1.0f, false);
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.sw == null) {
			this.sw = new ScaledResolution(this.mc);
		}
		if (waitKey != 0) {
			waitKey--;
		}
		for (int i = 0; i < 2; i++) {
			EntityNPCInterface dNpc = i == 0 ? this.npcAnim : this.npcPart;
			if (dNpc == null) {
				continue;
			}
			dNpc.field_20061_w = this.npc.field_20061_w;
			dNpc.field_20062_v = this.npc.field_20062_v;
			dNpc.field_20063_u = this.npc.field_20063_u;
			dNpc.field_20064_t = this.npc.field_20064_t;
			dNpc.field_20065_s = this.npc.field_20065_s;
			dNpc.field_20066_r = this.npc.field_20066_r;
			dNpc.ticksExisted = this.npc.ticksExisted;
		}
		for (Entity e : environmentEntitys) {
			e.ticksExisted = this.npc.ticksExisted;
		}
		EntityNPCInterface showNPC = this.getDisplayNpc();
		if (showNPC == null) {
			this.close();
		}
		// display data
		if (Mouse.isButtonDown(mousePressId)) {
			int x = mouseX - mousePressX;
			int y = mouseY - mousePressY;
			if (x != 0 || y != 0) {
				if (mousePressId == 0) {
					this.displayOffset(x, y);
				} // LMB
				else if (mousePressId == 1) {
					this.displayRotate(x, y);
				} // RMB
				mousePressX = mouseX;
				mousePressY = mouseY;
			}
		} else {
			mousePressId = -1;
		}
		this.hovered = this.isMouseHover(mouseX, mouseY, workU + 1, workV + 1, workS - 2, workS - 2);
		if (hovered) {
			int dWheel = Mouse.getDWheel();
			if (hovered && dWheel != 0) {
				this.dispScale += this.dispScale * (dWheel < 0 ? 0.1f : -0.1f);
				if (this.dispScale < 0.5f) {
					this.dispScale = 0.5f;
				} else if (this.dispScale > 5.0f) {
					this.dispScale = 5.0f;
				}
				this.dispScale = (float) (Math.round(this.dispScale * 20.0d) / 20.0d);
				if (this.dispScale == 0.95f || this.dispScale == 1.05f) {
					this.dispScale = 1.0f;
				}
			}
		}
		// back place
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, -300.0f);
		this.drawGradientRect(winU + 1, winV + 1, winU + winW - 1, winV + winH - 1, 0xFFC6C6C6, 0xFFC6C6C6);
		this.drawHorizontalLine(winU + 1, winU + winW - 2, winV, 0xFF000000);
		this.drawVerticalLine(winU, winV, winV + winH - 1, 0xFF000000);
		this.drawVerticalLine(winU + winW - 1, winV, winV + winH - 1, 0xFF000000);
		this.drawHorizontalLine(winU + 1, winU + winW - 2, winV + winH - 1, 0xFF000000);
		// work place
		int color = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFF080F0 : 0xFFF020F0;
		this.drawGradientRect(workU, workV, workU + workS, workV + workS, color, color);
		// Lines
		this.drawHorizontalLine(winU + 2, winU + 138, winV + 24, 0xFF000000); // common
		this.drawHorizontalLine(winU + 2, winU + 138, winV + 97, 0xFF000000); // frame
		for (int i = 0; i < 17; i++) {
			this.drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + 170, 0xFF000000); // part sets
		}
		GlStateManager.popMatrix();
		if (this.subgui == null) {
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = sw.getScaledWidth() < this.mc.displayWidth
					? (int) Math.round((double) this.mc.displayWidth / (double) sw.getScaledWidth())
					: 1;
			GL11.glScissor((workU + 1) * c, this.mc.displayHeight - (workV + workS - 1) * c, (workS - 2) * c,
					(workS - 2) * c);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawWork(showNPC, partialTicks);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 950.0f);
			// axis xyz vector
			GlStateManager.pushMatrix();
			GlStateManager.translate(workU + 12.5f, workV + 12.0f, this.dispScale > 1.0f ? -240.0f : 0.0f);
			if (this.dispRot[0] != 0.0f) {
				GlStateManager.rotate(this.dispRot[0], 0.0f, 1.0f, 0.0f);
			}
			if (this.dispRot[1] != 0.0f) {
				GlStateManager.rotate(this.dispRot[1], 0.0f, 0.0f, 1.0f);
			}
			if (this.dispRot[2] != 0.0f) {
				GlStateManager.rotate(this.dispRot[2], 1.0f, 0.0f, 0.0f);
			}
			GlStateManager.pushMatrix();
			GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
			GlStateManager.translate(0.0f, 0.0f, 0.5f);
			this.drawCRect(-10.5, -0.5d, -0.5d, 0.5d, 0xFF0000FF);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			this.drawCRect(-10.5, -0.5d, -0.5d, 0.5d, 0xFFFF0000);
			this.drawCRect(-0.5d, -10.5, 0.5d, -0.5d, 0xFF00D000);
			this.drawCRect(-0.5d, -0.5d, 0.5d, 0.5d, GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000);
			GlStateManager.popMatrix();
			GlStateManager.popMatrix();
			// display info
			GlStateManager.pushMatrix();
			GlStateManager.translate(workU, workV, 0.0f);
			String ts = "x" + this.dispScale;
			this.fontRenderer.drawString(ts, workS - 11 - this.fontRenderer.getStringWidth(ts), 1,
					GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000, false);
			ts = (int) this.dispRot[0] + "" + ((char) 176) + "/" + (int) this.dispRot[1] + ((char) 176) + "/"
					+ (int) this.dispRot[2] + ((char) 176);
			this.fontRenderer.drawString(ts, workS - 11 - this.fontRenderer.getStringWidth(ts), workS - 10,
					GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000, false);
			ts = (int) this.dispPos[0] + "/" + (int) this.dispPos[1];
			this.fontRenderer.drawString(ts, 11, workS - 10,
					GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000, false);
			GlStateManager.popMatrix();
			GlStateManager.popMatrix();
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(workU + 1, workV + 1, workU + workS - 1, workV + workS - 1, GuiNpcAnimation.backColor);
			GlStateManager.popMatrix();
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, 975.0f);
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.popMatrix();

		if (this.hasSubGui() || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (scrollParts != null && scrollParts.hovered) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.sel").getFormattedText());
		} else if (this.getLabel(0) != null && this.getLabel(0).hovered) {
			this.setHoverText(new TextComponentTranslation("animation.hover.help").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.block.type").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.block.size").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.color").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("animation.hover.frame", "" + (frame.id + 1)).getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.frame.add").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.frame.del").getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.frame")
					.appendSibling(new TextComponentTranslation("animation.hover.shift.0")).getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.add")
					.appendSibling(new TextComponentString("<br>"))
					.appendSibling(new TextComponentTranslation("gui.wip")).getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.del").getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.part")
					.appendSibling(new TextComponentTranslation("animation.hover.shift.0")).getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.disabled." + !part.isDisable())
					.appendSibling(new TextComponentTranslation("animation.hover.shift.1")).getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("animation.hover.smooth." + frame.isSmooth()).getFormattedText());
		} else if (this.getButton(12) != null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.color").getFormattedText());
		} else if (this.getButton(13) != null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.mesh.0")
					.appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(14) != null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.mesh.1")
					.appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(15) != null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.mesh.2")
					.appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(16) != null && this.getButton(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.mesh.3")
					.appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(18) != null && this.getButton(17).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.hitbox")
					.appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(18) != null && this.getButton(18).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.scale").getFormattedText());
		} else if (this.getButton(19) != null && this.getButton(19).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.pos").getFormattedText());
		} else if (this.getButton(20) != null && this.getButton(20).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.rot").getFormattedText());
		} else if (this.getButton(21) != null && this.getButton(21).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.work." + this.onlyCurrentPart,
					((char) 167) + "6" + (frame != null ? frame.id + 1 : -1)).getFormattedText());
		} else if (this.getButton(22) != null && this.getButton(22).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.show." + part.isShow())
					.appendSibling(new TextComponentTranslation("animation.hover.shift.1")).getFormattedText());
		} else if (this.getButton(23) != null && this.getButton(23).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.0").getFormattedText());
		} else if (this.getButton(24) != null && this.getButton(24).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.1").getFormattedText());
		} else if (this.getButton(25) != null && this.getButton(25).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.2").getFormattedText());
		} else if (this.getButton(26) != null && this.getButton(26).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.show.armor").getFormattedText());
		} else if (this.getButton(30) != null && this.getButton(30).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("animation.hover.reset." + toolType, "X").getFormattedText());
		} else if (this.getButton(31) != null && this.getButton(31).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("animation.hover.reset." + toolType, "Y").getFormattedText());
		} else if (this.getButton(32) != null && this.getButton(32).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("animation.hover.reset." + toolType, "Z").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.anim.repeat").getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.ticks").getFormattedText());
		} else if (this.getTextField(2) != null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.delay").getFormattedText());
		} else if ((this.getTextField(5) != null && this.getTextField(5).isMouseOver())
				|| (this.getSlider(0) != null && this.getSlider(0).isMouseOver())) {
			this.setHoverText(new TextComponentTranslation(
					"animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"),
					"X").getFormattedText());
		} else if ((this.getTextField(6) != null && this.getTextField(6).isMouseOver())
				|| (this.getSlider(1) != null && this.getSlider(1).isMouseOver())) {
			this.setHoverText(new TextComponentTranslation(
					"animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"),
					"Y").getFormattedText());
		} else if ((this.getTextField(7) != null && this.getTextField(7).isMouseOver())
				|| (this.getSlider(2) != null && this.getSlider(2).isMouseOver())) {
			this.setHoverText(new TextComponentTranslation(
					"animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"),
					"Z").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@SuppressWarnings("unchecked")
	private void drawWork(EntityNPCInterface showNPC, float partialTicks) {
		// work place
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, -300.0f);
		Gui.drawRect(workU + 1, workV + 1, workU + workS - 1, workV + workS - 1, GuiNpcAnimation.backColor);
		GlStateManager.popMatrix();

		// blocks
		GlStateManager.pushMatrix();
		this.postRender();
		RenderHelper.enableGUIStandardItemLighting();
		IBlockState state;
		switch (blockType) {
		case 1:
			state = Blocks.AIR.getDefaultState();
			break;
		case 3:
			state = Blocks.STONE_STAIRS.getDefaultState();
			break;
		case 4:
			state = Blocks.STONE_SLAB.getDefaultState();
			break;
		case 5:
			state = Blocks.CARPET.getDefaultState();
			break;
		default:
			state = Blocks.STONE.getDefaultState();
			break;
		}
		this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float ytr = offsetY;
		GlStateManager.translate(-8.0f * winScale, ytr * winScale, 8.0f * winScale);
		GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.scale(-16.0f, -16.0f, -16.0f);
		GlStateManager.scale(winScale, winScale, winScale);
		int yH = blockType == 0 ? blockSize : 0;
		Map<BlockPos, TileEntity> tiles = Maps.newHashMap();
		for (int y = -yH; y <= yH; y++) {
			// if (y < 1) { continue; }
			for (int x = -blockSize; x <= blockSize; x++) {
				for (int z = -blockSize; z <= blockSize; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					TileEntity tile = null;
					if (blockType == 0) {
						IBlockState s = this.environmentStates.get(pos);
						if (s != null) {
							state = (IBlockState) s;
						}
						TileEntity t = this.environmentTiles.get(pos);
						if (t != null) {
							tile = (TileEntity) t;
						}
					}
					if (tile != null) {
						TileEntitySpecialRenderer<TileEntity> render = (TileEntitySpecialRenderer<TileEntity>) TileEntityRendererDispatcher.instance.renderers
								.get(tile.getClass());
						if (render != null) {
							tiles.put(pos, tile);
							continue;
						}
					}
					if (state.getBlock() instanceof BlockAir) {
						continue;
					}
					GlStateManager.pushMatrix();
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					GlStateManager.translate(x, (y - 1), z);
					if (blockType == 4) {
						GlStateManager.translate(0.0f, 0.5f, 0.0f);
					} else if (blockType == 5) {
						GlStateManager.translate(0.0f, 0.9375f, 0.0f);
					}
					this.mc.getBlockRendererDispatcher().renderBlockBrightness(state, 1.0f);
					GlStateManager.popMatrix();
				}
			}
		}
		for (BlockPos p : tiles.keySet()) {
			TileEntity tile = tiles.get(p);
			TileEntitySpecialRenderer<TileEntity> render = (TileEntitySpecialRenderer<TileEntity>) TileEntityRendererDispatcher.instance.renderers
					.get(tile.getClass());
			if (render != null) {
				GlStateManager.pushMatrix();
				render.render(tile, (double) p.getX(), (double) p.getY() - 1, (double) (p.getZ() - 1), partialTicks, 0,
						1.0f);
				GlStateManager.popMatrix();
				continue;
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, -1.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (meshType == 0) {
			drawLine(0.0d, 0.0d, 0.0d, 10.0d, 0, 1.0f, 0.0f, 0.0f);
			drawLine(0.0d, 0.0d, 0.0d, 10.0d, 1, 0.0f, 1.0f, 0.0f);
			drawLine(0.0d, 0.0d, 0.0d, 10.0d, 2, 0.0f, 0.0f, 1.0f);
		} else if (meshType == 1) {
			drawLine(0.0d, 0.0d, -11.0d, 11.0d, 0, 1.0f, 1.0f, 1.0f);
			drawLine(-11.0d, 0.0d, 0.0d, 11.0d, 2, 1.0f, 1.0f, 1.0f);
			for (int i = -10; i <= 11; i++) {
				drawLine(0.0d, 0.0d, i, 11.0d, 0, 1.0f, 1.0f, 1.0f);
				drawLine(i, 0.0d, 0.0d, 11.0d, 2, 1.0f, 1.0f, 1.0f);
			}
		} else if (meshType == 2) {
			drawLine(0.0d, -11.0d, 0.0d, 11.0d, 0, 1.0f, 1.0f, 1.0f);
			drawLine(-11.0d, 0.0d, 0.0d, 11.0d, 1, 1.0f, 1.0f, 1.0f);
			for (int i = -10; i <= 11; i++) {
				drawLine(0.0d, i, 0.0d, 11.0d, 0, 1.0f, 1.0f, 1.0f);
				drawLine(i, 0.0d, 0.0d, 11.0d, 1, 1.0f, 1.0f, 1.0f);
			}
		} else if (meshType == 3) {
			drawLine(0.0d, 0.0d, -11.0d, 11.0d, 1, 1.0f, 1.0f, 1.0f);
			drawLine(0.0d, -11.0d, 0.0d, 11.0d, 2, 1.0f, 1.0f, 1.0f);
			for (int i = -10; i <= 11; i++) {
				drawLine(0.0d, 0.0d, i, 11.0d, 1, 1.0f, 1.0f, 1.0f);
				drawLine(0.0d, i, 0.0d, 11.0d, 2, 1.0f, 1.0f, 1.0f);
			}
		}
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		// npc
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawNpc(showNPC);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popMatrix();
	}

	public EntityNPCInterface getDisplayNpc() {
		return this.onlyCurrentPart ? this.npcPart : this.npcAnim;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.sw = new ScaledResolution(this.mc);
		if ((sw.getScaledWidth() - 144) < sw.getScaledHeight() - 8) {
			workS = sw.getScaledWidth() - 144;
			winU = 0;
			winW = sw.getScaledWidth();
			winV = (sw.getScaledHeight() - workS - 8) / 2;
			winH = workS + 8;

		} else {
			workS = sw.getScaledHeight() - 8;
			winU = (sw.getScaledWidth() - workS - 144) / 2;
			winW = workS + 144;
			winV = 0;
			winH = sw.getScaledHeight();
		}
		workU = winU + 140;
		workV = winV + 4;
		winScale = (float) workS / 100.0f;
		int x = winU + 3, y = winV + 12, lId = 0;
		GuiNpcButton button;
		GuiNpcTextField textField;
		// Common
		this.addLabel(new GuiNpcLabel(lId++, ((char) 167) + "n" + ((char) 167) + "l?", workU - 6, workV, 0xFF000000));
		this.addLabel(new GuiNpcLabel(lId++, "animation.place", x, y - 10));
		this.addButton(new GuiButtonBiDirectional(0, x, y, 105, 10, blockNames, blockType));
		button = new GuiNpcButton(1, x + 107, y, 17, 10, blockSizes, blockSize);
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(2, workU + 2, workV + 23, 8, 8, "");
		button.layerColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(26, workU + 2, workV + 31, 8, 8, "");
		button.layerColor = (this.showArmor ? 0xFFFF7200 : 0xFF6F3200);
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);

		// Frame
		this.addLabel(new GuiNpcLabel(lId++, "animation.frames", x, (y += 24) - 10));
		List<String> lFrames = Lists.newArrayList();
		for (int i = 0; i < anim.frames.size(); i++) {
			lFrames.add("" + (i + 1) + "/" + anim.frames.size());
		}
		this.addButton(
				new GuiButtonBiDirectional(3, x, y, 60, 20, lFrames.toArray(new String[lFrames.size()]), frame.id));
		button = new GuiNpcButton(4, x + 62, y, 10, 10, ""); // add frame
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(5, x + 62, y + 10, 10, 10, ""); // del frame
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = anim.frames.size() > 1;
		this.addButton(button);
		button = new GuiNpcButton(6, x + 126, y + 4, 10, 10, ""); // clear frame
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.time").getFormattedText() + ":", x,
				(y += 23) + 2));
		textField = new GuiNpcTextField(1, this, x + 35, y, 48, 12, "" + frame.getSpeed());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 3600, frame.getSpeed());
		this.addTextField(textField);
		textField = new GuiNpcTextField(2, this, x + 87, y, 48, 12, "" + frame.getEndDelay());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 3600, frame.getEndDelay());
		this.addTextField(textField);
		y += 21;
		if (anim.type == AnimationKind.DIES) {
			this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.repeat").getFormattedText() + ":", x,
					y - 7));
			if (anim.repeatLast < 0) {
				anim.repeatLast *= -1;
			}
			if (anim.repeatLast > anim.frames.size()) {
				anim.repeatLast = anim.frames.size();
			}
			textField = new GuiNpcTextField(0, this, x, y + 3, 48, 12, "" + anim.repeatLast);
			textField.setNumbersOnly();
			textField.setMinMaxDefault(0, anim.frames.size(), anim.repeatLast);
			this.addTextField(textField);
		}
		button = new GuiNpcCheckBox(11, x + 52, y + 1, 84, 14, frame.isSmooth() ? "gui.smooth" : "gui.linearly");
		((GuiNpcCheckBox) button).setSelected(frame.isSmooth());
		this.addButton(button);

		// Part
		this.addLabel(new GuiNpcLabel(lId++, "animation.parts", x, (y += 29) - 10));
		dataParts.clear();
		List<String> lParts = Lists.<String>newArrayList();
		for (int id : frame.parts.keySet()) {
			String key;
			PartConfig ps = frame.parts.get(id);
			switch (id) {
			case 0:
				key = "model.head";
				break;
			case 1:
				key = "model.larm";
				break;
			case 2:
				key = "model.rarm";
				break;
			case 3:
				key = "model.body";
				break;
			case 4:
				key = "model.lleg";
				break;
			case 5:
				key = "model.rleg";
				break;
			default:
				key = ps.name;
				break;
			}
			dataParts.put(key, ps);
			lParts.add(key);
		}
		if (this.scrollParts == null) {
			(this.scrollParts = new GuiCustomScroll(this, 0)).setSize(60, 60);
		}
		this.scrollParts.setListNotSorted(lParts);
		this.scrollParts.guiLeft = x;
		this.scrollParts.guiTop = y;
		this.scrollParts.selected = part.id;
		this.addScroll(this.scrollParts);

		button = new GuiNpcButton(7, x + 62, y, 10, 10, ""); // add part
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(8, x + 62, y + 10, 10, 10, ""); // del part
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = lParts.size() > 6;
		this.addButton(button);
		button = new GuiNpcButton(9, x + 126, y, 10, 10, ""); // clear part
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);

		button = new GuiNpcCheckBox(10, x + 62, y + 20, 74, 14, part.isDisable() ? "gui.disabled" : "gui.enabled");
		((GuiNpcCheckBox) button).setSelected(!part.isDisable());
		this.addButton(button);
		button = new GuiNpcCheckBox(22, x + 62, y + 34, 74, 14, part.isShow() ? "gui.show" : "gui.noshow");
		((GuiNpcCheckBox) button).setSelected(part.isShow());
		this.addButton(button);
		String color;
		for (color = Integer.toHexString(CustomNpcs.colorAnimHoverPart); color.length() < 6; color = "0" + color) {
		}
		button = new GuiNpcButton(12, x + 62, y + 50, 74, 10, color); // color hover
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		button.setTextColor(CustomNpcs.colorAnimHoverPart);
		button.dropShadow = false;
		this.addButton(button);

		y += 63;
		int f = 18;
		for (int i = 0; i < 3; i++) {
			this.addLabel(new GuiNpcLabel(lId++, i == 0 ? "X:" : i == 1 ? "Y:" : "Z:", x, y + i * f + 4));
			float[] values = toolType == 0 ? part.rotation : toolType == 1 ? part.offset : part.scale;
			float[] datas = new float[3];
			switch (toolType) {
			case 1: {
				for (int j = 0; j < 3; j++) {
					datas[j] = (float) (Math.round((10.0f * values[i] - 5.0f) * 1000.0f) / 1000.0d);
				}
				break;
			}
			case 2: {
				for (int j = 0; j < 3; j++) {
					datas[j] = (float) (Math.round(5000.0f * values[i]) / 1000.0d);
				}
				break;
			}
			default: {
				for (int j = 0; j < 3; j++) {
					datas[j] = (float) (Math.round(3600.0f * values[i]) / 10.0d);
				}
				break;
			}
			}
			this.addSlider(new GuiNpcSlider(this, i, x + 8, y + i * f, 128, 8, values[i]));
			textField = new GuiNpcTextField(i + 5, this, x + 9, y + 9 + i * f, 56, 8, "" + datas[i]);
			textField.setDoubleNumbersOnly();
			double m = 0.0d, n = 360.0d;
			if (toolType == 1) {
				m = -5.0d;
				n = 5.0d;
			} else if (toolType == 1) {
				m = 0.0d;
				n = 5.0d;
			}
			switch (i) {
			case 1:
				textField.setMinMaxDoubleDefault(m, n, (double) datas[i]);
				break;
			case 2:
				textField.setMinMaxDoubleDefault(m, n, (double) datas[i]);
				break;
			default:
				textField.setMinMaxDoubleDefault(m, n, (double) datas[i]);
				break;
			}
			this.addTextField(textField);
			button = new GuiNpcButton(30 + i, x + 67, y + 9 + i * f, 8, 8, "X");
			button.texture = btns;
			button.hasDefBack = false;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			this.addButton(button);
		}
		button = new GuiNpcButton(66, x, y + 56, 50, 10, "gui.back"); // color hover
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		// work
		button = new GuiNpcButton(13, workU + 25, workV + 2, 8, 8, ""); // simple mesh
		button.layerColor = meshType == 0 ? 0xFFD93070 : 0xFF360C1C;
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(14, workU + 34, workV + 2, 8, 8, ""); // xz mesh
		button.layerColor = meshType == 1 ? 0xFF6830D9 : 0xFF1A0C36;
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(15, workU + 43, workV + 2, 8, 8, ""); // xY mesh
		button.layerColor = meshType == 2 ? 0xFF30D980 : 0xFF0C3620;
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(16, workU + 52, workV + 2, 8, 8, ""); // zy mesh
		button.layerColor = meshType == 3 ? 0xFFD7D930 : 0xFF35360C;
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(17, workU + 61, workV + 2, 8, 8, ""); // show hit box
		button.layerColor = showHitBox ? 0 : 0xFF808080;
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);

		button = new GuiNpcButton(18, workU + workS - 10, workV + 2, 8, 8, ""); // reset scale
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(19, workU + 2, workV + workS - 10, 8, 8, ""); // reset pos
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(20, workU + workS - 10, workV + workS - 10, 8, 8, ""); // reset rot
		button.texture = btns;
		button.hasDefBack = false;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(21, workU + workS / 2 - 11, workV + workS - 12, 18, 10, ""); // part or anim
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = onlyCurrentPart ? 144 : 188;
		button.txrW = 44;
		button.txrH = 24;
		this.addButton(button);

		y = workV + workS - 64;
		button = new GuiNpcButton(23, workU + 2, y, 14, 14, ""); // tool pos
		button.texture = btns;
		button.hasDefBack = false;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 1 ? 0xFFFF8080 : 0xFFFFFFFF;
		this.addButton(button);
		button = new GuiNpcButton(24, workU + 2, y += 16, 14, 14, ""); // tool rot
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = 24;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 0 ? 0xFF80FF80 : 0xFFFFFFFF;
		this.addButton(button);
		button = new GuiNpcButton(25, workU + 2, y += 16, 14, 14, ""); // tool scale
		button.texture = btns;
		button.hasDefBack = false;
		button.txrX = 48;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 2 ? 0xFF8080FF : 0xFFFFFFFF;
		this.addButton(button);
		this.resetAnims();
	}

	private boolean isPressAltAndKey(int key, int id) {
		if (waitKey > 0 && waitKeyID == id) {
			return false;
		}
		boolean isPress = false;
		if (key == 56 || key == 184) {
			isPress = Keyboard.isKeyDown(id);
		} else if (key == id) {
			isPress = Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
		}
		if (isPress) {
			waitKey = 30;
			waitKeyID = id;
		}
		return isPress;
	}

	@Override
	public void keyTyped(char c, int i) {
		if (this.subgui == null) {
			if (i == 1) {
				this.close();
				return;
			}
			// tool pos - Alt + Q
			if (isPressAltAndKey(i, 16) && toolType != 1) {
				toolType = 1;
				playButtonClick();
				this.initGui();
			}
			// tool rot - Alt + W
			if (isPressAltAndKey(i, 17) && toolType != 0) {
				toolType = 0;
				playButtonClick();
				this.initGui();
			}
			// tool rot - Alt + E
			if (isPressAltAndKey(i, 18) && toolType != 2) {
				toolType = 2;
				playButtonClick();
				this.initGui();
			}
			// play stop - Alt + P
			if (isPressAltAndKey(i, 25)) {
				onlyCurrentPart = !onlyCurrentPart;
				if (this.getButton(21) != null) {
					this.getButton(21).txrX = onlyCurrentPart ? 144 : 188;
				}
				playButtonClick();
			}
			// reset scale - Alt + S
			if (isPressAltAndKey(i, 31)) {
				dispScale = 1.0f;
				playButtonClick();
			}
			// reset rot - Alt + R
			if (isPressAltAndKey(i, 19)) {
				dispRot[0] = 45.0f;
				dispRot[1] = 345.0f;
				dispRot[2] = 345.0f;
				playButtonClick();
			}
			// reset pos - Alt + O
			if (isPressAltAndKey(i, 24)) {
				for (int j = 0; j < 3; j++) {
					dispPos[j] = 0.0f;
				}
				playButtonClick();
			}
		}
		super.keyTyped(c, i);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if ((mouseBottom == 0 || mouseBottom == 1) && hovered) {
			mousePressId = mouseBottom;
			mousePressX = mouseX;
			mousePressY = mouseY;
		}
		super.mouseClicked(mouseX, mouseY, mouseBottom);
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		if (part == null) {
			return;
		}
		float value = 0.0f;
		switch (toolType) {
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
		if (this.getTextField(5 + slider.id) != null) {
			this.getTextField(5 + slider.id).setText("" + value);
		}
		this.resetAnims();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
	}

	private void playButtonClick() {
		this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	private void postRender() {
		GlStateManager.translate(workU + workS / 2.0f, workV + workS / 2.0f, 100.0f * dispScale); // center
		GlStateManager.translate(this.dispPos[0], this.dispPos[1], 0.0f);
		GlStateManager.rotate(this.dispRot[0], 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(this.dispRot[1], 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(this.dispRot[2], 0.0f, 0.0f, 1.0f);
		GlStateManager.scale(dispScale, dispScale, dispScale);
		GlStateManager.translate(0.0f, 25.0f, 0.0f);
	}

	private void resetAnims() {
		if (anim == null || frame == null || npcAnim == null) {
			this.npcPart = null;
			return;
		}

		NBTTagCompound npcNbt = new NBTTagCompound();
		this.npcAnim.animation.clear();
		this.npcAnim.writeEntityToNBT(npcNbt);
		this.npcAnim.writeToNBTOptional(npcNbt);
		this.npcAnim.animation.activeAnim = anim.copy();
		this.npcAnim.animation.activeAnim.isEdit = true;
		this.npcAnim.animation.activeAnim.type = AnimationKind.STANDING;
		this.npcAnim.setHealth(this.npcAnim.getMaxHealth());
		this.npcAnim.deathTime = 0;
		if (this.npcPart == null) {
			Entity animNpc = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
			if (animNpc instanceof EntityNPCInterface) {
				this.npcPart = (EntityNPCInterface) animNpc;
				this.npcPart.animation.clear();
				this.npcPart.display.setName("0_" + this.npc.getName());
				this.npcPart.rotationYaw = this.npcAnim.rotationYaw;
				this.npcPart.rotationPitch = this.npcAnim.rotationPitch;
				this.npcPart.ais.orientation = this.npcAnim.ais.orientation;
				this.npcPart.ais.setStandingType(1);
			}
		}
		if (this.npcPart == null) {
			return;
		}
		this.npcPart.animation.activeAnim = anim.copy();
		this.npcPart.animation.activeAnim.frames.clear();
		this.npcPart.animation.activeAnim.frames.put(0, frame);
		this.npcPart.animation.activeAnim.isEdit = true;
		this.npcPart.animation.activeAnim.type = AnimationKind.STANDING;
		this.npcPart.setHealth(this.npcPart.getMaxHealth());
		this.npcPart.deathTime = 0;
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (!this.dataParts.containsKey(scroll.getSelected())) {
			return;
		}
		part = dataParts.get(scroll.getSelected());
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	private void setEnvironment() {
		this.environmentEntitys.clear();
		this.environmentStates.clear();
		this.environmentTiles.clear();
		if (this.npc == null || this.npc.world == null) {
			return;
		}
		offsetY = 0.0f;
		if (this.npc.posY != Math.floor(this.npc.posY)) {
			offsetY = (float) ((this.npc.posY - Math.round(this.npc.posY)) * 16.0f);
		}
		for (int y = -4; y <= 4; y++) {
			for (int x = -4; x <= 4; x++) {
				for (int z = -4; z <= 4; z++) {
					double yP = this.npc.posY + y - 1;
					if (this.npc.posY != Math.floor(this.npc.posY)) {
						yP = Math.ceil(this.npc.posY) + y - 1;
					}
					BlockPos posWorld = new BlockPos(this.npc.posX + x, yP, this.npc.posZ + z);
					IBlockState state = this.npc.world.getBlockState(posWorld);
					TileEntity tile = this.npc.world.getTileEntity(posWorld);
					if (tile != null && TileEntityRendererDispatcher.instance.renderers.get(tile.getClass()) == null) {
						tile = null;
					}
					BlockPos pos = new BlockPos(x, y, z);
					this.environmentStates.put(pos, state);
					this.environmentTiles.put(pos, tile);
				}
			}
		}
		List<Entity> entities = this.npc.world.getEntitiesWithinAABB(Entity.class,
				new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(this.npc.getPosition()).grow(4.55d, 4.55d,
						4.55d));
		for (Entity e : entities) {
			if (e.equals(this.npc)) {
				continue;
			}
			NBTTagCompound nbt = new NBTTagCompound();
			e.writeToNBTAtomically(nbt);
			Entity le = EntityList.createEntityFromNBT(nbt, this.npc.world);
			if (le != null) {
				le.posX -= this.npc.posX;
				le.posY -= this.npc.posY;
				le.posZ -= this.npc.posZ;
				this.environmentEntitys.add(le);
			}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui.id == 0 && subgui instanceof SubGuiColorSelector) {
			CustomNpcs.colorAnimHoverPart = ((SubGuiColorSelector) subgui).color;
			this.initGui();
		}
		/*
		 * if (subgui.id == 1 && subgui instanceof SubGuiAddAnimationPart) {
		 * 
		 * }
		 */
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.hasSubGui() || anim == null) {
			return;
		}
		switch (textField.getId()) {
		case 0: { // repeatLast
			if (anim != null) {
				anim.setRepeatLast(textField.getInteger());
				this.resetAnims();
			}
			break;
		}
		case 1: { // speed
			if (frame == null) {
				return;
			}
			frame.setSpeed(textField.getInteger());
			this.resetAnims();
			break;
		}
		case 2: { // delay
			if (frame == null) {
				return;
			}
			frame.setEndDelay(textField.getInteger());
			this.resetAnims();
			break;
		}
		case 5: { // rotation X
			if (part == null) {
				return;
			}
			float value = 0.0f;
			switch (toolType) {
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
			if (this.getSlider(0) != null) {
				this.getSlider(0).sliderValue = value;
			}
			this.resetAnims();
			break;
		}
		case 6: { // rotation Y
			if (part == null) {
				return;
			}
			float value = 0.0f;
			switch (toolType) {
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
			if (this.getSlider(1) != null) {
				this.getSlider(1).sliderValue = value;
			}
			this.resetAnims();
			break;
		}
		case 7: { // rotation Z
			if (part == null) {
				return;
			}
			float value = 0.0f;
			switch (toolType) {
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
			if (this.getSlider(2) != null) {
				this.getSlider(2).sliderValue = value;
			}
			this.resetAnims();
			break;
		}
		}
	}

}
