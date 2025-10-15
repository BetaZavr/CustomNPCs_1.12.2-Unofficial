package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IScroll extends ICustomGuiComponent {

	int getDefaultSelection();

	int getHeight();

	String[] getList();

	int getWidth();

	boolean isMultiSelect();

	IScroll setDefaultSelection(@ParamName("defaultSelection") int defaultSelection);

	IScroll setList(@ParamName("list") String[] list);

	IScroll setMultiSelect(@ParamName("multiSelect") boolean multiSelect);

	IScroll setSize(@ParamName("width") int width, @ParamName("height") int height);

}
