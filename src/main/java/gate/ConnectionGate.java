package gate;

import settings.Settings;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionGate extends Thread {
    private ServerSocket gate;

    @Override
    public void run() {
        try {
            gate = new ServerSocket(Integer.parseInt(Settings.properties.getProperty("port")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(100);
        }

        while (true) {
            try {
                Socket newConnection = gate.accept();
                new Connection(newConnection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
