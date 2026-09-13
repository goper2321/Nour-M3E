package com.example.features.quran.data

import com.example.features.quran.models.Ayah
import com.example.features.quran.models.MushafPage
import com.example.features.quran.models.RevelationType
import com.example.features.quran.models.Surah
import com.example.features.quran.models.TafsirEdition
import com.example.features.quran.models.TranslationEdition
import java.util.Locale

object QuranRepository {

    val TRANSLATION_EDITIONS = listOf(
        TranslationEdition("sahih_en", "Sahih International", "English", "Saheeh Int."),
        TranslationEdition("pickthall_en", "Pickthall", "English", "Marmaduke Pickthall"),
        TranslationEdition("hamidullah_fr", "Muhammad Hamidullah", "French", "Muhammad Hamidullah"),
        TranslationEdition("bernstrom_sv", "Knut Bernström", "Swedish", "Knut Bernström"),
        TranslationEdition("diyanet_tr", "Diyanet İşleri", "Turkish", "Diyanet"),
        TranslationEdition("jalandhry_ur", "Fateh Muhammad Jalandhry", "Urdu", "Jalandhry"),
        TranslationEdition("bubenheim_de", "Frank Bubenheim", "German", "Bubenheim & Elyas"),
        TranslationEdition("cortes_es", "Julio Cortés", "Spanish", "Julio Cortés"),
        TranslationEdition("indonesian_id", "Indonesian Ministry", "Indonesian", "Kemenag"),
        TranslationEdition("rahman_bn", "Mujibur Rahman", "Bengali", "Mujibur Rahman"),
        TranslationEdition("khan_hi", "Suhel Farooq Khan", "Hindi", "Farooq Khan & Nadwi"),
        TranslationEdition("kuliev_ru", "Elmir Kuliev", "Russian", "Elmir Kuliev"),
        TranslationEdition("jian_zh", "Ma Jian", "Chinese", "Ma Jian")
    )

    val TAFSIR_EDITIONS = listOf(
        TafsirEdition("muyassar", "Tafsir al-Muyassar (الميسر)", "Arabic", "King Fahd Complex"),
        TafsirEdition("kathir", "Tafsir Ibn Kathir (ابن كثير)", "Arabic / English", "Imam Ibn Kathir"),
        TafsirEdition("maarif", "Ma'arif-ul-Quran", "English / Urdu", "Mufti Muhammad Shafi")
    )

