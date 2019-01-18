package com.enode.common.thirdparty.gson;

import com.enode.common.serializing.IJsonSerializer;

public class GsonJsonSerializer extends AbstractSerializer implements IJsonSerializer {
    public GsonJsonSerializer() {
        super(false,false);
    }

    @Override
    public String serialize(Object obj) {
        return this.gson().toJson(obj);
    }

    @Override
    public <T> T deserialize(String aSerialization, Class<T> aType) {
        return this.gson().fromJson(aSerialization, aType);
    }
}
