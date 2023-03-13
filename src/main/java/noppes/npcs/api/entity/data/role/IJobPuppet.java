package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCJob;

public interface IJobPuppet
extends INPCJob {
	
	public interface IJobPuppetPart {
		int getRotationX();

		int getRotationY();

		int getRotationZ();

		void setRotation(int p0, int p1, int p2);
	}

	int getAnimationSpeed();

	boolean getIsAnimated();

	IJobPuppetPart getPart(int p0);

	void setAnimationSpeed(int p0);

	void setIsAnimated(boolean p0);
}
