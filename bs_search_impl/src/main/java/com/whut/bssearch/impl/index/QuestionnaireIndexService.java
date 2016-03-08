package com.whut.bssearch.impl.index;

import java.io.IOException;

/**
 * Created by niuyang on 16/3/8.
 * 问卷索引创建服务
 */
public interface QuestionnaireIndexService {

    /**
     * 创建索引
     */
    void createIndex() throws IOException;

}
