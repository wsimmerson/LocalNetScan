import java.io.IOException;
import java.net.*;
import java.util.*;


public class LocalNetScan {

	/**
	 * @param args
	 * @throws SocketException 
	 */
	public static void main(String[] args) throws SocketException {
			System.setProperty("java.net.preferIPv4Stack", "true");
		
			System.out.println("\nLocalNetScan v1.0\nEvaluating Network... \n\n");
			
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			
			for (NetworkInterface netint : Collections.list(nets)){
				showNetInfo(netint);
			}
			
			System.out.println("\nPress Enter to exit");
			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private static String long2ip(long l) {
		// Use Bitwise functions to shift and mask
		return  ((	l >> 24 )	 & 0xFF) + "." +
				((	l >> 16 )	 & 0xFF) + "." +
				((	l >> 8 )	 & 0xFF) + "." +
				(	l			 & 0xFF);
	}
	
	private static long ip2long(String addr) {
		String[] addrArray = addr.split("\\.");
		
		long num = 0;
		
		for (int i = 0; i < addrArray.length; i++){
			int power = 3 - i;
			num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256,power)));
		}
		return num;
	}
	
	private static String[] cidrToRange(String addr, int cidr){
		String[] Range 	= new String[2];
		Range[0] 		= (long2ip((ip2long(addr)) & ((-1 << (32 - cidr)))));
		Range[1] 		= (long2ip((long) ((long)(ip2long(addr)) + Math.pow(2, (32 - cidr)) -1)));
		
		return Range;
	}
	
	private static int getNetworkSize(int cidr) {
		return (int) (Math.pow(2, (32 - cidr)) -2);
	}
	
	private static void showNetInfo(NetworkInterface netint) throws SocketException {
		
		String 	 ipAddr = null;
		String[] range  = new String[2];
		int		 cidr	= 0;
		
		System.out.printf("Display name: %s%n", netint.getDisplayName());
		System.out.printf("Name: %s%n", netint.getName());
		
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)){
			System.out.printf("InetAddress: %s%n", inetAddress);
		}
		
		System.out.printf("Parent: %s%n", netint.getParent());
		System.out.printf("Up? %s%n", netint.isUp());
		System.out.printf("Loopback?  %s%n", netint.isLoopback());
		System.out.printf("PointToPoint? %s%n", netint.isPointToPoint());
		System.out.printf("Supports multicast? %s%n", netint.isVirtual());
		System.out.printf("Virtual? %s%n", netint.isVirtual());
		System.out.printf("Hardware Address: %s%n", Arrays.toString(netint.getHardwareAddress()));
		System.out.printf("MTU %s%n", netint.getMTU());
		
		List<InterfaceAddress> interfaceAddresses = netint.getInterfaceAddresses();

		for (InterfaceAddress addr : interfaceAddresses) {
			
			System.out.printf("InterfaceAddress: %s%n", addr.getAddress());
			ipAddr = addr.getAddress().toString();
			
			// Remove leading /
			if (ipAddr.startsWith("/")){
				ipAddr = ipAddr.substring(1, ipAddr.length());
			}
			
			System.out.printf("Broadcast: %s%n", addr.getBroadcast());
			System.out.printf("Network Prefix: %s%n", addr.getNetworkPrefixLength());
			cidr = addr.getNetworkPrefixLength();
			System.out.printf("Subnet Mask: %s%n", long2ip(-1 <<(32 -cidr)));
			
			range = cidrToRange(ipAddr, cidr);
			if (range[0].endsWith(".0")){
				// make sure the gateway doesn't start with 0
				range[0] = long2ip(ip2long(range[0]) +1);
			}
			System.out.printf("Gateway: %s%n", range[0]);
			System.out.printf("Network Size: %s IPs%n", getNetworkSize(cidr));
			
			try {
				if (!range[0].startsWith("127.")){
					getIPList(range[0], cidr);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.printf("%n");
		
		Enumeration<NetworkInterface> subInterfaces = netint.getSubInterfaces();
		for (NetworkInterface networkInterface : Collections.list(subInterfaces)) {
			System.out.printf("%nSubInterface%n");
			showNetInfo(networkInterface);
		}
		System.out.printf("%n");
	}
	
	private static void getIPList(String addr, int cidr) throws IOException {
		String[] range 		= cidrToRange(addr, cidr);
		
		long firstIP 		= ip2long(range[0]);
		long lastIP  		= ip2long(range[1]);
		String realIP 		= null;
		InetAddress address = null;
		
		while (firstIP <= lastIP) {
			realIP = long2ip(firstIP);
			address = InetAddress.getByName(realIP);
			
			if (!realIP.endsWith(".0")){
				System.out.printf("%s is Active? %s%n", realIP, address.isReachable(1000));
			}
			firstIP++;
		}
	}
}
