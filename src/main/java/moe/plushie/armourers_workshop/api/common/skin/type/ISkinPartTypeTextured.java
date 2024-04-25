package moe.plushie.armourers_workshop.api.common.skin.type;

import java.awt.Point;

import moe.plushie.armourers_workshop.api.common.IPoint3D;

public interface ISkinPartTypeTextured extends ISkinPartType {

	/** UV location of the models base texture. */
	public Point getTextureBasePos();

	/** Size of the model the texture is used on. */
	public IPoint3D getTextureModelSize();

	/** UV location of the models overlay texture. */
	public Point getTextureOverlayPos();

	/** Location of the texture in skin storage. */
	public Point getTextureSkinPos();

	/** Should this texture be mirrored? */
	public boolean isTextureMirrored();
}
