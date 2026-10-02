package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.auth.SupabaseAuthService
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Supabase-Integrated Authentication Screen
 * Supports:
 * - 60 FPS buttery smooth transitions
 * - Existing user password login (one-time credentials check with session persistence)
 * - New user registration with Supabase Auth -> sends 6-digit email OTP
 * - OTP Verification View with 6-digit input and email arrival simulation
 */
@Composable
fun AuthScreen(
    supabaseAuth: SupabaseAuthService,
    onLoginSuccess: (email: String, name: String) -> Unit,
    onStartOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 0: Sign In (Existing User), 1: Register (New User), 2: OTP Verification
    var authStep by remember { mutableIntStateOf(0) }

    var email by remember { mutableStateOf("priyan1436ei@gmail.com") }
    var password by remember { mutableStateOf("••••••••") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("Priyan") }
    var enteredOtp by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resendCooldown by remember { mutableIntStateOf(45) }

    val pendingEmail by supabaseAuth.pendingEmail.collectAsState()
    val incomingEmailNotice by supabaseAuth.lastSimulatedEmailNotification.collectAsState()

    fun triggerHaptic(ms: Long = 35) {
        try {
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(ms)
                }
            }
        } catch (_: Exception) {}
    }

    // Resend countdown timer
    LaunchedEffect(authStep) {
        if (authStep == 2) {
            resendCooldown = 45
            while (resendCooldown > 0) {
                delay(1000)
                resendCooldown--
            }
        }
    }

    AmbientLiquidMeshBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    // App Header & Branding
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x33191922))
                                    .border(1.2.dp, ElectricLime, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ElectricLime,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "FITTRACK AI",
                                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Black, color = TextPrimary)
                                )
                                Text(
                                    text = "Powered by Supabase Auth",
                                    style = Typography.labelSmall.copy(color = ElectricCyan, fontSize = 10.sp)
                                )
                            }
                        }

                        if (authStep == 2) {
                            LiquidIconButton(
                                onClick = {
                                    triggerHaptic()
                                    authStep = 1
                                    errorMessage = null
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Step 0: Sign In, Step 1: Register, Step 2: OTP Verification
                    AnimatedContent(
                        targetState = authStep,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.95f, animationSpec = tween(220, easing = FastOutSlowInEasing)))
                                .togetherWith(fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
                        },
                        label = "auth_step_transition"
                    ) { currentStep ->
                        when (currentStep) {
                            0 -> {
                                // SIGN IN (EXISTING USER)
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Welcome Back",
                                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                    )
                                    Text(
                                        text = "ஒரே முறை லாகின் செய்யவும் • One-time secure Supabase login",
                                        style = Typography.bodyMedium.copy(color = TextSecondary),
                                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                                    )

                                    LiquidInput(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = "Email Address",
                                        placeholder = "athlete@example.com"
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    LiquidInput(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = "Password",
                                        placeholder = "Enter your password",
                                        visualTransformation = if (!isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                                        trailingIcon = {
                                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    )

                                    if (errorMessage != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(text = errorMessage ?: "", style = Typography.bodySmall.copy(color = ErrorRed))
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // 60fps Spring Sign In Button
                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            if (email.isBlank() || !email.contains("@")) {
                                                errorMessage = "Please enter a valid email address."
                                                return@Button
                                            }
                                            if (password.length < 4) {
                                                errorMessage = "Password is too short."
                                                return@Button
                                            }
                                            errorMessage = null
                                            isLoading = true
                                            coroutineScope.launch {
                                                val res = supabaseAuth.signInWithPassword(email, password)
                                                isLoading = false
                                                when (res) {
                                                    is SupabaseAuthService.AuthResult.Success -> {
                                                        onLoginSuccess(res.email, res.fullName)
                                                    }
                                                    is SupabaseAuthService.AuthResult.Error -> {
                                                        errorMessage = res.errorMessage
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("signin_button"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricLime,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
                                        } else {
                                            Text(text = "SIGN IN WITH SUPABASE", style = Typography.labelLarge.copy(fontWeight = FontWeight.Black))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Quick Demo Explore Button
                                    LiquidSecondaryButton(
                                        text = "Instant Sign-In as Priyan (One-Tap)",
                                        onClick = {
                                            triggerHaptic()
                                            onLoginSuccess("priyan1436ei@gmail.com", "Priyan")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        borderColor = ElectricLime.copy(alpha = 0.4f),
                                        textColor = ElectricLime
                                    )
                                }
                            }

                            1 -> {
                                // REGISTER (NEW USER)
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Create Supabase Account",
                                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                    )
                                    Text(
                                        text = "ரிஜிஸ்டர் செய்ததும் உங்கள் மெயிலுக்கு OTP அனுப்பப்படும்",
                                        style = Typography.bodyMedium.copy(color = ElectricLime),
                                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                                    )

                                    LiquidInput(
                                        value = fullName,
                                        onValueChange = { fullName = it },
                                        label = "Full Name",
                                        placeholder = "Enter your full name"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LiquidInput(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = "Email Address",
                                        placeholder = "athlete@example.com"
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    LiquidInput(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = "Create Password",
                                        placeholder = "Minimum 6 characters",
                                        visualTransformation = if (!isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                                        trailingIcon = {
                                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = TextSecondary
                                                )
                                            }
                                        }
                                    )

                                    if (errorMessage != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(text = errorMessage ?: "", style = Typography.bodySmall.copy(color = ErrorRed))
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            if (fullName.isBlank()) {
                                                errorMessage = "Please enter your name."
                                                return@Button
                                            }
                                            if (email.isBlank() || !email.contains("@")) {
                                                errorMessage = "Please enter a valid email address."
                                                return@Button
                                            }
                                            if (password.length < 4) {
                                                errorMessage = "Password must be at least 4 characters."
                                                return@Button
                                            }
                                            errorMessage = null
                                            isLoading = true
                                            coroutineScope.launch {
                                                val res = supabaseAuth.registerWithSupabase(email, password, fullName)
                                                isLoading = false
                                                when (res) {
                                                    is SupabaseAuthService.AuthResult.OtpSent -> {
                                                        authStep = 2 // Move to OTP verification view!
                                                    }
                                                    is SupabaseAuthService.AuthResult.Error -> {
                                                        errorMessage = res.errorMessage
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("register_button"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricLime,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
                                        } else {
                                            Text(text = "REGISTER & SEND EMAIL OTP", style = Typography.labelLarge.copy(fontWeight = FontWeight.Black))
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // OTP VERIFICATION VIEW
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Verify Email OTP",
                                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                    )
                                    Text(
                                        text = "Enter the 6-digit OTP code sent to:",
                                        style = Typography.bodyMedium.copy(color = TextSecondary),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = pendingEmail ?: email,
                                        style = Typography.bodyLarge.copy(color = ElectricLime, fontWeight = FontWeight.Bold)
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Simulated incoming email alert badge (allows 1-tap autofill)
                                    if (incomingEmailNotice != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0x3319E3FF))
                                                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                                .clickable {
                                                    triggerHaptic()
                                                    val otp = supabaseAuth.generatedOtp.value ?: "123456"
                                                    enteredOtp = otp
                                                }
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Mail, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(text = "INCOMING SUPABASE EMAIL", style = Typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = incomingEmailNotice ?: "", style = Typography.bodySmall.copy(color = TextPrimary))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = "👉 Tap here to autofill OTP", style = Typography.labelSmall.copy(color = ElectricLime, fontWeight = FontWeight.Bold))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(18.dp))
                                    }

                                    // 6-digit OTP Input
                                    OutlinedTextField(
                                        value = enteredOtp,
                                        onValueChange = { if (it.length <= 6) enteredOtp = it },
                                        label = { Text("6-Digit Verification OTP") },
                                        placeholder = { Text("Enter OTP (e.g. 123456)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("otp_input"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = ElectricLime,
                                            unfocusedBorderColor = Color(0x33FFFFFF)
                                        )
                                    )

                                    if (errorMessage != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(text = errorMessage ?: "", style = Typography.bodySmall.copy(color = ErrorRed))
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            if (enteredOtp.length < 6) {
                                                errorMessage = "Please enter all 6 digits of the OTP code."
                                                return@Button
                                            }
                                            errorMessage = null
                                            isLoading = true
                                            coroutineScope.launch {
                                                val res = supabaseAuth.verifyEmailOtp(email, enteredOtp)
                                                isLoading = false
                                                when (res) {
                                                    is SupabaseAuthService.AuthResult.Success -> {
                                                        onLoginSuccess(res.email, res.fullName)
                                                    }
                                                    is SupabaseAuthService.AuthResult.Error -> {
                                                        errorMessage = res.errorMessage
                                                    }
                                                    else -> {}
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("verify_otp_button"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricLime,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
                                        } else {
                                            Text(text = "VERIFY & PROCEED", style = Typography.labelLarge.copy(fontWeight = FontWeight.Black))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Resend OTP Button with countdown
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (resendCooldown > 0) {
                                            Text(
                                                text = "Resend OTP in ${resendCooldown}s",
                                                style = Typography.bodySmall.copy(color = TextSecondary)
                                            )
                                        } else {
                                            Text(
                                                text = "Resend OTP Code",
                                                style = Typography.bodySmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold),
                                                modifier = Modifier.clickable {
                                                    triggerHaptic()
                                                    resendCooldown = 45
                                                    coroutineScope.launch {
                                                        supabaseAuth.resendOtp(email)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Tab Switcher (Sign In vs Register)
                if (authStep != 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (authStep == 1) "Already have an account? " else "Don't have an account? ",
                            style = Typography.bodyMedium.copy(color = TextSecondary)
                        )
                        Text(
                            text = if (authStep == 1) "Sign In" else "Register",
                            style = Typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricLime
                            ),
                            modifier = Modifier.clickable {
                                triggerHaptic()
                                authStep = if (authStep == 1) 0 else 1
                                errorMessage = null
                            }
                        )
                    }
                }
            }
        }
    }
}
