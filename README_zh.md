# Whisper
[Wisper](https://github.com/benhouse1987/whisper)是一个轻量级I18n翻译框架，简单易用，性能出色，并且扩展简单。


**让我们看看如何开始使用Whisper!**

假设你有一个基于Spring的项目，并且项目的dataSource已经配置好。


**可以在下面链接得到Whisper的样例代码**
 
 [Whisper Demo](https://github.com/benhouse1987/whisper-demo)
 
## 准备工作
### 创建I18n翻译表
在你的服务基于的数据库中，创建一张i18n表，用来存储翻译关系。

如果恰好使用Mysql，你可以直接使用以下脚本创建这张表：

```
CREATE TABLE `i18n_item` (
  `i18n_key` varchar(36) NOT NULL,
  `language` varchar(20) NOT NULL,
  `i18n_code` varchar(45) NOT NULL,
  `i18n_name` longtext,
  `is_enabled` bigint(1) NOT NULL,
  `is_deleted` bigint(1) NOT NULL,
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `index_key_lang_code` (`i18n_key`,`language`,`i18n_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

```
其余数据库的实现我们后续会很快给出，也欢迎大家贡献其他库的实现方式。

### 添加Maven依赖
将Whisper的Maven依赖加入到项目中。

```
<dependency>
    <groupId>io.github.benhouse1987</groupId>
    <artifactId>whisper</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>>
```

### 初始化翻译Service
这里我们使用一个最基本的初始化方式。

该初始化的Service固定将所有内容翻译成中文。

事实上，你可以直接将这个类复制到你的项目中。

```
@Configuration
@EnableResourceServer
public class I18nTranslateConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public I18nTranslateService init() {
        //this basic translator service always translate your data in  Chinese language
        return new I18nTranslateService(applicationContext);

    }

}
```

**现在，Whisper已经准备完毕！**


## 尝试一下！
### Demo 1 
### 将部门名称翻译成中文

首先我们写一个Department类，像这样：


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

假如我们希望把部门名称翻译成中文，只需要加两个`I18nMapping`注解，分别用来指定待翻译实体的id以及，要翻译的字段。

在i18n_item表中，我们初始化一条中文翻译：


```
insert into `i18n_item`  values ( '1', 'zh_cn', 'name', '中文部门', '1', '0', '2018-08-23 21:41:24');
insert into `i18n_item`  values ( '1', 'en', 'name', 'english department name', '1', '0', '2018-08-23 21:41:24');
```


**现在让我们看看效果!**

在Controller中，添加一个示例的api,这个api中，我们返回一个名称为`english department name`的部门：

```

@Controller
public class DemoController {

    @RequestMapping(value = "/getOneDepartment")
    @I18nTranslate
    @ResponseBody
    public Department getOneDepartment() {
        Department department = new Department();
        department.setDepartmentId(1L);
        department.setDepartmentName("english department name");
        department.setDescription("english description");
        department.setLevel(0);
        throw new RuntimeException("i18n exception test");
        
    }
}

```

项目运行后，调用该api，你会发现Whisper把`departmentName`字段翻译成了中文！

```
{
    "departmentId": 1,
    "departmentName": "中文部门",
    "description": "english description",
    "level": 0
}
```

### Demo 2
#### 按指定的语言翻译（比如用户的当前语言）
在Demo 1中，我们总是把任何东西翻译成中文。

如果我们想按照api的调用者语言，返回相应语言的翻译怎么办呢？


非常简单！


创建一个语言翻译工具类，这个类将帮助我们决定按何种语言翻译。

这个类需要实现TranslateToolService 接口。

下面是个简单的示意：
```
public class MyTranslateToolService implements TranslateToolService {


    public String getCurrentLanguage() {

        //some exist logic to get current user language
        //for example from token or some other table
        return someClass.getCurrentUserLanguage();
    }


}

```

我们需要在`I18nTranslateConfig`中，使用新的构造函数初始化I18nTranslateService。

该初始化指定了语言工具类为我们刚才新建的`MyTranslateToolService`。


```
@Configuration
@EnableResourceServer
public class I18nTranslateConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public I18nTranslateService init() {        
        return new I18nTranslateService(applicationContext,MyTranslateToolService);

    }

}
```
现在，Whisper将按照`MyTranslateToolService.getCurrentLanguage()`方法的返回语言进行翻译！


### Demo 3
### 一个国际化异常的例子
也许你希望将返回的错误信息也按照当前用户的语言来翻译。

使用Whisper框架做这件事情非常简单！


#### 创建一个错误信息DTO
假设我们将`errorCode`属性作为i18n的翻译ID。

我们希望将`message`属性翻译成不同语言。

只需要加两个I18nMapping 的 annotation，非常简单。



```@Data
   @Builder
   public class ExceptionDetail {
       @I18nMapping(i18nCode = "message")
       private String message;
   
       @I18nMapping(i18nCode = "id")
       private String errorCode;
   }
```
#### 创建一个 ControllerAdvice来处理异常

```

@ControllerAdvice
public class ResourceAdvice {
    @ExceptionHandler(RuntimeException.class)
    @I18nTranslate
    public ResponseEntity<ExceptionDetail> handleValidationException(RuntimeException e) {

        ExceptionDetail detail = ExceptionDetail.builder().errorCode("e001").message("cccc").build();

        return new ResponseEntity(detail, HttpStatus.BAD_REQUEST);
    }
}

```

#### 在 i18n_item 表中，初始化两条翻译项。



```
insert into `i18n_item`  values ( 'e001', 'zh_cn', 'message', '中文报错', '1', '0', '2018-08-23 21:41:24');
insert into `i18n_item`  values ( 'e001', 'en', 'message', 'english error message', '1', '0', '2018-08-23 21:41:24');
```


现在，你抛出的所有code为e001的报错都将被翻译成指定的语言，修改DemoController自己试一试吧！


```

@Controller
public class DemoController {

    @RequestMapping(value = "/getOneDepartment")
    @I18nTranslate
    @ResponseBody
    public Department getOneDepartment() {
        Department department = new Department();
        department.setDepartmentId(1L);
        department.setDepartmentName("english department name");
        department.setDescription("english description");
        department.setLevel(0);
        throw new RuntimeException("i18n exception test");
        
    }
}

```





## 如何创建i18n翻译项
我们提供了一个简单的api，帮助你维护i18n翻译项，你可以通过这个api轻松地将你的项目与Whisper集成起来。


这里是一段样例代码：
```
        @Inject
	I18nTranslateService i18nTranslateService;

	public Boolean createI18nItems(){
		List<I18nTranslateItemDTO> i18nTranslateItemDTOS = new ArrayList<>();
		i18nTranslateItemDTOS.add(I18nTranslateItemDTO.builder().i18nKey("1").code("name").language("en").name("department english name").build());
		return i18nTranslateService.createOrUpdateI18nItems(i18nTranslateItemDTOS);
	}
``` 

**注意**

我们使用i18nKey,i18nCode,language，作为联合唯一索引。所以请保证所有的被指定为i18n id的属性值全局唯一(@I18nMapping(i18nCode = "id"))。
最佳实践是使用UUID作为i18n id，你可能需要为需要翻译的表添加一列i18n_id，并用随机的UUID填充。
