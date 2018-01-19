package gregtech.api.net;

import gregtech.api.capability.internal.ICustomDataTile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class CustomDataTileHandler {

    static final Map<BlockPos, PacketBuffer> pendingInitialSyncData = new ConcurrentHashMap<>();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase != Phase.END) return;
        World clientWorld = Minecraft.getMinecraft().world;
        if(clientWorld == null) return;
        Iterator<BlockPos> keyIterator = pendingInitialSyncData.keySet().iterator();
        while(keyIterator.hasNext()) {
            BlockPos blockPos = keyIterator.next();
            TileEntity tileEntity = clientWorld.getTileEntity(blockPos);
            if(tileEntity instanceof ICustomDataTile) {
                ((ICustomDataTile) tileEntity).handleDataPacket(pendingInitialSyncData.get(blockPos));
                keyIterator.remove();
            }
        }
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent.Watch event) {
        ChunkPos chunkPos = event.getChunk();
        EntityPlayerMP player = event.getPlayer();
        Chunk chunk = player.world.getChunkFromChunkCoords(chunkPos.x, chunkPos.z);
        for (TileEntity tileEntity : chunk.getTileEntityMap().values()) {
            if(tileEntity instanceof ICustomDataTile) {
                ICustomDataTile customDataTile = (ICustomDataTile) tileEntity;
                customDataTile.onBeingWatched(player);
            }
        }
    }

}