package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IRoleDialog {

	String getDialog();

	String getOption(@ParamName("option") int option);

	String getOptionDialog(@ParamName("option") int option);

	void setDialog(@ParamName("text") String text);

	void setOption(@ParamName("option") int option, @ParamName("text") String text);

	void setOptionDialog(@ParamName("option") int option, @ParamName("text") String text);

}
