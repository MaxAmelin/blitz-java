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
package io.blitz.command;

import io.blitz.curl.AbstractTest;
import io.blitz.curl.Rush;
import io.blitz.curl.Sprint;
import io.blitz.curl.TestStep;
import io.blitz.curl.config.BasicAuthentication;
import io.blitz.curl.config.Content;
import io.blitz.curl.config.HttpHeader;
import io.blitz.curl.config.Interval;
import io.blitz.curl.config.variable.AlphaVariable;
import io.blitz.curl.config.variable.IVariable;
import io.blitz.curl.config.variable.ListVariable;
import io.blitz.curl.config.variable.NumberVariable;
import io.blitz.curl.config.variable.UdidVariable;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates a curl (rush or sprint) test from a command line 
 * identical to the blitz bar.
 * @author ghermeto
 */
public class Curl {

    public static String RE_DQ = "\"[^\"\\\\\\r\\n]*(?:\\\\.[^\"\\\\\\r\\n]*)*\"";
    public static String RE_SQ = "'[^'\\\\\\r\\n]*(?:\\\\.[^'\\\\\\r\\n]*)*'";
    public static String RE_WS = "\\S+";
    public static String RE_PATTERN = "(?:.*\\s|^)(-p|--pattern)(?:\\s.+)";
    public static String RE_PATTERN_VALUE = "^(\\d+)-(\\d+):(\\d+)$";
    public static String RE_VARIABLE = "^(?:-v|--variable):(\\S+)$";
    public static String RE_VAR_LIST = "^(list)?\\[([^\\]]+)\\]$";
    public static String RE_VAR_ALPHA_1 = "^(a|alpha)$";
    public static String RE_VAR_ALPHA_2 = "^(a|alpha)\\[(\\d+),(\\d+)(,(\\d+))??\\]$";
    public static String RE_VAR_NUMBER_1 = "^(n|number)$";
    public static String RE_VAR_NUMBER_2 = "^(n|number)\\[(-?\\d+),(-?\\d+)(,(\\d+))?\\]$";
    public static String RE_VAR_UDID = "^(u|udid)$";
    

    /**
     * Parse a curl command line and create a sprint or rush instance, 
     * ready to be sent by the Client.
     * @param command the command to be parsed
     * @param username username to be used in the test
     * @param apiKey api key used to authenticate
     * @return a sprint of rush instance ready to run
     */
    public static AbstractTest parse(String username, 
            String apiKey, String command) {
        
        return parse(username, apiKey, null, null, command);
    }
    
    /**
     * Parse a curl command line and create a sprint or rush instance, 
     * ready to be sent by the Client.
     * @param command the command to be parsed
     * @param username username to be used in the test
     * @param apiKey api key used to authenticate
     * @param host host to connect
     * @param port port for the service
     * @return a sprint of rush instance ready to run
     */
    public static AbstractTest parse(String username, 
            String apiKey, String host, Integer port, String command) {
        return parse(username, apiKey, host, port, command, null);
    }

