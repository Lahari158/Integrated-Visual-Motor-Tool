package com.pdd.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.pdd.app.R
import com.pdd.app.ui.theme.PddAppTheme
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            PddAppTheme(dynamicColor = false) {
                SplashScreen(onFinished = {
                    startActivity(Intent(this, MainActivity::class.java))
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                })
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Entrance animation states
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.45f) }

    // Infinite breathing pulse for the logo
    val inf = rememberInfiniteTransition(label = "logo_pulse")
    val pulse by inf.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse_val"
    )

    LaunchedEffect(Unit) {
        delay(150)
        // Zoom and fade in logo smoothly
        logoAlpha.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
        delay(1600)
        onFinished()
    }

    // Pure white background matching the logo's exact white canvas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Centered Cyber Eye Logo (Only logo, no text)
        Image(
            painter = painterResource(id = R.drawable.logo_cyber_eye),
            contentDescription = "App Splash Logo",
            modifier = Modifier
                .size(260.dp)
                .scale(logoScale.value * pulse)
                .alpha(logoAlpha.value)
        )
    }
}
