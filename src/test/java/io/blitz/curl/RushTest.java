package io.blitz.curl;

import io.blitz.curl.config.Interval;
import io.blitz.curl.config.Pattern;
import io.blitz.curl.exception.AuthenticationException;
import io.blitz.curl.exception.BlitzException;
import io.blitz.curl.exception.ValidationException;
import io.blitz.curl.rush.IRushListener;
import io.blitz.curl.rush.RushResult;
import io.blitz.mock.MockURLStreamHandler;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author ghermeto
 */
public class RushTest {

    private static MockURLStreamHandler handler;
    private static URLStreamHandlerFactory factory;

    @BeforeClass
    public static void mockSetup() {
        handler = CurlTestSuite.getHandler();
        factory = CurlTestSuite.getFactory();
        try{ URL.setURLStreamHandlerFactory(factory); } catch(Error e) {}
        handler.getConnection().setUseMapped(true);
    }

    @Before
    public void resetMock() {
        handler.getConnection().setHeaders(new HashMap<String, String>());
        handler.getConnection().setOutput(new ByteArrayOutputStream());
    }
    
    @Test
    public void successful() throws MalformedURLException, InterruptedException {
        //login response
        handler.getConnection().setMappedData("/login/api", 
                "{\"ok\":true, \"api_key\":\"private-key\"}");
        
        //execute response
        handler.getConnection().setMappedData("/api/1/curl/execute", 
                "{\"ok\":true, \"status\":\"queued\", "
                    + "\"region\":\"california\", \"job_id\":\"c123\"}");
        
        //job_status response
        handler.getConnection().setMappedData("/api/1/jobs/c123/status",
                "{\"_id\":\"c123\",\"ok\":true, \"status\":\"completed\","
                + "\"result\":{\"region\":\"california\",\"timeline\":["
                + "{\"duration\":0.1,\"total\":10,\"executed\":8,\"errors\":1,"
                + "\"timeouts\":1,\"volume\":10},"
                + "{\"duration\":0.2,\"total\":100,\"executed\":80,\"errors\":10,"
                + "\"timeouts\":10,\"volume\":100}"
                + "]}}");
        
        Rush r = new Rush("user", "public-key", "localhost", 9295);
        Collection<TestStep> steps = new ArrayList<TestStep>();
        steps.add(new TestStep(new URL("http://example.com")));
        r.setSteps(steps);
        Collection<Interval> intervals = new ArrayList<Interval>();
        intervals.add(new Interval(1, 10, 10));
        r.setPattern(new Pattern(intervals));
        r.addListener(new IRushListener() {
            public void onComplete(RushResult result) {
                assertNotNull(result);
                assertNotNull(result.getTimeline());
                assertEquals("california", result.getRegion());
                assertFalse(result.getTimeline().isEmpty());
            }
            public boolean onStatus(RushResult result) { return true; }
        });
        r.execute();
        assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
        String output = handler.getConnection().getOutputStreamAsString("UTF-8");
        assertEquals(output, "{\"pattern\":{\"intervals\":["
                + "{\"start\":1,\"end\":10,\"duration\":10}]},"
                + "\"steps\":[{\"url\":\"http://example.com\"}]}");
    }
    
