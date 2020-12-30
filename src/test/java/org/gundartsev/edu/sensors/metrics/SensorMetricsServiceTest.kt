package org.gundartsev.edu.sensors.metrics

import com.hazelcast.map.IMap
import com.nhaarman.mockito_kotlin.*
import org.gundartsev.edu.sensors.common.exception.SensorNotFoundException
import org.gundartsev.edu.sensors.dateTime
import org.gundartsev.edu.sensors.domain.HourStatisticData
import org.gundartsev.edu.sensors.domain.SensorData
import org.gundartsev.edu.sensors.domain.metrics.MetricsBuffer
import org.gundartsev.edu.sensors.domain.metrics.StatisticSnapshot
import org.gundartsev.edu.sensors.hourId
import org.gundartsev.edu.sensors.measurements.service.SensorMeasurementServiceTest
import org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngine
import org.gundartsev.edu.sensors.metrics.buffer.IRollingStatisticBufferEngineFactory
import org.gundartsev.edu.sensors.metrics.buffer.RollingStatisticBufferEngineFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.assertThrows as _assertThrows

@ExtendWith(MockitoExtension::class)
internal class SensorMetricsServiceTest {
    @Mock
    private lateinit var metricBufferMap: IMap<UUID, MetricsBuffer>

    @Mock
    private lateinit var sensorDataMap: IMap<UUID, SensorData>

    @Mock
    private lateinit var rollingStatisticBufferEngineFactory: RollingStatisticBufferEngineFactory

    @Mock
    private lateinit var rollingStatisticBufferEngine: IRollingStatisticBufferEngine<HourStatisticData>
    private lateinit var service: SensorMetricsService

    companion object {
        val defaultSensorUUID: UUID = UUID.randomUUID()
        val defaultHourStatisticData = HourStatisticData()
        val defaultSensorData = SensorData().also {
            it.statusData = SensorMeasurementServiceTest.defaultStatusData
            it.rollingHourStatistic = defaultHourStatisticData
        }
        val exisingStatisticSnapshot: StatisticSnapshot = StatisticSnapshot.builder()
                .avgLevel(200f)
                .maxLevel(200)
                .periodId(hourId("2019-12-30T14:00:01"))
                .sensorUUID(defaultSensorUUID)
                .build()
        val defaulStatistic: StatisticSnapshot = StatisticSnapshot.builder()
                .avgLevel(100f)
                .maxLevel(100)
                .periodId(hourId("2019-12-30T15:00:01"))
                .sensorUUID(defaultSensorUUID)
                .build()
    }

    @BeforeEach
    fun setUp() {
        service = SensorMetricsService(metricBufferMap, sensorDataMap, rollingStatisticBufferEngineFactory)
        lenient().`when`(rollingStatisticBufferEngineFactory.getEngine<HourStatisticData>(any(), any())).thenReturn(rollingStatisticBufferEngine)
    }

    /**
     * Test of first arrival of StatisticSnaphot. MetricsBuffer to be created and the snaphshot registered there
     * Expected: buffer instance created and contains the data of arrived snapshot
     */
    @Test
    fun testFirstStatisticSnapshotArrival() {
        whenever(metricBufferMap.getOrDefault(any(), any())).thenAnswer { it.getArgument<SensorData>(1) }
        service.apply(defaulStatistic)
        val bufferCaptor = argumentCaptor<MetricsBuffer>()
        verify(metricBufferMap, times(1)).set(eq(defaultSensorUUID), bufferCaptor.capture())
        assertEquals(hourId("2019-12-30T15:00:01"), bufferCaptor.firstValue.periodIds[0])
        assertEquals(100, bufferCaptor.firstValue.maxLevels[0])
        assertEquals(100f, bufferCaptor.firstValue.avgLevels[0])
        assertEquals(30 * 24, bufferCaptor.firstValue.maxPeriodSize)
    }

    /**
     * Test of statistic calculation. There was data for such sensor (no metrics buffer, no status).
     * Expected: SensorNotFoundException is thrown
     */
    @Test
    fun testCalcNoSensorDataAvailable() {
        whenever(metricBufferMap.containsKey(eq(defaultSensorUUID))).thenReturn(false)
        whenever(sensorDataMap.containsKey(eq(defaultSensorUUID))).thenReturn(false)
        _assertThrows<SensorNotFoundException> { service.getMetrics(defaultSensorUUID) }
    }

    /**
     * Test of statistic calculation. There was no mertric buffer for sensor bu some measure(s) registered in
     * hour statistic buffer.
     * Expected: Calculated value based only on hour buffered data
     */
    @Test
    fun testCalcNoMetricBufferAndPresentHourBuffer() {
        whenever(sensorDataMap.containsKey(eq(defaultSensorUUID))).thenReturn(true)
        whenever(metricBufferMap.containsKey(eq(defaultSensorUUID))).thenReturn(false)
        whenever(sensorDataMap[eq(defaultSensorUUID)]).thenReturn(defaultSensorData)
        whenever(rollingStatisticBufferEngine.currentBufferSnapshot()).thenReturn(defaulStatistic)
        val value = service.getMetrics(defaultSensorUUID)
        verify(rollingStatisticBufferEngineFactory, times(1)).getEngine(
                IRollingStatisticBufferEngineFactory.BufferedTimeHorizon.HOUR,
                defaultHourStatisticData)
        assertEquals(defaulStatistic.avgLevel, value.avgValue)
        assertEquals(defaulStatistic.maxLevel, value.maxValue)
    }

    /**
     * Test of statistic calculation. There are metric buffer (with snapshot for previous hour)  and also some measure(s) registered in
     * hour statistic buffer.
     * Expected: Calculated value based on
     */
    @Test
    fun testCalcWitMetricBufferAndHourBufferAvaiable() {
        whenever(sensorDataMap.containsKey(eq(defaultSensorUUID))).thenReturn(true)
        whenever(metricBufferMap.containsKey(eq(defaultSensorUUID))).thenReturn(true)
        whenever(sensorDataMap[eq(defaultSensorUUID)]).thenReturn(defaultSensorData)
        val metricsBuffer = MetricsBuffer(720).also { it.put(exisingStatisticSnapshot) }
        whenever(metricBufferMap[eq(defaultSensorUUID)]).thenReturn(metricsBuffer)
        whenever(rollingStatisticBufferEngine.currentBufferSnapshot()).thenReturn(defaulStatistic)
        val nowDateTime = dateTime("2019-12-30T15:00:10")
        Mockito.mockStatic(OffsetDateTime::class.java).use {
            it.`when`<Any> { OffsetDateTime.now() }.thenReturn(nowDateTime)
            val value = service.getMetrics(defaultSensorUUID)
            verify(rollingStatisticBufferEngineFactory, times(1)).getEngine(
                    IRollingStatisticBufferEngineFactory.BufferedTimeHorizon.HOUR,
                    defaultHourStatisticData)
            assertEquals(150f, value.avgValue)  // taken avg of existing snapshot + bufferedSnapshot(for next hour) = 100f + 200f / 2 = 150f
            assertEquals(200, value.maxValue)   //
        }
    }
}