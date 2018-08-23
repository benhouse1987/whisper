package com.fane.whisper.service;


import com.fane.whisper.dto.I18nResponseEntity;
import com.fane.whisper.dto.I18nTranslateItemDTO;
import com.fane.whisper.serializer.I18nWriter;
import com.fane.whisper.service.impl.DefaultTranslateToolService;
import com.fane.whisper.annotation.I18nMapping;
import com.fane.whisper.aspect.I18nResourceAspect;
import com.fane.whisper.service.impl.JDBCTranslateItemStore;
import com.fane.whisper.util.TranselateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Slf4j
@Transactional
@Aspect
@Order(1)
public class I18nTranslateService {


    private TranslateItemStore translateItemStore;

    private TranslateToolService translateToolService;

    @Around("@annotation(com.fane.whisper.annotation.I18nTranslate)")
    protected Object aroudAdivce(ProceedingJoinPoint jp) throws Throwable {
        Object rt = jp.proceed();
        rt = translate(rt, getCurrentLanguage());
        return rt;
    }


    /**
     * @param applicationContext   requred, current applicationContext
     */
    public I18nTranslateService(ApplicationContext applicationContext) {


        this.translateToolService = new DefaultTranslateToolService();
        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");

        if (dataSource != null) {
            this.translateItemStore = new JDBCTranslateItemStore(dataSource);
        } else {
            throw new RuntimeException("translateItemStore not configured. Please init with valid translateItemStore,or config a valid datasource");
        }

    }


    /**
     * @param translateToolService nullable, to get current request language
     * @param applicationContext   requred, current applicationContext
     */
    public I18nTranslateService(ApplicationContext applicationContext, TranslateToolService translateToolService) {


        this.translateToolService = translateToolService;
        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");

        if (dataSource != null) {
            this.translateItemStore = new JDBCTranslateItemStore(dataSource);
        } else {
            throw new RuntimeException("translateItemStore not configured. Please init with valid translateItemStore,or config a valid datasource");
        }

    }


    /**
     * @param translateItemStore   nullable, interface implements to get i18n items,default using a jdbc store
     * @param translateToolService nullable, to get current request language
     * @param applicationContext   requred, current applicationContext
     */
    public I18nTranslateService(ApplicationContext applicationContext, TranslateItemStore translateItemStore, TranslateToolService translateToolService) {

        this.translateItemStore = translateItemStore;
        this.translateToolService = translateToolService;
        DataSource dataSource = (DataSource) applicationContext.getBean("dataSource");
        if (this.translateItemStore == null) {
            if (dataSource != null) {
                this.translateItemStore = new JDBCTranslateItemStore(dataSource);
            } else {
                throw new RuntimeException("translateItemStore not configured. Please init with valid translateItemStore,or config a valid datasource");
            }

        }


        if (this.translateToolService == null) {
            this.translateToolService = new DefaultTranslateToolService();
        }


        //regist aspect dynamicly

        I18nResourceAspect i18nResourceAspect = new I18nResourceAspect(this);

        applicationContext.getAutowireCapableBeanFactory().autowireBean(i18nResourceAspect);
        applicationContext.getAutowireCapableBeanFactory().initializeBean(i18nResourceAspect, "i18nResourceAspect");

    }


    public void setI18nItemStore(TranslateItemStore translateItemStore) {
        this.translateItemStore = translateItemStore;

    }

    public void setTranslateToolService(TranslateToolService translateToolService) {
        this.translateToolService = translateToolService;
    }

    public Object translate(Object i18nObject, String lang) {
        String response = "";
        try {
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new SimpleModule() {
                @Override
                public void setupModule(Module.SetupContext context) {
                    super.setupModule(context);

                    context.addBeanSerializerModifier(new BeanSerializerModifier() {
                        @Override
                        public List<BeanPropertyWriter> changeProperties(
                                SerializationConfig config, BeanDescription beanDesc,
                                List<BeanPropertyWriter> beanProperties) {
                            for (int i = 0; i < beanProperties.size(); i++) {
                                BeanPropertyWriter writer = beanProperties.get(i);
                                if (writer.getAnnotation(I18nMapping.class) != null) {
                                    beanProperties.set(i, new I18nWriter(writer));
                                }


                            }
                            return beanProperties;
                        }
                    });
                }
            });


            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            mapper.registerModule(javaTimeModule);
            response = mapper.writeValueAsString(i18nObject);
            List<String> oids = TranselateUtil.getI18nOids(response);
            List<I18nTranslateItemDTO> list = translateItemStore.selectByOidsAndLang(oids, lang);
            Map<String, Map<String, String>> map = TranselateUtil.getI18nTranslateItem(list);
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);


            response = TranselateUtil.translate(response, map);

            //兼容常用的缺少无参构造函数的返回类
            //目前包括
            //            ResponseEntity
            if (i18nObject.getClass().equals(ResponseEntity.class)) {

                return mapper.readValue(response, I18nResponseEntity.class);
            }
            return mapper.readValue(response, i18nObject.getClass());

        } catch (Exception e) {
            log.error("translate error", e);
            return i18nObject;
        }
    }

    public String getCurrentLanguage() {

        if (translateToolService == null) {
            translateToolService = new DefaultTranslateToolService();
        }

        return translateToolService.getCurrentLanguage();
    }

    public Boolean createOrUpdateI18nItems(List<I18nTranslateItemDTO> i18nTranslateItemDTOList){
        return translateItemStore.createOrUpdateI18nItems(i18nTranslateItemDTOList);

    }
}
