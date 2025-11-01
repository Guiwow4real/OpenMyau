package myau.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class MinecraftFontRendererWrapper implements IFontRenderer {

    private final FontRenderer mcFontRenderer;

    public MinecraftFontRendererWrapper() {
        this.mcFontRenderer = Minecraft.getMinecraft().fontRendererObj;
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return mcFontRenderer.drawStringWithShadow(text, x, y, color);
    }

    @Override
    public int getStringWidth(String text) {
        return mcFontRenderer.getStringWidth(text);
    }

    @Override
    public int getFontHeight() {
        return mcFontRenderer.FONT_HEIGHT;
    }
}
