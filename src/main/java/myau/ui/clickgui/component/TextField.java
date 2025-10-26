package myau.ui.clickgui.component;

import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

public class TextField extends Component {

    private String text;
    private String hintText;
    private boolean focused;
    private int cursorPosition;
    private long lastCursorToggle;
    private boolean drawBackground = true; // Add this field

    public TextField(int x, int y, int width, int height, String hintText) {
        super(x, y, width, height);
        this.text = "";
        this.hintText = hintText;
        this.focused = false;
        this.cursorPosition = 0;
        this.lastCursorToggle = System.currentTimeMillis();
    }

    public void setDrawBackground(boolean drawBackground) { // Add this method
        this.drawBackground = drawBackground;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, 1.0f, true);
    }

    public void render(int mouseX, int mouseY, float partialTicks, float animationProgress, boolean isLast) {
        // Animation
        float easedProgress = 1.0f - (float) Math.pow(1.0f - animationProgress, 4);
        if (easedProgress <= 0) return;

        int scaledHeight = (int) (height * easedProgress);
        int scaledY = y + (height - scaledHeight) / 2;

        if (this.drawBackground) {
            // Background
            RenderUtil.drawRoundedRect(x, scaledY, width, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress, MaterialTheme.getRGB(MaterialTheme.SURFACE_CONTAINER_LOW), true, true, true, true);
            // Border
            Color borderColor = focused ? MaterialTheme.PRIMARY_COLOR : MaterialTheme.OUTLINE_COLOR;
            RenderUtil.drawRoundedRectOutline(x, scaledY, width, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress, 1.0f, MaterialTheme.getRGB(borderColor), true, true, true, true);
        }

        if (easedProgress > 0.9f) {
            int alpha = (int) (((easedProgress - 0.9f) / 0.1f) * 255);
            alpha = Math.max(0, Math.min(255, alpha));
            int textColorValue = text.isEmpty() && !focused ? MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR_SECONDARY) : MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR);
            int textColor = (alpha << 24) | (textColorValue & 0x00FFFFFF);

            // Text
            String displayedText = text.isEmpty() && !focused ? hintText : text;
            fr.drawStringWithShadow(displayedText, x + 5, y + (height - fr.FONT_HEIGHT) / 2, textColor);

            // Cursor
            if (focused && System.currentTimeMillis() - lastCursorToggle > 500) {
                if (System.currentTimeMillis() % 1000 < 500) { // Blink every 500ms
                    String currentTextUntilCursor = text.substring(0, cursorPosition);
                    int cursorX = x + 5 + fr.getStringWidth(currentTextUntilCursor);
                    Gui.drawRect(cursorX, y + (height - fr.FONT_HEIGHT) / 2 - 1, cursorX + 1, y + (height + fr.FONT_HEIGHT) / 2 + 1, textColor);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            focused = true;
            lastCursorToggle = System.currentTimeMillis(); // Reset cursor blink
            // Set cursor position to end of text when clicked inside
            cursorPosition = text.length();
            return true;
        } else {
            focused = false;
            return false;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // Not applicable
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!focused) return;

        if (keyCode == Keyboard.KEY_BACK) {
            if (text.length() > 0 && cursorPosition > 0) {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
            }
        } else if (keyCode == Keyboard.KEY_DELETE) {
            if (cursorPosition < text.length()) {
                text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
            }
        } else if (keyCode == Keyboard.KEY_LEFT) {
            if (cursorPosition > 0) {
                cursorPosition--;
            }
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            if (cursorPosition < text.length()) {
                cursorPosition++;
            }
        } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            text = text.substring(0, cursorPosition) + typedChar + text.substring(cursorPosition);
            cursorPosition++;
        }
        lastCursorToggle = System.currentTimeMillis(); // Reset cursor blink on key press
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.cursorPosition = text.length();
    }

    public boolean isFocused() {
        return focused;
    }
}
