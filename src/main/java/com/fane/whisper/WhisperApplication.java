package com.fane.whisper;

import com.fane.whisper.dto.I18nTranslateItemDTO;
import com.fane.whisper.service.I18nTranslateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class WhisperApplication {

	public static void main(String[] args)
	{
		ConfigurableApplicationContext configurableApplicationContext= SpringApplication.run(WhisperApplication.class, args);
		I18nTranslateService i18nTranslateService=new I18nTranslateService(configurableApplicationContext);



	}

	@Autowired
	I18nTranslateService i18nTranslateService;

	public Boolean createI18nItems(){
		List<I18nTranslateItemDTO> i18nTranslateItemDTOS = new ArrayList<>();
		i18nTranslateItemDTOS.add(I18nTranslateItemDTO.builder().i18nKey("1").code("name").language("en").name("department english name").build());
		return i18nTranslateService.createOrUpdateI18nItems(i18nTranslateItemDTOS);
	}
}
