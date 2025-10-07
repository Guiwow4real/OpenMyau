package myau.ui.clickgui.component.impl;

import myau.property.properties.BooleanProperty;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.Component;
import myau.util.RenderUtils;
import net.minecraft.client.gui.Gui;
import myau.ui.clickgui.IntelliJTheme;
import java.awt.Color;

public class Checkbox extends Component {

    private final BooleanProperty booleanProperty;
    
    // IntelliJ IDEA主题颜色
    private static final int BACKGROUND_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
    private static final int CHECKBOX_COLOR = IntelliJTheme.getRGB(IntelliJTheme.CHECKBOX_COLOR);
    private static final int BOOLEAN_TEXT_COLOR = IntelliJTheme.getRGB(IntelliJTheme.BOOLEAN_COLOR);
    private static final int HOVER_COLOR = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);

    public Checkbox(BooleanProperty booleanProperty, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.booleanProperty = booleanProperty;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        // IntelliJ风格背景 - 检查鼠标悬停
        boolean isMouseOver = isMouseOver(mouseX, mouseY);
        int backgroundColor = isMouseOver ? HOVER_COLOR : BACKGROUND_COLOR;
        
        // Draw background
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor);

        // IntelliJ风格复选框 - 使用蓝色布尔值颜色
        int checkboxSize = 10;
        int checkboxX = this.x + 6;
        int checkboxY = this.y + this.height / 2 - checkboxSize / 2;
        
        // 绘制复选框边框
        Gui.drawRect(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize, BACKGROUND_COLOR);
        
        // 如果选中，填充复选框
        if (this.booleanProperty.getValue()) {
            Gui.drawRect(checkboxX + 1, checkboxY + 1, checkboxX + checkboxSize - 1, checkboxY + checkboxSize - 1, CHECKBOX_COLOR);
        }

        // IntelliJ风格属性名 - 使用布尔值蓝色
        RenderUtils.drawWrappedString(fr, this.booleanProperty.getName(), this.x + 22, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 26, BOOLEAN_TEXT_COLOR);
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
