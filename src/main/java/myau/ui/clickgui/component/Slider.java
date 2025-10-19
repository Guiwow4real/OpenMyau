package myau.ui.clickgui.component;

import myau.property.Property;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.PercentProperty;
import myau.ui.clickgui.ClickGuiScreen;
import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import net.minecraft.client.gui.Gui;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Slider extends Component {

    private final Property<?> property;
    private boolean dragging;
    private boolean hovered;

    public Slider(Property<?> property, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.property = property;
        this.dragging = false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, false);
    }

    public void render(int mouseX, int mouseY, float partialTicks, boolean isLast) {
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

        // Draw background for the property name, applying rounding if it's the last component
        RenderUtil.drawRoundedRect(x, scrolledY, width, height, MaterialTheme.CORNER_RADIUS_SMALL, MaterialTheme.getRGB(hovered ? MaterialTheme.SURFACE_CONTAINER_HIGH : MaterialTheme.SURFACE_CONTAINER_LOW), false, false, isLast, isLast);

        double min = 0, max = 0;
        double value = 0;

        if (this.property instanceof IntProperty) {
            min = ((IntProperty) this.property).getMinimum();
            max = ((IntProperty) this.property).getMaximum();
            value = (Integer) this.property.getValue();
        } else if (this.property instanceof FloatProperty) {
            min = ((FloatProperty) this.property).getMinimum();
            max = ((FloatProperty) this.property).getMaximum();
            value = (Float) this.property.getValue();
        } else if (this.property instanceof PercentProperty) {
            min = 0;
            max = 100;
            value = (Integer) this.property.getValue();
        }

        double fillProgress = 0;
        if (max - min != 0) { // Prevent division by zero
            fillProgress = (value - min) / (max - min);
        } else { // If min == max, progress is 1 if value == min (or max), else 0
            fillProgress = (value == min) ? 1 : 0;
        }
        fillProgress = Math.max(0, Math.min(1, fillProgress)); // Clamp between 0 and 1
        int fillWidth = (int) (width * fillProgress);

        // Draw the fill layer
        if (fillWidth > 0) {
            if (isLast && fillWidth >= MaterialTheme.CORNER_RADIUS_SMALL) {
                // Only draw rounded corners if fill width is sufficient and it's the last component
                RenderUtil.drawRoundedRect(x, scrolledY, fillWidth, height, MaterialTheme.CORNER_RADIUS_SMALL, 
                    MaterialTheme.getRGB(MaterialTheme.PRIMARY_CONTAINER_COLOR), 
                    false, false, isLast, isLast);
            } else {
                // Use regular rect for non-last components or when fill is too small for rounded corners
                Gui.drawRect(x, scrolledY, x + fillWidth, scrolledY + height, MaterialTheme.getRGB(MaterialTheme.PRIMARY_CONTAINER_COLOR));
            }
        }

        // Draw property name and value
        String name = property.getName();
        String valStr = "" + round(value, 2);
        if (this.property instanceof PercentProperty) {
            valStr = valStr + "%";
        }

        fr.drawStringWithShadow(name, x + 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR));
        fr.drawStringWithShadow(valStr, x + width - fr.getStringWidth(valStr) - 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR));

        if (this.dragging) {
            updateValue(mouseX, scrollOffset);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.dragging = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Not applicable
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

    private void updateValue(int mouseX, int scrollOffset) {
        double min = 0, max = 0;
        if (this.property instanceof IntProperty) {
            min = ((IntProperty) this.property).getMinimum();
            max = ((IntProperty) this.property).getMaximum();
        } else if (this.property instanceof FloatProperty) {
            min = ((FloatProperty) this.property).getMinimum();
            max = ((FloatProperty) this.property).getMaximum();
        } else if (this.property instanceof PercentProperty) {
            min = 0;
            max = 100;
        }

        double percent = Math.max(0, Math.min(1, (double) (mouseX - this.x) / (double) this.width));
        double value = min + (max - min) * percent;

        if (this.property instanceof IntProperty) {
            this.property.setValue((int) Math.round(value));
        } else if (this.property instanceof FloatProperty) {
            this.property.setValue((float) value);
        } else if (this.property instanceof PercentProperty) {
            this.property.setValue((int) Math.round(value));
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
