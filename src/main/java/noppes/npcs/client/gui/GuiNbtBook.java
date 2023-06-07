package noppes.npcs.client.gui;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNbtBook
extends GuiNPCInterface
implements IGuiData {
	
	private ItemStack blockStack;
	private NBTTagCompound compound;
	private Entity entity;
	private int entityId;
	private String errorMessage;
	private String faultyText;
	private NBTTagCompound originalCompound;
	private IBlockState state;
	private TileEntity tile;
	private int x;
	private int y;
	private int z;
	// New
	private ItemStack stack;

	public GuiNbtBook(int x, int y, int z) {
		this.faultyText = null;
		this.errorMessage = null;
		this.x = x;
		this.y = y;
		this.z = z;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		if (id == 0) {
			if (this.faultyText != null) {
				this.setSubGui(new SubGuiNpcTextArea(this.compound.toString(), this.faultyText).enableHighlighting());
			} else {
				this.setSubGui(new SubGuiNpcTextArea(this.compound.toString()).enableHighlighting());
			}
		}
		if (id == 67) {
			this.getLabel(0).setLabel("Saved");
			if (this.compound.equals(this.originalCompound)) {
				return;
			}
			if (this.stack != null) {
				Client.sendData(EnumPacketServer.NbtBookSaveItem, this.compound);
				return;
			}
			if (this.tile == null) {
				Client.sendData(EnumPacketServer.NbtBookSaveEntity, this.entityId, this.compound);
				return;
			}
			Client.sendData(EnumPacketServer.NbtBookSaveBlock, this.x, this.y, this.z, this.compound);
			this.originalCompound = this.compound.copy();
			this.getButton(67).enabled = false;
		}
		if (id == 66) {
			this.close();
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		if (gui instanceof SubGuiNpcTextArea) {
			try {
				this.compound = JsonToNBT.getTagFromJson(((SubGuiNpcTextArea) gui).text);
				String s = null;
				this.faultyText = s;
				this.errorMessage = s;
			} catch (NBTException e) {
				this.errorMessage = e.getLocalizedMessage();
				this.faultyText = ((SubGuiNpcTextArea) gui).text;
			}
			this.initGui();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.hasSubGui()) {
			return;
		}
		if (this.stack != null || this.state != null) {
			GlStateManager.pushMatrix();
			Gui.drawRect(this.guiLeft + 3, this.guiTop + 3, this.guiLeft + 55, this.guiTop + 55, 0xFF808080);
			Gui.drawRect(this.guiLeft + 4, this.guiTop + 4, this.guiLeft + 54, this.guiTop + 54, 0xFF000000);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translate((this.guiLeft + 5), (this.guiTop + 5), 0.0f);
			GlStateManager.scale(3.0f, 3.0f, 3.0f);
			RenderHelper.enableGUIStandardItemLighting();
			this.itemRender.renderItemAndEffectIntoGUI(this.stack != null ? this.stack : this.blockStack, 0, 0);
			this.itemRender.renderItemOverlays(this.fontRenderer, this.stack != null ? this.stack : this.blockStack, 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		if (this.entity instanceof EntityLivingBase) {
			GlStateManager.pushMatrix();
			this.drawNpc((EntityLivingBase) this.entity, 30, 80, 1.0f, 0, 0, true);
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(this.guiLeft + 5, this.guiTop + 13, this.guiLeft + 55, this.guiTop + 99, 0xFF808080);
			Gui.drawRect(this.guiLeft + 6, this.guiTop + 14, this.guiLeft + 54, this.guiTop + 98, 0xFF000000);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.stack != null) {
			this.addLabel(new GuiNpcLabel(11, "id: \"" + this.stack.getItem().getRegistryName()+"\"",
					this.guiLeft + 60, this.guiTop + 6));
		}
		if (this.state != null) {
			this.addLabel(new GuiNpcLabel(11, "x: " + this.x + ", y: " + this.y + ", z: " + this.z, this.guiLeft + 60, this.guiTop + 6));
			this.addLabel(new GuiNpcLabel(12, "id: " + Block.REGISTRY.getNameForObject(this.state.getBlock()), this.guiLeft + 60, this.guiTop + 16));
			this.addLabel(new GuiNpcLabel(13, "meta: " + this.state.getBlock().getMetaFromState(this.state), this.guiLeft + 60, this.guiTop + 26));
		}
		if (this.entity != null) {
			this.addLabel(
					new GuiNpcLabel(12, "id: " + EntityRegistry.getEntry(this.entity.getClass()).getRegistryName(),
							this.guiLeft + 60, this.guiTop + 6));
		}
		this.addButton(new GuiNpcButton(0, this.guiLeft + 38, this.guiTop + 144, 180, 20, "nbt.edit"));
		this.getButton(0).enabled = (this.compound != null && this.compound.getKeySet().size() > 0);
		this.addLabel(new GuiNpcLabel(0, "", this.guiLeft + 4, this.guiTop + 167));
		this.addLabel(new GuiNpcLabel(1, "", this.guiLeft + 4, this.guiTop + 177));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 128, this.guiTop + 190, 120, 20, "gui.close"));
		this.addButton(new GuiNpcButton(67, this.guiLeft + 4, this.guiTop + 190, 120, 20, "gui.save"));
		if (this.errorMessage != null) {
			this.getButton(67).enabled = false;
			int i = this.errorMessage.indexOf(" at: ");
			if (i > 0) {
				this.getLabel(0).setLabel(this.errorMessage.substring(0, i));
				this.getLabel(1).setLabel(this.errorMessage.substring(i));
			} else {
				this.getLabel(0).setLabel(this.errorMessage);
			}
		}
		if (this.getButton(67).enabled && this.originalCompound != null) {
			this.getButton(67).enabled = !this.originalCompound.equals(this.compound);
		}
	}

	@Override
	public void save() {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("Item") && compound.getBoolean("Item")) {
			this.stack = new ItemStack(compound.getCompoundTag("Data"));
		}
		else if (compound.hasKey("EntityId")) {
			this.entityId = compound.getInteger("EntityId");
			this.entity = this.player.world.getEntityByID(this.entityId);
		}
		else {
			this.tile = this.player.world.getTileEntity(new BlockPos(this.x, this.y, this.z));
			this.state = this.player.world.getBlockState(new BlockPos(this.x, this.y, this.z));
			this.blockStack = this.state.getBlock().getItem(this.player.world, new BlockPos(this.x, this.y, this.z),
					this.state);
		}
		this.originalCompound = compound.getCompoundTag("Data");
		this.compound = this.originalCompound.copy();
		this.initGui();
	}
}
