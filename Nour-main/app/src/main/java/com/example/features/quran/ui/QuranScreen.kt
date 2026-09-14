package com.example.features.quran.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.BookmarkEntity
import com.example.database.NourDatabase
import com.example.features.quran.data.QuranRepository
import com.example.features.quran.models.Ayah
import com.example.features.quran.models.RevelationType
import com.example.features.quran.models.Surah
import com.example.ui.m3e.M3EAlertDialog
import com.example.ui.m3e.M3EBadge
import com.example.ui.m3e.M3EBottomSheet
import com.example.ui.m3e.M3EButton
import com.example.ui.m3e.M3EButtonSize
import com.example.ui.m3e.M3EButtonVariant
import com.example.ui.m3e.M3ECard
import com.example.ui.m3e.M3ECardHeader
import com.example.ui.m3e.M3ECardVariant
import com.example.ui.m3e.M3EEmptyState
import com.example.ui.m3e.M3EFilterChipRow
import com.example.ui.m3e.M3ENumberBadge
import com.example.ui.m3e.M3EPillTabRow
import com.example.ui.m3e.M3ESearchField
import com.example.ui.m3e.M3ESliderSection
import com.example.ui.m3e.M3ESpacing
import com.example.ui.m3e.M3EViewModeToggle
import kotlinx.coroutines.launch

/**
 * M3E Quran experience — business logic preserved, presentation rebuilt.
 *
 * M3E mapping (web -> Compose):
 * - search        -> M3ESearchField (m3e-search, pill 56dp, clear trailing)
 * - tabs          -> M3EPillTabRow (m3e segmented-button, 3 destinations + badges)
 * - filters       -> M3EFilterChipRow (m3e filter-chip-set)
 * - surah rows    -> M3ECard outlined + M3ENumberBadge (m3e list + avatar)
 * - reader chrome -> small app header Surface (m3e-app-bar small)
 * - ayah cards    -> M3ECard outlined, header/actions/footer slots
 * - dialogs       -> M3EAlertDialog (m3e-dialog 28dp)
 * - settings      -> M3EBottomSheet (m3e-bottom-sheet 28dp top corners)
 * - empty states  -> M3EEmptyState (m3e content-pane)
 */
