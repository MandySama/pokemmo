package top.totodile.particlebridge;

import com.badlogic.gdx.graphics.Color;
import cn.pokemmo.graphics.gdx.render.GdxLayeredTexture;
import cn.pokemmo.graphics.animation.tween.WorldEntityTweenAccessor;
import f.*;

/** 原补间对象在无网格的 CPU 实例上执行，宿主读取位置、缩放和材质颜色。 */
public final class PoseBridge {
    private static final dl_1 UF = Cq0.E1(PoseBridge.class);
    private static final Color T8 = new Color();
    private final es_1 g10 = new es_1();
    private final com3__3[] lu;
    private final es_1 aZ;
    private final boolean Am = true;
    private final pw_1 timeline = pw_1.gb0();
    private float elapsed;

    static {
        ao_1.Gy0 = 4;
        ao_1.LH0.put(com3__3.class, new WorldEntityTweenAccessor());
        var model = new ut_0();
        var node = new Xz0();
        node.sJ0.Ue0(new I20(new U30(), new BM()));
        model.Wc0.Ue0(node);
        GdxLayeredTexture.lg0.j10(GdxLayeredTexture.lg0.yw0(65537), model);
    }

    private static com3__3 actor() {
        var result = new com3__3(1, 1, new LPT6_(), true);
        result.OF0(.01f);
        return result;
    }

    public PoseBridge(String[] operations, long[] starts, float[][] arguments) {
        lu = new com3__3[]{actor()};
        aZ = new es_1(new com3__3[]{actor()});
        for (int i = 0; i < operations.length; i++) {
            float[] a = arguments[i];
            pw_1 child = switch (operations[i]) {
                case "EN" -> EN((int)a[0], (int)a[1], (int)a[2], a[3], a[4], a[5], a[6]);
                case "Xq0" -> Xq0((int)a[0], (int)a[1], (int)a[2], a[3], a[4], a[5], a[6]);
                case "WW" -> WW((int)a[0], a[1], a[2], a[3], px_1.ep0((int)a[4]));
                case "df0" -> df0((int)a[0], (int)a[1]);
                default -> null;
            };
            if (child != null) timeline.xi0(pw_1.xC().p1(starts[i] / 1000f).xi0(child));
        }
        timeline.t();
    }

    public float duration() { return timeline.oX(); }

    public float[][] sample(float seconds) {
        if (seconds < elapsed) throw new IllegalArgumentException("Pose time cannot move backwards");
        timeline.mh(seconds - elapsed);
        elapsed = seconds;
        return new float[][]{state(lu[0]), state((com3__3)aZ.get(0))};
    }

    private static float[] state(com3__3 actor) {
        Color overlay = actor.Cf0.Vr;
        return new float[]{actor.j.x, actor.j.y, actor.j.z, actor.oW.x * 100, actor.oW.y * 100,
                overlay.r, overlay.g, overlay.b, overlay.a, actor.MI0().a};
    }

    public void close() { timeline.bC0(); }

    public pw_1 Ue0(int a, int b, float c, float d, float e) { return pw_1.gb0(); }

