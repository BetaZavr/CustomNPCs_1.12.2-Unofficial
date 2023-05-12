package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCJob;

public interface IJobPuppet
extends INPCJob {
	
	public interface IJobPuppetPart {
		int getRotationX();

		int getRotationY();

		int getRotationZ();

		void setRotation(int x, int y, int z);
	}

	int getAnimationSpeed();

	IJobPuppetPart getPart(int part);

	void setAnimationSpeed(int speed);
	
}
