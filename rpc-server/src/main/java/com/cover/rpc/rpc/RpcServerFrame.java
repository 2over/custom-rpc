package com.cover.rpc.rpc;

import com.cover.rpc.register.RegisterServiceWithRegCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/**
 * @author 谢浩
 * @date 2021-07-18 18:16
 */
@Service
public class RpcServerFrame {

	@Autowired
	private RegisterServiceWithRegCenter registerServiceWithRegCenter;

	/**服务端口号**/
	private int port;

	/** 处理服务请求任务**/
	private static class ServerTask implements Runnable {
		// 获取socket
		private Socket socket;

		private RegisterServiceWithRegCenter registerServiceWithRegCenter;

		public ServerTask(Socket socket, RegisterServiceWithRegCenter registerServiceWithRegCenter) {
			this.socket = socket;
			this.registerServiceWithRegCenter = registerServiceWithRegCenter;
		}

		@Override
		public void run() {
			try {

				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				// 接口名称
				String serviceName = inputStream.readUTF();
				// 方法名称
				String methodName = inputStream.readUTF();
				// 参数类型
				Class<?>[] paramTypes = (Class<?>[])inputStream.readObject();
				// 参数值
				Object[] args = (Object[]) inputStream.readObject();

				// 从容器中拿到服务的Class对象
				Class serviceClass = registerServiceWithRegCenter.getLocalService(serviceName);
				if (Objects.isNull(serviceClass)) {
					throw new ClassNotFoundException(serviceName + "not found");
				}

				// 这里是通过反射来执行实际的服务,
				// 在Dubbo中则是直接将实现类包装成Wrapper类进行调用
				Method method = serviceClass.getMethod(methodName, paramTypes);
				Object result = method.invoke(serviceClass.newInstance(), args);

				// 将服务的执行结果通知调用者
				outputStream.writeObject(result);
				outputStream.flush();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 启动服务
	 */
	public void startService(String serviceName, String host, int port, Class impl) throws IOException {
		// 服务端要启动一个ServerSocket而不是Socket
		ServerSocket serverSocket = new ServerSocket();
		// 一个ServerSocket需要依赖一个端口以此来区分和其他应用程序
		serverSocket.bind(new InetSocketAddress(port));
		System.out.println("RPC Server on :" + port + ":运行");
		// 将服务提供者的IP注册到注册中心
		registerServiceWithRegCenter.registerRemote(serviceName, host, port, impl);
		// 长轮询监听客户端发起的请求
		try {
			while (true) {
				// 启动一个线程去监听
				new Thread(new ServerTask(serverSocket.accept(),
						registerServiceWithRegCenter)).start();
			}
		} finally {
			// 理论上服务器是不会主动关闭的,除非手动关闭,但是为了逻辑上的严谨
			serverSocket.close();
		}

	}

}
