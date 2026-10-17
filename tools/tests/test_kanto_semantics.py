"""Check dataset coverage and source-backed provenance without a game save."""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


class SemanticsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        text = (ROOT / 'exports/fire-red-map-viewer/explorer-data.js').read_text(encoding='utf-8')
        cls.data = json.loads(text.removeprefix('const kantoData=').strip().removesuffix(';'))
        cls.semantics = cls.data['semantics']

    def test_every_event_has_guide(self):
        count = 0
        for m in self.data['maps']:
            for kind in ('npcs','warps','coordinate-events','background','map-scripts'):
                for e in m[kind]:
                    guide = self.semantics['events'][e['event_key']]
                    self.assertEqual(guide['map'], f'{m["group"]}/{m["map"]}')
                    self.assertEqual(guide['kind'], kind)
                    for state in guide['reads']:
                        self.assertIn(state, self.semantics['states'])
                    count += 1
        self.assertEqual(len(self.data['maps']), 425)
        self.assertEqual(count, self.semantics['coverage']['events'])

    def test_pallet_gate_and_starter_provenance(self):
        gate=self.semantics['events']['3/0:coordinate-events:2']['gate']
        self.assertEqual(gate, {'state':'3/0:variable:16386','value':1})
        self.assertTrue(any(w['event'].startswith('3/0:map-scripts:') and w['value']==1
                            for w in self.semantics['writers'][gate['state']]))
        self.assertTrue(any('初始' in ' '.join(self.semantics['events'][w['event']]['scenes'])
                            for w in self.semantics['writers']['flag:657'] if w['value']==1))

    def test_temporary_records_are_map_scoped(self):
        for k,v in self.semantics['states'].items():
            if v['temporary']:
                self.assertRegex(k, r'^\d+/\d+:')
                for writer in self.semantics['writers'].get(k,[]):
                    self.assertEqual(self.semantics['events'][writer['event']]['map'], k.split(':')[0])

    def test_every_provenance_link_resolves(self):
        for rows in self.semantics['writers'].values():
            for row in rows:self.assertIn(row['event'],self.semantics['events'])
        for ids in self.semantics['aliases'].values():
            self.assertEqual(len({self.semantics['events'][id]['entry'] for id in ids}),1)


if __name__ == '__main__': unittest.main()
