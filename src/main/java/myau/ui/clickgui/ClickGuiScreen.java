package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import myau.ui.clickgui.component.Component;
import myau.ui.clickgui.component.ModuleButton;
import myau.ui.clickgui.Frame;
import myau.module.modules.GuiModule;
import myau.property.properties.*;
import myau.ui.clickgui.component.impl.Checkbox;
import myau.ui.clickgui.component.impl.Slider;
import myau.ui.clickgui.component.impl.ModeSelector;
import myau.module.Module;
import org.lwjgl.input.Keyboard;
import myau.ui.clickgui.component.impl.BindButton;
import myau.util.RenderUtil;
import myau.ui.clickgui.IntelliJTheme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import org.lwjgl.input.Mouse;
import net.minecraft.util.ChatAllowedCharacters;

public class ClickGuiScreen extends GuiScreen {
    private static ClickGuiScreen instance;

    private ArrayList<Frame> frames;
    private Component dragComp = null;
    private Frame dragFrame = null;
    private BindButton listenBindBtn = null;
    private Frame selFrame = null;
    
    public static Map<String, int[]> framePos = new HashMap<>();
    
    public static String lastSelCat = null;
    public static String lastSelMod = null;
    public static Map<String, Boolean> catExpandStates = new HashMap<>();
    public static int savedNavScroll = 0;
    public static int savedPropScroll = 0;
    
    private int navScroll = 0;
    private int propScroll = 0;
    private final int SCROLL_SPD = 10;
    
    private static final int NAV_WIDTH = 150;
    private static final int BORDER_THICK = 1;
    private static final int TITLE_HEIGHT = 30;

    private static final int MIN_WINDOW_WIDTH = 600; // 最小窗口宽度
    private static final int MIN_WINDOW_HEIGHT = 400; // 最小窗口高度

    private ModuleButton selMod = null;
    private Object selNavElem = null;

    private String tooltipTxt = null;
    private int tooltipX = 0;
    private int tooltipY = 0;
    
    private boolean dragWin = false;
    private int winDragX = 0;
    private int winDragY = 0;
    public int winX = 200;
    public int winY = 80;
    public int winWidth = 650;
    public int winHeight = 480;
    
    private long lastClick = 0;
    private ModuleButton lastClickModBtn = null;
    private static final long DBL_CLICK_TIME = 200;
    
    private String searchQ = "";
    private boolean searching = false;
    private static final int SEARCH_HEIGHT = 20;
    private static final int SEARCH_Y_OFF = 5;
    
    private boolean resizing = false;
    private int resizeDir = 0;
    private int resizeStartX, resizeStartY, initWidth, initHeight;
    private static final int RESIZE_SIZE = 6;
    
    private boolean enableTransBg;
    private double cornerRad;
    
    public static ClickGuiScreen getInstance() {
        return instance;
    }

    public Frame getSelectedFrame() {
        return selFrame;
    }
    
    public void setSelectedFrame(Frame frame) {
        this.selFrame = frame;
    }
    
    public ClickGuiScreen() {
        instance = this;
        this.frames = new ArrayList<>();
        
        GuiModule guiModule = (GuiModule) Myau.moduleManager.getModule("GuiSettings");
        if (guiModule != null) {
            this.enableTransBg = guiModule.enableTransparentBackground.getValue();
            this.cornerRad = guiModule.cornerRadius.getValue();
            
            if (guiModule.saveGuiState.getValue()) {
                this.winX = guiModule.windowX.getValue();
                this.winY = guiModule.windowY.getValue();
                this.winWidth = guiModule.windowWidth.getValue();
                this.winHeight = guiModule.windowHeight.getValue();
            }
        } else {
            this.enableTransBg = false;
            this.cornerRad = IntelliJTheme.CORNER_RADIUS;
        }
        
        if (Myau.moduleManager == null) {
            return;
        }
        
        int frameY = 5;
        
        for (Category category : Category.values()) {
            ArrayList<Module> modulesInCategory = Myau.moduleManager.getModulesInCategory(category);
            
            int frameX = 5;
            int frameWidth = NAV_WIDTH - 10;
            int frameHeight = 18;
            Frame frame = new Frame(category, frameX, frameY, frameWidth, frameHeight);
            
            frame.setWidth(frameWidth);
            
            this.frames.add(frame);
            frameY += 20;
        }
        
        if (guiModule != null && guiModule.saveGuiState.getValue()) {
            for (Frame frame : this.frames) {
                String categoryName = frame.getCategory().getName();
                if (framePos.containsKey(categoryName)) {
                    int[] position = framePos.get(categoryName);
                    frame.setX(position[0]);
                    frame.setY(position[1]);
                }
                if (catExpandStates.containsKey(categoryName)) {
                    boolean wasExpanded = catExpandStates.get(categoryName);
                    frame.setExtended(wasExpanded);
                }
            }
        }
        
        restoreSelectedState();
        
        this.navScroll = savedNavScroll;
        this.propScroll = savedPropScroll;
        
        if (selFrame == null && !frames.isEmpty()) {
            selFrame = frames.get(0);
        }
        if (selMod != null) {
            selNavElem = selMod;
        } else if (selFrame != null) {
            selNavElem = selFrame;
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tooltipTxt = null;

        if (this.enableTransBg) {
            Gui.drawRect(this.winX, this.winY, this.winX + this.winWidth, this.winY + this.winHeight, new Color(0, 0, 0, 100).getRGB());
        }

        renderTitleBar(mouseX, mouseY);

        int contentX = this.winX;
        int contentY = this.winY + TITLE_HEIGHT;
        int contentWidth = this.winWidth;
        int contentHeight = this.winHeight - TITLE_HEIGHT;

        Gui.drawRect(contentX, contentY, contentX + NAV_WIDTH, contentY + contentHeight,
                    IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND));

        Gui.drawRect(contentX + NAV_WIDTH, contentY, contentX + NAV_WIDTH + BORDER_THICK, contentY + contentHeight,
                    IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));

        renderSearchBar(mouseX, mouseY);

        String navTooltip = renderNavigationPanel(mouseX, mouseY);
        if (navTooltip != null) {
            tooltipTxt = navTooltip;
            tooltipX = mouseX;
            tooltipY = mouseY;
        }

        String propTooltip = renderModuleProperties(contentX + NAV_WIDTH + BORDER_THICK, contentY,
                                      contentWidth - NAV_WIDTH - BORDER_THICK, contentHeight, mouseX, mouseY);
        if (propTooltip != null) {
            tooltipTxt = propTooltip;
            tooltipX = mouseX;
            tooltipY = mouseY;
        }

        if (this.dragComp != null) {
            this.dragComp.updatePosition(mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        renderResizeHandles(mouseX, mouseY);

        if (tooltipTxt != null) {
            renderTooltip(tooltipTxt, tooltipX, tooltipY);
        }
    }
    
    private void renderSearchBar(int mouseX, int mouseY) {
        int searchBarX = this.winX + BORDER_THICK;
        int searchBarY = this.winY + TITLE_HEIGHT + SEARCH_Y_OFF;
        int searchBarWidth = NAV_WIDTH - BORDER_THICK * 2;
        int searchBarHeight = SEARCH_HEIGHT;

        RenderUtil.drawRoundedRect(searchBarX, searchBarY, searchBarWidth, searchBarHeight, (float) this.cornerRad, IntelliJTheme.getRGB(IntelliJTheme.TEXT_FIELD_BG));

        RenderUtil.drawRoundedRectOutline(searchBarX, searchBarY, searchBarWidth, searchBarHeight, (float) this.cornerRad, 1.0f, searching ? IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));

        mc.fontRendererObj.drawStringWithShadow("\uD83D\uDD0D", searchBarX + 5, searchBarY + searchBarHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR_SECONDARY));

