package noppes.npcs.client.renderer.data;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import noppes.npcs.items.CustomArmor;

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
	public Optional<TRSRTransformation> apply(Optional<? extends IModelPart> part) {
		if (!(part.get() instanceof TransformType) || this.armor == null) { return super.apply(part); }
		Optional<TRSRTransformation> result = this.armor.getOptional((TransformType) part.get());
		return result != null ? result : super.apply(part);
	}

}
