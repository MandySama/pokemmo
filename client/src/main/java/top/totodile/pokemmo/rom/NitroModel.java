package top.totodile.pokemmo.rom;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static top.totodile.pokemmo.rom.NdsArchive.*;
import static top.totodile.pokemmo.rom.NitroData.*;

/** NSBMD 模型、SBC 骨骼矩阵栈及 GX 多边形命令。 */
final class NitroModel implements AutoCloseable {
    private final Node[] nodes;
    private final Material[] materials;
    private final List<Primitive>[] shapes;
    private final Matrix4[] inverse;
    private final Matrix4[] stack = new Matrix4[32];
    private final Matrix4 current = new Matrix4();
    private final Matrix4 weighted = new Matrix4();
    private final ByteBuffer commands;
    private final NitroTextures textures;
    private final float positionScale;
    private final float inverseScale;
    private final NitroAnimation animation;
    private final TextureAnimation textureAnimation;

    @SuppressWarnings("unchecked")
    NitroModel(ByteBuffer data, ByteBuffer joint, ByteBuffer pattern) {
        this(data, joint, pattern, null);
        textures.setAllowMissing(false);
    }

    @SuppressWarnings("unchecked")
    NitroModel(ByteBuffer data, ByteBuffer joint, ByteBuffer pattern, ByteBuffer externalTextures) {
        if (!text(data, 0, 4).equals("BMD0")) throw new IllegalArgumentException("Invalid NSBMD");
        int modelSection = data.getInt(16);
        int textureSection = ushort(data, 14) > 1 ? data.getInt(20) : -1;
        int base = modelSection + data.getInt(dictionary(data, modelSection + 8, 4).getFirst().offset());
        int size = data.getInt(base);
        int sbc = base + data.getInt(base + 4);
        int materialOffset = base + data.getInt(base + 8);
        int shapeOffset = base + data.getInt(base + 12);
        int envelopeOffset = base + data.getInt(base + 16);
        positionScale = data.getInt(base + 28) / 4096f;
        inverseScale = data.getInt(base + 32) / 4096f;
        commands = data.slice(sbc, materialOffset - sbc).order(ByteOrder.LITTLE_ENDIAN);
        List<Entry> nodeEntries = dictionary(data, base + 64, 4);
        nodes = new Node[nodeEntries.size()];
        for (int i = 0; i < nodes.length; i++) {
            data.position(base + 64 + data.getInt(nodeEntries.get(i).offset()));
            nodes[i] = new Node(i, data);
        }
        List<Entry> materialEntries = dictionary(data, materialOffset + 4, 4);
        materials = new Material[materialEntries.size()];
        for (int i = 0; i < materials.length; i++) {
            Entry entry = materialEntries.get(i);
            data.position(materialOffset + data.getInt(entry.offset()));
            materials[i] = new Material(entry.name(), data);
        }
        assignNames(data, materialOffset, materialOffset + ushort(data, materialOffset), true);
        assignNames(data, materialOffset, materialOffset + ushort(data, materialOffset + 2), false);
        List<Entry> shapeEntries = dictionary(data, shapeOffset, 4);
        shapes = new List[shapeEntries.size()];
        for (int i = 0; i < shapes.length; i++) {
            int offset = shapeOffset + data.getInt(shapeEntries.get(i).offset());
            shapes[i] = geometry(data.slice(offset + data.getInt(offset + 8), data.getInt(offset + 12))
                    .order(ByteOrder.LITTLE_ENDIAN));
        }
        inverse = new Matrix4[nodes.length];
        for (int i = 0; i < inverse.length; i++) {
            inverse[i] = new Matrix4();
            if (envelopeOffset < base + size) {
                for (int column = 0; column < 4; column++) {
                    for (int row = 0; row < 3; row++) {
                        inverse[i].val[column * 4 + row] = data.getInt(envelopeOffset + i * 84 + (column * 3 + row) * 4) / 4096f;
                    }
                }
            }
        }
        for (int i = 0; i < stack.length; i++) stack[i] = new Matrix4();
        textures = externalTextures == null ? new NitroTextures(data, textureSection)
                : new NitroTextures(externalTextures, externalTextures.getInt(16));
        textures.setAllowMissing(true);
        if (externalTextures != null && ushort(data, 14) > 1 && textureSection >= 0
                && textureSection < data.limit() - 8 && text(data, textureSection, 4).equals("TEX0")) {
            textures.setFallback(new NitroTextures(data, textureSection));
        }
        animation = joint == null ? null : new NitroAnimation(joint);
        textureAnimation = pattern == null ? null : new TextureAnimation(pattern);
    }

