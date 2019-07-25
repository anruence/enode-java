package com.enodeframework.configurations;

import java.util.HashMap;
import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class OptionSetting {
    private Map<String, String> options;

    public OptionSetting(StringKeyValuePair... pairs) {
        this.options = new HashMap<>();
        if (pairs == null || pairs.length == 0) {
            return;
        }
        for (int i = 0, len = pairs.length; i < len; i++) {
            StringKeyValuePair option = pairs[i];
            this.options.put(option.getKey(), option.getValue());
        }
    }

    public void setOptionValue(String key, String value) {
        options.put(key, value);
    }

    public String getOptionValue(String key) {
        return options.get(key);
    }
}
