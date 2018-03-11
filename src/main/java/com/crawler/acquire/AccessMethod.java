package com.crawler.acquire;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.test.TesseractExample;

/*----------------
 * 						
 * 发送请求并获得响应的方法
 * 
 * ------------*/
public class AccessMethod {

	// 发送请求并获取响应（cookie+验证码图片）
	public Map<String, String> sendRequestAndGetResponse() throws Exception {

		AccessMethod am = new AccessMethod();
		// 生成HttpClient对象
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建一个GET对象
		String codeUrl = "http://www.tianmasport.com/ms/ImageServlet?time=new%20Date().getTime()";
		HttpGet get = new HttpGet(codeUrl);
		// 执行请求
		CloseableHttpResponse response = httpclient.execute(get);
		// 取得响应结果
		int statusCode = response.getStatusLine().getStatusCode();
		System.out.println("获取验证码时状态码:" + statusCode);
		Header[] headers = response.getAllHeaders();
		Map<String, String> map = new HashMap<String, String>();
		for (Header h : headers) {
			map.put(h.getName(), h.getValue());
		}
		String cookie = map.get("Set-Cookie");

		// 生成图片
		int width = 65;
		int height = 30;
		File file = new File("tessdata/valcode.jpg");
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ImageIO.write(image, "jpg", file);
		OutputStream os = null;
		// 写入图片
		os = new FileOutputStream(file);
		response.getEntity().writeTo(os);
		BufferedImage images = ImageIO.read(file);
		ImageIO.write(image, "jpg", os);
		os.close();
		String vcode = am.getVcode();
		Map<String, String> cookieAndVcode = new HashMap<String, String>();
		cookieAndVcode.put("cookie", cookie);
		cookieAndVcode.put("vcode", vcode);
		httpclient.close();

		return cookieAndVcode;
	}

	// 解析验证码图片
	public String getVcode() throws UnsupportedEncodingException {

		TesseractExample teseeract = new TesseractExample();
		String vcode = teseeract.verificationCode("valcode");
		return vcode;
	}

	// 发送带参请求并获取数据
	public String sendRequestAndGetResponse(String nickName, String pwd, String remember, String articleno)
			throws Exception {

		AccessMethod am = new AccessMethod();
		Map<String, String> cookieAndVcode = am.sendRequestAndGetResponse();
		String cookie = cookieAndVcode.get("cookie");
		String vcode = cookieAndVcode.get("vcode").trim();
		String[] str = cookie.split(";");
		String[] strs = str[0].split("=");
		CookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookies = new BasicClientCookie(strs[0], strs[1]);
		cookies.setVersion(0);
		cookies.setDomain("www.tianmasport.com");
		cookies.setPath("/ms");
		cookieStore.addCookie(cookies);
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

		List<NameValuePair> form = new ArrayList<NameValuePair>();
		form.add(new BasicNameValuePair("nickName", nickName));
		form.add(new BasicNameValuePair("pwd", pwd));
		form.add(new BasicNameValuePair("verifyCode", vcode));
		form.add(new BasicNameValuePair("remember", remember));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, "UTF-8");
		HttpPost login = new HttpPost("http://www.tianmasport.com/ms/beLogin.do");
		login.setEntity(entity);
		login.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36");
		login.setHeader("Referer", "http://www.tianmasport.com/ms/login.shtml");
		CloseableHttpResponse loginResponse = httpclient.execute(login);
		String loginInfo = EntityUtils.toString(loginResponse.getEntity());

		// 进入主页，不然无权限
		HttpGet main = new HttpGet("http://www.tianmasport.com/ms/main.shtml");
		CloseableHttpResponse mainResponse = httpclient.execute(main);
		String htmlText = EntityUtils.toString(mainResponse.getEntity());

		// 爬去快速下单页面内容
		List<NameValuePair> searchForm = new ArrayList<NameValuePair>();
		searchForm.add(new BasicNameValuePair("articleno", articleno));
		UrlEncodedFormEntity articlenoEntity = new UrlEncodedFormEntity(searchForm);

		HttpPost quick = new HttpPost("http://www.tianmasport.com/ms/order/searchByArticleno.do");
		quick.setEntity(articlenoEntity);
		CloseableHttpResponse quickResponse = httpclient.execute(quick);
		String quickInfo = EntityUtils.toString(quickResponse.getEntity());

		System.out.println(loginInfo.indexOf("验证码输入错误"));
		if (loginInfo.indexOf("false") >= 0) {
			httpclient.close();
			return sendRequestAndGetResponse(nickName, pwd, remember, articleno);
		}
		httpclient.close();
		return quickInfo;
	}
}
