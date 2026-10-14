package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.*;

/** 管理添加只针对当前已选角色；背包查询与调试写入共用有界响应。 */
public final class SearchPackets {
    public static final int OPCODE = 0x7B;
    public static final int INVENTORY = 0, ADD_ITEM = 1, ADD_POKEMON = 2, DESTROY_ITEM = 3, USE_ITEM = 4;
    public enum Status { SUCCESS, DISABLED, INVALID, PARTY_FULL, STACK_FULL, SYSTEM_ERROR }
    public record Request(int action, int id, int amount) { }
    public record Result(Status status, Map<Integer, Integer> inventory, List<PartyMember> party, Map<Integer,Integer> rewards) {
        public Result(Status status, Map<Integer,Integer> inventory, List<PartyMember> party) { this(status, inventory, party, Map.of()); }
    }
    private SearchPackets() { }
    public static byte[] request(int action, int id, int amount) {
        return new PacketData(10).putByte(OPCODE).putByte(action).putInt(id).putInt(amount).written();
    }
    public static Request decodeRequest(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Unexpected search request");
        var request = new Request(data.byteValue(), data.intValue(), data.intValue());
        data.end();
        return request;
    }
    public static byte[] response(Result result) {
        var data = new PacketData(8192 + result.inventory().size() * 8).putByte(OPCODE).putByte(result.status().ordinal());
        data.putShort(result.inventory().size());
        for (var entry : new TreeMap<>(result.inventory()).entrySet()) data.putInt(entry.getKey()).putInt(entry.getValue());
        PartyMember.write(data, result.party());
        data.putShort(result.rewards().size());
        for(var entry:new TreeMap<>(result.rewards()).entrySet())data.putInt(entry.getKey()).putInt(entry.getValue());
        return data.written();
    }
    public static Result decodeResponse(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Unexpected search response");
        int status = data.byteValue(), count = data.shortValue();
        if (status >= Status.values().length || count > ItemCatalog.items().size()) throw new IOException("Invalid search result");
        Map<Integer, Integer> inventory = new TreeMap<>();
        for (int i = 0; i < count; i++) {
            int id = data.intValue(), amount = data.intValue();
            var item = ItemCatalog.get(id);
            if (item == null || !item.addable() || amount < 1 || amount > item.maxStack() || inventory.put(id, amount) != null)
                throw new IOException("Invalid inventory entry");
        }
        var party = PartyMember.read(data);
        int rewardCount=data.shortValue();
        if(rewardCount>32)throw new IOException("Invalid reward count");
        Map<Integer,Integer> rewards=new TreeMap<>();
        for(int i=0;i<rewardCount;i++){
            int id=data.intValue(),amount=data.intValue();var item=ItemCatalog.get(id);
            if(item==null||amount<1||amount>item.maxStack()||rewards.put(id,amount)!=null)throw new IOException("Invalid reward");
        }
        if(status!=Status.SUCCESS.ordinal()&&!rewards.isEmpty())throw new IOException("Rewards on rejected operation");
        data.end();
        return new Result(Status.values()[status], Map.copyOf(inventory), party, Map.copyOf(rewards));
    }
}
