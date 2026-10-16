"""Import the supported Gigantamax sprites from a user-supplied TIMOD ZIP."""
from pathlib import Path
import hashlib
import json
import sys
import zipfile

SPECIES = (12, 25, 52, 68, 99, 131, 133, 143, 569)


def main():
    if len(sys.argv) != 2:
        raise SystemExit("Usage: python tools/import_gmax_mod.py <TIMOD ZIP>")
    archive = Path(sys.argv[1]).resolve()
    root = Path(__file__).resolve().parents[1] / "data/resources/pokemon-forms-mod"
    records = []
    pending = []
    with zipfile.ZipFile(archive) as mod:
        info = mod.read("info.xml")
        for species in SPECIES:
            for side in ("front", "back"):
                prefix = f"sprites/battlesprites/{species:03}-{side}-n."
                entry = next(name for name in mod.namelist()
                             if name.startswith(prefix) and name.endswith((".gif", ".png")))
                data = mod.read(entry)
                if not data.startswith((b"GIF87a", b"GIF89a", b"\x89PNG\r\n\x1a\n")):
                    raise ValueError("Invalid sprite: " + entry)
                output = root / str(species) / "gmax" / (side + "-n" + Path(entry).suffix)
                pending.append((output, data))
                records.append(dict(species=species, file=output.relative_to(root).as_posix(),
                                    archive_entry=entry, sha256=hashlib.sha256(data).hexdigest()))
        for output, data in pending:
            output.parent.mkdir(parents=True, exist_ok=True)
            output.write_bytes(data)
            (output.parent / "info.xml").write_bytes(info)
    manifest = dict(source_archive=archive.name, archive_sha256=hashlib.sha256(archive.read_bytes()).hexdigest(),
                    author="Tim011", mod="[TIMOD] VARIANT FORMS", version="2.0",
                    shiny="No shiny form assets supplied; shiny individuals use normal form colors.", files=records)
    (root / "gmax-sources.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("Imported", len(records), "Gigantamax sprites")


if __name__ == "__main__":
    main()
