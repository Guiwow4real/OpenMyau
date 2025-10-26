package myau.ui.clickgui.component;

import myau.property.properties.ColorProperty;
import myau.ui.clickgui.ClickGuiScreen;
import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class ColorPicker extends Component {

    private final ColorProperty colorProperty;
    private boolean hovered;
    private boolean pickingColor; // When the picker UI is visible
    private boolean draggingHue;
    private boolean draggingSatBri;

    private float hue, saturation, brightness;

    private static final int PICKER_HEIGHT = 80;
    private static final int HUE_SLIDER_HEIGHT = 10;

    public ColorPicker(ColorProperty colorProperty, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.colorProperty = colorProperty;
        this.pickingColor = false;
        this.draggingHue = false;
        this.draggingSatBri = false;
        updateHSBFromProperty();
    }

    private void updateHSBFromProperty() {
        int rgb = colorProperty.getValue();
        float[] hsb = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, 1.0f, false);
    }

    public void render(int mouseX, int mouseY, float partialTicks, boolean isLast) {
        render(mouseX, mouseY, partialTicks, 1.0f, isLast);
    }

    public void render(int mouseX, int mouseY, float partialTicks, float animationProgress, boolean isLast) {
        // Animation
        float easedProgress = 1.0f - (float) Math.pow(1.0f - animationProgress, 4);
        if (easedProgress <= 0) return;

        int scaledHeight = (int) (height * easedProgress);

        // Get scroll offset from ClickGuiScreen
        int scrollOffset = 0;
        try {
            scrollOffset = ClickGuiScreen.getInstance().getScrollY();
        } catch (Exception e) {
            // Ignore if we can't get scroll offset
        }
        
        int scrolledY = y - scrollOffset;
        int scaledY = scrolledY + (height - scaledHeight) / 2;

        hovered = mouseX >= x && mouseX <= x + width && mouseY >= scrolledY && mouseY <= scrolledY + height;

        boolean shouldRoundBottom = isLast && !pickingColor;

        RenderUtil.drawRoundedRect(x, scaledY, width, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress, MaterialTheme.getRGB(hovered ? MaterialTheme.SURFACE_CONTAINER_HIGH : MaterialTheme.SURFACE_CONTAINER_LOW), false, false, shouldRoundBottom, shouldRoundBottom);

        if (easedProgress > 0.9f) {
            int alpha = (int) (((easedProgress - 0.9f) / 0.1f) * 255);
            alpha = Math.max(0, Math.min(255, alpha));
            int textColor = (alpha << 24) | (MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR) & 0x00FFFFFF);
            int colorValue = (alpha << 24) | (colorProperty.getValue() & 0x00FFFFFF);
            int outlineColor = (alpha << 24) | (MaterialTheme.getRGB(MaterialTheme.OUTLINE_COLOR) & 0x00FFFFFF);

            fr.drawStringWithShadow(colorProperty.getName(), x + 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, textColor);

            int previewSize = 16;
            int previewX = x + width - previewSize - 5;
            int previewY = scrolledY + (height - previewSize) / 2;
            RenderUtil.drawRoundedRect(previewX, previewY, previewSize, previewSize, MaterialTheme.CORNER_RADIUS_SMALL / 2, colorValue, true, true, true, true);
            RenderUtil.drawRoundedRectOutline(previewX, previewY, previewSize, previewSize, MaterialTheme.CORNER_RADIUS_SMALL / 2, 1.0f, outlineColor, true, true, true, true);
        }

        if (pickingColor && easedProgress >= 1.0f) {
            int pickerX = x;
            int pickerY = scrolledY + height;
            int pickerWidth = width;

            if (Mouse.isButtonDown(0)) {
                updateColor(mouseX, mouseY + scrollOffset);
            }

            drawPicker(pickerX, pickerY, pickerWidth, scrollOffset);
        }
    }

    private void drawPicker(int pickerX, int pickerY, int pickerWidth, int scrollOffset) {
        int satBriHeight = PICKER_HEIGHT - HUE_SLIDER_HEIGHT;

        // Draw Saturation/Brightness box
        Color hueColor = Color.getHSBColor(this.hue, 1.0f, 1.0f);
        drawGradientRect(pickerX, pickerY, pickerX + pickerWidth, pickerY + satBriHeight, Color.WHITE.getRGB(), hueColor.getRGB(), true);
        drawGradientRect(pickerX, pickerY, pickerX + pickerWidth, pickerY + satBriHeight, 0, Color.BLACK.getRGB(), false);

        // Draw Hue slider
        int hueSliderY = pickerY + satBriHeight;
        for (int i = 0; i < pickerWidth; i++) {
            float currentHue = (float) i / (pickerWidth - 1);
            RenderUtil.drawRect(pickerX + i, hueSliderY, pickerX + i + 1, hueSliderY + HUE_SLIDER_HEIGHT, Color.getHSBColor(currentHue, 1.0f, 1.0f).getRGB());
        }

        RenderUtil.drawRectOutline(pickerX, pickerY, pickerWidth, PICKER_HEIGHT, 1.0f, MaterialTheme.getRGB(MaterialTheme.OUTLINE_COLOR));

        // Sat/Bri indicator
        float indicatorX = pickerX + this.saturation * pickerWidth;
        float indicatorY = pickerY + (1 - this.brightness) * satBriHeight;
        Gui.drawRect((int)(indicatorX - 2), (int)(indicatorY - 0.5f), (int)(indicatorX + 2), (int)(indicatorY + 0.5f), Color.BLACK.getRGB());
        Gui.drawRect((int)(indicatorX - 0.5f), (int)(indicatorY - 2), (int)(indicatorX + 0.5f), (int)(indicatorY + 2), Color.BLACK.getRGB());

        // Hue indicator
        float hueIndicatorX = pickerX + this.hue * pickerWidth;
        Gui.drawRect((int)(hueIndicatorX - 0.5f), hueSliderY, (int)(hueIndicatorX + 0.5f), hueSliderY + HUE_SLIDER_HEIGHT, Color.WHITE.getRGB());
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                pickingColor = !pickingColor;
                if (pickingColor) updateHSBFromProperty();
                return true;
            }

            if (pickingColor) {
                int pickerX = x;
                int pickerY = y + height;
                int pickerWidth = width;
                int satBriHeight = PICKER_HEIGHT - HUE_SLIDER_HEIGHT;
                int hueSliderY = pickerY + satBriHeight;

                if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth && mouseY >= pickerY && mouseY <= pickerY + satBriHeight) {
                    draggingSatBri = true;
                    return true;
                }
                if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth && mouseY >= hueSliderY && mouseY <= hueSliderY + HUE_SLIDER_HEIGHT) {
                    draggingHue = true;
                    return true;
                }
            }
        }
        if (pickingColor && !isMouseOver(mouseX, mouseY)) {
            pickingColor = false;
        }
        return false;
    }

    private void updateColor(int mouseX, int mouseY) {
        // Get scroll offset from ClickGuiScreen
        int scrollOffset = 0;
        try {
            scrollOffset = ClickGuiScreen.getInstance().getScrollY();
        } catch (Exception e) {
            // Ignore if we can't get scroll offset
        }
        
        int scrolledY = this.y - scrollOffset;
        int pickerX = x;
        int pickerY = scrolledY + height;
        int pickerWidth = width;
        int satBriHeight = PICKER_HEIGHT - HUE_SLIDER_HEIGHT;

        if (draggingSatBri) {
            this.saturation = (float) (mouseX - pickerX) / pickerWidth;
            this.brightness = 1 - (float) (mouseY - pickerY) / satBriHeight;
            this.saturation = Math.max(0, Math.min(1, this.saturation));
            this.brightness = Math.max(0, Math.min(1, this.brightness));
        }

        if (draggingHue) {
            this.hue = (float) (mouseX - pickerX) / pickerWidth;
            this.hue = Math.max(0, Math.min(1, this.hue));
        }
        colorProperty.setValue(Color.getHSBColor(this.hue, this.saturation, this.brightness).getRGB());
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            draggingHue = false;
            draggingSatBri = false;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {}

    @Override
    public int getHeight() {
        return pickingColor ? height + PICKER_HEIGHT : height;
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + getHeight();
    }

    private void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor, boolean horizontal) {
        if (horizontal) {
            float startA = (float)(startColor >> 24 & 255) / 255.0F;
            float startR = (float)(startColor >> 16 & 255) / 255.0F;
            float startG = (float)(startColor >> 8 & 255) / 255.0F;
            float startB = (float)(startColor & 255) / 255.0F;
            float endA = (float)(endColor >> 24 & 255) / 255.0F;
            float endR = (float)(endColor >> 16 & 255) / 255.0F;
            float endG = (float)(endColor >> 8 & 255) / 255.0F;
            float endB = (float)(endColor & 255) / 255.0F;

            RenderUtil.enableRenderState();
            GL11.glBegin(GL11.GL_QUADS);
            for (int i = left; i < right; i++) {
                float ratio = (float)(i - left) / (right - left);
                GL11.glColor4f(startR + (endR - startR) * ratio, startG + (endG - startG) * ratio, startB + (endB - startB) * ratio, startA + (endA - startA) * ratio);
                GL11.glVertex2f(i, top);
                GL11.glVertex2f(i, bottom);
                GL11.glVertex2f(i + 1, bottom);
                GL11.glVertex2f(i + 1, top);
            }
            GL11.glEnd();
            RenderUtil.disableRenderState();
        } else {
            drawGradientRect(left, top, right, bottom, startColor, endColor);
        }
    }
    private void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float startA = (float)(startColor >> 24 & 255) / 255.0F;
        float startR = (float)(startColor >> 16 & 255) / 255.0F;
        float startG = (float)(startColor >> 8 & 255) / 255.0F;
        float startB = (float)(startColor & 255) / 255.0F;
        float endA = (float)(endColor >> 24 & 255) / 255.0F;
        float endR = (float)(endColor >> 16 & 255) / 255.0F;
        float endG = (float)(endColor >> 8 & 255) / 255.0F;
        float endB = (float)(endColor & 255) / 255.0F;

        RenderUtil.enableRenderState();
        GL11.glBegin(GL11.GL_QUADS);
        for (int i = top; i < bottom; i++) {
            float ratio = (float)(i - top) / (bottom - top);
            GL11.glColor4f(startR + (endR - startR) * ratio, startG + (endG - startG) * ratio, startB + (endB - startB) * ratio, startA + (endA - startA) * ratio);
            GL11.glVertex2f(left, i);
            GL11.glVertex2f(right, i);
            GL11.glVertex2f(right, i + 1);
            GL11.glVertex2f(left, i + 1);
        }
        GL11.glEnd();
        RenderUtil.disableRenderState();
    }
    

}
