package com.darkevilmac.creeperkiller;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class MineTogetherKiller implements MethodInterceptor {
    public static Object createProxy(Object realObject) {
        try {
            MethodInterceptor interceptor = new MineTogetherKiller();
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(realObject.getClass());
            enhancer.setCallbackType(interceptor.getClass());
            Class classForProxy = enhancer.createClass();
            Enhancer.registerCallbacks(classForProxy, new Callback[]{interceptor});
            Object createdProxy = classForProxy.newInstance();

            for (Field realField : FieldUtils.getAllFieldsList(realObject.getClass())) {
                if (Modifier.isStatic(realField.getModifiers()))
                    continue;
                realField.setAccessible(true);

                realField.set(createdProxy, realField.get(realObject));
            }
            CreeperKiller.LOG.info("Removed Minetogether main menu hook, ads will no longer be displayed.");
            return createdProxy;
        } catch (Exception e) {
            CreeperKiller.LOG.error("Failed to create a proxy for Minetogether ads, they will not be removed.", e);
        }
        return realObject;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (args == null || args.length > 1 || args.length == 0) {
            return methodProxy.invokeSuper(obj, args);
        }

        if (method.getName().contains("nitGui")) {
            Object arg0 = args[0];
            if (arg0 instanceof GuiScreenEvent.InitGuiEvent.Post) {
                GuiScreenEvent.InitGuiEvent.Post event = (GuiScreenEvent.InitGuiEvent.Post) arg0;
                Object result = methodProxy.invokeSuper(obj, args);
                List<GuiButton> undesiredButtons = Lists.newArrayList();

                for (GuiButton guiButton : event.getButtonList()) {
                    if (guiButton.getClass().getSimpleName().contains("ButtonCreeper")) {
                        undesiredButtons.add(guiButton);
                    }
                }

                if (event.getGui() instanceof GuiMultiplayer) {
                    GuiMultiplayer gui = (GuiMultiplayer) event.getGui();
                    Field serverListSelectorField = null;
                    Field serverListInternetField = null;
                    if (serverListSelectorField == null) {
                        serverListSelectorField = ReflectionHelper.findField(GuiMultiplayer.class, "field_146803_h", "serverListSelector");
                        serverListSelectorField.setAccessible(true);
                    }

                    if (serverListInternetField == null) {
                        serverListInternetField = ReflectionHelper.findField(ServerSelectionList.class, "field_148198_l", "serverListInternet");
                        serverListInternetField.setAccessible(true);
                    }

                    ServerSelectionList serverListSelector = (ServerSelectionList) serverListSelectorField.get(gui);
                    List<ServerListEntryNormal> serverListInternet = (List) serverListInternetField.get(serverListSelector);
                    serverListInternet.removeIf((o) -> o.getClass().getSimpleName().contains("Creeper"));
                    ServerSelectionList vanillaList = new ServerSelectionList(gui, Minecraft.getMinecraft(), gui.width, gui.height, 32, gui.height - 64, 36);
                    serverListInternetField.set(vanillaList, serverListInternet);
                    serverListSelectorField.set(gui, vanillaList);
                }
                event.getButtonList().removeAll(undesiredButtons);
                return result;
            }
        }
        return methodProxy.invokeSuper(obj, args);
    }
}
