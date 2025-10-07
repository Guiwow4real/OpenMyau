package myau.ui.clickgui.component.impl;

import myau.module.Module;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;
import myau.util.RenderUtils;
import myau.ui.clickgui.IntelliJTheme;
import java.awt.Color;

public class HideButton extends Component {

    private final Module module;
    
    // IntelliJ IDEA主题颜色
    private static final int BACKGROUND_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
    private static final int BOOLEAN_TEXT_COLOR = IntelliJTheme.getRGB(IntelliJTheme.BOOLEAN_COLOR);
    private static final int HOVER_COLOR = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);

    public HideButton(Module module, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.module = module;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        // IntelliJ风格背景 - 检查鼠标悬停
        boolean isMouseOver = isMouseOver(mouseX, mouseY);
        int backgroundColor = isMouseOver ? HOVER_COLOR : BACKGROUND_COLOR;
        
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor);
        RenderUtils.drawWrappedString(fr, "Hide: " + (module.isHidden() ? "True" : "False"), this.x + 6, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 12, BOOLEAN_TEXT_COLOR);
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
