package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Analyzer {
	ProxmoxAPI api;
	Controller controller;
	
	public Analyzer(ProxmoxAPI api, Controller controller) {
		this.api = api;
		this.controller = controller;
	}
	
	public void analyze(Map<String, List<LXC>> myCTsPerServer) throws LoginException, JSONException, IOException  {

		// Calculer la quantit锟� de RAM utilis锟絜 par mes CTs sur chaque serveur
		// ...
		long memOnServer1 = 0;
		if(myCTsPerServer.get(Constants.SERVER1).size()!=0) {
			for(LXC ctSrv1 : myCTsPerServer.get(Constants.SERVER1)) {
				memOnServer1 += ctSrv1.getMem();
			}
		}
		System.out.println("My cluster of CTs on Server 1 Consumes "+ memOnServer1 +" RAM.");
		
		// RAM used on Server2
		long memOnServer2 = 0;
		if(myCTsPerServer.get(Constants.SERVER2).size()!=0) {
			for(LXC ctSrv2 : myCTsPerServer.get(Constants.SERVER2)) {
				memOnServer2 += ctSrv2.getMem();
				}
		}
		System.out.println("My cluster of CTs on Server 2 Consumes "+ memOnServer2 +" RAM.");
		
		// M锟絤oire autoris锟絜 sur chaque serveur
		// ...
		// for SERVER1 & SERVER2, the threshold is the same.
		long memMigrate;
		long memStop;
		memMigrate = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MIGRATION_THRESHOLD);
		memStop = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.DROPPING_THRESHOLD);
		
		
		// Analyze et Actions
		// Do corresponding operation.
		if( memOnServer1 >= memMigrate){
			this.controller.migrateFromTo(Constants.SERVER1, Constants.SERVER2);
		}
		if( memOnServer2 >= memMigrate){
			this.controller.migrateFromTo(Constants.SERVER2, Constants.SERVER1);
		}
		if( memOnServer1 >= memStop){
			this.controller.offLoad(Constants.SERVER1);
		}
		if( memOnServer1 >= memStop){
			this.controller.offLoad(Constants.SERVER2);
		}
		
	}

}
