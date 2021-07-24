package com.cover.rpc.rpc;

import com.cover.rpc.vo.RegisterServiceVo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * @author 谢浩
 * @date 2021-07-18 17:20
 */
@Service
public class RpcClientFrame {

	/** 模拟负载均衡,从服务提供者列表中选取一个服务提供者 **/
	private static final Random RANDOM = new Random();

	/**
	 * 远程服务的代理对象,参数为客户端要调用的服务
	 * @param serviceInterface 要调用的服务接口
	 * @param <T> 服务的class
	 * @return T
	 */
	public static<T> T getRemoteProxyObject(final Class<?> serviceInterface) throws ClassNotFoundException {
		// 获得远程服务的一个网络地址
		InetSocketAddress addr = //new InetSocketAddress("127.0.0.1",8832);
				getService(serviceInterface.getName());

		return (T)Proxy.newProxyInstance(serviceInterface.getClassLoader(),
				new Class<?>[]{serviceInterface},
				new DynProxy(serviceInterface, addr));
	}

	/**
	 * 获得远程服务的地址,从众多服务提供者列表中选取一个
	 * @param serviceName 服务名称
	 * @return 服务提供者的地址
	 */
	private static InetSocketAddress getService(String serviceName) throws ClassNotFoundException {

		// 获得服务提供者的地址列表
		List<InetSocketAddress> serviceVoList = getServiceList(serviceName);
		// 随机选取服务提供者的地址列表
		InetSocketAddress inetSocketAddress = serviceVoList.get(RANDOM.nextInt(serviceVoList.size()));

		System.out.println("本次选择了服务器:" + inetSocketAddress);
		return inetSocketAddress;
	}

	/**
	 * 获得服务提供者的地址
	 * @param serviceName 服务名称
	 * @return 提供者列表
	 */
	private static List<InetSocketAddress> getServiceList(String serviceName) throws ClassNotFoundException {
		Socket socket = null;
		ObjectOutputStream outputStream = null;
		ObjectInputStream inputStream = null;

		try {
			// 建立socket连接
			socket = new Socket();
			// 发起连接请求
			socket.connect(new InetSocketAddress("127.0.0.1", 9999));
			// 获取socket的输出流
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			// 需要获取服务提供者
			outputStream.writeBoolean(true);
			// 告诉注册中心服务名
			outputStream.writeUTF(serviceName);
			// 刷新缓冲区
			outputStream.flush();

			// 获取socket的输入流
			inputStream = new ObjectInputStream(socket.getInputStream());
			// 提供该服务的服务提供者们
			Set<RegisterServiceVo> result =
					(Set<RegisterServiceVo>)inputStream.readObject();
			// 将服务提供者们转换为InetSocketAddress
			List<InetSocketAddress> services = new ArrayList<>();
			for (RegisterServiceVo serviceVo : result) {
				// 服务提供者的ip
				String host = serviceVo.getHost();
				// 服务提供者的端口号
				int port = serviceVo.getPort();

				services.add(new InetSocketAddress(host, port));
			}

			// 验证
			System.out.println("获取服务【" + serviceName + "】提供者的地址列表【" + services + "】, 准备调用");
			return services;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭资源 从内部依次向外关闭

			// 关闭input
			if (Objects.nonNull(inputStream)) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// 关闭output
			if (Objects.nonNull(outputStream)) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// 关闭socket
			if (Objects.nonNull(socket)) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return null;
	}


	/**
	 * 动态代理,实现对远程服务的访问
	 */
	private static class DynProxy implements InvocationHandler {
		private Class<?> serviceInterface;
		private InetSocketAddress address;

		public DynProxy(Class<?> serviceInterface, InetSocketAddress address) {
			this.serviceInterface = serviceInterface;
			this.address = address;
		}


		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Socket socket = null;
			ObjectInputStream inputStream = null;
			ObjectOutputStream outputStream = null;
			try {
				socket = new Socket();
				socket.connect(address);
				outputStream = new ObjectOutputStream(socket.getOutputStream());

				// 接口名称
				outputStream.writeUTF(serviceInterface.getName());
				// 方法名称
				outputStream.writeUTF(method.getName());

				// 参数类型
				outputStream.writeObject(method.getParameterTypes());

				// 参数值
				outputStream.writeObject(args);

				// 刷新缓冲区
				outputStream.flush();

				// 获取服务器的输出内容
				inputStream = new ObjectInputStream(socket.getInputStream());

				System.out.println(serviceInterface + "远程调用成功");
				return inputStream.readObject();
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
	}


}
