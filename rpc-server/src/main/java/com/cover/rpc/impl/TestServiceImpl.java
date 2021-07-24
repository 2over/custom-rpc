package com.cover.rpc.impl;

import com.cover.rpc.service.TestService;
import com.cover.rpc.vo.UserInfoVo;

/**
 * @author 谢浩
 * @date 2021-07-18 18:49
 */
public class TestServiceImpl implements TestService {


	@Override
	public UserInfoVo getUserInfo() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("正在返回结果----");
		return new UserInfoVo("Cover", 22);
	}
}
