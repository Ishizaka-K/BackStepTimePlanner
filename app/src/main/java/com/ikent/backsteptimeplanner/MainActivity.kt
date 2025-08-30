package com.ikent.backsteptimeplanner

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import com.ikent.backsteptimeplanner.ui.theme.BackStepTimePlannerTheme
import java.time.*
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ★ Edge-to-Edge は setContent の前に
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(0x00000000, 0x00000000),
            navigationBarStyle = SystemBarStyle.auto(0x00000000, 0x00000000)
        )

        setContent {
            BackStepTimePlannerTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("逆算アプリ（最小実装）") },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PlannerScreen()
        }
    }
}

@Composable
fun PlannerScreen() {
    val ctx = LocalContext.current

    // ====== State ======
    var destination by remember { mutableStateOf("") }
    val now = remember { ZonedDateTime.now() }
    var meetDate by remember { mutableStateOf(now.toLocalDate()) }
    var meetTime by remember { mutableStateOf(LocalTime.of(19, 0)) }
    var prepMin by remember { mutableStateOf("30") }
    var bufferMin by remember { mutableStateOf("10") }
    var travelMin by remember { mutableStateOf("45") }

    // ====== 計算 ======
    val meetAt = remember(meetDate, meetTime) {
        ZonedDateTime.of(meetDate, meetTime, ZoneId.systemDefault())
    }
    val prep = prepMin.toLongOrNull() ?: 0L
    val buffer = bufferMin.toLongOrNull() ?: 0L
    val travel = travelMin.toLongOrNull() ?: 0L

    val departAt = meetAt.minusMinutes(travel + buffer)
    val wakeAt = departAt.minusMinutes(prep)

    val fmtDate = DateTimeFormatter.ofPattern("yyyy/MM/dd (E)")
    val fmtTime = DateTimeFormatter.ofPattern("HH:mm")
    val tooLate = ZonedDateTime.now().isAfter(departAt)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        // タイトルはTopAppBarにあるので削除

        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text("目的地（例：天神駅）") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Button(onClick = { meetDate = meetDate.minusDays(1) }) { Text("前日") }
            Text(
                meetDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (E)")),
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { meetDate = meetDate.plusDays(1) }) { Text("翌日") }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Button(onClick = { meetTime = meetTime.minusMinutes(5) }) { Text("-5分") }
            Text(meetTime.format(fmtTime), modifier = Modifier.weight(1f))
            Button(onClick = { meetTime = meetTime.plusMinutes(5) }) { Text("+5分") }
        }

        // ★ 崩れ対策：FlowRow で折り返し
        PrepBufferTravelRow(
            prepMin = prepMin, onPrep = { prepMin = it },
            bufferMin = bufferMin, onBuffer = { bufferMin = it },
            travelMin = travelMin, onTravel = { travelMin = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Divider(Modifier.padding(top = 8.dp))

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("集合：${meetAt.format(fmtDate)} ${meetAt.format(fmtTime)}")
            Text("出発：${departAt.format(fmtDate)} ${departAt.format(fmtTime)}")
            Text("起床：${wakeAt.format(fmtDate)} ${wakeAt.format(fmtTime)}")
            if (tooLate) Text("⚠ すでに出発時刻を過ぎています", color = MaterialTheme.colorScheme.error)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Button(
                onClick = {
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, wakeAt.hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, wakeAt.minute)
                        putExtra(
                            AlarmClock.EXTRA_MESSAGE,
                            "起床（${destination.ifBlank { "おでかけ" }}）"
                        )
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    }
                    ctx.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            ) { Text("起床アラームをセット") }

            Button(
                onClick = {
                    val uri = android.net.Uri.parse(
                        "google.navigation:q=${android.net.Uri.encode(destination)}"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    ctx.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            ) { Text("Googleマップで経路を開く") }
        }
    }
}

@Composable
fun NumberField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrepBufferTravelRow(
    prepMin: String, onPrep: (String) -> Unit,
    bufferMin: String, onBuffer: (String) -> Unit,
    travelMin: String, onTravel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 3,
        modifier = modifier.fillMaxWidth()
    ) {
        NumberField(
            value = prepMin, onChange = onPrep, label = "準備(分)",
            modifier = Modifier.widthIn(min = 120.dp).weight(1f, fill = false)
        )
        NumberField(
            value = bufferMin, onChange = onBuffer, label = "バッファ(分)",
            modifier = Modifier.widthIn(min = 120.dp).weight(1f, fill = false)
        )
        NumberField(
            value = travelMin, onChange = onTravel, label = "所要(分)",
            modifier = Modifier.widthIn(min = 120.dp).weight(1f, fill = false)
        )
    }
}
