package br.com.senai.placarapp.aplicativoplacar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacarApp() {
    // ESTADO DE NAVEGAÇÃO
    var telaAtual by remember { mutableStateOf("inicio") }

    // ESTRUTURA PRINCIPAL
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MARCADOR DE PONTOS") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(Color.LightGray)
        ) {
            when (telaAtual) {
                "inicio" -> TelaInicio(onIniciar = { telaAtual = "jogo" })
                "jogo" -> TelaJogo(onVoltar = { telaAtual = "inicio" })
            }
        }
    }
}

@Composable
fun TelaInicio(onIniciar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏆", fontSize = 80.sp)
        Text("Bem-vindo ao Placar!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onIniciar) {
            Text("COMEÇAR PARTIDA")
        }
    }
}

@Composable
fun TelaJogo(onVoltar: () -> Unit) {
    // ESTADOS DOS PONTOS
    var pontosA by remember { mutableIntStateOf(0) }
    var pontosB by remember { mutableIntStateOf(0) }
    var pontosC by remember { mutableIntStateOf(0) }
    val pontuacaoMaxima = 12

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // COLUNA TIME A
            ColunaTime(
                nome = "TIME A",
                pontos = pontosA,
                venceu = pontosA >= pontuacaoMaxima,
                onSomar = { pontosA++ },
                onSomar3 = { pontosA += 3 },
                onSubtrair = { if (pontosA > 0) pontosA-- }
            )

            // Divisor visual simples
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.LightGray))

            // COLUNA TIME B
            ColunaTime(
                nome = "TIME B",
                pontos = pontosB,
                venceu = pontosB >= pontuacaoMaxima,
                onSomar = { pontosB++ },
                onSomar3 = { pontosB += 3 },
                onSubtrair = { if (pontosB > 0) pontosB-- }
            )
            // Divisor visual simples
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.LightGray))

            // COLUNA TIME C
            ColunaTime(
                nome = "TIME C",
                pontos = pontosC,
                venceu = pontosC >= pontuacaoMaxima,
                onSomar = { pontosC++ },
                onSomar3 = { pontosC += 3 },
                onSubtrair = { if (pontosC > 0) pontosC-- }
            )



        }
        // ÁREA DE CONTROLES INFERIORES
        Row(modifier = Modifier.padding(bottom = 32.dp)) {
            Button(
                onClick = { pontosA = 0; pontosB = 0; pontosC = 0;},
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("RESETAR")
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(onClick = onVoltar) {
                Text("SAIR")
            }


    }
        // Divisor visual simples
//        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.LightGray))





        }
    }


@Composable
fun ColunaTime(
    nome: String,
    pontos: Int,
    venceu: Boolean,
    onSomar: () -> Unit,
    onSomar3: () -> Unit,
    onSubtrair: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = nome, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Text(
            text = "$pontos",
            fontSize = 70.sp,
            fontWeight = FontWeight.Black,
            color = if (venceu) Color(0xFF4CAF50) else Color.Black // Verde se vencer
        )

        Button(onClick = onSomar, modifier = Modifier.width(100.dp)) { Text("+1") }
        Button(onClick = onSomar3, modifier = Modifier.width(100.dp)) { Text("+3") }
        OutlinedButton(onClick = onSubtrair, modifier = Modifier.width(100.dp)) { Text("-1") }

        if (venceu) {
            Text("VENCEDOR! 🎉", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlacarAppPreview() {
    MaterialTheme() {
       PlacarApp()
//        TelaJogo(onVoltar = {})
    }
}

// paara a coluna time precisamos criar uma previes especifica
//@Preview(showBackground = true)
//@Composable
//fun ColunaTimePreview() {
//    MaterialTheme {
//        ColunaTime(
//            nome = "TIME A",
//            pontos = 5,
//            venceu = false,
//            onSomar = {},
//            onSomar3 = {},
//            onSubtrair = {}
//        )
//    }
//}