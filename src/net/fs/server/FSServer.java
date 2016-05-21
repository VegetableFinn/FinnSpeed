package net.fs.server;

import net.fs.utils.LogUtil;

/**
 * 服务端
 * @author hefan.hf
 * @version $Id: FSServer, v 0.1 16/5/21 上午10:42 hefan.hf Exp $
 */
public class FSServer {

	//获得系统类型
	String osName = System.getProperty("os.name").toLowerCase();

	/**
	 * 默认构造函数
	 */
	public FSServer(){
		LogUtil.info("服务端开始初始化...");
		LogUtil.info("服务器操作系统:"+osName);
	}

}