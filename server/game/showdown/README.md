# Showdown engine pilot

The server exposes two independent `BattleEngine` strategies:

- `CUSTOM`: reserved for a future reference-derived battle engine; `start` explicitly reports unimplemented. The existing custom encounter preview/animation path is retained.
- `SHOWDOWN`: real Pokémon Showdown 0.11.10 `BattleStream` in a local Node process. No damage or battle-rule calculations are duplicated in Java.

## Run from the source checkout

Install Node.js, then run at the repository root:

```powershell
npm.cmd ci --prefix server/game/showdown --ignore-scripts --omit=optional
.\gradlew.bat :client:desktop:run
```

The desktop single-encounter action defaults to Showdown. To keep the old preview path, set `server.battle.engine=custom` in `client/config/main.properties`. Set it to `showdown` to enable the engine again. Invalid engine names are rejected. Android local mode and 3/5-member hordes retain the preview path; they do not claim to run Showdown.

Java can locate the adapter from the repository or a parent directory. External installations can set JVM properties `pokemmo.showdown.bridge` (absolute bridge.cjs path) and `pokemmo.showdown.node` (Node executable). No runtime downloads occur. Packaged desktop/Android standalone distribution of Node is not included in this pilot.

## Temporary specified-move testing

In the battle move panel, click `指定招式（测试）`, search by move ID or localized name, then click `使用` (or press Enter in the search field). The current active Pokemon uses that move for one turn without any species learnset check. The original four moves, their PP, and the character save are not changed. Showdown still handles turn order, status/target restrictions and battle resolution. The custom preview path can also preview a specified move.

Only moves in the loaded catalogue are listed; engine-specific custom IDs still require a verified Showdown mapping. Unknown IDs are rejected rather than approximated. This is an explicit test action, separate from normal move-slot submission.

## Scope

- Generation 5 singles and experimental 1v3/1v5 hordes, no team preview, generated party snapshots, original move/ability IDs, IVs/EVs/nature, initial HP/status/PP and PP Ups.
- Real turns, legal move checks, opponent default legal choice, damage, status, PP, switching, forced replacement, Struggle and battle end. The opponent is a test opponent, not a trained or PokeMMO AI.
- Existing client action animations follow Showdown's move order; battle snapshots update HP/status/PP after the action sequence. Missing move animations do not substitute damage calculations.
- Actor names and move names use existing ROM/localization resources, never raw engine protocol in the chat UI.
- Preview state remains transient: no HP/PP save writes, EXP, rewards or capture settlement.
- Held items and non-base forms are explicitly rejected until their PokeMMO ID mappings are verified. They are never silently stripped or guessed. The bag is disabled in this engine mode.
- One process per battle; replies time out after 15 seconds. Leaving, closing or replacing a battle cleans up its process. Errors do not silently switch to fake calculations.

## Experimental horde formation

Three- and five-member encounters now enter SHOWDOWN when that engine is selected. `horde.cjs` supplies a custom 1vN formation through a BattleStream subclass and format hooks; upstream files in `node_modules` are not changed. Singles keep the standard format.

The formation follows reference `BattleFormatType.HORDE_BATTLE` (one player active, up to five enemy actives) and `BattleMoveResolver.computeMoveTargetPokemons`: the entire surrounding faction is in range, rather than triple-battle lanes. Native Showdown still computes turn order, damage (including its 0.75 spread multiplier, also present in reference `BattleDamageCalculator`), abilities, status, PP, redirection, fainting and replacements. Target locations 4/5 are handed to the original `Side.chooseMove` API because the standard text parser only accepts 1..3.

The client sends a stable target UUID for selected moves and reuses its existing enemy-target panel. Spread events include all affected target UUIDs for multi-target animations. Fainted enemies retain their original slots and leave the field; only living targets can be selected. These are experimental formation semantics, not a claim of full PokeMMO rules parity. Existing item/form, transient-save, bag and reward limitations above still apply.

## Validation

```powershell
node --test server/game/showdown/bridge.test.cjs
```

The Java desktop tests `ShowdownEngineTest` and `ShowdownBattleUiTest` exercise Java→Node round trips and real rendered battle controls/HP updates. The existing custom preview remains separately testable.

## Primary references

- [Official simulator API](https://github.com/smogon/pokemon-showdown/blob/master/sim/SIMULATOR.md)
- [Official battle protocol](https://github.com/smogon/pokemon-showdown/blob/master/sim/SIM-PROTOCOL.md)
- [Showdown source and license](https://github.com/smogon/pokemon-showdown)
- Existing PokeMMO reference `BATTLE_COMMAND_PACKET.md`, `BattleManager`, `BattleSwitchService`; client `BattleActionPromptModifier` and existing animation/presentation classes.
