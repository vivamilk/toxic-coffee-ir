/**
 * 
 */
package app.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import com.google.api.services.customsearch.model.Result;


/**
 * @author Isolachine
 *
 */
public class ResultListParser {
    class WeightedResults implements Comparable<Object>{
    		Result result;
        int weight;
        WeightedResults (Result r, int w) {
    			this.result = r;
        		this.weight = w;
        }
        public int compareTo(Object rhs) {
        		WeightedResults other = (WeightedResults) rhs;
        		int diff = weight - other.weight;
        		if (diff == 0)
        			return 0;
        		else if (diff < 0)
        			return 1;
        		else
        			return -1;
        }
    }
    
    /**
     * [parse the results list and return sorted list]
     * @param  results 
     *             [results list from CSE]
     * @return [sorted results list, based on original position and times of appearance]
     */
    public List<Result> parseCSEList(List<Result> results) {
    		Map<String,WeightedResults> map = new HashMap<String,WeightedResults>();
        for (int i = 0; i < results.size() ; i++) {
        		Result r = results.get(i);
        		String u = new Util().parseUrl(r.getLink().toLowerCase());
        		if (!u.equals("")) {
        			map.put(u, new WeightedResults(r, map.getOrDefault(u, new WeightedResults(null,0)).weight+results.size()-i));
        		}
        		else continue;	
        }
        List<WeightedResults> tmpList = new ArrayList<WeightedResults>(map.values());
        Collections.sort(tmpList);
        List<Result> newList = new ArrayList<Result>();
        for (WeightedResults wr : tmpList) {
        		newList.add(wr.result);
        }
        return newList;
    }
    
    
    
    
}
