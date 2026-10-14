package top.totodile.pokemmo.protocol;

import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import top.totodile.pokemmo.protocol.CharacterPackets.ReturnPoint;

/** 无图像的世界裁决接口，地区采用各自地图格式。 */
public interface WorldRules {
    FireRedWorldRules.Decision move(Position position, int direction, boolean noclip);

    Position at(int group, int map, int x, int y, int facing, ReturnPoint returnPoint);
}
