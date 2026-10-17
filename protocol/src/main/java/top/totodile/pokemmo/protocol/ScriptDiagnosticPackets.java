package top.totodile.pokemmo.protocol;

import java.io.*;
import java.util.*;

/** 只查询当前认证角色；记录仅驻留内存，不进入剧情存档。 */
public final class ScriptDiagnosticPackets {
    public static final int OPCODE = 0x8C;
    public record Row(int label, String value) { }
    public record Snapshot(List<Row> rows) {
        public Snapshot { rows = List.copyOf(rows); }
    }
    private ScriptDiagnosticPackets() { }
    public static byte[] request() { return new byte[]{(byte) OPCODE}; }
    public static void readRequest(byte[] bytes) throws IOException {
        if (!Arrays.equals(bytes, request())) throw new IOException("Invalid diagnostic request");
    }
    public static byte[] response(Snapshot snapshot) throws IOException {
        if (snapshot.rows().size() > 1024) throw new IOException("Diagnostic row limit");
        var bytes = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(bytes)) {
            out.writeByte(OPCODE); out.writeShort(snapshot.rows().size());
            for (var row : snapshot.rows()) {
                out.writeInt(row.label()); out.writeUTF(row.value());
            }
        }
        if (bytes.size() > 60000) throw new IOException("Diagnostic packet limit");
        return bytes.toByteArray();
    }
    public static Snapshot decode(byte[] bytes) throws IOException {
        if (bytes.length > 60000) throw new IOException("Diagnostic packet limit");
        try (var in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (in.readUnsignedByte() != OPCODE) throw new IOException("Invalid diagnostic opcode");
            int count = in.readUnsignedShort();
            if (count > 1024) throw new IOException("Diagnostic row limit");
            var rows = new ArrayList<Row>();
            for (int i = 0; i < count; i++) {
                int label = in.readInt();
                if (label < 252040 || label > 252067) throw new IOException("Invalid diagnostic label");
                rows.add(new Row(label, in.readUTF()));
            }
            if (in.available() != 0) throw new IOException("Trailing diagnostic bytes");
            return new Snapshot(rows);
        }
    }
}
