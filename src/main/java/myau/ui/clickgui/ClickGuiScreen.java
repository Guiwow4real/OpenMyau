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
    
    // 窗口标题栏相关变量
    private boolean isDraggingWindow = false;
    private int windowDragX = 0;
    private int windowDragY = 0;
    private int windowX = 200; // 窗口X位置
    private int windowY = 80;  // 窗口Y位置
    private int windowWidth = 650;  // 窗口宽度
    private int windowHeight = 480; // 窗口高度
    
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
        
        // 如果没有恢复到选中的框架，默认选中第一个框架
        if (selectedFrame == null && !frames.isEmpty()) {
            selectedFrame = frames.get(0);
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
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
        
        // 渲染导航栏
        renderNavigationPanel();
        
        // 渲染模块属性区域
        renderModuleProperties(NAVIGATION_WIDTH + BORDER_THICKNESS, contentY, 
                              this.width - NAVIGATION_WIDTH - BORDER_THICKNESS, contentHeight, mouseX, mouseY);
        
        // 更新拖拽组件位置
        if (this.draggingComponent != null) {
            this.draggingComponent.updatePosition(mouseX, mouseY);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
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
     * 渲染左侧导航栏，模仿IntelliJ IDEA项目结构（支持滚动）
     */
    private void renderNavigationPanel() {
        FontRenderer fr = this.mc.fontRendererObj;
        int categoryHeight = 20; // 每个分类的高度
        int categoryY = TITLE_BAR_HEIGHT + 5; // 起始Y位置，在标题栏下方（分类标题不受滚动影响）
        
        // 调试信息：检查frames列表
        if (frames.isEmpty()) {
            // 显示调试信息
            String debugInfo = "No frames found! Categories: ";
            for (Category category : Category.values()) {
                debugInfo += category.getName() + " ";
            }
            debugInfo += "Total modules: " + Myau.moduleManager.modules.size();
            
            fr.drawString(debugInfo, 15, categoryY, 0xFFFFFF);
            categoryY += 12;
            
            // 显示所有分类
            for (Category category : Category.values()) {
                String categoryInfo = "Category: " + category.getName() + ", Modules: " + 
                    Myau.moduleManager.getModulesInCategory(category).size();
                fr.drawString(categoryInfo, 15, categoryY, 0xFFFFFF);
                categoryY += 12;
            }
            return;
        }
        
        // 正常渲染frames
        for (Frame frame : frames) {
            // 检查分类标题是否在可见区域内（分类标题始终可见，不受滚动影响）
            if (categoryY + categoryHeight > TITLE_BAR_HEIGHT && categoryY < this.height) {
                // 绘制包背景
                if (frame == selectedFrame) {
                    Gui.drawRect(5, categoryY, NAVIGATION_WIDTH - 5, categoryY + categoryHeight, SELECTED_BG_COLOR);
                }
                
                // 绘制包名称 - 使用包命名格式
                String categoryName = frame.getCategory().getName();
                String packageName = "myau.modules." + categoryName.toLowerCase();
                fr.drawString(packageName, 15, categoryY + 6, IntelliJTheme.getRGB(IntelliJTheme.TYPE_VALUE_COLOR));
                
                // 绘制展开/折叠指示器
                String indicator = frame.isExtended() ? "▼" : "▶";
                fr.drawString(indicator, NAVIGATION_WIDTH - 15, categoryY + 6, HEADER_TEXT_COLOR);
            }
            
            categoryY += categoryHeight;
            
            // 如果包展开，显示该包下的类（模块）- 只有模块部分受滚动影响
            if (frame.isExtended()) {
                List<ModuleButton> moduleButtons = frame.getModuleButtons();
                
                // 计算模块区域的滚动位置 - 只有模块受滚动影响
                int moduleStartY = categoryY - navigationScrollOffset;
                
                if (moduleButtons.isEmpty()) {
                    // 如果该分类下没有模块，显示提示
                    if (moduleStartY >= TITLE_BAR_HEIGHT && moduleStartY < this.height) {
                        String noModulesText = "    // No modules in this category";
                        fr.drawString(noModulesText, 20, moduleStartY + 4, IntelliJTheme.getRGB(IntelliJTheme.DISABLED_TEXT_COLOR));
                    }
                    categoryY += 15;
                } else {
                    for (ModuleButton moduleButton : moduleButtons) {
                        int moduleY = moduleStartY;
                        
                        // 检查是否在可见区域内
                        if (moduleY >= TITLE_BAR_HEIGHT - 15 && moduleY < this.height + 15) {
                            // 绘制类背景
                            if (moduleButton == selectedModule) {
                                Gui.drawRect(10, moduleY, NAVIGATION_WIDTH - 5, moduleY + 15, SELECTED_BG_COLOR);
                            }
                            
                            // 绘制类名称 - 使用类命名格式
                            String moduleName = moduleButton.getModule().getName();
                            String className = moduleName + ".java";
                            
                            // 根据模块状态设置颜色
                            int textColor = moduleButton.getModule().isEnabled() ? 
                                IntelliJTheme.getRGB(IntelliJTheme.VARIABLE_NAME_COLOR) : // 紫色表示启用
                                IntelliJTheme.getRGB(IntelliJTheme.DISABLED_TEXT_COLOR);   // 灰色表示禁用
                            
                            fr.drawString(className, 20, moduleY + 4, textColor);
                        }
                        
                        categoryY += 15;
                        moduleStartY += 15;
                    }
                }
            }
        }
    }
    
    /**
     * 渲染模块属性 - IDE代码编辑器风格（支持滚动）
     */
    private void renderModuleProperties(int x, int y, int width, int height, int mouseX, int mouseY) {
        FontRenderer fr = this.mc.fontRendererObj;
        
        // IDE代码编辑器风格背景
        Gui.drawRect(x, y, x + width, y + height, IntelliJTheme.getRGB(IntelliJTheme.BACKGROUND_COLOR));
        
        // 绘制行号区域背景
        int lineNumberWidth = 40;
        Gui.drawRect(x, y, x + lineNumberWidth, y + height, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_BG_COLOR));
        
        // 绘制行号区域分隔线
        Gui.drawRect(x + lineNumberWidth, y, x + lineNumberWidth + 1, y + height, IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR));
        
        // 绘制代码区域背景
        Gui.drawRect(x + lineNumberWidth + 1, y, x + width, y + height, IntelliJTheme.getRGB(IntelliJTheme.CODE_BG_COLOR));
        
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
            String[] imports = {
                "import myau.module.Module;",
                "import myau.module.Category;",
                "import myau.property.properties.*;"
            };
            
            for (String importStmt : imports) {
                fr.drawStringWithShadow(importStmt, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.KEYWORD_COLOR));
                fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
                contentY += 15;
            }
            
            // 空行
            contentY += 5;
            fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
            contentY += 15;
            
            // 类声明 - 使用新的类名颜色（绿色）
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
            String[] welcomeText = {
                "// Welcum to OpenMyau"
            };
            
            for (String line : welcomeText) {
                fr.drawStringWithShadow(line, x + lineNumberWidth + 10, contentY, IntelliJTheme.getRGB(IntelliJTheme.COMMENT_COLOR));
                fr.drawStringWithShadow(String.valueOf(lineNumber++), x + 5, contentY, IntelliJTheme.getRGB(IntelliJTheme.LINE_NUMBER_COLOR));
                contentY += 15;
            }
        }
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
                // 左侧导航栏滚动
                navigationScrollOffset += scrollAmount;
                navigationScrollOffset = Math.max(0, navigationScrollOffset);
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
    }
    
    /**
     * 处理属性的滚轮调整
     */
    private void handlePropertyScrollAdjustment(int mouseX, int mouseY, int wheel) {
        if (selectedModule == null) return;
        
        ArrayList<Component> subComponents = selectedModule.getSubComponents();
        if (subComponents == null || subComponents.isEmpty()) return;
        
        // 计算属性区域内的相对位置
        int contentStartY = 10 - propertiesScrollOffset; // 考虑滚动偏移
        int currentY = contentStartY + 15 * 7; // 跳过包声明、导入、类声明等
        
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
        
        // 计算属性区域内的相对位置
        int contentStartY = 10 - propertiesScrollOffset; // 考虑滚动偏移
        int currentY = contentStartY + 15 * 7; // 跳过包声明、导入、类声明等
        
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
        
        // 调整鼠标坐标为相对内容的坐标
        int relativeMouseY = mouseY - TITLE_BAR_HEIGHT;

        if (mouseX <= NAVIGATION_WIDTH && relativeMouseY >= 0) {
            int categoryY = 5; // 分类标题不受滚动影响
            int categoryHeight = 20;

            for (Frame frame : this.frames) {
                // 点击分类标题 - 分类标题不受滚动影响
                if (mouseX >= 5 && mouseX <= NAVIGATION_WIDTH - 5 &&
                    relativeMouseY >= categoryY && relativeMouseY <= categoryY + categoryHeight) {

                    if (mouseButton == 0) {
                        this.selectedFrame = frame;
                        this.selectedModule = null;
                        return;
                    } else if (mouseButton == 1) {
                        frame.toggleOpenClose();
                        return;
                    }
                }

                // 点击模块列表 - 只有模块点击受滚动影响
                int moduleY = categoryY + categoryHeight;
                if (frame.isExtended()) {
                    for (ModuleButton moduleButton : frame.getModuleButtons()) {
                        int adjustedModuleY = moduleY - navigationScrollOffset; // 只有模块受滚动影响
                        if (mouseX >= 10 && mouseX <= NAVIGATION_WIDTH - 5 &&
                            relativeMouseY >= adjustedModuleY && relativeMouseY <= adjustedModuleY + 15) {

                            if (mouseButton == 0) {
                                this.selectedModule = moduleButton;
                                this.selectedFrame = frame;
                                return;
                            }
                        }
                        moduleY += 15; // 每个模块高度
                    }
                }

                // 更新下一个分类的Y坐标，累加展开模块高度
                categoryY = moduleY;
            }

            // 点击框架内部组件
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

        } else if (selectedFrame != null && relativeMouseY >= 0) {
            // 右侧内容区域的点击交互 - 属性区域点击处理
            if (selectedModule != null) {
                handlePropertyClick(mouseX, mouseY, mouseButton);
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
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
    }
    
    @Override
    public void onGuiClosed() {
        // Save GUI state if enabled
        GuiModule guiModule = (GuiModule) myau.Myau.moduleManager.modules.get(GuiModule.class);
        if (guiModule != null && guiModule.saveGuiState.getValue()) {
            // Save frame positions
            framePositions.clear();
            for (Frame frame : this.frames) {
                String categoryName = frame.getCategory().getName();
                int[] position = new int[]{frame.getX(), frame.getY()};
                framePositions.put(categoryName, position);
            }
            
            // Save category expanded states
            categoryExpandedStates.clear();
            for (Frame frame : this.frames) {
                String categoryName = frame.getCategory().getName();
                categoryExpandedStates.put(categoryName, frame.isExtended());
            }
            
            // Save selected category
            if (selectedFrame != null) {
                lastSelectedCategory = selectedFrame.getCategory().getName();
            }
            
            // Save selected module
            if (selectedModule != null) {
                lastSelectedModule = selectedModule.getModule().getName();
            }
        }
        
        // Disable ClickGUI module when closing the screen
        myau.Myau.moduleManager.getModule("ClickGUI").setEnabled(false);
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
