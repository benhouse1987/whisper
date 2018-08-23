package com.fane.whisper.service.impl;

import com.fane.whisper.dto.I18nTranslateItemDTO;
import com.fane.whisper.service.TranslateItemStore;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class JDBCTranslateItemStore implements TranslateItemStore {

    private static final String GET_I18N_ITEMS_SQL = "select * from atl_i18n_item where i18n_key in (:i18nKeys) and language= :language and is_enabled=1";
    private static final String CREATE_OR_UPDATE_I18N_ITEMS_SQL = "replace into atl_i18n_item values(:i18nKey,:language,:code,:name,:enabled,:deleted,now())";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JDBCTranslateItemStore(DataSource dataSource) {
        Assert.notNull(dataSource, "DataSource required");
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Boolean createOrUpdateI18nItems(List<I18nTranslateItemDTO> i18nTranslateItemDTOList) {


        i18nTranslateItemDTOList.forEach(i18nTranslateItemDTO -> {
            MapSqlParameterSource i18nKeysParam = new MapSqlParameterSource();
            i18nKeysParam.addValue("i18nKey", i18nTranslateItemDTO.getI18nKey());
            i18nKeysParam.addValue("language", i18nTranslateItemDTO.getLanguage());
            i18nKeysParam.addValue("code", i18nTranslateItemDTO.getCode());
            i18nKeysParam.addValue("name", i18nTranslateItemDTO.getName());
            i18nKeysParam.addValue("enabled", i18nTranslateItemDTO.getEnabled()==null? 1: i18nTranslateItemDTO.getEnabled());
            i18nKeysParam.addValue("deleted", i18nTranslateItemDTO.getDeleted()==null? 0: i18nTranslateItemDTO.getDeleted());

            jdbcTemplate.execute(CREATE_OR_UPDATE_I18N_ITEMS_SQL, i18nKeysParam, new PreparedStatementCallback<Object>() {
                public Object doInPreparedStatement(PreparedStatement var1) {
                    return true;
                }
            });

        });

        return true;
    }


    public List<I18nTranslateItemDTO> selectByOidsAndLang(List<String> i18nKeys, String lang) {
        MapSqlParameterSource i18nKeysParam = new MapSqlParameterSource();
        Set<String> ids=new HashSet<>(i18nKeys);
        i18nKeysParam.addValue("i18nKeys", ids);
        i18nKeysParam.addValue("language",lang);
        List<I18nTranslateItemDTO> i18nTranslateItemDTOS=  jdbcTemplate.query(this.GET_I18N_ITEMS_SQL, i18nKeysParam, new RowMapper<I18nTranslateItemDTO>() {

            public I18nTranslateItemDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                return I18nTranslateItemDTO.builder().i18nKey(rs.getString("i18n_key"))
                        .code(rs.getString("i18n_code"))
                        .language(rs.getString("language"))
                        .name(rs.getString("i18n_name"))
                        .enabled(rs.getString("is_enabled"))
                        .deleted(rs.getString("is_deleted"))
                        .build();
            }
        });

        return i18nTranslateItemDTOS;
    }

}
