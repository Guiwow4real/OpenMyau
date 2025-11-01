package myau.ui.clickgui.component;

import myau.Myau;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.PercentProperty;
import myau.property.properties.ModeProperty;
import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ModuleEntry extends Component {

    private final Module module;
    private boolean expanded;
    private List<Component> propertiesComponents;
    private boolean hovered;

    public ModuleEntry(Module module, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.module = module;
        this.expanded = false;
        this.propertiesComponents = new ArrayList<>();
        initializePropertiesComponents();
    }

    private void initializePropertiesComponents() {
        if (Myau.propertyManager != null) {
            List<Property<?>> properties = Myau.propertyManager.properties.get(module.getClass());
            if (properties != null) {
                int currentY = y + height; // Start Y for properties, relative to module entry
                for (Property<?> property : properties) {
                    Component comp = null;
                    int compHeight = 20; // Default height for most components
                    if (property instanceof BooleanProperty) {
                        comp = new Switch((BooleanProperty) property, x, currentY, width, compHeight);
                    } else if (property instanceof IntProperty || property instanceof FloatProperty || property instanceof PercentProperty) {
                        comp = new Slider(property, x, currentY, width, compHeight);
                    } else if (property instanceof ModeProperty) {
                        comp = new Dropdown((ModeProperty) property, x, currentY, width, compHeight);
                    } else if (property instanceof myau.property.properties.ColorProperty) { // Add ColorProperty handling
                        comp = new ColorPicker((myau.property.properties.ColorProperty) property, x, currentY, width, 50); // ColorPicker might need more height
                        compHeight = 50;
                    }
                    
                    if (comp != null) {
                        propertiesComponents.add(comp);
                        currentY += compHeight;
                    }
                }
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks, float animationProgress, boolean isLast, int scrollOffset) {
        int scrolledY = y - scrollOffset;
        hovered = isMouseOver(mouseX, mouseY);

        float easedProgress = 1.0f - (float) Math.pow(1.0f - animationProgress, 4);
        int scaledHeight = (int) (height * easedProgress);
        int scaledY = scrolledY + (height - scaledHeight) / 2;

        if (easedProgress <= 0) return;

        boolean shouldRoundBottom = isLast && !expanded;

        Color bgColor = hovered ? MaterialTheme.SURFACE_CONTAINER_HIGH : MaterialTheme.SURFACE_CONTAINER_LOW;
        RenderUtil.drawRoundedRect(x, scaledY, width, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress,
                MaterialTheme.getRGB(bgColor), false, false, shouldRoundBottom, shouldRoundBottom);

        if (easedProgress > 0.9f) {
            int alpha = (int) (((easedProgress - 0.9f) / 0.1f) * 255);
            alpha = Math.max(0, Math.min(255, alpha));
            int mainColor = (alpha << 24) | (MaterialTheme.getRGB(module.isEnabled() ? MaterialTheme.PRIMARY_COLOR : MaterialTheme.TEXT_COLOR_SECONDARY) & 0x00FFFFFF);
            int secondaryColor = (alpha << 24) | (MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR_SECONDARY) & 0x00FFFFFF);

            RenderUtil.getFontRenderer().drawStringWithShadow(module.getName(), x + 5, scrolledY + (height - RenderUtil.getFontRenderer().getFontHeight()) / 2, mainColor);

            if (!propertiesComponents.isEmpty()) {
                String arrow = expanded ? "\u25B2" : "\u25BC";
                RenderUtil.getFontRenderer().drawStringWithShadow(arrow, x + width - RenderUtil.getFontRenderer().getStringWidth(arrow) - 5, scrolledY + (height - RenderUtil.getFontRenderer().getFontHeight()) / 2, secondaryColor);
            }
        }

        if (expanded && easedProgress >= 1.0f) {
            int currentY = y + height;
            for (int i = 0; i < propertiesComponents.size(); i++) {
                Component comp = propertiesComponents.get(i);
                comp.setX(x);
                comp.setY(currentY);
                comp.setWidth(width);

                boolean isLastComponent = isLast && (i == propertiesComponents.size() - 1);
                comp.render(mouseX, mouseY, partialTicks, animationProgress, isLastComponent, scrollOffset);
                currentY += comp.getHeight();
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseButton == 0) {
                module.toggle();
                return true;
            } else if (mouseButton == 1) {
                if (!propertiesComponents.isEmpty()) {
                    expanded = !expanded;
                }
                return true;
            }
        }

        if (expanded) {
            for (Component comp : propertiesComponents) {
                if (comp.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (expanded) {
            for (Component comp : propertiesComponents) {
                comp.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            for (Component comp : propertiesComponents) {
                comp.keyTyped(typedChar, keyCode);
            }
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public Module getModule() {
        return module;
    }

    public int getTotalHeight() {
        int total = height;
        if (expanded) {
            for (Component comp : propertiesComponents) {
                total += comp.getHeight();
            }
        }
        return total;
    }
    
    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height;
    }
}