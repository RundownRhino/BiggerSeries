package net.roguelogix.phosphophyllite;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.roguelogix.phosphophyllite.multiblock.generic.MultiblockController;
import net.roguelogix.phosphophyllite.multiblock.generic.MultiblockTile;
import net.roguelogix.phosphophyllite.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

@SuppressWarnings("unused")
@Mod(Phosphophyllite.modid)
public class Phosphophyllite {
    public static final String modid = "phosphophyllite";
    
    public static final Logger LOGGER = LogManager.getLogger("Phosphophyllite/Main");
    public static long lastTime = 0;
    // used to ensure i dont tick things twice
    private static long tick = 0;
    
    public Phosphophyllite() {
        Registry.onModLoad();
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public static long tickNumber() {
        return tick;
    }
    
    
    private static final HashMap<ServerWorld, ArrayList<MultiblockController<?, ?>>> controllersToTick = new HashMap<>();
    private static final HashMap<ServerWorld, ArrayList<MultiblockTile<?, ?>>> tilesToAttach = new HashMap<>();
    private static final ArrayList<MultiblockController<?, ?>> newControllers = new ArrayList<>();
    private static final ArrayList<MultiblockController<?, ?>> oldControllers = new ArrayList<>();
    private static final ArrayList<MultiblockTile<?, ?>> newTiles = new ArrayList<>();
    
    public static void addController(MultiblockController<?, ?> controller) {
        newControllers.add(controller);
    }
    
    public static void removeController(MultiblockController<?, ?> controller) {
        oldControllers.add(controller);
    }
    
    public static void attachTile(MultiblockTile<?, ?> tile) {
        newTiles.add(tile);
    }
    
    @SubscribeEvent
    void onWorldUnload(final WorldEvent.Unload worldUnloadEvent) {
        if (!worldUnloadEvent.getWorld().isRemote()) {
            //noinspection SuspiciousMethodCalls
            ArrayList<MultiblockController<?, ?>> controllersToTick = Phosphophyllite.controllersToTick.remove(worldUnloadEvent.getWorld());
            if (controllersToTick != null) {
                for (MultiblockController<?, ?> multiblockController : controllersToTick) {
                    multiblockController.suicide();
                }
            }
            // apparently, stragglers can exist
            //noinspection SuspiciousMethodCalls
            tilesToAttach.remove(worldUnloadEvent.getWorld());
        }
        newControllers.clear();
        oldControllers.clear();
        newTiles.clear();
    }
    
    @SubscribeEvent
    public void advanceTick(TickEvent.ServerTickEvent e) {
        if (!e.side.isServer()) {
            return;
        }
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        tick++;
        
        for (MultiblockController<?, ?> newController : newControllers) {
            controllersToTick.computeIfAbsent((ServerWorld) newController.getWorld(), k -> new ArrayList<>()).add(newController);
        }
        newControllers.clear();
        for (MultiblockController<?, ?> oldController : oldControllers) {
            //noinspection SuspiciousMethodCalls
            ArrayList<MultiblockController<?, ?>> controllers = controllersToTick.get(oldController.getWorld());
            controllers.remove(oldController);
        }
        oldControllers.clear();
        for (MultiblockTile<?, ?> newTile : newTiles) {
            tilesToAttach.computeIfAbsent((ServerWorld) newTile.getWorld(), k -> new ArrayList<>()).add(newTile);
        }
        newTiles.clear();
    }
    
    @SubscribeEvent
    public void tickWorld(TickEvent.WorldTickEvent e) {
        if (!(e.world instanceof ServerWorld)) {
            return;
        }
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        
        ArrayList<MultiblockController<?, ?>> controllersToTick = Phosphophyllite.controllersToTick.get(e.world);
        if (controllersToTick != null) {
            for (MultiblockController<?, ?> controller : controllersToTick) {
                if (controller != null) {
                    controller.update();
                }
            }
        }
        
        ArrayList<MultiblockTile<?, ?>> tilesToAttach = Phosphophyllite.tilesToAttach.get(e.world);
        if (tilesToAttach != null) {
            tilesToAttach.sort(Comparator.comparing(TileEntity::getPos));
            for (MultiblockTile<?, ?> toAttach : tilesToAttach) {
                if (toAttach != null) {
                    toAttach.attachToNeighbors();
                }
            }
            tilesToAttach.clear();
        }
    }
}