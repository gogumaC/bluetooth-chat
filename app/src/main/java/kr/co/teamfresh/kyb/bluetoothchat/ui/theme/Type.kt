package kr.co.teamfresh.kyb.bluetoothchat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kr.co.teamfresh.kyb.bluetoothchat.R

val jamsil=FontFamily(
    Font(R.font.the_jamsil_light, weight = FontWeight(100)),
    Font(R.font.the_jamsil_thin, weight = FontWeight(300)),
    Font(R.font.the_jamsil_regular, weight = FontWeight(400)),
    Font(R.font.the_jamsil_medium,weight=FontWeight(500)),
    Font(R.font.the_jamsil_bold,weight=FontWeight(700)),
    Font(R.font.the_jamsil_extrabold,weight=FontWeight(800))
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = jamsil,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

