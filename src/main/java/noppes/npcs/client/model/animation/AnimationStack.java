package noppes.npcs.client.model.animation;

import noppes.npcs.api.util.IModelRenderer;
import noppes.npcs.entity.data.DataAnimation;

public class AnimationStack implements IModelRenderer {
	
	// [ 0:rotX, 1:rotY, 2:rotZ, 3:ofsX, 4:ofsY, 5:ofsZ, 6:scX, 7:scY, 8:scZ, 9:rotX1, 10:rotY1 ]
	public Float[] partSets = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f };
	public boolean showModel = true;
	
	public void setAnimation(DataAnimation animation, int idPart) {
		if (animation.currentFrame != null && animation.currentFrame.parts.containsKey(idPart)) {
			this.showModel = animation.currentFrame.parts.get(idPart).show;
		}
		if (!this.showModel) { return; }
		this.partSets = animation.rots.get(idPart);
	}

	public void clear() {
		this.showModel = true;
		this.partSets = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f };
	}

	@Override
	public boolean isShowModel() { return showModel; }

	@Override
	public float[] getRotations() {
		return new float[] { partSets[0], partSets[1], partSets[2], partSets[9], partSets[10] };
	}

	@Override
	public float[] getOffsets() {
		return new float[] { partSets[3], partSets[4], partSets[5] };
	}

	@Override
	public float[] getScales() {
		return new float[] { partSets[7], partSets[6], partSets[8] };
	}
	
}
