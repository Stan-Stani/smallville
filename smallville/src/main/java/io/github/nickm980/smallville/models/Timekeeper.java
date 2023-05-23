package io.github.nickm980.smallville.models;

import java.time.Duration;

import java.time.Duration;
import java.time.LocalDateTime;

public class Timekeeper {

    private boolean isRealTime = true;
    private LocalDateTime simulationTime;
    private Duration timestepDuration;

    public Timekeeper() {
        isRealTime = true;
    }

    public Timekeeper(LocalDateTime simulationTime, Duration timestepDuration) {


        this.simulationTime = simulationTime;
        this.timestepDuration = timestepDuration;
    }

    public LocalDateTime getSimulationTime() {
        if (!isRealTime) {
            return simulationTime;
        } else if (isRealTime) {
            return LocalDateTime.now();
        }
        return null;
    }

    public void setSimulationTime(LocalDateTime simulationTime) {
        this.simulationTime = simulationTime;
    }

    public Duration getTimestepDuration() {
        return timestepDuration;
    }

    public void setTimestepDuration(Duration timestepDuration) {
        this.timestepDuration = timestepDuration;
    }

    public void incrementSimulationTime() {
        simulationTime = simulationTime.plus(timestepDuration);
    }


}


