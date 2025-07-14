package noppes.npcs.mixin.client.audio;

import noppes.npcs.api.mixin.client.audio.ILibraryMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import paulscode.sound.Library;
import paulscode.sound.Source;

import java.util.HashMap;

@Mixin(value = Library.class, remap = false, priority = 499)
public class LibraryMixin implements ILibraryMixin {

    @Shadow
    protected HashMap<String, Source> sourceMap;

    @Override
    public HashMap<String, Source> npcs$getSourceMap() { return sourceMap; }

}
