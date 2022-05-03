package com.r8.autoblastfurnace;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;

/** Allows us to use Chin Break Handler without necessarily having it installed. */
@Singleton
public class ReflectBreakHandler {
  @Inject
  private PluginManager pluginManager;

  private Object instance = null;
  private HashMap<String, Method> chinMethods = null;
  private boolean chinBreakHandlerInstalled = true;

  public void registerPlugin(Plugin p, boolean configure) {
    performReflection("registerPlugin2", p, configure);
  }

  public void registerPlugin(Plugin p) {
    performReflection("registerPlugin1", p);
  }

  public void unregisterPlugin(Plugin p) {
    performReflection("unregisterPlugin1", p);
  }

  public void startPlugin(Plugin p) {
    performReflection("startPlugin1", p);
  }

  public void stopPlugin(Plugin p) {
    performReflection("stopPlugin1", p);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
	public boolean isBreakActive(Plugin p) {
    Object o = performReflection("isBreakActive1", p);
    if (o != null) {
      return (boolean) o;
    }
    return false;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public boolean shouldBreak(Plugin p) {
    Object o = performReflection("shouldBreak1", p);
    if (o != null) {
      return (boolean) o;
    }
    return false;
  }

  public void startBreak(Plugin p) {
    performReflection("startBreak1", p);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public Instant getPlannedBreak(Plugin p) {
    Object o = performReflection("getPlannedBreak1", p);
    if (o != null) {
      return (Instant) o;
    }
    return null;
  }

  private Object performReflection(String methodName, Object... args) {
    if (checkReflection() && chinMethods.containsKey(methodName = methodName.toLowerCase())) {
      try {
        return chinMethods.get(methodName).invoke(instance, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  private boolean checkReflection() {
    if (!chinBreakHandlerInstalled) {
      return false;
    }

    if (chinMethods != null && instance != null) {
      return true;
    }

    chinMethods = new HashMap<>();
    for (Plugin p : pluginManager.getPlugins()) {
      if (p.getClass().getSimpleName().equalsIgnoreCase("chinbreakhandlerplugin")) {
        for (Field f : p.getClass().getDeclaredFields()) {
          if (f.getName().equalsIgnoreCase("chinbreakhandler")) {
            f.setAccessible(true);
            try {
              instance = f.get(p);
              for (Method m : instance.getClass().getDeclaredMethods()) {
                m.setAccessible(true);
                chinMethods.put(m.getName().toLowerCase() + m.getParameterCount(), m);
              }
              return true;
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            }
            return false;
          }
        }
      }
    }
    chinBreakHandlerInstalled = false;
    return false;
  }

}
