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
    public static ColorProperty primaryColor = new ColorProperty("Primary Color", new Color(139, 100, 255).getRGB());
    public static ColorProperty secondaryColor = new ColorProperty("Secondary Color", new Color(30, 30, 30, 180).getRGB());
    public static ColorProperty backgroundColor = new ColorProperty("Background Color", new Color(20, 20, 20).getRGB());
    public static ColorProperty textColor = new ColorProperty("Text Color", new Color(220, 220, 220).getRGB());
    public static ColorProperty clickGuiBackgroundColor = new ColorProperty("ClickGUI Background Color", new Color(25, 25, 25, 180).getRGB());
    
    // Fluent风格配置属性
    public static ColorProperty borderColor = new ColorProperty("Border Color", new Color(40, 40, 40).getRGB());
    public static ColorProperty hoverColor = new ColorProperty("Hover Color", new Color(40, 40, 40, 180).getRGB());
    public static BooleanProperty enableSmoothAnimation = new BooleanProperty("Enable Smooth Animation", true);
    public static PercentProperty animationSpeed = new PercentProperty("Animation Speed", 70, 10, 100, null);
    public static BooleanProperty enableTransparentBackground = new BooleanProperty("Enable Transparent Background", false);
    public static BooleanProperty savePosition = new BooleanProperty("Save Position", true);
    public static PercentProperty cornerRadius = new PercentProperty("Corner Radius", 20, 0, 50, null);

    public GuiModule() {
        super("GuiSettings", "Customize the ClickGUI appearance with Fluent design.", Category.RENDER, 0, false, false);
    }

    public List<Property<?>> getProperties() {
        return Arrays.asList(
                primaryColor,
                secondaryColor,
                backgroundColor,
                textColor,
                clickGuiBackgroundColor,
                // Fluent风格配置
                borderColor,
                hoverColor,
                enableSmoothAnimation,
                animationSpeed,
                enableTransparentBackground,
                savePosition,
                cornerRadius
        );
    }
}