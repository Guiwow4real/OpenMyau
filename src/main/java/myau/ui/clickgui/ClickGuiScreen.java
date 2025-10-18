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
        // Render frames (categories and modules)
        for (Frame frame : frames) {
            frame.render(mouseX, mouseY, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Pass click to frames
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
            draggingComponent = null; // Clear dragging component after release
        }

        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, state);
        }
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (draggingComponent != null) {
            // Only Frame has updatePosition currently for dragging
            if (draggingComponent instanceof Frame) {
                ((Frame)draggingComponent).updatePosition(mouseX, mouseY);
            }
            // If other components become draggable, they would need their own updatePosition or similar method
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
