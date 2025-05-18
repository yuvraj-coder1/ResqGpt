package com.nervesparks.resqgpt.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nervesparks.resqgpt.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageScreen(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    var messageText by remember { mutableStateOf("") }
    val connectedDevices = viewModel.connectedEndpoints.collectAsState().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Status text with connected device count
        Text(
            text = "Status: ${viewModel.connectionStatus}",
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color.White
        )

        // Role selection - more explicit options
        Row(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(
                onClick = { viewModel.startAsHub() },
                enabled = !viewModel.isDiscoverer && !viewModel.isAdvertiser,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Start as Hub")
            }

            Button(
                onClick = { viewModel.startAsNode() },
                enabled = !viewModel.isDiscoverer && !viewModel.isAdvertiser,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Start as Node")
            }
        }

        // Display connected devices
        if (connectedDevices.isNotEmpty()) {
            Text(
                text = "Connected devices (${connectedDevices.size}):",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            LazyRow(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(connectedDevices.entries.toList()) { entry ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = entry.value,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Messages list with improved design
        val messages = viewModel.messagesReceived.collectAsState().value
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(messages) { messageData ->
                MessageItem(messageData)
            }
        }

        // Only show message input area when connected
        if (viewModel.connectedEndpoints.collectAsState().value.isNotEmpty()) {
            // Message input and send area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = {
                        Text(
                            "Type your message...",
                            color = Color.White.copy(alpha = 0.5f),
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary

//                    textColor = Color.White,
//                    cursorColor = Color.White,
//                    placeholderColor = Color.White.copy(alpha = 0.5f),
//                    focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val currentMessage = messageText
                                viewModel.getCurrentLocation { latitude, longitude ->
                                    viewModel.sendMessage("MSG:$currentMessage|LOC:$latitude,$longitude")
                                    Log.d("message sent", messageText)
                                    messageText = ""
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text( if(viewModel.isDiscoverer)"Broadcast" else "Send")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.getCurrentLocation { latitude, longitude ->
                                viewModel.sendMessage("SOS:Emergency help needed|LOC:$latitude,$longitude")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Text("SOS")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(messageText: String) {
    val isIncoming = !messageText.startsWith("Sent:")
    val isSos = messageText.contains("SOS:")

    val backgroundColor = when {
        isSos -> Color.Red.copy(alpha = 0.7f)
        isIncoming -> Color.DarkGray.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    }

    val parts = messageText.split("|LOC:")

    val messageContent = if (parts.isNotEmpty()) {
        val rawMessage = parts[0]
        when {
            rawMessage.startsWith("Sent:") -> rawMessage.substringAfter("Sent:")
            rawMessage.startsWith("MSG:") -> rawMessage.substringAfter("MSG:")
            rawMessage.startsWith("SOS:") -> rawMessage.substringAfter("SOS:")
            else -> rawMessage
        }.trim()
    } else {
        messageText
    }
    Log.d("message received", messageText)
    val location = if (parts.size > 1) parts[1] else null

    Column(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        horizontalAlignment = if (isIncoming) Alignment.Start else Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = messageContent,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (location != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Message timestamp
        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}