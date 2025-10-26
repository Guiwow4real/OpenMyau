package myau.ui.clickgui.component;

import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

public class SearchBar extends Component {

    private final TextField textField;
    private boolean focused = false;
    private long focusTime = 0;

    public SearchBar(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.textField = new TextField(x + 10, y + 5, width - 20, height - 10, "Search modules...");
        this.textField.setDrawBackground(false); // Disable the inner background
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, 1.0f);
    }

    public void render(int mouseX, int mouseY, float partialTicks, float openCloseProgress) {
        // Main open/close animation
        float openCloseEased = 1.0f - (float) Math.pow(1.0f - openCloseProgress, 4);
        if (openCloseEased <= 0) return;

        // Dynamic Island-like width animation on focus
        float focusProgress = focused ? Math.min(1.0f, (System.currentTimeMillis() - focusTime) / 200.0f) : Math.max(0.0f, 1.0f - (System.currentTimeMillis() - focusTime) / 200.0f);
        float focusEased = 1.0f - (float) Math.pow(1.0f - focusProgress, 3);

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int baseWidth = 120;
        int focusedWidth = 240;
        int currentWidth = (int) (baseWidth + (focusedWidth - baseWidth) * focusEased);

        this.x = (screenWidth - currentWidth) / 2;
        this.width = currentWidth;

        int scaledHeight = (int) (this.height * openCloseEased);
        int scaledY = this.y + (this.height - scaledHeight) / 2;

        this.textField.x = this.x + 10;
        this.textField.y = this.y + (this.height - (this.textField.height)) / 2; // Center textfield vertically
        this.textField.width = this.width - 20;

        // Render background
        RenderUtil.drawRoundedRect(this.x, scaledY, this.width, scaledHeight, scaledHeight / 2.0f, MaterialTheme.getRGB(MaterialTheme.SURFACE_CONTAINER_HIGH), true, true, true, true);

        // Render text field (only if animation is mostly complete)
        if (openCloseEased > 0.9f) {
            textField.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean isOver = isMouseOver(mouseX, mouseY);
        if (isOver && mouseButton == 0) {
            if (!this.focused) {
                this.focused = true;
                this.focusTime = System.currentTimeMillis();
            }
            return textField.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            if (this.focused) {
                this.focused = false;
                this.focusTime = System.currentTimeMillis();
            }
            return textField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        textField.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (this.focused) {
            textField.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public String getText() {
        return textField.getText();
    }

    public boolean isFocused() {
        return this.focused;
    }
}
