package com.insigma.http;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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
	private  String appkey="faaaac26-8f96-11e7-bb31-be2e44b06b34";
	private  ContentType  contentType;
	
	public HttpRequestUtils( ContentType  contentType){
		jsonConfig=new JsonConfig();
		jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		this.contentType=contentType;
	}
	
	public HttpRequestUtils(){
		jsonConfig=new JsonConfig();
		jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		this.contentType=ContentType.JSON;
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
    	CloseableHttpClient httpClient = HttpClients.createDefault();
    	CloseableHttpResponse response =null;
        JSONObject jsonResult = null;
        String str = "";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("appkey", appkey);
        try {
            if (null != t) {
            	if(contentType.equals(ContentType.X_WWW_FORM_URLENCODED)){
            		//���������������
                    StringEntity entity = new StringEntity(parseURLPair(t), "utf-8");
                    entity.setContentEncoding("UTF-8");
                    entity.setContentType("application/x-www-form-urlencoded");
                    httppost.setEntity(entity);
            	}else if(contentType.equals(ContentType.JSON)){
            		//���������������
                    StringEntity entity = new StringEntity(toJsonObject(t).toString(), "utf-8");
                    entity.setContentEncoding("UTF-8");
                    entity.setContentType("application/json");
                    httppost.setEntity(entity);
                   }
            }
            /**���ؽ��*/
            response = httpClient.execute(httppost);
            url = URLDecoder.decode(url, "UTF-8");
            /**�����ͳɹ������õ���Ӧ**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    /**��ȡ���������ع�����json�ַ�������**/
                    str = EntityUtils.toString(response.getEntity());
                    /**��json�ַ���ת����json����**/
                    jsonResult = JSONObject.fromObject(str);
                    /**�Ƿ�ɹ�*/
                    String success= jsonResult.getString("success");
                    if(success.equals("true")){
                    	 log.info("���ýӿ�"+url+"�ɹ�");
                    }else{
                    	 log.info("���ýӿ�"+url+"ҵ��ʧ��,"+jsonResult.getString("message"));
                    	 throw new AppException(jsonResult.getString("message"));
                    }
            }else{
            	 log.error("post����"+url+"����ʧ��:" +response.getStatusLine().getStatusCode() );
            	 throw new AppException("post����"+url+"ʧ��:" +response);
            }
        } catch (Exception e) {
            log.error(e);
            throw new AppException(e);
        }finally {
            if(response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(httpClient != null){
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
    	CloseableHttpClient httpClient =null;
    	CloseableHttpResponse response =null;
    	JSONObject jsonResult = null;
        String strResult="";
        try {
        	 httpClient = HttpClients.createDefault();
        	 response =null;
            //����get����
            HttpGet request = new HttpGet(url);
            request.setHeader("appkey", appkey);
            response = httpClient.execute(request);
 
            /**�����ͳɹ������õ���Ӧ**/
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                /**��ȡ���������ع�����json�ַ�������**/
                 strResult = EntityUtils.toString(response.getEntity());
                /**��json�ַ���ת����json����**/
                jsonResult = JSONObject.fromObject(strResult);
                /**�Ƿ�ɹ�*/
                String success= jsonResult.getString("success");
                url = URLDecoder.decode(url, "UTF-8");
                if(success.equals("true")){
                	log.info("���ýӿ�"+url+"�ɹ�");
                }else{
                	log.info("���ýӿ�"+url+"ҵ��ʧ��,"+jsonResult.getString("message"));
                	throw new AppException(jsonResult.getString("message"));
                }
            } else {
                  log.error("get����"+url+"ʧ��:" + url+response.getStatusLine().getStatusCode());
                  throw new AppException("get����"+url+"ʧ�� "+response);
            }
        } catch (IOException e) {
            throw new AppException(e);
        }finally {
            if(response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(httpClient != null){
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonResult;
    }
    


    
    /**
     * ����ת����url k=v��ʽ
     * @param o
     * @return
     * @throws Exception
     */
    public  String parseURLPair(T t) throws Exception{  
    	    Class<? extends Object>c = t.getClass();
	        Field[] fields = c.getDeclaredFields();  
	        Map<String, Object> map = new TreeMap<String, Object>();  
	        for (Field field : fields) {  
	            field.setAccessible(true);  
	            String name = field.getName();  
	            Object value = field.get(t);  
	            if(value != null){
	            	if(field.getType().equals(Date.class)){
	            		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);    
	            		 map.put(name, sdf.format(value)); 
	            	}else{
	            		map.put(name, value); 
	            	}
	            }
	                 
	        }  
	        Set<Entry<String, Object>> set = map.entrySet();  
	        Iterator<Entry<String, Object>> it = set.iterator();  
	        StringBuffer sb = new StringBuffer("");  
	        while (it.hasNext()) {  
	             Entry<String, Object> e = it.next();  
	             sb.append(e.getKey()).append("=").append(e.getValue()).append("&");  
	        }  
	        System.out.println(sb.toString()+"length="+sb.length());
	        if(sb.length()>0){
	        	 System.out.println(sb.toString());
	        	 return sb.deleteCharAt(sb.length()-1).toString();  
	        }else{
	        	return "";
	        }
    } 
}
