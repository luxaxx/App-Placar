package br.com.senai.placarapp.jogodavelha

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
//  CORES
// ─────────────────────────────────────────────
private val BgDark      = Color(0xFF0D0D1A)
private val BgCard      = Color(0xFF1A1A2E)
private val AccentX     = Color(0xFF00D4FF)   // ciano – jogador X
private val AccentO     = Color(0xFFFF6B6B)   // coral – jogador O
private val Neutral     = Color(0xFF2E2E4A)
private val TextLight   = Color(0xFFF0F0FF)
private val TextMuted   = Color(0xFF8888AA)

// ─────────────────────────────────────────────
//  MODELOS
// ─────────────────────────────────────────────
enum class Player { X, O }

data class GameState(
    val board: List<Player?> = List(9) { null },
    val current: Player = Player.X,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val scoreX: Int = 0,
    val scoreO: Int = 0,
    val winningLine: List<Int> = emptyList()
)

private val WIN_LINES = listOf(
    listOf(0,1,2), listOf(3,4,5), listOf(6,7,8),
    listOf(0,3,6), listOf(1,4,7), listOf(2,5,8),
    listOf(0,4,8), listOf(2,4,6)
)

fun checkWinner(board: List<Player?>): Pair<Player?, List<Int>> {
    for (line in WIN_LINES) {
        val (a, b, c) = line
        if (board[a] != null && board[a] == board[b] && board[b] == board[c])
            return board[a] to line
    }
    return null to emptyList()
}

fun makeMove(state: GameState, index: Int): GameState {
    if (state.board[index] != null || state.winner != null || state.isDraw) return state
    val newBoard = state.board.toMutableList().apply { set(index, state.current) }
    val (winner, line) = checkWinner(newBoard)
    val isDraw = winner == null && newBoard.none { it == null }
    return state.copy(
        board = newBoard,
        current = if (state.current == Player.X) Player.O else Player.X,
        winner = winner,
        isDraw = isDraw,
        winningLine = line,
        scoreX = state.scoreX + if (winner == Player.X) 1 else 0,
        scoreO = state.scoreO + if (winner == Player.O) 1 else 0
    )
}

// ─────────────────────────────────────────────
//  TELA PRINCIPAL  ← use essa função no MainActivity
// ─────────────────────────────────────────────
@Composable
fun JogoDaVelhaScreen() {
    var state by remember { mutableStateOf(GameState()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgDark, Color(0xFF16213E)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // Título
            Text(
                "JOGO DA VELHA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = TextLight
            )

            // Placar
            ScoreBoard(state)

            // Status
            StatusBanner(state)

            // Tabuleiro
            Board(state) { idx -> state = makeMove(state, idx) }

            // Botão reiniciar rodada
            Button(
                onClick = {
                    state = state.copy(
                        board = List(9) { null },
                        current = Player.X,
                        winner = null,
                        isDraw = false,
                        winningLine = emptyList()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Neutral),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
            ) {
                Text("Nova Rodada", color = TextLight, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            // Botão zerar placar
            TextButton(onClick = { state = GameState() }) {
                Text("Zerar Placar", color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────
//  PLACAR
// ─────────────────────────────────────────────
@Composable
private fun ScoreBoard(state: GameState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreItem("X", state.scoreX, AccentX)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VS", fontSize = 14.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
        ScoreItem("O", state.scoreO, AccentO)
    }
}

@Composable
private fun ScoreItem(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color, letterSpacing = 2.sp)
        Text("$score", fontSize = 36.sp, fontWeight = FontWeight.Black, color = color)
        Text("pontos", fontSize = 11.sp, color = TextMuted)
    }
}

// ─────────────────────────────────────────────
//  STATUS
// ─────────────────────────────────────────────
@Composable
private fun StatusBanner(state: GameState) {
    val (text, color) = when {
        state.winner == Player.X -> "🎉  Jogador X venceu!" to AccentX
        state.winner == Player.O -> "🎉  Jogador O venceu!" to AccentO
        state.isDraw             -> "🤝  Empate!"          to TextMuted
        state.current == Player.X -> "Vez do Jogador X"    to AccentX
        else                      -> "Vez do Jogador O"    to AccentO
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = color, textAlign = TextAlign.Center)
    }
}

// ─────────────────────────────────────────────
//  TABULEIRO
// ─────────────────────────────────────────────
@Composable
private fun Board(state: GameState, onCellClick: (Int) -> Unit) {
    val size = 300.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in 0..2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        Cell(
                            player = state.board[idx],
                            isWinning = idx in state.winningLine,
                            isGameOver = state.winner != null || state.isDraw,
                            onClick = { onCellClick(idx) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  CÉLULA
// ─────────────────────────────────────────────
@Composable
private fun Cell(
    player: Player?,
    isWinning: Boolean,
    isGameOver: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetColor = when {
        isWinning && player == Player.X -> AccentX.copy(alpha = 0.25f)
        isWinning && player == Player.O -> AccentO.copy(alpha = 0.25f)
        else -> Neutral
    }
    val bgColor by animateColorAsState(targetColor, tween(300), label = "cellBg")
    val scale by animateFloatAsState(if (isWinning) 1.06f else 1f, tween(200), label = "cellScale")

    val borderColor = when (player) {
        Player.X -> AccentX.copy(alpha = if (isWinning) 1f else 0.5f)
        Player.O -> AccentO.copy(alpha = if (isWinning) 1f else 0.5f)
        null     -> Neutral
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = player == null && !isGameOver) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (player) {
            Player.X -> Text(
                "X",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = AccentX
            )
            Player.O -> Text(
                "O",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = AccentO
            )
            null -> {}
        }
    }
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF0D0D1A, widthDp = 390, heightDp = 844)
@Composable
fun JogoDaVelhaPreview() {
    JogoDaVelhaScreen()
}