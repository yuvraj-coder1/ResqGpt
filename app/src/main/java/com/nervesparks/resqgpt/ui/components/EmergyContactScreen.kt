package com.nervesparks.resqgpt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.nervesparks.resqgpt.MainViewModel
import com.nervesparks.resqgpt.model.EmergencyContact
import com.nervesparks.resqgpt.R

@Composable
fun EmergencyContactScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val contactList = viewModel.emergencyContactList.collectAsState()
    EmergencyContactList(contactList = contactList.value, viewModel = viewModel)
}

@Composable
fun EmergencyContactList(
    modifier: Modifier = Modifier,
    contactList: List<EmergencyContact>,
    viewModel: MainViewModel
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contactList) { item ->
            EmergencyContactItem(modifier = Modifier.fillMaxWidth(), item = item, viewModel = viewModel)
        }
        item {
            AddContactButton(viewModel = viewModel)
        }
    }
}

@Composable
fun EmergencyContactItem(
    modifier: Modifier = Modifier,
    item: EmergencyContact,
    viewModel: MainViewModel
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEmergencyContact(item)
                        showDialog = false
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        "Delete",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        "Cancel",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            title = {
                Text(
                    "Confirm Deletion",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this contact?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            },
            containerColor = Color(0xFF2C2C2C),
            tonalElevation = 8.dp
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF1F1F1F), shape = RoundedCornerShape(10.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = item.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = item.phoneNumber,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = { showDialog = true }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove Contact",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun AddContactButton(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF1F1F1F), shape = RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFF2C2C2C),
                focusedContainerColor = Color(0xFF3C3C3C),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedIndicatorColor = Color.Gray,
                focusedIndicatorColor = Color.Blue
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFF2C2C2C),
                focusedContainerColor = Color(0xFF3C3C3C),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedIndicatorColor = Color.Gray,
                focusedIndicatorColor = Color.Blue
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (name.isNotEmpty() && phoneNumber.isNotEmpty())
                    viewModel.addEmergencyContact(name, phoneNumber)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("Add Contact", style = MaterialTheme.typography.labelMedium)
        }
    }
}