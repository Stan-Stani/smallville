package io.github.nickm980.smallville.entities;

import java.time.Duration;

import java.time.LocalDateTime;

public class Timekeeper {
    private static boolean isRealTime = true;
    private static LocalDateTime simulationTime;
    private static Duration timestepDuration;

    public void initialize() {
        isRealTime = true;
    }

    public static void initialize(LocalDateTime simulationTime, Duration timestepDuration) {

        Timekeeper.simulationTime = simulationTime;
        Timekeeper.timestepDuration = timestepDuration;
    }


    public static LocalDateTime getSimulationTime() {
        if (!isRealTime) {
            System.out.println("simulationTime: " + simulationTime);
            return simulationTime;
        } else if (isRealTime) {
            System.out.println("simulationTime: " + LocalDateTime.now());
            return LocalDateTime.now();
        }

        return null;
    }

    public static void setSimulationTime(LocalDateTime simulationTime) {
        Timekeeper.simulationTime = simulationTime;
    }

    public static Duration getTimestepDuration() {
        return timestepDuration;
    }

    public static void setTimestepDuration(Duration timestepDuration) {
        Timekeeper.timestepDuration = timestepDuration;
        if (timestepDuration.toMinutes() > 0) {
            isRealTime = false;
        }
    }

    public static void incrementSimulationTime() {
        if (!isRealTime) {
            if (simulationTime == null) {
                simulationTime = LocalDateTime.now();
            }
            simulationTime = simulationTime.plus(timestepDuration);
        }
    }

}