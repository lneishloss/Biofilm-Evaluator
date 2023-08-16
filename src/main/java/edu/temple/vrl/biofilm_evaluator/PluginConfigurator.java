package edu.temple.vrl.biofilm_evaluator;

import eu.mihosoft.vrl.io.VJarUtil;
import eu.mihosoft.vrl.lang.visual.CompletionUtil;
import eu.mihosoft.vrl.system.InitPluginAPI;
import eu.mihosoft.vrl.system.PluginAPI;
import eu.mihosoft.vrl.system.VPluginConfigurator;

/**
 *
 */

public class PluginConfigurator extends VPluginConfigurator {

    @Override
    public void register(PluginAPI api) {
        // register plugin with canvas
    }

    @Override
    public void unregister(PluginAPI api) {
        // nothing to unregister
    }

    @Override
    public void init(InitPluginAPI iApi) {
        CompletionUtil.registerClassesFromJar(
                VJarUtil.getClassLocation(PluginConfigurator.class));
    }
}