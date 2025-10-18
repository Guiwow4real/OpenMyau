package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.ui.clickgui.component.Component;
import myau.ui.clickgui.component.ModuleEntry;
import myau.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Frame extends Component {

    private int dragX, dragY;
    private final Category category;
    private boolean dragging;
    private boolean expanded;
    private final ArrayList<ModuleEntry> moduleEntries;

    public Frame(Category category, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.category = category;
        this.dragging = false;
        this.expanded = true; // By default, categories are expanded
        this.moduleEntries = new ArrayList<>();

        // Populate module entries
        if (Myau.moduleManager != null) {
            for (Module module : Myau.moduleManager.getModulesInCategory(this.category)) {
                this.moduleEntries.add(new ModuleEntry(module, x, 0, width, 20)); // Y will be set during render
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        // Render Frame header (category name)
        boolean hovered = isMouseOver(mouseX, mouseY);
        Color headerColor = hovered ? MaterialTheme.PRIMARY_CONTAINER_COLOR.brighter() : MaterialTheme.PRIMARY_CONTAINER_COLOR;

        // Draw Frame header with top-left and top-right rounded corners
        RenderUtil.drawRoundedRect(x, y, width, height, MaterialTheme.CORNER_RADIUS_SMALL, MaterialTheme.getRGB(headerColor), true, true, false, false);
        fr.drawStringWithShadow(category.getName(), x + 5, y + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.ON_PRIMARY_CONTAINER_COLOR));

        // Draw expansion arrow
        String arrow = expanded ? "\u25B2" : "\u25BC";
        fr.drawStringWithShadow(arrow, x + width - fr.getStringWidth(arrow) - 5, y + (height - fr.FONT_HEIGHT) / 2, MaterialTheme.getRGB(MaterialTheme.ON_PRIMARY_CONTAINER_COLOR));

        if (expanded) {
            int currentY = y + height;
            for (int i = 0; i < moduleEntries.size(); i++) {
                ModuleEntry entry = moduleEntries.get(i);
                entry.setX(x);
                entry.setY(currentY);
                entry.setWidth(width);
                // Determine if this is the last entry in the frame to apply bottom rounded corners
                boolean isLastEntry = (i == moduleEntries.size() - 1);
                entry.render(mouseX, mouseY, partialTicks, isLastEntry); // Pass flag to ModuleEntry
                currentY += entry.getTotalHeight();
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseButton == 0) { // Left click: drag frame or interact with modules
                this.dragging = true;
                this.dragX = mouseX - this.x;
                this.dragY = mouseY - this.y;
                return true;
            } else if (mouseButton == 1) { // Right click: toggle expansion
                expanded = !expanded;
                return true;
            }
        }

        if (expanded) {
            for (ModuleEntry entry : moduleEntries) {
                if (entry.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.dragging = false;
        if (expanded) {
            for (ModuleEntry entry : moduleEntries) {
                entry.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            for (ModuleEntry entry : moduleEntries) {
                entry.keyTyped(typedChar, keyCode);
            }
        }
    }

    public void updatePosition(int mouseX, int mouseY) {
        if (this.dragging) {
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
        }
    }

    public Category getCategory() {
        return category;
    }

    public List<ModuleEntry> getModuleEntries() {
        return moduleEntries;
    }

    public int getTotalHeight() {
        int total = height;
        if (expanded) {
            for (ModuleEntry entry : moduleEntries) {
                total += entry.getTotalHeight();
            }
        }
        return total;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
