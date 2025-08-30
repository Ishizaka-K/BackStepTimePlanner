package com.ikent.backsteptimeplanner.ui.screen

import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ikent.backsteptimeplanner.model.PlannerState
import com.ikent.backsteptimeplanner.ui.component.DateRowWithPicker
import com.ikent.backsteptimeplanner.ui.component.TimeRowWithPicker
import com.ikent.backsteptimeplanner.ui.component.PrepBufferTravelRow
import com.ikent.backsteptimeplanner.util.fmtDate
import com.ikent.backsteptimeplanner.util.fmtTime
import java.time.*

@Composable
fun PlannerScreen() {
    val ctx = LocalContext.current

    var state by remember { mutableStateOf(PlannerState.init()) }

    val meetAt = state.meetAt()
    val departAt = state.departAt()
    val wakeAt = state.wakeAt()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding()
    ) {
        OutlinedTextField(
            value = state.destination,
            onValueChange = { state = state.copy(destination = it) },
            label = { Text("目的地（例：天神駅）") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        DateRowWithPicker(
            date = state.meetDate,
            onChange = { state = state.copy(meetDate = it) }
        )

        TimeRowWithPicker(
            time = state.meetTime,
            onChange = { state = state.copy(meetTime = it) }
        )

        PrepBufferTravelRow(
            prepMin = state.prepMin,
            onPrep = { state = state.copy(prepMin = it) },
            bufferMin = state.bufferMin,
            onBuffer = { state = state.copy(bufferMin = it) },
            travelMin = state.travelMin,
            onTravel = { state = state.copy(travelMin = it) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Divider(Modifier.padding(top = 8.dp))

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("集合：${meetAt.format(fmtDate)} ${meetAt.format(fmtTime)}")
            Text("出発：${departAt.format(fmtDate)} ${departAt.format(fmtTime)}")
            Text("起床：${wakeAt.format(fmtDate)} ${wakeAt.format(fmtTime)}")
            if (state.isTooLate()) Text("⚠ すでに出発時刻を過ぎています", color = MaterialTheme.colorScheme.error)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()
        ) {
            Button(
                onClick = {
                    val i = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, wakeAt.hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, wakeAt.minute)
                        putExtra(AlarmClock.EXTRA_MESSAGE, "起床（${state.destination.ifBlank { "おでかけ" }}）")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    }
                    ctx.startActivity(i)
                },
                modifier = Modifier.weight(1f)
            ) { Text("起床アラームをセット") }

            Button(
                onClick = {
                    val uri = android.net.Uri.parse("google.navigation:q=${android.net.Uri.encode(state.destination)}")
                    val i = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
                    ctx.startActivity(i)
                },
                modifier = Modifier.weight(1f)
            ) { Text("Googleマップで経路を開く") }
        }
    }
}
