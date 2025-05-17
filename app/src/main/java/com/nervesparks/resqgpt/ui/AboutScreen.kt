package com.nervesparks.resqgpt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AboutScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            SectionHeader(text = "Welcome to ResQ-GPT")
        }
        item {
            Text(
                text = "ResQ-GPT is an offline Android chatbot built to assist during emergencies and disasters. Powered by the efficient llama.cpp framework, it runs entirely on your device—no internet, no servers, no data leaks. Whether you're facing earthquakes, floods, fires, or other crises, ResQ-GPT provides instant, AI-powered guidance when you need it most.\n" +
                        "\n" +
                        "But it's more than just a chatbot. ResQ-GPT includes critical safety tools like:\n" +
                        "\n" +
                        "\uD83D\uDCCD Integrated offline maps\n" +
                        "\n" +
                        "\uD83D\uDEA8 SOS features for quick alerts\n" +
                        "\n" +
                        "\uD83D\uDDE3\uFE0F Voice recognition support\n" +
                        "\n" +
                        "\uD83D\uDCCB Preparedness prompts and emergency checklists",
                fontSize = 16.sp,
                color = Color.White,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionHeader(text = "Features")
        }

        items(features) { feature ->
            FeatureItem(feature = feature)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = "FAQs")
        }

        items(faqs) { faq ->
            FaqItem(question = faq.first, answer = faq.second)
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun FeatureItem(feature: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color(0xFF4CAF50), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = feature,
            fontSize = 16.sp,
            color = Color.White,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color(0xFF1b384f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = answer,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 20.sp,
            modifier = Modifier.padding(start = 32.dp)  // Aligned with question text
        )
    }
}

private val features = listOf(
    "Offline Functionality: Runs without the need for an internet connection.",
    "Privacy First: All data is processed locally on your device.",
    "Customizable Models: Download and use your preferred AI model with ease.",
    "Open Source: Built on the foundations of the llama.cpp Android example, enabling developers to contribute and modify."
)

private val faqs = listOf(
    "What is llama.cpp?" to "llama.cpp is an open-source project that enables running large language models (LLMs) on edge devices such as smartphones and laptops.",
    "Do I need an internet connection to use this app?" to "Yes, but only to download models to your device. After that, the app operates entirely offline. All operations are performed locally on your device.",
    "Which AI models are supported?" to "The app supports GGUF models. You can download and integrate them as needed.",
    "Is my data safe while using this app?" to "Yes, since the app works offline, no data is transmitted to external servers, ensuring complete privacy.",

)