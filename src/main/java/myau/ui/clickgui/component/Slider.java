package myau.ui.clickgui.component;

import myau.property.Property;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.PercentProperty;
import myau.ui.clickgui.ClickGuiScreen;
import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import net.minecraft.client.gui.Gui;

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
    public void render(int mouseX, int mouseY, float partialTicks, float animationProgress, boolean isLast, int scrollOffset) {
        // Animation
        float easedProgress = 1.0f - (float) Math.pow(1.0f - animationProgress, 4);
        if (easedProgress <= 0) return;

        int scaledHeight = (int) (height * easedProgress);
        
        int scrolledY = y - scrollOffset;
        int scaledY = scrolledY + (height - scaledHeight) / 2;

        hovered = isMouseOver(mouseX, mouseY);

        RenderUtil.drawRoundedRect(x, scaledY, width, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress, MaterialTheme.getRGB(hovered ? MaterialTheme.SURFACE_CONTAINER_HIGH : MaterialTheme.SURFACE_CONTAINER_LOW), false, false, isLast, isLast);

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
        if (max - min != 0) {
            fillProgress = (value - min) / (max - min);
        } else {
            fillProgress = (value == min) ? 1 : 0;
        }
        fillProgress = Math.max(0, Math.min(1, fillProgress));
        int fillWidth = (int) (width * fillProgress);

        if (fillWidth > 0) {
            if (isLast && fillWidth >= MaterialTheme.CORNER_RADIUS_SMALL) {
                RenderUtil.drawRoundedRect(x, scaledY, fillWidth, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress, 
                    MaterialTheme.getRGB(MaterialTheme.PRIMARY_CONTAINER_COLOR), 
                    false, false, isLast, isLast);
            } else {
                Gui.drawRect(x, scaledY, x + fillWidth, scaledY + scaledHeight, MaterialTheme.getRGB(MaterialTheme.PRIMARY_CONTAINER_COLOR));
            }
        }

        if (easedProgress > 0.9f) {
            int alpha = (int) (((easedProgress - 0.9f) / 0.1f) * 255);
            alpha = Math.max(0, Math.min(255, alpha));
            int textColor = (alpha << 24) | (MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR) & 0x00FFFFFF);

            String name = property.getName();
            String valStr = "" + round(value, 2);
            if (this.property instanceof PercentProperty) {
                valStr = valStr + "%";
            }

            RenderUtil.getFontRenderer().drawStringWithShadow(name, x + 5, scrolledY + (height - RenderUtil.getFontRenderer().getFontHeight()) / 2, textColor);
            RenderUtil.getFontRenderer().drawStringWithShadow(valStr, x + width - RenderUtil.getFontRenderer().getStringWidth(valStr) - 5, scrolledY + (height - RenderUtil.getFontRenderer().getFontHeight()) / 2, textColor);
        }

        if (this.dragging) {
            updateValue(mouseX);
        }
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height;
    }

    private void updateValue(int mouseX) {
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
            this.property.setValue((int) round(value, 0));
        } else if (this.property instanceof FloatProperty) {
            this.property.setValue((float) round(value, 2));
        } else if (this.property instanceof PercentProperty) {
            this.property.setValue((int) round(value, 0));
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Not needed for this component
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.dragging = false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            this.dragging = true;
            return true;
        }
        return false;
    }
}