package noppes.npcs.mixin.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import noppes.npcs.api.mixin.client.network.INetHandlerPlayClientMixin;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

@Mixin(value = NetHandlerPlayClient.class, priority = 4)
public class NetHandlerPlayClientMixin implements INetHandlerPlayClientMixin {

    @Shadow
    private Minecraft gameController;

    @Shadow
    private boolean doneLoadingTerrain;

    @Final
    @Shadow
    private NetworkManager netManager;

    @Final
    @Shadow
    private Map<UUID, NetworkPlayerInfo> playerInfoMap;

    @Override
    public Map<UUID, NetworkPlayerInfo> npcs$getplayerInfoMap() { return playerInfoMap; }

    /**
     * @author BetaZavr
     * @reason NPCs have hard hitboxes
     */
    @Overwrite
    public void handlePlayerPosLook(SPacketPlayerPosLook packetIn) {
        NetHandlerPlayClient parent = (NetHandlerPlayClient) (Object) this;
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, parent, gameController);
        EntityPlayer player = gameController.player;

        double d0 = packetIn.getX();
        double d1 = packetIn.getY();
        double d2 = packetIn.getZ();

        boolean onNPC = false;
        if (doneLoadingTerrain) {
            for (Entity entity : player.world.loadedEntityList) {
                if (entity instanceof EntityNPCInterface) {
                    EntityNPCInterface npc = (EntityNPCInterface) entity;
                    if (npc.display.getHitboxState() == 2 && !npc.isKilled() && npc.hitboxRiding.containsKey(player)) {
                        onNPC = true;
                        break;
                    }
                }
            }
        }
        if (!onNPC) {
            float yaw = packetIn.getYaw();
            float pitch = packetIn.getPitch();
            if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X))  { d0 += player.posX; }
            else { player.motionX = 0.0D; }
            if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y)) { d1 += player.posY; }
            else { player.motionY = 0.0D; }
            if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z)) { d2 += player.posZ; }
            else { player.motionZ = 0.0D; }
            if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) { yaw += player.rotationYaw; }
            if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) { pitch += player.rotationPitch; }
            player.setPositionAndRotation(d0, d1, d2, yaw, pitch);
        }
        netManager.sendPacket(new CPacketConfirmTeleport(packetIn.getTeleportId()));
        netManager.sendPacket(new CPacketPlayer.PositionRotation(player.posX, player.getEntityBoundingBox().minY, player.posZ, player.rotationYaw, player.rotationPitch, false));
        if (!doneLoadingTerrain) {
            gameController.player.prevPosX = gameController.player.posX;
            gameController.player.prevPosY = gameController.player.posY;
            gameController.player.prevPosZ = gameController.player.posZ;
            doneLoadingTerrain = true;
            gameController.displayGuiScreen(null);
        }
    }

}
