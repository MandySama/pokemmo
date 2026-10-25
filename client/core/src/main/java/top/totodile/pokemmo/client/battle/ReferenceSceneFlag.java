package top.totodile.pokemmo.client.battle;

/** Port of Modern_Util_Lpt44 / lpt4__4. */
final class ReferenceSceneFlag implements Runnable {
    private final EncounterMoveAnimation owner;
    private final int flag;
    private final boolean value;

    ReferenceSceneFlag(EncounterMoveAnimation owner, int flag, boolean value) {
        this.owner = owner;
        this.flag = flag;
        this.value = value;
    }

    public void run() {
        switch (flag) {
            case 0 -> owner.hidePlatforms = !value;
            case 1 -> owner.useBackground = !value;
            case 2 -> owner.backgroundEnabled = value;
            default -> {}
        }
    }
}
