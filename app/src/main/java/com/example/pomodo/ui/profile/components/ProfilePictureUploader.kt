package com.example.pomodo.ui.profile.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun ProfilePictureUploader(photoUrl: String?, onPictureSelected: (Uri) -> Unit) {
    var loading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            loading = true
            onPictureSelected(it)
        }
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .background(Color.LightGray, CircleShape)
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        } else {
            if (photoUrl.isNullOrEmpty()) {
                Text(
                    text = "Sem Foto",
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(photoUrl),
                    contentDescription = "Foto do perfil",
                    modifier = Modifier.size(120.dp)
                )
            }
        }
    }
}
