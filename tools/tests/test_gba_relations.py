"""Check native-table coverage, reference integrity, and the game's read-only boundary."""
from pathlib import Path
import hashlib
import json
import struct
import unittest

ROOT = Path(__file__).resolve().parents[2]


class RelationsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        text = (ROOT / 'exports/fire-red-map-viewer/relations-data.js').read_text(encoding='utf-8')
        cls.data = json.loads(text.removeprefix('const gbaRelations=').strip().removesuffix(';'))
        cls.rom = (ROOT / 'roms/firered.gba').read_bytes()

    def test_source_and_references(self):
        self.assertEqual(hashlib.sha256(self.rom).hexdigest(), self.data['sourceHash'])
        self.assertEqual(len(self.data['maps']), 425)
        self.assertEqual(self.data['diagnostics'], [])
        self.assertTrue(all(e['source'] in self.data['nodes'] and e['target'] in self.data['nodes'] for e in self.data['edges']))
        self.assertEqual(len({e['id'] for e in self.data['edges']}), len(self.data['edges']))

    def test_all_raw_event_counts(self):
        u32 = lambda at: struct.unpack_from('<I', self.rom, at)[0]
        for key, ids in self.data['maps'].items():
            nodes = [self.data['nodes'][id] for id in ids]
            header = int(nodes[0]['detail'][1].split()[1], 16)
            events = u32(header + 4) - 0x08000000
            if not 0 < events < len(self.rom):
                continue
            for slot, kinds in enumerate([{'npc'}, {'warp'}, {'coordinate', 'weather'}, {'background'}]):
                self.assertEqual(sum(n['kind'] in kinds for n in nodes), self.rom[events + slot], (key, slot))

    def test_pallet_native_relations(self):
        nodes, edges = self.data['nodes'], self.data['edges']
        npc = 'npc:3/0:0'
        self.assertEqual(nodes[npc]['record']['script_offset'], '0x0016575C')
        self.assertTrue(any(e['source'] == npc and e['target'] == 'script:0x0016575C' for e in edges))
        self.assertTrue(any(e['target'] == npc and e['relation'] == 'affects' for e in edges))
        self.assertTrue(any(e['target'] == 'variable:0x00004070' and e['relation'] == 'reads' for e in edges))
        self.assertTrue(any(n['kind'] == 'background' for n in (nodes[id] for id in self.data['maps']['3/0'])))

    def test_view_is_not_referenced_by_game(self):
        for directory in ['protocol/src', 'client/src', 'sever/game/src']:
            for path in (ROOT / directory).rglob('*.java'):
                text = path.read_text(encoding='utf-8')
                self.assertNotIn('ManagedStoryCatalog', text, str(path))
                self.assertNotIn('gba-view:', text, str(path))
                self.assertNotIn('relations-data.js', text, str(path))


if __name__ == '__main__':
    unittest.main()
