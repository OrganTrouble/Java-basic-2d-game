package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import jesse.game.Game;
import packets.Packet;
import packets.Packet.PacketTypes;
import packets.Packet00Login;
import entities.PlayerMP;

public class GameServer extends Thread{
	
	private DatagramSocket socket;
	private Game game;
	private List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>();
	
	public GameServer(Game game) {
		this.game = game;
		try {
			this.socket = new DatagramSocket(1331);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(true) {
			byte[] data = new byte[1024];
			DatagramPacket packet= new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
			//String message = new String(packet.getData());
			//System.out.println("CLIENT > [" + packet.getAddress().getHostAddress()+ ":"+ packet.getPort() + "] " + message); 
			//if(message.trim().equalsIgnoreCase("ping")) {
				//sendData("pong".getBytes(), packet.getAddress(), packet.getPort()); 
			//}
		}
	}
	
	private void parsePacket(byte[] data, InetAddress address, int port) {
		String message = new String(data).trim();
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet = null;
		switch(type) {
		default:
		case INVALID:
			break;
		case LOGIN:
			packet = new Packet00Login(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "] " + ((Packet00Login)packet).getUsername() + " has connected...");
			PlayerMP player = new PlayerMP(game.level, 100, 100, ((Packet00Login)packet).getUsername(), address, port);
			this.addConnection(player, ((Packet00Login)packet));
			if(player != null) {
				this.connectedPlayers.add(player);
				game.level.addEntity(player);
				game.player = player;
			}
			break;
		case DISCONNECT:
			break;
		}
	}
	

	private void addConnection(PlayerMP player, Packet00Login packet) {
		boolean alreadyConnected = false;
		for(PlayerMP p : this.connectedPlayers) {
			if(player.getUsername().equalsIgnoreCase(p.getUsername())) {
				
			}
		}
	}

	public void sendData(byte[] data, InetAddress ipAddress, int port){
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1331);
		try {
			this.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendDataToAllClients(byte[] data) {
		for(PlayerMP p : connectedPlayers) {
			sendData(data, p.ipAddress, p.port);
		}
	}

}

