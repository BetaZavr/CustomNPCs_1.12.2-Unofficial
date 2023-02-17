package noppes.npcs.client;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.util.ObfuscationHelper;

public class AssetsBrowser {
	public static String getRoot(String asset) {
		String mod = "minecraft";
		int index = asset.indexOf(":");
		if (index > 0) {
			mod = asset.substring(0, index);
			asset = asset.substring(index + 1);
		}
		if (asset.startsWith("/")) {
			asset = asset.substring(1);
		}
		String location = "/" + mod + "/" + asset;
		index = location.lastIndexOf("/");
		if (index > 0) {
			location = location.substring(0, index);
		}
		return location;
	}

	private int depth;
	private String[] extensions;
	public HashSet<String> files;
	private String folder;
	public HashSet<String> folders;

	public boolean isRoot;

	public AssetsBrowser(String folder, String[] extensions) {
		this.folders = new HashSet<String>();
		this.files = new HashSet<String>();
		this.extensions = extensions;
		this.setFolder(folder);
	}

	public AssetsBrowser(String[] extensions) {
		this.folders = new HashSet<String>();
		this.files = new HashSet<String>();
		this.extensions = extensions;
	}

	private void checkFile(String name) {
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		if (!name.startsWith(this.folder)) {
			return;
		}
		String[] split = name.split("/");
		int count = split.length;
		if (count == this.depth + 1) {
			if (this.validExtension(name)) {
				this.files.add(split[this.depth]);
			}
		} else if (this.depth + 1 < count) {
			this.folders.add(split[this.depth]);
		}
	}

	private void checkFolder(File file, int length) {
		File[] files = file.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			String name = f.getAbsolutePath().substring(length);
			name = name.replace("\\", "/");
			if (!name.startsWith("/")) {
				name = "/" + name;
			}
			if (f.isDirectory() && (this.folder.startsWith(name) || name.startsWith(this.folder))) {
				this.checkFile(name + "/");
				this.checkFolder(f, length);
			} else {
				this.checkFile(name);
			}
		}
	}

	public String getAsset(String asset) {
		String[] split = this.folder.split("/");
		if (split.length < 3) {
			return null;
		}
		String texture = split[2] + ":";
		texture = texture + this.folder.substring(texture.length() + 8) + asset;
		return texture;
	}

	private void getFiles() {
		this.folders.clear();
		this.files.clear();
		SimpleReloadableResourceManager simplemanager = (SimpleReloadableResourceManager) Minecraft.getMinecraft()
				.getResourceManager();
		Map<String, IResourceManager> map = ObfuscationHelper.getValue(SimpleReloadableResourceManager.class, simplemanager, 2);
		HashSet<String> set = new HashSet<String>();
		for (String name : map.keySet()) {
			if (!(map.get(name) instanceof FallbackResourceManager)) {
				continue;
			}
			FallbackResourceManager manager = (FallbackResourceManager) map.get(name);
			List<IResourcePack> list = ObfuscationHelper.getValue(FallbackResourceManager.class,
					manager, 1);
			for (IResourcePack pack : list) {
				if (pack instanceof AbstractResourcePack) {
					AbstractResourcePack p = (AbstractResourcePack) pack;
					File file = ObfuscationHelper.getValue(AbstractResourcePack.class, p, 1);
					if (file == null) {
						continue;
					}
					set.add(file.getAbsolutePath());
				}
			}
		}
		for (String file2 : set) {
			this.progressFile(new File(file2));
		}
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getSource().exists()) {
				this.progressFile(mod.getSource());
			}
		}
		ResourcePackRepository repos = Minecraft.getMinecraft().getResourcePackRepository();
		List<ResourcePackRepository.Entry> list2 = (List<ResourcePackRepository.Entry>) repos.getRepositoryEntries();
		for (ResourcePackRepository.Entry entry : list2) {
			File file3 = new File(repos.getDirResourcepacks(), entry.getResourcePackName());
			if (file3.exists()) {
				this.progressFile(file3);
			}
		}
		this.checkFolder(new File(CustomNpcs.Dir, "assets"), CustomNpcs.Dir.getAbsolutePath().length());
	}

	private void progressFile(File file) {
		try {
			if (!file.isDirectory() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {
				ZipFile zip = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zip.entries();
				while (entries.hasMoreElements()) {
					ZipEntry zipentry = (ZipEntry) entries.nextElement();
					String entryName = zipentry.getName();
					this.checkFile(entryName);
				}
				zip.close();
			} else if (file.isDirectory()) {
				int length = file.getAbsolutePath().length();
				this.checkFolder(file, length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setFolder(String folder) {
		if (!folder.endsWith("/")) {
			folder += "/";
		}
		this.isRoot = (folder.length() <= 1);
		this.folder = "/assets" + folder;
		this.depth = StringUtils.countMatches((CharSequence) this.folder, (CharSequence) "/");
		this.getFiles();
	}

	private boolean validExtension(String entryName) {
		int index = entryName.lastIndexOf(".");
		if (index < 0) {
			return false;
		}
		String extension = entryName.substring(index + 1);
		for (String ex : this.extensions) {
			if (ex.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}
}
