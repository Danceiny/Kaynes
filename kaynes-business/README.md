## Redis缓存结构
1. entity缓存：
分两种情形：string / hash
- string类型，以json字符串存储，带过期时间，且同一entity可能对应两个版本（两个redis键），一个键有attr（FULL），另一个没有（SIMPLE）。
- hash类型，hash中的field是业务id，value是json字符串，不带过期时间，只有一个版本（全量数据），目前只有CATEGORY采用了这一缓存结构。

>>> 采用string类型的考虑是需要一个过期时间，不至于长期缓存一些冷数据造成浪费。。

键名规则如下：
- string 全量数据版本：
`Dict:Entity:[TYPE]:FULL:[bid]`
- string 没有attr的版本：
`Dict:Entity:[type]:SIMPLE:[bid]`
- hash 全量数据版本
key: `Dict:Entity:[TYPE]`, field: `[bid]`

其中`[TYPE]`是字典库名称的大写，比如`CATEGORY`,`CAR`，`[bid]`是entity的业务id，比如`13`,`gongzuo`。

2. pids缓存：redis的hash，以json字符串存储。

键名规则如下:
`Dict:0:[TYPE]`
Hash中的field规则：
`[bid]`

内容实质是业务id的有序数组，数组中前一个元素的parent_bid指向后一个元素，即后一个元素是前一个元素的父级。

3. cids缓存

规则同pids缓存，只是0换成了1。

内容实质是业务id的有序数组，这些元素的parent_bid都指向`[bid]`。

## BaseTree代码导读

### 数据实体设计
1. [BaseTreeEntity](../kaynes-persistence/src/main/java/com/baixing/dict/persistence/entity/BaseTreeEntity.java)

### 接口
1. 增删改查
[TreeService](../kaynes-api/src/main/java/cc/cannot/dict/business/tree/TreeService.java) 

2. 排序
[WeightSortInterface](../kaynes-api/src/main/java/cc/cannot/dict/business/tree/WeightSortInterface.java)

### 实现
[TreeServiceImpl](../kaynes-api/src/main/java/cc/cannot/dict/business/tree/impl/TreeServiceImpl.java)的实现思路：

1. 查询，支持指定向上向下查询的深度
- `void loadParent(BaseTreeEntity entity, int depth)`
- `void loadChildren(BaseTreeEntity entity, int depth)`

2. 排序
- `void adjustSortedWeight(final BaseTreeEntity[] sortedEntities)`
- `void buildAllWeights(final DictTypeEnum type, final Object pid)`