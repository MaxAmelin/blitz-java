package io.blitz.curl;

import io.blitz.curl.config.variable.IVariable;
import java.util.Collection;
import java.util.Map;

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
     * Variables to be used on the url. The key should be the variable name and
     * the variable instance
     */
    private Map<String, IVariable> variables;

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
     * Getter for the variable hash
     * @return map of variables
     */
    public Map<String, IVariable> getVariables() {
        return variables;
    }

    /**
     * Setter for the variable hash
     * @param variables 
     */
    public void setVariables(Map<String, IVariable> variables) {
        this.variables = variables;
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
