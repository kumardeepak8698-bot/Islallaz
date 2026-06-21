package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WelcomeOnboardingScreen(
    onNavigateToSetup: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    isAlreadyConfigured: Boolean
) {
    val steps = listOf(
        OnboardingStep(
            title = "Welcome to Island",
            description = "A powerful, secure sandbox environment to clone, isolate, and control your apps using Android Enterprise technology.",
            icon = Icons.Filled.Layers,
            accentColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingStep(
            title = "Isolate Core Data",
            description = "Selected apps run in an isolated secondary work profile. They cannot access your personal photos, files, contacts, or call logs.",
            icon = Icons.Filled.Lock,
            accentColor = MaterialTheme.colorScheme.secondary
        ),
        OnboardingStep(
            title = "Freeze Background drain",
            description = "Freeze cloned apps completely with one tap. This turns off their background services, notification alarms, and battery drain.",
            icon = Icons.Filled.AcUnit,
            accentColor = MaterialTheme.colorScheme.tertiary
        )
    )

    var currentStepIdx by remember { mutableStateOf(0) }
    val currentStep = steps[currentStepIdx]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Unsolicited visual branding top row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ISLAND",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                
                TextButton(
                    onClick = {
                        if (isAlreadyConfigured) onNavigateToDashboard() else onNavigateToSetup()
                    },
                    modifier = Modifier.testTag("skip_button")
                ) {
                    Text("Skip")
                }
            }

            // Animated content middle section
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (fadeIn()).with(fadeOut())
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Accent circular badge for the icon
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(step.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = step.title,
                            tint = step.accentColor,
                            modifier = Modifier.size(72.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Bottom control navigation area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    steps.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (idx == currentStepIdx) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == currentStepIdx) currentStep.accentColor
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Action buttons
                Button(
                    onClick = {
                        if (currentStepIdx < steps.size - 1) {
                            currentStepIdx++
                        } else {
                            if (isAlreadyConfigured) onNavigateToDashboard() else onNavigateToSetup()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentStepIdx == steps.size - 1) "Get Started" else "Continue",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next Arrow"
                        )
                    }
                }
            }
        }
    }
}
