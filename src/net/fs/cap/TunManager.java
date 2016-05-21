package net.fs.cap;

import java.util.HashMap;
import java.util.Iterator;

import net.fs.rudp.CopiedIterator;

/**
 * @author hefan.hf
 * @version $Id: TunManager, v 0.1 16/5/21 上午11:07 hefan.hf Exp $
 */
public class TunManager {

	//Tun的链接维护哈希表.String为IP+Port,Value为对应的tun
	HashMap<String, TCPTun> connTable=new HashMap<String, TCPTun>();

	//单例模式?
	static TunManager tunManager;
	{
		tunManager=this;
	}
	CapEnv capEnv;
	TunManager(CapEnv capEnv){
		this.capEnv=capEnv;
	}

	public static TunManager get(){
		return tunManager;
	}

	TCPTun defaultTcpTun;

	//每隔1s执行一次扫描
	Thread scanThread;
	{
		scanThread=new Thread(){
			public void run(){
				while(true){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					scan();
				}
			}
		};
		scanThread.start();
	}

	Object syn_scan=new Object();

	/**
	 * 线程不断扫描
	 */
	void scan(){
		Iterator<String> it=getConnTableIterator();
		while(it.hasNext()){
			String key=it.next();
			TCPTun tun=connTable.get(key);
			if(tun!=null){
				if(tun.preDataReady){
					//无数据超时
					long t=System.currentTimeMillis()-tun.lastReceiveDataTime;
					if(t>6000){
						connTable.remove(key);
						if(capEnv.client){
							defaultTcpTun=null;
							MLog.println("tcp隧道超时");
						}
					}
				}else{
					//连接中超时
					if(System.currentTimeMillis()-tun.createTime>5000){
						connTable.remove(key);
					}
				}
			}
		}
	}

	/**
	 * 移除Tun
	 * @param tun
	 */
	public void removeTun(TCPTun tun){
		connTable.remove(tun.key);
	}

	/**
	 * 获得迭代器
	 * @return
	 */
	Iterator<String> getConnTableIterator(){
		Iterator<String> it=null;
		synchronized (syn_scan) {
			it=new CopiedIterator(connTable.keySet().iterator());
		}
		return it;
	}

	/**
	 * 根据IP+port以及本地Port,获得客户端的Tun
	 * @param remoteAddress
	 * @param remotePort
	 * @param localPort
	 * @return
	 */
	public TCPTun getTcpConnection_Client(String remoteAddress,short remotePort,short localPort){
		return connTable.get(remoteAddress+":"+remotePort+":"+localPort);
	}

	/**
	 * 添加Tun到table中
	 * @param conn
	 */
	public void addConnection_Client(TCPTun conn) {
		synchronized (syn_scan) {
			String key=conn.remoteAddress.getHostAddress()+":"+conn.remotePort+":"+conn.localPort;
			//MLog.println("addConnection "+key);
			conn.setKey(key);
			connTable.put(key, conn);
		}
	}

	/**
	 * 根据服务器的ip和Port获得服务器的tun
	 * @param remoteAddress
	 * @param remotePort
	 * @return
	 */
	public TCPTun getTcpConnection_Server(String remoteAddress,short remotePort){
		return connTable.get(remoteAddress+":"+remotePort);
	}

	/**
	 * 添加服务器的tun
	 * @param conn
	 */
	public void addConnection_Server(TCPTun conn) {
		synchronized (syn_scan) {
			String key=conn.remoteAddress.getHostAddress()+":"+conn.remotePort;
			//MLog.println("addConnection "+key);
			conn.setKey(key);
			connTable.put(key, conn);
		}
	}

	public TCPTun getDefaultTcpTun() {
		return defaultTcpTun;
	}

	public void setDefaultTcpTun(TCPTun defaultTcpTun) {
		this.defaultTcpTun = defaultTcpTun;
	}


}