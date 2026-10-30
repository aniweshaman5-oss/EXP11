package com.example.ex11

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

data class CustomMarkerData(
    val position: LatLng,
    val title: String,
    val snippet: String
)

data class CityLocation(
    val name: String,
    val latLng: LatLng
)

val cityPresets = listOf(
    CityLocation("Tokyo", LatLng(35.6762, 139.6503)),
    CityLocation("Paris", LatLng(48.8566, 2.3522)),
    CityLocation("New York", LatLng(40.7128, -74.0060)),
    CityLocation("Sydney", LatLng(-33.8688, 151.2093)),
    CityLocation("London", LatLng(51.5074, -0.1278))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Default starting camera position: Tokyo Tower
    val initialPosition = LatLng(35.6586, 139.7454)
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 12f)
    }

    // Map Settings State
    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    var isTrafficEnabled by remember { mutableStateOf(false) }
    var isZoomControlsEnabled by remember { mutableStateOf(true) }

    // User-added Markers
    val userMarkers = remember {
        mutableStateListOf(
            CustomMarkerData(
                position = initialPosition,
                title = "Tokyo Tower",
                snippet = "Landmark in Minato, Tokyo, Japan"
            )
        )
    }

    // Check if API key is still placeholder
    val isApiKeyMissing = remember {
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
            apiKey.isNull_or_Empty() || apiKey == "YOUR_API_KEY_HERE"
        } catch (_: Exception) {
            true
        }
    }
    var showApiKeyBanner by remember { mutableStateOf(isApiKeyMissing) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Google Maps Viewer",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Google Map Composable
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = selectedMapType,
                    isTrafficEnabled = isTrafficEnabled
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = isZoomControlsEnabled,
                    compassEnabled = true
                ),
                onMapClick = { latLng ->
                    // Add new marker when tapping on the map
                    userMarkers.add(
                        CustomMarkerData(
                            position = latLng,
                            title = "Custom Location",
                            snippet = "Lat: %.4f, Lng: %.4f".format(latLng.latitude, latLng.longitude)
                        )
                    )
                }
            ) {
                // Render markers
                userMarkers.forEach { markerData ->
                    Marker(
                        state = remember(markerData.position) { MarkerState(position = markerData.position) },
                        title = markerData.title,
                        snippet = markerData.snippet
                    )
                }
            }

            // Overlay Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // API Key Warning Banner if active
                AnimatedVisibility(
                    visible = showApiKeyBanner,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "Google Maps API Key Required",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                TextButton(onClick = { showApiKeyBanner = false }) {
                                    Text("Dismiss")
                                }
                            }
                            Text(
                                text = "Map tiles require a valid API key. Please update 'com.google.android.geo.API_KEY' in AndroidManifest.xml.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // City Preset Chips (Horizontal Scroll)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jump to:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        cityPresets.forEach { city ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(city.latLng, 12f)
                                        )
                                    }
                                },
                                label = { Text(city.name) },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                // Map Type & Layer Controls
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                            )
                            Text(
                                text = "Type:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        val mapTypes = listOf(
                            "Normal" to MapType.NORMAL,
                            "Satellite" to MapType.SATELLITE,
                            "Terrain" to MapType.TERRAIN,
                            "Hybrid" to MapType.HYBRID
                        )

                        mapTypes.forEach { (label, type) ->
                            FilterChip(
                                selected = (selectedMapType == type),
                                onClick = { selectedMapType = type },
                                label = { Text(label) },
                                modifier = Modifier.padding(horizontal = 2.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Traffic Toggle Chip
                        FilterChip(
                            selected = isTrafficEnabled,
                            onClick = { isTrafficEnabled = !isTrafficEnabled },
                            label = { Text("Traffic") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Traffic,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        // Zoom Controls Toggle
                        FilterChip(
                            selected = isZoomControlsEnabled,
                            onClick = { isZoomControlsEnabled = !isZoomControlsEnabled },
                            label = { Text("Zoom Buttons") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_Empty(): Boolean = this == null || this.isEmpty()
