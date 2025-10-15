package noppes.npcs.api;

@SuppressWarnings("all")
public interface ITimers {

	void clear();

	void forceStart(@ParamName("id") int id, @ParamName("ticks") int ticks, @ParamName("repeat") boolean repeat);

	boolean has(@ParamName("id") int id);

	void reset(@ParamName("id") int id);

	void start(@ParamName("id") int id, @ParamName("ticks") int ticks, @ParamName("repeat") boolean repeat);

	boolean stop(@ParamName("id") int id);

}