    /**
     * Parse a curl command line and create a sprint or rush instance,
     * ready to be sent by the Client.
     * @param command the command to be parsed
     * @param username username to be used in the test
     * @param apiKey api key used to authenticate
     * @param host host to connect
     * @param port port for the service
     * @param protocol protocol for the service
     * @return a sprint of rush instance ready to run
     */
    public static AbstractTest parse(String username,
            String apiKey, String host, Integer port, String command, String protocol) {
        
        if(command == null || command.length() == 0) {
            throw new IllegalArgumentException("No command line provided");
        }
        
        String quotesAndSpace = RE_DQ + "|" + RE_SQ + "|" + RE_WS;
        Pattern pattern = Pattern.compile(quotesAndSpace);
        Scanner scanner = new Scanner(command);

        // create the correct test instance based on the command
        AbstractTest test;
        if(command.matches(RE_PATTERN)) {
            test = new Rush(username, apiKey, host, port, protocol);
        }
        else {
            test = new Sprint(username, apiKey, host, port, protocol);
        }
        
        while(scanner.hasNext()) {
            String url = null;
            TestStep step = new TestStep();

            while(scanner.hasNext()) {
                String cmd = scanner.next();
                if(!cmd.startsWith("-")) {
                    url = cmd;
                    break;
                }

                //user-agent
                if(cmd.matches("-A|--user-agent")) {
                    String value = stripQuotes(scanner.findInLine(pattern));
                    step.setUserAgent(value);
                }
                //cookies
                else if(cmd.matches("-b|--cookie")) {
                    try {
                        String value = stripQuotes(scanner.findInLine(pattern));
                        if(step.getCookies() == null) {
                            step.setCookies(new ArrayList<HttpCookie>());
                        }
                        String[] pair = value.split("=");
                        step.getCookies().add(new HttpCookie(pair[0], pair[1]));
                    } 
                    catch (ArrayIndexOutOfBoundsException e) {
                        String msg = "Invalid cookie. Format: name=value";
                        throw new IllegalArgumentException(msg, e);
                    }
                }
                //input data
                else if(cmd.matches("-d|--data")) {
                    String value = stripQuotes(scanner.findInLine(pattern));
                    if(step.getContent() == null) {
                        step.setContent(new Content(new ArrayList<String>()));
                    }
                    step.getContent().getData().add(value);
                }
                //referer url
                else if(cmd.matches("-e|--referer")) {
                    try {
                        String value = stripQuotes(scanner.findInLine(pattern));
                        step.setReferrer(new URL(value));
                    } catch (MalformedURLException e) {
                        String msg = "Invalid referer URL";
                        throw new IllegalArgumentException(msg, e);
                    }
                }
                //header
                else if(cmd.matches("-H|--header")) {
                    try {
                        String value = stripQuotes(scanner.findInLine(pattern));
                        if(step.getHeaders() == null) {
                            step.setHeaders(new ArrayList<HttpHeader>());
                        }
                        String[] pair = value.split(":");
                        step.getHeaders().add(new HttpHeader(pair[0], pair[1]));
                    } 
                    catch (ArrayIndexOutOfBoundsException e) {
                        String msg = "Invalid header. Format: name:value";
                        throw new IllegalArgumentException(msg, e);
                    }
                }
                //region
                else if(cmd.matches("-r|--region")) {
                    String value = stripQuotes(scanner.findInLine(pattern));
                    if("".equals(value)) {
                        String msg = "Missing value for region";
                        throw new IllegalArgumentException(msg);
                    }
                    test.setRegion(value);
                }
                //HTTP status code
                else if(cmd.matches("-s|--status")) {
                    try {
                        String value = stripQuotes(scanner.findInLine(pattern));
                        Integer status = Integer.parseInt(value);
                        step.setStatus(status);
                    }
                    catch(NumberFormatException e) {
                        String msg = "Wrong HTTP status code format";
                        throw new IllegalArgumentException(msg, e);
                    }
                }
                //timeout
                else if(cmd.matches("-T|--timeout")) {
                    try {
                        String value = stripQuotes(scanner.findInLine(pattern));
                        Integer timeout = Integer.parseInt(value);
                        step.setTimeout(timeout);
                    }
                    catch(NumberFormatException e) {
                        String msg = "Timeout must be an integer";
                        throw new IllegalArgumentException(msg, e);
                    }
                }
                //user authentication
                else if(cmd.matches("-u|--user")) {
                    try {
                        String value = stripQuotes(scanner.findInLine(pattern));
                        String[] pair = value.split(":");
                        step.setUser(new BasicAuthentication(pair[0], pair[1]));
                    } 
                    catch (ArrayIndexOutOfBoundsException e) {
                        String msg = "Invalid user. Format: username:password";
                        throw new IllegalArgumentException(msg, e);
                    }
                }
                //request method: GET, POST, PUT
                else if(cmd.matches("-X|--request")) {
                    String value = stripQuotes(scanner.findInLine(pattern));
                    step.setRequest(value);
                }
                //pattern
                else if(cmd.matches("-p|--pattern")) {
                    //requires a rush object
                    Rush rush = (Rush) test;
                    String value = stripQuotes(scanner.findInLine(pattern));
                    String[] list = value.split(",");
                    //initialize if needed
                    if(rush.getPattern() == null) {
                        Collection<Interval> i = new ArrayList<Interval>();
                        rush.setPattern(new io.blitz.curl.config.Pattern(1, i));
                    }
                    //for each pattern given
                    for(String ptn : list) {
                        try {
                            //if doesn't match a valid pattern
                            if(!ptn.matches(RE_PATTERN_VALUE)) {
                                String msg = "Invalid ramp pattern";
                                throw new IllegalArgumentException(msg);
                            }
                            //gets the regex capture groups
                            Matcher matcher = Pattern.compile(RE_PATTERN_VALUE).matcher(ptn);
                            matcher.matches();
                            Integer start = Integer.parseInt(matcher.group(1));
                            Integer end = Integer.parseInt(matcher.group(2));
                            Integer duration = Integer.parseInt(matcher.group(3));
                            //creates interval
                            Interval interval = new Interval(1, start, end, duration);
                            rush.getPattern().getIntervals().add(interval);
                        }
                        catch(Exception e) {
                            String msg = "Invalid ramp pattern";
                            throw new IllegalArgumentException(msg, e);
                        }
                    }
                }
                //variables
                else if(cmd.matches(RE_VARIABLE)) {
                    String value = stripQuotes(scanner.findInLine(pattern));
                    //gets the variable name
                    Matcher matcher = Pattern.compile(RE_VARIABLE).matcher(cmd);
                    matcher.matches();
                    String vname = matcher.group(1);
                    //validated the name
                    if(!vname.matches("^[a-zA-Z][a-zA-Z0-9]*")) {
                        String msg = "Variable name must be alphanumeric: " + vname;
                        throw new IllegalArgumentException(msg);
                    }
                    //initializes if needed
                    if(step.getVariables() == null) {
                        step.setVariables(new HashMap<String, IVariable>());
                    }
                    try {
                        IVariable variable;
                        //if variable is a list
                        if(value.matches(RE_VAR_LIST)) {
                            Matcher m = Pattern.compile(RE_VAR_LIST).matcher(value);
                            m.matches();
                            String[] values = m.group(2).split(",");
                            List<String> list = Arrays.asList(values);
                            variable = new ListVariable(list);
                        }
                        //if is just a or alpha
                        else if(value.matches(RE_VAR_ALPHA_1)) {
                            variable = new AlphaVariable();
                        }
                        //if is alpha with min and max values
                        else if(value.matches(RE_VAR_ALPHA_2)) {
                            Matcher m = Pattern.compile(RE_VAR_ALPHA_2).matcher(value);
                            m.matches();
                            Integer min = Integer.parseInt(m.group(2));
                            Integer max = Integer.parseInt(m.group(3));
                            variable = new AlphaVariable(min, max);
                        }
                        //if is just n or number
                        else if(value.matches(RE_VAR_NUMBER_1)) {
                            variable = new NumberVariable();
                        }
                        //if is number with min and max values
                        else if(value.matches(RE_VAR_NUMBER_2)) {
                            Matcher m = Pattern.compile(RE_VAR_NUMBER_2).matcher(value);
                            m.matches();
                            Integer min = Integer.parseInt(m.group(2));
                            Integer max = Integer.parseInt(m.group(3));
                            variable = new NumberVariable(min, max);
                        }
                        //if is just u or uuid
                        else if(value.matches(RE_VAR_UDID)) {
                            variable = new UdidVariable();
                        }
                        //if not pattern was matched
                        else {
                            String msg = "Invalid variable args for " + vname 
                                    + ": " + value;
                            throw new IllegalArgumentException(msg);
                        }
                        // add variable to map
                        step.getVariables().put(vname, variable);
                    }
                    catch(Exception e) {
                        String msg = "Invalid variable args for " + vname 
                                + ": " + value;
                        throw new IllegalArgumentException(msg, e);
                    }
                }
                //ssl
                else if(cmd.matches("-1|--tlsv1")) {
                    step.setSsl("tlsv1");
                }
                //ssl
                else if(cmd.matches("-2|--sslv2")) {
                    step.setSsl("sslv2");
                }
                //ssl
                else if(cmd.matches("-3|--sslv3")) {
                    step.setSsl("sslv3");
                }
                //if the command is unknown
                else {
                    throw new IllegalArgumentException("Unknown option " + cmd);
                }
            }
            // no URL provided
            if(url == null) {
                throw new IllegalArgumentException("No URL specified");
            }
            try {
                step.setUrl(new URL(url));
                if(test.getSteps() == null) {
                    test.setSteps(new ArrayList<TestStep>());
                }
                test.getSteps().add(step);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Malformed URL", e);
            }
        }
        return test;
    }
    
    /**
     * Removed the quotes from around the string
     * @param quoted quoted string
     * @return string without quotes surrounding it
     */
    protected static String stripQuotes(String quoted) {
        if(quoted == null) {
            return "";
        }
        else if(quoted.startsWith("'") || quoted.startsWith("\"")) {
            return quoted.substring(1, quoted.length()-1);
        }
        return quoted;
    }
}
