package com.legalpathways.ai.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.legalpathways.ai.ui.theme.*

// ── Layer card data ───────────────────────────────────────────────────────────
data class LayerCard(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val route: String,
    val tint: Color
)

val layerCards = listOf(
    LayerCard(0, "Positioning",   Icons.Default.Explore,          "layer0",  NavyLight),
    LayerCard(1, "Eligibility",   Icons.Default.CheckCircle,      "layer1",  EmeraldAccent),
    LayerCard(2, "Compliance",    Icons.Default.VerifiedUser,     "layer2",  NavyMid),
    LayerCard(3, "Maintenance",   Icons.Default.AccountBalance,   "layer3",  GoldDark),
    LayerCard(4, "Severity",      Icons.Default.Warning,          "layer4",  CrimsonAccent),
    LayerCard(5, "Settlement",    Icons.Default.Handshake,        "layer5",  EmeraldAccent),
    LayerCard(6, "Litigation",    Icons.Default.Gavel,            "layer6",  NavyMid),
    LayerCard(7, "Interim",       Icons.Default.HourglassTop,     "layer7",  GoldPrimary),
    LayerCard(8, "Trial",         Icons.Default.CalendarToday,    "layer8",  NavyLight),
    LayerCard(9, "Appeal",        Icons.Default.Campaign,         "layer9",  CrimsonAccent),
    LayerCard(10,"Post-Divorce",  Icons.Default.WbSunny,          "layer10", EmeraldAccent)
)

// ── Screen ────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val headerAnim  = remember { Animatable(0f) }
    LaunchedEffect(Unit) { headerAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyDeep, NavyMid, ParchmentLight),
                    startY = 0f, endY = 900f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── Hero header ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Decorative circles
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(GoldPrimary.copy(alpha = 0.07f), 180.dp.toPx(), Offset(size.width * 0.85f, size.height * 0.2f))
                    drawCircle(GoldPrimary.copy(alpha = 0.05f), 120.dp.toPx(), Offset(size.width * 0.1f, size.height * 0.8f))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .graphicsLayer {
                            alpha       = headerAnim.value
                            translationY = (1f - headerAnim.value) * 40f
                        },
                    verticalArrangement   = Arrangement.Center,
                    horizontalAlignment   = Alignment.CenterHorizontally
                ) {
                    // Scales icon
                    Surface(
                        shape = CircleShape,
                        color = GoldPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Gavel, null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Legal Pathways AI",
                        style     = MaterialTheme.typography.displayMedium,
                        color     = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Know where you stand before making any legal move",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = SlateGray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Content card ──────────────────────────────────────────────────
            Surface(
                modifier      = Modifier.fillMaxSize(),
                shape         = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color         = ParchmentLight,
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Spacer(Modifier.height(4.dp))

                    // Phase grid label
                    Text(
                        "LEGAL PHASES",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = SlateGray,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(bottom = 14.dp)
                    )

                    // ── Layer grid ────────────────────────────────────────────
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement   = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(360.dp) // ~4 rows
                    ) {
                        items(layerCards) { card ->
                            LayerGridCard(card = card, onClick = { onNavigate(card.route) })
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Roadmap button ────────────────────────────────────────
                    Button(
                        onClick  = { onNavigate("roadmap") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyMid,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Map, null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "View Divorce Roadmap + AI Chat",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Counselor button ──────────────────────────────────────
                    OutlinedButton(
                        onClick  = { onNavigate("counselor") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldAccent),
                        border = BorderStroke(1.5.dp, EmeraldAccent)
                    ) {
                        Icon(Icons.Default.Favorite, null, modifier = Modifier.size(20.dp), tint = EmeraldAccent)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Emotional Support Counselor",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Disclaimer
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldPrimary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "AI-powered legal guidance. Not a substitute for professional legal advice.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GoldDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Grid card ─────────────────────────────────────────────────────────────────
@Composable
fun LayerGridCard(card: LayerCard, onClick: () -> Unit) {
    val scale = remember { Animatable(0.8f) }
    LaunchedEffect(card.index) {
        kotlinx.coroutines.delay(card.index * 40L)
        scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
    }

    Card(
        onClick   = onClick,
        modifier  = Modifier
            .aspectRatio(1f)
            .scale(scale.value),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = card.tint.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(card.icon, null, tint = card.tint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text      = card.label,
                style     = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color     = NavyDeep,  // ✅ EXPLICIT DARK COLOR FOR CONTRAST
                textAlign = TextAlign.Center
            )
        }
    }
}