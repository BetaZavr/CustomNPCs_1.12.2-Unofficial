package noppes.npcs.client.model.animation;

import noppes.npcs.entity.data.DataAnimation;

public class AnimationStack {
	
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
	
}
