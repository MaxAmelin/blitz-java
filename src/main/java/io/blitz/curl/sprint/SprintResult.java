package io.blitz.curl.sprint;

import io.blitz.curl.IResult;
import java.util.Collection;

/**
 * Contains the result from a successful sprint.
 * @author ghermeto
 */
public class SprintResult implements IResult{
    
    /**
     * The region from which this sprint was executed
     */
    private String region;
    
    /**
     * The overall response time for the successful hit
     */
    private Double duration;
    
    /**
     * Stats about the individual steps
     */
    private Collection<Step> steps;
    
    public SprintResult(String region, Double duration, Collection<Step> steps) {
        this.region = region;
        this.duration = duration;
        this.steps = steps;
    }

    public Double getDuration() {
        return duration;
    }

    public String getRegion() {
        return region;
    }

    public Collection<Step> getSteps() {
        return steps;
    }
}
