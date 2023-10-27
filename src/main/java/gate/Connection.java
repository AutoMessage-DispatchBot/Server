package gate;

import java.io.IOException;
import java.net.Socket;

public class Connection {
    public boolean isConnection;

    public final Input input;
    public final Output output;

    public boolean authorized;

    public Connection(Socket connection) throws IOException {
        isConnection = true;
        authorized = false;

        input = new Input(connection.getInputStream(), this);
        output = new Output(connection.getOutputStream(), this);

        input.start();
        output.start();
    }

}