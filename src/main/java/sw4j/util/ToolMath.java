package sw4j.util;

import java.util.Collection;

public class ToolMath {
	public static int max(Collection<Integer> ary, int defaultMax){
		int ret = defaultMax;
		for(Integer value: ary){
			ret = Math.max(ret, value);
		}
		return ret;
	}
	
	public static int avg(Collection<Integer> ary){
		if (ary.size()==0)
			return 0;
		
		int sum = 0;
		for(Integer value: ary){
			sum += value;
		}
		return sum/ary.size();
	}

	public static int sum(Collection<Integer> ary){
		int sum = 0;
		for(Integer value: ary){
			sum += value;
		}
		return sum;
	}
	
}
