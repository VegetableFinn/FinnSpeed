package net.fs.cap;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Java使用DatagramSocket代表UDP协议的Socket，DatagramSocket本身只是码头，不维护状态，不能产生IO流，
 * 它的唯一作用就是接收和发送数据报，Java使用DatagramPacket来代表数据报，
 * DatagramSocket接收和发送的数据都是通过DatagramPacket对象完成的。
 *
 * @author hefan.hf
 * @version $Id: VDatagramSocket, v 0.1 16/5/21 上午11:00 hefan.hf Exp $
 */
public class VDatagramSocket extends DatagramSocket {

	//本地端口号
	private int localPort;

	//是否客户端
	boolean isClient=true;

	/**
	 * 默认构造函数.
	 * 创建一个DatagramSocket实例，并将该对象绑定到本机默认IP地址、本机所有可用端口中随机选择的某个端口。
	 * @throws SocketException
	 */
	public VDatagramSocket() throws SocketException {

	}

	/**
	 * 默认构造函数.
	 * 创建一个DatagramSocket实例，并将该对象绑定到本机默认IP地址、指定端口。
	 *
	 * @param port              端口号
	 * @throws SocketException
	 */
	public VDatagramSocket(int port) throws SocketException {
		localPort=port;
	}

	public void send(DatagramPacket datagramPacket){
		TCPTun tun=null;
		if(isClient){

		}
	}




	/**
	 * Getter method for property <tt>localPort</tt>.
	 *
	 * @return property value of localPort
	 */

	@Override
	public int getLocalPort() {
		return localPort;
	}

	/**
	 * Setter method for property <tt>localPort</tt>.
	 *
	 * @param localPort value to be assigned to property localPort
	 */
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
}