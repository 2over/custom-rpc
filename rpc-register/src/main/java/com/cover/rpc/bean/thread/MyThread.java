package com.cover.rpc.bean.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 谢浩
 * @date 2021-07-10 17:06
 */
public class MyThread implements ThreadFactory {
	/** 前缀**/
	private String prefix;

	/** 计数器 **/
	private AtomicInteger counter;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public MyThread(String prefix) {
		this.prefix = prefix;
		counter = new AtomicInteger(1);
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, prefix + "_" + counter.getAndIncrement());
	}
}
