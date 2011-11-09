package io.blitz.curl.rush;

import java.util.Collection;

/**
 * Snapshot of a rush at time[i] containing information about hits, errors
 * timeouts, etc.
 * @author ghermeto
 * @see Step
 */
public class Point {
    
    /**
     * The timestamp of this snapshot
     */
    private Double timestamp;
    
    /**
     * The average response time at this time
     */
    private Double duration;
    
    /**
     * The total number of hits that were generated
     */
    private Integer total;
    
    /**
     * The number of successful hits
     */
    private Integer hits;
    
    /**
     * The number of errors
     */
    private Integer errors;
    
    /**
     * The number of timeouts
     */
    private Integer timeouts;
    
    /**
     * The concurrency level at this time
     */
    private Integer volume;
    
    /**
     * The total number of bytes sent
     */
    private Integer txBytes;
    
    /**
     * The total number of bytes received
     */
    private Integer rxBytes;
    
    /**
     * Per-step metric at this point in time
     */
    private Collection<Step> steps;

    public Point(Double timestamp, Double duration, Integer total, 
            Integer hits, Integer errors, Integer timeouts, Integer volume, 
            Integer txBytes, Integer rxBytes, Collection<Step> steps) {
        
        this.timestamp = timestamp;
        this.duration = duration;
        this.total = total;
        this.hits = hits;
        this.errors = errors;
        this.timeouts = timeouts;
        this.volume = volume;
        this.txBytes = txBytes;
        this.rxBytes = rxBytes;
        this.steps = steps;
    }

    public Double getDuration() {
        return duration;
    }

    public Integer getErrors() {
        return errors;
    }

    public Integer getHits() {
        return hits;
    }

    public Integer getRxBytes() {
        return rxBytes;
    }

    public Integer getTimeouts() {
        return timeouts;
    }

    public Double getTimestamp() {
        return timestamp;
    }

    public Integer getTotal() {
        return total;
    }

    public Integer getTxBytes() {
        return txBytes;
    }

    public Integer getVolume() {
        return volume;
    }

    public Collection<Step> getSteps() {
        return steps;
    }
}
