package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.module.modules.ClickGUIModule;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;
import java.util.ArrayList;

public class ClickGuiScreen extends GuiScreen {
    private static ClickGuiScreen instance;
    private ArrayList<Frame> frames;
    private Component draggingComponent = null;

    private int scrollY = 0;
    private int targetScrollY = 0;
    private double velocity = 0;
    private static final double FRICTION = 0.85;
    private static final double SNAP_STRENGTH = 0.15;

    protected Minecraft mc = Minecraft.getMinecraft();
    protected FontRenderer fr = mc.fontRendererObj;
    
    public ClickGuiScreen() {
        this.frames = new ArrayList<>();
        
        int currentX = 10;
        int currentY = 10;
        int frameWidth = 120;
        int frameHeight = 25;
        for (Category category : Category.values()) {
            Frame frame = new Frame(category, currentX, currentY, frameWidth, frameHeight);
            this.frames.add(frame);
            currentX += (frameWidth + 10);
        }
    }

    public static ClickGuiScreen getInstance() {
        if (instance == null) {
            instance = new ClickGuiScreen();
        }
        return instance;
    }
    
    private long openAnimationTimer = 0L;
    private static final long ANIMATION_DURATION = 200L;
    private static final long STAGGER_DELAY = 50L;

    @Override
    public void initGui() {
        super.initGui();
        this.openAnimationTimer = System.currentTimeMillis();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateScroll();

        long elapsedTime = System.currentTimeMillis() - openAnimationTimer;

        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            long startTime = i * STAGGER_DELAY;

            if (elapsedTime >= startTime) {
                long animationElapsedTime = elapsedTime - startTime;
                float progress = Math.min(1.0f, (float) animationElapsedTime / (float) ANIMATION_DURATION);
                frame.render(mouseX, mouseY, partialTicks, scrollY, progress);
            } else {
                // Don't render the frame until its start time is reached
            }
        }

        // InvWalk implementation
        try {
            Module invWalkModule = Myau.moduleManager.getModule("InvWalk");
            if (invWalkModule != null && invWalkModule.isEnabled()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
            }
        } catch (Exception e) {
            // To prevent crashes if something goes wrong with module loading
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            velocity += wheel > 0 ? -30 : 30;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        for (Frame frame : frames) {
            if (frame.mouseClicked(mouseX, mouseY, mouseButton)) {
                draggingComponent = frame;
                return;
            }
        }
    }
        
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        
        if (draggingComponent != null) {
            draggingComponent.mouseReleased(mouseX, mouseY, state);
            draggingComponent = null;
        }

        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, state);
        }
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        
        if (draggingComponent != null) {
            if (draggingComponent instanceof Frame) {
                ((Frame)draggingComponent).updatePosition(mouseX, mouseY);
            }
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        
        for (Frame frame : frames) {
            frame.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    private void updateScroll() {
        targetScrollY += (int)velocity;
        velocity *= FRICTION;
        
        int maxScroll = getMaxScroll();
        targetScrollY = Math.max(0, Math.min(targetScrollY, maxScroll));
        
        int delta = targetScrollY - scrollY;
        scrollY += (int)(delta * SNAP_STRENGTH);
        
        if (Math.abs(velocity) < 0.5) velocity = 0;
        if (Math.abs(delta) < 1 && Math.abs(velocity) < 0.5) scrollY = targetScrollY;
    }
    
    private int getMaxScroll() {
        int max = 0;
        for (Frame frame : frames) {
            int bottom = frame.getY() + frame.getTotalHeight();
            if (bottom > max) max = bottom;
        }
        ScaledResolution sr = new ScaledResolution(mc);
        return Math.max(0, max - sr.getScaledHeight() + 20);
    }
    
    public int getScrollY() {
        return scrollY;
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ClickGUIModule guiModule = (ClickGUIModule) Myau.moduleManager.getModule("ClickGUI");
        if (guiModule != null) {
            guiModule.setEnabled(false);
        }
    }
}