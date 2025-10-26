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

    public TextField(int x, int y, int width, int height, String hintText) {
        super(x, y, width, height);
        this.text = "";
        this.hintText = hintText;
        this.focused = false;
        this.cursorPosition = 0;
        this.lastCursorToggle = System.currentTimeMillis();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        //boolean hovered = isMouseOver(mouseX, mouseY);

        // Background
        RenderUtil.drawRoundedRect(x, y, width, height, MaterialTheme.CORNER_RADIUS_SMALL, MaterialTheme.getRGB(MaterialTheme.SURFACE_CONTAINER_LOW), true, true, true, true);

        // Border
        Color borderColor = focused ? MaterialTheme.PRIMARY_COLOR : MaterialTheme.OUTLINE_COLOR;
        RenderUtil.drawRoundedRectOutline(x, y, width, height, MaterialTheme.CORNER_RADIUS_SMALL, 1.0f, MaterialTheme.getRGB(borderColor), true, true, true, true);

        // Text
        String displayedText = text.isEmpty() && !focused ? hintText : text;
        int textColor = text.isEmpty() && !focused ? MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR_SECONDARY) : MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR);
        fr.drawStringWithShadow(displayedText, x + 5, y + (height - fr.FONT_HEIGHT) / 2, textColor);

        // Cursor
        if (focused && System.currentTimeMillis() - lastCursorToggle > 500) {
            if (System.currentTimeMillis() % 1000 < 500) { // Blink every 500ms
                String currentTextUntilCursor = text.substring(0, cursorPosition);
                int cursorX = x + 5 + fr.getStringWidth(currentTextUntilCursor);
                Gui.drawRect(cursorX, y + (height - fr.FONT_HEIGHT) / 2 - 1, cursorX + 1, y + (height + fr.FONT_HEIGHT) / 2 + 1, MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR));
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
