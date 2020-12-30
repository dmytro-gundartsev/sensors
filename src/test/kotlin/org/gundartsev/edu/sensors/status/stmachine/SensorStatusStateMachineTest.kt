package org.gundartsev.edu.sensors.status.stmachine

import org.gundartsev.edu.sensors.dateTime
import org.gundartsev.edu.sensors.domain.StatusData
import org.gundartsev.edu.sensors.domain.StatusEnum
import org.gundartsev.edu.sensors.domain.alert.AlertEventEnum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal class SensorStatusStateMachineTest {
    /**
     * Smoke test of basic status values update with arrival of the measure
     */
    @Test
    fun smokeTestOfBasicValuesUpdate() {
        val stMachine = SensorStatusStateMachine(StatusData(StatusEnum.OK, null, 1, 0))
        stMachine.evaluate(100, dateTime("2020-12-30T15:14:00"))
        val stData = stMachine.status()
        assertEquals(dateTime("2020-12-30T15:14:00"), stData.latestMeasurementTime)
        assertEquals((dateTime("2020-12-30T15:14:00").withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond() / 60).toInt(), stData.latestUTCMinuteId)
        assertEquals(StatusEnum.OK, stData.status)
        assertEquals(0, stData.statusCounter)
    }

    /**
     * Test: OK -> WARN transition
     */
    @Test
    fun testOkToWarnTransition(){
        val stMachine = SensorStatusStateMachine(StatusData(StatusEnum.OK, null, 1, 0))
        assertEquals( StatusEnum.OK, stMachine.status().status)
        stMachine.evaluate(2100, dateTime("2020-12-30T15:14:00"))
        assertEquals( StatusEnum.WARN, stMachine.status().status)
    }

    /**
     * Test: WARN -> OK transition
     */
    @Test
    fun testWarnToOkTransition(){
        val stMachine = SensorStatusStateMachine(StatusData(StatusEnum.WARN, null, 1, 0))
        assertEquals( StatusEnum.WARN, stMachine.status().status)
        stMachine.evaluate(2000, dateTime("2020-12-30T15:14:00"))
        assertEquals( StatusEnum.OK, stMachine.status().status)
    }

    /**
     * Test: OK ->WARN ->WARN -> ALERT transition (+ Emiting alert enum ALERT_START
     */
    @Test
    fun testOkToAlertTransition(){
        val stMachine = SensorStatusStateMachine(StatusData(StatusEnum.OK, null, 1, 0))
        assertEquals( StatusEnum.OK, stMachine.status().status)
        stMachine.evaluate(2200, dateTime("2020-12-30T15:14:00"))
        assertEquals( StatusEnum.WARN, stMachine.status().status)
        stMachine.evaluate(2300, dateTime("2020-12-30T15:15:00"))
        assertEquals( StatusEnum.WARN, stMachine.status().status)
        assertEquals(AlertEventEnum.START_ALERT, stMachine.evaluate(2400, dateTime("2020-12-30T15:16:00")))
        assertEquals( StatusEnum.ALERT, stMachine.status().status)
    }

    /**
     * Test: ALERT(cnt:3) (<2000) -> ALERT(cnt:2) (<2000) -> ALERT(cnt:1) -(>2000) -> ALERT(cnt:3+alertEnum:ALERT) emitting alertEvents on the way
     */
    @Test
    fun testAlertToToAlertTransition(){
        val stMachine = SensorStatusStateMachine(StatusData(StatusEnum.ALERT, null, 1, 3))
        assertNull(stMachine.evaluate(1200, dateTime("2020-12-30T15:14:00")))
        assertEquals( StatusEnum.ALERT, stMachine.status().status)
        assertEquals( 2, stMachine.status().statusCounter)
        assertNull(stMachine.evaluate(1200, dateTime("2020-12-30T15:15:00")))
        assertEquals( StatusEnum.ALERT, stMachine.status().status)
        assertEquals( 1, stMachine.status().statusCounter)
        stMachine.evaluate(2200, dateTime("2020-12-30T15:16:00"))!!
                .also { assertEquals(AlertEventEnum.ALERT, it) }
        assertEquals( StatusEnum.ALERT, stMachine.status().status)
        assertEquals( 3, stMachine.status().statusCounter)
    }

    /**
     * Test: ALERT(cnt:3) (<2000) -> ALERT(cnt:2) (<2000) -> ALERT(cnt:1) -(<2000) -> OK(+ emmiting alertEnum:ALERT_STOP)
     */
    @Test
    fun testAlertToOKTransition(){
        val stMachine = SensorStatusStateMachine(StatusData(StatusEnum.ALERT, null, 1, 3))
        assertNull(stMachine.evaluate(1200, dateTime("2020-12-30T15:14:00")))
        assertEquals( StatusEnum.ALERT, stMachine.status().status)
        assertEquals( 2, stMachine.status().statusCounter)
        assertNull(stMachine.evaluate(1200, dateTime("2020-12-30T15:15:00")))
        assertEquals( StatusEnum.ALERT, stMachine.status().status)
        assertEquals( 1, stMachine.status().statusCounter)
        stMachine.evaluate(1100, dateTime("2020-12-30T15:16:00"))!!
                .also { assertEquals(AlertEventEnum.STOP_ALERT, it) }
        assertEquals( StatusEnum.OK, stMachine.status().status)
    }
}