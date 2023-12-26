package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeMap;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IKeyBinding;
import noppes.npcs.api.handler.data.IKeySetting;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.data.KeyConfig;

public class KeyController
implements IKeyBinding {
	
	private static KeyController instance;
	public final TreeMap<Integer, IKeySetting> keybindings;
	public static int version = 0;
	private String filePath;
	
	public KeyController() {
		KeyController.instance = this;
		this.filePath = CustomNpcs.Dir.getAbsolutePath();
		this.keybindings = Maps.<Integer, IKeySetting>newTreeMap();
		this.loadKeys();
	}
	
	public static KeyController getInstance() {
		if (newInstance()) { KeyController.instance = new KeyController(); }
		return KeyController.instance;
	}

	private static boolean newInstance() {
		if (KeyController.instance == null ) { return true; }
		return CustomNpcs.Dir != null && !KeyController.instance.filePath.equals(CustomNpcs.Dir.getAbsolutePath());
	}
	
	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (int id : this.keybindings.keySet()) {
			NBTTagCompound nbtKey = ((KeyConfig) this.keybindings.get(id)).write();
			nbtKey.setInteger("ID", id);
			list.appendTag(nbtKey);
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Data", list);
		return compound;
	}
	
	private void loadKeys() {
		File saveDir = CustomNpcs.Dir;
		if (saveDir == null) { return; }
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadKeys");
		}
		this.filePath = saveDir.getName();
		try {
			File file = new File(saveDir, "keys.dat");
			if (file.exists()) {
				this.loadKeys(file);
			} else {
				this.loadDefaultKeys(-1);
			}
		} catch (Exception e) { this.loadDefaultKeys(-1); }
		CustomNpcs.debugData.endDebug("Common", null, "loadKeys");
	}

	private void loadKeys(File file) throws IOException {
		this.loadKeys(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}

	public void loadKeys(NBTTagCompound compound) {
		if (compound==null) { return; }
		this.keybindings.clear();
		if (compound.hasKey("Data", 9)) {
			for (int i = 0; i < compound.getTagList("Data", 10).tagCount(); ++i) {
				this.loadKey(compound.getTagList("Data", 10).getCompoundTagAt(i));
			}
		}
	}

	public IKeySetting loadKey(NBTTagCompound nbtKey) {
		if (nbtKey==null || !nbtKey.hasKey("ID", 3) || nbtKey.getInteger("ID")<0) { return null; }
		int id = nbtKey.getInteger("ID");
		KeyConfig ac;
		if (this.keybindings.containsKey(id)) {
			((KeyConfig) this.keybindings.get(id)).read(nbtKey);
			return this.keybindings.get(id);
		}
		ac = new KeyConfig(id);
		ac.read(nbtKey);
		this.keybindings.put(id, ac);
		return this.keybindings.get(id);
	}

	private void loadDefaultKeys(int version) {
		if (version == KeyController.version) { return; }
		KeyConfig ac = new KeyConfig(0);
		this.keybindings.put(0, ac);
		this.save();
	}
	
	public void save() {
		CustomNpcs.debugData.startDebug("Common", null, "saveKeys");
		try { CompressedStreamTools.writeCompressed(this.getNBT(), new FileOutputStream(new File(CustomNpcs.Dir, "keys.dat"))); }
		catch (Exception e) { }
		CustomNpcs.debugData.startDebug("Common", null, "saveKeys");
	}
	
	public int getUnusedId() {
		int id = 0;
		for (int i : this.keybindings.keySet()) { if (i>=id) { id = i + 1; } }
		return id;
	}

	public void update(int id) {
		IKeySetting kb = this.keybindings.get(id);
		if (kb!=null) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.KeysData, ((KeyConfig) kb).write()); } // change or add
		else { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, EnumSync.KeysData, id); } // remove
	}
	
	@Override
	public IKeySetting getKeySetting(int id) {
		return this.keybindings.get(id);
	}

	@Override
	public IKeySetting[] getKeySettings() {
		return this.keybindings.values().toArray(new IKeySetting[this.keybindings.size()]);
	}

	@Override
	public boolean removeKeySetting(int id) {
		return this.keybindings.remove(id)!=null;
	}

	@Override
	public IKeySetting createKeySetting() {
		KeyConfig ac = new KeyConfig(this.getUnusedId());
		this.keybindings.put(ac.getId(), ac);
		this.update(ac.getId());
		return ac;
	}

	public IKeySetting getKeySetting(String name, String category, int keyId, String modifer) {
		for (IKeySetting kb : this.keybindings.values()) {
			if (kb.getKeyId()!=keyId) { continue; }
			KeyConfig kc = (KeyConfig) kb;
			if (!kc.name.equals(name) || !kc.category.equals(category)) { continue; }
			switch(modifer.toLowerCase()) {
				case "shift": if (kc.modifer==1) { return kc; } break;
				case "control": if (kc.modifer==2) { return kc; } break;
				case "alt": if (kc.modifer==3) { return kc; } break;
				default: return kc;
			}
		}
		return null;
	}
	
}
