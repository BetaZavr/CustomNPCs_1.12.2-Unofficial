package noppes.npcs.api.item;

public interface IItemBook extends IItemStack {
	String getAuthor();

	String[] getText();

	String getTitle();

	void setAuthor(String p0);

	void setText(String[] p0);

	void setTitle(String p0);
}
