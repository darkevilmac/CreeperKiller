package com.darkevilmac.creeperkiller;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(name = "CreeperKiller", modid = "creeperkiller", clientSideOnly = true, version = "0.0.2", dependencies = "after:creeperhost;after:hammercore")
public class CreeperKiller {

    public static Logger LOG;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        LOG = e.getModLog();

        Map<Object, ModContainer> forgeListenerOwners = getForgeListenerOwners();
        // Remove CreeperHost ads.
        forgeListenerOwners.entrySet().stream()
                .filter(objectModContainerEntry -> objectModContainerEntry.getValue().getModId().equals("creeperhost"))
                .forEach(objectModContainerEntry -> {
                            LOG.info("CreeperKiller Removed {} handler from forge, CreeperHost ads will no longer be displayed", objectModContainerEntry.getKey().toString());
                            MinecraftForge.EVENT_BUS.unregister(objectModContainerEntry.getKey());
                        }
                );

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        Map<Object, ModContainer> forgeListenerOwners = getForgeListenerOwners();

        // Remove HammerCore ads.
        forgeListenerOwners.entrySet().stream()
                .filter(objectModContainerEntry -> objectModContainerEntry.getKey().toString().contains("hammercore.client.RenderGui"))
                .forEach(objectModContainerEntry -> {
                    // Remove the normal RenderGUI from the forge event bus, then generate a proxy.
                    MinecraftForge.EVENT_BUS.unregister(objectModContainerEntry.getKey());
                    MinecraftForge.EVENT_BUS.register(HammerKiller.createProxy(objectModContainerEntry.getKey()));
                });
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        Map<Object, ModContainer> forgeListenerOwners = getForgeListenerOwners();
        forgeListenerOwners.entrySet().stream()
                .filter(objectModContainerEntry -> objectModContainerEntry.getValue().getModId().equals("minetogether"))
                .forEach(objectModContainerEntry -> {
                            // Remove the normal RenderGUI from the forge event bus, then generate a proxy.
                            MinecraftForge.EVENT_BUS.unregister(objectModContainerEntry.getKey());
                            MinecraftForge.EVENT_BUS.register(MineTogetherKiller.createProxy(objectModContainerEntry.getKey()));
                        }
                );

    }

    public static Map<Object, ModContainer> getForgeListenerOwners() {
        try {
            return ((Map<Object, ModContainer>) FieldUtils.readField(MinecraftForge.EVENT_BUS, "listenerOwners", true));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
