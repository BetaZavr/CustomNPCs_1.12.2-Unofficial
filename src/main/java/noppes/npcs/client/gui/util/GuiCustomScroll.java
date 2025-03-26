package noppes.npcs.client.gui.util;

import java.util.*;
import java.util.function.Predicate;

import org.lwjgl.input.Mouse;

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
import noppes.npcs.util.Util;
import noppes.npcs.util.NaturalOrderComparator;

import javax.annotation.Nonnull;

public class GuiCustomScroll
extends GuiScreen
implements IComponentGui, IGuiCustomScroll {

	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/misc.png");

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
	protected int selected = -1;

	private long lastClickedTime = 0L;

	private HashSet<String> selectedList = new HashSet<>();
	private final List<String> list = new ArrayList<>();
	private List<Integer> colors = null;
	private List<String> suffixes;
	private List<ItemStack> stacks = null;
	private List<IResourceData> prefixes;

	private final Map<Integer, List<String>> hoversTexts = new TreeMap<>();
	private final List<String> hoverText = new ArrayList<>();

	// search data
	private final GuiNpcTextField textField = new GuiNpcTextField(0, null, 0, 0, 176, 16, "");
	private int listSize = 0;
	private boolean canSearch = true;
	private boolean hasSearch = false;
	private String searchString = "";
	private String[] searchWords = new String[0];

	public GuiCustomScroll(ICustomScrollListener parent, int scrollId) {
		id = scrollId;
		setParent(parent);
	}

	public GuiCustomScroll(ICustomScrollListener parent, boolean setSearch, int id) {
		this(parent, id);
		canSearch = setSearch;
	}

	public GuiCustomScroll(ICustomScrollListener parent, int id, boolean isMultipleSelection) {
		this(parent, id);
		multipleSelection = isMultipleSelection;
	}

	@Override
	public void canSearch(boolean setSearch) {
		canSearch = setSearch;
		reset();
	}

	@Override
	public void clear() {
		list.clear();
		selected = -1;
		scrollY = 0;
		searchString = "";
		searchWords = new String[0];
		textField.setText("");
		reset();
	}

	protected void drawItems() {
		int xOffset = (scrollHeight < height - 2) ? 0 : 10;
		int displayIndex = 0;
		for (int i = 0; i < list.size(); ++i) {
			if (!isSearched(list.get(i))) { continue; }
			int j = 4;
			int k = 14 * displayIndex + 4 - scrollY;
			displayIndex++;
			if (k < 4 || k + 10 > height) { continue; }
			String displayString = list.get(i) == null ? "null" : list.get(i);
			try { displayString = new TextComponentTranslation(displayString).getFormattedText(); } catch (Exception ignored) { }
			StringBuilder text = new StringBuilder();
			float maxWidth = width - xOffset - j - 2;
			if ((stacks != null && !stacks.isEmpty()) || (prefixes != null && !prefixes.isEmpty())) { maxWidth -= 12; }
			if (suffixes != null && !suffixes.get(i).isEmpty()) { maxWidth -= fontRenderer.getStringWidth(suffixes.get(i)) + 1; }
			if (fontRenderer.getStringWidth(displayString) > maxWidth) {
				for (int s = 0; s < displayString.length(); ++s) {
					if (fontRenderer.getStringWidth(text + "...") > maxWidth) { break; }
					char c = displayString.charAt(s);
					text.append(c);
				}
				text.append("...");
			}
			else { text = new StringBuilder(displayString); }
			int xo = 0;
			if ((stacks != null && i < stacks.size()) || (prefixes != null && i < prefixes.size())) {
				j = 14;
				xo = -14;
			}
			int c = CustomNpcs.MainColor.getRGB();
			if (colors != null && i < colors.size()) { c = colors.get(i); }
			if ((multipleSelection && Util.instance.containsDeleteColor(selectedList, text.toString(), false)) || (!multipleSelection && selected == i)) {
				drawVerticalLine(j - 2, k - 4, k + 10, -1);
				drawVerticalLine(j + width - 17 + xOffset + xo, k - 4, k + 10, -1);
				drawHorizontalLine(j - 2, j + width - 17 + xOffset + xo, k - 3, -1);
				drawHorizontalLine(j - 2, j + width - 17 + xOffset + xo, k + 10, -1);
				fontRenderer.drawString(text.toString(), j, k, c);
				c = CustomNpcs.MainColor.getRGB();
			} else if (i == hover) {
				fontRenderer.drawString(text.toString(), j, k, 65280);
				c = CustomNpcs.HoverColor.getRGB();
			} else {
				fontRenderer.drawString(text.toString(), j, k, c);
				c = CustomNpcs.MainColor.getRGB();
			}
			if (suffixes != null && i < suffixes.size() && suffixes.get(i) != null && !suffixes.get(i).isEmpty() && fontRenderer.getStringWidth(text + suffixes.get(i)) < width - 20) {
				fontRenderer.drawString(suffixes.get(i), width - xOffset - 2 - fontRenderer.getStringWidth(suffixes.get(i)), k, c);
			}
		}
	}

	private void drawPrefixes() {
		if (prefixes == null) { return; }
		int displayIndex = 0;
		for (int i = 0; i < list.size() && i < prefixes.size(); ++i) {
			if (!isSearched(list.get(i))) { continue; }
			int k = 14 * displayIndex + 4 - scrollY;
			displayIndex++;
			IResourceData rd = prefixes.get(i);
			if (k < 4 || k + 12 >= height || rd == null || rd.getResource() == null || rd.getWidth() <= 0 || rd.getHeight() <= 0) {
				continue;
			}
			boolean hasStack = stacks != null && !stacks.isEmpty() && i < stacks.size();
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop, 0.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(hasStack ? -13.0f : 0.5f, k - 1.5f + rd.getTextureHeight(), 0.0f); // position X, Y, Z
			float scale = 12.0f / (float) (Math.max(rd.getWidth(), rd.getHeight()));
			float scaleX = scale;
			float scaleY = scale;
			if (rd.getScaleX() != 0.0f || rd.getScaleY() != 0.0f) {
				scaleX *= rd.getScaleX();
				scaleY *= rd.getScaleY();
				GlStateManager.translate(12.0f * rd.getScaleX(), 6.0f * rd.getScaleY(), 0.0f);
			}
			GlStateManager.scale(scaleX, scaleY, 1.0f);
			Minecraft.getMinecraft().getTextureManager().bindTexture(rd.getResource());
			drawTexturedModalRect(0, 0, rd.getU(), rd.getV(), rd.getWidth(), rd.getHeight());
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		hovered = isMouseOver(mouseX, mouseY);
		drawScreen(mouseX, mouseY, !gui.hasSubGui() && !gui.hasArea());
	}

	public void drawScreen(int mouseX, int mouseY, boolean canScrolled) {
		if (hasSearch && listener instanceof IEditNPC) {
			textField.x = guiLeft;
			textField.y = guiTop;
			textField.render((IEditNPC) listener, mouseX, mouseY, 0.0f);
		}
		guiTop += textFieldHeight();
		// background
		if (border != 0xFF000000) { drawGradientRect(guiLeft - 1, guiTop - 1, width + guiLeft + 1, height + guiTop + 1, border, border); }
		drawGradientRect(guiLeft, guiTop, width + guiLeft, height + guiTop , colorBack, colorBack);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		// positions:
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTop, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (selectable) { hover = getMouseOver(mouseX, mouseY); }
		drawItems();
		GlStateManager.popMatrix();

		boolean parentAllows = listener == null || !listener.hasSubGui();
		if (parentAllows) {
			if (stacks != null) { drawStacks(); }
			if (prefixes != null) { drawPrefixes(); }
		}

		// scrolling
		if (scrollHeight <= height - 6) {
			// Bar
			drawScrollBar();
			// pos
			mouseX -= guiLeft;
			mouseY -= guiTop;
			isScrolling = Mouse.isButtonDown(0) && mouseX >= width - 9 && mouseX < width - 1 && mouseY >= 1 && mouseY < height - 1;
			if (isScrolling) {
				scrollY = (int) ((float) mouseY / ((float) height - 2.0f) * ((float) listHeight - (float) scrollHeight));
				if (scrollY < 0) { scrollY = 0; }
				if (scrollY > maxScrollY) { scrollY = maxScrollY; }
			}
			if (canScrolled && hovered) {
				int dWheel = Mouse.getDWheel();
				if (dWheel != 0) {
					scrollY += (dWheel > 0 ? -14 : 14);
					if (scrollY > maxScrollY) { scrollY = maxScrollY; }
					if (scrollY < 0) { scrollY = 0; }
				}
			}
		}
		if (hover >= 0 && hover < list.size() && parentAllows && listener instanceof IEditNPC) {
			if (hoversTexts.containsKey(hover)) { ((IEditNPC) listener).setHoverText(hoversTexts.get(hover)); }
			else if (!hoverText.isEmpty()) { ((IEditNPC) listener).setHoverText(hoverText); }
			else if (stacks != null && hover < stacks.size()) { ((IEditNPC) listener).setHoverText(stacks.get(hover).getTooltip(mc.player, TooltipFlags.NORMAL)); }
		}
		guiTop -= textFieldHeight();
	}

	private void drawScrollBar() {
		int posX = guiLeft + width - 9;
		int posY = guiTop + (int) ((float) scrollY / (float) listHeight * ((float) height - 2.0f)) + 1;
		Gui.drawRect(posX, posY, posX + 8, posY + scrollHeight + 1, 0xA0FFF0F0);
	}

	private void drawStacks() {
		if (stacks == null) {
			return;
		}
		int displayIndex = 0;
		for (int i = 0; i < list.size() && i < stacks.size(); ++i) {
			if (!isSearched(list.get(i))) { continue; }
			int k = 14 * displayIndex + 4 - scrollY;
			displayIndex++;
			if (k < 4 || k + 10 > height) { continue; }
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop, 0.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(0, k - 2.5f, 300.0f);
			GlStateManager.scale(0.75f, 0.75f, 0.75f);
			RenderHelper.enableGUIStandardItemLighting();
			mc.getRenderItem().renderItemAndEffectIntoGUI(stacks.get(i), 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
	}

	@Override
	public int getColor(int pos) {
		if (colors == null || colors.isEmpty()) { return 0; }
		return colors.get(pos);
	}

	public int getHeight() { return height; }

	@Override
	public void customKeyTyped(char c, int id) { keyTyped(c, id); }

	@Override
	public void customMouseClicked(int mouseX, int mouseY, int mouseButton) { mouseClicked(mouseX, mouseY, mouseButton); }

	@Override
	public void customMouseReleased(int mouseX, int mouseY, int mouseButton) { mouseReleased(mouseX, mouseY, mouseButton); }

	@Override
	public boolean isVisible() { return visible; }

	@Override
	public void setVisible(boolean bo) { visible = bo; }

	@Override
	public boolean isEnabled() { return true; }

	@Override
	public void setEnabled(boolean bo) { }

	@Override
	public boolean isMouseOver() { return hovered; }

	@Override
	public List<String> getList() { return list; }

	private int getMouseOver(int mouseX, int mouseY) {
		mouseX -= guiLeft;
		mouseY -= guiTop;
		if (mouseX >= 4 && mouseX < width - 4 && mouseY >= 4 && mouseY < height) {
			int displayIndex = 0;
			for (int pos = 0; pos < list.size(); ++pos) {
				if (isSearched(list.get(pos))) {
					if (mouseInOption(mouseX, mouseY, displayIndex)) { return pos; }
					++displayIndex;
				}
			}
		}
		return -1;
	}

	@Override
	public String getSelected() {
		if (selected == -1 || selected >= list.size()) { return null; }
		return list.get(selected);
	}

	@Override
	public HashSet<String> getSelectedList() { return selectedList; }

	@Override
	public int getWidth() { return width; }

	@Override
	public boolean hasSelected() { return selected >= 0 && getSelected() != null; }

	public boolean isMouseOver(int x, int y) {
		return x >= guiLeft && x <= guiLeft + width && y >= guiTop && y <= guiTop + height;
	}

	private boolean isSameList(List<String> checklist) {
		if (list.size() != checklist.size()) {
			return false;
		}
		for (int i = 0; i < checklist.size(); i++) {
			String s = list.get(i);
			if (!checklist.contains(s)) { return false; }
			String l = checklist.get(i);
			if (!s.equalsIgnoreCase(l)) { return false; }
		}
		return true;
	}

	@Override
	public void keyTyped(char c, int i) {
		if (hasSearch) {
			final boolean bo = textField.textboxKeyTyped(c, i);
			if (!searchString.equals(textField.getText())) {
				searchString = textField.getText().trim();
				searchWords = searchString.split(" ");
				reset();
			}
			if (bo) { return; }
		}
		if (!hovered || GuiNpcTextField.isActive()) { return; }
		if (list.size() <= 1) {
			return;
		}
		if (i == 200 || i == ClientProxy.frontButton.getKeyCode()) { // up
			if (selected < 1) {
				return;
			}
			selected--;
			if (maxScrollY > 0) {
				scrollY -= 14;
				if (scrollY < 0) {
					scrollY = 0;
				}
			}
			if (listener != null) {
				listener.scrollClicked(-1, -1, 0, this);
			}
		} else if (i == 208 || i == ClientProxy.backButton.getKeyCode()) { // down
			if (selected >= getList().size() - 1) {
				return;
			}
			selected++;
			if (maxScrollY > 0) {
				scrollY += 14;
				if (scrollY > maxScrollY) {
					scrollY = maxScrollY;
				}
			}
			if (listener != null) {
				listener.scrollClicked(-1, -1, 0, this);
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (hasSearch) { textField.mouseClicked(mouseX, mouseY, mouseButton); }
		if (mouseButton != 0 || hover < 0) { return; }
		if (multipleSelection) {
			if (selectedList.contains(list.get(hover))) {
				selectedList.remove(list.get(hover));
			} else {
				selectedList.add(list.get(hover));
			}
		} else {
			selected = hover;
			hover = -1;
		}
		if (listener != null) {
			long time = System.currentTimeMillis();
			listener.scrollClicked(mouseX, mouseY, mouseButton, this);
			if (selected >= 0 && selected == lastClickedItem && time - lastClickedTime < 500L) {
				listener.scrollDoubleClicked(list.get(selected), this);
			}
			lastClickedTime = time;
			lastClickedItem = selected;
		}
	}

	public boolean mouseInOption(int i, int j, int k) {
		int l = 4;
		int i2 = 14 * k + 4 - scrollY;
		return i >= l - 1 && i < l + width - 11 && j >= i2 - 1 && j < i2 + 8;
	}

	public void replace(String oldName, String newName) {
		if (!list.contains(oldName)) { return; }
		String select = getSelected();
		int i = list.indexOf(oldName);
		list.remove(oldName);
		list.add(i, newName);
		if (isSorted) { list.sort(new NaturalOrderComparator()); }
		if (oldName.equals(select)) { select = newName; }
		setSelected(select);
		reset();
	}

	@Override
	public void resetRoll() {
		if (selected <= 0) { return; }
		int pos = (selected + 1) * 14;
		if (pos >= scrollY && pos <= scrollY + height) { return; }
		scrollY = pos;
		if (pos - height / 2 > 0) { scrollY = pos - height / 2; }
		if (scrollY < 0) { scrollY = 0; }
		if (scrollY > maxScrollY) { scrollY = maxScrollY; }
	}

	@Override
	public void scrollTo(String name) {
		int i = list.indexOf(name);
		if (i < 0 || scrollHeight >= height - 2) { return; }
		int pos = (int) (1.0f * i / listSize * listHeight);
		if (pos > maxScrollY) { pos = maxScrollY; }
		scrollY = pos;
	}

	@Override
	public void setColors(List<Integer> newColors) { colors = newColors; }

	@Override
	public void setList(List<String> newList) {
		if (newList == null) { newList = new ArrayList<>(); }
		if (isSameList(newList)) { return; }
		isSorted = true;
		scrollY = 0;
		newList.sort(new NaturalOrderComparator());
		list.clear();
		list.addAll(newList);
		reset();
	}

	@Override
	public void setListNotSorted(List<String> newList) {
		if (isSameList(newList)) { return; }
		scrollY = 0;
		list.clear();
		list.addAll(newList);
		isSorted = false;
		reset();
	}

	@Override
	public void setPrefixes(List<IResourceData> newPrefixes) { prefixes = newPrefixes; }

	@Override
	public boolean hasSelected(String name) {
		if (name == null || name.isEmpty()) { return false; }
		if (list.contains(name)) { return true; }
		name = Util.instance.deleteColor(name);
        for (String s : list) {
            if (Util.instance.deleteColor(s).equalsIgnoreCase(name)) {
                return true;
            }
        }
		return false;
	}

	@Override
	public void setSelected(String name) {
		if (name == null || name.isEmpty()) { selected = -1; }
		selected = list.indexOf(name);
		if (selected == -1) {
			name = Util.instance.deleteColor(name);
			for (int i = 0; i < list.size(); i++) {
				if (Util.instance.deleteColor(list.get(i)).equalsIgnoreCase(name)) {
					selected = i;
					break;
				}
			}
		}
		resetRoll();
	}

	@Override
	public void setSelectedList(HashSet<String> newSelectedList) { selectedList = newSelectedList; }

	@Override
	public void setSize(int w, int h) {
		textField.width = w;
		height = h - textFieldHeight();
		width = w;
		listHeight = 14 * listSize;
		if (listHeight > 0) {
			scrollHeight = (int) Math.floor((float) height * ((float) height - 2.0f) / (float) listHeight);
		}
		else { scrollHeight = Integer.MAX_VALUE; }
		maxScrollY = listHeight - height;
		if (maxScrollY < 0) { maxScrollY = 0; }
		if (maxScrollY > 0 && scrollY > maxScrollY || maxScrollY == 0) {
			scrollY = 0;
		}
	}

	private int textFieldHeight() { return hasSearch ? textField.height + 2 : 0; }

	private void reset() {
		int h = textFieldHeight();
		hasSearch = canSearch && list.size() > 9;
		if (hasSearch) {
			if (searchString.isEmpty()) { listSize = list.size(); }
			else { listSize = (int) list.stream().filter((Predicate<? super String>)this::isSearched).count(); }
		}
		else {
			searchString = "";
			searchWords = new String[0];
			textField.setText("");
		}
		setSize(width, height + h);
	}

	private boolean isSearched(String s) {
		try { s = new TextComponentTranslation(s).getFormattedText(); } catch (Exception ignored) { }
        s = Util.instance.deleteColor(s);
		for (final String k : searchWords) {
			if (!s.toLowerCase().contains(Util.instance.deleteColor(k).toLowerCase())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setStacks(List<ItemStack> newStacks) { stacks = newStacks; }

	@Override
	public void setSuffixes(List<String> newSuffixes) { suffixes = newSuffixes; }

	@Override
	public IGuiCustomScroll setUnSelectable() {
		selectable = false;
		return this;
	}

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { guiLeft + width / 2, guiTop + height  / 2}; }

	@Override
	public void setHoverText(String text, Object ... args) {
		hoverText.clear();
		if (text == null || text.isEmpty()) { return; }
		if (!text.contains("%")) { text = new TextComponentTranslation(text, args).getFormattedText(); }
		if (text.contains("~~~")) { text = text.replaceAll("~~~", "%"); }
		while (text.contains("<br>")) {
			hoverText.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		hoverText.add(text);
	}

	@Override
	public void setHoverTexts(LinkedHashMap<Integer, List<String>> map) {
		hoversTexts.clear();
		if (map == null || map.isEmpty()) { return; }
		hoversTexts.putAll(map);
	}

	@Nonnull
	@Override
	public Map<Integer, List<String>> getHoversTexts() { return hoversTexts; }

	@Nonnull
	@Override
	public List<String> getHoversText() { return hoverText; }


	@Override
	public int getLeft() { return guiLeft; }

	@Override
	public int getTop() { return guiTop; }

	@Override
	public void setLeft(int left) { guiLeft = left; }

	@Override
	public void setTop(int top) { guiTop = top; }

	@Override
	public void setParent(ICustomScrollListener parent) { listener = parent; }

	@Override
	public int getSelect() { return selected; }

	@Override
	public void setSelect(int slotIndex) {
		if (slotIndex < 0) { selected = -1; }
		if (slotIndex >= list.size()) { return; }
		selected = slotIndex;
	}

}