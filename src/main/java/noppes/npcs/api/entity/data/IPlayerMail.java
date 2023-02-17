package noppes.npcs.api.entity.data;

import noppes.npcs.api.IContainer;
import noppes.npcs.api.handler.data.IQuest;

public interface IPlayerMail {
	IContainer getContainer();

	IQuest getQuest();

	String getSender();

	String getSubject();

	String[] getText();

	void setQuest(int p0);

	void setSender(String p0);

	void setSubject(String p0);

	void setText(String[] p0);
}
