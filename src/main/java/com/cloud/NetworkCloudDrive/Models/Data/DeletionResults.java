package com.cloud.NetworkCloudDrive.Models.Data;

public class DeletionResults extends Benchmark {
    private int successful_removals;
    private int removal_failures;

    public DeletionResults() {
    }

    public void incrementSuccessfulRemovals() {
        this.successful_removals++;
    }

    public void incrementRemovalFailures() {
        this.removal_failures++;
    }

    public int getSuccessful_removals() {
        return successful_removals;
    }

    public void setSuccessful_removals(int successful_removals) {
        this.successful_removals = successful_removals;
    }

    public int getRemoval_failures() {
        return removal_failures;
    }

    public void setRemoval_failures(int removal_failures) {
        this.removal_failures = removal_failures;
    }

    @Override
    public String toString() {
        return String.format(
                "Results\nSuccessfully Removed Files: %d\nFiles Failed to Remove: %d\nTime Taken: %d ms\n",
                this.successful_removals, this.removal_failures, getTimeTaken());
    }
}
