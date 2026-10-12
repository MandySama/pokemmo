package top.totodile.pokemmo.persistence;

/** 通过控制台或环境变量创建账号，不将密码放入命令行参数。 */
public final class AccountAdmin {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: AccountAdmin <username>");
        String password = System.getenv("POKEMMO_ACCOUNT_PASSWORD");
        if (password == null) {
            if (System.console() == null) throw new IllegalStateException("Set POKEMMO_ACCOUNT_PASSWORD or use an interactive console");
            char[] input = System.console().readPassword("Password: ");
            if (input == null) return;
            password = new String(input);
            java.util.Arrays.fill(input, '\0');
        }
        Database database = new Database();
        database.initialize();
        System.out.println(new AccountStore(database).create(args[0], password) ? "Account created" : "Account already exists; unchanged");
    }
}
