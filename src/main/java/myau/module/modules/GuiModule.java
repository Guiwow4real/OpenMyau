package myau.module.modules;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.BooleanProperty;
import myau.property.properties.PercentProperty;
import myau.property.properties.ColorProperty;

import java.util.Arrays;
import java.util.List;
import java.awt.Color;

public class GuiModule extends Module {
    public final BooleanProperty enableSmoothAnimation = new BooleanProperty("Enable Smooth Animation", true);
    public final PercentProperty animationSpeed = new PercentProperty("Animation Speed", 70, 10, 100, null);
    public final BooleanProperty enableTransparentBackground = new BooleanProperty("Enable Transparent Background", false);
    public final BooleanProperty saveGuiState = new BooleanProperty("Save GUI State", true);
    public final PercentProperty cornerRadius = new PercentProperty("Corner Radius", 20, 0, 50, null);

    public GuiModule() {
        super("GuiSettings", "Customize the ClickGUI appearance with Fluent design.", Category.RENDER, 0, false, false);
    }
}