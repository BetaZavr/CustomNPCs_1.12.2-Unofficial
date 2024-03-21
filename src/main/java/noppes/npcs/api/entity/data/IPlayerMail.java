package noppes.npcs.api.entity.data;

import noppes.npcs.api.IContainer;
import noppes.npcs.api.handler.data.IQuest;

public interface IPlayerMail {
	
	IContainer getContainer();

	IQuest getQuest();

	String getSender();

	String getSubject();

	String[] getText();

	void setQuest(int id);

	void setSender(String sender);

	void setSubject(String subject);

	void setText(String[] text);
	
	int getMoney();
	
	void setMoney(int money);

	int getRansom();
	
	void setRansom(int money);
	
}
