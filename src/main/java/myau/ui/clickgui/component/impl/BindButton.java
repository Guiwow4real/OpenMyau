package myau.ui.clickgui.component.impl;

import myau.module.Module;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.IntelliJTheme;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;
import myau.util.RenderUtils;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

public class BindButton extends Component {

    private final Module module;
    private boolean listeningForKey;
    
    // IntelliJ IDEA主题颜色
    private static final int BACKGROUND_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
    private static final int TYPE_VALUE_COLOR = IntelliJTheme.getRGB(IntelliJTheme.TYPE_VALUE_COLOR);
    private static final int HOVER_COLOR = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);

    public BindButton(Module module, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.module = module;
        this.listeningForKey = false;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        // IntelliJ风格背景 - 检查鼠标悬停
        boolean isMouseOver = isMouseOver(mouseX, mouseY);
        int backgroundColor = isMouseOver ? HOVER_COLOR : BACKGROUND_COLOR;
        
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor);
        String text = listeningForKey ? "Press a key..." : "Bind: " + Keyboard.getKeyName(module.getKey());
        RenderUtils.drawWrappedString(fr, text, this.x + 6, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 12, TYPE_VALUE_COLOR);
    }

    @Override
    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            this.listeningForKey = !this.listeningForKey;
            return null;
        }
        return null;
    }

    // This method will be called from ClickGuiScreen when a key is typed
    public void keyTyped(char typedChar, int keyCode) {
        if (listeningForKey) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                module.setKey(Keyboard.KEY_NONE);
            } else {
                module.setKey(keyCode);
            }
            this.listeningForKey = false;
        }
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        // No dragging for this component
    }

    public boolean isListeningForKey() {
        return listeningForKey;
    }
}
