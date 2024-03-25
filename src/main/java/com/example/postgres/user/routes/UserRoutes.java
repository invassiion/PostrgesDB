package com.example.postgres.user.routes;

public class UserRoutes {
    private final static String ROOT = "/api/v1/user";

    public final static String CREATE = ROOT;

    public static final String BY_ID = ROOT + "/{id}";
    public static final String SEARCH = ROOT;

}
