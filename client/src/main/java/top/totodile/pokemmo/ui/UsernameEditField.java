package top.totodile.pokemmo.ui;

/** 登录表单的用户名过滤器：键入和粘贴均排除空白字符。 */
final class UsernameEditField extends LoginEditField {
    @Override
    protected void insertChar(char character) {
        if (!Character.isWhitespace(character)) {
            super.insertChar(character);
        }
    }

    @Override
    public void insertText(String text) {
        var filtered = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (!Character.isWhitespace(character)) {
                filtered.append(character);
            }
        }
        // 过滤后为空时保留现有选区，不删除已选文字。
        if (!filtered.isEmpty()) {
            super.insertText(filtered.toString());
        }
    }
}
