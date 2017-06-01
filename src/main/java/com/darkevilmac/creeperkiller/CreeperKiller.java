package com.darkevilmac.creeperkiller;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.Map;

@Mod(name = "CreeperKiller", modid = "creeperkiller", clientSideOnly = true, version = "0.0.1", dependencies = "after:creeperhost")
public class CreeperKiller {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        getForgeListenerOwners().entrySet().stream()
                .filter(objectModContainerEntry -> objectModContainerEntry.getValue().getModId().equals("creeperhost")).forEach(
                objectModContainerEntry -> {
                    e.getModLog().info("CreeperKiller Removed {} handler from forge.", objectModContainerEntry.getKey().toString());
                    MinecraftForge.EVENT_BUS.unregister(objectModContainerEntry.getKey());
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
