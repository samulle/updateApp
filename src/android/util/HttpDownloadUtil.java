package com.samulle.plugin.update.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.os.Environment;
import android.util.Log;



/**
 * 下载的工具类
 * @author Dell
 *
 */
public class HttpDownloadUtil {
	private URL url = null;
	/**
	 * 根据URL获得文本内容的数据
	 * @param url URL地址
	 * @return 返回URL的文本数据
	 */
	public String getURLDate(String url) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(
					new InputStreamReader(getInputStreamFromUrl(url)
							)
					);
			while((line = br.readLine()) != null){
				sb.append(line);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	/**
	 * 根据URL下载文件，返回整形
	 * -1：代表下载文件出错 0：代表下载文件成功 1：代表文件已经存在
	 * @param urlStr URL地址
	 * @param path 下载指定的目录
	 * @param fileName 保存文件名
	 * @return
	 */
	public int downFile(String urlStr, String path, String fileName){
		InputStream in = null;
		try {
			FileUtil fileUtil = new FileUtil();
			if(fileUtil.isFileExist(path + fileName)){
				//该文件已存在
				return 1;
			}
			else {
				in = getInputStreamFromUrl(urlStr);
				File file = fileUtil.write2SDFromInput(path, fileName, in);
				if(file == null){
					return -1;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}
	
	public String downFileForPath(String urlStr, String path, String fileName){
		InputStream in = null;
		try {
			FileUtil fileUtil = new FileUtil();
//			if(fileUtil.isFileExist(path + fileName)){
//				//该文件已存在
//				return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+path + fileName;
//			}
//			else {
				in = getInputStreamFromUrl(urlStr);
				File file = fileUtil.write2SDFromInput(path, fileName, in);
				if(file == null){
					return null;
				}
				return file.getPath();
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 根据URL获得输入流
	 * @param urlStr URL地址
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStreamFromUrl(String urlStr) throws IOException{
		//创建URL对象
		url = new URL(urlStr);
		//获得httpURL连接
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
	    con.setRequestMethod("GET");
	    con.setRequestProperty("contentType", "utf-8");
	    con.setConnectTimeout(5 * 1000); 
		//根据httpURL连接获得输入流
		InputStream input = con.getInputStream();
		return input;
	}
	
	/**
	 * 根据URL获得输入流
	 * @param urlStr urlStr URL地址
	 * @param param 参数
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStreamFromUrl(String urlStr, String param) throws IOException{
//		System.out.println("=========param:"+param);
		//创建URL对象
		url = new URL(urlStr);
		//获得httpURL连接
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		//设置参数
		con.setDoOutput(true);   //需要输出
		con.setDoInput(true);   //需要输入
		con.setUseCaches(false);  //不允许缓存
		con.setRequestMethod("POST");   //设置POST方式连接
		//设置请求属性
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
		con.setRequestProperty("Charset", "UTF-8");
		//建立输入流，向指向的URL传入参数
	    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
	    dos.writeBytes(param);
	    dos.flush();
	    /*//获得响应状态
	    int resultCode = con.getResponseCode();
	    if(HttpURLConnection.HTTP_OK==resultCode){
	        StringBuffer sb=new StringBuffer();
	        String readLine=new String();
	        BufferedReader responseReader=new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
	        while((readLine=responseReader.readLine())!=null){
	          sb.append(readLine).append("\n");
	        }
	        responseReader.close();
	        System.out.println(sb.toString());
	      } */
		//根据httpURL连接获得输入流
		InputStream input = con.getInputStream();
		dos.close();
		return input;
	}

}
