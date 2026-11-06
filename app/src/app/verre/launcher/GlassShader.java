package app.verre.launcher;

import android.annotation.TargetApi;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.RuntimeShader;

/**
 * Réfraction « Liquid Glass » calculée par la carte graphique (Android 13+).
 * Portage du shader de la bibliothèque dashersw/liquid-glass-js (MIT, Copyright (c) 2025 Armagan
 * Amcalar, licence complète dans assets/licenses/liquid-glass-js-MIT.txt) : le fond est dévié le long
 * de la normale de la forme près des bords (bord + liseré + coins + légère ondulation), puis teinté
 * par un dégradé blanc → gris. Le flou de la bibliothèque est inutile ici : le fond est déjà flouté.
 */
@TargetApi(33)
final class GlassShader {
    private static final String SKSL =
            "uniform shader backdrop;\n"
            + "uniform float2 uOrigin;\n"
            + "uniform float2 uSize;\n"
            + "uniform float uRadius;\n"
            + "uniform float uScale;\n"
            + "uniform float2 uPage;\n"
            + "uniform float uVeil;\n"
            + "uniform float uTint;\n"
            + "float sdRound(float2 p, float2 b, float r) {\n"
            + "  float2 q = abs(p) - b + r;\n"
            + "  return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;\n"
            + "}\n"
            + "half4 main(float2 xy) {\n"
            + "  float2 hs = uSize * 0.5;\n"
            + "  float2 p = xy - (uOrigin + hs);\n"
            + "  float d = max(-sdRound(p, hs, uRadius), 0.0) * uScale;\n"
            + "  float2 n = p / max(hs, float2(1.0));\n"
            + "  n = length(n) > 0.0001 ? normalize(n) : float2(0.0, 1.0);\n"
            + "  float minRes = min(uSize.x, uSize.y) * uScale;\n"
            + "  float edge = exp(-d * 0.15) * 0.02;\n"
            + "  float rim = exp(-d * 0.8);\n"
            + "  float2 loc = (xy - uOrigin) / uSize;\n"
            + "  float corner = max(min(loc.x, 1.0 - loc.x), min(loc.y, 1.0 - loc.y)) * minRes;\n"
            + "  float2 refr = n * (edge + rim * 0.09 + exp(-corner * 0.3) * 0.03);\n"
            + "  refr += float2(-n.y, n.x) * sin(d / minRes * 25.0) * 0.12 * rim;\n"
            + "  half4 c = backdrop.eval(xy + refr * uPage);\n"
            + "  half l = dot(c.rgb, half3(0.2126, 0.7152, 0.0722));\n"
            + "  half3 col = clamp(mix(half3(l), c.rgb, half(1.7)), half3(0.0), half3(1.0));\n"
            + "  col *= half(1.0 - uVeil);\n"
            + "  half3 g = mix(half3(1.0), half3(0.7), half(clamp(loc.y, 0.0, 1.0)));\n"
            + "  col = mix(col, g, half(uTint));\n"
            + "  return half4(col, 1.0);\n"
            + "}\n";

    private static RuntimeShader shader;
    private static boolean broken;
    private static final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

    private GlassShader() {}

    /** Dessine une forme de verre. Renvoie faux si le shader n'est pas disponible. */
    static boolean draw(Canvas c, BitmapShader backdrop, RectF r, float rad, float pageW, float pageH,
                        float veil, float tint) {
        if (broken) return false;
        try {
            if (shader == null) shader = new RuntimeShader(SKSL);
            shader.setInputShader("backdrop", backdrop);
            shader.setFloatUniform("uOrigin", r.left, r.top);
            shader.setFloatUniform("uSize", r.width(), r.height());
            shader.setFloatUniform("uRadius", Math.min(rad, Math.min(r.width(), r.height()) / 2f));
            shader.setFloatUniform("uScale", U.s);
            shader.setFloatUniform("uPage", pageW, pageH);
            shader.setFloatUniform("uVeil", veil);
            shader.setFloatUniform("uTint", tint);
            PAINT.setShader(shader);
            c.drawRoundRect(r, rad, rad, PAINT);
            return true;
        } catch (Throwable t) {
            shader = null;
            broken = true; // on ne réessaie pas : l'effet verre simple prend le relais
            return false;
        }
    }
}
