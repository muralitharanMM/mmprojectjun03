package com.murali;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class TransferToVNMS {
	private static void jschMethodForFileTransfer(String srcPath,String destPath,String ipAddress,String username,String passwword) {
				
		JSch jsch = new JSch();
		Session session = null;
		boolean pingable = true;
		
		String[] binariesPath = {"snmpservice\\target\\snmp-6.3.0-SNAPSHOT.jar","faultservice\\target\\faultservice-6.3.0-SNAPSHOT.jar","commons\\target\\commons-6.3.0-SNAPSHOT.jar","webapp\\target\\webapp-6.3.0-SNAPSHOT.war"};
			
		
		try {
			InetAddress geek = InetAddress.getByName(ipAddress); 
		    	System.out.println("Sending Ping Request to " + ipAddress); 
		    if (geek.isReachable(5000)) 
		      pingable = true; 
		    else
		    	pingable = false;
		    if(pingable){
			for(int i=0;i<3;i++){	
				String actPath = srcPath+binariesPath[i];
				System.out.println("file to copy "+actPath);
				session = jsch.getSession(username, ipAddress);				
				session.setPassword(passwword);	
				java.util.Properties config = new java.util.Properties();
				
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);				
				session.connect();
				
				ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
				sftpChannel.connect();
				sftpChannel.put(actPath, destPath);
				System.out.println("Copied");
			   }			
			}
		    else {
		    	System.out.println("Unable to reach Destination !");
		    }				
			} catch (JSchException | SftpException e) {
				System.out.println("Unable to reach destination / exception while transfer "+e);
			} catch (UnknownHostException e) {
				System.out.println("unable to ping ip address");
			} catch (IOException e) {
				System.out.println("unable to ping ip address");
			}finally{
				if(session!=null && session.isConnected())
					session.disconnect();
			}
	}
	public static void main(String[] args) {
		
		String s = "'";
		System.out.println(s);
		//jschMethodForFileTransfer("D:\\myfork\\vnms\\","/home/vnms/BBinaries","172.31.140.165","vnms","vectastar");
	}
}
