package noppes.npcs.client.gui.animation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import noppes.npcs.LogWriter;
import noppes.npcs.api.util.IRayTraceVec;
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
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.SubGuiSelectItemStack;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcMiniWindow;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IComponentGui;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.client.model.animation.AddedPartConfig;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.PartConfig;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class SubGuiEditAnimation
		extends SubGuiInterface
		implements ISubGuiListener, ISliderListener, ICustomScrollListener, ITextfieldListener, GuiYesNoCallback {

	// data
	public static int meshType = 1;
	public static boolean showHitBox = true;
	public AnimationConfig anim;
	public AnimationFrameConfig frame;
	public PartConfig part;
	private GuiCustomScroll scrollParts;
	private int blockType, blockSize, toolType, waitKey, waitKeyID;
	private boolean onlyCurrentPart, hovered, hasExtend, hoverRight, hoverLeft, isHitPox;
	private final EntityNPCInterface npcAnim;
    private final EntityNPCInterface npcPart;
	private final String[] blockNames, blockSizes;
	// display
	private int workU, workV, workS, winU, winV, winW, winH, mousePressId, mousePressX, mousePressY;
	private final float[] dispRot, dispPos;
	private float dispScale, winScale, offsetY;
	private final Map<String, PartConfig> dataParts;
	private final List<Entity> environmentEntitys;
	private final Map<BlockPos, IBlockState> environmentStates;
	private final Map<BlockPos, TileEntity> environmentTiles;

	private GuiNpcMiniWindow partNames, tools;
	private double w = -1.0d, h = -1.0d;
	private String hitBoxPartName = "";
	private float baseRotation = 0.0f;

	public SubGuiEditAnimation(EntityNPCInterface npc, AnimationConfig anim, int id, GuiNpcAnimation parent) {
		super(npc);
		this.id = id;
		this.ySize = 240;
		this.xSize = 427;
		this.parent = parent;
		this.anim = anim;
		frame = anim.frames.get(0);
		this.setPart(frame.parts.get(3));
		waitKey = 0;

		// Display
		toolType = 0; // 0 - rotation, 1 - offset, 2 - scale
		blockType = 0; // 0 - environment, 1 - non, 2 - stone, 3 - stairs, 4 - stone_slab, 5 - carpet
		blockSize = 2; // 0 - x1, 1 - x3, 2 - x5, 3 - x7, 4 - x9
		dispScale = 1.0f;
		winScale = 1.0f;
		dispRot = new float[] { 45.0f, 345.0f, 345.0f };
		dispPos = new float[] { 0.0f, 0.0f, 0.0f };
		dataParts = Maps.newLinkedHashMap();
		environmentEntitys = Lists.newArrayList();
		environmentStates = Maps.newHashMap();
		environmentTiles = Maps.newHashMap();
		setEnvironment();
		mousePressId = -1;
		mousePressX = 0;
		mousePressY = 0;
		onlyCurrentPart = false;
		hitBoxPartName = new TextComponentTranslation("animation.hitbox.part.name").getFormattedText();

		npcAnim = Util.instance.copyToGUI(npc, mc.world, true);
		npcAnim.display.setName(npc.getName()+"_animation");
		baseRotation = npcAnim.rotationYaw;

		npcPart = Util.instance.copyToGUI(npc, mc.world, true);
		npcAnim.display.setName(npc.getName()+"_anim_part");

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

		ModelNpcAlt.editAnimDataSelect.red = (float) (CustomNpcs.colorAnimHoverPart >> 16 & 255) / 255.0F;
		ModelNpcAlt.editAnimDataSelect.green = (float) (CustomNpcs.colorAnimHoverPart >> 8 & 255) / 255.0F;
		ModelNpcAlt.editAnimDataSelect.blue = (float) (CustomNpcs.colorAnimHoverPart & 255) / 255.0F;
	}

	private void setPart(PartConfig partConfig) {
		GuiNpcTextField.unfocus();
		if (partConfig != null) { part = frame.parts.get(partConfig.id); }
		ModelNpcAlt.editAnimDataSelect.part = part == null ? null : part.getEnumType();
		if (this.tools != null && this.tools.visible) { this.showTools(); }
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
				if (anim == null || part == null || !anim.frames.containsKey(button.getValue()) || anim.frames.get(button.getValue()).id == -1) {
					return;
				}
				frame = anim.frames.get(button.getValue());
				this.setPart(frame.parts.get(part.id));
				this.initGui();
				break;
			}
			case 4: { // add frame
				if (anim == null) { return; }
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					SubGuiEditText subgui = new SubGuiEditText(0, "" + anim.frames.size());
					subgui.numbersOnly = new int[] { 0, anim.frames.size(), anim.frames.size() };
					this.setSubGui(subgui);
				} else {
					frame = (AnimationFrameConfig) anim.addFrame(-1, frame);
					this.setPart(frame.parts.get(part.id));
					this.initGui();
				}
				break;
			}
			case 5: { // del frame
				if (frame == null) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				this.displayGuiScreen(guiyesno);
				break;
			}
			case 6: { // clear frame
				if (frame == null) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.clearMessage").getFormattedText(),
						GuiScreen.isShiftKeyDown() ? 4 : 1);
				this.displayGuiScreen(guiyesno);
				break;
			}
			case 7: { // add part
				break;
			}
			case 8: { // del part
				if (part == null) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.part", "" + (part.id + 1),
								this.scrollParts.getSelected()).getFormattedText(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				this.displayGuiScreen(guiyesno);
				break;
			}
			case 9: { // clear part
				if (part == null) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.part", "" + (part.id + 1),
								this.scrollParts.getSelected()).getFormattedText(),
						new TextComponentTranslation("gui.clearMessage").getFormattedText(),
						GuiScreen.isShiftKeyDown() ? 5 : 3);
				this.displayGuiScreen(guiyesno);
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
				this.resetAnimation();
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
				this.resetAnimation();
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
				this.resetAnimation();
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
			case 26: { // show armor
				ModelNpcAlt.editAnimDataSelect.showArmor = !ModelNpcAlt.editAnimDataSelect.showArmor;
				button.layerColor = (ModelNpcAlt.editAnimDataSelect.showArmor ? 0xFFFF7200 : 0xFF6F3200);
				break;
			}
			case 27: { // select sound
				if (frame == null) { return; }
				this.setSubGui(new GuiSoundSelection(frame.getStartSound()));
				break;
			}
			case 28: { // remove sound
				if (frame == null) { return; }
				frame.setStartSound("");
				break;
			}
			case 29: { // show parts
				if (this.partNames == null) { this.showPartNames(); }
				if (this.mwindows.containsKey(this.partNames.id)) { return; }
				this.partNames.isMoving = false;
				this.partNames.visible = true;
				this.mwindows.put(this.partNames.id, this.partNames);
				button.enabled = false;
				break;
			}
			case 30: { // reset part set X
				if (this.isHitPox) {
					if (anim != null) {
						if (toolType == 1) { anim.offsetHitbox[0] = 0.0f; }
						else { anim.scaleHitbox[0] = 1.0f; }
						this.initGui();
					}
				} else if (part != null) {
					switch (toolType) {
						case 0: {
							part.rotation[0] = 0.5f;
							break;
						}
						case 1: {
							part.offset[0] = 0.5f;
							break;
						}
						case 2: {
							part.scale[0] = 0.2f;
							break;
						}
					}
					this.initGui();
				}
				break;
			}
			case 31: { // reset part set Y
				if (this.isHitPox) {
					if (anim != null) {
						if (toolType == 1) { anim.offsetHitbox[1] = 0.0f; }
						else { anim.scaleHitbox[1] = 1.0f; }
						this.initGui();
					}
				} else if (part != null) {
					switch (toolType) {
						case 0: {
							part.rotation[1] = 0.5f;
							break;
						}
						case 1: {
							part.offset[1] = 0.5f;
							break;
						}
						case 2: {
							part.scale[1] = 0.2f;
							break;
						}
					}
					this.initGui();
				}
				break;
			}
			case 32: { // reset part set Z
				if (this.isHitPox) {
					if (anim != null) {
						if (toolType == 1) { anim.offsetHitbox[2] = 0.0f; }
						else { anim.scaleHitbox[2] = 1.0f; }
						this.initGui();
					}
				} else if (part != null) {
					switch (toolType) {
						case 0: {
							part.rotation[2] = 0.5f;
							break;
						}
						case 1: {
							part.offset[2] = 0.5f;
							break;
						}
						case 2: {
							part.scale[2] = 0.2f;
							break;
						}
					}
					this.initGui();
				}
				break;
			}
			case 33: { // reset part set X1 rot
				part.rotation[3] = 0.5f;
				this.initGui();
				break;
			}
			case 34: { // reset part set Y1 rot
				part.rotation[4] = 0.5f;
				this.initGui();
				break;
			}
			case 35: { // show tools
				if (this.tools == null) { this.showTools(); }
				if (this.mwindows.containsKey(this.tools.id)) { return; }
				this.tools.isMoving = false;
				this.tools.visible = true;
				this.mwindows.put(this.tools.id, this.tools);
				button.enabled = false;
				break;
			}
			case 36: { // is Now Damage
				if (frame == null) { return; }
				frame.isNowDamage = ((GuiNpcCheckBox) button).isSelected();
				if (frame.isNowDamage) {
					for (AnimationFrameConfig f : anim.frames.values()) {
						if (f.id == frame.id) { continue; }
						f.isNowDamage = false;
					}
				} else {
					if (frame.id == 0) {
						if (anim.frames.containsKey(1)) { anim.frames.get(1).isNowDamage = true; }
						else {
							frame.isNowDamage = true;
							((GuiNpcCheckBox) button).setSelected(true);
						}
					}
					else { anim.frames.get(0).isNowDamage = true; }
				}
				((GuiNpcCheckBox) button).setText(frame.isNowDamage() ? "animation.now.attack" : "animation.notyet.attack");
				break;
			}
			case 37: { // right stack type
				if (frame == null) { return; }
				frame.setHoldRightStackType(button.getValue());
				this.resetAnimation();
				break;
			}
			case 38: { // left stack type
				if (frame == null) { return; }
				frame.setHoldLeftStackType(button.getValue());
				this.resetAnimation();
				break;
			}
			case 39: { // show Main Hand
				if (frame == null) { return; }
				frame.showMainHand = !frame.showMainHand;
				this.initGui();
				break;
			}
			case 40: { // show Offhand
				if (frame == null) { return; }
				frame.showOffHand = !frame.showOffHand;
				this.initGui();
				break;
			}
			case 41: { // show Helmet
				if (frame == null) { return; }
				frame.showHelmet = !frame.showHelmet;
				this.initGui();
				break;
			}
			case 42: { // show Body
				if (frame == null) { return; }
				frame.showBody = !frame.showBody;
				this.initGui();
				break;
			}
			case 43: { // show Legs
				if (frame == null) { return; }
				frame.showLegs = !frame.showLegs;
				this.initGui();
				break;
			}
			case 44: { // show Feet's
				if (frame == null) { return; }
				frame.showFeets = !frame.showFeets;
				this.initGui();
				break;
			}
			case 45: { // show Feet's
				if (anim == null) { return; }
				anim.setDamageHitboxType(button.getValue());
				this.initGui();
				break;
			}
			case 46: { // show alpha // show armor
				if (ModelNpcAlt.editAnimDataSelect.alpha >= 1.0f) {
					ModelNpcAlt.editAnimDataSelect.alpha = 0.25f;
					button.layerColor = 0xFF787758;
				} else {
					ModelNpcAlt.editAnimDataSelect.alpha = 1.0f;
					button.layerColor = 0xFFFFFEBF;
				}
				break;
			}
			case 47: {
				if (baseRotation == 0.0f) { return; }
				if (this.npcAnim.rotationYaw != baseRotation) {
					this.npcAnim.rotationYaw = baseRotation;
					this.npcAnim.prevRotationYaw = baseRotation;
					this.npcAnim.rotationYawHead = baseRotation;
					this.npcAnim.prevRotationYawHead = baseRotation;
					this.npcPart.rotationYaw = baseRotation;
					this.npcPart.prevRotationYaw = baseRotation;
					this.npcPart.rotationYawHead = baseRotation;
					this.npcPart.prevRotationYawHead = baseRotation;
					button.layerColor = 0xFF96FFC0;
				} else {
					this.npcAnim.rotationYaw = 0.0f;
					this.npcAnim.prevRotationYaw = 0.0f;
					this.npcAnim.rotationYawHead = 0.0f;
					this.npcAnim.prevRotationYawHead = 0.0f;
					this.npcPart.rotationYaw = 0.0f;
					this.npcPart.prevRotationYaw = 0.0f;
					this.npcPart.rotationYawHead = 0.0f;
					this.npcPart.prevRotationYawHead = 0.0f;
					button.layerColor = 0xFF426C53;
				}
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
				this.setPart(frame.parts.get(part.id));
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
				this.setPart(frame.parts.get(f));
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
		this.dispRot[1] += (float) (Math.cos(this.dispRot[0] * Math.PI / 180.0f) * (float) y);
		this.dispRot[2] += (float) (Math.sin(this.dispRot[0] * Math.PI / 180.0f) * (float) y);
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

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.w < 0 || this.h < 0) {
			ScaledResolution sw = new ScaledResolution(this.mc);
			this.w  = sw.getScaledWidth();
			this.h = sw.getScaledHeight();
		}
		if (waitKey != 0) { waitKey--; }
		for (int i = 0; i < 2; i++) {
			EntityNPCInterface dNpc = (i == 0 ? this.npcAnim : this.npcPart);
			if (dNpc == null) { continue; }
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
		if (this.getDisplayNpc() == null) { this.close(); }
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
		// Slots
		int y = 61 + (hasExtend ? 14 : 0);
		GlStateManager.pushMatrix();
		this.mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
		GlStateManager.translate(winU + 2, winV + y, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawTexturedModalRect(0, 0, 0, 0, 18, 18); // right
		IItemStack stack;
		switch(frame.getHoldRightStackType()) {
			case 1: stack = this.npc.inventory.getProjectile(); break;
			case 2: stack = this.npc.inventory.getLeftHand(); break;
			case 3: stack = frame.getHoldRightStack(); break;
			default: stack = this.npc.inventory.getRightHand(); break;
		}
		this.hoverRight = this.isMouseHover(mouseX, mouseY, winU + 3, winV + y + 1, 16, 16);
		if (this.hoverRight) {
			Gui.drawRect(1, 1, 17, 17, 0x80FFFFFF);
			if (stack != null && !stack.isEmpty()) {
				List<String> list = stack.getMCItemStack().getTooltip(this.player, this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
				if (!list.isEmpty()) { this.hoverText = list.toArray(new String[0]); }
			}
		}
		if (stack != null && !stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(1.0f, 1.0f, 0.0f);
			RenderHelper.enableStandardItemLighting();
			this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack.getMCItemStack(), 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		y += 20;
		this.mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
		GlStateManager.translate(0.0f, 20.0f, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawTexturedModalRect(0, 0, 0, 0, 18, 18); // left
		switch(frame.getHoldLeftStackType()) {
			case 1: stack = this.npc.inventory.getProjectile(); break;
			case 2: stack = this.npc.inventory.getRightHand(); break;
			case 3: stack = frame.getHoldLeftStack(); break;
			default: stack = this.npc.inventory.getLeftHand(); break;
		}
		this.hoverLeft = this.isMouseHover(mouseX, mouseY, winU + 3, winV + y + 1, 16, 16);
		if (this.hoverLeft) {
			Gui.drawRect(1, 1, 17, 17, 0x80FFFFFF);
			if (stack != null && !stack.isEmpty()) {
				List<String> list = stack.getMCItemStack().getTooltip(this.player, this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
				if (!list.isEmpty()) { this.hoverText = list.toArray(new String[0]); }
			}
		}
		if (stack != null && !stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(1.0f, 1.0f, 0.0f);
			RenderHelper.enableStandardItemLighting();
			this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack.getMCItemStack(), 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
		// Lines
		for (int i = 0; i < 17; i++) { // work -> frame
			this.drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + 23, 0xFF000000);
		}
		y += 19;
		for (int i = 0; i < 17; i++) { // frame -> part
			this.drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 38;
		for (int i = 0; i < 17; i++) { // part -> chance
			this.drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 17;
		for (int i = 0; i < 17; i++) { // chance -> sound
			this.drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 26;
		for (int i = 0; i < 17; i++) { // sound -> emotion
			this.drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 26;
		for (int i = 0; i < 17; i++) { // emotion -> equipment
			this.drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 25;
		for (int i = 0; i < 17; i++) { // equipment -> end
			this.drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		GlStateManager.popMatrix();
		if (this.subgui == null) {
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = this.w < this.mc.displayWidth ? (int) Math.round((double) this.mc.displayWidth / this.w) : 1;
			GL11.glScissor((workU + 1) * c, this.mc.displayHeight - (workV + workS - 1) * c, (workS - 2) * c, (workS - 2) * c);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawWork(partialTicks);
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

		if (this.hasSubGui() || !CustomNpcs.ShowDescriptions) { return; }
		if (this.hoverMiniWin) {
			if (this.hoverText != null) {
				this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
				this.hoverText = null;
			}
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
			this.setHoverText(new TextComponentTranslation("animation.hover.frame", "" + (frame.id + 1)).getFormattedText());
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
			this.setHoverText(new TextComponentTranslation("animation.hover.mesh.0").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(14) != null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.mesh.1").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(15) != null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.mesh.2").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(16) != null && this.getButton(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.mesh.3").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(18) != null && this.getButton(17).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.hitbox").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (this.getButton(18) != null && this.getButton(18).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.scale").getFormattedText());
		} else if (this.getButton(19) != null && this.getButton(19).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.pos").getFormattedText());
		} else if (this.getButton(20) != null && this.getButton(20).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset.rot").getFormattedText());
		} else if (this.getButton(21) != null && this.getButton(21).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.work." + this.onlyCurrentPart, ((char) 167) + "6" + (frame != null ? frame.id + 1 : -1)).getFormattedText());
		} else if (this.getButton(22) != null && this.getButton(22).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.part.show." + part.isShow()).appendSibling(new TextComponentTranslation("animation.hover.shift.1")).getFormattedText());
		} else if (this.getButton(23) != null && this.getButton(23).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.0").getFormattedText());
		} else if (this.getButton(24) != null && this.getButton(24).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.1").getFormattedText());
		} else if (this.getButton(25) != null && this.getButton(25).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.tool.2").getFormattedText());
		} else if (this.getButton(26) != null && this.getButton(26).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.show.armor").getFormattedText());
		} else if (this.getButton(27) != null && this.getButton(27).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.select.sound").getFormattedText());
		} else if (this.getButton(28) != null && this.getButton(28).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.del.sound").getFormattedText());
		} else if (this.getButton(29) != null && this.getButton(29).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.show.parts").getFormattedText());
		} else if (this.getButton(30) != null && this.getButton(30).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset." + toolType, "X").getFormattedText());
		} else if (this.getButton(31) != null && this.getButton(31).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset." + toolType, "Y").getFormattedText());
		} else if (this.getButton(32) != null && this.getButton(32).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.reset." + toolType, "Z").getFormattedText());
		} else if (this.getButton(35) != null && this.getButton(35).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.show.tools").getFormattedText());
		} else if (this.getButton(36) != null && this.getButton(36).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.now.attack").getFormattedText());
		} else if (this.getButton(37) != null && this.getButton(37).isMouseOver()) {
			int type = frame.getHoldRightStackType();
			if (type == 2) { type = 3; }
			else if (type == 3) { type = 4; }
			this.setHoverText(new TextComponentTranslation("animation.hover.stack.type."+type).getFormattedText());
		} else if (this.getButton(38) != null && this.getButton(38).isMouseOver()) {
			int type = frame.getHoldLeftStackType();
			if (type == 3) { type = 4; }
			this.setHoverText(new TextComponentTranslation("animation.hover.stack.type."+type).getFormattedText());
		} else if (this.getButton(39) != null && this.getButton(39).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.0").getFormattedText());
		} else if (this.getButton(40) != null && this.getButton(40).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.1").getFormattedText());
		} else if (this.getButton(41) != null && this.getButton(41).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.2").getFormattedText());
		} else if (this.getButton(42) != null && this.getButton(42).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.3").getFormattedText());
		} else if (this.getButton(43) != null && this.getButton(43).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.4").getFormattedText());
		} else if (this.getButton(44) != null && this.getButton(44).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.5").getFormattedText());
		} else if (this.getButton(45) != null && this.getButton(45).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.hitbox.type."+anim.getDamageHitboxType()).getFormattedText());
		} else if (this.getButton(46) != null && this.getButton(46).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.show.alpha").getFormattedText());
		} else if (this.getButton(47) != null && this.getButton(47).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.base.rot").getFormattedText());
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

	@Override
	public void setMiniHoverText(int id, IComponentGui c) {
		if (id < 0) { return; }
		if (c instanceof GuiNpcSlider) {
			switch(((GuiNpcSlider) c).id) {
				case 0: {
					if (this.isHitPox) { this.setHoverText(new TextComponentTranslation("animation.hover." + (this.toolType == 1 ? "offset.d" : "scale.hb"), "X").getFormattedText()); }
					else { this.setHoverText(new TextComponentTranslation("animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"), "X").getFormattedText()); }
					break;
				}
				case 1: {
					if (this.isHitPox) { this.setHoverText(new TextComponentTranslation("animation.hover." + (this.toolType == 1 ? "offset.h" : "scale.hb"), "Y").getFormattedText()); }
					else { this.setHoverText(new TextComponentTranslation("animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"), "Y").getFormattedText()); }
					break;
				}
				case 2: {
					if (this.isHitPox) { this.setHoverText(new TextComponentTranslation("animation.hover." + (this.toolType == 1 ? "offset.w" : "scale.hb"), "Z").getFormattedText()); }
					else { this.setHoverText(new TextComponentTranslation("animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"), "Z").getFormattedText()); }
					break; }
				case 3: { this.setHoverText(new TextComponentTranslation("animation.hover.rotation.x1").getFormattedText()); break; }
				case 4: { this.setHoverText(new TextComponentTranslation("animation.hover.rotation.y1").getFormattedText()); break; }
			}
		} else if (c instanceof GuiNpcTextField) {
			ITextComponent text = null;
			switch(((GuiNpcTextField) c).getId()) {
				case 5: {
					if (this.isHitPox) { text = new TextComponentTranslation("animation.hover." + (this.toolType == 1 ? "offset.d" : "scale.hb"), "X"); }
					else { text = new TextComponentTranslation("animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"), "X"); }
					break;
				}
				case 6: {
					if (this.isHitPox) { text = new TextComponentTranslation("animation.hover." + (this.toolType == 1 ? "offset.h" : "scale.hb"), "Y"); }
					else { text = new TextComponentTranslation("animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"), "Y"); }
					break;
				}
				case 7: {
					if (this.isHitPox) { text = new TextComponentTranslation("animation.hover." + (this.toolType == 1 ? "offset.w" : "scale.hb"), "Z"); }
					else { text = new TextComponentTranslation("animation.hover." + (this.toolType == 0 ? "rotation" : this.toolType == 1 ? "offset" : "scale"), "Z"); }
					break; }
				case 8: { text = new TextComponentTranslation("animation.hover.rotation.x1"); break; }
				case 9: { text = new TextComponentTranslation("animation.hover.rotation.y1"); break; }
			}
			if (text != null) {
				text.appendSibling(new TextComponentTranslation("animation.hover.tab"));
				this.setHoverText(text.getFormattedText());
			}
		} else if (c instanceof GuiNpcButton) {
			this.setHoverText(new TextComponentTranslation("hover.default.set").getFormattedText());
		}
	}

	@SuppressWarnings("unchecked")
	private void drawWork(float partialTicks) {
		// work place
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, -300.0f);
		Gui.drawRect(workU + 1, workV + 1, workU + workS - 1, workV + workS - 1, GuiNpcAnimation.backColor);
		this.drawCenteredString(this.fontRenderer, ((char) 167) + "7" + anim.getSettingName(), workU + workS / 2, workV + 3, 0xFFFFFFFF);
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
		this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
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
			for (int x = -blockSize; x <= blockSize; x++) {
				for (int z = -blockSize; z <= blockSize; z++) {
					BlockPos pos = new BlockPos(x, y + (blockSize == 0 ? 0 : 1), z);
					TileEntity tile = null;
					if (blockType == 0) {
						IBlockState s = this.environmentStates.get(pos);
						if (s != null) {
							state = s;
						}
						TileEntity t = this.environmentTiles.get(pos);
						if (t != null) {
							tile = t;
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
					GlStateManager.translate(x, y - (blockSize == 0 || blockType > 1 ? 1 : 0), z);
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
				render.render(tile, p.getX(), p.getY() - 1.0D, p.getZ() - 1, partialTicks, 0, 1.0f);
				GlStateManager.popMatrix();
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

		// npc
		GlStateManager.enableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		GlStateManager.enableBlend();
		GlStateManager.enableColorMaterial();
		GlStateManager.translate(0.5f, 0.0f, -0.5f);
		this.mc.getRenderManager().playerViewY = 180.0f;
		EntityNPCInterface showNPC = getDisplayNpc();
		if (showHitBox) {
			GlStateManager.glLineWidth(1.0F);
			GlStateManager.disableTexture2D();
			RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(showNPC.width / -2.0d, 0.0d, showNPC.width / -2.0d, showNPC.width / 2.0d, showNPC.height, showNPC.width / 2.0d), 1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableTexture2D();
		}
		if (anim.type == AnimationKind.ATTACKING && anim.getDamageHitboxType() > 0) {
			AxisAlignedBB aabb;
			if (anim.getDamageHitboxType() == 1) {
				aabb = new AxisAlignedBB(-showNPC.width, 0.0d, -showNPC.width, showNPC.width, showNPC.height, showNPC.width);
				IRayTraceVec data = Util.instance.getPosition(showNPC.posX, showNPC.posY, showNPC.posZ, showNPC.rotationYaw, 0.0d, 1.0d);
				aabb = aabb.offset((float) (data.getX() - showNPC.posX), (float) (data.getY() - showNPC.posY), (float) (data.getZ() - showNPC.posZ));
			}
			else { aabb = this.anim.getDamageHitbox(showNPC); }
			GlStateManager.glLineWidth(2.0F);
			GlStateManager.disableTexture2D();
			RenderGlobal.drawSelectionBoundingBox(aabb, 0.878431f, 0.0f, 0.25098f, 1.0f);
			GlStateManager.enableTexture2D();
		}

		ModelNpcAlt.editAnimDataSelect.displayNpc = showNPC;
		this.mc.getRenderManager().renderEntity(showNPC, 0.0, 0.0, 0.0, 0.0f, showNPC.rotationYaw != 0.0f ? 1.0f : 0.0f, false);


		if (blockType == 0) {
			for (Entity e : environmentEntitys) {
				int x = Math.abs((int) Math.round(e.posX));
				int y = Math.abs((int) Math.round(e.posY));
				int z = Math.abs((int) Math.round(e.posZ));
				int d = x;
				if (d < y) { d = y; }
				if (d < z) { d = z; }
				if (d > blockSize) { continue; }
				GlStateManager.pushMatrix();
				this.mc.getRenderManager().renderEntity(e, e.posX, e.posY, e.posZ, 0.0f, 0.0f, false);
				GlStateManager.popMatrix();
			}
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();

	}

	public EntityNPCInterface getDisplayNpc() {
		return this.onlyCurrentPart ? this.npcPart : this.npcAnim;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (frame.id < 0) {
			frame = anim.frames.get(0);
			if (frame.id < 0) { frame.id = 0; }
		}
		ScaledResolution sw = new ScaledResolution(this.mc);
		if (this.w > 0.0d && this.h > 0.0d) {
			if (this.partNames != null) {
				double left = 1.0d / this.w * this.partNames.guiLeft;
				double top = 1.0d / this.h * this.partNames.guiTop;
				this.partNames.guiLeft = (int) ((double) sw.getScaledWidth() * left);
				this.partNames.guiTop = (int) ((double) sw.getScaledHeight() * top);
				this.scrollParts.guiLeft = this.partNames.guiLeft + 4;
				this.scrollParts.guiTop = this.partNames.guiTop + 12;
			}
			if (this.tools != null) {
				double left = 1.0d / this.w * this.tools.guiLeft;
				double top = 1.0d / this.h * this.tools.guiTop;
				this.tools.guiLeft = (int) ((double) sw.getScaledWidth() * left);
				this.tools.guiTop = (int) ((double) sw.getScaledHeight() * top);
			}
		}
		this.w  = sw.getScaledWidth();
		this.h = sw.getScaledHeight();

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
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(2, workU + 2, workV + 23, 8, 8, "");
		button.layerColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(26, workU + 2, workV + 31, 8, 8, "");
		button.layerColor = (ModelNpcAlt.editAnimDataSelect.showArmor ? 0xFFFF7200 : 0xFF6F3200);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(46, workU + 2, workV + 39, 8, 8, "");
		button.layerColor = (ModelNpcAlt.editAnimDataSelect.alpha >= 1.0f ? 0xFFFFFEBF : 0xFF787758);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(47, workU + 2, workV + 47, 8, 8, "");
		button.layerColor = (this.baseRotation == 0.0f || this.baseRotation == this.npcAnim.rotationYaw ? 0xFF96FFC0 : 0xFF426C53);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);

		// Frame
		GuiNpcLabel label = new GuiNpcLabel(lId++, "animation.frames", x, (y += 24) - 10);
		this.addLabel(label);
		List<String> lFrames = Lists.newArrayList();
		for (int i = 0; i < anim.frames.size(); i++) { lFrames.add((i + 1) + "/" + anim.frames.size()); }
		this.addButton(new GuiButtonBiDirectional(3, x, y, 60, 10, lFrames.toArray(new String[0]), frame.id));
		button = new GuiNpcButton(4, x + label.width + 2, y - 10, 10, 10, ""); // add frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(5, x + label.width + 12, y - 10, 10, 10, ""); // del frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = anim.frames.size() > 1;
		this.addButton(button);
		button = new GuiNpcCheckBox(11, x + 62, y - 2, 74, 12, frame.isSmooth() ? "gui.smooth" : "gui.linearly");
		((GuiNpcCheckBox) button).setSelected(frame.isSmooth());
		this.addButton(button);
		button = new GuiNpcButton(6, x + 126, y - 10, 10, 10, ""); // clear frame
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.time").getFormattedText() + ":", x, (y += 12) + 2));
		textField = new GuiNpcTextField(1, this, x + 35, y, 48, 12, "" + frame.getSpeed());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 3600, frame.getSpeed());
		this.addTextField(textField);
		textField = new GuiNpcTextField(2, this, x + 87, y, 48, 12, "" + frame.getEndDelay());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 3600, frame.getEndDelay());
		this.addTextField(textField);
		this.hasExtend = false;
		if (anim.type == AnimationKind.DIES || anim.type == AnimationKind.JUMP) {
			this.hasExtend = true;
			this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.repeat").getFormattedText() + ":", x, (y += 14) + 2));
			if (anim.repeatLast < 0) { anim.repeatLast *= -1; }
			if (anim.repeatLast > anim.frames.size()) { anim.repeatLast = anim.frames.size(); }
			textField = new GuiNpcTextField(0, this, x + 87, y, 48, 12, "" + anim.repeatLast);
			textField.setNumbersOnly();
			textField.setMinMaxDefault(0, anim.frames.size(), anim.repeatLast);
			this.addTextField(textField);
		}
		if (anim.type == AnimationKind.ATTACKING) {
			this.hasExtend = true;
			button = new GuiNpcCheckBox(36, x, y += 14, 136, 12, frame.isNowDamage() ? "animation.now.attack" : "animation.notyet.attack");
			((GuiNpcCheckBox) button).setSelected(frame.isNowDamage());
			this.addButton(button);
		}
		this.addLabel(new GuiNpcLabel(lId++, "animation.hold.right", x + 20, y += 13));
		button = new GuiNpcButton(37, x + 20, y += 9, 116, 10, new String[] { "animation.stack.type.0", "animation.stack.type.1", "animation.stack.type.3", "animation.stack.type.4", "animation.stack.type.5", "animation.stack.type.6", "animation.stack.type.7", "animation.stack.type.8" }, frame.getHoldRightStackType());
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		this.addLabel(new GuiNpcLabel(lId++, "animation.hold.left", x + 20, y += 10));
		button = new GuiNpcButton(38, x + 20, y += 9, 116, 10, new String[] { "animation.stack.type.0", "animation.stack.type.1", "animation.stack.type.2", "animation.stack.type.4", "animation.stack.type.5", "animation.stack.type.6", "animation.stack.type.7", "animation.stack.type.8" }, frame.getHoldLeftStackType());
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);

		// Part
		label = new GuiNpcLabel(lId++, "animation.parts", x, y += 13);
		this.addLabel(label);
		button = new GuiNpcButton(29, workU + 2, y, 8, 8, ""); // show part names
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 232;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = this.partNames == null || !this.partNames.visible;
		button.layerColor = CustomNpcs.colorAnimHoverPart + 0xFF000000;
		this.addButton(button);
		if (this.scrollParts == null) { (this.scrollParts = new GuiCustomScroll(this, 0)).setSize(67, 112); }
		dataParts.clear();
		List<String> lParts = Lists.newArrayList();
		for (int id : frame.parts.keySet()) {
			PartConfig ps = frame.parts.get(id);
			String key = new TextComponentTranslation(ps.name).getFormattedText();
			dataParts.put(key, ps);
			lParts.add(key);
		}
		if (anim.type == AnimationKind.ATTACKING) {
			lParts.add(this.hitBoxPartName);
		}
		this.scrollParts.setListNotSorted(lParts);
		this.isHitPox = this.scrollParts.hasSelected() && this.scrollParts.getSelected().equals(this.hitBoxPartName);
		if (this.isHitPox && this.toolType == 0) { this.toolType = 1; }
		button = new GuiNpcButton(7, x + label.width + 2, y, 10, 10, ""); // add part
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 96;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = this.part != null;
		this.addButton(button);
		button = new GuiNpcButton(8, x + label.width + 12, y, 10, 10, ""); // del part
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = this.part != null && this.part.id > 11;
		this.addButton(button);
		button = new GuiNpcButton(9, x + 126, y, 10, 10, ""); // clear part
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcCheckBox(10, x, y += 10, 67, 14, part.isDisable() ? "gui.disabled" : "gui.enabled");
		((GuiNpcCheckBox) button).setSelected(!part.isDisable());
		this.addButton(button);
		button = new GuiNpcCheckBox(22, x + 69, y, 67, 14, part.isShow() ? "gui.show" : "gui.noshow");
		((GuiNpcCheckBox) button).setSelected(part.isShow());
		this.addButton(button);
		StringBuilder color = new StringBuilder(Integer.toHexString(CustomNpcs.colorAnimHoverPart));
		while (color.length() < 6) { color.insert(0, "0"); }
		button = new GuiNpcButton(12, x, y += 15, 67, 10, color.toString()); // color hover
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		button.setTextColor(CustomNpcs.colorAnimHoverPart);
		button.dropShadow = false;
		this.addButton(button);

		// Chance
		float ch = Math.round(anim.chance * 100000.0f) / 1000.0f;
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("drop.chance").getFormattedText()+":", x, (y += 14) + 1));
		textField = new GuiNpcTextField(10, this, x + 28, y, 40, 12, String.valueOf(ch));
		textField.setDoubleNumbersOnly();
		textField.setMinMaxDoubleDefault(0.0f, 100.0f, ch);
		this.addTextField(textField);
		this.addLabel(new GuiNpcLabel(lId++, "%", x + 72, y + 1));

		// Sound Settings
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("advanced.sounds").getFormattedText()+":", x, y += 16));
		textField = new GuiNpcTextField(3, this, x, y + 10, 135, 12, frame.getStartSound());
		this.addTextField(textField);
		button = new GuiNpcButton(27, x + textField.width - 17, y, 8, 8, "S");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		button.dropShadow = false;
		button.setTextColor(0xFFDC0000);
		this.addButton(button);
		button = new GuiNpcButton(28, x + textField.width - 8, y, 8, 8, "X");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		button.dropShadow = false;
		button.setTextColor(0xFFDC0000);
		this.addButton(button);

		// Emotion data
		this.addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("advanced.emotion").getFormattedText()+":", x, y += 26));
		textField = new GuiNpcTextField(4, this, x, y + 10, 48, 12, "" + frame.getStartEmotion());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, AnimationController.getInstance().getUnusedEmtnId() - 1, frame.getStartEmotion());
		this.addTextField(textField);

		// Equipment
		this.addLabel(new GuiNpcLabel(lId++, "animation.show.stacks", x, y += 26));
		button = new GuiNpcButton(39, x + 2, y += 10, 12, 12, ""); // main hand
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrY = frame.showMainHand ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(40, x + 16, y, 12, 12, ""); // offhand
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 24;
		button.txrY = frame.showOffHand ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(41, x + 30, y, 12, 12, ""); // helmet
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 48;
		button.txrY = frame.showHelmet ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(42, x + 44, y, 12, 12, ""); // body
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 72;
		button.txrY = frame.showBody ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(43, x + 58, y, 12, 12, ""); // legs
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 96;
		button.txrY = frame.showLegs ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);
		button = new GuiNpcButton(44, x + 72, y, 12, 12, ""); // feet's
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 120;
		button.txrY = frame.showFeets ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		this.addButton(button);

		if (anim.type == AnimationKind.ATTACKING) {
			this.addLabel(new GuiNpcLabel(lId, "animation.hitbox.type", x, y += 15));
			button = new GuiNpcButton(45, x, y + 9, 136, 10, new String[] { "animation.hitbox.0", "animation.hitbox.1", "animation.hitbox.2" }, anim.getDamageHitboxType());
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			this.addButton(button);
		}

		// Exit
		button = new GuiNpcButton(66, x, winV + winH - 12, 50, 10, "gui.back"); // back
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);

		// work
		button = new GuiNpcButton(13, workU + 25, workV + 2, 8, 8, ""); // simple mesh
		button.layerColor = meshType == 0 ? 0xFFD93070 : 0xFF360C1C;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(14, workU + 34, workV + 2, 8, 8, ""); // xz mesh
		button.layerColor = meshType == 1 ? 0xFF6830D9 : 0xFF1A0C36;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(15, workU + 43, workV + 2, 8, 8, ""); // xY mesh
		button.layerColor = meshType == 2 ? 0xFF30D980 : 0xFF0C3620;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(16, workU + 52, workV + 2, 8, 8, ""); // zy mesh
		button.layerColor = meshType == 3 ? 0xFFD7D930 : 0xFF35360C;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(17, workU + 61, workV + 2, 8, 8, ""); // show hit box
		button.layerColor = showHitBox ? 0 : 0xFF808080;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);

		button = new GuiNpcButton(18, workU + workS - 10, workV + 2, 8, 8, ""); // reset scale
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(19, workU + 2, workV + workS - 10, 8, 8, ""); // reset pos
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(20, workU + workS - 10, workV + workS - 10, 8, 8, ""); // reset rot
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		this.addButton(button);
		button = new GuiNpcButton(21, workU + workS / 2 - 11, workV + workS - 12, 18, 10, ""); // part or anim
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = onlyCurrentPart ? 144 : 188;
		button.txrW = 44;
		button.txrH = 24;
		this.addButton(button);

		y = workV + workS - 74;

		button = new GuiNpcButton(35, workU + 5, y, 8, 8, ""); // show tools
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 232;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = this.tools == null || !this.tools.visible;
		this.addButton(button);
		button = new GuiNpcButton(23, workU + 2, y += 10, 14, 14, ""); // tool pos
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 1 ? 0xFFFF8080 : 0xFFFFFFFF;
		button.enabled = !this.isHitPox || anim.getDamageHitboxType() == 2;
		this.addButton(button);
		button = new GuiNpcButton(24, workU + 2, y += 16, 14, 14, ""); // tool rot
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 24;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 0 ? 0xFF80FF80 : 0xFFFFFFFF;
		button.enabled = !this.isHitPox;
		this.addButton(button);
		button = new GuiNpcButton(25, workU + 2, y + 16, 14, 14, ""); // tool scale
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 48;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 2 ? 0xFF8080FF : 0xFFFFFFFF;
		button.enabled = !this.isHitPox || anim.getDamageHitboxType() == 2;
		this.addButton(button);

		this.resetAnimation();

		// Parts window
		boolean vPN = this.partNames == null || this.partNames.visible;
		this.showPartNames();
		this.partNames.visible = vPN;
		this.partNames.objs = new Object[] { part, toolType };
		// Tool window
		boolean vT = this.tools == null || this.tools.visible;
		this.showTools();
		this.tools.visible = vT;
	}

	private void showTools() {
		int f = 11, h = 0;
		int x = workU + 18;
		int y = workV + workS - 75;
		GuiNpcTextField textField;
		GuiNpcButton button;
		boolean notNormal = this.toolType == 0 && this.part != null && (
				(this.part instanceof AddedPartConfig && !((AddedPartConfig) this.part).isNormal) ||
						(this.part.id >= 1 && this.part.id <= 5));
		if (this.isHitPox) { notNormal = false; }
		y += notNormal ? -11 : 0;
		if (this.tools != null) {
			x = this.tools.guiLeft;
			y = this.tools.guiTop;
			h = this.tools.ySize;
		}
		this.tools = new GuiNpcMiniWindow(this, 1, x, y, 146, notNormal ? 60 : 38, new TextComponentTranslation("gui.tools").getFormattedText() + ":");
		this.tools.widthTexture = 256;
		this.tools.heightTexture = 256;

		x += 4;
		y += 13;
		for (int i = 0; i < 3; i++) {
			this.tools.addLabel(new GuiNpcLabel(i, i == 0 ? (this.isHitPox ? "D:" : "X:") : i == 1 ? (this.isHitPox ? "H:" : "Y:") : (this.isHitPox ? "W:" : "Z:"), x, y + i * f));
			float[] values;
			if (this.isHitPox) {
				if (toolType == 1) { values = new float[] { 0.0f + anim.offsetHitbox[0], 0.0f + anim.offsetHitbox[1], 0.0f + anim.offsetHitbox[2] }; }
				else { values = new float[] { 0.0f + anim.scaleHitbox[0], 0.0f + anim.scaleHitbox[1], 0.0f + anim.scaleHitbox[2] }; }
			} else {
				values = toolType == 0 ? part.rotation : toolType == 1 ? part.offset : part.scale;
			}
			float[] datas = new float[values.length];
			switch (toolType) {
				case 1: {
					for (int j = 0; j < values.length; j++) {
						if (this.isHitPox) { datas[j] = 0.0f + values[i]; }
						else { datas[j] = (float) (Math.round((10.0f * values[i] - 5.0f) * 1000.0f) / 1000.0d); }
					}
					if (this.isHitPox) {
						values[i] = (float) (Math.round((0.1f * values[i] + 0.5f) * 1000.0f) / 1000.0d);
						if (values[i] < 0.0f) { values[i] = 0.0f; }
						else if (values[i] > 1.0f) { values[i] = 1.0f; }
					}
					break;
				}
				case 2: {
					for (int j = 0; j < values.length; j++) {
						if (this.isHitPox) { datas[j] = 0.0f + values[j]; }
						else { datas[j] = (float) (Math.round(5000.0f * values[i]) / 1000.0d); }
					}
					if (this.isHitPox) {
						values[i] = (float) (Math.round(values[i] * 500.0f) / 1000.0d);
						if (values[i] < 0.0f) { values[i] = 0.0f; }
						else if (values[i] > 1.0f) { values[i] = 1.0f; }
					}
					break;
				}
				default: {
					for (int j = 0; j < values.length; j++) {
						datas[j] = (float) (Math.round(3600.0f * values[i]) / 10.0d);
					}
					break;
				}
			}
			this.tools.addSlider(new GuiNpcSlider(this.tools, i, x + 9, y + i * f, 75, 8, values[i]));
			textField = new GuiNpcTextField(i + 5, this.tools, x + 86, y + i * f, 42, 8, "" + datas[i]);
			textField.setDoubleNumbersOnly();
			double m = 0.0d, n = 360.0d;
			if (toolType == 1) {
				m = -5.0d;
				n = 5.0d;
			} else if (toolType == 2) {
				m = 0.0d;
				n = 5.0d;
			}
            textField.setMinMaxDoubleDefault(m, n, datas[i]);
            this.tools.addTextField(textField);
			button = new GuiNpcButton(30 + i, x + 130, y + i * f, 8, 8, "X");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			this.tools.addButton(button);
		}
		if (!this.isHitPox && notNormal) {
			y += 33;
			this.tools.addLabel(new GuiNpcLabel(3, "X1:", x, y));
			this.tools.addSlider(new GuiNpcSlider(this.tools, 3, x + 9, y , 75, 8, part.rotation[3]));
			double value = Math.round(3600.0d * part.rotation[3]) / 10.0d;
			textField = new GuiNpcTextField(8, this.tools, x + 86, y, 42, 8, "" + (float) value);
			textField.setDoubleNumbersOnly();
			textField.setMinMaxDoubleDefault(0.0d, 360.0d, value);
			this.tools.addTextField(textField);
			button = new GuiNpcButton(33, x + 130, y, 8, 8, "X");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			this.tools.addButton(button);

			y += 11;
			this.tools.addLabel(new GuiNpcLabel(4, "Y1:", x, y));
			this.tools.addSlider(new GuiNpcSlider(this.tools, 4, x + 9, y , 75, 8, part.rotation[4]));
			value = Math.round(1800.0d * part.rotation[4] + 900.0d) / 10.0d;
			textField = new GuiNpcTextField(9, this.tools, x + 86, y, 42, 8, "" + (float) value);
			textField.setDoubleNumbersOnly();
			textField.setMinMaxDoubleDefault(90.0d, 270.0d, value);
			this.tools.addTextField(textField);
			button = new GuiNpcButton(34, x + 130, y, 8, 8, "X");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			this.tools.addButton(button);
			if (h != 72) { this.tools.moveOffset(0, -11); }
		}
		else if (h != 50) { this.tools.moveOffset(0, 11); }
		switch(this.toolType) {
			case 1: {
				this.tools.setPoint(this.getButton(23));
				this.tools.setColorLine(0xFF8080);
				break;
			}
			case 2: {
				this.tools.setPoint(this.getButton(25));
				this.tools.setColorLine(0x8080FF);
				break;
			}
			default: {
				this.tools.setPoint(this.getButton(24));
				this.tools.setColorLine(0x80FF80);
				break;
			}
		}
		if (this.getButton(35) != null) { this.getButton(35).layerColor = this.tools.getColorLine() + 0xFF000000; }
		this.addMiniWindow(this.tools);
	}

	private void showPartNames() {
		if (this.partNames == null) {
			this.partNames = new GuiNpcMiniWindow(this, 0, workU + workS - 78, workV + 12, 75, 118, new TextComponentTranslation("gui.parts").getFormattedText() + ":");
			this.partNames.widthTexture = 256;
			this.partNames.heightTexture = 256;
			this.partNames.setPoint(this.getButton(29));
			this.partNames.setColorLine(CustomNpcs.colorAnimHoverPart);

			this.partNames.addScroll(this.scrollParts);
			this.scrollParts.guiLeft = this.partNames.guiLeft + 4;
			this.scrollParts.guiTop = this.partNames.guiTop + 12;
			this.scrollParts.selected = this.part.id;
		}
		this.addMiniWindow(this.partNames);
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
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (this.hoverMiniWin) {
			return;
		}
		if (this.hoverRight && frame.getHoldRightStackType() == 3) {
			IItemStack stack;
			switch(frame.getHoldRightStackType()) {
				case 1: stack = this.npc.inventory.getProjectile(); break;
				case 2: stack = this.npc.inventory.getLeftHand(); break;
				case 3: stack = frame.getHoldRightStack(); break;
				default: stack = this.npc.inventory.getRightHand(); break;
			}
			this.setSubGui(new SubGuiSelectItemStack(0, stack==null || stack.isEmpty() ? ItemStack.EMPTY : stack.getMCItemStack()));
		}
		else if (this.hoverLeft && frame.getHoldLeftStackType() == 3) {
			IItemStack stack;
			switch(frame.getHoldLeftStackType()) {
				case 1: stack = this.npc.inventory.getProjectile(); break;
				case 2: stack = this.npc.inventory.getRightHand(); break;
				case 3: stack = frame.getHoldLeftStack(); break;
				default: stack = this.npc.inventory.getLeftHand(); break;
			}
			this.setSubGui(new SubGuiSelectItemStack(1, stack==null || stack.isEmpty() ? ItemStack.EMPTY : stack.getMCItemStack()));
		}
		else if ((mouseBottom == 0 || mouseBottom == 1) && hovered) {
			mousePressId = mouseBottom;
			mousePressX = mouseX;
			mousePressY = mouseY;
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		float value = 0.0f;
		if (this.isHitPox) {
			if (anim == null || toolType == 0) { return; }
			if (toolType == 1) {
				value = Math.round((10.0f * slider.sliderValue - 5.0f) * 1000.0f) / 1000.0f;
				anim.offsetHitbox[slider.id] = value;
			}
			else {
				value = Math.round(5000.0f * slider.sliderValue) / 1000.0f;
				anim.scaleHitbox[slider.id] = value;
			}
			if (this.tools.getTextField(5 + slider.id) != null) { this.tools.getTextField(5 + slider.id).setText("" + value); }
			return;
		}
		if (part == null) { return; }
		if (slider.id == 3 || slider.id == 4) {
			if (toolType != 0) { return; }
			part.rotation[slider.id] = slider.sliderValue;
			if (slider.id == 3) { value = Math.round(360000.0f * slider.sliderValue) / 1000.0f; }
			else { value = Math.round(180000.0f * slider.sliderValue + 90000.0f) / 1000.0f; }
		} else {
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
		}
		if (this.tools.getTextField(5 + slider.id) != null) { this.tools.getTextField(5 + slider.id).setText("" + value); }
		this.resetAnimation();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

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

	private void resetAnimation() {
		if (anim == null || frame == null || npcAnim == null) {
			return;
		}
		AnimationConfig ac = this.anim.copy();

		this.npcAnim.animation.clear();
		this.npcAnim.animation.setAnimation(ac, AnimationKind.EDITING);
		this.npcAnim.setHealth(this.npcAnim.getMaxHealth());
		this.npcAnim.deathTime = 0;

		ac = this.anim.copy();
		ac.frames.clear();
		ac.frames.put(0, frame);

		this.npcPart.animation.clear();
		this.npcAnim.animation.setAnimation(ac, AnimationKind.EDITING);
		this.npcPart.setHealth(this.npcPart.getMaxHealth());
		this.npcPart.deathTime = 0;
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		this.setPart(dataParts.get(scroll.getSelected()));
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
		List<Entity> entities = this.npc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(this.npc.getPosition()).grow(4.55d, 4.55d, 4.55d));
		for (Entity e : entities) {
			if (e.equals(this.npc)) {
				continue;
			}
			NBTTagCompound nbt = new NBTTagCompound();
			Entity le;
			if (e instanceof EntityNPCInterface) {
				le = Util.instance.copyToGUI((EntityNPCInterface) e, mc.world, true);
			} else {
				e.writeToNBTAtomically(nbt);
				le = EntityList.createEntityFromNBT(nbt, this.npc.world);
			}
			if (le != null) {
				le.posX -= this.npc.posX;
				le.posY -= this.npc.posY;
				le.posZ -= this.npc.posZ;
				le.rotationYaw = e.rotationYaw;
				le.prevRotationYaw = e.rotationYaw;
				le.rotationPitch = e.rotationPitch;
				le.prevRotationPitch = e.rotationPitch;
				this.environmentEntitys.add(le);
			}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui.id == 0 && subgui instanceof SubGuiColorSelector) {
			CustomNpcs.colorAnimHoverPart = ((SubGuiColorSelector) subgui).color;
			ModelNpcAlt.editAnimDataSelect.red = (float) (CustomNpcs.colorAnimHoverPart >> 16 & 255) / 255.0F;
			ModelNpcAlt.editAnimDataSelect.green = (float) (CustomNpcs.colorAnimHoverPart >> 8 & 255) / 255.0F;
			ModelNpcAlt.editAnimDataSelect.blue = (float) (CustomNpcs.colorAnimHoverPart & 255) / 255.0F;
			this.partNames.setColorLine(CustomNpcs.colorAnimHoverPart);
			this.initGui();
		}
		if (subgui instanceof GuiSoundSelection) {
			if (frame != null) {
				frame.setStartSound(((GuiSoundSelection) subgui).selectedResource);
				this.initGui();
			}
		}
		if (subgui instanceof SubGuiEditText) {
			SubGuiEditText guiText = (SubGuiEditText) subgui;
			if (guiText.id == 0) {
				try {
					int pos = Integer.parseInt(guiText.text[0]) - 1;
					if (pos < 0) { pos = 0; } else if (pos > anim.frames.size()) { pos = anim.frames.size(); }
					frame = (AnimationFrameConfig) anim.addFrame(pos, frame);
					this.setPart(frame.parts.get(part.id));
					this.initGui();
				} catch (Exception e) { LogWriter.error("Error:", e); }
			}
		}
		if (subgui instanceof SubGuiSelectItemStack) {
			SubGuiSelectItemStack guiStack = (SubGuiSelectItemStack) subgui;
			if (guiStack.id == 0) { frame.setHoldRightStack(guiStack.stack); }
			else { frame.setHoldLeftStack(guiStack.stack); }
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.hasSubGui() || anim == null) { return; }
		switch (textField.getId()) {
			case 0: { // repeatLast
				if (anim == null) { return; }
				anim.setRepeatLast(textField.getInteger());
				this.resetAnimation();
				break;
			}
			case 1: { // speed
				if (frame == null) {
					return;
				}
				frame.setSpeed(textField.getInteger());
				this.resetAnimation();
				break;
			}
			case 2: { // delay
				if (frame == null) {
					return;
				}
				frame.setEndDelay(textField.getInteger());
				this.resetAnimation();
				break;
			}
			case 5: { // X
				if (this.isHitPox) {
					if (anim == null || toolType == 0) { return; }
					float value = (float) textField.getDouble();
					if (toolType == 1) { anim.offsetHitbox[0] = value; } else { anim.scaleHitbox[0] = value; }
					textField.setText("" + (float) Math.round(value * 1000.0d) / 1000.0f);
					return;
				}
				PartConfig p = (PartConfig) this.partNames.objs[0];
				int tType = (int) this.partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				float value = 0.0f;
				switch (toolType) {
					case 0: {
						p.rotation[0] = (value = (float) (textField.getDouble()) / 360.0f);
						break;
					}
					case 1: { // o
						p.offset[0] = (value = 0.1f * (float) (textField.getDouble()) + 0.5f);
						break;
					}
					case 2: { // s
						p.scale[0] = (value = (float) (textField.getDouble()) / 5.0f);
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (this.tools.getSlider(0) != null) {
					this.tools.getSlider(0).sliderValue = value;
				}
				this.resetAnimation();
				break;
			}
			case 6: { // Y
				if (this.isHitPox) {
					if (anim == null || toolType == 0) { return; }
					float value = (float) textField.getDouble();
					if (toolType == 1) { anim.offsetHitbox[1] = value; } else { anim.scaleHitbox[1] = value; }
					textField.setText("" + (float) Math.round(value * 1000.0d) / 1000.0f);
					return;
				}
				PartConfig p = (PartConfig) this.partNames.objs[0];
				int tType = (int) this.partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				float value = 0.0f;
				switch (toolType) {
					case 0: { // r
						p.rotation[1] = (value = (float) (textField.getDouble()) / 360.0f);
						break;
					}
					case 1: { // o
						p.offset[1] = (value = 0.1f * (float) (textField.getDouble()) + 0.5f);
						break;
					}
					case 2: { // s
						p.scale[1] = (value = (float) (textField.getDouble()) / 5.0f);
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (this.tools.getSlider(1) != null) {
					this.tools.getSlider(1).sliderValue = value;
				}
				this.resetAnimation();
				break;
			}
			case 7: { // Z
				if (this.isHitPox) {
					if (anim == null || toolType == 0) { return; }
					float value = (float) textField.getDouble();
					if (toolType == 1) { anim.offsetHitbox[2] = value; } else { anim.scaleHitbox[2] = value; }
					textField.setText("" + (float) Math.round(value * 1000.0d) / 1000.0f);
					return;
				}
				PartConfig p = (PartConfig) this.partNames.objs[0];
				int tType = (int) this.partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				float value = 0.0f;
				switch (toolType) {
					case 0: { // r
						p.rotation[2] = (value = (float) (textField.getDouble()) / 360.0f);
						break;
					}
					case 1: { // o
						p.offset[2] = (value = 0.1f * (float) (textField.getDouble()) + 0.5f);
						break;
					}
					case 2: { // s
						p.scale[2] = (value = (float) (textField.getDouble()) / 5.0f);
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (this.tools.getSlider(2) != null) {
					this.tools.getSlider(2).sliderValue = value;
				}
				this.resetAnimation();
				break;
			}
			case 8: { // X1
				PartConfig p = (PartConfig) this.partNames.objs[0];
				int tType = (int) this.partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				float value = (float) (textField.getDouble()) / 360.0f;
				p.rotation[3] = value;
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (this.tools.getSlider(3) != null) {
					this.tools.getSlider(3).sliderValue = value;
				}
				this.resetAnimation();
				break;
			}
			case 9: { // Y1
				PartConfig p = (PartConfig) this.partNames.objs[0];
				int tType = (int) this.partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				float value = (float) (textField.getDouble()) * 0.0055556f - 0.5f;
				p.rotation[4] = value;
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (this.tools.getSlider(4) != null) {
					this.tools.getSlider(4).sliderValue = value;
				}
				this.resetAnimation();
				break;
			}
			case 10: { // chance
				if (this.anim == null) { return; }
				this.anim.chance = (float) Math.round(textField.getDouble() * 1000.0d) / 100000.0f;
				textField.setText("" + (this.anim.chance * 100.0f));
				this.resetAnimation();
				break;
			}
		}
	}

	@Override
	public void save() {
		if (this.anim != null) { Client.sendData(EnumPacketServer.AnimationChange, this.anim.save()); }
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("save", true);
		Client.sendData(EnumPacketServer.EmotionChange, nbt);
	}

	@Override
	public void closeMiniWindow(GuiNpcMiniWindow miniWindow) {
		this.mwindows.remove(miniWindow.id);
		if (this.getButton(29) != null) {
			this.getButton(29).enabled = !this.mwindows.containsKey(this.partNames.id);
		}
		if (this.getButton(35) != null) {
			this.getButton(35).enabled = !this.mwindows.containsKey(this.tools.id);
		}
	}

}
