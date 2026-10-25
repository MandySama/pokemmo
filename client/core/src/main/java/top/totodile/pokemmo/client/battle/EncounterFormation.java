package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.math.Vector3;

/** BattlePokemonCombatant.Sz / Or / ZK: enemy positions in the five-slot horde format. */
final class EncounterFormation {
    private static final float[] X = {-.36f, -.5f, .25f, 1, .36f};
    private static final float[] Z = {0, -.75f, -.55f, -.25f, .1f};

    static Vector3 enemy(boolean horde, int slot) {
        return horde ? new Vector3(5 + X[slot], 3.08f, 1.5f + Z[slot]) : new Vector3(5, 3.08f, 1);
    }

    static int hudColumn(int slot) {
        return slot < 2 ? -1 : slot == 2 ? 0 : 1;
    }

    static int hudRow(int slot) {
        return slot == 0 || slot == 4 ? 1 : 0;
    }
}
