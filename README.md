# redis缓存操作器

## 需求描述

系统开发中，大多数功能都会用到redis来缓存数据，以此来提高数据的读取速度，进而提升系统的响应时间。但在高并发场景下缓存过期后，
如果处理不当将会导致一系列问题（缓存穿透、缓存击穿、缓存雪崩），因此，该缓存操作器主要是用来解决缓存使用过程中遇到的一系列问题。

### 存在问题
- 缓存穿透：当查询数据时没有命中缓存，则请求会到持久层数据库中查询，当请求量比较大时，这会给持久层数据库造成很大的压力
- 缓存击穿：当缓存中的大量key在同一时间过期，会导致大量请求直接到持久层数据库查询，大量请求瞬间可能会压垮数据库
- 缓存雪崩：当缓存层出现了错误，不能正常工作，于是请求都会直接请求持久层数据库，导致持久层数据库也收到影响

## 概述

缓存主要用于加快数据的读取速度，一般添加缓存时，都需要设置缓存的过期时间，设置缓存过期时间的目的为了节约内存资源与保障缓存数据的最终一致性。

考虑这样一种情况，如果缓存的过期时间设置为五分钟，那么当缓存过期时，如果有十个请求同时请求缓存资源时，会发生什么？

由于缓存中的数据过期失效，会导致十个请求都同时到穿透到数据库中查询数据，并将数据填写回缓存，对于这个十个请求来说，执行的逻辑都是一致的，这实际是一种资源的浪费。解决方法也很简单，当缓存数据过期时，控制只有一个请求去执行缓存的刷新逻辑，避免所有请求都到数据库执行相同的刷新逻辑。

那么，当前十个请求中，控制只有一个请求去执行缓存的刷新逻辑，那其他的九个请求怎么办呢？

可以设置一个最大阻塞时间，其他九个请求在阻塞时间内，每隔一段间隔时间，去查询缓存中的数据，探测缓存是否已经刷新完毕，如果刷新完毕，则返回缓存中的数据。

这时又引入一个问题，如果阻塞时间过期后，缓存的数据还是没有刷新完毕，该怎么办？

实际上，在设置缓存过期时间的时候，可以对缓存的过期时间进行延长，比如上面的例子，缓存的过期时间设置为5分钟，但底层对缓存的过期时间延长为2分钟，也就是缓存的真实过期时间为7分钟。那么当缓存的过期时间到时（也就是5分钟后），十个请求同时请求缓存数据，这时一个请求去执行缓存的刷新功能，其他九个请求可以返回缓存中的旧数据，因为缓存的实际过期时间被延长了，在延长时间返回内（2分钟），还是可以返回缓存中的旧数据，这实际是一种降级策略。

未完待续...

## 功能特点
- 单线程刷新：当缓存没有命中某个key或者缓存过期时，控制只能有一个线程可以查询数据库刷新缓存
- 异步刷新：当缓存没有命中某个key或者缓存过期时，异步刷新缓存，不会阻塞请求
- 缓存降级：当缓存没有命中某个key或者缓存过期时，如果缓存中存在旧值则返回缓存中的旧值
- 自定义过滤器：实现前置过滤和后置过滤，前置过滤可以用于过滤部分不符合条件的key，后置过滤可以取得key与返回的结果，支持SPI或程序手动注入过滤器
- 失败降级： 如果访问redis错误，则切断与redis的操作，走降级逻辑(自定义mock降级)，期间重试(频率30s)redis查看是否恢复，恢复则继续提供服务

## 参数说明

redis-cache-operator.properties文件用于存放参数配置

| 参数 | 默认值 | 说明 |
| :----:| :----: | ---- |
| loadingKeyExpire | 120000 | 刷新缓存的最大时间，单位毫秒，同一个key在该参数指定的时间内，只能有一个线程刷新缓存数据 |
| extendExpire | 300000 | 缓存数据保存延长时间，单位毫秒，对key的实际过期时间进行延长，当key的实际过期时间到期后，在延长时间内将返回缓存中的旧的值 |
| retryTime | 5 | 当redis连接失败时的重试次数，如果超过重试次数还连接不上，将切断与redis的连接，执行mock降级逻辑 |
| retryPeriod | 30000 | 当redis的连接被切断后，自动探测redis是否恢复的频率，单位毫秒 |
| blockTime | 1000 | 当没有命中缓存且已有其他线程在正在刷新缓存时，当前线程的最大阻塞时间，单位毫秒 |



## 使用
### 字符串对象
####  同步刷新缓存

- 当命中缓存时返回缓存中的值；
- 当没有命中缓存或者缓存失效时，执行业务逻辑获取数据并填充到缓存中，并将获取的值返回；

```java
CacheOperator cacheOperator = new RedisCacheOperator.Builder().build();
String key = "user_name_1";
long expire = 10 * 60 * 1000;  //10 分钟

String name = cacheOperator.get(key, expire, () -> {
    //执行业务逻辑，获取值
    String name = userService.getUserNameById(1);
    return name;
});
```
#### 异步刷新缓存

