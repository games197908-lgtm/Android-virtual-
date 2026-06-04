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
        if (!sharedPrefs.contains("virtual_ram")) {
            sharedPrefs.edit()
                .putInt("virtual_ram", 16)
                .putInt("virtual_storage", 512)
                .putString("virtual_processor", "Snapdragon 8 Gen 3 Octa-Core @ 3.39 GHz")
                .apply()
        }
    }

    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "Juvenira Reis"))
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(sharedPrefs.getString("user_email", "reisjuvenira468@gmail.com"))
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    fun loginWithGoogle() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            delay(1200) // Beautiful simulated loading time for Google authenticating window/token
            sharedPrefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", "Juvenira Reis")
                .putString("user_email", "reisjuvenira468@gmail.com")
                .apply()
            _isLoggedIn.value = true
            _userName.value = "Juvenira Reis"
            _userEmail.value = "reisjuvenira468@gmail.com"
            _isCloudSyncing.value = false
        }
    }

    fun logout() {
        sharedPrefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
        _isLoggedIn.value = false
    }

    private val _virtualRam = MutableStateFlow(sharedPrefs.getInt("virtual_ram", 16))
    val virtualRam: StateFlow<Int> = _virtualRam.asStateFlow()

    private val _virtualStorage = MutableStateFlow(sharedPrefs.getInt("virtual_storage", 512))
    val virtualStorage: StateFlow<Int> = _virtualStorage.asStateFlow()

    private val _virtualProcessor = MutableStateFlow(sharedPrefs.getString("virtual_processor", "MediaTek Dimensity 7300 Ultra Octa-Core @ 2.5 GHz") ?: "MediaTek Dimensity 7300 Ultra Octa-Core @ 2.5 GHz")
    val virtualProcessor: StateFlow<String> = _virtualProcessor.asStateFlow()

    private val _virtualDeviceModel = MutableStateFlow(sharedPrefs.getString("virtual_device_model", "Redmi Note 14") ?: "Redmi Note 14")
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

    private val _installedVirtualApps = MutableStateFlow<List<String>>(
        sharedPrefs.getStringSet("installed_virtual_apps", setOf("Aurora Store", "Unciv", "Google Antivírus"))?.toList() ?: listOf("Aurora Store", "Unciv", "Google Antivírus")
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
                val apiKey = BuildConfig.GEMINI_API_KEY
                var responseText = ""
                
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
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
                    val response = RetrofitClient.service.generateContent(apiKey, request)
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
                responseLines.add("Sistemas de Comandos Root Simulados:")
                responseLines.add("  su           - Solicita privilégios de superusuário (requer botão root ativo)")
                responseLines.add("  whoami       - Mostra o usuário ativo no shell")
                responseLines.add("  ls -la       - Lista arquivos na pasta do sistema root")
                responseLines.add("  getprop      - Mostra propriedades de build do aparelho")
                responseLines.add("  neofetch     - Informações detalhadas do hardware/software")
                responseLines.add("  rm -rf /     - [PERIGO] Tenta apagar os dados do sistema")
                responseLines.add("  clear        - Limpa o histórico de comandos")
            }
            "su" -> {
                if (!_isRootEnabled.value) {
                    _isRootEnabled.value = true
                    sharedPrefs.edit().putBoolean("is_root_enabled", true).apply()
                    responseLines.add(">> [AUTO_BYPASS] Acesso Superusuário habilitado automaticamente via terminal!")
                }
                _isSuActive.value = true
                responseLines.add("Superuser permission granted for package ID: com.aistudio.assistenteai")
                responseLines.add("uid=0(root) gid=0(root) groups=0(root) context=u:r:su:s0")
                responseLines.add("Acesso Root ATIVADO e Liberado com Sucesso! 🔓")
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
                responseLines.add("[ro.product.brand]: [Xiaomi]")
                responseLines.add("[ro.product.model]: [${_virtualDeviceModel.value}]")
                responseLines.add("[ro.secure]: [0]")
                responseLines.add("[ro.debuggable]: [1]")
                responseLines.add("[ro.build.version.release]: [14]")
                responseLines.add("[ro.device.cpu]: [${_virtualProcessor.value}]")
                responseLines.add("[ro.device.ram]: [${_virtualRam.value}GB_LPDDR5X]")
                responseLines.add("[ro.device.storage]: [${_virtualStorage.value}GB_UFS4.0]")
                responseLines.add("[ro.build.tags]: [test-keys (Rooted/Superuser Enabled)]")
            }
            "neofetch" -> {
                responseLines.add("        ,-_-,        OS: Android 14 (MIUI / HyperOS Virtualized)")
                responseLines.add("       /  _  \\       Host: Xiaomi ${_virtualDeviceModel.value} Virtual VM")
                responseLines.add("      (  / \\  )      Kernel: 5.15.10-superuser-x86_64")
                responseLines.add("       \\_ _ _/       Shell: bash / com.aistudio.assistenteai")
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
                responseLines.add("Comando '$baseCmd' não reconhecido. Digite 'help' para comandos de root.")
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
        if (userMessageText.isBlank()) return

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
                        text = "Você é o 'Guia', um assistente virtual e diário inteligente que ajuda o usuário a organizar pensamentos, manter hábitos produtivos e refletir sobre a vida. Seja encorajador, caloroso, direto e prestativo. Use emojis de forma moderada e estilosa. Escreva sempre em português do Brasil e com excelente diagramação de texto (use tópicos ou quebras de linhas quando apropriado, e negrito)."
                    )
                )
            )

            val request = GeminiRequest(
                contents = apiContents,
                systemInstruction = systemInstruction
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("API key is not configured. Please enter your GEMINI_API_KEY inside AI Studio.")
                }

                val response = RetrofitClient.service.generateContent(apiKey, request)
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
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("API key is not configured. Please enter your GEMINI_API_KEY inside AI Studio.")
                }

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Não recebi conteúdo do assistente."

                _aiInsight.value = responseText
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
}