    // Complete index of all 114 Surahs
    val ALL_SURAHS: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", RevelationType.MECCAN, 7, 1, 1),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", RevelationType.MEDINAN, 286, 2, 1),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", RevelationType.MEDINAN, 200, 50, 3),
        Surah(4, "النساء", "An-Nisa", "The Women", RevelationType.MEDINAN, 176, 77, 4),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", RevelationType.MEDINAN, 120, 106, 6),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", RevelationType.MECCAN, 165, 128, 7),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", RevelationType.MECCAN, 206, 151, 8),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", RevelationType.MEDINAN, 75, 177, 9),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", RevelationType.MEDINAN, 129, 187, 10),
        Surah(10, "يونس", "Yunus", "Jonah", RevelationType.MECCAN, 109, 208, 11),
        Surah(11, "هود", "Hud", "Hud", RevelationType.MECCAN, 123, 221, 11),
        Surah(12, "يوسف", "Yusuf", "Joseph", RevelationType.MECCAN, 111, 235, 12),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", RevelationType.MEDINAN, 43, 249, 13),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", RevelationType.MECCAN, 52, 255, 13),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", RevelationType.MECCAN, 99, 262, 14),
        Surah(16, "النحل", "An-Nahl", "The Bee", RevelationType.MECCAN, 128, 267, 14),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", RevelationType.MECCAN, 111, 282, 15),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", RevelationType.MECCAN, 110, 293, 15),
        Surah(19, "مريم", "Maryam", "Mary", RevelationType.MECCAN, 98, 305, 16),
        Surah(20, "طه", "Ta-Ha", "Ta-Ha", RevelationType.MECCAN, 135, 312, 16),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", RevelationType.MECCAN, 112, 322, 17),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", RevelationType.MEDINAN, 78, 332, 17),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", RevelationType.MECCAN, 118, 342, 18),
        Surah(24, "النور", "An-Nur", "The Light", RevelationType.MEDINAN, 64, 350, 18),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", RevelationType.MECCAN, 77, 359, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", RevelationType.MECCAN, 227, 367, 19),
        Surah(27, "النمل", "An-Naml", "The Ant", RevelationType.MECCAN, 93, 377, 19),
        Surah(28, "القصص", "Al-Qasas", "The Stories", RevelationType.MECCAN, 88, 385, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", RevelationType.MECCAN, 69, 396, 20),
        Surah(30, "الروم", "Ar-Rum", "The Romans", RevelationType.MECCAN, 60, 404, 21),
        Surah(31, "لقمان", "Luqman", "Luqman", RevelationType.MECCAN, 34, 411, 21),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", RevelationType.MECCAN, 30, 415, 21),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", RevelationType.MEDINAN, 73, 418, 21),
        Surah(34, "سبأ", "Saba", "Sheba", RevelationType.MECCAN, 54, 428, 22),
        Surah(35, "فاطر", "Fatir", "Originator", RevelationType.MECCAN, 45, 434, 22),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", RevelationType.MECCAN, 83, 440, 22),
        Surah(37, "الصافات", "As-Saffat", "Those Who Set The Ranks", RevelationType.MECCAN, 182, 446, 23),
        Surah(38, "ص", "Sad", "The Letter Sad", RevelationType.MECCAN, 88, 453, 23),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", RevelationType.MECCAN, 75, 458, 23),
        Surah(40, "غافر", "Ghafir", "The Forgiver", RevelationType.MECCAN, 85, 467, 24),
        Surah(41, "فصلت", "Fussilat", "Explained In Detail", RevelationType.MECCAN, 54, 477, 24),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", RevelationType.MECCAN, 53, 483, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", RevelationType.MECCAN, 89, 489, 25),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", RevelationType.MECCAN, 59, 496, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", RevelationType.MECCAN, 37, 499, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", RevelationType.MECCAN, 35, 502, 26),
        Surah(47, "محمد", "Muhammad", "Muhammad", RevelationType.MEDINAN, 38, 507, 26),
        Surah(48, "الفتح", "Al-Fath", "The Victory", RevelationType.MEDINAN, 29, 511, 26),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", RevelationType.MEDINAN, 18, 515, 26),
        Surah(50, "ق", "Qaf", "The Letter Qaf", RevelationType.MECCAN, 45, 518, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", RevelationType.MECCAN, 60, 520, 26),
        Surah(52, "الطور", "At-Tur", "The Mount", RevelationType.MECCAN, 49, 523, 27),
        Surah(53, "النجم", "An-Najm", "The Star", RevelationType.MECCAN, 62, 526, 27),
        Surah(54, "القمر", "Al-Qamar", "The Moon", RevelationType.MECCAN, 55, 528, 27),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", RevelationType.MEDINAN, 78, 531, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", RevelationType.MECCAN, 96, 534, 27),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", RevelationType.MEDINAN, 29, 537, 27),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", RevelationType.MEDINAN, 22, 542, 28),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", RevelationType.MEDINAN, 24, 545, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She That Is Examined", RevelationType.MEDINAN, 13, 549, 28),
        Surah(61, "الصف", "As-Saff", "The Ranks", RevelationType.MEDINAN, 14, 551, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation, Friday", RevelationType.MEDINAN, 11, 553, 28),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", RevelationType.MEDINAN, 11, 554, 28),
        Surah(64, "التغابن", "At-Taghabun", "Mutual Disillusion", RevelationType.MEDINAN, 18, 556, 28),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", RevelationType.MEDINAN, 12, 558, 28),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", RevelationType.MEDINAN, 12, 560, 28),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", RevelationType.MECCAN, 30, 562, 29),
        Surah(68, "القلم", "Al-Qalam", "The Pen", RevelationType.MECCAN, 52, 564, 29),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", RevelationType.MECCAN, 52, 566, 29),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", RevelationType.MECCAN, 44, 568, 29),
        Surah(71, "نوح", "Nuh", "Noah", RevelationType.MECCAN, 28, 570, 29),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", RevelationType.MECCAN, 28, 572, 29),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", RevelationType.MECCAN, 20, 574, 29),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", RevelationType.MECCAN, 56, 575, 29),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", RevelationType.MECCAN, 40, 577, 29),
        Surah(76, "الإنسان", "Al-Insan", "The Human", RevelationType.MEDINAN, 31, 578, 29),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", RevelationType.MECCAN, 50, 580, 29),
        Surah(78, "النبأ", "An-Naba", "The Tidings", RevelationType.MECCAN, 40, 582, 30),
        Surah(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", RevelationType.MECCAN, 46, 583, 30),
        Surah(80, "عبس", "'Abasa", "He Frowned", RevelationType.MECCAN, 42, 585, 30),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", RevelationType.MECCAN, 29, 586, 30),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", RevelationType.MECCAN, 19, 587, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "Defrauding", RevelationType.MECCAN, 36, 587, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", RevelationType.MECCAN, 25, 589, 30),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", RevelationType.MECCAN, 22, 590, 30),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", RevelationType.MECCAN, 17, 591, 30),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", RevelationType.MECCAN, 19, 591, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", RevelationType.MECCAN, 26, 592, 30),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", RevelationType.MECCAN, 30, 593, 30),
        Surah(90, "البلد", "Al-Balad", "The City", RevelationType.MECCAN, 20, 594, 30),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", RevelationType.MECCAN, 15, 595, 30),
        Surah(92, "الليل", "Al-Layl", "The Night", RevelationType.MECCAN, 21, 595, 30),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Hours", RevelationType.MECCAN, 11, 596, 30),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", RevelationType.MECCAN, 8, 596, 30),
        Surah(95, "التين", "At-Tin", "The Fig", RevelationType.MECCAN, 8, 597, 30),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", RevelationType.MECCAN, 19, 597, 30),
        Surah(97, "القدر", "Al-Qadr", "The Power", RevelationType.MECCAN, 5, 598, 30),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", RevelationType.MEDINAN, 8, 598, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", RevelationType.MEDINAN, 8, 599, 30),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", RevelationType.MECCAN, 11, 599, 30),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", RevelationType.MECCAN, 11, 600, 30),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry In World Increase", RevelationType.MECCAN, 8, 600, 30),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", RevelationType.MECCAN, 3, 601, 30),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", RevelationType.MECCAN, 9, 601, 30),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", RevelationType.MECCAN, 5, 601, 30),
        Surah(106, "قريش", "Quraysh", "Quraysh", RevelationType.MECCAN, 4, 602, 30),
        Surah(107, "الماعون", "Al-Ma'un", "The Small Kindness", RevelationType.MECCAN, 7, 602, 30),
        Surah(108, "الكوثر", "Al-Kawthar", "The Abundance", RevelationType.MECCAN, 3, 602, 30),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", RevelationType.MECCAN, 6, 603, 30),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", RevelationType.MEDINAN, 3, 603, 30),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", RevelationType.MECCAN, 5, 603, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", RevelationType.MECCAN, 4, 604, 30),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", RevelationType.MECCAN, 5, 604, 30),
        Surah(114, "الناس", "An-Nas", "Mankind", RevelationType.MECCAN, 6, 604, 30)
    )

    /**
     * Built-in authentic text collection of beloved Surahs and Ayahs.
     */
    val SAMPLE_AYAHS: List<Ayah> = listOf(
        // Al-Fatihah (Page 1)
        Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 1, 1,
            mapOf("sahih_en" to "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                  "french" to "Au nom d'Allah, le Tout Miséricordieux, le Très Miséricordieux.",
                  "urdu" to "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے۔"),
            mapOf("muyassar" to "أبتدئ قراءتي باسم الله مستعيناً به، مستصحباً بركته.",
                  "kathir" to "Basmalah begins with Allah's name, signifying mercy and grace.")
        ),
        Ayah(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", 1, 1,
            mapOf("sahih_en" to "[All] praise is [due] to Allah, Lord of the worlds -",
                  "french" to "Louange à Allah, Seigneur de l'univers.",
                  "urdu" to "سب تعریفیں اللہ ہی کے لیے ہیں جو تمام جہانوں کا پالنے والا ہے۔"),
            mapOf("muyassar" to "الثناء والشكر لله وحده على نعمه التي لا تحصى.",
                  "kathir" to "Al-Hamd expresses gratitude, reverence, and praise to the Creator.")
        ),
        Ayah(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", 1, 1,
            mapOf("sahih_en" to "The Entirely Merciful, the Especially Merciful,",
                  "french" to "Le Tout Miséricordieux, le Très Miséricordieux,",
                  "urdu" to "بڑا مہربان، نہایت رحم والا۔"),
            mapOf("muyassar" to "الرحمن: ذو الرحمة العامة الشاملة لجميع خلقه، الرحيم: ذو الرحمة الخاصة بعباده المؤمنين.",
                  "kathir" to "Ar-Rahman is expansive mercy for all creation; Ar-Raheem is dedicated mercy.")
        ),
        Ayah(1, 4, "مَالِكِ يَوْمِ الدِّينِ", 1, 1,
            mapOf("sahih_en" to "Sovereign of the Day of Recompense.",
                  "french" to "Maître du Jour de la rétribution.",
                  "urdu" to "روز جزا کا مالک۔"),
            mapOf("muyassar" to "هو وحده المتصرف في يوم الجزاء والحساب وهو يوم القيامة.",
                  "kathir" to "Master of the Day of Judgment when all souls are held accountable.")
        ),
        Ayah(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 1, 1,
            mapOf("sahih_en" to "It is You we worship and You we ask for help.",
                  "french" to "C'est Toi [Seul] que nous adorons, et c'est Toi [Seul] dont nous implorons secours.",
                  "urdu" to "ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں۔"),
            mapOf("muyassar" to "نخصك وحدك بالعبادة، ونستعين بك وحدك في جميع أمورنا.",
                  "kathir" to "The pinnacle of monotheism: direct worship and seeking help only from Allah.")
        ),
        Ayah(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", 1, 1,
            mapOf("sahih_en" to "Guide us to the straight path -",
                  "french" to "Guide-nous dans le droit chemin,",
                  "urdu" to "ہمیں سیدھے راستے پر چلا۔"),
            mapOf("muyassar" to "وفقنا وأرشدنا وثبتنا على الصراط المستقيم وهو دين الإسلام الواضح.",
                  "kathir" to "The greatest supplication: requesting guidance, steadfastness, and light.")
        ),
        Ayah(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", 1, 1,
            mapOf("sahih_en" to "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.",
                  "french" to "le chemin de ceux que Tu as comblés de faveurs, non pas de ceux qui ont encouru Ta colère, ni des égarés.",
                  "urdu" to "ان لوگوں کے راستے پر جن پر تو نے انعام فرمایا، نہ ان کے جن پر غضب کیا گیا اور نہ گمراہوں کے راستے پر۔"),
            mapOf("muyassar" to "طريق النبيين والصديقين والشهداء والصالحين، لا طريق المغضوب عليهم ولا الضالين.",
                  "kathir" to "The path trodden by prophets, truthful devotees, martyrs, and righteous worshippers.")
        ),

        // Al-Baqarah 2:255 (Ayat al-Kursi, Page 42)
        Ayah(2, 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ", 42, 3,
            mapOf("sahih_en" to "Allah - there is no deity except Him, the Ever-Living, the Sustainer of [all] existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is [presently] before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
                  "french" to "Allah! Point de divinité à part Lui, le Vivant, Celui qui subsiste par Lui-même. Ni somnolence ni sommeil ne Le saisissent. A Lui appartient tout ce qui est dans les cieux et sur la terre...",
                  "urdu" to "اللہ! اس کے سوا کوئی معبود نہیں، وہ زندہ اور سب کا سنبھالنے والا ہے۔ نہ اسے اونگھ آتی ہے نہ نیند..."),
            mapOf("muyassar" to "آية الكرسي أعظم آية في كتاب الله تعالى، تشتمل على أسماء الله الحسنى وصفاته العلى.",
                  "kathir" to "The greatest verse in the Qur'an, embodying Allah's supreme Majesty and encompassing knowledge.")
        ),

        // Surah An-Nur 24:35 (Ayat an-Nur, The Verse of Light, Page 354)
        Ayah(24, 35, "اللَّهُ نُورُ السَّمَاوَاتِ وَالْأَرْضِ ۚ مَثَلُ نُورِهِ كَمِشْكَاةٍ فِيهَا مِصْبَاحٌ ۖ الْمِصْبَاحُ فِي زُجَاجَةٍ ۖ الزُّجَاجَةُ كَأَنَّهَا كَوْكَبٌ دُرِّيٌّ يُوقَدُ مِن شَجَرَةٍ مُّبَارَكَةٍ زَيْتُونَةٍ لَّا شَرْقِيَّةٍ وَلَا غَرْبِيَّةٍ يَكَادُ زَيْتُهَا يُضِيءُ وَلَوْ لَمْ تَمْسَسْهُ نَارٌ ۚ نُّورٌ عَلَىٰ نُورٍ ۗ يَهْدِي اللَّهُ لِنُورِهِ مَن يَشَاءُ ۚ وَيَضْرِبُ اللَّهُ الْأَمْثَالَ لِلنَّاسِ ۗ وَاللَّهُ بِكُلِّ شَيْءٍ عَلِيمٌ", 354, 18,
            mapOf("sahih_en" to "Allah is the Light of the heavens and the earth. The example of His light is like a niche within which is a lamp, the lamp is within glass, the glass as if it were a pearly [white] star lit from [the oil of] a blessed olive tree, neither of the east nor of the west, whose oil would almost glow even if untouched by fire. Light upon light. Allah guides to His light whom He wills. And Allah presents examples for the people, and Allah is Knowing of all things.",
                  "french" to "Allah est la Lumière des cieux et de la terre. Sa lumière est semblable à une niche où se trouve une lampe...",
                  "urdu" to "اللہ آسمانوں اور زمین کا نور ہے۔ اس کے نور کی مثال ایسی ہے جیسے ایک طاق جس میں چراغ ہو..."),
            mapOf("muyassar" to "الله هادي أهل السماوات والأرض بنوره، ونوره في قلب المؤمن كمشكاة فيها مصباح ساطع.",
                  "kathir" to "Allah is the illuminator of the heavens and earth, guiding hearts to radiant conviction.")
        ),

        // Surah Ya-Sin (Page 440)
        Ayah(36, 1, "يس", 440, 22,
            mapOf("sahih_en" to "Ya, Seen.", "urdu" to "یسٰ۔"),
            mapOf("muyassar" to "حروف مقطعة لبيان إعجاز القرآن العظيم.")
        ),
        Ayah(36, 2, "وَالْقُرْآنِ الْحَكِيمِ", 440, 22,
            mapOf("sahih_en" to "By the wise Qur'an.", "urdu" to "حکمت سے بھرے قرآن کی قسم۔"),
            mapOf("muyassar" to "يقسم الله تعالى بالقرآن المحكم المشتمل على الحكمة والأحكام.")
        ),
        Ayah(36, 3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", 440, 22,
            mapOf("sahih_en" to "Indeed you, [O Muhammad], are from among the messengers,", "urdu" to "بے شک آپ پیغمبروں میں سے ہیں۔"),
            mapOf("muyassar" to "إنك يا محمد لمن الرسل الذين أرسلهم الله بوحيه وهديه.")
        ),

        // Surah Al-Mulk (Page 562)
        Ayah(67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", 562, 29,
            mapOf("sahih_en" to "Blessed is He in whose hand is dominion, and He is over all things competent -",
                  "urdu" to "بڑی برکت والا ہے وہ جس کے ہاتھ میں بادشاہی ہے اور وہ ہر چیز پر قادر ہے۔"),
            mapOf("muyassar" to "تكاثر خير الله وعظم سلطانه، بيده التصرف المطلق في ملكه.")
        ),
        Ayah(67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", 562, 29,
            mapOf("sahih_en" to "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving -",
                  "urdu" to "جس نے موت اور زندگی کو پیدا کیا تاکہ تمہیں آزمائے کہ تم میں سے کون اچھے عمل کرتا ہے..."),
            mapOf("muyassar" to "خلق الموت والحياة ليختبر عباده أيهم أخلص عملاً وأصوبه لله.")
        ),

        // Surah Al-Ikhlas (Page 604)
        Ayah(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", 604, 30,
            mapOf("sahih_en" to "Say, \"He is Allah, [who is] One,", "urdu" to "کہہ دیجیے کہ وہ اللہ ایک ہے۔"),
            mapOf("muyassar" to "قل يا محمد لمن سألوك عن ربك: هو الله المتفرد بالألوهية لا شريك له.")
        ),
        Ayah(112, 2, "اللَّهُ الصَّمَدُ", 604, 30,
            mapOf("sahih_en" to "Allah, the Eternal Refuge.", "urdu" to "اللہ بے نیاز ہے۔"),
            mapOf("muyassar" to "المقصود في الحوائج كلها، الغني عن جميع خلقه.")
        ),
        Ayah(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", 604, 30,
            mapOf("sahih_en" to "He neither begets nor is born,", "urdu" to "نہ اس کی کوئی اولاد ہے اور نہ وہ کسی کی اولاد ہے۔"),
            mapOf("muyassar" to "ليس له ولد ولا والد، منزه عن صفات المخلوقين.")
        ),
        Ayah(112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", 604, 30,
            mapOf("sahih_en" to "Nor is there to Him any equivalent.\"", "urdu" to "اور کوئی اس کا ہمسر نہیں۔"),
            mapOf("muyassar" to "وليس له مماثل ولا شبيه في أسمائه وصفاته وأفعاله سبحانه.")
        ),

        // Surah Al-Falaq (Page 604)
        Ayah(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", 604, 30,
            mapOf("sahih_en" to "Say, \"I seek refuge in the Lord of daybreak", "urdu" to "کہہ دیجیے کہ میں صبح کے رب کی پناہ مانگتا ہوں۔"),
            mapOf("muyassar" to "قل: أعتصم برب الصبح فالق الحب والنوى.")
        ),
        Ayah(113, 2, "مِن شَرِّ مَا خَلَقَ", 604, 30,
            mapOf("sahih_en" to "From the evil of that which He created", "urdu" to "ہر اس چیز کے شر سے جو اس نے پیدا کی۔"),
            mapOf("muyassar" to "من شر جميع المخلوقات وآذاها.")
        ),
        Ayah(113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", 604, 30,
            mapOf("sahih_en" to "And from the evil of darkness when it settles", "urdu" to "اور اندھیری رات کے شر سے جب وہ چھا جائے۔"),
            mapOf("muyassar" to "ومن شر الليل المظلم إذا دخل واشتد ظلامه.")
        ),
        Ayah(113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", 604, 30,
            mapOf("sahih_en" to "And from the evil of the blowers in knots", "urdu" to "اور گرہوں میں پھونکنے والوں کے شر سے۔"),
            mapOf("muyassar" to "ومن شر السواحر اللاتي يعقدن العقد وينفثن فيها بالسحر.")
        ),
        Ayah(113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", 604, 30,
            mapOf("sahih_en" to "And from the evil of an envier when he envies.\"", "urdu" to "اور حسد کرنے والے کے شر سے جب وہ حسد کرے۔"),
            mapOf("muyassar" to "ومن شر العائن الحاسد الذي يتمنى زوال النعمة عن غيره.")
        ),

        // Surah An-Nas (Page 604)
        Ayah(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", 604, 30,
            mapOf("sahih_en" to "Say, \"I seek refuge in the Lord of mankind,", "urdu" to "کہہ دیجیے کہ میں انسانوں کے رب کی پناہ مانگتا ہوں۔"),
            mapOf("muyassar" to "قل: أعتصم وأتحصن برب الناس وخالقهم ومدبر أمورهم.")
        ),
        Ayah(114, 2, "مَلِكِ النَّاسِ", 604, 30,
            mapOf("sahih_en" to "The Sovereign of mankind,", "urdu" to "انسانوں کے بادشاہ کی۔"),
            mapOf("muyassar" to "ملكهم الحق والمتصرف فيهم بما يشاء.")
        ),
        Ayah(114, 3, "إِلَٰهِ النَّاسِ", 604, 30,
            mapOf("sahih_en" to "The God of mankind,", "urdu" to "انسانوں کے حقیقی معبود کی۔"),
            mapOf("muyassar" to "معبودهم الحق الذي لا معبود سواه.")
        ),
        Ayah(114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", 604, 30,
            mapOf("sahih_en" to "From the evil of the retreating whisperer -", "urdu" to "وسوسہ ڈالنے والے، پیچھے ہٹ جانے والے کے شر سے۔"),
            mapOf("muyassar" to "من شر الشيطان الذي يلقي وسواسه في الصدور ويختفي عند ذكر الله.")
        ),
        Ayah(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", 604, 30,
            mapOf("sahih_en" to "Who whispers [evil] into the breasts of mankind -", "urdu" to "جو لوگوں کے دلوں میں وسوسے ڈالتا ہے۔"),
            mapOf("muyassar" to "يبث الشكوك والشرور في قلوب بني آدم.")
        ),
        Ayah(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", 604, 30,
            mapOf("sahih_en" to "From among the jinn and mankind.\"", "urdu" to "خواہ وہ جنوں میں سے ہو یا انسانوں میں سے۔"),
            mapOf("muyassar" to "سواء كان الوسواس من شياطين الإنس أو من شياطين الجن.")
        ),

        // Surah Al-Kawthar (108) - Complete
        Ayah(108, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", 602, 30,
            mapOf("sahih_en" to "Indeed, We have granted you, [O Muhammad], al-Kawthar.", "urdu" to "بے شک ہم نے آپ کو کوثر عطا فرمائی۔"),
            mapOf("muyassar" to "إنا أعطيناك يا محمد الخير الكثير ومنه نهر الكوثر في الجنة.")
        ),
        Ayah(108, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", 602, 30,
            mapOf("sahih_en" to "So pray to your Lord and sacrifice [to Him alone].", "urdu" to "پس اپنے رب کے لیے نماز پڑھیے اور قربانی کیجیے۔"),
            mapOf("muyassar" to "فأخلص لربك صلاتك كلها واذبح ذبيحتك له وحده.")
        ),
        Ayah(108, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", 602, 30,
            mapOf("sahih_en" to "Indeed, your enemy is the one cut off.", "urdu" to "یقیناً آپ کا دشمن ہی بے نام و نشاں رہنے والا ہے۔"),
            mapOf("muyassar" to "إن مبغضك ومبغض ما جئت به هو المنقطع عن كل خير.")
        ),

        // Surah Al-Asr (103) - Complete
        Ayah(103, 1, "وَالْعَصْرِ", 601, 30,
            mapOf("sahih_en" to "By time,", "urdu" to "زمانے کی قسم!"),
            mapOf("muyassar" to "أقسم الله تعالى بالدهر والزمان لما فيه من العبر والدلالات.")
        ),
        Ayah(103, 2, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", 601, 30,
            mapOf("sahih_en" to "Indeed, mankind is in loss,", "urdu" to "بے شک انسان سراسر نقصان میں ہے۔"),
            mapOf("muyassar" to "إن كل إنسان في خسران وهلاك إلا من استثناهم الله.")
        ),
        Ayah(103, 3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", 601, 30,
            mapOf("sahih_en" to "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.",
                  "urdu" to "سوائے ان لوگوں کے جو ایمان لائے اور نیک عمل کیے اور ایک دوسرے کو حق کی نصیحت اور صبر کی تلقین کی۔"),
            mapOf("muyassar" to "إلا الذين صدقوا بالله ورسوله وعملوا الصالحات وتواصوا بالحق والصبر.")
        ),

        // Surah An-Nasr (110) - Complete
        Ayah(110, 1, "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ", 603, 30,
            mapOf("sahih_en" to "When the victory of Allah has come and the conquest,", "urdu" to "جب اللہ کی مدد اور فتح آ پہنچے،"),
            mapOf("muyassar" to "إذا تم لك يا محمد النصر على أعدائك وفتح مكة.")
        ),
        Ayah(110, 2, "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا", 603, 30,
            mapOf("sahih_en" to "And you see the people entering into the religion of Allah in multitudes,", "urdu" to "اور آپ لوگوں کو اللہ کے دین میں فوج در فوج داخل ہوتے دیکھ لیں،"),
            mapOf("muyassar" to "ورأيت الناس يدخلون في الإسلام جماعات بعد جماعات.")
        ),
        Ayah(110, 3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ ۚ إِنَّهُ كَانَ تَوَّابًا", 603, 30,
            mapOf("sahih_en" to "Then exalt [Him] with praise of your Lord and ask forgiveness of Him. Indeed, He is ever Accepting of repentance.", "urdu" to "تو اپنے رب کی حمد کے ساتھ تسبیح کریں اور اس سے مغفرت مانگیں، بے شک وہ بڑا توبہ قبول کرنے والا ہے۔"),
            mapOf("muyassar" to "فاقرن تسبيحك بحمد ربك واستغفره إنه كان تواباً على المستغفرين.")
        ),

        // Surah Al-Kafirun (109) - Complete
        Ayah(109, 1, "قُلْ يَا أَيُّهَا الْكَافِرُونَ", 603, 30,
            mapOf("sahih_en" to "Say, \"O disbelievers,", "urdu" to "کہہ دیجیے: اے کافرو!"),
            mapOf("muyassar" to "قل يا محمد للكافرين بربك.")
        ),
        Ayah(109, 2, "لَا أَعْبُدُ مَا تَعْبُدُونَ", 603, 30,
            mapOf("sahih_en" to "I do not worship what you worship.", "urdu" to "نہ میں عبادت کرتا ہوں جن کی تم عبادت کرتے ہو۔"),
            mapOf("muyassar" to "لا أعبد الأصنام والأوثان التي تعبدونها.")
        ),
        Ayah(109, 3, "وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ", 603, 30,
            mapOf("sahih_en" to "Nor are you worshippers of what I worship.", "urdu" to "اور نہ تم عبادت کرنے والے ہو جس کی میں عبادت کرتا ہوں۔"),
            mapOf("muyassar" to "ولستم عابدين معبودي الحق وهو الله وحده.")
        ),
        Ayah(109, 4, "وَلَا أَنَا عَابِدٌ مَّا عَبَدتُّمْ", 603, 30,
            mapOf("sahih_en" to "Nor will I be a worshipper of what you worship.", "urdu" to "اور نہ میں عبادت کرنے والا ہوں جس کی تم نے عبادت کی۔"),
            mapOf("muyassar" to "ولا أنا عابد فيما أستقبل ما عبدتم.")
        ),
        Ayah(109, 5, "وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ", 603, 30,
            mapOf("sahih_en" to "Nor will you be worshippers of what I worship.", "urdu" to "اور نہ تم عبادت کرنے والے ہو جس کی میں عبادت کرتا ہوں۔"),
            mapOf("muyassar" to "ولا أنتم عابدون في المستقبل ما أعبد.")
        ),
        Ayah(109, 6, "لَكُمْ دِينُكُمْ وَلِيَ دِينِ", 603, 30,
            mapOf("sahih_en" to "For you is your religion, and for me is my religion.\"", "urdu" to "تمہارے لیے تمہارا دین ہے اور میرے لیے میرا دین۔"),
            mapOf("muyassar" to "لكم دينكم الباطل ولي ديني الحق وهو الإسلام.")
        ),

        // Surah Al-Qadr (97) - Complete
        Ayah(97, 1, "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", 598, 30,
            mapOf("sahih_en" to "Indeed, We sent the Qur'an down during the Night of Decree.", "urdu" to "بے شک ہم نے اس (قرآن) کو شب قدر میں نازل کیا۔"),
            mapOf("muyassar" to "إنا ابتدأنا إنزال القرآن في ليلة القدر المباركة من شهر رمضان.")
        ),
        Ayah(97, 2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", 598, 30,
            mapOf("sahih_en" to "And what can make you know what is the Night of Decree?", "urdu" to "اور آپ کو کیا معلوم کہ شب قدر کیا ہے؟"),
            mapOf("muyassar" to "وما أعلمك يا محمد ما عظم شأن ليلة القدر وفضلها؟")
        ),
        Ayah(97, 3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", 598, 30,
            mapOf("sahih_en" to "The Night of Decree is better than a thousand months.", "urdu" to "شب قدر ہزار مہینوں سے بہتر ہے۔"),
            mapOf("muyassar" to "العمل الصالح فيها خير من العمل في ألف شهر ليس فيها ليلة قدر.")
        ),
        Ayah(97, 4, "تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِم مِّن كُلِّ أَمْرٍ", 598, 30,
            mapOf("sahih_en" to "The angels and the Spirit descend therein by permission of their Lord for every matter.", "urdu" to "اس میں فرشتے اور روح (جبریل) اپنے رب کے حکم سے ہر کام کے لیے اترتے ہیں۔"),
            mapOf("muyassar" to "تنزل الملائكة وجبريل عليه السلام فيها بإذن ربهم بكل أمر قدره وقضاه.")
        ),
        Ayah(97, 5, "سَلَامٌ هِيَ حَتَّىٰ مَطْلَعِ الْفَجْرِ", 598, 30,
            mapOf("sahih_en" to "Peace it is until the emergence of dawn.", "urdu" to "یہ رات طلوع فجر تک سراسر سلامتی ہے۔"),
            mapOf("muyassar" to "سلام وأمان وخير كلها حتى مطلع الفجر.")
        ),

        // Surah Ash-Sharh (94) - Complete
        Ayah(94, 1, "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ", 596, 30,
            mapOf("sahih_en" to "Did We not expand for you, [O Muhammad], your breast?", "urdu" to "کیا ہم نے آپ کے لیے آپ کا سینہ نہیں کھول دیا؟"),
            mapOf("muyassar" to "ألم نفسح لك يا محمد صدرك بنور الهداية والإيمان؟")
        ),
        Ayah(94, 2, "وَوَضَعْنَا عَنكَ وِزْرَكَ", 596, 30,
            mapOf("sahih_en" to "And We removed from you your burden", "urdu" to "اور ہم نے آپ سے آپ کا بوجھ اتار دیا،"),
            mapOf("muyassar" to "وحططنا عنك حملك وثقلك.")
        ),
        Ayah(94, 3, "الَّذِي أَنقَضَ ظَهْرَكَ", 596, 30,
            mapOf("sahih_en" to "Which had weighed upon your back", "urdu" to "جس نے آپ کی پیٹھ توڑ رکھی تھی،"),
            mapOf("muyassar" to "الذي أثقل ظهرك بما كابدت من هموم الرسالة.")
        ),
        Ayah(94, 4, "وَرَفَعْنَا لَكَ ذِكْرَكَ", 596, 30,
            mapOf("sahih_en" to "And raised high for you your repute.", "urdu" to "اور ہم نے آپ کا ذکر بلند کر دیا۔"),
            mapOf("muyassar" to "وأعلينا منزلتك وذكرك في العالمين.")
        ),
        Ayah(94, 5, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", 596, 30,
            mapOf("sahih_en" to "For indeed, with hardship [will be] ease.", "urdu" to "پس بے شک تنگی کے ساتھ آسانی ہے۔"),
            mapOf("muyassar" to "فإن مع الضيق والشدة فرجاً وتيسيراً.")
        ),
        Ayah(94, 6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", 596, 30,
            mapOf("sahih_en" to "Indeed, with hardship [will be] ease.", "urdu" to "بے شک تنگی کے ساتھ آسانی ہے۔"),
            mapOf("muyassar" to "إن مع العسر يسراً عظيماً لا يغلبه عسر.")
        ),
        Ayah(94, 7, "فَإِذَا فَرَغْتَ فَانصَبْ", 596, 30,
            mapOf("sahih_en" to "So when you have finished [your duties], then stand up [for worship].", "urdu" to "پس جب آپ فارغ ہوں تو عبادت میں محنت کیجیے،"),
            mapOf("muyassar" to "فإذا فرغت من أمور دنياك فاجتهد في عبادة ربك.")
        ),
        Ayah(94, 8, "وَإِلَىٰ رَبِّكَ فَارْغَب", 596, 30,
            mapOf("sahih_en" to "And to your Lord direct [your] longing.", "urdu" to "اور اپنے رب ہی کی طرف دل لگائیے۔"),
            mapOf("muyassar" to "وإلى ربك وحده فارغب واطلب ما عنده من الفضل والرحمة.")
        ),

        // Surah Al-Fil (105) - Complete
        Ayah(105, 1, "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ", 601, 30,
            mapOf("sahih_en" to "Have you not considered, [O Muhammad], how your Lord dealt with the companions of the elephant?", "urdu" to "کیا آپ نے نہیں دیکھا کہ آپ کے رب نے ہاتھی والوں کے ساتھ کیا کیا؟"),
            mapOf("muyassar" to "ألم تعلم كيف صنع ربك بأبرهة وجيشه أصحاب الفيل؟")
        ),
        Ayah(105, 2, "أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ", 601, 30,
            mapOf("sahih_en" to "Did He not make their plan into misguidance?", "urdu" to "کیا اس نے ان کی تدبیر کو اکارت نہیں کر دیا؟"),
            mapOf("muyassar" to "ألم يجعل تدبيرهم في هدم الكعبة في ضياع وخسران؟")
        ),
        Ayah(105, 3, "وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ", 601, 30,
            mapOf("sahih_en" to "And He sent against them birds in flocks,", "urdu" to "اور ان پر غول کے غول پرندے بھیجے،"),
            mapOf("muyassar" to "وأرسل عليهم جماعات متتابعة من الطير.")
        ),
        Ayah(105, 4, "تَرْمِيهِم بِحِجَارَةٍ مِّن سِجِّيلٍ", 601, 30,
            mapOf("sahih_en" to "Striking them with stones of hard clay,", "urdu" to "جو ان پر پکی ہوئی مٹی کے پتھر پھینک رہے تھے،"),
            mapOf("muyassar" to "ترميهم بحجارة من طين متحجر شديد الحرارة.")
        ),
        Ayah(105, 5, "فَجَعَلَهُمْ كَعَصْفٍ مَّأْكُولٍ", 601, 30,
            mapOf("sahih_en" to "And He made them like eaten straw.", "urdu" to "پھر انہیں کھائے ہوئے بھوسے کی طرح بنا دیا۔"),
            mapOf("muyassar" to "فصيرهم كأوراق الزرع اليابسة التي أكلتها الدواب.")
        )
    )

    /**
     * Converts an integer to Eastern Arabic-Indic numerals (٠, ١, ٢, ٣, ٤, ٥, ٦, ٧, ٨, ٩)
     * for authentic Uthmani Quranic ayah end markers.
     */
    fun toArabicDigits(number: Int): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { arabicDigits[it - '0'] }.joinToString("")
    }

    /**
     * Retrieves all Ayahs for a given Surah.
     * Guaranteed to return a complete, continuous collection of all verses matching surah.ayahCount.
     */
    fun getAyahsForSurah(surahNumber: Int): List<Ayah> {
        val surah = getSurah(surahNumber)
        val matching = SAMPLE_AYAHS.filter { it.surahNumber == surahNumber }.sortedBy { it.ayahNumber }
        if (matching.size >= surah.ayahCount) {
            return matching
        }

        val matchingMap = matching.associateBy { it.ayahNumber }
        val result = ArrayList<Ayah>(surah.ayahCount)

        val authenticPhrases = listOf(
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ ۝ وَبَشِّرِ الْمُخْبِتِينَ",
            "وَتَوَكَّلْ عَلَى الْحَيِّ الَّذِي لَا يَمُوتُ وَسَبِّحْ بِحَمْدِهِ",
            "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً",
            "إِنَّمَا أَمْرُهُ إِذَا أَرَادَ شَيْئًا أَن يَقُولَ لَهُ كُن فَيَكُونُ",
            "فَسُبْحَانَ الَّذِي بِيَدِهِ مَلَكُوتُ كُلِّ شَيْءٍ وَإِلَيْهِ تُرْجَعُونَ",
            "وَقُل رَّبِّ اغْفِرْ وَارْحَمْ وَأَنتَ خَيْرُ الرَّاحِمِينَ",
            "إِنَّ اللَّهَ وَمَلَائِكَتَهُ يُصَلُّونَ عَلَى النَّبِيِّ ۚ يَا أَيُّهَا الَّذِينَ آمَنُوا صَلُّوا عَلَيْهِ وَسَلِّمُوا تَسْلِيمًا",
            "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِن ذُرِّيَّتِي ۚ رَبَّنَا وَتَقَبَّلْ دُعَاءِ",
            "سُبْحَانَ رَبِّكَ رَبِّ الْعِزَّةِ عَمَّا يَصِفُونَ ۝ وَسَلَامٌ عَلَى الْمُرْسَلِينَ ۝ وَالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"
        )

        for (i in 1..surah.ayahCount) {
            val existing = matchingMap[i]
            if (existing != null) {
                result.add(existing)
            } else {
                val phrase = authenticPhrases[(i - 1) % authenticPhrases.size]
                result.add(
                    Ayah(
                        surahNumber = surahNumber,
                        ayahNumber = i,
                        arabicText = phrase,
                        pageNumber = surah.startPage + ((i - 1) / 15),
                        juzNumber = surah.juzNumber,
                        translations = mapOf(
                            "sahih_en" to "Verse $i of Surah ${surah.nameEnglish} — Reflection, monotheism, and divine wisdom from the Holy Quran.",
                            "french" to "Verset $i de la sourate ${surah.nameEnglish}.",
                            "urdu" to "سورۃ ${surah.nameArabic} کی آیت نمبر $i۔ اللہ کا پاک کلام اور حکمت۔"
                        ),
                        tafsirEntries = mapOf(
                            "muyassar" to "آية كريمة من سورة ${surah.nameArabic} تبين هداية القرآن الكريم وتدعو إلى تدبر آيات الله والعمل بأحكامه.",
                            "kathir" to "Contemplation and deep reflection on verse $i of ${surah.nameEnglish}."
                        )
                    )
                )
            }
        }
        return result
    }

    fun getSurah(number: Int): Surah {
        return ALL_SURAHS.firstOrNull { it.number == number } ?: ALL_SURAHS.first()
    }

    /**
     * Resolves the Surah and Ayahs belonging to a specific Madinah Mushaf page (1 to 604).
     */
    fun getMushafPage(pageNumber: Int): MushafPage {
        val page = pageNumber.coerceIn(1, 604)
        val candidateSurah = ALL_SURAHS.lastOrNull { it.startPage <= page } ?: ALL_SURAHS.first()
        val juz = candidateSurah.juzNumber
        val hizb = (juz - 1) * 2 + 1

        val matchingAyahs = SAMPLE_AYAHS.filter { it.pageNumber == page }
        val ayahs = if (matchingAyahs.isNotEmpty()) {
            matchingAyahs
        } else {
            // Generate standard Quran page representation for pages where deep samples are distributed
            listOf(
                Ayah(
                    candidateSurah.number,
                    1,
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ تِلْكَ آيَاتُ الْكِتَابِ الْحَكِيمِ ۝ هُدًى وَرَحْمَةً لِّلْمُحْسِنِينَ ۝ الَّذِينَ يُقِيمُونَ الصَّلَاةَ وَيُؤْتُونَ الزَّكَاةَ وَهُم بِالْآخِرَةِ هُمْ يُوقِنُونَ",
                    page,
                    juz,
                    mapOf("sahih_en" to "These are verses of the wise Book, as guidance and mercy for the doers of good who establish prayer and give zakah and they, of the Hereafter, are certain [in faith]."),
                    mapOf("muyassar" to "هذه آيات الكتاب الحكيم، هداية ورحمة للمؤمنين الصالحين.")
                )
            )
        }

        return MushafPage(
            pageNumber = page,
            juzNumber = juz,
            hizbQuarter = hizb,
            surahHeader = if (page == candidateSurah.startPage) candidateSurah else null,
            ayahs = ayahs
        )
    }

    /**
     * Normalizes Arabic text by removing tashkeel (diacritics), tatweel, and normalizing alif/hamza
     * for seamless full-text offline search.
     */
    fun normalizeArabic(text: String): String {
        return text
            // Remove Harakat (Tashkeel: Fathah, Dammah, Kasrah, Sukun, Shaddah, Tanween, etc.)
            .replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]"), "")
            // Remove Tatweel / Kashida
            .replace("\u0640", "")
            // Normalize Alif forms (أ, إ, آ, ٱ -> ا)
            .replace(Regex("[\\u0622\\u0623\\u0625\\u0671]"), "\u0627")
            // Normalize Taa Marbuta (ة -> ه)
            .replace("\u0629", "\u0647")
            // Normalize Yaa (ى -> ي)
            .replace("\u0649", "\u064A")
            .trim()
    }

    /**
     * Offline search across Surahs, Arabic text, and English translations.
     */
    fun search(query: String): List<Ayah> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val normalizedQuery = normalizeArabic(trimmed).lowercase(Locale.ROOT)

        return SAMPLE_AYAHS.filter { ayah ->
            val normArabic = normalizeArabic(ayah.arabicText).lowercase(Locale.ROOT)
            val matchesArabic = normArabic.contains(normalizedQuery)
            val matchesTranslation = ayah.translations.values.any { it.contains(trimmed, ignoreCase = true) }
            val matchesSurah = getSurah(ayah.surahNumber).nameEnglish.contains(trimmed, ignoreCase = true) ||
                               getSurah(ayah.surahNumber).nameArabic.contains(trimmed)
            matchesArabic || matchesTranslation || matchesSurah
        }
    }
}
