package myau.util;

import net.minecraft.client.gui.FontRenderer;
import java.util.List;
import java.util.ArrayList;

public class RenderUtils {

    public static void drawWrappedString(FontRenderer fr, String text, float x, float y, int wrapWidth, int color) {
        List<String> wrappedLines = wrapText(fr, text, wrapWidth);
        float currentY = y;
        for (String line : wrappedLines) {
            fr.drawStringWithShadow(line, x, currentY, color);
            currentY += fr.FONT_HEIGHT + 2; // Add some line spacing
        }
    }

    public static List<String> wrapText(FontRenderer fr, String text, int wrapWidth) {
        List<String> lines = new ArrayList<>();
        if (fr.getStringWidth(text) <= wrapWidth) {
            lines.add(text);
            return lines;
        }

        String currentLine = "";
        String[] words = text.split(" ");

        for (String word : words) {
            if (fr.getStringWidth(currentLine + " " + word) <= wrapWidth) {
                if (!currentLine.isEmpty()) {
                    currentLine += " ";
                }
                currentLine += word;
            } else {
                lines.add(currentLine);
                currentLine = word;
            }
        }
        lines.add(currentLine);
        return lines;
    }

    public static int getWrappedStringHeight(FontRenderer fr, String text, int wrapWidth) {
        List<String> wrappedLines = wrapText(fr, text, wrapWidth);
        return wrappedLines.size() * (fr.FONT_HEIGHT + 2); // +2 for line spacing
    }
}
