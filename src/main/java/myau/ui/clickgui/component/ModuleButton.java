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
import net.minecraft.client.gui.Gui;

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
    
    // 默认颜色值
    private static final int SECONDARY_COLOR = new Color(30, 30, 30, 180).getRGB();
    private static final int PRIMARY_COLOR = new Color(30, 150, 250).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();
    private static final float DEFAULT_ANIMATION_SPEED = 3.0f; // 默认动画速度

    public ModuleButton(Module module, Frame parent, int yOffset) {
        super(parent, parent.getX(), parent.getY() + yOffset, parent.getWidth(), 16);
        this.module = module;
        this.subComponents = new ArrayList<>();
        this.open = false;
        this.opening = false;
        this.closing = false;
        this.animationProgress = 0.0f; // 默认完全关闭
        this.lastUpdateTime = System.currentTimeMillis();

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
        // 更新动画状态
        updateAnimation();
        
        // Use background color from GuiConfig
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, SECONDARY_COLOR);

        if (this.module.isEnabled()) {
            // Use primary color from GuiConfig for enabled indicator
            Gui.drawRect(this.x, this.y, this.x + 2, this.y + this.height, PRIMARY_COLOR);
        }

        // Use text color from GuiConfig
        RenderUtils.drawWrappedString(fr, this.module.getName(), this.x + 4, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 4, TEXT_COLOR);

        // 计算动画高度
        int animatedHeight = (int) (getSubComponentsHeight() * animationProgress);

        if (animatedHeight > 0 && !this.subComponents.isEmpty()) {
            int yOffset = this.y + this.height;
            int remainingHeight = animatedHeight;
            int screenHeight = mc.displayHeight; // 获取屏幕高度
            
            for (Component comp : this.subComponents) {
                // 检查是否有足够的空间渲染这个组件，并且不会超出屏幕高度
                if (remainingHeight >= comp.getHeight() && yOffset + comp.getHeight() <= screenHeight) {
                    comp.x = this.x + 2;
                    comp.y = yOffset;
                    comp.width = this.width - 4;
                    comp.render(mouseX, mouseY);
                    
                    int compHeight = comp.getHeight();
                    yOffset += compHeight;
                    remainingHeight -= compHeight;
                } else {
                    // 空间不足或超出屏幕高度，不再渲染更多组件
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
}
