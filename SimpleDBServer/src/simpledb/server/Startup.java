package simpledb.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import simpledb.remote.RemoteDriver;
import simpledb.remote.RemoteDriverImpl;

public class Startup {

	private static int g_clock_value;

	public static int Get_clock_value() {
		return g_clock_value;
	}

	public static void main(String args[]) throws Exception {
		try {
			if(args != null && args.length == 2) {
				g_clock_value = Integer.parseInt(args[1]);
				if(g_clock_value <= 0) {
					System.out.println("Setting default gclock value to 5 since specified value is negative");
					g_clock_value = 5; 
				}
			} else {
				System.out.println("Setting default gclock value to 5");
				g_clock_value = 5; 
			}
		} catch(Exception e) {
			System.out.println("Setting default gclock value to 5");
			g_clock_value = 5;
		}
		// configure and initialize the database
		if(args != null && args.length > 0) {
			SimpleDB.init(args[0]);
		} else {
			System.out.println("Default database name is set to SimpleDB");
			SimpleDB.init("SimpleDB");
		}
		// create a registry specific for the server on the default port
		Registry reg = LocateRegistry.createRegistry(1099);

		// and post the server entry in it
		RemoteDriver d = new RemoteDriverImpl();
		reg.rebind("simpledb", d);

		System.out.println("database server ready");
	}
}
