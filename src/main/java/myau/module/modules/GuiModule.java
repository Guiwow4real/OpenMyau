package myau.module.modules;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.BooleanProperty;
import myau.property.properties.PercentProperty;
import myau.property.properties.ColorProperty;
import myau.property.properties.IntProperty;

import java.util.Arrays;
import java.util.List;
import java.awt.Color;

public class GuiModule extends Module {
    public final BooleanProperty enableSmoothAnimation = new BooleanProperty("EnableSmoothAnimation", true);
    public final PercentProperty animationSpeed = new PercentProperty("AnimationSpeed", 70, 10, 100, null);
    public final BooleanProperty enableTransparentBackground = new BooleanProperty("EnableTransparentBackground", false);
    public final BooleanProperty saveGuiState = new BooleanProperty("SaveGuiState", true);
    public final PercentProperty cornerRadius = new PercentProperty("CornerRadius", 20, 0, 50, null);
    public final IntProperty windowX = new IntProperty("WindowX", 200, 0, 1920, null);
    public final IntProperty windowY = new IntProperty("WindowY", 80, 0, 1080, null);
    public final IntProperty windowWidth = new IntProperty("WindowWidth", 650, 400, 1920, null);
    public final IntProperty windowHeight = new IntProperty("WindowHeight", 480, 300, 1080, null);

    public GuiModule() {
        super("GuiSettings", "Customize the ClickGUI appearance with Fluent design.", Category.RENDER, 0, false, false);
    }
}