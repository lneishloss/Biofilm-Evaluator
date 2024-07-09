package edu.temple.vrl.biofilm_evaluator.components;


import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.MethodInfo;
import eu.mihosoft.vrl.annotation.OutputInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import edu.temple.vrl.biofilm_evaluator.BiofilmEvaluator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

@ComponentInfo(name="ImportMeshComponent", category="Custom")
public class ImportMeshComponent implements Serializable {
    private static final long serialVersionUID=1L;

    @MethodInfo(name = "import", valueName = "biofilm")
    public BiofilmEvaluator import_mesh(
            @ParamInfo(name = "OBJ File", style = "load-dialog", options = "endings=[\"obj\"]; description=\"OBJ File (.obj)\"") File obj,
            @ParamInfo(name = "TXT File", style = "load-dialog", options = "endings=[\"txt\"]; description=\"TXT File (.txt)\"") File txt,
            @ParamInfo(name = "Voxel width (x)", options = "value=\",\"") double width,
            @ParamInfo(name = "Voxel length (y)", options = "value=\",\"") double length,
            @ParamInfo(name = "Voxel height (z)", options = "value=\",\"") double height,
            @ParamInfo(name = "Image width (x)", options = "value=\",\"") int x,
            @ParamInfo(name = "Image length (y)", options = "value=\",\"") int y,
            @ParamInfo(name = "Image height (z)", options = "value=\",\"") int z) throws IOException {
        BiofilmEvaluator biofilm = new BiofilmEvaluator(width, length, height, x, y, z);
        biofilm.importMeshData(obj, txt);
        return biofilm;
    }
}
