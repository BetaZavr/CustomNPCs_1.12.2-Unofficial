package noppes.npcs.client.gui.script;

import java.util.*;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.util.Util;

public class GuiScriptList
extends SubGuiInterface
implements ICustomScrollListener {

	private final ScriptContainer container;
	private final Map<String, Long> scripts;
	private final Map<ResourceLocation, String> data = new TreeMap<>();
	private GuiCustomScroll base;
	private GuiCustomScroll selected;
	private final String back = "   " + Character.toChars(0x2190)[0] + " (" + new TextComponentTranslation("gui.back").getFormattedText() + ")";
	private String path = "";

	public GuiScriptList(Map<String, Long> scriptsList, ScriptContainer cont) {
		super();
		closeOnEsc = true;
		setBackground("menubg.png");
		xSize = 346;
		ySize = 216;

		container = cont;
		if (scriptsList == null) { scriptsList = new TreeMap<>(); }
		scripts = scriptsList;
		for (String path : scripts.keySet()) {
			ResourceLocation res;
			if (path.contains("/")) {res = new ResourceLocation(path.substring(0, path.lastIndexOf("/")), path.substring(path.lastIndexOf("/") + 1)); }
			else { res = new ResourceLocation("base", path); }
			data.put(res, res.getResourcePath());
		}
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		String file;
		if (button.getId() == 1 && base.hasSelected()) {
			try {
				file = base.getHoversTexts().get(base.getSelect()).get(0);
			} catch (Exception e) {
				return;
			}
			container.scripts.add(file);
			base.setSelect(-1);
			initGui();
		}
		if (button.getId() == 2 && selected.hasSelected()) {
			try {
				file = selected.getHoversTexts().get(selected.getSelect()).get(0);
			} catch (Exception e) {
				return;
			}
			container.scripts.remove(file);
			selected.setSelect(-1);
			initGui();
		}
		if (button.getId() == 3) {
			container.scripts.clear();
            container.scripts.addAll(scripts.keySet());
			base.setSelect(-1);
			initGui();
		}
		if (button.getId() == 4) {
			container.scripts.clear();
			base.setSelect(-1);
			initGui();
		}
		if (button.getId() == 66) {
			close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (base == null) {
			(base = new GuiCustomScroll(this, 0)).setSize(140, 180);
		}
		base.guiLeft = guiLeft + 4;
		base.guiTop = guiTop + 14;
		addScroll(base);
		addLabel(new GuiNpcLabel(1, "script.availableScripts", guiLeft + 4, guiTop + 4));
		if (selected == null) {
			(selected = new GuiCustomScroll(this, 1)).setSize(140, 180);
		}
		selected.guiLeft = guiLeft + 200;
		selected.guiTop = guiTop + 14;
		addScroll(selected);
		addLabel(new GuiNpcLabel(2, "script.loadedScripts", guiLeft + 200, guiTop + 4));

		String p = ".../" + path;
		if (path.length() > 20) {
			p = "..." + path.substring(path.length() - 20);
		}
		addLabel(new GuiNpcLabel(3, ((char) 167) + "0" + ((char) 167) + "l" + p, guiLeft + 4, guiTop + 16 + base.height));

		List<String> temp = new ArrayList<>(scripts.keySet());
		temp.removeAll(container.scripts);

		Map<String, Long> ds = new TreeMap<>();
		Map<String, Long> fs = new TreeMap<>();

		Map<String, Long> ft = new TreeMap<>();

		Map<String, String> hs = new TreeMap<>();

		List<String> listBase = new ArrayList<>();
		List<Integer> colorsBase = new ArrayList<>();
		List<String> suffixesBase = new ArrayList<>();
		List<String> list = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		List<String> suffixes = new ArrayList<>();

		char c = ((char) 167);
		int t = 1;
		if (!path.isEmpty()) { ds.put(back, 0L); }
		for (ResourceLocation res : data.keySet()) {
			String key = data.get(res);
			boolean hasDir = !res.getResourceDomain().equals("base");
			String file = (hasDir ? res.getResourceDomain() + "/" : "") + res.getResourcePath();
			if (temp.contains(file) || container.scripts.contains(file)) {
				boolean isBase = temp.contains(file);
				if (isBase) {
					String folder = hasDir ? res.getResourceDomain() : "";
					if (folder.isEmpty() && path.isEmpty()) {
						fs.put(key, scripts.get(file));
						hs.put(key, file);
						continue;
					}
					if (folder.isEmpty()) {
						continue;
					}
					if (!path.isEmpty()) {
						if (folder.indexOf(path) != 0) {
							continue;
						}
						folder = folder.replace(path, "");
						if (folder.indexOf("/") == 0) {
							folder = folder.substring(1);
						}
					}
					if (folder.contains("/")) {
						folder = folder.substring(0, folder.indexOf("/"));
					}

					if (path.isEmpty() && !folder.isEmpty()) {
						ds.put(folder, 0L);
						hs.put(folder, folder);
						continue;
					}
					if (folder.isEmpty()) {
						fs.put(key, scripts.get(file));
						hs.put(key, file);
					} else {
						ds.put(folder, 0L);
						hs.put(folder, path + "/" + folder);
					}
				} else {
					ft.put(c + "7" + t + ":" + c + "r " + key, scripts.get(file));
					hs.put(c + "7" + t + ":" + c + "r " + key, file);
					t++;
				}
			}
		}

		LinkedHashMap<Integer, List<String>> htsB= new LinkedHashMap<>();
		int i = 0;
		for (String key : ds.keySet()) {
			colorsBase.add(0xF3BE1E);
			suffixesBase.add("");
			listBase.add(key);
			if (hs.containsKey(key)) {
				htsB.put(i, Collections.singletonList(hs.get(key)));
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
			suffixesBase.add(size + "b");
			listBase.add(key);
			if (hs.containsKey(key)) {
				if (fs.get(key) < 0) {
					List<String> hl = new ArrayList<>();
					hl.add(hs.get(key));
					hl.add(((char) 167) + "4" + new TextComponentTranslation("gui.encrypted").getFormattedText());
					htsB.put(i, hl);
				} else {
					htsB.put(i, Collections.singletonList(hs.get(key)));
				}
			}
			i++;
		}

		LinkedHashMap<Integer, List<String>> htsS= new LinkedHashMap<>();
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
			suffixes.add(size + "b");
			list.add(key);
			if (hs.containsKey(key)) {
				if (ft.get(key) < 0) {
					List<String> hl = new ArrayList<>();
					hl.add(hs.get(key));
					hl.add(((char) 167) + "4" + new TextComponentTranslation("gui.encrypted").getFormattedText());
					htsS.put(i, hl);
				} else {
					htsS.put(i, Collections.singletonList(hs.get(key)));
				}
			}
			i++;
		}

		base.setColors(colorsBase);
		base.setSuffixes(suffixesBase);
		base.setListNotSorted(listBase);
		base.setHoverTexts(htsB);

		selected.setColors(colors);
		selected.setSuffixes(suffixes);
		selected.setListNotSorted(list);
		selected.setHoverTexts(htsS);
		int x = guiLeft + 145, y = guiTop + 40;
		addButton(new GuiNpcButton(1, x, y, 55, 20, ">", base.hasSelected()));
		addButton(new GuiNpcButton(2, x, (y += 22), 55, 20, "<", selected.hasSelected()));
		addButton(new GuiNpcButton(3, x, (y += 44), 55, 20, ">>", !temp.isEmpty()));
		addButton(new GuiNpcButton(4, x, (y += 22), 55, 20, "<<", !container.scripts.isEmpty()));
		addButton(new GuiNpcButton(66, x, (y + 46), 55, 20, "gui.done"));
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getId() == 0) {
			if (scroll.getSelected().equals(back)) {
				if (path.lastIndexOf("/") == -1) {
					path = "";
				} else {
					path = path.substring(0, path.lastIndexOf("/"));
				}
				base.setSelect(-1);
			} else if (scroll.getColor(scroll.getSelect()) == 0xF3BE1E) {
				if (!path.isEmpty()) {
					path += "/";
				}
				path += scroll.getSelected();
				base.setSelect(-1);
			}
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		String file = "";
		try {
			file = scroll.getHoversTexts().get(scroll.getSelect()).get(0);
		} catch (Exception e) { LogWriter.error("Error:", e); }
		if (file.isEmpty()) {
			return;
		}
		if (scroll.getId() == 0) {
			container.scripts.add(file);
			base.setSelect(-1);
			initGui();
		}
		if (scroll.getId() == 1) {
			container.scripts.remove(file);
			selected.setSelect(-1);
			initGui();
		}
	}

}
