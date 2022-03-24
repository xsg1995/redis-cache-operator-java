# redis缓存操作器

## 需求描述

系统开发中，很多功能都会用到`redis`来缓存数据，以此来提高数据的读取速度，进而提升系统的响应时间。但在高并发场景下缓存过期后，
如果处理不当将会导致一系列问题（缓存穿透、缓存击穿、缓存雪崩），因此，该缓存操作器主要是用来解决缓存使用过程中遇到的一系列问题。

### 存在问题
- 缓存穿透：当要查询的数据不存在（也就是再数据库中不存在，在缓存中也不存在），每次查询都会从缓存中查询，然后穿透到数据库中查询，当请求量比较大时，这会给持久层数据库造成很大的压力
- 缓存击穿：当某个热点'key'过期，导致大量请求同时在缓存中查不到数据，所有请求会同时穿透缓存层直接到数据库中进行查询，这会给持久层数据库造成很大的压力
- 缓存雪崩：当缓存层出现了错误，不能正常工作，于是请求都会直接请求持久层数据库，导致持久层数据库受到影响；当缓存中的大量`key`在同一时间过期，会导致大量请求在缓存中得不到处理，进而所有请求直接到数据库查询，大量请求瞬间可能会压垮数据库

## 功能概述

缓存主要用于加快数据的读取速度，提升系统的响应数据。一般设置缓存数据时，都需要设置缓存的过期时间，当缓存过期后，缓存数据就会被自动清除，这么做目的为了节约内存资源与保障缓存数据的最终一致性。

**问题1：** 如果缓存的过期时间设置为`5`分钟，那么当缓存过期时，如果有`10`个请求同时请求缓存资源时，会发生什么？

由于缓存中的数据过期失效，会导致`10`个请求都同时到穿透到数据库中查询数据，而且对于这`10
`个请求来说，执行的逻辑都是一样的，这是一种资源的浪费，请求量大时，还存在压垮数据库的风险。解决方法也很简单，当缓存数据过期时，控制只有`1`个请求去执行缓存的刷新逻辑，避免所有请求都到数据库执行相同的刷新逻辑。

**问题2：** 当前`10`个请求中，控制只有`1`个请求去执行缓存的刷新逻辑，那其他的`9`个请求怎么办呢？

可以设置一个最大阻塞时间，其他`9`个请求在阻塞时间内，每隔一段间隔时间，去查询缓存中的数据，探测缓存是否已经刷新完毕，如果刷新完毕，则返回缓存中的数据。

**问题3：** 如果阻塞时间过期后，缓存的数据还是没有刷新完毕，该怎么办？

实际上，在设置缓存过期时间的时候，可以对缓存的过期时间进行延长，比如上面的例子，缓存的过期时间设置为`5`分钟，但底层对缓存的过期时间延长为`2`分钟，也就是缓存的真实过期时间为`7`分钟。那么当缓存的过期时间到时（也就是`5
`分钟后），`10`个请求同时请求缓存数据，这时`1`个请求去执行缓存的刷新功能，其他九个请求可以返回缓存中的旧数据，因为缓存的实际过期时间被延长了，在延长时间返回内（`2`分钟），还是可以返回缓存中的旧数据，这可以看做是一种降级策略。

**问题4：** 当缓存层挂了之后会怎样？

查询数据的执行逻辑是`查缓存->缓存无数据则查数据库`，由于缓存已经挂了，因此每次`查缓存`这一步是多余的，因为缓存层已经不能提供服务，那么每次查询都查询一遍缓存已经没有意义。解决办法是如果缓存层在一定的访问次数内都连接不上，则切断缓存层的查询入口，不走缓存查询逻辑，并开启一个定时任务，定时访问缓存，探测缓存层是否已经恢复，如果已经恢复，则继续走查询缓存逻辑。

**问题5：** 在缓存不可用期间，请求应该怎么处理？

由于缓存层不可访问，这时所有的请求如果直接到数据库上查询，不仅获取数据的速度慢了，如果同一时间数据的请求量太大，还会存在把数据库压垮的风险。这时可以提供一个Mock层，做降级策略，当缓存不可用时，请求直接到Mock层取获取数据，Mock层接口的具体实现由客户端自己编码实现，可以查询本地缓存，也可以返回默认值等。

**问题5：** 如果某些key不存在，想要过滤掉这些数据的访问（避免缓存穿透），要怎么办？

如果查询到一些key不存在，一种办法是设置一个默认值到缓存中，每次查询缓存都返回设置的默认值，避免请求直接到数据库查询，；也可以使用另一种办法，使用类似布隆过滤器等方式，对这些不存在的key做前置过滤，避免直接查询缓存穿透到数据库。

**问题6：** 那么这些前置过滤要加在哪里？每个查询的地方都布隆过滤先过滤一遍？

可以加一个Filter层，在每个key进行查询前，都先使用Filter进行过滤一遍，被Filter过滤了的key，则不再执行后续的查询逻辑。有了Filter层，那么具体的布隆过滤器或者其它方式的过滤逻辑，都可以在Filter层统一处理实现。

**以上提到的功能点，便是这个组件要实现的功能。**

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
