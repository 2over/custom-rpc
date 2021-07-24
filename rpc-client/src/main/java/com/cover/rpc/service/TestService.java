package com.cover.rpc.service;

import com.cover.rpc.vo.UserInfoVo;

/**
 * @author 谢浩
 * @date 2021-07-18 18:13
 */
public interface TestService {

	/**
	 * 根据id获取UserInfo
	 * @param id {@link java.lang.Long}
	 * @return
	 */
	UserInfoVo getUserInfo(Long id);
}
