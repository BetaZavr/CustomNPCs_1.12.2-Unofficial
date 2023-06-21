package noppes.npcs.api.gui;

public interface IScroll
extends ICustomGuiComponent {
	
	int getDefaultSelection();

	int getHeight();

	String[] getList();

	int getWidth();

	boolean isMultiSelect();

	IScroll setDefaultSelection(int defaultSelection);

	IScroll setList(String[] list);

	IScroll setMultiSelect(boolean multiSelect);

	IScroll setSize(int width, int height);
	
}
