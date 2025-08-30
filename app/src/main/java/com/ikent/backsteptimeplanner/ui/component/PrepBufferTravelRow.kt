package com.ikent.backsteptimeplanner.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType

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
        NumberField(prepMin, onPrep, "準備(分)")
        NumberField(bufferMin, onBuffer, "バッファ(分)")
        NumberField(travelMin, onTravel, "所要(分)")
    }
}

@Composable
private fun NumberField(
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
        modifier = modifier.widthIn(min = 120.dp)
    )
}
