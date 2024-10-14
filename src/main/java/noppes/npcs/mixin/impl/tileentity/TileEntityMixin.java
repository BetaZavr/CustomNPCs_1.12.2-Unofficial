package noppes.npcs.mixin.impl.tileentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.mixin.tileentity.ITileEntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntity.class)
public class TileEntityMixin implements ITileEntityMixin {

    @Mutable
    @Shadow
    private int blockMetadata;

    @Mutable
    @Shadow
    protected Block blockType;

    // Correction of previously incorrectly registered blocks
    @Inject(method = "create", at = @At("HEAD"))
    private static void npcs$create(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<TileEntity> cir) {
        ResourceLocation rl = new ResourceLocation(compound.getString("id"));
        String p = rl.getResourcePath();
        if (rl.getResourceDomain().equals("minecraft") && (
                p.equals("tileredstoneblock") ||
                p.equals("tileblockanvil") ||
                p.equals("tilemailbox") ||
                p.equals("tilewaypoint") ||
                p.equals("tilenpcscripted") ||
                p.equals("tilenpcscripteddoor") ||
                p.equals("tilenpcbuilder") ||
                p.equals("tilenpccopy") ||
                p.equals("tilenpcborder")
        )) {
            compound.setString("id", new ResourceLocation(CustomNpcs.MODID, p).toString());
        }
    }

    @Override
    public void npcs$setBlockMetadata(int newBlockMetadata) {
        if (newBlockMetadata < 0) { newBlockMetadata *= -1; }
        blockMetadata = newBlockMetadata;
    }

    @Override
    public void npcs$setBlockType(Block newBlockType) {
        if (newBlockType == null) { return; }
        blockType = newBlockType;
    }

}
