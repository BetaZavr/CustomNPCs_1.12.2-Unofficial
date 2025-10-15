# CustomNPCs Unofficial by BetaZavr
[![CurseForge](http://cf.way2muchnoise.eu/full_1086839_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/customnpcs-unofficial-from-betazavr/files/all?page=1&pageSize=20)
[![CurseForge](http://cf.way2muchnoise.eu/versions/For%20MC_1086839_all.svg)](https://www.curseforge.com/minecraft/mc-mods/customnpcs-unofficial-from-betazavr)
[![License: CC BY-NC 3.0](https://img.shields.io/badge/License-CC%20BY--NC%203.0-lightgrey.svg)](http://creativecommons.org/licenses/by-nc/3.0/) 

[![Download mod: GitHub](https://img.shields.io/badge/Download%20mod-from%20GitHub-lightgrey.svg)](https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/releases/tag/first)

[![Discord Server for Discussion](https://discordapp.com/api/guilds/558230575278981120/widget.png?style=banner3)](https://discord.gg/RGb4JqE6Qz)

## Welcome to CustomNPCs Unofficial
CustomNPCs - modification For Minecraft games designed For creation own kart V RPG genre . A huge set of tools for creating your own NPCs and their abilities, custom blocks and items, quests and other game elements.

This is an unofficial repository of the Custom mod code . NPCs .

The code for version 1.20.1 is not available for the same reason.

Official **[website](https://www.kodevelopment.nl/minecraft/customnpcs)**.

<span style="color: #ff0000;">**Attention - this is important!**</span>
The modification provides extensive capabilities for writing scripts not only for the server-side but also for the client-side. Therefore:

For mod users:
* This could be exploited by malicious actors. Download maps or connect to servers you know you trust!

For server owners:
* The coder is given the ability to process packets, reflect, work with files, etc. Choose your programmers carefully.

## Differences from the original CustomNPCs mod by Noppes (or Goodbird):
Currently, everything written below applies to version **1.12.2**. Game version **1.20.1** currently only has a subset of these mechanics!

### Additional mods required:
A library of mixins for the corresponding game version. Compatible with both versions of the mod [MixinBootstrap-1.1.0](https://www.curseforge.com/minecraft/mc-mods/mixinbootstrap/download/3437402)

### API's:
To write your own scripts quickly and easily, use this mod:
1. Download **[Visual Studio Code](https://code.visualstudio.com/Download)**;
2. Download latest version of the **TypeScript Configuration**:
- [![English API: 1.12.2](https://img.shields.io/badge/English%20API-1.12.2-lightgrey.svg)](https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/releases/download/first/1.12.2.TypeScript.Configuration.EN.Package_API.v4.432_no.comments.rar)
   [![Russian API: 1.12.2](https://img.shields.io/badge/Russian%20API-1.12.2-lightgrey.svg)](https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/releases/download/first/1.12.2.TypeScript.Configuration.RU.Package_API.v4.432_no.comments.rar)
- [![English API: 1.20.1](https://img.shields.io/badge/English%20API-1.20.1-lightgrey.svg)](https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/releases/download/first/1.20.1.TypeScript.Configuration.EN.Package_API.v1.1010_no.comments.rar)
   [![Russian API: 1.20.1](https://img.shields.io/badge/Russian%20API-1.20.1-lightgrey.svg)](https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/releases/download/first/1.20.1.TypeScript.Configuration.RU.Package_API.v1.1010_no.comments.rar)
3. Unzip the archive to a convenient location;
4. In **Visual Studio Code**, **Open Folder...** where you unzipped the **TypeScript Configuration**;
5. Write your code however you like. There's an example in "example.js" file.

### Mod Code:
1. The mod configuration controls most of the new and old mechanics;
2. Some mechanics are marked as **WIP** (work in progress) - such mechanics are either disabled or may not work correctly;
3. Added a GUI for player consent to play with this mod when trying to connect to servers or enter a map.
4. <span style="background-color: #80808040;">Availabi<i style="color: red;">t</i>i<i style="color: red;">l</i>y</span> everywhere corrected on <span style="background-color: #80808040;">Availabi<i style="color: green;">l</i>i<i style="color: green;">t</i>y</span>;
5. Fixed names of some localization keys;
6. Availability Class **Availability** - conditions corrected:
* added a setting for checking the player's name;
* added a setting for checking player health;
* added a setting for checking custom regions;
* added a setting for checking the player's script data **StoredData**;
* added a setting for checking virtual currency;
* added a setting to check for the presence of items in the player's inventory;
* Improved time of day checking settings, now you can specify a specific time;
7. Improved **TranslateUtil** (class for text translation):
* The method of text translation has been changed. You can specify language codes;
* finally added a method to play sound for the player;
8. NPC **EntityEnderFX** particles have been pre-restored to the model;
9. Renderer **BlockMailboxRenderer** (block for letters) the rotation of the model has been restored meta date;
10. The way mod configuration is loaded has been changed. More data types and structured;
11. Added a debugging system **DataDebug** for mod methods to track micro freezes (heavily loads the system);
12. All methods of handling script events **EventHooks** have been changed. More custom call;
13. All mixins are set to mixing priority ID:499 (base 1000);
14. Most of the mixins those responsible for **Get**/**Set** were rewritten to use reflection to eliminate possible conflicts between mods;
15. **CustomNpcsPermissions** permissions have been improved:
* now they are all prohibited by default;
* can be configured through the **permissions.json** file by adding the names of players to each permission (the file is created in the map section/folder);
16. New permissions:
* EDIT_PERMISSION - allows changing players permissions;
* EDIT_CLIENT_SCRIPT - allows editing of client scripts;
* GLOBAL_MARKETS - allows editing stores;
* GLOBAL_AUCTIONS - allows editing auctions (WIP);
* GLOBAL_MAIL - allows editing mail settings;
* MONEY_MANAGER - allows viewing and changing players virtual currency;
* DONAT_MANAGER - allows viewing and changing players donations;
17. Method **NBTJsonUtil.Convert(CompoundTag compound)** now sorts key names alphabetically;
18. The logs mod logging **LogWriter** has been reworked to not create separate files;
19. Drop system added:
* loot/inventory changes see below;
* each item can be customized:
* specify the minimum and maximum quantity;
* specify options for enchantments , attributes, tags;
* automatic creation of settings for a specified subject (for the lazy or those who don’t understand it);
* the ability to create templates from several drop items;
20. Region system added:
* polygon shape;
* there are events of entry and exit to the region;
* a special item has been created to make editing regions easier;

### Command block commands added or changed:
1. scripts **CmdScript**:
* **/noppes script reload** - more reloads;
* **/noppes script logs** - (new) displays all detected errors and script elements with logs in the chat;
* **/noppes script apilist** - (new) displays all mod APIs for your scripts in the chat;
* **/noppes script clientlist** - (new) displays the names of all Forge events for the client in the chat;
* **/noppes script forgelist** - (new) displays the names of all Forge events for the server in the chat;
* **/noppes script list <type>** - (new) displays in the chat all detected errors and script elements with logs for the specified types of script elements;
2. configuration **CmdConfig**:
* **/noppes config report** - (new) displays names, running time, and number of mod method runs in the chat and console (helps find freezes);
* **/noppes config clear** - (new) clear the freeze catch lists;
3. (new) virtual currency **CmdMoney**:
* **/moneynpc ** - information about the team;
* **/moneynpc get money/donat <player>** - information about the player's balance;
* **/moneynpc set money/donat <player> <value>** - set the player's balance;
* **/moneynpc add money/donat <player> <value>** - add balance to the player (negative value is possible);
* **/moneynpc pay <player> <value>** - transfer coins to another player. A fee is charged (see the mod configuration);
* you can't pay yourself;
4. (new) permissions **CmdPermissions**:
* **/noppes permissions** - open the permissions editing window;

### Player
1. Changes in player data **PlayerData**:
* added common interface **IPlayerData**;
* added quest compass data **PlayerCompassData** (settings, current quests , etc.);
2. Added game data **PlayerGameData**. Contains:
* virtual currency/donation;
* currently pressed mouse and keyboard keys;
* which HUD elements to display;
* experience/development in stores;
* rendering distance in blocks;
* is a server operator or not;

### NPC
1. Improved **CombatHandler**:
* AI counts everyone who participated in the battle and how much damage they caused;
* the following is taken into account: distance to the attacker , objects in hands, etc.;
2. AI combat tactics have been reworked. Read the description in-game;
3. Stats:
* NPC level and rarity have been added. Changing the settings recalculates health, damage, model size, and experience drops (all can be disabled in the configuration);
* Improved NPC damage resistance settings. Now you can specify any damage type that exists in the game;
4. **ContainerNPCInv** inventory:
* NPC equipment has been corrected;
* added quick equip mechanic Shift + click on the item;
5. AI:
* new methods for **INPCAi**;
* You can disable most of the AI to reduce the load on performance. (This includes: combat tactics, roles, work, scripts, etc.)
* added a setting for damage delay;
6. The inventory has been redesigned for the drop system (see the Mod for features) Code above:
* you can specify up to 32 items or a template;

### NPC Jobs and Roles:
1. **Mercenary**:
* AI slightly improved;
* hiring for virtual currency;
* added an inventory for the player's items (automatically issued to the player upon completion of the recruitment);
2. **Banker** (completely remade):
* To save your system memory, player bank data is now stored in separate files;
* The bank can be publicly accessible. A player can be designated as the owner. The owner can restrict access to the bank or appoint another owner;
* there is no limit on the number of bank cells;
* in the cell you can specify the exact number of available slots, including for purchasing new slots;
* new slots are purchased by bartering items + electronic currency;
3. **Transporter**:
* player movement can now be paid (barter for items, + electronic currency);
* added full customization of location for movement (dimension, position);
4. **Mailman**:
* It is now possible to configure player mail management in the **Global** tab;
* automatic deletion of letters over time;
* inability to send to oneself;
* paid letter (the cost depends on the number of characters in the letter and the number of items to be sent);
* the appearance of a new letter is displayed on the screen;
5. **Bard**:
* more precise adjustment of the working area;
* increased the shutdown range to 256 blocks;
* Fixed issues with the sound triggering mechanics from the original mod;
* the bug with constant sound playback in 1.12.2 and below is based on the game 's sound system itself and I couldn't fix it completely;
6. **Guard**:
* slightly improved AI;
7. **Spawner** (completely redesigned):
* remove summoned creatures if there is no target to attack;
* control over the number of summoned creatures so as not to clutter the map (and the load on the system);
* separated spawn when alive and when killed;
* the number of summoned creatures has been increased;
8. **Chunk loader**:
* now loads all 3x3 chunks near itself (2x2 in the original mod);
9. **Puppet**:
* In 1.12.2, it was replaced with a custom animation system . Animations are available for any NPC with a standard model. The GUI for setting up this animation is very complex due to the variability of the animations;
10. **Trader** (completely reworked):
* stores have sections, are related to the player, the GUI settings and the store for NPCs have been redesigned;
* More store and deal settings. New elements can be enabled or disabled;
* you can set up only purchase and/or only sale for a transaction;
* cases have been added and are only for sale;
* any number of stores, sections in the store and transactions in each section;
* deal setup: product is the specified item or case with items;
* deal settings: currency - this is either a barter of up to 9 items, + electronic currency, + donation;
* availability settings for each transaction.
* items in the case are edited through the drop system;
* you can change the OBJ model and texture of the case chest;
* indicate the number of items to be issued from the case;

### GUI:
1. A custom variable background has been installed in the game menu (controlled via configuration);
2. Script GUI can store scripts and logs of almost any length without data loss;
3. Tooltip display has been applied to almost all buttons, labels and lists in the mod's GUI;
4. Quest Journal have been added to the player's inventory in creative mode;
5. NBT Tag Book **GuiNbtBook** (item):
* opens more readable text of object tags;
* can work with objects in the left hand;
* added a list of all fields, methods and subclasses of the edited object;
6. List of NPCs around **GuiNpcRemoteEditor** (NPC Customizer):
* the list of entities contains more information (distance to the player, entity ID, class name);
* added entity display;
* added a choice to display a list of all entities loaded on the server side, or only NPCs;
7. Completely redesigned quest log **GuiLog**:
* now in the form of a magazine, not a window;
* Faction Relations tab has been added to this GUI. The default **GuiFaction** has been removed;
* a quest compass settings tab (controlled through mod configurations);
* the journal has animations (I might add a configuration to disable this animation);
* more information is shown;
8. Script editing GUI **GuiScriptInterface**:
* The GUI window size has been changed to fit the current game window size;
9. Recreated the GUI for setting up the NPC model of body parts layers **GuiCreationParts**;
* added settings for displaying model layers;
* (1.12.2) The eye layer has been completely reworked. It will be used to create emotions;
10. Fixed the GuiBlockBuilder settings for the Building Block:
* added display of the selected scheme;
* the number of displayed schemes is not limited;
11. NPC work GUIs have been reworked to suit the new settings:
* both the settings GUI and the game GUI;
* more new elements and descriptions;
12. NPC settings GUI has been reworked:
* more new elements and descriptions;
13. Added GUI for convenient editing of mod configuration (available from the menu of the list of all mods);
14. Dialog box changed:
* scroll bars added;
* in the **Global** tab you can customize this window;
15. Sub-GUI texture selection:
* the method of viewing and selecting textures has been redesigned;
* fixed search for all images in the game (not only registered ones, but also available images from resources/ mods);
* texture is displayed in this sub -GUI;
16. Sub-GUI sound selection:
* the method of viewing and selecting sounds has been redesigned;
* Fixed sound search in the game;
* the play button also stops the currently playing sound;
17. (for modders) The main GUI classes, as well as all GUI elements, have been reworked to a more convenient format for creating GUI mods:
18. Optimized the GUI element - text field **GuiTextArea** (fewer freezes when changing the text);
19. The GUI element has been redesigned - the **GuiCustomScrollNop** list:
* scroll bar textured (now it is wider);
* the scroll bar continues to move even when the cursor is not on the list;
* prefixes in the form of an object, picture, OBJ model have been added to the beginning of the text of each position/line;
* added text suffixes to the end of the text of each position/line;

### Quests:
1. Quest system has been completely redesigned (structure, data storage, behavior mechanics, etc.);
2. Quests now have an icon;
3. You can add an image to the quest description;
4. A separate button for managing scripts has been added to the quest log;
5. Virtual currency has been added to the rewards;
6. Added the function of self-selection of one item from all rewards;
7. You can select any NPC from the list of loaded ones as an NPC to complete the quest (displayed in the quest journal);
8. Quest objectives have been changed:
* quests no longer have a type (find kill etc.), the type is now applied to each target separately;
* the number of quest objectives has been increased from 3 to 9;
* added target type for completing the quest (all, in turn , at least one);
* new type of goal - item crafting;
9. The quest can now be cancelled. A setting has been added for forgetting dialogues and quests;

### Dialogues:
1. Dialogue response system has been completely redesigned:
* you can add an icon;
* you can set options for the next dialog through accessibility conditions;
2. Dialog  can be shown more than once (display as when typing text);
3. You can add an image to the end of the dialog;
4. GUI for player communication with NPCs has been changed (see above in GUI);
5. More dialog settings;

### Factions:
1. Added flag selection. Flags can be installed on banners and the vanilla shield;
2. Added a setting for selecting friendly factions. Used for defense during attacks;

### Natural spawns:
1. Spawn mechanics have been finalized;
2. More settings for determining natural spawn;

### Scripts:
1. New interfaces:
* **IMinecraft** - API for client scripts;
* **IRenderSystem** - API for client scripts;
* **IClientMouse** - API for client scripts;
* **IMethods** - API of additional methods;
2. Improved interfaces:
* **IWorld** - work with the client;
* **IPos** - fixed a bug with displacement in direction; changed to exact numbers with a dot;
* **INbt** - all types for leaf tag, exceptions have been improved;
* **IVillager** - recipe settings;
* **IPlayer** - more methods;
3. Added scripts for the client **ClientScriptData**;
* Such scripts cannot be encrypted. These scripts are also stored on each client for possible analysis;
4. Added script handler for custom potions **PotionScriptData**;
5. Added handler for common scripts for all NPCs **NpcScriptData**;
6. Added constants file **constant_scripts.json** for all map scripts;
7. The **dump(Object)** method has been improved. Now it returns full information about the object: names of all constructors, subclasses, fields and methods of the object/class (including private ones);
8. Changed the **IData** data system:
* Player data is no longer stored separately from other entities. Mixins are now integrated into the **Entity** class;
* **Tempdata** can get data in the form of **INbt** tags. Only what can be converted .;
* **Storeddata** can now store any data that can be converted for storage in **INbt** tags;
* "can be converted": all numbers; strings; objects that can be written to JSON; Lists; Maps; and arrays of them.
9. Added processing of language search for scripts (**Rhino , Groovy , GraalJS etc.**);
10. Mouse-held item has been added to the slot click event in your GUI **CustomGuiEvent.SlotEvent**;
11. A table for deobfuscation of field and method names has been added to the mod;
12. It is possible to encrypt server-side map scripts (theft protection);

## Project uses:
### 1.12.2:
* SDK 			- Java 8 (1.8.0_401)
* Gradle 		- 6.8.3
* ForgeGradle 	- (plugin) 4.1.16
* MixinGradle 	- (plugin) 0.8.5
* Forge 			- 1.12.2 (14.23.5.2860)
* Mixin 			- (plugin) 0.7.38
### 1.20.1:
* SDK 			- Java 17.0.13
* Gradle 		- 8.8
* ForgeGradle 	- (plugin) 6.0.35
* MixinGradle 	- (plugin) 0.8.5
* Forge 			- 1.20.1 (47.3.12)
* Mixin 			- (plugin) 0.7.38

**Original Mod** Discord Server for discussions:

[![Discord Server for Scripting](https://discordapp.com/api/guilds/151785576557707264/widget.png?style=banner3)](https://discord.gg/AJ7qPy4)

**Original Mod** Discord Server for scripting:

[![Discord Server for Scripting](https://discordapp.com/api/guilds/177204059109982208/widget.png?style=banner3)](https://discord.gg/AJ7qPy4)

**Recommended Russian** Discord Server for scripting:

[![Discord Server for RU Scripting](https://discordapp.com/api/guilds/1146323402978762772/widget.png?style=banner3)](https://discord.gg/aDuNPDGr6t)