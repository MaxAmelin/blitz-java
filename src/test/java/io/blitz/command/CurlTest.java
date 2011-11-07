package io.blitz.command;

import io.blitz.curl.*;
import io.blitz.curl.config.BasicAuthentication;
import io.blitz.curl.config.HttpHeader;
import io.blitz.curl.config.Interval;
import io.blitz.curl.config.variable.AlphaVariable;
import io.blitz.curl.config.variable.IVariable;
import io.blitz.curl.config.variable.ListVariable;
import io.blitz.curl.config.variable.NumberVariable;
import io.blitz.curl.config.variable.UdidVariable;
import io.blitz.mock.MockURLStreamHandler;
import java.io.ByteArrayOutputStream;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author ghermeto
 */
public class CurlTest {

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
    public void re_dq_success() {
        String inside = "\"inside double quotes\"";
        String quoted = "\"inside \\\"double\\\" quotes\"";
        assertTrue(inside.matches(Curl.RE_DQ));
        assertTrue(quoted.matches(Curl.RE_DQ));
        
        Pattern p = Pattern.compile(Curl.RE_DQ);
        Scanner sc = new Scanner(inside);
        assertEquals(inside, sc.findInLine(p));
        
        sc = new Scanner(quoted);
        assertEquals(quoted, sc.findInLine(p));

        sc = new Scanner("--user-agent \"Mozilla 4.5.1\" http://example.com");
        assertEquals("\"Mozilla 4.5.1\"", sc.findInLine(p));
    }
   
    @Test
    public void re_dq_fail() {
        String outside = "outside double quotes";
        String quoted = "'inside single quotes'";
        
        Pattern p = Pattern.compile(Curl.RE_DQ);
        Scanner sc = new Scanner(outside);
        assertNull(sc.findInLine(p));
        
        sc = new Scanner(quoted);
        assertNull(sc.findInLine(p));
    }
    
    @Test
    public void re_sq_success() {
        String inside = "'inside double quotes'";
        String quoted = "'inside \\'double\\' quotes'";
        assertTrue(inside.matches(Curl.RE_SQ));
        assertTrue(quoted.matches(Curl.RE_SQ));
        
        Pattern p = Pattern.compile(Curl.RE_SQ);
        Scanner sc = new Scanner(inside);
        assertEquals(inside, sc.findInLine(p));
        
        sc = new Scanner(quoted);
        assertEquals(quoted, sc.findInLine(p));

        sc = new Scanner("--user-agent 'Mozilla 4.5.1' http://example.com");
        assertEquals("'Mozilla 4.5.1'", sc.findInLine(p));
    }
   
    @Test
    public void re_sq_fail() {
        String outside = "outside double quotes";
        String quoted = "\"inside single quotes\"";
        
        Pattern p = Pattern.compile(Curl.RE_SQ);
        Scanner sc = new Scanner(outside);
        assertNull(sc.findInLine(p));
        
        sc = new Scanner(quoted);
        assertNull(sc.findInLine(p));
    }
    
    @Test
    public void re_pattern() {
        assertTrue("-p 10-20:50".matches(Curl.RE_PATTERN));
        assertTrue("--pattern 10-20:50".matches(Curl.RE_PATTERN));
        assertTrue(" -p 10-20:50".matches(Curl.RE_PATTERN));
        assertTrue(" --pattern 10-20:50".matches(Curl.RE_PATTERN));
        
        assertFalse("a-p 10-20:50".matches(Curl.RE_PATTERN));
        assertFalse("-pa b".matches(Curl.RE_PATTERN));
    }
    
    @Test
    public void re_pattern_value() {
        Matcher matcher = Pattern.compile(Curl.RE_PATTERN_VALUE).matcher("10-20:30");
        assertTrue(matcher.matches());
        Integer start = Integer.parseInt(matcher.group(1));
        Integer end = Integer.parseInt(matcher.group(2));
        Integer duration = Integer.parseInt(matcher.group(3));
        assertEquals(new Integer(10), start);
        assertEquals(new Integer(20), end);
        assertEquals(new Integer(30), duration);
    }
    
    @Test
    public void re_variable() {
        String test1 = "-v:abacate";
        String test2 = "--variable:abacaxi";
        assertTrue(test1.matches(Curl.RE_VARIABLE));
        assertTrue(test2.matches(Curl.RE_VARIABLE));

        Matcher matcher = Pattern.compile(Curl.RE_VARIABLE).matcher(test1);
        assertTrue(matcher.matches());
        String name = matcher.group(1);
        assertEquals("abacate", name);

        matcher = Pattern.compile(Curl.RE_VARIABLE).matcher(test2);
        assertTrue(matcher.matches());
        name = matcher.group(1);
        assertEquals("abacaxi", name);
    }
    
