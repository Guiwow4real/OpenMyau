package myau.ui.clickgui.component.impl;

import myau.property.Property;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.PercentProperty;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;

import java.awt.Color;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Slider extends Component {

    // Use the raw Property type to avoid generic casting issues.
    @SuppressWarnings("rawtypes")
    private final Property property;
    private boolean dragging;
    
    // 默认颜色值
    private static final int SECONDARY_COLOR = new Color(30, 30, 30, 180).getRGB();
    private static final int PRIMARY_COLOR = new Color(30, 150, 250).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();

    public Slider(@SuppressWarnings("rawtypes") Property property, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.property = property;
        this.dragging = false;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        // 使用默认背景色
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, SECONDARY_COLOR);

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
            // PercentProperty默认是0-100的范围
            min = 0;
            max = 100;
            value = (Integer) this.property.getValue();
        }

        double renderWidth = (this.width) * (value - min) / (max - min);
        // 使用默认主色调
        Gui.drawRect(this.x, this.y, this.x + (int) renderWidth, this.y + this.height, PRIMARY_COLOR);

        String name = this.property.getName();
        String valStr = "" + round(value, 2);
        // 为PercentProperty添加%符号
        if (this.property instanceof PercentProperty) {
            valStr = valStr + "%";
        }
        // 使用默认文字颜色
        fr.drawStringWithShadow(name, this.x + 2, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, TEXT_COLOR);
        fr.drawStringWithShadow(valStr, this.x + this.width - fr.getStringWidth(valStr) - 2, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, TEXT_COLOR);

        if (this.dragging) {
            updateValue(mouseX);
        }
    }

    @Override
    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            this.dragging = true;
            return this; // This component is now dragging
        }
        return null; // No drag initiated by this component
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        this.dragging = false;
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
            // PercentProperty默认是0-100的范围
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
