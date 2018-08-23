package com.fane.whisper.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class I18nTranslateItemDTO {

    private String i18nKey;
    private String language;
    private String code;
    private String name;
    private String enabled;
    private String deleted;
}
