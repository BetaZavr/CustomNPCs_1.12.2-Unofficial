package noppes.npcs.client.gui.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.client.renderer.RenderHelper;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.client.util.ResourceData;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.util.Util;
import noppes.npcs.util.NaturalOrderComparator;

import javax.annotation.Nonnull;

public class GuiCustomScroll extends GuiScreen implements IComponentGui {

	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/misc.png");

	// main
	public int id;
	public int guiLeft = 0;
	public int guiTop = 0;
	protected int hover = -1;
	protected final ICustomScrollListener listener;
	// data
	protected final List<String> list = new ArrayList<>();
	protected final List<Integer> selectedList = new ArrayList<>();
	protected boolean selectable = true;
	protected int listSize = 0;
	protected int selected = -1;
	public boolean multipleSelection = false;
	// scroll vars
	protected final GuiNpcTextField textField = new GuiNpcTextField(0, null, 0, 0, 176, 16, "");
	protected int listHeight = 0;
	protected int scrollY = 0;
	protected int maxScrollY;
	protected int scrollHeight = 0;
	protected boolean isSorted = true;
	protected boolean mouseInList = false;
	protected boolean hasSearch = true;
	protected boolean canSearch = true;
	protected int lastClickedItem = -1;
	protected long lastClickedTime = 0L;
	protected String searchStr = "";
	protected String[] searchWords = new String[0];
	public boolean visible = true;

	// New from Unofficial (BetaZavr)
	protected boolean isScrolling = false;
	protected List<String> suffixes;
	protected List<ResourceData> prefixes;
	protected List<ItemStack> stacks = null;
	protected List<Integer> colors = null;
	protected final Map<Integer, List<String>> hoversTexts = new TreeMap<>();
	protected final List<String> hoverText = new ArrayList<>();
	public int colorBackS = 0xC0101010;
	public int colorBackE = 0xE0101010;
	public int border = 0xFF000000;

	public GuiCustomScroll(ICustomScrollListener parent, int idIn) {
		id = idIn;
		listener = parent;
	}

	public GuiCustomScroll(ICustomScrollListener parent, int id, boolean setSearch, boolean isMultipleSelection) {
		this(parent, id);
		canSearch = setSearch;
		multipleSelection = isMultipleSelection;
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		mouseInList = isMouseOver(mouseX, mouseY);
		drawScreen(mouseX, mouseY, !gui.hasSubGui() && !gui.hasArea());
	}

	@Override
	public boolean isHovered() { return mouseInList; }

	@Override
	public GuiCustomScroll setIsEnable(boolean isEnable) {
		selectable = isEnable;
		return this;
	}

