package me.joxquin.notivas.ui.components.aura

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.blur.RuntimeShader
import top.yukonga.miuix.kmp.blur.asBrush
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

private const val OS3_BG_FRAG = """
    uniform vec2 uResolution;
    uniform float uAnimTime;
    uniform vec4 uBound;
    uniform float uTranslateY;
    uniform vec3 uPoints[4];
    uniform vec2 uPointsAnim[4];
    uniform vec4 uColors[4];
    uniform float uAlphaMulti;
    uniform float uNoiseScale;
    uniform float uPointRadiusMulti;
    uniform float uSaturateOffset;
    uniform float uLightOffset;

    vec3 rgb2hsv(vec3 c) {
        vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
        vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
        vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
        float d = q.x - min(q.w, q.y);
        float e = 1.0e-10;
        return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
    }

    vec3 hsv2rgb(vec3 c) {
        vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
        vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
        return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
    }

    float hash(vec2 p) {
        vec3 p3 = fract(vec3(p.xyx) * 0.13);
        p3 += dot(p3, p3.yzx + 3.333);
        return fract((p3.x + p3.y) * p3.z);
    }

    float perlin(vec2 x) {
        vec2 i = floor(x); vec2 f = fract(x);
        float a = hash(i); float b = hash(i + vec2(1.0, 0.0));
        float c = hash(i + vec2(0.0, 1.0)); float d = hash(i + vec2(1.0, 1.0));
        vec2 u = f * f * (3.0 - 2.0 * f);
        return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
    }

    float gradientNoise(in vec2 uv) {
        return fract(52.9829189 * fract(dot(uv, vec2(0.06711056, 0.00583715))));
    }

    vec4 main(vec2 fragCoord){
        vec2 vUv = fragCoord/uResolution;
        vUv.y = 1.0-vUv.y;
        vec2 uv = vUv;
        uv -= vec2(0., uTranslateY);
        uv.xy -= uBound.xy;
        uv.xy /= uBound.zw;

        vec4 color = vec4(0.0);
        float noiseValue = perlin(vUv * uNoiseScale + vec2(-uAnimTime, -uAnimTime));

        for (int i = 0; i < 4; i++){
            vec4 pointColor = uColors[i];
            pointColor.rgb *= pointColor.a;
            vec2 point = uPointsAnim[i];
            float rad = uPoints[i].z * uPointRadiusMulti;

            float d = distance(uv, point);
            float pct = smoothstep(rad, 0., d);
            color.rgb = mix(color.rgb, pointColor.rgb, pct);
            color.a = mix(color.a, pointColor.a, pct);
        }

        float oppositeNoise = smoothstep(0., 1., noiseValue);
        color.rgb /= color.a;
        vec3 hsv = rgb2hsv(color.rgb);
        hsv.y = mix(hsv.y, 0.0, oppositeNoise * uSaturateOffset);
        color.rgb = hsv2rgb(hsv);
        color.rgb += oppositeNoise * uLightOffset;

        color.a = clamp(color.a, 0., 1.);
        color.a *= uAlphaMulti;

        color += (10.0 / 255.0) * gradientNoise(fragCoord.xy) - (5.0 / 255.0);
        return vec4(color.rgb * color.a, color.a);
    }
"""

internal class MonetAuraConfig(
    val points: FloatArray,
    val colors1: FloatArray,
    val colors2: FloatArray,
    val colors3: FloatArray,
    val colorInterpPeriod: Float = 6.0f,
    val lightOffset: Float = 0.05f,
    val saturateOffset: Float = 0.15f,
    val pointOffset: Float = 0.25f
)

private fun packColors(c1: Color, c2: Color, c3: Color, c4: Color, alpha: Float): FloatArray {
    return floatArrayOf(
        c1.red, c1.green, c1.blue, alpha,
        c2.red, c2.green, c2.blue, alpha,
        c3.red, c3.green, c3.blue, alpha,
        c4.red, c4.green, c4.blue, alpha
    )
}

internal class MonetAuraPainter {
    val runtimeShader by lazy {
        RuntimeShader(OS3_BG_FRAG).also {
            it.setFloatUniform("uTranslateY", 0f)
            it.setFloatUniform("uNoiseScale", 1.5f)
            it.setFloatUniform("uPointRadiusMulti", 1.1f)
            it.setFloatUniform("uAlphaMulti", 1f)
        }
    }

