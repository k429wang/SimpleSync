package com.example.simplesync.ui.components
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ScreenTitle(title: String) {
    Text(
        text = title,
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .padding(top = 16.dp, bottom = 12.dp)
    )
}
