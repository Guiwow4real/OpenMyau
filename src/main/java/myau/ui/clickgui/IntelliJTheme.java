package myau.ui.clickgui;

import java.awt.Color;

/**
 * IntelliJ IDEA主题配色配置
 * 模仿IntelliJ IDEA代码编辑器的深色主题风格
 */
public class IntelliJTheme {
    
    // 主色调 - 深灰背景
    public static final Color BACKGROUND_COLOR = new Color(43, 43, 43, 230); // 半透明深灰背景
    public static final Color SECONDARY_BACKGROUND = new Color(60, 63, 65); // 次级背景色
    public static final Color BORDER_COLOR = new Color(85, 85, 85); // 边框色
    
    // 文字颜色 - IntelliJ语法高亮风格
    public static final Color TYPE_VALUE_COLOR = new Color(204, 153, 204); // 紫色 - 类型/值
    public static final Color BOOLEAN_COLOR = new Color(86, 156, 214); // 蓝色 - 布尔值
    public static final Color TEXT_COLOR = new Color(187, 187, 187); // 浅灰色 - 普通文本
    public static final Color DISABLED_TEXT_COLOR = new Color(128, 128, 128); // 灰色 - 禁用文本
    public static final Color TEXT_COLOR_SECONDARY = new Color(150, 150, 150); // 次级文本颜色，用于提示等
    
    // 交互状态颜色
    public static final Color SELECTED_COLOR = new Color(62, 93, 126); // 选中高亮 - 浅蓝色
    public static final Color SELECTED_BG_COLOR = new Color(62, 93, 126); // 选中背景色 - 与SELECTED_COLOR相同
    public static final Color HOVER_COLOR = new Color(75, 110, 145); // 悬停颜色
    public static final Color ACTIVE_COLOR = new Color(64, 128, 214); // 激活状态颜色
    public static final Color ACTIVE_BORDER_COLOR = new Color(86, 156, 214); // 激活边框颜色
    
    // 组件颜色
    public static final Color CHECKBOX_COLOR = new Color(86, 156, 214); // 复选框颜色
    public static final Color SLIDER_COLOR = new Color(86, 156, 214); // 滑块颜色
    public static final Color BUTTON_COLOR = new Color(78, 148, 206); // 按钮颜色
    public static final Color TEXT_FIELD_BG = new Color(55, 57, 59); // 文本输入框背景
    
    // 工具提示颜色
    public static final Color TOOLTIP_BACKGROUND = new Color(70, 70, 70, 200); // 工具提示背景色
    public static final Color TOOLTIP_BORDER = new Color(100, 100, 100); // 工具提示边框色
    public static final Color TOOLTIP_TEXT_COLOR = new Color(200, 200, 200); // 工具提示文本颜色

    // 滚动条颜色
    public static final Color SCROLLBAR_COLOR = new Color(120, 120, 120, 150); // 滚动条颜色 (半透明)
    public static final Color SCROLLBAR_HOVER_COLOR = new Color(150, 150, 150, 200); // 滚动条悬停颜色 (半透明)
    
    // IDE代码编辑器风格颜色 - 更真实的IntelliJ配色
    public static final Color LINE_NUMBER_BG_COLOR = new Color(43, 43, 43); // 行号区域背景色
    public static final Color LINE_NUMBER_COLOR = new Color(98, 102, 108); // 行号颜色 - 更淡的灰色
    public static final Color CODE_BG_COLOR = new Color(49, 51, 53); // 代码区域背景色
    public static final Color KEYWORD_COLOR = new Color(204, 120, 50); // 关键字颜色 - 橙色 (public, final, new)
    public static final Color CLASS_NAME_COLOR = new Color(139, 191, 74); // 类名颜色 - 绿色 (BooleanProperty, IntProperty)
    public static final Color VARIABLE_NAME_COLOR = new Color(152, 118, 170); // 变量名颜色 - 紫色 (属性名)
    public static final Color STRING_COLOR = new Color(106, 153, 85); // 字符串颜色 - 绿色
    public static final Color NUMBER_COLOR = new Color(104, 151, 187); // 数字颜色 - 蓝色
    public static final Color COMMENT_COLOR = new Color(98, 102, 108); // 注释颜色 - 灰色
    public static final Color LITERAL_COLOR = new Color(104, 151, 187); // 字面量颜色 - 蓝色 (true, false)
    public static final Color ANNOTATION_COLOR = new Color(187, 181, 41); // 注解颜色 - 黄色
    public static final Color RUN_BUTTON_COLOR = new Color(85, 170, 85); // 运行按钮颜色 (绿色)
    public static final Color PAUSE_BUTTON_COLOR = new Color(170, 85, 85); // 暂停按钮颜色 (红色)

    // 圆角半径
    public static final int CORNER_RADIUS = 4; // 圆角半径
    
    // 动画速度
    public static final float ANIMATION_SPEED = 8.0f; // 动画速度
    
    /**
     * 根据属性类型获取对应的文字颜色
     */
    public static Color getTextColorByPropertyType(String propertyType) {
        switch (propertyType.toLowerCase()) {
            case "boolean":
                return BOOLEAN_COLOR;
            case "int":
            case "float":
            case "percent":
                return TYPE_VALUE_COLOR;
            case "mode":
            case "color":
                return TYPE_VALUE_COLOR;
            default:
                return VARIABLE_NAME_COLOR;
        }
    }
    
    /**
     * 获取RGB颜色值
     */
    public static int getRGB(Color color) {
        return color.getRGB();
    }
    
    /**
     * 获取带透明度的RGB颜色值
     */
    public static int getRGBWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }
}