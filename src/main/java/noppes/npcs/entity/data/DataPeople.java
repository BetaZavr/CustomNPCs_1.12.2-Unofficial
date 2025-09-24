package noppes.npcs.entity.data;

import noppes.npcs.CustomNpcs;

import java.util.Random;

public enum DataPeople {

    Noppes("Noppes", "Creator", true),
    Goodbird("Goodbird", "Porter & Fixer", true),
    BetaZavr("BetaZavr", "Porter & Fixer", true),
    Dati("Dati", "Patreon", true),
    Animekin("Animekin", "Patreon", true),
    Vin0m("Vin0m", "Patreon", false),
    Birb("Birb", "Patreon", true),
    Flashback("Flashback", "Patreon", false),
    Ronan("Ronan", "Patreon", false),
    Shivaxi("Shivaxi", "Patreon", false),
    GreatOrator("GreatOrator", "Patreon", false),
    Aphmau("Aphmau", "Patreon", false),
    Kithoras("Kithoras", "Patreon", false),
    Daniel_N("Daniel N", "Patreon", false),
    G1RCraft("G1RCraft", "Patreon", false),
    Joanie_H("Joanie H", "Patreon", false),
    Jaffra("Jaffra", "Patreon", false),
    Orphie("Orphie", "Patreon", false),
    PPap("PPap", "Patreon", false),
    RED9936("RED9936", "Patreon", false),
    NekoTune("NekoTune", "Patreon", false),
    JusCallMeNico("JusCallMeNico", "Patreon", false);

    private static final Random rnd = new Random();
    public final String name;
    public final String title;
    public final String skin;

    DataPeople(String nameIn, String titleIn, boolean hasSkin) {
        name = nameIn;
        title = titleIn;
        if (!hasSkin) { skin = ""; }
        else { skin = CustomNpcs.MODID + ":textures/entity/importantpeople/" + nameIn.toLowerCase().replace(" ", "_") + ".png"; }
    }

    public static DataPeople get() {
        return values()[rnd.nextInt(values().length)];
    }

}
