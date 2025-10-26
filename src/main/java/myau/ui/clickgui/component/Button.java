package myau.ui.clickgui.component;

import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;

import java.awt.Color;

public class Button extends Component {

    private String text;
    private Runnable action;
    private boolean hovered;

    public Button(int x, int y, int width, int height, String text, Runnable action) {
        super(x, y, width, height);
        this.text = text;
        this.action = action;
        this.hovered = false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, 1.0f, true);
    }

    public void render(int mouseX, int mouseY, float partialTicks, float animationProgress, boolean isLast) {
        // Animation
        float easedProgress = 1.0f - (float) Math.pow(1.0f - animationProgress, 4);
        if (easedProgress <= 0) return;

        int scaledHeight = (int) (height * easedProgress);
        int scaledY = y + (height - scaledHeight) / 2;

        hovered = isMouseOver(mouseX, mouseY);

        Color bgColor = hovered ? MaterialTheme.PRIMARY_COLOR.brighter() : MaterialTheme.PRIMARY_COLOR;
        Color textColor = MaterialTheme.ON_PRIMARY_COLOR;

        RenderUtil.drawRoundedRect(x, scaledY, width, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress, MaterialTheme.getRGB(bgColor), true, true, true, true);
        
        if (easedProgress > 0.9f) {
            int alpha = (int) (((easedProgress - 0.9f) / 0.1f) * 255);
            alpha = Math.max(0, Math.min(255, alpha));
            int textColorRgb = (alpha << 24) | (MaterialTheme.getRGB(textColor) & 0x00FFFFFF);
            fr.drawStringWithShadow(text, x + (width - fr.getStringWidth(text)) / 2, y + (height - fr.FONT_HEIGHT) / 2, textColorRgb);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            action.run();
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // No specific action on mouse release for a simple button
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Not applicable for a simple button
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
