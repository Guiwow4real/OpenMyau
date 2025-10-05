package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import myau.ui.clickgui.component.Component;
import org.lwjgl.input.Keyboard;
import myau.ui.clickgui.component.impl.BindButton;
import myau.util.RenderUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

public class ClickGuiScreen extends GuiScreen {

    private ArrayList<Frame> frames;
    private Component draggingComponent = null; // New field to track dragging component
    private BindButton listeningBindButton = null;
    
    // Static map to store frame positions for saving/loading
    public static Map<String, int[]> framePositions = new HashMap<>();
    
    // 默认颜色值
    private static final int BACKGROUND_COLOR = new Color(20, 20, 20).getRGB();
    private static final int BORDER_COLOR = new Color(40, 40, 40).getRGB();
    private static final boolean ENABLE_TRANSPARENT_BACKGROUND = true;
    private static final double CORNER_RADIUS = 3.0;

    public ClickGuiScreen() {
        this.frames = new ArrayList<>();
        int frameX = 5;
        int frameY = 5;
        int frameWidth = 100;
        int frameSpacing = 5;
        
        // Create a frame for each category
        for (Category category : Category.values()) {
            Frame frame = new Frame(category);
            frame.setX(frameX);
            frame.setY(frameY);
            
            // Load saved position if available
            if (framePositions.containsKey(category.getName())) {
                int[] position = framePositions.get(category.getName());
                frame.setX(position[0]);
                frame.setY(position[1]);
            }
            
            this.frames.add(frame);
            frameX += frameWidth + frameSpacing; // Position next frame to the right
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 绘制背景 - Fluent风格带圆角
        if (!ENABLE_TRANSPARENT_BACKGROUND) {
            // 使用圆角矩形绘制背景
            if (CORNER_RADIUS > 0) {
                RenderUtil.drawRoundedRect(0, 0, this.width, this.height, CORNER_RADIUS, BACKGROUND_COLOR);
                // 绘制圆角边框
                RenderUtil.drawRoundedOutline(0, 0, this.width, this.height, CORNER_RADIUS, 1.0f, BORDER_COLOR);
            } else {
                // 无圆角时使用原来的绘制方式
                Gui.drawRect(0, 0, this.width, this.height, BACKGROUND_COLOR);
                
                // Fluent风格边框效果
                Gui.drawRect(0, 0, this.width, 1, BORDER_COLOR); // 顶部边框
                Gui.drawRect(0, this.height - 1, this.width, this.height, BORDER_COLOR); // 底部边框
            }
        }
        
        // 渲染所有框架
        for (Frame frame : this.frames) {
            frame.render(mouseX, mouseY);
            frame.updatePosition(mouseX, mouseY);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleInput() throws IOException {
        // Allow movement keys to pass through to the game
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }

        super.handleInput(); // Call super to handle GUI-specific input (like mouse clicks, escape key)
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.listeningBindButton != null) {
            this.listeningBindButton.keyTyped(typedChar, keyCode);
            if (!this.listeningBindButton.isListeningForKey()) {
                this.listeningBindButton = null;
            }
            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // If a BindButton was listening, and a new click occurs, stop listening on the old one
        if (this.listeningBindButton != null) {
            this.listeningBindButton.keyTyped((char) 0, Keyboard.KEY_NONE); // Simulate a non-key press to stop listening
            this.listeningBindButton = null;
        }

        for (Frame frame : this.frames) {
            Component clickedComponent = frame.mouseClicked(mouseX, mouseY, mouseButton);
            if (clickedComponent != null) {
                // Handle component interaction and set dragging/listening state
                if (clickedComponent instanceof BindButton && ((BindButton) clickedComponent).isListeningForKey()) {
                    this.listeningBindButton = (BindButton) clickedComponent;
                } else {
                    this.draggingComponent = clickedComponent;
                }
                return; // Stop checking other frames once a component is interacted with
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.draggingComponent != null) {
            this.draggingComponent.onMouseReleased(mouseX, mouseY, state);
            this.draggingComponent = null;
        }

        for (Frame frame : this.frames) {
            frame.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        // Save frame positions if enabled
        if (myau.module.modules.GuiModule.savePosition.getValue()) {
            framePositions.clear();
            for (Frame frame : this.frames) {
                String categoryName = frame.getCategory().getName();
                int[] position = new int[]{frame.getX(), frame.getY()};
                framePositions.put(categoryName, position);
            }
        }
        
        // Disable ClickGUI module when closing the screen
        Myau.moduleManager.getModule("ClickGUI").setEnabled(false);
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
