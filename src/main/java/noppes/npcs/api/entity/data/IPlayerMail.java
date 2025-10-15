package noppes.npcs.api.entity.data;

import noppes.npcs.api.IContainer;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.IQuest;

public interface IPlayerMail {

	IContainer getContainer();

	int getMoney();

	IQuest getQuest();

	int getRansom();

	String getSender();

	String getSubject();

	String[] getText();

	void setMoney(@ParamName("money") int money);

	void setQuest(@ParamName("id") int id);

	void setRansom(@ParamName("money") int money);

	void setSender(@ParamName("sender") String sender);

	void setSubject(@ParamName("subject") String subject);

	void setText(@ParamName("text") String[] text);

}