   public final pw_1 EN(int var1, int var2, int var3, float var4, float var5, float var6, float var7) {
      this.g10.clear();
      pw_1 var8 = pw_1.xC();
      if (var1 != 14) {
         if (var1 != 16) {
            if (!this.Am) {
               UF.error("Unknown target_id for getMoveAnimation = {}", var1);
            }

            return var8;
         }

         es_1 var9;
         if ((var9 = this.aZ) != null && var9.KB > 0) {
            es_1 var10;
            es_1 var10000 = var10 = this.g10;
            es_1 var10001 = var9;
            var10.getClass();
            Object[] var52 = var9.rZ;
            int var55 = var10001.KB;
            var10000.G6(var52, 0, var55);
         }
      } else {
         es_1 var53;
         es_1 var57 = var53 = this.g10;
         com3__3[] var56;
         com3__3[] var80 = var56 = this.lu;
         var53.getClass();
         int var54 = var80.length;
         var57.G6(var56, 0, var54);
      }

      label93: {
         float var81 = var5;
         float var10002 = var4;
         var4 = var6 / 4.0F;
         var5 = var7 / 4.0F;
         var6 = (var81 + var10002) / 2.0F;
         if (var2 != 14) {
            if (var2 != 15) {
               switch (var2) {
                  case 0:
                  case 4:
                     var8.Xf0();
                     I2 var14 = this.g10.ZD();

                     while(var14.hasNext()) {
                        com3__3 var24 = (com3__3)var14.next();
                        pw_1 var72 = var8.TD0();
                        ao_1 var95 = ao_1.DX(var24, 1, var6);
                        float var40 = var24.j.x + var4;
                        var95.h5[0] = var40;
                        var72 = var72.y80(var95);
                        var95 = ao_1.DX(var24, 1, var6);
                        float var25 = var24.j.x;
                        var95.h5[0] = var25;
                        var72.y80(var95).mz0();
                     }
                     break;
                  case 1:
                     var8.Xf0();
                     I2 var13 = this.g10.ZD();

                     while(var13.hasNext()) {
                        com3__3 var22 = (com3__3)var13.next();
                        pw_1 var68 = var8.TD0();
                        ao_1 var91 = ao_1.DX(var22, 1, var6);
                        float var37 = var22.j.x + var4;
                        var91.h5[0] = var37;
                        var68 = var68.y80(var91);
                        var91 = ao_1.DX(var22, 1, var6);
                        var37 = var22.j.x;
                        var91.h5[0] = var37;
                        var68.y80(var91).mz0();
                        var68 = var8.TD0();
                        var91 = ao_1.DX(var22, 2, var6);
                        var37 = var22.j.y + var5;
                        var91.h5[0] = var37;
                        var68 = var68.y80(var91);
                        var91 = ao_1.DX(var22, 2, var6);
                        float var23 = var22.j.y;
                        var91.h5[0] = var23;
                        var68.y80(var91).mz0();
                     }
                     break;
                  case 2:
                     var8.Xf0();
                     I2 var12 = this.g10.ZD();

                     while(var12.hasNext()) {
                        com3__3 var20 = (com3__3)var12.next();
                        pw_1 var64 = var8.TD0();
                        ao_1 var87 = ao_1.DX(var20, 1, var6);
                        float var34 = var20.j.x + var4;
                        var87.h5[0] = var34;
                        var64 = var64.y80(var87);
                        var87 = ao_1.DX(var20, 1, var6);
                        var34 = var20.j.x;
                        var87.h5[0] = var34;
                        var64.y80(var87).mz0();
                        var64 = var8.TD0();
                        var87 = ao_1.DX(var20, 2, var6);
                        var34 = var20.j.y + var5;
                        var87.h5[0] = var34;
                        var64 = var64.y80(var87);
                        var87 = ao_1.DX(var20, 2, var6);
                        float var21 = var20.j.y;
                        var87.h5[0] = var21;
                        var64.y80(var87).mz0();
                     }

                     var8.mz0().Yu0(var3, 0.0F);
                     return var8;
                  case 3:
                     var8.Xf0();
                     I2 var17 = this.g10.ZD();

                     while(var17.hasNext()) {
                        com3__3 var30 = (com3__3)var17.next();
                        pw_1 var58 = var8.TD0();
                        ao_1 var82 = ao_1.DX(var30, 1, var6);
                        var7 = var30.j.x + var4;
                        var82.h5[0] = var7;
                        var58 = var58.y80(var82);
                        var82 = ao_1.DX(var30, 1, var6);
                        var7 = var30.j.x - var4;
                        var82.h5[0] = var7;
                        var58.y80(var82).mz0();
                        var58 = var8.TD0();
                        var82 = ao_1.DX(var30, 3, var6);
                        var7 = var30.j.z + var5;
                        var82.h5[0] = var7;
                        var58 = var58.y80(var82);
                        var82 = ao_1.DX(var30, 3, var6);
                        float var31 = var30.j.z - var5;
                        var82.h5[0] = var31;
                        var58.y80(var82).mz0();
                     }

                     var8.mz0().Yu0(var3, 0.0F);
                     I2 var11 = this.g10.ZD();

                     while(var11.hasNext()) {
                        com3__3 var18;
                        com3__3 var62 = var18 = (com3__3)var11.next();
                        ao_1 var32;
                        ao_1 var86 = var32 = ao_1.DX(var62, 1, var6);
                        float var45 = var18.j.x;
                        var86.h5[0] = var45;
                        var8.y80(var32);
                        ao_1 var63 = var32 = ao_1.DX(var62, 3, var6);
                        float var19 = var18.j.z;
                        var63.h5[0] = var19;
                        var8.y80(var32);
                     }
                     break label93;
                  case 5:
                     return var8;
                  default:
                     if (!this.Am) {
                        UF.error("Unknown type_id for getMoveAnimation = {}", var1);
                     }

                     return var8;
               }
            } else {
               this.Ue0(var2, 0, 0.0F, 1.0F, 3.0F);
               var8.Xf0();
               I2 var15 = this.g10.ZD();

               while(var15.hasNext()) {
                  com3__3 var26 = (com3__3)var15.next();
                  pw_1 var74 = var8.Xf0().y80(ao_1.yp(8, var26).Om0(new float[]{1.0F, 1.0F, 1.0F, 1.0F}));
                  ao_1 var97 = ao_1.DX(var26, 1, var6);
                  float var41 = var26.j.x - var4;
                  var97.h5[0] = var41;
                  var74.y80(var97).mz0();
                  var74 = var8.Xf0().y80(ao_1.yp(8, var26).Om0(new float[]{1.0F, 1.0F, 1.0F, 1.0F}));
                  var97 = ao_1.DX(var26, 2, var6);
                  float var27 = var26.j.y - var5;
                  var97.h5[0] = var27;
                  var74.y80(var97).mz0();
               }
            }
         } else {
            this.Ue0(var2, 0, 1.0F, 0.0F, 3.0F);
            var8.Xf0();
            I2 var16 = this.g10.ZD();

            while(var16.hasNext()) {
               com3__3 var28 = (com3__3)var16.next();
               pw_1 var76 = var8.Xf0();
               ao_1 var99 = ao_1.DX(var28, 9, var6);
               float var42 = 0.0F;
               var99.h5[0] = var42;
               var76 = var76.y80(var99);
               var99 = ao_1.DX(var28, 1, var6);
               var42 = var28.j.x + var4;
               var99.h5[0] = var42;
               var76.y80(var99).mz0();
               var76 = var8.Xf0();
               var99 = ao_1.DX(var28, 9, var6);
               var42 = 0.0F;
               var99.h5[0] = var42;
               var76 = var76.y80(var99);
               var99 = ao_1.DX(var28, 2, var6);
               float var29 = var28.j.y + var5;
               var99.h5[0] = var29;
               var76.y80(var99).mz0();
            }
         }

         var8.mz0();
      }

      return var8;
   }

