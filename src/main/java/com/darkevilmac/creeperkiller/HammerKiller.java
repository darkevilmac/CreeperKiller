package com.darkevilmac.creeperkiller;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Type.VOID;

public class HammerKiller implements MethodInterceptor {

    public static Object createProxy(Object realObject) {
        try {
            MethodInterceptor interceptor = new HammerKiller();
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
            CreeperKiller.LOG.info("Removed HammerCore main menu hook, ads will no longer be displayed.");
            return createdProxy;
        } catch (Exception e) {
            CreeperKiller.LOG.error("Failed to create a proxy for HammerCore ads, they will not be removed.", e);
        }
        return realObject;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (args == null || args.length > 1 || args.length == 0) {
            return methodProxy.invokeSuper(obj, args);
        }

        if (method.getName().contains("guiRender") || method.getName().contains("mouseClick")) {
            Object arg0 = args[0];
            if (arg0 instanceof GuiScreenEvent) {
                GuiScreenEvent drawEvent = (GuiScreenEvent) arg0;

                if (drawEvent.getGui() instanceof GuiMainMenu) {
                    // Don't invoke.
                    return methodProxy.getSignature().getReturnType().getOpcode(VOID);
                }
            }
        }
        return methodProxy.invokeSuper(obj, args);
    }


}