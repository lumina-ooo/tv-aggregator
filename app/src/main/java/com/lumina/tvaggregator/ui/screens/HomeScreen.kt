package com.lumina.tvaggregator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.lumina.tvaggregator.data.PlatformCategory
import com.lumina.tvaggregator.data.SearchFilter
import com.lumina.tvaggregator.data.StreamingPlatform
import com.lumina.tvaggregator.ui.components.PlatformCard

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    platforms: List<StreamingPlatform>,
    searchFilter: SearchFilter,
    onSearchQueryChanged: (String) -> Unit,
    onCategoryFilterChanged: (PlatformCategory?) -> Unit,
    onInstalledFilterChanged: (Boolean) -> Unit,
    onPlatformClick: (StreamingPlatform) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        // Title
        Text(
            text = "Agrégateur TV",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Plateformes de streaming gratuites francophones",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Search and filters section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search bar
            OutlinedTextField(
                value = searchFilter.query,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text("Rechercher une plateforme...")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Rechercher")
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true
            )

            // Category filter
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = searchFilter.category?.let { getCategoryDisplayName(it) } ?: "Toutes",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Catégorie") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .width(180.dp)
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Toutes") },
                        onClick = {
                            onCategoryFilterChanged(null)
                            categoryExpanded = false
                        }
                    )
                    PlatformCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(getCategoryDisplayName(category)) },
                            onClick = {
                                onCategoryFilterChanged(category)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Installed filter
            FilterChip(
                onClick = { onInstalledFilterChanged(!searchFilter.installedOnly) },
                label = { Text("Installées") },
                selected = searchFilter.installedOnly
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Platform grid
        if (platforms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune plateforme trouvée",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(240.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(platforms) { platform ->
                    PlatformCard(
                        platform = platform,
                        onClick = { onPlatformClick(platform) }
                    )
                }
            }
        }
    }
}

private fun getCategoryDisplayName(category: PlatformCategory): String = when (category) {
    PlatformCategory.NATIONAL_TV -> "TV Nationale"
    PlatformCategory.REGIONAL_TV -> "TV Régionale"
    PlatformCategory.CULTURAL -> "Culturel"
    PlatformCategory.INTERNATIONAL -> "International"
    PlatformCategory.LIVE_TV -> "TV en Direct"
}