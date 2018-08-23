package com.fane.whisper.util;

import com.alibaba.fastjson.JSONObject;
import com.fane.whisper.dto.I18nTranslateItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import java.util.*;

@Slf4j
public class TranselateUtil {
    public static final String I18N_TAG="*i18n";
    public static final String ID_TAG = "*i18nid*-";
    private static final String Start = "start";
    private static final String End = "end";
    public static List<String> getI18nIds(String input){

        List<String> ids=new ArrayList<>();
        int curLoc=0;
        int idLoc=0;
        int idLocEnd=0;
        String currId="";
        while (idLocEnd>=0){
            idLoc= input.indexOf(ID_TAG, curLoc);
            if (idLoc > 0) {
            idLocEnd=input.indexOf('\"',idLoc);
                if(idLocEnd>0) {
                    currId = input.substring(idLoc, idLocEnd).replace(ID_TAG,"");
                }else {
                    break;
                }
            }else {
                break;
            }
            curLoc = idLocEnd;
            System.out.println(currId);
            ids.add(currId);
        }
        return ids;
    }



    public static String replaceI18n(String input, Map<String, Map<String, String>> i18nResource){


        for(String id:i18nResource.keySet()){
            for(String itemCode:i18nResource.get(id).keySet()){

                String codePrefix = I18N_TAG + itemCode + "-";

                String replacement = TranselateUtil.getFirstMatch(input, codePrefix, input.indexOf(id));
                if(StringUtils.isNotEmpty(replacement)) {
                    input = input.replaceFirst(replacement, i18nResource.get(id).get(itemCode));
                }
            }

        }

        return input.replaceAll(ID_TAG,"").replaceAll(I18N_TAG,"");

    }

    //返回第一个匹配的code字段对应的字串
    private static  String getFirstMatch(String input,String match, final int startLoc){

        int start;
        int end;

        start=input.indexOf(match,startLoc);
        if(start>0 && startLoc>0) {
            end = input.indexOf('\"', start);
            return input.substring(start, end);
        }else {
            return "";
        }

    }