enum class QuranReadingMode(val label: String) {
    CARDS("Verse Cards"),
    CONTINUOUS("Continuous Text")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Surahs, 1 = Mushaf, 2 = Bookmarks
    var currentPageNumber by remember { mutableIntStateOf(1) } // 1 to 604
    var selectedSurahForReading by remember { mutableStateOf<Surah?>(null) }
    var readingMode by remember { mutableStateOf(QuranReadingMode.CONTINUOUS) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    var selectedEditionId by remember { mutableStateOf("sahih_en") }
    var selectedTafsirId by remember { mutableStateOf("muyassar") }
    var arabicFontSize by remember { mutableFloatStateOf(24f) }

    var selectedTafsirAyah by remember { mutableStateOf<Ayah?>(null) }
    var showPageJumpDialog by remember { mutableStateOf(false) }
    var jumpPageInput by remember { mutableStateOf("") }
    var showReadingSettingsSheet by remember { mutableStateOf(false) }
    var lastReadSurahNumber by remember { mutableIntStateOf(1) }

    val bookmarks by db.bookmarkDao().getAllBookmarks().collectAsState(initial = emptyList())

    fun toggleBookmark(ayah: Ayah) {
        coroutineScope.launch {
            val surah = QuranRepository.getSurah(ayah.surahNumber)
            val existing = db.bookmarkDao().getBookmark(ayah.surahNumber, ayah.ayahNumber)
            if (existing != null) {
                db.bookmarkDao().deleteBookmark(ayah.surahNumber, ayah.ayahNumber)
                Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
            } else {
                db.bookmarkDao().insertBookmark(
                    BookmarkEntity(
                        surahNumber = ayah.surahNumber,
                        ayahNumber = ayah.ayahNumber,
                        surahName = surah.nameEnglish,
                        ayahText = ayah.arabicText,
                        translationText = ayah.getTranslation(selectedEditionId)
                    )
                )
                Toast.makeText(context, "Ayah bookmarked!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("quran_screen")
    ) {
        if (selectedSurahForReading != null) {
            SurahReaderView(
                surah = selectedSurahForReading!!,
                readingMode = readingMode,
                onModeChange = { readingMode = it },
                editionId = selectedEditionId,
                tafsirId = selectedTafsirId,
                arabicFontSize = arabicFontSize,
                bookmarks = bookmarks,
                onBack = { selectedSurahForReading = null },
                onSelectSurah = { newSurah ->
                    selectedSurahForReading = newSurah
                    lastReadSurahNumber = newSurah.number
                },
                onFontSizeChange = { arabicFontSize = it },
                onSelectEdition = { selectedEditionId = it },
                onSelectTafsir = { selectedTafsirAyah = it },
                onOpenSettings = { showReadingSettingsSheet = true },
                onToggleBookmark = ::toggleBookmark
            )
        } else {
            QuranMainHeader(
                isSearchExpanded = isSearchExpanded || searchQuery.isNotBlank(),
                searchQuery = searchQuery,
                onToggleSearch = {
                    isSearchExpanded = !isSearchExpanded
                    if (!isSearchExpanded) searchQuery = ""
                },
                onSearchQueryChange = { searchQuery = it },
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                bookmarkCount = bookmarks.size
            )

            if (searchQuery.isNotBlank()) {
                val searchResults = remember(searchQuery) { QuranRepository.search(searchQuery) }
                SearchResultsView(
                    results = searchResults,
                    query = searchQuery,
                    editionId = selectedEditionId,
                    onSelectAyah = { ayah ->
                        selectedSurahForReading = QuranRepository.getSurah(ayah.surahNumber)
                        lastReadSurahNumber = ayah.surahNumber
                        searchQuery = ""
                        isSearchExpanded = false
                    }
                )
            } else {
                when (selectedTab) {
                    0 -> SurahListView(
                        lastReadSurah = QuranRepository.getSurah(lastReadSurahNumber),
                        onContinueReading = {
                            selectedSurahForReading = QuranRepository.getSurah(lastReadSurahNumber)
                        },
                        onSurahClick = { surah ->
                            selectedSurahForReading = surah
                            lastReadSurahNumber = surah.number
                        }
                    )
                    1 -> MushafPageView(
                        pageNumber = currentPageNumber,
                        readingMode = readingMode,
                        onModeChange = { readingMode = it },
                        editionId = selectedEditionId,
                        tafsirId = selectedTafsirId,
                        arabicFontSize = arabicFontSize,
                        onPrevPage = { if (currentPageNumber > 1) currentPageNumber-- },
                        onNextPage = { if (currentPageNumber < 604) currentPageNumber++ },
                        onJumpPageClick = {
                            jumpPageInput = currentPageNumber.toString()
                            showPageJumpDialog = true
                        },
                        onOpenSettings = { showReadingSettingsSheet = true },
                        onSelectTafsir = { selectedTafsirAyah = it },
                        onOpenFullSurah = { surah ->
                            selectedSurahForReading = surah
                            lastReadSurahNumber = surah.number
                            selectedTab = 0
                        },
                        onToggleBookmark = ::toggleBookmark,
                        bookmarks = bookmarks
                    )
                    2 -> BookmarksView(
                        bookmarks = bookmarks,
                        onGoToAyah = { bm ->
                            selectedSurahForReading = QuranRepository.getSurah(bm.surahNumber)
                            lastReadSurahNumber = bm.surahNumber
                            selectedTab = 0
                        },
                        onDeleteBookmark = { id ->
                            coroutineScope.launch { db.bookmarkDao().deleteById(id) }
                        }
                    )
                }
            }
        }
    }

    if (showPageJumpDialog) {
        M3EAlertDialog(
            onDismiss = { showPageJumpDialog = false },
            title = "Jump to page",
            text = {
                Column {
                    Text(
                        text = "Mushaf pages 1 – 604",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jumpPageInput,
                        onValueChange = { jumpPageInput = it },
                        label = { Text("Page number") },
                        placeholder = { Text("1 to 604") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirm = {
                M3EButton(
                    onClick = {
                        val p = jumpPageInput.toIntOrNull()
                        if (p != null && p in 1..604) {
                            currentPageNumber = p
                            showPageJumpDialog = false
                        } else {
                            Toast.makeText(context, "Enter a page between 1 and 604", Toast.LENGTH_SHORT).show()
                        }
                    },
                    size = M3EButtonSize.Small
                ) { Text("Go to page") }
            },
            dismiss = {
                M3EButton(
                    onClick = { showPageJumpDialog = false },
                    variant = M3EButtonVariant.Text,
                    size = M3EButtonSize.Small
                ) { Text("Cancel") }
            }
        )
    }

    if (showReadingSettingsSheet) {
        QuranSettingsBottomSheet(
            readingMode = readingMode,
            onModeChange = { readingMode = it },
            arabicFontSize = arabicFontSize,
            onFontSizeChange = { arabicFontSize = it },
            selectedEditionId = selectedEditionId,
            onSelectEdition = { selectedEditionId = it },
            selectedTafsirId = selectedTafsirId,
            onSelectTafsir = { selectedTafsirId = it },
            onDismiss = { showReadingSettingsSheet = false }
        )
    }

    selectedTafsirAyah?.let { ayah ->
        TafsirDialog(ayah = ayah, onDismiss = { selectedTafsirAyah = null })
    }
}

/** Browsing header: title row + expandable M3E search + pill tabs with badges. */
@Composable
fun QuranMainHeader(
    isSearchExpanded: Boolean,
    searchQuery: String,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    bookmarkCount: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "The Noble Qur'an",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "القرآن الكريم",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onToggleSearch,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearchExpanded) "Close search" else "Search Quran",
                        tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                M3ESearchField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = "Search Surah, verse, English or Arabic…",
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    } else null,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    testTag = "quran_search_field"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                M3EPillTabRow(
                    tabs = listOf("Surahs", "Mushaf", "Saved"),
                    selectedIndex = selectedTab,
                    onSelect = onSelectTab,
                    testTagPrefix = "quran_tab"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    M3EBadge(text = "114 surahs", modifier = Modifier.weight(1f))
                    M3EBadge(text = "604 pages", modifier = Modifier.weight(1f))
                    M3EBadge(text = "$bookmarkCount saved", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun QuranTabPill(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Kept for API compatibility; header now uses M3EPillTabRow.
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = subtitle, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Surah directory: continue-reading hero + revelation filters + outlined rows. */
@Composable
fun SurahListView(
    lastReadSurah: Surah,
    onContinueReading: () -> Unit,
    onSurahClick: (Surah) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredSurahs = remember(selectedFilter) {
        when (selectedFilter) {
            "Meccan" -> QuranRepository.ALL_SURAHS.filter { it.revelationType == RevelationType.MECCAN }
            "Medinan" -> QuranRepository.ALL_SURAHS.filter { it.revelationType == RevelationType.MEDINAN }
            else -> QuranRepository.ALL_SURAHS
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(M3ESpacing.md),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            M3ECard(
                variant = M3ECardVariant.Elevated,
                onClick = onContinueReading,
                header = {
                    M3ECardHeader(
                        eyebrow = "Continue reading",
                        title = "${lastReadSurah.number}. ${lastReadSurah.nameEnglish}",
                        subtitle = "${lastReadSurah.englishMeaning} • Page ${lastReadSurah.startPage}",
                        trailing = {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = lastReadSurah.nameArabic,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            ) {
                M3EButton(
                    onClick = onContinueReading,
                    size = M3EButtonSize.Small
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resume")
                }
            }
        }

        item {
            M3EFilterChipRow(
                options = listOf("All" to "114", "Meccan" to "86", "Medinan" to "28"),
                selected = selectedFilter,
                onSelect = { selectedFilter = it }
            )
        }

        items(filteredSurahs, key = { it.number }) { surah ->
            M3ECard(
                variant = M3ECardVariant.Outlined,
                onClick = { onSurahClick(surah) },
                modifier = Modifier.testTag("surah_item_${surah.number}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    M3ENumberBadge(text = "${surah.number}")
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = surah.nameEnglish,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = surah.englishMeaning,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Text(text = " • ${surah.ayahCount} verses", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = surah.nameArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${surah.revelationType.label} • Juz ${surah.juzNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** Minimal reader chrome + banner + verse list (cards / continuous). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderView(
    surah: Surah,
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    editionId: String,
    tafsirId: String,
    arabicFontSize: Float,
    bookmarks: List<BookmarkEntity>,
    onBack: () -> Unit,
    onSelectSurah: (Surah) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onSelectEdition: (String) -> Unit,
    onSelectTafsir: (Ayah) -> Unit,
    onOpenSettings: () -> Unit,
    onToggleBookmark: (Ayah) -> Unit
) {
    val ayahs = remember(surah.number) { QuranRepository.getAyahsForSurah(surah.number) }
    var showSurahDropdown by remember { mutableStateOf(false) }
    var selectedAyahForInspector by remember { mutableStateOf<Ayah?>(null) }
    var showTranslationsInContinuous by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().testTag("surah_reader_view")) {
        // Reader app bar (m3e-app-bar small)
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("surah_reader_back_button").size(48.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Surahs")
                    }
                    Box {
                        TextButton(onClick = { showSurahDropdown = true }) {
                            Column {
                                Text(
                                    text = "${surah.number}. ${surah.nameEnglish}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${surah.nameArabic} • ${surah.ayahCount} verses",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        DropdownMenu(expanded = showSurahDropdown, onDismissRequest = { showSurahDropdown = false }) {
                            QuranRepository.ALL_SURAHS.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.number}. ${s.nameEnglish} (${s.nameArabic})") },
                                    onClick = {
                                        onSelectSurah(s)
                                        selectedAyahForInspector = null
                                        showSurahDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        onModeChange(if (readingMode == QuranReadingMode.CONTINUOUS) QuranReadingMode.CARDS else QuranReadingMode.CONTINUOUS)
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (readingMode == QuranReadingMode.CONTINUOUS) Icons.Default.ViewAgenda else Icons.Default.AutoStories,
                            contentDescription = "Switch view mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = "Reading settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = PaddingValues(M3ESpacing.md),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                M3ECard(
                    variant = M3ECardVariant.Filled,
                    header = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "سُورَةُ ${surah.nameArabic}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Surah ${surah.nameEnglish} — ${surah.englishMeaning}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${surah.revelationType.label} • ${surah.ayahCount} Verses • Juz ${surah.juzNumber}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (surah.number != 9) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                ) {}
            }

            if (readingMode == QuranReadingMode.CARDS) {
                items(ayahs, key = { it.ayahNumber }) { ayah ->
                    val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber }
                    AyahCard(
                        ayah = ayah,
                        surah = surah,
                        editionId = editionId,
                        arabicFontSize = arabicFontSize,
                        isBookmarked = isBookmarked,
                        onSelectTafsir = { onSelectTafsir(ayah) },
                        onToggleBookmark = { onToggleBookmark(ayah) }
                    )
                }
            } else {
                item {
                    ContinuousQuranTextCard(
                        ayahs = ayahs,
                        arabicFontSize = arabicFontSize,
                        selectedAyahNumber = selectedAyahForInspector?.ayahNumber,
                        onSelectAyahNumber = { tappedNumber ->
                            selectedAyahForInspector = if (selectedAyahForInspector?.ayahNumber == tappedNumber) null
                            else ayahs.firstOrNull { it.ayahNumber == tappedNumber }
                        }
                    )
                }
                selectedAyahForInspector?.let { inspectedAyah ->
                    item {
                        AyahInspectorCard(
                            ayah = inspectedAyah,
                            editionId = editionId,
                            isBookmarked = bookmarks.any { it.surahNumber == inspectedAyah.surahNumber && it.ayahNumber == inspectedAyah.ayahNumber },
                            onDismiss = { selectedAyahForInspector = null },
                            onSelectTafsir = { onSelectTafsir(inspectedAyah) },
                            onToggleBookmark = { onToggleBookmark(inspectedAyah) }
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Full Translation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = { showTranslationsInContinuous = !showTranslationsInContinuous }) {
                            Text(if (showTranslationsInContinuous) "Hide All" else "Show All (${surah.ayahCount})")
                        }
                    }
                }
                if (showTranslationsInContinuous) {
                    items(ayahs, key = { "t_${it.ayahNumber}" }) { ayah ->
                        M3ECard(variant = M3ECardVariant.Filled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Verse ${ayah.ayahNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                TextButton(onClick = { onSelectTafsir(ayah) }) { Text("Tafsir", fontSize = 11.sp) }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = ayah.getTranslation(editionId), fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                M3ECard(variant = M3ECardVariant.Outlined) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (surah.number > 1) {
                                    onSelectSurah(QuranRepository.getSurah(surah.number - 1))
                                    selectedAyahForInspector = null
                                }
                            },
                            enabled = surah.number > 1
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }
                        M3EBadge(text = "${surah.number} / 114")
                        TextButton(
                            onClick = {
                                if (surah.number < 114) {
                                    onSelectSurah(QuranRepository.getSurah(surah.number + 1))
                                    selectedAyahForInspector = null
                                }
                            },
                            enabled = surah.number < 114
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/** Shared verse card (m3e outlined card, header/actions slots). */
@Composable
private fun AyahCard(
    ayah: Ayah,
    surah: Surah,
    editionId: String,
    arabicFontSize: Float,
    isBookmarked: Boolean,
    onSelectTafsir: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    M3ECard(
        variant = M3ECardVariant.Outlined,
        modifier = modifier.testTag("ayah_card_${ayah.ayahNumber}"),
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                M3EBadge(text = "${ayah.surahNumber}:${ayah.ayahNumber}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onSelectTafsir) { Text("Tafsir", fontSize = 12.sp) }
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark verse",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n[${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}]"))
                            Toast.makeText(context, "Verse copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy verse", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n\n— [Surah ${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}] via Nour")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Verse"))
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share verse", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    ) {
        Text(
            text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
            fontSize = arabicFontSize.sp,
            lineHeight = (arabicFontSize * 1.8f).sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = ayah.getTranslation(editionId), style = MaterialTheme.typography.bodyMedium)
    }
}

/** 604-page Mushaf viewer with M3E header + bottom page turner. */
@Composable
fun MushafPageView(
    pageNumber: Int,
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    editionId: String,
    tafsirId: String,
    arabicFontSize: Float,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onJumpPageClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectTafsir: (Ayah) -> Unit,
    onOpenFullSurah: (Surah) -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    bookmarks: List<BookmarkEntity>
) {
    val mushafPage = remember(pageNumber) { QuranRepository.getMushafPage(pageNumber) }
    var selectedAyahForInspector by remember { mutableStateOf<Ayah?>(null) }
    var showTranslationsInContinuous by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onJumpPageClick) {
                    Text(
                        text = "Page $pageNumber / 604 • Juz ${mushafPage.juzNumber}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        onModeChange(if (readingMode == QuranReadingMode.CONTINUOUS) QuranReadingMode.CARDS else QuranReadingMode.CONTINUOUS)
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (readingMode == QuranReadingMode.CONTINUOUS) Icons.Default.ViewAgenda else Icons.Default.AutoStories,
                            contentDescription = "Toggle view mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onOpenSettings, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = "Reading settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(M3ESpacing.md),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            mushafPage.surahHeader?.let { surah ->
                item {
                    M3ECard(
                        variant = M3ECardVariant.Filled,
                        header = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "سُورَةُ ${surah.nameArabic}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                Text(text = "Surah ${surah.nameEnglish} • ${surah.englishMeaning}", fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                            }
                        }
                    ) {
                        M3EButton(
                            onClick = { onOpenFullSurah(surah) },
                            variant = M3EButtonVariant.Outlined,
                            size = M3EButtonSize.ExtraSmall
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Read whole Surah (${surah.ayahCount} verses)", fontSize = 11.sp)
                        }
                    }
                }
            }

            if (readingMode == QuranReadingMode.CARDS) {
                items(mushafPage.ayahs, key = { "${it.surahNumber}_${it.ayahNumber}" }) { ayah ->
                    val surah = QuranRepository.getSurah(ayah.surahNumber)
                    val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber }
                    AyahCard(
                        ayah = ayah,
                        surah = surah,
                        editionId = editionId,
                        arabicFontSize = arabicFontSize,
                        isBookmarked = isBookmarked,
                        onSelectTafsir = { onSelectTafsir(ayah) },
                        onToggleBookmark = { onToggleBookmark(ayah) }
                    )
                }
            } else {
                item {
                    ContinuousQuranTextCard(
                        ayahs = mushafPage.ayahs,
                        arabicFontSize = arabicFontSize,
                        selectedAyahNumber = selectedAyahForInspector?.ayahNumber,
                        onSelectAyahNumber = { tappedNumber ->
                            selectedAyahForInspector = if (selectedAyahForInspector?.ayahNumber == tappedNumber) null
                            else mushafPage.ayahs.firstOrNull { it.ayahNumber == tappedNumber }
                        }
                    )
                }
                selectedAyahForInspector?.let { inspectedAyah ->
                    item {
                        AyahInspectorCard(
                            ayah = inspectedAyah,
                            editionId = editionId,
                            isBookmarked = bookmarks.any { it.surahNumber == inspectedAyah.surahNumber && it.ayahNumber == inspectedAyah.ayahNumber },
                            onDismiss = { selectedAyahForInspector = null },
                            onSelectTafsir = { onSelectTafsir(inspectedAyah) },
                            onToggleBookmark = { onToggleBookmark(inspectedAyah) }
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Page Translations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = { showTranslationsInContinuous = !showTranslationsInContinuous }) {
                            Text(if (showTranslationsInContinuous) "Hide" else "Show All")
                        }
                    }
                }
                if (showTranslationsInContinuous) {
                    items(mushafPage.ayahs, key = { "pt_${it.surahNumber}_${it.ayahNumber}" }) { ayah ->
                        M3ECard(variant = M3ECardVariant.Filled) {
                            Text(text = "Verse ${ayah.ayahNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = ayah.getTranslation(editionId), fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                M3EButton(
                    onClick = onPrevPage,
                    enabled = pageNumber > 1,
                    variant = M3EButtonVariant.Outlined,
                    size = M3EButtonSize.Small
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Previous")
                }
                Text(text = "Page $pageNumber", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                M3EButton(
                    onClick = onNextPage,
                    enabled = pageNumber < 604,
                    size = M3EButtonSize.Small
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/** Reading preferences sheet (m3e-bottom-sheet port). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSettingsBottomSheet(
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    arabicFontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    selectedEditionId: String,
    onSelectEdition: (String) -> Unit,
    selectedTafsirId: String,
    onSelectTafsir: (String) -> Unit,
    onDismiss: () -> Unit
) {
    M3EBottomSheet(
        onDismiss = onDismiss,
        title = "Reading Preferences",
        subtitle = "Layout, type size, translation"
    ) {
        Column {
            Text(text = "Layout style", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            QuranViewModeToggle(readingMode = readingMode, onModeChange = onModeChange)
        }
        M3ESliderSection(title = "Arabic font size", valueLabel = "${arabicFontSize.toInt()} sp") {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("A", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = arabicFontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 18f..40f,
                    steps = 10,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )
                Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column {
            Text(text = "Translation edition", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(QuranRepository.TRANSLATION_EDITIONS) { edition ->
                    val isSelected = edition.id == selectedEditionId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectEdition(edition.id) },
                        label = { Text("${edition.name} (${edition.language})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun QuranViewModeToggle(
    readingMode: QuranReadingMode,
    onModeChange: (QuranReadingMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // M3E segmented port with legacy per-mode testTags preserved for UI tests.
    M3EViewModeToggle(
        options = listOf(
            "Verse Cards" to Icons.Default.ViewAgenda,
            "Continuous Page" to Icons.Default.AutoStories
        ),
        selectedIndex = if (readingMode == QuranReadingMode.CARDS) 0 else 1,
        onSelect = { onModeChange(if (it == 0) QuranReadingMode.CARDS else QuranReadingMode.CONTINUOUS) },
        modifier = modifier,
        testTag = "quran_view_mode_toggle",
        optionTestTags = listOf("toggle_cards_mode", "toggle_continuous_mode")
    )
}

/** Flowing Uthmani text with tappable ayah markers (container now M3E outlined). */
@Composable
fun ContinuousQuranTextCard(
    ayahs: List<Ayah>,
    arabicFontSize: Float,
    selectedAyahNumber: Int?,
    onSelectAyahNumber: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val ayahMarkerColor = MaterialTheme.colorScheme.primary

    val continuousAnnotatedString = remember(ayahs, selectedAyahNumber, arabicFontSize, primaryContainerColor) {
        buildAnnotatedString {
            ayahs.forEach { ayah ->
                val isSelected = (ayah.ayahNumber == selectedAyahNumber)
                pushStringAnnotation(tag = "AYAH", annotation = "${ayah.ayahNumber}")
                if (isSelected) {
                    pushStyle(
                        SpanStyle(
                            background = primaryContainerColor.copy(alpha = 0.5f),
                            color = onPrimaryContainerColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                append(ayah.arabicText)
                append(" ")
                pushStyle(
                    SpanStyle(
                        color = ayahMarkerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = (arabicFontSize * 0.9f).sp
                    )
                )
                append("\u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)} ")
                pop()
                if (isSelected) pop()
                pop()
            }
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    M3ECard(
        variant = M3ECardVariant.Outlined,
        modifier = modifier.testTag("continuous_text_surface")
    ) {
        Text(
            text = continuousAnnotatedString,
            fontSize = arabicFontSize.sp,
            lineHeight = (arabicFontSize * 2.1f).sp,
            textAlign = TextAlign.Right,
            style = TextStyle(textDirection = TextDirection.Rtl),
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(continuousAnnotatedString) {
                    detectTapGestures { offset ->
                        textLayoutResult?.let { layout ->
                            val charOffset = layout.getOffsetForPosition(offset)
                            val annotations = continuousAnnotatedString.getStringAnnotations("AYAH", charOffset, charOffset)
                            annotations.firstOrNull()?.item?.toIntOrNull()?.let(onSelectAyahNumber)
                        }
                    }
                },
            onTextLayout = { textLayoutResult = it }
        )
    }
}

/** Inspector for a tapped verse (m3e elevated card, actions slot). */
@Composable
fun AyahInspectorCard(
    ayah: Ayah,
    editionId: String,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onSelectTafsir: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val surah = QuranRepository.getSurah(ayah.surahNumber)

    M3ECard(
        variant = M3ECardVariant.Elevated,
        modifier = modifier.testTag("ayah_inspector_card"),
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                M3EBadge(text = "Surah ${surah.nameEnglish} • Verse ${ayah.ayahNumber}")
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Close inspector")
                }
            }
        }
    ) {
        Text(
            text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
            fontSize = 18.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = ayah.getTranslation(editionId), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            M3EButton(onClick = onSelectTafsir, size = M3EButtonSize.ExtraSmall, modifier = Modifier.weight(1f)) {
                Text("Tafsir", fontSize = 12.sp)
            }
            M3EButton(
                onClick = onToggleBookmark,
                variant = M3EButtonVariant.Outlined,
                size = M3EButtonSize.ExtraSmall
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isBookmarked) "Saved" else "Save", fontSize = 12.sp)
            }
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString("${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n[${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}]"))
                    Toast.makeText(context, "Verse copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy verse", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "${ayah.arabicText}\n\n\"${ayah.getTranslation(editionId)}\"\n\n— [Surah ${surah.nameEnglish} ${ayah.surahNumber}:${ayah.ayahNumber}] via Nour")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Verse"))
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share verse", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SearchResultsView(
    results: List<Ayah>,
    query: String,
    editionId: String,
    onSelectAyah: (Ayah) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${results.size} verses found for \"$query\"",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(14.dp)
            )
        }

        if (results.isEmpty()) {
            M3EEmptyState(
                icon = Icons.Default.SearchOff,
                title = "No verses found",
                message = "No verses matched \"$query\". Try an English keyword, Surah name, or Arabic phrase.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(M3ESpacing.md),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results, key = { "${it.surahNumber}_${it.ayahNumber}" }) { ayah ->
                    M3ECard(
                        variant = M3ECardVariant.Outlined,
                        onClick = { onSelectAyah(ayah) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Surah ${QuranRepository.getSurah(ayah.surahNumber).nameEnglish} (${ayah.surahNumber}:${ayah.ayahNumber})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = "Page ${ayah.pageNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = ayah.getTranslation(editionId), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarksView(
    bookmarks: List<BookmarkEntity>,
    onGoToAyah: (BookmarkEntity) -> Unit,
    onDeleteBookmark: (Long) -> Unit
) {
    if (bookmarks.isEmpty()) {
        M3EEmptyState(
            icon = Icons.Default.BookmarkBorder,
            title = "No saved verses yet",
            message = "Tap the heart icon while reading any Ayah to save your reflections and favorite verses here.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(M3ESpacing.md),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bookmarks, key = { it.id }) { bm ->
                M3ECard(
                    variant = M3ECardVariant.Outlined,
                    onClick = { onGoToAyah(bm) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${bm.surahName} (${bm.surahNumber}:${bm.ayahNumber})",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onDeleteBookmark(bm.id) }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Remove bookmark ${bm.surahNumber}:${bm.ayahNumber}")
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = bm.ayahText, fontSize = 16.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    if (bm.translationText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = bm.translationText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** Tafsir reflection dialog (m3e-dialog port with tonal Arabic header). */
@Composable
private fun TafsirDialog(
    ayah: Ayah,
    onDismiss: () -> Unit
) {
    val surah = QuranRepository.getSurah(ayah.surahNumber)
    M3EAlertDialog(
        onDismiss = onDismiss,
        title = "Tafsir • ${surah.nameEnglish} (${ayah.surahNumber}:${ayah.ayahNumber})",
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(text = "Authentic scholarly exegesis", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${ayah.arabicText} \u06DD${QuranRepository.toArabicDigits(ayah.ayahNumber)}",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 30.sp,
                        modifier = Modifier.fillMaxWidth().padding(14.dp)
                    )
                }
                Column {
                    Text(text = "Tafsir al-Muyassar (التفسير الميسر):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = ayah.getTafsir("muyassar"), fontSize = 14.sp, lineHeight = 22.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Column {
                    Text(text = "Ibn Kathir reflection:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = ayah.getTafsir("kathir"), fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirm = {
            M3EButton(onClick = onDismiss, size = M3EButtonSize.Small) { Text("Close") }
        }
    )
}
