package myau.module.modules;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.ui.clickgui.ClickGuiScreen;
import myau.event.EventTarget;
import myau.events.LoadWorldEvent;
import myau.property.properties.BooleanProperty;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Module {
    
    public final BooleanProperty enable = new BooleanProperty("enable", true);

    public ClickGUI() {
        super("ClickGUI", "Opens the client GUI.", Category.RENDER, Keyboard.KEY_RSHIFT, false, false);
    }

    @Override
    public void onEnabled() {
        // 如果不在世界中，只阻止打开GUI，不设置为禁用
        if (mc.theWorld == null || mc.thePlayer == null) {
            // Do nothing (effectively preventing GUI from opening)
            this.setEnabled(false);
            return;
        }

        if (mc.currentScreen instanceof ClickGuiScreen) {
            mc.displayGuiScreen(null);
            this.setEnabled(false);
        } else {
            mc.displayGuiScreen(new ClickGuiScreen());
            // Set enabled to true when GUI is shown
            this.setEnabled(true);
        }
    }
    
    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        // 世界切换时关闭ClickGUI
        if (this.isEnabled()) {
            this.setEnabled(false);
        }
    }
}