   public final pw_1 Xq0(int var1, int var2, int var3, float var4, float var5, float var6, float var7) {
      this.g10.clear();
      pw_1 var8 = pw_1.xC();
      if (var1 != 14) {
         if (var1 != 16) {
            if (!this.Am) {
               UF.error("Unknown target_id for getScaleAnimation = {}", var1);
            }

            return var8;
         }

         es_1 var9;
         if ((var9 = this.aZ) != null && var9.KB > 0) {
            es_1 var10;
            es_1 var10000 = var10 = this.g10;
            es_1 var10001 = var9;
            var10.getClass();
            Object[] var37 = var9.rZ;
            int var42 = var10001.KB;
            var10000.G6(var37, 0, var42);
         }
      } else {
         es_1 var38;
         es_1 var45 = var38 = this.g10;
         com3__3[] var43;
         com3__3[] var54 = var43 = this.lu;
         var38.getClass();
         int var39 = var54.length;
         var45.G6(var43, 0, var39);
      }

      label87: {
         int var46 = var2;
         var2 = var3 - 1;
         float var29 = var5 + var4;
         switch (var46) {
            case 0:
               var8.Xf0();
               I2 var15 = this.g10.ZD();

               while(true) {
                  if (!var15.hasNext()) {
                     break label87;
                  }

                  com3__3 var22;
                  C8 var61 = (var22 = (com3__3)var15.next()).oW;
                  float var28 = var61.x * 100.0F * var6;
                  var4 = var61.y * 100.0F * var7;
                  pw_1 var53 = var8.TD0().Xf0().y80(ao_1.DX(var22, 7, var29).UD(var28, var4));
                  ao_1 var62 = ao_1.DX(var22, 2, var29);
                  float var23 = var22.j.y - var4;
                  var62.h5[0] = var23;
                  var53.y80(var62).mz0().mz0();
               }
            case 1:
               var8.Xf0();
               I2 var14 = this.g10.ZD();

               while(var14.hasNext()) {
                  com3__3 var20 = (com3__3)var14.next();
                  var4 = (1.0F - var7) / 2.0F;
                  pw_1 var52 = var8.TD0().Xf0().y80(ao_1.DX(var20, 7, var29).UD(var6, var7));
                  ao_1 var60 = ao_1.DX(var20, 2, var29);
                  float var21 = var20.j.y - var4;
                  var60.h5[0] = var21;
                  var52.y80(var60).mz0().mz0();
               }

               var8.mz0();
               if (var2 <= 1) {
                  return var8;
               }
               break;
            case 2:
               var8.Xf0();
               I2 var13 = this.g10.ZD();

               while(var13.hasNext()) {
                  com3__3 var18 = (com3__3)var13.next();
                  var4 = var6 + 1.0F;
                  var5 = var7 + 1.0F;
                  float var41 = (1.0F - var5) / 2.0F;
                  pw_1 var50 = var8.TD0().Xf0().y80(ao_1.DX(var18, 7, var29).UD(var4, var5));
                  ao_1 var58 = ao_1.DX(var18, 2, var29);
                  var4 = var18.j.y - var41;
                  var58.h5[0] = var4;
                  var50 = var50.y80(var58).mz0().Xf0().y80(ao_1.DX(var18, 7, var29).UD(1.0F, 1.0F));
                  var58 = ao_1.DX(var18, 2, var29);
                  float var19 = var18.j.y;
                  var58.h5[0] = var19;
                  var50.y80(var58).mz0().mz0();
               }

               var8.mz0();
               if (var2 <= 1) {
                  return var8;
               }
               break;
            case 3:
               var8.Xf0();
               I2 var12 = this.g10.ZD();

               while(true) {
                  if (!var12.hasNext()) {
                     break label87;
                  }

                  com3__3 var16 = (com3__3)var12.next();
                  float var25 = var6 + 1.0F;
                  var4 = var7 + 1.0F;
                  var5 = (1.0F - var4) / 2.0F;
                  float var40 = 1.0F - var6;
                  float var44 = 1.0F - var7;
                  float var11 = (1.0F - var44) / 2.0F;
                  pw_1 var47 = var8.TD0().Xf0().y80(ao_1.DX(var16, 7, var29).UD(var25, var4));
                  ao_1 var55 = ao_1.DX(var16, 2, var29);
                  var25 = var16.j.y - var5;
                  var55.h5[0] = var25;
                  var47 = var47.y80(var55).mz0().Xf0().y80(ao_1.DX(var16, 7, var29).UD(var40, var44));
                  var55 = ao_1.DX(var16, 2, var29);
                  var25 = var16.j.y - var11;
                  var55.h5[0] = var25;
                  var47 = var47.y80(var55).mz0().Xf0().y80(ao_1.DX(var16, 7, var29).UD(1.0F, 1.0F));
                  var55 = ao_1.DX(var16, 2, var29);
                  float var17 = var16.j.y;
                  var55.h5[0] = var17;
                  var47.y80(var55).mz0().mz0();
               }
            default:
               if (!this.Am) {
                  UF.error("Unknown type_id for getScaleAnimation = {}", var1);
               }

               return var8;
         }

         ((D2)var8).Yu0(var2, 0.0F);
         return var8;
      }

      var8.mz0();
      return var8;
   }

