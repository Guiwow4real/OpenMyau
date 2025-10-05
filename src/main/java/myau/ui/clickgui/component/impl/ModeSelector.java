package myau.ui.clickgui.component.impl;

import myau.property.properties.ModeProperty;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;
import myau.util.RenderUtils;
import java.awt.Color;

public class ModeSelector extends Component {

    private final ModeProperty modeProperty;
    
    // 默认颜色值
    private static final int SECONDARY_COLOR = new Color(30, 30, 30, 180).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();

    public ModeSelector(ModeProperty modeProperty, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.modeProperty = modeProperty;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, SECONDARY_COLOR);
        String displayText = this.modeProperty.getName() + ": " + this.modeProperty.getModeString();
        RenderUtils.drawWrappedString(fr, displayText, this.x + 2, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 4, TEXT_COLOR);
    }

    @Override
    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseButton == 0) { // Left click to cycle forward
                int currentValue = this.modeProperty.getValue();
                int newValue = (currentValue + 1) % this.modeProperty.getValuePrompt().split(", ").length;
                this.modeProperty.setValue(newValue);
            } else if (mouseButton == 1) { // Right click to cycle backward
                int currentValue = this.modeProperty.getValue();
                int modeCount = this.modeProperty.getValuePrompt().split(", ").length;
                int newValue = (currentValue - 1 + modeCount) % modeCount;
                this.modeProperty.setValue(newValue);
            }
            return this; // Indicate that this component handled the click
        }
        return null; // Indicate that this component did not handle the click
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        // No dragging for this component
    }
}