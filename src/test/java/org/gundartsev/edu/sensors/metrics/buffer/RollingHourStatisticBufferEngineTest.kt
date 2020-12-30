package org.gundartsev.edu.sensors.metrics.buffer

import org.gundartsev.edu.sensors.domain.HourStatisticData
import org.gundartsev.edu.sensors.hourId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class RollingHourStatisticBufferEngineTest {

    /**
     * Scenario #1: The very first measurement registration for a new sensor
     * Description: current hour buffer: empty
     *              arrived value: (m:100, l:2100) <-- m:100 = hourId:1, minuteInHour:40
     * Expected result: accAvgTimeWeighted: 1400 = 2100 * 40 /60 <-- it's the only case when the padding from left is done usine level value
     *                                                           (not lastValue as it's absent for first time measurement).
     *                  maxLevel: 2100
     */
    @Test
    fun testHourStatBufferFirstItemCollection() {
        val engine = RollingHourStatisticBufferEngine(HourStatisticData())
        assertNull(engine.accept(100, 2100))
        assertEquals(1, engine.buffer.hourUTCId) // hourUTCId calculated from minuteUTCId % 60
        assertEquals(1400f, engine.buffer.accAvgTimeWeighted)  // 2100 * 40 sec / 60 = 1400 <-- timeWeighted avgValue
        assertEquals(2100, engine.buffer.maxLevel)
        assertEquals(40, engine.buffer.lastMinuteInHour)
        assertEquals(2100, engine.buffer.lastValue)
    }

    /**
     * Scenario #2: Next value within the same hour arrives
     * Description: current hour buffer: lastMinuteId:40, lastValue:2100, maxLevel:2100, accTimeWeighted:1400f
     *              arrived value: (m:105, l:2400) <-- m:105 = hourId:1, minuteInHour:45
     * Expected result: accAvgTimeWeighted: 1575 = 1400 + 2100 * 5 /60 <-- for right adjustment we use lastValue
     *                                                            (as we aagreed on right-adjusted sensor value model)
     *                  maxLevel: 2400
     */
    @Test
    fun testHourStatBufferSecondItemCollection() {
        val engine = RollingHourStatisticBufferEngine(HourStatisticData(1, 40, 2100, 1400f, 2100))
        assertNull(engine.accept(105, 2400))
        assertEquals(1575f, engine.buffer.accAvgTimeWeighted)
        assertEquals(2400, engine.buffer.maxLevel)
        assertEquals(2400, engine.buffer.lastValue)
    }

    /**
     * Scenario #2: New value arrives for new hour
     * Description: current hour buffer: lastMinuteId:45, lastValue:2400, maxLevel:2400, accTimeWeighted:1575f
     *              arrived value: (m:125, l:3600) <-- m:125 = hourId:2, minuteInHour:5
     * Expected result: a statistic snapshot is generated as follow:
     *                      hourId = 1
     *                      avgLevel = 2175 = 1575f + 2400 *(60-45) / 60 <-- right adjustment to the hour boundary by lastValue
     *                      maxLevel = 2400
     *                  and current buffer: accAvgTimeWeighted: 200f = 2400 * 5/60 <-- left adjustment using lastValue
     *                                      maxLevel: 3600
     *                                      minuteInHour: 5
     *                                      lastValue: 3600
     *                                      hourId: 2
     */
    @Test
    fun testHourStatBufferAnotherValueCollectionButForNewHour() {
        val engine = RollingHourStatisticBufferEngine(HourStatisticData(1, 45, 2400, 1575f, 2400))
        val snapshot = engine.accept(125, 3600)
        assertNotNull(snapshot)
        assertEquals(1, snapshot.periodId)
        assertEquals(2175f, snapshot.avgLevel)
        assertEquals(2400, snapshot.maxLevel)
        assertEquals(200f, engine.buffer.accAvgTimeWeighted)
        assertEquals(3600, engine.buffer.maxLevel)
        assertEquals(3600, engine.buffer.lastValue)
        assertEquals(2, engine.buffer.hourUTCId)
    }

    /**
     * Test of unfinished hour statistic calculation as it was finished for one reason: to use it in metrics calculation
     * Description: we need to provide the last X day value at some time 2020-12-31T15:32:12
     *              and we're having already some data gathered for hour: 2020-12-31T15:00:00 and we would like definitely
     *              to use it in our mertrics. Current buffer: lastMinuteId:25, lastValue:2400, maxLevel:2400, accAvgTimeWeighted:200f.
     *              So we cannot use that accAvgTimeWeigted as it is for 25 sec timeframe cummulative value.
     *              So we need to get a value adjusted to the right:
     *                  avgLevel = accAvgTimeWeigted + 2400 * (60-25)/60 = 200 + 1400 = 1600
     *
     * Expected result: a statistic snapshot:
     *                      hourId = <taken from buffer>
     *                      avgLevel = 1600 <-- adjusted
     *                      maxLevel = 2400 <-- taken from buffer as is
     */
    @Test
    fun testHourStatGetBufferSnapshot() {
        val engine = RollingHourStatisticBufferEngine(HourStatisticData(hourId("2020-12-31T15:32:12"), 25, 2400, 200f, 2400))
        val curSnapshot = engine.currentBufferSnapshot()
        assertNotNull(curSnapshot)
        assertEquals(hourId("2020-12-31T15:32:12"), curSnapshot!!.periodId)
        assertEquals(1600f, curSnapshot.avgLevel)
        assertEquals(2400, curSnapshot.maxLevel)
    }

    /**
     * Unlikely attempt to getCurrentBufferSnapshot for empty HourStatisticData
     */
    @Test
    fun testGetCurrentBufferSnapshotFromEmpty() {
        val engine = RollingHourStatisticBufferEngine(HourStatisticData())
        assertNull(engine.currentBufferSnapshot())
    }

}