   public final pw_1 WW(int var1, float var2, float var3, float var4, Color var5) {
      this.g10.clear();
      pw_1 var6 = pw_1.xC();
      if (var1 != 14) {
         if (var1 != 16) {
            if (!this.Am) {
               UF.error("Unknown target_id for getMoveAnimation = {}", var1);
            }

            return var6;
         }

         es_1 var9;
         if ((var9 = this.aZ) != null && var9.KB > 0) {
            es_1 var7;
            es_1 var10000 = var7 = this.g10;
            es_1 var10001 = var9;
            var7.getClass();
            Object[] var10 = var9.rZ;
            int var17 = var10001.KB;
            var10000.G6(var10, 0, var17);
         }
      } else {
         es_1 var11;
         es_1 var20 = var11 = this.g10;
         com3__3[] var18;
         com3__3[] var21 = var18 = this.lu;
         var11.getClass();
         int var12 = var21.length;
         var20.G6(var18, 0, var12);
      }

      T8.set(var5);
      var6.Xf0();
      I2 var13 = this.g10.ZD();

      while(var13.hasNext()) {
         ao_1 var22 = ao_1.yp(10, (com3__3)var13.next());
         float[] var16;
         float[] var10002 = var16 = new float[4];
         Color var19;
         Color var10005 = var19 = T8;
         var16[0] = var19.r;
         var16[1] = var19.g;
         var10002[2] = var10005.b;
         var10002[3] = var3;
         var6.y80(var22.Om0(var10002));
      }

      var6.mz0();
      var6.Xf0();
      I2 var8 = this.g10.ZD();

      while(var8.hasNext()) {
         ao_1 var23 = ao_1.DX((com3__3)var8.next(), 10, var2);
         float[] var14;
         float[] var24 = var14 = new float[4];
         Color var15;
         Color var25 = var15 = T8;
         var14[0] = var15.r;
         var14[1] = var15.g;
         var24[2] = var25.b;
         var24[3] = var4;
         var6.y80(var23.Om0(var24));
      }

      var6.mz0();
      return var6;
   }

