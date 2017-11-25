package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Monitor implements Runnable {

	Analyzer analyzer;
	ProxmoxAPI api;
	Map<String, List<LXC>> myCTsPerServer;
	
	public Monitor(ProxmoxAPI api, Analyzer analyzer,Map<String, List<LXC>> myCTsPerServer) {
		this.api = api;
		this.analyzer = analyzer;
		this.myCTsPerServer = myCTsPerServer;
	}
	

	@Override
	public void run() {
		
		while(true) {
			
			// R�cup�rer les donn�es sur les serveurs
			// ...
			
			// 
			
			System.out.println("Updating List on Server1.");
			List<LXC> myCTsPerServer1_cp = new ArrayList<LXC>(myCTsPerServer.get(Constants.SERVER1)); //copy the old one
			List<LXC> myCTsPerServer2_cp = new ArrayList<LXC>(myCTsPerServer.get(Constants.SERVER2));
			// clear all and then we update it with concurrent information
			myCTsPerServer.clear();
			myCTsPerServer.put(Constants.SERVER1, new ArrayList<>());
			myCTsPerServer.put(Constants.SERVER2, new ArrayList<>());
			//re-fill the hashmap
			for(LXC ct: myCTsPerServer1_cp) {
				String ctId_loop = ct.getVmid();
				try {
					myCTsPerServer.get(Constants.SERVER1).add(api.getCT(Constants.SERVER1,ctId_loop));
				} catch (LoginException | JSONException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("CT Info updated: "+myCTsPerServer.get(Constants.SERVER1).get(0).getName()+" On Server1");
			}
			for(LXC ct: myCTsPerServer2_cp) {
				String ctId_loop = ct.getVmid();
				try {
					myCTsPerServer.get(Constants.SERVER2).add(api.getCT(Constants.SERVER2,ctId_loop));
				} catch (LoginException | JSONException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("CT Info updated: "+myCTsPerServer.get(Constants.SERVER2)+" On Server2");
			}
			
			
			// Lancer l'anlyse
			// ...
			try {
				this.analyzer.analyze(myCTsPerServer);
			} catch (LoginException | JSONException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// attendre une certaine p�riode
			try {
				Thread.sleep(Constants.MONITOR_PERIOD * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
