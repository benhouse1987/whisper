package com.fane.whisper.service;

import com.fane.whisper.dto.I18nTranslateItemDTO;

import java.util.List;

public interface TranslateItemStore {
     List<I18nTranslateItemDTO> selectByOidsAndLang(List<String> i18nKeys, String lang);
     Boolean createOrUpdateI18nItems(List<I18nTranslateItemDTO> i18nTranslateItemDTOList);
}
