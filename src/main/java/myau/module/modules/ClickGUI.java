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
        // 检查是否在世界中
        if (mc.theWorld == null || mc.thePlayer == null) {
            // 如果不在世界中，禁止开启
            this.setEnabled(false);
            return;
        }
        
        if (mc.currentScreen instanceof ClickGuiScreen) {
            mc.displayGuiScreen(null);
            this.setEnabled(false);
        } else {
            mc.displayGuiScreen(new ClickGuiScreen());
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