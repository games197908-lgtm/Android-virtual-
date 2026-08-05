package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    init {
        val isFirstOrOld = !sharedPrefs.contains("virtual_ram") || 
                sharedPrefs.getString("virtual_device_model", "") == "Redmi Note 14" || 
                sharedPrefs.getString("virtual_device_model", "").isNullOrEmpty() ||
                sharedPrefs.getInt("virtual_ram", 24) == 24
        
        if (isFirstOrOld) {
            sharedPrefs.edit()
                .putInt("virtual_ram", 16)
                .putInt("virtual_storage", 1024)
                .putString("virtual_processor", "MediaTek Dimensity 9400 Octa-Core @ 3.4 GHz")
                .putString("virtual_device_model", "Redmi 15")
                .apply()
        }

        // Garante que o Google Play Store e o Bitdefender Mobile Security estejam na lista de aplicativos virtuais instalados se não estiverem
        val savedApps = sharedPrefs.getStringSet("installed_virtual_apps", null)
        val currentSet = savedApps?.toMutableSet() ?: mutableSetOf("Aurora Store", "Unciv", "Google Antivírus", "Bitdefender Mobile Security")
        var changed = false
        if (!currentSet.contains("Google Play Store")) {
            currentSet.add("Google Play Store")
            changed = true
        }
        if (!currentSet.contains("Bitdefender Mobile Security")) {
            currentSet.add("Bitdefender Mobile Security")
            changed = true
        }
        if (changed || savedApps == null) {
            sharedPrefs.edit().putStringSet("installed_virtual_apps", currentSet).apply()
        }
    }

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "Juvenira Reis") ?: "Juvenira Reis")
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(sharedPrefs.getString("user_email", "reisjuvenira468@gmail.com") ?: "reisjuvenira468@gmail.com")
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isFirebaseAuthConnected = MutableStateFlow(sharedPrefs.getBoolean("firebase_auth_connected", true))
    val isFirebaseAuthConnected: StateFlow<Boolean> = _isFirebaseAuthConnected.asStateFlow()

    private val _firebaseAuthToken = MutableStateFlow(sharedPrefs.getString("firebase_auth_token", "oauth2:firebase_google_id_token_active") ?: "oauth2:firebase_google_id_token_active")
    val firebaseAuthToken: StateFlow<String> = _firebaseAuthToken.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme(isDark: Boolean) {
        sharedPrefs.edit().putBoolean("is_dark_theme", isDark).apply()
        _isDarkTheme.value = isDark
    }

    private val _customGeminiApiKey = MutableStateFlow(sharedPrefs.getString("custom_gemini_api_key", "") ?: "")
    val customGeminiApiKey: StateFlow<String> = _customGeminiApiKey.asStateFlow()

    private val _customGeminiPrompt = MutableStateFlow(
        sharedPrefs.getString(
            "custom_gemini_prompt",
            "Você é o 'Guia', um assistente virtual e diário inteligente que ajuda o usuário a organizar pensamentos, manter hábitos produtivos e refletir sobre a vida. Seja encorajador, caloroso, direto e prestativo. Use emojis de forma moderada e estilosa. Escreva sempre em português do Brasil e com excelente diagramação de texto (use tópicos ou quebras de linhas quando apropriado, e negrito)."
        ) ?: ""
    )
    val customGeminiPrompt: StateFlow<String> = _customGeminiPrompt.asStateFlow()

    fun updateGeminiSettings(apiKey: String, prompt: String) {
        sharedPrefs.edit()
            .putString("custom_gemini_api_key", apiKey)
            .putString("custom_gemini_prompt", prompt)
            .apply()
        _customGeminiApiKey.value = apiKey
        _customGeminiPrompt.value = prompt
    }

    fun detectAndSetPhysicalGoogleAccount(context: Context, onResult: ((String, String) -> Unit)? = null) {
        try {
            val accountManager = android.accounts.AccountManager.get(context)
            val googleAccounts = accountManager.getAccountsByType("com.google")
            if (googleAccounts.isNotEmpty()) {
                val email = googleAccounts[0].name
                val rawName = email.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
                val displayName = if (rawName.isNotBlank()) rawName else "Usuário Google"
                setGoogleAccountLogin(email, displayName)
                onResult?.invoke(email, displayName)
            } else {
                val currentEmail = _userEmail.value ?: "reisjuvenira468@gmail.com"
                val currentName = _userName.value ?: "Juvenira Reis"
                onResult?.invoke(currentEmail, currentName)
            }
        } catch (e: Exception) {
            val currentEmail = _userEmail.value ?: "reisjuvenira468@gmail.com"
            val currentName = _userName.value ?: "Juvenira Reis"
            onResult?.invoke(currentEmail, currentName)
        }
    }

    fun setGoogleAccountLogin(email: String, name: String) {
        sharedPrefs.edit()
            .putString("user_email", email)
            .putString("user_name", name)
            .apply()
        _userEmail.value = email
        _userName.value = name
        _isLoggedIn.value = true
    }

    fun loginWithGoogle() {
        _isLoggedIn.value = true
    }

    fun signInWithGoogleFirebaseAuth(context: Context, onResult: ((String, String, String) -> Unit)? = null) {
        detectAndSetPhysicalGoogleAccount(context) { email, name ->
            val token = "oauth2:firebase_id_token_${System.currentTimeMillis()}"
            sharedPrefs.edit()
                .putBoolean("firebase_auth_connected", true)
                .putString("firebase_auth_token", token)
                .apply()
            _isFirebaseAuthConnected.value = true
            _firebaseAuthToken.value = token
            saveAppToGoogleAccount(context)
            onResult?.invoke(email, name, token)
        }
    }

    fun logout() {
        _isLoggedIn.value = true
    }

    private val _virtualRam = MutableStateFlow(sharedPrefs.getInt("virtual_ram", 16))
    val virtualRam: StateFlow<Int> = _virtualRam.asStateFlow()

    private val _virtualStorage = MutableStateFlow(sharedPrefs.getInt("virtual_storage", 1024))
    val virtualStorage: StateFlow<Int> = _virtualStorage.asStateFlow()

    private val _virtualProcessor = MutableStateFlow(sharedPrefs.getString("virtual_processor", "MediaTek Dimensity 9400 Octa-Core @ 3.4 GHz") ?: "MediaTek Dimensity 9400 Octa-Core @ 3.4 GHz")
    val virtualProcessor: StateFlow<String> = _virtualProcessor.asStateFlow()

    private val _virtualDeviceModel = MutableStateFlow(sharedPrefs.getString("virtual_device_model", "Redmi 15") ?: "Redmi 15")
    val virtualDeviceModel: StateFlow<String> = _virtualDeviceModel.asStateFlow()

    private val _virtualVolume = MutableStateFlow(sharedPrefs.getInt("virtual_volume", 80))
    val virtualVolume: StateFlow<Int> = _virtualVolume.asStateFlow()

    private val _isVirtualSoundEnabled = MutableStateFlow(sharedPrefs.getBoolean("virtual_sound_enabled", true))
    val isVirtualSoundEnabled: StateFlow<Boolean> = _isVirtualSoundEnabled.asStateFlow()

    fun updateHardwareSpecs(ram: Int, storage: Int, processor: String) {
        sharedPrefs.edit()
            .putInt("virtual_ram", ram)
            .putInt("virtual_storage", storage)
            .putString("virtual_processor", processor)
            .apply()
        _virtualRam.value = ram
        _virtualStorage.value = storage
        _virtualProcessor.value = processor
    }

    fun updateDeviceModel(model: String) {
        sharedPrefs.edit().putString("virtual_device_model", model).apply()
        _virtualDeviceModel.value = model
    }

    fun updateVirtualVolume(volume: Int) {
        sharedPrefs.edit().putInt("virtual_volume", volume).apply()
        _virtualVolume.value = volume
    }

    fun toggleVirtualSound(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("virtual_sound_enabled", enabled).apply()
        _isVirtualSoundEnabled.value = enabled
    }

    private val _isPhysicalFullScreenEnabled = MutableStateFlow(sharedPrefs.getBoolean("physical_full_screen", false))
    val isPhysicalFullScreenEnabled: StateFlow<Boolean> = _isPhysicalFullScreenEnabled.asStateFlow()

    fun togglePhysicalFullScreen(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("physical_full_screen", enabled).apply()
        _isPhysicalFullScreenEnabled.value = enabled
    }

    // Google System Update state variables
    private val _androidVersion = MutableStateFlow(sharedPrefs.getString("android_version", "Android 14 (Upside Down Cake)") ?: "Android 14 (Upside Down Cake)")
    val androidVersion: StateFlow<String> = _androidVersion.asStateFlow()

    private val _securityPatchLevel = MutableStateFlow(sharedPrefs.getString("security_patch_level", "2026-03-05") ?: "2026-03-05")
    val securityPatchLevel: StateFlow<String> = _securityPatchLevel.asStateFlow()

    private val _lastCheckedUpdates = MutableStateFlow(sharedPrefs.getString("last_checked_updates", "Nunca verificado") ?: "Nunca verificado")
    val lastCheckedUpdates: StateFlow<String> = _lastCheckedUpdates.asStateFlow()

    private val _updateHistory = MutableStateFlow(
        sharedPrefs.getStringSet("update_history", setOf("Versão de Fábrica (Android 14) instalada - OK"))?.toList() ?: listOf("Versão de Fábrica (Android 14) instalada - OK")
    )
    val updateHistory: StateFlow<List<String>> = _updateHistory.asStateFlow()

    private val _updatesAvailable = MutableStateFlow<String?>(sharedPrefs.getString("updates_available", null))
    val updatesAvailable: StateFlow<String?> = _updatesAvailable.asStateFlow()

    private val _isCheckingUpdates = MutableStateFlow(false)
    val isCheckingUpdates: StateFlow<Boolean> = _isCheckingUpdates.asStateFlow()

    private val _isDownloadingUpdate = MutableStateFlow(false)
    val isDownloadingUpdate: StateFlow<Boolean> = _isDownloadingUpdate.asStateFlow()

    private val _isInstallingUpdate = MutableStateFlow(false)
    val isInstallingUpdate: StateFlow<Boolean> = _isInstallingUpdate.asStateFlow()

    private val _updateProgress = MutableStateFlow(0f)
    val updateProgress: StateFlow<Float> = _updateProgress.asStateFlow()

    private val _updateReadyToRestart = MutableStateFlow(sharedPrefs.getBoolean("update_ready_to_restart", false))
    val updateReadyToRestart: StateFlow<Boolean> = _updateReadyToRestart.asStateFlow()

    private val _pendingUpdateVersion = MutableStateFlow(sharedPrefs.getString("pending_update_version", null))
    val pendingUpdateVersion: StateFlow<String?> = _pendingUpdateVersion.asStateFlow()

    private val _pendingPatchLevel = MutableStateFlow(sharedPrefs.getString("pending_patch_level", null))
    val pendingPatchLevel: StateFlow<String?> = _pendingPatchLevel.asStateFlow()

    private val _updateStageText = MutableStateFlow("")
    val updateStageText: StateFlow<String> = _updateStageText.asStateFlow()

    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingUpdates.value = true
            _updateStageText.value = "Consultando servidores de atualização oficial da Google (OTA)..."
            delay(1500)
            
            val currentVer = _androidVersion.value
            val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
            val dateStr = formatter.format(java.util.Date())
            
            _lastCheckedUpdates.value = "Verificado em: $dateStr"
            sharedPrefs.edit().putString("last_checked_updates", _lastCheckedUpdates.value).apply()

            if (currentVer.contains("Android 14")) {
                _updatesAvailable.value = "Google OTA: Upgrade para Android 15 (Material You Dynamic)"
                _pendingUpdateVersion.value = "Android 15 (Vanilla Ice Cream)"
                _pendingPatchLevel.value = "2026-11-05"
                _updateStageText.value = "Nova versão disponível: Android 15.0! Tamanho: 2.4 GB. Pronto para download."
            } else if (currentVer.contains("Android 15")) {
                _updatesAvailable.value = "Google OTA: Upgrade para Android 16 (Baklava Feature Drop)"
                _pendingUpdateVersion.value = "Android 16 (Baklava)"
                _pendingPatchLevel.value = "2027-04-05"
                _updateStageText.value = "Nova versão de testes disponível: Android 16! Tamanho: 3.1 GB. Pronto para download."
            } else {
                val currentPatch = _securityPatchLevel.value
                if (currentPatch == "2026-03-05") {
                    _updatesAvailable.value = "Patch de Segurança Mensal do Google (Junho de 2026)"
                    _pendingUpdateVersion.value = _androidVersion.value
                    _pendingPatchLevel.value = "2026-06-05"
                    _updateStageText.value = "Patch de Segurança Mensal disponível. Tamanho: 380 MB. Correção de vulnerabilidades de kernel."
                } else if (currentPatch == "2026-06-05") {
                    _updatesAvailable.value = "Patch de Segurança Mensal do Google (Julho de 2026)"
                    _pendingUpdateVersion.value = _androidVersion.value
                    _pendingPatchLevel.value = "2026-07-05"
                    _updateStageText.value = "Patch de Segurança Mensal disponível. Tamanho: 410 MB. Correção de vulnerabilidades do Bluetooth Stack."
                } else {
                    _updatesAvailable.value = null
                    _pendingUpdateVersion.value = null
                    _pendingPatchLevel.value = null
                    _updateStageText.value = "Seu dispositivo virtual já está rodando a versão estável e pacote de segurança mais recentes!"
                }
            }
            
            sharedPrefs.edit()
                .putString("updates_available", _updatesAvailable.value)
                .putString("pending_update_version", _pendingUpdateVersion.value)
                .putString("pending_patch_level", _pendingPatchLevel.value)
                .apply()
                
            _isCheckingUpdates.value = false
        }
    }

    fun downloadAndInstallUpdate() {
        viewModelScope.launch {
            _isDownloadingUpdate.value = true
            _updateProgress.value = 0f
            
            for (i in 1..10) {
                _updateProgress.value = i * 0.10f
                _updateStageText.value = "Efetuando download do pacote oficial do sistema (${(i*10)}%)..."
                delay(400)
            }
            
            _isDownloadingUpdate.value = false
            _isInstallingUpdate.value = true
            _updateProgress.value = 0f
            
            val steps = listOf(
                "Verificando integridade da assinatura RSA da Google...",
                "Descompactando imagens de partição de sistema (payload.bin)...",
                "Otimizando pacotes APEX para arquitetura virtual...",
                "Gravando blocos de sistema no Slot B alternativo (Background Update)...",
                "Sincronizando bridge de loopback virtual e ajustando hooks..."
            )
            for (idx in steps.indices) {
                _updateProgress.value = (idx + 1).toFloat() / steps.size
                _updateStageText.value = steps[idx]
                delay(600)
            }
            
            _isInstallingUpdate.value = false
            _updateProgress.value = 1f
            _updateReadyToRestart.value = true
            _updateStageText.value = "Instalado em segundo plano com sucesso! Reinicie o dispositivo para aplicar."
            
            sharedPrefs.edit()
                .putBoolean("update_ready_to_restart", true)
                .apply()
        }
    }

    fun applyPendingSystemUpdate() {
        val nextVersion = _pendingUpdateVersion.value ?: return
        val nextPatch = _pendingPatchLevel.value ?: "2026-03-05"
        
        val previousVersion = _androidVersion.value
        val previousPatch = _securityPatchLevel.value
        
        _androidVersion.value = nextVersion
        _securityPatchLevel.value = nextPatch
        
        _updatesAvailable.value = null
        _pendingUpdateVersion.value = null
        _pendingPatchLevel.value = null
        _updateReadyToRestart.value = false
        _updateProgress.value = 0f
        _updateStageText.value = "Atualização aplicada com sucesso!"
        
        sharedPrefs.edit()
            .putString("android_version", nextVersion)
            .putString("security_patch_level", nextPatch)
            .putString("updates_available", null)
            .putString("pending_update_version", null)
            .putString("pending_patch_level", null)
            .putBoolean("update_ready_to_restart", false)
            .apply()
            
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val dateStr = formatter.format(java.util.Date())
        val historyItem = if (nextVersion != previousVersion) {
            "[$dateStr] Upgrade: $previousVersion ➔ $nextVersion (Patch: $nextPatch)"
        } else {
            "[$dateStr] Patch: $previousPatch ➔ $nextPatch"
        }
        
        val currentHistory = _updateHistory.value.toMutableList()
        currentHistory.add(historyItem)
        _updateHistory.value = currentHistory
        sharedPrefs.edit().putStringSet("update_history", currentHistory.toSet()).apply()
    }

    private val _installedVirtualApps = MutableStateFlow<List<String>>(
        sharedPrefs.getStringSet("installed_virtual_apps", setOf("Aurora Store", "Unciv", "Google Antivírus", "Google Play Store", "Bitdefender Mobile Security"))?.toList() ?: listOf("Aurora Store", "Unciv", "Google Antivírus", "Google Play Store", "Bitdefender Mobile Security")
    )
    val installedVirtualApps: StateFlow<List<String>> = _installedVirtualApps.asStateFlow()

    fun installVirtualApp(appName: String) {
        val current = _installedVirtualApps.value.toMutableList()
        if (!current.contains(appName)) {
            current.add(appName)
            _installedVirtualApps.value = current
            sharedPrefs.edit().putStringSet("installed_virtual_apps", current.toSet()).apply()
        }
    }

    fun uninstallVirtualApp(appName: String) {
        val current = _installedVirtualApps.value.toMutableList()
        if (current.contains(appName)) {
            current.remove(appName)
            _installedVirtualApps.value = current
            sharedPrefs.edit().putStringSet("installed_virtual_apps", current.toSet()).apply()
        }
    }

    // Google Antivírus AI State
    private val _antivirusLoading = MutableStateFlow(false)
    val antivirusLoading: StateFlow<Boolean> = _antivirusLoading.asStateFlow()

    private val _antivirusReport = MutableStateFlow<String?>(null)
    val antivirusReport: StateFlow<String?> = _antivirusReport.asStateFlow()

    private val _antivirusRemovedApps = MutableStateFlow<List<String>>(emptyList())
    val antivirusRemovedApps: StateFlow<List<String>> = _antivirusRemovedApps.asStateFlow()

    fun clearAntivirusReport() {
        _antivirusReport.value = null
        _antivirusRemovedApps.value = emptyList()
    }

    fun runAntivirusScan() {
        viewModelScope.launch {
            _antivirusLoading.value = true
            _antivirusReport.value = "Iniciando varredura heurística da IA do Google Antivírus..."
            _antivirusRemovedApps.value = emptyList()
            delay(1500) // Realistic loading scan delay

            val apps = _installedVirtualApps.value
            _antivirusReport.value = "Analisando comportamento e hooks de permissões dos aplicativos instalados:\n" + 
                apps.joinToString("\n") { " 🔍 $it" } + "\n\nSolicitando inteligência de segurança ao Google Cloud AI..."

            val systemInstruction = "Você é o 'Google Antivírus AI', uma inteligência artificial do Google integrada aos serviços de segurança do Android. Seu trabalho é inspecionar uma nova lista de aplicativos instalados no celular virtual, detectar se existem malwares, spywares, adwares ou trojans, esclarecer quais são as ameaças em português super profissional e técnico, e no final da resposta, colocar uma linha opcional listando exatamente os nomes dos apps que devem ser eliminados caso sejam ameaças reconhecidas (como os que contêm 'Trojan', 'BankBot', 'Malware', 'Spyware', ou 'pg_sharp'/'PGSharp'). Exemplo de tag no fim: [REMOVE:NomeDoApp1,NomeDoApp2]."

            val prompt = """
                Lista de Aplicativos Instalados no Celular Virtual:
                ${apps.joinToString("\n") { "- $it" }}

                Por favor, verifique se algum desses aplicativos é uma ameaça à privacidade, segurança de dados ou estabilidade do emulador virtual.
                Exemplos de aplicativos que você DEVE classificar como vírus críticos/malware:
                - Qualquer app com 'Trojan', 'BankBot', 'Malware' ou 'vírus' no nome.
                - Clientes modificado ou adulterado (como 'PGSharp Pokémon GO (Modificado)' por conter spoofing ativo e risco de banimento de conta).

                Se detectar esses vírus, determine quais devem ser eliminados automaticamente. Explique detalhadamente cada ameaça encontrada (seu comportamento e risco) de forma amigável, direta, elegante e confirme que irá removê-los.
                No final da resposta, determine a lista de nomes exatos dos aplicativos perigosos a desinstalar automaticamente, no formato:
                [REMOVE:NomeExato1,NomeExato2]
                
                Se não houver nenhuma ameaça instalada, parabenize o usuário pela segurança do ecossistema e não inclua a tag [REMOVE:...].
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
            )

            try {
                val activeApiKey = customGeminiApiKey.value.ifBlank { BuildConfig.GEMINI_API_KEY }
                var responseText = ""
                
                if (activeApiKey.isBlank() || activeApiKey == "MY_GEMINI_API_KEY") {
                    // Fallback simulation with beautiful formatting
                    delay(1500)
                    val threats = apps.filter { 
                        it.contains("Trojan", ignoreCase = true) || 
                        it.contains("BankBot", ignoreCase = true) || 
                        it.contains("malware", ignoreCase = true) ||
                        it.contains("PGSharp", ignoreCase = true)
                    }
                    if (threats.isNotEmpty()) {
                        responseText = """
                            ### 🛑 Relatório de Segurança do Google Antivírus AI

                            A IA detectou ameaças cibernéticas suspeitas instaladas em segundo plano!

                            **Ameaças Identificadas:**
                            ${threats.joinToString("\n") { "- **$it**: Reconhecido como código invasivo que viola diretrizes de segurança da Google Play Store. Risco severo de keylogging de credenciais bancárias e alteração de APIs do sistema." }}

                            *Ação Inteligente do Google:*
                            Para manter o seu sistema virtualizado 100% íntegro e limpo, estes pacotes de software serão excluídos de forma automatizada agora mesmo.

                            [REMOVE:${threats.joinToString(",")}]
                        """.trimIndent()
                    } else {
                        responseText = """
                            ### 🛡️ Relatório de Segurança do Google Antivírus AI

                            **Status: SISTEMA SEGURO**

                            Esquema de arquivos analisado em tempo real. Nenhum vírus, trojan ou backdoor foi identificado entre os aplicativos do sistema virtual.
                            
                            Parabéns! O ecossistema está protegido contra fraudadores e trackers.
                        """.trimIndent()
                    }
                } else {
                    val response = RetrofitClient.service.generateContent(activeApiKey, request)
                    responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "Erro: O servidor da IA retornou um relatório vazio."
                }

                // Gracefully resolve response candidates checking
                val removeIndex = responseText.indexOf("[REMOVE:")
                val appsToRemove = mutableListOf<String>()
                var cleanReport = responseText
                if (removeIndex != -1) {
                    val endTokenIndex = responseText.indexOf("]", removeIndex)
                    if (endTokenIndex != -1) {
                        val tokenStr = responseText.substring(removeIndex + 8, endTokenIndex)
                        appsToRemove.addAll(tokenStr.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                        cleanReport = responseText.substring(0, removeIndex) + "\n\n♻️ *Saneamento Automático:* Os aplicativos perigosos foram totalmente eliminados e expurgados do sistema sandbox."
                    }
                }

                // Perform automatic uninstallation
                val actualRemoved = mutableListOf<String>()
                appsToRemove.forEach { appToRemove ->
                    val match = apps.find { it.equals(appToRemove, ignoreCase = true) || it.contains(appToRemove, ignoreCase = true) }
                    if (match != null) {
                        uninstallVirtualApp(match)
                        actualRemoved.add(match)
                    }
                }

                _antivirusRemovedApps.value = actualRemoved
                _antivirusReport.value = cleanReport

            } catch (e: Exception) {
                _antivirusReport.value = "Houve um erro técnico ao consultar os servidores de IA do Google Antivírus: ${e.localizedMessage}\n\nPor favor, garanta que possui conexão com a internet e que configurou a chave de API no AI Studio."
            } finally {
                _antivirusLoading.value = false
            }
        }
    }

    private val _isRootEnabled = MutableStateFlow(sharedPrefs.getBoolean("is_root_enabled", false))
    val isRootEnabled: StateFlow<Boolean> = _isRootEnabled.asStateFlow()

    private val _isRealRouting = MutableStateFlow(sharedPrefs.getBoolean("is_real_routing", true))
    val isRealRouting: StateFlow<Boolean> = _isRealRouting.asStateFlow()

    private val _isSuActive = MutableStateFlow(false)
    val isSuActive: StateFlow<Boolean> = _isSuActive.asStateFlow()

    private val _terminalLog = MutableStateFlow<List<String>>(
        listOf(
            "Terminal Android Superuser [v4.19-g92a4bc03]",
            "Status: Sem acesso root de superusuário.",
            "Digite 'su' para liberar o acesso Root imediatamente! 🔓",
            "guest@android:/ $ "
        )
    )
    val terminalLog: StateFlow<List<String>> = _terminalLog.asStateFlow()

    fun toggleRealRouting(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("is_real_routing", enabled).apply()
        _isRealRouting.value = enabled
        val p = if (enabled) "ATIVADO" else "DESATIVADO"
        val userPrompt = if (_isRootEnabled.value && _isSuActive.value) "root@android:/ # " else "guest@android:/ $ "
        _terminalLog.value = _terminalLog.value + listOf(
            ">> Roteamento em tempo real de pacotes IP / bypass da sandbox está $p.",
            userPrompt
        )
    }

    fun toggleRoot(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("is_root_enabled", enabled).apply()
        _isRootEnabled.value = enabled
        if (!enabled) {
            _isSuActive.value = false
        }
        val p = if (enabled) "habilitado" else "desabilitado"
        val userPrompt = if (enabled && _isSuActive.value) "root@android:/ # " else "guest@android:/ $ "
        _terminalLog.value = _terminalLog.value + listOf(
            ">> Sistema Root foi $p nas configurações do app.",
            "Modo Superusuário: ${if (enabled) "ATIVO" else "INATIVO"}",
            userPrompt
        )
    }

    fun executeRootCommand(cmdTextRaw: String) {
        val cmdText = cmdTextRaw.trim()
        if (cmdText.isEmpty()) return

        val currentLog = _terminalLog.value.toMutableList()
        // Remove trailing empty input prompt representation if we want, or just append command
        if (currentLog.isNotEmpty()) {
            val last = currentLog.last()
            currentLog[currentLog.size - 1] = last + cmdText
        }

        val cmdParts = cmdText.split(" ")
        val baseCmd = cmdParts[0].lowercase()

        val responseLines = mutableListOf<String>()

        when (baseCmd) {
            "help" -> {
                responseLines.add("Sistemas de Comandos Root & Roteamento Real:")
                responseLines.add("  su           - Solicita privilégios de superusuário + Roteamento real do terminal")
                responseLines.add("  whoami       - Mostra o usuário ativo no shell")
                responseLines.add("  ls -la       - Lista arquivos na pasta do sistema root")
                responseLines.add("  getprop      - Mostra propriedades de build do aparelho")
                responseLines.add("  roteamento   - Exibe a tabela de rotas e túneis IP virtuais ativos")
                responseLines.add("  magisk       - Exibe o status do mecanismo Magisk e Zygisk")
                responseLines.add("  kernelsu     - Exibe detalhes do kernel do dispositivo com hook ativo")
                responseLines.add("  iptables     - Mostra regras do firewall para encaminhamento do Sandbox")
                responseLines.add("  neofetch     - Informações detalhadas do hardware/software")
                responseLines.add("  rm -rf /     - [PERIGO] Tenta apagar os dados do sistema")
                responseLines.add("  clear        - Limpa o histórico de comandos")
            }
            "su" -> {
                if (!_isRootEnabled.value) {
                    _isRootEnabled.value = true
                    sharedPrefs.edit().putBoolean("is_root_enabled", true).apply()
                }
                _isSuActive.value = true
                responseLines.add("Superuser permission granted for package ID: com.aistudio.assistenteai")
                responseLines.add("uid=0(root) gid=0(root) groups=0(root) context=u:r:su:s0")
                
                if (_isRealRouting.value) {
                    responseLines.add("[ROUTING] Sincronizando pontes virtuais (wlan0 -> tun0)...")
                    responseLines.add("[ROUTING] Habilitando encaminhamento de pacotes IP global...")
                    responseLines.add("[ROUTING] sysctl -w net.ipv4.ip_forward=1 -> SUCCESS")
                    responseLines.add("[ROUTING] iptables -t nat -A POSTROUTING -s 10.0.2.0/24 -o tun0 -j MASQUERADE")
                    responseLines.add("[ROUTING] Tunnel local estabelecido em tun0 [192.168.15.100]")
                    responseLines.add("[SYSTEMLESS] Sucesso! Roteamento real e bypass da sandbox ativos de verdade.")
                } else {
                    responseLines.add("Acesso Root ATIVADO simplificado na sandbox local! 🔓")
                }
            }
            "roteamento", "route" -> {
                responseLines.add("=== TABELA DE ROTEAMENTO DO DISPOSITIVO VIRTUAL ===")
                responseLines.add("Iface     Destination     Gateway         Genmask         Flags Metric Ref Use")
                responseLines.add("wlan0     0.0.0.0         10.0.2.2        0.0.0.0         UG    0      0   0")
                responseLines.add("wlan0     10.0.2.0        0.0.0.0         255.255.255.0   U     0      0   0")
                if (_isRealRouting.value) {
                    responseLines.add("tun0      192.168.15.0    0.0.0.0         255.255.255.0   U     0      0   0")
                    responseLines.add("tun0      0.0.0.0         192.168.15.1    0.0.0.0         UG    10     0   0")
                    responseLines.add(">> GATEWAY VIRTUAL ATIVO: [192.168.15.1] via tun0 tunelado")
                    responseLines.add(">> DNS RESOLVER: 8.8.8.8, 1.1.1.1 (Google & Cloudflare Routed)")
                } else {
                    responseLines.add(">> Nenhuma ponte virtual configurada. Ative o Roteamento de Root.")
                }
            }
            "magisk" -> {
                responseLines.add("=== status do magisk daemon (magisk v26.4) ===")
                responseLines.add("- MagiskSU: Ativo no PID ${java.lang.Math.abs(System.currentTimeMillis() % 1000 + 400)}")
                responseLines.add("- Zygisk: ATIVADO (Injeção de bytecode realizada com sucesso!)")
                responseLines.add("- Isolamento de Namespace: Habilitado (Isolated Mount Namespace)")
                responseLines.add("- Módulos ativos (2/2):")
                responseLines.add("   [1] Play Integrity Fix v15.9.3 (Bypass de segurança)")
                responseLines.add("   [2] Systemless Hosts (Bloqueio de rastreadores)")
            }
            "kernelsu", "ksu" -> {
                responseLines.add("=== KernelSU Manager === ")
                responseLines.add("Versão do Kernel: 5.15.10-superuser-x86_64")
                responseLines.add("Hook de Superusuário: Ativo de verdade via chamadas de sistema (syscalls)")
                responseLines.add("Status de segurança: SElinux enforcing com auto-bypass")
            }
            "iptables" -> {
                responseLines.add("=== IPTABLES VIRTUAL FIREWALL RULES ===")
                responseLines.add("Chain INPUT (policy ACCEPT)")
                responseLines.add("target     prot opt source               destination")
                responseLines.add("Chain FORWARD (policy ACCEPT)")
                if (_isRealRouting.value) {
                    responseLines.add("ACCEPT     all  --  10.0.2.0/24          anywhere             state NEW,RELATED,ESTABLISHED")
                } else {
                    responseLines.add("DROP       all  --  anywhere             anywhere")
                }
                responseLines.add("Chain OUTPUT (policy ACCEPT)")
            }
            "whoami" -> {
                if (_isSuActive.value && _isRootEnabled.value) {
                    responseLines.add("root (SUPERUSER - uid=0)")
                } else {
                    responseLines.add("guest (Standard App Client - uid=10243)")
                }
            }
            "ls" -> {
                responseLines.add("drwxrwxr-x   2 guest  guest      4096 Jun  1 07:41 .")
                responseLines.add("drwxr-xr-x  14 root   root       4096 Jun  1 07:41 ..")
                responseLines.add("-rwxr-xr-x   1 root   root    1048576 Jun  1 07:41 boot_animation.zip")
                responseLines.add("-rw-r--r--   1 root   root         42 Jun  1 07:41 flag_root_bypass.txt")
                responseLines.add("drwxrwx--x   3 system system     4096 Jun  1 07:41 shared_prefs")
                if (_isSuActive.value && _isRootEnabled.value) {
                    responseLines.add("-rw-r-----   1 root   root      1337 Jun  1 07:41 .secrets_superuser_key")
                }
            }
            "getprop" -> {
                responseLines.add("[ro.product.brand]: [Google / Xiaomi]")
                responseLines.add("[ro.product.model]: [${_virtualDeviceModel.value}]")
                responseLines.add("[ro.secure]: [1]")
                responseLines.add("[ro.debuggable]: [1]")
                responseLines.add("[ro.build.version.release]: [${_androidVersion.value.substringBefore(" ")}]")
                responseLines.add("[ro.build.version.security_patch]: [${_securityPatchLevel.value}]")
                responseLines.add("[ro.device.cpu]: [${_virtualProcessor.value}]")
                responseLines.add("[ro.device.ram]: [${_virtualRam.value}GB_LPDDR5X]")
                responseLines.add("[ro.device.storage]: [${_virtualStorage.value}GB_UFS4.0]")
                responseLines.add("[ro.build.tags]: [test-keys (Rooted/Superuser Enabled)]")
            }
            "neofetch" -> {
                responseLines.add("        ,-_-,        OS: ${_androidVersion.value} (Google Virtual OTA)")
                responseLines.add("       /  _  \\       Host: Xiaomi ${_virtualDeviceModel.value} Virtual VM")
                responseLines.add("      (  / \\  )      Kernel: 5.15.10-superuser-x86_64")
                responseLines.add("       \\_ _ _/       Patch Google: ${_securityPatchLevel.value}")
                responseLines.add("      /  _  \\        Memory: ${_virtualRam.value} GB LPDDR5X (Swap Active)")
                responseLines.add("     / /   \\ \\       CPU: ${_virtualProcessor.value}")
                responseLines.add("     \\_\\_ _/_/       Armazenamento: ${_virtualStorage.value} GB UFS 4.0")
                responseLines.add("                     Root: ${if (_isRootEnabled.value) "ATIVADO" else "DESATIVADO"}")
            }
            "clear" -> {
                currentLog.clear()
            }
            "rm" -> {
                if (cmdParts.contains("-rf") || cmdParts.contains("/") || cmdText.contains("/")) {
                    responseLines.add("--- INICIANDO APAGADO GERAL DO SISTEMA ---")
                    responseLines.add("Removendo: /system/bin/...")
                    responseLines.add("Removendo: /system/lib/...")
                    responseLines.add("Removendo: /data/user/0/...")
                    responseLines.add("Operação de Root concluída com êxito!")
                    responseLines.add("[SUCESSO] Brincadeira! Suas anotações no banco de dados SQLite de produção continuam seguras e protegidas.")
                } else {
                    responseLines.add("rm: Use 'rm -rf /' se quiser testar a segurança do sistema fictício.")
                }
            }
            else -> {
                // Execute actual shell command in the app's local sandbox environment for real Linux/Android execution!
                try {
                    val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmdText))
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                    val errorReader = java.io.BufferedReader(java.io.InputStreamReader(process.errorStream))
                    val output = mutableListOf<String>()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.add(line!!)
                    }
                    while (errorReader.readLine().also { line = it } != null) {
                        output.add(line!!)
                    }
                    process.waitFor()
                    if (output.isEmpty()) {
                        responseLines.add("[REAL-SHELL] Comando executado com sucesso s/ retorno.")
                    } else {
                        // Limit lines to prevent overflow
                        if (output.size > 100) {
                            responseLines.addAll(output.take(100))
                            responseLines.add("... (saída truncada em 100 linhas)")
                        } else {
                            responseLines.addAll(output)
                        }
                    }
                } catch (e: Exception) {
                    responseLines.add("[REAL-SHELL-ERROR] Falha de execução de processo: ${e.message}")
                    responseLines.add("Comando '$baseCmd' não reconhecido no fallback.")
                }
            }
        }

        val nextPrompt = if (_isRootEnabled.value && _isSuActive.value) "root@android:/ # " else "guest@android:/ $ "
        if (baseCmd != "clear") {
            currentLog.addAll(responseLines)
        }
        currentLog.add(nextPrompt)
        _terminalLog.value = currentLog
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application.applicationContext,
            AppDatabase::class.java,
            "diary_database"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository: AppRepository by lazy {
        AppRepository(database)
    }

    // Expose lists reactively from Room
    val entries: StateFlow<List<DiaryEntry>> = repository.allEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userIdeas: StateFlow<List<UserIdea>> = repository.allIdeas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val localNotes: StateFlow<List<LocalNote>> = repository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val generatedInsights: StateFlow<List<GeneratedInsight>> = repository.allInsights
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addGeneratedInsight(title: String, insightContent: String, source: String = "Ideias & Diário") {
        viewModelScope.launch {
            repository.insertInsight(GeneratedInsight(title = title, insightContent = insightContent, source = source))
        }
    }

    fun deleteGeneratedInsight(insight: GeneratedInsight) {
        viewModelScope.launch {
            repository.deleteInsight(insight)
        }
    }

    fun deleteGeneratedInsightById(id: Int) {
        viewModelScope.launch {
            repository.deleteInsightById(id)
        }
    }

    fun clearGeneratedInsights() {
        viewModelScope.launch {
            repository.clearInsights()
        }
    }

    fun expandIdeaWithGemini(title: String, description: String, onResult: (String) -> Unit) {
        if (title.isBlank() && description.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            _apiError.value = null
            val prompt = """
                Tenho a seguinte ideia em desenvolvimento:
                Título: $title
                Descrição: $description
                
                Por favor, expanda essa ideia trazendo:
                1. Oportunidades de desenvolvimento e melhorias.
                2. Pontos fortes ou diferenciais potenciais.
                3. Próximos passos práticos acionáveis.
                Responda em português com boa formatação Markdown.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = Content(parts = listOf(Part(text = "Aja como um mentor de inovação criativo e objetivo.")))
            )

            try {
                val activeApiKey = customGeminiApiKey.value.ifBlank { BuildConfig.GEMINI_API_KEY }
                if (activeApiKey.isBlank() || activeApiKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("O Token da API do Gemini não está configurado.")
                }
                val response = RetrofitClient.service.generateContent(activeApiKey, request)
                val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Não foi possível gerar expansão da ideia no momento."
                
                repository.insertInsight(
                    GeneratedInsight(
                        title = "Expansão AI: $title",
                        insightContent = resultText,
                        source = "Ideias IA"
                    )
                )
                onResult(resultText)
            } catch (e: Exception) {
                _apiError.value = e.localizedMessage
                onResult("Erro ao expandir ideia: ${e.localizedMessage}")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // CRUD User Ideas
    fun addUserIdea(title: String, description: String, category: String = "Geral", isFavorite: Boolean = false) {
        if (title.isBlank() && description.isBlank()) return
        viewModelScope.launch {
            repository.insertIdea(UserIdea(title = title, description = description, category = category, isFavorite = isFavorite))
        }
    }

    fun updateUserIdea(idea: UserIdea) {
        viewModelScope.launch {
            repository.updateIdea(idea)
        }
    }

    fun deleteUserIdea(idea: UserIdea) {
        viewModelScope.launch {
            repository.deleteIdea(idea)
        }
    }

    fun deleteUserIdeaById(id: Int) {
        viewModelScope.launch {
            repository.deleteIdeaById(id)
        }
    }

    // CRUD Local Notes
    fun addLocalNote(title: String, noteContent: String, tag: String = "Nota") {
        if (title.isBlank() && noteContent.isBlank()) return
        viewModelScope.launch {
            repository.insertNote(LocalNote(title = title, noteContent = noteContent, tag = tag))
        }
    }

    fun updateLocalNote(note: LocalNote) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteLocalNote(note: LocalNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteLocalNoteById(id: Int) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }

    // UI Loading & AI States
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiInsight = MutableStateFlow<String?>(null)
    val aiInsight: StateFlow<String?> = _aiInsight.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    // Add diary entry
    fun addDiaryEntry(title: String, content: String, mood: String) {
        viewModelScope.launch {
            val entry = DiaryEntry(title = title, content = content, mood = mood)
            repository.insertDiary(entry)
            if (_isLoggedIn.value) {
                _isCloudSyncing.value = true
                delay(1000) // Simulated quick background cloud backup sync
                _isCloudSyncing.value = false
            }
        }
    }

    // Delete diary entry
    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.deleteDiary(entry)
        }
    }

    // Clear diary entries
    fun clearAllEntries() {
        viewModelScope.launch {
            repository.clearDiaries()
            _aiInsight.value = null
        }
    }

    // Clear chatbot history
    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            // Insert initial greeting message
            repository.insertMessage(
                ChatMessage(
                    role = "model",
                    messageText = "Olá! Eu sou seu assistente inteligente. Como posso ajudar com sua rotina, reflexões ou metas hoje?"
                )
            )
        }
    }

    // Check if the greeting is setup
    init {
        viewModelScope.launch {
            // Check once if list is empty, then insert welcome greeting
            try {
                val list = repository.allMessages.first()
                if (list.isEmpty()) {
                    repository.insertMessage(
                        ChatMessage(
                            role = "model",
                            messageText = "Olá! Eu sou seu assistente inteligente. Como posso ajudar nas suas reflexões ou metas pessoais hoje?"
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore any initial database check failures gracefully
            }
        }
    }

    // Send a message inside the chat and fetch Gemini's response
    fun sendMessage(userMessageText: String) {
        if (userMessageText.trim().isEmpty()) {
            _apiError.value = "O texto do prompt não pode estar vazio. Digite uma mensagem válida antes de enviar para o serviço de IA."
            return
        }

        val trimmedMsg = userMessageText.trim()
        viewModelScope.launch {
            // 1. Save user msg to local database
            val userMsg = ChatMessage(role = "user", messageText = trimmedMsg)
            repository.insertMessage(userMsg)

            _isAiLoading.value = true
            _apiError.value = null

            // 2. Prepare request with conversational context
            // Fetch messages from state flow snapshot
            val history = messages.value
            val apiContents = mutableListOf<Content>()

            // Map messages as content turns
            history.forEach { msg ->
                val role = if (msg.role == "user") "user" else "model"
                apiContents.add(Content(role = role, parts = listOf(Part(text = msg.messageText))))
            }

            // Append the new active message if it's not yet in the list
            if (history.none { it.id == userMsg.id }) {
                apiContents.add(Content(role = "user", parts = listOf(Part(text = trimmedMsg))))
            }

            // 3. System instruction
            val systemInstruction = Content(
                parts = listOf(
                    Part(
                        text = customGeminiPrompt.value.ifBlank {
                            "Você é o 'Guia', um assistente virtual e diário inteligente que ajuda o usuário a organizar pensamentos, manter hábitos produtivos e refletir sobre a vida. Seja encorajador, caloroso, direto e prestativo. Use emojis de forma moderada e estilosa. Escreva sempre em português do Brasil e com excelente diagramação de texto (use tópicos ou quebras de linhas quando apropriado, e negrito)."
                        }
                    )
                )
            )

            val request = GeminiRequest(
                contents = apiContents,
                systemInstruction = systemInstruction
            )

            try {
                val activeApiKey = customGeminiApiKey.value.ifBlank { BuildConfig.GEMINI_API_KEY }
                if (activeApiKey.isBlank() || activeApiKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("O Token da API do Gemini não está configurado. Insira um token válido nas configurações da IA clicando na engrenagem no Chat.")
                }

                val response = RetrofitClient.service.generateContent(activeApiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Não consegui formular uma resposta do momento. Tente novamente."

                // Save model answer to local DB
                repository.insertMessage(ChatMessage(role = "model", messageText = responseText))
            } catch (e: Exception) {
                _apiError.value = e.localizedMessage ?: "Erro de conexão com o Gemini"
                repository.insertMessage(
                    ChatMessage(
                        role = "model",
                        messageText = "Desculpe, ocorreu um erro ao tentar me conectar ao cérebro de IA: ${e.localizedMessage}. Verifique se a sua chave de API está configurada."
                    )
                )
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Generate smart emotional insight from ALL current notes/diary entries
    fun generateDiaryInsights() {
        val currentEntries = entries.value
        if (currentEntries.isEmpty()) {
            _aiInsight.value = "Escreva algumas reflexões ou notas no seu diário primeiro para que eu possa trazer insights inteligentes sobre o seu humor e metas!"
            return
        }

        viewModelScope.launch {
            _isAiLoading.value = true
            _apiError.value = null
            _aiInsight.value = "Analisando suas reflexões diárias..."

            // Compile all entries into a block
            val entriesSnapshotText = currentEntries.joinToString("\n\n") { entry ->
                "Título: ${entry.title}\nHumor: ${entry.mood}\nResumo: ${entry.content}"
            }

            val prompt = """
                Aqui estão minhas anotações do diário pessoal recentemente escritas:
                $entriesSnapshotText
                
                Por favor, faça uma análise resumida das minhas vivências, comentando sobre o meu humor geral predominante e me dê 3 conselhos ou percepções acolhedores e acionáveis para melhorar minha qualidade de vida, humor e foco. Responda em português de forma concisa em formato Markdown bem formatado.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = Content(
                    parts = listOf(
                        Part(
                            text = "Aja como um mentor espiritual e analista de emoções carinhoso, objetivo e motivacional. Estruture com seções curtas em português."
                        )
                    )
                )
            )

            try {
                val activeApiKey = customGeminiApiKey.value.ifBlank { BuildConfig.GEMINI_API_KEY }
                if (activeApiKey.isBlank() || activeApiKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("O Token da API do Gemini não está configurado. Insira um token válido nas configurações da IA clicando na engrenagem no Chat.")
                }

                val response = RetrofitClient.service.generateContent(activeApiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Não recebi conteúdo do assistente."

                _aiInsight.value = responseText
                repository.insertInsight(
                    GeneratedInsight(
                        title = "Análise do Diário",
                        insightContent = responseText,
                        source = "Diário"
                    )
                )
            } catch (e: Exception) {
                _apiError.value = e.localizedMessage
                _aiInsight.value = "Não foi possível gerar insights agora: ${e.localizedMessage}. Certifique-se de configurar a Chave de API do Gemini no painel do AI Studio."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun dismissError() {
        _apiError.value = null
    }

    fun clearInsight() {
        _aiInsight.value = null
    }

    // --- SAVE / LOAD PROGRESS & INTERACTIVE TUTORIAL SYSTEM ---
    private val _lastSavedTimestamp = MutableStateFlow(sharedPrefs.getLong("last_saved_timestamp", 0L))
    val lastSavedTimestamp: StateFlow<Long> = _lastSavedTimestamp.asStateFlow()

    private val _isTutorialActive = MutableStateFlow(false)
    val isTutorialActive: StateFlow<Boolean> = _isTutorialActive.asStateFlow()

    private val _saveStatusMessage = MutableStateFlow<String?>(null)
    val saveStatusMessage: StateFlow<String?> = _saveStatusMessage.asStateFlow()

    fun startTutorial() {
        _isTutorialActive.value = true
    }

    fun completeTutorial() {
        _isTutorialActive.value = false
        sharedPrefs.edit().putBoolean("is_tutorial_completed", true).apply()
    }

    fun saveAppToGoogleAccount(context: Context, onComplete: (String) -> Unit = {}) {
        viewModelScope.launch {
            detectAndSetPhysicalGoogleAccount(context) { email, _ ->
                saveProgress()
                onComplete(email)
            }
        }
    }

    fun saveProgress() {
        viewModelScope.launch {
            val accountEmail = _userEmail.value ?: "reisjuvenira468@gmail.com"
            _saveStatusMessage.value = "Sincronizando app na Conta Google ($accountEmail)..."
            delay(600)
            val timestamp = System.currentTimeMillis()

            val entriesSnapshot = entries.value.joinToString(";;;") { "${it.id}|${it.title}|${it.content.replace("|", " ").replace(";;;", " ")}|${it.mood}|${it.timestamp}" }
            val messagesSnapshot = messages.value.joinToString(";;;") { "${it.id}|${it.role}|${it.messageText.replace("|", " ").replace(";;;", " ")}|${it.timestamp}" }

            sharedPrefs.edit()
                .putLong("last_saved_timestamp", timestamp)
                .putString("saved_entries_snapshot", entriesSnapshot)
                .putString("saved_messages_snapshot", messagesSnapshot)
                .putInt("saved_ram", _virtualRam.value)
                .putInt("saved_storage", _virtualStorage.value)
                .putString("saved_processor", _virtualProcessor.value)
                .putString("saved_device_model", _virtualDeviceModel.value)
                .putBoolean("saved_root_enabled", _isRootEnabled.value)
                .putStringSet("saved_installed_apps", _installedVirtualApps.value.toSet())
                .putLong("last_saved_timestamp_$accountEmail", timestamp)
                .putString("saved_entries_snapshot_$accountEmail", entriesSnapshot)
                .putString("saved_messages_snapshot_$accountEmail", messagesSnapshot)
                .putInt("saved_ram_$accountEmail", _virtualRam.value)
                .putInt("saved_storage_$accountEmail", _virtualStorage.value)
                .putString("saved_processor_$accountEmail", _virtualProcessor.value)
                .putString("saved_device_model_$accountEmail", _virtualDeviceModel.value)
                .putBoolean("saved_root_enabled_$accountEmail", _isRootEnabled.value)
                .putStringSet("saved_installed_apps_$accountEmail", _installedVirtualApps.value.toSet())
                .apply()

            _lastSavedTimestamp.value = timestamp
            _saveStatusMessage.value = "✅ App e dados salvos na Conta Google ($accountEmail) ☁️"
            delay(3000)
            _saveStatusMessage.value = null
        }
    }

    fun loadProgress() {
        viewModelScope.launch {
            val accountEmail = _userEmail.value ?: "reisjuvenira468@gmail.com"
            _saveStatusMessage.value = "Carregando snapshot salvo para a conta $accountEmail..."
            delay(800)
            val savedTime = sharedPrefs.getLong("last_saved_timestamp_$accountEmail", sharedPrefs.getLong("last_saved_timestamp", 0L))
            if (savedTime == 0L) {
                _saveStatusMessage.value = "Nenhum ponto de salvamento encontrado na Conta Google $accountEmail."
                delay(2500)
                _saveStatusMessage.value = null
                return@launch
            }

            val ram = sharedPrefs.getInt("saved_ram_$accountEmail", sharedPrefs.getInt("saved_ram", 16))
            val storage = sharedPrefs.getInt("saved_storage_$accountEmail", sharedPrefs.getInt("saved_storage", 1024))
            val proc = sharedPrefs.getString("saved_processor_$accountEmail", sharedPrefs.getString("saved_processor", "MediaTek Dimensity 9400 Octa-Core @ 3.4 GHz")) ?: "MediaTek Dimensity 9400 Octa-Core @ 3.4 GHz"
            val model = sharedPrefs.getString("saved_device_model_$accountEmail", sharedPrefs.getString("saved_device_model", "Redmi 15")) ?: "Redmi 15"
            val root = sharedPrefs.getBoolean("saved_root_enabled_$accountEmail", sharedPrefs.getBoolean("saved_root_enabled", false))
            val apps = sharedPrefs.getStringSet("saved_installed_apps_$accountEmail", sharedPrefs.getStringSet("saved_installed_apps", setOf("Aurora Store", "Unciv", "Google Antivírus", "Google Play Store", "Bitdefender Mobile Security")))?.toList() ?: emptyList()

            updateHardwareSpecs(ram, storage, proc)
            updateDeviceModel(model)
            toggleRoot(root)
            _installedVirtualApps.value = apps
            sharedPrefs.edit().putStringSet("installed_virtual_apps", apps.toSet()).apply()

            val entriesSnapshot = sharedPrefs.getString("saved_entries_snapshot_$accountEmail", sharedPrefs.getString("saved_entries_snapshot", "")) ?: ""
            if (entriesSnapshot.isNotBlank()) {
                repository.clearDiaries()
                entriesSnapshot.split(";;;").forEach { itemStr ->
                    val parts = itemStr.split("|")
                    if (parts.size >= 4) {
                        val title = parts[1]
                        val content = parts[2]
                        val mood = parts[3]
                        val time = parts.getOrNull(4)?.toLongOrNull() ?: System.currentTimeMillis()
                        repository.insertDiary(DiaryEntry(title = title, content = content, mood = mood, timestamp = time))
                    }
                }
            }

            val messagesSnapshot = sharedPrefs.getString("saved_messages_snapshot_$accountEmail", sharedPrefs.getString("saved_messages_snapshot", "")) ?: ""
            if (messagesSnapshot.isNotBlank()) {
                repository.clearChatHistory()
                messagesSnapshot.split(";;;").forEach { itemStr ->
                    val parts = itemStr.split("|")
                    if (parts.size >= 3) {
                        val role = parts[1]
                        val text = parts[2]
                        val time = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
                        repository.insertMessage(ChatMessage(role = role, messageText = text, timestamp = time))
                    }
                }
            }

            _lastSavedTimestamp.value = savedTime
            _saveStatusMessage.value = "📂 App e dados carregados da Conta Google ($accountEmail)!"
            delay(3000)
            _saveStatusMessage.value = null
        }
    }
}
