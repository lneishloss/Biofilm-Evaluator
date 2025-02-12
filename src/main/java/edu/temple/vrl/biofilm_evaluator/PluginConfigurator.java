package edu.temple.vrl.biofilm_evaluator;

import edu.temple.vrl.biofilm_evaluator.components.ExportAllRigidStructuresDataComponent;
import edu.temple.vrl.biofilm_evaluator.components.ExportDensityDataComponent;
import edu.temple.vrl.biofilm_evaluator.components.ImportMeshComponent;
import edu.temple.vrl.biofilm_evaluator.components.ExportAllDataComponent;
import eu.mihosoft.vrl.io.VJarUtil;
import eu.mihosoft.vrl.io.VersionInfo;
import eu.mihosoft.vrl.lang.visual.CompletionUtil;
import eu.mihosoft.vrl.system.*;

/**
 *
 */

public class PluginConfigurator extends VPluginConfigurator {

    public PluginConfigurator(){
        setIdentifier(new PluginIdentifier("BiofilmEvaluator", "0.1"));
        setDescription("BiofilmEvaluator VRL Plugin");
        setCopyrightInfo("BiofilmEvaluator-Plugin", "(c) placeholder", "www...", "placeholder", "placeholder text");
        addDependency(new PluginDependency("VRL", "0.4.4.0.3", VersionInfo.UNDEFINED));
    }

    @Override
    public void register(PluginAPI api) {
        if (api instanceof VPluginAPI) {
            VPluginAPI vapi = (VPluginAPI) api;

            vapi.addComponent(ImportMeshComponent.class);
            vapi.addComponent(ExportDensityDataComponent.class);
            vapi.addComponent(ExportAllRigidStructuresDataComponent.class);
            vapi.addComponent(ExportAllDataComponent.class);
        }
    }

    @Override
    public void unregister(PluginAPI api) {

    }

    @Override
    public void init(InitPluginAPI iApi) {
        CompletionUtil.registerClassesFromJar(
                VJarUtil.getClassLocation(PluginConfigurator.class));
    }
}