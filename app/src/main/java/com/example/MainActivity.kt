package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChatMessage
import com.example.data.DiaryEntry
import com.example.ui.DiaryViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    private val viewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainDashboard(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainDashboard(
    viewModel: DiaryViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiInsight by viewModel.aiInsight.collectAsStateWithLifecycle()
    val apiError by viewModel.apiError.collectAsStateWithLifecycle()

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

    val isRootEnabled by viewModel.isRootEnabled.collectAsStateWithLifecycle()
    val isSuActive by viewModel.isSuActive.collectAsStateWithLifecycle()
    val isRealRouting by viewModel.isRealRouting.collectAsStateWithLifecycle()
    val terminalLog by viewModel.terminalLog.collectAsStateWithLifecycle()

    val virtualRam by viewModel.virtualRam.collectAsStateWithLifecycle()
    val virtualStorage by viewModel.virtualStorage.collectAsStateWithLifecycle()
    val virtualProcessor by viewModel.virtualProcessor.collectAsStateWithLifecycle()
    val virtualDeviceModel by viewModel.virtualDeviceModel.collectAsStateWithLifecycle()
    val virtualVolume by viewModel.virtualVolume.collectAsStateWithLifecycle()
    val isVirtualSoundEnabled by viewModel.isVirtualSoundEnabled.collectAsStateWithLifecycle()
    val isPhysicalFullScreenEnabled by viewModel.isPhysicalFullScreenEnabled.collectAsStateWithLifecycle()
    val installedVirtualApps by viewModel.installedVirtualApps.collectAsStateWithLifecycle()
    
    // Google System Update state flows observed in UI
    val androidVersion by viewModel.androidVersion.collectAsStateWithLifecycle()
    val securityPatchLevel by viewModel.securityPatchLevel.collectAsStateWithLifecycle()
    val lastCheckedUpdates by viewModel.lastCheckedUpdates.collectAsStateWithLifecycle()
    val updateHistory by viewModel.updateHistory.collectAsStateWithLifecycle()
    val updatesAvailable by viewModel.updatesAvailable.collectAsStateWithLifecycle()
    val isCheckingUpdates by viewModel.isCheckingUpdates.collectAsStateWithLifecycle()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsStateWithLifecycle()
    val isInstallingUpdate by viewModel.isInstallingUpdate.collectAsStateWithLifecycle()
    val updateProgress by viewModel.updateProgress.collectAsStateWithLifecycle()
    val updateReadyToRestart by viewModel.updateReadyToRestart.collectAsStateWithLifecycle()
    val pendingUpdateVersion by viewModel.pendingUpdateVersion.collectAsStateWithLifecycle()
    val pendingPatchLevel by viewModel.pendingPatchLevel.collectAsStateWithLifecycle()
    val updateStageText by viewModel.updateStageText.collectAsStateWithLifecycle()

    val lastSavedTimestamp by viewModel.lastSavedTimestamp.collectAsStateWithLifecycle()
    val isTutorialActive by viewModel.isTutorialActive.collectAsStateWithLifecycle()
    val saveStatusMessage by viewModel.saveStatusMessage.collectAsStateWithLifecycle()

    var showHardwareDialog by remember { mutableStateOf(false) }
    var showGoogleUpdateCenterDialog by remember { mutableStateOf(false) }

    var isNotificationShadeExpanded by remember { mutableStateOf(false) }
    var isUpgradingDevice by remember { mutableStateOf(false) }
    var upgradeProgress by remember { mutableStateOf(0f) }
    var upgradeStepText by remember { mutableStateOf("") }
    
    var isRebooting by remember { mutableStateOf(false) }
    var rebootStep by remember { mutableStateOf(0) }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Diário, 1 = Chat com IA, 2 = Play Store, 3 = Instalador, 4 = Nuvem Cloud
    var showAddDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSignInGoogleDialog by remember { mutableStateOf(false) }
    var showRootDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    var isFullScreen by remember { mutableStateOf(false) }
    val view = androidx.compose.ui.platform.LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val window = (context as? android.app.Activity)?.window

    val triggerGoogleOtaReboot = {
        coroutineScope.launch {
            showGoogleUpdateCenterDialog = false
            showHardwareDialog = false
            isNotificationShadeExpanded = false
            isUpgradingDevice = true
            upgradeProgress = 1.0f
            upgradeStepText = "Preparando reinicialização dinâmica para aplicar atualização da Google..."
            delay(1000)
            
            isUpgradingDevice = false
            isRebooting = true
            rebootStep = 0 // Black Screen reboot loop start
            delay(1500)
            
            rebootStep = 1 // Android animated boot screen logo
            delay(3000)
            
            rebootStep = 2 // System initialization setup and optimization
            delay(2500)
            
            // Apply pending update states in viewModel
            viewModel.applyPendingSystemUpdate()
            
            isRebooting = false
            android.widget.Toast.makeText(
                context,
                "Sistema Android atualizado com sucesso! Versão e Patches em conformidade com Google OTA.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
        Unit
    }

    val onTriggerSoftwareUpdate = {
        coroutineScope.launch {
            showHardwareDialog = false
            isNotificationShadeExpanded = false
            isUpgradingDevice = true
            upgradeProgress = 0f
            upgradeStepText = "Solicitando permissão OTA para os servidores Xiaomi..."
            delay(1000)
            upgradeProgress = 0.15f
            upgradeStepText = "Efetuando download de Redmi 15 ROM (Android 14 / HyperOS v15)..."
            delay(1500)
            upgradeProgress = 0.35f
            upgradeStepText = "Alocando 256 GB de armazenamento interno UFS v4.0 estendido..."
            delay(1500)
            upgradeProgress = 0.65f
            upgradeStepText = "Alocando barramento de memória RAM LPDDR5X físico-virtual de 8 GB..."
            delay(1500)
            upgradeProgress = 0.85f
            upgradeStepText = "Instalando kernel do sistema Xiaomi MIUI e patches de superusuário..."
            delay(1200)
            upgradeProgress = 1.0f
            upgradeStepText = "Tudo pronto! Reiniciando celular virtual de forma segura..."
            delay(1000)
            
            // Reboot Stage Transition
            isUpgradingDevice = false
            isRebooting = true
            rebootStep = 0 // Black Screen reboot loop start
            delay(1500)
            
            rebootStep = 1 // Xiaomi / Redmi HyperOS animated logo Screen 
            delay(3000)
            
            rebootStep = 2 // System initialization setup Optimization screen
            delay(2500)
            
            // Apply permanent specs inside state values
            viewModel.updateDeviceModel("Redmi 15")
            viewModel.updateHardwareSpecs(
                ram = 8,
                storage = 256,
                processor = "MediaTek Dimensity 8300-Ultra Octa-Core @ 3.35 GHz"
            )
            
            isRebooting = false
            android.widget.Toast.makeText(
                context,
                "Redmi 15 inicializado com sucesso! 256GB / 8GB RAM LPDDR5X OK.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
        Unit
    }

    LaunchedEffect(isPhysicalFullScreenEnabled) {
        isFullScreen = isPhysicalFullScreenEnabled
    }

    LaunchedEffect(isFullScreen) {
        if (window != null) {
            try {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                if (isFullScreen) {
                    insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                }
            } catch (e: Exception) {
                // Ignore any system bar window manager exception to prevent App Crash on emulate
                android.util.Log.e("MainActivity", "Erro ao ajustar tela cheia: ${e.message}")
            }
        }
    }

    // Slate Minimalist Dark Gradient Background Vibe for Header
    val premiumHeaderBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- VIRTUAL SYSTEM STATUS BAR (Tapping opens notification tray - 'rolage pra baixo') ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .clickable { isNotificationShadeExpanded = !isNotificationShadeExpanded }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Operator, clock, notification badge dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "17:53",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "|",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "TIM Brasil",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    if (virtualDeviceModel != "Redmi 15") {
                        // Flashing update available indicator badge with descriptive text
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                                Text(
                                    text = "Atualização disponível 🔔",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Center: Pull-down indicator handle (rolagem para baixo)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.alpha(0.8f)
                ) {
                    Text(
                        text = if (isNotificationShadeExpanded) "Puxe para subir ⬆️" else "Puxe para baixo ⬇️",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Right Side: Wi-Fi, Sound, Battery Level
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📶 5G", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("🔋 98%", color = Color(0xFF4ADE80), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- SIMULATED PULLDOWN NOTIFICATION DRAWERS SHADE PANEL (Com rolagem para baixo) ---
        AnimatedVisibility(
            visible = isNotificationShadeExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = Color(0xFF0F172A), // Dark slate theme
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🔔", fontSize = 16.sp)
                            Text(
                                text = "NOTIFICAÇÕES DO SISTEMA",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        IconButton(
                            onClick = { isNotificationShadeExpanded = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar notificações",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Simulated Quick Toggles Grid Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("📶", "Rede 5G", true),
                            Triple("⚡", "Desempenho", true),
                            Triple("🔊", "Som Ativo", isVirtualSoundEnabled),
                            Triple("🔋", "Economia", false)
                        ).forEach { (emoji, label, isActive) ->
                            val activeState = if (isActive is Boolean) isActive else isActive as Boolean
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (activeState) Color(0xFF0284C7) else Color(0xFF1E293B))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = label,
                                        color = if (activeState) Color.White else Color.White.copy(alpha = 0.6f),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    androidx.compose.material3.HorizontalDivider(color = Color(0xFF1E293B))

                    // Notifications Stack List (Fully scrollable container - 'uma rolage pra baixo')
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (virtualDeviceModel != "Redmi 15") {
                            // MANDATORY SYSTEM NOTIFICATION CARD
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1E293B)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("🔄", fontSize = 16.sp)
                                            Text(
                                                text = "ATUALIZAÇÃO DISPONÍVEL",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFF59E0B)
                                            )
                                        }
                                        Text(
                                            text = "Agora",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 9.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Atualização do sistema virtual pronta para download! O modelo disponível será Redmi 15 e terá 256GB de armazenamento com 8GB de RAM.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                onTriggerSoftwareUpdate()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                                        ) {
                                            Text("ATUALIZAR AGORA ⚡", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Success Status notification
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1E293B)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("🎉", fontSize = 24.sp)
                                    Column {
                                        Text(
                                            text = "Sistema Atualizado para Redmi 15!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF4ADE80)
                                        )
                                        Text(
                                            text = "O sistema virtual está rodando a versão estável mais recente do Redmi 15 com os 256GB / 8GB RAM requisitados.",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.7f),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Play Protect check notice
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🛡️", fontSize = 14.sp)
                                Column {
                                    Text(
                                        text = "Google Play Protect Ativo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Text(
                                        text = "Suas partições e sandbox estão seguras.",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }

        // --- SYSTEM UPGRADE OVERLAY (Full-Screen HD Xiaomi OTA Flasher Overlay) ---
        if (isUpgradingDevice) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF000000) // Pitch black installation environment
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Logo & Meta info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 48.dp)
                    ) {
                        Text(
                            text = "ATUALIZAÇÃO DE SISTEMA DISPONÍVEL",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Xiaomi HyperOS (Redmi 15)",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estável | Versão 15.0.21 (Android 14)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }

                    // Progress circular graphics
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = upgradeProgress,
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF0284C7),
                            strokeWidth = 6.dp,
                            trackColor = Color(0xFF1E293B)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(upgradeProgress * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "BAIXADO",
                                color = Color(0xFF0284C7),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Bottom info and current operation text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = upgradeStepText,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = "Aviso: Não minimize o aplicativo nem desligue o celular. A máquina virtual será reiniciada após a flashing.",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }

        // --- SIMULATED HARDWARE REBOOT SEQUENCES OVERLAY (Black screen + boot logos) ---
        if (isRebooting) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF000000)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (rebootStep == 0) {
                        // Power off black state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                strokeWidth = 1.dp
                            )
                            Text(
                                text = "Encerrando sandbox virtual...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp
                            )
                        }
                    } else if (rebootStep == 1) {
                        // Xiaomi HyperOS branding boot loader
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "R e d m i",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Xiaomi HyperOS",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(48.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White.copy(alpha = 0.7f),
                                strokeWidth = 2.dp
                            )
                        }

                        // Powered by Android label
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 40.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "P o w e r e d   b y",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 9.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "a n d r o i d",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (rebootStep == 2) {
                        // Optimization / Post-flash system setup
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF0284C7),
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Carregando Redmi 15",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Otimizando armazenamento virtual de 256 GB UFS 4.0...",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "RAM LPDDR5X: 8 GB OK. Iniciando Launcher...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        if (!isLoggedIn) {
            GoogleGmsSetupWizardScreen(viewModel = viewModel)
        } else {
            // 1. Premium Elegant Top App Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(premiumHeaderBrush)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Guia AI",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("app_logo")
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Seu Diário Pessoal & Mentor",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isLoggedIn) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Sincronizado",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // AI Status indicator badge
                    Surface(
                        shape = CircleShape,
                        color = if (isAiLoading) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.testTag("ai_status_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isAiLoading) Color.Green else Color.Gray)
                            )
                            Text(
                                text = if (isAiLoading) "IA Ativa" else "IA Livre",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // --- Virtual Hardware Status Badge ---
                    Surface(
                        onClick = { showHardwareDialog = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.testTag("hardware_status_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configurar Hardware",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "⚙️ ${virtualRam}GB RAM / ${virtualStorage}GB OK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // --- Full Screen Toggle ---
                    androidx.compose.material3.IconButton(
                        onClick = { isFullScreen = !isFullScreen },
                        modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) androidx.compose.material.icons.Icons.Default.FullscreenExit else androidx.compose.material.icons.Icons.Default.Fullscreen,
                            contentDescription = "Tela Cheia",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // --- Save Progress Quick Button ---
                    Surface(
                        onClick = { viewModel.saveProgress() },
                        shape = CircleShape,
                        color = Color(0xFF1976D2).copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.testTag("button_save_progress")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("💾", fontSize = 11.sp)
                            Text("Salvar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        }
                    }

                    // --- Interactive Tutorial Quick Button ---
                    Surface(
                        onClick = { viewModel.startTutorial() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier.testTag("button_open_tutorial")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🎓", fontSize = 11.sp)
                            Text("Tutorial", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // User Profile Button (Always active - Login system removed)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { showProfileDialog = true }
                            .testTag("button_profile"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "J",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Error message notification bar
        AnimatedVisibility(
            visible = apiError != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = apiError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.dismissError() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar erro",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Save status message banner
        AnimatedVisibility(
            visible = saveStatusMessage != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                color = Color(0xFF1565C0),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = saveStatusMessage ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Tab Selector Pil
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Diário Tab Button
                Button(
                    onClick = { selectedTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_diary"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Filled.Book else Icons.Outlined.Book,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Diário", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Chat Tab Button
                Button(
                    onClick = { selectedTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_chat"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (selectedTab == 1) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sessão AI", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Play Store Tab Button
                Button(
                    onClick = { selectedTab = 2 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 2) Color(0xFF01875F) else Color.Transparent,
                        contentColor = if (selectedTab == 2) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_play_store"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Store", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Export APK Tab Button
                Button(
                    onClick = { selectedTab = 3 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 3) Color(0xFF3DDC84) else Color.Transparent,
                        contentColor = if (selectedTab == 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_export_apk"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Instalar APK", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Nuvem Cloud Tab Button
                Button(
                    onClick = { selectedTab = 4 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 4) Color(0xFF1976D2) else Color.Transparent,
                        contentColor = if (selectedTab == 4) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_cloud_sync"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nuvem Cloud", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // 3. Dynamic Screen Switcher
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() with
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                }
            ) { targetTab ->
                when (targetTab) {
                    0 -> DiaryScreen(
                        entries = entries,
                        aiInsight = aiInsight,
                        isAiLoading = isAiLoading,
                        virtualRam = virtualRam,
                        virtualStorage = virtualStorage,
                        virtualProcessor = virtualProcessor,
                        virtualDeviceModel = virtualDeviceModel,
                        onOpenHardwareSettings = { showHardwareDialog = true },
                        onAddClick = { showAddDialog = true },
                        onDeleteClick = { viewModel.deleteDiaryEntry(it) },
                        onClearAllClick = { viewModel.clearAllEntries() },
                        onGenerateInsightClick = { viewModel.generateDiaryInsights() },
                        onDismissInsight = { viewModel.clearInsight() }
                    )
                    1 -> ChatScreen(
                        viewModel = viewModel,
                        messages = messages,
                        isAiLoading = isAiLoading,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onClearHistory = { viewModel.clearChatHistory() }
                    )
                    2 -> PlayStoreScreen(viewModel = viewModel)
                    3 -> ExportApkScreen(viewModel = viewModel, onNavigateToPlayStore = { selectedTab = 2 })
                    4 -> NuvemCloudScreen(viewModel = viewModel)
                }
            }
        }
        }
    }

    // Hardware/System Configurator Dialog
    if (showHardwareDialog) {
        HardwareConfigurationDialog(
            currentRam = virtualRam,
            currentStorage = virtualStorage,
            currentProcessor = virtualProcessor,
            currentVolume = virtualVolume,
            isSoundEnabled = isVirtualSoundEnabled,
            isPhysicalFullScreenEnabled = isPhysicalFullScreenEnabled,
            virtualDeviceModel = virtualDeviceModel,
            onTogglePhysicalFullScreen = { viewModel.togglePhysicalFullScreen(it) },
            onDismiss = { showHardwareDialog = false },
            onTriggerSoftwareUpdate = { onTriggerSoftwareUpdate() },
            androidVersion = androidVersion,
            securityPatchLevel = securityPatchLevel,
            onOpenGoogleUpdateCenter = {
                showHardwareDialog = false
                showGoogleUpdateCenterDialog = true
            },
            onSave = { ram, storage, processor, volume, soundEnabled ->
                viewModel.updateHardwareSpecs(ram, storage, processor)
                viewModel.updateVirtualVolume(volume)
                viewModel.toggleVirtualSound(soundEnabled)
                showHardwareDialog = false
            }
        )
    }

    // Google Android Virtual Update Center Dialog
    if (showGoogleUpdateCenterDialog) {
        GoogleUpdateCenterDialog(
            androidVersion = androidVersion,
            securityPatchLevel = securityPatchLevel,
            lastChecked = lastCheckedUpdates,
            history = updateHistory,
            updatesAvailable = updatesAvailable,
            isChecking = isCheckingUpdates,
            isDownloading = isDownloadingUpdate,
            isInstalling = isInstallingUpdate,
            progress = updateProgress,
            readyToRestart = updateReadyToRestart,
            pendingVersion = pendingUpdateVersion,
            pendingPatch = pendingPatchLevel,
            updateStageText = updateStageText,
            onCheckForUpdates = { viewModel.checkForUpdates() },
            onDownloadInstall = { viewModel.downloadAndInstallUpdate() },
            onReboot = { triggerGoogleOtaReboot() },
            onDismiss = { showGoogleUpdateCenterDialog = false }
        )
    }

    // Modal dialog to add diary record
    if (showAddDialog) {
        AddEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, content, mood ->
                viewModel.addDiaryEntry(title, content, mood)
                showAddDialog = false
            }
        )
    }

    // Interactive Tutorial Modal Dialog
    if (isTutorialActive) {
        InteractiveTutorialDialog(
            onDismiss = { viewModel.completeTutorial() },
            onSelectTab = { selectedTab = it }
        )
    }

    // Google Sign In Mock account chooser modal
    if (showSignInGoogleDialog) {
        GoogleSignInChooserDialog(
            onDismiss = { showSignInGoogleDialog = false },
            onSelectAccount = {
                viewModel.loginWithGoogle()
                showSignInGoogleDialog = false
            }
        )
    }

    // User Profile dialog
    if (showProfileDialog) {
        UserProfileDialog(
            userName = userName ?: "User Name",
            userEmail = userEmail ?: "user@gmail.com",
            isCloudSyncing = isCloudSyncing,
            isRootEnabled = isRootEnabled,
            onToggleRoot = { viewModel.toggleRoot(it) },
            onOpenTerminal = {
                showRootDialog = true
                showProfileDialog = false
            },
            onDismiss = { showProfileDialog = false },
            onLogout = { viewModel.logout() }
        )
    }

    if (showRootDialog) {
        AndroidRootTerminalDialog(
            terminalLog = terminalLog,
            isRootEnabled = isRootEnabled,
            isSuActive = isSuActive,
            isRealRouting = isRealRouting,
            onToggleRealRouting = { viewModel.toggleRealRouting(it) },
            onToggleRoot = { viewModel.toggleRoot(it) },
            onExecuteCommand = { viewModel.executeRootCommand(it) },
            onDismiss = { showRootDialog = false }
        )
    }
}

@Composable
fun GoogleGmsSetupWizardScreen(viewModel: DiaryViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var tempEmail by remember { mutableStateOf("reisjuvenira468@gmail.com") }
    var tempPassword by remember { mutableStateOf("••••••••") }
    var isLoggingIn by remember { mutableStateOf(false) }
    var stepMessage by remember { mutableStateOf("") }
    var progressValue by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Google Colorful Logo
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("G", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
            Text("o", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
            Text("o", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBC05))
            Text("g", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
            Text("l", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34A853))
            Text("e", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
        }

        Text(
            text = "Configuração do GMS & Nuvem 🌍",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Alerta de Login Obrigatório para o Celular Virtual
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("⚠️", fontSize = 24.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LOGIN OBRIGATÓRIO REQUERIDO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Conforme as políticas GMS e segurança do Android Virtual, você precisa entrar com sua conta Google para liberar o acesso ao sistema do Redmi 15.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoggingIn) {
            // Elegant loading card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        progress = progressValue,
                        color = Color(0xFF4285F4),
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 4.dp
                    )
                    
                    Text(
                        text = "Sincronizando...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stepMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    LinearProgressIndicator(
                        progress = progressValue,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = Color(0xFF34A853),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        } else {
            // Cloud syncing informative cards group
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "O que será salvo na sua conta do Google? ☁️",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    RestoreFeatureRow("🎮", "Jogos instalados", "Seus saves e recordações do Snake, Diário e Jogos AI.")
                    RestoreFeatureRow("🛍️", "Google Play Store", "Lista de aplicativos instalados, downloads e sandbox seguros.")
                    RestoreFeatureRow("📧", "Serviços Gmail & GMS", "E-mails, agenda, contatos virtuais e Google Play Protect ativo.")
                    RestoreFeatureRow("📝", "Anotações do Diário", "Todas as suas postagens e insights da Inteligência AI preservados.")
                    RestoreFeatureRow("🔒", "Segurança Total", "Restauração automática em caso de atualizações de sistema ou reboots.")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Inputs
            OutlinedTextField(
                value = tempEmail,
                onValueChange = { tempEmail = it },
                label = { Text("E-mail Google GMS") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF4285F4)) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tempPassword,
                onValueChange = { tempPassword = it },
                label = { Text("Senha da Conta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEA4335)) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (tempEmail.isBlank()) {
                        Toast.makeText(context, "Por favor, digite seu e-mail do Google.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoggingIn = true
                    coroutineScope.launch {
                        stepMessage = "Iniciante handshake GMS v15.0..."
                        progressValue = 0.1f
                        delay(700)
                        
                        stepMessage = "Conectando ao núcleo de banco de dados do Google Drive..."
                        progressValue = 0.3f
                        delay(800)
                        
                        stepMessage = "Verificando token de segurança criptográfico..."
                        progressValue = 0.5f
                        delay(800)
                        
                        stepMessage = "Vinculando e-mail: $tempEmail..."
                        progressValue = 0.7f
                        delay(800)
                        
                        stepMessage = "Sincronizando aplicativos, jogos, Gmail e anotações..."
                        progressValue = 0.9f
                        delay(900)
                        
                        stepMessage = "Restaurando saves anteriores e instalando o Play Store..."
                        progressValue = 1.0f
                        delay(600)
                        
                        viewModel.loginWithGoogle()
                        Toast.makeText(context, "Sincronização com o Google Cloud finalizada com sucesso!", Toast.LENGTH_LONG).show()
                        isLoggingIn = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("VINCULAR CONTA E LOGAR COM GOOGLE 🔐", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun RestoreFeatureRow(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 16.sp)
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GoogleUpdateCenterDialog(
    androidVersion: String,
    securityPatchLevel: String,
    lastChecked: String,
    history: List<String>,
    updatesAvailable: String?,
    isChecking: Boolean,
    isDownloading: Boolean,
    isInstalling: Boolean,
    progress: Float,
    readyToRestart: Boolean,
    pendingVersion: String?,
    pendingPatch: String?,
    updateStageText: String,
    onCheckForUpdates: () -> Unit,
    onDownloadInstall: () -> Unit,
    onReboot: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isDownloading && !isInstalling) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Central Android (OTA Google)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss, 
                        enabled = !isDownloading && !isInstalling,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Text(
                    text = "Central integrada de gerenciamento de atualizações do Google. Simule patch-days de segurança e updates de builds para as versões mais novas do ecossistema.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Main Update Status Section
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // System Info Grid Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "VERSÃO DO SO",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = androidVersion,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "PATCH SEGURANÇA",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = securityPatchLevel.replace("-", "/"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Checking / Installing / Downloading States
                    if (isChecking) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2D3D))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Text(
                                    text = updateStageText,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (isDownloading || isInstalling) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A24))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isDownloading) "Baixando atualização do sistema..." else "Instalando virtual...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF52C41A)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF52C41A)
                                )
                                Text(
                                    text = updateStageText,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCEE3F6),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    } else if (readyToRestart) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF132A13)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF52C41A))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "✓ Instalação Concluída (Segundo Plano)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF52C41A)
                                )
                                Text(
                                    text = "O sistema foi gravado no slot virtual paralelo. É necessário reiniciar o dispositivo fictício para inicializar a nova compilação com êxito.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD4EDDA)
                                )
                                Button(
                                    onClick = onReboot,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text("Reiniciar para Aplicar Atualização 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    } else if (updatesAvailable != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x334285F4)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📢", fontSize = 16.sp)
                                    Text(
                                        text = updatesAvailable ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Disponibilizado oficialmente: ${pendingPatch?.replace("-", "/") ?: ""}\n" +
                                           "Tamanho aproximado: ${if (updatesAvailable?.contains("Upgrade") == true) "2.4 GB - 3.1 GB" else "380 MB - 410 MB"}\n\n" +
                                           "• $updateStageText",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCEE3F6)
                                )
                                Button(
                                    onClick = onDownloadInstall,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text("Baixar e Instalar em Segundo Plano 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    } else {
                        // Fully updated state
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x1110B981)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("✅", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = "Google Android atualizado!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = lastChecked,
                                        fontSize = 10.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onCheckForUpdates,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text("Verificar Atualizações 🔍", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // History Section
                    Text(
                        text = "HISTÓRICO DE ATUALIZAÇÕES INSTALADAS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (history.isEmpty()) {
                                Text(
                                    text = "Nenhum histórico disponível.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                history.reversed().forEach { log ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("📦", fontSize = 11.sp)
                                        Text(
                                            text = log,
                                            fontSize = 10.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeminiSettingsDialog(
    currentApiKey: String,
    currentPrompt: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyText by remember { mutableStateOf(currentApiKey) }
    var promptText by remember { mutableStateOf(currentPrompt) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configuração do Gemini ⚙️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Text(
                    text = "Ajuste as chaves de API e mude a personalidade (Prompt de Instruções) padrão da Inteligência Artificial do celular virtual.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                // API Key input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Chave de API do Gemini (Token)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        placeholder = { Text("Preenchido por padrão do AI Studio...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text(
                        text = "Se deixar vazio, o app usará automaticamente a Chave de API integrada no painel Secrets do AI Studio.",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Prompt do Gemini input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Prompt do Gemini (Instruções Principais)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text(
                        text = "Esse prompt molda o comportamento, as saídas e as regras da Inteligência Artificial do Diário Inteligente.",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            // Reset prompt back to default
                            promptText = "Você é o 'Guia', um assistente virtual e diário inteligente que ajuda o usuário a organizar pensamentos, manter hábitos produtivos e refletir sobre a vida. Seja encorajador, caloroso, direto e prestativo. Use emojis de forma moderada e estilosa. Escreva sempre em português do Brasil e com excelente diagramação de texto (use tópicos ou quebras de linhas quando apropriado, e negrito)."
                            apiKeyText = ""
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Resetar Padrão", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            onSave(apiKeyText.trim(), promptText.trim())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.8f)
                    ) {
                        Text("Salvar Ajustes ✔️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleSignInChooserDialog(
    onDismiss: () -> Unit,
    onSelectAccount: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Google Logo mockup multicolor
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("G", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                    Text("o", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                    Text("o", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBC05))
                    Text("g", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                    Text("l", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34A853))
                    Text("e", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                }

                Text(
                    text = "Escolha uma conta",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "para prosseguir no app Assistente AI",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Account selection item
                Surface(
                    onClick = onSelectAccount,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth().testTag("google_account_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar J
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "J",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Juvenira Reis",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "reisjuvenira468@gmail.com",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Conta Principal",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Use another account button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Usar outra conta do Google", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Para ajudar a garantir a segurança e sincronizar as anotações do seu diário pessoal, o Google compartilhará seu nome, endereço de e-mail e foto do perfil com este aplicativo.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun UserProfileDialog(
    userName: String,
    userEmail: String,
    isCloudSyncing: Boolean,
    isRootEnabled: Boolean,
    onToggleRoot: (Boolean) -> Unit,
    onOpenTerminal: () -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sua Conta Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar bubble icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = userEmail,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sync status indicator card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isCloudSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sincronizando diário...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Backup em Nuvem Ativo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                        Text(
                            text = "Suas reflexões estão seguras e salvas na sua conta Google.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Developer / Root Controller card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isRootEnabled) Color(0xFF1B2E21) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = if (isRootEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Acesso Root (Superusuário)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRootEnabled) Color(0xFF81C784) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = isRootEnabled,
                                onCheckedChange = onToggleRoot,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF2E7D32),
                                    checkedTrackColor = Color(0xFFC8E6C9)
                                )
                            )
                        }

                        Text(
                            text = if (isRootEnabled) 
                                "Acesso de administração root ATIVADO de verdade! Digite qualquer comando do Linux/Android no shell terminal." 
                                else "Habilite para ativar privilégios de superusuário e acessar o shell Linux real no terminal.",
                            fontSize = 11.sp,
                            color = if (isRootEnabled) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )

                        if (isRootEnabled) {
                            Button(
                                onClick = onOpenTerminal,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .testTag("button_open_terminal")
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Abrir Terminal Shell (Root)", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Voltar")
                    }

                    Button(
                        onClick = {
                            onLogout()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sair")
                    }
                }
            }
        }
    }
}

@Composable
fun AndroidRootTerminalDialog(
    terminalLog: List<String>,
    isRootEnabled: Boolean,
    isSuActive: Boolean,
    isRealRouting: Boolean,
    onToggleRealRouting: (Boolean) -> Unit,
    onToggleRoot: (Boolean) -> Unit,
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var cmdInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to end when terminalLog updates
    LaunchedEffect(terminalLog.size) {
        if (terminalLog.isNotEmpty()) {
            listState.animateScrollToItem(terminalLog.size - 1)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .padding(4.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F141C), // Deep premium cyber dark
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D3D))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Terminal Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isRootEnabled && isSuActive) Color(0xFF00FF66) else Color(0xFFFAAD14))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Root Terminal: " + if (isRootEnabled) (if (isSuActive) "Superusuário Ativo" else "Bypass Disponível") else "Acesso Limitado",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA6B5C5),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar Terminal",
                            tint = Color(0xFF8C9BA5),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Interactive Quick Toggles in terminal via Custom Boxes (Scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRootEnabled) Color(0xFF2C1D21) else Color(0xFF132A13))
                            .clickable { onToggleRoot(!isRootEnabled) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isRootEnabled) "Remover Root" else "Aplicar Root",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRootEnabled) Color(0xFFFFA39E) else Color(0xFFB7EB8F)
                        )
                    }

                    val buttons = listOf(
                        "su" to Color(0xFF91CAFF),
                        "roteamento" to Color(0xFFFFD591),
                        "magisk" to Color(0xFFB7EB8F),
                        "kernelsu" to Color(0xFFFFA39E),
                        "iptables" to Color(0xFFFFC069),
                        "neofetch" to Color(0xFFD3ADF7),
                        "help" to Color(0xFFCEE3F6)
                    )

                    buttons.forEach { (cmd, color) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF162335))
                                .clickable { onExecuteCommand(cmd) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cmd,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }

                // Real Gateway Routing Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF141F2E))
                        .border(1.dp, Color(0xFF1E2D3D), RoundedCornerShape(8.dp))
                        .clickable { onToggleRealRouting(!isRealRouting) }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isRealRouting) "🔌" else "❌",
                            fontSize = 14.sp
                        )
                        Column {
                            Text(
                                text = "Roteamento Físico de Root (Gateway)",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isRealRouting) "Ativo via tun0 (192.168.15.100) -> 10.0.2.15" 
                                       else "Inativo (Apenas sandbox local)",
                                color = if (isRealRouting) Color(0xFF52C41A) else Color(0xFF8C9BA5),
                                fontSize = 8.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                    
                    Text(
                        text = if (isRealRouting) "ROTEADO" else "SEM ROTA",
                        color = if (isRealRouting) Color(0xFF52C41A) else Color(0xFFF5222D),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                            .background(
                                if (isRealRouting) Color(0xFF132A13) else Color(0xFF2C1D21),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Console Window Output
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    color = Color(0xFF06090F),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D3D))
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(terminalLog) { line ->
                            Text(
                                text = line,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = if (line.startsWith(">>") || line.contains("[SUCESSO]") || line.contains("[SUCCESS]") || line.contains("Sucesso") || line.contains("[SYSTEMLESS]")) Color(0xFF52C41A)
                                        else if (line.startsWith("guest") || line.startsWith("root") || line.startsWith("$") || line.startsWith("#")) Color(0xFF1890FF)
                                        else if (line.startsWith("[ROUTING]") || line.startsWith("===")) Color(0xFFFA8C16)
                                        else if (line.contains("PERIGO") || line.contains("Negado") || line.contains("DROP") || line.contains("DROP")) Color(0xFFF5222D)
                                        else Color(0xFFCEE3F6),
                                fontSize = 11.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Command Line Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isRootEnabled && isSuActive) "#" else "$",
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = if (isRootEnabled && isSuActive) Color(0xFFFAAD14) else Color(0xFF1890FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    TextField(
                        value = cmdInput,
                        onValueChange = { cmdInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_input"),
                        placeholder = {
                            Text(
                                "Comando (help)...",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFF595959)
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF00FF66)
                        ),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF00FF66)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (cmdInput.isNotBlank()) {
                                    onExecuteCommand(cmdInput)
                                    cmdInput = ""
                                }
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            if (cmdInput.isNotBlank()) {
                                onExecuteCommand(cmdInput)
                                cmdInput = ""
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar comando",
                            tint = Color(0xFF00FF66)
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-SCREEN: DIARY ---

@Composable
fun DiaryScreen(
    entries: List<DiaryEntry>,
    aiInsight: String?,
    isAiLoading: Boolean,
    virtualRam: Int,
    virtualStorage: Int,
    virtualProcessor: String,
    virtualDeviceModel: String,
    onOpenHardwareSettings: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (DiaryEntry) -> Unit,
    onClearAllClick: () -> Unit,
    onGenerateInsightClick: () -> Unit,
    onDismissInsight: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Live virtual hardware specs status widget as requested by the User
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Xiaomi $virtualDeviceModel (Virtual)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = "Sandbox ON",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF01875F),
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Specs Grid Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // RAM card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("MEMÓRIA RAM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${virtualRam} GB LPDDR5X", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        // ROM card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("INTERNA (ROM)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${virtualStorage} GB Flash", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // CPU Info Line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧠 ", fontSize = 12.sp)
                        Text(
                            text = "CPU: $virtualProcessor",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = onOpenHardwareSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RECONFIGURAR HARDWARE & ÁUDIO ⚙️", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Premium Fullscreen Scaling Tip Notice
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💡", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Super Aumento de Tela",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Você pode aumentar e esticar a tela do seu celular físico para caber no tamanho cheio total do celular virtual, eliminando bordas pretas! Toque no ícone de Tela Cheia ⛶ no menu superior para ativar instantaneamente.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
            // Insight Area (Shows when generated)
            AnimatedVisibility(
                visible = aiInsight != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Insights de IA",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                               )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Análise do seu Diário",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 15.sp
                                )
                            }
                            IconButton(onClick = onDismissInsight, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar insights",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = aiInsight ?: "",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gerado pelo modelo gemini-3.5-flash",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Quick controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suas Reflexões (${entries.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (entries.isNotEmpty()) {
                        TextButton(
                            onClick = onClearAllClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpar", fontSize = 13.sp)
                        }

                        Button(
                            onClick = onGenerateInsightClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Insights IA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Entries List
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "O diário está vazio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toque no botão '+' abaixo para registrar seu primeiro pensamento, sentimento ou meta para o dia de hoje.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        DiaryEntryCard(
                            entry = entry,
                            onDelete = { onDeleteClick(entry) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 4.dp)
                .testTag("fab_add_entry"),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo Registro")
        }
    }
}

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd 'de' MMMM, yyyy - HH:mm", Locale("pt", "BR")) }
    val formattedDate = remember(entry.timestamp) { sdf.format(Date(entry.timestamp)) }

    // Map mood tags to visual markers
    val (moodEmoji, moodColor) = getMoodVisualProps(entry.mood)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("entry_card_${entry.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mood Tag Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = moodColor.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = moodEmoji, fontSize = 12.sp)
                        Text(
                            text = entry.mood,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = moodColor
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Deletar registro",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = entry.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = entry.content,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun getMoodVisualProps(mood: String): Pair<String, Color> {
    return when (mood) {
        "Feliz" -> Pair("😄", Color(0xFF2E7D32))      // Emerald Green
        "Neutro" -> Pair("😐", Color(0xFF455A64))     // Steel slate
        "Reflexivo" -> Pair("🧠", Color(0xFF6A1B9A))  // Orchid violet
        "Ansioso" -> Pair("🥺", Color(0xFFC62828))    // Rust red
        "Inspirado" -> Pair("💡", Color(0xFFEF6C00))  // Sunrise orange
        else -> Pair("📝", Color.Gray)
    }
}


// --- SUB-SCREEN: CHAT WITH AI ---

@Composable
fun ChatScreen(
    viewModel: DiaryViewModel,
    messages: List<ChatMessage>,
    isAiLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    val customGeminiApiKey by viewModel.customGeminiApiKey.collectAsStateWithLifecycle()
    val customGeminiPrompt by viewModel.customGeminiPrompt.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }

    var chatInputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom of list when new message is loaded
    LaunchedEffect(messages.size, isAiLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionPrompts = listOf(
        "🧠 Sugira metas de foco",
        "💡 Frase motivacional",
        "🧘 Exercício rápido de calma",
        "📅 Como priorizar meu dia?"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Quick Actions panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Conversa recente",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurar Gemini",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            TextButton(
                onClick = onClearHistory,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpar chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Gemini Settings Modal Dialog
        if (showSettingsDialog) {
            GeminiSettingsDialog(
                currentApiKey = customGeminiApiKey,
                currentPrompt = customGeminiPrompt,
                onDismiss = { showSettingsDialog = false },
                onSave = { apiKey, prompt ->
                    viewModel.updateGeminiSettings(apiKey, prompt)
                }
            )
        }

        // Suggestions horizontal row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(suggestionPrompts) { prompt ->
                Surface(
                    onClick = {
                        onSendMessage(prompt.substring(2)) // strip emoji
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.testTag("suggestion_prompt_${prompt}")
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chat conversation bubble window list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 40.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            // Typing loading badge simulator
            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "O Guia está pensando...",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom dialog input zone
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInputText,
                    onValueChange = { chatInputText = it },
                    placeholder = { Text("Fale com o Guia...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input")
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (chatInputText.isNotBlank()) {
                                onSendMessage(chatInputText)
                                chatInputText = ""
                                keyboardController?.hide()
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    onSendMessage(chatInputText)
                                    chatInputText = ""
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier.testTag("chat_send_button"),
                            enabled = chatInputText.isNotBlank() && !isAiLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = if (chatInputText.isNotBlank() && !isAiLoading) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    }

    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            tonalElevation = 1.dp,
            border = if (!isUser) CardDefaults.outlinedCardBorder() else null
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Name Tag
                Text(
                    text = if (isUser) "Você" else "Guia AI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // Actual message text
                Text(
                    text = message.messageText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = textColor
                )
            }
        }
    }
}


// --- MODAL DIALOG: ADD NEW DIARY NOTE ---

@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, mood: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("Neutro") }

    val moodsList = listOf("Feliz", "Neutro", "Reflexivo", "Ansioso", "Inspirado")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Anotar uma reflexão",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do momento") },
                    placeholder = { Text("Ex: Insights da tarde, Super focado") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("No que está pensando?") },
                    placeholder = { Text("Escreva suas reflexões, metas ou frustrações de agora...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("dialog_input_content"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6
                )

                // Mood selector Title
                Column {
                    Text(
                        text = "Qual seu humor predominante?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal mood list selector
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(moodsList) { mood ->
                            val (emoji, color) = getMoodVisualProps(mood)
                            val isSelected = mood == selectedMood

                            Surface(
                                onClick = { selectedMood = mood },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder else CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.testTag("dialog_mood_${mood}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(emoji, fontSize = 14.sp)
                                    Text(
                                        text = mood,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions buttons inside Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onSave(title, content, selectedMood)
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_save_button")
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

@Composable
fun ExportApkScreen(viewModel: com.example.ui.DiaryViewModel, onNavigateToPlayStore: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val installedApps by viewModel.installedVirtualApps.collectAsStateWithLifecycle()
    val isRootEnabled by viewModel.isRootEnabled.collectAsStateWithLifecycle()

    val antivirusLoading by viewModel.antivirusLoading.collectAsStateWithLifecycle()
    val antivirusReport by viewModel.antivirusReport.collectAsStateWithLifecycle()
    val antivirusRemovedApps by viewModel.antivirusRemovedApps.collectAsStateWithLifecycle()

    var showAntivirusDialog by remember { mutableStateOf(false) }
    var showBitdefenderDialog by remember { mutableStateOf(false) }
    var bitdefenderScanning by remember { mutableStateOf(false) }
    var bitdefenderScanProgress by remember { mutableStateOf(0) }
    var bitdefenderScanMessage by remember { mutableStateOf("") }
    var bitdefenderLastScanResult by remember { mutableStateOf("Nenhum escaneamento completo realizado nesta sessão.") }

    var customUrl by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    // APK Analysis States
    data class TargetApk(
        val name: String,
        val category: String,
        val url: String,
        val emoji: String,
        val desc: String,
        val riskScore: Int,
        val riskLevel: String, // "Seguro", "Médio-Alto (Modificado)", "Crítico (Malware)"
        val detailDesc: String,
        val requiresDoubleConfirm: Boolean = false
    )

    var selectedAppToAnalyze by remember { mutableStateOf<TargetApk?>(null) }
    var analysisStep by remember { mutableStateOf(0) } // 0 = None, 1 = Scanning, 2 = Finished Analysis
    var analysisProgressMessage by remember { mutableStateOf("") }

    // Estado do Explorador USB / Celular Físico Simulado
    var showPhysicalExplorerDialog by remember { mutableStateOf(false) }
    var customApkNameInput by remember { mutableStateOf("") }
    var customApkSizeInput by remember { mutableStateOf("45") }
    var customApkRiskInput by remember { mutableStateOf("Seguro") } // "Seguro", "Médio-Alto", "Crítico (Malware)"
    var customApkEmojiInput by remember { mutableStateOf("🎮") }

    var simulatedPhysicalApksList by remember {
        mutableStateOf(
            listOf(
                TargetApk(
                    name = "WhatsApp_Oficial_v2.24.12.apk",
                    category = "Comunicação / Chat",
                    url = "physical://storage/downloads/whatsapp.apk",
                    emoji = "💬💚",
                    desc = "Cópia exportada extraída da pasta do WhatsApp do seu celular real.",
                    riskScore = 5,
                    riskLevel = "Seguro (Assinatura Confiável)",
                    detailDesc = "Código e assinatura correspondentes aos servidores oficiais da Meta. Sem ameaças detectadas."
                ),
                TargetApk(
                    name = "Subway_Surfers_Dinheiro_Infinito.apk",
                    category = "Jogo Modificado / Offline",
                    url = "physical://storage/jogos/subway_mod.apk",
                    emoji = "🏃‍♂️🛹",
                    desc = "Arquivo contendo mod de moedas e chaves infinitas do seu celular real.",
                    riskScore = 55,
                    riskLevel = "Médio-Alto (Modificado)",
                    detailDesc = "Assinatura alterada para possibilitar trapaças no jogo. Sem vírus ou hooks de rede ofensivos."
                ),
                TargetApk(
                    name = "Minecraft_Pocket_Edition_Gratis.apk",
                    category = "Aventura / Sandbox",
                    url = "physical://storage/jogos/minecraft.apk",
                    emoji = "🟩🧱",
                    desc = "Instalador grátis de jogo pago obtido externamente no aparelho real.",
                    riskScore = 15,
                    riskLevel = "Seguro (Verificação Heurística)",
                    detailDesc = "Assinatura de terceiros limpa. Funciona estavelmente sem requisição de acessos abusivos."
                ),
                TargetApk(
                    name = "Fisico_Trojan_BankBot.apk",
                    category = "Capturador de Senhas / Spyware Bancário",
                    url = "physical://storage/documents/malware_test.apk",
                    emoji = "🛑🏴‍☠️",
                    desc = "Alerta de segurança: arquivo malicioso presente no armazenamento do celular real.",
                    riskScore = 95,
                    riskLevel = "Crítico (Malware)",
                    detailDesc = "Spyware perigoso detectado! Tenta capturar dados digitados em aplicativos e roubar dados bancários bancários.",
                    requiresDoubleConfirm = true
                )
            )
        )
    }

    // Double confirmation for Malware Bypass
    var rootMalwareBypassChecked by remember { mutableStateOf(false) }

    // Downloading simulation
    var isDownloadingSimulated by remember { mutableStateOf(false) }
    var currentDownloadingAppname by remember { mutableStateOf("") }
    var downloadProgressSimulated by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    val recommendedApks = remember {
        listOf(
            TargetApk(
                name = "Google Play Store (Oficial GMS APK v41.2.22)",
                category = "Loja de Aplicativos Oficial Google",
                url = "https://www.apkmirror.com/apk/google-inc/google-play-store/google-play-store-41-2-22-release/",
                emoji = "🛍️⚡",
                desc = "Loja oficial de aplicativos do Google integrada ao sistema GMS (Google Mobile Services). Assinatura oficial certificada.",
                riskScore = 0,
                riskLevel = "Seguro (Verificado por Assinatura Google)",
                detailDesc = "Assinatura digital oficial do Google Inc. verificada com sucesso. Sem detecções por antivírus. Permissões normais do sistema para instalação de pacotes."
            ),
            TargetApk(
                name = "Google Play Services (GMS Core)",
                category = "Serviços de Sistema Oficiais",
                url = "https://www.apkmirror.com/apk/google-inc/google-play-services/",
                emoji = "⚙️🧩",
                desc = "Núcleo de APIs oficial do Google indispensável para sincronizar contas de jogos, conquistas e pagamentos.",
                riskScore = 0,
                riskLevel = "Seguro (Componente Oficial do Sistema)",
                detailDesc = "Código original do sistema Android assinado pelo Google. Livre de adware ou exploits. Executa em privilégio de sistema virtualizado."
            ),
            TargetApk(
                name = "Aurora Store (Oficial v4.4.4)",
                category = "Loja de Aplicativos Segura",
                url = "https://files.auroraoss.com/AuroraStore/Stable/AuroraStore-4.4.4.apk",
                emoji = "🛍️",
                desc = "Gera uma interface limpa do Google Play sem trackers. Assinatura certificada no emulador.",
                riskScore = 5,
                riskLevel = "Seguro",
                detailDesc = "Assinatura do desenvolvedor verificada. Permissões normais de rede."
            ),
            TargetApk(
                name = "PGSharp Pokémon GO (Modificado)",
                category = "Jogo Modificado / Spoofer Localização",
                url = "https://pgsharp.com/downloads/pgsharp_mod.apk",
                emoji = "🟡⚡",
                desc = "Cliente modificado de Pokémon GO para simular GPS. Contorna políticas da Niantic.",
                riskScore = 65,
                riskLevel = "Médio-Alto (Modificado)",
                detailDesc = "Código adulterado para spoofing de localização activa. Sem vírus direto, mas detectável pelos servidores do jogo com risco de suspensão da conta."
            ),
            TargetApk(
                name = "Trojan.BankBot.Android (Malware)",
                category = "Capturador de Senhas / Spyware Bancário",
                url = "http://simulado.malware/bankbot.apk",
                emoji = "🛑🏴‍☠️",
                desc = "Pacote de testes de segurança que intercepta inputs do teclado e credenciais de segurança.",
                riskScore = 98,
                riskLevel = "Crítico (Malware)",
                detailDesc = "Ameaça grave detectada! O aplicativo solicita controle de acessibilidade de segundo plano para registrar teclas digitadas e roubar tokens bancários.",
                requiresDoubleConfirm = true
            ),
            TargetApk(
                name = "Shattered Pixel Dungeon",
                category = "RPG de Turnos Retrô Seguro",
                url = "https://github.com/00-Evan/shattered-pixel-dungeon/releases/download/v2.4.2/ShatteredPD-v2.4.2-Android.apk",
                emoji = "⚔️",
                desc = "Clássico RPG de masmorra leve de código aberto, 100% certificado.",
                riskScore = 2,
                riskLevel = "Seguro",
                detailDesc = "Código aberto auditado. Sem conexões externas suspeitas ou requisição de permissões sensíveis."
            )
        )
    }

    fun startAnalysisFlow(apk: TargetApk) {
        selectedAppToAnalyze = apk
        analysisStep = 1
        rootMalwareBypassChecked = false
        coroutineScope.launch {
            analysisProgressMessage = "Descompactando assinatura do manifesto APK..."
            delay(600)
            analysisProgressMessage = "Varrendo bytecode com assinaturas Play Protect..."
            delay(700)
            analysisProgressMessage = "Inspecionando hooks de permissões do sistema virtual..."
            delay(600)
            analysisStep = 2
        }
    }

    // Permissão e Seleção de Arquivo Físico
    var isStoragePermissionGranted by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                // No Android 13+ (SDK 33+), o seletor de arquivos do sistema (GetContent) gerencia permissões de forma automática e segura.
                true
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isStoragePermissionGranted = isGranted
        if (isGranted) {
            android.widget.Toast.makeText(context, "Permissão de pastas concedida com sucesso! ✓", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Permissão de pastas negada pelo usuário.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun getFileName(uri: android.net.Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            result = it.getString(index)
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "App_Selecionado.apk"
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val fileName = getFileName(uri)
            val cleanedName = if (fileName.endsWith(".apk", ignoreCase = true)) {
                fileName.substringBeforeLast(".apk")
            } else {
                fileName
            }
            startAnalysisFlow(
                TargetApk(
                    name = "$cleanedName (Celular Físico)",
                    category = "Aplicativo do Armazenamento Host",
                    url = uri.toString(),
                    emoji = "📲💾",
                    desc = "Aplicativo carregado em tempo real a partir da memória do telefone físico / hospedeiro.",
                    riskScore = 12,
                    riskLevel = "Seguro (Assinatura Local)",
                    detailDesc = "Nenhuma ameaça encontrada. Assinatura do instalador gerada no ambiente seguro do sandbox virtual."
                )
            )
        } else {
            android.widget.Toast.makeText(context, "Nenhum arquivo APK selecionado.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Primary warning banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF01875F).copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF01875F).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF01875F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 28.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sandbox de APKs e Antivírus",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF01875F)
                        )
                        Text(
                            text = "Instale de forma monitorada. O Play Protect analisa e gera pontuação de riscos de malware (0 a 100) em tempo real.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Card de Conexão com o Celular Físico (Importador/Instalador de APK)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📲", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Instalar APK do Celular Físico (Hospedeiro)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Acesse e selecione arquivos APK locais do seu celular real para rodar segurança sandbox no celular virtual.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Permission indicator status badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isStoragePermissionGranted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            )
                            .border(
                                androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isStoragePermissionGranted) Color(0xFF81C784).copy(alpha = 0.6f) else Color(0xFFFFB74D).copy(alpha = 0.6f)
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(if (isStoragePermissionGranted) "✓" else "💡", fontSize = 14.sp, color = if (isStoragePermissionGranted) Color(0xFF2E7D32) else Color(0xFFE65100))
                            Column {
                                Text(
                                    text = if (isStoragePermissionGranted) "Permissão / Acesso: Autorizado pelo Sistema" else "Seletor de arquivos ativo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStoragePermissionGranted) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                                Text(
                                    text = if (isStoragePermissionGranted) "O emulador virtual está pronto para receber arquivos APK selecionados do seu celular real." else "Você pode tocar diretamente em 'Procurar APK' para carregar arquivos do seu celular.",
                                    fontSize = 10.sp,
                                    color = if (isStoragePermissionGranted) Color(0xFF1B5E20) else Color(0xFF5D4037)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isStoragePermissionGranted) {
                            Button(
                                onClick = {
                                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                                        android.widget.Toast.makeText(context, "Sistemas modernos (Android 13+) não exigem mais permissões manuais de armazenamento para o seletor. Já está ativo! ✓", android.widget.Toast.LENGTH_LONG).show()
                                        isStoragePermissionGranted = true
                                    } else {
                                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Liberar Acesso 🔌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                showPhysicalExplorerDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Procurar APK / Pastas 📂", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Google Antivírus IA Prominent Action Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.05f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 26.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google Antivírus AI (Oficial MD3)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0369A1)
                        )
                        Text(
                            text = "Varredor de malware e ameaças com Inteligência Artificial integrada ao GMS. Explica riscos na nuvem e expurga os vírus e trojans de forma automatizada do sandbox.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAntivirusDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("ABRIR GOOGLE ANTIVÍRUS AI 🚀", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Custom URL Direct Analysis Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Instalar jogo por URL customizada 🌐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = {
                            customUrl = it
                            inputError = null
                        },
                        placeholder = { Text("https://url.do.app/jogo.apk", fontSize = 12.sp) },
                        label = { Text("Cole a URL do APK para análise de Malware", fontSize = 11.sp) },
                        isError = inputError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (inputError != null) {
                        Text(
                            text = inputError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (customUrl.isBlank() || !customUrl.startsWith("http")) {
                                inputError = "Por favor, digite uma URL válida começando com http://"
                            } else {
                                val name = customUrl.substringAfterLast("/").substringBefore("?").ifBlank { "App_Customizado.apk" }
                                startAnalysisFlow(
                                    TargetApk(
                                        name = name,
                                        category = "Pacote de Terceiros Customizado",
                                        url = customUrl,
                                        emoji = "📦",
                                        desc = "Aplicativo baixado via URL remota pelo utilizador.",
                                        riskScore = 45,
                                        riskLevel = "Médio (Não Identificado)",
                                        detailDesc = "Assinatura não registrada no banco de dados do Google Play Store. Recomenda-sê cautela no provisionamento sandbox."
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submeter à Varredura Google Play Protect 🔍", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Simulating downloading state if active
        if (isDownloadingSimulated) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Baixando e Provisionando: $currentDownloadingAppname", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${downloadProgressSimulated}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { downloadProgressSimulated / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = Color(0xFF01875F)
                        )
                    }
                }
            }
        }

        // Section header for Presets
        item {
            Text(
                text = "Preset de Aplicativos e Testes de Vírus 🦠",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(recommendedApks) { preset ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(preset.emoji, fontSize = 24.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (preset.riskScore > 80) Color(0xFFC62828) else if (preset.riskScore > 30) Color(0xFFE65100) else Color(0xFF01875F)
                            )
                        }

                        // Risk level Badge display
                        Text(
                            text = "Risco: ${preset.riskScore}/100",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (preset.riskScore > 80) Color(0xFFC62828) else if (preset.riskScore > 30) Color(0xFFE65100) else Color(0xFF2E7D32),
                            modifier = Modifier
                                .background(
                                    if (preset.riskScore > 80) Color(0xFFFDE8E8) else if (preset.riskScore > 30) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = preset.desc,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { startAnalysisFlow(preset) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (preset.riskScore > 80) Color(0xFFC62828) else Color(0xFF01875F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("Analisar no Play Protect 🔍", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Installed Sandbox Applications (Requirement 5)
        item {
            Text(
                text = "Aplicativos Instalados no Celular Virtual (${installedApps.size}) 📱",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (installedApps.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Não há aplicativos adicionais instalados no sandbox.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(installedApps) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (app == "Google Play Store") Color(0xFFE3F2FD)
                                        else if (app == "Google Antivírus") Color(0xFFE8F5E9)
                                        else if (app.contains("Bitdefender")) Color(0xFFFFEBEE)
                                        else MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (app == "Google Antivírus") "🛡️"
                                    else if (app == "Google Play Store") "🛍️"
                                    else if (app.contains("Bitdefender")) "🛡️"
                                    else if (app.contains("Trojan")) "🛑"
                                    else if (app.contains("PGSharp")) "⚡"
                                    else "📦",
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(app, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (app == "Google Antivírus") "Status: Protegido com Inteligência AI"
                                    else if (app == "Google Play Store") "Loja Oficial de Apps Otimizada"
                                    else if (app.contains("Bitdefender")) "Status: Proteção Ativa & Antivírus"
                                    else if (app.contains("Trojan")) "Risco Crítico! Trojans Ativos - Remova"
                                    else "Status: Instalado e Ativo em Sandbox",
                                    fontSize = 10.sp,
                                    color = if (app == "Google Antivírus" || app == "Google Play Store" || app.contains("Bitdefender")) Color(0xFF2E7D32)
                                    else if (app.contains("Trojan")) Color(0xFFC62828)
                                    else Color(0xFF01875F)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (app == "Google Antivírus") {
                                Button(
                                    onClick = { showAntivirusDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("ABRIR 🚀", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else if (app.contains("Bitdefender")) {
                                Button(
                                    onClick = { showBitdefenderDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("ABRIR 🚀", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else if (app == "Google Play Store") {
                                Button(
                                    onClick = {
                                        onNavigateToPlayStore()
                                        Toast.makeText(context, "Abrindo a Google Play Store Oficial...", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("ABRIR 🚀", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.uninstallVirtualApp(app)
                                    Toast.makeText(context, "$app desinstalado com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }
    }

    // PLAY PROTECT INSTALLATION PROTECTION DIALOG (Requirement 2, 3, 4, 5)
    if (selectedAppToAnalyze != null) {
        val apk = selectedAppToAnalyze!!
        Dialog(onDismissRequest = { selectedAppToAnalyze = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Análise Play Protect",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF01875F)
                        )
                        IconButton(onClick = { selectedAppToAnalyze = null }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    if (analysisStep == 1) {
                        // Scan Loading State
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF01875F))
                            Text(
                                text = "Procurando ameaças em '${apk.name}'...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = analysisProgressMessage,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (analysisStep == 2) {
                        // Scan results finished (Requirement 3, 4)
                        val isHighRisk = apk.riskScore > 75
                        val isMediumRisk = apk.riskScore in 30..75
                        val resultColor = if (isHighRisk) Color(0xFFC62828) else if (isMediumRisk) Color(0xFFE65100) else Color(0xFF2E7D32)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(resultColor.copy(alpha = 0.1f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isHighRisk) "🛑" else if (isMediumRisk) "⚠️" else "✓", fontSize = 28.sp)
                                Column {
                                    Text(
                                        text = "Resultado: ${apk.riskLevel}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = resultColor
                                    )
                                    Text(
                                        text = "Score de Risco Play Protect: ${apk.riskScore} / 100",
                                        fontSize = 11.sp,
                                        color = resultColor
                                    )
                                }
                            }

                            Text(
                                text = "Relatório de Permissões e Assinaturas:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = apk.detailDesc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )

                            if (isHighRisk) {
                                // Double confirm requirement
                                Text(
                                    text = "⚠️ ATENÇÃO: A instalação de troyanos ou malwares conhecidos viola todos os protocolos de estabilidade do emulador e GMS. É necessária confirmação redobrada para prosseguir.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFDE8E8))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Forçar Bypass via Superusuário?",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = rootMalwareBypassChecked,
                                        onCheckedChange = { rootMalwareBypassChecked = it },
                                        modifier = Modifier.scale(0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedAppToAnalyze = null },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancelar 🛡️", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        if (isHighRisk && !rootMalwareBypassChecked) {
                                            Toast.makeText(context, "BLOQUEADO! Ative o interruptor de bypass para forçar no sandbox seguro.", Toast.LENGTH_LONG).show()
                                        } else {
                                            selectedAppToAnalyze = null
                                            // Start simulated installation progress
                                            isDownloadingSimulated = true
                                            currentDownloadingAppname = apk.name
                                            downloadProgressSimulated = 0
                                            coroutineScope.launch {
                                                for (p in 1..100) {
                                                    delay(15)
                                                    downloadProgressSimulated = p
                                                }
                                                isDownloadingSimulated = false
                                                viewModel.installVirtualApp(apk.name)
                                                Toast.makeText(context, "'${apk.name}' instalado no sandbox com sucesso!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = resultColor)
                                ) {
                                    Text("Prosseguir Instalação ⚠️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPhysicalExplorerDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPhysicalExplorerDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(4.dp),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔌", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Explorador USB do Celular Real",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Armazenamento Físico do Hospedeiro",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { showPhysicalExplorerDialog = false }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scrollable Area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Explanatory info box
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "💡 Ponte Inteligente Simulada",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Como você está acessando no modo web (Nuvem), as restrições evitam ler as pastas privadas do aparelho diretamente. Use esta ponte de simulação inteligente para carregar APKs rápidos ou criar novos testes personalizados de vírus!",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        // Section 1: Simulated folders and APK files
                        Text(
                            text = "📁 Pastas do Celular Real (.apk)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        simulatedPhysicalApksList.forEach { apk ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(apk.emoji, fontSize = 20.sp)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = apk.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${apk.category} • ${apk.riskLevel}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (apk.riskScore > 75) Color(0xFFC62828) else if (apk.riskScore > 30) Color(0xFFE65100) else Color(0xFF2E7D32)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            showPhysicalExplorerDialog = false
                                            startAnalysisFlow(apk)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Exportar 📲", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Section 2: Create custom app on physical phone
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🛠️", fontSize = 14.sp)
                                    Text(
                                        text = "Cadastrar APK do meu Celular Físico",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "Digite qualquer nome de APK (ex: Minecraft, WhatsApp Mod, Jogo Hack). Ele será criado instantaneamente no seu celular host pronto para exportar!",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = customApkNameInput,
                                    onValueChange = { customApkNameInput = it },
                                    placeholder = { Text("Ex: Fortnite_Mod_2026.apk", fontSize = 11.sp) },
                                    label = { Text("Nome do Aplicativo", fontSize = 10.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customApkEmojiInput,
                                        onValueChange = { customApkEmojiInput = it },
                                        label = { Text("Emoji ícone", fontSize = 10.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.width(80.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Nível de Risco/Vírus:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            listOf("Seguro", "Modificado", "Vírus").forEach { type ->
                                                val isSelected = customApkRiskInput == type
                                                val bgTypeColor = when (type) {
                                                    "Seguro" -> Color(0xFFE8F5E9)
                                                    "Modificado" -> Color(0xFFFFF3E0)
                                                    else -> Color(0xFFFDE8E8)
                                                }
                                                val textTypeColor = when (type) {
                                                    "Seguro" -> Color(0xFF2E7D32)
                                                    "Modificado" -> Color(0xFFE65100)
                                                    else -> Color(0xFFC62828)
                                                }
                                                Surface(
                                                    onClick = { customApkRiskInput = type },
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSelected) bgTypeColor else MaterialTheme.colorScheme.surfaceVariant,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) textTypeColor else Color.Transparent),
                                                    modifier = Modifier.clickable { customApkRiskInput = type }
                                                ) {
                                                    Text(
                                                        text = type,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) textTypeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (customApkNameInput.isBlank()) {
                                            Toast.makeText(context, "Por favor, digite o nome do APK!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val nameWithExtension = if (customApkNameInput.endsWith(".apk", ignoreCase = true)) {
                                                customApkNameInput
                                            } else {
                                                "$customApkNameInput.apk"
                                            }

                                            val riskScore = when (customApkRiskInput) {
                                                "Seguro" -> 2
                                                "Modificado" -> 45
                                                else -> 98
                                            }

                                            val riskLevel = when (customApkRiskInput) {
                                                "Seguro" -> "Seguro (Assinatura Confiável)"
                                                "Modificado" -> "Médio-Alto (Modificado)"
                                                else -> "Crítico (Malware)"
                                            }

                                            val detailDesc = when (customApkRiskInput) {
                                                "Seguro" -> "A assinatura digital do arquivo local corresponde a uma chave pública autenticada."
                                                "Modificado" -> "Certificado customizado detectado. Bytecode adulterado para liberar recursos extras, livre de spam ou rootkits agressivos."
                                                else -> "Ameaça crítica detectada pelo robô do Play Protect! O arquivo contém rotinas de injeção remota e tenta contornar os sistemas de isolamento."
                                            }

                                            val createdApk = TargetApk(
                                                name = nameWithExtension,
                                                category = "Aplicativo Customizado do Telefone",
                                                url = "physical://storage/custom/$nameWithExtension",
                                                emoji = customApkEmojiInput.ifBlank { "📦" },
                                                desc = "Aplicativo criado pelo usuário a partir do armazenamento do celular físico.",
                                                riskScore = riskScore,
                                                riskLevel = riskLevel,
                                                detailDesc = detailDesc,
                                                requiresDoubleConfirm = customApkRiskInput == "Vírus"
                                            )

                                            simulatedPhysicalApksList = simulatedPhysicalApksList + createdApk
                                            Toast.makeText(context, "Novo APK '$nameWithExtension' salvo na memória física simulada! 🎉", Toast.LENGTH_LONG).show()
                                            customApkNameInput = "" // reset
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("Gravar APK no Armazenamento Física 💾", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        // Section 3: Native backup trigger
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPhysicalExplorerDialog = false
                                    filePickerLauncher.launch("*/*")
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            Text("📁", fontSize = 18.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Usar Seletor Nativo do Celular", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Abre as pastas reais do dispositivo caso o app esteja rodando instalado no aparelho celular físico verdadeiramente.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("➜", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (showAntivirusDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { 
            viewModel.clearAntivirusReport()
            showAntivirusDialog = false 
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🛡️", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Google Antivírus AI",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                                Text(
                                    text = "Varredura Heurística com Inteligência Gemini",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { 
                            viewModel.clearAntivirusReport()
                            showAntivirusDialog = false 
                        }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (antivirusLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF0284C7), modifier = Modifier.size(50.dp))
                            Text(
                                text = "Buscando assinaturas de vírus e malwares...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "O Google AI está analisando o sandbox...",
                                fontSize = 10.sp,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (antivirusReport != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        ) {
                            val cleanReport = antivirusReport ?: ""
                            val cleanRemoved = antivirusRemovedApps
                            
                            if (cleanRemoved.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFDE8E8))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🧹", fontSize = 28.sp)
                                    Column {
                                        Text(
                                            text = "Ameaças Removidas e Sanitizadas!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFC62828)
                                        )
                                        Text(
                                            text = "A IA limpou automaticamente os seguintes pacotes: ${cleanRemoved.joinToString(", ")}",
                                            fontSize = 10.sp,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎉", fontSize = 28.sp)
                                    Column {
                                        Text(
                                            text = "Sistema Sandbox 100% Saudável",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "Nenhum trojan, logger ou ameaça residual detectado.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "RELATÓRIO DETALHADO DA IA:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = cleanReport,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.runAntivirusScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Repetir Varredura 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { 
                                    viewModel.clearAntivirusReport()
                                    showAntivirusDialog = false 
                                },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Fechar", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Text("🛡️🤖⚡", fontSize = 42.sp)
                            Text(
                                text = "Como o Google Antivírus do Guia AI ajuda você?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Nossa Inteligência Heurística vasculha o sandbox virtual à procura de trojans, spywares e scripts de hijacking. Se encontrados, nossa IA remove e limpa as ameaças instantaneamente.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.runAntivirusScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("INICIAR ESCANEAMENTO COM IA COMPLETO 🎯", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBitdefenderDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { 
            showBitdefenderDialog = false 
            bitdefenderScanning = false
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🛡️", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Bitdefender Mobile Security",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "Proteção Avançada para o Virtual Redmi 15",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { 
                            showBitdefenderDialog = false 
                            bitdefenderScanning = false
                        }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (bitdefenderScanning) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFFC62828), modifier = Modifier.size(50.dp))
                            Text(
                                text = "Varrendo bytecode e recursos no sandbox...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = bitdefenderScanMessage,
                                fontSize = 11.sp,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = bitdefenderScanProgress / 100f,
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFC62828)
                            )
                            Text(
                                text = "$bitdefenderScanProgress%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Active Shield Card
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("⚡", fontSize = 28.sp)
                                    Column {
                                        Text(
                                            text = "AUTOPILOT: PROTEÇÃO ATIVA",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "Seu ambiente virtual Redmi 15 está 100% blindado por assinaturas cibernéticas em tempo real.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF2E7D32).copy(alpha = 0.85f),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "RECURSOS INTEGRADOS DO BITDEFENDER:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // List of features
                            val features = listOf(
                                "🛡️ Verificação de Malware Heurística por IA" to "Checa vulnerabilidades e atividades suspeitas na memória virtual.",
                                "🌐 Proteção Web do Navegador" to "Bloqueia tentativas de phishing e ataques aos dados e credenciais.",
                                "🔑 VPN Ultrarrápida Embutida" to "Conexão sandbox 100% criptografada e segura.",
                                "🔒 Bloqueio de Apps & Privacidade" to "Garante que malware local não leia recursos privados do usuário."
                            )

                            features.forEach { (title, desc) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(text = "✓", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Column {
                                        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "ÚLTIMO ESCANEAMENTO:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = bitdefenderLastScanResult,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        bitdefenderScanning = true
                                        bitdefenderScanProgress = 0
                                        coroutineScope.launch {
                                            val scanSteps = listOf(
                                                "Descompactando banco de dados de ameaças...",
                                                "Indexando e buscando pacotes sandbox ativos...",
                                                "Analisando arquivos na pasta Aurora Store...",
                                                "Lendo banco de dados Room persistente local...",
                                                "Varrendo bytecode executável do sandbox virtual...",
                                                "Varredura Heurística em tempo real concluída!"
                                            )
                                            for (i in 1..100) {
                                                delay(25)
                                                bitdefenderScanProgress = i
                                                val stepIdx = ((i - 1) * scanSteps.size) / 100
                                                if (stepIdx < scanSteps.size) {
                                                    bitdefenderScanMessage = scanSteps[stepIdx]
                                                }
                                            }
                                            bitdefenderScanning = false
                                            bitdefenderLastScanResult = "Verificação completa finalizada com sucesso! 18 pacotes verificados. 0 ameaças encontradas de forma proativa. O sistema virtual está 100% livre de malware."
                                            Toast.makeText(context, "Varredura Bitdefender completa! 0 vírus detectados.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("ESCANEAR AGORA 🧹", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { 
                                        showBitdefenderDialog = false 
                                    },
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Fechar", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Composable
fun NuvemCloudScreen(viewModel: com.example.ui.DiaryViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val diaryEntries by viewModel.entries.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

    var customBackupProgress by remember { mutableStateOf(0f) }
    var isBackupOngoing by remember { mutableStateOf(false) }
    var backupStatusText by remember { mutableStateOf("") }

    var selectedBackupToRestore by remember { mutableStateOf<String?>(null) }
    var showRestoreSuccessDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Mock games and progress list
    val mockCloudData = remember {
        listOf(
            "Shattered Pixel Dungeon" to "Saves: Mago Nível 12 (Andar 7) - Sincronizado",
            "Unciv" to "Saves: Império de Juvenira (Turno 132) - Sincronizado",
            "Jogo da Velha AI" to "Estatísticas: Vitórias 24, Derrotas 10 - Sincronizado",
            "Cobrinha Retro Arcade" to "Recorde: 450 pontos (Dificuldade Rápida) - Sincronizado",
            "Aurora Store / Play Store" to "Sessão conectada: Conta Google ativa - Sincronizada"
        )
    }

    if (showRestoreSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreSuccessDialog = false },
            confirmButton = {
                TextButton(onClick = { showRestoreSuccessDialog = false }) {
                    Text("Ótimo!")
                }
            },
            title = { Text("Backup Restaurado! 🔄") },
            text = { Text("Todos os seus jogos salvos, apps instalados, diários e progressos foram baixados da nuvem e restaurados com sucesso para o emulador.") }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        android.widget.Toast.makeText(context, "Saves e backups em nuvem excluídos com sucesso.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir de Vez")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Excluir Backups? ⚠️") },
            text = { Text("Esta ação apagará permanentemente todos os seus progressos salvos na nuvem do Google Drive deste aplicativo. Deseja continuar?") }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2).copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1976D2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Cloud logo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Central de Backup Google Cloud ☁️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "Sincronize todo o seu progresso de jogos, apps instalados e anotações do diário em tempo real usando sua Conta Google.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Account Status Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Status da Conexão Nuvem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1976D2).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (userName ?: "J").take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1976D2)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName ?: "Juvenira Reis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = userEmail ?: "reisjuvenira468@gmail.com",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Text(
                                    text = "Conectado",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveProgress()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar Tudo na Nuvem Google ☁️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live simulated backup progress
        if (isBackupOngoing) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = backupStatusText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = "${(customBackupProgress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1565C0)
                            )
                        }
                        LinearProgressIndicator(
                            progress = customBackupProgress,
                            color = Color(0xFF1976D2),
                            trackColor = Color(0xFF1976D2).copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }

        // Sincronização com GitHub (GitHub Sync)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF24292F).copy(alpha = 0.05f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24292F).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF24292F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐙", fontSize = 20.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sincronização via GitHub Repos 📂",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Vincule seus arquivos para commit de backup remoto",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.3f)))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Text(
                                text = "Essa funcionalidade requer um repositório com pelo menos 2 branches.",
                                fontSize = 11.sp,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Erro: Essa funcionalidade requer um repositório com pelo menos 2 branches.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292F)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Vincular Repositório do GitHub 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Backup statistics / Synced content view
        item {
            Text(
                text = "📊 Elementos Sincronizados na Nuvem",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // List synced resources
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Diaries item
                    ListItem(
                        headlineContent = { Text("Diários e Reflexões", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        supportingContent = { Text("${diaryEntries.size} anotações criptografadas e salvas no Google Drive", fontSize = 11.sp) },
                        leadingContent = {
                            Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(24.dp))
                        },
                        trailingContent = {
                            Icon(Icons.Default.CloudDone, contentDescription = "OK", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    // Games list item
                    mockCloudData.forEachIndexed { idx, pair ->
                        ListItem(
                            headlineContent = { Text(pair.first, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            supportingContent = { Text(pair.second, fontSize = 11.sp) },
                            leadingContent = {
                                Text(if (idx % 2 == 0) "🎮" else "🚀", fontSize = 20.sp)
                            },
                            trailingContent = {
                                Icon(Icons.Default.CloudDone, contentDescription = "Saves em nuvem atualizados", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            }
                        )
                        if (idx < mockCloudData.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        // Advanced Backups & Restore section
        item {
            Text(
                text = "🔄 Ponto de Restauração em Nuvem",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Histórico de Backups Disponíveis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Display mock backup points
                    val backups = listOf(
                        "Backup Automático - Hoje as 09:47" to "7.2 MB • Diário + 4 Jogos inclusos",
                        "Instalação e Saves Limpos - Ontem as 21:12" to "5.1 MB • Diário integrado",
                        "Backup Inicial do Sistema GMS - Ontem as 18:35" to "1.2 MB • Configurações base"
                    )

                    backups.forEach { backupPair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedBackupToRestore == backupPair.first) Color(0xFF1976D2).copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                                .clickable { selectedBackupToRestore = backupPair.first }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = selectedBackupToRestore == backupPair.first,
                                onClick = { selectedBackupToRestore = backupPair.first }
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(backupPair.first, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(backupPair.second, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.loadProgress()
                            },
                            enabled = isLoggedIn,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restaurar Selecionado 🔄", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showDeleteConfirmDialog = true },
                            enabled = isLoggedIn,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                        ) {
                            Text("Excluir Backups 🗑️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayStoreScreen(viewModel: com.example.ui.DiaryViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedVirtualApps.collectAsStateWithLifecycle()

    var activePlayingGame by remember { mutableStateOf<String?>(null) }
    var showPlayProtectDashboard by remember { mutableStateOf(false) }

    var progress1 by remember { mutableStateOf(0) }
    var progress2 by remember { mutableStateOf(0) }
    var isDownloading1 by remember { mutableStateOf(false) }
    var isDownloading2 by remember { mutableStateOf(false) }

    // Google Login Simulated State
    var tempEmail by remember { mutableStateOf("reisjuvenira468@gmail.com") }
    var tempPassword by remember { mutableStateOf("••••••••") }
    var isLoggingInSimulated by remember { mutableStateOf(false) }
    var loginStepMessage by remember { mutableStateOf("") }

    // Play Protect Scan Simulated State
    var isScanningPlayProtect by remember { mutableStateOf(false) }
    var scanStatusMessage by remember { mutableStateOf("Único escaneamento ativo: OK") }

    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Official Google Play Store Content
    when (activePlayingGame) {
            "tic_tac_toe" -> {
                TicTacToeGameScreen(onBack = { activePlayingGame = null })
            }
            "snake" -> {
                SnakeGameScreen(onBack = { activePlayingGame = null })
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Search layout and status indicators
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Pesquisar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Pesquisar de forma segura GMS...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voz",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                // Profile badge showing Google account details when tapped
                                var showAccountPopup by remember { mutableStateOf(false) }
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF01875F))
                                            .clickable { showAccountPopup = !showAccountPopup },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userName?.firstOrNull()?.toString() ?: "J",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (showAccountPopup) {
                                        // Simple dropdown context menu
                                        androidx.compose.ui.window.Popup(
                                            onDismissRequest = { showAccountPopup = false },
                                            alignment = Alignment.BottomEnd,
                                            offset = androidx.compose.ui.unit.IntOffset(0, 32)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                tonalElevation = 8.dp,
                                                border = CardDefaults.outlinedCardBorder(),
                                                modifier = Modifier.width(240.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFF01875F)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("J", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(userName ?: "Juvenira Reis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text(userEmail ?: "reisjuvenira468@gmail.com", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    TextButton(
                                                        onClick = {
                                                            viewModel.logout()
                                                            showAccountPopup = false
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                                    ) {
                                                        Text("Sair da Conta Google 🚪", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Google Play Protect & Antivírus Security Status Banner (Requirement 6)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE8F5E9), // Light green
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { showPlayProtectDashboard = true }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verificado",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Google Play Protect Ativo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                                Text(
                                    text = "VER RELATÓRIO 🛡️",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1B5E20),
                                    modifier = Modifier
                                        .background(Color(0xFFC8E6C9), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "GMS protegido em tempo real contra malware, vírus e aplicativos clonados ou modificados sem assinatura oficial.",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    // Play Store Sub Tabs: Em Alta / Jogos / Premium
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tabs = listOf("Para você", "Em alta", "Jogos 🎮", "Premium")
                        tabs.forEachIndexed { idx, label ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (idx == 2) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.clickable { }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (idx == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (idx == 2) Color(0xFF137333) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Aplicativos e Jogos GMS Certificados",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Official Play Store Web Portal connection card
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF01875F).copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🛍️", fontSize = 22.sp)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Google Play Store Oficial",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Loja Conectada via GMS do Emulador",
                                                fontSize = 10.sp,
                                                color = Color(0xFF01875F),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = "O emulador do Guia AI possui certificação GMS (Google Mobile Services). Toque no botão abaixo para abrir a Play Store Oficial do Google em modo sandbox direto no seu navegador e gerenciar seus aplicativos!",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                    
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps"))
                                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Não foi possível abrir o navegador: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ABRIR GOOGLE PLAY STORE OFICIAL 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        // Official certified Pokémon GO card compared to modified (Requirement 4)
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF01875F).copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFFECB3)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🔴⚡", fontSize = 22.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Pokémon GO (Oficial)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "✓ GMS",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF01875F),
                                                modifier = Modifier
                                                    .background(Color(0xFFE6F4EA), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Text("Niantic, Inc. • Aventura • Seguro", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("4.7 ★", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                            Text("Play Protect: 100% SEGURO (Risco: 0/100)", fontSize = 9.sp, color = Color(0xFF2E7D32))
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.installVirtualApp("Pokémon GO (Oficial)")
                                            Toast.makeText(context, "Pokémon GO oficial instalado com certificado GMS!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text(if (installedApps.contains("Pokémon GO (Oficial)")) "Instalado" else "Instalar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Bitdefender Mobile Security card
                        item {
                            val isBitdefenderInstalled = installedApps.contains("Bitdefender Mobile Security")
                            var isBitdefenderDownloading by remember { mutableStateOf(false) }
                            var bitdefenderProgress by remember { mutableStateOf(0) }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFFEBEE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🛡️🔴", fontSize = 22.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Bitdefender Security",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SEG",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828),
                                                modifier = Modifier
                                                    .background(Color(0xFFFFEBEE), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Text("Bitdefender • Antivírus & VPN • 4.8 ★", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        if (isBitdefenderDownloading) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                CircularProgressIndicator(
                                                    progress = bitdefenderProgress / 100f,
                                                    modifier = Modifier.size(12.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = Color(0xFFC62828)
                                                )
                                                Text(
                                                    text = "Baixando $bitdefenderProgress%...",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFFC62828),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Text("Play Protect: 100% SEGURO (Risco: 0/100)", fontSize = 9.sp, color = Color(0xFF2E7D32))
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (isBitdefenderInstalled) {
                                                viewModel.uninstallVirtualApp("Bitdefender Mobile Security")
                                                Toast.makeText(context, "Bitdefender desinstalado!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                isBitdefenderDownloading = true
                                                bitdefenderProgress = 0
                                                coroutineScope.launch {
                                                    for (p in 1..100) {
                                                        delay(15)
                                                        bitdefenderProgress = p
                                                    }
                                                    isBitdefenderDownloading = false
                                                    viewModel.installVirtualApp("Bitdefender Mobile Security")
                                                    Toast.makeText(context, "Bitdefender Mobile Security instalado com sucesso!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isBitdefenderInstalled) Color(0xFFC62828) else Color(0xFF01875F)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        enabled = !isBitdefenderDownloading
                                    ) {
                                        Text(if (isBitdefenderInstalled) "Remover" else "Instalar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Jogo 1: Jogo da Velha (Tic-Tac-Toe AI)
                        item {
                            PlayStoreGameCard(
                                title = "Jogo da Velha (com Smart AI)",
                                category = "Jogos de Tabuleiro - Inteligência Artificial",
                                rating = "4.8 ★",
                                downloads = "50 Mil+",
                                sizeDesc = "12 MB",
                                emojiDisplay = "❌ ⭕ ⚔️",
                                progress = progress1,
                                isDownloading = isDownloading1,
                                onInstallClick = {
                                    if (!isDownloading1 && progress1 < 100) {
                                        isDownloading1 = true
                                        coroutineScope.launch {
                                            for (p in 1..100) {
                                                delay(15)
                                                progress1 = p
                                            }
                                            isDownloading1 = false
                                            viewModel.installVirtualApp("Jogo da Velha (com Smart AI)")
                                        }
                                    }
                                },
                                onPlayClick = {
                                    activePlayingGame = "tic_tac_toe"
                                }
                            )
                        }

                        // Jogo 2: Cobrinha Retro Arcade (Classic Snake)
                        item {
                            PlayStoreGameCard(
                                title = "Cobrinha Retro Arcade Classic",
                                category = "Jogos Arcade - Nostalgia Anos 2000",
                                rating = "4.6 ★",
                                downloads = "120 Mil+",
                                sizeDesc = "5 MB",
                                emojiDisplay = "🐍 🍎 ⬛",
                                progress = progress2,
                                isDownloading = isDownloading2,
                                onInstallClick = {
                                    if (!isDownloading2 && progress2 < 100) {
                                        isDownloading2 = true
                                        coroutineScope.launch {
                                            for (p in 1..100) {
                                                delay(20)
                                                progress2 = p
                                            }
                                            isDownloading2 = false
                                            viewModel.installVirtualApp("Cobrinha Retro Arcade Classic")
                                        }
                                    }
                                },
                                onPlayClick = {
                                    activePlayingGame = "snake"
                                }
                            )
                        }

                        item {
                            SimulatedPlayStoreCard(
                                title = "Shattered Pixel Dungeon",
                                category = "Roguelike RPG • Seguro GMS",
                                rating = "4.9 ★",
                                sizeDesc = "18 MB",
                                emojiDisplay = "🛡️ 🗡️ 🧙"
                            )
                        }

                        item {
                            SimulatedPlayStoreCard(
                                title = "Aurora Store",
                                category = "Play Store Client Libres • Seguro GMS",
                                rating = "4.7 ★",
                                sizeDesc = "8 MB",
                                emojiDisplay = "🛍️ 📦 🔐"
                            )
                        }
                    }
                }
            }
        }

    // PLAY PROTECT ADVANCED SECURITY DASHBOARD DIALOG (Requirement 2, 3, 4, 6)
    if (showPlayProtectDashboard) {
        Dialog(onDismissRequest = { showPlayProtectDashboard = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 8.dp,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play Protect & Antivírus",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                        IconButton(onClick = { showPlayProtectDashboard = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    // Scanning State
                    if (isScanningPlayProtect) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Inspecionando diretórios sandbox...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(scanStatusMessage, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Status Card (Requirement 2, 6)
                        val isThreatInstalled = installedApps.any { it.contains("Trojan") || it.contains("BankBot") || it.contains("Malware") }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isThreatInstalled) Color(0xFFFDE8E8) else Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isThreatInstalled) Color(0xFFF8B4B4) else Color(0xFFA5D6A7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(if (isThreatInstalled) "🛑" else "🛡️", fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = if (isThreatInstalled) "Ameaça Crítica Detectada!" else "Nenhum app nocivo encontrado",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isThreatInstalled) Color(0xFF9B1C1C) else Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = if (isThreatInstalled) "O malware Trojan.BankBot foi instalado e representa perigos graves. Remova agora!" else "O Play Protect escaneou seus aplicativos instalados e não detectou comportamento suspeito.",
                                        fontSize = 10.sp,
                                        color = if (isThreatInstalled) Color(0xFF9B1C1C) else Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                isScanningPlayProtect = true
                                coroutineScope.launch {
                                    scanStatusMessage = "Scaneando Aurora Store para Trojan..."
                                    delay(400)
                                    scanStatusMessage = "Analisando telemetria do Pokémon GO..."
                                    delay(400)
                                    scanStatusMessage = "Auditando assinaturas de pacotes e APIs..."
                                    delay(450)
                                    scanStatusMessage = "Finalizando varredura profunda..."
                                    delay(300)
                                    isScanningPlayProtect = false
                                    Toast.makeText(context, "Varredura do Antivírus concluída com sucesso! Sistema estável.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("EXECUTAR VARREDURA COMPLETA AGORA 🔍", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Text(
                            text = "Histórico de Análise dos Apps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 200.dp).fillMaxWidth()
                        ) {
                            items(installedApps) { app ->
                                // Determine app risk parameters
                                val riskScore: Int
                                val riskLevel: String
                                val riskColor: Color
                                val detailDesc: String

                                when {
                                    app.contains("Oficial") || app.contains("Unciv") -> {
                                        riskScore = 0
                                        riskLevel = "Seguro"
                                        riskColor = Color(0xFF2E7D32)
                                        detailDesc = "Assinatura original Google Play GMS. Protegido."
                                    }
                                    app.contains("Aurora Store") -> {
                                        riskScore = 5
                                        riskLevel = "Baixo"
                                        riskColor = Color(0xFF2E7D32)
                                        detailDesc = "Navegador de pacotes livre de trackers e malware."
                                    }
                                    app.contains("PGSharp") -> {
                                        riskScore = 65
                                        riskLevel = "Médio-Alto (Modificado)"
                                        riskColor = Color(0xFFE65100)
                                        detailDesc = "Aplicativo Modificado / Não Oficial. Assinatura alterada com spoofing GPS."
                                    }
                                    app.contains("Trojan") || app.contains("BankBot") -> {
                                        riskScore = 98
                                        riskLevel = "Crítico (Malware)"
                                        riskColor = Color(0xFFC62828)
                                        detailDesc = "Trojan bancário detectado. Tenta acessar permissões do teclado."
                                    }
                                    else -> {
                                        riskScore = 15
                                        riskLevel = "Baixo"
                                        riskColor = Color(0xFF2E7D32)
                                        detailDesc = "Código limpo escaneado localmente."
                                    }
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(if (riskScore > 80) "🛑" else if (riskScore > 30) "⚠️" else "✓", fontSize = 16.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(app, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "$riskScore/100 ($riskLevel)",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = riskColor
                                                )
                                            }
                                            Text(detailDesc, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.uninstallVirtualApp(app)
                                                Toast.makeText(context, "$app removido do celular virtual de forma segura!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayStoreGameCard(
    title: String,
    category: String,
    rating: String,
    downloads: String,
    sizeDesc: String,
    emojiDisplay: String,
    progress: Int,
    isDownloading: Boolean,
    onInstallClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Game Cover / Icon Placeholder
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF202124)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emojiDisplay, fontSize = 20.sp, textAlign = TextAlign.Center)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = rating, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF01875F))
                        Text(text = "•", fontSize = 11.sp, color = Color.Gray)
                        Text(text = downloads, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "•", fontSize = 11.sp, color = Color.Gray)
                        Text(text = sizeDesc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Install or Play Action Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (progress in 1..99) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        CircularProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = Color(0xFF01875F)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Baixando seguro e instalando: $progress%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF01875F)
                        )
                    }
                } else {
                    Text(
                        text = if (progress == 100) "Verificado pelo Play Protect ✔️" else "Grátis para baixar e jogar",
                        fontSize = 11.sp,
                        color = if (progress == 100) Color(0xFF01875F) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (progress == 100) {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Jogar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isDownloading) {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pendente...", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onInstallClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Instalar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedPlayStoreCard(
    title: String,
    category: String,
    rating: String,
    sizeDesc: String,
    emojiDisplay: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2F)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emojiDisplay, fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = category,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = rating, fontSize = 10.sp, color = Color.Gray)
                    Text(text = "•", fontSize = 10.sp, color = Color.Gray)
                    Text(text = sizeDesc, fontSize = 10.sp, color = Color.Gray)
                }
            }

            // Real simulated external badge that prompts Google Play emulator behavior
            Button(
                onClick = {},
                enabled = false,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Incompatível", fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun TicTacToeGameScreen(onBack: () -> Unit) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var activePlayer by remember { mutableStateOf("X") }
    var gameResultMsg by remember { mutableStateOf<String?>(null) }

    var playerWins by remember { mutableStateOf(0) }
    var aiWins by remember { mutableStateOf(0) }
    var draws by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    // Helper functions
    fun checkWinner(b: List<String>): String? {
        val winPositions = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
            listOf(0, 4, 8), listOf(2, 4, 6)                  // diagonals
        )
        for (pos in winPositions) {
            if (b[pos[0]].isNotEmpty() && b[pos[0]] == b[pos[1]] && b[pos[1]] == b[pos[2]]) {
                return b[pos[0]]
            }
        }
        if (b.none { it.isEmpty() }) {
            return "Draw"
        }
        return null
    }

    fun resetGame() {
        board = List(9) { "" }
        activePlayer = "X"
        gameResultMsg = null
    }

    fun makeAiMove() {
        if (gameResultMsg != null) return

        coroutineScope.launch {
            delay(400) // Beautiful simulated thinking delay of AI
            val currentBoard = board.toMutableList()

            // AI strategy:
            // 1. Can AI win on next move?
            var aiMoveIndex = -1
            for (i in 0..8) {
                if (currentBoard[i].isEmpty()) {
                    currentBoard[i] = "O"
                    if (checkWinner(currentBoard) == "O") {
                        aiMoveIndex = i
                        break
                    }
                    currentBoard[i] = ""
                }
            }

            // 2. Can player win on next move? Block them!
            if (aiMoveIndex == -1) {
                for (i in 0..8) {
                    if (currentBoard[i].isEmpty()) {
                        currentBoard[i] = "X"
                        if (checkWinner(currentBoard) == "X") {
                            aiMoveIndex = i
                            break
                        }
                        currentBoard[i] = ""
                    }
                }
            }

            // 3. Take center if free
            if (aiMoveIndex == -1 && currentBoard[4].isEmpty()) {
                aiMoveIndex = 4
            }

            // 4. Random empty slot
            if (aiMoveIndex == -1) {
                val emptyIndices = currentBoard.indices.filter { currentBoard[it].isEmpty() }
                if (emptyIndices.isNotEmpty()) {
                    aiMoveIndex = emptyIndices.random()
                }
            }

            if (aiMoveIndex != -1) {
                val finalBoard = board.toMutableList()
                finalBoard[aiMoveIndex] = "O"
                board = finalBoard

                val winner = checkWinner(finalBoard)
                if (winner != null) {
                    if (winner == "O") {
                        gameResultMsg = "O Robô AI Venceu! 🤖"
                        aiWins += 1
                    } else if (winner == "Draw") {
                        gameResultMsg = "Empate Perfeito! 🤝"
                        draws += 1
                    }
                } else {
                    activePlayer = "X"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top action bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color(0xFF01875F))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Google Play: Jogo da Velha AI",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF01875F)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live score board
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Você (X)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$playerWins", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Divider(modifier = Modifier.height(28.dp).width(1.dp), color = Color.Gray.copy(alpha = 0.4f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Empates", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$draws", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Divider(modifier = Modifier.height(28.dp).width(1.dp), color = Color.Gray.copy(alpha = 0.4f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Robô (O)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$aiWins", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Message Bar
        Text(
            text = when {
                gameResultMsg != null -> gameResultMsg!!
                activePlayer == "X" -> "Sua vez! Coloque o X"
                else -> "Robô AI pensando..."
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (gameResultMsg != null) Color(0xFF01875F) else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3x3 Gaming grid styled beautifully
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            val value = board[index]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (value.isEmpty()) MaterialTheme.colorScheme.surfaceVariant
                                        else if (value == "X") MaterialTheme.colorScheme.primaryContainer
                                        else Color(0xFFFFEBEE)
                                    )
                                    .clickable {
                                        if (value.isEmpty() && activePlayer == "X" && gameResultMsg == null) {
                                            val nextBoard = board.toMutableList()
                                            nextBoard[index] = "X"
                                            board = nextBoard

                                            val winner = checkWinner(nextBoard)
                                            if (winner != null) {
                                                if (winner == "X") {
                                                    gameResultMsg = "Você Venceu! 🎉"
                                                    playerWins += 1
                                                } else if (winner == "Draw") {
                                                    gameResultMsg = "Empate Perfeito! 🤝"
                                                    draws += 1
                                                }
                                            } else {
                                                activePlayer = "O"
                                                makeAiMove()
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = value,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (value == "X") MaterialTheme.colorScheme.primary else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { resetGame() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875F)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nova Partida", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SnakeGameScreen(onBack: () -> Unit) {
    // Grid: 13 x 13 is perfect for smaller screens
    val gridWidth = 13
    val gridHeight = 13

    var snake by remember { mutableStateOf(listOf(Pair(6, 6), Pair(6, 7), Pair(6, 8))) }
    var direction by remember { mutableStateOf(Pair(0, -1)) } // Start direction: Up
    var apple by remember { mutableStateOf(Pair(3, 3)) }
    var snakeScore by remember { mutableStateOf(0) }
    var snakeHighScore by remember { mutableStateOf(0) }
    var isSnakeGameOver by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Resets snake
    fun startNewGame() {
        snake = listOf(Pair(6, 6), Pair(6, 7), Pair(6, 8))
        direction = Pair(0, -1)
        apple = Pair((1..(gridWidth - 2)).random(), (1..(gridHeight - 2)).random())
        snakeScore = 0
        isSnakeGameOver = false
    }

    // Tick/motion of classical game
    LaunchedEffect(isSnakeGameOver) {
        while (!isSnakeGameOver) {
            delay(280) // game speed tick rate millisecond
            
            val head = snake.firstOrNull() ?: Pair(6, 6)
            val nextHead = Pair(head.first + direction.first, head.second + direction.second)

            // Collision check: borders
            if (nextHead.first < 0 || nextHead.first >= gridWidth || nextHead.second < 0 || nextHead.second >= gridHeight) {
                isSnakeGameOver = true
                if (snakeScore > snakeHighScore) {
                    snakeHighScore = snakeScore
                }
                break
            }

            // Collision check: body
            if (snake.contains(nextHead)) {
                isSnakeGameOver = true
                if (snakeScore > snakeHighScore) {
                    snakeHighScore = snakeScore
                }
                break
            }

            val nextSnake = mutableListOf(nextHead)
            // Eat apple check
            if (nextHead == apple) {
                nextSnake.addAll(snake)
                snakeScore += 10
                // Generate next random apple location away from snake
                var newApple = Pair((1..(gridWidth-2)).random(), (1..(gridHeight-2)).random())
                while (nextSnake.contains(newApple)) {
                    newApple = Pair((1..(gridWidth-2)).random(), (1..(gridHeight-2)).random())
                }
                apple = newApple
            } else {
                nextSnake.addAll(snake.dropLast(1))
            }
            snake = nextSnake
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color(0xFF01875F))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Google Play: Cobrinha Retro Arcade",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF01875F)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // HighScore monitor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Frutas: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("🍎 $snakeScore", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Melhor: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$snakeHighScore pts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF01875F))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Retro Game LCD Console Frame container
        Surface(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = Color(0xFF1B261C), // Classic green-dark retro tone
            border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF425642))
        ) {
            if (isSnakeGameOver) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MORTA! 💀", color = Color(0xFFFF5252), fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pontos: $snakeScore", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { startNewGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF425642)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Recomeçar", color = Color.White)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0 until gridHeight) {
                        Row(
                            modifier = Modifier.height(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            for (col in 0 until gridWidth) {
                                val cell = Pair(col, row)
                                val isSnakeBody = snake.contains(cell)
                                val isSnakeHead = snake.isNotEmpty() && snake.first() == cell
                                val isApple = apple == cell

                                val cellColor = when {
                                    isSnakeHead -> Color(0xFF00FF66)
                                    isSnakeBody -> Color(0xFF81C784)
                                    isApple -> Color(0xFFFF5252)
                                    else -> Color(0xFF263326) // background dot representation grid
                                }

                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(cellColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fully tactile joystick directional keys pad for premium device comfort and touches
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Up button
            IconButton(
                onClick = {
                    if (direction != Pair(0, 1)) {
                        direction = Pair(0, -1)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF333E33))
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Cima", tint = Color.White)
            }

            // Left / Right controls row
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (direction != Pair(1, 0)) {
                            direction = Pair(-1, 0)
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333E33))
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Esquerda", tint = Color.White)
                }

                IconButton(
                    onClick = {
                        if (direction != Pair(-1, 0)) {
                            direction = Pair(1, 0)
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333E33))
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Direita", tint = Color.White)
                }
            }

            // Down button
            IconButton(
                onClick = {
                    if (direction != Pair(0, -1)) {
                        direction = Pair(0, 1)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF333E33))
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Baixo", tint = Color.White)
            }
        }
    }
}

@Composable
fun HardwareConfigurationDialog(
    currentRam: Int,
    currentStorage: Int,
    currentProcessor: String,
    currentVolume: Int,
    isSoundEnabled: Boolean,
    isPhysicalFullScreenEnabled: Boolean,
    virtualDeviceModel: String,
    onTogglePhysicalFullScreen: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onTriggerSoftwareUpdate: () -> Unit,
    androidVersion: String,
    securityPatchLevel: String,
    onOpenGoogleUpdateCenter: () -> Unit,
    onSave: (ram: Int, storage: Int, processor: String, volume: Int, soundEnabled: Boolean) -> Unit
) {
    var selectedRam by remember { mutableStateOf(currentRam) }
    var selectedStorage by remember { mutableStateOf(currentStorage) }
    var selectedProcessor by remember { mutableStateOf(currentProcessor) }
    var selectedVolume by remember { mutableStateOf(currentVolume) }
    var selectedSoundEnabled by remember { mutableStateOf(isSoundEnabled) }
    var selectedPhysicalFullScreen by remember { mutableStateOf(isPhysicalFullScreenEnabled) }
    
    var isSaving by remember { mutableStateOf(false) }
    var savingStepMessage by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val processorOptions = listOf(
        "MediaTek Dimensity 9400 Octa-Core @ 3.4 GHz",
        "Snapdragon 8 Gen 4 Octa-Core @ 4.09 GHz",
        "MediaTek Dimensity 7300 Ultra Octa-Core @ 2.5 GHz",
        "Snapdragon 8 Gen 3 Octa-Core @ 3.39 GHz",
        "MediaTek Dimensity 9300 Deca-Core @ 3.25 GHz"
    )
    val ramOptions = listOf(8, 12, 16, 24, 32)
    val storageOptions = listOf(128, 256, 512, 1024)

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            border = CardDefaults.outlinedCardBorder()
        ) {
            if (isSaving) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ajustando Hardware Virtual...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = savingStepMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚙️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Configurar Hardware Virtual",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Text(
                        text = "Customize as especificações físicas simuladas para o seu emulador Android do Guia AI.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    // Wrap form inside a scrollable column that takes up remaining space
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- GOOGLE SYSTEM UPDATE REGISTRATION ---
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ATUALIZAÇÃO DE SISTEMA (GOOGLE OTA)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57C00)
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4285F4).copy(alpha = 0.08f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4285F4).copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("🤖", fontSize = 20.sp)
                                        Column {
                                            Text(
                                                text = "Google Android Update Center",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Versão atual: $androidVersion | Patch: $securityPatchLevel",
                                                fontSize = 11.sp,
                                                color = Color(0xFF93C5FD)
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = "Gerencie as atualizações oficiais de sistema operacional e de segurança mensais da Google diretamente pelos servidores OTA oficiais.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        lineHeight = 15.sp
                                    )
                                    
                                    Button(
                                        onClick = { onOpenGoogleUpdateCenter() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Abrir Central de Atualizações 🔐", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // 1. Selector de Processador
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "PROCESSADOR (CPU)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        processorOptions.forEach { proc ->
                            val isSelected = selectedProcessor == proc
                            Surface(
                                onClick = { selectedProcessor = proc },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedProcessor = proc },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = proc,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 2. Selector de RAM
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "MEMÓRIA RAM: ${selectedRam} GB LPDDR5X",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ramOptions.forEach { ram ->
                                val isSelected = selectedRam == ram
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .clickable { selectedRam = ram }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${ram}GB",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 3. Selector de Armazenamento
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ARMAZENAMENTO (MEMÓRIA INTERNA): ${if (selectedStorage >= 1024) "1 TB" else "${selectedStorage} GB"} UFS 4.0",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            storageOptions.forEach { storage ->
                                val isSelected = selectedStorage == storage
                                val storageLabel = if (storage >= 1024) "1TB" else "${storage}GB"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .clickable { selectedStorage = storage }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = storageLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 4. ÁUDIO VIRTUAL DO EMULADOR (Novo!)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🔊 VOLUME DO EMULADOR: $selectedVolume%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (selectedVolume == 0) Icons.Default.VolumeOff else if (selectedVolume < 50) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                                contentDescription = "Volume",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Slider(
                                value = selectedVolume.toFloat(),
                                onValueChange = { selectedVolume = it.toInt() },
                                valueRange = 0f..150f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (selectedVolume > 100) "${selectedVolume}% 🚀" else "${selectedVolume}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedVolume > 100) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .clickable { selectedSoundEnabled = !selectedSoundEnabled }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🎧", fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = "Transmitir Áudio para o Dispositivo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Direciona o som do emulador e dos jogos (ex: Pokémon GO) para as caixas de som físicas.",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                            Switch(
                                checked = selectedSoundEnabled,
                                onCheckedChange = { selectedSoundEnabled = it },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }

                    // 5. ESCALA DO DISPOSITIVO FÍSICO (TELA CHEIA)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "📱 ESCALA DO CELULAR FÍSICO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .clickable { selectedPhysicalFullScreen = !selectedPhysicalFullScreen }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⛶", fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = "Aumentar Tela do Celular Físico",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Você pode aumentar a tela do seu celular físico para ficar no tamanho cheio total do celular virtual, eliminando todas as bordas e distorções.",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                            Switch(
                                checked = selectedPhysicalFullScreen,
                                onCheckedChange = { selectedPhysicalFullScreen = it },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }
                    } // Ends the scrollable Column wrapper

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                isSaving = true
                                coroutineScope.launch {
                                    savingStepMessage = "Efetuando checkout de partições sandbox no emulador..."
                                    delay(500)
                                    savingStepMessage = "Sincronizando saídas mixer virtuais (ALSA/WASAPI)..."
                                    delay(500)
                                    savingStepMessage = "Ajustando proporções para preencher celular físico..."
                                    delay(500)
                                    savingStepMessage = "Ajustando volume emulador para ${selectedVolume}%..."
                                    delay(400)
                                    onTogglePhysicalFullScreen(selectedPhysicalFullScreen)
                                    onSave(selectedRam, selectedStorage, selectedProcessor, selectedVolume, selectedSoundEnabled)
                                    Toast.makeText(context, "Configurações de Hardware, Áudio e Tela atualizadas com sucesso!", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Aplicar Configurações 🚀", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameBoosterCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isOptimizing by remember { mutableStateOf(false) }
    var optimizationProgress by remember { mutableStateOf(0f) }
    var optimizationStepText by remember { mutableStateOf("") }
    var isOptimized by remember { mutableStateOf(false) }
    
    // Toggles for specialized booster metrics
    var isFpsUnlocked by remember { mutableStateOf(true) }
    var isGpsStabilized by remember { mutableStateOf(true) }
    var isGpuAccelerated by remember { mutableStateOf(true) }
    var isCacheCleaned by remember { mutableStateOf(false) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE53935).copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE53935).copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_booster_ai_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🚀", fontSize = 24.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Game Booster AI & Redutor de Lag 🔥",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Sua internet ou jogos pesados (como Pokémon GO) travando? Melhore o ping, RAM e FPS simulados do emulador na nuvem.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
            
            HorizontalDivider(color = Color(0xFFE53935).copy(alpha = 0.15f))
            
            if (isOptimizing) {
                // Optimization ongoing layout
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = optimizationStepText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                text = "${(optimizationProgress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFC62828)
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = { optimizationProgress },
                            color = Color(0xFFD32F2F),
                            trackColor = Color(0xFFD32F2F).copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            } else {
                // Optimize Action Button
                Button(
                    onClick = {
                        isOptimizing = true
                        optimizationProgress = 0f
                        coroutineScope.launch {
                            try {
                                optimizationStepText = "Analisando gargalos de hardware virtual..."
                                optimizationProgress = 0.15f
                                delay(600)
                                optimizationStepText = "Liberando cache de renderização 3D (OpenGL/Vulcan)..."
                                optimizationProgress = 0.4f
                                delay(700)
                                optimizationStepText = "Limpando 3.8 GB de RAM virtual e limpando partições do Diário..."
                                optimizationProgress = 0.65f
                                delay(800)
                                optimizationStepText = "Estabilizando telemetria de GPS GPS-Smooth para Pokémon GO..."
                                optimizationProgress = 0.85f
                                delay(600)
                                optimizationProgress = 1.0f
                                isOptimized = true
                                isCacheCleaned = true
                                android.widget.Toast.makeText(context, "Emulador Otimizado! Modo Super Turbo habilitado para Pokémon GO e todos os jogos.", android.widget.Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                // ignore
                            } finally {
                                isOptimizing = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOptimized) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isOptimized) Icons.Default.CheckCircle else Icons.Default.FlashOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOptimized) "SISTEMA OTIMIZADO (SUPER TURBO ATIVO)" else "OTIMIZAR AGORA (LIMPAR LAG DOS JOGOS 🚀)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
            
            // Configuration controls rows
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Control 1: GPS Smooth
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isGpsStabilized = !isGpsStabilized }
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📍", fontSize = 16.sp)
                        Column {
                            Text(
                                "Estabilizar GPS do Pokémon GO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Evita a mensagem de Erro de Sinal GPS 11 e oscilação de coordenadas no simulador.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    Switch(
                        checked = isGpsStabilized,
                        onCheckedChange = { isGpsStabilized = it },
                        modifier = Modifier.scale(0.85f)
                    )
                }
                
                // Control 2: 120 FPS limit unlock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isFpsUnlocked = !isFpsUnlocked }
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📱", fontSize = 16.sp)
                        Column {
                            Text(
                                "Desbloquear Taxa de Quadros (120 FPS)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Força fluidez máxima de quadros usando re-interpolação por IA em tempo real.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    Switch(
                        checked = isFpsUnlocked,
                        onCheckedChange = { isFpsUnlocked = it },
                        modifier = Modifier.scale(0.85f)
                    )
                }
                
                // Control 3: GPU Virtual Compilation Accel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isGpuAccelerated = !isGpuAccelerated }
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎮", fontSize = 16.sp)
                        Column {
                            Text(
                                "Aceleração por Hardware de GPU Vulcan Virtual v3",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Resolve travamentos de renderização 3D e renderiza o mapa 80% mais rápido.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    Switch(
                        checked = isGpuAccelerated,
                        onCheckedChange = { isGpuAccelerated = it },
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }
            
            // Mini Diagnostic/Help box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFECEFF1).copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "❓ Dica do Técnico para Rodar Liso:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Como emuladores baseados em nuvem rodam através de compartilhamento de processos, abrir o painel de configurações (ícone de engrenagem no topo) e aumentar a memória RAM Virtual para 32 GB e o processador para Snapdragon 8 Gen 3 ajudará o servidor a alocar recursos exclusivos de GPU física H100 para o seu jogo, eliminando qualquer lag residual no Pokémon GO ou Unciv!",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

// --- INTERACTIVE TUTORIAL DIALOG ---
@Composable
fun InteractiveTutorialDialog(
    onDismiss: () -> Unit,
    onSelectTab: (Int) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 5

    val stepTitles = listOf(
        "Boas-vindas ao Celular Virtual & Diário AI 📱",
        "Anotações & Insights com Gemini AI 🧠",
        "Google Play Store, Jogos & Antivírus AI 🛡️",
        "Configurações de Hardware, Root & OTA ⚙️",
        "Sistema de Salvamento & Backup Cloud 💾"
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🎓",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Tutorial Interativo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Passo $currentStep de $totalSteps",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar Tutorial", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Progress Indicator
                LinearProgressIndicator(
                    progress = currentStep.toFloat() / totalSteps.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )

                // Step Content Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stepTitles[currentStep - 1],
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        when (currentStep) {
                            1 -> {
                                Text(
                                    text = "Seja bem-vindo ao ambiente de celular virtual Android com IA integrada! Aqui você encontra um ambiente completo com alta performance.",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StepDetailRow("📱 Dispositivo Virtual", "Redmi 15 com Android 14 HyperOS oficial e suporte total.")
                                StepDetailRow("⚡ Ações Rápidas", "Acesse abas inferiores ou puxe a barra superior para notificações.")
                            }
                            2 -> {
                                Text(
                                    text = "Na aba Diário e na Sessão AI, você pode registrar o que está sentindo e conversar com a inteligência artificial Gemini.",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StepDetailRow("✍️ Registro Pessoal", "Toque no botão '+' para salvar memórias e categorizar seu humor.")
                                StepDetailRow("💡 Insights de IA", "Clique em 'Insights IA' no Diário para receber análises profundas do Gemini.")
                                StepDetailRow("⏳ Indicador Visual", "Um indicador circular avisará quando o Gemini estiver processando.")
                            }
                            3 -> {
                                Text(
                                    text = "Acesse a Play Store integrada para navegar por aplicativos, rodar jogos arcade e manter seu celular protegido.",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StepDetailRow("🛍️ Play Store & Aurora", "Baixe APKs e instale aplicativos no sandbox virtual.")
                                StepDetailRow("🎮 Jogos Embutidos", "Jogue Cobrinha Retro, Jogo da Velha AI e Pixel Dungeon.")
                                StepDetailRow("🛡️ Antivírus Heurístico", "Execute varreduras preventivas com Google Antivírus & Bitdefender.")
                            }
                            4 -> {
                                Text(
                                    text = "Configure recursos de hardware avançados e aproveite o acesso a superusuário e atualizações oficiais.",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StepDetailRow("⚙️ RAM & Armazenamento", "Defina até 32 GB de RAM e 1 TB de memória interna UFS 4.0.")
                                StepDetailRow("⚡ Root Verdadeiro", "Execute comandos shell Linux reais diretamente no terminal root.")
                                StepDetailRow("🔄 Atualizações OTA", "Receba novos patches de segurança oficiais da Google em 1 toque.")
                            }
                            5 -> {
                                Text(
                                    text = "Nunca perca seu progresso! Nosso sistema permite salvar instantaneamente seu progresso e restaurá-lo a qualquer momento.",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StepDetailRow("💾 Salvar Progresso", "Clique em 'Salvar Tudo' na aba Nuvem Cloud ou no menu superior.")
                                StepDetailRow("📂 Restaurar Ponto", "Restaure diários, dados de jogos e preferências salvas com 1 clique.")
                                StepDetailRow("☁️ Nuvem Google Drive", "Mantenha backups em nuvem sempre sincronizados.")
                            }
                        }
                    }
                }

                // Interactive Quick Jump Tab Button
                OutlinedButton(
                    onClick = {
                        val targetTab = when (currentStep) {
                            1 -> 0
                            2 -> 1
                            3 -> 2
                            4 -> 3
                            5 -> 4
                            else -> 0
                        }
                        onSelectTab(targetTab)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (currentStep) {
                            1 -> "Ir para a aba Diário 📖"
                            2 -> "Ir para a aba Sessão AI 🧠"
                            3 -> "Ir para a Play Store 🛍️"
                            4 -> "Ir para a aba Instalador APK ⚡"
                            else -> "Ir para a aba Nuvem Cloud ☁️"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bottom Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Anterior", fontSize = 11.sp)
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Pular", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentStep < totalSteps) "Próximo ➡️" else "Concluir Tutorial 🎉",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepDetailRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Column {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