    @Test
    public void failedLogin() throws MalformedURLException {
        //login response
        handler.getConnection().setMappedData("/login/api", 
                "{\"error\":\"login\", \"reason\":\"test\"}");
        
        try {
            Rush r = new Rush("user", "public-key", "localhost", 9295);
            Collection<TestStep> steps = new ArrayList<TestStep>();
            steps.add(new TestStep(new URL("http://example.com")));
            r.setSteps(steps);
            Collection<Interval> intervals = new ArrayList<Interval>();
            intervals.add(new Interval(1, 10, 10));
            r.setPattern(new Pattern(intervals));
            r.addListener(new IRushListener() {
                public void onComplete(RushResult result) {
                    // fail if we get a ok message
                    assertFalse(true);
                }
                public boolean onStatus(RushResult result) { 
                    assertFalse(true);
                    return true; 
                }
            });
            r.execute();
        }
        catch(AuthenticationException ex) {
            assertNotNull(ex);
            assertEquals("login", ex.getError());
            assertEquals("test", ex.getReason());
        }
        finally {
            assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "public-key");
        }
    }

    @Test
    public void failedToQueue() throws MalformedURLException{
        //login response
        handler.getConnection().setMappedData("/login/api", 
                "{\"ok\":true, \"api_key\":\"private-key\"}");
        
        //execute response
        handler.getConnection().setMappedData("/api/1/curl/execute", 
                "{\"error\":\"throttle\", \"reason\":\"Slow down please!\"}");

        try {
            Rush r = new Rush("user", "public-key", "localhost", 9295);
            Collection<TestStep> steps = new ArrayList<TestStep>();
            steps.add(new TestStep(new URL("http://example.com")));
            r.setSteps(steps);
            Collection<Interval> intervals = new ArrayList<Interval>();
            intervals.add(new Interval(1, 10, 10));
            r.setPattern(new Pattern(intervals));
            r.addListener(new IRushListener() {
                public void onComplete(RushResult result) {
                    // fail if we get a ok message
                    assertFalse(true);
                }
                public boolean onStatus(RushResult result) { 
                    assertFalse(true);
                    return true; 
                }
            });
            r.execute();
        }
        catch(BlitzException ex) {
            assertNotNull(ex);
            assertEquals("throttle", ex.getError());
            assertEquals("Slow down please!", ex.getReason());
        }
        finally {
            assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
            String output = handler.getConnection().getOutputStreamAsString("UTF-8");
            assertEquals(output, "{\"pattern\":{\"intervals\":["
                    + "{\"start\":1,\"end\":10,\"duration\":10}]},"
                    + "\"steps\":[{\"url\":\"http://example.com\"}]}");
        }
    }

    @Test
    public void failedPatternValidation() throws MalformedURLException{
        try {
            Rush r = new Rush("user", "public-key", "localhost", 9295);
            Collection<TestStep> steps = new ArrayList<TestStep>();
            steps.add(new TestStep(new URL("http://example.com")));
            r.setSteps(steps);
            r.addListener(new IRushListener() {
                public void onComplete(RushResult result) {
                    // fail if we get a ok message
                    assertFalse(true);
                }
                public boolean onStatus(RushResult result) { 
                    assertFalse(true);
                    return true; 
                }
            });
            r.execute();

        } catch (ValidationException ex) {
            assertNotNull(ex);
            assertEquals("validation", ex.getError());
            assertEquals("A valid pattern is required", ex.getReason());
        }
    }

    @Test
    public void failedStepsValidation() throws MalformedURLException{
        try {
            Rush r = new Rush("user", "public-key", "localhost", 9295);
            Collection<Interval> intervals = new ArrayList<Interval>();
            intervals.add(new Interval(1, 10, 10));
            r.setPattern(new Pattern(intervals));
            r.addListener(new IRushListener() {
                public void onComplete(RushResult result) {
                    // fail if we get a ok message
                    assertFalse(true);
                }
                public boolean onStatus(RushResult result) { 
                    assertFalse(true);
                    return true; 
                }
            });
            r.execute();
        } catch (ValidationException ex) {
            assertNotNull(ex);
            assertEquals("validation", ex.getError());
            assertEquals("At least one step is required", ex.getReason());
        }
    }

    @Test
    public void abort() throws MalformedURLException, InterruptedException {
        //abort response
        handler.getConnection().setMappedData("/api/1/jobs/c123/abort",
                "{\"_id\":\"c123\",\"ok\":true}");
        
        //login response
        handler.getConnection().setMappedData("/login/api", 
                "{\"ok\":true, \"api_key\":\"private-key\"}");
        
        //execute response
        handler.getConnection().setMappedData("/api/1/curl/execute", 
                "{\"ok\":true, \"status\":\"queued\", "
                    + "\"region\":\"california\", \"job_id\":\"c123\"}");
        
        //job_status response
        handler.getConnection().setMappedData("/api/1/jobs/c123/status",
                "{\"_id\":\"c123\",\"ok\":true, \"status\":\"running\","
                + "\"result\":{\"region\":\"california\",\"timeline\":["
                + "{\"duration\":0.1,\"total\":10,\"executed\":8,\"errors\":1,"
                + "\"timeouts\":1,\"volume\":10},"
                + "{\"duration\":0.2,\"total\":100,\"executed\":80,\"errors\":10,"
                + "\"timeouts\":10,\"volume\":100}"
                + "]}}");
        
        Rush r = new Rush("user", "public-key", "localhost", 9295);
        Collection<TestStep> steps = new ArrayList<TestStep>();
        steps.add(new TestStep(new URL("http://example.com")));
        r.setSteps(steps);
        Collection<Interval> intervals = new ArrayList<Interval>();
        intervals.add(new Interval(1, 10, 10));
        r.setPattern(new Pattern(intervals));
        r.addListener(new IRushListener() {
            public boolean onStatus(RushResult result) {
                assertNotNull(result);
                assertNotNull(result.getTimeline());
                assertEquals("california", result.getRegion());
                assertFalse(result.getTimeline().isEmpty());
                return false;
            }
            public void onComplete(RushResult result) {
                assertFalse(true);
            }
        });
        r.execute();
        assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
        String output = handler.getConnection().getOutputStreamAsString("UTF-8");
        assertEquals(output, "{\"pattern\":{\"intervals\":["
                + "{\"start\":1,\"end\":10,\"duration\":10}]},"
                + "\"steps\":[{\"url\":\"http://example.com\"}]}");
    }
}

