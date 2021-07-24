package com.cover.rpc.service;

import com.cover.rpc.vo.UserInfoVo;

/**
 * @author 谢浩
 * @date 2021-07-18 18:50
 */
public interface TestService {
	/**
	 * 根据id获取UserInfo
	 * @return
	 */
	UserInfoVo getUserInfo();
}
