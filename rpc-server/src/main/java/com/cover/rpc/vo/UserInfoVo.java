package com.cover.rpc.vo;

/**
 * @author 谢浩
 * @date 2021-07-18 18:51
 */
public class UserInfoVo {

	/** 姓名 **/
	private String name;
	/** 年龄 **/
	private int age;

	public UserInfoVo(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
