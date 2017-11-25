package org.ctlv.proxmox.api;

public class Constants {
	
	public static String USER_NAME = "zli1";
	public static String PASS_WORD = "waHF=J";
	
	public static String HOST = "srv-px1.insa-toulouse.fr";
	public static String REALM = "Ldap-INSA";
	
	public static String SERVER1 = "srv-px7";	// team b13
	public static String SERVER2 = "srv-px8";	// team b13
	public static String CT_BASE_NAME = "ct-tpgei-ctlv-bB13-ct";  // exemple: ct-tpgei-ctlv-A23-ct 锟� concat闁歟r avec le num闁瀘 du CT
	public static long CT_BASE_ID = 21300;	 // 锟� modifier (cf. sujet de tp) from 21300 to 21399

	
	public static long GENERATION_WAIT_TIME = 10;
	public static String CT_TEMPLATE = "template:vztmpl/debian-8-turnkey-nodejs_14.2-1_amd64.tar.gz";
	public static String CT_PASSWORD = "tpuser";
	public static String CT_HDD = "vm:3";
	public static String CT_NETWORK = "name=eth0,bridge=vmbr1,ip=dhcp,tag=2028,type=veth";
	
	public static float CT_CREATION_RATIO_ON_SERVER1 = 0.67f;
	public static float CT_CREATION_RATIO_ON_SERVER2 = 0.33f;
	public static long RAM_SIZE[] = new long[]{256, 512, 768};
	
	public static long MONITOR_PERIOD = 10;
	public static float MIGRATION_THRESHOLD = 0.01f;   // to save resources, keep this constant small.
	public static float DROPPING_THRESHOLD = 0.015f;
	public static float MAX_THRESHOLD = 0.02f;  
			

}