    /**
     * 找出字符串中所有需要翻译的对象OID
     * @param input
     * @return
     */
    public static List<String> getI18nOids(String input){
        List<String> ids=new ArrayList<>();
        int curLoc=0;
        int idLoc=0;
        int idLocEnd=0;
        String currId="";
        try {
            while (idLocEnd >= 0) {
                idLoc = input.indexOf(ID_TAG, curLoc);
                if (idLoc > 0) {
                    idLocEnd = input.indexOf('\"', idLoc);
                    if (idLocEnd > 0) {
                        currId = input.substring(idLoc, idLocEnd).replace(ID_TAG, "");
                    } else {
                        break;
                    }
                } else {
                    break;
                }
                curLoc = idLocEnd;
                ids.add(currId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            log.info("需要翻译的Oid->{}",ids);
            return ids;
        }
    }

    /**
     * 将数据库查询到的翻译记录转化为以对象OID进行划分的格式
     * @param list
     * @return
     */
    public static Map<String,Map<String,String>> getI18nTranslateItem(List<I18nTranslateItemDTO> list){
        Map<String,Map<String ,String>> map =  new HashMap<>();
        for(I18nTranslateItemDTO item : list){
            if(CollectionUtils.isEmpty(map.get(item.getI18nKey()))){
                Map<String,String> codeName = new HashMap<>();
                codeName.put(item.getCode(),item.getName());
                map.put(item.getI18nKey(),codeName);
            }else {
                Map<String,String> codeName =  map.get(item.getI18nKey());
                codeName.put(item.getCode(),item.getName());
                map.put(item.getI18nKey(),codeName);
            }
        }
        return map;
    }

    /**
     * 替换需要翻译的字段
     * @param input
     * @param i18nResource
     * @return
     */
    public static String translate(String input, Map<String, Map<String, String>> i18nResource) {
        for(String id:i18nResource.keySet()){
            for(String itemCode:i18nResource.get(id).keySet()){
                String codePrefix = I18N_TAG + itemCode + "*-";
                String oidPrefix = ID_TAG + id;
                Map<String,Integer> map = getMatchOid(input,codePrefix,oidPrefix);
                if((int)map.get(Start)==-1||(int)map.get(End)==-1){
                    log.debug("对象->{},Code->{}在当前字符串中不存在",id,itemCode);
                    continue;
                }else{
                    int start = (int)map.get(Start);
                    int end = (int)map.get(End);
                    StringBuilder string = new StringBuilder(input);
                    string.replace(start,end,i18nResource.get(id).get(itemCode));//进行翻译内容的替换
                    input = string.toString();
                }
            }

        }
        String pattern = "\\*i18n[a-z0-9A-Z\\*]+-";//去除未翻译的标记字段的标记
        return input.replaceAll(pattern,"");
    }

    /**
     * 返回翻译匹配的字段的起始和终止坐标
     * @param input
     * @param match
     * @param oid
     * @return
     */
    private static Map<String,Integer> getMatchOid(String input,String match,String oid){
        int end = -1;
        int start = -1;
        Map<String,Integer> subindex = new HashMap<>();
        subindex.put(Start,start);
        subindex.put(End,end);
        int startmatch = input.indexOf(match);
        int startoid = input.indexOf(oid);
        if(startmatch==-1||startoid==-1){
            return subindex;
        }
        List<Integer> list = findAllIndex(input,match,startmatch);
        if(list.size()==1){//只有一项匹配，直接返回匹配下标
            end = input.indexOf('\"', startmatch);
            subindex.put(Start,startmatch);
            subindex.put(End,end);
            return subindex;
        }else{//有多项内容匹配，则需要进行检查，找到需要匹配的
            for(Integer index : list){
                if(startoid<index){
                    if(findCurrentLevel(startoid,index,input)){
                        end = input.indexOf('\"', index);
                        subindex.put(Start,index);
                        subindex.put(End,end);
                        break;
                    }
                }else{
                    if(findCurrentLevel(index,startoid,input)){
                        end = input.indexOf('\"', index);
                        subindex.put(Start,index);
                        subindex.put(End,end);
                        break;
                    }
                }
            }
            return subindex;
        }
    }

    /**
     * 返回所有匹配翻译字段code的code起始位置
     * @param input
     * @param match
     * @param startLoc
     * @return
     */
    private static List<Integer> findAllIndex(String input,String match, int startLoc){
        List<Integer> list = new ArrayList<>();
        while (startLoc!=-1){
            list.add(startLoc);
            startLoc = input.indexOf(match,startLoc+1);
        }
        return list;
    }

    /**
     * 判断当前替换的code字段是否和当前oid处于同一层级，即是否为同一对象
     * @param left 左边待匹配内容的起始位置
     * @param right 右边待匹配内容的起始位置
     * @param input
     * @return
     */
    private static boolean findCurrentLevel(int left,int right,String input){
        for (int i = 0;i<3;i++){
            left = input.substring(0,left).lastIndexOf('\"');//找到左侧待匹配内容对应的key的起始位置
        }
        right = input.indexOf('\"',right);//找到左侧待匹配内容对应的value的结束位置
        input = input.substring(left,right+1);
        input = "{" + input + "}";//将截取下来的字符串构建成json格式
        try {
            JSONObject JSON = JSONObject.parseObject(input);//将字符串转化json,如果能转化则说明当前code和oid为同一对象
        }catch (Exception e){
            return false;
        }
        return true;
    }

    private static String replace(String input,String pattern,String name){
      return input.replaceFirst(pattern, name);
    }

    public static void main(String[] args){
        String input= "{\n" +
                "    \"statusCode\": \"S\",\n" +
                "    \"msg\": null,\n" +
                "    \"rows\": [\n" +
                "        {\n" +
                "            \"configItem\": {\n" +
                "                \"id\": \"i18nid-996642904355127298\",\n" +
                "                \"isEnabled\": true,\n" +
                "                \"isDeleted\": false,\n" +
                "                \"createdDate\": \"2018-05-16T14:46:00+08:00\",\n" +
                "                \"createdBy\": 180113,\n" +
                "                \"lastUpdatedDate\": \"2018-05-16T14:46:00+08:00\",\n" +
                "                \"lastUpdatedBy\": 180113,\n" +
                "                \"itemName\": \"i18nitemName-发票生成费用管控\",\n" +
                "                \"itemCode\": \"RECEIPT_TO_INVOICE_CONTROL\",\n" +
                "                \"description\": \"是否允许发票生成费用\",\n" +
                "                \"defaultValue\": \"Y\",\n" +
                "                \"defaultValueDesc\": \"3条默认规则，优先级从高到低分别为：\\n1.发票不重复／重复未知、且不作废／作废未知、且返回001-查验成功，可生成费用\\n2.发票不重复／重复未知、且不作废／作废未知、且返回10023-抬头不存在，可生成费用\\n3.其他情况均不可生成费用\\n默认规则优先级低于自定义规则。注：生成费用时还需要判断其他不在配置中心的校验，如必填、金额大小比较。\",\n" +
                "                \"resultType\": \"LIST\",\n" +
                "                \"dataConfig\": \"{\\\"listCode\\\":\\\"3006\\\",\\\"listType\\\":\\\"SYSTEM\\\"}\"\n" +
                "            },\n" +
                "            \"unsettled\": \"N\",\n" +
                "            \"configRules\": [\n" +
                "                {\n" +
                "                    \"configRule\": {\n" +
                "                        \"id\": \"1003646973717073922\",\n" +
                "                        \"isEnabled\": true,\n" +
                "                        \"isDeleted\": false,\n" +
                "                        \"createdDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                        \"createdBy\": 120651,\n" +
                "                        \"lastUpdatedDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                        \"lastUpdatedBy\": 120651,\n" +
                "                        \"configItemId\": \"996642904355127298\",\n" +
                "                        \"ruleName\": null,\n" +
                "                        \"description\": null,\n" +
                "                        \"priority\": \"2\",\n" +
                "                        \"value\": \"Y\",\n" +
                "                        \"valueName\": \"可生成费用\",\n" +
                "                        \"valueDesc\": \"可生成费用\",\n" +
                "                        \"itemCode\": \"RECEIPT_TO_INVOICE_CONTROL\",\n" +
                "                        \"tenantId\": \"943460872925614082\"\n" +
                "                    },\n" +
                "                    \"configRuleFactorMap\": {\n" +
                "                        \"CANCELLED_RECEIPT\": {\n" +
                "                            \"id\": \"1003646973788377089\",\n" +
                "                            \"isEnabled\": true,\n" +
                "                            \"isDeleted\": false,\n" +
                "                            \"createdDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                            \"createdBy\": 120651,\n" +
                "                            \"lastUpdatedDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                            \"lastUpdatedBy\": 120651,\n" +
                "                            \"configItemId\": \"996642904355127298\",\n" +
                "                            \"configRuleId\": \"1003646973717073922\",\n" +
                "                            \"configRuleFactorId\": \"996642905844105218\",\n" +
                "                            \"factorCode\": \"CANCELLED_RECEIPT\",\n" +
                "                            \"factorValue\": \"Y\",\n" +
                "                            \"factorValueDesc\": \"是\"\n" +
                "                        },\n" +
                "                        \"SALES_PARTY\": {\n" +
                "                            \"id\": \"1003646973826125826\",\n" +
                "                            \"isEnabled\": true,\n" +
                "                            \"isDeleted\": false,\n" +
                "                            \"createdDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                            \"createdBy\": 120651,\n" +
                "                            \"lastUpdatedDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                            \"lastUpdatedBy\": 120651,\n" +
                "                            \"configItemId\": \"996642904355127298\",\n" +
                "                            \"configRuleId\": \"1003646973717073922\",\n" +
                "                            \"configRuleFactorId\": \"996642905927991297\",\n" +
                "                            \"factorCode\": \"SALES_PARTY\",\n" +
                "                            \"factorValue\": null,\n" +
                "                            \"factorValueDesc\": null\n" +
                "                        },\n" +
                "                        \"VAT_INCLUSIVE\": {\n" +
                "                            \"id\": \"1003646973872263170\",\n" +
                "                            \"isEnabled\": true,\n" +
                "                            \"isDeleted\": false,\n" +
                "                            \"createdDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                            \"createdBy\": 120651,\n" +
                "                            \"lastUpdatedDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                            \"lastUpdatedBy\": 120651,\n" +
                "                            \"configItemId\": \"996642904355127298\",\n" +
                "                            \"configRuleId\": \"1003646973717073922\",\n" +
                "                            \"configRuleFactorId\": \"996642905911214082\",\n" +
                "                            \"factorCode\": \"VAT_INCLUSIVE\",\n" +
                "                            \"factorValue\": null,\n" +
                "                            \"factorValueDesc\": null\n" +
                "                        },\n" +
                "                        \"TENANT\": {\n" +
                "                            \"id\": \"1003646973910011905\",\n" +
                "                            \"isEnabled\": true,\n" +
                "                            \"isDeleted\": false,\n" +
                "                            \"createdDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                            \"createdBy\": 120651,\n" +
                "                            \"lastUpdatedDate\": \"2018-06-04T22:37:40+08:00\",\n" +
                "                            \"lastUpdatedBy\": 120651,\n" +
                "                            \"configItemId\": \"996642904355127298\",\n" +
                "                            \"configRuleId\": \"1003646973717073922\",\n" +
                "                            \"configRuleFactorId\": \"996642905764413441\",\n" +
                "                            \"factorCode\": \"TENANT\",\n" +
                "                            \"factorValue\": \"943460872925614082\",";

        Map<String, Map<String, String>> i18nResource = new HashMap<>();
        Map<String, String> mapItem = new HashMap<>();
        mapItem.put("itemName", "it chinese lang");
        i18nResource.put("996642904355127298", mapItem);



        System.out.println(TranselateUtil.replaceI18n(input, i18nResource));



    }
}