    private void assignNames(ByteBuffer data, int base, int dictionary, boolean texture) {
        for (Entry entry : dictionary(data, dictionary, 4)) {
            int offset = base + ushort(data, entry.offset());
            int count = data.get(entry.offset() + 2) & 255;
            for (int i = 0; i < count; i++) {
                Material material = materials[data.get(offset + i) & 255];
                if (texture) material.texture = entry.name();
                else material.palette = entry.name();
            }
        }
    }

    void draw(float frame, boolean translucent) {
        for (Node node : nodes) node.update(animation, frame);
        current.idt();
        for (Matrix4 matrix : stack) matrix.idt();
        commands.position(0);
        int material = 0;
        float[] view = null;
        while (commands.hasRemaining()) {
            int command = commands.get() & 255;
            switch (command & 31) {
                case 0 -> {
                }
                case 1 -> {
                    return;
                }
                case 2, 12, 13 -> commands.position(commands.position() + 2);
                case 3 -> current.set(stack[commands.get() & 31]);
                case 4 -> material = commands.get() & 255;
                case 5 -> drawShape(commands.get() & 255, materials[material], frame, translucent);
                case 6 -> {
                    int index = commands.get() & 255;
                    commands.get();
                    commands.get();
                    int save = (command & 32) != 0 ? commands.get() & 31 : -1;
                    int restore = (command & 64) != 0 ? commands.get() & 31 : -1;
                    if (restore >= 0) current.set(stack[restore]);
                    current.mul(nodes[index].matrix);
                    if (save >= 0) stack[save].set(current);
                }
                case 7, 8 -> {
                    commands.get();
                    int save = (command & 32) != 0 ? commands.get() & 31 : -1;
                    int restore = (command & 64) != 0 ? commands.get() & 31 : -1;
                    if (restore >= 0) current.set(stack[restore]);
                    if (view == null) { view = new float[16]; glGetFloatv(GL_MODELVIEW_MATRIX, view); }
                    billboard(view, (command & 31) == 8);
                    if (save >= 0) stack[save].set(current);
                }
                case 9 -> {
                    int destination = commands.get() & 31;
                    int count = commands.get() & 255;
                    float[] combined = new float[16];
                    for (int i = 0; i < count; i++) {
                        int source = commands.get() & 31;
                        int index = commands.get() & 255;
                        float weight = (commands.get() & 255) / 256f;
                        weighted.set(stack[source]).mul(inverse[index]);
                        for (int j = 0; j < 16; j++) combined[j] += weighted.val[j] * weight;
                    }
                    current.set(combined);
                    stack[destination].set(current);
                }
                case 11 -> {
                    float scale = (command & 32) == 0 ? positionScale : inverseScale;
                    current.scale(scale, scale, scale);
                }
                default -> throw new IllegalArgumentException("Unsupported SBC command: " + command);
            }
        }
    }

