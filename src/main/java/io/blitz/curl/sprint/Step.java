package io.blitz.curl.sprint;

/**
 * Use this to run a sprint against your app. The return values include the 
 * response time, the region from which the sprint was run along with the full 
 * request and response headers and the response body.
 * @author ghermeto
 */
public class Step {
    
    /**
     * The overall response time for the successful hit
     */
    private Double duration;
    
    /**
     * The time it took for the TCP connection
     */
    private Double connect;
    
    /**
     * The request object containing the URL, headers and content, if any
     */
    private Request request;
    
    /**
     * The response object containing the status code, headers and content, if any
     */
    private Response response;

    public Step(Double duration, Double connect, 
            Request request, Response response) {

        this.duration = duration;
        this.connect = connect;
        this.request = request;
        this.response = response;
    }

    public Double getConnect() {
        return connect;
    }

    public Double getDuration() {
        return duration;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }
}