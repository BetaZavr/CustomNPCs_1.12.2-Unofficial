package noppes.npcs.client.renderer.data;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import noppes.npcs.items.CustomArmor;

import javax.annotation.Nonnull;

@SuppressWarnings("deprecation")
public class CustomOBJState extends OBJModel.OBJState implements IModelState {
	
	CustomArmor armor;
	
	public CustomOBJState(List<String> visibleGroups, boolean visibility, CustomArmor armor) {
		this(visibleGroups, visibility, armor, TRSRTransformation.identity());
	}

	public CustomOBJState(List<String> visibleGroups, boolean visibility, CustomArmor armor, IModelState parent) {
		super(visibleGroups, visibility, parent);
		this.armor = armor;
	}
	
	@Override
	public @Nonnull Optional<TRSRTransformation> apply(@Nonnull Optional<? extends IModelPart> part) {
		IModelPart p = part.orElse(null);
		if (!(p instanceof TransformType) || this.armor == null) { return super.apply(part); }
		Optional<TRSRTransformation> result = this.armor.getOptional((TransformType) p);
		return result.isPresent() ? result : super.apply(part);
	}

}
