package com.fane.whisper.aspect;

import com.fane.whisper.service.I18nTranslateService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

@Aspect
@Order(1)
public class I18nResourceAspect {
    public static final Logger log = LoggerFactory.getLogger(I18nResourceAspect.class);
    I18nTranslateService i18nTranslateService;

    public I18nResourceAspect(I18nTranslateService i18nTranslateService){
        this.i18nTranslateService=i18nTranslateService;
    }

    @Around("@annotation(com.fane.whisper.annotation.I18nTranslate)")
    protected Object aroudAdivce(ProceedingJoinPoint jp) throws Throwable {
        Object rt = jp.proceed();
        rt = i18nTranslateService.translate(rt, i18nTranslateService.getCurrentLanguage());
        return rt;
    }
}