    @Test
    public void re_var_list() {
        String test1 = "list[a,b]";
        String test2 = "[1,2,3]";
        String test3 = "list[unique]";
        
        Matcher m = Pattern.compile(Curl.RE_VAR_LIST).matcher(test1);
        assertTrue(m.matches());
        String[] values = m.group(2).split(",");
        assertEquals("a", values[0]);
        assertEquals("b", values[1]);
        
        m = Pattern.compile(Curl.RE_VAR_LIST).matcher(test2);
        assertTrue(m.matches());
        values = m.group(2).split(",");
        assertEquals("1", values[0]);
        assertEquals("2", values[1]);
        assertEquals("3", values[2]);

        m = Pattern.compile(Curl.RE_VAR_LIST).matcher(test3);
        assertTrue(m.matches());
        values = m.group(2).split(",");
        assertEquals("unique", values[0]);
    }
    
    @Test
    public void re_var_alpha_1() {
        assertTrue("a".matches(Curl.RE_VAR_ALPHA_1));
        assertTrue("alpha".matches(Curl.RE_VAR_ALPHA_1));
    }

    @Test
    public void re_var_alpha_2() {
        String test1 = "a[1,2,3]";
        String test2 = "alpha[1,2]";
        String test3 = "a[1,2]";
        
        Matcher m = Pattern.compile(Curl.RE_VAR_ALPHA_2).matcher(test1);
        assertTrue(m.matches());
        Integer min = Integer.parseInt(m.group(2));
        Integer max = Integer.parseInt(m.group(3));
        assertEquals(new Integer(1), min);
        assertEquals(new Integer(2), max);

        m = Pattern.compile(Curl.RE_VAR_ALPHA_2).matcher(test2);
        assertTrue(m.matches());
        min = Integer.parseInt(m.group(2));
        max = Integer.parseInt(m.group(3));
        assertEquals(new Integer(1), min);
        assertEquals(new Integer(2), max);

        m = Pattern.compile(Curl.RE_VAR_ALPHA_2).matcher(test3);
        assertTrue(m.matches());
        min = Integer.parseInt(m.group(2));
        max = Integer.parseInt(m.group(3));
        assertEquals(new Integer(1), min);
        assertEquals(new Integer(2), max);
    }
    
    @Test
    public void singleURL() {
        String url = "http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, url);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals(url, step.getUrl().toString());
    }
    
    @Test
    public void userAgent() {
        String cmd = "--user-agent \"Mozilla Firefox 3.6\" http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals("Mozilla Firefox 3.6", step.getUserAgent());
    }
    
    @Test
    public void noCommand() {
        boolean throwed= false;
        try {
            AbstractTest test = Curl.parse(null, null, null, null, "");
        }
        catch(IllegalArgumentException e) {
            throwed = true;
        }
        assertTrue(throwed);
    }

    @Test
    public void oneCookie() {
        String cmd = "--cookie jsessionid=A256343FC47E http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals(1, step.getCookies().size());
        HttpCookie cookie = ((List<HttpCookie>)step.getCookies()).get(0);
        assertEquals("jsessionid", cookie.getName());
        assertEquals("A256343FC47E", cookie.getValue());
    }
    
    @Test
    public void twoCookies() {
        String cmd = "--cookie a=b -b c=d http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals(2, step.getCookies().size());
        HttpCookie cookie = ((List<HttpCookie>)step.getCookies()).get(0);
        assertEquals("a", cookie.getName());
        assertEquals("b", cookie.getValue());
        cookie = ((List<HttpCookie>)step.getCookies()).get(1);
        assertEquals("c", cookie.getName());
        assertEquals("d", cookie.getValue());
    }
    
    @Test
    public void data() {
        String cmd = "--data form_field=123 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        String data = ((List<String>)step.getContent().getData()).get(0);
        assertEquals("form_field=123", data);
    }
    
    @Test
    public void referer() {
        String cmd = "-e http://www.google.com http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals("http://www.google.com", step.getReferrer().toString());
    }
    
    @Test
    public void header() {
        String cmd = "--header h1:v1 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals(1, step.getHeaders().size());
        HttpHeader header = ((List<HttpHeader>)step.getHeaders()).get(0);
        assertEquals("h1", header.getField());
        assertEquals("v1", header.getValue());
    }
    
    @Test
    public void region() {
        String cmd = "--region california http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals("california", test.getRegion());
    }
    
    @Test
    public void status() {
        String cmd = "-s 200 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals(new Integer(200), step.getStatus());
    }
    
