# redis缓存操作器

## 需求描述

系统开发中，大多数功能都会用到redis来缓存数据，以此来提高数据的读取速度，进而提升系统的响应时间。但在高并发场景下缓存过期后，
如果处理不当将会导致一系列问题（缓存穿透、缓存击穿、缓存雪崩），因此，该缓存操作器主要是用来解决缓存使用过程中遇到的一系列问题。

### 存在问题
- 缓存穿透：当查询数据时没有命中缓存，则请求会到持久层数据库中查询，当请求量比较大时，这会给持久层数据库造成很大的压力
- 缓存击穿：当缓存中的大量key在同一时间过期，会导致大量请求直接到持久层数据库查询，大量请求瞬间可能会压垮数据库
- 缓存雪崩：当缓存层出现了错误，不能正常工作，于是请求都会直接请求持久层数据库，导致持久层数据库也收到影响

## 功能特点
- 单线程刷新：当缓存没有命中某个key或者缓存过期时，控制只能有一个线程可以查询数据库刷新缓存
- 异步刷新：当缓存没有命中某个key或者缓存过期时，异步刷新缓存，不会阻塞请求
- 缓存降级：当缓存没有命中某个key或者缓存过期时，如果缓存中存在旧值则返回缓存中的旧值
- 自定义过滤器：实现前置过滤和后置过滤，前置过滤可以用于过滤部分不符合条件的key，后置过滤可以取得key与返回的结果，支持SPI或程序手动注入过滤器
- 失败降级： 如果访问redis错误，则切断与redis的操作，走降级逻辑(自定义mock降级)，期间重试(频率30s)redis查看是否恢复，恢复则继续提供服务

## 使用
### 字符串对象
```java
CacheOperator cacheOperator = new RedisCacheOperator();
String key = "sayHello";
long expire = 10 * 60 * 1000;  //10 分钟

String cacheValue = cacheOperator.getString(key, expire, () -> {
    //执行业务逻辑，获取值
    String value = "hello world!";
    return value;
});
```
### map对象
```java
CacheOperator cacheOperator = new RedisCacheOperator();

String mapKey = "mapKey";
long expire = 10 * 60 * 1000;  //10 分钟
Map<String, String> res = cacheOperator.getAllMap(mapKey, expire, () -> {
    //执行业务逻辑，获取值
    Map<String, String> data = new HashMap<>();
    data.put("value", "mapValue");
    return data;
});
```
### mock降级使用
* 方式一：使用SPI，则在META-INF/services/live.xsg.cacheoperator.mock.Mock文件中添加Mock实现类
* 方式二：代码注入mock降级逻辑
```java
String key = "sayHello";

MockRegister.getInstance().register((k, cacheOperator, method) -> {
    if (key.equals(k)) {
        return "i am mock value.";
    }
    return null;
});
```

### filter过滤器使用
* 方式一：使用SPI，则在META-INF/services/live.xsg.cacheoperator.filter.Filter文件中添加Filter实现类
* 方式二：代码注入filter实现逻辑
```java
String ignoreKey = "ignoreKey";
FilterChain.getInstance().addFilter(new Filter() {
    @Override
    public boolean preFilter(String key) {
        if (ignoreKey.equals(key)) {
            return false;
        }
        return true;
    }

    @Override
    public void postFilter(String key, Object result) {
        System.out.println("key:" + key + " 查询结果:" + result);
    }
});
```
