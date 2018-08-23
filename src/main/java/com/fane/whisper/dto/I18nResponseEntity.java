package com.fane.whisper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class I18nResponseEntity<T> extends ResponseEntity {
    @JsonIgnore
    private Integer statusCodeValue=200;
    private  Object statusCode;

    public I18nResponseEntity(){
        super(HttpStatus.OK);
        statusCode= HttpStatus.OK;
    }

    public I18nResponseEntity(HttpStatus status) {
        super(status);
        statusCode =status;
    }

    public I18nResponseEntity(T body, HttpStatus status) {
        this(body, (MultiValueMap) null, (HttpStatus) status);
        statusCode = status;
    }

    public I18nResponseEntity(MultiValueMap<String, String> headers, HttpStatus status) {
        super((Object) null, headers, (HttpStatus) status);
        statusCode = status;
    }

    public I18nResponseEntity(T body, MultiValueMap<String, String> headers, HttpStatus status) {
        super(body, headers,status);
        statusCode = status;
    }


    public int getStatusCodeValue() {

        if(this.statusCode instanceof HttpStatus){
            return ((HttpStatus) this.statusCode).value();
        }else if (this.statusCode instanceof String){
            return HttpStatus.valueOf((String)this.statusCode).value();
        }else {
            return (Integer) this.statusCode;
        }

    }


}
