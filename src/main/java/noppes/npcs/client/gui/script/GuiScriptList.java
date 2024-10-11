package noppes.npcs.client.gui.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.util.Util;

public class GuiScriptList extends SubGuiInterface implements ICustomScrollListener {

	private final ScriptContainer container;
	private final Map<String, Long> scripts;
	private final Map<ResourceLocation, String> data = Maps.newTreeMap();
	private GuiCustomScroll base;
	private GuiCustomScroll selected;
	private final String back = "   " + Character.toChars(0x2190)[0] + " (" + new TextComponentTranslation("gui.back").getFormattedText() + ")";
	private String path = "";

	public GuiScriptList(Map<String, Long> scripts, ScriptContainer container) {
		this.container = container;
		this.closeOnEsc = true;
		this.setBackground("menubg.png");
		this.xSize = 346;
		this.ySize = 216;
		if (scripts == null) { scripts = Maps.newTreeMap(); }
		this.scripts = scripts;
		for (String path : this.scripts.keySet()) {
			ResourceLocation res;
			if (path.contains("/")) {res = new ResourceLocation(path.substring(0, path.lastIndexOf("/")), path.substring(path.lastIndexOf("/") + 1)); }
			else { res = new ResourceLocation("base", path); }
			this.data.put(res, res.getResourcePath());
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		String file;
		if (button.id == 1 && this.base.hasSelected()) {
			try {
				file = this.base.hoversTexts[this.base.selected][0];
			} catch (Exception e) {
				return;
			}
			this.container.scripts.add(file);
			this.base.selected = -1;
			this.initGui();
		}
		if (button.id == 2 && this.selected.hasSelected()) {
			try {
				file = this.selected.hoversTexts[this.selected.selected][0];
			} catch (Exception e) {
				return;
			}
			this.container.scripts.remove(file);
			this.selected.selected = -1;
			this.initGui();
		}
		if (button.id == 3) {
			this.container.scripts.clear();
            this.container.scripts.addAll(this.scripts.keySet());
			this.base.selected = -1;
			this.initGui();
		}
		if (button.id == 4) {
			this.container.scripts.clear();
			this.base.selected = -1;
			this.initGui();
		}
		if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.base == null) {
			(this.base = new GuiCustomScroll(this, 0)).setSize(140, 180);
		}
		this.base.guiLeft = this.guiLeft + 4;
		this.base.guiTop = this.guiTop + 14;
		this.addScroll(this.base);
		this.addLabel(new GuiNpcLabel(1, "script.availableScripts", this.guiLeft + 4, this.guiTop + 4));
		if (this.selected == null) {
			(this.selected = new GuiCustomScroll(this, 1)).setSize(140, 180);
		}
		this.selected.guiLeft = this.guiLeft + 200;
		this.selected.guiTop = this.guiTop + 14;
		this.addScroll(this.selected);
		this.addLabel(new GuiNpcLabel(2, "script.loadedScripts", this.guiLeft + 200, this.guiTop + 4));

		String p = ".../" + this.path;
		if (this.path.length() > 20) {
			p = "..." + this.path.substring(this.path.length() - 20);
		}
		this.addLabel(new GuiNpcLabel(3, ((char) 167) + "0" + ((char) 167) + "l" + p, this.guiLeft + 4, this.guiTop + 16 + this.base.height));

		List<String> temp = new ArrayList<>(this.scripts.keySet());
		temp.removeAll(this.container.scripts);

		Map<String, Long> ds = Maps.newTreeMap();
		Map<String, Long> fs = Maps.newTreeMap();

		Map<String, Long> ft = Maps.newTreeMap();

		List<String> listBase = Lists.newArrayList();
		List<Integer> colorsBase = Lists.newArrayList();
		List<String> suffixsBase = Lists.newArrayList();
		List<String> list = Lists.newArrayList();
		List<Integer> colors = Lists.newArrayList();
		List<String> suffixs = Lists.newArrayList();

		Map<String, String> hs = Maps.newTreeMap();
		char c = ((char) 167);
		int t = 1;
		if (!this.path.isEmpty()) {
			ds.put(this.back, 0L);
		}
		for (ResourceLocation res : this.data.keySet()) {
			String key = this.data.get(res);
			boolean hasDir = !res.getResourceDomain().equals("base");
			String file = (hasDir ? res.getResourceDomain() + "/" : "") + res.getResourcePath();
			if (temp.contains(file) || this.container.scripts.contains(file)) {
				boolean isBase = temp.contains(file);
				if (isBase) {
					String folder = hasDir ? res.getResourceDomain() : "";
					if (folder.isEmpty() && this.path.isEmpty()) {
						fs.put(key, this.scripts.get(file));
						hs.put(key, file);
						continue;
					}
					if (folder.isEmpty()) {
						continue;
					}
					if (!this.path.isEmpty()) {
						if (folder.indexOf(this.path) != 0) {
							continue;
						}
						folder = folder.replace(this.path, "");
						if (folder.indexOf("/") == 0) {
							folder = folder.substring(1);
						}
					}
					if (folder.contains("/")) {
						folder = folder.substring(0, folder.indexOf("/"));
					}

					if (this.path.isEmpty() && !folder.isEmpty()) {
						ds.put(folder, 0L);
						hs.put(folder, folder);
						continue;
					}
					if (folder.isEmpty()) {
						fs.put(key, this.scripts.get(file));
						hs.put(key, file);
					} else {
						ds.put(folder, 0L);
						hs.put(folder, this.path + (this.path.isEmpty() ? "" : "/") + folder);
					}
				} else {
					ft.put(c + "7" + t + ":" + c + "r " + key, this.scripts.get(file));
					hs.put(c + "7" + t + ":" + c + "r " + key, file);
					t++;
				}
			}
		}

		this.base.hoversTexts = new String[ds.size() + fs.size()][];
		int i = 0;
		for (String key : ds.keySet()) {
			colorsBase.add(0xF3BE1E);
			suffixsBase.add("");
			listBase.add(key);
			if (hs.containsKey(key)) {
				this.base.hoversTexts[i] = new String[] { hs.get(key) };
			}
			i++;
		}
		for (String key : fs.keySet()) {
			long l = fs.get(key);
			colorsBase.add(l >= 0 ? 0xCAEAEA : 0xEAEACA);
			if (l < 0L) {
				l *= -1L;
			}
			String size = "" + l;
			if (l > 999) {
				size = Util.instance.getTextReducedNumber(l, false, false, true);
			}
			suffixsBase.add(size + "b");
			listBase.add(key);
			if (hs.containsKey(key)) {
				if (fs.get(key) < 0) {
					this.base.hoversTexts[i] = new String[] { hs.get(key), ((char) 167) + "4" + new TextComponentTranslation("gui.encrypted").getFormattedText() };
				} else {
					this.base.hoversTexts[i] = new String[] { hs.get(key) };
				}
			}
			i++;
		}

		this.selected.hoversTexts = new String[ft.size()][];
		i = 0;
		for (String key : ft.keySet()) {
			long l = ft.get(key);
			colors.add(l >= 0 ? 0xCAEAEA : 0xEAEACA);
			if (l < 0L) {
				l *= -1L;
			}
			String size = "" + l;
			
			if (l > 999) {
				size = Util.instance.getTextReducedNumber(l, false, false, true);
			}
			suffixs.add(size + "b");
			list.add(key);
			if (hs.containsKey(key)) {
				if (ft.get(key) < 0) {
					this.selected.hoversTexts[i] = new String[] { hs.get(key),
							((char) 167) + "4" + new TextComponentTranslation("gui.encrypted").getFormattedText() };
				} else {
					this.selected.hoversTexts[i] = new String[] { hs.get(key) };
				}
			}
			i++;
		}

		this.base.setColors(colorsBase);
		this.base.setSuffixes(suffixsBase);
		this.base.setListNotSorted(listBase);

		this.selected.setColors(colors);
		this.selected.setSuffixes(suffixs);
		this.selected.setListNotSorted(list);
		int x = this.guiLeft + 145, y = this.guiTop + 40;
		this.addButton(new GuiNpcButton(1, x, y, 55, 20, ">", this.base.hasSelected()));
		this.addButton(new GuiNpcButton(2, x, (y += 22), 55, 20, "<", this.selected.hasSelected()));
		this.addButton(new GuiNpcButton(3, x, (y += 44), 55, 20, ">>", !temp.isEmpty()));
		this.addButton(new GuiNpcButton(4, x, (y += 22), 55, 20, "<<", !this.container.scripts.isEmpty()));
		this.addButton(new GuiNpcButton(66, x, (y + 46), 55, 20, "gui.done"));
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			if (scroll.getSelected().equals(this.back)) {
				if (this.path.lastIndexOf("/") == -1) {
					this.path = "";
				} else {
					this.path = this.path.substring(0, this.path.lastIndexOf("/"));
				}
				this.base.selected = -1;
			} else if (scroll.getColor(scroll.selected) == 0xF3BE1E) {
				if (!this.path.isEmpty()) {
					this.path += "/";
				}
				this.path += scroll.getSelected();
				this.base.selected = -1;
			}
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		String file = "";
		try {
			file = scroll.hoversTexts[scroll.selected][0];
		} catch (Exception e) { LogWriter.error("Error:", e); }
		if (file.isEmpty()) {
			return;
		}
		if (scroll.id == 0) {
			this.container.scripts.add(file);
			this.base.selected = -1;
			this.initGui();
		}
		if (scroll.id == 1) {
			this.container.scripts.remove(file);
			this.selected.selected = -1;
			this.initGui();
		}
	}

}