- 当命中缓存时返回缓存中的值；
- 当没有命中缓存或者缓存失效时，异步执行业务逻辑获取数据并填充到缓存中，结果返回空字符串，可以通过 RedisCacheContext.getContext().getFuture() 获取异步执行的 future 对象

```java
String key = "user_name_1";
String sourceValue = "hello world!";
long expire = 10 * 60 * 1000;  //10 分钟

CacheOperator cacheOperator = new RedisCacheOperator.Builder().build();
String name = cacheOperator.getAsync(key, expire, () -> {
    //执行业务逻辑，获取值
    String name = userService.getUserNameById(1);
    return name;
});

//获取future对象
Future<String> resultFuture = RedisCacheContext.getContext().getFuture();
if (resultFuture != null) {
    //获取到异步刷新的结果
    name = resultFuture.get();
}
```
> 当缓存中的数据没有过期时，通过RedisCacheContext.getContext().getFuture()获取到future将为null

### map对象
####  同步刷新缓存

- 当命中缓存时返回缓存中的值；
- 当没有命中缓存或者缓存失效时，执行业务逻辑获取数据并填充到缓存中，并将获取的值返回；

```java
CacheOperator cacheOperator = new RedisCacheOperator.Builder().build();

String mapKey = "user_1";
long expire = 10 * 60 * 1000;  //10 分钟
Map<String, String> res = cacheOperator.hgetAll(mapKey, expire, () -> {
    //执行业务逻辑，获取值
    Map<String, String> data = new HashMap<>();
    data.put("id", "1");
    data.put("name", "zhangsan");
    return data;
});
```
#### 异步刷新缓存
- 当命中缓存时返回缓存中的值；
- 当没有命中缓存或者缓存失效时，异步执行业务逻辑获取数据并填充到缓存中，结果返回空的HashMap对象，可以通过 RedisCacheContext.getContext().getFuture() 获取异步执行的 future 对象

```java
String mapKey = "user_1";
long expire = 10 * 60 * 1000;  //10 分钟

CacheOperator cacheOperator = new RedisCacheOperator.Builder().build();
Map<String, String> res = cacheOperator.hgetAllAsync(mapKey, expire, () -> {
    //执行业务逻辑，获取值
    Map<String, String> data = new HashMap<>();
    data.put("id", "1");
    data.put("name", "zhangsan");
    return data;
});

//获取future对象
Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture();
if (future != null) {
    //获取到异步刷新的结果
    res = future.get(); 
}
```
> 当缓存中的数据没有过期时，通过RedisCacheContext.getContext().getFuture()获取到future将为null

### list对象

####  同步刷新缓存

- 当命中缓存时返回缓存中的值；
- 当没有命中缓存或者缓存失效时，执行业务逻辑获取数据并填充到缓存中，并将获取的值返回；

```java
CacheOperator cacheOperator = new RedisCacheOperator.Builder().build();

String key = "fruit";
long expire = 10 * 60 * 1000;  //10 分钟
List<String> result = cacheOperator.lrange(key, 0, -1, expire, () -> {
    //执行业务逻辑，获取值
    List<String> fruits = Arrays.asList("apple", "peach", "lemon", "pear");
    return fruits;
});
```

#### 异步刷新缓存
- 当命中缓存时返回缓存中的值；
- 当没有命中缓存或者缓存失效时，异步执行业务逻辑获取数据并填充到缓存中，返回null，可以通过 RedisCacheContext.getContext().getFuture() 获取异步执行的 future 对象

```java
String key = "fruit";
long expire = 10 * 60 * 1000;  //10 分钟

List<String> result = cacheOperator.lrange(key, 0, -1, expire, () -> {
    //执行业务逻辑，获取值
    List<String> fruits = Arrays.asList("apple", "peach", "lemon", "pear");
    return fruits;
});

//获取future对象
Future<List<String>> future = RedisCacheContext.getContext().getFuture();
if (future != null) {
    //获取到异步刷新的结果
    result = future.get(); 
}
```

### mock降级使用

#### 使用mock降级的时机
- 当redis连接不上时，对数据的操作将走Mock降级逻辑，可以根据key来获取降级数据
- 当请求在blockTime指定的最大阻塞时间过后仍然获取不到数据时，对数据的操作将走Mock降级逻辑，可以根据key来获取降级数据

#### 使用方式
* 方式一：使用SPI，则在META-INF/services/live.xsg.cacheoperator.mock.Mock文件中添加Mock实现类
* 方式二：代码注入mock降级逻辑

```java
String key = "user_name_1";

MockRegister.getInstance().register((k, cacheOperator, method) -> {
    if (key.equals(k)) {
        return "zhangsan";
    }
    return null;
});
```
> Mock实现类可以实现Order接口指定调用顺序

### filter过滤器使用

#### 执行filter的时机
- 在每个key操作执行前，会先执行过滤器的preFilter方法，如果返回false，将不会执行后续的逻辑
- 在每个key操作执行后，会将key与获取的结果传入postFilter中

#### 使用方式
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
> Filter实现类可以实现Order接口指定调用顺序
