package noppes.npcs.api.entity.data.role;

public interface IRoleDialog {
	
	String getDialog();

	String getOption(int option);

	String getOptionDialog(int option);

	void setDialog(String text);

	void setOption(int option, String text);

	void setOptionDialog(int option, String text);
	
}
