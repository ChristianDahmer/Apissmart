package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val label: String, val description: String) {
    DARK("🌙 Tema Escuro (Padrão)", "Modo noturno com fundo escuro e economia de bateria"),
    LIGHT("☀️ Tema Claro", "Modo diurno com fundo claro e alto brilho"),
    SYSTEM("📱 Padrão do Celular", "Segue o modo Claro/Escuro do sistema Android")
}

enum class PrimaryPalette(
    val label: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val containerLight: Color,
    val containerDark: Color
) {
    HONEY_GOLD(
        label = "🍯 Dourado Mel (Padrão)",
        primaryLight = Color(0xFFD97706),
        primaryDark = Color(0xFFFBBF24),
        containerLight = Color(0xFFFEF3C7),
        containerDark = Color(0xFF78350F)
    ),
    MEADOW_GREEN(
        label = "🌿 Verde Flora & Mata",
        primaryLight = Color(0xFF059669),
        primaryDark = Color(0xFF34D399),
        containerLight = Color(0xFFD1FAE5),
        containerDark = Color(0xFF064E3B)
    ),
    WARM_AMBER(
        label = "☀️ Âmbar Solar",
        primaryLight = Color(0xFFEA580C),
        primaryDark = Color(0xFFFB923C),
        containerLight = Color(0xFFFFEDD5),
        containerDark = Color(0xFF7C2D12)
    ),
    ROYAL_BLUE(
        label = "🌊 Azul Real Apícola",
        primaryLight = Color(0xFF0284C7),
        primaryDark = Color(0xFF38BDF8),
        containerLight = Color(0xFFE0F2FE),
        containerDark = Color(0xFF075985)
    ),
    LAVENDER(
        label = "🪻 Lavanda & Alecrim",
        primaryLight = Color(0xFF7C3AED),
        primaryDark = Color(0xFFA78BFA),
        containerLight = Color(0xFFEDE9FE),
        containerDark = Color(0xFF4C1D95)
    ),
    PROPOLIS_ORANGE(
        label = "🪵 Própolis & Cera",
        primaryLight = Color(0xFFB45309),
        primaryDark = Color(0xFFF59E0B),
        containerLight = Color(0xFFFEEBC8),
        containerDark = Color(0xFF5B2B04)
    )
}

enum class FontColorOption(
    val label: String,
    val lightColor: Color,
    val darkColor: Color
) {
    DEFAULT(
        label = "Automático (Padrão)",
        lightColor = Color(0xFF1C1917),
        darkColor = Color(0xFFF1F5F9)
    ),
    HIGH_CONTRAST(
        label = "Alto Contraste (Preto Puro / Branco)",
        lightColor = Color(0xFF000000),
        darkColor = Color(0xFFFFFFFF)
    ),
    GOLDEN_AMBER(
        label = "Dourado Mel Intenso",
        lightColor = Color(0xFF78350F),
        darkColor = Color(0xFFFEF08A)
    ),
    FOREST_GREEN(
        label = "Verde Floresta",
        lightColor = Color(0xFF064E3B),
        darkColor = Color(0xFF6EE7B7)
    ),
    DEEP_NAVY(
        label = "Azul Noturno Profundo",
        lightColor = Color(0xFF0F172A),
        darkColor = Color(0xFFBAE6FD)
    ),
    CHARCOAL(
        label = "Grafite Profundo",
        lightColor = Color(0xFF334155),
        darkColor = Color(0xFFE2E8F0)
    )
}

enum class SubtitleColorOption(
    val label: String,
    val lightColor: Color,
    val darkColor: Color
) {
    DEFAULT_SLATE(
        label = "Cinza Ardósia (Padrão)",
        lightColor = Color(0xFF475569),
        darkColor = Color(0xFF94A3B8)
    ),
    MINT_GREEN(
        label = "Menta Suave",
        lightColor = Color(0xFF047857),
        darkColor = Color(0xFFA7F3D0)
    ),
    WARM_HONEY(
        label = "Caramelo & Mel",
        lightColor = Color(0xFF92400E),
        darkColor = Color(0xFFFDE68A)
    ),
    MUTED_CYAN(
        label = "Azul Céu Suave",
        lightColor = Color(0xFF0369A1),
        darkColor = Color(0xFF7DD3FC)
    ),
    SOFT_SILVER(
        label = "Prata & Cinza Claro",
        lightColor = Color(0xFF64748B),
        darkColor = Color(0xFFCBD5E1)
    )
}

