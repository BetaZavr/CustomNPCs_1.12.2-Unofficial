package noppes.npcs;

import java.io.*;
import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.RecipeBook;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.blocks.CustomBlock;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.blocks.CustomBlockSlab;
import noppes.npcs.blocks.CustomBlockStairs;
import noppes.npcs.blocks.CustomChest;
import noppes.npcs.blocks.CustomDoor;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.blocks.tiles.CustomTileEntityChest;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.containers.*;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.items.CustomBow;
import noppes.npcs.items.CustomFishingRod;
import noppes.npcs.items.CustomShield;
import noppes.npcs.items.CustomTool;
import noppes.npcs.items.CustomWeapon;
import noppes.npcs.api.mixin.entity.player.IEntityPlayerMPMixin;
import noppes.npcs.util.Util;
import noppes.npcs.util.TempFile;

public class CommonProxy implements IGuiHandler {

	public static final Map<String, TempFile> downloadableFiles = new HashMap<>();
	public static final Map<EntityPlayer, Availability> availabilityStacks = new HashMap<>();

	public boolean newVersionAvailable;
	public int revision;

	public CommonProxy() {
		this.newVersionAvailable = false;
		this.revision = 4;
	}

	public void checkBlockFiles(ICustomElement customblock) {
		// get data dirs
		String name = customblock.getCustomName().toLowerCase();
		String fileName = Objects.requireNonNull(((Block) customblock).getRegistryName()).getResourcePath().toLowerCase();
		File blockstatesDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/blockstates");
		File blockModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/block");
		File itemModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/item");
		if (!blockstatesDir.exists() && !blockstatesDir.mkdirs()) { return; }
		if (!blockModelsDir.exists() && !blockModelsDir.mkdirs()) { return; }
		if (!itemModelsDir.exists() && !itemModelsDir.mkdirs()) { return; }
		String crEnt = "" + ((char) 10);
		String crTab = "" + ((char) 9);

		// Standard orientable base block:
		File orientable = new File(blockModelsDir, "orientable.json");
		if (!orientable.exists() && Util.instance.saveFile(orientable, Util.instance.getDataFile("ort.dat"))) { LogWriter.debug("Create Orientable Block Model for \"orientable\" block"); }

		// Standard chest base block:
		File chest = new File(blockModelsDir, "chest.json");
		if (!chest.exists() && Util.instance.saveFile(chest, Util.instance.getDataFile("jch.dat"))) { LogWriter.debug("Create Chest Block Model for \"custom chest\" block"); }

		// Block state file:
		File blockstate = new File(blockstatesDir, fileName.toLowerCase() + ".json");
		if (!blockstate.exists()) {
			String jsonState = null;
			if (customblock instanceof CustomLiquid) {
				jsonState = Util.instance.getDataFile("jlq.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
			} else if (customblock instanceof CustomBlockStairs) {
				jsonState = Util.instance.getDataFile("jbs.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
			} else if (customblock instanceof CustomBlockSlab) {
				if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
					jsonState = Util.instance.getDataFile("jss.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
				} else {
					jsonState = Util.instance.getDataFile("jsd.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
				}
			} else if (customblock instanceof CustomBlockPortal) {
				jsonState = Util.instance.getDataFile("jbp.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
			} else if (customblock instanceof CustomBlock && ((CustomBlock) customblock).hasProperty()) {
				NBTTagCompound data = ((CustomBlock) customblock).nbtData.getCompoundTag("Property");
				jsonState = Util.instance.getDataFile("jpr.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
				StringBuilder variants = new StringBuilder();
				if (data.getByte("Type") == (byte) 1) {
					variants.append(crTab).append(crTab).append("\"").append(data.getString("Name")).append("=true\": { \"model\": \"").append(CustomNpcs.MODID).append(":").append(fileName.toLowerCase()).append("_true\" },").append(crEnt);
					variants.append(crTab).append(crTab).append("\"").append(data.getString("Name")).append("=false\": { \"model\": \"").append(CustomNpcs.MODID).append(":").append(fileName.toLowerCase()).append("_false\" }");
					jsonState = jsonState.replace("{type}", "Byte").replace("{variants}", variants.toString());
				} else if (data.getByte("Type") == (byte) 3) {
					for (int i = data.getInteger("Min"); i <= data.getInteger("Max"); i++) {
						variants.append(crTab).append(crTab).append("\"").append(data.getString("Name")).append("=").append(i).append("\": { \"model\": \"").append(CustomNpcs.MODID).append(":").append(fileName.toLowerCase()).append("_").append(i).append("\" }");
						if (i < data.getInteger("Max") - 1) { variants.append(",").append(crEnt); }
					}
					jsonState = jsonState.replace("{type}", "Integer").replace("{variants}", variants.toString());
				} else if (data.getByte("Type") == (byte) 4) {
					int i = 0;
					for (EnumFacing ef : EnumFacing.VALUES) {
						if (ef == EnumFacing.DOWN || ef == EnumFacing.UP) {
							continue;
						}
						variants.append(crTab).append(crTab).append("\"").append(data.getString("Name")).append("=").append(ef.getName2()).append("\": { \"model\": \"").append(CustomNpcs.MODID).append(":").append(fileName.toLowerCase()).append("\"");
						if (ef == EnumFacing.SOUTH) { variants.append(", \"y\": 180"); }
						else if (ef == EnumFacing.WEST) { variants.append(", \"y\": 270"); }
						else if (ef == EnumFacing.EAST) { variants.append(", \"y\": 90"); }
						variants.append(" }");
						if (i < 3) { variants.append(",").append(crEnt); }
						i++;
					}
				}
				jsonState = jsonState.replace("{type}", "Fasing").replace("{variants}", variants.toString());
			} else if (customblock instanceof CustomDoor) {
				jsonState = Util.instance.getDataFile("jbd.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
			} else if (customblock instanceof CustomChest) {
				jsonState = Util.instance.getDataFile("jb" + (((CustomChest) customblock).isChest ? "h" : "c") + ".dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
			}
			if (jsonState == null) {
				jsonState = Util.instance.getDataFile("jb.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
			}
			if (Util.instance.saveFile(blockstate, jsonState)) {
				LogWriter.debug("Create Default Blockstate for \"" + fileName + "\" block");
			}
		}

		// Block model file:
		File blockModel = new File(blockModelsDir, fileName + ".json");
		if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
			blockModel = new File(blockModelsDir, "bottom_" + fileName + ".json");
		}
		if (!blockModel.exists()) {
			String jsonModel = null;
			if (customblock instanceof CustomBlockStairs) {
				jsonModel = Util.instance.getDataFile("bms.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
				if (Util.instance.saveFile(blockModel, jsonModel.replace("{type}", ""))) { LogWriter.debug("Create Default Stairs Block Model for \"" + fileName + "\" block"); }
				if (Util.instance.saveFile(new File(blockModelsDir, fileName + "_inner.json"), jsonModel.replace("{type}", "inner_"))) { LogWriter.debug("Create Default Inner Stairs Block Model for \"" + fileName + "\" block"); }
				if (Util.instance.saveFile(new File(blockModelsDir, fileName + "_outer.json"), jsonModel.replace("{type}", "outer_"))) { LogWriter.debug("Create Default Outer Stairs Block Model for \"" + fileName + "\" block"); }
			}
			else if (customblock instanceof CustomBlockSlab) {
				if (customblock instanceof CustomBlockSlab.CustomBlockSlabSingle) {
					jsonModel = Util.instance.getDataFile("bmss.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
					boolean bo = Util.instance.saveFile(blockModel, jsonModel.replace("{type}", "half")); // bottom_
					if (Util.instance.saveFile(new File(blockModelsDir, "upper_" + fileName + ".json"), jsonModel.replace("{type}", "upper")) && bo) { LogWriter.debug("Create Default Slab Simple Block Model for \"" + fileName + "\" block"); }
				}
				if (customblock instanceof CustomBlockSlab.CustomBlockSlabDouble) {
					jsonModel = "";
					boolean bo = Util.instance.saveFile(blockModel, Util.instance.getDataFile("bmsd.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName.replace("double_", "")));
					if (Util.instance.saveFile(new File(blockModelsDir, fileName + "_top.json"), Util.instance.getDataFile("bmsdt.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName.replace("double_", ""))) && bo) { LogWriter.debug("Create Default Slab Blocks Model for \"" + fileName + "\" block"); }
				}
			}
			else if (customblock instanceof CustomDoor) {
				jsonModel = Util.instance.getDataFile("bmd.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
				boolean bo = Util.instance.saveFile(new File(blockModelsDir, fileName + "_bottom.json"), jsonModel.replace("{type}", "door_bottom"));
				bo = Util.instance.saveFile(new File(blockModelsDir, fileName + "_bottom_rh.json"), jsonModel.replace("{type}", "door_bottom_rh")) && bo;
				bo = Util.instance.saveFile(new File(blockModelsDir, fileName + "_top.json"), jsonModel.replace("{type}", "door_top")) && bo;
				bo = Util.instance.saveFile(new File(blockModelsDir, fileName + "_top_rh.json"), jsonModel.replace("{type}", "door_top_rh")) && bo;
				if (Util.instance.saveFile(blockModel, jsonModel.replace("{type}", "door_bottom")) && bo) { LogWriter.debug("Create Default Door Blocks Model for \"" + fileName + "\" block"); }
			}
			else if (customblock instanceof CustomChest) {
				boolean isChest = ((CustomChest) customblock).isChest;
				if (isChest) {
					jsonModel = Util.instance.getDataFile("bmh.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
					if (Util.instance.saveFile(blockModel, jsonModel)) { LogWriter.debug("Create Default Chest Blocks Model for \"" + fileName + "\" block"); }
				} else {
					jsonModel = Util.instance.getDataFile("bmc_m.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
					boolean bo = Util.instance.saveFile(new File(blockModelsDir, "obj/" + fileName + ".mtl"), jsonModel);
					jsonModel = Util.instance.getDataFile("bmc_o.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName);
					if (Util.instance.saveFile(new File(blockModelsDir, "obj/" + fileName + ".obj"), jsonModel) && bo) {
						LogWriter.debug("Create Default Container Blocks Model for \"" + fileName + "\" block");
					}
				}
			}
			else {
				if (customblock instanceof CustomBlock && ((CustomBlock) customblock).hasProperty()) {
					if (((CustomBlock) customblock).FACING != null) {
						jsonModel = Util.instance.getDataFile("bmp.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name);
						if (Util.instance.saveFile(blockModel, jsonModel)) { LogWriter.debug("Create Default Facing Block Model for \"" + fileName.toLowerCase() + "\" block"); }
					}
				}
			}
			if (jsonModel == null) {
				String texture = "blocks/" + name;
				if (customblock instanceof CustomBlockPortal) { texture = "environment/custom_" + name + "_portal"; }
				jsonModel = Util.instance.getDataFile("bm.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", texture);
				if (Util.instance.saveFile(blockModel, jsonModel)) { LogWriter.debug("Create Default Block Model for \"" + fileName.toLowerCase() + "\" block"); }
			}
		}

		// Item model file:
		File itemModel = new File(itemModelsDir, fileName.toLowerCase() + ".json");
		if (!itemModel.exists()) {
			if (Util.instance.saveFile(itemModel, Util.instance.getDataFile("bmi" + (customblock instanceof CustomDoor ? "d" : "") + ".dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName))) { LogWriter.debug("Create Default Block Item Model for \"" + name + "\" block"); }
		}
	}

	public void checkItemFiles(ICustomElement customitem) {
		String name = customitem.getCustomName().toLowerCase();
		String fileName = Objects.requireNonNull(((Item) customitem).getRegistryName()).getResourcePath().toLowerCase();
		NBTTagCompound nbtData = customitem.getCustomNbt().getMCNBT();
		File itemModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/item");
		if (!itemModelsDir.exists() && !itemModelsDir.mkdirs()) { return; }
		File itemModel = new File(itemModelsDir, fileName + ".json");

        // OBJ Armor model
		if (customitem instanceof CustomArmor && (nbtData.hasKey("OBJData", 9) || nbtData.hasKey("OBJData", 10))) {
			File armorModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/armor");
			if (!armorModelsDir.exists() && !armorModelsDir.mkdirs()) { return; }
			File objModel = new File(armorModelsDir, name + ".obj");
			if (!objModel.exists()) {
				boolean bo = Util.instance.saveFile(objModel, Util.instance.getDataFile("am_o.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name));
				if (Util.instance.saveFile(new File(armorModelsDir, name + ".mtl"), Util.instance.getDataFile("am_m.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name)) && bo) {
					LogWriter.debug("Create default OBJ armor \"" + fileName + "\"");
				}
			}
		}
		// Item model
		if (!itemModel.exists()) {
			String jsonModel = null;
			if (customitem instanceof CustomBow) {
				boolean bo = true;
				jsonModel = Util.instance.getDataFile("imbp.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName).replace("{name}", name);
				for (int i = 0; i < 3; i++) {
					File pulling = new File(itemModelsDir, fileName + "_pulling_" + i + ".json");
					if (!pulling.exists()) { bo = Util.instance.saveFile(pulling, jsonModel.replace("{num}", "" + i)) && bo; }
				}
				if (Util.instance.saveFile(itemModel, Util.instance.getDataFile("imb.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName).replace("{name}", name)) && bo) {
					LogWriter.debug("Create Default Bow Item Model for \"" + name + "\" item");
				}
			}
			else if (customitem instanceof CustomShield) {
				jsonModel = Util.instance.getDataFile("imsb.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName).replace("{name}", name);
				boolean bo = true;
				File blocking = new File(itemModelsDir, fileName + "_blocking.json");
				if (!blocking.exists()) { bo = Util.instance.saveFile(blocking, jsonModel); }
				if (Util.instance.saveFile(itemModel, Util.instance.getDataFile("ims.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName).replace("{name}", name)) && bo) {
					LogWriter.debug("Create Default Bow Item Model for \"" + name + "\" item");
				}
			}
			else if (customitem instanceof CustomFishingRod) {
				jsonModel = Util.instance.getDataFile("imfc.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name);
				boolean bo = true;
				File cast = new File(itemModelsDir, fileName.toLowerCase() + "_cast.json");
				if (!cast.exists()) { bo = Util.instance.saveFile(cast, jsonModel); }
				if (Util.instance.saveFile(itemModel, Util.instance.getDataFile("imf.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{file_name}", fileName).replace("{name}", name)) && bo) {
					LogWriter.debug("Create Default Bow Item Model for \"" + name + "\" item");
				}
			}
			else if (customitem instanceof CustomWeapon) {
				jsonModel = Util.instance.getDataFile("imw.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name);
				if (Util.instance.saveFile(itemModel, jsonModel)) {
					LogWriter.debug("Create Default Weapon Item Model for \"" + name + "\" item");
				}
			}
			else if (customitem instanceof CustomTool) {
				if (name.equals("axeexample")) {
					// obj model
					File blockModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/block/obj");
					if (!blockModelsDir.exists() && !blockModelsDir.mkdirs()) { return; }
					File objModel = new File(blockModelsDir, name + ".obj");
					boolean bo = true;
					if (!objModel.exists()) {
						File obj = new File(blockModelsDir, name + ".obj");
						if (!obj.exists()) { bo = Util.instance.saveFile(obj, Util.instance.getDataFile("ima_o.dat").replace("{name}", name)); }
						File mtl = new File(blockModelsDir, name + ".mtl");
						if (!mtl.exists()) { bo = Util.instance.saveFile(mtl, Util.instance.getDataFile("ima_m.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name)) && bo; }
					}
					// block state
					File blockStatesDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/blockstates");
					if (!blockStatesDir.exists() && !blockStatesDir.mkdirs()) { return; }
					File itemState = new File(blockStatesDir, "custom_" + name + ".json");
					if (!itemState.exists()) {
						bo = Util.instance.saveFile(itemState, Util.instance.getDataFile("imas.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name)) && bo;
					}
					// item model
					jsonModel = Util.instance.getDataFile("ima.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name);
					if (Util.instance.saveFile(itemModel, jsonModel) && bo) {
						LogWriter.debug("Create Default Item Axe Model for \"" + name + "\" item");
					}
				} else {
					jsonModel = Util.instance.getDataFile("imt.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name);
					if (Util.instance.saveFile(itemModel, jsonModel)) {
						LogWriter.debug("Create Default Tool Item Model for \"" + name + "\" item");
					}
				}
			}
			else if (customitem instanceof CustomArmor) {
				if (((CustomArmor) customitem).objModel == null) {
					jsonModel = Util.instance.getDataFile("imr.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name + "_" + ((CustomArmor) customitem).armorType.name().toLowerCase());
					if (Util.instance.saveFile(itemModel, jsonModel)) {
						LogWriter.debug("Create Default Armor Item Model for \"" + name + "\" item");
					}
				} else {
					jsonModel = Util.instance.getDataFile("imro.dat");
					if (Util.instance.saveFile(itemModel, jsonModel)) {
						LogWriter.debug("Create Default OBJ Armor Item Model for \"" + name + "\" item");
					}
				}
			}
			// simple model
			if (jsonModel == null && Util.instance.saveFile(itemModel, Util.instance.getDataFile("im.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name))) {
				LogWriter.debug("Create Default Item Model for \"" + name + "\" item");
			}
		}
	}

	public void checkParticleFiles(ICustomElement customparticle) {

	}

	public void checkPotionFiles(ICustomElement custompotion) {

	}

	public void checkTexture(EntityNPCInterface npc) {
	}

	public void fixTileEntityData(TileEntity tile) {
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SET_TILE_DATA, tile.writeToNBT(new NBTTagCompound()));
	}

	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	public Container getContainer(EnumGuiType gui, EntityPlayer player, int x, int y, int z, EntityNPCInterface npc) {
		switch (gui) {
			case AvailabilityStack: {
				return new ContainerAvailabilityInv(player);
			}
			case CustomContainer: {
				TileEntity tile = player.world.getTileEntity(new BlockPos(x, y, z));
				if (tile instanceof CustomTileEntityChest) {
					return ((CustomTileEntityChest) tile).createContainer(player.inventory, player);
				}
				return null;
			}
			case CustomChest: {
				return new ContainerCustomChest(player, x);
			}
			case MainMenuInv: {
				return new ContainerNPCInv(npc, player);
			}
			case MainMenuInvDrop: {
				return new ContainerNPCDropSetup(npc, player, x, y, z);
			} // New
			case ManageTransport: {
				TransportLocation loc = TransportController.getInstance().getTransport(x);
				if (loc == null) {
					loc = new TransportLocation();
					loc.id = x;
					loc.category = TransportController.getInstance().categories.get(y);
				}
				if (player.world.isRemote) {
					loc = loc.copy();
				}
				return new ContainerNPCTransportSetup(player, loc, y);
			}
			case PlayerAnvil: {
				return new ContainerCarpentryBench(player.inventory, player.world, new BlockPos(x, y, z));
			}
			case PlayerBank: {
				Bank bank = BankController.getInstance().getBank(x);
				if (bank == null) {
					bank = new Bank();
				}
				return new ContainerNPCBank(player, bank, y, z);
			}
			case PlayerFollowerHire:
            case PlayerFollower: {
				return new ContainerNPCFollowerHire(npc, player, x);
			}
            case PlayerTrader: {
				if (npc != null) {
					return new ContainerNPCTrader(npc, player);
				}
				return new ContainerNPCTrader(x, player);
			}
			case SetupItemGiver: {
				return new ContainerNpcItemGiver(npc, player);
			}
			case SetupTraderDeal: { // Change
				MarcetController mData = MarcetController.getInstance();
				Marcet marcet = (Marcet) mData.getMarcet(x);
				if (marcet == null) {
					marcet = new Marcet(x);
				}
				Deal deal = (Deal) mData.getDeal(y);
				if (deal == null) {
					deal = new Deal(y);
				}
				return new ContainerNPCTraderSetup(marcet, deal, player);
			}
			case SetupFollower: {
				return new ContainerNPCFollowerSetup(npc, player);
			}
			case QuestReward: {
				return new ContainerNpcQuestReward(player);
			}
			case QuestTypeItem: {
				return new ContainerNpcQuestTypeItem(player, x);
			}
			case QuestRewardItem: {
				return new ContainerNpcQuestRewardItem(x);
			} // New
			case ManageRecipes: {
				return new ContainerManageRecipes(player);
			} // Change
			case ManageBanks: {
				return new ContainerManageBanks(player);
			}
			case MerchantAdd: {
				return new ContainerMerchantAdd(player, ServerEventsHandler.Merchant, player.world);
			}
			case PlayerMailOpen: {
				return new ContainerMail(player, x == 1, y == 1);
			}
			case CompanionInv: {
				return new ContainerNPCCompanion(npc, player);
			}
			case CustomGui: {
				return new ContainerCustomGui(new InventoryBasic("", false, x));
			}
			case BuilderSetting:
            case ReplaceSetting:
            case PlacerSetting:
            case SaverSetting:
            case RemoverSetting: {
				return new ContainerBuilderSettings(player, x, y);
			}
            default: {
				return null;
			}
		}
	}

	public EntityPlayer getPlayer() {
		return null;
	}

	public PlayerData getPlayerData(EntityPlayer player) {
		if (player == null) {
			return null;
		}
		return PlayerData.get(player);
	}

	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID > EnumGuiType.values().length) {
			return null;
		}
		EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
		EnumGuiType gui = EnumGuiType.values()[ID];
		return this.getContainer(gui, player, x, y, z, npc);
	}

	public void load() {

	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
	}

	public void openGui(EntityNPCInterface npc, EnumGuiType gui, int x, int y, int z) {
	}

	public void openGui(EntityPlayer player, Object guiscreen) {
	}

	public void openGui(int i, int j, int k, EnumGuiType gui, EntityPlayer player) {
	}

	public void postload() {
	}

	public void preload() {
		CustomNpcs.Channel.register(new PacketHandlerServer());
		CustomNpcs.ChannelPlayer.register(new PacketHandlerPlayer());
	}

	public void reloadItemTextures() {
	}

	public void spawnParticle(EntityLivingBase player, String string, Object... ob) {
	}

	public void spawnParticle(EnumParticleTypes type, double x, double y, double z, double motionX, double motionY,
			double motionZ, float scale) {
	}

	public void updateGUI() {
	}

	public void updateKeys() {
	}

    public void applyRecipe(INpcRecipe recipe, boolean added) {
		if (recipe == null) { return; }
		// Changed in Players
		if (!added || recipe.isKnown()) {
			List<EntityPlayerMP> players = CustomNpcs.Server != null ? CustomNpcs.Server.getPlayerList().getPlayers() : new ArrayList<>();
			for (EntityPlayerMP player : players) {
				RecipeBook book = player.getRecipeBook();
				if (!added) { book.lock((IRecipe) recipe); } else { book.unlock((IRecipe) recipe); }
			}
		}
    }

	public String getTranslateLanguage(EntityPlayer player) {
		if (!(player instanceof EntityPlayerMP)) { return "en"; }
		String lang = ((IEntityPlayerMPMixin) player).npcs$getLanguage();
		if (lang.contains("_")) { lang = lang.substring(0, lang.indexOf("_")); }
		return lang;
	}

}
