package top.totodile.pokemmo.client.startup;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import java.io.IOException;
import java.nio.*;

/** 对应 KeyframeAnimationTrack 与 Abstract3DCutsceneScene.pG0，30 FPS。 */
final class RomCameraTrack {
    private final ByteBuffer data;
    final int frames;
    private final int stride;
    RomCameraTrack(byte[] bytes) throws IOException {
        data=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (bytes.length<8) throw new IOException("Missing login camera track");
        frames=data.getInt();
        stride=switch (data.getInt()) { case 65792 -> 24; case 65793 -> 36; default -> 0; };
        if (frames<=0 || stride==0 || (long)frames*stride!=bytes.length-8) throw new IOException("Invalid login camera track");
    }
    private float sample(int component,int frame) { return data.getFloat(8+Math.min(frames-1,frame)*stride+component*4); }
    void apply(PerspectiveCamera camera, float time) {
        float frame=Math.max(0,time*30);
        int first=Math.min((int)frame,frames-1), next=Math.min(first+1,frames-1);
        float fraction=frame-first, distance=0, rotation=0;
        for (int i=0;i<3;i++) {
            rotation+=Math.abs(sample(i,next)-sample(i,first));
            distance+=Math.abs(sample(i+3,next)-sample(i+3,first));
        }
        if (rotation>=10 || distance>=10) fraction=0;
        float[] values=new float[6];
        for (int i=0;i<6;i++) values[i]=sample(i,first)+(sample(i,next)-sample(i,first))*fraction;
        camera.position.set(values[3],values[4],values[5]);
        camera.direction.set(0,0,-1);
        camera.up.set(Vector3.Y);
        for (int i=0;i<3;i++) {
            Vector3 axis=i==0?Vector3.X:i==1?Vector3.Y:Vector3.Z;
            camera.direction.rotate(axis,values[i]); camera.up.rotate(axis,values[i]);
        }
        camera.update();
    }
}
