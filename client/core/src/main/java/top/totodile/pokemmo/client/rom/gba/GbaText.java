package top.totodile.pokemmo.client.rom.gba;

/** The international GBA character map and control codes used by reference GbaTextDecoder.Xc. */
final class GbaText {
    private static final String[] TEXT=new String[256];
    static {
        for(int i=0;i<10;i++)TEXT[0xa1+i]=Character.toString((char)('0'+i));
        for(int i=0;i<26;i++) {
            TEXT[0xbb+i]=Character.toString((char)('A'+i));
            TEXT[0xd5+i]=Character.toString((char)('a'+i));
        }
        TEXT[0]=" ";TEXT[1]="À";TEXT[2]="Á";TEXT[3]="Â";
        TEXT[4]="Ç";TEXT[5]="È";TEXT[6]="É";TEXT[7]="Ê";
        TEXT[8]="Ë";TEXT[9]="Ì";TEXT[11]="Î";TEXT[12]="Ï";
        TEXT[13]="Ò";TEXT[14]="Ó";TEXT[15]="Ô";TEXT[16]="Œ";
        TEXT[17]="Ù";TEXT[18]="Ú";TEXT[19]="Û";TEXT[20]="Ñ";
        TEXT[21]="ß";TEXT[22]="à";TEXT[23]="á";TEXT[25]="ç";
        TEXT[26]="è";TEXT[27]="é";TEXT[28]="ê";TEXT[29]="ë";
        TEXT[30]="ì";TEXT[32]="î";TEXT[33]="ï";TEXT[34]="ò";
        TEXT[35]="ó";TEXT[36]="ô";TEXT[37]="œ";TEXT[38]="ù";
        TEXT[39]="ú";TEXT[40]="û";TEXT[41]="ñ";TEXT[42]="º";
        TEXT[43]="ª";TEXT[44]="er";TEXT[45]="&";TEXT[46]="+";
        TEXT[52]="[Lv]";TEXT[53]=", ";TEXT[54]=";";TEXT[81]="¿";
        TEXT[82]="¡";TEXT[83]="PK";TEXT[84]="MN";TEXT[85]="PO";
        TEXT[86]="Ké";TEXT[87]="BL";TEXT[88]="OC";TEXT[89]="K";
        TEXT[90]="Í";TEXT[91]="%";TEXT[92]="(";TEXT[93]=")";
        TEXT[94]=" PO";TEXT[95]="Ké";TEXT[96]="ME";TEXT[97]="LL";
        TEXT[98]="";TEXT[99]="E";TEXT[104]="â";TEXT[111]="í";
        TEXT[121]="↑";TEXT[122]="↓";TEXT[123]="←";TEXT[124]="→";
        TEXT[132]="e";TEXT[133]="<";TEXT[134]=">";TEXT[136]="Ć";
        TEXT[137]="Ę";TEXT[145]="Ó";TEXT[146]="ą";TEXT[147]="ę";
        TEXT[148]="ó";TEXT[149]="ś";TEXT[150]="ł";TEXT[151]="ń";
        TEXT[152]="ż";TEXT[153]="ź";TEXT[154]="ć";TEXT[155]="Ą";
        TEXT[156]="Ż";TEXT[157]="Ź";TEXT[158]="Ł";TEXT[159]="Ś";
        TEXT[171]="!";TEXT[172]="?";TEXT[173]=".";TEXT[174]="-";
        TEXT[175]="·";TEXT[176]="...";TEXT[177]="«";TEXT[178]="»";
        TEXT[179]="\'";TEXT[180]="\'";TEXT[181]="♂";TEXT[182]="♀";
        TEXT[183]="$";TEXT[184]=",";TEXT[185]="*";TEXT[186]="/";
        TEXT[239]="|>|";TEXT[240]=":";TEXT[241]="Ä";TEXT[242]="Ö";
        TEXT[243]="Ü";TEXT[244]="ä";TEXT[245]="ö";TEXT[246]="ü";
        TEXT[247]="|A|";TEXT[248]="|V|";TEXT[249]="|<|";TEXT[250]="\n\n";
        TEXT[251]="\n\n";TEXT[252]="|FC|";TEXT[253]="|FD|";TEXT[254]="\n";
        TEXT[255]="|end|";
    }
    private GbaText() {}
    static String decode(byte[] rom,int offset) {
        if(offset<0 || offset>=rom.length)throw new IllegalArgumentException("Invalid GBA text offset");
        StringBuilder text=new StringBuilder();
        int end=(int)Math.min(rom.length,(long)offset+4096);
        for(int p=offset;p<end;) {
            int value=rom[p++]&255;
            if(value==0xff)return text.toString();
            if(value==0xfc) {
                if(p>=end)break;
                int control=rom[p++]&255;
                p+=switch(control) {case 6,8,17 -> 1;case 11,16 -> 2;default -> 0;};
            } else if(value==0xfd) {
                if(p>=end)break;
                int variable=rom[p++]&255;
                text.append('{').append(Character.toUpperCase(Character.forDigit(variable>>>4,16)))
                        .append(Character.toUpperCase(Character.forDigit(variable&15,16))).append('}');
            } else {
                String character=TEXT[value];
                if(character==null)return text.toString();
                text.append(character);
            }
        }
        throw new IllegalArgumentException("Unterminated GBA text");
    }
}
