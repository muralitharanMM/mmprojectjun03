package com.murali;

import java.util.ArrayList;
import java.util.Date;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;



/**
 * @author mchopker
 * 
 */
public class SNMPTrapGeneratorClient {

	
	//VBS[1.3.6.1.2.1.1.3.0 = 0:22:27.28; 1.3.6.1.6.3.1.1.4.1.0 = 1.3.6.1.4.1.29601.2.217.8.0.2; 1.3.6.1.2.1.196.1.5.1 = 28:76:10:19:99:31; 1.3.6.1.4.1.29601.2.217.7.1.2 = 1]]
	
	//VBS[requestID=1704051389, errorStatus=Success(0), errorIndex=0, VBS[1.3.6.1.2.1.1.3.0 = 0:15:00.52; 1.3.6.1.6.3.1.1.4.1.0 = 1.3.6.1.4.1.29601.2.82.5.0.1; 1.3.6.1.2.1.196.1.5.1 = 28:76:10:0c:3d:02;
	//1.3.6.1.2.1.196.1.2.2.1.7 = 7]
			
	
	/*Received PDU...TRAP[requestID=184640147, errorStatus=Success(0), errorIndex=0, 
	 *VBS[1.3.6.1.2.1.1.3.0 = 0:24:07.17; 
	 *1.3.6.1.6.3.1.1.4.1.0 = 1.3.6.1.4.1.29601.2.83.9.0.3;
	 * 1.3.6.1.2.1.2.2.1.1 = 0; 
	 * 1.3.6.1.4.1.29601.2.83.8.1.3 = 28:76:10:19:92:e1;
	 *  1.3.6.1.4.1.29601.2.83.3.15.1.6 = 1]]*/
			
	private static final String community = "public";
	
	private static final String trapOid = ".1.3.6.1.4.1.29601.2.83.9.0.3";
											   	
	private static final String ipAddress = "172.31.140.188";
	
	private static final int port = 161;

	public static void main(String args[]) {
		sendV1orV2Trap(SnmpConstants.version2c, community, ipAddress, port);
		
	}
	
	
	private static PDU createPdu(Target tg ,int snmpVersion) {
		PDU pdu = DefaultPDUFactory.createPDU(tg, snmpVersion);
		if (snmpVersion == SnmpConstants.version1) {
			pdu.setType(PDU.V1TRAP);
		} else {
			pdu.setType(PDU.TRAP);
		}		
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime));
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(trapOid)));
		//pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,new IpAddress(ipAddress)));
		//pdu.add(new VariableBinding(new OID(trapOid), new OctetString("sdsds")));		
		//pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.29601.2.83.8.1.3"), new OctetString("11:22:11:22:11:22")));
		pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.2.2.1.1"),new OctetString("0")));
		pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.29601.2.83.8.1.3"),new OctetString("28:76:10:19:92:e1")));
		pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.29601.2.83.3.15.1.6"),new OctetString("1")));
		
		return pdu;
	}
	

	private static void sendV1orV2Trap(int snmpVersion, String community,
			String ipAddress, int port) {
		try {			
			// Create Transport Mapping
			TransportMapping transport = new DefaultUdpTransportMapping();
			transport.listen();

			// Create Target
			CommunityTarget comtarget = new CommunityTarget();
			comtarget.setCommunity(new OctetString(community));
			comtarget.setVersion(snmpVersion);
			comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
			comtarget.setRetries(2);
			comtarget.setTimeout(5000);

			// Send the PDU
			Snmp snmp = new Snmp(transport);
			// create v1/v2 PDU
			PDU snmpPDU = createPdu((Target)comtarget,snmpVersion);
			snmp.send(snmpPDU, comtarget);
			System.out.println("Sent Trap to (IP:Port)=> " + ipAddress + ":"+ port);
			snmp.close();
		} catch (Exception e) {
			System.err.println("Error in Sending Trap to (IP:Port)=> "
					+ ipAddress + ":" + port);
			System.err.println("Exception Message = " + e.getMessage());
		}
	}

}
