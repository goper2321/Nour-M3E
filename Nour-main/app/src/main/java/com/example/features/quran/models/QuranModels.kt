package com.example.features.quran.models

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishMeaning: String,
    val revelationType: RevelationType,
    val ayahCount: Int,
    val startPage: Int,
    val juzNumber: Int
)

enum class RevelationType(val label: String) {
    MECCAN("Meccan"),
    MEDINAN("Medinan")
}

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val translations: Map<String, String> = emptyMap(),
    val tafsirEntries: Map<String, String> = emptyMap()
) {
    fun getTranslation(editionId: String): String {
        return translations[editionId] ?: translations["sahih_en"] ?: "Translation not available"
    }

    fun getTafsir(editionId: String): String {
        return tafsirEntries[editionId] ?: tafsirEntries["muyassar"] ?: "Tafsir not cached for this verse."
    }
}

data class MushafPage(
    val pageNumber: Int,
    val juzNumber: Int,
    val hizbQuarter: Int,
    val surahHeader: Surah?,
    val ayahs: List<Ayah>
)

data class TranslationEdition(
    val id: String,
    val name: String,
    val language: String,
    val author: String
)

data class TafsirEdition(
    val id: String,
    val name: String,
    val language: String,
    val author: String
)
