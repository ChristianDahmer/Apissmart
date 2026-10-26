package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import java.util.Locale

data class GpsCoordinates(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context): GpsCoordinates? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            try {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < (bestLocation.accuracy)) {
                    bestLocation = loc
                }
            } catch (_: SecurityException) {
                // Ignore if permission not granted
            } catch (_: Exception) {
                // Ignore fallback
            }
        }

        return bestLocation?.let {
            GpsCoordinates(
                latitude = it.latitude,
                longitude = it.longitude,
                altitude = if (it.hasAltitude()) it.altitude else null,
                accuracy = if (it.hasAccuracy()) it.accuracy else null,
                timestamp = it.time
            )
        }
    }

    fun formatCoordinates(lat: Double?, lon: Double?): String {
        if (lat == null || lon == null) return "Não informado"
        return String.format(Locale.US, "%.5f, %.5f", lat, lon)
    }

    fun formatDms(lat: Double?, lon: Double?): String {
        if (lat == null || lon == null) return "Coordenadas não disponíveis"
        val latDir = if (lat >= 0) "N" else "S"
        val absLat = Math.abs(lat)
        val latDeg = absLat.toInt()
        val latMin = ((absLat - latDeg) * 60).toInt()
        val latSec = ((absLat - latDeg - latMin / 60.0) * 3600).toInt()

        val lonDir = if (lon >= 0) "E" else "W"
        val absLon = Math.abs(lon)
        val lonDeg = absLon.toInt()
        val lonMin = ((absLon - lonDeg) * 60).toInt()
        val lonSec = ((absLon - lonDeg - lonMin / 60.0) * 3600).toInt()

        return "$latDeg°$latMin'$latSec\"$latDir, $lonDeg°$lonMin'$lonSec\"$lonDir"
    }

    fun createMapIntent(lat: Double, lon: Double, label: String = "Colmeia / Isca"): Intent {
        val uriStr = "geo:$lat,$lon?q=$lat,$lon($label)"
        val gmmIntentUri = Uri.parse(uriStr)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return mapIntent
    }

    fun createNavigationIntent(lat: Double, lon: Double): Intent {
        val uriStr = "google.navigation:q=$lat,$lon"
        val gmmIntentUri = Uri.parse(uriStr)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return mapIntent
    }

    fun createShareLocationIntent(
        title: String,
        lat: Double,
        lon: Double,
        altitude: Double? = null,
        extraInfo: String = ""
    ): Intent {
        val mapsUrl = "https://maps.google.com/?q=$lat,$lon"
        val text = buildString {
            appendLine("📍 $title")
            appendLine("Coordenadas: ${formatCoordinates(lat, lon)}")
            if (altitude != null) {
                appendLine("Altitude: ${altitude.toInt()} m")
            }
            if (extraInfo.isNotBlank()) {
                appendLine("Detalhes: $extraInfo")
            }
            appendLine("Abrir no Google Maps: $mapsUrl")
            append("Enviado via ApisSmart 🐝")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Localização: $title")
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Compartilhar Localização")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return chooser
    }
}
