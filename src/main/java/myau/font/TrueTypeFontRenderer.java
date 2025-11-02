package myau.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager; // Import GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class TrueTypeFontRenderer implements IFontRenderer {

    private Font font;
    private boolean antiAlias;
    private float fontSize;

    private Map<Character, CharData> charDataMap = new HashMap<>();
    private int fontHeight = 0;
    private int textureWidth = 512;
    private int textureHeight = 512;
    private DynamicTexture fontTexture; // Changed type to DynamicTexture

    public TrueTypeFontRenderer(Font font, float fontSize, boolean antiAlias) {
        this.font = font.deriveFont(fontSize);
        this.fontSize = fontSize;
        this.antiAlias = antiAlias;
        setupTexture();
    }

    private void setupTexture() {
        BufferedImage bufferedImage = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.setFont(font);

        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        }

        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, textureWidth, textureHeight);
        g.setColor(Color.WHITE);

        int currentX = 0;
        int currentY = 0;
        int rowHeight = 0;

        FontRenderContext frc = g.getFontRenderContext();

        for (int i = 0; i < 256; i++) { // ASCII characters
            char c = (char) i;
            if (c == 0) continue;

            String charStr = String.valueOf(c);
            Rectangle2D bounds = font.getStringBounds(charStr, frc);

            int charWidth = (int) Math.ceil(bounds.getWidth());
            int charHeight = (int) Math.ceil(bounds.getHeight());

            if (currentX + charWidth >= textureWidth) {
                currentX = 0;
                currentY += rowHeight + 2;
                rowHeight = 0;
            }

            if (currentY + charHeight >= textureHeight) {
                System.err.println("Font texture too small for all characters!");
                break;
            }

            g.drawString(charStr, currentX, currentY + font.getLineMetrics(charStr, frc).getAscent());

            charDataMap.put(c, new CharData(currentX, currentY, charWidth, charHeight));

            currentX += charWidth + 2;
            rowHeight = Math.max(rowHeight, charHeight);
            fontHeight = Math.max(fontHeight, charHeight);
        }

        fontTexture = new DynamicTexture(bufferedImage); // Store DynamicTexture directly
        // Apply nearest filtering for sharper font rendering
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTexture.getGlTextureId());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // Unbind texture
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return 0;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(fontTexture.getGlTextureId()); // Bind using texture ID from DynamicTexture

        // Draw shadow first (offset by 1 pixel)
        drawStringInternal(text, x + 1, y + 1, new Color(0, 0, 0, 100).getRGB());

        drawStringInternal(text, x, y, color);

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        return 0; // Return value is not critical for custom font renderer
    }

    private void drawStringInternal(String text, float x, float y, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);

        float currentX = x;
        for (char c : text.toCharArray()) {
            CharData charData = charDataMap.get(c);
            if (charData == null) {
                continue;
            }

            float texX = (float) charData.x / textureWidth;
            float texY = (float) charData.y / textureHeight;
            float texWidth = (float) charData.width / textureWidth;
            float texHeight = (float) charData.height / textureHeight;

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(texX, texY);
            GL11.glVertex2f(currentX, y);

            GL11.glTexCoord2f(texX, texY + texHeight);
            GL11.glVertex2f(currentX, y + charData.height);

            GL11.glTexCoord2f(texX + texWidth, texY + texHeight);
            GL11.glVertex2f(currentX + charData.width, y + charData.height);

            GL11.glTexCoord2f(texX + texWidth, texY);
            GL11.glVertex2f(currentX + charData.width, y);
            GL11.glEnd();

            currentX += charData.width;
        }
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        int width = 0;
        for (char c : text.toCharArray()) {
            CharData charData = charDataMap.get(c);
            if (charData != null) {
                width += charData.width;
            }
        }
        return width;
    }

    @Override
    public int getFontHeight() {
        return fontHeight;
    }

    private static class CharData {
        public int x, y, width, height;

        public CharData(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
