package noppes.npcs.api;

public interface ITimers {

	void clear();

	void forceStart(int id, int ticks, boolean repeat);

	boolean has(int id);

	void reset(int id);

	void start(int id, int ticks, boolean repeat);

	boolean stop(int id);

}