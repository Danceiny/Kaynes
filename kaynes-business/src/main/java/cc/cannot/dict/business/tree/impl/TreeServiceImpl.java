package cc.cannot.dict.business.tree.impl;

import cc.cannot.dict.api.req.BaseTreeUpdateReq;
import cc.cannot.dict.business.exception.ArgException;
import cc.cannot.dict.business.redis.RedisImpl;
import cc.cannot.dict.business.tree.TreeCacheService;
import cc.cannot.dict.business.tree.TreeRepositoryService;
import cc.cannot.dict.business.tree.TreeService;
import cc.cannot.dict.business.tree.impl.mq.sender.DeleteNodeSender;
import cc.cannot.dict.common.utils.PrimeTypeUtils;
import cc.cannot.dict.persistence.entity.BaseTreeEntity;
import cc.cannot.dict.persistence.entity.constants.DictTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

// Tree Structure: only one parent, many children, parent also can be grandpa
//            root-node                     level 0
//         /     |      \
//        1-1    |      1-3                 level 1
//       /  \    |    /  |  \
//     21  22   23   24 25  26              level 2

/**
 * 变量名约定：
 * - cids: 子节点数组（一级），即符合 findByParentId(id) 筛选条件的所有节点
 * - pids: 父节点数组（直到根节点），即findById(pid)的递归调用筛选出的所有节点
 * <p>
 * 二、weight排序算法介绍
 * 1. weight值单独作为一个字段落库，类型是int，即范围区间是[-2147483648,2147483648]，通俗点讲是正负21亿之间
 * 2. weight初始化时，有一个对应于table的table_interval，此外有一个对应于level的level_interval，
 * 他们之间的关系是：level_interval = table_interval / (level + 1)，该公式隐含如下逻辑：
 * 越low（即level枚举值越大）的level，数据量越多，相应地，区间需要越小。
 * 简言之，interval，是作为buildWeight过程中用作使weight数组成为等差数列的一个值
 * 随机添加元素时，取当前最大weight，加上 level_interval，记为新元素的weight
 */
@Service
@Slf4j
public class TreeServiceImpl implements TreeService {
    private static final int MAX_DEPTH = 1024;

    private final TreeRepositoryService repository;
    private final TreeCacheService treeCacheService;

    private final DeleteNodeSender deleteNodeSender;

    public TreeServiceImpl(final TreeRepositoryService repository, final TreeCacheService treeCacheService,
                           final DeleteNodeSender deleteNodeSender) {
        this.repository = repository;
        this.treeCacheService = treeCacheService;
        this.deleteNodeSender = deleteNodeSender;
    }

    /**
     * @param simple 是否去除attr等“重型字段”
     */
    public BaseTreeEntity get(final DictTypeEnum type, Object bid, boolean simple) {
        // 如果是hash缓存，则强制simple=false
        simple = !type.useHashCache() && simple;
        BaseTreeEntity entity = treeCacheService.getEntityCache(type, bid, simple);
        if (entity == null) {
            log.debug("【Cache Miss】type:{}, bid:{}", type, bid);
            entity = repository.get(type, bid, simple);
            if (entity != null) {
                treeCacheService.cacheEntity(entity, simple);
            }
        }
        return entity;
    }

    /**
     * @return 可能有null，调用方须check
     */
    public List<BaseTreeEntity> multiGet(final DictTypeEnum type, List<Object> bids, boolean simple) {
        List<BaseTreeEntity> entities = treeCacheService.multiGetEntityCache(type, bids, simple);
        List<BaseTreeEntity> nulls = new ArrayList<>();
        for (int i = 0, l = bids.size(); i < l; i++) {
            if (entities.get(i) == null) {
                log.debug("【Cache Miss】type:{}, bid:{}", type, bids.get(i));
                BaseTreeEntity entity = repository.get(type, bids.get(i), simple);
                if (entity == null) {
                    log.error("【数据异常】DB Not Found, type:{}, bid:{}", type, bids.get(i));
                    continue;
                }
                entities.set(i, entity);
                nulls.add(entity);
            }
        }
        if (!nulls.isEmpty()) {
            treeCacheService.multiCacheEntity(nulls);
        }
        return entities;
    }

