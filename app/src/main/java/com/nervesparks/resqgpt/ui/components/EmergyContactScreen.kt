package com.nervesparks.resqgpt.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nervesparks.resqgpt.MainViewModel
import com.nervesparks.resqgpt.model.EmergencyContact

@Composable
fun EmergencyContactScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val contactList =
        viewModel.emergencyContactList.collectAsState()
    EmergencyContactList(contactList = contactList.value, viewModel = viewModel)
}

@Composable
fun EmergencyContactList(
    modifier: Modifier = Modifier,
    contactList: List<EmergencyContact>,
    viewModel: MainViewModel
) {
    LazyColumn {
        items(contactList) {
            EmergencyContactItem(modifier, it)
        }
        item { AddContactButton(viewModel = viewModel) }
    }
}

@Composable
fun EmergencyContactItem(modifier: Modifier = Modifier, item: EmergencyContact) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Text(text = item.name, color = Color.White)
        Text(text = item.phoneNumber, color = Color.White)

    }
}

@Composable
fun AddContactButton(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
//            colors = TextFieldDefaults.colors(
//                unfocusedTextColor = Color.White,
//
//            )
        )
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") }
        )
        Button(onClick = {
            if (name.isNotEmpty() && phoneNumber.isNotEmpty())
                viewModel.addEmergencyContact(name, phoneNumber)
        }
        ) {
            Text("Add Contact")
        }
    }
}
