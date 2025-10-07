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
    // 默认颜色属性
    public final ColorProperty primaryColor = new ColorProperty("Primary Color", new Color(139, 100, 255).getRGB());
    public final ColorProperty secondaryColor = new ColorProperty("Secondary Color", new Color(30, 30, 30, 180).getRGB());
    public final ColorProperty backgroundColor = new ColorProperty("Background Color", new Color(20, 20, 20).getRGB());
    public final ColorProperty textColor = new ColorProperty("Text Color", new Color(220, 220, 220).getRGB());
    public final ColorProperty clickGuiBackgroundColor = new ColorProperty("ClickGUI Background Color", new Color(25, 25, 25, 180).getRGB());
    
    // Fluent风格配置属性
    public final ColorProperty borderColor = new ColorProperty("Border Color", new Color(40, 40, 40).getRGB());
    public final ColorProperty hoverColor = new ColorProperty("Hover Color", new Color(40, 40, 40, 180).getRGB());
    public final BooleanProperty enableSmoothAnimation = new BooleanProperty("Enable Smooth Animation", true);
    public final PercentProperty animationSpeed = new PercentProperty("Animation Speed", 70, 10, 100, null);
    public final BooleanProperty enableTransparentBackground = new BooleanProperty("Enable Transparent Background", false);
    public final BooleanProperty saveGuiState = new BooleanProperty("Save GUI State", true);
    public final PercentProperty cornerRadius = new PercentProperty("Corner Radius", 20, 0, 50, null);

    public GuiModule() {
        super("GuiSettings", "Customize the ClickGUI appearance with Fluent design.", Category.RENDER, 0, false, false);
    }
}