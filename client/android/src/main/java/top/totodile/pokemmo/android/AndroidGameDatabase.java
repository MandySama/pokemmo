package top.totodile.pokemmo.android;

import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.util.*;
import top.totodile.pokemmo.server.game.GameDatabase;

final class AndroidGameDatabase implements GameDatabase {
    private final SQLiteDatabase database;
    AndroidGameDatabase(File file) {
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) throw new IllegalStateException("Cannot create game directory");
        database = SQLiteDatabase.openOrCreateDatabase(file, null);
    }
    @Override public void execute(String sql, Object... values) { database.execSQL(sql, values); }
    @Override public List<String[]> query(String sql, String... values) {
        try (var rows = database.rawQuery(sql, values)) {
            List<String[]> result = new ArrayList<>();
            while (rows.moveToNext()) {
                String[] row = new String[rows.getColumnCount()];
                for (int i = 0; i < row.length; i++) row[i] = rows.getString(i);
                result.add(row);
            }
            return result;
        }
    }
    @Override public void transaction(Runnable operation) {
        database.beginTransaction();
        try { operation.run(); database.setTransactionSuccessful(); }
        finally { database.endTransaction(); }
    }
    @Override public void close() { database.close(); }
}
