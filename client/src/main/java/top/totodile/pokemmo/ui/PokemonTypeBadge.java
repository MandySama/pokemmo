package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Label;
import top.totodile.pokemmo.ClientStrings;

/** ROM 类型标牌之外的妖精标签使用语言文字，避免误显示一般属性。 */
final class PokemonTypeBadge extends Label {
    PokemonTypeBadge() { setTheme("type-0"); }
    @Override public void setTheme(String theme) {
        super.setTheme(theme);
        setText(theme.endsWith("-17") ? ClientStrings.get(230009) : "");
    }
}
