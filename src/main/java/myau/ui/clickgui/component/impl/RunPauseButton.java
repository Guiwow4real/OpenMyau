package myau.ui.clickgui.component.impl;

import myau.module.Module;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.IntelliJTheme;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;

import java.awt.Color;

public class RunPauseButton extends Component {

    private final Module module;

    public RunPauseButton(Frame parent, Module module, int width, int height) {
        super(parent, 0, 0, width, height);
        this.module = module;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        int bgColor = module.isEnabled() ? IntelliJTheme.getRGB(IntelliJTheme.RUN_BUTTON_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.PAUSE_BUTTON_COLOR);
        Gui.drawRect(x, y, x + width, y + height, bgColor);

        // Draw text
        String text = module.isEnabled() ? "||" : "▶";
        int textColor = new Color(220, 220, 220).getRGB(); // White text
        fr.drawStringWithShadow(text, x + width / 2 - fr.getStringWidth(text) / 2, y + height / 2 - fr.FONT_HEIGHT / 2, textColor);
    }

    @Override
    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (mouseButton == 0) { // Left click
                module.toggle();
                return this;
            }
        }
        return null;
    }
}
