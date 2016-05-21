package net.fs.cap;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapStat;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.MacAddress;

import net.fs.rudp.Route;
import net.fs.utils.ByteShortConvert;
import net.fs.utils.LogUtil;
import net.fs.utils.PacketUtils;

/**
 * @author hefan.hf
 * @version $Id: CapEnv, v 0.1 16/5/21 上午11:05 hefan.hf Exp $
 */
public class CapEnv {

	//是否为客户端
	boolean isClient=false;

	public boolean fwSuccess=true;

	private  final int SNAPLEN= 10*1024;
	private  final int READ_TIMEOUT=1;
	private  final int COUNT=-1;


	boolean ppp=false;

	String testIp_tcp="";
	String testIp_udp="5.5.5.5";
	public MacAddress gateway_mac;
	public MacAddress local_mac;
	Inet4Address local_ipv4;
	String selectedInterfaceName=null;
	String selectedInterfaceDes="";


	//Tun管理器,主要维护了Tun的table
	TunManager tcpManager=null;

	/**
	 * 默认构造函数
	 * @param isClient
	 * @param fwSuccess
	 */
	public CapEnv(boolean isClient,boolean fwSuccess){
		this.isClient=isClient;
		this.fwSuccess=fwSuccess;
		tcpManager=new TunManager(this);
	}

	/**
	 * 初始化
	 */
	public void init(){

	}

	/**
	 * 初始化接口
	 * @return
	 */
	private boolean initInterface(){
		boolean success=false;

		return success;
	}


