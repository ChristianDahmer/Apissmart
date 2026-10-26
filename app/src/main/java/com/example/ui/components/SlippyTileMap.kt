package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.tan

enum class MapType(val displayName: String, val serviceName: String, val icon: String) {
    SATELLITE("Esri World Imagery", "World_Imagery", "🛰️"),
    TERRAIN("Esri World Topo Map", "World_Topo_Map", "⛰️"),
    STANDARD("Esri World Street Map", "World_Street_Map", "🗺️")
}

object MapTileMath {
    fun lonToTileX(lon: Double, zoom: Int): Double {
        return (lon + 180.0) / 360.0 * (1 shl zoom)
    }

    fun latToTileY(lat: Double, zoom: Int): Double {
        val latRad = Math.toRadians(lat.coerceIn(-85.0511, 85.0511))
        return (1.0 - log(tan(PI / 4.0 + latRad / 2.0), Math.E) / PI) / 2.0 * (1 shl zoom)
    }

    fun tileXToLon(x: Double, zoom: Int): Double {
        return x / (1 shl zoom) * 360.0 - 180.0
    }

    fun tileYToLat(y: Double, zoom: Int): Double {
        val n = PI - 2.0 * PI * y / (1 shl zoom)
        return Math.toDegrees(2.0 * atan(exp(n)) - PI / 2.0)
    }

    fun metersToPixels(meters: Double, latitude: Double, zoomLevel: Float, density: Float): Float {
        val latRad = Math.toRadians(latitude.coerceIn(-85.0511, 85.0511))
        val metersPerPixel = 156543.03392 * cos(latRad) / 2.0.pow(zoomLevel.toDouble())
        return ((meters / metersPerPixel) * density).toFloat()
    }
}

class SlippyTileMapState(
    initialLat: Double,
    initialLon: Double,
    initialZoom: Float = 15.5f
) {
    var centerLatitude by mutableStateOf(initialLat)
    var centerLongitude by mutableStateOf(initialLon)
    var zoomLevel by mutableFloatStateOf(initialZoom.coerceIn(10.0f, 19.5f))
    var panOffsetX by mutableFloatStateOf(0f)
    var panOffsetY by mutableFloatStateOf(0f)
    var mapType by mutableStateOf(MapType.SATELLITE)

    fun zoomIn() {
        zoomLevel = (zoomLevel + 1.0f).coerceAtMost(19.5f)
    }

    fun zoomOut() {
        zoomLevel = (zoomLevel - 1.0f).coerceAtLeast(10.0f)
    }

    fun recenter(lat: Double? = null, lon: Double? = null, zoom: Float? = null) {
        if (lat != null) centerLatitude = lat
        if (lon != null) centerLongitude = lon
        if (zoom != null) zoomLevel = zoom.coerceIn(10.0f, 19.5f)
        panOffsetX = 0f
        panOffsetY = 0f
    }

    fun getCurrentCenter(density: Float, widthPx: Float, heightPx: Float): Pair<Double, Double> {
        val baseZ = zoomLevel.toInt().coerceIn(10, 19)
        val scaleFraction = 2f.pow(zoomLevel - baseZ)
        val tileSizePx = 256f * density * scaleFraction
        val centerTileX = MapTileMath.lonToTileX(centerLongitude, baseZ)
        val centerTileY = MapTileMath.latToTileY(centerLatitude, baseZ)
        val currentTileX = centerTileX - (panOffsetX / tileSizePx)
        val currentTileY = centerTileY - (panOffsetY / tileSizePx)
        val lon = MapTileMath.tileXToLon(currentTileX, baseZ)
        val lat = MapTileMath.tileYToLat(currentTileY, baseZ)
        return Pair(lat, lon)
    }

    fun latLonToScreen(
        lat: Double,
        lon: Double,
        density: Float,
        widthPx: Float,
        heightPx: Float
    ): Offset {
        val baseZ = zoomLevel.toInt().coerceIn(10, 19)
        val scaleFraction = 2f.pow(zoomLevel - baseZ)
        val tileSizePx = 256f * density * scaleFraction
        val centerTileX = MapTileMath.lonToTileX(centerLongitude, baseZ)
        val centerTileY = MapTileMath.latToTileY(centerLatitude, baseZ)
        val markerTileX = MapTileMath.lonToTileX(lon, baseZ)
        val markerTileY = MapTileMath.latToTileY(lat, baseZ)
        val x = (widthPx / 2f) + panOffsetX + (markerTileX - centerTileX).toFloat() * tileSizePx
        val y = (heightPx / 2f) + panOffsetY + (markerTileY - centerTileY).toFloat() * tileSizePx
        return Offset(x, y)
    }

    fun screenToLatLon(
        screenOffset: Offset,
        density: Float,
        widthPx: Float,
        heightPx: Float
    ): Pair<Double, Double> {
        val baseZ = zoomLevel.toInt().coerceIn(10, 19)
        val scaleFraction = 2f.pow(zoomLevel - baseZ)
        val tileSizePx = 256f * density * scaleFraction
        val centerTileX = MapTileMath.lonToTileX(centerLongitude, baseZ)
        val centerTileY = MapTileMath.latToTileY(centerLatitude, baseZ)
        val tx = centerTileX + (screenOffset.x - widthPx / 2f - panOffsetX) / tileSizePx
        val ty = centerTileY + (screenOffset.y - heightPx / 2f - panOffsetY) / tileSizePx
        val lon = MapTileMath.tileXToLon(tx, baseZ)
        val lat = MapTileMath.tileYToLat(ty, baseZ)
        return Pair(lat, lon)
    }
}

