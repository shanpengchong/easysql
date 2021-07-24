package cn.easyutil.easySql.config;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.druid.pool.DruidDataSource;
import cn.easyutil.easySql.util.LoggerUtil;
import cn.easyutil.easySql.util.YamlUitl;

public class EasySqlConfiguration {
	public static DataSource dataSource;
	private  static Map<String, String> propers = new HashMap<String, String>();
	/** 多数据源存储*/
	private static ConcurrentHashMap<String, DataSource> dataSourceMap = new ConcurrentHashMap<String, DataSource>();
	public static String url;
	public static String username;
	public static String password;
	
	public static DataSource getDataSourceMapValue(String flag){
		return dataSourceMap.get(flag);
	}
	public static void setDataSourceMapValue(String flag,DataSource dataSource){
		if(dataSource == null){
			dataSourceMap.put(flag, new EasySqlConfiguration().dataSource(null));
		}else{
			dataSourceMap.put(flag, dataSource);
		}
	}
	public  DruidDataSource dataSource(String propertiesName) {
		if(url!=null && username!=null && password!=null){
			propers.put("url", url);
			propers.put("username", username);
			propers.put("password", password);
		}else{
			if(propertiesName==null || propertiesName.length()==0){
//				listFile(new File(EasySqlConfiguration.class.getResource("/").getPath()));
				loadResource();
			}else if(propertiesName.endsWith("yaml")){
				loadYaml(propertiesName);
			}else if(propertiesName.endsWith("properties")){
				loadPropertites(propertiesName);
			}
		}
		if(propers.get("url")==null || propers.get("username")==null || propers.get("password")==null){
			throw new RuntimeException("JDBC config not found in "+propertiesName);
		}
		url = propers.get("url");
		username = propers.get("username");
		password = propers.get("password");
		LoggerUtil.info(this.getClass(),"读取到配置文件的username=" + propers.get("username"));
		LoggerUtil.info(this.getClass(),"读取到配置文件的password=" + propers.get("password"));
		LoggerUtil.info(this.getClass(),"读取到配置文件的url=" + propers.get("url"));
		
		
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl(propers.get("url"));
		// 用户名
		dataSource.setUsername(propers.get("username"));
		// 密码
		dataSource.setPassword(propers.get("password"));
		dataSource.setInitialSize(2);
		dataSource.setMaxActive(20);
		dataSource.setMinIdle(0);
		dataSource.setMaxWait(60000);
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setTestOnBorrow(false);
		dataSource.setTestWhileIdle(true);
		dataSource.setPoolPreparedStatements(false);
		//设置超时回收时间
		dataSource.setRemoveAbandoned(true);
		dataSource.setRemoveAbandonedTimeout(1800);
		Properties properties = dataSource.getConnectProperties();
		properties.put("autoReconnect", "true");
		dataSource.setConnectProperties(properties);
		return dataSource;
	}
	
	/**
	 * 检索有可能是配置文件的文件
	 */
	private void loadResource() {
		List<String> resourceName = new ArrayList<String>();
		resourceName.add("/application.properties");
		resourceName.add("/application.yaml");
		for (String name : resourceName) {
			if(name.endsWith("properties")){
				try {
					loadPropertites(name);
				} catch (Exception e) {}
			}else{
				try {
					loadYaml(name);
				} catch (Exception e) {}
			}
		}
		
	}
	private void loadYaml(String yamlPath){
		YamlUitl util = new YamlUitl();
		try {
			Map<String, Object> map = util.readYaml(EasySqlConfiguration.class.getResourceAsStream(yamlPath));
			Set<Entry<String, Object>> set = map.entrySet();
			for (Entry<String, Object> entry : set) {
				hasMySqlConfig(entry.getKey(), entry.getValue().toString());
			}
		} catch (Exception e) {}
	}
	
	
	private void loadPropertites(String propertiesPath){
		Properties properties = new Properties();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(EasySqlConfiguration.class.getResourceAsStream(propertiesPath)));
			properties.load(bufferedReader);
			Iterator<Entry<Object, Object>> iterator = properties.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Object, Object> next = iterator.next();
				String key = next.getKey().toString();
				hasMySqlConfig(key, next.getValue().toString());
			}
		} catch (Exception e) {} finally {
			if(bufferedReader != null) {
				try {
					bufferedReader.close();
					return;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
	}
	
	public  void listFile(File file) {
		if(propers.get("url")!=null && propers.get("username")!=null && propers.get("password")!=null){
			return;
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				try {
					if (f.isFile() && f.getName().endsWith(".properties")) {
						loadPropertites(f.getAbsolutePath());
					}else if(f.isFile() && f.getName().endsWith(".yaml")){
						loadYaml(f.getAbsolutePath());
					}
				} catch (Exception e) {}
				if (f.isDirectory()) {
					listFile(f);
				}
			}
		}
	}
	
	private  void hasMySqlConfig(String key,String value){
		key = key.toUpperCase();
		// 获取username
		if (key.contains("MYSQL") && key.contains("USER") && key.contains("NAME")) {
			propers.put("username", value);
		} else if (key.contains("DATASOURCE") && key.contains("USER") && key.contains("NAME")) {
			propers.put("username", value);
		} else if (key.contains("JDBC") && key.contains("USER") && key.contains("NAME")) {
			propers.put("username", value);
		}
		// 获取password
		if (key.contains("MYSQL") && (key.contains("PASSWORD") || key.contains("PWD"))) {
			propers.put("password", value);
		} else if (key.contains("DATASOURCE")
				&& (key.contains("PASSWORD") || key.contains("PWD"))) {
			propers.put("password", value);
		} else if (key.contains("JDBC") && (key.contains("PASSWORD") || key.contains("PWD"))) {
			propers.put("password", value);
		}
		// 获取url
		if (key.contains("MYSQL") && key.contains("URL")) {
			propers.put("url", value);
		} else if (key.contains("DATASOURCE") && key.contains("URL")) {
			propers.put("url", value);
		} else if (key.contains("JDBC") && key.contains("URL")) {
			propers.put("url", value);
		}
		propers.put("driver", "mysql");
		// 获取驱动类型
		if(key.contains("DRIVER") && key.contains("MYSQL")){
			propers.put("driver", "mysql");
		}
		if(key.contains("DRIVER") && key.contains("ORACLE")){
			propers.put("driver", "oracle");
		}
		if(propers.get("url")!=null && propers.get("username")!=null && propers.get("password")!=null){
			return;
		}
	}
}
