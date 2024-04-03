package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.util.AdditionalMethods;

public class PlayerSkinController {
	
	//NetHandlerPlayClient <- NetHandlerPlayClient <- PlayerList.playerLoggedIn <- SPacketPlayerListItem
	private static PlayerSkinController instance;
	public final Map<UUID, String> playerNames = Maps.<UUID, String>newHashMap();
	public final Map<UUID, Map<Type, ResourceLocation>> playerTextures = Maps.<UUID, Map<Type, ResourceLocation>>newHashMap();
	private String filePath;
	
	public PlayerSkinController() {
		PlayerSkinController.instance = this;
		this.filePath = CustomNpcs.getWorldSaveDirectory().getAbsolutePath();
		this.loadPlayerSkins();
	}

	public static PlayerSkinController getInstance() {
		if (newInstance()) { PlayerSkinController.instance = new PlayerSkinController(); }
		return PlayerSkinController.instance;
	}

	private static boolean newInstance() {
		if (PlayerSkinController.instance == null) { return true; }
		File file = CustomNpcs.Dir;
		return file != null && !PlayerSkinController.instance.filePath.equals(file.getName());
	}

	private void loadPlayerSkins() {
		File saveDir = CustomNpcs.Dir;
		if (saveDir == null) { return; }
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadPlayerSkins");
		}
		this.filePath = saveDir.getName();
		try {
			File file = new File(saveDir, "player_skins.dat");
			if (file.exists()) {
				this.loadPlayerSkins(file);
			}
		} catch (Exception e) { e.printStackTrace(); }
		CustomNpcs.debugData.endDebug("Common", null, "loadPlayerSkins");
	}

	private void loadPlayerSkins(File file) {
		try { loadPlayerSkins(CompressedStreamTools.readCompressed(new FileInputStream(file))); }
		catch (Exception e) { e.printStackTrace(); }
	}

	public void loadPlayerSkins(NBTTagCompound compound) throws Exception {
		playerNames.clear();
		playerTextures.clear();
		if (compound.hasKey("Data", 9)) {
			for (int i = 0; i < compound.getTagList("Data", 10).tagCount(); ++i) {
				this.loadPlayerSkin(compound.getTagList("Data", 10).getCompoundTagAt(i));
			}
		}
	}
	
	public UUID loadPlayerSkin(NBTTagCompound nbtSkin) {
		if (nbtSkin == null) { return null; }
		UUID uuid = nbtSkin.getUniqueId("UUID");
		playerNames.put(uuid, nbtSkin.getString("Player"));
		if (!playerTextures.containsKey(uuid)) { playerTextures.put(uuid, Maps.newEnumMap(Type.class)); }
		Map<Type, ResourceLocation> skins = playerTextures.get(uuid);
		for (int i = 0; i < nbtSkin.getTagList("Textures", 10).tagCount(); i++) {
			NBTTagCompound nbt = nbtSkin.getTagList("Textures", 10).getCompoundTagAt(i);
			Type type;
			switch(nbt.getString("Type").toLowerCase()) {
				case "cape": type = Type.CAPE; break;
				case "elytra": type = Type.ELYTRA; break;
				default: type = Type.SKIN; break;
			}
			skins.put(type, new ResourceLocation(AdditionalMethods.instance.deleteColor(nbt.getString("Location"))));
		}
		playerTextures.put(uuid, skins);
		return uuid;
	}
	
	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		NBTTagList listUUIDs = new NBTTagList();
		for (UUID uuid : playerTextures.keySet()) {
			NBTTagCompound nbtPlayer = new NBTTagCompound();
			nbtPlayer.setUniqueId("UUID", uuid);
			NBTTagList listTxrs = new NBTTagList();
			for (Type epst : playerTextures.get(uuid).keySet()) {
				ResourceLocation loc = playerTextures.get(uuid).get(epst);
				if (loc == null) { continue; }
				NBTTagCompound nbtSkin = new NBTTagCompound();
				nbtSkin.setString("Type", epst.name());
				nbtSkin.setString("Location", loc.toString());
				listTxrs.appendTag(nbtSkin);
			}
			nbtPlayer.setTag("Textures", listTxrs);
			nbtPlayer.setString("Player", playerNames.get(uuid) == null ? "null" : playerNames.get(uuid));
			listUUIDs.appendTag(nbtPlayer);
		}
		compound.setTag("Data", listUUIDs);
		return compound;
	}
	
	public NBTTagCompound getNBT(UUID uuid) {
		NBTTagCompound nbtPlayer = new NBTTagCompound();
		nbtPlayer.setUniqueId("UUID", uuid);
		NBTTagList listTxrs = new NBTTagList();
		for (Type epst : playerTextures.get(uuid).keySet()) {
			ResourceLocation loc = playerTextures.get(uuid).get(epst);
			if (loc == null) { continue; }
			NBTTagCompound nbtSkin = new NBTTagCompound();
			nbtSkin.setString("Type", epst.name());
			nbtSkin.setString("Location", loc.toString());
			listTxrs.appendTag(nbtSkin);
		}
		nbtPlayer.setTag("Textures", listTxrs);
		nbtPlayer.setString("Player", playerNames.get(uuid) == null ? "null" : playerNames.get(uuid));
		return nbtPlayer;
	}
	
	public void save() {
		try { CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(new File(CustomNpcs.Dir, "player_skins.dat"))); }
		catch (Exception e) { e.printStackTrace(); }
	}

	public void logged(EntityPlayerMP player) {
		UUID uuid = player.getUniqueID();
		if (playerTextures.containsKey(uuid)) {
			playerNames.put(uuid, player.getName());
			sendToAll(player);
		}
		else { Server.sendData(player, EnumPacketClient.PLAYER_SKIN_GET); }
		for (EntityPlayerMP pl : player.getServer().getPlayerList().getPlayers()) {
			if (pl.equals(player) || !playerTextures.containsKey(pl.getUniqueID())) { continue; }
			Server.sendData(player, EnumPacketClient.PLAYER_SKIN_ADD, getNBT(pl.getUniqueID()));
		}
	}

	public void sendToAll(EntityPlayerMP player) {
		UUID uuid = player.getUniqueID();
		if (!playerTextures.containsKey(uuid)) { return; }
		playerNames.put(uuid, player.getName());
		NBTTagCompound nbtPlayer = getNBT(uuid);
		for (EntityPlayerMP pl : player.getServer().getPlayerList().getPlayers()) { Server.sendData(pl, EnumPacketClient.PLAYER_SKIN_ADD, nbtPlayer); }
	}

	public Map<Type, ResourceLocation> getData(UUID uuid) {
		if (!playerTextures.containsKey(uuid)) {
			playerTextures.put(uuid, Maps.newEnumMap(Type.class));
			playerTextures.get(uuid).put(Type.SKIN, new ResourceLocation("minecraft", (uuid.hashCode() & 1) == 1 ? "textures/entity/alex.png" : "textures/entity/steve.png")); // DefaultPlayerSkin
		}
		return playerTextures.get(uuid);
	}

	public String get(EntityPlayerMP player, int type) {
		if (type < 0) { type *= -1; }
		Map<Type, ResourceLocation> data = getData(player.getUniqueID());
		ResourceLocation loc = data.get(Type.values()[type % Type.values().length]);
		return loc == null ? null : loc.toString();
	}

	public void set(EntityPlayerMP player, boolean isSmallArms, int body, int bodyColor, int hair, int hairColor, int face, int eyesColor, int leg, int jacket, int shoes, int ... peculiarities) {
		UUID uuid = player.getUniqueID();
		if (!playerTextures.containsKey(uuid)) { playerTextures.put(uuid, Maps.newEnumMap(Type.class)); }
		Map<Type, ResourceLocation> data = getData(player.getUniqueID());
		String path = "textures/entity/custom/" + (isSmallArms ? "female" : "male") + "_" + body + "_" + bodyColor + "_" + hair + "_" + hairColor + "_" + face + "_" + eyesColor + "_" + leg + "_" + jacket + "_" + shoes;
		for (int id : peculiarities) { path += "_"+id; }
		path += ".png";
		data.put(Type.SKIN, new ResourceLocation(CustomNpcs.MODID, AdditionalMethods.instance.deleteColor(path)));
		playerTextures.put(uuid, data);
		sendToAll(player);
	}

	public void set(EntityPlayerMP player, String location, int type) {
		UUID uuid = player.getUniqueID();
		if (type < 0) { type *= -1; }
		Type t = Type.values()[type % Type.values().length];
		if (!playerTextures.containsKey(uuid)) {
			playerTextures.put(uuid, Maps.newEnumMap(Type.class));
			playerTextures.get(uuid).put(Type.SKIN, new ResourceLocation("minecraft", (uuid.hashCode() & 1) == 1 ? "textures/entity/alex.png" : "textures/entity/steve.png")); // DefaultPlayerSkin
		}
		Map<Type, ResourceLocation> data = getData(player.getUniqueID());
		if (location == null || location.isEmpty()) { data.remove(t); }
		else { data.put(t, new ResourceLocation(AdditionalMethods.instance.deleteColor(location))); }
		playerTextures.put(uuid, data);
		sendToAll(player);
	}
    
}
