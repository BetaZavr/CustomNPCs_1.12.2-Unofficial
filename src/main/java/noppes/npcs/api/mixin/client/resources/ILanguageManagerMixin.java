package noppes.npcs.api.mixin.client.resources;

import net.minecraft.client.resources.Locale;

public interface ILanguageManagerMixin {

    Locale npcs$getCurrentLocate();

    String npcs$getCurrentLanguage();

}