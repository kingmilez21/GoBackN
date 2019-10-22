import java.io.*; 

import java.net.*; 

import java.nio.file.Files;

import java.nio.file.Paths;

import com.ibm.rmi.util.buffer.ByteBuffer;

import java.nio.file.Path;



class client {

	//init

	static String host = null;

	static String udp_acks = null;

	static String udp_datas = null;



	static int udp_ack = 0;

	static int udp_data = 0;

	private final int maxDataLength = 500;

	private final int SeqNumModulo = 32;

	static String filename = null;



	static int totalpacs = 0;



	static int acked = 0;

	public byte[] getUDPdata() {

		ByteBuffer buffer = ByteBuffer.allocate(512);

		buffer.putInt(packet.type);

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

	//check the string whether include letter

	public static boolean isInteger( String input ){//function to check whether a string is all numbers

	    try{

	      Integer.parseInt(input);

	      return true;

	    }

	    catch(Exception e){

	      return false;

	    }

	}

	//translate the byte array to packet array

	public static packet[] transToPacket(byte[] content) throws Exception {

		totalpacs = (int)Math.ceil((double)content.length / 500.0);//total packets

		int rest = 0;

		rest = content.length % 500;//not the multiple of 500

		packet packets[] = new packet[totalpacs];//create packets array

		for (int i = 0; i < totalpacs ; ++i) {

			byte change[];

			if(i == totalpacs - 1 && rest != 0){//if last packet and not the multiple of 500

				change = new byte[rest];

				System.arraycopy(content, i * 500, change, 0, rest);

			}

			else{

				change = new byte[500];

				System.arraycopy(content, i * 500, change, 0, 500);

			}

			packets[i] = packet.createPacket(i, new String(change));

		}

		return packets;//retuen array of packets

	}



	//send packet to reveiver with

	//begin: index that packet we start to send

	//end: index that when we stop

	//allpackets: total packets

	public static void sendPac(int begin, int end, packet allpackets[], PrintWriter seqnum) throws Exception{

		InetAddress IPAddress = InetAddress.getByName(host);

		for (int i = begin; i < end ; ++i) {//send the packet one by one

			byte[] sendData = allpackets[i].getUDPdata();

			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, udp_data);

			DatagramSocket clientSocketUDP = new DatagramSocket();

			clientSocketUDP.send(sendPacket);

			seqnum.println(allpackets[i].getSeqNum());//if send write the seqnum in the log

			clientSocketUDP.close();

		}

	}



	//reveive the ack number from reveiver

	public static int receiveAck(PrintWriter acklog) throws Exception{

		DatagramSocket serverSocket = new DatagramSocket(udp_ack);

		serverSocket.setSoTimeout(100);//timer set as 100ms

		byte[] receiveData = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		try{

			//try to get ack from reveiver

			serverSocket.receive(receivePacket);

		}

		catch(Exception e){//timeout

			serverSocket.close();

			return -1;

		}

		//if no timeout

		//get the ack number and return it

		packet tempac = packet.parseUDPdata(receiveData);

		acklog.println(tempac.getSeqNum());

		serverSocket.close();

		return tempac.getSeqNum();

	}

	

	//help to send the EOT packet

	public static void sendEOT() throws Exception{

		packet temeot = packet.createEOT(totalpacs);

		InetAddress IPAddress = InetAddress.getByName(host);

		byte[] sendData = temeot.getUDPdata();

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, udp_data);

		DatagramSocket clientSocketUDP = new DatagramSocket();

		clientSocketUDP.send(sendPacket);

	}



	public static void main(String args[]) throws Exception {





		if(args.length != 4){//check the number of arguments

			System.out.println("Please enter <host address of the network emulator>, <UDP port number used by the emulator to receive data from the sender>,<UDP port number used by the sender to receive ACKs from the emulator>, and <name of the file to be transferred> in the given order.");

			return;

		}



		host = args[0];

		udp_datas = args[1];

		udp_acks = args[2];

		filename = args[3];

		



		if(!((isInteger(udp_acks)) && (isInteger(udp_datas)))){//check the req_code whether all numbers

			System.out.println("Please enter an integer UDP port number");

			return;

		}

		udp_ack = Integer.valueOf(udp_acks);

		udp_data = Integer.valueOf(udp_datas);



		PrintWriter seqnum = new PrintWriter("seqnum.log");//creat the file name as filenmae which is the paramerter

		PrintWriter acklog = new PrintWriter("ack.log");//creat the file arrival.log to save the seqNUM



		byte[] content = Files.readAllBytes(new File(filename).toPath());



		packet allpackets[] = transToPacket(content);



		int begin = 0;



		boolean first = false;//to check whether is first run the loop below



		int end = 10;



		while(begin < totalpacs){

			if(totalpacs < 10){

				end = totalpacs;

			}

			else{

				if(10 + begin > totalpacs){

					end = totalpacs;

				}

				else{

					end = 10 + begin;

				}

			}

			sendPac(begin, end, allpackets, seqnum);

			int recresult = 0;

			int c = 0;

			do{

				recresult = receiveAck(acklog);

				if(recresult == -1){

					if(!first){//if not ack packet 0

						break;

					}else{//set begin as the last acked + 1

						begin = acked + 1;

						break;

					}

				}

				else{

					first = true;

					acked = recresult;

				}

				c = recresult;

				int x = begin / 32;//

				if (begin % 32 >= 23){

					if (recresult <= 8){

						c = recresult + 32 * (x + 1);

					} 

					else{

						c = recresult + 32 * x;

					}

				} 

				else{

					c = recresult + 32 * x;

				}

				acked = c;

				//System.out.println("begin:    "  + begin);

				begin +=1;

				//System.out.println("totalpacs:    "  + totalpacs);

				//System.out.println("x:    "  + x);

				//System.out.println("c:    "  + c);

				//System.out.println("end:    "  + end);

				//System.out.println("    ");

			}while(c < end);

		}

		sendEOT();

		seqnum.close();

		acklog.close();

	}

}