    /** 用镜头的逆旋转替换面板旋转，保留节点位置及各轴缩放。 */
    private void billboard(float[] view, boolean vertical) {
        float[] matrix = current.val;
        float sx = (float) Math.sqrt(matrix[0] * matrix[0] + matrix[1] * matrix[1] + matrix[2] * matrix[2]);
        float sy = (float) Math.sqrt(matrix[4] * matrix[4] + matrix[5] * matrix[5] + matrix[6] * matrix[6]);
        float sz = (float) Math.sqrt(matrix[8] * matrix[8] + matrix[9] * matrix[9] + matrix[10] * matrix[10]);
        if (vertical) {
            float length = (float) Math.sqrt(view[2] * view[2] + view[10] * view[10]);
            float x = length < 0.00001f ? 0 : view[2] / length;
            float z = length < 0.00001f ? 1 : view[10] / length;
            matrix[0] = z * sx; matrix[1] = 0; matrix[2] = -x * sx;
            matrix[4] = 0; matrix[5] = sy; matrix[6] = 0;
            matrix[8] = x * sz; matrix[9] = 0; matrix[10] = z * sz;
        } else {
            matrix[0] = view[0] * sx; matrix[1] = view[4] * sx; matrix[2] = view[8] * sx;
            matrix[4] = view[1] * sy; matrix[5] = view[5] * sy; matrix[6] = view[9] * sy;
            matrix[8] = view[2] * sz; matrix[9] = view[6] * sz; matrix[10] = view[10] * sz;
        }
    }

