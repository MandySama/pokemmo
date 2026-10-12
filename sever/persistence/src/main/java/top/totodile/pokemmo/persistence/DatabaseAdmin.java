package top.totodile.pokemmo.persistence;

/** 显式新建独立数据库；同名库存在时拒绝复用，避免覆盖其他项目。 */
public final class DatabaseAdmin {
    private DatabaseAdmin() { }
    public static void main(String[] args) throws Exception {
        if (args.length != 0) throw new IllegalArgumentException("DatabaseAdmin reads the local database configuration");
        new Database().createNew();
        System.out.println("Created project database and schema");
    }
}
