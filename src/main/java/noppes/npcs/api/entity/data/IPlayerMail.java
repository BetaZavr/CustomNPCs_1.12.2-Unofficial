package noppes.npcs.api.entity.data;

import noppes.npcs.api.IContainer;
import noppes.npcs.api.handler.data.IQuest;

public interface IPlayerMail {

	IContainer getContainer();

	int getMoney();

	IQuest getQuest();

	int getRansom();

	String getSender();

	String getSubject();

	String[] getText();

	void setMoney(int money);

	void setQuest(int id);

	void setRansom(int money);

	void setSender(String sender);

	void setSubject(String subject);

	void setText(String[] text);

}