    val brush: Brush get() = runtimeShader.asBrush()

    private val resolution = FloatArray(2)
    private val bound = FloatArray(4)
    private val colorsBuffer = FloatArray(16)
    private val pointsAnimBuffer = FloatArray(8)
    private var animTime = Float.NaN

    fun updateResolution(width: Float, height: Float) {
        if (resolution[0] == width && resolution[1] == height) return
        resolution[0] = width
        resolution[1] = height
        runtimeShader.setFloatUniform("uResolution", resolution)
    }

    fun updateAnimTime(time: Float) {
        if (animTime == time) return
        animTime = time
        runtimeShader.setFloatUniform("uAnimTime", animTime)
    }

    fun updateBound(drawHeight: Float, totalHeight: Float, totalWidth: Float) {
        bound[0] = 0f
        bound[1] = (totalHeight - drawHeight) / totalHeight
        bound[2] = 1f
        bound[3] = drawHeight / totalHeight
        runtimeShader.setFloatUniform("uBound", bound)
    }

    fun updatePreset(preset: MonetAuraConfig) {
        runtimeShader.setFloatUniform("uPoints", preset.points)
        runtimeShader.setFloatUniform("uLightOffset", preset.lightOffset)
        runtimeShader.setFloatUniform("uSaturateOffset", preset.saturateOffset)
    }

    fun updatePointsAnim(time: Float, preset: MonetAuraConfig) {
        val offset = preset.pointOffset
        var i = 0
        while (i < 4) {
            val srcX = preset.points[i * 3]
            val srcY = preset.points[i * 3 + 1]
            val animX = srcX + sin(time + srcY) * offset
            val animY = srcY + cos(time + animX) * offset
            pointsAnimBuffer[i * 2] = animX
            pointsAnimBuffer[i * 2 + 1] = animY
            i++
        }
        runtimeShader.setFloatUniform("uPointsAnim", pointsAnimBuffer)
    }

    fun updateColors(preset: MonetAuraConfig, stage: Float) {
        val base = stage.toInt()
        val fraction = stage - base
        val start = colorsForCycle(preset, base)
        val end = colorsForCycle(preset, base + 1)
        var i = 0
        while (i < 16) {
            colorsBuffer[i] = start[i] + (end[i] - start[i]) * fraction
            i++
        }
        runtimeShader.setFloatUniform("uColors", colorsBuffer)
    }

    private fun colorsForCycle(preset: MonetAuraConfig, index: Int): FloatArray {
        return when (index % 3) {
            0 -> preset.colors1
            1 -> preset.colors2
            else -> preset.colors3
        }
    }
}

