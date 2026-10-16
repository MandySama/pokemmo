"""Import Charizard battle GIFs from the author's PokeMMO Mega sprite mods."""
from pathlib import Path
import hashlib
import html
import json
import re
import subprocess
import zipfile

BASE = Path(__file__).resolve().parents[1]
ROOT = BASE / "data/resources/pokemon-forms-mod"
TOPIC = "https://forums.pokemmo.com/index.php?/topic/154855-mega-evolution-sprites-2d/"
PACKS = {
    "megax": "https://www.mediafire.com/file/dodwkr5767f1c88/PokeMMO+Mega+Sprites+-+X.zip/file",
    "megay": "https://www.mediafire.com/file/h6m938xz2xgcz6n/PokeMMO+Mega+Sprites+-+Y.zip/file",
}


def download(url, target):
    subprocess.run(["curl.exe", "-sS", "-L", "--fail", "--max-time", "90",
                    "-A", "Mozilla/5.0", url, "-o", str(target)], check=True)


def main():
    cache = BASE / "target/pokemon-form-mods"
    cache.mkdir(parents=True, exist_ok=True)
    records = []
    for form, page in PACKS.items():
        archive = cache / (form + ".zip")
        if not archive.exists():
            listing = cache / (form + ".html")
            download(page, listing)
            links = re.findall(r'https://download[^"<> ]+', listing.read_text(encoding="utf-8"))
            if not links:
                raise RuntimeError("Download link missing: " + page)
            download(html.unescape(links[0]), archive)
        digest = hashlib.sha256(archive.read_bytes()).hexdigest()
        folder = ROOT / "6" / form
        folder.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(archive) as mod:
            (folder / "info.xml").write_bytes(mod.read("info.xml"))
            for side in ("front", "back"):
                for color in ("n", "s"):
                    entry = f"sprites/battlesprites/006-{side}-{color}.gif"
                    data = mod.read(entry)
                    if not data.startswith((b"GIF87a", b"GIF89a")):
                        raise ValueError("Invalid sprite: " + entry)
                    output = folder / f"{side}-{color}.gif"
                    output.write_bytes(data)
                    records.append(dict(file=output.relative_to(ROOT).as_posix(), archive_entry=entry,
                                        sha256=hashlib.sha256(data).hexdigest(), mod="PokeMMO Mega Sprites " + form,
                                        version="2.1.3", author="AdamantSceptile", download_page=page,
                                        archive_sha256=digest))
    manifest = dict(topic=TOPIC, credits=["MBCMechachu", "Diegotoon20", "Pokemon Showdown Gen 5 sprite resource",
                                       "Pokemon Indigo fan game (animated back sprites)"],
                    rights="Source and artist credits retained. Public download does not establish a general redistribution license.",
                    files=records)
    (ROOT / "sources.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print("Imported", len(records), "Charizard MOD animations")


if __name__ == "__main__":
    main()
