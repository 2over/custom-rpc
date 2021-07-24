package com.cover.rpc.server;

import com.cover.rpc.rpc.RpcServerFrame;
import com.cover.rpc.impl.TestServiceImpl;
import com.cover.rpc.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Random;

/**
 * @author 谢浩
 * @date 2021-07-18 18:59
 */
@Service
public class MyServer {

	@Autowired
	private RpcServerFrame rpcServerFrame;

	@PostConstruct
	public void server() throws Throwable {
		Random random = new Random();
		int port = 8778 + random.nextInt(100);
		rpcServerFrame.startService(TestService.class.getName(),
				"127.0.0.1", port, TestServiceImpl.class);
	}
}
