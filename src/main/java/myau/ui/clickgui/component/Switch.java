package myau.ui.clickgui.component;

import myau.property.properties.BooleanProperty;
import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import net.minecraft.client.gui.Gui;

import java.awt.Color;

public class Switch extends Component {

    private final BooleanProperty booleanProperty;
    private boolean hovered;

    public Switch(BooleanProperty booleanProperty, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.booleanProperty = booleanProperty;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, false);
    }

    public void render(int mouseX, int mouseY, float partialTicks, boolean isLast) {
        hovered = isMouseOver(mouseX, mouseY);

        // Draw background for the property name, applying rounding if it's the last component
        RenderUtil.drawRoundedRect(x, y, width, height, MaterialTheme.CORNER_RADIUS_SMALL, MaterialTheme.getRGB(hovered ? MaterialTheme.SURFACE_CONTAINER_HIGH : MaterialTheme.SURFACE_CONTAINER_LOW), false, false, isLast, isLast);

        // Draw property name
        fr.drawStringWithShadow(booleanProperty.getName(), x + 5, y + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR));

        // Draw switch itself (Material Design 3 style)
        int switchWidth = 28;
        int switchHeight = 16;
        int switchX = x + width - switchWidth - 5;
        int switchY = y + (height - switchHeight) / 2;

        // Track color
        Color trackColor = booleanProperty.getValue() ? MaterialTheme.PRIMARY_COLOR.darker().darker() : MaterialTheme.SURFACE_VARIANT_COLOR;
        RenderUtil.drawRoundedRect(switchX, switchY + switchHeight / 4, switchWidth, switchHeight / 2, switchHeight / 4, MaterialTheme.getRGB(trackColor), true, true, true, true);

        // Thumb color
        Color thumbColor = booleanProperty.getValue() ? MaterialTheme.PRIMARY_COLOR : MaterialTheme.ON_PRIMARY_COLOR;
        int thumbSize = switchHeight;
        int thumbX = booleanProperty.getValue() ? switchX + switchWidth - thumbSize : switchX;
        RenderUtil.drawRoundedRect(thumbX, switchY, thumbSize, thumbSize, thumbSize / 2, MaterialTheme.getRGB(thumbColor), true, true, true, true);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            booleanProperty.setValue(!booleanProperty.getValue());
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // No specific action
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Not applicable
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