    /**
     * 查询的核心方法
     *
     * @param type          DictTypeEnum
     * @param bid           business id
     * @param simple        是否去掉一些属性
     * @param parentDepth   -1 => all, 0 => none
     * @param childrenDepth -1 => all, 0 => none
     */
    @Override
    public BaseTreeEntity get(final DictTypeEnum type, Object bid, boolean simple, int parentDepth, int childrenDepth) {
        boolean perfCheck = parentDepth != 0 || childrenDepth != 0;
        long t1 = System.currentTimeMillis();
        BaseTreeEntity entity = this.get(type, bid, simple);
        if (entity == null) {
            return null;
        }
        long t2 = System.currentTimeMillis();
        this.loadParent(entity, parentDepth, simple);
        long t3 = System.currentTimeMillis();
        this.loadChildren(entity, childrenDepth, simple);
        long t4 = System.currentTimeMillis();
        if (perfCheck) {
            log.info("【Performance Checking】get entity {}-{}: {}ms, " +
                            "load {} parent: {}ms, load {} children: {}ms",
                    type, bid, t2 - t1, parentDepth, t3 - t2, childrenDepth, t4 - t3);
        }
        return entity;
    }

    /**
     * 注意，返回的值区间就是[Integer.MIN_VALUE, Integer.MAX_VALUE]
     */
    private int getMaxWeight(final DictTypeEnum type, final Object pid) {
        return repository.getMaxWeight(type, pid);
    }

    private void initEntityWeight(final BaseTreeEntity entity) {
        long curMW = this.getMaxWeight(entity.getType(), entity.getParentBid());
        long interval = (long) entity.getLevelWeightInterval();
        while (curMW > (Integer.MAX_VALUE - interval)) {
            log.warn("【警告：权重值溢出风险】interval is too big for this pid：{}", entity.getParentBid());
            interval /= 2;
        }
        long newWeight = curMW + interval;
        entity.setWeight((int) newWeight);
    }

    @Override
    public void save(final BaseTreeEntity entity) {
        DictTypeEnum type = entity.getType();
        if (entity.getId() == null) {
            Object pid = entity.getParentBid();
            // 注：level应该是非空的，但这里也会防
            Short level = entity.getLevel();
            if (!Objects.equals(pid, entity.getDefaultBid())) {
                // 指定了一个非零的pid，则必须保证该pid指向的元素存在
                BaseTreeEntity parent = this.get(type, pid, true);
                if (parent == null) {
                    throw new ArgException("非法的parentBid");
                }
                if (PrimeTypeUtils.lt(level, parent.getLevel())) {
                    throw new ArgException("level higher than parent.level is not allowed");
                }
                // 删除parent的cids
                treeCacheService.deleteChildrenBids(type, pid);
            }
            this.initEntityWeight(entity);
            repository.add(entity);
        } else {
            // 修改了level，这是一件大事
            if (entity.getOldLevel() != null && !Objects.equals(entity.getOldLevel(), entity.getLevel())) {
                // 注：升降级level后，不处理children
                BaseTreeEntity parent = this.get(type, entity.getParentBid(), true);
                if (PrimeTypeUtils.lt(entity.getLevel(), parent.getLevel())) {
                    throw new ArgException("level higher than parent.level is not allowed");
                }
                this.loadChildren(entity, 1, true);
                for (Object o : entity.getChildren()) {
                    BaseTreeEntity child = (BaseTreeEntity) o;
                    if (PrimeTypeUtils.gt(entity.getLevel(), child.getLevel())) {
                        throw new ArgException("level lower than child.level is not allowed");
                    }
                }
            }
            // 修改了pid
            if (entity.getOldParentBid() != null && !Objects.equals(entity.getOldParentBid(), entity.getParentBid())) {
                this.changeNodeParent(type, entity.getBid(), entity.getParentBid(), entity.getOldParentBid());
            }
            repository.update(entity);
        }
        treeCacheService.cacheEntity(entity);
    }

