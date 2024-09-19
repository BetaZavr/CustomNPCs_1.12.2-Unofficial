package noppes.npcs.mixin.api.client.audio;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import paulscode.sound.Library;
import paulscode.sound.Source;

import java.util.HashMap;

@Mixin(value = Library.class, remap = false)
public interface LibraryAPIMixin {

    @Accessor(value="sourceMap")
    HashMap<String, Source> npcs$getSourceMap();

}
