package io.github.nickm980.smallville.models;

import java.sql.Time;
import java.time.LocalDateTime;
import io.github.nickm980.smallville.Smallville;


public class AccessTime {
    private static Timekeeper timekeeper;
    public static LocalDateTime START = timekeeper.getSimulationTime();
    
    private LocalDateTime lastAccessed;
    private LocalDateTime createdAt;
    
    public AccessTime(Timekeeper timekeeper) {
        this.timekeeper = timekeeper;

	this.lastAccessed = Smallville.getServer().getSimulationService().getTimekeeper().getSimulationTime();
	this.createdAt = Smallville.getServer().getSimulationService().getTimekeeper().getSimulationTime();
    }
    
    public LocalDateTime createdAt() {
	return createdAt;
    }
    
    public void update() {
	    this.lastAccessed = Smallville.getServer().getSimulationService().getTimekeeper().getSimulationTime();
    }
    
    public LocalDateTime getLastAccessed() {
	return lastAccessed;
    }
}
