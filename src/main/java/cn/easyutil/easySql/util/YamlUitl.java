package cn.easyutil.easySql.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

/**
 * 读写yaml
 * @author spc
 *
 */
public class YamlUitl {
	
	private Map<String, Object> data = new HashMap<String, Object>();

	public Map<String, Object> readYaml(InputStream input){
		Yaml yaml = new Yaml();
		Map<String,Object> map = yaml.load(input);
		exchange(map, null);
		return data;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void exchange(Map<String,Object> map,String k){
		Set<Entry<String, Object>> set = map.entrySet();
		for (Entry<String, Object> entry : set) {
			String key = entry.getKey();
			if(k == null){
				k = key;
			}
			Object value = entry.getValue();
			if(value instanceof Map){
				exchange((Map)value,k+"."+key);
			}else{
				data.put(k+"."+key, value);
			}
		}
	}
}
