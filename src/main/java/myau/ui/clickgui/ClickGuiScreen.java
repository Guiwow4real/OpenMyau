package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import myau.module.modules.ClickGUIModule; // Import ClickGUIModule
import myau.ui.clickgui.component.Component;
import myau.ui.clickgui.component.TextField;
import myau.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.client.Minecraft; // Import Minecraft
import net.minecraft.client.gui.FontRenderer; // Import FontRenderer
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends GuiScreen {
    private static ClickGuiScreen instance;
    private ArrayList<Frame> frames;
    private Component draggingComponent = null;

    // Scrolling variables
    private double scrollY = 0;
    private double targetScrollY = 0;
    private double scrollVelocity = 0;
    private boolean isScrolling = false;
    private int lastMouseY = 0;
    private long lastScrollTime = 0;

    protected Minecraft mc = Minecraft.getMinecraft(); // Initialize mc
    protected FontRenderer fr = mc.fontRendererObj; // Initialize fr
    
    public ClickGuiScreen() {
        this.frames = new ArrayList<>();
        
        // Initialize Frames for each category
        int currentX = 10; // Start at a fixed X position from the screen edge
        int currentY = 10; // Start at a fixed Y position from the screen edge
        int frameWidth = 120;
        int frameHeight = 25;
        for (Category category : Category.values()) {
            Frame frame = new Frame(category, currentX, currentY, frameWidth, frameHeight);
            this.frames.add(frame);
            currentX += (frameWidth + 10); // Spacing between frames
        }
    }

    public static ClickGuiScreen getInstance() {
        if (instance == null) {
            instance = new ClickGuiScreen();
        }
        return instance;
    }

    // Removed updatePositionAndSize method as there is no main window to update
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Handle scrolling animation
        updateScrolling();
        
        // Apply scroll offset to mouse coordinates for component interactions
        int scrolledMouseY = mouseY - (int) scrollY;
        
        // Render frames (categories and modules) with scroll offset
        for (Frame frame : frames) {
            frame.render(mouseX, scrolledMouseY, partialTicks, (int) scrollY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            // Invert scrolling direction for natural scroll
            targetScrollY += dWheel > 0 ? -20 : 20;
            isScrolling = true;
            lastScrollTime = System.currentTimeMillis();
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Apply scroll offset to mouse coordinates
        int scrolledMouseY = mouseY - (int) scrollY;
        
        // Pass click to frames
        for (Frame frame : frames) {
            if (frame.mouseClicked(mouseX, scrolledMouseY, mouseButton)) {
                draggingComponent = frame;
                return;
            }
        }
        
        // Start drag scrolling with middle mouse button
        if (mouseButton == 2) {
            isScrolling = true;
            lastMouseY = mouseY;
        }
    }
        
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (draggingComponent != null) {
            draggingComponent.mouseReleased(mouseX, mouseY - (int) scrollY, state);
            draggingComponent = null; // Clear dragging component after release
        }

        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY - (int) scrollY, state);
        }
        
        // Stop drag scrolling
        if (state == 2) {
            isScrolling = false;
        }
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        
        // Handle drag scrolling with middle mouse button
        if (clickedMouseButton == 2 && isScrolling) {
            int delta = mouseY - lastMouseY;
            targetScrollY += delta * 2; // Increase sensitivity
            lastMouseY = mouseY;
        }
        
        if (draggingComponent != null) {
            // Only Frame has updatePosition currently for dragging
            if (draggingComponent instanceof Frame) {
                ((Frame)draggingComponent).updatePosition(mouseX, mouseY - (int) scrollY);
            }
            // If other components become draggable, they would need their own updatePosition or similar method
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        // Apply scroll offset to mouse coordinates for key events
        ScaledResolution sr = new ScaledResolution(mc);
        int mouseY = sr.getScaledHeight() / 2; // Use center of screen as reference
        int scrolledMouseY = mouseY - (int) scrollY;
        
        for (Frame frame : frames) {
            frame.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    /**
     * Update scrolling with non-linear animation
     */
    private void updateScrolling() {
        if (isScrolling || Math.abs(scrollY - targetScrollY) > 0.1) {
            // Calculate difference between current and target scroll
            double diff = targetScrollY - scrollY;
            
            // Apply non-linear easing function (smooth start and end)
            double easingFactor = 0.2; // Adjust this value to change animation speed
            scrollVelocity = diff * easingFactor;
            
            // Apply velocity to scroll position
            scrollY += scrollVelocity;
            
            // Apply bounds to prevent excessive scrolling
            int totalHeight = getTotalContentHeight();
            ScaledResolution sr = new ScaledResolution(mc);
            int screenHeight = sr.getScaledHeight();
            
            // Allow some overscroll for bounce effect
            targetScrollY = Math.max(-50, Math.min(targetScrollY, Math.max(0, totalHeight - screenHeight + 50)));
            
            // Stop scrolling if velocity is very small and target is reached
            if (Math.abs(diff) < 0.1 && Math.abs(scrollVelocity) < 0.1) {
                scrollY = targetScrollY;
                scrollVelocity = 0;
                // Only stop scrolling if no recent scroll events
                if (System.currentTimeMillis() - lastScrollTime > 100) {
                    isScrolling = false;
                }
            }
        }
    }
    
    /**
     * Get total height of all frames and their content
     */
    private int getTotalContentHeight() {
        int maxHeight = 0;
        for (Frame frame : frames) {
            int frameBottom = frame.getY() + frame.getTotalHeight();
            if (frameBottom > maxHeight) {
                maxHeight = frameBottom;
            }
        }
        return maxHeight;
    }
    
    /**
     * Get current scroll offset
     */
    public int getScrollY() {
        return (int) scrollY;
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ClickGUIModule guiModule = (ClickGUIModule) Myau.moduleManager.getModule("ClickGUI");
        if (guiModule != null) {
            // Removed saving window position/size as there is no main window anymore
            guiModule.setEnabled(false); // Disable the module when GUI is closed
        }
    }
}
