package myau.font;

public interface IFontRenderer {
    int drawStringWithShadow(String text, float x, float y, int color);
    int getStringWidth(String text);
    int getFontHeight();
}
