package myau.ui.clickgui.component;

import myau.property.properties.ModeProperty;
import myau.ui.clickgui.ClickGuiScreen;
import myau.ui.clickgui.MaterialTheme;
import myau.util.RenderUtil;
import net.minecraft.client.gui.Gui;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class Dropdown extends Component {

    private final ModeProperty modeProperty;
    private boolean hovered;
    private boolean expanded;

    private static final int ITEM_HEIGHT = 16;

    public Dropdown(ModeProperty modeProperty, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.modeProperty = modeProperty;
        this.expanded = false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, false);
    }

    public void render(int mouseX, int mouseY, float partialTicks, boolean isLast) {
        // Get scroll offset from ClickGuiScreen
        int scrollOffset = 0;
        try {
            scrollOffset = ClickGuiScreen.getInstance().getScrollY();
        } catch (Exception e) {
            // Ignore if we can't get scroll offset
        }
        
        // Apply scroll offset
        int scrolledY = y - scrollOffset;
        hovered = isMouseOver(mouseX, mouseY + scrollOffset);

        boolean shouldRoundBottom = isLast && !expanded;

        // Draw background for the property name
        RenderUtil.drawRoundedRect(x, scrolledY, width, height, MaterialTheme.CORNER_RADIUS_SMALL, MaterialTheme.getRGB(hovered ? MaterialTheme.SURFACE_CONTAINER_HIGH : MaterialTheme.SURFACE_CONTAINER_LOW), false, false, shouldRoundBottom, shouldRoundBottom);

        // Draw property name and current mode
        String displayText = modeProperty.getName() + ": " + modeProperty.getModeString();
        fr.drawStringWithShadow(displayText, x + 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR));

        // Draw dropdown arrow
        String arrow = expanded ? "\u25B2" : "\u25BC"; // Up or Down arrow
        fr.drawStringWithShadow(arrow, x + width - fr.getStringWidth(arrow) - 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR_SECONDARY));

        if (expanded) {
            List<String> modes = Arrays.asList(modeProperty.getValuePrompt().split(", "));
            int dropdownY = scrolledY + height;
            int dropdownHeight = modes.size() * ITEM_HEIGHT;

            boolean roundBottomList = isLast;

            // Scissor test to prevent dropdown items from drawing outside the visible area
            RenderUtil.scissor(x, scrolledY + height, width, dropdownHeight);

            // Draw background for the entire dropdown list (with bottom rounded corners if it's the last component)
            RenderUtil.drawRoundedRect(x, dropdownY, width, dropdownHeight, MaterialTheme.CORNER_RADIUS_SMALL, MaterialTheme.getRGB(MaterialTheme.SURFACE_CONTAINER_LOW), false, false, roundBottomList, roundBottomList);

            for (int i = 0; i < modes.size(); i++) {
                String mode = modes.get(i);
                int itemY = dropdownY + i * ITEM_HEIGHT;
                boolean itemHovered = mouseX >= x && mouseX <= x + width && mouseY + scrollOffset >= itemY && mouseY + scrollOffset <= itemY + ITEM_HEIGHT;

                Color itemBgColor = itemHovered ? MaterialTheme.PRIMARY_CONTAINER_COLOR : MaterialTheme.SURFACE_CONTAINER_LOW;
                // Draw individual item background (no rounded corners)
                RenderUtil.drawRoundedRect(x, itemY, width, ITEM_HEIGHT, 0, MaterialTheme.getRGB(itemBgColor), false, false, false, false);

                fr.drawStringWithShadow(mode, x + 10, itemY + (ITEM_HEIGHT - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.TEXT_COLOR));
            }
            RenderUtil.releaseScissor(); // Release scissor test
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Check if click is on the main component body
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (mouseButton == 0) { // Left click toggles expansion
                expanded = !expanded;
                return true;
            }
        }

        // If expanded, check for clicks on the items
        if (expanded) {
            List<String> modes = Arrays.asList(modeProperty.getValuePrompt().split(", "));
            int dropdownY = y + height;
            for (int i = 0; i < modes.size(); i++) {
                int itemY = dropdownY + i * ITEM_HEIGHT;
                if (mouseX >= x && mouseX <= x + width && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT) {
                    if (mouseButton == 0) {
                        modeProperty.setValue(i);
                        expanded = false; // Close dropdown after selection
                        return true;
                    }
                }
            }
        }
        
        // If click is outside the component, close it
        if (expanded && !isMouseOver(mouseX, mouseY)) {
            expanded = false;
            // Don't return true here, let other components process the click
        }

        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // No specific action
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Not applicable
    }

    @Override
    public int getHeight() {
        if (expanded) {
            List<String> modes = Arrays.asList(modeProperty.getValuePrompt().split(", "));
            return height + (modes.size() * ITEM_HEIGHT);
        }
        return height;
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        // Get scroll offset from ClickGuiScreen
        int scrollOffset = 0;
        try {
            scrollOffset = ClickGuiScreen.getInstance().getScrollY();
        } catch (Exception e) {
            // Ignore if we can't get scroll offset
        }
        
        int scrolledY = this.y - scrollOffset;
        
        // For dropdown, consider expanded area when checking mouse over
        if (expanded) {
            return mouseX >= x && mouseX <= x + width && mouseY >= scrolledY && mouseY <= scrolledY + getHeight();
        }
        return mouseX >= x && mouseX <= x + width && mouseY >= scrolledY && mouseY <= scrolledY + height;
    }
}
