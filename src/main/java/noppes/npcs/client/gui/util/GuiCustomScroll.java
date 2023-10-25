package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.lwjgl.input.Mouse;

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
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NaturalOrderComparator;

public class GuiCustomScroll
extends GuiScreen {
	
	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/misc.png");
	public int colorBack = 0xC0101010, border = 0xFF000000;
	private List<Integer> colors;
	private List<String> suffixs;
	public int guiLeft;
	public int guiTop;
	private int hover;
	public boolean hovered;
	public String[][] hoversTexts;
	public String[] hoverText;
	public int id;
	public boolean isScrolling;
	private boolean isSorted;
	private int lastClickedItem;
	private long lastClickedTime;
	private List<String> list;
	private ICustomScrollListener listener;
	private int listHeight;
	public int maxScrollY;
	private boolean multipleSelection;
	private GuiScreen parent;
	private int scrollHeight;
	public int scrollY;
	private boolean selectable;
	public int selected;
	private HashSet<String> selectedList;
	private List<ItemStack> stacks;
	public boolean visible;

	public GuiCustomScroll(GuiScreen parent, int id) {
		this.guiLeft = 0;
		this.guiTop = 0;
		this.multipleSelection = false;
		this.isSorted = true;
		this.visible = true;
		this.selectable = true;
		this.lastClickedTime = 0L;
		this.width = 176;
		this.height = 159;
		this.selected = -1;
		this.hover = -1;
		this.selectedList = new HashSet<String>();
		this.listHeight = 0;
		this.scrollY = 0;
		this.scrollHeight = 0;
		this.isScrolling = false;
		if (parent instanceof ICustomScrollListener) {
			this.listener = (ICustomScrollListener) parent;
		}
		this.list = new ArrayList<String>();
		this.id = id;
		// New
		this.parent = parent;
		this.hoversTexts = null;
		this.colors = null;
		this.stacks = null;
		this.setSize(this.width, this.height);
	}

	public GuiCustomScroll(GuiScreen parent, int id, boolean multipleSelection) {
		this(parent, id);
		this.multipleSelection = multipleSelection;
	}

	public void clear() {
		this.list = new ArrayList<String>();
		this.selected = -1;
		this.scrollY = 0;
		this.setSize(this.width, this.height);
	}

	protected void drawItems() {
		int xOffset = (this.scrollHeight < this.height - 2) ? 0 : 10;
		for (int i = 0; i < this.list.size(); ++i) {
			int j = 4;
			int k = 14 * i + 4 - this.scrollY;
			if (k < 4 || k + 10 >= this.height) {
				continue;
			}
			String displayString = this.list.get(i).toString();
			try { 
				displayString = new TextComponentTranslation(this.list.get(i).toString()).getFormattedText();
			} catch (Exception e) { }
			
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
			if (this.stacks != null && i < this.stacks.size()) {
				j = 14;
				xo = -14;
			}
			int c = CustomNpcs.mainColor;
			if (this.colors != null && i < this.colors.size()) { c = this.colors.get(i); }
			if ((this.multipleSelection && AdditionalMethods.containsDeleteColor(this.selectedList, text, false)) || (!this.multipleSelection && this.selected == i)) {
				this.drawVerticalLine(j - 2, k - 4, k + 10, -1);
				this.drawVerticalLine(j + this.width - 18 + xOffset + xo, k - 4, k + 10, -1);
				this.drawHorizontalLine(j - 2, j + this.width - 18 + xOffset + xo, k - 3, -1);
				this.drawHorizontalLine(j - 2, j + this.width - 18 + xOffset + xo, k + 10, -1);
				this.fontRenderer.drawString(text, j, k, c);
				c = CustomNpcs.mainColor;
			} else if (i == this.hover) {
				this.fontRenderer.drawString(text, j, k, 65280);
				c = CustomNpcs.hoverColor;
			} else {
				this.fontRenderer.drawString(text, j, k, c);
				c = CustomNpcs.mainColor;
			}
			if (this.suffixs != null &&
					i < this.suffixs.size() &&
					!this.suffixs.get(i).isEmpty() &&
					this.fontRenderer.getStringWidth(text+this.suffixs.get(i))<this.width-20) {
				this.fontRenderer.drawString(this.suffixs.get(i), j+this.width-9 + (this.listHeight>this.height ? -11 : 0) - this.fontRenderer.getStringWidth(this.suffixs.get(i)), k, c);
			}
		}
	}

	public void drawScreen(int mouseX, int mouseY, float f, int mouseScrolled) {
		if (!this.visible) { return; }
		if (this.border!=0xFF000000) {
			this.drawGradientRect(this.guiLeft - 1, this.guiTop - 1, this.width + this.guiLeft + 1, this.height + this.guiTop + 1, this.border, this.border);
		}
		this.hovered = this.isMouseOver(mouseX, mouseY);
		this.drawGradientRect(this.guiLeft, this.guiTop, this.width + this.guiLeft, this.height + this.guiTop, this.colorBack, this.colorBack);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(GuiCustomScroll.resource);
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
		if (this.stacks != null) { this.drawStacks(); }
		if (this.scrollHeight < this.height - 8) {
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
				this.scrollY = (int) ((float) mouseY / ((float) this.height - 2.0f) * ((float) this.listHeight - (float) this.scrollHeight)); // Changed
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
			String[] texts = new String[0];
			if (this.hoverText != null) {
				texts = this.hoverText;
			} else if (this.hoversTexts != null && this.hover < this.hoversTexts.length) {
				texts = this.hoversTexts[this.hover];
			} else if (this.stacks != null && this.hover < this.stacks.size()) {
				List<String> l = this.stacks.get(this.hover).getTooltip(this.mc.player, TooltipFlags.NORMAL);
				texts = l.toArray(new String[l.size()]);
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

	private void drawScrollBar() { // Changed
		int posX = this.guiLeft + this.width - 9;
		int posY = this.guiTop + (int) ((float) this.scrollY / (float) this.listHeight * ((float) this.height - 18.0f)) + 1;
		Gui.drawRect(posX, posY, posX + 8, posY + this.scrollHeight + 1, 0xA0FFF0F0);
	}

	protected void drawStacks() {
		if (this.stacks == null) { return; }
		for (int i = 0; i < this.list.size() && i < this.stacks.size(); ++i) {
			int k = 14 * i + 4 - this.scrollY;
			if (k < 4 || k + 12 >= this.height) {
				continue;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(0, k - 2.5f, 300.0f); // position X, Y, Z
			GlStateManager.scale(0.75f, 0.75f, 0.75f);
			RenderHelper.enableStandardItemLighting();
			/*
			 * GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
			 * this.mc.getRenderItem().renderItem(this.stacks.get(i),
			 * ItemCameraTransforms.TransformType.GROUND);
			 */
			this.mc.getRenderItem().renderItemAndEffectIntoGUI(this.stacks.get(i), 0, 0);
			// this.mc.getRenderItem().renderItemOverlays(this.mc.fontRenderer,
			// this.stacks.get(i), 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
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

	// New
	public int getPos(String select) {
		if (select == null || select.isEmpty() || this.list == null || this.list.size() == 0) {
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
		return this.selected >= 0 && this.getSelected()!=null;
	}

	public boolean isMouseOver(int x, int y) {
		return x >= this.guiLeft && x <= this.guiLeft + this.width && y >= this.guiTop && y <= this.guiTop + this.height;
	}

	private boolean isSameList(List<String> list) {
		if (this.list.size() != list.size()) {
			return false;
		}
		for (String s : this.list) {
			if (!list.contains(s)) {
				return false;
			}
		}
		return true;
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
			if (this.hover >= 0) {
				this.selected = this.hover;
			}
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
		if (!this.list.contains(oldName)) { return; }
		String select = this.getSelected();
		int i = this.list.indexOf(oldName);
		this.list.remove(oldName);
		this.list.add(i, newName);
		if (this.isSorted) { Collections.sort(this.list, new NaturalOrderComparator()); }
		if (oldName.equals(select)) {
			select = newName;
		}
		this.selected = this.list.indexOf(select);
		this.setSize(this.width, this.height);
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

	public void setSuffixs(List<String> suffixs) {
		this.suffixs = suffixs;
	}

	public void setList(List<String> list) {
		if (this.isSameList(list)) { return; }
		this.isSorted = true;
		this.scrollY = 0;
		Collections.sort(list, new NaturalOrderComparator());
		this.list = list;
		this.setSize(this.width, this.height);
	}

	public void setListNotSorted(List<String> list) {
		if (this.isSameList(list)) { return; }
		this.isSorted = true;
		this.scrollY = 0;
		this.list = list;
		this.setSize(this.width, this.height);
		this.isSorted = false;
	}

	public void setSelected(String name) {
		if (name == null || name.isEmpty()) {
			this.selected = -1;
		}
		this.selected = this.list.indexOf(name);
		this.resetRoll();
	}

	public void resetRoll() {
		if (this.selected <= 0) { return; }
		int pos = (this.selected + 1) * 14;
		if (pos >= this.scrollY && pos <= this.scrollY + this.height) { return; }
		this.scrollY = pos;
		if (pos - this.height / 2 > 0) { this.scrollY = pos - this.height / 2; }
		if (this.scrollY < 0) { this.scrollY = 0; }
		if (this.scrollY > this.maxScrollY) { this.scrollY = this.maxScrollY; }
	}

	public void setSelectedList(HashSet<String> selectedList) {
		this.selectedList = selectedList;
	}

	public void setSize(int x, int y) {
		this.height = y;
		this.width = x;
		this.listHeight = 14 * this.list.size();
		if (this.listHeight > 0) { this.scrollHeight = (int) ((float) this.height * ((float) this.height - 2.0f) / (float) this.listHeight); }
		else { this.scrollHeight = Integer.MAX_VALUE; }
		this.maxScrollY = this.listHeight - this.height;
		if (this.maxScrollY<0) { this.maxScrollY = 0; }
	}

	public void setStacks(List<ItemStack> stacks) {
		this.stacks = stacks;
	}

	public GuiCustomScroll setUnselectable() {
		this.selectable = false;
		return this;
	}

	public void setUnsortedList(List<String> list) {
		if (this.isSameList(list)) {
			return;
		}
		this.isSorted = false;
		this.scrollY = 0;
		this.list = list;
		this.setSize(this.width, this.height);
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (!this.hovered || GuiNpcTextField.isActive()) { return; }
		if (this.list.size()<=1) { return; }
		if (i==200 || i==ClientProxy.frontButton.getKeyCode()) { // up
			if (this.selected<1) { return; }
			this.selected--;
			if (this.maxScrollY>0) {
				this.scrollY -= 14;
				if (this.scrollY<0) { this.scrollY = 0; }
			}
		}
		else if (i==208 || i==ClientProxy.backButton.getKeyCode()) { // down
			if (this.selected>=this.getList().size()-1) { return; }
			this.selected++;
			if (this.maxScrollY>0) {
				this.scrollY += 14;
				if (this.scrollY>this.maxScrollY) { this.scrollY = this.maxScrollY; }
			}
		}
	}

	public int getColor(int pos) {
		if (this.colors==null || this.colors.isEmpty()) { return 0; }
		return this.colors.get(pos);
	}

	public String getSuffixs(int pos) {
		if (this.suffixs==null || this.suffixs.isEmpty()) { return null; }
		return this.suffixs.get(pos);
	}
	
}
