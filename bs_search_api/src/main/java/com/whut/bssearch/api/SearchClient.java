package com.whut.bssearch.api;

import com.whut.cailiao.api.commons.ApiResponse;
import com.whut.cailiao.api.model.questionnaire.search.QuestionnaireContentQueryBean;

import java.io.IOException;

/**
 * 只提供搜索功能
 * 不提供创建索引的功能
 * Created by niuyang on 16/3/8.
 */
public interface SearchClient {

    /**
     * 根据查询条件搜索
     * @param questionnaireContentQueryBean
     * @return
     */
    ApiResponse search(QuestionnaireContentQueryBean questionnaireContentQueryBean) throws IOException;

}
