'use strict';
const {BattleStream, Battle, Dex} = require('pokemon-showdown');

// Experimental formation adapter only. Reference: BattleFormatType.HORDE_BATTLE (1 vs 5),
// BattleMoveResolver.computeMoveTargetPokemons (all surrounding members, no triple lanes).
// Damage, spread modifiers, move effects, abilities, speed order and fainting remain Showdown.
function formationFormat(count) {
  const base = Dex.formats.get('gen5customgame@@@!Team Preview', true);
  const format = Object.assign(Object.create(Object.getPrototypeOf(base)), base, {
    name: `PokeMMO experimental 1v${count}`, gameType: 'doubles',
    onBegin() {
      this.activePerHalf = count;
      this.speedOrder = Array.from({length: count * 2}, (_, i) => i);
      for (const side of this.sides) {
        const size = side.n === 0 ? 1 : count;
        side.active = Array(size).fill(null);
        side.slotConditions = Array.from({length: size}, () => ({}));
        // Side.choose's text parser accepts target locations 1..3 only. For 4/5,
        // pass the parsed location to the original chooseMove API; all validation
        // and Battle.choose/commitChoices still execute inside Showdown.
        const choose = side.choose, chooseMove = side.chooseMove;
        let expandedTarget;
        side.chooseMove = function(move, loc, ...args) {
          return chooseMove.call(this,move,expandedTarget ?? loc,...args);
        };
        side.choose = function(input) {
          const match = /^move (\d+) ([+-]?[45])$/.exec(input);
          if(!match) return choose.call(this,input);
          expandedTarget=Number(match[2]);
          try { return choose.call(this,'move '+match[1]); }
          finally { expandedTarget=undefined; }
        };
        for (const pokemon of side.pokemon) {
          pokemon.isAdjacent = function (other) {
            return this !== other && !this.fainted && !other.fainted;
          };
        }
      }
    },
    battle: {
      validTargetLoc(loc, source, type) {
        if (loc === 0) return true;
        if (!Number.isInteger(loc)) return false;
        const side = loc < 0 ? source.side : source.side.foe;
        const target = side.active[Math.abs(loc)-1];
        if (!target) return false;
        const self = source === target, foe = !source.isAlly(target);
        switch (type) {
          case 'normal': case 'randomNormal': case 'scripted': return !self;
          case 'adjacentFoe': return foe;
          case 'adjacentAlly': return !foe && !self;
          case 'adjacentAllyOrSelf': return !foe;
          case 'any': return !self;
          default: return false;
        }
      },
    },
  });
  return format;
}

class HordeBattleStream extends BattleStream {
  constructor(count, options) { super(options); this.hordeSize = count; }
  _writeLine(type, message) {
    if (type !== 'start') return super._writeLine(type, message);
    const options = JSON.parse(message);
    options.format = formationFormat(this.hordeSize);
    options.send = (kind, data) => {
      this.pushMessage(kind, Array.isArray(data) ? data.join('\n') : data);
      if (kind === 'end' && !this.keepAlive) this.pushEnd();
    };
    this.battle = new Battle(options);
  }
}
module.exports = {HordeBattleStream};
