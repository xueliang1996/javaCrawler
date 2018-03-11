package com.crawler.domain;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class FormData {

	private String nickName;
	private String pwd;
	private String remember;

	public FormData() {
		// TODO Auto-generated constructor stub
	}

	public String getNickName() {
		return nickName;
	}

	public String getPwd() {
		return pwd;
	}

	public String getRemember() {
		return remember;
	}

	public void getData() throws Exception {
		String rootPath = System.getProperty("user.dir").replace("\\", "/");
		FileInputStream fs = new FileInputStream(rootPath + "/FormData.properties");
		Properties pro = new Properties();
		pro.load(new InputStreamReader(fs, "utf-8"));// 直接用pro.load(fs)会出现输出内容中文乱码
		fs.close();
		nickName = pro.getProperty("nickName");
		pwd = pro.getProperty("pwd");
		remember = pro.getProperty("remember");
	}
}