private class MonetAuraModifierNode(
    private var painter: MonetAuraPainter,
    private var preset: MonetAuraConfig,
    private var surface: Color,
    private var isTransitioning: Boolean,
    private var alpha: () -> Float,
    private var colorStage: () -> Float
) : Modifier.Node(), DrawModifierNode {

    private var animationJob: Job? = null
    private var animTime: Float = 0f

    override fun onAttach() {
        if (!isTransitioning && alpha() > 0.001f) {
            startAnimation()
        }
    }

    override fun onDetach() {
        animationJob?.cancel()
        animationJob = null
    }

    fun update(
        painter: MonetAuraPainter,
        preset: MonetAuraConfig,
        surface: Color,
        isTransitioning: Boolean,
        alpha: () -> Float,
        colorStage: () -> Float
    ) {
        val transitionChanged = this.isTransitioning != isTransitioning
        this.painter = painter
        this.preset = preset
        this.surface = surface
        this.isTransitioning = isTransitioning
        this.alpha = alpha
        this.colorStage = colorStage

        if (transitionChanged) {
            if (isTransitioning) {
                animationJob?.cancel()
                animationJob = null
            } else if (alpha() > 0.001f) {
                startAnimation()
            }
        }
        invalidateDraw()
    }

    private fun startAnimation() {
        if (isTransitioning || alpha() <= 0.001f) return
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            val minDeltaNanos = 1_000_000_000L / 30L
            val origin = withFrameNanos { it }
            var lastEmit = origin
            while (isActive) {
                val now = withFrameNanos { it }
                if (now - lastEmit < minDeltaNanos) continue
                lastEmit = now
                animTime = (now - origin) / 1_000_000_000f
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawRect(surface)
        val alphaVal = alpha()
        if (alphaVal <= 0.001f) {
            animationJob?.cancel()
            animationJob = null
        } else {
            if (!isTransitioning && animationJob == null) {
                startAnimation()
            }
            painter.updateResolution(size.width, size.height)
            painter.updateBound(size.height, size.height, size.width)
            painter.updatePreset(preset)
            painter.updateColors(preset, colorStage())
            painter.updateAnimTime(animTime)
            painter.updatePointsAnim(animTime, preset)
            drawRect(painter.brush, alpha = alphaVal)
        }
        drawContent()
    }
}

private data class MonetAuraModifierElement(
    val painter: MonetAuraPainter,
    val preset: MonetAuraConfig,
    val surface: Color,
    val isTransitioning: Boolean,
    val alpha: () -> Float,
    val colorStage: () -> Float
) : ModifierNodeElement<MonetAuraModifierNode>() {
    override fun create(): MonetAuraModifierNode =
        MonetAuraModifierNode(painter, preset, surface, isTransitioning, alpha, colorStage)

    override fun update(node: MonetAuraModifierNode) =
        node.update(painter, preset, surface, isTransitioning, alpha, colorStage)
}

@Composable
fun MonetAuraBackground(
    alpha: Float,
    isTransitioning: Boolean = false,
    modifier: Modifier = Modifier,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val shaderSupported = remember { isRuntimeShaderSupported() }
    val colorScheme = MaterialTheme.colorScheme
    val surface = if (surfaceColor != Color.Unspecified) surfaceColor else colorScheme.surface

    if (!shaderSupported) {
        // Fallback dinámico armónico basado en degradados radiales Monet
        val infiniteTransition = rememberInfiniteTransition(label = "monet_fallback_anim")
        val animX by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fallback_x"
        )
        val animY by infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(5500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fallback_y"
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(surface)
                    if (alpha > 0.001f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colorScheme.primary.copy(alpha = 0.35f * alpha),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * animX, size.height * animY),
                                radius = size.minDimension * 0.75f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colorScheme.tertiary.copy(alpha = 0.30f * alpha),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * (1f - animX), size.height * (1f - animY)),
                                radius = size.minDimension * 0.85f
                            )
                        )
                    }
                },
            content = content
        )
        return
    }

    val painter = remember { MonetAuraPainter() }
    val primary = colorScheme.primary
    val secondary = colorScheme.secondary
    val tertiary = colorScheme.tertiary
    val primaryContainer = colorScheme.primaryContainer
    val tertiaryContainer = colorScheme.tertiaryContainer

    val preset = remember(primary, secondary, tertiary, primaryContainer, tertiaryContainer) {
        val targetAlpha = 0.65f
        MonetAuraConfig(
            points = floatArrayOf(
                0.8f, 0.2f, 1.0f,
                0.8f, 0.85f, 1.0f,
                0.2f, 0.85f, 1.0f,
                0.2f, 0.2f, 1.0f
            ),
            colors1 = packColors(primary, tertiary, secondary, primaryContainer, targetAlpha),
            colors2 = packColors(tertiary, primaryContainer, primary, secondary, targetAlpha),
            colors3 = packColors(secondary, tertiaryContainer, tertiary, primary, targetAlpha),
            colorInterpPeriod = 6.0f,
            pointOffset = 0.22f
        )
    }

    val colorStage = remember { Animatable(0f) }

    LaunchedEffect(preset, alpha > 0.001f) {
        if (alpha <= 0.001f) return@LaunchedEffect
        var target = floor(colorStage.value) + 1f
        while (isActive) {
            delay((preset.colorInterpPeriod * 500).toLong())
            colorStage.animateTo(
                targetValue = target,
                animationSpec = spring(dampingRatio = 0.9f, stiffness = 30f)
            )
            target += 1f
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .graphicsLayer {
                    scaleX = 2f
                    scaleY = 2f
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .then(
                    MonetAuraModifierElement(
                        painter = painter,
                        preset = preset,
                        surface = surface,
                        isTransitioning = isTransitioning,
                        alpha = { alpha },
                        colorStage = { colorStage.value }
                    )
                )
        )
        content()
    }
}

