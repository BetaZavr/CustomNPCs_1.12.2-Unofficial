package noppes.npcs.api.entity.data.role;

public interface IRoleDialog {
	String getDialog();

	String getOption(int p0);

	String getOptionDialog(int p0);

	void setDialog(String p0);

	void setOption(int p0, String p1);

	void setOptionDialog(int p0, String p1);
}
