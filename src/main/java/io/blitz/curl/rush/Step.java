/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.blitz.curl.rush;

/**
 * Per-step (for transactional rushes) metrics of a rush at time[i]
 * @author ghermeto
 * @see Point
 */
public class Step {
    
    /**
     * The duration of this step, when successful
     */
    private Double duration;
    
    /**
     * Average TCP connect time for this step
     */
    private Double connect;
    
    /**
     * Cummulative errors for this step
     */
    private Integer errors;
    
    /**
     * Cummulative timeouts for this step
     */
    private Integer timeouts;
    
    /**
     * Cummulative assertion failures on status code for this step
     */
    private Integer asserts;

    public Step(Double duration, Double connect, 
            Integer errors, Integer timeouts, Integer asserts) {
        
        this.duration = duration;
        this.connect = connect;
        this.errors = errors;
        this.timeouts = timeouts;
        this.asserts = asserts;
    }

    public Integer getAsserts() {
        return asserts;
    }

    public Double getConnect() {
        return connect;
    }

    public Double getDuration() {
        return duration;
    }

    public Integer getErrors() {
        return errors;
    }

    public Integer getTimeouts() {
        return timeouts;
    }
}