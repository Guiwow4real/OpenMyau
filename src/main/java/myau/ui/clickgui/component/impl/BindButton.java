package myau.ui.clickgui.component.impl;

import myau.module.Module;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;
import myau.util.RenderUtils;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

public class BindButton extends Component {

    private final Module module;
    private boolean listeningForKey;
    
    // 默认颜色值
    private static final int SECONDARY_COLOR = new Color(30, 30, 30, 180).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();

    public BindButton(Module module, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.module = module;
        this.listeningForKey = false;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, SECONDARY_COLOR);
        String text = listeningForKey ? "Press a key..." : "Bind: " + Keyboard.getKeyName(module.getKey());
        RenderUtils.drawWrappedString(fr, text, this.x + 2, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 4, TEXT_COLOR);
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
