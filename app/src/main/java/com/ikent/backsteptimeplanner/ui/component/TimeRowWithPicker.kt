package com.ikent.backsteptimeplanner.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.*
import java.time.format.DateTimeFormatter

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
            modifier = Modifier.weight(1f).clickable { open = true }.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
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
