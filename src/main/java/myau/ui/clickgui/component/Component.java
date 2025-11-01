package myau.ui.clickgui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public abstract class Component {
    protected final Minecraft mc = Minecraft.getMinecraft();
    protected final FontRenderer fr = mc.fontRendererObj;

    public int x, y, width, height;

    public Component(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(int mouseX, int mouseY, float partialTicks, float animationProgress, boolean isLast, int scrollOffset);

    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, 1.0f, false, 0);
    }

    // Overloaded render method for animations. Components can override this to animate.
    public void render(int mouseX, int mouseY, float partialTicks, float animationProgress) {
        render(mouseX, mouseY, partialTicks, animationProgress, false, 0);
    }

    // Overloaded render method for animations and corner rounding. Components can override this.
    public void render(int mouseX, int mouseY, float partialTicks, float animationProgress, boolean isLast) {
        render(mouseX, mouseY, partialTicks, animationProgress, isLast, 0);
    }

    public abstract boolean mouseClicked(int mouseX, int mouseY, int mouseButton);
    public abstract void mouseReleased(int mouseX, int mouseY, int mouseButton);
    public abstract void keyTyped(char typedChar, int keyCode);
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}
