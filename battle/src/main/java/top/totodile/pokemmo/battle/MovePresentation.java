package top.totodile.pokemmo.battle;

/** 招式展示变换采样，不修改宝可梦数值。 */
public final class MovePresentation {
    public static final int TACKLE = 33;
    public static final long DURATION = 1600;

    public record Frame(float advance, float shake, float impact, float alpha, boolean flash) { }

    private MovePresentation() { }

    public record Actor(float x, float y, float scaleX, float scaleY, float tint) { }

    public static Actor actor(int move, long milliseconds, int target) {
        float x=0,y=0,sx=1,sy=1,tint=1;
        if (move==TACKLE) return new Actor(x,y,sx,sy,tint);
        for (var command:MoveAnimations.get(move).commands()) {
            var a=command.arguments();long elapsed=milliseconds-command.start();
            if (a.isEmpty() || a.getFirst().intValue()!=target || elapsed<0) continue;
            if (command.operation().equals("EN") && a.size()>=7) {
                float half=Math.max(16,(a.get(3)+a.get(4))*500);
                float length=half*2*Math.max(1,a.get(2));
                if (elapsed<length) {
                    float wave=(float)Math.sin(elapsed/half*Math.PI);
                    x+=wave*a.get(5)*50*(target==14?1:-1); y-=wave*a.get(6)*50;
                }
            } else if (command.operation().equals("Xq0") && a.size()>=7) {
                float period=Math.max(16,(a.get(3)+a.get(4))*1000);
                float length=period*2*Math.max(1,a.get(2));
                if (elapsed<length) {
                    float progress=Math.min(1,elapsed/period);
                    int mode=a.get(1).intValue();
                    float wave=(float)Math.sin(elapsed/period*Math.PI);
                    sx=mode<2?1+(a.get(5)-1)*progress:1+a.get(5)*wave;
                    sy=mode<2?1+(a.get(6)-1)*progress:1+a.get(6)*wave;
                }
            } else if (command.operation().equals("WW") && a.size()>=4) {
                float progress=Math.min(1,elapsed/Math.max(16,a.get(1)*1000));
                tint=1-Math.clamp(a.get(2)+(a.get(3)-a.get(2))*progress,0,1);
            }
        }
        return new Actor(x,y,Math.clamp(sx,.05f,4),Math.clamp(sy,.05f,4),tint);
    }

    public static float backdrop(int move,long milliseconds) {
        if (move==TACKLE) return 0;
        float opacity=0;
        for(var command:MoveAnimations.get(move).commands()) {
            var a=command.arguments();long elapsed=milliseconds-command.start();
            if(elapsed>=0&&command.operation().equals("Ue0")&&a.size()>=5&&a.getFirst().intValue()==4) {
                float progress=Math.min(1,elapsed/Math.max(16,a.get(4)*10000));
                opacity=Math.clamp(a.get(2)+(a.get(3)-a.get(2))*progress,0,1);
            }
        }
        return opacity;
    }

    public static Frame sample(int move, long milliseconds) {
        return move==TACKLE?sample(milliseconds):new Frame(0,0,0,0,false);
    }

    public static Frame sample(long milliseconds) {
        long time = Math.max(0, milliseconds);
        float advance = time < 400 ? 0 : time < 560 ? (time - 400) / 160f
                : time < 960 ? 1 - (time - 560) / 400f : 0;
        float hit = (time - 560) / 500f;
        float alpha = hit >= 0 && hit < 1 ? 1 - hit : 0;
        float shake = alpha * (float) Math.sin(Math.max(0, time - 560) * .09) * 8;
        return new Frame(advance, shake, Math.max(0, Math.min(1, hit)), alpha,
                time >= 560 && time < 760 && ((time - 560) / 50) % 2 == 0);
    }
}
