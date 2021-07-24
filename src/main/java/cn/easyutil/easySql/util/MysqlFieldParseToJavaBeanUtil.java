package cn.easyutil.easySql.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.easyutil.easySql.EasySqlExecution;

/**
 * example1:mysql表生成java文件
 * 		MysqlFieldParseToJavaBeanUtil.queryFields("user", "jdbc-url","jdbc-username","jdbc-password")
 * 				.buildYifeiExample("com.yifei.example")
 * 				.build(true);
 * 
 * example2:将mysql表对应的java文件删除
 * 		MysqlFieldParseToJavaBeanUtil.queryFields("user", "jdbc-url","jdbc-username","jdbc-password")
 * 				.buildYifeiExample("com.yifei.example")
 * 				.unBuild();
 * 
 * @author spc
 *
 */
public class MysqlFieldParseToJavaBeanUtil{
	/** 数据库字段名称*/
	private String Field;
	/** 数据库字段类型*/
	private String Type;
	/** 数据库字段注释*/
	private String Comment;
	/** 是否添加swagger注解*/
	private boolean addSwaggerAnnotation = false;
	/** 是否转换为驼峰格式*/
	private boolean formatToHump = false;
	/** 是否添加java注释*/
	private boolean addComment = false;
	private String baseName;
	/** bean所在的包路径*/
	private String beanPackage;
	private String beanPath;
	/** javaBean名称*/
	private String beanName;
	/** mapper所在的包路径*/
	private String mapperPackage;
	private String mapperPath;
	/** mapper名称*/
	private String mapperName;
	private String xmlPath;
	/** xml名称*/
	private String xmlName;
	/** service所在的包路径*/
	private String servicePackage;
	private String servicePath;
	/** service名称*/
	private String serviceName;
	/** impl所在的包路径*/
	private String serviceImplPackage;
	private String serviceImplPath;
	/** impl名称*/
	private String serviceImplName;
	/** 是否使用驿飞的core*/
	private boolean useYifeiCore = false;
	/** 不需要生成的javabean的字段*/
	private List<String> ignoreFields = new ArrayList<String>();
	/** 执行sql脚本的执行器*/
	private EasySqlExecution easySqlExecution;
	/** 数据库字段查询结果*/
	private List<MysqlFieldParseToJavaBeanUtil> mysqlFields;
	/** 自定义注解集合*/
	private Map<String, List<AnnotationBuild>> costomAnnotations = new HashMap<String, List<AnnotationBuild>>();

	public static MysqlFieldParseToJavaBeanUtil queryFields(String tableName,String url, String username, String password){
		MysqlFieldParseToJavaBeanUtil util = new MysqlFieldParseToJavaBeanUtil();
		util.easySqlExecution = new EasySqlExecution(url,username,password);
		return queryFields(tableName, util.easySqlExecution);
	}
	
