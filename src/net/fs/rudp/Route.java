package net.fs.rudp;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hefan.hf
 * @version $Id: Route, v 0.1 16/5/21 上午10:51 hefan.hf Exp $
 */
public class Route {

	//线程池
	public static ThreadPoolExecutor es;

	//1客户端,2服务端
	public int mode=1;

	//是否使用TCP
	public boolean useTcpTun=true;

	public String processName="";

	/**
	 * 静态构造函数
	 */
	static{
		//阻塞队列
		SynchronousQueue queue = new SynchronousQueue();
		/**
		 * corePoolSize： 线程池维护线程的最少数量
		 * maximumPoolSize：线程池维护线程的最大数量
		 * keepAliveTime： 线程池维护线程所允许的空闲时间,10s
		 * unit： 线程池维护线程所允许的空闲时间的单位,毫秒
		 * workQueue： 线程池所使用的缓冲队列
		 */
		es = new ThreadPoolExecutor(100, Integer.MAX_VALUE, 10*1000, TimeUnit.MILLISECONDS, queue);
	}


	/**
	 * 默认构造函数
	 * @param processName
	 * @param routePort     端口号
	 * @param mode          客户端还是服务器端
	 * @param tcp           是否开启TCP
	 * @param tcpEnvSuccess
	 * @throws Exception
	 */
	public Route(String processName,short routePort,int mode,boolean tcp,boolean tcpEnvSuccess) throws Exception{
		this.mode = mode;
		this.useTcpTun = tcp;
		this.processName = processName;
		//如果开启TCP
		if(useTcpTun){
			//服务器端
			if(mode == 2){

			}
		}
	}

}