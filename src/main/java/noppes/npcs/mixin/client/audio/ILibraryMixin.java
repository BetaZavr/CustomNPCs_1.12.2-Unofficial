package noppes.npcs.mixin.client.audio;

import paulscode.sound.Source;

import java.util.HashMap;

public interface ILibraryMixin {

    HashMap<String, Source> npcs$getSourceMap();

}
