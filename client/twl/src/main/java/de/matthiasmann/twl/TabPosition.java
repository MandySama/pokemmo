package de.matthiasmann.twl;

/** 参考主题 tabposition 的完整取值；ABSOLUTE 用于绝对位置标签。 */
public enum TabPosition {
    TOP(true), LEFT(false), RIGHT(false), BOTTOM(true), ABSOLUTE(false);
    public final boolean horizontal;
    TabPosition(boolean horizontal) { this.horizontal=horizontal; }
}