	/**
	 * 获得网络接口
	 */
	private void detectInterface(){
		List<PcapNetworkInterface> allDevs = null;
		HashMap<PcapNetworkInterface, PcapHandle> handleTable=new HashMap<PcapNetworkInterface, PcapHandle>();
		//获得所有网络接口
		try {
			allDevs = Pcaps.findAllDevs();
		} catch (PcapNativeException e1) {
			e1.printStackTrace();
			return;
		}
		//遍历所有网络接口
		for (final PcapNetworkInterface pi : allDevs){
			try{
				//开启网卡拦截
				final PcapHandle handle = pi.openLive(SNAPLEN, getMode(pi), READ_TIMEOUT);
				handleTable.put(pi, handle);
				//网卡抓包监听
				final PacketListener listener= new PacketListener() {
					//获得包时
					@Override
					public void gotPacket(Packet packet) {
						try {
							if(packet instanceof EthernetPacket){
								EthernetPacket packet_eth=(EthernetPacket) packet;
								EthernetPacket.EthernetHeader head_eth=packet_eth.getHeader();
								//判断是否pppoe类型
								if(head_eth.getType().value()==0xffff8864){
									ppp=true;
									PacketUtils.ppp=ppp;
								}

								IpV4Packet ipV4Packet=null;
								IpV4Packet.IpV4Header ipV4Header=null;
								//获得ipv4的包
								if(ppp){
									ipV4Packet=getIpV4Packet_pppoe(packet_eth);
								}else {
									if(packet_eth.getPayload() instanceof IpV4Packet){
										ipV4Packet=(IpV4Packet) packet_eth.getPayload();
									}
								}

								if(ipV4Packet!=null){
									ipV4Header=ipV4Packet.getHeader();

									if(ipV4Header.getSrcAddr().getHostAddress().equals(testIp_tcp)){
										local_mac=head_eth.getDstAddr();
										gateway_mac=head_eth.getSrcAddr();
										local_ipv4=ipV4Header.getDstAddr();
										selectedInterfaceName=pi.getName();
										if(pi.getDescription()!=null){
											selectedInterfaceDes=pi.getDescription();
										}
										//MLog.println("local_mac_tcp1 "+gateway_mac+" gateway_mac "+gateway_mac+" local_ipv4 "+local_ipv4);
									}
									if(ipV4Header.getDstAddr().getHostAddress().equals(testIp_tcp)){
										local_mac=head_eth.getSrcAddr();
										gateway_mac=head_eth.getDstAddr();
										local_ipv4=ipV4Header.getSrcAddr();
										selectedInterfaceName=pi.getName();
										if(pi.getDescription()!=null){
											selectedInterfaceDes=pi.getDescription();
										}
										//MLog.println("local_mac_tcp2 local_mac "+local_mac+" gateway_mac "+gateway_mac+" local_ipv4 "+local_ipv4);
									}
									//udp
									if(ipV4Header.getDstAddr().getHostAddress().equals(testIp_udp)){
										local_mac=head_eth.getSrcAddr();
										gateway_mac=head_eth.getDstAddr();
										local_ipv4=ipV4Header.getSrcAddr();
										selectedInterfaceName=pi.getName();
										if(pi.getDescription()!=null){
											selectedInterfaceDes=pi.getDescription();
										}
										//MLog.println("local_mac_udp "+gateway_mac+" gateway_mac"+gateway_mac+" local_ipv4 "+local_ipv4);
									}

								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				};
				//开始启动监听
				Thread thread=new Thread(){

					public void run(){
						try {
							handle.loop(COUNT, listener);
							PcapStat ps = handle.getStats();
							handle.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				};
				thread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//获得本机的MAC地址
		detectMac_tcp();

		//已经拿到MAC地址,关闭所有网卡的loop
		//其实这个地方很危险,万一loop没关掉....
		Iterator<PcapNetworkInterface> it=handleTable.keySet().iterator();
		while(it.hasNext()){
			PcapNetworkInterface pi=it.next();
			PcapHandle handle=handleTable.get(pi);
			try {
				handle.breakLoop();
			} catch (NotOpenException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 前面已经启动了监听,现在发送数据包,从而从包中获得机器网卡信息
	 */
	private void detectMac_tcp() {
		InetAddress address=null;
		try {
			address = InetAddress.getByName("bing.com");
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			try {
				address = InetAddress.getByName("163.com");
			} catch (UnknownHostException e) {
				e.printStackTrace();
				try {
					address = InetAddress.getByName("apple.com");
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
		}
		if(address==null){
			LogUtil.info("域名解析失败,请检查DNS设置!");
		}
		final int por=80;
		testIp_tcp=address.getHostAddress();
		for(int i=0;i<5;i++){
			try {
				Route.es.execute(new Runnable() {

					@Override
					public void run() {
						try {
							Socket socket=new Socket(testIp_tcp,por);
							socket.close();
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				Thread.sleep(500);
				if(local_mac!=null){
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * 判断网卡是否具备混杂模式.
	 * 这里采用的是判断是否是无线网卡?
	 * @param pi
	 * @return
	 */
	PcapNetworkInterface.PromiscuousMode getMode(PcapNetworkInterface pi){
		PcapNetworkInterface.PromiscuousMode mode=null;
		String string=(pi.getDescription()+":"+pi.getName()).toLowerCase();
		if(string.contains("wireless")){
				mode= PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS;
		}else {
			mode= PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
		}
		return mode;
	}

	/**
	 * 从pppoe中获得ipv4的包
	 * @param packet_eth
	 * @return
	 * @throws IllegalRawDataException
	 */
	IpV4Packet getIpV4Packet_pppoe(EthernetPacket packet_eth) throws IllegalRawDataException {
		IpV4Packet ipV4Packet=null;
		byte[] pppData=packet_eth.getPayload().getRawData();
		if(pppData.length>8&&pppData[8]==0x45){
			byte[] b2=new byte[2];
			System.arraycopy(pppData, 4, b2, 0, 2);
			short len=(short) ByteShortConvert.toShort(b2, 0);
			int ipLength=toUnsigned(len)-2;
			byte[] ipData=new byte[ipLength];
			//设置ppp参数
			PacketUtils.pppHead_static[2]=pppData[2];
			PacketUtils.pppHead_static[3]=pppData[3];
			if(ipLength==(pppData.length-8)){
				System.arraycopy(pppData, 8, ipData, 0, ipLength);
				ipV4Packet=IpV4Packet.newPacket(ipData, 0, ipData.length);
			}else {
				LogUtil.info("长度不符!");
			}
		}
		return ipV4Packet;
	}
	public static int toUnsigned(short s) {
		return s & 0x0FFFF;
	}
}