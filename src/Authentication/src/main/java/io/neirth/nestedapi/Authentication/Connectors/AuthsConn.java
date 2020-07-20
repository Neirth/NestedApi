package io.neirth.nestedapi.Authentication.Connectors;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

public class AuthsConn implements Closeable {
    AuthsConn(Connection conn) {

    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }
}