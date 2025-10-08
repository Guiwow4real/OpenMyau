package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import myau.ui.clickgui.component.Component;
import myau.ui.clickgui.component.ModuleButton;
import myau.module.modules.GuiModule;
import myau.property.properties.*;
import myau.ui.clickgui.component.impl.Checkbox;
import myau.ui.clickgui.component.impl.Slider;
import myau.ui.clickgui.component.impl.ModeSelector;
import myau.module.Module;
import org.lwjgl.input.Keyboard;
import myau.ui.clickgui.component.impl.BindButton;
import myau.util.RenderUtil;
import myau.util.RenderUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import org.lwjgl.input.Mouse;
import net.minecraft.util.ChatAllowedCharacters;

public class ClickGuiScreen extends GuiScreen {
    // 单例实例
    private static ClickGuiScreen instance;

    private ArrayList<Frame> frames;
    private Component draggingComponent = null; // New field to track dragging component
    private BindButton listeningBindButton = null;
    private Frame selectedFrame = null; // 当前选中的框架
    
    // Static map to store frame positions for saving/loading
    public static Map<String, int[]> framePositions = new HashMap<>();
    
    // Static variables to store GUI state
    public static String lastSelectedCategory = null;
    public static String lastSelectedModule = null;
    public static Map<String, Boolean> categoryExpandedStates = new HashMap<>();
    public static int savedNavigationScrollOffset = 0; // 保存的导航栏滚动偏移
    public static int savedPropertiesScrollOffset = 0; // 保存的属性区域滚动偏移
    
    // 滚动相关变量
    private int navigationScrollOffset = 0; // 左侧导航栏滚动偏移
    private int propertiesScrollOffset = 0; // 右侧属性区域滚动偏移
    private final int SCROLL_SPEED = 10; // 滚动速度
    