enum class FontSizeScale(
    val label: String,
    val scaleFactor: Float,
    val description: String
) {
    SMALL("Pequeno", 0.90f, "Compacto, exibe mais conteúdo"),
    NORMAL("Normal (Padrão)", 1.0f, "Equilíbrio padrão"),
    LARGE("Grande", 1.15f, "Melhor leitura em movimento"),
    EXTRA_LARGE("Extra Grande (Campo)", 1.30f, "Alta visibilidade sob luz solar")
}

data class AppThemeConfig(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val primaryPalette: PrimaryPalette = PrimaryPalette.HONEY_GOLD,
    val fontColorOption: FontColorOption = FontColorOption.DEFAULT,
    val subtitleColorOption: SubtitleColorOption = SubtitleColorOption.DEFAULT_SLATE,
    val fontSizeScale: FontSizeScale = FontSizeScale.NORMAL
)

class ThemePreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("apis_smart_theme_prefs", Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<AppThemeConfig> = _themeConfig.asStateFlow()

    fun loadConfig(): AppThemeConfig {
        val modeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val paletteStr = prefs.getString(KEY_PRIMARY_PALETTE, PrimaryPalette.HONEY_GOLD.name) ?: PrimaryPalette.HONEY_GOLD.name
        val fontColorStr = prefs.getString(KEY_FONT_COLOR, FontColorOption.DEFAULT.name) ?: FontColorOption.DEFAULT.name
        val subtitleColorStr = prefs.getString(KEY_SUBTITLE_COLOR, SubtitleColorOption.DEFAULT_SLATE.name) ?: SubtitleColorOption.DEFAULT_SLATE.name
        val fontScaleStr = prefs.getString(KEY_FONT_SCALE, FontSizeScale.NORMAL.name) ?: FontSizeScale.NORMAL.name

        return AppThemeConfig(
            themeMode = try { AppThemeMode.valueOf(modeStr) } catch (_: Exception) { AppThemeMode.DARK },
            primaryPalette = try { PrimaryPalette.valueOf(paletteStr) } catch (_: Exception) { PrimaryPalette.HONEY_GOLD },
            fontColorOption = try { FontColorOption.valueOf(fontColorStr) } catch (_: Exception) { FontColorOption.DEFAULT },
            subtitleColorOption = try { SubtitleColorOption.valueOf(subtitleColorStr) } catch (_: Exception) { SubtitleColorOption.DEFAULT_SLATE },
            fontSizeScale = try { FontSizeScale.valueOf(fontScaleStr) } catch (_: Exception) { FontSizeScale.NORMAL }
        )
    }

    fun saveConfig(config: AppThemeConfig) {
        prefs.edit()
            .putString(KEY_THEME_MODE, config.themeMode.name)
            .putString(KEY_PRIMARY_PALETTE, config.primaryPalette.name)
            .putString(KEY_FONT_COLOR, config.fontColorOption.name)
            .putString(KEY_SUBTITLE_COLOR, config.subtitleColorOption.name)
            .putString(KEY_FONT_SCALE, config.fontSizeScale.name)
            .apply()
        _themeConfig.value = config
    }

    fun resetToDefaults() {
        val defaultConfig = AppThemeConfig()
        saveConfig(defaultConfig)
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_PRIMARY_PALETTE = "primary_palette"
        private const val KEY_FONT_COLOR = "font_color"
        private const val KEY_SUBTITLE_COLOR = "subtitle_color"
        private const val KEY_FONT_SCALE = "font_scale"
    }
}
