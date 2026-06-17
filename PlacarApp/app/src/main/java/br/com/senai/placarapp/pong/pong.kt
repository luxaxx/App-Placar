package br.com.senai.placarapp.pong
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ─────────────────────────────────────────────
//  CORES CARTOON
// ─────────────────────────────────────────────
private val BgTop        = Color(0xFF1B0045)
private val BgBottom     = Color(0xFF0A0025)
private val PaddleColor  = Color(0xFFFFD93D)
private val PaddleShadow = Color(0xFFFF6B35)
private val BallColor    = Color(0xFFFF6B9D)
private val BallGlow     = Color(0xFFFF1493)
private val WallColor    = Color(0xFF6C63FF)
private val WallGlow     = Color(0xFF9D4EDD)
private val TextLight    = Color(0xFFF8F8FF)
private val TextMuted    = Color(0xFFAA99CC)
private val StarColor    = Color(0xFFFFE66D)

private val BallTrailColors = listOf(
    Color(0xFFFF6B9D), Color(0xFFFF9A3C), Color(0xFFFFD93D),
    Color(0xFF6BFF9A), Color(0xFF6BB5FF)
)

// ─────────────────────────────────────────────
//  ESTADO DO JOGO
// ─────────────────────────────────────────────
enum class PongGameStatus { WAITING, PLAYING, GAME_OVER }

data class PongState(
    val ballX: Float = 0.5f,        // 0..1 normalizado
    val ballY: Float = 0.4f,
    val ballDX: Float = 0.012f,
    val ballDY: Float = 0.015f,
    val paddleX: Float = 0.5f,      // centro do paddle
    val score: Int = 0,
    val lives: Int = 3,
    val status: PongGameStatus = PongGameStatus.WAITING,
    val bounces: Int = 0,
    val trail: List<Offset> = emptyList()
)

private const val PADDLE_W   = 0.22f   // largura do paddle (normalizado)
private const val PADDLE_H   = 0.025f
private const val PADDLE_Y   = 0.88f   // posição vertical do paddle
private const val BALL_R     = 0.025f  // raio da bola
private const val SPEED_INC  = 0.0008f // aceleração por rebatida
private const val MAX_SPEED  = 0.032f

fun pongUpdate(state: PongState): PongState {
    if (state.status != PongGameStatus.PLAYING) return state

    var bx = state.ballX + state.ballDX
    var by = state.ballY + state.ballDY
    var dx = state.ballDX
    var dy = state.ballDY
    var lives = state.lives
    var score = state.score
    var bounces = state.bounces
    var status = state.status

    // Paredes laterais
    if (bx - BALL_R <= 0f) { bx = BALL_R; dx = abs(dx) }
    if (bx + BALL_R >= 1f) { bx = 1f - BALL_R; dx = -abs(dx) }

    // Teto
    if (by - BALL_R <= 0.05f) { by = 0.05f + BALL_R; dy = abs(dy) }

    // Paddle
    val paddleLeft  = state.paddleX - PADDLE_W / 2f
    val paddleRight = state.paddleX + PADDLE_W / 2f
    val paddleTop   = PADDLE_Y - PADDLE_H / 2f

    if (dy > 0 && by + BALL_R >= paddleTop && by - BALL_R <= PADDLE_Y + PADDLE_H / 2f
        && bx >= paddleLeft && bx <= paddleRight
    ) {
        by = paddleTop - BALL_R
        dy = -abs(dy)
        // Ângulo baseado em onde bateu no paddle
        val hitPos = (bx - paddleLeft) / PADDLE_W - 0.5f  // -0.5..0.5
        dx = hitPos * 0.03f + dx * 0.3f
        // Acelera
        val speed = minOf(
            kotlin.math.sqrt(dx * dx + dy * dy) + SPEED_INC,
            MAX_SPEED
        )
        val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble())
        dx = (speed * cos(angle)).toFloat()
        dy = (speed * sin(angle)).toFloat()
        if (dy > 0) dy = -dy
        score += 10 + (bounces / 5) * 5
        bounces++
    }

    // Perdeu
    if (by - BALL_R > 1f) {
        lives--
        status = if (lives <= 0) PongGameStatus.GAME_OVER else PongGameStatus.PLAYING
        bx = 0.5f; by = 0.4f
        dx = if (Random.nextBoolean()) 0.012f else -0.012f
        dy = 0.015f
        bounces = 0
    }

    // Trail (últimas 8 posições)
    val newTrail = (state.trail + Offset(bx, by)).takeLast(8)

    return state.copy(
        ballX = bx, ballY = by,
        ballDX = dx, ballDY = dy,
        score = score, lives = lives,
        status = status, bounces = bounces,
        trail = newTrail
    )
}

