package org.gundartsev.edu.sensors.domain.metrics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal class MetricsBufferTest {
    /**
     * Test of not letting wrong size of the buffer slip during buffer creations
     */
    @Test
    fun testWrongBufferSize() {
        assertThrows<IllegalArgumentException> { MetricsBuffer(Integer.MAX_VALUE) }
        assertThrows<IllegalArgumentException> { MetricsBuffer(0) }
    }

    /**
     * Test that coming snapshot to be ordered by decreasing period(hour)Id.
     */
    @Test
    fun testOrederingByPeriodIdInPersistentStructure() {
        val buffer = MetricsBuffer(10)
        buffer.put(snapshot(2100.0f, 2100, 100))
        assertEquals(2100f, buffer.avgLevels[0])
        assertEquals(2100, buffer.maxLevels[0])
        assertEquals(100, buffer.periodIds[0])
        buffer.put(snapshot(2300.0f, 2300, 200))
        assertEquals(2300f, buffer.avgLevels[0])
        assertEquals(2300, buffer.maxLevels[0])
        assertEquals(200, buffer.periodIds[0])
        //emulate late arrival (theoretically can happen in distributed systems)
        buffer.put(snapshot(2000.0f, 2000, 150))
        assertEquals(2000f, buffer.avgLevels[1])
        assertEquals(2000, buffer.maxLevels[1])
        assertEquals(150, buffer.periodIds[1])
    }

    /**
     * Test buffer got trimming by size
     */
    @Test
    fun testBufferTrimmingBySize() {
        val buffer = MetricsBuffer(1)
        buffer.put(snapshot(2100.0f, 2100, 100))
        buffer.put(snapshot(2300.0f, 2300, 200))
        assertEquals(1, buffer.periodIds.size)
    }

    /**
     * Test buffer must have got trimmed, for the data not being able to participate in statistic calc.
     */
    @Test
    fun testBufferTrimmingOldValues() {
        val buffer = MetricsBuffer(6) // buffer is supposed to keep upto 5 hours values for (last 6 hours) calc.
        buffer.put(snapshot(2100.0f, 2100, 100))
        buffer.put(snapshot(2300.0f, 2300, 101))
        buffer.put(snapshot(2100.0f, 2100, 106))
        assertEquals(2, buffer.periodIds.size)
    }

    /**
     * Statistic calculation test.
     * Scenario #1: Unlikely scenario of calculating statistic for sensor with no measurements received so far.
     * Decription:  current hour buffer empty,
     *              metric buffer is empty
     * Expected result: avgLevel= 0.0f, maxLevel=0
     */
    @Test
    fun testStatisticScenario1() {
        val buffer = MetricsBuffer(1)
        val value = buffer.getStatistic(null, 100)
        assertEquals(0.0f, value.avgValue)
        assertEquals(0, value.maxValue)
    }

    /**
     * Statistic calculation test.
     * Scenario #2: First metric request arrival on empty buffer of metrics, but with one measure received and in hour buffer
     * Description: current hour buffer filled = (h:100,a:2100.0f,m:2100),
     *              metric buffer is empty and for size = 30 * 24  //for last 30days
     *              calculation for current hour = h:102
     * Expected result: avgLevel= 2100.0f, maxLevel=2100
     */
    @Test
    fun testStatisticScenario2() {
        val buffer = MetricsBuffer(720)
        val value = buffer.getStatistic(snapshot(2100.0f, 2100, 100), 100)
        assertEquals(2100.0f, value.avgValue)
        assertEquals(2100, value.maxValue)
    }

    /**
     * Statistic calculation test.
     * Scenario #3: Following metric request arrivals.
     * Description: current hour buffer filled = (h:103,a:2100.0f,m:2100)
     *              there was no measures at h=101
     *              metric buffer contains(h:100, a:2200.0f, m:2200)of size = 30 * 24 //for last 30days
     *              calculation for current hour = h:103
     * Expected result: avgLevel= 2175.0f (2200.0f * 3h + 2100.0f)/4), maxLevel=2200
     */
    @Test
    fun testStatisticScenario3() {
        val buffer = MetricsBuffer(720)
        buffer.put(snapshot(2200.0f, 2200, 100))
        val value = buffer.getStatistic(snapshot(2100.0f, 2100, 103), 103)
        assertEquals(2175f, value.avgValue)
        assertEquals(2200, value.maxValue)
    }

    /**
     * Statistic calculation test.
     * Scenario #4: Following metric request arrivals, sparsely prefilled metric buffer
     * Description: current hour buffer filled = (h:104,a:2000.0f,m:2000)
     *              there were no measures at h=101, h=103, h=105;
     *              metric buffer contains(h:100, a:2200.0f, m:2200), (h:102, a:2100.0f, m:2100) of size = 30 * 24  //for last 30days
     *              calculation for current hour = h:105
     * Expected result: avgLevel= 2100.0f (2200 * 2h + 2100.0f * 2h + 2000.f * 2)/6), maxLevel=2200
     */
    @Test
    fun testStatisticScenario4() {
        val buffer = MetricsBuffer(720)
        buffer.put(snapshot(2200.0f, 2200, 100))
        buffer.put(snapshot(2100.0f, 2100, 102))
        val value = buffer.getStatistic(snapshot(2000.0f, 2000, 104), 105)
        assertEquals(2100.0f, value.avgValue)
        assertEquals(2200, value.maxValue)
    }

    /**
     * Statistic calculation test.
     * Scenario #5: Metric buffer filled with data covering all stat hours. For simplification: we're calculating for
     *             last 5 hours stat.
     * Description: current hour buffer filled = (h:104,a:2000.0f,m:2000)
     *              metric buffer contains (h:100, a:2200.0f, m:2200), (h:101, a:2100.0f, m:2100),
     *                                     (h:102, a:1800.0f, m:1800), (h:103, a:1600.0f, m:2900) of size = 5h //for last 5 hours stat
     *              calculation for current hour = h:104
     * Expected result: avgLevel=1940.0f (2200 + 2100 + 1800 + 1600 + 2000) /5,
     *                  maxLevel=2900
     */
    @Test
    fun testStatisticScenario5() {
        val buffer = MetricsBuffer(5)
        buffer.put(snapshot(2200.0f, 2200, 100))
        buffer.put(snapshot(2100.0f, 2100, 101))
        buffer.put(snapshot(1800.0f, 1800, 102))
        buffer.put(snapshot(1600.0f, 2900, 103))
        val value = buffer.getStatistic(snapshot(2000.0f, 2000, 104), 104)
        assertEquals(1940.0f, value.avgValue)
        assertEquals(2900, value.maxValue)
    }

    /**
     * Statistic calculation test.
     * Scenario #6: Most general case scenario. Metric buffer fully prefilled.
     *             last 30 days stat.
     * Description: analysis dateTime = 2020-12-31T15:05:34 <-- hourId=
     *              current hour buffer contains: (2020-12-31T15:00:00 :avg: 2140.0, max: 2100)
     *              metric buffer contains (
     *              2020-12-01T04:00:00 :avg: 2500.0, max: 4000  <--
     *              2020-12-13T10:00:00 :avg: 2000.0, max: 2000, <--
     *              2020-12-20T14:00:00 :avg: 1500.0, max: 1500  <--
     *
     * Expected result: time boundaries for analysis= 2020-12-01T16:00:00 - 2020-12-31T15:00
     *                  avgLevel= 2020-12-01T16:00:00 - 2020-12-13T09:00:00: 2500.0 * 282h + (value taken for previous 2020-12-01T04:00:00)
     *                            2020-12-13T10:00:00 - 2020-12-20T13:00:00: 2000.0 * 172h +
     *                            2020-12-20T14:00:00 - 2020-12-31T14:00:00: 1500.0 * 265h +
     *                            2020-12-31T15:00:00: 2140.0 * 1h <-- from current hour buffer
     *                            = 2012
     *                  maxLevel=4000
     */
    @Test
    fun testStatisticScenario6() {
        val buffer = MetricsBuffer(720)
        buffer.put(snapshot(2500.0f, 4000, "2020-12-01T04:00:00"))
        buffer.put(snapshot(2000.0f, 2000, "2020-12-13T10:00:00"))
        buffer.put(snapshot(1500.0f, 1500, "2020-12-20T14:00:00"))
        val value = buffer.getStatistic(
                snapshot(2140.0f, 2100, "2020-12-31T15:00:00"),
                hourIdFor("2020-12-31T15:05:34"))
        assertEquals(2012f, value.avgValue)
        assertEquals(4000, value.maxValue)
    }

    private fun snapshot(avgLevel: Float, maxLevel: Int, hourId: Int) =
            StatisticSnapshot(null, hourId, avgLevel, maxLevel)

    private fun hourIdFor(offsetdataTime: String) =
            (OffsetDateTime.parse("$offsetdataTime+00:00")
                    .withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond() / (60 * 60)).toInt()

    private fun snapshot(avgLevel: Float, maxLevel: Int, dateTime: String) =
            snapshot(avgLevel, maxLevel, hourIdFor(dateTime))

}