	@Override
	public List<String> getHoversText() { return hoverText; }

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { guiLeft + width / 2, guiTop + height  / 2}; }

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (hasSearch && textField.isFocused()) {
			final boolean bo = textField.textboxKeyTyped(typedChar, keyCode);
			if (!searchStr.equals(textField.getText())) {
				searchStr = textField.getText().trim();
				searchWords = searchStr.split(" ");
				reset();
			}
			return bo;
		}
		if (list.size() <= 1) { return false; }
		boolean canPressed = !GuiNpcTextField.isActive();
		if (canPressed) {
			if (listener instanceof IEditNPC && !((IEditNPC) listener).hasSubGui()) {
				if (listener instanceof GuiNPCInterface) {
					canPressed = ((GuiNPCInterface) listener).scrolls.size() < 2 || mouseInList; }
				else if (listener instanceof GuiContainerNPCInterface) {
					canPressed = ((GuiContainerNPCInterface) listener).scrolls.size() < 2 || mouseInList;
				}
			}
		}
		if (canPressed) {
			if (keyCode == Keyboard.KEY_UP || keyCode == mc.gameSettings.keyBindForward.getKeyCode()) { // up
				if (multipleSelection) { scrollY = ValueUtil.correctInt(scrollY - 14, 0, maxScrollY); }
				else {
					if (selected < 1) { return false; }
					selected--;
					resetRoll();
					listener.scrollClicked(-1, -1, 0, this);
				}
				return true;
			}
			else if (keyCode == Keyboard.KEY_DOWN || keyCode == mc.gameSettings.keyBindBack.getKeyCode()) { // down
				if (multipleSelection) { scrollY = ValueUtil.correctInt(scrollY + 14, 0, maxScrollY); }
				else {
					if (selected >= getList().size() - 1) { return false; }
					selected++;
					resetRoll();
					listener.scrollClicked(-1, -1, 0, this);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (hasSearch) { textField.mouseCnpcsPressed(mouseX, mouseY, mouseButton); }
		if (scrollHeight < height - 2) {
			double x = mouseX - guiLeft;
			double y = mouseY - guiTop;
			int h = height;
			if (hasSearch) { h += 19; }
			isScrolling = x >= width - 10 && x < width - 1 && y >= 1 && y < h - 2;
			if (isScrolling) { return true; }
		}
		if (mouseButton != 0 || hover < 0) { return false; }
		if (multipleSelection) {
			if (selectedList.contains(hover)) { selectedList.removeIf(value -> value == hover); }
			else { selectedList.add(hover); }
		}
		else {
			selected = hover;
			hover = -1;
		}
		if (listener != null) { listener.scrollClicked(mouseX, mouseY, mouseButton, this); }
		long time = System.currentTimeMillis();
		if (listener != null && selected >= 0 && selected == lastClickedItem && time - lastClickedTime < 500L) { listener.scrollDoubleClicked(list.get(selected), this); }
		lastClickedTime = time;
		lastClickedItem = selected;
		return true;
	}

	@Override
	public boolean mouseCnpcsReleased(int mouseX, int mouseY, int state) { return false; }

	@Override
	public GuiCustomScroll setHoverText(String text, Object ... args) {
		hoverText.clear();
		if (text == null || text.isEmpty()) { return this; }
		if (!text.contains("%")) { text = new TextComponentTranslation(text, args).getFormattedText(); }
		if (text.contains("~~~")) { text = text.replaceAll("~~~", "%"); }
		while (text.contains("<br>")) {
			hoverText.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		hoverText.add(text);
		return this;
	}

	@Override
	public GuiCustomScroll setIsVisible(boolean bo) { visible = bo; return this; }

	@Override
	public void moveTo(int addX, int addY) {
		guiLeft += addX;
		guiTop += addY;
	}

	@Override
	public void updateCnpcsScreen() { }

	public void drawScreen(int mouseX, int mouseY, boolean canScrolled) {
		if (hasSearch && listener instanceof IEditNPC) {
			textField.x = guiLeft;
			textField.y = guiTop;
			textField.render((IEditNPC) listener, mouseX, mouseY, 0.0f);
		}
		guiTop += textFieldHeight();

		// add elements
		boolean parentAllows = !(listener instanceof IEditNPC) || !((IEditNPC) listener).hasSubGui();
		if (parentAllows) {
			if (prefixes != null) { drawPrefixes(); }
			if (stacks != null) { drawStacks(); }
		}

		// background
		if (border != 0xFF000000) { drawGradientRect(guiLeft - 1, guiTop - 1, width + guiLeft + 1, height + guiTop + 1, border, border); }
		if ((colorBackS >> 24 & 255) > 0 || (colorBackE >> 24 & 255) > 0) {
			drawGradientRect(guiLeft, guiTop, width + guiLeft, height + guiTop , colorBackS, colorBackE);
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		// positions:
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTop, 0.0f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (selectable) { hover = getMouseOver(mouseX, mouseY); }
		drawItems();
		GlStateManager.popMatrix();

		// scrolling
		if (scrollHeight <= height - 6) {
			// Bar
			drawScrollBar();
			// pos
			//mouseX -= guiLeft;
			mouseY -= guiTop;
			if (isScrolling) {
				isScrolling = Mouse.isButtonDown(0);
				if (isScrolling) {
					scrollY = (int) ((float) mouseY / ((float) height - 2.0f) * ((float) listHeight - (float) scrollHeight));
					if (scrollY < 0) { scrollY = 0; }
					if (scrollY > maxScrollY) { scrollY = maxScrollY; }
				}
			}
			if (canScrolled && mouseInList) {
				int dWheel = Mouse.getDWheel();
				if (dWheel != 0) {
					scrollY += (dWheel > 0 ? -14 : 14);
					if (scrollY > maxScrollY) { scrollY = maxScrollY; }
					if (scrollY < 0) { scrollY = 0; }
				}
			}
		}
		if (hover >= 0 && hover < list.size() && parentAllows && listener instanceof IEditNPC) {
			if (hoversTexts.containsKey(hover)) { ((IEditNPC) listener).putHoverText(hoversTexts.get(hover)); }
			else if (!hoverText.isEmpty()) { ((IEditNPC) listener).putHoverText(hoverText); }
			else if (stacks != null && hover < stacks.size()) { ((IEditNPC) listener).putHoverText(stacks.get(hover).getTooltip(mc.player, TooltipFlags.NORMAL)); }
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

	public int getColor(int pos) {
		if (colors == null || colors.isEmpty()) { return 0; }
		return colors.get(pos);
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

	public void clear() {
		list.clear();
		selectedList.clear();
		selected = -1;
		scrollY = 0;
		searchStr = "";
		searchWords = new String[0];
		textField.setText("");
		reset();
	}

	protected void drawItems() {
		int xOffset = (scrollHeight <= height - 6) ? 0 : 10;
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
			if (suffixes != null && i < suffixes.size() && !suffixes.get(i).isEmpty()) {
				maxWidth -= fontRenderer.getStringWidth(suffixes.get(i)) + 1;
			}
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
			if ((multipleSelection && selectedList.contains(i)) || (!multipleSelection && selected == i)) {
				int r = j + width - 15 + xOffset + xo;
				drawVerticalLine(j - 2, k - 4, k + 10, -1);
				drawVerticalLine(r, k - 4, k + 10, -1);
				drawHorizontalLine(j - 2, r, k - 3, -1);
				drawHorizontalLine(j - 2, r, k + 10, -1);
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
				fontRenderer.drawString(suffixes.get(i), width - 11 - fontRenderer.getStringWidth(suffixes.get(i)), k, c);
			}
		}
	}

	private void drawPrefixes() {
		if (prefixes == null || mc == null) { return; }
		int size = Math.min(list.size(), prefixes.size());
		if (size == 0) { return; }
		int displayIndex = 0;
		for (int i = 0; i < list.size() && i < prefixes.size(); ++i) {
			if (!isSearched(list.get(i))) { continue; }
			ResourceData rd = prefixes.get(i);
			int k = 14 * displayIndex + 4 - scrollY;
			displayIndex++;
			if (rd == null || rd.resource == null || rd.width <= 0 || rd.height <= 0) { continue; }
			if (k < 4 || k + 12 >= height) { continue; }
			GlStateManager.pushMatrix();
			if (rd.isOBJ()) {
				GlStateManager.translate(guiLeft + 5.0f + rd.tW, guiTop + k + 3.0f + rd.tH, rd.tD);
				if (rd.rotateX != 0.0f) { GlStateManager.rotate(rd.rotateX, 1.0f, 0.0f, 0.0f); }
				if (rd.rotateY != 0.0f) { GlStateManager.rotate(rd.rotateY, 0.0f, 1.0f, 0.0f); }
				if (rd.rotateZ != 0.0f) { GlStateManager.rotate(rd.rotateZ, 0.0f, 0.0f, 1.0f); }
				GlStateManager.scale(rd.scaleX, rd.scaleY, rd.scaleZ);
				GlStateManager.callList(ModelBuffer.getDisplayList(rd.resource, null, rd.materialTextures));
			}
			else {
				boolean hasStack = stacks != null && !stacks.isEmpty() && i < stacks.size();
				GlStateManager.translate(guiLeft + (hasStack ? -13.0f : 0.5f) + rd.tW, guiTop + k - 1.5f + rd.tH, rd.tD);
				float scale = 12.0f / (float) (Math.max(rd.width, rd.height));
				float scaleX = scale;
				float scaleY = scale;
				if (rd.scaleX != 0.0f || rd.scaleY != 0.0f) {
					scaleX *= rd.scaleX;
					scaleY *= rd.scaleY;
					GlStateManager.translate(12.0f * rd.scaleX, 6.0f * rd.scaleY, 0.0f);
				}
				GlStateManager.scale(scaleX, scaleY, 1.0f);
				mc.getTextureManager().bindTexture(rd.resource);
				drawTexturedModalRect(0, 0, rd.u, rd.v, rd.width, rd.height);
			}
			GlStateManager.popMatrix();
		}
	}

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

	public String getSelected() {
		if (selected == -1 || selected >= list.size()) { return null; }
		return list.get(selected);
	}

	public List<String> getSelectedList() {
		return IntStream.range(0, list.size())
				.filter(selectedList::contains)
				.mapToObj(list::get)
				.collect(Collectors.toList());
	}

	public boolean hasSelected() { return selected >= 0 && getSelected() != null; }

	public boolean isMouseOver(int x, int y) {
		return x >= guiLeft && x <= guiLeft + width && y >= guiTop && y <= guiTop + height;
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

	public void scrollTo(String name) {
		int i = list.indexOf(name);
		if (i >= 0 && scrollHeight < height - 2) {
			int pos = (int)((float) i / (float) list.size() * (float) listHeight);
			if (pos > maxScrollY) { pos = maxScrollY; }
			scrollY = pos;
		}
	}

	public void resetRoll() {
		if (selected < 0 || scrollHeight >= height - 2) { return; }
		int pos = (int)((float) selected / (float) list.size() * (float) listHeight);
		if (pos < scrollY) { scrollY = pos; }
		else {
			while (pos >= scrollY + height - 14) { scrollY += 14; }
			if (scrollY > maxScrollY) { scrollY = maxScrollY; }
		}
	}

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

	@SuppressWarnings("all")
	public GuiCustomScroll canSearch(boolean setSearch) {
		canSearch = setSearch;
		reset();
		return this;
	}

	public GuiCustomScroll setColors(List<Integer> newColors) { colors = newColors; return this; }

	public GuiCustomScroll setList(List<String> newList) {
		if (newList == null) { newList = new ArrayList<>(); }
		if (isSameList(newList)) { return this; }
		isSorted = true;
		newList.sort(new NaturalOrderComparator());
		list.clear();
		list.addAll(newList);
		searchStr = "";
		listSize = list.size();
		reset();
		return this;
	}

	public GuiCustomScroll setUnsortedList(List<String> newList) {
		if (isSameList(newList)) { return this; }
		list.clear();
		list.addAll(newList);
		searchStr = "";
		listSize = list.size();
		isSorted = false;
		reset();
		return this;
	}

	public GuiCustomScroll setPrefixes(List<ResourceData> newPrefixes) { prefixes = newPrefixes; return this; }

	public GuiCustomScroll setSelected(String name) {
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
		return this;
	}

	public GuiCustomScroll setSelectedList(HashSet<String> newSelectedList) {
		int i = 0;
        newSelectedList.removeIf(line -> !list.contains(line));
		selectedList.clear();
		for (String line : list) {
			for (String str : newSelectedList) {
				if (line.equals(str)) {
					selectedList.add(i);
					break;
				}
			}
			i++;
		}
		return this;
	}

	public GuiCustomScroll setSize(int w, int h) {
		textField.width = w;
		height = h - textFieldHeight();
		width = w;
		listHeight = 14 * listSize;
		if (listHeight > 0) { scrollHeight = (int) Math.floor((float) height * ((float) height - 2.0f) / (float) listHeight); }
		else { scrollHeight = Integer.MAX_VALUE; }
		maxScrollY = listHeight - height;
		if (maxScrollY < 0) { maxScrollY = 0; }
		resetRoll();
		return this;
	}

	public GuiCustomScroll setStacks(List<ItemStack> newStacks) { stacks = newStacks; return this; }

	public GuiCustomScroll setSuffixes(List<String> newSuffixes) { suffixes = newSuffixes; return this; }

	public GuiCustomScroll setHoverTexts(LinkedHashMap<Integer, List<String>> map) {
		hoversTexts.clear();
		if (map == null || map.isEmpty()) { return this; }
		hoversTexts.putAll(map);
		return this;
	}
	public GuiCustomScroll setSelect(int slotIndex) {
		if (slotIndex < 0) { selected = -1; }
		if (slotIndex >= list.size()) { return this; }
		selected = slotIndex;
		return this;
	}

	private int textFieldHeight() { return hasSearch ? textField.height + 2 : 0; }

	private void reset() {
		int h = textFieldHeight();
		hasSearch = canSearch && list.size() > 9;
		if (hasSearch) {
			if (searchStr.isEmpty()) { listSize = list.size(); }
			else { listSize = (int) list.stream().filter((Predicate<? super String>)this::isSearched).count(); }
		}
		else {
			searchStr = "";
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

	public @Nonnull Map<Integer, List<String>> getHoversTexts() { return hoversTexts; }

	public int getSelect() { return selected; }

}