@Composable
fun rememberSlippyTileMapState(
    initialLat: Double,
    initialLon: Double,
    initialZoom: Float = 15.5f
): SlippyTileMapState {
    return remember(initialLat, initialLon) {
        SlippyTileMapState(initialLat, initialLon, initialZoom)
    }
}

@Composable
fun SlippyTileMap(
    state: SlippyTileMapState,
    modifier: Modifier = Modifier,
    onTap: ((Double, Double) -> Unit)? = null,
    overlayContent: (@Composable (widthPx: Float, heightPx: Float, density: Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density

    val backgroundColor = when (state.mapType) {
        MapType.SATELLITE -> Color(0xFF1E2D1D)
        MapType.TERRAIN -> Color(0xFFD8E3CE)
        MapType.STANDARD -> Color(0xFFE3EBDD)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(state) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Apply zoom change with a more controlled factor and ensure limits are strictly enforced
                    val zoomFactor = (zoom - 1f) * 2f
                    state.zoomLevel = (state.zoomLevel + zoomFactor).coerceIn(10.0f, 19.5f)
                    state.panOffsetX += pan.x
                    state.panOffsetY += pan.y
                }
            }
            .then(
                if (onTap != null) {
                    Modifier.pointerInput(state) {
                        detectTapGestures { tapOffset ->
                            val widthPx = size.width.toFloat()
                            val heightPx = size.height.toFloat()
                            val (lat, lon) = state.screenToLatLon(tapOffset, density, widthPx, heightPx)
                            onTap(lat, lon)
                        }
                    }
                } else Modifier
            )
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val baseZ = state.zoomLevel.toInt().coerceIn(10, 19)
        val scaleFraction = 2f.pow(state.zoomLevel - baseZ)
        val scaledTileSizePx = 256f * density * scaleFraction
        val scaledTileSizeDp = (256 * scaleFraction).dp

        val screenCenterX = widthPx / 2f
        val screenCenterY = heightPx / 2f

        val centerTileX = MapTileMath.lonToTileX(state.centerLongitude, baseZ)
        val centerTileY = MapTileMath.latToTileY(state.centerLatitude, baseZ)

        val maxTileIndex = (1 shl baseZ) - 1

        val minTx = floor(centerTileX - (screenCenterX + state.panOffsetX + scaledTileSizePx) / scaledTileSizePx)
            .toInt().coerceIn(0, maxTileIndex)
        val maxTx = ceil(centerTileX + (widthPx - screenCenterX - state.panOffsetX) / scaledTileSizePx)
            .toInt().coerceIn(0, maxTileIndex)

        val minTy = floor(centerTileY - (screenCenterY + state.panOffsetY + scaledTileSizePx) / scaledTileSizePx)
            .toInt().coerceIn(0, maxTileIndex)
        val maxTy = ceil(centerTileY + (heightPx - screenCenterY - state.panOffsetY) / scaledTileSizePx)
            .toInt().coerceIn(0, maxTileIndex)

        // Render static cached tiles in 2D grid
        for (tx in minTx..maxTx) {
            for (ty in minTy..maxTy) {
                val leftPx = screenCenterX + state.panOffsetX + (tx - centerTileX).toFloat() * scaledTileSizePx
                val topPx = screenCenterY + state.panOffsetY + (ty - centerTileY).toFloat() * scaledTileSizePx

                val tileUrl = "https://services.arcgisonline.com/ArcGIS/rest/services/${state.mapType.serviceName}/MapServer/tile/$baseZ/$ty/$tx"

                Box(
                    modifier = Modifier
                        .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                        .size(scaledTileSizeDp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(tileUrl)
                            .crossfade(150)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Overlay elements (markers, pins, ranges, canvas)
        overlayContent?.invoke(widthPx, heightPx, density)
    }
}
