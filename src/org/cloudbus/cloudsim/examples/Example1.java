package org.cloudbus.cloudsim.examples;

import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * A simple example showing how to create a data center with one host and run
 * one cloudlet on it.
 */
public class Example1 {
	
	private static final String filePath = "/home/ngtrieuvi92/zz/cloudsim/src/org/cloudbus/cloudsim/examples/testcase.json";
	
	private static List<DatacenterBroker> brokersList;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args
	 *            the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample1...");
		brokersList = new ArrayList<DatacenterBroker>();

		try {
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance(); // Calendar whose fields
														// have been initialized
														// with the current date
														// and time.
			boolean trace_flag = false; // trace events

			CloudSim.init(num_user, calendar, trace_flag);
			
			
			// Read data from json file
			FileReader reader = new FileReader(filePath);

            JSONParser jsonParser = new JSONParser();
            JSONArray members = (JSONArray) jsonParser.parse(reader);
            
            // Create Datacenterbrokers
            for (int i = 0; i < members.size(); i++) {
            	JSONObject member = (JSONObject) members.get(i);
            	
            	String m_name = (String) member.get("name");
            	Log.printLine(m_name);
            	DatacenterBroker broker = createBroker(m_name);
            	brokersList.add(broker);
            	
            	List<Vm> vmList = new ArrayList<Vm>();
            	List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
            	
            	// Cretae datacenters
            	JSONArray m_datacenters = (JSONArray) member.get("datacenters");
            	for (int j = 0; j < m_datacenters.size(); j++) {
            		JSONObject m_datacenter = (JSONObject) m_datacenters.get(j);
            		Datacenter datacenter = createDatacenter(m_datacenter, broker);
            		
            		// Create vms
                	JSONArray m_vms = (JSONArray) m_datacenter.get("vms");
                	if (m_vms != null) {
    	            	for (int k = 0; k < m_vms.size(); k++) {
    	            		JSONObject m_vm = (JSONObject) m_vms.get(k);
    	            		Vm vm = createVirtualMachine(vmList.size(), m_vm, broker);
    	            		vmList.add(vm);
    	            	}
                	}
            	}
            	broker.submitVmList(vmList);
            	
            	JSONArray m_cloudlets = (JSONArray) member.get("cloudlets");
            	if (m_cloudlets == null) {
            		Log.printLine(broker.getName() + ": There is no cloudlet");
            		continue;
            	}
            	
            	int totalVms = vmList.size();
            	if (totalVms <= 0) {
            		Log.printLine(broker.getName() + ": There is no vm => can not submit cloudlet.");
            		continue;
            	}
            	
            	for (int j = 0; j < m_cloudlets.size(); j++) {
            		int selectedVm = j % totalVms;
            		JSONObject m_cloudlet = (JSONObject) m_cloudlets.get(j);
            		long length = (Long) m_cloudlet.get("long");
            		long fileSize = (Long) m_cloudlet.get("fileSize");
            		long outputSize = (Long) m_cloudlet.get("outputSize");
            		int pesNumber = ((Long) m_cloudlet.get("pesNumber")).intValue();
            		
            		UtilizationModel utilizationModel = new UtilizationModelFull();
            		
            		Cloudlet cloudlet = new Cloudlet(j, length, pesNumber, fileSize, outputSize, 
            				utilizationModel, utilizationModel, utilizationModel);
            		
            		cloudlet.setUserId(broker.getId());
            		cloudlet.setVmId(selectedVm);
            		
            		cloudletList.add(cloudlet);
            	}

            	broker.submitCloudletList(cloudletList);
            }
            
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
//			List<Cloudlet> newList = hcmutBroker.getCloudletReceivedList();
//			printCloudletList(newList);
			
//			List<Cloudlet> newList1 = hcmusBroker.getCloudletReceivedList();
//			printCloudletList(newList1);
			
			for (DatacenterBroker broker : brokersList) {
				List<Cloudlet> newList = broker.getCloudletReceivedList();
				printCloudletList(newList);
			}
				
		

			Log.printLine("CloudSimExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	
	private static Vm createVirtualMachine(int vmId, JSONObject v_info, DatacenterBroker broker) {
		int mips = ((Long) v_info.get("mips")).intValue();
		long size = (Long) v_info.get("size"); // image size (MB)
		int ram = ((Long) v_info.get("ram")).intValue(); // vm memory (MB)
		long bw = (Long) v_info.get("bw");
		int pesNumber = ((Long) v_info.get("pesNumber")).intValue(); // number of cpus
		String vmm = "Xen"; // VMM name

		// create VM
		Vm vm = new Vm(vmId, broker.getId(), mips, pesNumber, ram, bw, size, vmm,
				new CloudletSchedulerSpaceShared());
		
		return vm;
	}
	
	private static Datacenter createDatacenter(JSONObject d_info, DatacenterBroker broker) {
		String name = broker.getName() + "---" + (String) d_info.get("name");
		Log.printLine(name);
		
		List<Host> hostList = new ArrayList<Host>();
		
		JSONArray hosts = (JSONArray) d_info.get("hosts");
		for (int i = 0; i < hosts.size(); i++) {
			JSONObject host = (JSONObject) hosts.get(i);
			
			int ram = ((Long) host.get("ram")).intValue();
			long storage = (Long) host.get("storage");
			int bw = ((Long) host.get("bw")).intValue();
			
			JSONArray pes = (JSONArray) host.get("pes");
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < pes.size(); j++) {
				long mips = (Long) pes.get(j);
				peList.add(new Pe(j, new PeProvisionerSimple(mips)));
			}
			
			hostList.add(new Host(i, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), 
					storage , peList, new VmSchedulerTimeShared(peList)));
		}
		
		String arch = (String) d_info.get("arch");
		String os = (String) d_info.get("os");
		String vmm = (String) d_info.get("vmm");
		double time_zone = (double) d_info.get("time_zone");
		double cost = (double) d_info.get("cost");
		double costPerMem = (double) d_info.get("costPerMem");
		double costPerStorage = (double) d_info.get("costPerStorage");
		double costPerBw = (double) d_info.get("costPerBw");
		
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
			
			broker.addDatacenter(datacenter.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name
	 *            the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name, DatacenterBroker broker) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store
																// Pe id and
																// MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		hostList.add(new Host(hostId, new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw), storage, peList,
				new VmSchedulerTimeShared(peList))); // This is our machine

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
																		// not
																		// adding
																		// SAN
																		// devices
																		// by
																		// now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
			
			broker.addDatacenter(datacenter.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker(String name) {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list
	 *            list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}
}