package com.factory.slumberaisleepcoach.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.factory.slumberaisleepcoach.billing.BillingConnectionState
import com.factory.slumberaisleepcoach.billing.BillingManager
import com.factory.slumberaisleepcoach.billing.PremiumManager
import com.factory.slumberaisleepcoach.billing.PremiumProduct
import com.factory.slumberaisleepcoach.billing.PurchaseEvent
import com.factory.slumberaisleepcoach.data.entities.SleepSession
import com.factory.slumberaisleepcoach.data.entities.SnoringRecord
import com.factory.slumberaisleepcoach.data.repository.SettingsRepository
import com.factory.slumberaisleepcoach.data.repository.SleepRepository
import com.factory.slumberaisleepcoach.data.repository.SleepStats
import com.factory.slumberaisleepcoach.model.SleepStage
import com.factory.slumberaisleepcoach.model.determineSleepStage
import com.factory.slumberaisleepcoach.service.SleepTrackingService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class TrackingState { IDLE, TRACKING }

data class StageDataPoint(
    val elapsedMs: Long,
    val stage: SleepStage
)

class SleepViewModel(
    application: Application,
    val repository: SleepRepository,
    private val settingsRepository: SettingsRepository,
    private val premiumManager: PremiumManager,
    private val billingManager: BillingManager
) : AndroidViewModel(application) {

    // Premium / billing state
    val isPremium: StateFlow<Boolean> = premiumManager.isPremiumFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val hasCompletedOnboarding: StateFlow<Boolean?> = settingsRepository.hasCompletedOnboardingFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val productDetails: StateFlow<Map<String, ProductDetails>> = billingManager.productDetails
    val billingConnectionState: StateFlow<BillingConnectionState> = billingManager.connectionState

    private val _purchaseMessage = MutableStateFlow<String?>(null)
    val purchaseMessage: StateFlow<String?> = _purchaseMessage.asStateFlow()

    init {
        viewModelScope.launch {
            billingManager.connect()
            billingManager.loadProductDetails()
            billingManager.syncPurchases()
        }
        viewModelScope.launch {
            billingManager.purchaseEvents.collect { event ->
                _purchaseMessage.value = messageFor(event)
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.setOnboardingComplete() }
    }

    fun purchase(activity: Activity, product: PremiumProduct) {
        billingManager.launchPurchaseFlow(activity, product)
    }

    fun restorePurchases() {
        viewModelScope.launch { billingManager.syncPurchases(isUserInitiatedRestore = true) }
    }

    fun dismissPurchaseMessage() {
        _purchaseMessage.value = null
    }

    private fun messageFor(event: PurchaseEvent): String = when (event) {
        is PurchaseEvent.Success -> if (event.product == PremiumProduct.SMALL_IAP) {
            "Thank you for your support!"
        } else {
            "You're now Premium! Enjoy full access to all features."
        }
        is PurchaseEvent.Cancelled -> "Purchase cancelled."
        is PurchaseEvent.Pending -> "Your purchase is pending. Premium will unlock once it's confirmed."
        is PurchaseEvent.AlreadyOwned -> "You already own this. Try Restore Purchases."
        is PurchaseEvent.NoPurchasesToRestore -> "No previous purchases were found to restore."
        is PurchaseEvent.Error -> event.message
    }

    // Tracking state
    private val _trackingState = MutableStateFlow(TrackingState.IDLE)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _currentStage = MutableStateFlow(SleepStage.AWAKE)
    val currentStage: StateFlow<SleepStage> = _currentStage.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _snoringDetected = MutableStateFlow(false)
    val snoringDetected: StateFlow<Boolean> = _snoringDetected.asStateFlow()

    private val _snoringEventsCount = MutableStateFlow(0)
    val snoringEventsCount: StateFlow<Int> = _snoringEventsCount.asStateFlow()

    private val _stageHistory = MutableStateFlow<List<StageDataPoint>>(emptyList())
    val stageHistory: StateFlow<List<StageDataPoint>> = _stageHistory.asStateFlow()

    // Audio amplitude history for waveform (last 60 samples)
    private val _amplitudeHistory = MutableStateFlow<List<Float>>(emptyList())
    val amplitudeHistory: StateFlow<List<Float>> = _amplitudeHistory.asStateFlow()

    val snoringSensitivity: StateFlow<Float> = settingsRepository.sensitivityFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, 3500f
    )

    private val _selectedSession = MutableStateFlow<SleepSession?>(null)
    val selectedSession: StateFlow<SleepSession?> = _selectedSession.asStateFlow()

    private val _trackingError = MutableStateFlow<String?>(null)
    val trackingError: StateFlow<String?> = _trackingError.asStateFlow()

    fun dismissTrackingError() {
        _trackingError.value = null
    }

    fun onTrackingPermissionDenied() {
        _trackingError.value = "Microphone permission is required to track your sleep."
    }

    fun loadSession(id: Long) {
        viewModelScope.launch {
            _selectedSession.value = repository.getSessionById(id)
        }
    }

    fun clearSelectedSession() {
        _selectedSession.value = null
    }

    // Snoring detection cooldown to avoid double-counting
    private val _lastStoppedSnoringMs = MutableStateFlow(0L)

    // DB-backed flows
    val allSessions = repository.getAllSessions()
    val recentSessions = repository.getRecentSessions()
    val averageSleepDuration = repository.getAverageSleepDuration()
    val averageQuality = repository.getAverageQuality()
    val totalSessionCount = repository.getTotalSessionCount()
    val totalSnoringEvents = repository.getTotalSnoringEvents()

    private var timerJob: Job? = null
    private var audioJob: Job? = null
    private var startTimeMs: Long = 0L
    private val stageDurations = mutableMapOf<SleepStage, Long>()
    private var snoringDurationMs = 0L
    private var lastSnoringStartMs: Long? = null
    private var audioRecord: AudioRecord? = null

    fun setSensitivity(value: Float) {
        viewModelScope.launch {
            settingsRepository.setSensitivity(value)
        }
    }

    fun startTracking() {
        if (_trackingState.value == TrackingState.TRACKING) return
        viewModelScope.launch {
            val sessionId = repository.startSession()
            _currentSessionId.value = sessionId
            startTimeMs = System.currentTimeMillis()
            _trackingState.value = TrackingState.TRACKING
            _elapsedMs.value = 0L
            _stageHistory.value = listOf(StageDataPoint(0L, SleepStage.AWAKE))
            _amplitudeHistory.value = emptyList()
            stageDurations.clear()
            snoringDurationMs = 0L
            lastSnoringStartMs = null
            _snoringEventsCount.value = 0
            _currentStage.value = SleepStage.AWAKE
            _snoringDetected.value = false

            startForegroundService()
            startTimer()
            startAudioMonitoring()
        }
    }

    fun stopTracking() {
        if (_trackingState.value == TrackingState.IDLE) return
        viewModelScope.launch {
            timerJob?.cancel()
            audioJob?.cancel()
            stopAudioRecord()
            stopForegroundService()

            val sessionId = _currentSessionId.value ?: return@launch
            val totalDuration = _elapsedMs.value

            // Close open snoring window if still active
            lastSnoringStartMs?.let { start ->
                snoringDurationMs += System.currentTimeMillis() - start
            }
            lastSnoringStartMs = null

            val stats = SleepStats(
                totalDurationMs = totalDuration,
                qualityScore = calculateQuality(totalDuration),
                deepSleepMs = stageDurations[SleepStage.DEEP] ?: 0L,
                lightSleepMs = stageDurations[SleepStage.LIGHT] ?: 0L,
                remSleepMs = stageDurations[SleepStage.REM] ?: 0L,
                awakeMs = stageDurations[SleepStage.AWAKE] ?: 0L,
                snoringEvents = _snoringEventsCount.value,
                snoringDurationMs = snoringDurationMs
            )

            repository.endSession(sessionId, stats)

            _trackingState.value = TrackingState.IDLE
            _currentSessionId.value = null
            _elapsedMs.value = 0L
            _currentStage.value = SleepStage.AWAKE
            _audioAmplitude.value = 0f
            _snoringDetected.value = false
            _stageHistory.value = emptyList()
            _amplitudeHistory.value = emptyList()
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            var lastStage = SleepStage.AWAKE
            while (isActive) {
                delay(1000L)
                val elapsed = System.currentTimeMillis() - startTimeMs
                _elapsedMs.value = elapsed

                val elapsedMinutes = elapsed / 60_000L
                val newStage = determineSleepStage(elapsedMinutes)

                // Accumulate time in each stage
                val prev = stageDurations[lastStage] ?: 0L
                stageDurations[lastStage] = prev + 1000L

                if (newStage != lastStage) {
                    _currentStage.value = newStage
                    val history = _stageHistory.value.toMutableList()
                    history.add(StageDataPoint(elapsed, newStage))
                    _stageHistory.value = history
                    lastStage = newStage
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAudioMonitoring() {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBuffer = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = maxOf(minBuffer * 4, 8192)

        audioJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
                audioRecord = recorder

                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    withContext(Dispatchers.Main) {
                        _trackingError.value = "Couldn't access the microphone. Sleep tracking was stopped."
                        stopTracking()
                    }
                    return@launch
                }

                recorder.startRecording()
                val buffer = ShortArray(bufferSize / 2)

                while (isActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var maxAmp = 0
                        var sumAmp = 0L
                        for (i in 0 until read) {
                            val abs = Math.abs(buffer[i].toInt())
                            if (abs > maxAmp) maxAmp = abs
                            sumAmp += abs
                        }
                        val avgAmp = if (read > 0) sumAmp.toFloat() / read else 0f
                        val amplitude = maxAmp.toFloat()

                        withContext(Dispatchers.Main) {
                            _audioAmplitude.value = amplitude
                            val history = _amplitudeHistory.value.toMutableList()
                            history.add(amplitude)
                            if (history.size > 80) history.removeAt(0)
                            _amplitudeHistory.value = history

                            val threshold = snoringSensitivity.value
                            val nowMs = System.currentTimeMillis()

                            if (amplitude > threshold) {
                                if (!_snoringDetected.value) {
                                    _snoringDetected.value = true
                                    lastSnoringStartMs = nowMs
                                    _snoringEventsCount.value++

                                    val sessionId = _currentSessionId.value
                                    if (sessionId != null) {
                                        viewModelScope.launch {
                                            repository.addSnoringRecord(
                                                SnoringRecord(
                                                    sessionId = sessionId,
                                                    timestamp = nowMs,
                                                    maxAmplitude = amplitude,
                                                    avgAmplitude = avgAmp
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                if (_snoringDetected.value) {
                                    _snoringDetected.value = false
                                    lastSnoringStartMs?.let { start ->
                                        snoringDurationMs += nowMs - start
                                    }
                                    lastSnoringStartMs = null
                                }
                            }
                        }
                    }
                    delay(100L)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Ignore failures from the recorder being torn down by a concurrent stopTracking() call;
                // only surface errors that happen while still actively tracking.
                if (isActive) {
                    withContext(Dispatchers.Main) {
                        _trackingError.value = "Sleep tracking stopped because of a microphone error."
                        stopTracking()
                    }
                }
            } finally {
                stopAudioRecord()
            }
        }
    }

    private fun stopAudioRecord() {
        audioRecord?.apply {
            try {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) stop()
                release()
            } catch (_: Exception) {}
        }
        audioRecord = null
    }

    private fun startForegroundService() {
        val context = getApplication<Application>()
        val intent = Intent(context, SleepTrackingService::class.java).apply {
            action = SleepTrackingService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopForegroundService() {
        val context = getApplication<Application>()
        val intent = Intent(context, SleepTrackingService::class.java).apply {
            action = SleepTrackingService.ACTION_STOP
        }
        context.startService(intent)
    }

    private fun calculateQuality(durationMs: Long): Int {
        val hours = durationMs / 3_600_000.0
        val durationScore = when {
            hours >= 7 && hours <= 9 -> 100
            hours >= 6 && hours < 7 -> 80
            hours > 9 && hours <= 10 -> 80
            hours >= 5 && hours < 6 -> 60
            hours > 10 -> 60
            else -> 40
        }
        val snoringPenalty = minOf(30, _snoringEventsCount.value * 3)
        return maxOf(0, durationScore - snoringPenalty)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioJob?.cancel()
        stopAudioRecord()
        billingManager.endConnection()
    }
}
