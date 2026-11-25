package com.kevinandrey.optimizer.annealing;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoolingScheduleTest {

    @Test
    void testGeometricCoolingMonotonicallyDecreases() {
        CoolingSchedule schedule = new CoolingSchedule.Geometric(0.90);
        double temp = 100.0;
        for (int i = 1; i <= 10; i++) {
            double next = schedule.nextTemperature(temp, i, 100.0);
            assertTrue(next < temp, "Temperature must decrease: " + next + " < " + temp);
            assertEquals(temp * 0.90, next, 1e-6);
            temp = next;
        }
    }

    @Test
    void testLinearCoolingDecreasesByFixedDelta() {
        CoolingSchedule schedule = new CoolingSchedule.Linear(5.0);
        double temp = 100.0;
        double next = schedule.nextTemperature(temp, 1, 100.0);
        assertEquals(95.0, next, 1e-6);
    }

    @Test
    void testLogarithmicCoolingDecreases() {
        CoolingSchedule schedule = new CoolingSchedule.Logarithmic(1.0);
        double t1 = schedule.nextTemperature(100.0, 1, 100.0);
        double t10 = schedule.nextTemperature(t1, 10, 100.0);
        assertTrue(t10 < t1, "Logarithmic cooling must decrease over iterations");
    }

    @Test
    void testInvalidAlphaThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new CoolingSchedule.Geometric(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new CoolingSchedule.Geometric(1.5));
    }
}
