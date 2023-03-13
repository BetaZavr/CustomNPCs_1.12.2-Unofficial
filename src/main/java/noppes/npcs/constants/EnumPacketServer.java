package noppes.npcs.constants;

import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;

public enum EnumPacketServer
{
	Delete(CustomNpcsPermissions.NPC_DELETE, true), 
	RemoteMainMenu(CustomNpcsPermissions.NPC_GUI), 
	NpcMenuClose(CustomNpcsPermissions.NPC_GUI, true), 
	RemoteDelete(CustomNpcsPermissions.NPC_DELETE, true), 
	RemoteFreeze(CustomNpcsPermissions.NPC_FREEZE), 
	RemoteReset(CustomNpcsPermissions.NPC_RESET), 
	SpawnMob(CustomNpcsPermissions.SPAWNER_MOB), 
	MobSpawner(CustomNpcsPermissions.SPAWNER_CREATE), 
	MainmenuAISave(CustomNpcsPermissions.NPC_ADVANCED, true), 
	MainmenuAIGet(true), 
	MainmenuInvSave(CustomNpcsPermissions.NPC_INVENTORY, true), 
	MainmenuInvGet(true), 
	MainmenuStatsSave(CustomNpcsPermissions.NPC_STATS, true), 
	MainmenuStatsGet(true), 
	MainmenuDisplaySave(CustomNpcsPermissions.NPC_DISPLAY, true), 
	MainmenuDisplayGet(true), 
	ModelDataSave(CustomNpcsPermissions.NPC_DISPLAY, true), 
	MainmenuAdvancedSave(CustomNpcsPermissions.NPC_ADVANCED, true), 
	MainmenuAdvancedGet(true), 
	MainmenuAdvancedMarkData(CustomNpcsPermissions.NPC_ADVANCED, true), 
	DialogNpcSet(CustomNpcsPermissions.NPC_ADVANCED), 
	DialogNpcRemove(CustomNpcsPermissions.NPC_ADVANCED, true), 
	DialogNpcMove(CustomNpcsPermissions.NPC_ADVANCED, true), // New
	FactionSet(CustomNpcsPermissions.NPC_ADVANCED, true), 
	TransportSave(CustomNpcsPermissions.NPC_ADVANCED, true), 
	TransformSave(CustomNpcsPermissions.NPC_ADVANCED, true), 
	TransformGet(true), 
	TransformLoad(CustomNpcsPermissions.NPC_ADVANCED, true), 
	TraderMarketSave(CustomNpcsPermissions.GLOBAL_MARCET, true), 
	TraderMarketGet(CustomNpcsPermissions.GLOBAL_MARCET, true), // New
	TraderMarketNew(CustomNpcsPermissions.GLOBAL_MARCET, true), // New
	TraderMarketDel(CustomNpcsPermissions.GLOBAL_MARCET, true), // New
	JobSave(CustomNpcsPermissions.NPC_ADVANCED, true),
	JobClear(CustomNpcsPermissions.NPC_ADVANCED, true),
	JobGet(true), 
	RoleSave(CustomNpcsPermissions.NPC_ADVANCED, true), 
	RoleGet(true), 
	JobSpawnerAdd(CustomNpcsPermissions.NPC_ADVANCED, true), 
	JobSpawnerRemove(CustomNpcsPermissions.NPC_ADVANCED, true), 
	RoleCompanionUpdate(CustomNpcsPermissions.NPC_ADVANCED, true), 
	LinkedSet(CustomNpcsPermissions.NPC_ADVANCED, true), 
	ClonePreSave(CustomNpcsPermissions.NPC_CLONE), 
	CloneSave(CustomNpcsPermissions.NPC_CLONE), 
	CloneRemove(CustomNpcsPermissions.NPC_CLONE), 
	CloneList, 
	LinkedGetAll, 
	LinkedRemove(CustomNpcsPermissions.GLOBAL_LINKED), 
	LinkedAdd(CustomNpcsPermissions.GLOBAL_LINKED), 
	PlayerDataRemove(CustomNpcsPermissions.GLOBAL_PLAYERDATA), 
	BankSave(CustomNpcsPermissions.GLOBAL_BANK), 
	BanksGet, 
	BankGet, 
	BankRemove(CustomNpcsPermissions.GLOBAL_BANK), 
	DialogCategorySave(CustomNpcsPermissions.GLOBAL_DIALOG), 
	DialogCategoryRemove(CustomNpcsPermissions.GLOBAL_DIALOG), 
	DialogSave(CustomNpcsPermissions.GLOBAL_DIALOG), 
	DialogRemove(CustomNpcsPermissions.GLOBAL_DIALOG), 
	TransportCategoryRemove(CustomNpcsPermissions.GLOBAL_TRANSPORT), 
	TransportGetLocation(true), 
	TransportRemove(CustomNpcsPermissions.GLOBAL_TRANSPORT), 
	TransportsGet, 
	TransportCategorySave(CustomNpcsPermissions.GLOBAL_TRANSPORT), 
	TransportCategoriesGet, 
	FactionRemove(CustomNpcsPermissions.GLOBAL_FACTION), 
	FactionSave(CustomNpcsPermissions.GLOBAL_FACTION), 
	FactionsGet, 
	FactionGet, 
	QuestCategorySave(CustomNpcsPermissions.GLOBAL_QUEST), 
	QuestRemove(CustomNpcsPermissions.GLOBAL_QUEST), 
	QuestCategoryRemove(CustomNpcsPermissions.GLOBAL_QUEST), 
	QuestRewardSave(CustomNpcsPermissions.GLOBAL_QUEST), 
	QuestSave(CustomNpcsPermissions.GLOBAL_QUEST), 
	QuestDialogGetTitle(CustomNpcsPermissions.GLOBAL_QUEST), 
	RecipeSave(CustomNpcsPermissions.GLOBAL_RECIPE), 
	RecipeRemove(CustomNpcsPermissions.GLOBAL_RECIPE), 
	RecipeRemoveGroup(CustomNpcsPermissions.GLOBAL_RECIPE), // New
	RecipesAddGroup(CustomNpcsPermissions.GLOBAL_RECIPE), // New
	RecipesRenameGroup(CustomNpcsPermissions.GLOBAL_RECIPE), // New
	RecipesRename(CustomNpcsPermissions.GLOBAL_RECIPE), // New
	NaturalSpawnSave(CustomNpcsPermissions.GLOBAL_NATURALSPAWN), 
	NaturalSpawnGet, 
	NaturalSpawnRemove(CustomNpcsPermissions.GLOBAL_NATURALSPAWN), 
	MerchantUpdate(CustomNpcsPermissions.EDIT_VILLAGER), 
	PlayerRider(CustomNpcsPermissions.TOOL_MOUNTER), 
	SpawnRider(CustomNpcsPermissions.TOOL_MOUNTER), 
	MovingPathSave(CustomNpcsPermissions.TOOL_PATHER, true), 
	MovingPathGet(true), 
	DoorSave(CustomNpcsPermissions.TOOL_SCRIPTER), 
	DoorGet, 
	ScriptDataSave(CustomNpcsPermissions.TOOL_SCRIPTER, true), 
	ScriptDataGet(true), 
	ScriptBlockDataSave(CustomNpcsPermissions.TOOL_SCRIPTER, false), 
	ScriptBlockDataGet(false), 
	ScriptDoorDataSave(CustomNpcsPermissions.TOOL_SCRIPTER, false), 
	ScriptDoorDataGet(false), 
	ScriptPlayerSave(CustomNpcsPermissions.TOOL_SCRIPTER, false), 
	ScriptPlayerGet(false), 
	ScriptItemDataSave(CustomNpcsPermissions.TOOL_SCRIPTER, false), 
	ScriptItemDataGet(false), 
	ScriptForgeSave(CustomNpcsPermissions.TOOL_SCRIPTER, false), 
	ScriptForgeGet(false), 
	SpawnerNpcMove(CustomNpcsPermissions.NPC_ADVANCED, true), // New
	DialogNpcGet, 
	RecipesGet, 
	RecipeGet, 
	QuestOpenGui,
	QuestReset, // New
	QuestMinID, // New
	PlayerDataGet, 
	RemoteNpcsGet(CustomNpcsPermissions.NPC_GUI), 
	RemoteTpToNpc, 
	SaveTileEntity, 
	NaturalSpawnGetAll, 
	MailOpenSetup, 
	DimensionsGet, 
	DimensionTeleport, 
	GetTileEntity, 
	GetClone,
	Gui, 
	SchematicsTile, 
	SchematicsSet, 
	SchematicsBuild, 
	SchematicsTileSave, 
	SchematicStore, 
	SceneStart(CustomNpcsPermissions.SCENES), 
	SceneReset(CustomNpcsPermissions.SCENES), 
	NbtBookSaveEntity(CustomNpcsPermissions.TOOL_NBTBOOK), 
	NbtBookSaveItem(CustomNpcsPermissions.TOOL_NBTBOOK), 
	NbtBookSaveBlock(CustomNpcsPermissions.TOOL_NBTBOOK),
	ScriptPotionGet(false), //New
	ScriptPotionSave(CustomNpcsPermissions.TOOL_SCRIPTER, false), // New
	TeleportTo(false), // New
	RegionData(false), // New
	BuilderSetting(false), // New
	OpenBuilder(false); // New
	
	public CustomNpcsPermissions.Permission permission;
	public boolean needsNpc;
	private boolean exempt;
	
	private EnumPacketServer() {
		this.needsNpc = false;
		this.exempt = false;
	}
	
	private EnumPacketServer(CustomNpcsPermissions.Permission permission, boolean npc) {
		this(permission);
	}
	
	private EnumPacketServer(boolean need) {
		this.exempt = false;
		this.needsNpc = need;
	}
	
	private EnumPacketServer(CustomNpcsPermissions.Permission permission) {
		this.needsNpc = false;
		this.exempt = false;
		this.permission = permission;
	}
	
	public boolean hasPermission() {
		return this.permission != null;
	}
	
	public void exempt() {
		this.exempt = true;
	}
	
	public boolean isExempt() {
		return CustomNpcs.OpsOnly || this.exempt;
	}
	
	static {
		EnumPacketServer.GetTileEntity.exempt();
		EnumPacketServer.ScriptBlockDataGet.exempt();
		EnumPacketServer.ScriptDoorDataGet.exempt();
		EnumPacketServer.FactionsGet.exempt();
		EnumPacketServer.FactionGet.exempt();
		EnumPacketServer.SceneStart.exempt();
		EnumPacketServer.SceneReset.exempt();
	}
}
