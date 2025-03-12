package com.example.healthconnect.codelab.presentation.screen.inputreadings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import androidx.lifecycle.viewModelScope
import com.example.healthconnect.codelab.data.HealthConnectManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.launch

class InputReadingsViewModelTest {

    private lateinit var healthConnectManager: HealthConnectManager

    @Before
    fun setup() {
        healthConnectManager = Mockito.mock(HealthConnectManager::class.java)
    }

    @Test
    fun testInitialLoad() {
        // Given
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val now = Instant.now()
        val endofWeek = startOfDay.toInstant().plus(7, ChronoUnit.DAYS)
        val dummyWeightRecord = listOf(WeightRecord(Mass.kilograms(80.0), Instant.now(), ZonedDateTime.now()))
        Mockito.`when`(healthConnectManager.readWeightInputs(startOfDay.toInstant(), now)).thenReturn(dummyWeightRecord)

        // When
        val viewModel = InputReadingsViewModel(healthConnectManager)
        viewModel.initialLoad()

        // Then
        assertEquals(dummyWeightRecord, viewModel.readingsList.value)
    }

    @Test
    fun testInputReadings() {
        // Given
        val inputValue = 70.0
        val dummyWeightRecord = listOf(WeightRecord(Mass.kilograms(inputValue), Instant.now(), ZonedDateTime.now()))
        Mockito.`when`(healthConnectManager.writeWeightInput(inputValue)).thenReturn(Unit)
        Mockito.`when`(healthConnectManager.readWeightInputs(Instant.now(), Instant.now())).thenReturn(dummyWeightRecord)

        // When
        val viewModel = InputReadingsViewModel(healthConnectManager)
        viewModel.inputReadings(inputValue)

        // Then
        assertEquals(dummyWeightRecord, viewModel.readingsList.value)
    }

    @Test
    fun testTryWithPermissionsCheck() {
        // Given
        val permissions = setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getWritePermission(WeightRecord::class),
        )
        Mockito.`when`(healthConnectManager.hasAllPermissions(permissions)).thenReturn(true)

        // When
        val viewModel = InputReadingsViewModel(healthConnectManager)
        viewModel.tryWithPermissionsCheck {
            viewModel.readWeightInputs()
        }

        // Then
        assertTrue(viewModel.permissionsGranted.value)
    }

    @Test
    fun testUiState() {
        // Given
        val exception = Exception("Test Exception")

        // When
        val viewModel = InputReadingsViewModel(healthConnectManager)
        viewModel.uiState = InputReadingsViewModel.UiState.Error(exception)

        // Then
        assertEquals(InputReadingsViewModel.UiState.Error(exception), viewModel.uiState)
    }
}
