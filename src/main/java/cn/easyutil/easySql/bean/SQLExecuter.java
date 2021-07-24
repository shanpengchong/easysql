package cn.easyutil.easySql.bean;

import cn.easyutil.easySql.util.LambdaFunction;
import cn.easyutil.easySql.util.LamdbaUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SQLExecuter implements Serializable {

	private static final long serialVersionUID = 1L;

	// 组装sql
	private StringBuffer sql = new StringBuffer("");

	// mybatis需要的参数语句
	private StringBuffer mybatisSql = new StringBuffer("");
	// limit起始位置
	private Integer limitStart = 0;
	// limit结束位置
	private Integer limitSize = 0;
	//是否在使用括号
	private boolean useBrackets;
	// 排序字段
	private List<String> mybatisDescList = new ArrayList<String>();
	// 排序字段
	private List<String> descList = new ArrayList<String>();
	// 自增字段
	private Map<String, Object> incrMap = new HashMap<String, Object>();
	// 返回字段过滤
	private StringBuffer returnParam = new StringBuffer();
	// 参数
	private List<Object> params = new ArrayList<Object>();
	//最后拼接的参数
	private List<String> lastAppends = new ArrayList<>();
	// 数据库映射对象
	private Object t;
	// 参数map
	private Map<String, Object> mybatisParams = new HashMap<String, Object>();
	// 设置字段值为null的集合列表
	private List<String> nullValFields = new ArrayList<String>();

	public SQLExecuter(Object t) {
		this.t = t;
	}

	public static SQLExecuter build(Object t){
		SQLExecuter executer = new SQLExecuter(t);
		return executer;
	}

	/**
	 * 获取字段值为null的集合列表
	 *
	 * @return
	 */
	public List<String> getNullValFields() {
		return this.nullValFields;
	}

	private void checkBrackets(Boolean... useOr){
		if(useBrackets){
			useBrackets = false;
			sql.append("`");
			return ;
		}
		useBrackets = false;
		if(useOr.length>0 && useOr[0]){
			sql.append(" or `" );
			mybatisSql.append(" or `");
		}else{
			sql.append(" and `" );
			mybatisSql.append(" and `");
		}

	}

	/**
	 * 设置数据库操作对象
	 *
	 * @param t
	 */
	public <T> void setBean(T t) {
		this.t = t;
	}

	public <T>SQLExecuter like(LambdaFunction<T,?> function, String like, Boolean... useOr){
		return like(LamdbaUtil.getFieldName(function),like,useOr);
	}

	public <T>SQLExecuter like(boolean judge,LambdaFunction<T,?> function, String like, Boolean... useOr){
		if(!judge){
			return this;
		}
		return like(LamdbaUtil.getFieldName(function),like,useOr);
	}
	/**
	 * 模糊匹配
	 *
	 * @param field
	 *            字段
	 * @param like
	 *            关键字
	 * @param useOr
	 *            是否使用or关键字,默认或不传为and语句
	 * @return
	 */
	public SQLExecuter like(String field, String like, Boolean... useOr) {
		String key = UUID.randomUUID().toString().replace("-", "");
		checkBrackets(useOr);
		sql.append(field + "` like" + " ? ");
		mybatisSql.append(field + "` like " + " #{mybatisParams." + key + "} ");
//		if (useOr.length > 0 && useOr[0]) {
//			sql.append(field + "` like" + " ? ");
//			mybatisSql.append(field + "` like " + " #{mybatisParams." + key + "} ");
//		} else {
//			if(!useBrackets){
//				sql.append(" and `" );
//				mybatisSql.append(" and `");
//			}
//			sql.append(field + "` like" + " ? ");
//			mybatisSql.append(field + "` like" + " #{mybatisParams." + key + "} ");
//		}
		params.add("%" + like + "%");
		mybatisParams.put(key, "%" + like + "%");
		return this;
	}

	/**
	 * 排序字段,默认正序
	 *
	 * @param field
	 * @return
	 */
	public SQLExecuter orderBy(String field) {
		return orderBy(field, true);
	}

	public SQLExecuter orderBy(boolean judge,String field) {
		if(!judge){
			return this;
		}
		return orderBy(field);
	}

	public <T>SQLExecuter max(LambdaFunction<T,?> function, String asName){
		return max(LamdbaUtil.getFieldName(function),asName);
	}

	public SQLExecuter max(String field) {
		return max(field, null);
	}

	public SQLExecuter max(String field, String asName) {
		if (asName != null) {
			this.returnParam.append("MAX(" + field + ") as " + asName + " ");
		} else {
			this.returnParam.append("MAX(" + field + ") as " + field + " ");
		}
		this.returnParam.append(",");
		return this;
	}

	public <T>SQLExecuter min(LambdaFunction<T,?> function, String asName){
		return min(LamdbaUtil.getFieldName(function),asName);
	}

	public SQLExecuter min(String field) {
		return min(field, null);
	}

	public SQLExecuter min(String field, String asName) {
		if (asName != null) {
			this.returnParam.append("MIN(" + field + ") as " + asName + " ");
		} else {
			this.returnParam.append("MIN(" + field + ") as " + field + " ");
		}
		this.returnParam.append(",");
		return this;
	}

	public <T>SQLExecuter count(LambdaFunction<T,?> function, String asName){
		return count(LamdbaUtil.getFieldName(function),asName);
	}

	public SQLExecuter count(String field) {
		return count(field, null);
	}

	public SQLExecuter count(String field, String asName) {
		if (asName != null) {
			this.returnParam.append("COUNT(" + field + ") as " + asName + " ");
		} else {
			this.returnParam.append("COUNT(" + field + ") as " + field + " ");
		}
		this.returnParam.append(",");
		return this;
	}

	public <T>SQLExecuter sum(LambdaFunction<T,?> function, String asName){
		return sum(LamdbaUtil.getFieldName(function),asName);
	}

	public SQLExecuter sum(String field) {
		return sum(field, null);
	}

	public SQLExecuter sum(String field, String asName) {
		if (asName != null) {
			this.returnParam.append("SUM(" + field + ") as " + asName + " ");
		} else {
			this.returnParam.append("SUM(" + field + ") as " + field + " ");
		}
		this.returnParam.append(",");
		return this;
	}

	public <T>SQLExecuter orderBy(boolean judge,LambdaFunction<T,?> function, boolean desc){
		if(!judge){
			return this;
		}
		return orderBy(LamdbaUtil.getFieldName(function),desc);
	}

	public <T>SQLExecuter orderBy(LambdaFunction<T,?> function, boolean desc){
		return orderBy(LamdbaUtil.getFieldName(function),desc);
	}

	/**
	 * 排序
	 *
	 * @param field
	 *            字段
	 * @param desc
	 *            true:正序 false:倒序
	 * @return
	 */
	public SQLExecuter orderBy(String field, boolean desc) {
		return orderBy(field, null, null, desc);
	}

	public <T>SQLExecuter orderByIf(LambdaFunction<T,?> function, Object val, boolean desc){
		return orderByIf(LamdbaUtil.getFieldName(function),val,desc);
	}

	public <T>SQLExecuter orderByIf(boolean judge,LambdaFunction<T,?> function, Object val, boolean desc){
		if(!judge){
			return this;
		}
		return orderByIf(LamdbaUtil.getFieldName(function),val,desc);
	}

	/**
	 * 排序，并且指定值放在最前或最后
	 *
	 * @param field
	 * @param val
	 * @param desc
	 * @return
	 */
	public SQLExecuter orderByIf(String field, Object val, boolean desc) {
		return orderBy(field, val, null, desc);
	}


	public <T>SQLExecuter orderByIn(LambdaFunction<T,?> function, Collection in, boolean desc){
		return orderByIn(LamdbaUtil.getFieldName(function),in,desc);
	}
	public <T>SQLExecuter orderByIn(boolean judge,LambdaFunction<T,?> function, Collection in, boolean desc){
		if(!judge){
			return this;
		}
		return orderByIn(LamdbaUtil.getFieldName(function),in,desc);
	}

	/**
	 * 排序,并且指定集合值放在最前或最后
	 *
	 * @param field
	 * @param in
	 * @param desc
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public SQLExecuter orderByIn(String field, Collection in, boolean desc) {
		return orderBy(field, null, in, desc);
	}

	@SuppressWarnings("rawtypes")
	private SQLExecuter orderBy(String field, Object val, Collection in, boolean desc) {
		String descStr = "";
		if (!desc) {
			descStr = " desc ";
		}
		if (val != null) {
			String key = UUID.randomUUID().toString().replace("-", "");
			mybatisDescList.add(" if (`" + field + "`=#{mybatisParams." + key + "},0,1),`" + field + "` " + descStr);
			descList.add(" if (`" + field + "`=?,0,1),`" + field + "` " + descStr);
			mybatisParams.put(key, val);
			params.add(val);
		} else if (in != null) {
			StringBuffer mybatisSql = new StringBuffer("`" + field + "` in(");
			StringBuffer sql = new StringBuffer("`" + field + "` in(");
			for (Object obj : in) {
				String key = UUID.randomUUID().toString().replace("-", "");
				mybatisSql.append("#{mybatisParams." + key + "},");
				sql.append("?,");
				params.add(obj);
				mybatisParams.put(key, obj);
			}
			mybatisDescList.add(mybatisSql.deleteCharAt(mybatisSql.length() - 1).toString() + ")," + field);
			descList.add(sql.deleteCharAt(sql.length() - 1).toString() + ")," + field);
		} else {
			mybatisDescList.add(" `" + field + "` " + descStr);
			descList.add(" `" + field + "` " + descStr);
		}
		return this;
	}

	public <T> SQLExecuter in(LambdaFunction<T,?> function, Collection<T> in, Boolean... useOr){
		return in(LamdbaUtil.getFieldName(function),in,useOr);
	}

	public <T> SQLExecuter in(boolean judge,LambdaFunction<T,?> function, Collection<T> in, Boolean... useOr){
		if(!judge){
			return this;
		}
		return in(LamdbaUtil.getFieldName(function),in,useOr);
	}

	/**
	 * in语句
	 *
	 * @param <T>
	 * @param field
	 *            字段
	 * @param in
	 * @param useOr
	 *            是否使用or关键字,默认或不传为and语句
	 * @return
	 */
	public <T> SQLExecuter in(String field, Collection<T> in, Boolean... useOr) {
		if (in == null || in.size() == 0) {
			return this;
		}
		checkBrackets(useOr);
		sql.append(field + "` in (");
		mybatisSql.append(field + "` in (");
//		if (useOr.length > 0 && useOr[0]) {
//			sql.append(" or `" + field + "` in (");
//			mybatisSql.append(" or `" + field + "` in (");
//		} else {
//			sql.append(" and `" + field + "` in (");
//			mybatisSql.append(" and `" + field + "` in (");
//		}
		for (Object o : in) {
			String key = UUID.randomUUID().toString().replace("-", "");
			sql.append("?,");
			mybatisSql.append("#{mybatisParams." + key + "},");
			mybatisParams.put(key, o);
			params.add(o);
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(") ");
		mybatisSql.deleteCharAt(mybatisSql.length() - 1);
		mybatisSql.append(") ");
		return this;
	}

	public <T> SQLExecuter notIn(LambdaFunction<T,?> function, Collection<T> in, Boolean... useOr){
		return notIn(LamdbaUtil.getFieldName(function),in,useOr);
	}

	public <T> SQLExecuter notIn(boolean judge,LambdaFunction<T,?> function, Collection<T> in, Boolean... useOr){
		if(!judge){
			return this;
		}
		return notIn(LamdbaUtil.getFieldName(function),in,useOr);
	}

	/**
	 * sql not in 语句
	 *
	 * @param <T>
	 * @param field
	 * @param in
	 * @param useOr
	 *            是否使用or关键字,默认或不传为and语句
	 * @return
	 */
	public <T> SQLExecuter notIn(String field, Collection<T> in, Boolean... useOr) {
		if (in == null || in.size() == 0) {
			return this;
		}
		checkBrackets(useOr);
		sql.append(field + "` not in (");
		mybatisSql.append(field + "` not in (");
//		if (useOr.length > 0 && useOr[0]) {
//			sql.append(" or `" + field + "` not in (");
//			mybatisSql.append(" or `" + field + "` not in (");
//		} else {
//			sql.append(" and `" + field + "` not in (");
//			mybatisSql.append(" and `" + field + "` not in (");
//		}
		for (Object o : in) {
			String key = UUID.randomUUID().toString().replace("-", "");
			sql.append("?,");
			mybatisSql.append("#{mybatisParams." + key + "},");
			mybatisParams.put(key, o);
			params.add(o);
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(") ");
		mybatisSql.deleteCharAt(mybatisSql.length() - 1);
		mybatisSql.append(") ");
		return this;
	}

	public <T>SQLExecuter lte(LambdaFunction<T,?> function, Object obj, Boolean... useOr){
		return lte(LamdbaUtil.getFieldName(function),obj,useOr);
	}

	public <T>SQLExecuter lte(boolean judge,LambdaFunction<T,?> function, Object obj, Boolean... useOr){
		if(!judge){
			return this;
		}
		return lte(LamdbaUtil.getFieldName(function),obj,useOr);
	}

	/**
	 * 小于等于语句 <=
	 *
	 * @param field
	 * @param obj
	 * @param useOr
	 *            是否使用or关键字,默认或不传为and语句
	 * @return
	 */
	public SQLExecuter lte(String field, Object obj, Boolean... useOr) {
		if (obj == null) {
			return this;
		}
		checkBrackets(useOr);
		String key = UUID.randomUUID().toString().replace("-", "");
		sql.append(field + "` <= ?");
		mybatisSql.append(field + "` <= #{mybatisParams." + key + "} ");
//		if (useOr.length > 0 && useOr[0]) {
//			sql.append(" or `" + field + "` <= ?");
//			mybatisSql.append(" or `" + field + "` <= #{mybatisParams." + key + "} ");
//		} else {
//			sql.append(" and `" + field + "` <= ?");
//			mybatisSql.append(" and `" + field + "` <= #{mybatisParams." + key + "} ");
//		}
		params.add(obj);
		mybatisParams.put(key, obj);
		return this;
	}

	public <T>SQLExecuter gte(LambdaFunction<T,?> function, Object obj, Boolean... useOr){
		return gte(LamdbaUtil.getFieldName(function),obj,useOr);
	}

	public <T>SQLExecuter gte(boolean judge,LambdaFunction<T,?> function, Object obj, Boolean... useOr){
		if(!judge){
			return this;
		}
		return gte(LamdbaUtil.getFieldName(function),obj,useOr);
	}


	/**
	 * 大于等于语句
	 *
	 * @param field
	 * @param obj
	 * @param useOr
	 *            是否使用or关键字,默认或不传为and语句
	 * @return
	 */
	public SQLExecuter gte(String field, Object obj, Boolean... useOr) {
		if (obj == null) {
			return this;
		}
		String key = UUID.randomUUID().toString().replace("-", "");
		checkBrackets(useOr);
		sql.append(field + "` >= ?");
		mybatisSql.append(field + "` >= #{mybatisParams." + key + "} ");
//		if (useOr.length > 0 && useOr[0]) {
//			sql.append(" or `" + field + "` >= ?");
//			mybatisSql.append(" or `" + field + "` >= #{mybatisParams." + key + "} ");
//		} else {
//			sql.append(" and `" + field + "` >= ?");
//			mybatisSql.append(" and `" + field + "` >= #{mybatisParams." + key + "} ");
//		}
		params.add(obj);
		mybatisParams.put(key, obj);
		return this;
	}

	public <T>SQLExecuter setNull(LambdaFunction<T,?> function){
		return setNull(LamdbaUtil.getFieldName(function));
	}

	public <T>SQLExecuter setNull(boolean judge,LambdaFunction<T,?> function){
		if(!judge){
			return this;
		}
		return setNull(LamdbaUtil.getFieldName(function));
	}
	/**
	 * 设置字段值为null
	 *
	 * @param field
	 *            字段名
	 * @param fields
	 *            字段名
	 * @return
	 */
	public SQLExecuter setNull(String field, String... fields) {
		nullValFields.add(field);
		if (fields.length > 0) {
			nullValFields.addAll(Arrays.asList(fields));
		}
		return this;

	}

	public final <T>SQLExecuter eq(LambdaFunction<T,?> function, Object obj, Boolean... useOr){
		return eq(LamdbaUtil.getFieldName(function),obj,useOr);
	}

	public final <T>SQLExecuter eq(boolean judge,LambdaFunction<T,?> function, Object obj, Boolean... useOr){
		if(!judge){
			return this;
		}
		return eq(LamdbaUtil.getFieldName(function),obj,useOr);
	}

	/**
	 * 相等语句
	 *
	 * @param field
	 * @param obj
	 * @param useOr
	 *            是否使用or关键字,默认或不传为and语句
	 * @return
	 */
	public SQLExecuter eq(String field, Object obj, Boolean... useOr) {
		if (obj == null) {
			return this;
		}
		String key = UUID.randomUUID().toString().replace("-", "");
		checkBrackets(useOr);
		sql.append(field + "` = ? ");
		mybatisSql.append(field + "` = #{mybatisParams." + key + "} ");
//		if (useOr.length > 0 && useOr[0]) {
//			sql.append(" or `" + field + "` = ? ");
//			mybatisSql.append(" or `" + field + "` = #{mybatisParams." + key + "} ");
//		} else {
//			sql.append(" and `" + field + "` = ? ");
//			mybatisSql.append(" and `" + field + "` = #{mybatisParams." + key + "} ");
//		}
		params.add(obj);
		mybatisParams.put(key, obj);
		return this;
	}

	public <T>SQLExecuter notEquals(LambdaFunction<T,?> function, Object obj, Boolean... useOr){
		return notEquals(LamdbaUtil.getFieldName(function),obj,useOr);
	}

	public <T>SQLExecuter notEquals(boolean judge,LambdaFunction<T,?> function, Object obj, Boolean... useOr){
		if(!judge){
			return this;
		}
		return notEquals(LamdbaUtil.getFieldName(function),obj,useOr);
	}

	/**
	 * 不等于语句
	 *
	 * @param field
	 * @param obj
	 * @param useOr
	 *            是否使用or关键字,默认或不传为and语句
	 * @return
	 */
	public SQLExecuter notEquals(String field, Object obj, Boolean... useOr) {
		if (obj == null) {
			return this;
		}
		String key = UUID.randomUUID().toString().replace("-", "");
		checkBrackets(useOr);
		sql.append(field + "` != ? ");
		mybatisSql.append(field + "` != #{mybatisParams." + key + "} ");
//		if (useOr.length > 0 && useOr[0]) {
//			sql.append(" or `" + field + "` != ? ");
//			mybatisSql.append(" or `" + field + "` != #{mybatisParams." + key + "} ");
//		} else {
//			sql.append(" and `" + field + "` != ? ");
//			mybatisSql.append(" and `" + field + "` != #{mybatisParams." + key + "} ");
//		}
		params.add(obj);
		mybatisParams.put(key, obj);
		return this;
	}

	public <T>SQLExecuter incr(LambdaFunction<T,?> function, Number num){
		return incr(LamdbaUtil.getFieldName(function),num);
	}

	public <T>SQLExecuter incr(boolean judge, LambdaFunction<T,?> function, Number num){
		if(!judge){
			return this;
		}
		return incr(LamdbaUtil.getFieldName(function),num);
	}

	/**
	 * 属性值自增
	 *
	 * @param field
	 *            自增值，正数或负数
	 * @return
	 */
	public SQLExecuter incr(String field, Number num) {
		if (num == null) {
			return this;
		}
		if (num instanceof Double || num instanceof Float) {
			incrMap.put(field, num.doubleValue());
		} else {
			incrMap.put(field, num.longValue());
		}
		return this;
	}

	public SQLExecuter appendSql(boolean judge,String sql){
		if(!judge){
			return this;
		}
		return appendSql(sql);
	}

	/**
	 * 拼在语句最后的自定义sql和参数
	 * @param sql
	 * @return
	 */
	public SQLExecuter appendSql(String sql){
		if(sql==null || sql.length()==0){
			return this;
		}
		this.sql.append(" "+sql+" ");
		this.mybatisSql.append(" "+sql+" ");
		return this;
	}

	/**
	 * 获取生成的原生sql
	 *
	 *            数据库映射bean属性名规则
	 * @return
	 */
	public String getSql() {
		StringBuffer orderby = new StringBuffer("");
		if (this.descList != null && this.descList.size() > 0) {
			orderby.append(" order by ");
			for (String desc : this.descList) {
				orderby.append(" " + desc + ",");
			}
			orderby.deleteCharAt(orderby.length() - 1);
		}
		StringBuffer appendSql = new StringBuffer();
		for (String append : lastAppends) {
			appendSql.append(" "+append+" ");
		}
		return this.sql.toString() +appendSql.toString()+ orderby.toString();
	}

	/**
	 * 获取sql语句的参数
	 *
	 * @return
	 */
	public List<Object> getParams() {
		return this.params;
	}

	/**
	 * 获取数据库映射对象
	 *
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean() {
		return (T) this.t;
	}

	/**
	 * 添加左括号 '('
	 *
	 * @return
	 */
	public SQLExecuter leftBrackets(Boolean... useOr) {
		if (useOr.length > 0 && useOr[0]) {
			sql.append(" or ( ");
			mybatisSql.append(" or (  ");
		} else {
			sql.append(" and (  ");
			mybatisSql.append(" and (  ");
		}
		this.useBrackets = true;
		return this;
	}

	/**
	 * 添加右括号 ')'
	 *
	 * @return
	 */
	public SQLExecuter rightBrackets() {
		sql.append(" ) ");
		mybatisSql.append(" ) ");
		this.useBrackets = false;
		return this;
	}

	/**
	 * 获取mybatis需要的语句
	 *
	 * 数据库映射bean属性名规则
	 * @return
	 */
	public String getMybatisSql() {
		StringBuffer orderby = new StringBuffer("");
		if (this.mybatisDescList != null && this.mybatisDescList.size() > 0) {
			orderby.append(" order by ");
			for (String desc : this.mybatisDescList) {
				orderby.append(" " + desc + ",");
			}
			orderby.deleteCharAt(orderby.length() - 1);
		}
		StringBuffer appendSql = new StringBuffer();
		for (String append : lastAppends) {
			appendSql.append(" "+append+" ");
		}
		return this.mybatisSql.toString() +appendSql.toString()+ orderby.toString();
	}

	public Map<String, Object> getMybatisParams() {
		return mybatisParams;
	}

	public Map<String, Object> getIncrMap() {
		return incrMap;
	}

	public String getReturnParam() {
		if (this.returnParam.length() > 0 && this.returnParam.toString().endsWith(",")) {
			return this.returnParam.toString().substring(0, this.returnParam.lastIndexOf(","));
		}
		return returnParam.toString();
	}

	public SQLExecuter setReturnParam(String...field) {
		if (field.length == 0) {
			return this;
		}
		for (String s : field) {
			this.returnParam.append("`" + s + "`,");
		}
		return this;
	}

	public Integer getLimitStart() {
		return limitStart;
	}

	public SQLExecuter setLimitStart(Integer limitStart) {
		this.limitStart = limitStart;
		return this;
	}

	public Integer getLimitSize() {
		return limitSize;
	}

	public SQLExecuter setLimitSize(Integer limitSize) {
		this.limitSize = limitSize;
		return this;
	}

	public static void main(String[] args) {
		SQLExecuter ex = new SQLExecuter(null);
		Set<String> set = new HashSet<String>();
		set.add("小红");
		set.add("笑话");
		set.add("小黄");
		ex.orderBy("name").orderByIf("name", "小明", false).orderByIn("name", set, true);
		System.out.println(ex.getSql());
		System.out.println(ex.getParams());
	}
}
