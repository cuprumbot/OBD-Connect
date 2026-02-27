package edu.galileo.innovacion.obdconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.galileo.innovacion.obdconnect.ui.components.Gauge
import edu.galileo.innovacion.obdconnect.ui.viewmodels.ConnectionViewModel

@Composable
fun CarStatusTabContent(
    viewModel: ConnectionViewModel
) {
    val rpmValue by viewModel.rpmValue.collectAsState()
    val speedValue by viewModel.speedValue.collectAsState()
    val coolantValue by viewModel.coolantValue.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vehicle Status",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (!isConnected) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Connect to OBD2 sensor to view vehicle data",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Create list of gauges
            val gauges = listOf(
                GaugeData("RPM", rpmValue, 0f, 7000f, "rpm"),
                GaugeData("Speed", speedValue, 0f, 160f, "km/h"),
                GaugeData("Coolant", coolantValue, -40f, 120f, "°C")
            )

            // Display gauges in rows of 2
            gauges.chunked(2).forEach { rowGauges ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = if (rowGauges.size == 1) {
                        Arrangement.Center
                    } else {
                        Arrangement.SpaceEvenly
                    }
                ) {
                    rowGauges.forEach { gaugeData ->
                        Box(
                            modifier = if (rowGauges.size == 1) {
                                Modifier.width(200.dp)
                            } else {
                                Modifier.weight(1f)
                            }
                        ) {
                            Gauge(
                                label = gaugeData.label,
                                value = gaugeData.value,
                                minValue = gaugeData.minValue,
                                maxValue = gaugeData.maxValue,
                                unit = gaugeData.unit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class GaugeData(
    val label: String,
    val value: Float,
    val minValue: Float,
    val maxValue: Float,
    val unit: String
)