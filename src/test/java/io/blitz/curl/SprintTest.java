package io.blitz.curl;

import io.blitz.curl.exception.AuthenticationException;
import io.blitz.curl.exception.BlitzException;
import io.blitz.curl.exception.ValidationException;
import io.blitz.curl.sprint.ISprintListener;
import io.blitz.curl.sprint.SprintResult;
import io.blitz.curl.sprint.Step;
import io.blitz.mock.MockURLStreamHandler;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author ghermeto
 */
public class SprintTest {

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
    public void successful() throws MalformedURLException, InterruptedException{
        //login response
        handler.getConnection().setMappedData("/login/api", 
                "{\"ok\":true, \"api_key\":\"private-key\"}");
        
        //execute response
        handler.getConnection().setMappedData("/api/1/curl/execute", 
                "{\"ok\":true, \"status\":\"queued\", "
                    + "\"region\":\"california\", \"job_id\":\"a123\"}");
        
        //job_status response
        handler.getConnection().setMappedData("/api/1/jobs/a123/status",
                "{\"_id\":\"a123\",\"ok\":true, \"status\":\"completed\","
                + "\"result\":{\"region\":\"california\",\"duration\":10,"
                + "\"steps\":[{\"duration\":10,\"connect\":1,"
                + "\"request\":{\"line\":\"GET / HTTP/1.1\",\"method\":\"GET\","
                + "\"url\":\"http://localhost:9295\",\"headers\":{\"a\":\"b\"},\"content\":\"abc\"},"
                + "\"response\":{\"line\":\"GET / HTTP/1.1\",\"message\":\"message\","
                + "\"status\":200,\"headers\":{\"c\":\"d\"},\"content\":\"abd\"}}]}}");
        
        Sprint s = new Sprint("user", "public-key", "localhost", 9295);
        Collection<TestStep> steps = new ArrayList<TestStep>();
        steps.add(new TestStep(new URL("http://example.com")));
        s.setSteps(steps);
        s.addListener(new ISprintListener() {
            public void onComplete(SprintResult result) {
                assertNotNull(result);
                assertNotNull(result.getSteps());
                assertEquals(result.getSteps().size(), 1);
                List<Step> steps = (List<Step>) result.getSteps();
                assertNotNull(steps.get(0).getRequest());
                assertNotNull(steps.get(0).getResponse());
                assertEquals("california", result.getRegion());
                assertEquals("GET", steps.get(0).getRequest().getMethod());
                assertEquals(new Integer(200), steps.get(0).getResponse().getStatus());
                assertNotNull(steps.get(0).getRequest().getHeaders());
                assertEquals(1, steps.get(0).getRequest().getHeaders().size());
                assertNotNull(steps.get(0).getRequest().getHeaders().get("a"));
                assertEquals("b", steps.get(0).getRequest().getHeaders().get("a"));
            }
            public boolean onStatus(SprintResult result){ return true; }
        });
        s.execute();
        assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
        String output = handler.getConnection().getOutputStreamAsString("UTF-8");
        assertEquals(output, "{\"steps\":[{\"url\":\"http://example.com\"}]}");
    }
    
