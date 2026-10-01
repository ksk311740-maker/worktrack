package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AnimatedNumberText
import com.example.ui.theme.PrimaryIndigo
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.random.Random

enum class RevealPhase {
    GLITCH_TRIGGER,
    REVEALED
}

@Composable
fun GlitchMoneyRevealDialog(
    totalEarned: Double,
    thisMonthEarned: Double,
    totalMinutes: Double,
    avgPerProject: Double,
    currencySymbol: String,
    reducedMotion: Boolean,
    onDismiss: () -> Unit
) {
    var phase by remember {
        mutableStateOf(if (reducedMotion) RevealPhase.REVEALED else RevealPhase.GLITCH_TRIGGER)
    }

    var glitchSeed by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            // Rapid glitch flicker for 380ms
            for (i in 0 until 7) {
                glitchSeed = Random.nextInt(1000)
                delay(55)
            }
            phase = RevealPhase.REVEALED
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .testTag("glitch_reveal_dialog"),
            contentAlignment = Alignment.Center
        ) {
            if (phase == RevealPhase.GLITCH_TRIGGER) {
                // Digital Glitch Canvas Effect
                GlitchEffectCanvas(glitchSeed = glitchSeed)
            } else {
                // Reveal Content Card
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.92f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                ) {
                    RevealContentCard(
                        totalEarned = totalEarned,
                        thisMonthEarned = thisMonthEarned,
                        totalMinutes = totalMinutes,
                        avgPerProject = avgPerProject,
                        currencySymbol = currencySymbol,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun GlitchEffectCanvas(glitchSeed: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Scanline sweep
        val lineSpacing = 8f
        var y = 0f
        while (y < h) {
            drawLine(
                color = Color.Cyan.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.5f
            )
            y += lineSpacing
        }

        // Random horizontal glitch slice blocks
        val random = Random(glitchSeed)
        val sliceCount = 8
        for (i in 0 until sliceCount) {
            val sliceY = random.nextFloat() * h
            val sliceH = random.nextFloat() * 24f + 8f
            val offsetX = (random.nextFloat() - 0.5f) * 60f
            val color = if (i % 2 == 0) AccentCyan.copy(alpha = 0.55f) else AccentRose.copy(alpha = 0.55f)

            drawRect(
                color = color,
                topLeft = Offset(offsetX, sliceY),
                size = Size(w + 100f, sliceH)
            )
        }
    }
}

@Composable
private fun RevealContentCard(
    totalEarned: Double,
    thisMonthEarned: Double,
    totalMinutes: Double,
    avgPerProject: Double,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(AccentEmerald, AccentCyan, PrimaryIndigo)),
                RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A) // Sleek dark slate
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Lock icon + Dismiss button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentEmerald.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EARNINGS REVEAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = AccentEmerald
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "TOTAL COMPLETED EARNINGS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main Giant Counter Animation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currencySymbol,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = AccentEmerald,
                    modifier = Modifier.padding(end = 4.dp)
                )

                AnimatedNumberText(
                    targetValue = totalEarned,
                    decimals = if (totalEarned % 1.0 == 0.0) 0 else 2,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Text(
                text = "Calculated from ${if (totalMinutes % 1.0 == 0.0) totalMinutes.toInt() else totalMinutes} video minutes completed",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Breakdown Grid in dark frosted container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.06f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // This Month
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "THIS MONTH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.0f", thisMonthEarned)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentCyan
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    // Average per Project
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AVG / VIDEO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.0f", avgPerProject)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Return to Work-Focused Dashboard Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dismiss_reveal_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentEmerald,
                    contentColor = Color(0xFF0F172A)
                )
            ) {
                Text(
                    text = "Return to Work Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
