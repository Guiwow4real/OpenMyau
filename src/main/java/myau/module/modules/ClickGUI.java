package myau.module.modules;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.ui.clickgui.ClickGuiScreen;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {

    public ClickGUI() {
        super("ClickGUI", "Opens the client GUI.", Category.RENDER, Keyboard.KEY_RSHIFT, false, false);
    }

    @Override
    public void onEnabled() {
        if (mc.currentScreen instanceof ClickGuiScreen) {
            mc.displayGuiScreen(null);
            this.setEnabled(false);
        } else {
            mc.displayGuiScreen(new ClickGuiScreen());
        }
    }
}