   public final pw_1 df0(int var1, int var2) {
      this.g10.clear();
      pw_1 var3 = pw_1.xC();
      if (var1 != 14) {
         if (var1 != 16) {
            if (!this.Am) {
               UF.error("Unknown target_id for getMoveAnimation = {}", var1);
            }

            return var3;
         }

         es_1 var7;
         if ((var7 = this.aZ) != null && var7.KB > 0) {
            es_1 var4;
            es_1 var10000 = var4 = this.g10;
            es_1 var10001 = var7;
            var4.getClass();
            Object[] var8 = var7.rZ;
            int var14 = var10001.KB;
            var10000.G6(var8, 0, var14);
         }
      } else {
         es_1 var9;
         es_1 var16 = var9 = this.g10;
         com3__3[] var15;
         com3__3[] var17 = var15 = this.lu;
         var9.getClass();
         int var10 = var17.length;
         var16.G6(var15, 0, var10);
      }

      var3.Xf0();
      if (var2 != 3) {
         if (var2 == 4) {
            I2 var5 = this.g10.ZD();

            while(var5.hasNext()) {
               com3__3 var11;
               (var11 = (com3__3)var5.next()).getClass();
               var3.y80(ao_1.yp(8, var11).Om0(new float[]{1.0F, 1.0F, 1.0F, 1.0F}));
            }
         }
      } else {
         I2 var6 = this.g10.ZD();

         while(var6.hasNext()) {
            com3__3 var12;
            (var12 = (com3__3)var6.next()).getClass();
            ao_1 var18 = ao_1.yp(9, var12);
            float var13 = 0.0F;
            var18.h5[0] = var13;
            var3.y80(var18);
         }
      }

      var3.mz0();
      return var3;
   }
}
