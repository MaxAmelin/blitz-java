package io.blitz.curl;

import java.util.Collection;

/**
 * Base class for the Blitz curl tests.
 * @author ghermeto
 */
public class TestEntity {

    /**
     * Sequential test steps
     */
    private Collection<TestStep> steps;

    /**
     * Region from which the test should start
     */
    private String region;
    
    /**
     * Getter for the region property
     * @return region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Setter for the region property
     * @param region 
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Getter for the list of test steps
     * @return collection of steps
     */
    public Collection<TestStep> getSteps() {
        return steps;
    }

    /**
     * Setter for the list of test steps
     * @param steps 
     */
    public void setSteps(Collection<TestStep> steps) {
        this.steps = steps;
    }
}
