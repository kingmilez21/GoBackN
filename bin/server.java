import java.io.*; 

import java.net.*; 

class server {



	//init

	static String hostname = null;

	static String udp_acks = null;

	static String udp_datas = null;



	static int udp_ack = 0;

	static int udp_data = 0;



	static String filename = null;



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

	//receive

	public static void doreceiver() throws Exception {



		//socket and datagram init

		byte[] receiveData = new byte[1024];//buffer recived data

	

		DatagramSocket sendSOC = new DatagramSocket();//send ack socket

		DatagramSocket receiveSOC = new DatagramSocket(udp_data);//receive data socket



		PrintWriter file = new PrintWriter(filename);//creat the file name as filenmae which is the paramerter

		PrintWriter log = new PrintWriter("arrival.log");//creat the file arrival.log to save the seqNUM



		int seqfornow = 0;//seq num

		int expecseqnum = 0;

		int firstrec = 0;//check whether is first arrival



		//loop to reveive

		while (true) {

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			receiveSOC.receive(receivePacket);//get the packet

			packet receivePac = packet.parseUDPdata(receivePacket.getData());



			log.println(receivePac.getSeqNum());//get the seq num and write it in arrival.log

			packet sendbackACK = null;



			if(receivePac.getType() == 2){//get EOT

				file.close();

				log.close();

				sendbackACK = packet.createEOT(receivePac.getSeqNum());//sent back eot

				InetAddress IPAddress = InetAddress.getByName(hostname);

				byte sendACK[] = sendbackACK.getUDPdata();

				DatagramPacket sendPacket = new DatagramPacket(sendACK, sendACK.length, IPAddress, udp_ack);

            	sendSOC.send(sendPacket);

				return;

			}



			if(receivePac.getSeqNum() == expecseqnum){//if receive the expected seqnum

				firstrec = 1;//to set the flag as 1 represent that we already accepy packet 0

				seqfornow = receivePac.getSeqNum();

				expecseqnum = (seqfornow + 1) % 32;



				//if it is content, that write in file which is named filename 

				file.print(new String(receivePac.getData()));

			}

			else if(firstrec == 0){//if not yet recive packet 0 

				continue;

			}

			//sendback ack

			sendbackACK = packet.createACK(seqfornow);



			InetAddress IPAddress = InetAddress.getByName(hostname);

			byte sendACK[] = sendbackACK.getUDPdata();

			DatagramPacket sendPacket = new DatagramPacket(sendACK, sendACK.length, IPAddress, udp_ack);

            sendSOC.send(sendPacket);

		}



	}

	public static void main(String args[]) throws Exception {





		if(args.length != 4){//check the number of arguments

			System.out.println("Please enter  <hostname for the network emulator>, <UDP port number used by the link emulator to receive ACKs from the receiver>, <UDP port number used by the receiver to receive data from the emulator>, and <name of the file into which the received data is written>inthegivenorder.");

			return;

		}

		//store the input

		hostname = args[0];

		udp_acks = args[1];

		udp_datas = args[2];

		filename = args[3];

		



		if(!((isInteger(udp_acks)) && (isInteger(udp_datas)))){//check the req_code whether all numbers

			System.out.println("Please enter an integer UDP port number");

			return;

		}

		udp_ack = Integer.valueOf(udp_acks);

		udp_data = Integer.valueOf(udp_datas);



		doreceiver();

	}



}