package com.samulle.plugin.update.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

/**
 * 操作文件的工具类
 * @author Dell
 *
 */
public class FileUtil {
	/**
	 * 手机存储路径
	 */
	private String SDPath;
	
	public String getSDPath(){
		return SDPath;
	}
	public FileUtil(){
		//获得手机外部存储路径
		//SDPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/";
		SDPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
	}
	/**
	 * 在SD卡上新建文件
	 * @param fileName 文件名
	 * @return
	 * @throws IOException
	 */
	public File createSDFile(String fileName) throws IOException {
		File file = new File(SDPath + fileName);
		file.createNewFile();
		return file;
	}
	/**
	 * 在SD卡上创建目录
	 * @param dirName 目录名
	 * @return
	 */
	public File createSDDir(String dirName){
		File dir = new File(SDPath + dirName);
		dir.mkdirs();
		System.out.println("---创建目录=》"+dir);
		return dir;
	}
	/**
	 * 判断文件在SD卡上是否存在
	 * @param fileName 文件名
	 * @return
	 */
	public boolean isFileExist(String fileName){
		File file = new File(SDPath + fileName);
		return file.exists();
	}
	/**
	 * 将input流里面的数据写到指定位置
	 * @param path 目标位置
	 * @param fileName 文件名
	 * @param input 输入流
	 * @return
	 */
	public File write2SDFromInput(String path, String fileName, InputStream input) {
		File file = null;
		OutputStream out = null;
		System.out.println("-----SD目录=》"+SDPath);
		try {
			//创建目录
			createSDDir(path);
			file = createSDFile(path + fileName);
			out = new FileOutputStream(file);
			byte[] buffer = new byte[4*1024];
			int temp;
			while((temp = input.read(buffer)) != -1){
				out.write(buffer,0,temp);
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}
	/**
	 * 将input流里面的数据写到指定位置
	 * @param path 目标位置
	 * @param fileName 文件名
	 * @param input 输入流
	 * @return
	 */
	public File write2SDFromInput(String path, String fileName, byte[] data) {
		File file = null;
		OutputStream out = null;
		System.out.println("-----SD目录=》"+SDPath);
		try {
			//创建目录
			createSDDir(path);
			file = createSDFile(path + fileName);
			out = new FileOutputStream(file);
			out.write(data);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}
}
