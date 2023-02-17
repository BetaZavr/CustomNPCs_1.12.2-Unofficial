package noppes.npcs.api.gui;

public interface IScroll extends ICustomGuiComponent {
	int getDefaultSelection();

	int getHeight();

	String[] getList();

	int getWidth();

	boolean isMultiSelect();

	IScroll setDefaultSelection(int p0);

	IScroll setList(String[] p0);

	IScroll setMultiSelect(boolean p0);

	IScroll setSize(int p0, int p1);
}
