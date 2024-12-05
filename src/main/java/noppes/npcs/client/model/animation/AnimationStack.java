package noppes.npcs.client.model.animation;

import noppes.npcs.api.util.IModelRenderer;
import noppes.npcs.entity.data.DataAnimation;

public class AnimationStack implements IModelRenderer {
	
	// [ 0:rotX, 1:rotY, 2:rotZ, 3:ofsX, 4:ofsY, 5:ofsZ, 6:scX, 7:scY, 8:scZ, 9:rotX1, 10:rotY1 ]
	public Float[] partSets = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f };
	public boolean showModel = true;
	private final int partId;

	public AnimationStack(int id) {
		partId = id;
	}
	
	public void setAnimation(DataAnimation animation, int idPart) {
		showModel = animation.getAnimationPartShow(idPart);
		if (!showModel) { return; }
		partSets = animation.getAnimationPartData(idPart);
	}

	public void clear() {
		showModel = true;
		partSets = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f };
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

	public void putAnimation(DataAnimation animation) {
		AnimationFrameConfig preFrame = animation.getPreFrame();
		if (preFrame == null || !preFrame.parts.containsKey(partId)) { return; }
		preFrame.parts.get(partId).show = showModel;

		preFrame.parts.get(partId).rotation[0] = partSets[0];
		preFrame.parts.get(partId).rotation[1] = partSets[1];
		preFrame.parts.get(partId).rotation[2] = partSets[2];
		preFrame.parts.get(partId).rotation[3] = partSets[9];
		preFrame.parts.get(partId).rotation[4] = partSets[10];

		preFrame.parts.get(partId).offset[0] = partSets[3];
		preFrame.parts.get(partId).offset[1] = partSets[4];
		preFrame.parts.get(partId).offset[2] = partSets[5];

		preFrame.parts.get(partId).scale[0] = partSets[6];
		preFrame.parts.get(partId).scale[1] = partSets[7];
		preFrame.parts.get(partId).scale[2] = partSets[8];
	}

}
