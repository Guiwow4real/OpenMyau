package myau.ui.clickgui.component.impl;

import myau.property.properties.ColorProperty;
import myau.ui.clickgui.Frame;
import myau.ui.clickgui.component.Component;
import net.minecraft.client.gui.Gui;
import myau.util.RenderUtils;
import myau.Myau;
import myau.module.modules.HUD;

import java.awt.*;

public class ColorPicker extends Component {

    private final ColorProperty colorProperty;
    private boolean isCustomMode = false;
    private boolean isSyncMode = false;
    
    // 默认颜色值
    private static final int SECONDARY_COLOR = new Color(30, 30, 30, 180).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();

    public ColorPicker(ColorProperty colorProperty, Frame parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        this.colorProperty = colorProperty;
        
        // 检查是否是GUI颜色属性
        if (colorProperty.getName().contains("Color")) {
            isCustomMode = true;
        }
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, SECONDARY_COLOR);
        
        String displayText = this.colorProperty.getName() + ": ";
        
        // 如果是GUI颜色属性，添加Custom/Sync选项
        if (isCustomMode) {
            // 检查是否应该同步HUD颜色
            if (isSyncMode) {
                displayText += "Sync (HUD)";
                // 实时同步HUD颜色
                syncWithHUD();
            } else {
                displayText += "Custom";
            }
        } else {
            // 显示当前颜色值
            displayText += this.colorProperty.formatValue();
        }
        
        RenderUtils.drawWrappedString(fr, displayText, this.x + 2, this.y + this.height / 2 - fr.FONT_HEIGHT / 2, this.width - 4, TEXT_COLOR);
        
        // 绘制颜色预览框
        int previewSize = 8;
        int previewX = this.x + this.width - previewSize - 2;
        int previewY = this.y + this.height / 2 - previewSize / 2;
        Gui.drawRect(previewX, previewY, previewX + previewSize, previewY + previewSize, this.colorProperty.getValue());
        Gui.drawRect(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000); // 黑色边框
    }

    @Override
    public Component mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isMouseOver(mouseX, mouseY)) {
            if (isCustomMode) {
                // 对于GUI颜色属性，切换Custom/Sync模式
                if (mouseButton == 0) { // 左键切换模式
                    isSyncMode = !isSyncMode;
                    if (isSyncMode) {
                        // 同步HUD颜色
                        syncWithHUD();
                    }
                } else if (mouseButton == 1) { // 右键打开颜色选择器
                    // 这里可以打开更详细的颜色选择器界面
                    openColorPicker();
                }
            } else {
                // 对于普通颜色属性，打开颜色选择器
                if (mouseButton == 0 || mouseButton == 1) {
                    openColorPicker();
                }
            }
            return this;
        }
        return null;
    }

    private void syncWithHUD() {
        // 获取HUD模块实例
        HUD hud = (HUD) Myau.moduleManager.getModule("HUD");
        if (hud != null) {
            // 获取HUD的当前颜色（支持动态颜色）
            int hudColor = hud.getColor(System.currentTimeMillis()).getRGB();
            // 同步颜色值
            this.colorProperty.setValue(hudColor);
        }
    }

    private void openColorPicker() {
        // 这里可以实现更复杂的颜色选择器逻辑
        // 目前我们只是简单地循环几种预设颜色
        int currentColor = this.colorProperty.getValue();
        int[] presetColors = {
            0xFF0000, // 红色
            0x00FF00, // 绿色
            0x0000FF, // 蓝色
            0xFFFF00, // 黄色
            0xFF00FF, // 紫色
            0x00FFFF, // 青色
            0xFFFFFF  // 白色
        };
        
        // 找到当前颜色在预设中的位置
        int currentIndex = -1;
        for (int i = 0; i < presetColors.length; i++) {
            if (presetColors[i] == currentColor) {
                currentIndex = i;
                break;
            }
        }
        
        // 切换到下一个颜色
        int nextIndex = (currentIndex + 1) % presetColors.length;
        this.colorProperty.setValue(presetColors[nextIndex]);
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        // No dragging for this component
    }
}