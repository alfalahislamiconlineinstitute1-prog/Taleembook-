package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserRole
import com.example.ui.components.InstituteLogoImage
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(customLogoPath: String?) {
    var startAnim by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showTag by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnim = true
        delay(850L)
        showTitle = true
        delay(550L)
        showTag = true
    }

    val ringScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.8f,
        animationSpec = tween(1600, easing = FastOutSlowInEasing),
        label = "ring_scale"
    )
    val ringRotation by animateFloatAsState(
        targetValue = if (startAnim) 20f else 0f,
        animationSpec = tween(1600, easing = FastOutSlowInEasing),
        label = "ring_rot"
    )
    val ringAlpha by animateFloatAsState(
        targetValue = if (startAnim) 0.28f else 0f,
        animationSpec = tween(1600),
        label = "ring_alpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.6f,
        animationSpec = tween(1000, delayMillis = 250, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(900, delayMillis = 250),
        label = "logo_alpha"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(800),
        label = "title_alpha"
    )
    val titleOffsetY by animateDpAsState(
        targetValue = if (showTitle) 0.dp else 10.dp,
        animationSpec = tween(800),
        label = "title_offset"
    )

    val tagAlpha by animateFloatAsState(
        targetValue = if (showTag) 1f else 0f,
        animationSpec = tween(800),
        label = "tag_alpha"
    )

    val loaderProgress by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(2200, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "loader_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Purple600, Purple900, Purple850),
                    center = Offset(540f, 380f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        listOf(400.dp, 520.dp, 640.dp).forEach { ringSize ->
            Box(
                modifier = Modifier
                    .size(ringSize)
                    .scale(ringScale)
                    .rotate(ringRotation)
                    .alpha(ringAlpha)
                    .border(1.dp, Gold500.copy(alpha = 0.65f), CircleShape)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .size(124.dp)
                    .clip(CircleShape)
                    .background(PureWhite.copy(alpha = 0.08f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .shadow(24.dp, CircleShape, ambientColor = Gold500, spotColor = Gold500)
                        .clip(CircleShape)
                        .background(PureWhite)
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InstituteLogoImage(customLogoPath = customLogoPath, size = 102.dp)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = titleOffsetY)
                    .alpha(titleAlpha)
            ) {
                Text(
                    text = "${stringResource(R.string.institute_upper_line1)}\n${stringResource(R.string.institute_upper_line2)}",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.institute_urdu_name),
                    color = Gold400,
                    fontSize = 18.sp,
                    fontFamily = AmiriFontFamily,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.institute_tagline),
                color = PureWhite.copy(alpha = 0.78f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(tagAlpha)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 44.dp)
                .width(120.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(PureWhite.copy(alpha = 0.16f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.42f)
                    .offset(x = (120 * (loaderProgress * 1.4f - 0.4f)).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Gold400)
            )
        }
    }
}

@Composable
fun AuthScreen(
    customLogoPath: String?,
    selectedRole: UserRole,
    isLoading: Boolean,
    errorMessage: String?,
    onRoleChange: (UserRole) -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val colors = AlFalahTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        // Purple hero
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Purple600, Purple900),
                        center = Offset(540f, 80f),
                        radius = 900f
                    )
                )
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 34.dp, bottom = 60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(16.dp, CircleShape, ambientColor = Gold500, spotColor = Gold500)
                    .clip(CircleShape)
                    .background(PureWhite)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                InstituteLogoImage(customLogoPath = customLogoPath, size = 64.dp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.institute_full_name),
                color = PureWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.institute_urdu_name),
                color = Gold400,
                fontSize = 16.sp,
                fontFamily = AmiriFontFamily,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.institute_tagline),
                color = PureWhite.copy(alpha = 0.78f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Overlapping Google Sign-In Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .offset(y = (-34).dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign In to Continue",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sign in securely with your Google account to sync your courses, assignments, books, and Gemini AI Study Tutor across devices.",
                    color = colors.textMuted,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Workspace mode selector (Student / Teacher / Admin)
                Text(
                    text = "SELECT WORKSPACE VIEW",
                    color = colors.textMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) DarkCard else colors.goldTint)
                        .border(
                            width = if (colors.isDark) 1.dp else 0.dp,
                            color = if (colors.isDark) colors.line else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp)
                ) {
                    UserRole.entries.forEach { role ->
                        AuthSegmentButton(
                            text = role.label,
                            active = selectedRole == role,
                            testTag = "workspace_role_${role.key}",
                            modifier = Modifier.weight(1f),
                            onClick = { onRoleChange(role) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!errorMessage.isNullOrBlank()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.dangerText.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, colors.dangerText.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("auth_error_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "Sign-in error",
                                tint = colors.dangerText,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errorMessage,
                                color = colors.dangerText,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Primary Google Sign-In button
                Button(
                    onClick = onGoogleSignInClick,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple900,
                        contentColor = PureWhite,
                        disabledContainerColor = Purple900.copy(alpha = 0.6f)
                    ),
                    contentPadding = PaddingValues(vertical = 15.dp, horizontal = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("google_sign_in_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Gold400
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Signing in with Google...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PureWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                color = Purple900,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = colors.line)
                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AuthFeatureRow(
                        icon = Icons.Outlined.CloudDone,
                        title = "Real-time Firebase Cloud Sync",
                        subtitle = "Your progress, books, assignments & hadith stay saved securely."
                    )
                    AuthFeatureRow(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "Gemini AI Study Tutor",
                        subtitle = "Ask questions on Tajweed, Duas, Salah & Daily Hadith anytime."
                    )
                    AuthFeatureRow(
                        icon = Icons.Outlined.VerifiedUser,
                        title = "Private & Protected",
                        subtitle = "Secured with Google Sign-In and Firebase App Check."
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val colors = AlFalahTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (colors.isDark) DarkCard else colors.goldTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Gold500,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = colors.textMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun AuthSegmentButton(
    text: String,
    active: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = AlFalahTheme.colors
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 42.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) Purple900 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (active) PureWhite else colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
