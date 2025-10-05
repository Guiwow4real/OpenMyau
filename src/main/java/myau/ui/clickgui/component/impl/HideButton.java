package myau.ui.clickgui.component.impl;

import myau.module.Module;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;
import myau.util.RenderUtils;

import java.awt.Color;

public class HideButton extends Component {

    private final Module module;
    
    // 默认颜色值
    private static final int SECONDARY_COLOR = new Color(30, 30, 30, 180).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();

    public HideButton(Module module, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.module = module;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, SECONDARY_COLOR);
        RenderUtils.drawWrappedString(fr, "Hide: " + (module.isHidden() ? "True" : "False"), this.x + 2, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 4, TEXT_COLOR);
    }

    @Override
    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            module.setHidden(!module.isHidden());
            return null;
        }
        return null;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        // No dragging for this component
    }
}
