package net.fs.server;

import net.fs.rudp.ConnectionUDP;

/**
 * @author hefan.hf
 * @version $Id: MapTunnelProcessor, v 0.1 16/5/21 上午10:48 hefan.hf Exp $
 */
public class MapTunnelProcessor {

	public void process(final ConnectionUDP conn){
		this.conn=conn;
		pc=this;
		Route.es.execute(new Runnable(){
			public void run(){
				process();
			}
		});
	}


}