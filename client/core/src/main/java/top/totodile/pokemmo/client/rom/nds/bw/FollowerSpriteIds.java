package top.totodile.pokemmo.client.rom.nds.bw;

/** Direct mapping from reference PokemonSpeciesCryModelMapper.Prn/gu. */
public final class FollowerSpriteIds {
    public static short nativeModel(int i, short s) {
        switch (s) {
            case 494:
                return 125;
            case 504:
                return 128;
            case 517:
                return 126;
            case 518:
                return 127;
            case 546:
                return 209;
            case 548:
                return 129;
            case 552:
                return 131;
            case 570:
                return 195;
            case 572:
                return 130;
            case 585:
                switch (i) {
                    case 0:
                        return 217;
                    case 1:
                        return 218;
                    case 2:
                        return 219;
                    case 3:
                        return 220;
                    default:
                        return 0;
                }
            case 638:
                return 115;
            case 639:
                return 116;
            case 640:
                return 117;
            case 647:
                return 122;
            case 648:
                return 123;
            default:
                return 0;
        }
    }

    public static short modelId(short s, boolean isFemale, boolean isShiny, int form) {
        if (s < 3) {
            return s;
        }
        if (s == 3) {
            if (isFemale) {
                s = (short) (s + 1);
            }
            return s;
        }
        short cur = (short) (s + 1);
        if (s < 25) {
            return cur;
        }
        if (s == 25) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s < 154) {
            return cur;
        }
        if (s == 154) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s <= 172) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 1);
        if (s <= 201) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 27);
        if (s == 202) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s < 208) {
            return cur;
        }
        if (s == 208) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s < 214) {
            return cur;
        }
        if (s == 214) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s <= 386) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 3);
        if (s <= 412) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 2);
        if (s <= 413) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 2);
        if (s < 415) {
            return cur;
        }
        if (s == 415) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s <= 422) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 1);
        if (s <= 423) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 1);
        if (s < 443) {
            return cur;
        }
        if (s == 443) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s == 444) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s == 445) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s < 449) {
            return cur;
        }
        if (s == 449) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s == 450) {
            if (isFemale) {
                cur = (short) (cur + 1);
            }
            return cur;
        }
        cur = (short) (cur + 1);
        if (s <= 479) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 5);
        if (s <= 487) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 1);
        if (s <= 492) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 1);
        if (s == 493) {
            return (short) (cur + form);
        }
        cur = (short) (cur + 18);
        if (isShiny) {
            if (s == 585) {
                return (short) (cur + form);
            }
            return cur;
        }
        if (nativeModel(form, s) > 0) {
            return cur;
        }
        return 0;
    }

}
