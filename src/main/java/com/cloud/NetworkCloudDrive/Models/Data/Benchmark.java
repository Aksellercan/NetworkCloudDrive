package com.cloud.NetworkCloudDrive.Models.Data;

/**
 * Benchmarks how long process has taken
 */
public class Benchmark {
    private long timeTaken = System.currentTimeMillis();

    public Benchmark() {
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public long stopTimerAndGetTimeTaken() {
        this.timeTaken = System.currentTimeMillis() - this.timeTaken;
        return this.timeTaken;
    }
}
