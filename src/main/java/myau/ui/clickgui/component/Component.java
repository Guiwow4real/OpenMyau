package myau.ui.clickgui.component;

import myau.ui.clickgui.Frame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public abstract class Component {

    protected final Frame parent;
    public int x;
    public int y;
    protected int width;
    protected int height;

    protected Minecraft mc = Minecraft.getMinecraft();
    protected FontRenderer fr = mc.fontRendererObj;

    public Component(Frame parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(int mouseX, int mouseY);

    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return null; // Default: no drag initiated by this component
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {}

    // This method will be called by ClickGuiScreen directly on the draggingComponent.
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        // Default implementation: do nothing. Subclasses like Slider will override this.
    }

    public void updatePosition(int mouseX, int mouseY) {
        // Default implementation: update component position based on mouse drag
        this.x = mouseX;
        this.y = mouseY;
    }

    public void keyTyped(char typedChar, int keyCode) {}

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
}
