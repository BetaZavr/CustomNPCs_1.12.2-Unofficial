package noppes.npcs.api.item;

import noppes.npcs.api.ParamName;

public interface IItemBook extends IItemStack {

	String getAuthor();

	String[] getText();

	String getTitle();

	void setAuthor(@ParamName("author") String author);

	void setText(@ParamName("pages") String ... pages);

	void setTitle(@ParamName("title") String title);

}
