#ifdef GL_ES
#define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float glowIntensity;
uniform float glowThreshold;
uniform float glowSize;
uniform vec3 glowColor;
uniform vec2 glowInvTexSize;
uniform float grayScale;

// how was this calculated? 9-tap kernel, sigma ~1.5 table:
// Offset   -4      -3      -2      -1      0       +1      +2      +3      +4
// Weight   0.0162  0.0540  0.1218  0.1872  0.2416  0.1872  0.1218  0.0540  0.0162

// lets take pair (+3, +4) for example
// weight = wA + wB
// weight = 0.0540 + 0.0162 = 0.0702
// offset = (posA * wA + posB * wB) / (wA + wB)
// offset = (+3 * 0.0540 + +4 * 0.0162) / 0.0702 = 3.2308

const float weightOuter  = 0.0702; // pair(-4,-3) and pair(+3,+4)
const float weightMiddle = 0.3090; // pair(-2,-1) and pair(+1,+2)
const float weightCenter = 0.2416; // center

// our offsets, since blur is symmetrical so only two are needed - should be negated for top/left
const float offsetOuter  = 3.2308;
const float offsetMiddle = 1.3883;

// 3-tap vertical weights, sigma ~0.85
const float weight3Outer  = 0.25;
const float weight3Middle = 0.50;

// single horizontal 5-tap row
float blurRow(sampler2D tex, vec2 rowUV, vec2 sX)
{
    return weightOuter  * texture2D(tex, rowUV - sX * offsetOuter).a
         + weightMiddle * texture2D(tex, rowUV - sX * offsetMiddle).a
         + weightCenter * texture2D(tex, rowUV).a
         + weightMiddle * texture2D(tex, rowUV + sX * offsetMiddle).a
         + weightOuter  * texture2D(tex, rowUV + sX * offsetOuter).a;
}

#ifdef glowHQ
// HQ: 5x5 tap bilinear = 25 fetches
float blurGlow(sampler2D tex, vec2 uv)
{
    vec2 sX = vec2(glowInvTexSize.x * glowSize, 0.0);
    vec2 sY = vec2(0.0, glowInvTexSize.y * glowSize);

    float sum = 0.0;

    sum += weightOuter  * blurRow(tex, uv - sY * offsetOuter, sX);
    sum += weightMiddle * blurRow(tex, uv - sY * offsetMiddle, sX);
    sum += weightCenter * blurRow(tex, uv, sX);
    sum += weightMiddle * blurRow(tex, uv + sY * offsetMiddle, sX);
    sum += weightOuter  * blurRow(tex, uv + sY * offsetOuter, sX);

    return sum;
}

#else
// non-HQ blur ( 3x5 ) = 15 fetches
float blurGlow(sampler2D tex, vec2 uv)
{
    vec2 sX = vec2(glowInvTexSize.x * glowSize, 0.0);
    float stepY = glowInvTexSize.y * glowSize;

    float sum = 0.0;
    vec2 rowUV;

    rowUV = uv + vec2(0.0, -stepY);
    sum += weight3Outer * blurRow(tex, rowUV, sX);
    sum += weight3Middle * blurRow(tex, uv, sX);
    rowUV = uv + vec2(0.0, stepY);
    sum += weight3Outer * blurRow(tex, rowUV, sX);

    return sum;
}
#endif

void main()
{
    vec4 diffuse = texture2D(u_texture, v_texCoords);

    if (glowSize > 0.0 && diffuse.a <= glowThreshold)
    {
        float v = blurGlow(u_texture, v_texCoords) * glowIntensity;
        diffuse = vec4(glowColor, v);
    }

    vec4 base = v_color * diffuse;

    // Convert with ITU-R BT.709
    float gray = dot(base.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 blendedColor = mix(base.rgb, vec3(gray), grayScale);

    gl_FragColor = vec4(blendedColor, base.a);
}
