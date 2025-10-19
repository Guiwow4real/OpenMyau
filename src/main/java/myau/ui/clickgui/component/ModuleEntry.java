package myau.ui.clickgui.component;

import myau.Myau;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.PercentProperty;
import myau.property.properties.ModeProperty;
import myau.ui.clickgui.ClickGuiScreen;
import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import myau.ui.clickgui.component.Switch;
import myau.ui.clickgui.component.Slider;
import myau.ui.clickgui.component.Dropdown;
import net.minecraft.client.gui.Gui;

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
                    // Add more property types as needed
                    
                    if (comp != null) {
                        propertiesComponents.add(comp);
                        currentY += compHeight; // Increment currentY based on component height
                    }
                }
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, false);
    }

    public void render(int mouseX, int mouseY, float partialTicks, boolean isLastEntry) {
        // Get scroll offset from ClickGuiScreen
        int scrollOffset = 0;
        try {
            scrollOffset = ClickGuiScreen.getInstance().getScrollY();
        } catch (Exception e) {
            // Ignore if we can't get scroll offset
        }
        
        // Apply scroll offset
        int scrolledY = y - scrollOffset;
        hovered = isMouseOver(mouseX, mouseY + scrollOffset);

        // Determine if this entry itself should have rounded bottom corners
        boolean shouldRoundBottom = isLastEntry && !expanded;

        // Draw background
        Color bgColor = hovered ? MaterialTheme.SURFACE_CONTAINER_HIGH : MaterialTheme.SURFACE_CONTAINER_LOW;
        RenderUtil.drawRoundedRect(x, scrolledY, width, height, MaterialTheme.CORNER_RADIUS_SMALL, MaterialTheme.getRGB(bgColor),
                false, false, shouldRoundBottom, shouldRoundBottom); // Apply rounding if it's the last and not expanded

        // Module Name
        fr.drawStringWithShadow(module.getName(), x + 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(module.isEnabled() ? MaterialTheme.PRIMARY_COLOR : MaterialTheme.TEXT_COLOR_SECONDARY));

        // Expansion arrow (only if module has properties)
        if (!propertiesComponents.isEmpty()) {
            String arrow = expanded ? "\u25B2" : "\u25BC";
            fr.drawStringWithShadow(arrow, x + width - fr.getStringWidth(arrow) - 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR_SECONDARY));
        }

        if (expanded) {
            int currentY = scrolledY + height;
            for (int i = 0; i < propertiesComponents.size(); i++) {
                Component comp = propertiesComponents.get(i);
                comp.setX(x);
                comp.setY(currentY);
                comp.setWidth(width);

                // Pass down the 'isLast' flag if this is the last property of the last module entry
                boolean isLastComponent = isLastEntry && (i == propertiesComponents.size() - 1);
                
                // A bit of a hacky way to call render with the new parameter.
                // Assumes all property components will have this new render method.
                if (comp instanceof Switch) {
                    ((Switch) comp).render(mouseX, mouseY + scrollOffset, partialTicks, isLastComponent);
                } else if (comp instanceof Slider) {
                    ((Slider) comp).render(mouseX, mouseY + scrollOffset, partialTicks, isLastComponent);
                } else if (comp instanceof Dropdown) {
                    ((Dropdown) comp).render(mouseX, mouseY + scrollOffset, partialTicks, isLastComponent);
                } else if (comp instanceof ColorPicker) {
                    ((ColorPicker) comp).render(mouseX, mouseY + scrollOffset, partialTicks, isLastComponent);
                } else {
                    comp.render(mouseX, mouseY + scrollOffset, partialTicks);
                }

                currentY += comp.getHeight();
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Check if click is on the module entry header (where module name and arrow are)
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (mouseButton == 0) { // Left click: toggle module enable/disable state
                module.toggle();
                return true;
            } else if (mouseButton == 1) { // Right click: toggle expansion if properties exist
                if (!propertiesComponents.isEmpty()) {
                    expanded = !expanded;
                }
                return true;
            }
        }

        // If expanded, pass click to properties components
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
        // Get scroll offset from ClickGuiScreen
        int scrollOffset = 0;
        try {
            scrollOffset = ClickGuiScreen.getInstance().getScrollY();
        } catch (Exception e) {
            // Ignore if we can't get scroll offset
        }
        
        int scrolledY = this.y - scrollOffset;
        return mouseX >= x && mouseX <= x + width && mouseY >= scrolledY && mouseY <= scrolledY + height;
    }
}
