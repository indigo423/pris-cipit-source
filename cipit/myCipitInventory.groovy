import org.opennms.pris.model.*
// Path is passed from requisition.properties variable
def csvFile = config.getString("cipitFile")
def separator = ","

def requisition = new Requisition()
requisition.setForeignSource(instance)

/**
 * Convert to an OpenNMS Requisition from Cisco IP Phone Inventory Tool
 * https://github.com/vloschiavo/cipit
 *
 * Fields in CSV file:
 *
 *  fields[0] // Phone Record Number
 *  fields[1] // IP Address
 *  fields[2] // Model Number
 *  fields[3] // MAC Address
 *  fields[4] // Host Name
 *  fields[5] // Phone DN
 *  fields[6] // Phone Load Version
 *  fields[7] // Phone Serial Number
 *  fields[8] // XML Capable
 *  fields[9] // CDP Switch Host Name
 *  fields[10] // CDP Switch IPv4 Address
 *  fields[11] // CDP Switch IPv6 Address
 *  fields[12] // CDP Switch Port
 *  fields[13] // LLDP Switch Hostname
 *  fields[14] // LLDP Switch IPv4 Address
 *  fields[15] // LLDP Switch IPv6 Address
 *  fields[16] // LLDP Switch Port
 *  fields[17] // Port Speed
 *  fields[18] // Port Duplex
 *  fields[19] // Port Information
 */

def file = new File(csvFile)

file.splitEachLine(separator) {fields ->
    def requisitionNode = new RequisitionNode()

    def interfaceList = new ArrayList<RequisitionInterface>()
    def requisitionInterface = new RequisitionInterface()
    def monitoredServiceList = new ArrayList<RequisitionMonitoredService>()

    def assets = new ArrayList<RequisitionAsset>()
    def assetModelNumber = new RequisitionAsset()
    def serialNumber = new RequisitionAsset()

    requisitionNode.setNodeLabel(fields[4]) // Host Name

    requisitionInterface.setIpAddr(fields[1]) // Phone IP Address
    requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY)

    // Optional: Hard code a service ICMP to the IP interface
    monitoredServiceList.add(new RequisitionMonitoredService(null,"ICMP"))
    requisitionInterface.getMonitoredServices().addAll(monitoredServiceList)

    // Add this interface to the list
    interfaceList.add(requisitionInterface)

    // Add interface list to node
    requisitionNode.getInterfaces().addAll(interfaceList)

    // Phone Model Number
    assetModelNumber.setName("modelNumber")
    assetModelNumber.setValue(fields[2])

    // Phone Serial Number
    serialNumber.setName("serialNumber")
    serialNumber.setValue(fields[7])

    // Add assets to to list
    assets.add(assetModelNumber)
    assets.add(serialNumber)

    // Assign all assets to the node
    requisitionNode.getAssets().addAll(assets)

    // Add Node to the requisition
    requisition.getNodes().add(requisitionNode)
}

// remove the node which has the information from the CSV header
requisition.getNodes().remove(0)

return requisition
