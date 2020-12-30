package org.gundartsev.edu.sensors.measurements.service

import com.hazelcast.map.IMap
import com.nhaarman.mockito_kotlin.*
import org.gundartsev.edu.sensors.common.mq.registrars.IQueueItemRegistrar
import org.gundartsev.edu.sensors.dateTime
import org.gundartsev.edu.sensors.domain.*
import org.gundartsev.edu.sensors.domain.alert.AlertEvent
import org.gundartsev.edu.sensors.domain.alert.AlertEventEnum
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot
import org.gundartsev.edu.sensors.hourId
import org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngine
import org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngineFactory
import org.gundartsev.edu.sensors.minuteId
import org.gundartsev.edu.sensors.status.stmachine.ISensorStatusStateMachine
import org.gundartsev.edu.sensors.status.stmachine.ISensorStatusStateMachineFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class SensorMeasurementServiceTest {
    @Mock
    private lateinit var dataMap: IMap<UUID, SensorData>

    @Mock
    private lateinit var statisticSnapshotRegistrar: IQueueItemRegistrar<StatisticSnapshot>

    @Mock
    private lateinit var alertEventRegistrar: IQueueItemRegistrar<AlertEvent>

    @Mock
    private lateinit var stateMachineFactory: ISensorStatusStateMachineFactory

    @Mock
    private lateinit var statBufferEngineFactory: IRollingStatisticBufferEngineFactory

    @Mock
    private lateinit var stateMachine: ISensorStatusStateMachine

    @Mock
    private lateinit var statBufferMachine: IRollingStatisticBufferEngine<HourStatisticData>

    private lateinit var service: SensorMeasurementService

    companion object {
        val defaultStatusData = StatusData(StatusEnum.OK,
                dateTime("2020-12-30T13:00:12"),
                minuteId("2020-12-30T13:00:12"), 0)
        val defaultHourStatisticData = HourStatisticData()
        val defaultSensorData = SensorData().also {
            it.statusData = defaultStatusData
            it.rollingHourStatistic = defaultHourStatisticData
        }
        val defaultSensorUUID = UUID.randomUUID()
        val defaultStatisticSnapshot = StatisticSnapshot.builder()
                .avgLevel(100f)
                .maxLevel(100)
                .periodId(hourId("2020-12-30T13:00:12"))
                .sensorUUID(defaultSensorUUID).build()
    }

    @BeforeEach
    fun setUp() {
        whenever(stateMachineFactory.forStatus(any())).thenReturn(stateMachine)
        whenever(statBufferEngineFactory.getEngine<HourStatisticData>(eq(IRollingStatisticBufferEngineFactory.BufferedTimeHorizon.HOUR), any()))
                .thenReturn(statBufferMachine)
        whenever(statBufferMachine.buffer).thenReturn(defaultHourStatisticData)
        lenient().`when`(stateMachine.status()).thenReturn(defaultStatusData)
        service = SensorMeasurementService(dataMap, statisticSnapshotRegistrar, alertEventRegistrar,
                stateMachineFactory, statBufferEngineFactory) // not nic but @InjectedMock does mock duplicities

    }

    /**
     * Test for first measurement arrival. Only status update. No StatisticSnapshot and AlertEvent to be emitted in the queue/
     */
    @Test
    fun testAcceptFirstMeasureNoEmitting() {
        whenever(dataMap.getOrDefault(any(), any())).thenAnswer { it.getArgument<SensorData>(1) }
        service.apply(MeasurementData.builder()
                .time(dateTime("2020-12-30T13:02:12"))
                .level(1400)
                .uuid(defaultSensorUUID).build())
        val sensorDataCapture = argumentCaptor<SensorData>()
        verify(dataMap, times(1)).set(eq(defaultSensorUUID), sensorDataCapture.capture())
        assertEquals(defaultStatusData, sensorDataCapture.firstValue.statusData)
        assertEquals(defaultHourStatisticData, sensorDataCapture.firstValue.rollingHourStatistic)
        verify(statisticSnapshotRegistrar, never()).register(any())
        verify(alertEventRegistrar, never()).register(any())
    }

    /**
     * Test for emitting alert event when status changed
     */
    @Test
    fun testAcceptMeasureAndEmitAlertEvent() {
        whenever(dataMap.getOrDefault(any(), any())).thenReturn(defaultSensorData)
        whenever(stateMachine.evaluate(any(), any())).thenReturn(AlertEventEnum.ALERT)
        service.apply(MeasurementData.builder()
                .time(dateTime("2020-12-30T13:02:12"))
                .level(2100)
                .uuid(defaultSensorUUID).build())
        verify(statisticSnapshotRegistrar, never()).register(any())
        val alertEventCapture = argumentCaptor<AlertEvent>()
        verify(alertEventRegistrar, times(1)).register(alertEventCapture.capture())
        assertEquals(dateTime("2020-12-30T13:02:12"), alertEventCapture.firstValue.dateTime)
        assertEquals(AlertEventEnum.ALERT, alertEventCapture.firstValue.eventType)
        assertEquals(2100, alertEventCapture.firstValue.value)
    }

    /**
     * Test for emitting StatisticSnapshot for newHour arrived data
     */
    @Test
    fun testAcceptMeasureAndEmitStatisticSnapshot() {
        whenever(dataMap.getOrDefault(any(), any())).thenReturn(defaultSensorData)
        whenever(statBufferMachine.accept(any(), any())).thenReturn(defaultStatisticSnapshot)
        service.apply(MeasurementData.builder()
                .time(dateTime("2020-12-30T14:00:12"))
                .level(2100)
                .uuid(defaultSensorUUID).build())
        verify(alertEventRegistrar, never()).register(any())
        verify(statisticSnapshotRegistrar, times(1)).register(eq(defaultStatisticSnapshot))
    }

    /**
     * Test of unlike late measurement arrival (no updated status)
     */
    @Test
    fun testAcceptMeasureDiscardingStatusUpdate() {
        whenever(dataMap.getOrDefault(any(), any())).thenReturn(defaultSensorData)
        whenever(statBufferMachine.accept(any(), any())).thenReturn(defaultStatisticSnapshot)
        whenever(stateMachine.evaluate(any(), any())).thenThrow(IllegalArgumentException())
        service.apply(MeasurementData.builder()
                .time(dateTime("2020-12-30T12:00:12"))
                .level(2100)
                .uuid(defaultSensorUUID).build())
        verify(stateMachine, never()).status()
    }
}

