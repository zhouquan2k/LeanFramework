package io.leanddd.component.spring;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogbackThreadIdConvensionRule extends ClassicConverter {
	Map<String, Integer> map = new ConcurrentHashMap<String, Integer>();
	int nextKey = 0;

	@Override
	public String convert(ILoggingEvent event) {
		String thread = event.getThreadName();
		if (!map.containsKey(thread)) {
			map.put(thread, nextKey++);
		}
		return "" + map.get(thread);
		// return ""+Thread.currentThread().getId();
	}

}
