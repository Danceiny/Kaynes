package cc.cannot.dict.business.tree.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import cc.cannot.dict.common.utils.StrUtils;
import cc.cannot.dict.persistence.entity.BaseTreeEntity;

import java.util.*;

public class PathCids {
    /**
     * bid:2 => cids: [3,4,5]
     * bid:3 => cids: [75,324,13]
     * bid:5 => cids: [323,33]
     * bid:2 => paths: [["3","4","5"],["3.75","3.324","3.13","5.323","5.33"]]
     */
    private List<List<String>> paths;

    private List<List<String>> getSubPaths() {
        return paths.subList(0, this.depth);
    }

    private Set<String> cids;

    public Set<String> getCids() {
        if (cids == null) {
            cids = new HashSet<>();
            for (List<String> list : this.getSubPaths()) {
                for (String path : list) {
                    String[] ids = StrUtils.split(path, ".");
                    cids.addAll(Arrays.asList(ids));
                }
            }
        }
        return cids;
    }

    public void setEntities(final List<BaseTreeEntity> entities) {
        for (Object o : entities) {
            BaseTreeEntity entity = (BaseTreeEntity) o;
            this.map.put(entity.getBid().toString(), entity);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadChildren(final BaseTreeEntity parent) {
        int i = depth;
        while (i > 0) {
            List<String> list = this.getSubPaths().get(depth - i);
            for (String path : list) {
                String[] ids = StrUtils.split(path, ".");
                if (ids == null || ids.length == 0) {
                    break;
                }
                int l = ids.length;
                if (l > 1) {
                    String cid = ids[l - 1];
                    String pid = ids[l - 2];
                    this.map.get(pid).getChildren().add(this.map.get(cid));
                } else {
                    parent.getChildren().add(this.map.get(ids[0]));
                }
            }
            i--;
        }
    }

    private Map<String, BaseTreeEntity> map = new HashMap<>();

    private String bid;

    private int depth;

    public PathCids(Object bid, int depth, String paths) {
        this.bid = bid.toString();
        this.paths = JSONObject.parseObject(paths, new TypeReference<List<List<String>>>() {});
        this.depth = depth > this.paths.size() ? this.paths.size() : depth;
    }
}