    @SuppressWarnings("unchecked")
    private void loadParent(BaseTreeEntity entity, int depth, boolean simple) {
        if (depth == 0) {
            return;
        }
        if (depth < 0) {
            depth = MAX_DEPTH;
        }
        Object bid = entity.getBid();
        Object pid = entity.getParentBid();
        Object bidZero = entity.getDefaultBid();
        if (pid == bidZero) {
            return;
        }
        DictTypeEnum type = entity.getType();
        List<BaseTreeEntity> nodes = new ArrayList<>();
        List<Object> pids = treeCacheService.getParentBids(type, bid);
        if (pids == null) {
            pids = new ArrayList<>();
            BaseTreeEntity parent;
            while (depth-- > 0 && pid != bidZero) {
                parent = this.get(type, pid, true);
                if (parent != null) {
                    pid = parent.getParentBid();
                    // for cache
                    pids.add(parent.getBid());
                    nodes.add(parent);
                } else {
                    log.warn("find parents by bid not found: {}", pid);
                    break;
                }
            }
            treeCacheService.setParentBids(type, bid, pids);
            return;
        }
        if (pids.isEmpty()) {
            return;
        }
        int toIndex = depth > pids.size() ? pids.size() : depth;
        List<BaseTreeEntity> list = this.multiGet(type, pids.subList(0, toIndex), simple);
        for (BaseTreeEntity p : list) {
            if (p != null) {
                nodes.add(p);
            }
        }
        entity.setParentChain(nodes);
    }

    /**
     * 递归调用
     *
     * @param entity 父节点
     * @param depth  向下查询深度
     * @param simple 是否不加载attr
     */
    @SuppressWarnings("unchecked")
    private void loadChildren(final BaseTreeEntity entity, int depth, boolean simple) {
        if (depth == 0) {
            return;
        }
        if (depth < 0) {
            depth = MAX_DEPTH;
        }
        Object bid = entity.getBid();
        DictTypeEnum type = entity.getType();
        // 此处为减少redis请求数，使用multi-get将cids绑到entity身上
        List<Object> cids = entity.getCids();
        // 从缓存中取cids
        if (CollectionUtils.isEmpty(cids)) {
            entity.setCids(treeCacheService.getChildrenBids(type, bid));
            cids = entity.getCids();
        }
        // cids无缓存，读数据库
        if (cids == null) {
            cids = new ArrayList<>();
            List<? extends BaseTreeEntity> children = repository.getByParentBid(type, bid);
            if (depth-- > 0) {
                for (BaseTreeEntity child : children) {
                    cids.add(child.getBid());
                    this.loadChildren(child, depth, simple);
                }
            } else {
                for (BaseTreeEntity child : children) {
                    cids.add(child.getBid());
                }
            }
            entity.setChildren(children);
            treeCacheService.setChildrenBids(type, bid, cids);
        } else if (!cids.isEmpty()) {
            depth--;
            final int finalDepth = depth;
            List<BaseTreeEntity> children2 = this.multiGet(type, cids, simple);
            // prefetch child's cids for all children
            if (depth > 0 && !children2.isEmpty()) {
                List<String> bids = new ArrayList<>();
                for (BaseTreeEntity child : children2) {
                    bids.add(String.valueOf(child.getBid()));
                }
                List<List<Object>> cidsList = treeCacheService.multiGetChildrenBids(type, bids);
                for (int i = 0, l = children2.size(); i < l; i++) {
                    children2.get(i).setCids(cidsList.get(i));
                }
                children2.forEach(child -> {
                    if (child != null) {
                        this.loadChildren(child, finalDepth, simple);
                    }
                });
            }
            entity.setChildren(children2);
        }
    }

    /**
     * 非递归版本的load
     *
     * @param entity
     * @param depth
     * @param simple
     */
    private void loadChildren2(final BaseTreeEntity entity, int depth, boolean simple) {
        if (depth == 0) {
            return;
        }
        if (depth < 0) {
            depth = MAX_DEPTH;
        }
        Object bid = entity.getBid();
        DictTypeEnum type = entity.getType();
        PathCids cids = this.getPathCids(type, bid, depth);
        if (cids == null) {

        }
        List<BaseTreeEntity> entities = this.multiGet(type, new ArrayList<>(cids.getCids()), simple);
        cids.setEntities(entities);
        cids.loadChildren(entity);
    }

