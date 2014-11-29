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
	g_clock_value = Integer.parseInt(args[1]);  
	// configure and initialize the database
      SimpleDB.init(args[0]);
      // create a registry specific for the server on the default port
      Registry reg = LocateRegistry.createRegistry(1099);
      
      // and post the server entry in it
      RemoteDriver d = new RemoteDriverImpl();
      reg.rebind("simpledb", d);
      
      System.out.println("database server ready");
   }
}
