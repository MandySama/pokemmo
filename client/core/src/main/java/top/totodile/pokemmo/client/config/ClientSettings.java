package top.totodile.pokemmo.client.config;

import com.badlogic.gdx.files.FileHandle;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** 客户端配置文件；提交时合并未由设置页管理的键，成功替换文件后才更新内存。 */
public final class ClientSettings {
    private final FileHandle file;
    private final Properties values = new Properties();
    public ClientSettings(FileHandle directory) {
        file = directory.child("main.properties");
        read(values);
    }
    private void read(Properties target) {
        if (!file.exists()) return;
        try (var input = file.read()) { target.load(input); }
        catch (IOException error) { throw new IllegalStateException("Cannot read client settings",error); }
    }
    public synchronized String get(String key,String fallback) { return values.getProperty(key,fallback); }
    public synchronized Properties snapshot() { var result = new Properties(); result.putAll(values); return result; }
    public synchronized void save(Map<String,String> changes) {
        Properties next = new Properties(); read(next); next.putAll(changes);
        Path destination = file.file().toPath().toAbsolutePath(), temporary = null;
        try {
            Files.createDirectories(destination.getParent());
            temporary = Files.createTempFile(destination.getParent(),"settings-",".tmp");
            try (var output = new FileOutputStream(temporary.toFile())) {
                next.store(output,null); output.getFD().sync();
            }
            try { Files.move(temporary,destination,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException error) { Files.move(temporary,destination,StandardCopyOption.REPLACE_EXISTING); }
            values.clear(); values.putAll(next);
        } catch (IOException error) { throw new IllegalStateException("Cannot save client settings",error); }
        finally { if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) {} }
    }
    public static String key(int id) {
        return switch (id) {
            case 600 -> "client.mobile.notch";
            case 1243 -> "client.graphics.android.immersive_mode";
            case 1254 -> "client.graphics.max_fps";
            case 1221 -> "client.graphics.gba.zoom_level";
            case 1297 -> "client.graphics.nds.reduced_fov";
            case 1264 -> "client.graphics.msaa_samples";
            case 1241 -> "client.graphics.max_visible_players";
            case 1257 -> "client.graphics.render.overworld_in_battle";
            case 1255 -> "client.graphics.render.battlebg";
            case 1253 -> "client.graphics.shader_quality";
            case 1252 -> "client.graphics.render_border";
            case 7701 -> "client.graphics.battle_sprites.quality";
            case 1237 -> "client.graphics.battle_sprites.render_scale.front";
            case 1238 -> "client.graphics.battle_sprites.render_scale.back";
            case 1233 -> "client.cache.model_cache.enable";
            case 1270 -> "client.graphics.display_mode";
            case 1217 -> "client.graphics.graphics_api";
            case 1276 -> "client.sound_engine.music_volume";
            case 1277 -> "client.sound_engine.sfx_volume";
            case 1279 -> "client.sound_engine.system_volume";
            case 1365 -> "client.sound_engine.output_device";
            case 1282 -> "client.sound_engine.music_mute_when_unfocused";
            case 1283 -> "client.sound_engine.battle_sfx.panning";
            case 1343 -> "client.ui.battle.notification";
            case 1260 -> "client.ui.theme";
            case 1247 -> "client.gui.scale.ui_zoom2";
            case 1274 -> "client.graphics.battle.size";
            case 1266 -> "client.gui.swcursor";
            case 609 -> "client.gui.scale.fbo_scaling.enabled";
            case 1245 -> "client.gui.scale.hidpifont";
            case 1236 -> "client.gui.scale.cjkfonts";
            case 1261 -> "client.ui.smallhud";
            case 1258 -> "client.graphics.show_fps";
            case 1248 -> "client.ui.particles";
            case 1299 -> "client.mobile.onscreencontrols.show";
            case 1290 -> "client.mobile.dpad.scale";
            case 1293 -> "client.mobile.dpad.deadzone_radius";
            case 1295 -> "client.mobile.dpad.sticky_directionals_strength";
            case 1291 -> "client.mobile.ab.scale";
            case 1292 -> "client.mobile.ab.reversed";
            case 1331 -> "client.mobile.hotbar.swap_party";
            case 1319 -> "client.ui.hud.battle.force_enable_battle_hud";
            case 1318 -> "client.ui.hud.battle.show_damage_percentage_level";
            case 1325 -> "client.gameplay.textspeed";
            case 1345 -> "client.gameplay.autosprint.enabled";
            case 1330 -> "client.gui.showchatbubbles";
            case 6160 -> "client.ui.nameplate.others";
            case 1347 -> "client.ui.nameplate.self";
            case 1284 -> "client.graphics.disabled.ow.particles";
            case 8603 -> "client.gameplay.auto_replenish_pp_items";
            case 1213 -> "client.graphics.battle.flash.animation";
            case 1993 -> "client.graphics.battle.camera_movement";
            case 1234 -> "client.ui.hud.battle.replays.enable_instant_replays";
            case 1317 -> "client.graphics.nds.hgss_sprites_enabled_in_gba";
            case 6156 -> "client.gameplay.hatching_animations";
            case 1315 -> "client.gameplay.follower_bounce";
            case 1349 -> "client.input.controllers.enable_native_support";
            case 1332 -> "client.ui.chat.language.current_language";
            case 1326 -> "client.ui.chat.word_filter.enabled";
            case 1342 -> "client.ui.chat.buffer.size";
            case 1335 -> "client.ui.chat.timestamp.enabled";
            case 1336 -> "client.ui.chat.type_name_display.enabled";
            case 1338 -> "client.ui.chat.language_name_display.enabled";
            case 1346 -> "client.ui.chat.linked_images.enabled";
            case 1219 -> "ipv6.enable";
            case 605 -> "client.android.foreground_service.enabled";
            case 1387 -> "client.integrations.discord_rich_presence.enabled";
            default -> throw new IllegalArgumentException("Unmapped setting " + id);
        };
    }
}
