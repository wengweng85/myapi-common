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
 * httprequest������ 
 * @author wengsh
 *
 * @param <T>
 */
public class HttpRequestUtils<T> {
	
	
	private  Log log = LogFactory.getLog(HttpRequestUtils.class);    //��־��¼
	 
	public   JsonConfig jsonConfig;
	private  String appkey;
	
	public HttpRequestUtils(String appkey){
		jsonConfig=new JsonConfig();
		jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		this.appkey=appkey;
	}
    
    /**
     * ����ת����jsonobject
     * @param t
     * @return
     */
    private   JSONObject toJsonObject(T t){
		JSONObject jsonParam=JSONObject.fromObject(t,jsonConfig);
		return jsonParam;
	}
    
    /**
     * jsonobjectת���ɶ���
     * @param t
     * @return
     */
    private  T tobean(JSONObject jsonobject,T t){
		return (T)JSONObject.toBean(jsonobject, t.getClass());
	}
    
    /**
     * jsonobjectת���ɶ�������
     * @param t
     * @return
     */
	public   List<T> toList(JSONArray jsonarray,T t){
    	return (List<T>)JSONArray.toList(jsonarray,t.getClass());
	}
	
	 /**
     * jsonobjectת���ɶ�������
     * @param t
     * @return
     */
	public   List<T> toList(JSONArray jsonarray,Class c){
    	return (List<T>)JSONArray.toList(jsonarray,c);
	}

    
    
	 /**
     * ����get���� ����json����
     * @param url    ·��
     * @return
     */
    private JSONArray httpGetReturnArray(String url) throws AppException{
    	return httpGet(url).getJSONArray("obj");
    }
	
	   /**
     * ����get���� ����json����
     * @param url    ·��
     * @param t  �������ݶ���
     * @return
     */
    private  JSONArray httpPostReturnArray(String url,T t) throws AppException{
    	return httpPost(url,t).getJSONArray("obj");
    }
   
    
    /**
     * ����get���� ����json����
     * @param url    ·��
     * @param t  �������ݶ���
     * @return
     */
    public  JSONObject httpPostReturnObject(String url,T t) throws AppException{
        return httpPost(url,t).getJSONObject("obj");
    }
    
    
    /**
     * ���ض���list
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
     * ����get���� ����json����
     * @param url    ·��
     * @return
     */
    public  JSONObject httpGetReturnObject(String url) throws AppException{
        return httpGet(url).getJSONObject("obj");
    }
    
 
    
    /**
     * ���ض���list
     * @param url
     * @param beanclass
     * @return
     * @throws AppException
     */
    public  List<T> httpGetReturnList(String url,Class c) throws AppException{
    	return toList(httpGetReturnArray(url),c);
    }
    
    
	/**
	 *  post����
	 * @param url ��ַ
	 * @param t ����
	 * @param contentType �������� json����x-www-form
	 * @return
	 * @throws AppException
	 */
    public JSONObject httpPost(String url,T t) throws AppException{
        JSONObject jsonResult = null;
        try {
        	 HttpResult httpresult=HttpHelper.executePost(url, t, appkey);
			 if (httpresult.getStatusCode()== HttpStatus.SC_OK) {
				 jsonResult = JSONObject.fromObject(httpresult.getContent());
				  /**�Ƿ�ɹ�*/
	            String success= jsonResult.getString("success");
				  if(success.equals("true")){
	             	log.info("���ýӿ�"+url+"�ɹ�");
	             }else{
	             	log.info("���ýӿ�"+url+"ҵ��ʧ��,"+jsonResult.getString("message"));
	             	throw new AppException(jsonResult.getString("message"));
	             }
			 }else{
				 log.error("get����"+url+"ʧ��,������:" + url+httpresult.getStatusCode());
	            throw new AppException("get����"+url+"ʧ�� "+httpresult.getContent());
			 }
        } catch (Exception e) {
            log.error(e);
            throw new AppException(e);
        }
        return jsonResult;
    }
    
   /**
    * ����get����
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
    			  /**�Ƿ�ɹ�*/
                 String success= jsonResult.getString("success");
    			  if(success.equals("true")){
                  	log.info("���ýӿ�"+url+"�ɹ�");
                  }else{
                  	log.info("���ýӿ�"+url+"ҵ��ʧ��,"+jsonResult.getString("message"));
                  	throw new AppException(jsonResult.getString("message"));
                  }
    		 }else{
    			 log.error("get����"+url+"ʧ��,������:" +httpresult.getStatusCode());
                 throw new AppException("get����"+url+"ʧ�� "+httpresult.getContent());
    		 }
    	 }catch (IOException e) {
            throw new AppException(e);
        }
        return jsonResult;
    }
    
    
    /**
     * ����get����
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
     			  /**�Ƿ�ɹ�*/
                  String success= jsonResult.getString("success");
     			  if(success.equals("true")){
                   	log.info("�����ļ��ϴ��ӿ�"+url+"�ɹ�");
                   }else{
                   	log.info("�����ļ��ϴ��ӿ�"+url+"ҵ��ʧ��,"+jsonResult.getString("message"));
                   	throw new AppException(jsonResult.getString("message"));
                   }
     		 }else{
     			 log.error("�ļ��ϴ�����"+url+"ʧ��:" + url+httpresult.getStatusCode());
                  throw new AppException("�ļ��ϴ�����"+url+"ʧ�� "+httpresult.getContent());
     		 }
     	 }catch (IOException e) {
             throw new AppException(e);
         }
         return jsonResult;
     }
     
     
     
     /**
      * �ļ�����
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
