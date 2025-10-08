package myau.ui.clickgui.component;

import myau.Myau;
import myau.module.Module;
import myau.property.Property;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.property.properties.ColorProperty;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.impl.Checkbox;
import myau.ui.clickgui.component.impl.Slider;
import myau.ui.clickgui.component.impl.HideButton;
import myau.ui.clickgui.component.impl.BindButton;
import myau.ui.clickgui.component.impl.ModeSelector;
import myau.ui.clickgui.component.impl.ColorPicker;
import myau.ui.clickgui.component.impl.RunPauseButton; // Import RunPauseButton
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import myau.ui.clickgui.IntelliJTheme;
import myau.util.RenderUtils; // Import RenderUtils
import java.awt.Color;

import java.util.ArrayList;

public class ModuleButton extends Component {

    private final Module module;
    private final ArrayList<Component> subComponents;
    private boolean open;
    private boolean opening; // 新增：标记正在打开
    private boolean closing; // 新增：标记正在关闭
    private float animationProgress; // 新增：动画进度 (0.0 - 1.0)
    private long lastUpdateTime; // 新增：上次更新时间
    private boolean visible; // 新增：控制模块按钮的可见性
    private RunPauseButton runPauseButton; // 新增：运行/暂停按钮
    
    // IntelliJ IDEA主题颜色
    private static final int BACKGROUND_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SECONDARY_BACKGROUND);
    private static final int SELECTED_COLOR = IntelliJTheme.getRGB(IntelliJTheme.SELECTED_COLOR);
    private static final int HOVER_COLOR = IntelliJTheme.getRGB(IntelliJTheme.HOVER_COLOR);
    private static final int MODULE_NAME_COLOR = IntelliJTheme.getRGB(IntelliJTheme.VARIABLE_NAME_COLOR);
    private static final float DEFAULT_ANIMATION_SPEED = IntelliJTheme.ANIMATION_SPEED; // IntelliJ动画速度

    private static final int RUN_BUTTON_WIDTH = 15; // 运行按钮宽度
    private static final int RUN_BUTTON_PADDING = 2; // 运行按钮的左右内边距

    public ModuleButton(Module module, Frame parent, int yOffset) {
        super(parent, parent.getX(), parent.getY() + yOffset, parent.getWidth(), 16);
        this.module = module;
        this.subComponents = new ArrayList<>();
        this.open = false;
        this.opening = false;
        this.closing = false;
        this.animationProgress = 0.0f; // 默认完全关闭
        this.lastUpdateTime = System.currentTimeMillis();
        this.visible = true; // 默认可见

        // 初始化运行/暂停按钮
        this.runPauseButton = new RunPauseButton(parent, module, RUN_BUTTON_WIDTH, this.height - RUN_BUTTON_PADDING * 2);

        ArrayList<Property<?>> properties = Myau.propertyManager.getPropertiesByModule(this.module);
        if (properties != null) {
            for (Property<?> prop : properties) {
                if (prop instanceof BooleanProperty) {
                    this.subComponents.add(new Checkbox((BooleanProperty) prop, parent, 0, 0, this.width, 16));
                } else if (prop instanceof ModeProperty) {
                    this.subComponents.add(new ModeSelector((ModeProperty) prop, parent, 0, 0, this.width, 16));
                } else if (prop instanceof IntProperty || prop instanceof FloatProperty || prop instanceof PercentProperty) {
                    // Corrected: No longer casting to Property<Number>
                    this.subComponents.add(new Slider(prop, parent, 0, 0, this.width, 16));
                } else if (prop instanceof ColorProperty) {
                    this.subComponents.add(new ColorPicker((ColorProperty) prop, parent, 0, 0, this.width, 16));
                }
            }
        }
        this.subComponents.add(new HideButton(module, parent, 0, 0, this.width, 16));
        this.subComponents.add(new BindButton(module, parent, 0, 0, this.width, 16));
    }

    @Override
    public void render(int mouseX, int mouseY) {
        if (!visible) return; // 如果不可见，则不渲染

        // 更新动画状态
        updateAnimation();
        
        // IntelliJ风格背景 - 检查鼠标悬停
        boolean isMouseOver = isMouseOver(mouseX, mouseY);
        int backgroundColor = isMouseOver ? HOVER_COLOR : BACKGROUND_COLOR;
        
        // 绘制IntelliJ风格背景
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor);

        // IntelliJ风格选中指示器 - 模块启用时显示选中状态
        if (this.module.isEnabled()) {
            Gui.drawRect(this.x, this.y, this.x + 3, this.y + this.height, SELECTED_COLOR);
        }

        // IntelliJ风格模块名称 - 使用变量名橙色
        RenderUtils.drawWrappedString(fr, this.module.getName(), this.x + 8, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 12 - RUN_BUTTON_WIDTH - RUN_BUTTON_PADDING, MODULE_NAME_COLOR);

        // 绘制运行/暂停按钮
        this.runPauseButton.x = this.x + this.width - RUN_BUTTON_WIDTH - RUN_BUTTON_PADDING; // Update button position
        this.runPauseButton.y = this.y + RUN_BUTTON_PADDING; // Update button position
        this.runPauseButton.render(mouseX, mouseY);
        
        // 绘制模块状态指示器（右侧小圆点, 现在在运行/暂停按钮的左边）
        int indicatorX = this.x + this.width - RUN_BUTTON_WIDTH - RUN_BUTTON_PADDING * 2 - 4;
        int indicatorY = this.y + (this.height - 4) / 2;
        int indicatorColor = this.module.isEnabled() ? 0xFF00FF00 : 0xFFFF0000;
        Gui.drawRect(indicatorX, indicatorY, indicatorX + 4, indicatorY + 4, indicatorColor);

        // 计算动画高度
        int animatedHeight = (int) (getSubComponentsHeight() * animationProgress);

        if (animatedHeight > 0 && !this.subComponents.isEmpty()) {
            int yOffset = this.y + this.height;
            int remainingHeight = animatedHeight;
            int screenHeight = new ScaledResolution(mc).getScaledHeight(); // 获取屏幕高度
            
            // 计算每个子组件应该渲染的高度
            int currentY = yOffset;
            for (Component comp : this.subComponents) {
                int compHeight = comp.getHeight();
                
                // 如果这个组件完全在动画高度内
                if (remainingHeight >= compHeight) {
                    comp.x = this.x + 2;
                    comp.y = currentY;
                    comp.width = this.width - 4;
                    comp.render(mouseX, mouseY);
                    
                    currentY += compHeight;
                    remainingHeight -= compHeight;
                } else if (remainingHeight > 0) {
                    // 部分可见的组件 - 使用简单的透明度控制
                    comp.x = this.x + 2;
                    comp.y = currentY;
                    comp.width = this.width - 4;
                    
                    // 保存原始颜色状态
                    net.minecraft.client.renderer.GlStateManager.pushMatrix();
                    
                    // 根据可见比例设置透明度
                    float alpha = remainingHeight / (float)compHeight;
                    net.minecraft.client.renderer.GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
                    
                    comp.render(mouseX, mouseY);
                    
                    // 恢复颜色状态
                    net.minecraft.client.renderer.GlStateManager.popMatrix();
                    
                    break; // 剩余组件不再渲染
                } else {
                    // 没有剩余空间，停止渲染
                    break;
                }
            }
        }
    }

    /**
     * 更新动画状态
     */
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f; // 转换为秒
        lastUpdateTime = currentTime;

        // 使用默认动画速度，因为GuiConfig已被移除
        float animationSpeed = DEFAULT_ANIMATION_SPEED;

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

    @Override
    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible) return null; // 如果不可见，则不响应点击

        // Check run/pause button click first
        Component clickedRunPauseButton = this.runPauseButton.mouseClicked(mouseX, mouseY, mouseButton);
        if (clickedRunPauseButton != null) {
            return clickedRunPauseButton;
        }

        // First, check if any sub-component was clicked if the module button is open.
        // 只有在完全打开时才能与子组件交互
        if (this.open && animationProgress >= 1.0f) {
            for (Component comp : this.subComponents) {
                // Only pass the click event to sub-components that are visible and within their bounds
                if (comp.isMouseOver(mouseX, mouseY)) {
                    Component clickedComponent = comp.mouseClicked(mouseX, mouseY, mouseButton);
                    if (clickedComponent != null) {
                        return clickedComponent; // A sub-component handled the click
                    }
                }
            }
        }

        // If no sub-component handled the click, or if the module button is closed, then check the module button itself.
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseButton == 0) {
                this.module.toggle();
            } else if (mouseButton == 1) {
                toggleOpenClose();
            }
        }
        return null; // No drag initiated by this component or its sub-components
    }

    /**
     * 切换打开/关闭状态以启动动画
     */
    private void toggleOpenClose() {
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

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        // ModuleButton itself doesn't have a dragging state, but its sub-components might.
        // However, ClickGuiScreen will call onMouseReleased directly on the draggingComponent,
        // so we don't need to propagate it here.
    }

    public boolean isOpen() {
        return open;
    }

    public int getSubComponentsHeight() {
        // 如果正在关闭或打开，仍然返回完整高度用于动画计算
        int height = 0;
        for (Component comp : this.subComponents) {
            height += comp.getHeight();
        }
        return height;
    }

    public Module getModule() {
        return module;
    }
    
    public ArrayList<Component> getSubComponents() {
        return subComponents;
    }

    /**
     * 获取此模块按钮所属的Frame
     * @return 所属的Frame对象
     */
    public Frame getParentFrame() {
        return this.parent;
    }

    /**
     * 检查模块按钮是否可见
     * @return true如果可见，否则false
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * 设置模块按钮的可见性
     * @param visible true为可见，false为隐藏
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