    @Resource
    RedisImpl redis;

    private PathCids getPathCids(final DictTypeEnum type, final Object bid, final int depth) {
        String data = redis.getStr(String.format("Dict:PathIds:%s", bid));
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        return new PathCids(bid, depth, data);
    }

    private void changeNodeParent(final DictTypeEnum type, final Object bid, Object parentBid, Object oldParentBid) {
        log.info("【树结构调整】type:{} bid:{} oldPid:{} newPid:{}", type, bid, oldParentBid, parentBid);
        // 删除子节点的pids
        List<Object> cids = treeCacheService.getChildrenBids(type, bid);
        if (cids == null) {
            cids = new ArrayList<>();
            List<? extends BaseTreeEntity> children = repository.getByParentBid(type, bid);
            for (Object o : children) {
                cids.add(((BaseTreeEntity) o).getBid());
            }
        }
        cids.add(bid);
        treeCacheService.multiDeleteParentIds(type, cids);
        treeCacheService.deleteChildrenBids(type, parentBid);
        treeCacheService.deleteChildrenBids(type, oldParentBid);
    }

    /**
     * Deprecated: 对父节点被删除的情况，不删除子节点，而是重置pid
     */
    @SuppressWarnings("unchecked")
    private void removeParent(final BaseTreeEntity entity) {
        entity.markOldTreeNode();
        entity.setParentBid(entity.getDefaultBid());
        this.save(entity);
    }

    @Override
    public void delete(final DictTypeEnum type, final Object bid) {
        // 由于可能要删除众多子节点，比较耗时，扔到队列异步处理
        deleteNodeSender.sendProcess(type, bid);
        // this.deleteNode(type, bid);
    }

    public void deleteNode(final DictTypeEnum type, final Object bid) {
        BaseTreeEntity entity = this.get(type, bid, true);
        if (entity == null) {
            return;
        }
        this.recursiveDelete(entity);
    }

    /**
     * 移除所有的子节点
     */
    @SuppressWarnings("unchecked")
    private void recursiveDelete(final BaseTreeEntity entity) {
        DictTypeEnum type = entity.getType();
        Object bid = entity.getBid();
        // 经过讨论，决定删除所有的子节点（包括子节点的子节点，即该节点下的整个树枝）
        this.loadChildren(entity, 1, true);
        entity.getChildren().forEach(o ->
                this.recursiveDelete((BaseTreeEntity) o));
        // 删除父节点的cids
        this.loadParent(entity, 1, true);
        BaseTreeEntity parent = entity.getParent();
        if (parent != null) {
            treeCacheService.deleteChildrenBids(type, parent.getBid());
        }
        // 从数据库中软删除该节点数据
        repository.delete(entity);
        // 从缓存中删除该节点数据
        treeCacheService.deleteEntityCache(type, bid);
        // 从缓存中删除该节点的cids和pids
        treeCacheService.deleteChildrenBids(type, bid);
        treeCacheService.deleteParentBids(type, bid);
    }


    @Override
    @SuppressWarnings("unchecked")
    public void updateCommonProps(final BaseTreeEntity old, final BaseTreeUpdateReq req) {
        old.updateAttr(req.getAttr());
        if (req.getLevel() != null) {
            old.setLevel(req.getLevel());
        }
        if (req.getPid() != null) {
            old.setParentBid(req.getPid());
        }
    }

    /**
     * 将某个pid下面的所有元素按照现有顺序重置weight值
     */
    private void buildAllWeights(final DictTypeEnum type, final Object pid) {
        List<? extends BaseTreeEntity> entities = repository.getByParentBid(type, pid, Sort.Direction.ASC);
        int l = entities.size();
        log.info("rebuild weights of type:{} pid:{}, with {} db lines to update", type, pid, l);
        if (l == 0) {
            return;
        }
        BaseTreeEntity[] arr = new BaseTreeEntity[l];
        this.buildWeights(type, pid, entities.toArray(arr), 0, Integer.MAX_VALUE, 0, l);
    }

