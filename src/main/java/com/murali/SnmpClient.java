package com.murali;

/*
 * snmp.properties
 * 
 * 	protocol=udp
	port=161
	community = public

 * */

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import mm.murali.exception.FileNotFoundException;
import mm.murali.exception.SNMPRuntimeException;
import mm.murali.murali.logging.LoggerUtil;

public class SnmpClient {

	static private Logger logger = (Logger) LoggerFactory.getLogger(SnmpClient.class);

	private static String protocol;
	private String port;
	private static String community;

	private Snmp snmp = null;
	private String address = null;
	private TransportMapping transport = null;

	/**
	 * Constructor
	 * 
	 * @param add
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public SnmpClient(String add) throws FileNotFoundException {
		address = add;
		loadSNMPClientProperties();
	}

	public SnmpClient(String add, String port) throws FileNotFoundException {
		address = add;
		this.port = port;
		loadSNMPClientProperties();
	}

	/**
	 * Start the Snmp session. If you forget the listen() method you will not
	 * get any answers because the communication is asynchronous and the
	 * listen() method listens for answers.
	 * 
	 * @throws SNMPRuntimeException
	 * @throws IOException
	 */
	public void start() throws SNMPRuntimeException {
		
		if (transport == null) {
			try {
				transport = new DefaultUdpTransportMapping();
			} catch (IOException e) {
				String message = "Socket binding fails";
				LoggerUtil.error(logger, message, e);
				throw new SNMPRuntimeException(message, e);
			}
		} else {
			if (!transport.isListening()) {
				try {
					transport = new DefaultUdpTransportMapping();
				} catch (IOException e) {
					String message = "Socket binding fails";
					LoggerUtil.error(logger, message, e);
					throw new SNMPRuntimeException(message, e);
				}
			}
		}
		snmp = new Snmp(transport);
		try {
			transport.listen();
		} catch (IOException e) {
			String message = "Socket binding fails";
			LoggerUtil.error(logger, message, e);
			throw new SNMPRuntimeException(message, e);
		}
		logger.info("Exiting SnmpClient.start() ");
	}

	/**
	 * Closes the socket and stops the listener thread.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (transport != null && transport.isListening()) {
			transport.close();
			snmp.close();
		}
	}

	/**
	 * Method which takes a single OID and returns the response from the agent
	 * as a String.
	 * 
	 * @param oid
	 * @return
	 * @throws SNMPRuntimeException
	 * @throws IOException
	 */
	public String getAsString(OID oid) throws SNMPRuntimeException {
		
		ResponseEvent event = get(new OID[] { oid });

		if (event.getResponse() == null) {
			LoggerUtil.error(logger, "NO RESPONSE from " + address);

			return "0";
		}
		return event.getResponse().get(0).getVariable().toString();
	}

	/**
	 * Method which takes a single OID and returns the bulk response from the
	 * agent as a String.
	 * 
	 * @param oid
	 * @return
	 * @throws IOException
	 * @throws SNMPRuntimeException
	 */
	public PDU getBulk(OID oid, int maxRepetitions) throws IOException, SNMPRuntimeException {
		ResponseEvent event = get(new OID[] { oid }, maxRepetitions);

		if (event.getResponse() == null) {
			LoggerUtil.error(logger, "NO RESPONSE from " + address);
			// throw new RuntimeException("NO RESPONSE from " + address);
		}

		return event.getResponse();
	}

	/**
	 * This method is capable of handling multiple OIDs
	 * 
	 * @param oids
	 * @return
	 * @throws SNMPRuntimeException
	 * @throws IOException
	 */
	public ResponseEvent get(OID oids[]) throws SNMPRuntimeException {

		LoggerUtil.info(logger, "SnmpClient.get(" + oids + ") invoked");
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}

		pdu.setType(PDU.GET);

		ResponseEvent event = null;

		try {
			event = snmp.send(pdu, getTarget(), null);
		} catch (Exception e) {
			LoggerUtil.error(logger, "NO RESPONSE from " + address);
			/* throw new RuntimeException("NO RESPONSE from " + address); */

		}

		if (event != null) {
			return event;
		}
		LoggerUtil.error(logger, "GET timed out on " + address);

