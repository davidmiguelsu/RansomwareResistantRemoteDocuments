package pt.tecnico.CAServer;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class CAServerMain {
    /** ZooKeeper host name. */
    private static String zooHost;
    /** ZooKeeper host port. */
    private static int zooPort;
    /** ZooKeeper helper object. */
    private static ZKNaming zkNaming = null;

    /** ZooKeeper path where information about the server will be published. */
    private static String path;
    /** Server host name. */
    private static String host;
    /** Server host port. */
    private static int port;

    public static void main(String[] args) throws Exception {
        System.out.println(CAServerMain.class.getSimpleName());

        // Print received arguments.
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Check arguments.
        if (args.length < 6) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s zooHost zooPort path host port caPassword%n", CAServerMain.class.getName());
            return;
        }

        zooHost = args[0];
        zooPort = Integer.valueOf(args[1]);
        path = args[2];
        host = args[3];
        port = Integer.valueOf(args[4]);
        String password = args[5];

        CAServerServiceImpl caServerService = new CAServerServiceImpl();
        caServerService.SetupSystem(password.toCharArray());
        final BindableService impl = caServerService;

        // Create a new server to listen on port.
        Server serverMain = ServerBuilder.forPort(port).addService(impl).build();
        // Start the server.
        serverMain.start();
        // Server threads are running in the background.

        // Register on ZooKeeper.
        System.out.println("Contacting ZooKeeper at " + zooHost + ":" + zooPort + "...");
        zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));
        System.out.println("Binding " + path + " to " + host + ":" + port + "...");
        zkNaming.rebind(path, host, Integer.toString(port));
        // Use hook to register a thread to be called on shutdown.
        Runtime.getRuntime().addShutdownHook(new Unbind());

        System.out.println("Server started and awaiting requests on port " + port);


        // Do not exit the main thread. Wait until server is terminated.
        serverMain.awaitTermination();
    }

    /** 
     * Unbind class unbinds replica from ZKNaming after interruption.
     */
    static class Unbind extends Thread {
        public void run() {
            if (zkNaming != null) {
                try {
                    System.out.println("Unbinding " + path + " from ZooKeeper...");
                    zkNaming.unbind(path, host, String.valueOf(port));
                }
                catch (ZKNamingException e) {
                    System.err.println("Could not close connection with ZooKeeper: " + e);
                    return;
                }
            }
        }
    }
}