    /**
     * 使用类似“夹逼定理”的方式重置weight值
     *
     * @param min  value exclusive
     * @param max  value exclusive
     * @param from index inclusive
     * @param to   index exclusive
     */
    private void buildWeights(final DictTypeEnum type, final Object pid, final BaseTreeEntity[] sortedEntities,
                              int min, int max,
                              int from, int to) {
        int l = to - from;
        if (l < 0 || max < min) {
            // max == min的情况会包在下面的interval==0中
            return;
        }
        long interval = sortedEntities[from].getLevelWeightInterval();
        if (interval * l + min >= max) {
            interval = ((long) max - (long) min) / l;
            if (interval < 1L) {
                log.warn("【警告】interval=={}, min={}, max={}, 首先尝试重排整个数组", interval, min, max);
                // 还是先尝试把区间调到最大，争取不要去数据库里拿全量数据
                if (max < Integer.MAX_VALUE) {
                    max = Integer.MAX_VALUE;
                    to = sortedEntities.length;
                    l = to - from;
                }
                if (min > Integer.MIN_VALUE) {
                    min = Integer.MIN_VALUE;
                    from = 0;
                    l = to - from;
                }
                interval = ((long) max - (long) min) / l;
                if (interval < 1L) {
                    log.warn("【警告】interval=={}, min={}, max={}, 尝试重排整个数组失败，现在重建weight", interval, min, max);
                    this.buildAllWeights(type, pid);
                    // force update entities and continue sorting
                    Object[] bids = new Object[l];
                    for (int i = from; i < to; i++) {
                        bids[i - from] = sortedEntities[i].getBid();
                    }
                    this.resort(type, bids);
                    return;
                }
            }
        }
        for (int i = from; i < to; i++) {
            BaseTreeEntity entity = sortedEntities[i];
            // 保持比min至少大一个interval
            entity.setWeight((i - from + 1) * (int) interval + min);
            this.save(entity);
        }
    }

    /**
     * 目前的实现是找到左右两边的升序序列，然后夹逼中间的无序序列
     * 一种更理想的做法是，从左到右，以升序序列（这个序列是动态的）的右端点为计算斜率的起点，找到所有“斜率<1”的区间，只需要对这些区间进行buildWeights即可
     *
     * @param left  inclusive
     * @param right exclusive
     */
    private void sort(DictTypeEnum type, Object pid, final BaseTreeEntity[] sortedEntities, int left, int right) {
        int l = right - left;
        // `[case 3]`：三个元素的排序，中间元素可以没有weight值，插入专用
        if (l == 3) {
            // left, mid, right
            if (sortedEntities[left].gt(sortedEntities[left + 1])
                    && sortedEntities[left + 1].gt(sortedEntities[left + 2])) {
                log.debug("【完全有序】【CASE 3】");
                return;
            }
            long prev = (long) sortedEntities[left].getWeight();
            long next = (long) sortedEntities[right - 1].getWeight();
            int avg = (int) ((prev + next) / 2);
            sortedEntities[left + 1].setWeight(avg);
            this.save(sortedEntities[left + 1]);
            if (avg == prev) {
                log.warn("【REBUILD】conflict value: {}, {}", prev, next);
                this.buildWeights(type, pid, sortedEntities, (int) prev, Integer.MAX_VALUE, left, right);
            }
            return;
        }
        int[] weights = new int[l];
        for (int i = left; i < right; i++) {
            // 防一下恶心的NEP
            weights[i] = PrimeTypeUtils.intValue(sortedEntities[i].getWeight());
        }
        // from: 无序区间的左边界(inclusive)，即左起升序区间的右边界(exclusive)
        int from = left;
        if (weights[from] < weights[from + 1]) {
            while (++from <= right - 1 && weights[from - 1] < weights[from]) ;
        }
        // 本身有序
        if (from == right) {
            log.debug("【完全有序】{}, from:{}", weights, from);
            return;
        }
        int interval = sortedEntities[from].getLevelWeightInterval();
        // 特殊场景：只有最后一个元素不是有序的
        if (from == right - 1) {
            log.debug("【n-1有序】");
            sortedEntities[right - 1].setWeight(weights[right - 2] + interval);
            this.save(sortedEntities[right - 1]);
            return;
        }
        // min是无序区间可取的最小weight（包含）
        int min = weights[from];
        if (from > left) {
            min = weights[from - 1];
        }
        // smallTo: 无序区间的右边界(exclusive)
        // bigTo: “安全”无序区间的右边界(inclusive), 即右起“安全”降序区间的左边界(exclusive)
        // 两个to的big/small指的是index的大小
        // “安全”的意思是，比如[1,4,10,9,8,6,18,20]，则bigTo应该是18所在的位置，虽然6是右侧递增序列的左端点，
        // 但它与左侧递增序列的右端点10太近，它们中间的元素9和8无处安放（可以安放的位置数是6-5-1=0），而18与5之间有18-10-1=12个位置
        int bigTo = right - 1;
        int smallTo = bigTo;
        if (weights[bigTo] > weights[bigTo - 1]) {
            while (--bigTo >= left + 1 && weights[bigTo] > weights[bigTo - 1]) {
                if (weights[bigTo] - min >= (bigTo - from)) {
                    smallTo = bigTo;
                }
            }
        }
        int max = weights[bigTo];
        log.debug("\n【局部安全无序区间】：[from:{}, bigTo:{}]，min:{},max:{}" +
                "\n【局部无序区间】：[from:{}, smallTo:{}), ", from, bigTo, min, max, from, smallTo);
        // 如果后面的有序区间更长，就以后面的为准，即调整前面递增序列+无序序列
        // 否则保留前面的递增序列，对后面的无序序列+递增序列进行重排
        if (smallTo - left < right - smallTo) {
            // 这里取Integer.MIN_VALUE作为min，会导致weight值散布得非常宽
            log.info("【前部调整】左区间长度：{}，右区间长度：{}", smallTo - left, right - smallTo);
            this.buildWeights(type, pid, sortedEntities, Integer.MIN_VALUE, weights[smallTo] - 1, left, smallTo);
            return;
        }
        if (min >= max) {
            // 同上，这里取Integer.MAX_VALUE作为max，也会导致weight值散布得非常宽
            log.debug("【右区间内有序序列无效】右区间起始：{}, min:{} > max:{}", from, min, max);
            this.buildWeights(type, pid, sortedEntities, min, Integer.MAX_VALUE, from, right);
        } else {
            this.buildWeights(type, pid, sortedEntities, min, max, from, bigTo);
        }
    }

