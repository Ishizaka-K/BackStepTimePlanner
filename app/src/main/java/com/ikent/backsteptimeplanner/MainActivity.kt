package com.ikent.backsteptimeplanner

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
            DateRowWithPicker(date = meetDate, onChange = { meetDate = it})
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            TimeRowWithPicker(time = meetTime, onChange = { meetTime = it })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    date: LocalDate,
    onChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    val initMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
    var open by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(initialSelectedDateMillis = initMillis)

    OutlinedTextField(
        value = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (E)")),
        onValueChange = {},
        label = { Text("集合日") },
        readOnly = true,
        modifier = modifier.clickable { open = true }
    )

    if (open) {
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                        onChange(picked)
                    }
                    open = false
                }) { Text("決定") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("キャンセル") } }
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    time: LocalTime,
    onChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = true
) {
    var open by remember { mutableStateOf(false) }
    val tpState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = is24Hour
    )

    OutlinedTextField(
        value = time.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a")),
        onValueChange = {},
        label = { Text("集合時刻") },
        readOnly = true,
        modifier = modifier.clickable { open = true }
    )

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(LocalTime.of(tpState.hour, tpState.minute))
                    open = false
                }) { Text("決定") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("キャンセル") } },
            text = {
                // Material3 の TimePicker
                TimePicker(state = tpState)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRowWithPicker(
    date: LocalDate,
    onChange: (LocalDate) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()
    val fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd (E)")

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(onClick = { onChange(date.minusDays(1)) }) { Text("前日") }

        Text(
            text = date.format(fmt),
            modifier = Modifier
                .weight(1f)
                .clickable { open = true }
                .padding(vertical = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

        Button(onClick = { onChange(date.plusDays(1)) }) { Text("翌日") }
    }

    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                        onChange(picked)
                    }
                    open = false
                }) { Text("決定") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("キャンセル") } }
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRowWithPicker(
    time: LocalTime,
    onChange: (LocalTime) -> Unit,
    is24Hour: Boolean = true
) {
    var open by remember { mutableStateOf(false) }
    val fmt = DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a")

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(onClick = { onChange(time.minusMinutes(5)) }) { Text("-5分") }

        Text(
            text = time.format(fmt),
            modifier = Modifier
                .weight(1f)
                .clickable { open = true }
                .padding(vertical = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

        Button(onClick = { onChange(time.plusMinutes(5)) }) { Text("+5分") }
    }

    if (open) {
        val tpState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = is24Hour
        )
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(LocalTime.of(tpState.hour, tpState.minute))
                    open = false
                }) { Text("決定") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("キャンセル") } },
            text = { TimePicker(state = tpState) }
        )
    }
}
