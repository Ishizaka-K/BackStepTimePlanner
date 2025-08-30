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
            modifier = Modifier.weight(1f).clickable { open = true }.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
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
                        onChange(Instant.ofEpochMilli(millis).atZone(zone).toLocalDate())
                    }
                    open = false
                }) { Text("決定") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("キャンセル") } }
        ) { DatePicker(state = state) }
    }
}
