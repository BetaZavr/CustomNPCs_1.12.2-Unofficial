package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import noppes.npcs.LogWriter;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.util.ResourceData;
import noppes.npcs.util.Util;
import noppes.npcs.util.NaturalOrderComparator;

public class GuiCustomScroll
extends GuiScreen
implements IComponentGui {

	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/misc.png");

	private GuiScreen parent;
	private ICustomScrollListener listener;

	public boolean hovered;
	public boolean isScrolling = false;
	public boolean selectable = true;
	public boolean visible = true;
	private boolean isSorted = true;
	private boolean multipleSelection = false;

	public int colorBack = 0xC0101010, border = 0xFF000000;
	public int guiLeft = 0;
	public int guiTop = 0;
	public int hover = -1;
	public int id;
	private int lastClickedItem;
	private int listHeight = 0;
	public int maxScrollY;
	private int scrollHeight = 0;
	public int scrollY = 0;
	public int selected = -1;

	private long lastClickedTime = 0L;

	private HashSet<String> selectedList = new HashSet<>();
	private final List<String> list = new ArrayList<>();
	private List<Integer> colors = null;
	private List<String> suffixes;
	private List<ItemStack> stacks = null;
	private List<ResourceData> prefixes;

	public String[][] hoversTexts = null;
	public String[] hoverText;

	public GuiCustomScroll(GuiScreen parent, int id) {
		this.id = id;
		this.setParent(parent);
		this.setSize(176, 159);
	}

	public void setParent(GuiScreen parent) {
		this.parent = parent;
		if (parent instanceof ICustomScrollListener) {
			this.listener = (ICustomScrollListener) parent;
		}
	}

	public GuiCustomScroll(GuiScreen parent, int id, boolean multipleSelection) {
		this(parent, id);
		this.multipleSelection = multipleSelection;
	}

	public void clear() {
		this.list.clear();
		this.selected = -1;
		this.scrollY = 0;
		this.setSize(this.width, this.height);
	}

	protected void drawItems() {
		int xOffset = (this.scrollHeight < this.height - 2) ? 0 : 10;
		for (int i = 0; i < this.list.size(); ++i) {
			int j = 4;
			int k = 14 * i + 4 - this.scrollY;
			if (k < 4 || k + 10 > this.height) {
				continue;
			}
			String displayString = this.list.get(i) == null ? "null" : this.list.get(i);
			try {
				displayString = new TextComponentTranslation(displayString).getFormattedText();
			} catch (Exception ignored) { }

			String text = "";
			float maxWidth = (this.width + xOffset - 8) * 0.8f;
			if (this.fontRenderer.getStringWidth(displayString) > maxWidth) {
				for (int h = 0; h < displayString.length(); ++h) {
					char c = displayString.charAt(h);
					text += c;
					if (this.fontRenderer.getStringWidth(text) > maxWidth) {
						break;
					}
				}
				if (displayString.length() > text.length()) {
					text += "...";
				}
			} else {
				text = displayString;
			}
			int xo = 0;
			if ((this.stacks != null && i < this.stacks.size()) || (this.prefixes != null && i < this.prefixes.size())) {
				j = 14;
				xo = -14;
			}
			int c = CustomNpcs.MainColor.getRGB();
			if (this.colors != null && i < this.colors.size()) {
				c = this.colors.get(i);
			}
			if ((this.multipleSelection && Util.instance.containsDeleteColor(this.selectedList, text, false))
					|| (!this.multipleSelection && this.selected == i)) {
				this.drawVerticalLine(j - 2, k - 4, k + 10, -1);
				this.drawVerticalLine(j + width - 17 + xOffset + xo, k - 4, k + 10, -1);
				this.drawHorizontalLine(j - 2, j + this.width - 17 + xOffset + xo, k - 3, -1);
				this.drawHorizontalLine(j - 2, j + this.width - 17 + xOffset + xo, k + 10, -1);
				this.fontRenderer.drawString(text, j, k, c);
				c = CustomNpcs.MainColor.getRGB();
			} else if (i == this.hover) {
				this.fontRenderer.drawString(text, j, k, 65280);
				c = CustomNpcs.HoverColor.getRGB();
			} else {
				this.fontRenderer.drawString(text, j, k, c);
				c = CustomNpcs.MainColor.getRGB();
			}
			if (this.suffixes != null && i < this.suffixes.size() && this.suffixes.get(i) != null && !this.suffixes.get(i).isEmpty()
					&& this.fontRenderer.getStringWidth(text + this.suffixes.get(i)) < this.width - 20) {
				this.fontRenderer.drawString(this.suffixes.get(i),
						this.width - 9 + (this.listHeight > this.height ? -11 : 0)
								- this.fontRenderer.getStringWidth(this.suffixes.get(i)),
						k, c);
			}
		}
	}

	private void drawPrefixs() {
		if (this.prefixes == null) {
			return;
		}
		for (int i = 0; i < this.list.size() && i < this.prefixes.size(); ++i) {
			int k = 14 * i + 4 - this.scrollY;
			ResourceData rd = this.prefixes.get(i);
			if (k < 4 || k + 12 >= this.height || rd == null || rd.resource == null || rd.width <= 0
					|| rd.height <= 0) {
				continue;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(0.5f, k - 1.5f + rd.tH, 0.0f); // position X, Y, Z
			float scale = 12.0f / (float) (Math.max(rd.width, rd.height));
			GlStateManager.scale(scale, scale, scale);
			Minecraft.getMinecraft().getTextureManager().bindTexture(rd.resource);
			this.drawTexturedModalRect(0, 0, rd.u, rd.v, rd.width, rd.height);
			GlStateManager.popMatrix();
		}
	}

	public void drawScreen(int mouseX, int mouseY, int mouseScrolled) {
		if (!this.visible) {
			return;
		}
		if (this.border != 0xFF000000) {
			this.drawGradientRect(this.guiLeft - 1, this.guiTop - 1, this.width + this.guiLeft + 1,
					this.height + this.guiTop + 1, this.border, this.border);
		}
		this.hovered = this.isMouseOver(mouseX, mouseY);
		this.drawGradientRect(this.guiLeft, this.guiTop, this.width + this.guiLeft, this.height + this.guiTop,
				this.colorBack, this.colorBack);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(GuiCustomScroll.resource);
		GlStateManager.pushMatrix();
		GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (this.selectable) {
			this.hover = this.getMouseOver(mouseX, mouseY);
		}
		this.drawItems();
		GlStateManager.popMatrix();
		if (this.stacks != null && this.parent != null
				&& ((this.parent instanceof GuiContainerNPCInterface
						&& !((GuiContainerNPCInterface) this.parent).hasSubGui())
						|| (this.parent instanceof GuiNPCInterface && !((GuiNPCInterface) this.parent).hasSubGui()))) {
			this.drawStacks();
		}
		if (this.prefixes != null && this.parent != null
				&& ((this.parent instanceof GuiContainerNPCInterface
						&& !((GuiContainerNPCInterface) this.parent).hasSubGui())
						|| (this.parent instanceof GuiNPCInterface && !((GuiNPCInterface) this.parent).hasSubGui()))) {
			this.drawPrefixs();
		}
		if (this.scrollHeight <= this.height - 6) {
			this.drawScrollBar(); // Changed
			mouseX -= this.guiLeft;
			mouseY -= this.guiTop;
			if (Mouse.isButtonDown(0)) {
				if (mouseX >= this.width - 9 && mouseX < this.width - 1 && mouseY >= 1 && mouseY < this.height - 1) {
					this.isScrolling = true;
				}
			} else {
				this.isScrolling = false;
			}
			if (this.isScrolling) {
				this.scrollY = (int) ((float) mouseY / ((float) this.height - 2.0f)
						* ((float) this.listHeight - (float) this.scrollHeight)); // Changed
				if (this.scrollY < 0) {
					this.scrollY = 0;
				}
				if (this.scrollY > this.maxScrollY) {
					this.scrollY = this.maxScrollY;
				}
			}
			if (mouseScrolled != 0) {
				this.scrollY += ((mouseScrolled > 0) ? -14 : 14);
				if (this.scrollY > this.maxScrollY) {
					this.scrollY = this.maxScrollY;
				}
				if (this.scrollY < 0) {
					this.scrollY = 0;
				}
			}
		}
		if (this.hover >= 0 && this.hover < this.list.size()) {
			if (!(this.parent instanceof IEditNPC) || !((IEditNPC) this.parent).hasSubGui()) {
				String[] texts = new String[0];
				if (this.hoverText != null) {
					texts = this.hoverText;
				} else if (this.hoversTexts != null && this.hover < this.hoversTexts.length) {
					texts = this.hoversTexts[this.hover];
				} else if (this.stacks != null && this.hover < this.stacks.size()) {
					List<String> l = this.stacks.get(this.hover).getTooltip(this.mc.player, TooltipFlags.NORMAL);
					texts = l.toArray(new String[0]);
				}
				if (texts != null) {
					if (this.parent instanceof GuiNPCInterface) {
						((GuiNPCInterface) this.parent).hoverText = texts;
					} else if (this.parent instanceof GuiContainerNPCInterface) {
						((GuiContainerNPCInterface) this.parent).hoverText = texts;
					}
				}
			}
		}
	}

	private void drawScrollBar() {
		int posX = this.guiLeft + this.width - 9;
		int posY = this.guiTop + (int) ((float) this.scrollY / (float) this.listHeight * ((float) this.height - 18.0f))
				+ 1;
		Gui.drawRect(posX, posY, posX + 8, posY + this.scrollHeight + 1, 0xA0FFF0F0);
	}

	private void drawStacks() {
		if (this.stacks == null) {
			return;
		}
		for (int i = 0; i < this.list.size() && i < this.stacks.size(); ++i) {
			int k = 14 * i + 4 - this.scrollY;
			if (k < 4 || k + 12 >= this.height) {
				continue;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(0, k - 2.5f, 300.0f);
			GlStateManager.scale(0.75f, 0.75f, 0.75f);
			RenderHelper.enableStandardItemLighting();
			this.mc.getRenderItem().renderItemAndEffectIntoGUI(this.stacks.get(i), 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
	}

	public int getColor(int pos) {
		if (this.colors == null || this.colors.isEmpty()) {
			return 0;
		}
		return this.colors.get(pos);
	}

	public int getHeight() {
		return this.height;
	}

	public List<String> getList() {
		return this.list;
	}

	private int getMouseOver(int i, int j) {
		i -= this.guiLeft;
		j -= this.guiTop;
		if (i >= 4 && i < this.width - 4 && j >= 4 && j < this.height) {
			for (int j2 = 0; j2 < this.list.size(); ++j2) {
				if (this.mouseInOption(i, j, j2)) {
					return j2;
				}
			}
		}
		return -1;
	}

	public int getPos(String select) {
		if (select == null || select.isEmpty() || this.list.isEmpty()) {
			return -1;
		}
		for (int i = 0; i < this.list.size(); i++) {
			if (this.list.get(i).equalsIgnoreCase(select)) {
				return i;
			}
		}
		return -1;
	}

	public String getSelected() {
		if (this.selected == -1 || this.selected >= this.list.size()) {
			return null;
		}
		return this.list.get(this.selected);
	}

	public HashSet<String> getSelectedList() {
		return this.selectedList;
	}

	public int getWidth() {
		return this.width;
	}

	public boolean hasSelected() {
		return this.selected >= 0 && this.getSelected() != null;
	}

	public boolean isMouseOver(int x, int y) {
		return x >= this.guiLeft && x <= this.guiLeft + this.width && y >= this.guiTop
				&& y <= this.guiTop + this.height;
	}

	private boolean isSameList(List<String> list) {
		if (this.list.size() != list.size()) {
			return false;
		}
		for (int i = 0; i < this.list.size(); i++) {
			String s = this.list.get(i);
			if (!list.contains(s)) {
				return false;
			}
			String l = list.get(i);
			if (!s.equalsIgnoreCase(l)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void keyTyped(char c, int i) {
		if (!this.hovered || GuiNpcTextField.isActive()) {
			return;
		}
		if (this.list.size() <= 1) {
			return;
		}
		if (i == 200 || i == ClientProxy.frontButton.getKeyCode()) { // up
			if (this.selected < 1) {
				return;
			}
			this.selected--;
			if (this.maxScrollY > 0) {
				this.scrollY -= 14;
				if (this.scrollY < 0) {
					this.scrollY = 0;
				}
			}
			if (this.listener != null) {
				this.listener.scrollClicked(-1, -1, 0, this);
			}
		} else if (i == 208 || i == ClientProxy.backButton.getKeyCode()) { // down
			if (this.selected >= this.getList().size() - 1) {
				return;
			}
			this.selected++;
			if (this.maxScrollY > 0) {
				this.scrollY += 14;
				if (this.scrollY > this.maxScrollY) {
					this.scrollY = this.maxScrollY;
				}
			}
			if (this.listener != null) {
				this.listener.scrollClicked(-1, -1, 0, this);
			}
		}
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != 0 || this.hover < 0) {
			return;
		}
		if (this.multipleSelection) {
			if (this.selectedList.contains(this.list.get(this.hover))) {
				this.selectedList.remove(this.list.get(this.hover));
			} else {
				this.selectedList.add(this.list.get(this.hover));
			}
		} else {
            this.selected = this.hover;
            this.hover = -1;
		}
		if (this.listener != null) {
			long time = System.currentTimeMillis();
			this.listener.scrollClicked(mouseX, mouseY, mouseButton, this);
			if (this.selected >= 0 && this.selected == this.lastClickedItem && time - this.lastClickedTime < 500L) {
				this.listener.scrollDoubleClicked(this.list.get(this.selected), this);
			}
			this.lastClickedTime = time;
			this.lastClickedItem = this.selected;
		}
	}

	public boolean mouseInOption(int i, int j, int k) {
		int l = 4;
		int i2 = 14 * k + 4 - this.scrollY;
		return i >= l - 1 && i < l + this.width - 11 && j >= i2 - 1 && j < i2 + 8;
	}

	public void replace(String oldName, String newName) {
		if (!this.list.contains(oldName)) {
			return;
		}
		String select = this.getSelected();
		int i = this.list.indexOf(oldName);
		this.list.remove(oldName);
		this.list.add(i, newName);
		if (this.isSorted) {
			this.list.sort(new NaturalOrderComparator());
		}
		if (oldName.equals(select)) {
			select = newName;
		}
		this.selected = this.list.indexOf(select);
		this.setSize(this.width, this.height);
	}

	public void resetRoll() {
		if (this.selected <= 0) {
			return;
		}
		int pos = (this.selected + 1) * 14;
		if (pos >= this.scrollY && pos <= this.scrollY + this.height) {
			return;
		}
		this.scrollY = pos;
		if (pos - this.height / 2 > 0) {
			this.scrollY = pos - this.height / 2;
		}
		if (this.scrollY < 0) {
			this.scrollY = 0;
		}
		if (this.scrollY > this.maxScrollY) {
			this.scrollY = this.maxScrollY;
		}
	}

	public void scrollTo(String name) {
		int i = this.list.indexOf(name);
		if (i < 0 || this.scrollHeight >= this.height - 2) {
			return;
		}
		int pos = (int) (1.0f * i / this.list.size() * this.listHeight);
		if (pos > this.maxScrollY) {
			pos = this.maxScrollY;
		}
		this.scrollY = pos;
	}

	public void setColors(List<Integer> colors) {
		this.colors = colors;
	}

	public void setList(List<String> list) {
		if (list == null) {
			list = Lists.newArrayList();
		}
		if (this.isSameList(list)) {
			return;
		}
		this.isSorted = true;
		this.scrollY = 0;
		list.sort(new NaturalOrderComparator());
		this.list.clear();
		this.list.addAll(list);
		this.setSize(this.width, this.height);
	}

	public void setListNotSorted(List<String> list) {
		if (this.isSameList(list)) {
			return;
		}
		this.scrollY = 0;
		this.list.clear();
		this.list.addAll(list);
		this.setSize(this.width, this.height);
		this.isSorted = false;
	}

	public void setPrefixes(List<ResourceData> prefixes) {
		this.prefixes = prefixes;
	}

	public void setSelected(String name) {
		if (name == null || name.isEmpty()) {
			this.selected = -1;
		}
		this.selected = this.list.indexOf(name);
		if (this.selected == -1) {
			name = Util.instance.deleteColor(name);
			for (int i = 0; i < this.list.size(); i++) {
				if (Util.instance.deleteColor(this.list.get(i)).equalsIgnoreCase(name)) {
					this.selected = i;
					break;
				}
			}
		}
		this.resetRoll();
	}

	public void setSelectedList(HashSet<String> selectedList) {
		this.selectedList = selectedList;
	}

	public void setSize(int x, int y) {
		this.height = y;
		this.width = x;
		this.listHeight = 14 * this.list.size();
		if (this.listHeight > 0) {
			this.scrollHeight = (int) ((float) this.height * ((float) this.height - 2.0f) / (float) this.listHeight);
		} else {
			this.scrollHeight = Integer.MAX_VALUE;
		}
		this.maxScrollY = this.listHeight - this.height;
		if (this.maxScrollY < 0) {
			this.maxScrollY = 0;
		}
	}

	public void setStacks(List<ItemStack> stacks) {
		this.stacks = stacks;
	}

	public void setSuffixes(List<String> suffixes) {
		this.suffixes = suffixes;
	}

	public GuiCustomScroll setUnselectable() {
		this.selectable = false;
		return this;
	}

	@Override
	public int[] getCenter() {
		return new int[] { this.guiLeft + this.width / 2, this.guiTop + this.height / 2};
	}
	
}