        String displayText = searchQ.isEmpty() && !searching ? "Search..." : searchQ;
        int textColor = searchQ.isEmpty() && !searching ? IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR_SECONDARY) : IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR);
        mc.fontRendererObj.drawStringWithShadow(displayText, searchBarX + 5 + mc.fontRendererObj.getStringWidth("\uD83D\uDD0D "), searchBarY + searchBarHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2, textColor);

        if (searching && System.currentTimeMillis() / 500 % 2 == 0) {
            String currentText = searchQ.isEmpty() ? "" : searchQ;
            int cursorX = searchBarX + 5 + mc.fontRendererObj.getStringWidth("\uD83D\uDD0D " + currentText);
            Gui.drawRect(cursorX, searchBarY + searchBarHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2 - 1, cursorX + 1, searchBarY + searchBarHeight / 2 + mc.fontRendererObj.FONT_HEIGHT / 2 + 1, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
        }
    }
    
    private void renderResizeHandles(int mouseX, int mouseY) {
        int rbX = this.winX + this.winWidth - RESIZE_SIZE;
        int rbY = this.winY + this.winHeight - RESIZE_SIZE;
        boolean hoverRB = mouseX >= rbX && mouseX <= this.winX + this.winWidth &&
                          mouseY >= rbY && mouseY <= this.winY + this.winHeight;
        Gui.drawRect(rbX, rbY, this.winX + this.winWidth, this.winY + this.winHeight, 
                     hoverRB ? IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));

        int rX1 = this.winX + this.winWidth - RESIZE_SIZE;
        int rY1 = this.winY + RESIZE_SIZE;
        int rX2 = this.winX + this.winWidth;
        int rY2 = this.winY + this.winHeight - RESIZE_SIZE;
        boolean hoverR = mouseX >= rX1 && mouseX <= rX2 &&
                         mouseY >= rY1 && mouseY <= rY2;
        Gui.drawRect(rX1, rY1, rX2, rY2,
                     hoverR ? IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));

        int bX1 = this.winX + RESIZE_SIZE;
        int bY1 = this.winY + this.winHeight - RESIZE_SIZE;
        int bX2 = this.winX + this.winWidth - RESIZE_SIZE;
        int bY2 = this.winY + this.winHeight;
        boolean hoverB = mouseX >= bX1 && mouseX <= bX2 &&
                         mouseY >= bY1 && mouseY <= bY2;
        Gui.drawRect(bX1, bY1, bX2, bY2,
                     hoverB ? IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));
    }

    private void renderTooltip(String text, int mouseX, int mouseY) {
        if (text == null || text.isEmpty()) return;

        FontRenderer fr = this.mc.fontRendererObj;
        int textWidth = fr.getStringWidth(text);
        int textHeight = fr.FONT_HEIGHT;

        int padding = 3;
        int tooltipX = mouseX + 10;
        int tooltipY = mouseY - 5;

        if (tooltipX + textWidth + padding * 2 > this.width) {
            tooltipX = this.width - textWidth - padding * 2;
        }

        Gui.drawRect(tooltipX, tooltipY, tooltipX + textWidth + padding * 2, tooltipY + textHeight + padding * 2, IntelliJTheme.getRGB(IntelliJTheme.TOOLTIP_BACKGROUND));
        RenderUtil.drawRectOutline(tooltipX, tooltipY, textWidth + padding * 2, textHeight + padding * 2, 1.0f, IntelliJTheme.getRGB(IntelliJTheme.TOOLTIP_BORDER));
        fr.drawStringWithShadow(text, tooltipX + padding, tooltipY + padding, IntelliJTheme.getRGB(IntelliJTheme.TOOLTIP_TEXT_COLOR));
    }

    private void renderTitleBar(int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        
        boolean isHoveringTitleBar = mouseX >= this.winX && mouseX <= this.winX + this.winWidth && 
                                     mouseY >= this.winY && mouseY <= this.winY + TITLE_HEIGHT;
        
        int titleBarColor =
                isHoveringTitleBar ?
                IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_COLOR) : // Accent color for hover
                IntelliJTheme.getRGB(IntelliJTheme.BACKGROUND_COLOR); // Primary background for non-hover
        
        Gui.drawRect(this.winX, this.winY, this.winX + this.winWidth, this.winY + TITLE_HEIGHT, titleBarColor);
        Gui.drawRect(this.winX, this.winY + TITLE_HEIGHT - 1, this.winX + this.winWidth, this.winY + TITLE_HEIGHT,
                     IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));
        
        int iconX = this.winX + 8;
        int iconY = this.winY + (TITLE_HEIGHT - 16) / 2;
        
        Gui.drawRect(iconX, iconY, iconX + 16, iconY + 16, IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_COLOR));
        Gui.drawRect(iconX + 1, iconY + 1, iconX + 15, iconY + 15, IntelliJTheme.getRGB(IntelliJTheme.BACKGROUND_COLOR));
        
        fr.drawString("M", iconX + 5, iconY + 4, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
        
        String title = "OpenMyau - ClickGUI Configuration";
        int titleX = iconX + 20;
        int titleY = this.winY + (TITLE_HEIGHT - fr.FONT_HEIGHT) / 2;
        fr.drawString(title, titleX, titleY, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
        
        renderWindowControls(mouseX, mouseY);
    }
    
    private void renderWindowControls(int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        
        int buttonSize = 20;
        int buttonY = this.winY + (TITLE_HEIGHT - buttonSize) / 2;
        
        int closeX = this.winX + this.winWidth - buttonSize - 5;
        boolean isHoveringClose = mouseX >= closeX && mouseX <= closeX + buttonSize && 
                                 mouseY >= buttonY && mouseY <= buttonY + buttonSize;
        
        int closeColor = isHoveringClose ? 
            new Color(255, 96, 96).getRGB() : 
            IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR);
        
        Gui.drawRect(closeX, buttonY, closeX + buttonSize, buttonY + buttonSize, closeColor);
        fr.drawString("×", closeX + 7, buttonY + 6, 0xFFFFFF);
        
        int maximizeX = closeX - buttonSize - 2;
        boolean isHoveringMaximize = mouseX >= maximizeX && mouseX <= maximizeX + buttonSize && 
                                    mouseY >= buttonY && mouseY <= buttonY + buttonSize;
        
        int maximizeColor = isHoveringMaximize ? 
            IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR) : 
            IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR);
        
        Gui.drawRect(maximizeX, buttonY, maximizeX + buttonSize, buttonY + buttonSize, maximizeColor);
        fr.drawString("□", maximizeX + 6, buttonY + 6, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
        
        int minimizeX = maximizeX - buttonSize - 2;
        boolean isHoveringMinimize = mouseX >= minimizeX && mouseX <= minimizeX + buttonSize && 
                                    mouseY >= buttonY && mouseY <= buttonY + buttonSize;
        
        int minimizeColor = isHoveringMinimize ? 
            IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR) : 
            IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR);
        
        Gui.drawRect(minimizeX, buttonY, minimizeX + buttonSize, buttonY + buttonSize, minimizeColor);
        fr.drawString("−", minimizeX + 7, buttonY + 6, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
    }
    
    private String renderNavigationPanel(int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        int categoryHeight = 20;
        int contentAreaX = this.winX;
        int contentAreaY = this.winY + TITLE_HEIGHT + SEARCH_HEIGHT + SEARCH_Y_OFF * 2;
        int contentAreaWidth = NAV_WIDTH;
        int contentAreaHeight = this.winHeight - TITLE_HEIGHT - SEARCH_HEIGHT - SEARCH_Y_OFF * 2;

        String hoveredTooltip = null;

        int totalNavigationContentHeight = 0;
        for (Frame frame : frames) {
            totalNavigationContentHeight += categoryHeight;
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                for (ModuleButton moduleButton : moduleButtons) {
                    if (moduleButton.isVisible()) {
                        totalNavigationContentHeight += 15;
                    }
                }
            }
        }

        int maxScrollOffset = Math.max(0, totalNavigationContentHeight - contentAreaHeight);
        this.navScroll = Math.max(0, Math.min(maxScrollOffset, this.navScroll));

        int navigationScrollbarX = contentAreaX + NAV_WIDTH - 5;
        int navigationScrollbarWidth = 3;
        renderScrollbar(navigationScrollbarX, contentAreaY, navigationScrollbarWidth, contentAreaHeight, totalNavigationContentHeight, this.navScroll, mouseX, mouseY);

        RenderUtil.scissor(contentAreaX + 5, contentAreaY, NAV_WIDTH - 10, contentAreaHeight);
        
        int currentY = contentAreaY + 5 - this.navScroll;

        if (frames.isEmpty()) {
            String debugInfo = "No frames found";
            if (currentY >= contentAreaY && currentY < contentAreaY + contentAreaHeight) {
                fr.drawString(debugInfo, contentAreaX + 15, currentY, 0xFFFFFF);
            }
            RenderUtil.releaseScissor();
            return null;
        }
        
        for (Frame frame : frames) {
            if (currentY + categoryHeight > contentAreaY && currentY < contentAreaY + contentAreaHeight) {
                int categoryBgColor = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
                boolean isCategoryHovered = mouseX >= contentAreaX + 5 && mouseX <= contentAreaX + NAV_WIDTH - 5 &&
                                            mouseY >= currentY && mouseY <= currentY + categoryHeight;

                if (isCategoryHovered) {
                    categoryBgColor = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);
                }
                if (frame == this.selFrame) {
                    categoryBgColor = IntelliJTheme.getRGB(IntelliJTheme.SELECTED_BG_COLOR);
                }
                Gui.drawRect(contentAreaX + 5, currentY, contentAreaX + NAV_WIDTH - 5, currentY + categoryHeight, categoryBgColor);
                
                if (frame == this.selNavElem) {
                    RenderUtil.drawRectOutline(contentAreaX + 5, currentY, NAV_WIDTH - 10, categoryHeight, 1.0f, IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR));
                }
                
                String categoryName = frame.getCategory().getName();
                String packageName = "myau.modules." + categoryName.toLowerCase();
                fr.drawString(packageName, contentAreaX + 15, currentY + 6, IntelliJTheme.getRGB(IntelliJTheme.TYPE_VALUE_COLOR));
                
                String indicator = frame.isExtended() ? "▼" : "▶";
                fr.drawString(indicator, contentAreaX + NAV_WIDTH - 15, currentY + 6, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
            }
            
            currentY += categoryHeight;
            
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                
                if (!moduleButtons.isEmpty()) {
                    for (ModuleButton moduleButton : moduleButtons) {
                        if (moduleButton.isVisible() && currentY + 15 > contentAreaY && currentY < contentAreaY + contentAreaHeight) {
                            int moduleBgColor = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
                            boolean isModuleHovered = mouseX >= contentAreaX + 10 && mouseX <= contentAreaX + NAV_WIDTH - 5 &&
                                                      mouseY >= currentY && mouseY <= currentY + 15;

                            if (isModuleHovered) {
                                moduleBgColor = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);
                                hoveredTooltip = moduleButton.getModule().getDescription();
                            }
                            if (moduleButton == this.selMod) {
                                moduleBgColor = IntelliJTheme.getRGB(IntelliJTheme.SELECTED_BG_COLOR);
                            }
                            Gui.drawRect(contentAreaX + 10, currentY, contentAreaX + NAV_WIDTH - 5, currentY + 15, moduleBgColor);
                            
                            String moduleName = moduleButton.getModule().getName();
                            String className = moduleName + ".java";
                            
                            int textColor = moduleButton.getModule().isEnabled() ? 
                                IntelliJTheme.getRGB(IntelliJTheme.VARIABLE_NAME_COLOR) : 
                                IntelliJTheme.getRGB(IntelliJTheme.DISABLED_TEXT_COLOR);
                            
                            fr.drawString(className, contentAreaX + 20, currentY + 4, textColor);

                            if (moduleButton == this.selNavElem) {
                                RenderUtil.drawRectOutline(contentAreaX + 10, currentY, NAV_WIDTH - 15, 15, 1.0f, IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR));
                            }
                        }
                        
                        currentY += 15;
                    }
                }
            }
        }
        RenderUtil.releaseScissor();
        return hoveredTooltip;
    }
    
    private String renderModuleProperties(int x, int y, int width, int height, int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        String hoveredTooltip = null;
        
        Gui.drawRect(x, y, x + width, y + height, IntelliJTheme.getRGB(IntelliJTheme.BACKGROUND_COLOR));
        
        int lineNumberWidth = 40;
        Gui.drawRect(x, y, x + lineNumberWidth, y + height, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_BG_COLOR));
        
        Gui.drawRect(x + lineNumberWidth, y, x + lineNumberWidth + 1, y + height, IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));
        
        Gui.drawRect(x + lineNumberWidth + 1, y, x + width, y + height, IntelliJTheme.getRGB(IntelliJTheme.CODE_BG_COLOR));
        
        int totalPropertiesContentHeight = 0;
        int initialContentOffset = 10;
        int linesBeforeProperties = 7;
        totalPropertiesContentHeight += (linesBeforeProperties + 1) * 15;

        if (this.selMod != null) {
            ArrayList<Component> subComponents = this.selMod.getSubComponents();
            if (subComponents != null && !subComponents.isEmpty()) {
                totalPropertiesContentHeight += subComponents.size() * 15;
            } else {
                totalPropertiesContentHeight += 15;
            }
            totalPropertiesContentHeight += 15;
        } else {
            totalPropertiesContentHeight += 2 * 15;
        }
        totalPropertiesContentHeight += initialContentOffset * 2;

        int propertiesScrollbarX = x + width - 5;
        int propertiesScrollbarWidth = 3;
        renderScrollbar(propertiesScrollbarX, y, propertiesScrollbarWidth, height, totalPropertiesContentHeight, this.propScroll, mouseX, mouseY);

        RenderUtil.scissor(x + lineNumberWidth + 1, y, width - lineNumberWidth - 1, height);
        
        int contentY = y + 10 - this.propScroll;
        int lineNumber = 1;
        
        if (this.selMod != null) {
            Module module = this.selMod.getModule();
            
            String packageDeclaration = "package myau.module.modules;";
            fr.drawStringWithShadow(packageDeclaration, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            contentY += 15;
            
            String[] imports = {"import myau.module.Module;", "import myau.module.Category;", "import myau.property.properties.*;"};
            
            for (String importStmt : imports) {
                fr.drawStringWithShadow(importStmt, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
                contentY += 15;
            }
            
            contentY += 20;

            String classDeclaration = "public class " + module.getName() + " extends Module {";
            fr.drawStringWithShadow("public class ", x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
            fr.drawStringWithShadow(module.getName(), x + lineNumberWidth + 10 + fr.getStringWidth("public class "), contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
            fr.drawStringWithShadow(" extends ", x + lineNumberWidth + 10 + fr.getStringWidth("public class " + module.getName()), contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
            fr.drawStringWithShadow("Module {", x + lineNumberWidth + 10 + fr.getStringWidth("public class " + module.getName() + " extends "), contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            contentY += 15;
            
            contentY += 5;
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            contentY += 15;
            
            ArrayList<Component> subComponents = this.selMod.getSubComponents();
            if (subComponents != null && !subComponents.isEmpty()) {
                for (Component component : subComponents) {
                    Object property = null;
                    
                    if (component instanceof Checkbox) {
                        Checkbox checkbox = (Checkbox) component;
                        try {
                            java.lang.reflect.Field field = checkbox.getClass().getDeclaredField("booleanProperty");
                            field.setAccessible(true);
                            property = field.get(checkbox);
                        } catch (Exception e) {
                            continue;
                        }
                    } else if (component instanceof Slider) {
                        Slider slider = (Slider) component;
                        try {
                            java.lang.reflect.Field field = slider.getClass().getDeclaredField("property");
                            field.setAccessible(true);
                            property = field.get(slider);
                        } catch (Exception e) {
                            continue;
                        }
                    } else if (component instanceof ModeSelector) {
                        ModeSelector modeSelector = (ModeSelector) component;
                        try {
                            java.lang.reflect.Field field = modeSelector.getClass().getDeclaredField("modeProperty");
                            field.setAccessible(true);
                            property = field.get(modeSelector);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                    
                    if (property != null) {
                        String propertyName = "";
                        String propertyValue = "";
                        String propertyType = "";
                        String propertyDescription = "";

                        int propertyLineY = contentY;
                        int propertyLineHeight = 15;
                        boolean isPropertyLineHovered = mouseX >= x + lineNumberWidth + 1 && mouseX <= x + width &&
                                                        mouseY >= propertyLineY && mouseY <= propertyLineY + propertyLineHeight;

                        if (isPropertyLineHovered) {
                            Gui.drawRect(x + lineNumberWidth + 1, propertyLineY, x + width, propertyLineY + propertyLineHeight, IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR));
                            hoveredTooltip = propertyDescription;
                        }
                        
                        if (property instanceof BooleanProperty) {
                            BooleanProperty boolProp = (BooleanProperty) property;
                            propertyName = boolProp.getName();
                            propertyValue = boolProp.getValue() ? "true" : "false";
                            propertyType = "BooleanProperty";
                            propertyDescription = "(Click or scroll to toggle)";
                        } else if (property instanceof IntProperty) {
                            IntProperty intProp = (IntProperty) property;
                            propertyName = intProp.getName();
                            propertyValue = String.valueOf(intProp.getValue());
                            propertyType = "IntProperty";
                            propertyDescription = "(Hover and scroll to adjust ±1)";
                        } else if (property instanceof FloatProperty) {
                            FloatProperty floatProp = (FloatProperty) property;
                            propertyName = floatProp.getName();
                            propertyValue = String.valueOf(floatProp.getValue());
                            propertyType = "FloatProperty";
                            propertyDescription = "(Hover and scroll to adjust ±0.1)";
                        } else if (property instanceof PercentProperty) {
                            PercentProperty percentProp = (PercentProperty) property;
                            propertyName = percentProp.getName();
                            propertyValue = String.valueOf(percentProp.getValue());
                            propertyType = "PercentProperty";
                            propertyDescription = "(Hover and scroll to adjust ±1%)";
                        } else if (property instanceof ModeProperty) {
                            ModeProperty modeProp = (ModeProperty) property;
                            propertyName = modeProp.getName();
                            propertyValue = "\"" + modeProp.getValue() + "\"";
                            propertyType = "ModeProperty";
                            propertyDescription = "(Click or scroll to cycle)";
                        }
                        
                        if (!propertyName.isEmpty()) {
                            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
                            
                            int textX = x + lineNumberWidth + 10;
                            
                            String tabIndent = "    ";
                            fr.drawStringWithShadow(tabIndent, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
                            textX += fr.getStringWidth(tabIndent);
                            
                            fr.drawStringWithShadow("public final ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                            textX += fr.getStringWidth("public final ");
                            
                            fr.drawStringWithShadow(propertyType + " ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
                            textX += fr.getStringWidth(propertyType + " ");
                            
                            fr.drawStringWithShadow(propertyName, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.VARIABLE_NAME_COLOR));
                            textX += fr.getStringWidth(propertyName);
                            
                            fr.drawStringWithShadow(" = ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                            textX += fr.getStringWidth(" = ");
                            fr.drawStringWithShadow("new ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                            textX += fr.getStringWidth("new ");
                            
                            fr.drawStringWithShadow(propertyType + "(\"", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
                            textX += fr.getStringWidth(propertyType + "(\"");
                            
                            fr.drawStringWithShadow(propertyName, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.STRING_COLOR));
                            textX += fr.getStringWidth(propertyName);
                            
                            fr.drawStringWithShadow("\", ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
                            textX += fr.getStringWidth("\", ");
                            
                            if (property instanceof BooleanProperty) {
                                fr.drawStringWithShadow(propertyValue, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.LITERAL_COLOR));
                            } else {
                                fr.drawStringWithShadow(propertyValue, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.NUMBER_COLOR));
                            }
                            textX += fr.getStringWidth(propertyValue);
                            
                            fr.drawStringWithShadow(");", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));

                            contentY += 15;
                            
                            if (contentY > y + height - 20) {
                                break;
                            }
                        }
                    }
                }
            } else {
                String comment = "// No configurable properties for this module";
                fr.drawStringWithShadow(comment, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.COMMENT_COLOR));
                fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            }
            
            contentY += 15;
            fr.drawStringWithShadow("}", x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            
        } else {
            String[] welcomeText = {"// Welcome to OpenMyau", "// Click on modules to configure settings."};
            
            for (String line : welcomeText) {
                fr.drawStringWithShadow(line, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.COMMENT_COLOR));
                fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
                contentY += 15;
            }
        }
        RenderUtil.releaseScissor();
        return hoveredTooltip;
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int mouseX = Mouse.getEventX();
        int mouseY = Mouse.getEventY();

        int wheel = Mouse.getEventDWheel();

        if (wheel != 0) {
            int scrollAmount = wheel > 0 ? -SCROLL_SPD : SCROLL_SPD;

            boolean isInNavigation = mouseX >= this.winX && mouseX <= this.winX + NAV_WIDTH &&
                                   mouseY >= this.winY + TITLE_HEIGHT && mouseY <= this.winY + this.winHeight;

            if (isInNavigation) {
                int maxScrollOffset = calculateMaxNavigationScrollOffset();
                this.navScroll += scrollAmount;
                this.navScroll = Math.max(0, Math.min(maxScrollOffset, this.navScroll));
            } else {
                if (this.selMod != null) {
                    handlePropertyScrollAdjustment(mouseX, mouseY, wheel);
                } else {
                    this.propScroll += scrollAmount;
                    this.propScroll = Math.max(0, this.propScroll);
                }
            }
        }
    }
    
    @Override
    public void handleInput() throws IOException {
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        }
        
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }

        super.handleInput();

        for (Frame frame : this.frames) {
            Component component = frame.handleInput((char) Keyboard.getEventCharacter(), Keyboard.getEventKey());
            if (component != null) {
                if (component instanceof BindButton && ((BindButton) component).isListeningForKey()) {
                    this.listenBindBtn = (BindButton) component;
                } else {
                    this.dragComp = component;
                }
                return;
            }
        }
    }
    
    private void handlePropertyScrollAdjustment(int mouseX, int mouseY, int wheel) {
        if (this.selMod == null) return;

        ArrayList<Component> subComponents = this.selMod.getSubComponents();
        if (subComponents == null || subComponents.isEmpty()) return;

        int propertiesX = this.winX + NAV_WIDTH + BORDER_THICK;
        int propertiesY = this.winY + TITLE_HEIGHT;
        int propertiesWidth = this.winWidth - NAV_WIDTH - BORDER_THICK;
        int propertiesHeight = this.winHeight - TITLE_HEIGHT;

        if (mouseX < propertiesX || mouseY < propertiesY) return;
        if (mouseX > propertiesX + propertiesWidth || mouseY > propertiesY + propertiesHeight) return;

        int contentStartY = propertiesY + 10 - this.propScroll;
        int currentY = contentStartY + 15 * 7; // Adjust for module name and description

        boolean propertyAdjusted = false;
        for (Component component : subComponents) {
            if (mouseX >= propertiesX + 5 && mouseX <= propertiesX + propertiesWidth - 5 &&
                mouseY >= currentY && mouseY <= currentY + component.getHeight()) {
                if (component.mouseScrolled(mouseX, mouseY, wheel)) {
                    propertyAdjusted = true;
                    break;
                }
            }
            currentY += component.getHeight();
        }

        if (!propertyAdjusted) {
            int scrollAmount = wheel > 0 ? -SCROLL_SPD : SCROLL_SPD;
            this.propScroll += scrollAmount;
            int maxScrollOffset = calculateMaxPropertiesScrollOffset();
            this.propScroll = Math.max(0, Math.min(maxScrollOffset, this.propScroll));
        }
    }
    
    private void adjustPropertyValue(Component component, boolean increase) {
        if (component instanceof Checkbox) {
            Checkbox checkbox = (Checkbox) component;
            try {
                java.lang.reflect.Field field = checkbox.getClass().getDeclaredField("booleanProperty");
                field.setAccessible(true);
                BooleanProperty property = (BooleanProperty) field.get(checkbox);
                property.setValue(!property.getValue());
            } catch (Exception e) {
            }
        } else if (component instanceof Slider) {
            Slider slider = (Slider) component;
            try {
                java.lang.reflect.Field field = slider.getClass().getDeclaredField("property");
                field.setAccessible(true);
                Object property = field.get(slider);
                
                if (property instanceof IntProperty) {
                    IntProperty intProp = (IntProperty) property;
                    int currentValue = intProp.getValue();
                    int newValue = increase ? currentValue + 1 : currentValue - 1;
                    newValue = Math.max(intProp.getMinimum(), Math.min(intProp.getMaximum(), newValue));
                    intProp.setValue(newValue);
                } else if (property instanceof FloatProperty) {
                    FloatProperty floatProp = (FloatProperty) property;
                    float currentValue = floatProp.getValue();
                    float newValue = increase ? currentValue + 0.1f : currentValue - 0.1f;
                    newValue = Math.max(floatProp.getMinimum(), Math.min(floatProp.getMaximum(), newValue));
                    floatProp.setValue(newValue);
                } else if (property instanceof PercentProperty) {
                    PercentProperty percentProp = (PercentProperty) property;
                    int currentValue = percentProp.getValue();
                    int newValue = increase ? currentValue + 1 : currentValue - 1;
                    newValue = Math.max(0, Math.min(100, newValue));
                    percentProp.setValue(newValue);
                }
            } catch (Exception e) {
            }
        } else if (component instanceof ModeSelector) {
            ModeSelector modeSelector = (ModeSelector) component;
            try {
                java.lang.reflect.Field field = modeSelector.getClass().getDeclaredField("modeProperty");
                field.setAccessible(true);
                ModeProperty property = (ModeProperty) field.get(modeSelector);
                
                int currentValue = property.getValue();
                int modeCount = property.getValuePrompt().split(", ").length;
                int newValue;
                if (increase) {
                    newValue = (currentValue + 1) % modeCount;
                } else {
                    newValue = (currentValue - 1 + modeCount) % modeCount;
                }
                property.setValue(newValue);
            } catch (Exception e) {
            }
        }
    }
    
    private int calculateMaxNavigationScrollOffset() {
        int totalHeight = 0;
        int categoryHeight = 20;
        
        for (Frame frame : frames) {
            totalHeight += categoryHeight;
            
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                for (ModuleButton moduleButton : moduleButtons) {
                    if (moduleButton.isVisible()) {
                        totalHeight += 15;
                    }
                }
            }
        }
        
        int availableHeight = winHeight - (TITLE_HEIGHT + SEARCH_HEIGHT + SEARCH_Y_OFF * 2 + 5);
        return Math.max(0, totalHeight - availableHeight);
    }

    private int calculateMaxPropertiesScrollOffset() {
        if (this.selMod == null) return 0;

        int totalHeight = 15 * 7; // Account for module name and description lines
        ArrayList<Component> subComponents = this.selMod.getSubComponents();
        if (subComponents != null) {
            for (Component component : subComponents) {
                totalHeight += component.getHeight();
            }
        }

        int availableHeight = this.winHeight - (TITLE_HEIGHT + SEARCH_HEIGHT + SEARCH_Y_OFF * 2 + 5);
        return Math.max(0, totalHeight - availableHeight);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.listenBindBtn != null) {
            this.listenBindBtn.keyTyped(typedChar, keyCode);
            if (!this.listenBindBtn.isListeningForKey()) {
                this.listenBindBtn = null;
            }
            return;
        }

        // Special handling for scroll in navigation and properties panels
        if (keyCode == Keyboard.KEY_UP) {
            navigateUp();
            return;
        }
        if (keyCode == Keyboard.KEY_DOWN) {
            navigateDown();
            return;
        }

        if (this.searching) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (!this.searchQ.isEmpty()) {
                    this.searchQ = this.searchQ.substring(0, this.searchQ.length() - 1);
                }
            } else if (keyCode == Keyboard.KEY_RETURN) {
                this.searching = false;
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                this.searchQ += typedChar;
            }
            filterModules();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
        
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.listenBindBtn != null) {
            this.listenBindBtn.keyTyped((char) 0, Keyboard.KEY_NONE);
            this.listenBindBtn = null;
        }

        for (Frame frame : this.frames) {
            Component clickedEntity = frame.mouseClicked(mouseX, mouseY, mouseButton);
            if (clickedEntity != null) {
                if (clickedEntity instanceof Frame) {
                    this.dragFrame = (Frame) clickedEntity;
                } else if (clickedEntity instanceof BindButton && ((BindButton) clickedEntity).isListeningForKey()) {
                    this.listenBindBtn = (BindButton) clickedEntity;
                } else {
                    this.dragComp = clickedEntity;
                }
                return;
            }
        }

        // Search bar click handling
        int searchBarX = this.winX + BORDER_THICK;
        int searchBarY = this.winY + TITLE_HEIGHT + SEARCH_Y_OFF;
        int searchBarWidth = NAV_WIDTH - BORDER_THICK * 2;
        int searchBarHeight = SEARCH_HEIGHT;

        if (mouseX >= searchBarX && mouseX <= searchBarX + searchBarWidth &&
            mouseY >= searchBarY && mouseY <= searchBarY + searchBarHeight) {
            this.searching = true;
        } else {
            this.searching = false;
        }

        if (mouseButton == 0 &&
            mouseX >= this.winX && mouseX <= this.winX + this.winWidth &&
            mouseY >= this.winY && mouseY <= this.winY + TITLE_HEIGHT) {
            this.dragWin = true;
            this.winDragX = mouseX - this.winX;
            this.winDragY = mouseY - this.winY;
            return;
        }

        if (mouseButton != 0) return;

        int rbX = this.winX + this.winWidth - RESIZE_SIZE;
        int rbY = this.winY + this.winHeight - RESIZE_SIZE;
        if (mouseX >= rbX && mouseX <= this.winX + this.winWidth &&
            mouseY >= rbY && mouseY <= this.winY + this.winHeight) {
            this.resizing = true;
            this.resizeDir = 3;
            this.resizeStartX = mouseX;
            this.resizeStartY = mouseY;
            this.initWidth = this.winWidth;
            this.initHeight = this.winHeight;
            return; // Return true as resize event is handled
        }

        int rX1 = this.winX + this.winWidth - RESIZE_SIZE;
        int rY1 = this.winY + RESIZE_SIZE;
        int rX2 = this.winX + this.winWidth;
        int rY2 = this.winY + this.winHeight - RESIZE_SIZE;
        if (mouseX >= rX1 && mouseX <= rX2 &&
            mouseY >= rY1 && mouseY <= rY2) {
            this.resizing = true;
            this.resizeDir = 1;
            this.resizeStartX = mouseX;
            this.initWidth = this.winWidth;
            return; // Return true as resize event is handled
        }

        int bX1 = this.winX + RESIZE_SIZE;
        int bY1 = this.winY + this.winHeight - RESIZE_SIZE;
        int bX2 = this.winX + this.winWidth - RESIZE_SIZE;
        int bY2 = this.winY + this.winHeight;
        if (mouseX >= bX1 && mouseX <= bX2 &&
            mouseY >= bY1 && mouseY <= bY2) {
            this.resizing = true;
            this.resizeDir = 2;
            this.resizeStartY = mouseY;
            this.initHeight = this.winHeight;
            return; // Return true as resize event is handled
        }

        if (handleNavigationPanelClick(mouseX, mouseY, mouseButton)) return;
        if (handlePropertiesPanelClick(mouseX, mouseY, mouseButton)) return;

        // Handle window close, maximize, minimize buttons
        int buttonSize = 20;
        int buttonY = this.winY + (TITLE_HEIGHT - buttonSize) / 2;

        int closeX = this.winX + this.winWidth - buttonSize - 5;
        if (mouseX >= closeX && mouseX <= closeX + buttonSize &&
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            // Close button clicked
            this.mc.displayGuiScreen(null);
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
            return;
        }

        int maximizeX = closeX - buttonSize - 2;
        if (mouseX >= maximizeX && mouseX <= maximizeX + buttonSize &&
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            // Maximize button clicked (Toggle fullscreen or restore)
            // For now, let's just resize to a default large size or restore original
            if (this.winWidth == 1920 && this.winHeight == 1080) { // Assuming 1920x1080 is max
                this.winX = 200; // Restore to default
                this.winY = 80;
                this.winWidth = 650;
                this.winHeight = 480;
            } else {
                this.winX = 0;
                this.winY = 0;
                this.winWidth = this.width;
                this.winHeight = this.height;
            }
            return;
        }

        int minimizeX = maximizeX - buttonSize - 2;
        if (mouseX >= minimizeX && mouseX <= minimizeX + buttonSize &&
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            // Minimize button clicked (Minimize to taskbar or hide)
            // For Minecraft GUI, usually just close the GUI
            this.mc.displayGuiScreen(null);
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
            return;
        }

    }
    
    private boolean handleWindowResizeClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;

        int rbX = this.winX + this.winWidth - RESIZE_SIZE;
        int rbY = this.winY + this.winHeight - RESIZE_SIZE;
        if (mouseX >= rbX && mouseX <= this.winX + this.winWidth &&
            mouseY >= rbY && mouseY <= this.winY + this.winHeight) {
            this.resizing = true;
            this.resizeDir = 3;
            this.resizeStartX = mouseX;
            this.resizeStartY = mouseY;
            this.initWidth = this.winWidth;
            this.initHeight = this.winHeight;
            return true;
        }

        int rX1 = this.winX + this.winWidth - RESIZE_SIZE;
        int rY1 = this.winY + RESIZE_SIZE;
        int rX2 = this.winX + this.winWidth;
        int rY2 = this.winY + this.winHeight - RESIZE_SIZE;
        if (mouseX >= rX1 && mouseX <= rX2 &&
            mouseY >= rY1 && mouseY <= rY2) {
            this.resizing = true;
            this.resizeDir = 1;
            this.resizeStartX = mouseX;
            this.initWidth = this.winWidth;
            return true;
        }

        int bX1 = this.winX + RESIZE_SIZE;
        int bY1 = this.winY + this.winHeight - RESIZE_SIZE;
        int bX2 = this.winX + this.winWidth - RESIZE_SIZE;
        int bY2 = this.winY + this.winHeight;
        if (mouseX >= bX1 && mouseX <= bX2 &&
            mouseY >= bY1 && mouseY <= bY2) {
            this.resizing = true;
            this.resizeDir = 2;
            this.resizeStartY = mouseY;
            this.initHeight = this.winHeight;
            return true;
        }
        
        return false;
    }
    
    private boolean handleNavigationPanelClick(int mouseX, int mouseY, int mouseButton) {
        int contentAreaX = this.winX;
        int contentAreaY = this.winY + TITLE_HEIGHT + SEARCH_HEIGHT + SEARCH_Y_OFF * 2;
        int contentAreaHeight = this.winHeight - TITLE_HEIGHT - SEARCH_HEIGHT - SEARCH_Y_OFF * 2;

        if (mouseX >= contentAreaX && mouseX <= contentAreaX + NAV_WIDTH &&
            mouseY >= contentAreaY && mouseY <= contentAreaY + contentAreaHeight) {

            int relativeMouseY = mouseY - contentAreaY + this.navScroll;
            int currentY = 5;
            int categoryHeight = 20;

            for (Frame frame : this.frames) {
                if (mouseX >= contentAreaX + 5 && mouseX <= contentAreaX + NAV_WIDTH - 5 &&
                    relativeMouseY >= currentY && relativeMouseY <= currentY + categoryHeight) {

                    if (mouseButton == 0) {
                        this.selFrame = frame;
                        this.selMod = null;
                    } else if (mouseButton == 1) {
                        frame.setExtended(!frame.isExtended());
                    }
                    this.selNavElem = frame;
                    return true;
                }
                currentY += categoryHeight;

                if (frame.isExtended()) {
                    for (ModuleButton moduleButton : frame.getModuleButtons()) {
                        if (moduleButton.isVisible() &&
                            mouseX >= contentAreaX + 10 && mouseX <= contentAreaX + NAV_WIDTH - 5 &&
                            relativeMouseY >= currentY && relativeMouseY <= currentY + 15) {

                            if (mouseButton == 0) {
                                long currentTime = System.currentTimeMillis();
                                if (moduleButton == this.lastClickModBtn &&
                                    (currentTime - this.lastClick) < DBL_CLICK_TIME) {
                                    moduleButton.getModule().toggle();
                                    this.lastClickModBtn = null;
                                } else {
                                    this.selMod = moduleButton;
                                    this.selFrame = frame;
                                }
                                this.lastClick = currentTime;
                                this.lastClickModBtn = moduleButton;
                            }
                            this.selNavElem = moduleButton;
                            return true;
                        }
                        currentY += 15;
                    }
                }
            }
        }
        return false;
    }
    
    private void handleModuleButtonClick(ModuleButton moduleButton, Frame frame, int mouseButton) {
        if (mouseButton == 0) {
            long currentTime = System.currentTimeMillis();
            if (moduleButton == lastClickModBtn && 
                (currentTime - lastClick) < DBL_CLICK_TIME) {
                moduleButton.getModule().toggle();
                lastClickModBtn = null;
            } else {
                this.selMod = moduleButton;
                this.selFrame = frame;
            }
            lastClick = currentTime;
            lastClickModBtn = moduleButton;
        }
        this.selNavElem = moduleButton;
    }
    
    private boolean handlePropertiesPanelClick(int mouseX, int mouseY, int mouseButton) {
        int propertiesX = this.winX + NAV_WIDTH + BORDER_THICK;
        int propertiesY = this.winY + TITLE_HEIGHT;
        int propertiesWidth = this.winWidth - NAV_WIDTH - BORDER_THICK;
        int propertiesHeight = this.winHeight - TITLE_HEIGHT;

        if (mouseX >= propertiesX && mouseX <= propertiesX + propertiesWidth &&
            mouseY >= propertiesY && mouseY <= propertiesY + propertiesHeight &&
            this.selMod != null) {

            ArrayList<Component> subComponents = this.selMod.getSubComponents();
            if (subComponents == null || subComponents.isEmpty()) return false;

            int contentStartY = propertiesY + 10 - this.propScroll;
            int currentY = contentStartY + 15 * 7; // Adjust for module name and description

            for (Component component : subComponents) {
                if (component.isVisible() &&
                    mouseX >= propertiesX + 5 && mouseX <= propertiesX + propertiesWidth - 5 &&
                    mouseY >= currentY && mouseY <= currentY + component.getHeight()) {

                    if (mouseButton == 0) {
                        adjustPropertyValue(component, true);
                    } else if (mouseButton == 1) {
                        adjustPropertyValue(component, false);
                    }
                    return true;
                }
                currentY += component.getHeight();
            }
        }
        return false;
    }
    
    private boolean handleWindowControlsClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;
        
        int buttonSize = 20;
        int buttonY = winY + (TITLE_HEIGHT - buttonSize) / 2;
        
        int closeX = winX + winWidth - buttonSize - 5;
        if (mouseX >= closeX && mouseX <= closeX + buttonSize && 
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            this.mc.displayGuiScreen(null);
            return true;
        }
        
        int maximizeX = closeX - buttonSize - 2;
        if (mouseX >= maximizeX && mouseX <= maximizeX + buttonSize && 
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            return true;
        }
        
        int minimizeX = maximizeX - buttonSize - 2;
        if (mouseX >= minimizeX && mouseX <= minimizeX + buttonSize && 
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            this.mc.displayGuiScreen(null);
            return true;
        }
        
        return false;
    }

    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (this.dragWin && clickedMouseButton == 0) {
            int newX = mouseX - this.winDragX;
            int newY = mouseY - this.winDragY;

            this.winX = Math.max(0, Math.min(this.width - this.winWidth, newX));
            this.winY = Math.max(0, Math.min(this.height - this.winHeight, newY));
        }

        if (this.resizing) {
            int deltaX = mouseX - this.resizeStartX;
            int deltaY = mouseY - this.resizeStartY;

            if (this.resizeDir == 1 || this.resizeDir == 3) {
                this.winWidth = Math.max(MIN_WINDOW_WIDTH, Math.min(this.width - this.winX, this.initWidth + deltaX));
                this.winWidth = Math.min(this.winWidth, this.width - this.winX); // Limit max width
            }
            if (this.resizeDir == 2 || this.resizeDir == 3) {
                this.winHeight = Math.max(MIN_WINDOW_HEIGHT, Math.min(this.height - this.winY, this.initHeight + deltaY));
                this.winHeight = Math.min(this.winHeight, this.height - this.winY); // Limit max height
            }
        }

        if (this.dragFrame != null) {
            this.dragFrame.updatePosition(mouseX, mouseY);
        } else if (this.dragComp != null) {
            this.dragComp.updatePosition(mouseX, mouseY);
        }
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (this.dragComp != null) {
            this.dragComp.onMouseReleased(mouseX, mouseY, state);
            this.dragComp = null;
        }

        if (this.dragFrame != null) {
            this.dragFrame.mouseReleased(mouseX, mouseY, state);
            this.dragFrame = null;
        }

        this.dragWin = false;
        this.resizing = false;

        for (Frame frame : this.frames) {
            frame.mouseReleased(mouseX, mouseY, state);
        }
    }
    
    private void restoreSelectedState() {
        if (lastSelCat != null) {
            for (Frame frame : frames) {
                if (frame.getCategory().getName().equals(lastSelCat)) {
                    this.selFrame = frame;
                    break;
                }
            }
        }
        
        if (lastSelMod != null && this.selFrame != null) {
            List<ModuleButton> moduleButtons = this.selFrame.getModuleButtons();
            for (ModuleButton moduleButton : moduleButtons) {
                if (moduleButton.getModule().getName().equals(lastSelMod)) {
                    this.selMod = moduleButton;
                    break;
                }
            }
        }
        
        if (lastSelMod != null && this.selMod == null) {
            for (Frame frame : frames) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                for (ModuleButton moduleButton : moduleButtons) {
                    if (moduleButton.getModule().getName().equals(lastSelMod)) {
                        this.selMod = moduleButton;
                        this.selFrame = frame;
                        break;
                    }
                }
                if (this.selMod != null) {
                    break;
                }
            }
        }
    }

    private void navigateUp() {
        if (frames.isEmpty()) return;

        List<Object> navigableElements = new ArrayList<>();
        for (Frame frame : frames) {
            navigableElements.add(frame);
            if (frame.isExtended()) {
                for (ModuleButton moduleButton : frame.getModuleButtons()) {
                    if (moduleButton.isVisible()) {
                        navigableElements.add(moduleButton);
                    }
                }
            }
        }

        if (navigableElements.isEmpty()) return;

        int currentIndex = navigableElements.indexOf(this.selNavElem);
        if (currentIndex == -1) {
            this.selNavElem = navigableElements.get(0);
            return;
        }

        int newIndex = currentIndex - 1;
        if (newIndex < 0) {
            newIndex = navigableElements.size() - 1;
        }
        this.selNavElem = navigableElements.get(newIndex);
        ensureVisible(this.selNavElem);
    }

    private void navigateDown() {
        if (frames.isEmpty()) return;

        List<Object> navigableElements = new ArrayList<>();
        for (Frame frame : frames) {
            navigableElements.add(frame);
            if (frame.isExtended()) {
                for (ModuleButton moduleButton : frame.getModuleButtons()) {
                    if (moduleButton.isVisible()) {
                        navigableElements.add(moduleButton);
                    }
                }
            }
        }

        if (navigableElements.isEmpty()) return;

        int currentIndex = navigableElements.indexOf(this.selNavElem);
        if (currentIndex == -1) {
            this.selNavElem = navigableElements.get(0);
            return;
        }

        int newIndex = currentIndex + 1;
        if (newIndex >= navigableElements.size()) {
            newIndex = 0;
        }
        this.selNavElem = navigableElements.get(newIndex);
        ensureVisible(this.selNavElem);
    }

    private void ensureVisible(Object element) {
        int elementY = -1;
        int elementHeight = 0;

        int contentAreaY = winY + TITLE_HEIGHT + SEARCH_HEIGHT + SEARCH_Y_OFF * 2;
        int contentAreaHeight = winHeight - TITLE_HEIGHT - SEARCH_HEIGHT - SEARCH_Y_OFF * 2;
        
        int currentY = contentAreaY + 5 - this.navScroll;
        int categoryHeight = 20;

        for (Frame frame : frames) {
            if (frame == element) {
                elementY = currentY;
                elementHeight = categoryHeight;
                break;
            }
            currentY += categoryHeight;
            if (frame.isExtended()) {
                for (ModuleButton moduleButton : frame.getModuleButtons()) {
                    if (moduleButton.isVisible()) {
                        if (moduleButton == element) {
                            elementY = currentY;
                            elementHeight = 15;
                            break;
                        }
                        currentY += 15;
                    }
                }
            }
            if (elementY != -1) break;
        }

        if (elementY != -1) {
            if (elementY < contentAreaY) {
                this.navScroll -= (contentAreaY - elementY);
            }
            else if (elementY + elementHeight > contentAreaY + contentAreaHeight) {
                this.navScroll += (elementY + elementHeight - (contentAreaY + contentAreaHeight));
            }
            int maxScrollOffset = calculateMaxNavigationContentHeight() - contentAreaHeight;
            this.navScroll = Math.max(0, Math.min(maxScrollOffset, this.navScroll));
        }
    }

    private void activateSelectedElement() {
        if (this.selNavElem instanceof Frame) {
            Frame frame = (Frame) this.selNavElem;
            this.selFrame = frame;
            this.selMod = null;
            frame.setExtended(!frame.isExtended());
        } else if (this.selNavElem instanceof ModuleButton) {
            ModuleButton moduleButton = (ModuleButton) this.selNavElem;
            this.selMod = moduleButton;
            this.selFrame = moduleButton.getParentFrame();
            moduleButton.getModule().toggle();
        }
    }

    private int calculateMaxNavigationContentHeight() {
        int totalHeight = 0;
        int categoryHeight = 20;
        for (Frame frame : frames) {
            totalHeight += categoryHeight;
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                for (ModuleButton moduleButton : moduleButtons) {
                    if (moduleButton.isVisible()) {
                        totalHeight += 15;
                    }
                }
            }
        }
        return totalHeight;
    }
    
    @Override
    public void onGuiClosed() {
        GuiModule guiModule = (GuiModule) Myau.moduleManager.getModule("GuiSettings");
        if (guiModule != null) {
            if (guiModule.saveGuiState.getValue()) {
                framePos.clear();
                for (Frame frame : this.frames) {
                    String categoryName = frame.getCategory().getName();
                    int[] position = new int[]{frame.getX(), frame.getY()};
                    framePos.put(categoryName, position);
                }
                
                catExpandStates.clear();
                for (Frame frame : this.frames) {
                    String categoryName = frame.getCategory().getName();
                    boolean isExpanded = frame.isExtended();
                    catExpandStates.put(categoryName, isExpanded);
                }
                
                if (this.selFrame != null) {
                    lastSelCat = this.selFrame.getCategory().getName();
                } else {
                    lastSelCat = null;
                }
                
                if (this.selMod != null) {
                    lastSelMod = this.selMod.getModule().getName();
                } else {
                    lastSelMod = null;
                }
                
                savedNavScroll = this.navScroll;
                savedPropScroll = this.propScroll;
                
                guiModule.windowX.setValue(winX);
                guiModule.windowY.setValue(winY);
                guiModule.windowWidth.setValue(winWidth);
                guiModule.windowHeight.setValue(winHeight);
            }
        }
        
        Myau.moduleManager.getModule("ClickGUI").setEnabled(false);
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void filterModules() {
        String lowerCaseQuery = searchQ.toLowerCase();

        for (Frame frame : frames) {
            boolean categoryMatches = frame.getCategory().getName().toLowerCase().contains(lowerCaseQuery);
            boolean hasVisibleModule = false;

            for (ModuleButton moduleButton : frame.getModuleButtons()) {
                boolean moduleMatches = moduleButton.getModule().getName().toLowerCase().contains(lowerCaseQuery);
                if (searchQ.isEmpty()) {
                    moduleButton.setVisible(true);
                    hasVisibleModule = true;
                } else if (moduleMatches) {
                    moduleButton.setVisible(true);
                    hasVisibleModule = true;
                } else {
                    moduleButton.setVisible(false);
                }
            }

            if (searchQ.isEmpty()) {
                frame.setExtended(true);
            } else if (categoryMatches || hasVisibleModule) {
                frame.setExtended(true);
            } else {
                frame.setExtended(false);
            }
        }
    }

    private void renderScrollbar(int x, int y, int width, int height, int contentHeight, int scrollOffset, int mouseX, int mouseY) {
        if (contentHeight <= height) return;

        float scrollbarRatio = (float) height / contentHeight;
        float scrollbarHeight = Math.max(20, scrollbarRatio * height);
        float scrollProgress = (float) scrollOffset / (contentHeight - height);
        float scrollbarY = y + scrollProgress * (height - scrollbarHeight);

        Gui.drawRect(x, y, x + width, y + height, new Color(50, 50, 50, 100).getRGB());

        int scrollbarColor = mouseX >= x && mouseX <= x + width && 
                            mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight ?
                            0xFF888888 : 0xFF666666;
        
        Gui.drawRect(x, (int) scrollbarY, x + width, (int) (scrollbarY + scrollbarHeight), scrollbarColor);
    }
}