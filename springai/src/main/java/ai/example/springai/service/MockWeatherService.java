package ai.example.springai.service;


import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import java.util.function.Function;

public class MockWeatherService implements Function<Request, Response> {

    @Override
    public Response apply(Request request) {
        return null;
    }

    public enum Unit { C, F }

    public record Request1(String location, Unit unit) {}

    public record Response1(double temp, Unit unit) {}

}