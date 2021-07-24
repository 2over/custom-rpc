package com.cover.rpc.register;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 谢浩
 * @date 2021-07-18 18:20
 */
@Service
public class RegisterServiceWithRegCenter {

	/**本地可提供服务的一个名单, 用缓存实现**/
	private static final Map<String, Class> SERVICE_CACHE = new ConcurrentHashMap<>();

	/**
	 * 向远程注册服务器注册该服务,同时将服务端可提供的服务缓存起来
	 * @param serviceName 服务名称
	 * @param host ip地址
	 * @param port 端口号
	 * @param impl 服务对应的实现类
	 */
	public void registerRemote(String serviceName, String host, int port, Class impl) throws IOException {
		// 注册到注册中心
		Socket socket = null;
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress("127.0.0.1", 9999));

			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.writeBoolean(false);
			// 注册服务
			// 提供的服务名称
			outputStream.writeUTF(serviceName);
			// 服务提供者的ip
			outputStream.writeUTF(host);
			// 服务提供者的端口号
			outputStream.writeInt(port);
			// 刷新缓冲区
			outputStream.flush();

			// 输入流
			inputStream = new ObjectInputStream(socket.getInputStream());
			if (inputStream.readBoolean()) {
				System.out.println("服务[" + serviceName + "]注册成功");
			}

			// 将可提供的服务放入本地缓存
			SERVICE_CACHE.put(serviceName, impl);

		} finally {
			if (Objects.nonNull(socket)) {
				socket.close();
			}

			if (Objects.nonNull(outputStream)) {
				outputStream.close();
			}
			if (Objects.nonNull(inputStream)) {
				inputStream.close();
			}
		}
	}

	/**
	 * 获取服务
	 * @param serviceName
	 * @return
	 */
	public Class getLocalService(String serviceName) {
		return SERVICE_CACHE.get(serviceName);
	}
}
