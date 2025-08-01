package noppes.npcs.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.TreeMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IKeyBinding;
import noppes.npcs.api.handler.data.IKeySetting;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.data.KeyConfig;

public class KeyController implements IKeyBinding {

	private static KeyController instance;
	public static KeyController getInstance() {
		if (newInstance()) {
			KeyController.instance = new KeyController();
		}
		return KeyController.instance;
	}
	private static boolean newInstance() {
		if (KeyController.instance == null) {
			return true;
		}
		return CustomNpcs.Dir != null && !KeyController.instance.filePath.equals(CustomNpcs.Dir.getAbsolutePath());
	}

	public final TreeMap<Integer, IKeySetting> keybindings = new TreeMap<>();

	private String filePath;

	public KeyController() {
		KeyController.instance = this;
		this.filePath = CustomNpcs.Dir.getAbsolutePath();
		this.loadKeys();
	}

	@Override
	public IKeySetting createKeySetting() {
		KeyConfig ac = new KeyConfig(this.getUnusedId());
		this.keybindings.put(ac.getId(), ac);
		this.update(ac.getId());
		return ac;
	}

	@Override
	public IKeySetting getKeySetting(int id) {
		return this.keybindings.get(id);
	}

	@SuppressWarnings("all")
	public IKeySetting getKeySetting(String name, String category, int keyId, String modifier) {
		for (IKeySetting kb : this.keybindings.values()) {
			if (kb.getKeyId() != keyId) {
				continue;
			}
			KeyConfig kc = (KeyConfig) kb;
			if (!kc.name.equals(name) || !kc.category.equals(category)) {
				continue;
			}
			switch (modifier.toLowerCase()) {
			case "shift":
				if (kc.modifer == 1) {
					return kc;
				}
				break;
			case "control":
				if (kc.modifer == 2) {
					return kc;
				}
				break;
			case "alt":
				if (kc.modifer == 3) {
					return kc;
				}
				break;
			default:
				return kc;
			}
		}
		return null;
	}

	@Override
	public IKeySetting[] getKeySettings() {
		return this.keybindings.values().toArray(new IKeySetting[0]);
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

	public int getUnusedId() {
		int id = 0;
		for (int i : this.keybindings.keySet()) {
			if (i >= id) {
				id = i + 1;
			}
		}
		return id;
	}

	private void loadDefaultKeys() {
		KeyConfig ac = new KeyConfig(0);
		this.keybindings.put(0, ac);
		this.save();
	}

	public void loadKey(NBTTagCompound nbtKey) {
		if (nbtKey == null || !nbtKey.hasKey("ID", 3) || nbtKey.getInteger("ID") < 0) {
			return;
		}
		int id = nbtKey.getInteger("ID");
		KeyConfig ac;
		if (this.keybindings.containsKey(id)) {
			((KeyConfig) this.keybindings.get(id)).read(nbtKey);
			this.keybindings.get(id);
			return;
		}
		ac = new KeyConfig(id);
		ac.read(nbtKey);
		this.keybindings.put(id, ac);
		this.keybindings.get(id);
	}

	private void loadKeys() {
		CustomNpcs.debugData.start(null);
		File saveDir = CustomNpcs.Dir;
		if (saveDir == null) {
			return;
		}
		this.filePath = saveDir.getName();
		try {
			File file = new File(saveDir, "keys.dat");
			if (file.exists()) {
				this.loadKeys(file);
			} else {
				this.loadDefaultKeys();
			}
		} catch (Exception e) {
			this.loadDefaultKeys();
		}
		CustomNpcs.debugData.end(null);
	}

	private void loadKeys(File file) throws IOException {
		this.loadKeys(CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath())));
	}

	public void loadKeys(NBTTagCompound compound) {
		if (compound == null) {
			return;
		}
		this.keybindings.clear();
		if (compound.hasKey("Data", 9)) {
			for (int i = 0; i < compound.getTagList("Data", 10).tagCount(); ++i) {
				this.loadKey(compound.getTagList("Data", 10).getCompoundTagAt(i));
			}
		}
	}

	@Override
	public void removeKeySetting(int id) {
		this.keybindings.remove(id);
	}

	public void save() {
		CustomNpcs.debugData.start(null);
		try {
			CompressedStreamTools.writeCompressed(this.getNBT(), Files.newOutputStream(new File(CustomNpcs.Dir, "keys.dat").toPath()));
		} catch (Exception e) { LogWriter.error(e); }
		CustomNpcs.debugData.end(null);
	}

	public void update(int id) {
		IKeySetting kb = this.keybindings.get(id);
		if (kb != null) {
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.KeysData,
					((KeyConfig) kb).write());
		} // change or add
		else {
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, EnumSync.KeysData, id);
		} // remove
	}

}
