# whisper
Whisper is an I18n translator framework to add i18n support for our service.

It's really fast and easy to use.

Now let's get a quick start!

# quick start
## prepare
### create an i18n table
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
### add maven dependency import
```
       <dependency>
            <groupId>com.huilianyi</groupId>
            <artifactId>whisper</artifactId>
            <version>1.0.1-SNAPSHOT</version>
        </dependency>
```

### init translate service for your service
Let's try with a basic init here. You can even paste this i18n config class direct into your service.
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

Now you're ready to get i18n capability for your service!


## try it
### demo1 translate department name in different language
Let's assume you have a Department class like this.

Say we want to translate department's name attribute to Chinese.

Simply add two I18nMapping annotation here.

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

Now let's do the magic!

Init an Chinese translation in i18n_items tabel.

insert into `i18n_item`  values ( '1', 'zh_cn', 'name', '中文部门', '1', '0', '2018-08-23 21:41:24');



Try with this simple function
```
    @I18nTranslate
    public Department getOneDepartment(){
        Department department=new Department();
        department.setDepartmentId(1L);
        department.setDepartmentName("english department name");
        department.setDescription("english description");
        department.setLevel(0);
        
        
        return department;
        
    }
    
    
     public void tryDepartmentTranslation(){
        Department department= this.getOneDepartment();
        System.out.println("deparment name is "+department.getDepartmentName());
        
    }
```