// ─────────────────────────────────────────────
//  TELA PRINCIPAL  ← use essa função no MainActivity
// ─────────────────────────────────────────────
@Composable
fun PongScreen() {
    var state by remember { mutableStateOf(PongState()) }
    var canvasW by remember { mutableFloatStateOf(1f) }
    var canvasH by remember { mutableFloatStateOf(1f) }

    // Game loop
    LaunchedEffect(state.status) {
        while (state.status == PongGameStatus.PLAYING) {
            delay(16L)
            state = pongUpdate(state)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {
        // HUD topo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vidas
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { i ->
                    Text(
                        if (i < state.lives) "❤️" else "🖤",
                        fontSize = 20.sp
                    )
                }
            }
            // Pontuação
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${state.score}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TextLight
                )
                Text("pontos", fontSize = 11.sp, color = TextMuted)
            }
            // Nível de velocidade
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val speedLevel = (state.bounces / 5) + 1
                Text("Lv $speedLevel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarColor)
                Text("nível", fontSize = 11.sp, color = TextMuted)
            }
        }

        // Canvas do jogo
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            if (state.status == PongGameStatus.WAITING ||
                                state.status == PongGameStatus.GAME_OVER
                            ) {
                                state = PongState(status = PongGameStatus.PLAYING)
                            }
                        }
                    ) { change, _ ->
                        val nx = change.position.x / canvasW
                        state = state.copy(
                            paddleX = nx.coerceIn(PADDLE_W / 2f, 1f - PADDLE_W / 2f)
                        )
                    }
                }
        ) {
            canvasW = size.width
            canvasH = size.height

            fun fx(v: Float) = v * size.width
            fun fy(v: Float) = v * size.height

            // Linha do teto decorativa
            drawLine(
                brush = Brush.horizontalGradient(listOf(WallGlow, WallColor, WallGlow)),
                start = Offset(0f, fy(0.05f)),
                end = Offset(size.width, fy(0.05f)),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Linha central tracejada
            val dashCount = 20
            for (i in 0 until dashCount) {
                val y = fy(0.05f) + (i * (fy(0.83f) / dashCount))
                if (i % 2 == 0) {
                    drawLine(
                        color = WallColor.copy(alpha = 0.2f),
                        start = Offset(size.width / 2f, y),
                        end = Offset(size.width / 2f, y + fy(0.83f) / dashCount - 4f),
                        strokeWidth = 2f
                    )
                }
            }

            // Trail da bola
            state.trail.forEachIndexed { i, pos ->
                val alpha = (i + 1f) / state.trail.size * 0.5f
                val r = BALL_R * size.width * (0.3f + 0.7f * (i + 1f) / state.trail.size)
                val col = BallTrailColors[i % BallTrailColors.size].copy(alpha = alpha)
                drawCircle(color = col, radius = r, center = Offset(fx(pos.x), fy(pos.y)))
            }

            // Glow da bola
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(BallGlow.copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(fx(state.ballX), fy(state.ballY)),
                    radius = BALL_R * size.width * 2.5f
                ),
                radius = BALL_R * size.width * 2.5f,
                center = Offset(fx(state.ballX), fy(state.ballY))
            )

            // Bola
            drawCircle(
                color = BallColor,
                radius = BALL_R * size.width,
                center = Offset(fx(state.ballX), fy(state.ballY))
            )
            // Reflexo
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = BALL_R * size.width * 0.35f,
                center = Offset(
                    fx(state.ballX) - BALL_R * size.width * 0.2f,
                    fy(state.ballY) - BALL_R * size.width * 0.2f
                )
            )

            // Paddle – sombra
            drawRoundRect(
                color = PaddleShadow.copy(alpha = 0.5f),
                topLeft = Offset(
                    fx(state.paddleX - PADDLE_W / 2f) + 4f,
                    fy(PADDLE_Y - PADDLE_H / 2f) + 6f
                ),
                size = Size(fx(PADDLE_W), fy(PADDLE_H)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f)
            )

            // Paddle – corpo
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(PaddleShadow, PaddleColor, PaddleShadow),
                    startX = fx(state.paddleX - PADDLE_W / 2f),
                    endX = fx(state.paddleX + PADDLE_W / 2f)
                ),
                topLeft = Offset(
                    fx(state.paddleX - PADDLE_W / 2f),
                    fy(PADDLE_Y - PADDLE_H / 2f)
                ),
                size = Size(fx(PADDLE_W), fy(PADDLE_H)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f)
            )

            // Reflexo no paddle
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(
                    fx(state.paddleX - PADDLE_W / 2f) + fx(PADDLE_W) * 0.1f,
                    fy(PADDLE_Y - PADDLE_H / 2f) + 2f
                ),
                size = Size(fx(PADDLE_W) * 0.8f, fy(PADDLE_H) * 0.35f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f)
            )
        }

        // Overlay: WAITING
        if (state.status == PongGameStatus.WAITING) {
            OverlayCard {
                Text("🏓", fontSize = 56.sp)
                Spacer(Modifier.height(8.dp))
                Text("PONG", fontSize = 40.sp, fontWeight = FontWeight.Black, color = TextLight, letterSpacing = 6.sp)
                Spacer(Modifier.height(8.dp))
                Text("Arraste para mover o paddle", fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("Arraste para começar!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PaddleColor)
            }
        }

        // Overlay: GAME OVER
        if (state.status == PongGameStatus.GAME_OVER) {
            OverlayCard {
                Text("💥", fontSize = 56.sp)
                Spacer(Modifier.height(8.dp))
                Text("GAME OVER", fontSize = 28.sp, fontWeight = FontWeight.Black, color = BallColor, letterSpacing = 4.sp)
                Spacer(Modifier.height(12.dp))
                Text("Pontuação final", fontSize = 13.sp, color = TextMuted)
                Text("${state.score}", fontSize = 48.sp, fontWeight = FontWeight.Black, color = StarColor)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { state = PongState(status = PongGameStatus.PLAYING) },
                    colors = ButtonDefaults.buttonColors(containerColor = WallColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
                ) {
                    Text("Jogar Novamente", fontWeight = FontWeight.Bold, color = TextLight)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  OVERLAY GENÉRICO
// ─────────────────────────────────────────────
@Composable
private fun OverlayCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF2A1060), Color(0xFF0D0030))),
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 40.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF1B0045, widthDp = 390, heightDp = 844)
@Composable
fun PongPreview() {
    PongScreen()
}