    /**
     * @param sortedEntities 目标排序结果，必须不包含空元素
     */
    @Override
    public void adjustSortedWeight(final BaseTreeEntity[] sortedEntities) {
        if (sortedEntities == null) {
            return;
        }
        int l = sortedEntities.length;
        if (l < 2) {
            return;
        }
        DictTypeEnum type = sortedEntities[0].getType();
        Object pid = sortedEntities[0].getParentBid();
        this.sort(type, pid, sortedEntities, 0, l);
        // 删除父节点的cids缓存
        treeCacheService.deleteChildrenBids(type, pid);
    }

    @Override
    public void resort(final DictTypeEnum type, final Object[] sortedBids) {
        if (sortedBids == null || sortedBids.length == 0) {
            return;
        }
        LinkedHashSet<Object> bids = new LinkedHashSet<>(Arrays.asList(sortedBids));
        if (bids.isEmpty()) {
            return;
        }
        Object pid = null;
        List<BaseTreeEntity> entities = new ArrayList<>();
        for (Object sortedBid : bids) {
            if (sortedBid == null) {
                continue;
            }
            BaseTreeEntity entity = this.get(type, sortedBid, true);
            if (entity != null) {
                // 检查父节点是否相同
                if (pid == null) {
                    pid = entity.getParentBid();
                } else if (!pid.equals(entity.getParentBid())) {
                    throw new ArgException("只能对parentBid相同的元素进行排序！");
                }
                entities.add(entity);
            } else {
                log.warn("{} entity: {} not found, will not be sorted", type, sortedBid);
            }
        }
        BaseTreeEntity[] entityArr = new BaseTreeEntity[entities.size()];
        this.adjustSortedWeight(entities.toArray(entityArr));
    }

    @Override
    public void resort(final DictTypeEnum type, final Object previousBid, final Object bid,
                       final Object nextBid) {
        this.resort(type, new Object[]{previousBid, bid, nextBid});
    }
}
