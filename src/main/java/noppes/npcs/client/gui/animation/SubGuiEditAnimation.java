package noppes.npcs.client.gui.animation;

import java.util.*;

import net.minecraft.util.math.Vec3d;
import noppes.npcs.LogWriter;
import noppes.npcs.client.model.animation.*;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

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
	public AddedPartConfig addedPartConfig;
	public AnimationDamageHitbox hitbox;
	private GuiCustomScroll scrollParts;
	private GuiCustomScroll scrollHitboxes;
	private int blockType;
	private int blockSize;
	private int toolType;
	private int waitKey;
	private int waitKeyID;
	private boolean onlyCurrentPart;
	private boolean hovered;
	private boolean hasExtend;
	private boolean hoverRight;
	private boolean hoverLeft;
	private boolean isHitbox = false;
	private boolean isMotion = false;
	private boolean isChanged = true;
	private final EntityNPCInterface npcAnim;
    private final EntityNPCInterface npcPart;
	private final double[] basePos;
	private final String[] blockNames, blockSizes;
	// display
	private int workU;
	private int workV;
	private int workS;
	private int winU;
	private int winV;
	private int winW;
	private int winH;
	private int mousePressId;
	private int mousePressX;
	private int mousePressY;
	private final float[] dispRot;
	private final float[] dispPos;
	private float dispScale;
	private float winScale;
	private float offsetY;
	private final Map<String, PartConfig> dataParts = new LinkedHashMap<>();
	private final Map<String, AnimationDamageHitbox> dataHitboxes = new LinkedHashMap<>();
	private final List<Entity> environmentEntitys= new ArrayList<>();
	private final Map<BlockPos, IBlockState> environmentStates = new HashMap<>();
	private final Map<BlockPos, TileEntity> environmentTiles = new HashMap<>();

	private GuiNpcMiniWindow partNames;
	private GuiNpcMiniWindow tools;
	private GuiNpcMiniWindow hitboxes;
	private double w = -1.0d;
	private double h = -1.0d;
	private float baseRotation = 0.0f;

	public SubGuiEditAnimation(EntityNPCInterface npc, AnimationConfig animation, int animId, GuiNpcAnimation parentGUI) {
		super(npc);
		id = animId;
		ySize = 240;
		xSize = 427;
		parent = parentGUI;
		anim = animation;
		frame = anim.frames.get(0);
		setPart(frame.parts.get(3));
		setHitbox(frame.damageHitboxes.get(0));
		waitKey = 0;

		// Display
		toolType = 0; // 0 - rotation, 1 - offset, 2 - scale
		blockType = 0; // 0 - environment, 1 - non, 2 - stone, 3 - stairs, 4 - stone_slab, 5 - carpet
		blockSize = 2; // 0 - x1, 1 - x3, 2 - x5, 3 - x7, 4 - x9
		dispScale = 1.0f;
		winScale = 1.0f;
		dispRot = new float[] { 45.0f, 345.0f, 345.0f };
		dispPos = new float[] { 0.0f, 0.0f, 0.0f };
		setEnvironment();
		mousePressId = -1;
		mousePressX = 0;
		mousePressY = 0;
		onlyCurrentPart = false;

		basePos = new double[] {npc.posX, npc.posY, npc.posZ};
		npcAnim = Util.instance.copyToGUI(npc, mc.world, true);
		npcAnim.display.setName(npc.getName()+"_animation");
		baseRotation = npcAnim.rotationYaw;

		npcPart = Util.instance.copyToGUI(npc, mc.world, true);
		npcPart.display.setName(npc.getName()+"_anim_part");

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
		ModelNpcAlt.editAnimDataSelect.part = part == null || isHitbox || isMotion ? null : part.getEnumType();
		if (part != null && part.id > 7) { addedPartConfig = anim.getAddedPart(part.id); }
		else { addedPartConfig = null; }
		if (tools != null && tools.visible) { showTools(); }
	}

	private void setHitbox(AnimationDamageHitbox hitboxConfig) {
		GuiNpcTextField.unfocus();
		if (hitboxConfig != null) {
			hitbox = frame.damageHitboxes.get(hitboxConfig.id);
			if (scrollHitboxes != null && scrollHitboxes.hasSelected()) {
				isHitbox = true;
				ModelNpcAlt.editAnimDataSelect.part = null;
			}
		} else {
			isHitbox = false;
			if (scrollHitboxes != null) { scrollHitboxes.setSelected(null); }
		}
		if (tools != null && tools.visible) { showTools(); }
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
System.out.println("buttonID: "+button.id);
		switch (button.id) {
			case 0: {
				blockType = button.getValue();
				if (getButton(16) != null) {
					getButton(16).setEnabled(blockType != 1);
				}
				break;
			} // block place
			case 1: {
				blockSize = button.getValue();
				break;
			} // block size
			case 2: {
				GuiNpcAnimation.backColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000);
				button.layerColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
				break;
			} // back color
			case 3: {
				if (anim == null || part == null || !anim.frames.containsKey(button.getValue()) || anim.frames.get(button.getValue()).id == -1) {
					return;
				}
				frame = anim.frames.get(button.getValue());
				setPart(frame.parts.get(part.id));
				initGui();
				break;
			} // select frame
			case 4: {
				if (anim == null) { return; }
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					SubGuiEditText subgui = new SubGuiEditText(0, "" + anim.frames.size());
					subgui.numbersOnly = new int[] { 0, anim.frames.size(), anim.frames.size() };
					setSubGui(subgui);
				} else {
					frame = (AnimationFrameConfig) anim.addFrame(-1, frame);
					setPart(frame.parts.get(part.id));
					initGui();
				}
				break;
			} // add new (copy) frame
			case 5: {
				if (frame == null) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			} // remove frame
			case 6: {
				if (frame == null) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.frame", "" + (frame.id + 1)).getFormattedText(),
						new TextComponentTranslation("gui.clearMessage").getFormattedText(),
						GuiScreen.isShiftKeyDown() ? 4 : 1);
				displayGuiScreen(guiyesno);
				break;
			} // clear frame
			case 7: {
				if (isHitbox) {
					if (hitbox == null) { return; }
					hitbox = new AnimationDamageHitbox(frame.damageHitboxes.size());
					hitbox.clear();
					frame.damageHitboxes.put(hitbox.id, hitbox);
					initGui();
				} else {
					if (isMotion || anim == null || part == null || part.id == 6 || part.id == 7) { return; }
					System.out.println("CNPCs: " + anim.addParts);

					/*addedPartConfig = anim.addAddedPart(part.id);
					frame = anim.frames.get(frame.id);
					setPart(frame.parts.get(addedPartConfig.id));*/
				}
				break;
			} // add new part / damage hitbox
			case 8: {
				if (isHitbox) {
					if (hitbox == null) { return; }
					if (frame.damageHitboxes.size() == 1) {
						hitbox.clear();
					} else {
						int id = hitbox.id;
						frame.damageHitboxes.remove(id);
						hitbox = frame.damageHitboxes.get(id - 1);
					}
					initGui();
				} else {
					if (isMotion || anim == null || addedPartConfig == null || part == null) { return; }
					GuiYesNo guiyesno = new GuiYesNo(this,
							new TextComponentTranslation("animation.clear.part", "" + (part.id + 1),
									scrollParts.getSelected()).getFormattedText(),
							new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
					displayGuiScreen(guiyesno);
				}
				break;
			} // del part
			case 9: {
				if (part == null) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("animation.clear.part", "" + (part.id + 1),
								scrollParts.getSelected()).getFormattedText(),
						new TextComponentTranslation("gui.clearMessage").getFormattedText(),
						GuiScreen.isShiftKeyDown() ? 5 : 3);
				displayGuiScreen(guiyesno);
				break;
			} // clear part
			case 10: {
				if (anim == null || part == null) {
					return;
				}
				part.setDisable(!((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					for (AnimationFrameConfig f : anim.frames.values()) {
						f.parts.get(part.id).setDisable(part.isDisable());
					}
				}
				resetAnimation();
				break;
			} // disabled part
			case 11: {
				if (anim == null || frame == null) {
					return;
				}
				frame.setSmooth(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					for (AnimationFrameConfig f : anim.frames.values()) {
						f.setSmooth(frame.isSmooth());
					}
				}
				resetAnimation();
				break;
			} // smooth
			case 12: {
				setSubGui(new SubGuiColorSelector(CustomNpcs.colorAnimHoverPart));
				break;
			} // color hover
			case 13: {
				if (meshType == 0) {
					meshType = -1;
					button.layerColor = 0xFF360C1C;
				} else {
					meshType = 0;
					button.layerColor = 0xFFD93070;
				}
				if (getButton(14) != null) {
					getButton(14).layerColor = 0xFF1A0C36;
				}
				if (getButton(15) != null) {
					getButton(15).layerColor = 0xFF0C3620;
				}
				if (getButton(16) != null) {
					getButton(16).layerColor = 0xFF35360C;
				}
				break;
			} // reset mesh
			case 14: {
				if (meshType == 1) {
					meshType = -1;
					button.layerColor = 0xFF1A0C36;
				} else {
					meshType = 1;
					button.layerColor = 0xFF6830D9;
				}
				if (getButton(13) != null) {
					getButton(13).layerColor = 0xFF360C1C;
				}
				if (getButton(15) != null) {
					getButton(15).layerColor = 0xFF0C3620;
				}
				if (getButton(16) != null) {
					getButton(16).layerColor = 0xFF35360C;
				}
				break;
			} // xz mesh
			case 15: {
				if (meshType == 2) {
					meshType = -1;
					button.layerColor = 0xFF0C3620;
				} else {
					meshType = 2;
					button.layerColor = 0xFF30D980;
				}
				if (getButton(13) != null) {
					getButton(13).layerColor = 0xFF360C1C;
				}
				if (getButton(14) != null) {
					getButton(14).layerColor = 0xFF1A0C36;
				}
				if (getButton(16) != null) {
					getButton(16).layerColor = 0xFF35360C;
				}
				break;
			} // xy mesh
			case 16: {
				if (meshType == 3) {
					meshType = -1;
					button.layerColor = 0xFF35360C;
				} else {
					meshType = 3;
					button.layerColor = 0xFFD7D930;
				}
				if (getButton(13) != null) {
					getButton(13).layerColor = 0xFF360C1C;
				}
				if (getButton(14) != null) {
					getButton(14).layerColor = 0xFF1A0C36;
				}
				if (getButton(15) != null) {
					getButton(15).layerColor = 0xFF0C3620;
				}
				break;
			} // xy mesh
			case 17: {
				showHitBox = !showHitBox;
				button.layerColor = showHitBox ? 0 : 0xFF808080;
				break;
			} // show NPC hitbox
			case 18: {
				dispScale = 1.0f;
				break;
			} // display reset scale
			case 19: {
				for (int i = 0; i < 3; i++) {
					dispPos[i] = 0.0f;
				}
				break;
			} // display reset pos
			case 20: {
				dispRot[0] = 45.0f;
				dispRot[1] = 345.0f;
				dispRot[2] = 345.0f;
				break;
			} // display reset rot
			case 21: {
				onlyCurrentPart = !onlyCurrentPart;
				button.txrX = onlyCurrentPart ? 144 : 188;
				resetAnimation();
				break;
			} // show only current part or animation
			case 22: {
				if (anim == null || part == null) {
					return;
				}
				part.setShow(((GuiNpcCheckBox) button).isSelected());
				if (GuiScreen.isShiftKeyDown()) { // Shift pressed
					for (AnimationFrameConfig f : anim.frames.values()) {
						f.parts.get(part.id).setShow(part.isShow());
					}
				}
				resetAnimation();
				break;
			} // show part in frame
			case 23: {
				if (toolType == 1) { return; }
				GuiNpcTextField.unfocus();
				toolType = 1;
				initGui();
				break;
			} // select tool pos
			case 24: {
				if (toolType == 0) { return; }
				GuiNpcTextField.unfocus();
				toolType = 0;
				initGui();
				break;
			} // select tool rot
			case 25: {
				if (toolType == 2) { return; }
				GuiNpcTextField.unfocus();
				toolType = 2;
				initGui();
				break;
			} // select tool scale
			case 26: {
				ModelNpcAlt.editAnimDataSelect.showArmor = !ModelNpcAlt.editAnimDataSelect.showArmor;
				button.layerColor = (ModelNpcAlt.editAnimDataSelect.showArmor ? 0xFFFF7200 : 0xFF6F3200);
				break;
			} // show armor
			case 27: {
				if (frame == null) { return; }
				setSubGui(new GuiSoundSelection(frame.getStartSound()));
				break;
			} // select sound
			case 28: {
				if (frame == null) { return; }
				frame.setStartSound("");
				break;
			} // remove sound
			case 29: {
				if (partNames == null) { showPartNames(); }
				if (mwindows.containsKey(partNames.id)) { return; }
				partNames.isMoving = false;
				partNames.visible = true;
				mwindows.put(partNames.id, partNames);
				button.enabled = false;
				break;
			} // show parts
			case 30: {
				if (isHitbox) {
					if (hitbox != null) {
						if (toolType == 1) { hitbox.offset[0] = 0.0f; }
						else { hitbox.scale[0] = 1.0f; }
						initGui();
					}
				} else if (isMotion) {
					frame.motions[0] = 0.0f;
					initGui();
				} else if (part != null) {
					switch (toolType) {
						case 0: {
							part.rotation[0] = 0.0f;
							break;
						}
						case 1: {
							part.offset[0] = 0.0f;
							break;
						}
						case 2: {
							part.scale[0] = 1.0f;
							break;
						}
					}
					initGui();
				}
				break;
			} // reset part set X
			case 31: {
				if (isHitbox) {
					if (hitbox != null) {
						if (toolType == 1) { hitbox.offset[1] = 0.0f; }
						else { hitbox.scale[1] = 1.0f; }
						initGui();
					}
				} else if (isMotion) {
					frame.motions[1] = 0.0f;
					initGui();
				} else if (part != null) {
					switch (toolType) {
						case 0: {
							part.rotation[1] = 0.0f;
							break;
						}
						case 1: {
							part.offset[1] = 0.0f;
							break;
						}
						case 2: {
							part.scale[1] = 1.0f;
							break;
						}
					}
					initGui();
				}
				break;
			} // reset part set Y
			case 32: {
				if (isHitbox) {
					if (hitbox != null) {
						if (toolType == 1) { hitbox.offset[2] = 0.0f; }
						else { hitbox.scale[2] = 1.0f; }
						initGui();
					}
				} else if (isMotion) {
					frame.motions[2] = 0.0f;
					initGui();
				} else if (part != null) {
					switch (toolType) {
						case 0: {
							part.rotation[2] = 0.0f;
							break;
						}
						case 1: {
							part.offset[2] = 0.0f;
							break;
						}
						case 2: {
							part.scale[2] = 1.0f;
							break;
						}
					}
					initGui();
				}
				break;
			} // reset part set Z
			case 33: {
				part.rotation[3] = 0.0f;
				initGui();
				break;
			} // reset part set X1 rot
			case 34: {
				part.rotation[4] = 0.0f;
				initGui();
				break;
			} // reset part set Y1 rot
			case 35: {
				if (tools == null) { showTools(); }
				if (mwindows.containsKey(tools.id)) { return; }
				tools.isMoving = false;
				tools.visible = true;
				mwindows.put(tools.id, tools);
				button.enabled = false;
				break;
			} // show window tools
			case 36: {
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
				break;
			} // is now damage
			case 37: {
				if (frame == null) { return; }
				frame.setHoldRightStackType(button.getValue());
				resetAnimation();
				break;
			} // right stack type
			case 38: {
				if (frame == null) { return; }
				frame.setHoldLeftStackType(button.getValue());
				resetAnimation();
				break;
			} // left stack type
			case 39: {
				if (frame == null) { return; }
				frame.showMainHand = !frame.showMainHand;
				initGui();
				break;
			} // show main hand
			case 40: {
				if (frame == null) { return; }
				frame.showOffHand = !frame.showOffHand;
				initGui();
				break;
			} // show offhand
			case 41: {
				if (frame == null) { return; }
				frame.showHelmet = !frame.showHelmet;
				initGui();
				break;
			} // show helmet
			case 42: {
				if (frame == null) { return; }
				frame.showBody = !frame.showBody;
				initGui();
				break;
			} // show body
			case 43: {
				if (frame == null) { return; }
				frame.showLegs = !frame.showLegs;
				initGui();
				break;
			} // show legs
			case 44: {
				if (frame == null) { return; }
				frame.showFeets = !frame.showFeets;
				initGui();
				break;
			} // show feet's
			case 45: {
				showHitBoxes();
				hitboxes.isMoving = false;
				hitboxes.visible = true;
				mwindows.put(hitboxes.id, hitboxes);
				button.enabled = false;
				break;
			} // show window hitbox
			case 46: {
				if (ModelNpcAlt.editAnimDataSelect.alpha >= 1.0f) {
					ModelNpcAlt.editAnimDataSelect.alpha = 0.25f;
					button.layerColor = 0xFF787758;
				} else {
					ModelNpcAlt.editAnimDataSelect.alpha = 1.0f;
					button.layerColor = 0xFFFFFEBF;
				}
				break;
			} // show alpha // show armor
			case 47: {
				if (baseRotation == 0.0f) { return; }
				if (npcAnim.rotationYaw != baseRotation) {
					npcAnim.rotationYaw = baseRotation;
					npcAnim.prevRotationYaw = baseRotation;
					npcAnim.rotationYawHead = baseRotation;
					npcAnim.prevRotationYawHead = baseRotation;
					npcPart.rotationYaw = baseRotation;
					npcPart.prevRotationYaw = baseRotation;
					npcPart.rotationYawHead = baseRotation;
					npcPart.prevRotationYawHead = baseRotation;
					button.layerColor = 0xFF96FFC0;
				} else {
					npcAnim.rotationYaw = 0.0f;
					npcAnim.prevRotationYaw = 0.0f;
					npcAnim.rotationYawHead = 0.0f;
					npcAnim.prevRotationYawHead = 0.0f;
					npcPart.rotationYaw = 0.0f;
					npcPart.prevRotationYaw = 0.0f;
					npcPart.rotationYawHead = 0.0f;
					npcPart.prevRotationYawHead = 0.0f;
					button.layerColor = 0xFF426C53;
				}
				break;
			} // reset NPC rotation
			case 48: {
				isMotion = true;
				button.enabled = false;
				if (toolType != 1) { toolType = 1; }

				scrollHitboxes.setSelected(null);
				setHitbox(null);

				scrollParts.setSelected(null);
				setPart(null);
				initGui();
				break;
			} // show motion type
			case 66: {
				close();
				break;
			} // exit
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		displayGuiScreen(parent);
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
				setPart(frame.parts.get(part.id));
				initGui();
				break;
			}
			case 1: { // clear frame
				if (frame == null) {
					return;
				}
				for (PartConfig p : frame.parts.values()) {
					p.clear();
				}
				initGui();
				break;
			}
			case 2: { // remove added part
				if (anim == null || addedPartConfig == null || part == null) { return; }
				int f = addedPartConfig.id - 1;
				if (f < 0) { f = 0; }
				anim.removeAddedPart(addedPartConfig);
				frame = anim.frames.get(frame.id);
				setPart(frame.parts.get(f));
				initGui();
				break;
			}
			case 3: { // clear part
				if (part == null) {
					return;
				}
				part.clear();
				initGui();
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
				initGui();
				break;
			}
			case 5: { // clear all part
				if (anim == null || part == null) {
					return;
				}
				for (AnimationFrameConfig f : anim.frames.values()) {
					f.parts.get(part.id).clear();
				}
				initGui();
				break;
			}
		}
	}

	private void displayOffset(int x, int y) {
		for (int i = 0; i < 2; i++) {
			dispPos[i] += (i == 0 ? x : y);
			if (dispPos[i] > workS * dispScale) {
				dispPos[i] = workS * dispScale;
			} else if (dispPos[i] < -workS * dispScale) {
				dispPos[i] = -workS * dispScale;
			}
		}
	}

	private void displayRotate(int x, int y) {
		dispRot[0] += x;
		dispRot[1] += (float) (Math.cos(dispRot[0] * Math.PI / 180.0f) * (float) y);
		dispRot[2] += (float) (Math.sin(dispRot[0] * Math.PI / 180.0f) * (float) y);
		for (int i = 0; i < 3; i++) {
			if (dispRot[i] > 360.0f) {
				dispRot[i] -= 360.0f;
			} else if (dispRot[i] < 0.0f) {
				dispRot[i] += 360.0f;
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

	/**
	 * @param type 0:X; 1:Y; 2:Z
	 */
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

	private void drawLine(double x0, double y0, double z0, double x1, double y1, double z1, float size, float red, float green, float blue) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(size);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(x0, y0, z0).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(x1, y1, z1).color(red, green, blue, 1.0f).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void drawCircle(double cx, double cy, double cz, double radius, int type, int segments, float size, float red, float green, float blue) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(size);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		for (int i = 0; i <= segments; ++i) {
			double angle = ((double) i / segments) * Math.PI * 2;

			switch (type) {
				case 0: // XZ
					buffer.pos(cx + Math.cos(angle) * radius, cy, cz + Math.sin(angle) * radius).color(red, green, blue, 1.0F).endVertex();
					break;
				case 1: // XY
					buffer.pos(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, cz).color(red, green, blue, 1.0F).endVertex();
					break;
				case 2: // YZ
					buffer.pos(cx, cy + Math.cos(angle) * radius, cz + Math.sin(angle) * radius).color(red, green, blue, 1.0F).endVertex();
					break;
			}
		}

		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	private void drawArrow(double radius, float size, float red, float green, float blue) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(size);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		buffer.pos(0.0d, 0.0d, 0.0d).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(0.0d, 0.0d, radius).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(0.0d, 0.0d, radius).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(radius * 0.05d, 0.0d, radius * 0.75d).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(0.0d, 0.0d, radius).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(radius * -0.05d, 0.0d, radius * 0.75d).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(0.0d, 0.0d, radius).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(0.0d, radius * 0.05d, radius * 0.75d).color(red, green, blue, 1.0f).endVertex();

		buffer.pos(0.0d, 0.0d, radius).color(red, green, blue, 1.0f).endVertex();
		buffer.pos(0.0d, radius * -0.05d, radius * 0.75d).color(red, green, blue, 1.0f).endVertex();

		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui != null) {
			subgui.drawScreen(mouseX, mouseY, partialTicks);
			return;
		}
		if (w < 0 || h < 0) {
			ScaledResolution sw = new ScaledResolution(mc);
			w  = sw.getScaledWidth();
			h = sw.getScaledHeight();
		}
		if (waitKey != 0) { waitKey--; }
		for (int i = 0; i < 2; i++) {
			EntityNPCInterface dNpc = (i == 0 ? npcAnim : npcPart);
			if (dNpc == null) { continue; }
			dNpc.animation.updateTime();
			dNpc.field_20061_w = npc.field_20061_w;
			dNpc.field_20062_v = npc.field_20062_v;
			dNpc.field_20063_u = npc.field_20063_u;
			dNpc.field_20064_t = npc.field_20064_t;
			dNpc.field_20065_s = npc.field_20065_s;
			dNpc.field_20066_r = npc.field_20066_r;
			dNpc.ticksExisted = npc.ticksExisted;
		}
		for (Entity e : environmentEntitys) {
			e.ticksExisted = npc.ticksExisted;
		}
		if (getDisplayNpc() == null) { close(); }
		// display data
		if (Mouse.isButtonDown(mousePressId)) {
			int x = mouseX - mousePressX;
			int y = mouseY - mousePressY;
			if (x != 0 || y != 0) {
				if (mousePressId == 0) {
					displayOffset(x, y);
				} // LMB
				else if (mousePressId == 1) {
					displayRotate(x, -y);
				} // RMB
				mousePressX = mouseX;
				mousePressY = mouseY;
			}
		} else {
			mousePressId = -1;
		}
		hovered = isMouseHover(mouseX, mouseY, workU + 1, workV + 1, workS - 2, workS - 2);
		if (hovered && !tools.hovered && !partNames.hovered) {
			int dWheel = Mouse.getDWheel();
			if (dWheel != 0) {
				dispScale += dispScale * (dWheel < 0 ? 0.1f : -0.1f);
				if (dispScale < 0.5f) {
					dispScale = 0.5f;
				} else if (dispScale > 5.0f) {
					dispScale = 5.0f;
				}
				dispScale = (float) (Math.round(dispScale * 20.0d) / 20.0d);
				if (dispScale == 0.95f || dispScale == 1.05f) {
					dispScale = 1.0f;
				}
			}
		}
		// back place
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, -300.0f);
		drawGradientRect(winU + 1, winV + 1, winU + winW - 1, winV + winH - 1, 0xFFC6C6C6, 0xFFC6C6C6);
		drawHorizontalLine(winU + 1, winU + winW - 2, winV, 0xFF000000);
		drawVerticalLine(winU, winV, winV + winH - 1, 0xFF000000);
		drawVerticalLine(winU + winW - 1, winV, winV + winH - 1, 0xFF000000);
		drawHorizontalLine(winU + 1, winU + winW - 2, winV + winH - 1, 0xFF000000);
		// work place
		int color = GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFF080F0 : 0xFFF020F0;
		drawGradientRect(workU, workV, workU + workS, workV + workS, color, color);
		// Slots
		int y = 86 + (hasExtend ? 14 : 0);
		GlStateManager.pushMatrix();
		mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
		GlStateManager.translate(winU + 3, winV + y, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(0, 0, 0, 0, 18, 18); // right
		IItemStack stack;
		switch(frame.getHoldRightStackType()) {
			case 1: stack = npc.inventory.getProjectile(); break;
			case 2: stack = npc.inventory.getLeftHand(); break;
			case 3: stack = frame.getHoldRightStack(); break;
			default: stack = npc.inventory.getRightHand(); break;
		}
		hoverRight = isMouseHover(mouseX, mouseY, winU + 3, winV + y + 1, 16, 16);
		if (hoverRight) {
			Gui.drawRect(1, 1, 17, 17, 0x80FFFFFF);
			if (stack != null && !stack.isEmpty()) {
				List<String> list = stack.getMCItemStack().getTooltip(player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
				if (!list.isEmpty()) { hoverText = list.toArray(new String[0]); }
			}
		}
		if (stack != null && !stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(1.0f, 1.0f, 0.0f);
			RenderHelper.enableStandardItemLighting();
			mc.getRenderItem().renderItemAndEffectIntoGUI(stack.getMCItemStack(), 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		y += 20;
		mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
		GlStateManager.translate(0.0f, 20.0f, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(0, 0, 0, 0, 18, 18); // left
		switch(frame.getHoldLeftStackType()) {
			case 1: stack = npc.inventory.getProjectile(); break;
			case 2: stack = npc.inventory.getRightHand(); break;
			case 3: stack = frame.getHoldLeftStack(); break;
			default: stack = npc.inventory.getLeftHand(); break;
		}
		hoverLeft = isMouseHover(mouseX, mouseY, winU + 3, winV + y + 1, 16, 16);
		if (hoverLeft) {
			Gui.drawRect(1, 1, 17, 17, 0x80FFFFFF);
			if (stack != null && !stack.isEmpty()) {
				List<String> list = stack.getMCItemStack().getTooltip(player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
				if (!list.isEmpty()) { hoverText = list.toArray(new String[0]); }
			}
		}
		if (stack != null && !stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(1.0f, 1.0f, 0.0f);
			RenderHelper.enableStandardItemLighting();
			mc.getRenderItem().renderItemAndEffectIntoGUI(stack.getMCItemStack(), 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
		// Lines
		y -= 80 + (hasExtend ? 14 : 0);
		for (int i = 0; i < 17; i++) { // name -> work
			drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 23;
		for (int i = 0; i < 17; i++) { // work -> frame
			drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 76 + (hasExtend ? 14 : 0);
		for (int i = 0; i < 17; i++) { // frame -> part
			drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 38;
		for (int i = 0; i < 17; i++) { // part -> chance
			drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 17;
		for (int i = 0; i < 17; i++) { // chance -> sound
			drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 26;
		for (int i = 0; i < 17; i++) { // sound -> emotion
			drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 26;
		for (int i = 0; i < 17; i++) { // emotion -> equipment
			drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		y += 25;
		for (int i = 0; i < 17; i++) { // equipment -> end
			drawHorizontalLine(winU + 4 + i * 8, winU + 8 + i * 8, winV + y, 0xFF000000);
		}
		GlStateManager.popMatrix();
		if (subgui == null) {
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			int c = w < mc.displayWidth ? (int) Math.round((double) mc.displayWidth / w) : 1;
			GL11.glScissor((workU + 1) * c, mc.displayHeight - (workV + workS - 1) * c, (workS - 2) * c, (workS - 2) * c);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawWork(partialTicks);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 950.0f);
			// axis xyz vector
			GlStateManager.pushMatrix();
			GlStateManager.translate(workU + 12.5f, workV + 12.0f, dispScale > 1.0f ? -240.0f : 0.0f);
			if (dispRot[0] != 0.0f) {
				GlStateManager.rotate(dispRot[0], 0.0f, 1.0f, 0.0f);
			}
			if (dispRot[1] != 0.0f) {
				GlStateManager.rotate(dispRot[1], 0.0f, 0.0f, 1.0f);
			}
			if (dispRot[2] != 0.0f) {
				GlStateManager.rotate(dispRot[2], 1.0f, 0.0f, 0.0f);
			}
			GlStateManager.pushMatrix();
			GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
			GlStateManager.translate(0.0f, 0.0f, 0.5f);
			drawCRect(-10.5, -0.5d, -0.5d, 0.5d, 0xFF0000FF);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			drawCRect(-10.5, -0.5d, -0.5d, 0.5d, 0xFFFF0000);
			drawCRect(-0.5d, -10.5, 0.5d, -0.5d, 0xFF00D000);
			drawCRect(-0.5d, -0.5d, 0.5d, 0.5d, GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000);
			GlStateManager.popMatrix();
			GlStateManager.popMatrix();
			// display info
			GlStateManager.pushMatrix();
			GlStateManager.translate(workU, workV, 0.0f);
			String ts = "x" + dispScale;
			fontRenderer.drawString(ts, workS - 11 - fontRenderer.getStringWidth(ts), 1,
					GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000, false);
			ts = (int) dispRot[0] + "" + ((char) 176) + "/" + (int) dispRot[1] + ((char) 176) + "/"
					+ (int) dispRot[2] + ((char) 176);
			fontRenderer.drawString(ts, workS - 11 - fontRenderer.getStringWidth(ts), workS - 10,
					GuiNpcAnimation.backColor == 0xFF000000 ? 0xFFFFFFFF : 0xFF000000, false);
			ts = (int) dispPos[0] + "/" + (int) dispPos[1];
			fontRenderer.drawString(ts, 11, workS - 10,
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

		getButton(7).enabled = !isMotion && ((isHitbox && hitbox != null) || (anim != null && part != null && part.id != 6 && part.id != 7));
		getButton(8).enabled = !isMotion && ((isHitbox && hitbox != null) || (anim != null && addedPartConfig != null && part != null && part.id > 8));

		if (hasSubGui() || !CustomNpcs.ShowDescriptions) { return; }
		if (hoverMiniWin) {
			if (hoverText != null) {
				drawHoveringText(Arrays.asList(hoverText), mouseX, mouseY, fontRenderer);
				hoverText = null;
			}
			return;
		}
		if (scrollParts != null && scrollParts.hovered) {
			setHoverText(new TextComponentTranslation("animation.hover.part.sel").getFormattedText());
		} else if (getLabel(0) != null && getLabel(0).hovered) {
			setHoverText(new TextComponentTranslation("animation.hover.help").getFormattedText());
		} else if (getButton(0) != null && getButton(0).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.block.type").getFormattedText());
		} else if (getButton(1) != null && getButton(1).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.block.size").getFormattedText());
		} else if (getButton(2) != null && getButton(2).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.color").getFormattedText());
		} else if (getButton(3) != null && getButton(3).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.frame", "" + (frame.id + 1)).getFormattedText());
		} else if (getButton(4) != null && getButton(4).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.frame.add").getFormattedText());
		} else if (getButton(5) != null && getButton(5).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.frame.del").getFormattedText());
		} else if (getButton(6) != null && getButton(6).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.reset.frame")
					.appendSibling(new TextComponentTranslation("animation.hover.shift.0")).getFormattedText());
		} else if (getButton(7) != null && getButton(7).isMouseOver()) {
			if (isHitbox) {
				setHoverText(new TextComponentTranslation("animation.hover.hitbox.add").getFormattedText());
			} else if (isMotion) {
				setHoverText(new TextComponentTranslation("animation.hover.not.in.motion").getFormattedText());
			} else {
				setHoverText(new TextComponentTranslation("animation.hover.part.add").getFormattedText());
			}
		} else if (getButton(8) != null && getButton(8).isMouseOver()) {
			if (isHitbox) {
				setHoverText(new TextComponentTranslation("animation.hover.hitbox.del").getFormattedText());
			} else if (isMotion) {
				setHoverText(new TextComponentTranslation("animation.hover.not.in.motion").getFormattedText());
			} else {
				setHoverText(new TextComponentTranslation("animation.hover.part.del").getFormattedText());
			}
		} else if (getButton(9) != null && getButton(9).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.reset.part")
					.appendSibling(new TextComponentTranslation("animation.hover.shift.0")).getFormattedText());
		} else if (getButton(10) != null && getButton(10).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.part.disabled." + !part.isDisable())
					.appendSibling(new TextComponentTranslation("animation.hover.shift.1")).getFormattedText());
		} else if (getButton(11) != null && getButton(11).isMouseOver()) {
			setHoverText(
					new TextComponentTranslation("animation.hover.smooth." + frame.isSmooth()).getFormattedText());
		} else if (getButton(12) != null && getButton(12).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.part.color").getFormattedText());
		} else if (getButton(13) != null && getButton(13).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.mesh.0").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (getButton(14) != null && getButton(14).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.mesh.1").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (getButton(15) != null && getButton(15).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.mesh.2").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (getButton(16) != null && getButton(16).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.mesh.3").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (getButton(18) != null && getButton(17).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.hitbox").appendSibling(new TextComponentTranslation("animation.hover.again")).getFormattedText());
		} else if (getButton(18) != null && getButton(18).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.reset.scale").getFormattedText());
		} else if (getButton(19) != null && getButton(19).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.reset.pos").getFormattedText());
		} else if (getButton(20) != null && getButton(20).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.reset.rot").getFormattedText());
		} else if (getButton(21) != null && getButton(21).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.work." + onlyCurrentPart, ((char) 167) + "6" + (frame != null ? frame.id + 1 : -1)).getFormattedText());
		} else if (getButton(22) != null && getButton(22).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.part.show." + part.isShow()).appendSibling(new TextComponentTranslation("animation.hover.shift.1")).getFormattedText());
		} else if (getButton(23) != null && getButton(23).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.tool.0").getFormattedText());
		} else if (getButton(24) != null && getButton(24).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.tool.1").getFormattedText());
		} else if (getButton(25) != null && getButton(25).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.tool.2").getFormattedText());
		} else if (getButton(26) != null && getButton(26).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.show.armor").getFormattedText());
		} else if (getButton(27) != null && getButton(27).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.select.sound").getFormattedText());
		} else if (getButton(28) != null && getButton(28).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.del.sound").getFormattedText());
		} else if (getButton(29) != null && getButton(29).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.show.parts").getFormattedText());
		} else if (getButton(30) != null && getButton(30).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.reset." + toolType, "X").getFormattedText());
		} else if (getButton(31) != null && getButton(31).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.reset." + toolType, "Y").getFormattedText());
		} else if (getButton(32) != null && getButton(32).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.reset." + toolType, "Z").getFormattedText());
		} else if (getButton(35) != null && getButton(35).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.show.tools").getFormattedText());
		} else if (getButton(36) != null && getButton(36).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.now.attack").getFormattedText());
		} else if (getButton(37) != null && getButton(37).isMouseOver()) {
			int type = frame.getHoldRightStackType();
			if (type == 2) { type = 3; }
			else if (type == 3) { type = 4; }
			setHoverText(new TextComponentTranslation("animation.hover.stack.type."+type).getFormattedText());
		} else if (getButton(38) != null && getButton(38).isMouseOver()) {
			int type = frame.getHoldLeftStackType();
			if (type == 3) { type = 4; }
			setHoverText(new TextComponentTranslation("animation.hover.stack.type."+type).getFormattedText());
		} else if (getButton(39) != null && getButton(39).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.0").getFormattedText());
		} else if (getButton(40) != null && getButton(40).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.1").getFormattedText());
		} else if (getButton(41) != null && getButton(41).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.2").getFormattedText());
		} else if (getButton(42) != null && getButton(42).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.3").getFormattedText());
		} else if (getButton(43) != null && getButton(43).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.4").getFormattedText());
		} else if (getButton(44) != null && getButton(44).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.5").getFormattedText());
		} else if (getButton(46) != null && getButton(46).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.show.alpha").getFormattedText());
		} else if (getButton(47) != null && getButton(47).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.base.rot").getFormattedText());
		} else if (getButton(66) != null && getButton(66).isMouseOver()) {
			setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (getTextField(0) != null && getTextField(0).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.anim.repeat").getFormattedText());
		} else if (getTextField(1) != null && getTextField(1).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.part.ticks").getFormattedText());
		} else if (getTextField(2) != null && getTextField(2).isMouseOver()) {
			setHoverText(new TextComponentTranslation("animation.hover.part.delay").getFormattedText());
		} else if ((getTextField(5) != null && getTextField(5).isMouseOver())
				|| (getSlider(0) != null && getSlider(0).isMouseOver())) {
			setHoverText(new TextComponentTranslation(
					"animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"),
					"X").getFormattedText());
		} else if ((getTextField(6) != null && getTextField(6).isMouseOver())
				|| (getSlider(1) != null && getSlider(1).isMouseOver())) {
			setHoverText(new TextComponentTranslation(
					"animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"),
					"Y").getFormattedText());
		} else if ((getTextField(7) != null && getTextField(7).isMouseOver())
				|| (getSlider(2) != null && getSlider(2).isMouseOver())) {
			setHoverText(new TextComponentTranslation(
					"animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"),
					"Z").getFormattedText());
		}
		if (hoverText != null) {
			drawHoveringText(Arrays.asList(hoverText), mouseX, mouseY, fontRenderer);
			hoverText = null;
		}
	}

	@Override
	public void setMiniHoverText(int id, IComponentGui c) {
		if (id < 0) { return; }
		if (c instanceof GuiNpcSlider) {
			switch(((GuiNpcSlider) c).id) {
				case 0: {
					if (isHitbox || isMotion) { setHoverText(new TextComponentTranslation("animation.hover." + (toolType == 1 ? "offset.d" : "scale.hb"), "X").getFormattedText()); }
					else { setHoverText(new TextComponentTranslation("animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"), "X").getFormattedText()); }
					break;
				}
				case 1: {
					if (isHitbox || isMotion) { setHoverText(new TextComponentTranslation("animation.hover." + (toolType == 1 ? "offset.h" : "scale.hb"), "Y").getFormattedText()); }
					else { setHoverText(new TextComponentTranslation("animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"), "Y").getFormattedText()); }
					break;
				}
				case 2: {
					if (isHitbox || isMotion) { setHoverText(new TextComponentTranslation("animation.hover." + (toolType == 1 ? "offset.w" : "scale.hb"), "Z").getFormattedText()); }
					else { setHoverText(new TextComponentTranslation("animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"), "Z").getFormattedText()); }
					break; }
				case 3: { setHoverText(new TextComponentTranslation("animation.hover.rotation.x1").getFormattedText()); break; }
				case 4: { setHoverText(new TextComponentTranslation("animation.hover.rotation.y1").getFormattedText()); break; }
			}
		} else if (c instanceof GuiNpcTextField) {
			ITextComponent text = null;
			switch(((GuiNpcTextField) c).getId()) {
				case 5: {
					if (isHitbox || isMotion) { text = new TextComponentTranslation("animation.hover." + (toolType == 1 ? "offset.d" : "scale.hb"), "X"); }
					else { text = new TextComponentTranslation("animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"), "X"); }
					break;
				}
				case 6: {
					if (isHitbox || isMotion) { text = new TextComponentTranslation("animation.hover." + (toolType == 1 ? "offset.h" : "scale.hb"), "Y"); }
					else { text = new TextComponentTranslation("animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"), "Y"); }
					break;
				}
				case 7: {
					if (isHitbox || isMotion) { text = new TextComponentTranslation("animation.hover." + (toolType == 1 ? "offset.w" : "scale.hb"), "Z"); }
					else { text = new TextComponentTranslation("animation.hover." + (toolType == 0 ? "rotation" : toolType == 1 ? "offset" : "scale"), "Z"); }
					break; }
				case 8: { text = new TextComponentTranslation("animation.hover.rotation.x1"); break; }
				case 9: { text = new TextComponentTranslation("animation.hover.rotation.y1"); break; }
			}
			if (text != null) {
				text.appendSibling(new TextComponentTranslation("animation.hover.tab"));
				setHoverText(text.getFormattedText());
			}
		} else if (c instanceof GuiNpcButton) {
			setHoverText(new TextComponentTranslation("hover.default.set").getFormattedText());
		} else if (c instanceof GuiCustomScroll) {
			if (((GuiCustomScroll) c).hoversTexts != null  && ((GuiCustomScroll) c).hover >= 0 && ((GuiCustomScroll) c).hover < ((GuiCustomScroll) c).hoversTexts.length) {
				hoverText = ((GuiCustomScroll) c).hoversTexts[((GuiCustomScroll) c).hover];
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void drawWork(float partialTicks) {
		// work place
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, -300.0f);
		Gui.drawRect(workU + 1, workV + 1, workU + workS - 1, workV + workS - 1, GuiNpcAnimation.backColor);
		drawCenteredString(fontRenderer, ((char) 167) + "7" + anim.getSettingName(), workU + workS / 2, workV + 3, 0xFFFFFFFF);
		GlStateManager.popMatrix();

		// blocks
		GlStateManager.pushMatrix();
		postRender();
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
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
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
		Map<BlockPos, TileEntity> tiles = new HashMap<>();
		for (int y = -yH; y <= yH; y++) {
			for (int x = -blockSize; x <= blockSize; x++) {
				for (int z = -blockSize; z <= blockSize; z++) {
					BlockPos pos = new BlockPos(x, y + (blockSize == 0 ? 0 : 1), z);
					TileEntity tile = null;
					if (blockType == 0) {
						IBlockState s = environmentStates.get(pos);
						if (s != null) {
							state = s;
						}
						TileEntity t = environmentTiles.get(pos);
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
					mc.getBlockRendererDispatcher().renderBlockBrightness(state, 1.0f);
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
		mc.getRenderManager().playerViewY = 180.0f;
		EntityNPCInterface showNPC = getDisplayNpc();
		if (showHitBox) {
			GlStateManager.glLineWidth(1.0F);
			GlStateManager.disableTexture2D();
			RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(showNPC.width / -2.0d, 0.0d, showNPC.width / -2.0d, showNPC.width / 2.0d, showNPC.height, showNPC.width / 2.0d), 1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableTexture2D();
		}

		// Damage hitboxes
		if (anim.type == AnimationKind.ATTACKING && frame.isNowDamage() && !frame.damageHitboxes.isEmpty()) {
			GlStateManager.glLineWidth(2.0F);
			GlStateManager.disableTexture2D();
			int i = 0;
			for (AxisAlignedBB aabb : anim.getDamageHitboxes(showNPC, frame.id)) {
				float r = 0.75f;
				float g = 0.5f;
				float b = 0.5f;
				if (scrollHitboxes.selected == i) {
					r = 1.0f;
					g = 0.0f;
					b = 0.25f;
				} else if (scrollHitboxes.hover == i) {
					r = 0.875f;
					g = 0.0f;
					b = 0.875f;
				}
				aabb = aabb.offset(-showNPC.posX, -showNPC.posY, -showNPC.posZ);
				RenderGlobal.drawSelectionBoundingBox(aabb, r, g, b, 1.0f);

				Vec3d center = aabb.getCenter();
				for (int j = 0; j < 3; j++) {
					AnimationDamageHitbox adh = frame.damageHitboxes.get(i);
					double s = 0.1d;
					if (adh != null) {
						s *= adh.scale[j];
					}
					drawLine(center.x, center.y, center.z, ValueUtil.correctDouble(s, 0.025d, 0.25d), j, b, r, g);
				}

				if (scrollHitboxes.selected == i) {
					r = 0.5f;
					g = 0.5f;
					b = 0.5f;
					float s = 1.0f;
					if (tools.getSlider(0).isMouseOver() || tools.getTextField(5).hovered || tools.getTextField(5).isFocused() || tools.getButton(30).isMouseOver()) {
						r = 0.825f;
						g = 0.625f;
						b = 0.195f;
						s = 3.0f;
					}
					// distance
					drawLine(0.0f, 0.0f, 0.0f, center.x, 0.0f, center.z, s, r, g, b);

					r = 0.5f;
					g = 0.5f;
					b = 0.5f;
					s = 1.0f;
					if (tools.getSlider(1).isMouseOver() || tools.getTextField(6).hovered || tools.getTextField(6).isFocused() || tools.getButton(31).isMouseOver()) {
						r = 0.825f;
						g = 0.625f;
						b = 0.195f;
						s = 3.0f;
					}
					// height
					drawLine(center.x, 0.0f, center.z, center.x, center.y, center.z, s, r, g, b);

					// round radius
					r = 0.5f;
					g = 0.5f;
					b = 0.5f;
					s = 1.0f;
					if (tools.getSlider(2).isMouseOver() || tools.getTextField(7).hovered || tools.getTextField(7).isFocused() || tools.getButton(32).isMouseOver()) {
						r = 0.825f;
						g = 0.625f;
						b = 0.195f;
						s = 3.0f;
					}
					drawCircle(0.0d, 0.0d, 0.0d, Math.sqrt(Math.pow(center.x, 2.0d) + Math.pow(center.z, 2.0d)), 0, 64, s, r, g, b);
				}
				i++;
			}
			GlStateManager.enableTexture2D();
		}

		// Motion
		if (frame.motions[0] != 0.0f || frame.motions[1] != 0.0f || frame.motions[2] != 0.0f) {

			double radYaw = Math.toRadians(showNPC.rotationYaw) + frame.motions[2];

			double x = Math.sin(radYaw) * -frame.motions[0];
			double y = frame.motions[1];
			double z = Math.cos(radYaw) * frame.motions[0];

			float r = 0.5f;
			float g = 0.5f;
			float b = 0.75f;
			float s = 2.0f;
			if (isMotion) {
				b = 0.5f;
				if (tools.getSlider(0).isMouseOver() || tools.getTextField(5).hovered || tools.getTextField(5).isFocused() || tools.getButton(30).isMouseOver()) {
					r = 0.825f;
					g = 0.625f;
					b = 0.195f;
					s = 3.0f;
				}
				// distance
				drawLine(0.0f, 0.0f, 0.0f, x, 0.0f, z, s, r, g, b);

				r = 0.5f;
				g = 0.5f;
				b = 0.5f;
				s = 1.0f;
				if (tools.getSlider(1).isMouseOver() || tools.getTextField(6).hovered || tools.getTextField(6).isFocused() || tools.getButton(31).isMouseOver()) {
					r = 0.825f;
					g = 0.625f;
					b = 0.195f;
					s = 3.0f;
				}
				// height
				drawLine(x, 0.0f, z, x, y, z, s, r, g, b);

				// round radius
				r = 0.5f;
				g = 0.5f;
				b = 0.5f;
				s = 1.0f;
				if (tools.getSlider(2).isMouseOver() || tools.getTextField(7).hovered || tools.getTextField(7).isFocused() || tools.getButton(32).isMouseOver()) {
					r = 0.825f;
					g = 0.625f;
					b = 0.195f;
					s = 3.0f;
				}
				drawCircle(0.0d, 0.0d, 0.0d, Math.sqrt(Math.pow(x, 2.0d) + Math.pow(z, 2.0d)), 0, 64, s, r, g, b);

				r = 0.25f;
				g = 0.25f;
				b = 0.875f;
				s = 3.0f;
			}
			double radius = Math.sqrt(Math.pow(x, 2.0d) + Math.pow(y, 2.0d) + Math.pow(z, 2.0d));

			GlStateManager.pushMatrix();
			GlStateManager.rotate((float) Math.toDegrees(-radYaw), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate((float) Math.toDegrees(-Math.atan2(frame.motions[1], frame.motions[0])), 1.0f, 0.0f, 0.0f);
			drawArrow(radius, s, r, g, b);

			GlStateManager.popMatrix();
		}

		// model mesh rotation axes:
		/*double xN = 0.0d;
		double yN = 0.0d;
		double zN = 0.0d;
		if (isHitbox) {}
		else {
			ModelNpcAlt model = (ModelNpcAlt) ((RenderLiving<?>) Objects.requireNonNull(mc.getRenderManager().getEntityRenderObject(showNPC))).getMainModel();
			GlStateManager.pushMatrix();
			model.bipedBody.postRender(0.0625f);
			for (int i = 0; i < 3; i++) { drawLine(xN, yN, zN, 0.5d / dispScale, i, 0.5f, 0.5f, 0.5f); }
			GlStateManager.popMatrix();
		}*/



		ModelNpcAlt.editAnimDataSelect.displayNpc = showNPC;
		mc.getRenderManager().renderEntity(showNPC, 0.0, 0.0, 0.0, 0.0f, showNPC.rotationYaw != 0.0f ? 1.0f : 0.0f, false);


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
				mc.getRenderManager().renderEntity(e, e.posX, e.posY, e.posZ, 0.0f, 0.0f, false);
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
		return onlyCurrentPart ? npcPart : npcAnim;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (frame.id < 0) {
			frame = anim.frames.get(0);
			if (frame.id < 0) { frame.id = 0; }
		}
		if (hitbox.id < 0) {
			hitbox = frame.damageHitboxes.get(0);
			if (hitbox.id < 0) { hitbox.id = 0; }
		}
		ScaledResolution sw = new ScaledResolution(mc);
		if (w > 0.0d && h > 0.0d) {
			if (partNames != null) {
				double left = 1.0d / w * partNames.guiLeft;
				double top = 1.0d / h * partNames.guiTop;
				partNames.guiLeft = (int) ((double) sw.getScaledWidth() * left);
				partNames.guiTop = (int) ((double) sw.getScaledHeight() * top);
				scrollParts.guiLeft = partNames.guiLeft + 4;
				scrollParts.guiTop = partNames.guiTop + 12;
			}
			if (tools != null) {
				double left = 1.0d / w * tools.guiLeft;
				double top = 1.0d / h * tools.guiTop;
				tools.guiLeft = (int) ((double) sw.getScaledWidth() * left);
				tools.guiTop = (int) ((double) sw.getScaledHeight() * top);
			}
		}
		w  = sw.getScaledWidth();
		h = sw.getScaledHeight();

		if ((sw.getScaledWidth() - 144) < sw.getScaledHeight() - 8) {
			workS = sw.getScaledWidth() - 144;
			winU = 0;
			winW = sw.getScaledWidth();
			winV = (sw.getScaledHeight() - workS - 8) / 2;
			winH = workS + 8;

		}
		else {
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

		// common to settings
		addLabel(new GuiNpcLabel(lId++, ((char) 167) + "n" + ((char) 167) + "l?", workU - 6, workV, 0xFF000000));

		// name
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.name").getFormattedText() + ":", x, y - 10));
		textField = new GuiNpcTextField(11, this, x, y, 125, 12, anim.name);
		addTextField(textField);

		// work place
		addLabel(new GuiNpcLabel(lId++, "animation.place", x, (y += 26) - 10));
		addButton(new GuiButtonBiDirectional(0, x, y, 105, 10, blockNames, blockType));
		button = new GuiNpcButton(1, x + 107, y, 17, 10, blockSizes, blockSize);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		button = new GuiNpcButton(2, workU + 2, workV + 23, 8, 8, "");
		button.layerColor = (GuiNpcAnimation.backColor == 0xFF000000 ? 0xFF00FFFF : 0xFF008080);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		button = new GuiNpcButton(26, workU + 2, workV + 31, 8, 8, "");
		button.layerColor = (ModelNpcAlt.editAnimDataSelect.showArmor ? 0xFFFF7200 : 0xFF6F3200);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		button = new GuiNpcButton(46, workU + 2, workV + 39, 8, 8, "");
		button.layerColor = (ModelNpcAlt.editAnimDataSelect.alpha >= 1.0f ? 0xFFFFFEBF : 0xFF787758);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		button = new GuiNpcButton(47, workU + 2, workV + 47, 8, 8, "");
		button.layerColor = (baseRotation == 0.0f || baseRotation == npcAnim.rotationYaw ? 0xFF96FFC0 : 0xFF426C53);
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		// frame
		GuiNpcLabel label = new GuiNpcLabel(lId++, "animation.frames", x, (y += 23) - 10);
		addLabel(label);
		List<String> lFrames = new ArrayList<>();
		for (int i = 0; i < anim.frames.size(); i++) { lFrames.add((i + 1) + "/" + anim.frames.size()); }
		addButton(new GuiButtonBiDirectional(3, x, y, 60, 10, lFrames.toArray(new String[0]), frame.id));
		// add frame
		button = new GuiNpcButton(4, x + label.width + 2, y - 10, 10, 10, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		// del frame
		button = new GuiNpcButton(5, x + label.width + 12, y - 10, 10, 10, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = anim.frames.size() > 1;
		addButton(button);
		// frame smooth animated
		addButton(new GuiNpcCheckBox(11, x + 62, y - 2, 74, 12, "gui.smooth", "gui.linearly", frame.isSmooth()));
		// clear frame
		button = new GuiNpcButton(6, x + 126, y - 10, 10, 10, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		// frame times
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.time").getFormattedText() + ":", x, (y += 12) + 2));
		textField = new GuiNpcTextField(1, this, x + 35, y, 48, 12, "" + frame.getSpeed());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 3600, frame.getSpeed());
		addTextField(textField);
		textField = new GuiNpcTextField(2, this, x + 87, y, 48, 12, "" + frame.getEndDelay());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 3600, frame.getEndDelay());
		addTextField(textField);
		// frame repeat
		hasExtend = false;
		if (anim.type == AnimationKind.DIES || anim.type == AnimationKind.JUMP) {
			hasExtend = true;
			addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.repeat").getFormattedText() + ":", x, (y += 14) + 2));
			if (anim.repeatLast < 0) { anim.repeatLast *= -1; }
			if (anim.repeatLast > anim.frames.size()) { anim.repeatLast = anim.frames.size(); }
			textField = new GuiNpcTextField(0, this, x + 87, y, 48, 12, "" + anim.repeatLast);
			textField.setNumbersOnly();
			textField.setMinMaxDefault(0, anim.frames.size(), anim.repeatLast);
			addTextField(textField);
		}
		// has damage hitbox
		if (anim.type == AnimationKind.ATTACKING) {
			hasExtend = true;
			addButton(new GuiNpcCheckBox(36, x, y += 14, 136, 12, "animation.now.attack", "animation.notyet.attack", frame.isNowDamage()));
		}
		// stack show
		addLabel(new GuiNpcLabel(lId++, "animation.hold.right", x + 20, y += 13));
		button = new GuiNpcButton(37, x + 20, y += 9, 116, 10, new String[] { "animation.stack.type.0", "animation.stack.type.1", "animation.stack.type.3", "animation.stack.type.4", "animation.stack.type.5", "animation.stack.type.6", "animation.stack.type.7", "animation.stack.type.8" }, frame.getHoldRightStackType());
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);
		addLabel(new GuiNpcLabel(lId++, "animation.hold.left", x + 20, y += 10));
		button = new GuiNpcButton(38, x + 20, y += 9, 116, 10, new String[] { "animation.stack.type.0", "animation.stack.type.1", "animation.stack.type.2", "animation.stack.type.4", "animation.stack.type.5", "animation.stack.type.6", "animation.stack.type.7", "animation.stack.type.8" }, frame.getHoldLeftStackType());
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		// Part
		label = new GuiNpcLabel(lId++, isMotion ? "animation.motion" :  isHitbox ? "animation.hitbox" : "animation.parts", x, y += 13);
		addLabel(label);
		// show part names
		button = new GuiNpcButton(29, workU + 2, y, 8, 8, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 232;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = partNames == null || !partNames.visible;
		button.layerColor = CustomNpcs.colorAnimHoverPart + 0xFF000000;
		addButton(button);
		// show hitbox names
		if (anim.type == AnimationKind.ATTACKING) {
			button = new GuiNpcButton(45, workU + 2, y + 12, 8, 8, "");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrX = 232;
			button.txrW = 24;
			button.txrH = 24;
			button.enabled = hitboxes == null || !hitboxes.visible;
			button.layerColor = 0xFFFAF700;
			addButton(button);
		}

		// scrolls data set
		if (scrollParts == null) { (scrollParts = new GuiCustomScroll(this, 0)).setSize(67, 112); }
		dataParts.clear();
		List<String> lParts = new ArrayList<>();
		for (int id : frame.parts.keySet()) {
			PartConfig ps = frame.parts.get(id);
			String key = new TextComponentTranslation(ps.name).getFormattedText();
			dataParts.put(key, ps);
			lParts.add(key);
		}
		scrollParts.setListNotSorted(lParts);
		dataHitboxes.clear();
		if (scrollHitboxes == null) { (scrollHitboxes = new GuiCustomScroll(this, 1)).setSize(112, 112); }
		List<String> lHitboxes = new ArrayList<>();
		scrollHitboxes.hoversTexts = new String[frame.damageHitboxes.size()][];
		int i = 0;
		char c = (char) 167;
		for (int id : frame.damageHitboxes.keySet()) {
			AnimationDamageHitbox aDH = frame.damageHitboxes.get(id);
            String key = aDH.getKey();
			dataHitboxes.put(key, aDH);
			lHitboxes.add(key);
			scrollHitboxes.hoversTexts[i] = new String[] {
					c + "7ID: " + c + "6" + (id + 1),
					c + "7D:" + c + "a" + aDH.offset[0],
					c + "7H:" + c + "a" + aDH.offset[1],
					c + "7W:" + c + "a" + aDH.offset[2],
					c + "7Scale X:" + c + "b" + aDH.scale[0],
					c + "7Scale Y:" + c + "b" + aDH.scale[1],
					c + "7Scale Z:" + c + "b" + aDH.scale[2]
			};
			i++;
		}
		scrollHitboxes.setListNotSorted(lHitboxes);
		if (isHitbox && toolType == 0) { toolType = 1; }
		// add part / hitbox
		button = new GuiNpcButton(7, x + label.width + 2, y, 10, 10, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		// del part / hitbox
		button = new GuiNpcButton(8, x + label.width + 12, y, 10, 10, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 72;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		// clear part
		button = new GuiNpcButton(9, x + 126, y, 10, 10, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 120;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = !isMotion && !isHitbox;
		addButton(button);
		// used part in frame
		button = new GuiNpcCheckBox(10, x, y += 10, 67, 14, "gui.disabled", "gui.enabled", part.isDisable());
		button.enabled = !isMotion && !isHitbox;
		addButton(button);
		// show part in frame
		button = new GuiNpcCheckBox(22, x + 69, y, 67, 14, "gui.show", "gui.noshow", part.isShow());
		button.enabled = !isMotion && !isHitbox;
		addButton(button);
		// display color hover
		StringBuilder color = new StringBuilder(Integer.toHexString(CustomNpcs.colorAnimHoverPart));
		while (color.length() < 6) { color.insert(0, "0"); }
		button = new GuiNpcButton(12, x, y += 15, 67, 10, color.toString());
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		button.setTextColor(CustomNpcs.colorAnimHoverPart);
		button.dropShadow = false;
		button.enabled = !isMotion && !isHitbox;
		addButton(button);

		// Chance
		float ch = Math.round(anim.chance * 100000.0f) / 1000.0f;
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("drop.chance").getFormattedText()+":", x, (y += 14) + 1));
		textField = new GuiNpcTextField(10, this, x + 28, y, 40, 12, String.valueOf(ch));
		textField.setDoubleNumbersOnly();
		textField.setMinMaxDoubleDefault(0.0f, 100.0f, ch);
		addTextField(textField);
		addLabel(new GuiNpcLabel(lId++, "%", x + 72, y + 1));
		// Sound Settings
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("advanced.sounds").getFormattedText()+":", x, y += 16));
		textField = new GuiNpcTextField(3, this, x, y + 10, 135, 12, frame.getStartSound());
		addTextField(textField);
		button = new GuiNpcButton(27, x + textField.width - 17, y, 8, 8, "S");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		button.dropShadow = false;
		button.setTextColor(0xFFDC0000);
		addButton(button);
		button = new GuiNpcButton(28, x + textField.width - 8, y, 8, 8, "X");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		button.dropShadow = false;
		button.setTextColor(0xFFDC0000);
		addButton(button);

		// Emotion data
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("advanced.emotion").getFormattedText()+":", x, y += 26));
		textField = new GuiNpcTextField(4, this, x, y + 10, 48, 12, "" + frame.getStartEmotion());
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, AnimationController.getInstance().getUnusedEmtnId() - 1, frame.getStartEmotion());
		addTextField(textField);

		// Equipment
		addLabel(new GuiNpcLabel(lId, "animation.show.stacks", x, y += 26));
		button = new GuiNpcButton(39, x + 2, y += 10, 12, 12, ""); // main hand
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrY = frame.showMainHand ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		button = new GuiNpcButton(40, x + 16, y, 12, 12, ""); // offhand
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 24;
		button.txrY = frame.showOffHand ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		button = new GuiNpcButton(41, x + 30, y, 12, 12, ""); // helmet
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 48;
		button.txrY = frame.showHelmet ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		button = new GuiNpcButton(42, x + 44, y, 12, 12, ""); // body
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 72;
		button.txrY = frame.showBody ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		button = new GuiNpcButton(43, x + 58, y, 12, 12, ""); // legs
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 96;
		button.txrY = frame.showLegs ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);
		button = new GuiNpcButton(44, x + 72, y, 12, 12, ""); // feet's
		button.texture = ANIMATION_BUTTONS_SLOTS;
		button.hasDefBack = false;
		button.txrX = 120;
		button.txrY = frame.showFeets ? 0 : 96;
		button.txrW = 24;
		button.txrH = 24;
		addButton(button);

		// Exit
		button = new GuiNpcButton(66, x, winV + winH - 12, 50, 10, "gui.back"); // back
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		// work
		button = new GuiNpcButton(13, workU + 25, workV + 2, 8, 8, ""); // simple mesh
		button.layerColor = meshType == 0 ? 0xFFD93070 : 0xFF360C1C;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);
		button = new GuiNpcButton(14, workU + 34, workV + 2, 8, 8, ""); // xz mesh
		button.layerColor = meshType == 1 ? 0xFF6830D9 : 0xFF1A0C36;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);
		button = new GuiNpcButton(15, workU + 43, workV + 2, 8, 8, ""); // xY mesh
		button.layerColor = meshType == 2 ? 0xFF30D980 : 0xFF0C3620;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);
		button = new GuiNpcButton(16, workU + 52, workV + 2, 8, 8, ""); // zy mesh
		button.layerColor = meshType == 3 ? 0xFFD7D930 : 0xFF35360C;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);
		button = new GuiNpcButton(17, workU + 61, workV + 2, 8, 8, ""); // show hit box
		button.layerColor = showHitBox ? 0 : 0xFF808080;
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);

		button = new GuiNpcButton(18, workU + workS - 10, workV + 2, 8, 8, ""); // reset scale
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);
		button = new GuiNpcButton(19, workU + 2, workV + workS - 10, 8, 8, ""); // reset pos
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);
		button = new GuiNpcButton(20, workU + workS - 10, workV + workS - 10, 8, 8, ""); // reset rot
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrY = 96;
		addButton(button);
		button = new GuiNpcButton(21, workU + workS / 2 - 11, workV + workS - 12, 18, 10, ""); // part or anim
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = onlyCurrentPart ? 144 : 188;
		button.txrW = 44;
		button.txrH = 24;
		addButton(button);

		y = workV + workS - 74;
		// show tools
		button = new GuiNpcButton(35, workU + 5, y, 8, 8, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 232;
		button.txrW = 24;
		button.txrH = 24;
		button.enabled = tools == null || !tools.visible;
		addButton(button);
		// tool pos
		button = new GuiNpcButton(23, workU + 2, y += 10, 14, 14, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 1 ? 0xFFFF8080 : 0xFFFFFFFF;
		addButton(button);
		// tool rot
		button = new GuiNpcButton(24, workU + 2, y += 16, 14, 14, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 24;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 0 ? 0xFF80FF80 : 0xFFFFFFFF;
		button.enabled = !isHitbox && !isMotion;
		addButton(button);
		// tool scale
		button = new GuiNpcButton(25, workU + 2, y + 16, 14, 14, "");
		button.texture = ANIMATION_BUTTONS;
		button.hasDefBack = false;
		button.isAnim = true;
		button.txrX = 48;
		button.txrW = 24;
		button.txrH = 24;
		button.layerColor = toolType == 2 ? 0xFF8080FF : 0xFFFFFFFF;
		button.enabled = !isMotion;
		addButton(button);

		resetAnimation();

		// Parts window
		boolean vPN = partNames == null || partNames.visible;
		showPartNames();
		partNames.visible = vPN;
		partNames.objs = new Object[] { part, toolType };
		// Tool window
		boolean vT = tools == null || tools.visible;
		showTools();
		tools.visible = vT;
		if (anim.type == AnimationKind.ATTACKING) {
			boolean vH = hitboxes == null || hitboxes.visible;
			showHitBoxes();
			hitboxes.visible = vH;
		}

		if (ModelNpcAlt.editAnimDataSelect.part != (part == null ? null : part.getEnumType())) { setPart(part); }
	}

	private void showHitBoxes() {
		if (hitboxes == null) {
			hitboxes = new GuiNpcMiniWindow(this, 2, workU + 18, workV + 12, 120, 118, new TextComponentTranslation("gui.hitboxes", ":").getFormattedText());
			hitboxes.widthTexture = 256;
			hitboxes.heightTexture = 256;
			hitboxes.setColorLine(0xFAF700);
			hitboxes.addScroll(scrollHitboxes);
		}
		hitboxes.setPoint(getButton(45));
		scrollHitboxes.guiLeft = hitboxes.guiLeft + 4;
		scrollHitboxes.guiTop = hitboxes.guiTop + 12;
		addMiniWindow(hitboxes);
	}

	private void showTools() {
		int f = 11, h = 0;
		int x = workU + 18;
		int y = workV + workS - 65;
		GuiNpcTextField textField;
		GuiNpcButton button;
		boolean notNormal = toolType == 0 && part != null &&
				((addedPartConfig != null && !addedPartConfig.isNormal) || (part.id >= 1 && part.id <= 5));
		if (isHitbox) { notNormal = false; }
		y += notNormal ? -11 : 0;
		if (tools != null) {
			x = tools.guiLeft;
			y = tools.guiTop;
			h = tools.ySize;
		}
		tools = new GuiNpcMiniWindow(this, 1, x, y, 146, notNormal ? 60 : 38, new TextComponentTranslation("gui.tools").getFormattedText() + ":");
		tools.widthTexture = 256;
		tools.heightTexture = 256;

		x += 4;
		y += 13;
		for (int i = 0; i < 3; i++) {
			tools.addLabel(new GuiNpcLabel(i, i == 0 ? (isHitbox || isMotion ? "D:" : "X:") : i == 1 ? (isHitbox || isMotion ? "H:" : "Y:") : (isHitbox || isMotion ? "W:" : "Z:"), x, y + i * f));
			float[] values;
			if (isHitbox && hitbox != null) {
				values = toolType == 1 ? hitbox.offset : hitbox.scale;
			}
			else if (isMotion && frame != null) {
				toolType = 1;
				values = frame.motions;
			}
			else { values = toolType == 0 ? part.rotation : toolType == 1 ? part.offset : part.scale; }
			float[] textToFields = new float[values.length];
			float[] sliderValues = new float[values.length];
			switch (toolType) {
				case 1: { // o
					if (i == 2 && (isHitbox || isMotion)) {
						textToFields[i] = Math.round(values[i] * 180000.0f / Math.PI) / 1000.0f;
						sliderValues[i] = ValueUtil.correctFloat(values[i] * 0.159155f + 0.5f, 0.0f, 1.0f);
					} else {
						textToFields[i] = Math.round(values[i] * 1000.0f) / 1000.0f;
						if (isMotion) {
							if (i == 0) {
								sliderValues[i] = ValueUtil.correctFloat(values[i] * 2.0f / 3.0f, 0.0f, 1.0f);
							} else {
								sliderValues[i] = ValueUtil.correctFloat(values[i] / 3.0f + 0.5f, 0.0f, 1.0f);
							}
						} else if (isHitbox) {
							if (i == 0) {
								sliderValues[i] = ValueUtil.correctFloat(values[i] * 0.1f, 0.0f, 1.0f);
							} else {
								sliderValues[i] = ValueUtil.correctFloat(values[i] * 0.5f + 0.5f, 0.0f, 1.0f);
							}
						} else {
							sliderValues[i] = ValueUtil.correctFloat(values[i] * 0.1f + 0.5f, 0.0f, 1.0f);
						}
					}
					break;
				}
				case 2: { // s
					textToFields[i] = Math.round(values[i] * 1000.0f) / 1000.0f;
					sliderValues[i] = ValueUtil.correctFloat(values[i] * 0.2f, 0.0f, 1.0f);
					break;
				}
				default: { // r
					textToFields[i] = Math.round(values[i] * 180000.0f / Math.PI) / 1000.0f;
					sliderValues[i] = ValueUtil.correctFloat(values[i] * 0.159155f + 0.5f, 0.0f, 1.0f);
					break;
				}
			}
			tools.addSlider(new GuiNpcSlider(tools, i, x + 9, y + i * f, 75, 8, sliderValues[i]));
			textField = new GuiNpcTextField(i + 5, tools, x + 86, y + i * f, 42, 8, "" + textToFields[i]);
			textField.setDoubleNumbersOnly();
			double m = -180.0d, n = 180.0d;
			if (toolType == 1) {
				if (isMotion) { // D H W
					if (i == 0) {
						m = 0.0d;
						n = 1.5d;
					} if (i == 1) {
						m = -1.5d;
						n = 1.5d;
					}
				}
				else if (isHitbox) { // D H W
					if (i == 0) {
						m = 0.0d;
						n = 10.0d;
					} if (i == 1) {
						m = -10.0d;
						n = 10.0d;
					}
				}
				else {
					m = -5.0d;
					n = 5.0d;
				}
			}
			else if (toolType == 2) {
				m = 0.0d;
				n = 5.0d;
			}
            textField.setMinMaxDoubleDefault(m, n, textToFields[i]);
            tools.addTextField(textField);
			button = new GuiNpcButton(30 + i, x + 130, y + i * f, 8, 8, "X");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			tools.addButton(button);
		}
		if (!isHitbox && notNormal) {
			y += 33;
			tools.addLabel(new GuiNpcLabel(3, "X1:", x, y));
			float sliderValue = part.rotation[3] * 0.159155f + 0.5f;
			float textToFields = Math.round(part.rotation[3] * 180000.0f / Math.PI) / 1000.0f;
			tools.addSlider(new GuiNpcSlider(tools, 3, x + 9, y , 75, 8, sliderValue));
			textField = new GuiNpcTextField(8, tools, x + 86, y, 42, 8, "" + textToFields);
			textField.setDoubleNumbersOnly();
			textField.setMinMaxDoubleDefault(-180.0d, 180.0d, textToFields);
			tools.addTextField(textField);
			button = new GuiNpcButton(33, x + 130, y, 8, 8, "X");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			tools.addButton(button);

			y += 11;
			tools.addLabel(new GuiNpcLabel(4, "Y1:", x, y));
			sliderValue = part.rotation[4] * 0.159155f + 0.5f;
			textToFields = Math.round(part.rotation[4] * 180000.0f / Math.PI) / 1000.0f;
			tools.addSlider(new GuiNpcSlider(tools, 4, x + 9, y , 75, 8, sliderValue));
			textField = new GuiNpcTextField(9, tools, x + 86, y, 42, 8, "" + textToFields);
			textField.setDoubleNumbersOnly();
			textField.setMinMaxDoubleDefault(-90.0d, 90.0d, textToFields);
			tools.addTextField(textField);
			button = new GuiNpcButton(34, x + 130, y, 8, 8, "X");
			button.texture = ANIMATION_BUTTONS;
			button.hasDefBack = false;
			button.isAnim = true;
			button.txrY = 96;
			button.dropShadow = false;
			button.setTextColor(0xFFDC0000);
			tools.addButton(button);
			if (h != 72) { tools.moveOffset(0, -11); }
		}
		else if (h != 50) { tools.moveOffset(0, 11); }
		switch(toolType) {
			case 1: {
				tools.setPoint(getButton(23));
				tools.setColorLine(0xFF8080);
				break;
			}
			case 2: {
				tools.setPoint(getButton(25));
				tools.setColorLine(0x8080FF);
				break;
			}
			default: {
				tools.setPoint(getButton(24));
				tools.setColorLine(0x80FF80);
				break;
			}
		}
		if (getButton(35) != null) { getButton(35).layerColor = tools.getColorLine() + 0xFF000000; }
		addMiniWindow(tools);
	}

	private void showPartNames() {
		if (partNames == null) {
			partNames = new GuiNpcMiniWindow(this, 0, workU + workS - 78, workV + 12, 75, 129, new TextComponentTranslation("gui.parts").getFormattedText() + ":");
			partNames.widthTexture = 256;
			partNames.heightTexture = 256;
			partNames.setColorLine(CustomNpcs.colorAnimHoverPart);

			scrollParts.selected = part.id;
			partNames.addScroll(scrollParts);

			partNames.addButton(new GuiNpcButton(48, partNames.guiLeft + 4, partNames.guiTop + 125, 67, 12, "ai.movement"));
		}
		partNames.setPoint(getButton(29));
		if (scrollParts != null) {
			scrollParts.guiLeft = partNames.guiLeft + 4;
			scrollParts.guiTop = partNames.guiTop + 12;
		}
		if (partNames.getButton(48) != null) {
			partNames.getButton(48).x = partNames.guiLeft + 4;
			partNames.getButton(48).y = partNames.guiTop + 125;
		}
		addMiniWindow(partNames);
	}

	private boolean isPressAltAndKey(int id) {
		if (waitKey > 0 && waitKeyID == id) { return false; }
		boolean isPress = isAltKeyDown() && Keyboard.isKeyDown(id);
		if (isPress) {
			waitKey = 30;
			waitKeyID = id;
		}
		return isPress;
	}

	@Override
	public void keyTyped(char c, int i) {
		if (subgui == null) {
			if (i == 1) {
				close();
				return;
			}
			// tool pos - Alt + Q
			if (isPressAltAndKey(16) && toolType != 1) {
				toolType = 1;
				playButtonClick();
				initGui();
			}
			// tool rot - Alt + W
			if (isPressAltAndKey(17) && toolType != 0) {
				toolType = 0;
				playButtonClick();
				initGui();
			}
			// tool rot - Alt + E
			if (isPressAltAndKey(18) && toolType != 2) {
				toolType = 2;
				playButtonClick();
				initGui();
			}
			// play stop - Alt + P
			if (isPressAltAndKey(25)) {
				onlyCurrentPart = !onlyCurrentPart;
				if (getButton(21) != null) { getButton(21).txrX = onlyCurrentPart ? 144 : 188; }
				playButtonClick();
				isChanged = true;
				initGui();
			}
			// reset scale - Alt + S
			if (isPressAltAndKey(31)) {
				dispScale = 1.0f;
				playButtonClick();
			}
			// reset rot - Alt + R
			if (isPressAltAndKey(19)) {
				dispRot[0] = 45.0f;
				dispRot[1] = 345.0f;
				dispRot[2] = 345.0f;
				playButtonClick();
			}
			// reset pos - Alt + O
			if (isPressAltAndKey(24)) {
				for (int j = 0; j < 3; j++) {
					dispPos[j] = 0.0f;
				}
				playButtonClick();
			}
		}
		super.keyTyped(c, i);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (subgui != null) {
			subgui.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (hoverMiniWin) {
			return;
		}
		if (hoverRight && frame.getHoldRightStackType() == 3) {
			IItemStack stack;
			switch(frame.getHoldRightStackType()) {
				case 1: stack = npc.inventory.getProjectile(); break;
				case 2: stack = npc.inventory.getLeftHand(); break;
				case 3: stack = frame.getHoldRightStack(); break;
				default: stack = npc.inventory.getRightHand(); break;
			}
			setSubGui(new SubGuiSelectItemStack(0, stack==null || stack.isEmpty() ? ItemStack.EMPTY : stack.getMCItemStack()));
		}
		else if (hoverLeft && frame.getHoldLeftStackType() == 3) {
			IItemStack stack;
			switch(frame.getHoldLeftStackType()) {
				case 1: stack = npc.inventory.getProjectile(); break;
				case 2: stack = npc.inventory.getRightHand(); break;
				case 3: stack = frame.getHoldLeftStack(); break;
				default: stack = npc.inventory.getLeftHand(); break;
			}
			setSubGui(new SubGuiSelectItemStack(1, stack==null || stack.isEmpty() ? ItemStack.EMPTY : stack.getMCItemStack()));
		}
		else if ((mouseButton == 0 || mouseButton == 1) && hovered) {
			mousePressId = mouseButton;
			mousePressX = mouseX;
			mousePressY = mouseY;
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		isChanged = true;
		float value = 0.0f;
		float pi = (float) Math.PI;
		if (isHitbox) {
			if (hitbox == null || toolType == 0) { return; }
			if (toolType == 1) {
				if (slider.id == 0) {
					hitbox.offset[0] = 10.0f * slider.sliderValue;
					value = Math.round(hitbox.offset[0] * 1000.0f) / 1000.0f;
				}
				else if (slider.id == 1) {
					hitbox.offset[1] = 20.0f * slider.sliderValue - 10.0f;
					value = Math.round(hitbox.offset[1] * 1000.0f) / 1000.0f;
				}
				else if (slider.id == 2) {
					hitbox.offset[2] = 2.0f * pi * slider.sliderValue - pi;
					value = Math.round(360.0f * slider.sliderValue - 180.0f);
				}
			}
			else {
				value = Math.round(5000.0f * slider.sliderValue) / 1000.0f;
				hitbox.scale[slider.id] = value;
			}
			if (tools.getTextField(5 + slider.id) != null) { tools.getTextField(5 + slider.id).setText("" + value); }
			return;
		}
		if (isMotion) {
			if (toolType != 1) { return; }
			if (slider.id == 0) {
				value = Math.round(slider.sliderValue * 1500.0f) / 1000.0f;
				frame.motions[0] = value;
			} else if (slider.id == 1) {
				value = Math.round((slider.sliderValue * 3.0f - 1.5f) * 1000.0f) / 1000.0f;
				frame.motions[1] = value;
			} else if (slider.id == 2) {
				value = Math.round(360.0f * slider.sliderValue - 180.0f);
				frame.motions[2] = 2.0f * pi * slider.sliderValue - pi;
			}
			if (tools.getTextField(5 + slider.id) != null) { tools.getTextField(5 + slider.id).setText("" + value); }
			return;
		}
		if (part == null) { return; }
		if (slider.id == 3 || slider.id == 4) {
			if (toolType != 0) { return; }
			part.rotation[slider.id] = (2.0f * pi * slider.sliderValue - pi) / (slider.id == 4 ? 2.0f : 1.0f);
			value = Math.round(360.0f * slider.sliderValue - 180.0f) / (slider.id == 4 ? 2.0f : 1.0f);
		} else {
			switch (toolType) {
				case 0: { // r
					part.rotation[slider.id] = 2.0f * pi * slider.sliderValue - pi;
					value = Math.round(360.0f * slider.sliderValue - 180.0f);
					break;
				}
				case 1: { // o
					part.offset[slider.id] = slider.sliderValue * 10.0f - 5.0f;
					value = Math.round(part.offset[slider.id] * 1000.0f) / 1000.0f;
					break;
				}
				case 2: { // s
					part.scale[slider.id] = slider.sliderValue * 5.0f;
					value = Math.round(part.scale[slider.id] * 1000.0f) / 1000.0f;
					break;
				}
			}
		}
		if (tools.getTextField(5 + slider.id) != null) { tools.getTextField(5 + slider.id).setText("" + value); }
		resetAnimation();
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	private void playButtonClick() {
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	private void postRender() {
		GlStateManager.translate(workU + workS / 2.0f, workV + workS / 2.0f, 100.0f * dispScale); // center
		GlStateManager.translate(dispPos[0], dispPos[1], 0.0f);
		GlStateManager.rotate(dispRot[0], 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(dispRot[1], 1.0f, 0.0f, 0.0f);
		GlStateManager.rotate(dispRot[2], 0.0f, 0.0f, 1.0f);
		GlStateManager.scale(dispScale, dispScale, dispScale);
		GlStateManager.translate(0.0f, 25.0f, 0.0f);
	}

	private void resetAnimation() {
		if (!isChanged || anim == null || frame == null || npcAnim == null) {
			return;
		}
		npcAnim.posX = basePos[0];
		npcAnim.posY = basePos[1];
		npcAnim.posZ = basePos[2];
		npcPart.posX = basePos[0];
		npcPart.posY = basePos[1];
		npcPart.posZ = basePos[2];

		AnimationConfig ac = anim.copy();

		npcAnim.animation.reset();
		npcAnim.animation.tryRunAnimation(ac, AnimationKind.EDITING_All);
		npcAnim.setHealth(npcAnim.getMaxHealth());
		npcAnim.deathTime = 0;

		ac = anim.copy();
		ac.frames.clear();
		ac.frames.put(0, frame);

		npcPart.animation.reset();
		npcPart.animation.tryRunAnimation(ac, AnimationKind.EDITING_PART);
		npcPart.setHealth(npcPart.getMaxHealth());
		npcPart.deathTime = 0;
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		GuiNpcTextField.unfocus();
		 if (isMotion) {
			 isMotion = false;
			 partNames.getButton(48).enabled = true;
		 }
		 if (scroll.id == 0) {
			 if (anim.type == AnimationKind.ATTACKING) { setHitbox(null); }
			 if (part.id == dataParts.get(scroll.getSelected()).id) { return; }
			 setPart(dataParts.get(scroll.getSelected()));
			 initGui();
		 }
		 else if (scroll.id == 1 && anim.type == AnimationKind.ATTACKING) {
			 setHitbox(dataHitboxes.get(scroll.getSelected()));
			 if (scrollParts != null) { scrollParts.selected = -1; }
			 initGui();
		 }
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	private void setEnvironment() {
		environmentEntitys.clear();
		environmentStates.clear();
		environmentTiles.clear();
		if (npc == null || npc.world == null) {
			return;
		}
		offsetY = 0.0f;
		if (npc.posY != Math.floor(npc.posY)) {
			offsetY = (float) ((npc.posY - Math.round(npc.posY)) * 16.0f);
		}
		for (int y = -4; y <= 4; y++) {
			for (int x = -4; x <= 4; x++) {
				for (int z = -4; z <= 4; z++) {
					double yP = npc.posY + y - 1;
					if (npc.posY != Math.floor(npc.posY)) {
						yP = Math.ceil(npc.posY) + y - 1;
					}
					BlockPos posWorld = new BlockPos(npc.posX + x, yP, npc.posZ + z);
					IBlockState state = npc.world.getBlockState(posWorld);
					TileEntity tile = npc.world.getTileEntity(posWorld);
					if (tile != null && TileEntityRendererDispatcher.instance.renderers.get(tile.getClass()) == null) {
						tile = null;
					}
					BlockPos pos = new BlockPos(x, y, z);
					environmentStates.put(pos, state);
					environmentTiles.put(pos, tile);
				}
			}
		}
		List<Entity> entities = npc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(npc.getPosition()).grow(4.55d, 4.55d, 4.55d));
		for (Entity e : entities) {
			if (e.equals(npc)) {
				continue;
			}
			NBTTagCompound nbt = new NBTTagCompound();
			Entity le;
			if (e instanceof EntityNPCInterface) {
				le = Util.instance.copyToGUI((EntityNPCInterface) e, mc.world, true);
			} else {
				e.writeToNBTAtomically(nbt);
				le = EntityList.createEntityFromNBT(nbt, npc.world);
			}
			if (le != null) {
				le.posX -= npc.posX;
				le.posY -= npc.posY;
				le.posZ -= npc.posZ;
				le.rotationYaw = e.rotationYaw;
				le.prevRotationYaw = e.rotationYaw;
				le.rotationPitch = e.rotationPitch;
				le.prevRotationPitch = e.rotationPitch;
				environmentEntitys.add(le);
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
			partNames.setColorLine(CustomNpcs.colorAnimHoverPart);
			initGui();
		}
		if (subgui instanceof GuiSoundSelection) {
			if (frame != null) {
				frame.setStartSound(((GuiSoundSelection) subgui).selectedResource);
				initGui();
			}
		}
		if (subgui instanceof SubGuiEditText) {
			SubGuiEditText guiText = (SubGuiEditText) subgui;
			if (guiText.id == 0) {
				try {
					int pos = Integer.parseInt(guiText.text[0]) - 1;
					if (pos < 0) { pos = 0; } else if (pos > anim.frames.size()) { pos = anim.frames.size(); }
					frame = (AnimationFrameConfig) anim.addFrame(pos, frame);
					setPart(frame.parts.get(part.id));
					initGui();
				} catch (Exception e) { LogWriter.error("Error:", e); }
			}
		}
		if (subgui instanceof SubGuiSelectItemStack) {
			SubGuiSelectItemStack guiStack = (SubGuiSelectItemStack) subgui;
			if (guiStack.id == 0) { frame.setHoldRightStack(guiStack.stack); }
			else { frame.setHoldLeftStack(guiStack.stack); }
			isChanged = true;
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (hasSubGui() || anim == null) { return; }
		switch (textField.getId()) {
			case 0: {
				if (anim == null || anim.repeatLast == textField.getInteger()) { return; }
				anim.setRepeatLast(textField.getInteger());
				isChanged = true;
				resetAnimation();
				break;
			} // repeatLast
			case 1: {
				if (frame == null || frame.speed == textField.getInteger()) { return; }
				frame.setSpeed(textField.getInteger());
				isChanged = true;
				resetAnimation();
				break;
			} // speed
			case 2: {
				if (frame == null || frame.getEndDelay() == textField.getInteger()) { return; }
				frame.setEndDelay(textField.getInteger());
				isChanged = true;
				resetAnimation();
				break;
			} // delay
			case 5: {
				isChanged = true;
				float sliderValue = 0.5f;
				if (isHitbox) {
					if (hitbox == null || toolType == 0) { return; }
					if (toolType == 1) {
						hitbox.offset[0] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.1f; // 0 -> 10
					} else {
						hitbox.scale[0] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.2f; // 0 -> 5
					}
					textField.setText("" + (float) Math.round(textField.getDouble() * 1000.0d) / 1000.0f);
					if (tools.getSlider(0) != null) {
						tools.getSlider(0).sliderValue = sliderValue;
					}
					initGui();
					return;
				}
				if (isMotion) {
					if (frame == null || toolType != 1) { return; }
					frame.motions[0] = (float) textField.getDouble();
					sliderValue = (float) textField.getDouble() * 2.0f / 3.0f; // 0 -> 1.5
					textField.setText("" + (float) Math.round(textField.getDouble() * 1000.0d) / 1000.0f);
					if (tools.getSlider(0) != null) {
						tools.getSlider(0).sliderValue = sliderValue;
					}
					initGui();
					return;
				}
				PartConfig p = (PartConfig) partNames.objs[0];
				int tType = (int) partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				switch (toolType) {
					case 0: { // r
						p.rotation[0] = (float) Math.toRadians(textField.getDouble());
						sliderValue = (float) textField.getDouble() * 0.002778f + 0.5f;
						break;
					}
					case 1: { // o
						p.offset[0] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.1f + 0.5f;
						break;
					}
					case 2: { // s
						p.scale[0] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.2f;
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (tools.getSlider(0) != null) {
					tools.getSlider(0).sliderValue = sliderValue;
				}
				resetAnimation();
				break;
			} // X
			case 6: {
				isChanged = true;
				float sliderValue = 0.0f;
				if (isHitbox) {
					if (hitbox == null || toolType == 0) { return; }
					if (toolType == 1) {
						hitbox.offset[1] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.05f + 0.5f; // -10.0 -> 10.0
					} else {
						hitbox.scale[1] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.2f; // 0.0 -> 5.0
					}
					textField.setText("" + (float) Math.round(textField.getDouble() * 1000.0d) / 1000.0f);
					if (tools.getSlider(1) != null) {
						tools.getSlider(1).sliderValue = sliderValue;
					}
					return;
				}
				if (isMotion) {
					if (frame == null || toolType != 1) { return; }
					frame.motions[1] = (float) textField.getDouble();
					sliderValue = (float) textField.getDouble() / 3.0f + 0.5f; // -1.5 -> 1.5
					textField.setText("" + (float) Math.round(frame.motions[1] * 1000.0d) / 1000.0f);
					if (tools.getSlider(1) != null) {
						tools.getSlider(1).sliderValue = sliderValue;
					}
					initGui();
					return;
				}
				PartConfig p = (PartConfig) partNames.objs[0];
				int tType = (int) partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				switch (toolType) {
					case 0: {
						p.rotation[1] = (float) Math.toRadians(textField.getDouble());
						sliderValue = (float) textField.getDouble() * 0.002778f + 0.5f;
						break;
					}
					case 1: {
						p.offset[1] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.1f + 0.5f;
						break;
					}
					case 2: {
						p.scale[1] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.2f;
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (tools.getSlider(1) != null) {
					tools.getSlider(1).sliderValue = sliderValue;
				}
				resetAnimation();
				break;
			} // Y
			case 7: {
				isChanged = true;
				if (isHitbox) {
					if (hitbox == null || toolType == 0) { return; }
					float sliderValue;
					if (toolType == 1) {
						hitbox.offset[2] = (float) Math.toRadians(textField.getDouble());
						sliderValue = (float) textField.getDouble() * 0.002778f + 0.5f;
					}
					else {
						hitbox.scale[2] = (float) textField.getDouble();
						sliderValue = (float) textField.getDouble() * 0.2f;
					}
					textField.setText("" + (float) Math.round(textField.getDouble() * 1000.0d) / 1000.0f);
					if (tools.getSlider(2) != null) {
						tools.getSlider(2).sliderValue = sliderValue;
					}
					return;
				}
				if (isMotion) {
					if (frame == null || toolType != 1) { return; }
					frame.motions[2] = (float) Math.toRadians(textField.getDouble());
					float sliderValue = (float) textField.getDouble() * 0.002778f + 0.5f;
					textField.setText("" + (float) Math.round(textField.getDouble() * 1000.0d) / 1000.0f);
					if (tools.getSlider(2) != null) {
						tools.getSlider(2).sliderValue = sliderValue;
					}
					initGui();
					return;
				}
				PartConfig p = (PartConfig) partNames.objs[0];
				int tType = (int) partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				float value = 0.0f;
				switch (toolType) {
					case 0: {
						p.rotation[2] = (float) Math.toRadians(textField.getDouble());
						value = (float) textField.getDouble() * 0.002778f + 0.5f;
						break;
					}
					case 1: {
						p.offset[2] = (float) textField.getDouble();
						value = (float) textField.getDouble() * 0.1f + 0.5f;
						break;
					}
					case 2: {
						p.scale[2] = (float) textField.getDouble();
						value = (float) textField.getDouble() * 0.2f;
						break;
					}
				}
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (tools.getSlider(2) != null) {
					tools.getSlider(2).sliderValue = value;
				}
				resetAnimation();
				break;
			} // Z
			case 8: {
				if (isHitbox || isMotion ) {
					initGui();
					return;
				}
				isChanged = true;
				PartConfig p = (PartConfig) partNames.objs[0];
				int tType = (int) partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				p.rotation[3] = (float) Math.toRadians(textField.getDouble());
				float value = (float) textField.getDouble() * 0.002778f + 0.5f;
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (tools.getSlider(3) != null) {
					tools.getSlider(3).sliderValue = value;
				}
				resetAnimation();
				break;
			} // X1
			case 9: {
				if (isHitbox || isMotion ) {
					initGui();
					return;
				}
				isChanged = true;
				PartConfig p = (PartConfig) partNames.objs[0];
				int tType = (int) partNames.objs[1];
				if (part == null || !part.equals(p) || tType != toolType) { return; }
				p.rotation[4] = (float) Math.toRadians(textField.getDouble());
				float value = (float) textField.getDouble() * 0.005556f + 0.5f;
				textField.setText("" + (float) (Math.round(textField.getDouble() * 1000.0d) / 1000.0d));
				if (tools.getSlider(4) != null) {
					tools.getSlider(4).sliderValue = value;
				}
				resetAnimation();
				break;
			} // Y1
			case 10: {
				if (anim == null) { return; }
				anim.chance = (float) Math.round(textField.getDouble() * 1000.0d) / 100000.0f;
				textField.setText("" + (anim.chance * 100.0f));
				isChanged = true;
				resetAnimation();
				break;
			} // chance
			case 11: {
				if (anim == null) { return; }
				anim.name = textField.getText();
				break;
			} // name
		}
	}

	@Override
	public void save() {
		if (anim != null) { Client.sendData(EnumPacketServer.AnimationChange, anim.save()); }
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("save", true);
		Client.sendData(EnumPacketServer.EmotionChange, nbt);
	}

	@Override
	public void closeMiniWindow(GuiNpcMiniWindow miniWindow) {
		mwindows.remove(miniWindow.id);
		if (miniWindow.id == 0 && getButton(29) != null) {
			getButton(29).enabled = !mwindows.containsKey(0);
		}
		if (miniWindow.id == 1 && getButton(35) != null) {
			getButton(35).enabled = !mwindows.containsKey(1);
		}
		else
		if (miniWindow.id == 2 && getButton(45) != null) {
			getButton(45).enabled = !mwindows.containsKey(2);
		}
	}

}
