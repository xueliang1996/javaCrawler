package com.crawler.test;

import com.crawler.acquire.AccessMethod;
import com.crawler.domain.FormData;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		FormData fd = new FormData();
		// 读取配置文件中的登录信息
		fd.getData();
		String nickName = fd.getNickName();
		String pwd = fd.getPwd();
		String remember = fd.getRemember();
		String articleno = "819474-405";
		AccessMethod am = new AccessMethod();
		String htmlText = am.sendRequestAndGetResponse(nickName, pwd, remember, articleno);
		System.out.println(htmlText);
	}

}
