package edu.temple.vrl.biofilm_evaluator.components;

import edu.temple.vrl.biofilm_evaluator.BiofilmEvaluator;
import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.MethodInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@ComponentInfo(name="ExportAllRigidStructuresComponent", category="Custom")
public class ExportAllRigidStructuresComponent implements Serializable {
    private static final long serialVersionUID=1L;

    @MethodInfo(valueStyle = "multi-out", interactive = true)
    public void export_data(
            @ParamInfo(name="Mesh", options="value=false") BiofilmEvaluator biofilm,
            @ParamInfo(name="Biofilm Image",style="load-dialog",options="endings=[\"tif\",\"tiff\"]; description=\"Image Files (.tif, .tiff)\"") File image,
            @ParamInfo(name="Exported CSV Data", style="save-dialog", options="") File f
    ) throws IOException {

        biofilm.generateSurfaces(0);
        biofilm.generateQuadtree(100);
        biofilm.computeSurfaceMeasurements();
        biofilm.determineInteriorPoints();
        biofilm.importImage(image);
        biofilm.computeSurfaceDensities();

        if(!f.isFile()){
            String finalDataToFile = biofilm.printRigidStructuresData();
            Files.write(f.toPath(), finalDataToFile.getBytes());
        }
        else{
            String finalDataToFile = biofilm.printRigidStructuresData();
            Files.write(f.toPath(), finalDataToFile.getBytes(), StandardOpenOption.APPEND);
        }

    }
}
