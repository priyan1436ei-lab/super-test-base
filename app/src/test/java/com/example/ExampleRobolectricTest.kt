package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FITTRACK AI", appName)
  }

  @Test
  fun `supabase auth registers and verifies otp`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authService = com.example.data.auth.SupabaseAuthService(context)

    val registerResult = authService.registerWithSupabase(
      email = "priyan1436ei@gmail.com",
      password = "mypassword123",
      fullName = "Priyan"
    )
    org.junit.Assert.assertTrue(registerResult is com.example.data.auth.SupabaseAuthService.AuthResult.OtpSent)
    val otpSent = registerResult as com.example.data.auth.SupabaseAuthService.AuthResult.OtpSent
    assertEquals(6, otpSent.otpCode.length)

    // Verification with valid OTP
    val validVerify = authService.verifyEmailOtp("priyan1436ei@gmail.com", otpSent.otpCode)
    org.junit.Assert.assertTrue(validVerify is com.example.data.auth.SupabaseAuthService.AuthResult.Success)
    val success = validVerify as com.example.data.auth.SupabaseAuthService.AuthResult.Success
    assertEquals("priyan1436ei@gmail.com", success.email)
    assertEquals("Priyan", success.fullName)
  }
}
