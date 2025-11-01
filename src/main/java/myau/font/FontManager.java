package myau.font;

import myau.Myau;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private Map<String, Font> loadedFonts = new HashMap<>();
    private IFontRenderer currentTrueTypeFontRenderer;
    private String currentFontName = "Minecraft"; // Default to Minecraft font
    private float defaultFontSize = 18.0f; // Default font size
    private float currentFontSize = defaultFontSize;

    public FontManager() {
        loadFonts();
    }

    private void loadFonts() {
        File fontDir = new File(Minecraft.getMinecraft().mcDataDir, "myau/fonts");
        if (!fontDir.exists()) {
            fontDir.mkdirs();
        }

        loadedFonts.put("Minecraft", null); // Placeholder, actual font object not needed for default

        try {
            String[] fontFiles = {
                "ComicShannsMonoNerdFont-Regular.otf",
                "ComicShannsMonoNerdFontMono-Regular.otf",
                "ComicShannsMonoNerdFontPropo-Regular.otf"
            };

            for (String fileName : fontFiles) {
                try (InputStream is = Myau.class.getResourceAsStream("/fonts/" + fileName)) {
                    if (is != null) {
                        Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
                        String fontName = fileName.split("-")[0];
                        loadedFonts.put(fontName, customFont);
                        ChatUtil.sendFormatted(String.format("%sLoaded font: &o%s&r", Myau.clientName, fontName));
                    } else {
                        ChatUtil.sendFormatted(String.format("%sFailed to load font from resources: &o%s&r", Myau.clientName, fileName));
                    }
                } catch (Exception e) {
                    ChatUtil.sendFormatted(String.format("%sError loading font &o%s&r: %s", Myau.clientName, fileName, e.getMessage()));
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            ChatUtil.sendFormatted(String.format("%sError scanning font directory: %s", Myau.clientName, e.getMessage()));
            e.printStackTrace();
        }

        setCurrentFont("Minecraft");
    }

    public void setCurrentFont(String fontName) {
        if (fontName.equalsIgnoreCase("Minecraft")) {
            this.currentFontName = "Minecraft";
            this.currentTrueTypeFontRenderer = null; // Use default Minecraft FontRenderer
            ChatUtil.sendFormatted(String.format("%sFont set to &oMinecraft&r", Myau.clientName));
        } else if (loadedFonts.containsKey(fontName)) {
            this.currentFontName = fontName;
            Font selectedFont = loadedFonts.get(fontName);
            this.currentTrueTypeFontRenderer = new TrueTypeFontRenderer(selectedFont, currentFontSize, true);
            ChatUtil.sendFormatted(String.format("%sFont set to &o%s&r (Size: %.1f)&r", Myau.clientName, fontName, currentFontSize));
        } else {
            ChatUtil.sendFormatted(String.format("%sFont &o%s&r not found! Available: %s&r", Myau.clientName, fontName, String.join(", ", loadedFonts.keySet())));
        }
    }

    public void setFontSize(float newSize) {
        if (newSize <= 0) {
            ChatUtil.sendFormatted(String.format("%sFont size must be greater than 0!&r", Myau.clientName));
            return;
        }
        this.currentFontSize = newSize;
        if (!this.currentFontName.equalsIgnoreCase("Minecraft")) {
            // Re-initialize the custom font renderer with the new size
            setCurrentFont(this.currentFontName);
        }
        ChatUtil.sendFormatted(String.format("%sFont size set to &o%.1f&r", Myau.clientName, currentFontSize));
    }

    public IFontRenderer getFontRenderer() {
        if (currentTrueTypeFontRenderer == null) {
            return new MinecraftFontRendererWrapper();
        }
        return currentTrueTypeFontRenderer;
    }

    public String getCurrentFontName() {
        return currentFontName;
    }

    public float getCurrentFontSize() {
        return currentFontSize;
    }

    public Map<String, Font> getLoadedFonts() {
        return loadedFonts;
    }
}