    private void drawShape(int index, Material material, float frame, boolean translucent) {
        boolean transparent = material.alpha < 1 || textures.transparent(material.texture);
        if (transparent != translucent) return;
        String texture = material.texture;
        String palette = material.palette;
        if (textureAnimation != null) {
            String[] names = textureAnimation.names(material.name, frame);
            if (names != null) {
                texture = names[0];
                palette = names[1];
            }
        }
        int id = textures.texture(texture, palette);
        if (id == 0) glDisable(GL_TEXTURE_2D);
        else {
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, id);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, material.wrapS);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, material.wrapT);
        }
        if (material.cull == 3) glDisable(GL_CULL_FACE);
        else {
            glEnable(GL_CULL_FACE);
            glCullFace(material.cull == 1 ? GL_FRONT : material.cull == 2 ? GL_BACK : GL_FRONT_AND_BACK);
        }
        glDepthMask(!transparent);
        for (Primitive primitive : shapes[index]) {
            glBegin(primitive.type);
            for (Vertex vertex : primitive.vertices) {
                float[] matrix = (vertex.slot < 0 ? current : stack[vertex.slot]).val;
                glColor4f(vertex.r, vertex.g, vertex.b, material.alpha);
                glTexCoord2f(vertex.u * material.scaleU / material.width, vertex.v * material.scaleV / material.height);
                glVertex3f((matrix[0] * vertex.x + matrix[4] * vertex.y + matrix[8] * vertex.z + matrix[12]) / 64f,
                        (matrix[1] * vertex.x + matrix[5] * vertex.y + matrix[9] * vertex.z + matrix[13]) / 64f,
                        (matrix[2] * vertex.x + matrix[6] * vertex.y + matrix[10] * vertex.z + matrix[14]) / 64f);
            }
            glEnd();
        }
    }

    private static List<Primitive> geometry(ByteBuffer data) {
        List<Primitive> result = new ArrayList<>();
        List<Vertex> vertices = null;
        int slot = -1;
        Matrix4 local = new Matrix4();
        float x = 0, y = 0, z = 0, u = 0, v = 0, r = 1, g = 1, b = 1;
        while (data.hasRemaining()) {
            int packed = data.getInt();
            for (int i = 0; i < 4; i++) {
                int command = packed >>> (i * 8) & 255;
                boolean emit = false;
                switch (command) {
                    case 0, 17 -> {
                    }
                    case 20 -> {
                        slot = data.getInt() & 31;
                        local.idt();
                    }
                    case 21 -> local.idt();
                    case 22, 23, 24, 25, 26 -> {
                        Matrix4 value = new Matrix4();
                        int columns = command == 26 ? 3 : 4;
                        int rows = command == 22 || command == 24 ? 4 : 3;
                        for (int column = 0; column < columns; column++) {
                            for (int row = 0; row < rows; row++) value.val[column * 4 + row] = fixed(data);
                        }
                        if (command == 22 || command == 23) local.set(value);
                        else local.mul(value);
                    }
                    case 27 -> local.scale(fixed(data), fixed(data), fixed(data));
                    case 28 -> local.translate(fixed(data), fixed(data), fixed(data));
                    case 32 -> {
                        int color = data.getInt();
                        r = (color & 31) / 31f;
                        g = (color >> 5 & 31) / 31f;
                        b = (color >> 10 & 31) / 31f;
                    }
                    case 34 -> {
                        u = data.getShort() / 16f;
                        v = data.getShort() / 16f;
                    }
                    case 35 -> {
                        x = data.getShort() / 4096f;
                        y = data.getShort() / 4096f;
                        z = data.getShort() / 4096f;
                        data.getShort();
                        emit = true;
                    }
                    case 36 -> {
                        int value = data.getInt();
                        x = signed10(value) / 64f;
                        y = signed10(value >> 10) / 64f;
                        z = signed10(value >> 20) / 64f;
                        emit = true;
                    }
                    case 37 -> {
                        x = data.getShort() / 4096f;
                        y = data.getShort() / 4096f;
                        emit = true;
                    }
                    case 38 -> {
                        x = data.getShort() / 4096f;
                        z = data.getShort() / 4096f;
                        emit = true;
                    }
                    case 39 -> {
                        y = data.getShort() / 4096f;
                        z = data.getShort() / 4096f;
                        emit = true;
                    }
                    case 40 -> {
                        int value = data.getInt();
                        x += signed10(value) / 4096f;
                        y += signed10(value >> 10) / 4096f;
                        z += signed10(value >> 20) / 4096f;
                        emit = true;
                    }
                    case 64 -> {
                        int type = data.getInt();
                        vertices = new ArrayList<>();
                        result.add(new Primitive(switch (type) {
                            case 0 -> GL_TRIANGLES;
                            case 1 -> GL_QUADS;
                            case 2 -> GL_TRIANGLE_STRIP;
                            case 3 -> GL_QUAD_STRIP;
                            default -> throw new IllegalArgumentException("Invalid GX primitive");
                        }, vertices));
                    }
                    case 65 -> vertices = null;
                    case 16, 18, 19, 33, 41, 42, 43, 48, 49, 50, 51, 80, 96, 114 -> data.getInt();
                    default -> throw new IllegalArgumentException("Unsupported GX command: " + command);
                }
                if (emit && vertices != null) {
                    float[] matrix = local.val;
                    vertices.add(new Vertex(slot,
                            matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12],
                            matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13],
                            matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14], u, v, r, g, b));
                }
            }
        }
        return result;
    }

    private static int signed10(int value) {
        return value << 22 >> 22;
    }

    private record Vertex(int slot, float x, float y, float z, float u, float v, float r, float g, float b) {
    }

    private record Primitive(int type, List<Vertex> vertices) {
    }

    private static final class Node {
        final int index;
        final Vector3 translation = new Vector3();
        final Quaternion rotation = new Quaternion();
        final Vector3 scale = new Vector3(1, 1, 1);
        final Matrix4 matrix = new Matrix4();
        final Vector3 animatedTranslation = new Vector3();
        final Quaternion animatedRotation = new Quaternion();
        final Vector3 animatedScale = new Vector3();

        Node(int index, ByteBuffer data) {
            this.index = index;
            int flags = data.getShort() & 65535;
            float first = data.getShort() / 4096f;
            if ((flags & 1) == 0) translation.set(fixed(data), fixed(data), fixed(data));
            if ((flags & 2) == 0) {
                Matrix4 value = new Matrix4();
                if ((flags & 8) != 0) {
                    value = pivot(flags >> 4 & 15, flags >> 8, data.getShort() / 4096f, data.getShort() / 4096f);
                } else {
                    value.val[0] = first;
                    for (int i = 1; i < 9; i++) value.val[i / 3 * 4 + i % 3] = data.getShort() / 4096f;
                }
                rotation.setFromMatrix(true, value).nor();
            }
            if ((flags & 4) == 0) scale.set(fixed(data), fixed(data), fixed(data));
        }

        void update(NitroAnimation animation, float frame) {
            animatedTranslation.set(translation);
            animatedRotation.set(rotation);
            animatedScale.set(scale);
            if (animation != null) animation.apply(index, frame, animatedTranslation, animatedRotation, animatedScale);
            matrix.set(animatedTranslation, animatedRotation, animatedScale);
        }
    }

    private static final class Material {
        final String name;
        final float alpha;
        final int cull;
        final int width;
        final int height;
        final int wrapS;
        final int wrapT;
        float scaleU = 1;
        float scaleV = 1;
        String texture;
        String palette;

        Material(String name, ByteBuffer data) {
            this.name = name;
            int base = data.position();
            int polygon = data.getInt(base + 12);
            int texture = data.getInt(base + 20);
            int flags = ushort(data, base + 30);
            alpha = (polygon >> 16 & 31) / 31f;
            cull = polygon >> 6 & 3;
            width = ushort(data, base + 32) * ((texture >> 18 & 1) + 1);
            height = ushort(data, base + 34) * ((texture >> 19 & 1) + 1);
            wrapS = (texture & 65536) == 0 ? GL_CLAMP_TO_EDGE_VALUE
                    : (texture & 262144) != 0 ? GL_MIRRORED_REPEAT_VALUE : GL_REPEAT;
            wrapT = (texture & 131072) == 0 ? GL_CLAMP_TO_EDGE_VALUE
                    : (texture & 524288) != 0 ? GL_MIRRORED_REPEAT_VALUE : GL_REPEAT;
            if ((flags & 2) == 0) {
                scaleU = data.getInt(base + 44) / 4096f;
                scaleV = data.getInt(base + 48) / 4096f;
            }
        }

        private static final int GL_CLAMP_TO_EDGE_VALUE = 0x812F;
        private static final int GL_MIRRORED_REPEAT_VALUE = 0x8370;
    }

    @Override
    public void close() {
        textures.close();
    }

    /** 纹理图案动画用于眼睛等材质的逐帧切换。 */
    private static final class TextureAnimation {
        private final ByteBuffer data;
        private final int base;
        private final int frames;
        private final List<Entry> tracks;
        private final String[] textures;
        private final String[] palettes;

        TextureAnimation(ByteBuffer data) {
            this.data = data;
            int section = data.getInt(16);
            base = section + data.getInt(dictionary(data, section + 8, 4).getFirst().offset());
            frames = ushort(data, base + 4);
            textures = names(data.get(base + 6) & 255, base + ushort(data, base + 8));
            palettes = names(data.get(base + 7) & 255, base + ushort(data, base + 10));
            tracks = dictionary(data, base + 12, 8);
        }

        private String[] names(int count, int offset) {
            String[] result = new String[count];
            for (int i = 0; i < count; i++) result[i] = text(data, offset + i * 16, 16);
            return result;
        }

        String[] names(String material, float frame) {
            for (Entry entry : tracks) {
                if (!entry.name().equals(material)) continue;
                int count = ushort(data, entry.offset());
                int offset = base + ushort(data, entry.offset() + 6);
                int time = Math.min((int) frame, frames - 1);
                int selected = 0;
                for (int i = 0; i < count; i++) {
                    if (ushort(data, offset + i * 4) > time) break;
                    selected = i;
                }
                return new String[]{textures[data.get(offset + selected * 4 + 2) & 255],
                        palettes[data.get(offset + selected * 4 + 3) & 255]};
            }
            return null;
        }
    }
}