    @Test
    public void failedLogin() throws MalformedURLException {
        //login response
        handler.getConnection().setMappedData("/login/api", 
                "{\"error\":\"login\", \"reason\":\"test\"}");

        try {
            Sprint s = new Sprint("user", "public-key", "localhost", 9295);
            Collection<TestStep> steps = new ArrayList<TestStep>();
            steps.add(new TestStep(new URL("http://example.com")));
            s.setSteps(steps);
            s.addListener(new ISprintListener() {
                public void onComplete(SprintResult result) {
                    assertFalse(true);
                }
                public boolean onStatus(SprintResult result) {
                    // fail if we get a ok message
                    assertFalse(true);
                    return true;
                }
            });
            s.execute();
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
    public void failedToQueue() throws MalformedURLException {
        //login response
        handler.getConnection().setMappedData("/login/api", 
                "{\"ok\":true, \"api_key\":\"private-key\"}");
        
        //execute response
        handler.getConnection().setMappedData("/api/1/curl/execute", 
                "{\"error\":\"throttle\", \"reason\":\"Slow down please!\"}");
        
        try {
            Sprint s = new Sprint("user", "public-key", "localhost", 9295);
            Collection<TestStep> steps = new ArrayList<TestStep>();
            steps.add(new TestStep(new URL("http://example.com")));
            s.setSteps(steps);
            s.addListener(new ISprintListener() {
                public void onComplete(SprintResult result) {
                    assertFalse(true);
                }
                public boolean onStatus(SprintResult result) {
                    // fail if we get a ok message
                    assertFalse(true);
                    return true;
                }
            });
            s.execute();
        }
        catch(BlitzException ex) {
            assertNotNull(ex);
            assertEquals("throttle", ex.getError());
            assertEquals("Slow down please!", ex.getReason());
        }
        finally {
            assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
            String output = handler.getConnection().getOutputStreamAsString("UTF-8");
            assertEquals(output, "{\"steps\":[{\"url\":\"http://example.com\"}]}");
        }
    }

    @Test
    public void failedStepsValidation() {
        try {
            Sprint s = new Sprint("user", "public-key", "localhost", 9295);
            s.addListener(new ISprintListener() {
                public void onComplete(SprintResult result) {
                    assertFalse(true);
                }
                public boolean onStatus(SprintResult result) {
                    // fail if we get a ok message
                    assertFalse(true);
                    return true;
                }
            });
            s.execute();

        } catch (ValidationException ex) {
            assertNotNull(ex);
            assertEquals("validation", ex.getError());
            assertEquals("At least one step is required", ex.getReason());
        }
    }

    @Test
    public void abort() throws MalformedURLException, InterruptedException{
        //abort response
        handler.getConnection().setMappedData("/api/1/jobs/a123/abort",
                "{\"_id\":\"a123\",\"ok\":true}");

        //login response
        handler.getConnection().setMappedData("/login/api", 
                "{\"ok\":true, \"api_key\":\"private-key\"}");
        
        //execute response
        handler.getConnection().setMappedData("/api/1/curl/execute", 
                "{\"ok\":true, \"status\":\"queued\", "
                    + "\"region\":\"california\", \"job_id\":\"a123\"}");
        
        //job_status response
        handler.getConnection().setMappedData("/api/1/jobs/a123/status",
                "{\"_id\":\"a123\",\"ok\":true, \"status\":\"running\","
                + "\"result\":{\"region\":\"california\",\"duration\":10,"
                + "\"steps\":[{\"duration\":10,\"connect\":1,"
                + "\"request\":{\"line\":\"GET / HTTP/1.1\",\"method\":\"GET\","
                + "\"url\":\"http://localhost:9295\",\"headers\":{\"a\":\"b\"},\"content\":\"abc\"},"
                + "\"response\":{\"line\":\"GET / HTTP/1.1\",\"message\":\"message\","
                + "\"status\":200,\"headers\":{\"c\":\"d\"},\"content\":\"abd\"}}]}}");
        
        Sprint s = new Sprint("user", "public-key", "localhost", 9295);
        Collection<TestStep> steps = new ArrayList<TestStep>();
        steps.add(new TestStep(new URL("http://example.com")));
        s.setSteps(steps);
        s.addListener(new ISprintListener() {
            public boolean onStatus(SprintResult result) {
                assertNotNull(result);
                assertNotNull(result.getSteps());
                assertEquals(result.getSteps().size(), 1);
                List<Step> steps = (List<Step>) result.getSteps();
                assertNotNull(steps.get(0).getRequest());
                assertNotNull(steps.get(0).getResponse());
                assertEquals("california", result.getRegion());
                assertEquals("GET", steps.get(0).getRequest().getMethod());
                assertEquals(new Integer(200), steps.get(0).getResponse().getStatus());
                assertNotNull(steps.get(0).getRequest().getHeaders());
                assertEquals(1, steps.get(0).getRequest().getHeaders().size());
                assertNotNull(steps.get(0).getRequest().getHeaders().get("a"));
                assertEquals("b", steps.get(0).getRequest().getHeaders().get("a"));
                return false;
            }
            public void onComplete(SprintResult result) {
                assertFalse(true);
            }
        });
        s.execute();
        assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
        String output = handler.getConnection().getOutputStreamAsString("UTF-8");
        assertEquals(output, "{\"steps\":[{\"url\":\"http://example.com\"}]}");
    }
}

