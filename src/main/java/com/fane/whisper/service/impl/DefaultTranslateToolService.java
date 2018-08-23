package com.fane.whisper.service.impl;

import com.fane.whisper.service.TranslateToolService;


public class DefaultTranslateToolService implements TranslateToolService {

    private static final String CHINESE_LANG="zh_CN";


    public String getCurrentLanguage() {
        return CHINESE_LANG;
    }


}
