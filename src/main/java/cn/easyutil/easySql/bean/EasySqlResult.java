package cn.easyutil.easySql.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询时的返回对象,内含两个属性
 * 1:resultList<T> 查询的结果集合
 * 2:page : 分页的相关信息
 * @author spc
 * @param <T>
 *
 */
public class EasySqlResult<T> implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<T> resultList = new ArrayList<>();
	
	private Page page;

	/** 获取查询的结果集*/
	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}

	/** 获取分页对象*/
	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}
}
