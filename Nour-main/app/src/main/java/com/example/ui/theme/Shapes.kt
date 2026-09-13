package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Shape Scale
 * Characterized by distinctive, expressive squircle and capsule contours,
 * following the official M3E shape tokens:
 * xsmall 4 / small 8 / medium 12 / large 16 / xlarge 28 / xxlarge 48.
 */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)