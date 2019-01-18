package com.enode.configurations;

import java.util.HashMap;
import java.util.Map;

public class OptionSetting {
    private Map<String, String> _options;

    public OptionSetting(StringKeyValuePair... options) {
        _options = new HashMap<>();
        if (options == null || options.length == 0) {
            return;
        }
        for (int i = 0, len = options.length; i < len; i++) {
            StringKeyValuePair option = options[i];
            _options.put(option.getKey(), option.getValue());
        }
    }

    public void setOptionValue(String key, String value) {
        _options.put(key, value);
    }

    public String getOptionValue(String key) {
        return _options.get(key);
    }
}
