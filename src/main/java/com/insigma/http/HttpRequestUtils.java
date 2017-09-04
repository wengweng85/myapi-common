package com.insigma.http;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.insigma.json.JsonDateValueProcessor;
import com.insigma.resolver.AppException;


/**
 * httprequest工具类 
 * @author wengsh
 *
 * @param <T>
 */
public class HttpRequestUtils<T> {
	
	
	private  Log log = LogFactory.getLog(HttpRequestUtils.class);    //日志记录
	 
	public   JsonConfig jsonConfig;
	private  String appkey;
	
	public HttpRequestUtils(String appkey){
		jsonConfig=new JsonConfig();
		jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		this.appkey=appkey;
	}
    
    /**
     * 对象转换成jsonobject
     * @param t
     * @return
     */
    private   JSONObject toJsonObject(T t){
		JSONObject jsonParam=JSONObject.fromObject(t,jsonConfig);
		return jsonParam;
	}
    
    /**
     * jsonobject转换成对象
     * @param t
     * @return
     */
    private  T tobean(JSONObject jsonobject,T t){
		return (T)JSONObject.toBean(jsonobject, t.getClass());
	}
    
    /**
     * jsonobject转换成对象数组
     * @param t
     * @return
     */
	public   List<T> toList(JSONArray jsonarray,T t){
    	return (List<T>)JSONArray.toList(jsonarray,t.getClass());
	}
	
	 /**
     * jsonobject转换成对象数组
     * @param t
     * @return
     */
	public   List<T> toList(JSONArray jsonarray,Class c){
    	return (List<T>)JSONArray.toList(jsonarray,c);
	}

    
    
	 /**
     * 发送get请求 返回json数组
     * @param url    路径
     * @return
     */
    private JSONArray httpGetReturnArray(String url) throws AppException{
    	return httpGet(url).getJSONArray("obj");
    }
	
	   /**
     * 发送get请求 返回json数组
     * @param url    路径
     * @param t  请求数据对象
     * @return
     */
    private  JSONArray httpPostReturnArray(String url,T t) throws AppException{
    	return httpPost(url,t).getJSONArray("obj");
    }
   
    
    /**
     * 发送get请求 返回json对象
     * @param url    路径
     * @param t  请求数据对象
     * @return
     */
    public  JSONObject httpPostReturnObject(String url,T t) throws AppException{
        return httpPost(url,t).getJSONObject("obj");
    }
    
    
    /**
     * 返回对象list
     * @param url
     * @param jsonParam
     * @param beanclass
     * @return
     * @throws AppException
     */
    public  List<T> httpPostReturnList(String url,T t) throws AppException{
    	return toList(httpPostReturnArray(url,t),t);
    }
    
 
    /**
     * 发送get请求 返回json对象
     * @param url    路径
     * @return
     */
    public  JSONObject httpGetReturnObject(String url) throws AppException{
        return httpGet(url).getJSONObject("obj");
    }
    
 
    
    /**
     * 返回对象list
     * @param url
     * @param beanclass
     * @return
     * @throws AppException
     */
    public  List<T> httpGetReturnList(String url,Class c) throws AppException{
    	return toList(httpGetReturnArray(url),c);
    }
    
    
	/**
	 *  post请求
	 * @param url 地址
	 * @param t 对象
	 * @param contentType 请求类型 json或者x-www-form
	 * @return
	 * @throws AppException
	 */
    public JSONObject httpPost(String url,T t) throws AppException{
        JSONObject jsonResult = null;
        try {
        	 HttpResult httpresult=HttpHelper.executePost(url, t, appkey);
			 if (httpresult.getStatusCode()== HttpStatus.SC_OK) {
				 jsonResult = JSONObject.fromObject(httpresult.getContent());
				  /**是否成功*/
	            String success= jsonResult.getString("success");
				  if(success.equals("true")){
	             	log.info("调用接口"+url+"成功");
	             }else{
	             	log.info("调用接口"+url+"业务失败,"+jsonResult.getString("message"));
	             	throw new AppException(jsonResult.getString("message"));
	             }
			 }else{
				 log.error("get请求"+url+"失败,错误码:" + url+httpresult.getStatusCode());
	            throw new AppException("get请求"+url+"失败 "+httpresult.getContent());
			 }
        } catch (Exception e) {
            log.error(e);
            throw new AppException(e);
        }
        return jsonResult;
    }
    
   /**
    * 发送get请求
    * @param url
    * @return
    * @throws AppException
    */
    public  JSONObject httpGet(String url) throws AppException{
    	JSONObject jsonResult = null;
    	 try {
    		 HttpResult httpresult= HttpHelper.executeGet(url, appkey);
    		 if (httpresult.getStatusCode()== HttpStatus.SC_OK) {
    			 jsonResult = JSONObject.fromObject(httpresult.getContent());
    			  /**是否成功*/
                 String success= jsonResult.getString("success");
    			  if(success.equals("true")){
                  	log.info("调用接口"+url+"成功");
                  }else{
                  	log.info("调用接口"+url+"业务失败,"+jsonResult.getString("message"));
                  	throw new AppException(jsonResult.getString("message"));
                  }
    		 }else{
    			 log.error("get请求"+url+"失败,错误码:" +httpresult.getStatusCode());
                 throw new AppException("get请求"+url+"失败 "+httpresult.getContent());
    		 }
    	 }catch (IOException e) {
            throw new AppException(e);
        }
        return jsonResult;
    }
    
    
    /**
     * 发送get请求
     * @param url
     * @return
     * @throws AppException
     */
     public  JSONObject httpUploadFile(String url,File file,String file_bus_type) throws AppException{
     	JSONObject jsonResult = null;
     	 try {
     		 HttpResult httpresult= HttpHelper.executeUploadFile(url, appkey, file, file_bus_type);
     		 if (httpresult.getStatusCode()== HttpStatus.SC_OK) {
     			  jsonResult = JSONObject.fromObject(httpresult.getContent());
     			  /**是否成功*/
                  String success= jsonResult.getString("success");
     			  if(success.equals("true")){
                   	log.info("调用文件上传接口"+url+"成功");
                   }else{
                   	log.info("调用文件上传接口"+url+"业务失败,"+jsonResult.getString("message"));
                   	throw new AppException(jsonResult.getString("message"));
                   }
     		 }else{
     			 log.error("文件上传请求"+url+"失败:" + url+httpresult.getStatusCode());
                  throw new AppException("文件上传请求"+url+"失败 "+httpresult.getContent());
     		 }
     	 }catch (IOException e) {
             throw new AppException(e);
         }
         return jsonResult;
     }
     
     
     
     /**
      * 文件下载
      * @param url
      * @return
      * @throws AppException
      */
      public  File httpDownLoadFile(String url,String localdir) throws AppException{
    	  File  file=null;
      	 try {
      		  file= HttpHelper.executeDownloadFile(url, appkey, localdir);
      	 }catch (IOException e) {
              throw new AppException(e);
          }
          return file;
      }
}
