# Redis

# Introduction

## Feature

- open source (BSD)
- written in c language
- data structure store
- key value pairs in memory 
- 10w ops / single node
- standalone, master / slave, cluster

## Use Case

- cache
- database































# Data Structure

## string
|Command|O|Version|Note|
|:--|---|--|---|
|APPEND key value|~O(1)|2.0.0||
|GET key|O(1)|1.0.0||
|GETRANGE key start end|O(N)|2.4.0|<2.0 SUBSTR|
|GETSET key value|O(1)|1.0.0||
|MGET k1 [k2 ...]|O(N)|1.0.0||
|MSET k1 v1 [k2 v2 ...]|O(N)|1.0.1||
|MSETNX k1 v1 [k2 v2 ...]|O(N)|1.0.1|atomic|
|PSETEX key milliseconds value|O(1)|2.6.0||
|<font color="blue">SET key value [EX seconds] [PX milliseconds] [NX\|XX]</font>|O(1)|1.0.0|EX\|PX\|NX\|XX 2.6.12|
|SETEX key seconds value|O(1)|2.0.0||
|SETNX key value|O(1)|1.0.0||
|SETRANGE key offset value|O(1)|2.2.0||
|STRLEN key|O(1)|2.2.0||





















### number

|Command|O|Version||
|:--|---|--|---|
|DECR key|O(1)|1.0.0||
|DECRBY key decrement|O(1)|1.0.0||
|INCR key|O(1)|1.0.0||
|INCRBY key increment|O(1)|1.0.0||
|INCRBYFLOAT key increment|O(1)|2.6.0||


































### bitmap

|Command|O|Version||
|:--|---|--|---|
|BITCOUNT key [start end]|O(N)|2.6.0||
|BITOP NOT destkey srckey|O(1)|2.6.0||
|BITOP AND destkey sk1 sk2 ... skn | O(N) | 2.6.0 |  |
|BITOP OR   destkey sk1 sk2 ... skn | O(N) | 2.6.0 |  |
|BITOP XOR destkey sk1 sk2 ... skn | O(N) | 2.6.0 |  |
|BITPOS key bit [start] [end] | O(N) | 2.8.7 |  |
|GETBIT key offset | O(1) | 2.2.0 | |
|SETBIT key offset value | O(1) | 2.2.0 | |












































### hyperloglog


|Command|O|Version||
|:--|---|--|---|
|PFADD key v1 [v2 ...]|~O(1)|2.8.9||
|PFCOUNT k1 [k2 ...]|~O(1)|2.8.9||
|PFMERGE destkey sk1 [sk2 ...]|O(N)|2.8.9||







































## list

|Command|O|Version|Note|
|:--|---|--|---|
|BRPOP k1 [k2 ...] timeout|O(1)|2.0.0||
|BRPOPLPUSH src dest timeout|O(1)|2.2.0||
|BLPOP k1 [k2 ...] timeout|O(1)|2.0.0||
|LINDEX key index|O(N)|1.0.0||
|LINSERT key BEFORE\|AFTER pivot value|O(N)|2.2.0||
|LLEN key|O(1)|1.0.0||
|LPOP key|O(1)|1.0.0||
|LPUSH key v1 [v2 ...]|~O(1)|1.0.0|<2.4 one value|
|LPUSHX key value|O(1)|2.2.0||





## hash
|Command|O|Version||
|:--|---|--|---|
|PFADD key v1 [v2 ...]|~O(1)|2.8.9||




## set

|Command|O|Version||
|:--|---|--|---|
|PFADD key v1 [v2 ...]|~O(1)|2.8.9||





## sorted set
|Command|O|Version||
|:--|---|--|---|
|PFADD key v1 [v2 ...]|~O(1)|2.8.9||


### geo
|Command|O|Version||
|:--|---|--|---|
|PFADD key v1 [v2 ...]|~O(1)|2.8.9||


















# Persistence

## RDB (Snapshot in Binary )
- disable
```
save ""
```
- enable (default)
```
save 900  1
save 300  10
save 60   10000
```

```
rdbcompression yes  # LZF
dbfilename dump.rdb
rdb-save-incremental-fsync yes # fsync/32MB
```

- save		#  sync 
- bgsave	# async



```mermaid
graph LR

A[bgsave] -->B(父进程)
B --> C{有子进程}
C -->|True| B
C -->|False| E[fork]
E --> F[子进程]
F --> H[RDB文件]
F -->|通知父进程| B

```



info persistence

config get dir

config get rdbcompression

redis-check-rdb 



## AOF (Write Command in Text)



```mermaid
graph LR

A[写命令] -->|append|B(AOF缓冲)
B -->|sync| C(AOF文件)
C -->|rewrite| C
```

```
appendonly no
appendfilename appendonly.aof

auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

aof-use-rdb-preamble yes

#appendfsync always
appendfsync everysec
#appendfsync no
```

- sync policy
  
  - always : write + fsync
  - everysec : write + fsync / seconds 
- no :  write + fsync / os
  
- rewrite

  big -> small

  - expired key

  - set k1 v1 , set k1 v2  等对一个key做多次操作，只保留最后一条命令

  - lpush list a,   lpush list b,   lpush list c 等 合并成一条命令 lpush list a b c

- bgrewriteaof

- auto-aof-rewrite-percentage 100

- auto-aof-rewrite-min-size 64mb











## RDB v.s. AOF



||rdb|aof|rdb + aof|
| ---  | ---  | ---  | ---  |
|操作|全量数据快照|增量追加写命令|先快照再增量|
| 频度 | save 60 10000<br>默认一分钟修改一万次以上快照一次 | 默认每秒写一次盘，推荐 | bgrewriteaof |
| 文件 | 压缩，二进制文件小，加载快 | 文本文件大，需要rewrite，加载慢 |  |
| 故障 | 可能丢失数据多 | 可能丢失数据少 |  |

rdb+aof 二者兼得

 























## RESTART - load data

```mermaid
graph LR

A[start] -->B{appendonly}
B -->|yes| C{AOF file}
C -->|exist| D[load AOF file]
B -->|no| E{RDB file}
E -->|exist| F[load RDB file]
C -->|not exist| E
E -->|not exist| G[end]
D --> G
F --> G
```



# Replication

```
replicaof ip port / slaveof ip port
masterauth master-password

replica-serve-stale-data yes
replica-read-only yes

replica-priority 100
```

# Master - Slave

slaveof  ip port

 

# Sentinel



# Cluster



# Case Study

| 缓存 |           流量特点            |  cache   |  database  | 解决方案                |
| ---- | :---------------------------: | :------: | :--------: | ----------------------- |
| 穿透 | 大流量访问少数key<br>如恶意攻击 |    无    | 无数据 | 1.先鉴权后访问，拦截非法访问<br>2.检查key是否合法，范围检查，布隆过滤器等<br>3. 缓存空值，并短期有效<br> |
| 击穿 | 大流量访问少数key<br>如爆款产品 |    无    |  有数据  | 1.热key不过期<br>2.加锁，请求相同的key互斥<br> |
| 雪崩 |       大流量访问多个key       | 集中失效 |  有数据  | 1.过期时间+随机数，避免集中失效<br>2.使用缓存集群，分散热点key<br>3.热key不过期 |



# Optimize

slowlog  get  n