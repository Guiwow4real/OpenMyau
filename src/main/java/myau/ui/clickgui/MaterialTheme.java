package myau.ui.clickgui;

import java.awt.Color;

/**
 * Material Design 3 主题配色配置
 * 模仿 Material Design 3 的深色主题风格
 */
public class MaterialTheme {

    // 主色调 - 根据 Material Design 3 深色主题指南
    public static final Color PRIMARY_COLOR = new Color(208, 188, 255); // Primary
    public static final Color ON_PRIMARY_COLOR = new Color(71, 39, 137); // On Primary
    public static final Color PRIMARY_CONTAINER_COLOR = new Color(94, 61, 160); // Primary Container
    public static final Color ON_PRIMARY_CONTAINER_COLOR = new Color(234, 221, 255); // On Primary Container

    public static final Color SECONDARY_COLOR = new Color(204, 194, 220); // Secondary
    public static final Color ON_SECONDARY_COLOR = new Color(55, 47, 72); // On Secondary
    public static final Color SECONDARY_CONTAINER_COLOR = new Color(78, 70, 95); // Secondary Container
    public static final Color ON_SECONDARY_CONTAINER_COLOR = new Color(232, 221, 248); // On Secondary Container

    public static final Color TERTIARY_COLOR = new Color(233, 183, 196); // Tertiary
    public static final Color ON_TERTIARY_COLOR = new Color(74, 37, 50); // On Tertiary
    public static final Color TERTIARY_CONTAINER_COLOR = new Color(97, 59, 72); // Tertiary Container
    public static final Color ON_TERTIARY_CONTAINER_COLOR = new Color(255, 216, 228); // On Tertiary Container

    public static final Color BACKGROUND_COLOR = new Color(28, 27, 31); // Background
    public static final Color ON_BACKGROUND_COLOR = new Color(230, 225, 229); // On Background
    public static final Color SURFACE_COLOR = new Color(28, 27, 31); // Surface
    public static final Color ON_SURFACE_COLOR = new Color(230, 225, 229); // On Surface
    public static final Color SURFACE_VARIANT_COLOR = new Color(73, 69, 78); // Surface Variant
    public static final Color ON_SURFACE_VARIANT_COLOR = new Color(204, 196, 206); // On Surface Variant

    public static final Color OUTLINE_COLOR = new Color(147, 140, 152); // Outline
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 100); // Shadow (半透明黑色)
    public static final Color INVERSE_SURFACE_COLOR = new Color(230, 225, 229); // Inverse Surface
    public static final Color INVERSE_ON_SURFACE_COLOR = new Color(49, 48, 52); // Inverse On Surface
    public static final Color INVERSE_PRIMARY_COLOR = new Color(94, 61, 160); // Inverse Primary

    public static final Color ERROR_COLOR = new Color(255, 180, 166); // Error
    public static final Color ON_ERROR_COLOR = new Color(105, 0, 5); // On Error
    public static final Color ERROR_CONTAINER_COLOR = new Color(147, 0, 10); // Error Container
    public static final Color ON_ERROR_CONTAINER_COLOR = new Color(255, 218, 214); // On Error Container

    // 额外常用颜色
    public static final Color TEXT_COLOR = ON_SURFACE_COLOR; // 主要文本颜色
    public static final Color TEXT_COLOR_SECONDARY = ON_SURFACE_VARIANT_COLOR; // 次要文本颜色
    public static final Color DISABLED_TEXT_COLOR = new Color(140, 140, 140); // 禁用文本颜色

    // 组件特定颜色
    public static final Color SURFACE_BRIGHT = new Color(54, 50, 56); // 较亮的表面色，用于卡片等
    public static final Color SURFACE_DIM = new Color(28, 27, 31); // 较暗的表面色
    public static final Color SURFACE_CONTAINER_LOW = new Color(40, 38, 43); // 低强调度的表面容器
    public static final Color SURFACE_CONTAINER_HIGH = new Color(54, 50, 56); // 高强调度的表面容器

    // 排版相关（虽然是图片渲染，但可以作为参考）
    public static final float CORNER_RADIUS_SMALL = 8.0f; // 小圆角
    public static final float CORNER_RADIUS_MEDIUM = 12.0f; // 中圆角
    public static final float CORNER_RADIUS_LARGE = 16.0f; // 大圆角
    public static final float CORNER_RADIUS_FULL = 24.0f; // 完全圆角（例如FAB）

    public static final float ELEVATION_0_ALPHA = 0.0f; // 0 dp 阴影透明度
    public static final float ELEVATION_1_ALPHA = 0.05f; // 1 dp 阴影透明度
    public static final float ELEVATION_2_ALPHA = 0.07f; // 2 dp 阴影透明度
    public static final float ELEVATION_3_ALPHA = 0.08f; // 3 dp 阴影透明度
    public static final float ELEVATION_4_ALPHA = 0.09f; // 4 dp 阴影透明度
    public static final float ELEVATION_5_ALPHA = 0.10f; // 5 dp 阴影透明度

    public static final float ANIMATION_DURATION_SHORT = 0.1f; // 短动画时长 (秒)
    public static final float ANIMATION_DURATION_MEDIUM = 0.2f; // 中动画时长 (秒)
    public static final float ANIMATION_DURATION_LONG = 0.3f; // 长动画时长 (秒)

    /**
     * 获取 RGB 颜色值
     */
    public static int getRGB(Color color) {
        return color.getRGB();
    }

    /**
     * 获取带透明度的 RGB 颜色值
     */
    public static int getRGBWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }
}
