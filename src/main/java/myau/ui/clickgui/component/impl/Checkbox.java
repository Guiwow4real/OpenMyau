package myau.ui.clickgui.component.impl;

import myau.property.properties.BooleanProperty;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;

import java.awt.Color;

public class Checkbox extends Component {

    private final BooleanProperty booleanProperty;
    
    // 默认颜色值
    private static final int SECONDARY_COLOR = new Color(30, 30, 30, 180).getRGB();
    private static final int PRIMARY_COLOR = new Color(30, 150, 250).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();
    private static final int BACKGROUND_COLOR = new Color(20, 20, 20).getRGB();

    public Checkbox(BooleanProperty booleanProperty, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.booleanProperty = booleanProperty;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        // Draw background
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, SECONDARY_COLOR);

        // Draw property name
        fr.drawStringWithShadow(this.booleanProperty.getName(), this.x + 20, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, TEXT_COLOR);

        // Draw checkbox
        int checkboxSize = 12;
        int checkboxX = this.x + 4;
        int checkboxY = this.y + this.height / 2 - checkboxSize / 2;
        Gui.drawRect(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize, BACKGROUND_COLOR);

        if (this.booleanProperty.getValue()) {
            Gui.drawRect(checkboxX + 1, checkboxY + 1, checkboxX + checkboxSize - 1, checkboxY + checkboxSize - 1, PRIMARY_COLOR);
        }
    }

    @Override
    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            this.booleanProperty.setValue(!this.booleanProperty.getValue());
            return this; // Checkbox doesn't initiate a drag, but it handles a click
        }
        return null;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        // Checkbox doesn't have a dragging state, so no action needed here.
    }
}
