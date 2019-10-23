// Author: Maxwell Young
// Date: Feb 1, 2016


import java.nio.ByteBuffer;
public class packet implements
java.io.Serializable
{
    
    private int type;      // 0 if an ACK, 1 if a data packet
	private int seqnum;    // sequence number
	private int length;    // number of characters carried in data field 
	private String data;   // should be 0 for ACK packets  

	// constants

	private final int maxDataLength = 500;

	private final int SeqNumModulo = 32;
    
    // constructor
	public packet(int t, int s, int l, String d){
	    type = t;
	    seqnum = s;
	    length = l;
	    data = d;
	}

	//////////////////////// CONSTRUCTORS //////////////////////////////////////////

	

	// hidden constructor to prevent creation of invalid packets

	private packet(int Type, int SeqNum, String strData) throws Exception {

		// if data seqment larger than allowed, then throw exception

		if (strData.length() > maxDataLength)

			throw new Exception("data too large (max 500 chars)");

			

		type = Type;

		seqnum = SeqNum % SeqNumModulo;

		data = strData;

	}

	

	// special packet constructors to be used in place of hidden constructor

	public static packet createACK(int SeqNum) throws Exception {

		return new packet(0, SeqNum, new String());

	}

	

	public static packet createPacket(int SeqNum, String data) throws Exception {

		return new packet(1, SeqNum, data);

	}

	

	public static packet createEOT(int SeqNum) throws Exception {

		return new packet(2, SeqNum, new String());

	}

	
	
	public int getType(){
	    return type;
	}
	
	public int getSeqNum(){
	     return seqnum;   
	}
	
	public int getLength(){
	     return length;   
	}
	
	public String getData(){
	     return data;   
	}
	
	//////////////////////////// UDP HELPERS ///////////////////////////////////////

	

	public byte[] getUDPdata() {

		ByteBuffer buffer = ByteBuffer.allocate(512);

		buffer.putInt(type);

        buffer.putInt(seqnum);

        buffer.putInt(data.length());

        buffer.put(data.getBytes(),0,data.length());

		return buffer.array();

	}

	

	public static packet parseUDPdata(byte[] UDPdata) throws Exception {

		ByteBuffer buffer = ByteBuffer.wrap(UDPdata);

		int type = buffer.getInt();

		int seqnum = buffer.getInt();

		int length = buffer.getInt();

		byte data[] = new byte[length];

		buffer.get(data, 0, length);

		return new packet(type, seqnum, new String(data));

	}
	
    // print function for testing 
	public void printContents(){
	     System.out.println("type: " + type + "  seqnum: " + seqnum + " length: " + length);
	     System.out.println("data: " + data);
	     System.out.println();
	}
	
} // end of class
