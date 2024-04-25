package noppes.npcs.api.item;

public interface IItemBook extends IItemStack {

	String getAuthor();

	String[] getText();

	String getTitle();

	void setAuthor(String author);

	void setText(String[] pages);

	void setTitle(String title);

}
