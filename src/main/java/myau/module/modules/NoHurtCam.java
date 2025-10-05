package myau.module.modules;

import myau.module.Module;
import myau.property.properties.PercentProperty;
import myau.module.Category;

public class NoHurtCam extends Module {
    public final PercentProperty multiplier = new PercentProperty("multiplier", 0);

    public NoHurtCam() {
        super("NoHurtCam","",Category.RENDER,0, false, true);
    }
}
