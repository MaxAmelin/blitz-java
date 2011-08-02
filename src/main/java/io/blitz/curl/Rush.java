package io.blitz.curl;

import io.blitz.curl.config.Pattern;
import io.blitz.curl.exception.ValidationException;
import io.blitz.curl.rush.IRushListener;
import io.blitz.curl.rush.Point;
import io.blitz.curl.rush.RushResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Used to generate a Rush Test.
 * <p> 
 * Rushing in Blitz parlance is load and performance testing. Instantly launches 
 * a load test against your app, where the number of concurrent users goes from
 * the min interval value to the max interval value in the specified duration.
 * </p>
 * <pre>
 * Rush rush = new Rush("you@example.com", "my-bitz-api-key");
 * rush.setUrl(new java.net.URL("http://your.cool.app"));
 * Collection<Interval> intervals = new ArrayList<Interval>();
 * intervals.add(new Interval(1, 250, 60));
 * rush.setPattern(new Pattern(intervals));
 * rush.addListener(new IRushListener() {
 *      booolean onData(RushResult result) {
 *          //do something...
 *      }
 * });
 * rush.execute();
 * </pre>
 * @author ghermeto
 * @version 0.1.0
 * @see io.blitz.curl.AbstractTest
 */
public class Rush extends AbstractTest<IRushListener, RushResult> {
    
    /**
     * Rush pattern. Must be set before test execution. The pattern should have
     * at least 1 (one) interval.
     */
    private Pattern pattern;

    public Rush(String username, String apiKey) {
        setCredentials(username, apiKey);
    }

    public Rush(String username, String apiKey, String host, Integer port) {
        setCredentials(username, apiKey, host, port);
    }
    
    /**
     * Getter for the pattern property
     * @return pattern object with the list of intervals
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Setter for the pattern property
     * @param pattern 
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Verifies the Rush requirements. Should throw a <code>ValidationException</code>
     * if the URL field is not set or if a pattern with at least 1 interval 
     * is not present.
     * @throws ValidationException 
     */
    @Override
    public void checkRequirements() throws ValidationException {
        if(getUrl() == null) {
            throw new ValidationException("Url is required");
        }
        else if(pattern == null || 
                pattern.getIntervals() == null ||
                pattern.getIntervals().isEmpty()) {

            throw new ValidationException("A valid pattern is required");
        }
    }

    /**
     * Should return a <code>RushResult</code> object populated with the 
     * successful response from the server.
     * @param result the deserialized result from the JSON response
     * @return a successful rush result object
     * @see RushResult
     */
    @Override
    protected RushResult createSuccessResult(Map<String, Object> result) {
        String region = (String) result.get("region");
        Collection<Point> timeline = new ArrayList<Point>();

        Collection<?> resultLine = (Collection<?>) result.get("timeline");
        if(resultLine != null) {
            for(Object obj : resultLine) {
                Map<String, Object> item = (Map<String, Object>) obj;
                Number timestamp = (Number) item.get("timestamp");
                Number duration = (Number) item.get("duration");
                Integer total = (Integer) item.get("total");
                Integer hits = (Integer) item.get("executed");
                Integer errors = (Integer) item.get("errors");
                Integer timeouts = (Integer) item.get("timeouts");
                Integer volume = (Integer) item.get("volume");
                Integer txBytes = (Integer) item.get("txBytes");
                Integer rxBytes = (Integer) item.get("rxBytes");
                
                Date time = (timestamp == null) ? null : new Date(timestamp.intValue()*1000);
               Point point = new Point(time, duration.doubleValue(), total, hits, 
                       errors, timeouts, volume, txBytes, rxBytes);
               
               timeline.add(point);
            }
        }
        return new RushResult(region, timeline);
    }
}
