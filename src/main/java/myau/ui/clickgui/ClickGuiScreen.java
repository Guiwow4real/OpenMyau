package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.module.modules.ClickGUIModule;
import myau.ui.clickgui.component.Component;
import myau.ui.clickgui.component.SearchBar;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    // Animation states
    private boolean isClosing = false;
    private long openTime = 0L;
    private static final long ANIMATION_DURATION = 200L;
    private static final long STAGGER_DELAY = 50L;

    // Search Bar
    private SearchBar searchBar;
    private String searchQuery = "";
    
    public ClickGuiScreen() {
        this.frames = new ArrayList<>();
        // Initialize SearchBar at a temporary position; it will be centered in drawScreen
        this.searchBar = new SearchBar(0, 10, 120, 20); // Reduced height for a sleeker look
        
        int currentX = 10;
        int currentY = 40; // Move frames down to make space for the centered search bar
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

    @Override
    public void initGui() {
        super.initGui();
        this.isClosing = false;
        this.openTime = System.currentTimeMillis();
        for (Frame frame : frames) {
            frame.refilter(this.searchQuery);
        }
    }

    public void close() {
        if (isClosing) return;
        this.isClosing = true;
        this.openTime = System.currentTimeMillis(); // Reset timer for closing animation
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateScroll();

        // Handle Search Query Change
        String newQuery = searchBar.getText();
        if (!newQuery.equals(this.searchQuery)) {
            this.searchQuery = newQuery;
            for (Frame frame : frames) {
                frame.refilter(this.searchQuery);
            }
        }

        long elapsedTime = System.currentTimeMillis() - openTime;
        long totalAnimationTime = (frames.size() * STAGGER_DELAY) + ANIMATION_DURATION;

        if (isClosing && elapsedTime > totalAnimationTime) {
            mc.displayGuiScreen(null);
            return;
        }

        // Animate Frames first to ensure they are in the background
        for (int i = 0; i < frames.size(); i++) {
            int frameIndex = isClosing ? (frames.size() - 1 - i) : i;
            Frame frame = frames.get(frameIndex);
            long startTime = i * STAGGER_DELAY + 50; // Add a small delay so it starts after the search bar

            if (elapsedTime >= startTime) {
                long animationElapsedTime = elapsedTime - startTime;
                float progress = Math.min(1.0f, (float) animationElapsedTime / (float) ANIMATION_DURATION);
                frame.render(mouseX, mouseY, partialTicks, isClosing ? 1.0f - progress : progress, false, scrollY);
            }
        }

        // Center and animate SearchBar on top
        ScaledResolution sr = new ScaledResolution(mc);
        searchBar.updatePosition(sr.getScaledWidth() / 2 - searchBar.getWidth() / 2, searchBar.getY());
        float searchBarProgress = Math.min(1.0f, (float) elapsedTime / (float) ANIMATION_DURATION);
        searchBar.render(mouseX, mouseY, partialTicks, isClosing ? 1.0f - searchBarProgress : searchBarProgress);

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
        if (isClosing) return;
        super.handleMouseInput();
        
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            velocity += wheel > 0 ? -30 : 30;
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isClosing) return;
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (searchBar.mouseClicked(mouseX, mouseY, mouseButton)) return;
        
        for (Frame frame : frames) {
            if (frame.mouseClicked(mouseX, mouseY + scrollY, mouseButton)) {
                draggingComponent = frame;
                return;
            }
        }
    }
        
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (isClosing) return;
        super.mouseReleased(mouseX, mouseY, state);
        
        if (draggingComponent != null) {
            draggingComponent.mouseReleased(mouseX, mouseY + scrollY, state);
            draggingComponent = null;
        }

        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY + scrollY, state);
        }
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isClosing) return;
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        
        if (draggingComponent != null) {
            if (draggingComponent instanceof Frame) {
                ((Frame)draggingComponent).updatePosition(mouseX, mouseY);
            }
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isClosing) return;

        if (System.currentTimeMillis() - this.openTime < 100) {
            return;
        }

        if (searchBar.isFocused()) {
            searchBar.keyTyped(typedChar, keyCode);
            return;
        }

        Module clickGUIModule = Myau.moduleManager.getModule("ClickGUI");
        if (keyCode == Keyboard.KEY_ESCAPE || (clickGUIModule != null && keyCode == clickGUIModule.getKey())) {
            close();
            return;
        }
        
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