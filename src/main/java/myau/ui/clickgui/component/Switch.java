package myau.ui.clickgui.component;

import myau.property.properties.BooleanProperty;
import myau.ui.clickgui.ClickGuiScreen;
import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;

import java.awt.Color;

public class Switch extends Component {

    private final BooleanProperty booleanProperty;
    private boolean hovered;

    public Switch(BooleanProperty booleanProperty, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.booleanProperty = booleanProperty;
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

        // Draw background
        RenderUtil.drawRoundedRect(x, scaledY, width, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress, MaterialTheme.getRGB(hovered ? MaterialTheme.SURFACE_CONTAINER_HIGH : MaterialTheme.SURFACE_CONTAINER_LOW), false, false, isLast, isLast);

        if (easedProgress > 0.9f) {
            int alpha = (int) (((easedProgress - 0.9f) / 0.1f) * 255);
            alpha = Math.max(0, Math.min(255, alpha));
            int textColor = (alpha << 24) | (MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR) & 0x00FFFFFF);

            // Draw property name
            RenderUtil.getFontRenderer().drawStringWithShadow(booleanProperty.getName(), x + 5, scrolledY + (height - RenderUtil.getFontRenderer().getFontHeight()) / 2, textColor);

            // Draw switch itself
            int switchWidth = 28;
            int switchHeight = 16;
            int switchX = x + width - switchWidth - 5;
            int switchY = scrolledY + (height - switchHeight) / 2;

            Color trackColor = booleanProperty.getValue() ? MaterialTheme.PRIMARY_COLOR.darker().darker() : MaterialTheme.SURFACE_VARIANT_COLOR;
            int trackColorRgb = (alpha << 24) | (MaterialTheme.getRGB(trackColor) & 0x00FFFFFF);
            RenderUtil.drawRoundedRect(switchX, switchY + switchHeight / 4, switchWidth, switchHeight / 2, switchHeight / 4, trackColorRgb, true, true, true, true);

            Color thumbColor = booleanProperty.getValue() ? MaterialTheme.PRIMARY_COLOR : MaterialTheme.ON_PRIMARY_COLOR;
            int thumbColorRgb = (alpha << 24) | (MaterialTheme.getRGB(thumbColor) & 0x00FFFFFF);
            int thumbSize = switchHeight;
            int thumbX = booleanProperty.getValue() ? switchX + switchWidth - thumbSize : switchX;
            RenderUtil.drawRoundedRect(thumbX, switchY, thumbSize, thumbSize, thumbSize / 2, thumbColorRgb, true, true, true, true);
        }
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Not needed for this component
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // No specific action
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            booleanProperty.setValue(!booleanProperty.getValue());
            return true;
        }
        return false;
    }
}
