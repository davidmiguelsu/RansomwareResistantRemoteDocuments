package pt.tecnico.Server;

public class ServerMain {
	public static void main(String[] args) throws Exception {
		System.out.println(ServerMain.class.getSimpleName());

		// Print received arguments.
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort path host port%n", ServerMain.class.getName());
			return;
		}

		ServerController controller = new ServerController();
		controller.main(args);	//Used as a bridge between ClientServerServiceImpl and ServerServerServiceImpl + handles communications
	}

}
