package org.gundartsev.edu.sensors.api.v1.controller

import com.nhaarman.mockito_kotlin.*
import org.gundartsev.edu.sensors.alert.ISensorAlertService
import org.gundartsev.edu.sensors.api.exceptions.ControllerExceptionHandlerAdvice
import org.gundartsev.edu.sensors.api.v1.converter.AlertDataV1Converter
import org.gundartsev.edu.sensors.api.v1.converter.SensorMeasurementV1Converter
import org.gundartsev.edu.sensors.api.v1.converter.SensorMetricsV1Converter
import org.gundartsev.edu.sensors.api.v1.converter.SensorStatusV1Converter
import org.gundartsev.edu.sensors.api.v1.model.AlertDTO
import org.gundartsev.edu.sensors.api.v1.model.SensorMetricsDTO
import org.gundartsev.edu.sensors.api.v1.model.SensorStatusDTO
import org.gundartsev.edu.sensors.common.exception.SensorNotFoundException
import org.gundartsev.edu.sensors.common.mq.registrars.IQueueItemRegistrar
import org.gundartsev.edu.sensors.config.ThreadPoolsConfig
import org.gundartsev.edu.sensors.dateTime
import org.gundartsev.edu.sensors.domain.MeasurementData
import org.gundartsev.edu.sensors.domain.StatusData
import org.gundartsev.edu.sensors.domain.StatusEnum
import org.gundartsev.edu.sensors.domain.alert.Alert
import org.gundartsev.edu.sensors.domain.metrics.MetricsValue
import org.gundartsev.edu.sensors.metrics.ISensorMetricsService
import org.gundartsev.edu.sensors.status.ISensorStatusService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.util.*

@ExtendWith(SpringExtension::class, MockitoExtension::class)
@Import(ControllerExceptionHandlerAdvice::class,
        AlertDataV1Converter::class, SensorMeasurementV1Converter::class,
        SensorMetricsV1Converter::class, SensorStatusV1Converter::class,
        ThreadPoolsConfig::class)
@WebFluxTest(SensorsControllerV1::class)
internal class SensorsControllerV1Test {
    @MockBean
    private lateinit var measurementsRegistrar: IQueueItemRegistrar<MeasurementData>

    @MockBean
    private lateinit var metricsService: ISensorMetricsService

    @MockBean
    private lateinit var alertService: ISensorAlertService

    @MockBean
    private lateinit var statusService: ISensorStatusService

    @Autowired
    private lateinit var mockMvc: WebTestClient

    companion object {
        val refSensorUUID = UUID.randomUUID()
    }

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun testPostedMeasureBadBody() {
        mockMvc.post()
                .uri("/api/v1/sensors/{UUID}/measurements", refSensorUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("\"co2\":100, \"time\":\"2020-12-27T14:55:47+00:00\"")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun testPostedMeasureBadValue() {
        mockMvc.post()
                .uri("/api/v1/sensors/{UUID}/measurements", refSensorUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"co2\":-1, \"time\":\"2020-12-27T14:55:47+00:00\"}")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun testInternalException() {
        whenever(measurementsRegistrar.register(any())).thenThrow(RuntimeException())
        mockMvc.post()
                .uri("/api/v1/sensors/{UUID}/measurements", refSensorUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"co2\":1, \"time\":\"2020-12-27T14:55:47+00:00\"}")
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Test
    fun testPostedMeasureOKBody() {
        mockMvc.post()
                .uri("/api/v1/sensors/{UUID}/measurements", refSensorUUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"co2\":100, \"time\":\"2020-12-27T14:55:47+00:00\"}")
                .exchange()
                .expectStatus().isOk
        verify(measurementsRegistrar, times(1)).register(any())
    }


    @Test
    fun testGetStatusForAbsentSensor404() {
        whenever(metricsService.getMetrics(eq(refSensorUUID))).thenThrow(SensorNotFoundException(refSensorUUID))
        mockMvc.get()
                .uri("/api/v1/sensors/{UUID}/metrics", refSensorUUID)
                .exchange()
                .expectStatus().isNotFound
    }

    @Test
    fun testGetStatusForPresentSensor_WARN() {
        whenever(statusService.getStatus(eq(refSensorUUID))).thenReturn(StatusData(StatusEnum.WARN, null, 1, 1))
        mockMvc.get()
                .uri("/api/v1/sensors/{UUID}/status", refSensorUUID)
                .exchange()
                .expectStatus().isOk
                .expectBody(SensorStatusDTO::class.java)
                .returnResult().responseBody.run {
                    assertEquals(StatusEnum.WARN, this!!.status)
                }
    }

    @Test
    fun testGetMetricsForAbsentSensor404() {
        whenever(metricsService.getMetrics(eq(refSensorUUID))).thenThrow(SensorNotFoundException(refSensorUUID))
        mockMvc.get()
                .uri("/api/v1/sensors/{UUID}/metrics", refSensorUUID)
                .exchange()
                .expectStatus().isNotFound
    }

    @Test
    fun testGetMetricsOK() {
        whenever(metricsService.getMetrics(eq(refSensorUUID))).thenReturn(MetricsValue(100, 105f))
        mockMvc.get()
                .uri("/api/v1/sensors/{UUID}/metrics", refSensorUUID)
                .exchange()
                .expectStatus().isOk
                .expectBody(SensorMetricsDTO::class.java)
                .returnResult().responseBody.run {
                    assertEquals(100, this!!.maxLast30Days)
                    assertEquals(105, avgLast30Days)
                }
    }

    @Test
    fun testGetAlertsOK() {
        whenever(alertService.getAlerts(eq(refSensorUUID))).thenReturn(
                Flux.just(
                        Alert.builder().startTime(dateTime("2020-12-01T15:03:02"))
                                .endTime(dateTime("2020-12-01T19:03:02"))
                                .measurements(listOf(2100, 2400, 2500, 2100)).build())
        )
        mockMvc.get()
                .uri("/api/v1/sensors/{UUID}/alerts", refSensorUUID)
                .exchange()
                .expectStatus().isOk
                .expectBodyList(AlertDTO::class.java)
                .returnResult().responseBody.run {
                    assertEquals(1, this!!.size)
                    assertEquals(6, this[0].size)
                    assertEquals(2100, this[0][AlertDTO.MEASUREMENT_PREFIX + 1])
                    assertEquals(2400, this[0][AlertDTO.MEASUREMENT_PREFIX + 2])
                    assertEquals(2500, this[0][AlertDTO.MEASUREMENT_PREFIX + 3])
                    assertEquals(2100, this[0][AlertDTO.MEASUREMENT_PREFIX + 4])
                }
    }

    @Test
    fun testGetAlertsForAbsentSensor404() {
        whenever(alertService.getAlerts(eq(refSensorUUID))).thenThrow(SensorNotFoundException(refSensorUUID))
        mockMvc.get()
                .uri("/api/v1/sensors/{UUID}/alerts", refSensorUUID)
                .exchange()
                .expectStatus().isNotFound
    }
}