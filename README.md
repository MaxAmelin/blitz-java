### ![blitz.io](http://blitz.io/images/logo2.png)

### Make load and performance a fun sport.

* Run a sprint from around the world
* Rush your API and website to scale it out
* Condition your site around the clock

## Getting started

Login to [blitz.io](http://blitz.io) and in the blitz bar type:
    
    --api-key

On your **pom.xml**

    <dependency>
        <groupId>io.blitz</groupId>
        <artifactId>blitz-api-client</artifactId>
        <version>0.2.1</version>
    </dependency>

Then

**Sprint**

```javascript
Sprint s = new Sprint("your@account.com", "aqbcdge-sjfkgurti-sjdhgft-skdiues");
Collection<TestStep> steps = new ArrayList<TestStep>();
steps.add(new TestStep(new URL("http://your.cool.app")));
s.setSteps(steps);
s.addListener(new ISprintListener() {
    public boolean onStatus(SprintResult result) {
        System.err.print(".");
    }
    public void onComplete(SprintResult result) {
        System.err.println("SUCCESS!");
    }
});
s.execute();
```

OR

```javascript
Sprint s = (Sprint) io.blitz.command.Curl.parse(
    "your@account.com", 
    "aqbcdge-sjfkgurti-sjdhgft-skdiues",
    "-r japan http://your.cool.app"
);
s.addListener(new ISprintListener() {
    public boolean onStatus(SprintResult result) {
        System.err.print(".");
    }
    public void onComplete(SprintResult result) {
        System.err.println("SUCCESS!");
    }
});
s.execute();
```

**Rush**

```javascript
Rush r = new Rush("your@account.com", "aqbcdge-sjfkgurti-sjdhgft-skdiues");
Collection<TestStep> steps = new ArrayList<TestStep>();
steps.add(new TestStep(new URL("http://your.cool.app")));
r.setSteps(steps);
Collection<Interval> intervals = new ArrayList<Interval>();
intervals.add(new Interval(1, 10, 10));
r.setPattern(new Pattern(intervals));
r.addListener(new IRushListener() {
    public boolean onStatus(RushResult result) {
        System.err.print(".");
    }
    public void onComplete(RushResult result) {
        System.err.println("SUCCESS!");
    }
});
r.execute();
```

OR

```javascript
Rush r =  (Rush) io.blitz.command.Curl.parse(
    "your@account.com", 
    "aqbcdge-sjfkgurti-sjdhgft-skdiues",
    "-p 10-50:60 -r california http://your.cool.app"
);
r.addListener(new IRushListener() {
    public boolean onStatus(RushResult result) {
        System.err.print(".");
    }
    public void onComplete(RushResult result) {
        System.err.println("SUCCESS!");
    }
});
r.execute();
```

## Maven

The blitz api client is available on Maven Central:

    http://repo1.maven.org/maven2/io/blitz/

## Dependencies

If you are not using maven, you must download the dependencies jar archives and 
add them to your classpath.

### Runtime

Needed to use the API client.

* Google Gson v1.7.1 [Homepage](http://code.google.com/p/google-gson/)
* Apache Commons Codec v1.5 [Homepage](http://commons.apache.org/codec/)

### Testing

Needed to run the unit tests.

* JUnit v4.8.2 [Homepage](http://www.junit.org/)
* Mockito v1.8.5 [Homepage](http://mockito.org/)