	public static MysqlFieldParseToJavaBeanUtil queryFields(String tableName,Connection connection){
		MysqlFieldParseToJavaBeanUtil util = new MysqlFieldParseToJavaBeanUtil();
		util.easySqlExecution = new EasySqlExecution(connection);
		return queryFields(tableName, util.easySqlExecution);
	}
	/**
	 * 执行数据库表查询
	 * @param tableName	数据库表名称
	 * @return	该表的所有字段
	 */
	public static MysqlFieldParseToJavaBeanUtil queryFields(String tableName,EasySqlExecution easySqlExecution){
		MysqlFieldParseToJavaBeanUtil util = new MysqlFieldParseToJavaBeanUtil();
		util.easySqlExecution = easySqlExecution;
		util.mysqlFields = util.easySqlExecution.customQuery("show full fields from "+tableName,MysqlFieldParseToJavaBeanUtil.class);
		if(util.mysqlFields==null || util.mysqlFields.isEmpty()){
			throw new RuntimeException("数据库表:"+tableName+" 无字段");
		}
		util.baseName = util.conversionCamelCase(tableName);
		String first = new String(new char[]{util.baseName.charAt(0)}).toUpperCase();
		util.baseName = first+util.baseName.substring(1);
		return util;
	}
	public String getField() {
		return Field;
	}
	public void setField(String field) {
		Field = field;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getComment() {
		return Comment;
	}
	public void setComment(String comment) {
		Comment = comment;
	}
	
	/**
	 * 字段上添加自定义注解
	 * @param annotation	注解名称
	 * @param annotationField	注解内部字段
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil addCostomAnnotation(String annotation,String annotationField){
		return addCostomAnnotation(annotation, AnnotationBuild.build(annotationField, TableFieldDetail.TABLE_FIELD_NAME));
	}
	
	/**
	 * 添加自定义注解
	 * @param annotation	注解名字
	 * @param build	注解字段
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil addCostomAnnotation(String annotation,AnnotationBuild...build){
		if(costomAnnotations.get(annotation) == null){
			costomAnnotations.put(annotation, new ArrayList<MysqlFieldParseToJavaBeanUtil.AnnotationBuild>());
		}
		if(build.length == 0){
			build = new AnnotationBuild[]{AnnotationBuild.build("value", TableFieldDetail.TABLE_FIELD_NAME)};
		}
		for (AnnotationBuild bu : build) {
			costomAnnotations.get(annotation).add(AnnotationBuild.build(bu.getAnnotationField(), bu.getValue(),bu.isToUpperCase()));
		}
		return this;
	}
	
	/**
	 * 添加自定义注解
	 * @param annotation	注解名字
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil addCostomAnnotation(String annotation){
		return addCostomAnnotation(annotation, "value");
	}
	/**
	 * 添加swagger注解
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil addSwaggerAnnotation(){
		addSwaggerAnnotation = true;
		return this;
	}
	
	/**
	 * 添加注释文档
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil addComment(){
		addComment = true;
		return this;
	}
	
	/**
	 * 被忽略的字段
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil addIgnore(String... fieldName){
		if(fieldName.length > 0){
			ignoreFields.addAll(Arrays.asList(fieldName));
		}
		return this;
	}
	
	/**
	 * 转换成驼峰格式
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil formatToHump(){
		formatToHump = true;
		return this;
	}
	
	/**
	 * 构建java实体
	 * @param beanPath	包路径(计算机全路径)
	 * @param beanPackage	包名
	 * @param beanName	类名
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildJavaBean(String beanPath,String beanPackage,String beanName){
		this.beanPackage = beanPackage;
		this.beanName = beanName;
		this.beanPath = beanPath;
		return this;
	}
	
	/**
	 * 根据全类名构建实体(包名+类名)
	 * @param classFullName	全类名:example(com.yifei.core.bean.UserBean)
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildJavaBean(String classFullName){
		if(!classFullName.contains(".")){
			throw new RuntimeException("请传入包名+类名");
		}
		String className = classFullName.substring(classFullName.lastIndexOf(".")+1);
		String packageName = classFullName.substring(0, classFullName.lastIndexOf("."));
		String packagePath = "";
		try {
			File file = new File("");
			String filePath = file.getCanonicalPath()+File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
			packagePath = filePath+packageName.replace(".", File.separator);
		} catch (IOException e) {}
		return buildJavaBean(packagePath,packageName, className);
	}
	
	/**
	 * 构建mapper实体
	 * @param mapperPath	包路径(计算机全路径)
	 * @param mapperPackage	包名
	 * @param mapperName	类名
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildMapperBean(String mapperPath,String mapperPackage,String mapperName){
		this.mapperPackage = mapperPackage;
		this.mapperName = mapperName;
		this.mapperPath = mapperPath;
		return this;
	}
	
	/**
	 * 根据全类名构建实体(包名+类名)
	 * @param classFullName	全类名:example(com.yifei.core.bean.UserBean)
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildMapperBean(String classFullName){
		if(!classFullName.contains(".")){
			throw new RuntimeException("请传入包名+类名");
		}
		String className = classFullName.substring(classFullName.lastIndexOf(".")+1);
		String packageName = classFullName.substring(0, classFullName.lastIndexOf("."));
		String packagePath = "";
		try {
			File file = new File("");
			String filePath = file.getCanonicalPath()+File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
			packagePath = filePath+packageName.replace(".", File.separator);
		} catch (IOException e) {}
		return buildMapperBean(packagePath,packageName, className);
	}
	
	
	/**
	 * 构建xml实体
	 * @param xmlPath	包路径(计算机全路径)
	 * @param xmlName	xml名称
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildMapperXml(String xmlPath,String xmlName){
		this.xmlName = xmlName;
		this.xmlPath = xmlPath;
		return this;
	}
	
	/**
	 * 根据全类名构建实体(包名+类名)
	 * @param classFullName	全类名:example(com.yifei.core.bean.UserBean)
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildMapperXml(String classFullName){
		if(!classFullName.contains(".")){
			throw new RuntimeException("请传入包名+类名");
		}
		String className = classFullName.substring(classFullName.lastIndexOf(".")+1);
		String packageName = classFullName.substring(0, classFullName.lastIndexOf("."));
		String packagePath = "";
		try {
			File file = new File("");
			String filePath = file.getCanonicalPath()+File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
			packagePath = filePath+packageName.replace(".", File.separator);
		} catch (IOException e) {}
		return buildMapperXml(packagePath, className);
	}
	
	
	/**
	 * 构建service实体
	 * @param servicePath	包路径(计算机全路径)
	 * @param servicePackage	包名
	 * @param serviceName	类名
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildServiceBean(String servicePath,String servicePackage,String serviceName){
		this.servicePackage = servicePackage;
		this.serviceName = serviceName;
		this.servicePath = servicePath;
		return this;
	}
	
	/**
	 * 根据全类名构建实体(包名+类名)
	 * @param classFullName	全类名:example(com.yifei.core.bean.UserBean)
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildServiceBean(String classFullName){
		if(!classFullName.contains(".")){
			throw new RuntimeException("请传入包名+类名");
		}
		String className = classFullName.substring(classFullName.lastIndexOf(".")+1);
		String packageName = classFullName.substring(0, classFullName.lastIndexOf("."));
		String packagePath = "";
		try {
			File file = new File("");
			String filePath = file.getCanonicalPath()+File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
			packagePath = filePath+packageName.replace(".", File.separator);
		} catch (IOException e) {}
		return buildServiceBean(packagePath, packageName, className);
	}
	
	/**
	 * 构建serviceImpl实体
	 * @param implPath	包路径(计算机全路径)
	 * @param implPackage	包名	
	 * @param implName	类名
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildServiceImplBean(String implPath,String implPackage,String implName){
		this.serviceImplPackage = implPackage;
		this.serviceImplName = implName;
		this.serviceImplPath = implPath;
		return this;
	}
	
	/**
	 * 根据全类名构建实体(包名+类名)
	 * @param classFullName	全类名:example(com.yifei.core.bean.UserBean)
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildServiceImplBean(String classFullName){
		if(!classFullName.contains(".")){
			throw new RuntimeException("请传入包名+类名");
		}
		String className = classFullName.substring(classFullName.lastIndexOf(".")+1);
		String packageName = classFullName.substring(0, classFullName.lastIndexOf("."));
		String packagePath = "";
		try {
			File file = new File("");
			String filePath = file.getCanonicalPath()+File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
			packagePath = filePath+packageName.replace(".", File.separator);
		} catch (IOException e) {}
		return buildServiceImplBean(packagePath, packageName, className);
	}
	
	/**
	 * 构建驿飞样式的工程
	 * @param basePackagePath	父包的全路径,比如D:/workspace/project/src/main/java/com/yifei/project/
	 * @return
	 */
	public MysqlFieldParseToJavaBeanUtil buildYifeiExample(String basePackagePath){
		if(basePackagePath.contains(".")){
			try {
				File file = new File("");
				String filePath = file.getCanonicalPath()+File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
				basePackagePath = filePath+basePackagePath.replace(".", File.separator);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(this.beanPath == null){
			this.beanPath = basePackagePath+File.separator+"bean";
		}
		if(this.mapperPath == null){
			this.mapperPath = basePackagePath+File.separator+"mapper";
		}
		if(this.xmlPath == null){
			this.xmlPath = basePackagePath+File.separator+"mapper"+File.separator+"xml";
		}
		if(this.servicePath == null){
			this.servicePath = basePackagePath+File.separator+"service";
		}
		if(this.serviceImplPath == null){
			this.serviceImplPath = basePackagePath+File.separator+"service"+File.separator+"impl";
		}
		formatToHump = true;
		addSwaggerAnnotation();
		addIgnore("id","create_time","update_time","deleted");
		addCostomAnnotation("TableField");
		useYifeiCore = true;
		return this;
	}
	
	private StringBuffer createJavaBeanTitle(StringBuffer sb){
		if(this.beanPackage == null){
			if(this.beanPath == null){
				return sb;
			}
			String basePackage = this.beanPath.replace(File.separator, ".");
			this.beanPackage = basePackage.substring(basePackage.indexOf("java.")+5);
		}
		sb.append("package "+this.beanPackage+";"+newline());
		if(useYifeiCore){
			sb.append("import com.yifei.core.bean.BizBean;"+newline());
			sb.append("import com.yifei.core.sqlExecuter.annotation.TableField;"+newline());
			sb.append("import io.swagger.annotations.ApiModel;"+newline());
			sb.append("import io.swagger.annotations.ApiModelProperty;"+newline());
			sb.append(newline());
			sb.append("@ApiModel()"+newline());
			sb.append("public class "+this.beanName+" extends BizBean {"+newline());
		}else{
			sb.append("public class "+this.beanName+" {"+newline());
		}
		return sb;
	}
	
	private StringBuffer createMapperBean(StringBuffer sb){
		if(this.mapperPackage == null){
			if(this.mapperPath == null){
				return sb;
			}
			String basePackage = this.mapperPath.replace(File.separator, ".");
			this.mapperPackage = basePackage.substring(basePackage.indexOf("java.")+5);
		}
		sb.append("package "+this.mapperPackage+";"+newline());
		if(useYifeiCore){
			sb.append("import com.yifei.core.service.BaseMapper;"+newline());
			sb.append("import "+this.beanPackage+"."+this.beanName+";"+newline());
			sb.append(newline());
			sb.append("public interface "+this.mapperName+" extends BaseMapper<"+beanName+"> {"+newline());
		}else{
			sb.append("public interface "+this.mapperName+" {"+newline());
		}
		return sb;
	}
	
	private StringBuffer createServiceBean(StringBuffer sb){
		if(this.servicePackage == null){
			if(this.servicePath == null){
				return sb;
			}
			String basePackage = this.servicePath.replace(File.separator, ".");
			this.servicePackage = basePackage.substring(basePackage.indexOf("java.")+5); 
		}
		sb.append("package "+this.servicePackage+";"+newline());
		if(useYifeiCore){
			sb.append("import com.yifei.core.service.BaseService;"+newline());
			sb.append("import "+this.beanPackage+"."+this.beanName+";"+newline());
			sb.append(newline());
			sb.append("public interface "+this.serviceName+" extends BaseService<"+beanName+"> {"+newline());
		}else{
			sb.append("public interface "+this.serviceName+" {"+newline());
		}
		return sb;
	}
	
	private StringBuffer createImplBean(StringBuffer sb){
		if(this.serviceImplPackage == null){
			if(this.serviceImplPath == null){
				return sb;
			}
			String basePackage = this.serviceImplPath.replace(File.separator, ".");
			this.serviceImplPackage = basePackage.substring(basePackage.indexOf("java.")+5);
		}
		sb.append("package "+this.serviceImplPackage+";"+newline());
		sb.append("import org.springframework.beans.factory.annotation.Autowired;"+newline());
		sb.append("import org.springframework.stereotype.Service;"+newline());
		if(useYifeiCore){
			sb.append("import com.yifei.core.service.BaseMapper;"+newline());
			sb.append("import com.yifei.core.service.BaseServiceImpl;"+newline());
		}
		sb.append("import "+this.beanPackage+"."+this.beanName+";"+newline());
		sb.append("import "+this.mapperPackage+"."+this.mapperName+";"+newline());
		sb.append("import "+this.servicePackage+"."+this.serviceName+";"+newline());
		sb.append(newline());
		sb.append("@Service"+newline());
		if(useYifeiCore){
			sb.append("public class "+this.serviceImplName+" extends BaseServiceImpl<"+this.beanName+"> implements "+this.serviceName+"{");
		}else{
			sb.append("public class "+this.serviceImplName+" implements "+this.serviceName+"{");
		}
		sb.append(newline());
		sb.append(tab()+"@Autowired");
		sb.append(newline());
		String mapperBeanName = new String(new char[]{this.mapperName.charAt(0)}).toLowerCase()+this.mapperName.substring(1);
		sb.append(tab()+"private "+this.mapperName+" "+mapperBeanName+";");
		sb.append(newline());
		if(useYifeiCore){
			sb.append(tab()+"@Override");
			sb.append(newline());
			sb.append(tab()+"protected BaseMapper<"+this.beanName+"> getMainMapper() {");
			sb.append(newline());
			sb.append(tab()+tab()+"return "+mapperBeanName+";");
			sb.append(newline());
			sb.append(tab()+"}");
			sb.append(newline());
		}
		return sb;
	}
	
	
	private StringBuffer createXml(StringBuffer sb){
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
		sb.append(newline());
		sb.append("<mapper namespace=\""+this.mapperPackage+"."+this.mapperName+"\">");
		sb.append(newline());
		String xmlContext = "<select id=\"listPage\" parameterType=\"BackUserBean\" resultType=\"BackUserBean\">\r\n" + 
				"    </select>\r\n" + 
				"\r\n" + 
				"    <select id=\"get\" parameterType=\"BackUserBean\" resultType=\"BackUserBean\">\r\n" + 
				"    </select>\r\n" + 
				"\r\n" + 
				"    <delete id=\"delete\" parameterType=\"BackUserBean\">\r\n" + 
				"    </delete>\r\n" + 
				"\r\n" + 
				"    <insert id=\"add\" parameterType=\"BackUserBean\" keyProperty=\"id\" useGeneratedKeys=\"true\">\r\n" + 
				"    </insert>\r\n" + 
				"\r\n" + 
				"    <update id=\"update\" parameterType=\"BackUserBean\">\r\n" + 
				"    </update>\r\n" + 
				"\r\n" + 
				"    <select id=\"select\" parameterType=\"BackUserBean\" resultType=\"BackUserBean\">\r\n" + 
				"    </select>\r\n" + 
				"\r\n" + 
				"    <select id=\"count\" parameterType=\"BackUserBean\" resultType=\"Integer\">\r\n" + 
				"    </select>\r\n" + 
				"    \r\n" + 
				"</mapper>";
		sb.append(xmlContext.replace("BackUserBean", beanName));
		return sb;
	}
	
	private void createFile(String basePackage,StringBuffer sb){
		File file = new File(basePackage);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(sb.toString().getBytes());
		}catch (Exception e) {
			throw new RuntimeException(e);
		}finally {
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * 删除已构建的文件
	 */
	public void  unBuild(){
		unBuild(true, true, true, true, true);
	}
	
	/**
	 * 删除已构建的文件
	 * @param delJavaBeanFile	是否删除javabean文件
	 * @param delMapperFile	是否删除mapper文件
	 * @param delXmlFile	是否删除xml文件
	 * @param delServiceFile	是否删除service文件
	 * @param delImplFile	是否删除serviceImpl文件
	 */
	public void unBuild(boolean delJavaBeanFile,boolean delMapperFile,boolean delXmlFile,boolean delServiceFile,boolean delImplFile){
		build(false);
		if(delJavaBeanFile && this.beanPath!=null){
			File file = new File(this.beanPath+File.separator+beanName+".java");
			if(!file.exists()){
				throw new RuntimeException("javaBean文件:"+file.getPath()+"未找到");
			}
			file.delete();
		}
		if(delMapperFile && this.mapperPath!=null){
			File file = new File(this.mapperPath+File.separator+mapperName+".java");
			if(!file.exists()){
				throw new RuntimeException("mapper文件:"+file.getPath()+"未找到");
			}
			file.delete();
		}
		if(delXmlFile && this.xmlPath!=null){
			File file = new File(this.xmlPath+File.separator+xmlName+".xml");
			if(!file.exists()){
				throw new RuntimeException("xml文件:"+file.getPath()+"未找到");
			}
			file.delete();
		}
		if(delServiceFile && this.servicePath!=null){
			File file = new File(this.servicePath+File.separator+serviceName+".java");
			if(!file.exists()){
				throw new RuntimeException("service文件:"+file.getPath()+"未找到");
			}
			file.delete();
		}
		if(delImplFile && this.serviceImplPath!=null){
			File file = new File(this.serviceImplPath+File.separator+serviceImplName+".java");
			if(!file.exists()){
				throw new RuntimeException("serviceImpl文件:"+file.getPath()+"未找到");
			}
			file.delete();
		}
	}
	
	/**
	 * 构建java
	 * @param createFile	是否生成文件
	 * @return
	 */
	public String build(boolean createFile){
		return build(createFile, createFile, createFile, createFile, createFile);
	}
	/**
	 * 构建javabean
	 * @param createJavaBeanFile	是否生成javaBean文件
	 * @param createMapperFile	是否生成mapper文件
	 * @param createXmlFile	是否生成xml文件
	 * @param createServiceFile	是否生成service文件
	 * @param createImplFile	是否生成serviceImpl文件
	 * @return
	 */
	public String build(boolean createJavaBeanFile,boolean createMapperFile,boolean createXmlFile,boolean createServiceFile,boolean createImplFile){
		if(mysqlFields==null || mysqlFields.size()==0){
			throw new RuntimeException("请先执行 queryFields(String tableName) 方法");
		}
		//以下添加java实体bean头部
		StringBuffer javaBeanSb = new StringBuffer();
		if(this.beanName == null){
			this.beanName = this.baseName+"Bean";
		}
		javaBeanSb = createJavaBeanTitle(javaBeanSb);
		//----------------------end
		StringBuffer mapperSb = new StringBuffer();
		//以下添加mapper实体
		if(this.mapperName == null){
			this.mapperName = this.baseName+"Mapper";
		}
		mapperSb = createMapperBean(mapperSb);
		//----------------------end
		StringBuffer xmlSb = new StringBuffer();
		//以下添加xml实体
		if(this.xmlName == null){
			this.xmlName = this.baseName+"Mapper";
		}
		xmlSb = createXml(xmlSb);
		//-----------------------end
		StringBuffer serviceSb = new StringBuffer();
		//以下添加service实体
		if(this.serviceName == null){
			this.serviceName = this.baseName+"Service";
		}
		serviceSb = createServiceBean(serviceSb);
		//-----------------------end
		StringBuffer implSb = new StringBuffer();
		//以下添加impl实体
		if(this.serviceImplName == null){
			this.serviceImplName = this.baseName+"ServiceImpl";
		}
		implSb = createImplBean(implSb);
		for (MysqlFieldParseToJavaBeanUtil field : mysqlFields) {
			if(ignoreFields.contains(field.Field)){
				continue;
			}
			javaBeanSb.append(newline());
			//先添加注解和注释
			if(addComment){
				javaBeanSb.append(tab()+"/** "+field.Comment+"*/"+newline());
			}
			if(addSwaggerAnnotation){
				javaBeanSb.append(tab()+"@ApiModelProperty(\""+field.Comment+"\")"+newline());
			}
			if(costomAnnotations.size() > 0){
				Set<Entry<String, List<AnnotationBuild>>> entrySet = costomAnnotations.entrySet();
				for (Entry<String, List<AnnotationBuild>> entry : entrySet) {
					String annotation = entry.getKey();
					List<AnnotationBuild> annotationValues = entry.getValue();
					javaBeanSb.append(tab()+"@"+annotation+"(");
					for (AnnotationBuild annotationValue : annotationValues) {
						String anv = field.Field;
						switch(annotationValue.value){
							case TABLE_FIELD_COMMENT:{
								anv = field.Comment;
								break;
							}
							case TABLE_FIELD_TYPE:{
								anv = field.Type;
								if(anv.contains("(") && anv.contains(")")){
									anv = anv.substring(0, anv.indexOf("("));
								}
								break;
							}
							default:{
								break;
							}
						}
						if(annotationValue.toUpperCase){
							anv = anv.toUpperCase();
						}
						javaBeanSb.append(annotationValue.getAnnotationField()+"=\""+anv+"\",");
					}
					if(javaBeanSb.toString().endsWith(",")){
						javaBeanSb.deleteCharAt(javaBeanSb.length()-1);
					}
					javaBeanSb.append(")"+newline());
				}
			}
			javaBeanSb.append(tab()+"private "+mysqlTypeToJavaType(field.Type));
			if(formatToHump){
				javaBeanSb.append(" "+conversionCamelCase(field.Field)+";");
			}else{
				javaBeanSb.append(" "+field.Field+";");
			}
			javaBeanSb.append(newline());
		}
		//文件结尾
		javaBeanSb.append(newline());
		javaBeanSb.append("}");
		mapperSb.append(newline());
		mapperSb.append("}");
		serviceSb.append(newline());
		serviceSb.append("}");
		implSb.append(newline());
		implSb.append("}");
		//生成javabean文件
		if(createJavaBeanFile && beanPath!=null){
			File file = new File(beanPath);
			if(!file.exists()){
				file.mkdirs();
			}
			createFile(beanPath+File.separator+this.beanName+".java", javaBeanSb);
		}
		if(createMapperFile && mapperPath!=null){
			File file = new File(mapperPath);
			if(!file.exists()){
				file.mkdirs();
			}
			createFile(mapperPath+File.separator+this.mapperName+".java", mapperSb);
		}
		if(createXmlFile && xmlPath!=null){
			File file = new File(xmlPath);
			if(!file.exists()){
				file.mkdirs();
			}
			createFile(xmlPath+File.separator+this.xmlName+".xml", xmlSb);
		}
		if(createServiceFile && servicePath!=null){
			File file = new File(servicePath);
			if(!file.exists()){
				file.mkdirs();
			}
			createFile(servicePath+File.separator+this.serviceName+".java", serviceSb);
		}
		if(createImplFile && serviceImplPath!=null){
			File file = new File(serviceImplPath);
			if(!file.exists()){
				file.mkdirs();
			}
			createFile(serviceImplPath+File.separator+this.serviceImplName+".java", implSb);
		}
		StringBuffer all = new StringBuffer();
		all.append(javaBeanSb.toString());
		all.append(newline());
		all.append("=================================================================");
		all.append(newline());
		all.append(mapperSb.toString());
		all.append(newline());
		all.append("=================================================================");
		all.append(newline());
		all.append(xmlSb.toString());
		all.append(newline());
		all.append("=================================================================");
		all.append(newline());
		all.append(serviceSb.toString());
		all.append(newline());
		all.append("=================================================================");
		all.append(newline());
		all.append(implSb.toString());
		return all.toString();
	}
	
	private String mysqlTypeToJavaType(String mysqlType){
		String type = mysqlType.toUpperCase().trim();
		if(type.startsWith("INT") || type.startsWith("INTEGER") || type.startsWith("TINYINT") || type.startsWith("SMALLINT") || type.startsWith("MEDIUMINT")){
			return "Integer";
		}
		if(type.startsWith("BIGINT")){
			return "Long";
		}
		if(type.startsWith("DOUBLE")){
			return "Double";
		}
		if(type.startsWith("FLOAT")){
			return "Float";
		}
		if(type.startsWith("DECIMAL")){
			return "BigDecimal";
		}
		if(type.startsWith("DATE") || type.startsWith("YEAR")){
			return "Date";
		}
		if(type.startsWith("TIME")){
			return "Time";
		}
		if(type.startsWith("DATATIME")){
			return "Timestamp";
		}
		return "String";
	}
	
    /**
     * 下划线转驼峰格式
     */
    private String conversionCamelCase(String v) {
        int index = v.indexOf("_");
        while (index != -1) {
            v = v.replace(v.substring(index, index + 2), v.substring(index + 1, index + 2).toUpperCase());
            index = v.indexOf("_");
        }
        return v;
    }
    
    private String newline(){
    	return " \n";
    }
    
    private String tab(){
    	return " \t";
    }
    
    static class AnnotationBuild{
    	/** 注解属性名*/
    	private String annotationField;
    	/** 数据库字段属性枚举*/
    	private TableFieldDetail value;
    	/** 是否转换大写*/
    	private boolean toUpperCase = false;
    	
    	/**
    	 * 构建注解属性
    	 * @param annotationField	注解属性名称
    	 * @param value	数据库字段属性枚举
    	 * @param toUpperCase	是否转换成大写
    	 * @return
    	 */
    	public static AnnotationBuild build(String annotationField,TableFieldDetail value,boolean... toUpperCase){
    		AnnotationBuild build = new AnnotationBuild();
    		build.annotationField = annotationField;
    		build.value = value;
    		if(toUpperCase.length > 0){
    			build.toUpperCase = toUpperCase[0];
    		}
    		return build;
    	}

		public String getAnnotationField() {
			return annotationField;
		}

		public TableFieldDetail getValue() {
			return value;
		}

		public boolean isToUpperCase() {
			return toUpperCase;
		}
		
		
    	
    }
    
    /**
     * 数据库字段枚举
     * @author spc
     *
     */
    enum TableFieldDetail{
    	/** 字段名*/
    	TABLE_FIELD_NAME,
    	/** 字段说明*/
    	TABLE_FIELD_COMMENT,
    	/** 字段类型*/
    	TABLE_FIELD_TYPE;
    }
    
}
