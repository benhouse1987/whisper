# whisper
Whisper is an I18n translator framework to add i18n support for your service.

It's really fast and easy to use.

**Let's get a quick start!**

I will assume you have a spring based service project, and it's dataSource has been set correctly.


## Prepare
### Create an i18n table
create an i18n table in the database your service based on

```
CREATE TABLE `i18n_item` (
  `i18n_key` varchar(36) NOT NULL,
  `language` varchar(20) NOT NULL,
  `i18n_code` varchar(45) NOT NULL,
  `i18n_name` longtext,
  `is_enabled` bigint(1) NOT NULL,
  `is_deleted` bigint(1) NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  UNIQUE KEY `index_key_lang_code` (`i18n_key`,`language`,`i18n_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

```
### Add maven dependency import
Add whisper maven dependency to your service project.
```
<dependency>
    <groupId>io.github.benhouse1987</groupId>
    <artifactId>whisper</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>>
```

### Init translate service for your service
Let's try with a basic init here. You can paste this sample i18n config class directly into your service.
```
@Configuration
@EnableResourceServer
public class I18nTranslateConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public I18nTranslateService init() {
        ## a translator service always translate your data in  Chinese language
        return new I18nTranslateService(applicationContext);

    }

}
```

Now you're ready!


## Try it
### Demo1 
### Translate department name in different language
Say we want to translate department's name attribute to Chinese.

Simply add two I18nMapping annotation here.

We build a Department class like this:



```

@Data
public class Department {
    // this is the department id , we will translate the department name by this id
    @I18nMapping(i18nCode = "id")
    private Long departmentId;
    
    // this is the attribute we need to translate
    @I18nMapping(i18nCode = "name")
    private String departmentName;

    private String description;
    
    private Integer level;
    
    
}

```


Init an Chinese translation in i18n_items tabel.

insert into `i18n_item`  values ( '1', 'zh_cn', 'name', '中文部门', '1', '0', '2018-08-23 21:41:24');



**Now let's do the magic!**


Try with this simple function in your controler
```
  @RequestMapping(value = "/getOneDepartment")
    @I18nTranslate
    @ResponseBody
    public Department getOneDepartment(){
        Department department = new Department();
        department.setDepartmentId(1L);
        department.setDepartmentName("english department name");
        department.setDescription("english description");
        department.setLevel(0);
        return department;
    }
```


You will see this magic work as the api resulted in Chinese!
```
{
    "departmentId": 1,
    "departmentName": "中文部门",
    "description": "english description",
    "level": 0
}
```

### Demo2
#### Response in an specific(say current user's)  language
In Demo 1 we return everything in Chinese.

What if we want to return response in the api caller's language?

It's easy!

Create a language tool class, which help us decide which language we should translate to.
This class should implement interface TranslateToolService.

```
public class MyTranslateToolService implements TranslateToolService {


    public String getCurrentLanguage() {

        //some exist logic to get current user language
        //for example from token or some other table
        return getCurrentUserLanguage();
    }


}

```

We should init translatService with this new customized language tool class

```
@Configuration
@EnableResourceServer
public class I18nTranslateConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public I18nTranslateService init() {
        ## a translator service always translate your data in  Chinese language
        return new I18nTranslateService(applicationContext,MyTranslateToolService);

    }

}
```


Now every time before whisper translate something, it  will invoke **MyTranslateToolService.getCurrentLanguage()** to decide in witch language to translate.

## Create i18n translation items
We provide an neat api to help you to maintain i18n translate items.

Here's a sample code
```
        @Inject
	I18nTranslateService i18nTranslateService;

	public Boolean createI18nItems(){
		List<I18nTranslateItemDTO> i18nTranslateItemDTOS = new ArrayList<>();
		i18nTranslateItemDTOS.add(I18nTranslateItemDTO.builder().i18nKey("1").code("name").language("en").name("department english name").build());
		return i18nTranslateService.createOrUpdateI18nItems(i18nTranslateItemDTOS);
	}
``` 

**Important Note**

We use i18nKey,i18nCode,language as an union unique key in table i18n_items.
