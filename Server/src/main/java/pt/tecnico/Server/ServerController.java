package pt.tecnico.Server;

import java.io.IOException;
import java.security.KeyStore;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.grpc.ClientServer;
import pt.tecnico.grpc.ClientToServerServiceGrpc;
import pt.tecnico.grpc.ServerServer;
import pt.tecnico.Common.CAServerCommandsImpl;
import pt.tecnico.Common.CryptographyImpl;
import pt.tecnico.Common.DatabaseImpl;
import pt.tecnico.grpc.ServerToServerServiceGrpc;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class ServerController {
	/** ZooKeeper host name. */
	private static String zooHost;
	/** ZooKeeper host port. */
	private static int zooPort;
	/** ZooKeeper helper object. */
	private static ZKNaming zkNaming = null;

	/** ZooKeeper path where information about the server will be published. */
	private static String path;
	private static String realPath;
	private static String serverName;
	/** Server host name. */
	private static String host;
	/** Server host port. */
	private static int portForClient;
	private static int portForServer;

	public boolean isLeader;
	public List<ChildServerInfo> childServerList = new ArrayList<ChildServerInfo>();
	public DatabaseImpl db;
	public Connection conn;

	public KeyStore ks;
	private String keyStorePath = System.getProperty("user.home") + "/Documents/SIRS_KeyStores/";
	private String keyStorePassword = "pwd";
	public CAServerCommandsImpl caServer = null;


    public void main(String[] args) throws ZKNamingException {

		zooHost = args[0];
		zooPort = Integer.valueOf(args[1]);
		path = args[2];
		realPath = args[2];
		host = args[3];
		portForClient = Integer.valueOf(args[4]);
		portForServer = portForClient + 1000;

        ClientServerServiceImpl clientServerImpl = new ClientServerServiceImpl();
		// clientServerImpl.SetupStoragePath();

		ServerServerServiceImpl serverServerImpl = new ServerServerServiceImpl();


		final BindableService impl = clientServerImpl;
		final BindableService betweenServerImpl = serverServerImpl;

        System.out.println("Contacting ZooKeeper at " + zooHost + ":" + zooPort + "...");
		zkNaming = new ZKNaming(zooHost, Integer.toString(zooPort));


		//TODO: Manage whenever a machine goes down
		System.out.println("Looking up " + path + "...");
		try {
			Collection<ZKRecord> records = zkNaming.listRecords(path);
			int numberOfServers = records.size();
			ArrayList<ZKRecord> recordList = new ArrayList<>(records);
			if(recordList.size() == 0) {
				//TODO: Change server name from /grpc/Ransom/1 to /grpc/Ransom/Leader
				realPath += "/1";
				serverName = "LeadServer";
				
				System.out.println("Binding " + realPath + " to " + host + ":" + portForClient + "...");
				zkNaming.rebind(realPath, host, Integer.toString(portForClient));
				isLeader = true;

				clientServerImpl.SetServerMain(this);
				serverServerImpl.SetServerMain(this);

			}
			else {
				String[] nameList = recordList.get(recordList.size() - 1).getPath().split("/");
				int lastToken = Integer.parseInt(nameList[nameList.length - 1]);
				// System.out.println(lastToken);
				System.out.println("NUMBER: " + numberOfServers);
				portForServer += lastToken;
				
				realPath += "/" + Integer.toString(lastToken + 1);
				serverName =  Integer.toString(lastToken + 1);

				System.out.println("Binding " + realPath + " to " + host + ":" + portForServer + "...");
				zkNaming.rebind(realPath, host, Integer.toString(portForServer));
				isLeader = false;

				clientServerImpl.SetServerMain(this);
			}
			
		} catch (ZKNamingException e) {
			e.printStackTrace();
			// System.out.println(e.printStackTrace(););
			realPath += "/1";
			serverName = "LeadServer";
			System.out.println("Binding " + realPath + " to " + host + ":" + portForClient + "...");
			zkNaming.rebind(realPath, host, Integer.toString(portForClient));
		}
		
		clientServerImpl.SetupStoragePath(realPath);
		Runtime.getRuntime().addShutdownHook(new Unbind());

		ks = CryptographyImpl.InitializeKeyStore(keyStorePassword.toCharArray(), keyStorePath + "standard_" + serverName + ".jceks");
		caServer = new CAServerCommandsImpl(zkNaming, ks);
		caServer.SetUser(serverName, "pwd");
		loadKeys();
        
		try {
            if (isLeader) {
                // Create a new server to listen on port. This will listen to client requests
                Server serverMain = ServerBuilder.forPort(portForClient).addService(impl).build();

                serverMain.start();

                Server betweenServer = ServerBuilder.forPort(portForServer).addService(impl).build();
                betweenServer.start();
                //We can use ZK watches here?
                System.out.println("Server started and awaiting requests from clients on port " + portForClient + ", and pings from servers on port " + portForServer);
               
				db = new DatabaseImpl();
				conn = db.connect(); 
				System.out.println("Database ON && conn updated");
				
                serverMain.awaitTermination();
                betweenServer.awaitTermination();
            }
            else {
                Server betweenServer = ServerBuilder.forPort(portForServer).addService(impl).build();
                betweenServer.start();

                notifyServerLeader(zkNaming);
                System.out.println("Server started and awaiting requests from Leader Server on port " + portForServer);

                betweenServer.awaitTermination();

            }
        } catch (IOException ioe) {
            System.out.println("Failed to bind server: " + ioe.getMessage());
			System.exit(0);
        } catch (InterruptedException ie) {
            System.out.println("Interruped: " + ie.getMessage());
			System.exit(0);
        }
	}


	public void addChildServerToList(String childPath) {
        String uri = "";
        try {
            uri = zkNaming.lookup(childPath).getURI();
        } catch (ZKNamingException zkne) {
			System.out.println("Failed to locate child server");
            return;
        }

		ManagedChannel channel = ManagedChannelBuilder.forTarget(uri).usePlaintext().build();
		ClientToServerServiceGrpc.ClientToServerServiceBlockingStub stub = ClientToServerServiceGrpc.newBlockingStub(channel);

		ChildServerInfo info = new ChildServerInfo(channel, stub);
		childServerList.add(info);

		System.out.println(childServerList);
	}

	static void notifyServerLeader(ZKNaming zk) {
		try {
			ArrayList<ZKRecord> recordList = new ArrayList<>(zk.listRecords(path));
			String uri = recordList.get(0).getURI();
            
			String[] arr = uri.split(":");
			String realURI = arr[0] + ":" + Integer.toString(Integer.parseInt(arr[1]) + 1000);

			ManagedChannel channel = ManagedChannelBuilder.forTarget(realURI).usePlaintext().build();
            ClientToServerServiceGrpc.ClientToServerServiceBlockingStub stub = ClientToServerServiceGrpc.newBlockingStub(channel);
            
			ClientServer.HelloRequest req = ClientServer.HelloRequest.newBuilder()
												.setName(realPath).build();
			ClientServer.HelloResponse res = stub.greeting(req);

			channel.shutdownNow();
		}
		catch (ZKNamingException zkne) {
			System.err.println("Unable to notify leader server");
		}
	}

	void loadKeys() {

		try {
			if(ks.size() == 0) {	//Brand new KeyStore
				caServer.requestKeyPair();
	
				CryptographyImpl.UpdateKeyStore(ks, keyStorePassword.toCharArray(), keyStorePath + "standard_" + serverName + ".jceks");
			}
		} catch (Exception e) {
			System.out.println("Failed to load new keys, will shutdown");
			System.exit(0);
		}
	}

	class ChildServerInfo {
		public ManagedChannel channel;
		public ClientToServerServiceGrpc.ClientToServerServiceBlockingStub stub;

		ChildServerInfo(ManagedChannel ch, ClientToServerServiceGrpc.ClientToServerServiceBlockingStub st) {
			channel = ch;
			stub = st;
		}
	}

    /** 
	 * Unbind class unbinds replica from ZKNaming after interruption.
    */
	class Unbind extends Thread {
		public void run() {
			if (zkNaming != null) {
				try {
					System.out.println("Unbinding " + realPath + " from ZooKeeper...");
					if(isLeader) {
						zkNaming.unbind(realPath, host, String.valueOf(portForClient));
					}
					else {
						zkNaming.unbind(realPath, host, String.valueOf(portForServer));
					}
				}
			   	catch (ZKNamingException e) {
					System.err.println("Could not close connection with ZooKeeper: " + e);
					return;
				}
			}
		}
	}
}