    @Test
    public void timeout() {
        String cmd = "-T 5000 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals(new Integer(5000), step.getTimeout());
    }

    @Test
    public void user() {
        String cmd = "-u john:smith http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        BasicAuthentication auth = step.getUser();
        assertEquals("john", auth.getUsername());
        assertEquals("smith", auth.getPassword());
    }

    @Test
    public void request() {
        String cmd = "-X GET http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals("GET", step.getRequest());
    }
    
    @Test
    public void onePattern() {
        String cmd = "-p 10-20:30 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        io.blitz.curl.config.Pattern p = ((Rush)test).getPattern();
        assertEquals(1, p.getIntervals().size());
        Interval i = ((List<Interval>)p.getIntervals()).get(0);
        assertEquals(new Integer(10), i.getStart());
        assertEquals(new Integer(20), i.getEnd());
        assertEquals(new Integer(30), i.getDuration());
    }

    @Test
    public void twoPatterns() {
        String cmd = "-p 10-20:30,4-5:6 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        io.blitz.curl.config.Pattern p = ((Rush)test).getPattern();
        assertEquals(2, p.getIntervals().size());
        
        Interval i1 = ((List<Interval>)p.getIntervals()).get(0);
        assertEquals(new Integer(10), i1.getStart());
        assertEquals(new Integer(20), i1.getEnd());
        assertEquals(new Integer(30), i1.getDuration());

        Interval i2 = ((List<Interval>)p.getIntervals()).get(1);
        assertEquals(new Integer(4), i2.getStart());
        assertEquals(new Integer(5), i2.getEnd());
        assertEquals(new Integer(6), i2.getDuration());
    }
    
    @Test 
    public void listVariable() {
        String cmd = "-v:var list[a,b,c] http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertTrue(step.getVariables().containsKey("var"));
        ListVariable var = (ListVariable) step.getVariables().get("var");
        List<String> list = (List<String>) var.getEntries();
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test 
    public void alphaVariable() {
        String cmd = "-v:var alpha[1,2] http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertTrue(step.getVariables().containsKey("var"));
        AlphaVariable var = (AlphaVariable) step.getVariables().get("var");
        assertEquals(1, var.getMin());
        assertEquals(2, var.getMax());
    }

    @Test 
    public void numberVariable() {
        String cmd = "-v:var number[1,2] http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertTrue(step.getVariables().containsKey("var"));
        NumberVariable var = (NumberVariable) step.getVariables().get("var");
        assertEquals(1, var.getMin());
        assertEquals(2, var.getMax());
    }
    
    @Test 
    public void udidVariable() {
        String cmd = "-v:var udid http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertTrue(step.getVariables().containsKey("var"));
        IVariable var = step.getVariables().get("var");
        assertEquals(UdidVariable.class, var.getClass());
    }
    
    @Test
    public void tlsv1() {
        String cmd = "-1 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals("tlsv1", step.getSsl());
    }
    
    @Test
    public void sslv2() {
        String cmd = "-2 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals("sslv2", step.getSsl());
    }

    @Test
    public void sslv3() {
        String cmd = "-3 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());
        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals("sslv3", step.getSsl());
    }
    
    @Test
    public void multistep() {
        String cmd = "http://example.com http://example.com/test";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(2,test.getSteps().size());
    
        TestStep step1 = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step1.getUrl().toString());

        TestStep step2 = ((List<TestStep>)test.getSteps()).get(1);
        assertEquals("http://example.com/test", step2.getUrl().toString());
    }

    @Test
    public void multistepWithOptions() {
        String cmd = "-s 200 -X POST http://example.com -X GET http://example.com/test";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(2,test.getSteps().size());

        TestStep step1 = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step1.getUrl().toString());
        assertEquals(new Integer(200), step1.getStatus());
        assertEquals("POST", step1.getRequest());

        TestStep step2 = ((List<TestStep>)test.getSteps()).get(1);
        assertEquals("http://example.com/test", step2.getUrl().toString());
        assertEquals("GET", step2.getRequest());
    }
    
    @Test
    public void withOptions() {
        String cmd = "-s 200 -X POST -r ireland -T 1000 http://example.com";
        AbstractTest test = Curl.parse(null, null, null, null, cmd);
        assertEquals(1,test.getSteps().size());

        TestStep step = ((List<TestStep>)test.getSteps()).get(0);
        assertEquals("http://example.com", step.getUrl().toString());
        assertEquals(new Integer(200), step.getStatus());
        assertEquals("POST", step.getRequest());
        assertEquals("ireland", test.getRegion());
        assertEquals(new Integer(1000), step.getTimeout());
    }
}