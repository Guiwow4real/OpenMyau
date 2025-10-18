package myau.ui.clickgui.component;

import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import net.minecraft.client.gui.Gui;

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
        hovered = isMouseOver(mouseX, mouseY);

        Color bgColor = hovered ? MaterialTheme.PRIMARY_COLOR.brighter() : MaterialTheme.PRIMARY_COLOR;
        Color textColor = MaterialTheme.ON_PRIMARY_COLOR;

        RenderUtil.drawRoundedRect(x, y, width, height, MaterialTheme.CORNER_RADIUS_SMALL, MaterialTheme.getRGB(bgColor), true, true, true, true);
        fr.drawStringWithShadow(text, x + (width - fr.getStringWidth(text)) / 2, y + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(textColor));
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
