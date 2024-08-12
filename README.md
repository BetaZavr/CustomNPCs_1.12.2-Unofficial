# CustomNPCs 1.12.2 Unofficial

[![Curseforge](http://cf.way2muchnoise.eu/full_1052708_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/customnpcs-unofficial)  [![Curseforge](http://cf.way2muchnoise.eu/versions/For%20MC_1052708_all.svg)](https://www.curseforge.com/minecraft/mc-mods/customnpcs-unofficial)  <a title="Join us on Discord!" href="https://discord.gg/RGb4JqE6Qz"><img src="https://img.shields.io/discord/558230575278981120?label=CNPCs Un%20Discord&amp;logo=Discord&amp;style=?flat" alt="Discord"/></a>

## Welcome to CustomNPCs: Unofficial

CustomNPCs is a modification for the game Minecraft aimed at creating your own maps in the RPG genre. A huge set of tools for creating your own NPCs and their capabilities; your own blocks and items; quests and other game elements.

This is an unofficial code repository for the Custom NPCs mod

This code is posted without permission, but under the terms of the Creative Commons Attribution-Non Commercial 3.0 Unported license for CustomNPCs.

http://creativecommons.org/licenses/by-nc/3.0/

Official **[website](https://www.kodevelopment.nl/minecraft/customnpcs)**.

## Differences from the main CustomNPCs mod:

### Game

* mod configuration is now configurable in-game;
* most of the mod's GUI has a description for each element. The description appears when you hover the mouse cursor over the GUI element;
* new mod commands (see Tab);
* **Factions* log * and **Quests** changed;
* added Quest Compass;
* added virtual currency;
* more accurate mod and player data;

### Quests

* quest type is no longer determined by quest tasks;
* number of quest tasks increased from 3 to 9; * quest tasks can be completed one by one or at least one;
* the player can cancel the quest;
* you can select an NPC to hand in a quest when completing it;
* added advanced settings (for example, an icon);
* reset ID for PRO;
* added virtual currency to rewards, and also fixed the operation of the type of receiving a reward;
* reset ID for PRO;

## # Dialogues

* the GUI for the player's dialogue with the NPC has been redesigned;
* letter-by-letter text display is available;
* a delay for player responses is available;
* the ability to add an image to the dialogue window;
* the ability to turn off the dialogue sound when exiting the GUI;
* the number of player responses is not limited and can have their own conditions;
* ID reset for PRO;

### NPC

* mechanics of most **Jobs** and **Roles** have been changed;
* **Shops**: any number of goods, limited goods, shop update, shop reputation to the player, etc.;
* **Mercenary**: slightly improved AI, possibility of hiring for game currency;
* **Banks**: the number of slots and the method of unlocking new bank cells have been reworked;
* **Transporters**: the ability to edit the location, payment with virtual currency has been added;
* **Mail**: the ability to auto-delete letters over time, paid sending letters, etc.;
* **Bard**: expanded settings and corrected mechanics;
* **Guardian**: slightly improved AI;
* **Spawner**: completed, also added new settings for various situations; * **Chunk Loader**: loads 3x3 chunks around itself (2x2 in the main mod);
* **Puppet**: animating your NPC in various situations is no longer a job and is not limited to two frames and three situations. Added a mini animation editor with a huge set of tools;
* **Factions**: flag available (can be installed on a shield or as a block), friendly factions added for protection;
* some mechanics marked **WIP** have been improved (for example Natural spawns);
* AI behavior in battle has been reworked (battle tactics);
* **Level** and **Rarity** have been added for convenience;
* immortality time management after getting hit;
* the mechanics of item drop upon death have been reworked. You can configure up to 32 items, each of which has advanced settings. Or create an item drop template, etc.;

### Scripting

* new script tabs. For all NPCs, for your potions, client scripts;
* ability to write scripts for player clients;
* ability to encrypt your script (protection against theft);
* added ability to use **Graal JS** (requires libraries in your JVM);
* added **[new APIs](https://minecraft.fandom.com/ru/wiki/Custom_NPCs/Unoficial_API_1.12.2)**;
* changed the way your files are connected;