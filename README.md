# 1. 项目基本情况
## 项目介绍
> 以电商为场景设计的一个秒杀系统，并对系统进行逐步优化，减少访问压力。

## 项目技术栈 
* Spring Boot
* MyBatis
* MySQL
* Redis
* RabbitMQ

## 运行环境 
* Java版本：JDK8
* 开发工具：eclipse 
* 项目管理工具：Maven
* 数据源：Druid
* Linux版本：CENTOS6
* Redis版本：4.0.2
* RabbitMQ版本：3.6.14


# 2. 项目搭建
## 登录验证
* 对密码进行2次`MD5加密`，为了防止彩虹表反查推出MD5原来的值
* 用户端 password1 = MD5（明文密码+固定salt）（在login.html，用js实现）
* 服务端 password2 = MD5（password1 +随机salt）
* 验证业务逻辑：获取前端传递的mobile和formPass（已第一次MD5），用mobile作为ID验证用户是否存在，把formPass加上当前user的salt进行第二次MD5，判断与数据库的密码（MD5化）是否相等
* 验证工具类文件：`MD5util.java、ValidatorUtil.java`

## JSR303校验
* 通过自定义注解@interface编写IsMobile，在IsMobile下的 @Constraint注解填写需要校验的类是自定义的IsMobileValidator.class。然后重写以下2个方法
* IsMobile类中，重写message()字段写上手机号格式错误的信息，自定义required()字段来判断当前值是否为必须项，默认true
* IsMobileValidator类中，继承ConstraintValidator接口，重写initialize和isValid方法
* 文件：`IsMobile.java、IsMobileValidator.java `

## 全局异常处理
* @ControllerAdvice注解标注某个类为全局异常拦截器，在里面 @ExceptionHandler标注某个方法为异常处理方法
* 作用是利用springboot提供的注解把程序运行时的异常都集中到一个类中，便于管理，保证业务代码的简洁

## 分布式session
* 为了解决用户请求在不同机器上session不同步的问题
* 用户验证通过后，生成token（UUID实现），用来标识此用户，与user一起写入缓存层redis，然后token写到cookie中，传递给客户端
* `总结：token表示用户，用缓存层来管理session，而不是存到服务器中`

## 自定义参数解析器
* 定义一个类实现HandlerMethodArgumentResolver接口，重写supportsParameter和resolveArgument方法，用来返回controller中的形参对象
* 定义一个类继承WebMvcConfigurerAdapter接口，重写addArgumentResolvers方法并把解析器（注入）注册到MVC中
* `文件：（解析器）UserArgumentResolver.java、WebConfig.java`


# 3. 秒杀逻辑设计
## 数据库设计
* 数据表把普通的用户表和商品表和秒杀成功的用户表和参与秒杀的商品表分离，原因是如果不分离，就要对用户表和商品表进行频繁修改，造成不必要的维护成本

## 秒杀过程设计
* 用户点击秒杀后，后端会获取前端传递的货物ID，查询到对应的货物对象，用来判断库存量不足、重复秒杀问题，检测通过才进行秒杀
* 秒杀分为3步：库存-1、创建商品订单、写入秒杀订单，把整个过程封装事务来处理
* 秒杀成功后跳转到秒杀订单详情页，用户选择支付


# 4. 项目优化（缓存篇）
## 页面缓存和URL缓存
* 在controller中，不直接return模板，而改为return渲染好的页面（取缓存，或手动渲染，返回页面）
* 具体是先从redis中取缓存，如果没有，再通过SpringWebContext和ViewResolver视图解析器手动渲染模板并保存到redis中，再返回页面
适合实时变化需求不大且大量并发访问的场景

## 对象缓存
* 在service中，不直接查询数据库，而先查询缓存（取缓存，或取数据库，返回对象）
* 具体是先从redis中取缓存，如果没有，再通过SpringWebContext和ViewResolver视图解析器手动渲染模板并保存到redis中，再返回页面
注：如果要对数据库的数据更新，必须把缓存也一致更新，更新顺序是先更新数据库，再更新缓存

## 使用缓存后压测结果
* 对商品列表接口进行压测，目前在1000并发下的QPS/吞吐量为954	

## 页面静态化
* 客户端第一次请求是通过服务器发送页面，把静态资源存储在本地，之后的请求是直接调用本地缓存，动态数据通过接口从服务器获取，实现前后端分离
* 页面直接跳转到静态页面，而不是由服务器跳转
* 具体是，浏览器不会动态生成数据，而是在缓存的静态页面上，通过ajax异步请求获取部分数据，然后渲染页面
* controller改为返回页面所需的动态数据的一个接口，被ajax异步调用，而不是跳转到一个页面

## 解决卖超问题
* 数据库本身有事务机制，在减少库存前加上库存量必须>0的判断条件
* 同时对订单表建立唯一索引，防止同一用户同时秒杀商品
* 


# 5. 项目优化（接口篇）
## 秒杀接口优化
步骤
* 系统初始化时，把库存数量加载到redis中；
* 初始化是通过SeckillController实现了InitializingBean接口，重写afterPropertiesSet方法来实现。
* 当用户秒杀时，redis预减库存，使用异步下单，生产者把请求放入MQ中，返回“排队中”的状态，库存不足则直接返回错误 （SeckillController中，把访问数据库改为用redis预减库存）
* 把请求出队，生成订单，减少库存
* 在消费者（Receiver）中，同样需要对队列中每一个消息判断库存是否不足、是否重复秒杀，之后才能成功秒杀
* 客户端通过轮询查询是否成功
* 在客户端中，利用ajax异步轮询服务器，直到消费者消费了一个秒杀消息并缓存在redis中，表示用户秒杀成功

压测结果：对秒杀接口压测，添加优化前，在5000并发量下QPS约为1000，优化后QPS约为1900
`总结：尽量减少对数据库的直接访问，把更多的请求放在缓存层中，对不满足条件的请求及时返回错误，就能减轻数据库的压力`


# 6. 安全策略
## 隐藏接口地址
作用：防止恶意用户直接获取静态地址，对接口进行大量请求
思路
* 添加一个生成秒杀地址的接口，和验证地址的接口
* 生成接口UUID类生成随机数，作为整个秒杀地址的前缀，写在redis中
* 秒杀开始前，先到新定义的请求接口获取秒杀地址
* 前端接收随机数，把它作为参数去请求秒杀接口，接口把随机数和秒杀地址拼接，与redis中已缓存的地址比较
* 秒杀接口传入秒杀地址Path，收到请求先验证Path

## 验证码
作用：错峰请求，使请求分散到不同的时间段
思路
* 服务器渲染页面时自动生成验证码，并计算结果，缓存在redis中
* 请求秒杀接口时，验证用户输入是否正确

## 接口限流
作用： 限制一定时间内接口访问的次数
思路
* 可以直接获取当前用户访问次数，缓存在redis中，每次请求秒杀都先判断次数是否超出限制，但容易造成冗余
* 定义一个自定义注解，标明属性
* 定义一个拦截器，继承HandlerInterceptorAdapter接口，重写preHandle方法，
* 定义一个threadLocal存放当前线程的信息
* 注册到继承WebMvcConfigurerAdapter的类（WebConfig）中