		// throw new RuntimeException("GET timed out on " + address);
		throw new SNMPRuntimeException("GET timed out on " + address);
	}

	/**
	 * 
	 * This method return bulk response from the agent
	 * 
	 * @param oids
	 * @param maxRepetitions
	 * @return
	 */
	private ResponseEvent get(OID[] oids, int maxRepetitions) throws SNMPRuntimeException {
		LoggerUtil.info(logger, "SnmpClient.get(" + oids + "," + maxRepetitions + ") invoked");
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}

		pdu.setMaxRepetitions(maxRepetitions);
		pdu.setType(PDU.GETBULK);

		ResponseEvent event = null;

		try {
			event = snmp.send(pdu, getTarget(), null);
		} catch (Exception e) {
			LoggerUtil.error(logger, "NO RESPONSE from " + address);
			// throw new RuntimeException("NO RESPONSE from " + address);
			throw new SNMPRuntimeException("NO RESPONSE from " + address);
		}

		if (event != null) {
			return event;
		}
		LoggerUtil.error(logger, "GET timed out on " + address);
		// throw new RuntimeException("GET timed out on " + address);
		throw new SNMPRuntimeException("GET timed out on " + address);
	}

	/**
	 * This method returns a Target, which contains information about where the
	 * data should be fetched and how.
	 * 
	 * @return
	 */
	private Target getTarget() {
		LoggerUtil.info(logger, "SnmpClient.getTarget() invoked");
		Address targetAddress = GenericAddress.parse(protocol + ":" + address + "/" + port);
		CommunityTarget target = new CommunityTarget();
		// target.setCommunity(new OctetString(community));
		target.setCommunity(new OctetString("public"));

		target.setAddress(targetAddress);
		target.setRetries(3);
		target.setTimeout(15000);
		target.setVersion(SnmpConstants.version2c);
		return target;
	}

	/**
	 * Loads the SNMP client properties
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 */
	private void loadSNMPClientProperties() throws FileNotFoundException {

		LoggerUtil.info(logger, "SnmpClient.loadSNMPClientProperties() invoked");
		String propFileName = "snmp.properties";

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

		Properties properties = new Properties();

		try {
			properties.load(inputStream);
		} catch (IOException e) {
			String message = propFileName + " File not found..";
			LoggerUtil.error(logger, message, e);
			throw new FileNotFoundException(message, e);
		}

		protocol = properties.getProperty("protocol");

		if (port == null) {
			port = properties.getProperty("port");
		}
		community = properties.getProperty("community");
		try {
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LoggerUtil.info(logger, "Closing Input Stream");
		}
		LoggerUtil.info(logger, "Exiting SnmpClient.loadSNMPClientProperties()");

	}
	
	/**
	 * Method which takes a single OID and returns the bulk response from the
	 * agent as a String.
	 * 
	 * @param oid
	 * @return
	 * @throws IOException
	 * @throws SNMPRuntimeException
	 */
	public Map<String, String> getBulkRT(OID oid) throws IOException, SNMPRuntimeException {

		LoggerUtil.info(logger, "SnmpClient.getBulk() invoked");
		 Map<String, String> result = getRT( oid );

		if (result == null ) {
			LoggerUtil.error(logger, "NO RESPONSE from " + address);
			// throw new RuntimeException("NO RESPONSE from " + address);
		}

		return result;
	}
	
	
	/**
	 * This method is capable of getting the tree of RT
	 * 
	 * @param oids
	 * @return
	 * @throws SNMPRuntimeException
	 * @throws IOException
	 */
	public  Map<String, String> getRT(OID oid) throws SNMPRuntimeException {

		LoggerUtil.info(logger, "SnmpClient.get(" + oid + ") invoked");
		
		  Map<String, String> result = new TreeMap();
		 TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
			
	     List<TreeEvent> events = treeUtils.getSubtree(getTarget(), new OID(oid));
	     if (events == null || events.size() == 0) {
	 		
	            System.out.println("Error: Unable to read table...");
	
	            return result;
	
	        }
	
	 
	
	        for (TreeEvent event : events) {
	
	            if (event == null) {
	
	                continue;
	
	            }
	
	            if (event.isError()) {
	
	                System.out.println("Error: table OID [" + oid + "] " + event.getErrorMessage());
	
	                continue;
	
	            }
	
	 
	
	            VariableBinding[] varBindings = event.getVariableBindings();
	
	            if (varBindings == null || varBindings.length == 0) {
	
	                continue;
	
	            }
	
	            for (VariableBinding varBinding : varBindings) {
	
	                if (varBinding == null) {
	
	                    continue;
	
	                }
	
	                 
	
	                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
	
	            }
	
	 
	
	        }
	        	return result;
	

	
	}
	
	/**
	 * This method is capable of handling multiple OIDs
	 * 
	 * @param oids
	 * @return
	 * @throws SNMPRuntimeException
	 * @throws IOException
	 */
	public ResponseEvent getBootVersion(OID oids[]) throws SNMPRuntimeException {

		LoggerUtil.info(logger, "SnmpClient.get(" + oids + ") invoked");
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}
		pdu.setType(PDU.GETNEXT);
		ResponseEvent event = null;

		try {
			event = snmp.send(pdu, getTarget(), null);
		} catch (Exception e) {
			LoggerUtil.error(logger, "NO RESPONSE from " + address);
			/* throw new RuntimeException("NO RESPONSE from " + address); */

		}

		if (event != null) {
			return event;
		}
		LoggerUtil.error(logger, "GET timed out on " + address);

		// throw new RuntimeException("GET timed out on " + address);
		throw new SNMPRuntimeException("GET timed out on " + address);
	}
	
	
	/**
	 * Method which takes a single OID and returns the response from the agent
	 * as a String.
	 * 
	 * @param oid
	 * @return
	 * @throws SNMPRuntimeException
	 * @throws IOException
	 */
	public String getNextString(OID oid) throws SNMPRuntimeException {
		LoggerUtil.info(logger, "SnmpClient.getAsString() invoked");
		ResponseEvent event = getBootVersion(new OID[] { oid });
		

		if (event.getResponse() == null) {
			LoggerUtil.error(logger, "NO RESPONSE from " + address);

			return "0";
			// throw new RuntimeException("NO RESPONSE from " + address);
			// throw new SNMPRuntimeException("NO RESPONSE from " + address);
		}

		return event.getResponse().get(0).getVariable().toString();
	}
	
	

	/**
	 * Method which takes a single OID and returns the response from the agent
	 * as a String.
	 * 
	 * @param oid
	 * @return
	 * @throws SNMPRuntimeException
	 * @throws IOException
	 */
	public Integer getAsInteger(OID oid) throws SNMPRuntimeException {
		
		ResponseEvent event = get(new OID[] { oid });

		if (event.getResponse() == null) { 
			LoggerUtil.error(logger, "NO RESPONSE from " + address);
			LoggerUtil.info(logger, "NO RESPONSE from " + address);
			return 0;
			
		}
		LoggerUtil.info(logger, "getAsInteger"+event.getResponse().get(0).getVariable().toInt());
		return event.getResponse().get(0).getVariable().toInt();
	}

}

