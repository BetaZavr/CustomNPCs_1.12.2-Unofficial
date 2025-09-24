package noppes.npcs.client.gui.util;

public class Poses {

	public int x;
	public int y;

	public Poses(int pos) {
		switch (pos) {
			case 1: x -= 11; y -= 11; break;
			case 2: x -= 15; break;
			case 3: x -= 11; y += 11; break;
			case 4: y += 15; break;
			case 5: x += 11; y += 11; break;
			case 6: x += 15; break;
			case 7: x += 11; y -= 11; break;
			default: y -= 15; break;
		}
	}

}
