package com.cover.rpc.vo;

import java.io.Serializable;

/**
 * @author 谢浩
 * @date 2021-07-10 15:40
 */
public class RegisterServiceVo implements Serializable {

	/** 服务提供者的端口 **/
	private int port;
	/** 服务提供者的host地址 **/
	private String host;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public RegisterServiceVo(int port, String host) {
		this.port = port;
		this.host = host;
	}
}
