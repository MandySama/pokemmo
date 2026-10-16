package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 配置请求按稳定个体 ID 定位；只发送操作，不接收客户端构造的个体资料。 */
public final class PokemonConfigPackets {
    public static final int OPCODE = 0x85;
    public enum Action { SHINY, ALPHA, GENDER, ABILITY, NATURE, IVS, EVS, EV, FRIENDSHIP, BALL, FORM }
    public enum Status { SUCCESS, DISABLED, INVALID, SYSTEM_ERROR }
    public record Request(long individual, Action action, int index, int value) { }
    public record Result(Status status, PartyMember updated, List<PartyMember> party) {
        public Result { party = List.copyOf(party); }
    }
    private PokemonConfigPackets() { }

    public static byte[] request(Request request) {
        return new PacketData(18).putByte(OPCODE).putLong(request.individual()).putByte(request.action().ordinal())
                .putInt(request.index()).putInt(request.value()).written();
    }

    public static Request decodeRequest(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Unexpected Pokemon configuration request");
        long id = data.longValue(); int action = data.byteValue();
        if (id <= 0 || action >= Action.values().length) throw new IOException("Invalid Pokemon configuration request");
        var result = new Request(id, Action.values()[action], data.intValue(), data.intValue());
        data.end();
        return result;
    }

    public static byte[] response(Result result) {
        var data = new PacketData(16384).putByte(OPCODE).putByte(result.status().ordinal());
        PartyMember.write(data, result.updated() == null ? List.of() : List.of(result.updated()));
        PartyMember.write(data, result.party());
        return data.written();
    }

    public static Result decodeResponse(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Unexpected Pokemon configuration response");
        int status = data.byteValue();
        if (status >= Status.values().length) throw new IOException("Invalid Pokemon configuration status");
        var updated = PartyMember.read(data); var party = PartyMember.read(data); data.end();
        if (updated.size() > 1 || (status == Status.SUCCESS.ordinal()) != (updated.size() == 1))
            throw new IOException("Invalid Pokemon configuration result");
        return new Result(Status.values()[status], updated.isEmpty() ? null : updated.getFirst(), party);
    }

    /** 所有数值在服务端应用时校验，重算 HP 上限并保留已损失的 HP。 */
    public static PartyMember apply(PartyMember member, Request request) {
        var d = member.individual();
        if (d == null || d.id() != request.individual()) throw new IllegalArgumentException("Individual mismatch");
        int value = request.value(), index = request.index();
        if (request.action() != Action.EV && index != 0) throw new IllegalArgumentException("Unexpected stat index");
        boolean shiny = member.shiny(), alpha = member.alpha();
        int form = d.form();
        int nature = d.nature(), ability = d.ability(), gender = d.gender(), friendship = d.friendship(), ball = d.ball();
        var ivs = new ArrayList<>(d.ivs()); var evs = new ArrayList<>(d.evs());
        var species = PokemonCatalog.get(member.species());
        switch (request.action()) {
            case SHINY, ALPHA -> {
                require(value == 0 || value == 1);
                if (request.action() == Action.SHINY) shiny = value == 1; else alpha = value == 1;
            }
            case FORM -> { require(PokemonFormsCatalog.supports(member.species(), value)); form = value; }
            case GENDER -> {
                require(value >= 0 && value <= 2);
                require(species.gender() == 255 ? value == 2 : species.gender() == 254 ? value == 1
                        : species.gender() == 0 ? value == 0 : value != 2);
                gender = value;
            }
            case ABILITY -> {
                require(value >= 0 && value <= 2);
                ability = value == 0 ? species.ability1() : value == 1 ? species.ability2() : species.hiddenAbility();
                require(ability > 0);
            }
            case NATURE -> { require(value >= 0 && value < 25); nature = value; }
            case IVS -> {
                require(value == -1 || value == 0 || value == 15 || value == 31);
                for (int i = 0; i < 6; i++) ivs.set(i, value < 0 ? ThreadLocalRandom.current().nextInt(32) : value);
            }
            case EVS -> { require(value == 0 || value == 42 || value == 85); evs.replaceAll(old -> value); }
            case EV -> {
                require(index >= 0 && index < 6 && (value == 0 || value == 126 || value == 252));
                evs.set(index, value); require(evs.stream().mapToInt(Integer::intValue).sum() <= 510);
            }
            case FRIENDSHIP -> { require(value == 0 || value == 127 || value == 255); friendship = value; }
            case BALL -> { require(value >= 1 && value <= 25); ball = value; }
        }
        var next = new PokemonIndividual(d.id(), d.nickname(), d.originalTrainerId(), d.originalTrainer(), nature, ability,
                gender, friendship, d.experience(), d.heldItem(), ball, d.metAt(), d.metRegion(), d.metGroup(), d.metMap(),
                d.metLevel(), ivs, evs, d.moves(), d.marks(), d.ribbons(), d.contest(), d.particles(), form, d.status());
        int maxHp = next.stat(member.species(), member.level(), 0);
        int hp = member.hp() == 0 ? 0 : Math.max(1, maxHp - (member.maxHp() - member.hp()));
        return new PartyMember(member.species(), shiny, alpha, Math.min(hp, maxHp), maxHp, member.level(), next);
    }

    private static void require(boolean valid) {
        if (!valid) throw new IllegalArgumentException("Invalid Pokemon configuration value");
    }
}