    // IntelliJ IDEA主题颜色
    private static final int BACKGROUND_COLOR = IntelliJTheme.getRGB(IntelliJTheme.BACKGROUND_COLOR);
    private static final int BORDER_COLOR = IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR);
    private static final int NAVIGATION_BG_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
    private static final int HEADER_TEXT_COLOR = IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR);
    private static final int SELECTED_BG_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SELECTED_BG_COLOR);
    private static final boolean ENABLE_TRANSPARENT_BACKGROUND = false; // 禁用透明背景，使用IntelliJ风格的背景
    private static final double CORNER_RADIUS = IntelliJTheme.CORNER_RADIUS;
    
    // 布局常量
    private static final int NAVIGATION_WIDTH = 150; // 左侧导航栏宽度
    private static final int BORDER_THICKNESS = 1; // 边框厚度
    private static final int TITLE_BAR_HEIGHT = 30; // 窗口标题栏高度
    
    // 当前选中的模块
    private ModuleButton selectedModule = null;
    // 用于键盘导航选中的元素
    private Object selectedNavigationElement = null; // 可以是 Frame 或 ModuleButton

    // Tooltip management
    private String tooltipText = null;
    private int tooltipX = 0;
    private int tooltipY = 0;
    
    // 窗口标题栏相关变量
    private boolean isDraggingWindow = false;
    private int windowDragX = 0;
    private int windowDragY = 0;
    public int windowX = 200; // 窗口X位置
    public int windowY = 80;  // 窗口Y位置
    public int windowWidth = 650;  // 窗口宽度
    public int windowHeight = 480; // 窗口高度
    
    // Double-click detection variables
    private long lastClickTime = 0;
    private ModuleButton lastClickedModuleButton = null;
    private static final long DOUBLE_CLICK_TIME_MS = 200; // 200 milliseconds for double click
    
    // 搜索框相关变量
    private String searchQuery = "";
    private boolean isSearching = false;
    private static final int SEARCH_BAR_HEIGHT = 20;
    private static final int SEARCH_BAR_Y_OFFSET = 5;
    
    // 窗口大小调整相关变量
    private boolean isResizingWindow = false;
    private int resizeDirection = 0; // 1: right, 2: bottom, 3: right-bottom
    private int resizeStartX, resizeStartY, initialWindowWidth, initialWindowHeight;
    private static final int RESIZE_HANDLE_SIZE = 6;
    
    // 获取单例实例
    public static ClickGuiScreen getInstance() {
        return instance;
    }
    
    // 获取选中的框架
    public Frame getSelectedFrame() {
        return selectedFrame;
    }
    
    // 设置选中的框架
    public void setSelectedFrame(Frame selectedFrame) {
        this.selectedFrame = selectedFrame;
    }
    
    public ClickGuiScreen() {
        instance = this; // 设置单例实例
        this.frames = new ArrayList<>();
        
        // 调试信息：检查Myau.moduleManager是否为空
        if (Myau.moduleManager == null) {
            System.out.println("ERROR: Myau.moduleManager is null!");
            return;
        }
        
        // 调试信息：检查模块数量
        System.out.println("Total modules: " + Myau.moduleManager.modules.size());
        
        int frameY = 5;
        
        // 为每个分类创建Frame，不管是否有模块
        for (Category category : Category.values()) {
            System.out.println("Processing category: " + category.getName());
            
            // 获取该分类下的模块
            ArrayList<Module> modulesInCategory = Myau.moduleManager.getModulesInCategory(category);
            System.out.println("Modules in " + category.getName() + ": " + modulesInCategory.size());
            
            // 创建Frame
            Frame frame = new Frame(category);
            frame.setX(5);
            frame.setY(frameY);
            
            // 动态计算Frame宽度以适应导航栏
            int frameWidth = NAVIGATION_WIDTH - 10;
            frame.setWidth(frameWidth);
            
            // Load saved position if available
            if (framePositions.containsKey(category.getName())) {
                int[] position = framePositions.get(category.getName());
                frame.setX(position[0]);
                frame.setY(position[1]);
            }
            
            // 恢复分类的展开状态
            if (categoryExpandedStates.containsKey(category.getName())) {
                boolean wasExpanded = categoryExpandedStates.get(category.getName());
                if (!wasExpanded && frame.isExtended()) {
                    frame.toggleOpenClose();
                } else if (wasExpanded && !frame.isExtended()) {
                    frame.toggleOpenClose();
                }
            }
            
            this.frames.add(frame);
            frameY += 20; // 每个Frame之间的间距
        }
        
        // 调试信息：检查创建的frames数量
        System.out.println("Total frames created: " + frames.size());
        
        // 恢复选中的框架和模块
        restoreSelectedState();
        
        // 恢复滚动偏移
        this.navigationScrollOffset = savedNavigationScrollOffset;
        this.propertiesScrollOffset = savedPropertiesScrollOffset;
        
        // 如果没有恢复到选中的框架，默认选中第一个框架
        if (selectedFrame == null && !frames.isEmpty()) {
            selectedFrame = frames.get(0);
        }
        // 初始化键盘导航选中的元素
        if (selectedModule != null) {
            selectedNavigationElement = selectedModule;
        } else if (selectedFrame != null) {
            selectedNavigationElement = selectedFrame;
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Reset tooltip at the beginning of each frame
        tooltipText = null;

        // 绘制背景（半透明遮罩）
        Gui.drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 100).getRGB());
        
        // 绘制标题栏
        renderTitleBar(mouseX, mouseY);
        
        // 绘制主内容区域（在标题栏下方）
        int contentY = TITLE_BAR_HEIGHT;
        int contentHeight = this.height - TITLE_BAR_HEIGHT;
        
        // 绘制左侧导航栏背景
        Gui.drawRect(0, contentY, NAVIGATION_WIDTH, this.height, 
                    IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND));
        
        // 绘制导航栏边框
        Gui.drawRect(NAVIGATION_WIDTH, contentY, NAVIGATION_WIDTH + BORDER_THICKNESS, this.height, 
                    IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));

        // 渲染搜索栏
        renderSearchBar(mouseX, mouseY);

        // Render navigation panel and collect tooltip
        String navigationTooltip = renderNavigationPanel(mouseX, mouseY);
        if (navigationTooltip != null) {
            tooltipText = navigationTooltip;
            tooltipX = mouseX;
            tooltipY = mouseY;
        }
        
        // Render module properties area and collect tooltip
        String propertiesTooltip = renderModuleProperties(NAVIGATION_WIDTH + BORDER_THICKNESS, contentY, 
                                      this.width - NAVIGATION_WIDTH - BORDER_THICKNESS, contentHeight, mouseX, mouseY);
        if (propertiesTooltip != null) {
            tooltipText = propertiesTooltip;
            tooltipX = mouseX;
            tooltipY = mouseY;
        }
        
        // 更新拖拽组件位置
        if (this.draggingComponent != null) {
            this.draggingComponent.updatePosition(mouseX, mouseY);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 渲染窗口大小调整手柄
        renderResizeHandles(mouseX, mouseY);

        // Render tooltip last to ensure it's on top
        if (tooltipText != null) {
            renderTooltip(tooltipText, tooltipX, tooltipY);
        }
    }
    
    /**
     * 渲染搜索栏
     */
    private void renderSearchBar(int mouseX, int mouseY) {
        int searchBarX = BORDER_THICKNESS;
        int searchBarY = TITLE_BAR_HEIGHT + SEARCH_BAR_Y_OFFSET;
        int searchBarWidth = NAVIGATION_WIDTH - BORDER_THICKNESS * 2;
        int searchBarHeight = SEARCH_BAR_HEIGHT;

        // 绘制搜索框背景
        RenderUtil.drawRoundedRect(searchBarX, searchBarY, searchBarWidth, searchBarHeight, CORNER_RADIUS, IntelliJTheme.getRGB(IntelliJTheme.TEXT_FIELD_BG));

        // 绘制搜索框边框
        RenderUtil.drawRoundedRectOutline(searchBarX, searchBarY, searchBarWidth, searchBarHeight, CORNER_RADIUS, 1.0f, isSearching ? IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));

        // 绘制搜索图标
        mc.fontRendererObj.drawStringWithShadow("\uD83D\uDD0D", searchBarX + 5, searchBarY + searchBarHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR_SECONDARY)); // Unicode search icon

        // 绘制搜索文本
        String displayText = searchQuery.isEmpty() && !isSearching ? "Search..." : searchQuery;
        int textColor = searchQuery.isEmpty() && !isSearching ? IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR_SECONDARY) : IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR);
        mc.fontRendererObj.drawStringWithShadow(displayText, searchBarX + 5 + mc.fontRendererObj.getStringWidth("\uD83D\uDD0D "), searchBarY + searchBarHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2, textColor);

        // 绘制输入光标
        if (isSearching && System.currentTimeMillis() / 500 % 2 == 0) {
            String currentText = searchQuery.isEmpty() ? "" : searchQuery;
            int cursorX = searchBarX + 5 + mc.fontRendererObj.getStringWidth("\uD83D\uDD0D " + currentText);
            Gui.drawRect(cursorX, searchBarY + searchBarHeight / 2 - mc.fontRendererObj.FONT_HEIGHT / 2 - 1, cursorX + 1, searchBarY + searchBarHeight / 2 + mc.fontRendererObj.FONT_HEIGHT / 2 + 1, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
        }
    }
    
    /**
     * 渲染窗口大小调整手柄
     */
    private void renderResizeHandles(int mouseX, int mouseY) {
        // 右下角
        int rightBottomX = windowX + windowWidth - RESIZE_HANDLE_SIZE;
        int rightBottomY = windowY + windowHeight - RESIZE_HANDLE_SIZE;
        boolean isHoveringRightBottom = mouseX >= rightBottomX && mouseX <= windowX + windowWidth &&
                                        mouseY >= rightBottomY && mouseY <= windowY + windowHeight;
        Gui.drawRect(rightBottomX, rightBottomY, windowX + windowWidth, windowY + windowHeight, 
                     isHoveringRightBottom ? IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));

        // 右边缘
        int rightEdgeX1 = windowX + windowWidth - RESIZE_HANDLE_SIZE;
        int rightEdgeY1 = windowY + RESIZE_HANDLE_SIZE;
        int rightEdgeX2 = windowX + windowWidth;
        int rightEdgeY2 = windowY + windowHeight - RESIZE_HANDLE_SIZE;
        boolean isHoveringRightEdge = mouseX >= rightEdgeX1 && mouseX <= rightEdgeX2 &&
                                      mouseY >= rightEdgeY1 && mouseY <= rightEdgeY2;
        Gui.drawRect(rightEdgeX1, rightEdgeY1, rightEdgeX2, rightEdgeY2,
                     isHoveringRightEdge ? IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));

        // 下边缘
        int bottomEdgeX1 = windowX + RESIZE_HANDLE_SIZE;
        int bottomEdgeY1 = windowY + windowHeight - RESIZE_HANDLE_SIZE;
        int bottomEdgeX2 = windowX + windowWidth - RESIZE_HANDLE_SIZE;
        int bottomEdgeY2 = windowY + windowHeight;
        boolean isHoveringBottomEdge = mouseX >= bottomEdgeX1 && mouseX <= bottomEdgeX2 &&
                                       mouseY >= bottomEdgeY1 && mouseY <= bottomEdgeY2;
        Gui.drawRect(bottomEdgeX1, bottomEdgeY1, bottomEdgeX2, bottomEdgeY2,
                     isHoveringBottomEdge ? IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR) : IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));
    }

    /**
     * 渲染工具提示
     */
    private void renderTooltip(String text, int mouseX, int mouseY) {
        if (text == null || text.isEmpty()) return;

        FontRenderer fr = this.mc.fontRendererObj;
        int textWidth = fr.getStringWidth(text);
        int textHeight = fr.FONT_HEIGHT;

        int padding = 3;
        int tooltipX = mouseX + 10; // 偏移鼠标位置，避免遮挡
        int tooltipY = mouseY - 5;  // 偏移鼠标位置

        // 确保提示框不会超出屏幕右侧
        if (tooltipX + textWidth + padding * 2 > this.width) {
            tooltipX = this.width - textWidth - padding * 2; // 调整到屏幕边缘
        }

        // 绘制背景
        Gui.drawRect(tooltipX, tooltipY, tooltipX + textWidth + padding * 2, tooltipY + textHeight + padding * 2, IntelliJTheme.getRGB(IntelliJTheme.TOOLTIP_BACKGROUND));
        // 绘制边框
        RenderUtil.drawRectOutline(tooltipX, tooltipY, textWidth + padding * 2, textHeight + padding * 2, 1.0f, IntelliJTheme.getRGB(IntelliJTheme.TOOLTIP_BORDER));
        // 绘制文本
        fr.drawStringWithShadow(text, tooltipX + padding, tooltipY + padding, IntelliJTheme.getRGB(IntelliJTheme.TOOLTIP_TEXT_COLOR));
    }

    /**
     * 渲染窗口标题栏（模仿 IntelliJ IDEA 和 Windows 风格）
     */
    private void renderTitleBar(int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        
        // 检查是否悬停在标题栏上
        boolean isHoveringTitleBar = mouseY >= 0 && mouseY <= TITLE_BAR_HEIGHT;
        
        // 绘制标题栏背景（根据悬停状态改变颜色）
        int titleBarColor = isHoveringTitleBar ? 
            IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR) : 
            IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
        
        Gui.drawRect(0, 0, this.width, TITLE_BAR_HEIGHT, titleBarColor);
        
        // 绘制标题栏底部分隔线
        Gui.drawRect(0, TITLE_BAR_HEIGHT - 1, this.width, TITLE_BAR_HEIGHT, 
                    IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));
        
        // 绘制窗口图标（模仿 IDEA 风格）
        int iconX = 8;
        int iconY = (TITLE_BAR_HEIGHT - 16) / 2;
        
        // 绘制简单的正方形图标（代表 IDEA 风格）
        Gui.drawRect(iconX, iconY, iconX + 16, iconY + 16, IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_COLOR));
        Gui.drawRect(iconX + 1, iconY + 1, iconX + 15, iconY + 15, IntelliJTheme.getRGB(IntelliJTheme.BACKGROUND_COLOR));
        
        // 在图标中间绘制字母 "M"（代表 Myau）
        fr.drawString("M", iconX + 5, iconY + 4, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
        
        // 绘制窗口标题
        String title = "OpenMyau - ClickGUI Configuration";
        int titleX = iconX + 20;
        int titleY = (TITLE_BAR_HEIGHT - fr.FONT_HEIGHT) / 2;
        fr.drawString(title, titleX, titleY, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
        
        // 绘制窗口控制按钮（最小化、最大化、关闭）
        renderWindowControls(mouseX, mouseY);
    }
    
    /**
     * 渲染窗口控制按钮（最小化、最大化、关闭）
     */
    private void renderWindowControls(int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        
        int buttonSize = 20;
        int buttonY = (TITLE_BAR_HEIGHT - buttonSize) / 2;
        
        // 关闭按钮（红色）
        int closeX = this.width - buttonSize - 5;
        boolean isHoveringClose = mouseX >= closeX && mouseX <= closeX + buttonSize && 
                                 mouseY >= buttonY && mouseY <= buttonY + buttonSize;
        
        int closeColor = isHoveringClose ? 
            new Color(255, 96, 96).getRGB() : 
            IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR);
        
        Gui.drawRect(closeX, buttonY, closeX + buttonSize, buttonY + buttonSize, closeColor);
        fr.drawString("×", closeX + 7, buttonY + 6, 0xFFFFFF);
        
        // 最大化按钮（灰色）
        int maximizeX = closeX - buttonSize - 2;
        boolean isHoveringMaximize = mouseX >= maximizeX && mouseX <= maximizeX + buttonSize && 
                                    mouseY >= buttonY && mouseY <= buttonY + buttonSize;
        
        int maximizeColor = isHoveringMaximize ? 
            IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR) : 
            IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR);
        
        Gui.drawRect(maximizeX, buttonY, maximizeX + buttonSize, buttonY + buttonSize, maximizeColor);
        fr.drawString("□", maximizeX + 6, buttonY + 6, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
        
        // 最小化按钮（灰色）
        int minimizeX = maximizeX - buttonSize - 2;
        boolean isHoveringMinimize = mouseX >= minimizeX && mouseX <= minimizeX + buttonSize && 
                                    mouseY >= buttonY && mouseY <= buttonY + buttonSize;
        
        int minimizeColor = isHoveringMinimize ? 
            IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR) : 
            IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR);
        
        Gui.drawRect(minimizeX, buttonY, minimizeX + buttonSize, buttonY + buttonSize, minimizeColor);
        fr.drawString("−", minimizeX + 7, buttonY + 6, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
    }
    
    /**
     * 渲染左侧导航栏，模仿IntelliJ IDEA项目结构（整体滚动）
     * @return 返回需要显示的Tooltip文本，如果没有则返回null
     */
    private String renderNavigationPanel(int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        int categoryHeight = 20; // 每个分类的高度
        int contentAreaY = TITLE_BAR_HEIGHT + SEARCH_BAR_HEIGHT + SEARCH_BAR_Y_OFFSET * 2; // 内容区域的起始Y坐标
        int contentAreaHeight = this.height - contentAreaY; // 内容区域的高度

        String hoveredTooltip = null;

        // 计算导航栏内容的总高度
        int totalNavigationContentHeight = 0;
        for (Frame frame : frames) {
            totalNavigationContentHeight += categoryHeight; // 分类标题高度
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                if (moduleButtons.isEmpty()) {
                    totalNavigationContentHeight += 15; // 空提示文本高度
                } else {
                    totalNavigationContentHeight += moduleButtons.size() * 15; // 每个模块15像素高度
                }
            }
        }

        // 限制navigationScrollOffset的范围
        int maxScrollOffset = Math.max(0, totalNavigationContentHeight - contentAreaHeight); // 计算最大滚动偏移量
        navigationScrollOffset = Math.max(0, Math.min(maxScrollOffset, navigationScrollOffset)); // 限制滚动范围

        // 渲染导航栏的滚动条
        int navigationScrollbarX = NAVIGATION_WIDTH - 5; // 滚动条X位置
        int navigationScrollbarWidth = 3; // 滚动条宽度
        renderScrollbar(navigationScrollbarX, contentAreaY, navigationScrollbarWidth, contentAreaHeight, totalNavigationContentHeight, navigationScrollOffset, mouseX, mouseY);

        // 设置裁剪区域，只绘制在导航栏内容区域内的内容
        RenderUtil.scissor(5, contentAreaY, NAVIGATION_WIDTH - 10, contentAreaHeight);
        
        int currentY = contentAreaY + 5 - navigationScrollOffset; // 绘制内容的起始Y位置，受滚动影响

        // 调试信息：检查frames列表
        if (frames.isEmpty()) {
            // 显示调试信息
            String debugInfo = "No frames found! Categories: ";
            for (Category category : Category.values()) {
                debugInfo += category.getName() + " ";
            }
            debugInfo += "Total modules: " + Myau.moduleManager.modules.size();
            
            if (currentY >= contentAreaY && currentY < contentAreaY + contentAreaHeight) {
                fr.drawString(debugInfo, 15, currentY, 0xFFFFFF);
            }
            currentY += 12;
            
            // 显示所有分类
            for (Category category : Category.values()) {
                String categoryInfo = "Category: " + category.getName() + ", Modules: " + 
                    Myau.moduleManager.getModulesInCategory(category).size();
                if (currentY >= contentAreaY && currentY < contentAreaY + contentAreaHeight) {
                    fr.drawString(categoryInfo, 15, currentY, 0xFFFFFF);
                }
                currentY += 12;
            }
            RenderUtil.releaseScissor();
            return null;
        }
        
        // 正常渲染frames - 整体滚动
        for (Frame frame : frames) {
            // 绘制分类标题
            if (currentY + categoryHeight > contentAreaY && currentY < contentAreaY + contentAreaHeight) {
                // 绘制包背景
                int categoryBgColor = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
                boolean isCategoryHovered = mouseX >= 5 && mouseX <= NAVIGATION_WIDTH - 5 &&
                                            mouseY >= currentY && mouseY <= currentY + categoryHeight;

                if (isCategoryHovered) {
                    categoryBgColor = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);
                }
                if (frame == selectedFrame) {
                    categoryBgColor = IntelliJTheme.getRGB(IntelliJTheme.SELECTED_BG_COLOR); // 选中时覆盖悬停色
                }
                Gui.drawRect(5, currentY, NAVIGATION_WIDTH - 5, currentY + categoryHeight, categoryBgColor);
                
                // 绘制键盘导航选中高亮
                if (frame == selectedNavigationElement) {
                    RenderUtil.drawRectOutline(5, currentY, NAVIGATION_WIDTH - 10, categoryHeight, 1.0f, IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR));
                }
                
                // 绘制包名称 - 使用包命名格式
                String categoryName = frame.getCategory().getName();
                String packageName = "myau.modules." + categoryName.toLowerCase();
                fr.drawString(packageName, 15, currentY + 6, IntelliJTheme.getRGB(IntelliJTheme.TYPE_VALUE_COLOR));
                
                // 绘制展开/折叠指示器
                String indicator = frame.isExtended() ? "▼" : "▶";
                fr.drawString(indicator, NAVIGATION_WIDTH - 15, currentY + 6, HEADER_TEXT_COLOR);
            }
            
            currentY += categoryHeight;
            
            // 如果包展开，显示该包下的类（模块）
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                
                if (moduleButtons.isEmpty()) {
                    // 如果该分类下没有模块，显示提示
                    if (currentY >= contentAreaY && currentY < contentAreaY + contentAreaHeight) {
                        String noModulesText = "    // No modules in this category";
                        fr.drawString(noModulesText, 20, currentY + 4, IntelliJTheme.getRGB(IntelliJTheme.DISABLED_TEXT_COLOR));
                    }
                    currentY += 15;
                } else {
                    for (ModuleButton moduleButton : moduleButtons) {
                        // 检查是否在可见区域内，并且是可见的模块
                        if (moduleButton.isVisible() && currentY + 15 > contentAreaY && currentY < contentAreaY + contentAreaHeight) {
                            // 绘制类背景
                            int moduleBgColor = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
                            boolean isModuleHovered = mouseX >= 10 && mouseX <= NAVIGATION_WIDTH - 5 &&
                                                      mouseY >= currentY && mouseY <= currentY + 15; // Corrected mouseY for hover detection

                            if (isModuleHovered) {
                                moduleBgColor = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);
                                hoveredTooltip = moduleButton.getModule().getDescription(); // Collect tooltip
                            }
                            if (moduleButton == selectedModule) {
                                moduleBgColor = IntelliJTheme.getRGB(IntelliJTheme.SELECTED_BG_COLOR); // 选中时覆盖悬停色
                            }
                            Gui.drawRect(10, currentY, NAVIGATION_WIDTH - 5, currentY + 15, moduleBgColor);
                            
                            // 绘制类名称 - 使用类命名格式
                            String moduleName = moduleButton.getModule().getName();
                            String className = moduleName + ".java";
                            
                            // 根据模块状态设置颜色
                            int textColor = moduleButton.getModule().isEnabled() ? 
                                IntelliJTheme.getRGB(IntelliJTheme.VARIABLE_NAME_COLOR) : // 紫色表示启用
                                IntelliJTheme.getRGB(IntelliJTheme.DISABLED_TEXT_COLOR);   // 灰色表示禁用
                            
                            fr.drawString(className, 20, currentY + 4, textColor);

                            // 绘制键盘导航选中高亮
                            if (moduleButton == selectedNavigationElement) {
                                RenderUtil.drawRectOutline(10, currentY, NAVIGATION_WIDTH - 15, 15, 1.0f, IntelliJTheme.getRGB(IntelliJTheme.ACTIVE_BORDER_COLOR));
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
    
    /**
     * 渲染模块属性 - IDE代码编辑器风格（支持滚动）
     * @return 返回需要显示的Tooltip文本，如果没有则返回null
     */
    private String renderModuleProperties(int x, int y, int width, int height, int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        String hoveredTooltip = null;
        
        // IDE代码编辑器风格背景
        Gui.drawRect(x, y, x + width, y + height, IntelliJTheme.getRGB(IntelliJTheme.BACKGROUND_COLOR));
        
        // 绘制行号区域背景
        int lineNumberWidth = 40;
        Gui.drawRect(x, y, x + lineNumberWidth, y + height, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_BG_COLOR));
        
        // 绘制行号区域分隔线
        Gui.drawRect(x + lineNumberWidth, y, x + lineNumberWidth + 1, y + height, IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));
        
        // 绘制代码区域背景
        Gui.drawRect(x + lineNumberWidth + 1, y, x + width, y + height, IntelliJTheme.getRGB(IntelliJTheme.CODE_BG_COLOR));
        
        // 计算属性内容的总高度
        int totalPropertiesContentHeight = 0;
        int initialContentOffset = 10; // 初始偏移量
        int linesBeforeProperties = 7; // 包声明、导入、类声明等前面的行数
        totalPropertiesContentHeight += (linesBeforeProperties + 1) * 15; // 每行15像素高度

        if (selectedModule != null) {
            ArrayList<Component> subComponents = selectedModule.getSubComponents();
            if (subComponents != null && !subComponents.isEmpty()) {
                totalPropertiesContentHeight += subComponents.size() * 15; // 每个属性15像素高度
            } else {
                totalPropertiesContentHeight += 15; // "没有可配置的属性" 提示文本高度
            }
            totalPropertiesContentHeight += 15; // 类结束大括号
        } else {
            totalPropertiesContentHeight += 2 * 15; // 欢迎文本高度
        }
        totalPropertiesContentHeight += initialContentOffset * 2; // 上下边距

        // 渲染属性区域的滚动条
        int propertiesScrollbarX = x + width - 5; // 滚动条X位置
        int propertiesScrollbarWidth = 3; // 滚动条宽度
        renderScrollbar(propertiesScrollbarX, y, propertiesScrollbarWidth, height, totalPropertiesContentHeight, propertiesScrollOffset, mouseX, mouseY);

        // 设置裁剪区域，只绘制在属性内容区域内的内容
        RenderUtil.scissor(x + lineNumberWidth + 1, y, width - lineNumberWidth - 1, height);
        
        int contentY = y + 10 - propertiesScrollOffset; // 支持滚动
        int lineNumber = 1;
        
        // 如果选中了模块，显示模块属性
        if (selectedModule != null) {
            Module module = selectedModule.getModule();
            
            // 绘制包声明 - IDE风格
            String packageDeclaration = "package myau.module.modules;";
            fr.drawStringWithShadow(packageDeclaration, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            contentY += 15;
            
            // 绘制导入语句
            String[] imports = {"import myau.module.Module;", "import myau.module.Category;", "import myau.property.properties.*;"};
            
            for (String importStmt : imports) {
                fr.drawStringWithShadow(importStmt, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
                contentY += 15;
            }
            
            // 空行
            contentY += 20; // Add space for empty line without line number

            // 类声明 - 使用新的类名颜色（绿色)
            String classDeclaration = "public class " + module.getName() + " extends Module {";
            fr.drawStringWithShadow("public class ", x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
            fr.drawStringWithShadow(module.getName(), x + lineNumberWidth + 10 + fr.getStringWidth("public class "), contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
            fr.drawStringWithShadow(" extends ", x + lineNumberWidth + 10 + fr.getStringWidth("public class " + module.getName()), contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
            fr.drawStringWithShadow("Module {", x + lineNumberWidth + 10 + fr.getStringWidth("public class " + module.getName() + " extends "), contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            contentY += 15;
            
            // 空行
            contentY += 5;
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            contentY += 15;
            
            // 渲染属性为IDE风格 - 支持语法高亮和交互
            ArrayList<Component> subComponents = selectedModule.getSubComponents();
            if (subComponents != null && !subComponents.isEmpty()) {
                for (Component component : subComponents) {
                    // 获取属性对象
                    Object property = null;
                    
                    // 根据组件类型获取属性对象
                    if (component instanceof myau.ui.clickgui.component.impl.Checkbox) {
                        myau.ui.clickgui.component.impl.Checkbox checkbox = (myau.ui.clickgui.component.impl.Checkbox) component;
                        // 通过反射获取属性对象
                        try {
                            java.lang.reflect.Field field = checkbox.getClass().getDeclaredField("booleanProperty");
                            field.setAccessible(true);
                            property = field.get(checkbox);
                        } catch (Exception e) {
                            // 如果无法获取属性，跳过
                            continue;
                        }
                    } else if (component instanceof myau.ui.clickgui.component.impl.Slider) {
                        myau.ui.clickgui.component.impl.Slider slider = (myau.ui.clickgui.component.impl.Slider) component;
                        // 通过反射获取属性对象
                        try {
                            java.lang.reflect.Field field = slider.getClass().getDeclaredField("property");
                            field.setAccessible(true);
                            property = field.get(slider);
                        } catch (Exception e) {
                            // 如果无法获取属性，跳过
                            continue;
                        }
                    } else if (component instanceof myau.ui.clickgui.component.impl.ModeSelector) {
                        myau.ui.clickgui.component.impl.ModeSelector modeSelector = (myau.ui.clickgui.component.impl.ModeSelector) component;
                        // 通过反射获取属性对象
                        try {
                            java.lang.reflect.Field field = modeSelector.getClass().getDeclaredField("modeProperty");
                            field.setAccessible(true);
                            property = field.get(modeSelector);
                        } catch (Exception e) {
                            // 如果无法获取属性，跳过
                            continue;
                        }
                    }
                    
                    // 绘制属性声明 - IDE语法高亮风格
                    if (property != null) {
                        String propertyName = "";
                        String propertyValue = "";
                        String propertyType = "";
                        String propertyDescription = "";

                        // 检查是否悬停在属性行上
                        int propertyLineY = contentY;
                        int propertyLineHeight = 15; // 每行属性的高度
                        boolean isPropertyLineHovered = mouseX >= x + lineNumberWidth + 1 && mouseX <= x + width &&
                                                        mouseY >= propertyLineY && mouseY <= propertyLineY + propertyLineHeight;

                        if (isPropertyLineHovered) {
                            Gui.drawRect(x + lineNumberWidth + 1, propertyLineY, x + width, propertyLineY + propertyLineHeight, IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR));
                            hoveredTooltip = propertyDescription; // Collect tooltip
                        }
                        
                        // 根据属性类型获取信息
                        if (property instanceof myau.property.properties.BooleanProperty) {
                            myau.property.properties.BooleanProperty boolProp = (myau.property.properties.BooleanProperty) property;
                            propertyName = boolProp.getName();
                            propertyValue = boolProp.getValue() ? "true" : "false";
                            propertyType = "BooleanProperty";
                            propertyDescription = "(鼠标点击或滚轮切换)";
                        } else if (property instanceof myau.property.properties.IntProperty) {
                            myau.property.properties.IntProperty intProp = (myau.property.properties.IntProperty) property;
                            propertyName = intProp.getName();
                            propertyValue = String.valueOf(intProp.getValue());
                            propertyType = "IntProperty";
                            propertyDescription = "(鼠标悬停用滚轮调整，每次±1)";
                        } else if (property instanceof myau.property.properties.FloatProperty) {
                            myau.property.properties.FloatProperty floatProp = (myau.property.properties.FloatProperty) property;
                            propertyName = floatProp.getName();
                            propertyValue = String.valueOf(floatProp.getValue());
                            propertyType = "FloatProperty";
                            propertyDescription = "(鼠标悬停用滚轮调整，每次±0.1)";
                        } else if (property instanceof myau.property.properties.PercentProperty) {
                            myau.property.properties.PercentProperty percentProp = (myau.property.properties.PercentProperty) property;
                            propertyName = percentProp.getName();
                            propertyValue = String.valueOf(percentProp.getValue());
                            propertyType = "PercentProperty";
                            propertyDescription = "(鼠标悬停用滚轮调整，每次±1%)";
                        } else if (property instanceof myau.property.properties.ModeProperty) {
                            myau.property.properties.ModeProperty modeProp = (myau.property.properties.ModeProperty) property;
                            propertyName = modeProp.getName();
                            propertyValue = "\"" + modeProp.getValue() + "\"";
                            propertyType = "ModeProperty";
                            propertyDescription = "(点击或滚轮切换)";
                        }
                        
                        if (!propertyName.isEmpty()) {
                            // 绘制行号
                            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
                            
                            // 绘制属性声明 - 语法高亮
                            int textX = x + lineNumberWidth + 10;
                            
                            // Tab缩进
                            String tabIndent = "    ";
                            fr.drawStringWithShadow(tabIndent, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR));
                            textX += fr.getStringWidth(tabIndent);
                            
                            // public final - 关键字
                            fr.drawStringWithShadow("public final ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                            textX += fr.getStringWidth("public final ");
                            
                            // 属性类型 - 类名（绿色）
                            fr.drawStringWithShadow(propertyType + " ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
                            textX += fr.getStringWidth(propertyType + " ");
                            
                            // 属性名称 - 变量名（紫色）
                            fr.drawStringWithShadow(propertyName, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.VARIABLE_NAME_COLOR));
                            textX += fr.getStringWidth(propertyName);
                            
                            // = new - 关键字
                            fr.drawStringWithShadow(" = ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                            textX += fr.getStringWidth(" = ");
                            fr.drawStringWithShadow("new ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                            textX += fr.getStringWidth("new ");
                            
                            // 构造函数调用 - 类名（绿色）
                            fr.drawStringWithShadow(propertyType + "(\"", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
                            textX += fr.getStringWidth(propertyType + "(\"");
                            
                            // 属性名称（字符串） - 字符串颜色（绿色）
                            fr.drawStringWithShadow(propertyName, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.STRING_COLOR));
                            textX += fr.getStringWidth(propertyName);
                            
                            // 结束引号和逗号
                            fr.drawStringWithShadow("\", ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
                            textX += fr.getStringWidth("\", ");
                            
                            // 属性值 - 数字或字面量
                            if (property instanceof myau.property.properties.BooleanProperty) {
                                fr.drawStringWithShadow(propertyValue, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.LITERAL_COLOR));
                            } else {
                                fr.drawStringWithShadow(propertyValue, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.NUMBER_COLOR));
                            }
                            textX += fr.getStringWidth(propertyValue);
                            
                            // 结束括号和分号
                            fr.drawStringWithShadow("); ", textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.CLASS_NAME_COLOR));
                            textX += fr.getStringWidth("); ");
                            
                            // 属性描述 - 注释颜色（灰色）
                            fr.drawStringWithShadow("// " + propertyDescription, textX, contentY, IntelliJTheme.getRGB(IntelliJTheme.COMMENT_COLOR));

                            // 更新位置
                            contentY += 15;
                            
                            // 检查是否超出显示区域
                            if (contentY > y + height - 20) {
                                break;
                            }
                        }
                    }
                }
            } else {
                // 如果没有属性，显示注释
                String comment = "// 该模块没有可配置的属性";
                fr.drawStringWithShadow(comment, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.COMMENT_COLOR));
                fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            }
            
            // 类结束大括号
            contentY += 15;
            fr.drawStringWithShadow("}", x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            
        } else {
            // 如果没有选中模块，显示IDE风格的欢迎界面
            String[] welcomeText = {"// Welcum to OpenMyau", "// u can click on modules and set settings."};
            
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
        
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int wheel = Mouse.getEventDWheel();
        
        if (wheel != 0) {
            int scrollAmount = wheel > 0 ? -SCROLL_SPEED : SCROLL_SPEED;
            
            if (mouseX <= NAVIGATION_WIDTH) {
                // 左侧导航栏滚动 - 添加边界检查
                int maxScrollOffset = calculateMaxNavigationScrollOffset();
                navigationScrollOffset += scrollAmount;
                navigationScrollOffset = Math.max(0, Math.min(maxScrollOffset, navigationScrollOffset));
            } else {
                // 右侧属性区域滚动 - 优先检查是否需要调整属性
                if (selectedModule != null) {
                    handlePropertyScrollAdjustment(mouseX, mouseY, wheel);
                } else {
                    // 没有选中模块时，直接滚动界面
                    propertiesScrollOffset += scrollAmount;
                    propertiesScrollOffset = Math.max(0, propertiesScrollOffset);
                }
            }
        }
    }
    
    @Override
    public void handleInput() throws IOException {
        // Allow movement keys to pass through to the game
        // wtf
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

        super.handleInput(); // Call super to handle GUI-specific input (like mouse clicks, escape key)

        // 遍历所有Frame中的所有组件，进行键盘事件处理
        for (Frame frame : this.frames) {
            Component component = frame.handleInput((char) Keyboard.getEventCharacter(), Keyboard.getEventKey());
            if (component != null) {
                if (component instanceof BindButton && ((BindButton) component).isListeningForKey()) {
                    this.listeningBindButton = (BindButton) component;
                } else {
                    this.draggingComponent = component;
                }
                return;
            }
        }
    }
    
    /**
     * 处理属性的滚轮调整
     */
    private void handlePropertyScrollAdjustment(int mouseX, int mouseY, int wheel) {
        if (selectedModule == null) return;
        
        ArrayList<Component> subComponents = selectedModule.getSubComponents();
        if (subComponents == null || subComponents.isEmpty()) return;
        
        // 计算属性区域内的相对位置（考虑滚动偏移）
        int contentStartY = TITLE_BAR_HEIGHT + 10;
        int adjustedContentY = contentStartY - propertiesScrollOffset;
        int currentY = adjustedContentY + 15 * 7; // 跳过包声明、导入、类声明等
        
        boolean propertyAdjusted = false;
        for (Component component : subComponents) {
            if (mouseY >= currentY && mouseY <= currentY + 15) {
                // 鼠标悬停在这个属性上，调整其值并阻止界面滚动
                adjustPropertyValue(component, wheel > 0);
                propertyAdjusted = true;
                break;
            }
            currentY += 15;
        }
        
        // 如果没有调整属性，才进行界面滚动
        if (!propertyAdjusted) {
            int scrollAmount = wheel > 0 ? -SCROLL_SPEED : SCROLL_SPEED;
            propertiesScrollOffset += scrollAmount;
            propertiesScrollOffset = Math.max(0, propertiesScrollOffset);
        }
    }
    
    /**
     * 处理右侧属性区域的点击交互
     */
    private void handlePropertyClick(int mouseX, int mouseY, int mouseButton) {
        if (selectedModule == null) return;
        
        ArrayList<Component> subComponents = selectedModule.getSubComponents();
        if (subComponents == null || subComponents.isEmpty()) return;
        
        // 计算属性区域内的相对位置（考虑滚动偏移）
        int contentStartY = TITLE_BAR_HEIGHT + 10;
        int adjustedContentY = contentStartY - propertiesScrollOffset;
        int currentY = adjustedContentY + 15 * 7; // 跳过包声明、导入、类声明等
        
        for (Component component : subComponents) {
            if (mouseY >= currentY && mouseY <= currentY + 15) {
                // 鼠标点击在这个属性上
                if (mouseButton == 0) { // 左键
                    adjustPropertyValue(component, true);
                } else if (mouseButton == 1) { // 右键
                    adjustPropertyValue(component, false);
                }
                break;
            }
            currentY += 15;
        }
    }
    
    /**
     * 计算导航栏的最大滚动偏移，防止内容完全消失
     */
    private int calculateMaxNavigationScrollOffset() {
        int totalHeight = 0;
        int categoryHeight = 20;
        
        for (Frame frame : frames) {
            totalHeight += categoryHeight; // 分类标题高度
            
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                if (moduleButtons.isEmpty()) {
                    totalHeight += 15; // 空提示文本高度
                } else {
                    totalHeight += moduleButtons.size() * 15; // 每个模块15像素高度
                }
            }
        }
        
        int availableHeight = this.height - (TITLE_BAR_HEIGHT + SEARCH_BAR_HEIGHT + SEARCH_BAR_Y_OFFSET * 2 + 5); // 可用显示高度, 加上搜索栏和顶部的5px偏移
        return Math.max(0, totalHeight - availableHeight);
    }
    
    /**
     * 调整属性值
     */
    private void adjustPropertyValue(Component component, boolean increase) {
        if (component instanceof Checkbox) {
            // 布尔值直接切换
            Checkbox checkbox = (Checkbox) component;
            try {
                java.lang.reflect.Field field = checkbox.getClass().getDeclaredField("booleanProperty");
                field.setAccessible(true);
                BooleanProperty property = (BooleanProperty) field.get(checkbox);
                property.setValue(!property.getValue());
            } catch (Exception e) {
                // 忽略错误
            }
        } else if (component instanceof Slider) {
            // 数值调整
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
                // 忽略错误
            }
        } else if (component instanceof ModeSelector) {
            // 模式切换
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
                // 忽略错误
            }
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.listeningBindButton != null) {
            this.listeningBindButton.keyTyped(typedChar, keyCode);
            if (!this.listeningBindButton.isListeningForKey()) {
                this.listeningBindButton = null;
            }
            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }

        // 处理搜索栏输入
        if (isSearching) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                }
            } else if (keyCode == Keyboard.KEY_RETURN) {
                isSearching = false;
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                searchQuery += typedChar;
            }
            filterModules();
            return; // Consume the event if we are searching
        }

        // 处理键盘导航
        if (selectedNavigationElement != null) {
            if (keyCode == Keyboard.KEY_UP) {
                navigateUp();
                return;
            } else if (keyCode == Keyboard.KEY_DOWN) {
                navigateDown();
                return;
            } else if (keyCode == Keyboard.KEY_RETURN) {
                activateSelectedElement();
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }
        
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.listeningBindButton != null) {
            this.listeningBindButton.keyTyped((char) 0, Keyboard.KEY_NONE);
            this.listeningBindButton = null;
        }

        // 检查窗口控制按钮点击
        if (handleWindowControlsClick(mouseX, mouseY, mouseButton)) {
            return;
        }

        // 处理搜索栏点击
        int searchBarX = BORDER_THICKNESS;
        int searchBarY = TITLE_BAR_HEIGHT + SEARCH_BAR_Y_OFFSET;
        int searchBarWidth = NAVIGATION_WIDTH - BORDER_THICKNESS * 2;
        int searchBarHeight = SEARCH_BAR_HEIGHT;

        if (mouseX >= searchBarX && mouseX <= searchBarX + searchBarWidth &&
            mouseY >= searchBarY && mouseY <= searchBarY + searchBarHeight) {
            isSearching = true;
        } else {
            isSearching = false;
        }

        // 处理窗口拖拽
        if (mouseButton == 0 && mouseX >= windowX && mouseX <= windowX + windowWidth && mouseY >= windowY && mouseY <= windowY + TITLE_BAR_HEIGHT) {
            this.isDraggingWindow = true;
            this.windowDragX = mouseX - windowX;
            this.windowDragY = mouseY - windowY;
            return;
        }

        // 处理窗口大小调整的点击
        int rightBottomX = windowX + windowWidth - RESIZE_HANDLE_SIZE;
        int rightBottomY = windowY + windowHeight - RESIZE_HANDLE_SIZE;

        // 检查右下角
        if (mouseX >= rightBottomX && mouseX <= windowX + windowWidth &&
            mouseY >= rightBottomY && mouseY <= windowY + windowHeight) {
            isResizingWindow = true;
            resizeDirection = 3; // Right-bottom
            resizeStartX = mouseX;
            resizeStartY = mouseY;
            initialWindowWidth = windowWidth;
            initialWindowHeight = windowHeight;
            return;
        }

        // 检查右边缘
        int rightEdgeX1 = windowX + windowWidth - RESIZE_HANDLE_SIZE;
        int rightEdgeY1 = windowY + RESIZE_HANDLE_SIZE;
        int rightEdgeX2 = windowX + windowWidth;
        int rightEdgeY2 = windowY + windowHeight - RESIZE_HANDLE_SIZE;
        if (mouseX >= rightEdgeX1 && mouseX <= rightEdgeX2 &&
            mouseY >= rightEdgeY1 && mouseY <= rightEdgeY2) {
            isResizingWindow = true;
            resizeDirection = 1; // Right
            resizeStartX = mouseX;
            initialWindowWidth = windowWidth;
            return;
        }

        // 检查下边缘
        int bottomEdgeX1 = windowX + RESIZE_HANDLE_SIZE;
        int bottomEdgeY1 = windowY + windowHeight - RESIZE_HANDLE_SIZE;
        int bottomEdgeX2 = windowX + windowWidth - RESIZE_HANDLE_SIZE;
        int bottomEdgeY2 = windowY + windowHeight;
        if (mouseX >= bottomEdgeX1 && mouseX <= bottomEdgeX2 &&
            mouseY >= bottomEdgeY1 && mouseY <= bottomEdgeY2) {
            isResizingWindow = true;
            resizeDirection = 2; // Bottom
            resizeStartY = mouseY;
            initialWindowHeight = windowHeight;
            return;
        }
        
        // Calculate content area Y and height for relative mouse coordinates
        int contentAreaY = TITLE_BAR_HEIGHT + SEARCH_BAR_HEIGHT + SEARCH_BAR_Y_OFFSET * 2;
        int contentAreaHeight = this.height - contentAreaY;

        // Adjust mouse Y to be relative to the content area for navigation panel and properties
        int relativeMouseY = mouseY - contentAreaY;

        // Handle clicks in the navigation panel (left side)
        if (mouseX <= NAVIGATION_WIDTH && mouseY >= contentAreaY && mouseY <= contentAreaY + contentAreaHeight) {
            int currentYInContent = 5 - navigationScrollOffset; // Initial Y position in content, affected by scroll
            int categoryHeight = 20;

            for (Frame frame : this.frames) {
                // Click on category title
                if (mouseX >= 5 && mouseX <= NAVIGATION_WIDTH - 5 &&
                    relativeMouseY >= currentYInContent && relativeMouseY <= currentYInContent + categoryHeight) {
                    
                    if (mouseButton == 0) { // Left click
                        this.selectedFrame = frame;
                        this.selectedModule = null;
                    } else if (mouseButton == 1) { // Right click
                        frame.toggleOpenClose();
                    }
                    selectedNavigationElement = frame; // Update selected navigation element
                    return; // Consume click event
                }
                currentYInContent += categoryHeight;

                // Click on module list
                if (frame.isExtended()) {
                    for (ModuleButton moduleButton : frame.getModuleButtons()) {
                        if (moduleButton.isVisible()) {
                            if (mouseX >= 10 && mouseX <= NAVIGATION_WIDTH - 5 &&
                                relativeMouseY >= currentYInContent && relativeMouseY <= currentYInContent + 15) {
                                
                                if (mouseButton == 0) { // Left click
                                    long currentTime = System.currentTimeMillis();
                                    if (moduleButton == lastClickedModuleButton && (currentTime - lastClickTime) < DOUBLE_CLICK_TIME_MS) {
                                        // Double-click detected, toggle module
                                        moduleButton.getModule().toggle(); 
                                        lastClickedModuleButton = null; // Reset double-click state
                                    } else {
                                        // Single click: Select module
                                        this.selectedModule = moduleButton;
                                        this.selectedFrame = frame;
                                        // Removed module toggle here for single click
                                    }
                                    lastClickTime = currentTime;
                                    lastClickedModuleButton = moduleButton;
                                }
                                selectedNavigationElement = moduleButton; // Update selected navigation element
                                return; // Consume click event
                            }
                            currentYInContent += 15; // Height of each module
                        }
                    }
                }
            }
        } else if (selectedFrame != null && mouseX > NAVIGATION_WIDTH && mouseY >= contentAreaY && mouseY <= contentAreaY + contentAreaHeight) {
            // Handle clicks in the properties area (right side)
            if (selectedModule != null) {
                handlePropertyClick(mouseX, mouseY, mouseButton);
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);

        // If a component was clicked and it's a draggable component, set it as the current dragging component
        // This logic remains here if frames or their subcomponents can be dragged independently
        for (Frame frame : this.frames) {
            Component clickedComponent = frame.mouseClicked(mouseX, mouseY, mouseButton);
            if (clickedComponent != null) {
                if (clickedComponent instanceof BindButton && ((BindButton) clickedComponent).isListeningForKey()) {
                    this.listeningBindButton = (BindButton) clickedComponent;
                } else {
                    this.draggingComponent = clickedComponent;
                }
                return;
            }
        }
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        // 处理窗口拖拽
        if (this.isDraggingWindow && clickedMouseButton == 0) {
            this.windowX = mouseX - this.windowDragX;
            this.windowY = mouseY - this.windowDragY;

            // 边界检查，防止窗口拖出屏幕
            this.windowX = Math.max(0, Math.min(this.width - windowWidth, this.windowX));
            this.windowY = Math.max(0, Math.min(this.height - windowHeight, this.windowY));
        }

        // 处理窗口大小调整
        if (isResizingWindow) {
            int deltaX = mouseX - resizeStartX;
            int deltaY = mouseY - resizeStartY;

            if (resizeDirection == 1 || resizeDirection == 3) { // Right or Right-bottom
                windowWidth = Math.max(200, initialWindowWidth + deltaX); // 最小宽度200
            }
            if (resizeDirection == 2 || resizeDirection == 3) { // Bottom or Right-bottom
                windowHeight = Math.max(150, initialWindowHeight + deltaY); // 最小高度150
            }
            // No need to set resizeStartX/Y again, as delta is calculated from initial
        }

        // 更新拖拽组件位置
        if (this.draggingComponent != null) {
            this.draggingComponent.updatePosition(mouseX, mouseY);
        }
    }
    
    /**
     * 处理窗口控制按钮点击
     */
    private boolean handleWindowControlsClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;
        
        int buttonSize = 20;
        int buttonY = (TITLE_BAR_HEIGHT - buttonSize) / 2;
        
        // 关闭按钮
        int closeX = this.width - buttonSize - 5;
        if (mouseX >= closeX && mouseX <= closeX + buttonSize && 
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            this.mc.displayGuiScreen(null);
            return true;
        }
        
        // 最大化按钮（全屏模式下无意义，但保留以保持一致性）
        int maximizeX = closeX - buttonSize - 2;
        if (mouseX >= maximizeX && mouseX <= maximizeX + buttonSize && 
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            // 在全屏模式下，最大化按钮不执行任何操作
            return true;
        }
        
        // 最小化按钮（直接关闭）
        int minimizeX = maximizeX - buttonSize - 2;
        if (mouseX >= minimizeX && mouseX <= minimizeX + buttonSize && 
            mouseY >= buttonY && mouseY <= buttonY + buttonSize) {
            this.mc.displayGuiScreen(null);
            return true;
        }
        
        return false;
    }

    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.draggingComponent != null) {
            this.draggingComponent.onMouseReleased(mouseX, mouseY, state);
            this.draggingComponent = null;
        }

        this.isResizingWindow = false; // Reset resizing flag on mouse release

        for (Frame frame : this.frames) {
            frame.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }
    
    /**
     * 恢复选中的状态（分类、模块等）
     */
    private void restoreSelectedState() {
        // 恢复选中的分类
        if (lastSelectedCategory != null) {
            for (Frame frame : frames) {
                if (frame.getCategory().getName().equals(lastSelectedCategory)) {
                    selectedFrame = frame;
                    break;
                }
            }
        }
        
        // 恢复选中的模块
        if (lastSelectedModule != null && selectedFrame != null) {
            List<ModuleButton> moduleButtons = selectedFrame.getModuleButtons();
            for (ModuleButton moduleButton : moduleButtons) {
                if (moduleButton.getModule().getName().equals(lastSelectedModule)) {
                    selectedModule = moduleButton;
                    break;
                }
            }
        }
        
        // 如果没有恢复到模块但恢复了分类，尝试在其他分类中查找模块
        if (lastSelectedModule != null && selectedModule == null) {
            for (Frame frame : frames) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                for (ModuleButton moduleButton : moduleButtons) {
                    if (moduleButton.getModule().getName().equals(lastSelectedModule)) {
                        selectedModule = moduleButton;
                        selectedFrame = frame; // 同时更新选中的分类
                        break;
                    }
                }
                if (selectedModule != null) {
                    break;
                }
            }
        }
        
        // 调试输出
        System.out.println("[ClickGUI] State restored:");
        System.out.println("  - Selected Category: " + (selectedFrame != null ? selectedFrame.getCategory().getName() : "None"));
        System.out.println("  - Selected Module: " + (selectedModule != null ? selectedModule.getModule().getName() : "None"));
        System.out.println("  - Navigation Scroll: " + savedNavigationScrollOffset);
        System.out.println("  - Properties Scroll: " + savedPropertiesScrollOffset);
    }

    /**
     * 向上导航选中的元素
     */
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

        int currentIndex = navigableElements.indexOf(selectedNavigationElement);
        if (currentIndex == -1) {
            selectedNavigationElement = navigableElements.get(0);
            return;
        }

        int newIndex = currentIndex - 1;
        if (newIndex < 0) {
            newIndex = navigableElements.size() - 1; // 循环到最后一个元素
        }
        selectedNavigationElement = navigableElements.get(newIndex);
        ensureVisible(selectedNavigationElement);
    }

    /**
     * 向下导航选中的元素
     */
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

        int currentIndex = navigableElements.indexOf(selectedNavigationElement);
        if (currentIndex == -1) {
            selectedNavigationElement = navigableElements.get(0);
            return;
        }

        int newIndex = currentIndex + 1;
        if (newIndex >= navigableElements.size()) {
            newIndex = 0; // 循环到第一个元素
        }
        selectedNavigationElement = navigableElements.get(newIndex);
        ensureVisible(selectedNavigationElement);
    }

    /**
     * 确保选中的元素在可见区域内
     */
    private void ensureVisible(Object element) {
        int elementY = -1; // 元素的实际Y坐标
        int elementHeight = 0;

        int currentY = TITLE_BAR_HEIGHT + SEARCH_BAR_HEIGHT + SEARCH_BAR_Y_OFFSET * 2 + 5 - navigationScrollOffset; // 绘制内容的起始Y位置
        int categoryHeight = 20;
        int contentAreaY = TITLE_BAR_HEIGHT + SEARCH_BAR_HEIGHT + SEARCH_BAR_Y_OFFSET * 2; // 内容区域的起始Y坐标
        int contentAreaHeight = this.height - contentAreaY; // 内容区域的高度

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
            // 检查元素是否在可见区域上方
            if (elementY < contentAreaY) {
                navigationScrollOffset -= (contentAreaY - elementY); // 向上滚动
            }
            // 检查元素是否在可见区域下方
            else if (elementY + elementHeight > contentAreaY + contentAreaHeight) {
                navigationScrollOffset += (elementY + elementHeight - (contentAreaY + contentAreaHeight)); // 向下滚动
            }
            // 限制navigationScrollOffset的范围
            int maxScrollOffset = calculateMaxNavigationContentHeight() - contentAreaHeight; // 可滚动内容的总高度
            navigationScrollOffset = Math.max(0, Math.min(maxScrollOffset, navigationScrollOffset));
        }
    }

    /**
     * 激活选中的元素（切换分类或模块）
     */
    private void activateSelectedElement() {
        if (selectedNavigationElement instanceof Frame) {
            Frame frame = (Frame) selectedNavigationElement;
            selectedFrame = frame; // 选中框架
            selectedModule = null; // 取消模块选中
            frame.toggleOpenClose(); // 切换展开/折叠状态
        } else if (selectedNavigationElement instanceof ModuleButton) {
            ModuleButton moduleButton = (ModuleButton) selectedNavigationElement;
            selectedModule = moduleButton; // 选中模块
            selectedFrame = moduleButton.getParentFrame(); // 选中模块所属的框架
            moduleButton.getModule().toggle(); // 切换模块启用状态
        }
    }

    /**
     * 计算导航栏所有内容的总高度 (包括折叠/展开的模块)
     */
    private int calculateMaxNavigationContentHeight() {
        int totalHeight = 0;
        int categoryHeight = 20;
        for (Frame frame : frames) {
            totalHeight += categoryHeight; // 分类标题高度
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                if (moduleButtons.isEmpty()) {
                    totalHeight += 15; // 空提示文本高度
                } else {
                    totalHeight += moduleButtons.size() * 15; // 每个模块15像素高度
                }
            }
        }
        return totalHeight;
    }
    
    @Override
    public void onGuiClosed() {
        // Save GUI state if enabled
        GuiModule guiModule = (GuiModule) myau.Myau.moduleManager.getModule("GuiModule");
        if (guiModule != null && guiModule.saveGuiState.getValue()) {
            System.out.println("[ClickGUI] Saving GUI state...");
            
            // Save frame positions
            framePositions.clear();
            for (Frame frame : this.frames) {
                String categoryName = frame.getCategory().getName();
                int[] position = new int[]{frame.getX(), frame.getY()};
                framePositions.put(categoryName, position);
                System.out.println("  - Frame position saved: " + categoryName + " at (" + position[0] + ", " + position[1] + ")");
            }
            
            // Save category expanded states
            categoryExpandedStates.clear();
            for (Frame frame : this.frames) {
                String categoryName = frame.getCategory().getName();
                boolean isExpanded = frame.isExtended();
                categoryExpandedStates.put(categoryName, isExpanded);
                System.out.println("  - Category state saved: " + categoryName + " -> " + (isExpanded ? "Expanded" : "Collapsed"));
            }
            
            // Save selected category
            if (selectedFrame != null) {
                lastSelectedCategory = selectedFrame.getCategory().getName();
                System.out.println("  - Selected category saved: " + lastSelectedCategory);
            } else {
                lastSelectedCategory = null;
                System.out.println("  - No selected category to save");
            }
            
            // Save selected module
            if (selectedModule != null) {
                lastSelectedModule = selectedModule.getModule().getName();
                System.out.println("  - Selected module saved: " + lastSelectedModule);
            } else {
                lastSelectedModule = null;
                System.out.println("  - No selected module to save");
            }
            
            // Save scroll offsets
            savedNavigationScrollOffset = this.navigationScrollOffset;
            savedPropertiesScrollOffset = this.propertiesScrollOffset;
            System.out.println("  - Scroll offsets saved: Navigation=" + savedNavigationScrollOffset + ", Properties=" + savedPropertiesScrollOffset);
            
            System.out.println("[ClickGUI] GUI state saved successfully!");
        } else {
            System.out.println("[ClickGUI] GUI state saving is disabled");
        }
        
        // Disable ClickGUI module when closing the screen
        myau.Myau.moduleManager.getModule("ClickGUI").setEnabled(false);
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * 过滤模块列表以匹配搜索查询
     */
    private void filterModules() {
        String lowerCaseQuery = searchQuery.toLowerCase();

        for (Frame frame : frames) {
            boolean categoryMatches = frame.getCategory().getName().toLowerCase().contains(lowerCaseQuery);
            boolean hasVisibleModule = false;

            for (ModuleButton moduleButton : frame.getModuleButtons()) {
                boolean moduleMatches = moduleButton.getModule().getName().toLowerCase().contains(lowerCaseQuery);
                if (searchQuery.isEmpty()) {
                    // If no search query, all modules are visible
                    moduleButton.setVisible(true);
                    hasVisibleModule = true; // Mark that at least one module is visible
                } else if (moduleMatches) {
                    moduleButton.setVisible(true);
                    hasVisibleModule = true;
                } else {
                    moduleButton.setVisible(false);
                }
            }

            // Set category extended state based on search results
            if (searchQuery.isEmpty()) {
                frame.setExtended(true); // If no search query, all categories are extended
            } else if (categoryMatches || hasVisibleModule) {
                frame.setExtended(true); // If category matches or contains visible modules, extend it
            } else {
                frame.setExtended(false); // Otherwise, collapse it
            }
        }
    }

    /**
     * 渲染自定义滚动条
     */
    private void renderScrollbar(int x, int y, int width, int height, int contentHeight, int scrollOffset, int mouseX, int mouseY) {
        if (contentHeight <= height) return; // 内容未超出区域，无需滚动条

        float scrollbarHeight = (float) height / contentHeight * height; // 滚动条高度
        float scrollbarY = y + (float) scrollOffset / contentHeight * height; // 滚动条Y位置

        // 绘制滚动条背景
        Gui.drawRect(x, y, x + width, y + height, IntelliJTheme.getRGBWithAlpha(IntelliJTheme.BACKGROUND_COLOR, 100));

        // 绘制滚动条滑块
        int scrollbarColor = IntelliJTheme.getRGB(IntelliJTheme.SCROLLBAR_COLOR);
        if (mouseX >= x && mouseX <= x + width && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight) {
            scrollbarColor = IntelliJTheme.getRGB(IntelliJTheme.SCROLLBAR_HOVER_COLOR);
        }
        Gui.drawRect(x + 1, (int) scrollbarY, x + width - 1, (int) (scrollbarY + scrollbarHeight), scrollbarColor);
    }
}
