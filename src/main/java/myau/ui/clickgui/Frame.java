package myau.ui.clickgui;

import myau.Myau;
import myau.module.Category;
import myau.module.Module;
import myau.ui.clickgui.component.Component;
import myau.ui.clickgui.component.ModuleButton;
import myau.ui.clickgui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import myau.util.RenderUtils; // Import RenderUtils
import myau.util.RenderUtil; // Import RenderUtil

import java.util.ArrayList;
import java.awt.Color;
import java.util.List;

public class Frame {

    private int x, y, width, height, dragX, dragY;
    private final Category category;
    private boolean dragging;
    private boolean open;
    private boolean opening; // 新增：标记正在打开
    private boolean closing; // 新增：标记正在关闭
    
    /**
     * 检查Frame是否展开（用于导航栏显示）
     */
    public boolean isExtended() {
        return open;
    }
    private float animationProgress; // 新增：动画进度 (0.0 - 1.0)
    private long lastUpdateTime; // 新增：上次更新时间
    private final ArrayList<Component> components;
    
    // 新增：选中状态颜色
    private static final int HEADER_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
    private static final int HEADER_HOVER_COLOR = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);
    private static final int HEADER_SELECTED_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SELECTED_COLOR);
    private static final int HEADER_TEXT_COLOR = IntelliJTheme.getRGB(IntelliJTheme.TEXT_COLOR);
    private static final int BORDER_COLOR = IntelliJTheme.getRGB(IntelliJTheme.BORDER_COLOR);
    private static final int BACKGROUND_COLOR = IntelliJTheme.getRGB(IntelliJTheme.BACKGROUND_COLOR);

    protected Minecraft mc = Minecraft.getMinecraft();
    protected FontRenderer fr = mc.fontRendererObj;

    public Frame(Category category) {
        this.category = category;
        this.components = new ArrayList<>();
        this.x = 5;
        this.y = 5;
        this.width = 140; // 默认宽度，将在ClickGuiScreen中动态调整
        this.height = 18; // Header height
        this.open = true;
        this.opening = false;
        this.closing = false;
        this.animationProgress = 1.0f; // 默认完全展开
        this.lastUpdateTime = System.currentTimeMillis();
        this.dragging = false;

        // 初始化组件列表。组件的实际 Y 坐标将在 render 方法中动态计算。
        for (Module module : Myau.moduleManager.getModulesInCategory(this.category)) {
            // 初始 yOffset 设为 0 或其他占位值，render 方法会覆盖它
            this.components.add(new ModuleButton(module, this, 0));
        }
    }

    /**
     * 渲染 Frame（包括标题和所有组件）。
     * IntelliJ IDEA风格布局实现
     */
    public void render(int mouseX, int mouseY) {
        // 更新动画状态
        updateAnimation();
        
        // IntelliJ风格标题栏 - 检查鼠标悬停和选中状态
        boolean isMouseOverHeader = isMouseOnHeader(mouseX, mouseY);
        boolean isSelected = ClickGuiScreen.getInstance() != null && ClickGuiScreen.getInstance().getSelectedFrame() == this;
        int headerColor = isSelected ? HEADER_SELECTED_COLOR : (isMouseOverHeader ? HEADER_HOVER_COLOR : HEADER_COLOR);
        
        // 使用IntelliJ主题的圆角半径
        double cornerRadius = IntelliJTheme.CORNER_RADIUS;
        
        // 1. 绘制 Frame 标题栏 (Header) - IntelliJ风格带圆角
        if (cornerRadius > 0) {
            // 使用圆角矩形绘制标题栏
            RenderUtil.drawRoundedRect(this.x, this.y, this.width, this.height, cornerRadius, headerColor);
            // 绘制圆角边框
            RenderUtil.drawRoundedOutline(this.x, this.y, this.width, this.height, cornerRadius, 1.0f, BORDER_COLOR);
        } else {
            // 无圆角时使用原来的绘制方式
            Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, headerColor);
            
            // IntelliJ风格边框效果
            Gui.drawRect(this.x, this.y, this.x + this.width, this.y + 1, BORDER_COLOR); // 顶部边框
            Gui.drawRect(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, BORDER_COLOR); // 底部边框
        }
        
        // IntelliJ风格文字渲染
        String categoryName = this.category.getName();
        int maxWidth = this.width - 16; // 左右各留8px边距
        
        // 如果文本宽度超过最大宽度，则截断并添加省略号
        if (fr.getStringWidth(categoryName) > maxWidth) {
            String truncated = fr.trimStringToWidth(categoryName, maxWidth - fr.getStringWidth("..."));
            fr.drawString(truncated + "...", this.x + 8, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, HEADER_TEXT_COLOR);
        } else {
            fr.drawString(categoryName, this.x + 8, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, HEADER_TEXT_COLOR);
        }

        // 计算动画高度
        int animatedHeight = (int) (getTotalComponentsHeight() * animationProgress);

        if (animatedHeight > 0) {
            int currentY = this.y + this.height; // 组件起始 Y 坐标，在标题栏下方

            // 2. 第一次遍历：更新组件位置
            for (Component component : this.components) {
                // 设置当前组件的位置
                component.y = currentY;
                component.x = this.x;

                // 更新下一组件的起始 Y 坐标
                int componentTotalHeight = component.getHeight();
                if (component instanceof ModuleButton && ((ModuleButton) component).isOpen()) {
                    componentTotalHeight += ((ModuleButton) component).getSubComponentsHeight();
                }
                currentY += componentTotalHeight;
            }

            // 3. 绘制组件背景 - IntelliJ风格带圆角
            if (cornerRadius > 0) {
                // 使用圆角矩形绘制组件背景
                RenderUtil.drawRoundedRect(this.x, this.y + this.height, this.width, animatedHeight, cornerRadius, BACKGROUND_COLOR);
                // 绘制圆角边框
                RenderUtil.drawRoundedOutline(this.x, this.y + this.height, this.width, animatedHeight, cornerRadius, 1.0f, BORDER_COLOR);
            } else {
                // 背景从标题栏底部开始，延伸到动画计算的高度
                Gui.drawRect(this.x, this.y + this.height, this.x + this.width, this.y + this.height + animatedHeight, BACKGROUND_COLOR);
                
                // IntelliJ风格边框效果
                Gui.drawRect(this.x, this.y + this.height, this.x + this.width, this.y + this.height + 1, BORDER_COLOR); // 组件区域顶部边框
                Gui.drawRect(this.x, this.y + this.height + animatedHeight - 1, this.x + this.width, this.y + this.height + animatedHeight, BORDER_COLOR); // 组件区域底部边框
            }

            // 4. 渲染组件（根据动画进度决定渲染哪些组件）
            renderComponentsWithAnimation(mouseX, mouseY, animatedHeight);
        }
    }

    /**
     * 根据动画进度渲染组件
     */
    private void renderComponentsWithAnimation(int mouseX, int mouseY, int availableHeight) {
        int currentY = this.y + this.height;
        int remainingHeight = availableHeight;
        int screenHeight = new ScaledResolution(mc).getScaledHeight(); // 获取屏幕高度

        for (Component component : this.components) {
            int componentTotalHeight = component.getHeight();
            if (component instanceof ModuleButton && ((ModuleButton) component).isOpen()) {
                componentTotalHeight += ((ModuleButton) component).getSubComponentsHeight();
            }

            // 检查是否有足够的空间渲染这个组件，并且不会超出屏幕高度
            if (remainingHeight >= component.getHeight() && currentY + component.getHeight() <= screenHeight) {
                component.render(mouseX, mouseY);
                
                // 更新剩余高度
                remainingHeight -= componentTotalHeight;
                currentY += componentTotalHeight;
            } else {
                // 空间不足或超出屏幕高度，不再渲染更多组件
                break;
            }
        }
    }

    /**
     * 获取所有组件的总高度
     */
    private int getTotalComponentsHeight() {
        int totalHeight = 0;
        for (Component component : this.components) {
            int componentTotalHeight = component.getHeight();
            if (component instanceof ModuleButton && ((ModuleButton) component).isOpen()) {
                componentTotalHeight += ((ModuleButton) component).getSubComponentsHeight();
            }
            totalHeight += componentTotalHeight;
        }
        return totalHeight;
    }

    /**
     * 更新动画状态
     */
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f; // 转换为秒
        lastUpdateTime = currentTime;

        // 默认动画速度（转换为每秒完成的比例）
        float animationSpeed = 2.0f; // 默认值，范围 0.5 - 5.5

        if (opening) {
            animationProgress += deltaTime * animationSpeed;
            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                opening = false;
            }
        } else if (closing) {
            animationProgress -= deltaTime * animationSpeed;
            if (animationProgress <= 0.0f) {
                animationProgress = 0.0f;
                closing = false;
            }
        }
    }

    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOnHeader(mouseX, mouseY)) {
            if (mouseButton == 0) {
                // 设置此Frame为选中状态
                ClickGuiScreen clickGui = ClickGuiScreen.getInstance();
                if (clickGui != null) {
                    clickGui.setSelectedFrame(this);
                }
                this.dragging = true;
                this.dragX = mouseX - this.x;
                this.dragY = mouseY - this.y;
            } else if (mouseButton == 1) {
                toggleOpenClose();
            }
            return null; // Frame header click, no component drag initiated
        }

        // 只有在完全打开时才能与组件交互
        if (this.open && animationProgress >= 1.0f) {
            for (Component component : this.components) {
                Component clickedComponent = component.mouseClicked(mouseX, mouseY, mouseButton);
                if (clickedComponent != null) {
                    return clickedComponent; // A component initiated a drag
                }
            }
        }
        return null; // No component drag initiated
    }

    /**
     * 切换打开/关闭状态以启动动画
     */
    public void toggleOpenClose() {
        // 重置动画时间
        lastUpdateTime = System.currentTimeMillis();
        
        if (open) {
            // 正在打开，现在要关闭
            opening = false;
            closing = true;
            open = false;
        } else {
            // 正在关闭，现在要打开
            closing = false;
            opening = true;
            open = true;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        this.dragging = false;
        // ClickGuiScreen will handle calling onMouseReleased on the specific draggingComponent.
        // This method only needs to handle the frame's own dragging state.
    }

    public void updatePosition(int mouseX, int mouseY) {
        if (this.dragging) {
            this.x = mouseX - this.dragX;
            this.y = mouseY - this.dragY;
        }
    }

    private boolean isMouseOnHeader(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    // --- Getters ---

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }
    
    public boolean isOpen() {
        return open;
    }
    
    public Category getCategory() {
        return category;
    }
    
    /**
     * 获取此Frame中的所有ModuleButton组件
     * @return ModuleButton列表
     */
    public List<ModuleButton> getModuleButtons() {
        List<ModuleButton> moduleButtons = new ArrayList<>();
        for (Component component : components) {
            if (component instanceof ModuleButton) {
                moduleButtons.add((ModuleButton) component);
            }
        }
        return moduleButtons;
    }
}