package com.lumina.tvaggregator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lumina.tvaggregator.data.model.Content

// Palette de couleurs pour le Mode Enfant
private val KidsOrange = Color(0xFFFF6B35)
private val KidsYellow = Color(0xFFFFD700)
private val KidsPurple = Color(0xFF9B59B6)
private val KidsTeal = Color(0xFF1ABC9C)
private val KidsPink = Color(0xFFFF69B4)
private val KidsBackground = Color(0xFF1A0A2E)
private val KidsCardBackground = Color(0xFF2D1B4E)
private val KidsSurface = Color(0xFF3D2460)

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun KidsScreen(
    kidsContent: List<Content>,
    isLoading: Boolean,
    onContentClick: (Content) -> Unit,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(KidsBackground, Color(0xFF0D1B2A))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp)
        ) {
            // Header coloré et ludique
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(KidsPurple, KidsOrange)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bouton retour
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }

                // Titre avec emojis
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🧸 Mode Enfant",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 36.sp
                    )
                    Text(
                        text = "🎠 Animations & Films famille — du contenu pour tous les petits !",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Emojis décoratifs
                Text(
                    text = "🌈✨🚀",
                    fontSize = 28.sp
                )

                // Bouton rafraîchir
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.background(
                        Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Rafraîchir",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Compteur de contenu
            if (kidsContent.isNotEmpty() && !isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KidsTeal.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "🎬 ${kidsContent.size} titres disponibles",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = KidsTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = KidsYellow.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "🌟 Belgique & France",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = KidsYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Contenu principal
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = KidsOrange, strokeWidth = 4.dp)
                            Text(
                                text = "🎪 Chargement des aventures...",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "On cherche les meilleures animations pour toi !",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                kidsContent.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "😢", fontSize = 64.sp)
                            Text(
                                text = "Aucun contenu disponible",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reviens plus tard ou rafraîchis la page !",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(containerColor = KidsOrange)
                            ) {
                                Text("🔄 Réessayer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(kidsContent) { content ->
                            KidsContentCard(
                                content = content,
                                onClick = { onContentClick(content) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun KidsContentCard(
    content: Content,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Couleur d'accentuation variée selon l'index (basé sur l'id)
    val accentColors = listOf(KidsOrange, KidsPurple, KidsTeal, KidsPink, KidsYellow)
    val accentColor = accentColors[(content.id.hashCode() and 0x7FFFFFFF) % accentColors.size]

    Card(
        onClick = onClick,
        modifier = modifier
            .width(220.dp)
            .height(350.dp)
            .then(Modifier.clickable { onClick() })
    ) {
        Box(modifier = Modifier.fillMaxSize().background(KidsCardBackground)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Poster
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(content.getPosterImageUrl())
                            .crossfade(true)
                            .build(),
                        contentDescription = content.title,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                        error = painterResource(android.R.drawable.ic_menu_gallery),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    )

                    // Badge coloré en haut à gauche
                    if (content.genres.contains("ani")) {
                        Surface(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopStart),
                            shape = RoundedCornerShape(8.dp),
                            color = KidsOrange
                        ) {
                            Text(
                                text = "🎨 Anim",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (content.genres.contains("fml")) {
                        Surface(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopStart),
                            shape = RoundedCornerShape(8.dp),
                            color = KidsTeal
                        ) {
                            Text(
                                text = "👨‍👩‍👧 Famille",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Score IMDB
                    content.imdbScore?.let { score ->
                        Surface(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopEnd),
                            shape = RoundedCornerShape(8.dp),
                            color = KidsYellow.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "⭐ ${String.format("%.1f", score)}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Barre colorée en bas du poster
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter)
                            .background(accentColor)
                    )
                }

                // Infos du contenu
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = content.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )

                    content.originalReleaseYear?.let { year ->
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
