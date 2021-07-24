package com.cover.rpc.register;

import com.cover.rpc.vo.RegisterServiceVo;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author 谢浩
 * @date 2021-07-10 15:38
 * 服务注册中心,服务提供者在启动时需要在注册中登记自己的信息
 * 服务消费者需要从注册中心获取服务提供者的ip地址列表
 */
@Service
public class RegisterCenter {

	/** 注册服务的端口号 **/
	private int port;
	/** 服者名称对应的服务提供者地址的集合 **/
	private static final Map<String, Set<RegisterServiceVo>> SERVICE_HOLDER = new HashMap<>();

	/**
	 * 服务注册, 可能会有多个提供者同时注册, 进行加锁
	 * @param serviceName 方法名称
	 * @param host 提供者的host
	 * @param port 端口号
	 */
	private static synchronized void registerService(String serviceName, String host, int port) {
		// 获取当前服务的已有地址集合
		Set<RegisterServiceVo> serviceVoSet = SERVICE_HOLDER.get(serviceName);
		// 判断该服务是否存在注册中心
		if (Objects.isNull(serviceVoSet)) {
			// 该服务在注册中心对应的地址为空
			serviceVoSet = new HashSet<>();
			SERVICE_HOLDER.put(serviceName, serviceVoSet);
		}

		// 将新增的服务提供者加入到注册中心
		serviceVoSet.add(new RegisterServiceVo(port, host));
		System.out.println("服务已注册[" + serviceName + "],"
				+ "地址[" + host + "], 端口[" + port + "]");
	}

	/**
	 * 获取服务提供者所能提供的地址和端口
	 * @param serviceName 服务名称
	 * @return 服务提供者在注册中心所能提供的地址和端口
	 */
	private static Set<RegisterServiceVo> getService(String serviceName) {
		return SERVICE_HOLDER.get(serviceName);
	}

	/**
	 * 处理服务请求的任务,主要分为以下两种服务:
	 * 1.服务向注册中心注册服务
	 * 2.服务向注册中心获取服务
	 */
	private static class ServerTask implements Runnable {

		private Socket client = null;

		public ServerTask(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			ObjectInputStream inputStream = null;
			ObjectOutputStream outputStream = null;

			try {
				// 获取客户端的输入和输出流
				// 客户端的输入流转换为对象输入流
				inputStream = new ObjectInputStream(client.getInputStream());
				outputStream = new ObjectOutputStream(client.getOutputStream());

				// 检查当前请求是注册服务还是获取服务
				boolean isGetService = inputStream.readBoolean();
				// 服务查询服务, 获取服务提供者
				if (isGetService) {
					String serviceName = inputStream.readUTF();
					// 取出该服务对应的提供者集合
					Set<RegisterServiceVo> result = getService(serviceName);
					// 返回给客户端
					outputStream.writeObject(result);
					// 强制把输出流的内容返回给客户端,
					// 负责输出流则需要等待输入流的容量达到一定限制后,才会返回
					outputStream.flush();

					System.out.println("将已注册的服务[" + serviceName + "],提供给给客户端");
				} else { // 注册服务
					// 获取待注册的服务提供方的ip和端口
					String serviceName = inputStream.readUTF();
					// 获取地址
					String host = inputStream.readUTF();
					// 获取端口号
					int port = inputStream.readInt();
					// 注册中心引入服务
					registerService(serviceName, host, port);
					// 设置已经写入
					outputStream.writeBoolean(true);
					outputStream.flush();

				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// 关闭流
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * 启动服务注册中心
	 */
	public void startService() throws IOException {
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress(port));
		System.out.println("服务注册中心 on:" + port + "运行");
		// 服务注册中心,是需要进行长轮询来监听的,时刻关注何时有客户端的连接
		try {
			while (true) {
				// 在BIO中client是一个请求建立一次TCP连接.
				// 这里用一个线程来表示
				new Thread(new ServerTask(serverSocket.accept())).start();
			}
		} finally {
			// 连接关闭 对于服务端来说,一般是不关闭的,但是为了逻辑上的严谨.增加了服务端的关闭
			serverSocket.close();
		}
	}

	@PostConstruct
	public void init() {
		// 注册中心的初始化端口
		this.port = 9999;
		new Thread(() -> {
			try {
				startService();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

}
