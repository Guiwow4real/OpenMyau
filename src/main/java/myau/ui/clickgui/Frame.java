package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.ui.clickgui.component.Component;
import myau.ui.clickgui.component.ModuleEntry;
import myau.util.RenderUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Frame extends Component {

    private int dragX, dragY;
    private final Category category;
    private boolean dragging;
    private boolean expanded;
    private final ArrayList<ModuleEntry> moduleEntries; // The master list of all modules
    private ArrayList<ModuleEntry> visibleEntries; // The list of modules to display after filtering

    public Frame(Category category, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.category = category;
        this.dragging = false;
        this.expanded = true;
        this.moduleEntries = new ArrayList<>();

        if (Myau.moduleManager != null) {
            for (Module module : Myau.moduleManager.getModulesInCategory(this.category)) {
                this.moduleEntries.add(new ModuleEntry(module, x, 0, width, 20));
            }
        }
        this.visibleEntries = new ArrayList<>(this.moduleEntries);
    }

    public void refilter(String query) {
        if (query == null || query.isEmpty()) {
            this.visibleEntries = new ArrayList<>(this.moduleEntries);
        } else {
            this.visibleEntries = this.moduleEntries.stream()
                .filter(entry -> entry.getModule().getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, 0, 1.0f);
    }
    
    public void render(int mouseX, int mouseY, float partialTicks, int scrollOffset) {
        render(mouseX, mouseY, partialTicks, scrollOffset, 1.0f);
    }

    public void render(int mouseX, int mouseY, float partialTicks, int scrollOffset, float animationProgress) {
        // Ease-out quint function for smooth animation
        float easedProgress = 1.0f - (float) Math.pow(1.0f - animationProgress, 4);

        int scaledWidth = (int) (width * easedProgress);
        int scaledHeight = (int) (height * easedProgress);

        int scaledX = x + (width - scaledWidth) / 2;
        int scrolledY = y - scrollOffset;
        int scaledY = scrolledY + (height - scaledHeight) / 2;

        if (easedProgress <= 0) return; // Don't render if invisible

        boolean hovered = isMouseOver(mouseX, mouseY);
        Color headerColor = hovered ? MaterialTheme.PRIMARY_CONTAINER_COLOR.brighter() : MaterialTheme.PRIMARY_CONTAINER_COLOR;

        RenderUtil.drawRoundedRect(scaledX, scaledY, scaledWidth, scaledHeight, MaterialTheme.CORNER_RADIUS_SMALL * easedProgress, MaterialTheme.getRGB(headerColor), true, true, false, false);
        
        if (easedProgress > 0.95f) {
            int alpha = (int) (((easedProgress - 0.95f) / 0.05f) * 255);
            alpha = Math.max(0, Math.min(255, alpha));
            int textColor = (alpha << 24) | (MaterialTheme.getRGB(MaterialTheme.ON_PRIMARY_CONTAINER_COLOR) & 0x00FFFFFF);

            fr.drawStringWithShadow(category.getName(), x + 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, textColor);

            String arrow = expanded ? "\u25B2" : "\u25BC";
            fr.drawStringWithShadow(arrow, x + width - fr.getStringWidth(arrow) - 5, scrolledY + (height - fr.FONT_HEIGHT) / 2, textColor);
        }

        if (expanded && easedProgress > 0) {
            int currentWorldY = y + height;
            for (int i = 0; i < visibleEntries.size(); i++) {
                ModuleEntry entry = visibleEntries.get(i);
                entry.setX(x);
                entry.setY(currentWorldY);
                entry.setWidth(width);
                
                boolean isLastEntry = (i == visibleEntries.size() - 1);
                entry.render(mouseX, mouseY, partialTicks, isLastEntry, animationProgress);
                currentWorldY += entry.getTotalHeight();
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseButton == 0) {
                this.dragging = true;
                this.dragX = mouseX - this.x;
                this.dragY = mouseY - this.y;
                return true;
            } else if (mouseButton == 1) {
                expanded = !expanded;
                return true;
            }
        }

        if (expanded) {
            for (ModuleEntry entry : visibleEntries) {
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
            for (ModuleEntry entry : visibleEntries) {
                entry.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expanded) {
            for (ModuleEntry entry : visibleEntries) {
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
            for (ModuleEntry entry : visibleEntries) {
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
    
    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= this.y && mouseY < this.y + height;
    }
}