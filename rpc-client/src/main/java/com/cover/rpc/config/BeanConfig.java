package com.cover.rpc.config;

import com.cover.rpc.service.TestService;
import com.cover.rpc.rpc.RpcClientFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 谢浩
 * @date 2021-07-18 18:10
 */
@Configuration
public class BeanConfig {

	@Autowired
	private RpcClientFrame rpcClientFrame;

	@Bean
	public TestService getUserInfo() throws ClassNotFoundException {
		return rpcClientFrame.getRemoteProxyObject(TestService.class);
	}













}
