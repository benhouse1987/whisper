package com.fane.whisper.serializer;

import com.fane.whisper.annotation.I18nMapping;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

public class I18nWriter extends BeanPropertyWriter {
    BeanPropertyWriter _writer;
    String i18nCode;

    public I18nWriter(BeanPropertyWriter w) {
        super(w);
        this.i18nCode= w.getAnnotation(I18nMapping.class).i18nCode();
        _writer = w;
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator gen,
                                 SerializerProvider prov) throws Exception {

        String value = (_accessorMethod == null) ? _field.get(bean).toString()
                : ((_accessorMethod.invoke(bean) == null) ? null : "*i18n" + i18nCode + "*-" + _accessorMethod.invoke(bean).toString());
        gen.writeStringField(_name.toString(), value);
    }
}
