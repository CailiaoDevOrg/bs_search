package com.whut.bssearch.impl.api;

import com.whut.bssearch.api.SearchClient;
import com.whut.cailiao.api.commons.ApiResponse;
import com.whut.cailiao.api.model.questionnaire.search.QuestionnaireContentQueryBean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niuyang on 16/3/8.
 */
@Service("searchClient")
public class SearchClientImpl implements SearchClient {

    /**
     * 根据查询条件搜索
     * @param questionnaireContentQueryBean
     * @return
     */
    @Override
    public ApiResponse search(QuestionnaireContentQueryBean questionnaireContentQueryBean) throws IOException {
        ApiResponse response = ApiResponse.createDefaultApiResponse();
        if (questionnaireContentQueryBean == null) {
            return response;
        }
        BooleanQuery booleanQuery = createBooleanQuery(questionnaireContentQueryBean);
        IndexSearcher searcher = createSearcherInstance();
        List<String> resultList = getResultList(searcher, booleanQuery);
        if (CollectionUtils.isNotEmpty(resultList)) {
            response.addBody("resultList", resultList);
        }
        return response;
    }

    /**
     * 创建组合搜索条件
     * @param questionnaireContentQueryBean
     * @return
     */
    private BooleanQuery createBooleanQuery(QuestionnaireContentQueryBean questionnaireContentQueryBean) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        if (questionnaireContentQueryBean.getId() != null
                && questionnaireContentQueryBean.getId().compareTo(0) > 0) {
            TermQuery idQuery = new TermQuery(new Term("id", questionnaireContentQueryBean.getId().toString()));
            builder.add(idQuery, BooleanClause.Occur.MUST);
        }
        if (questionnaireContentQueryBean.getQuestionnaireTemplateId() != null
                && questionnaireContentQueryBean.getQuestionnaireTemplateId().compareTo(0) > 0) {
            TermQuery templateIdQuery = new TermQuery(new Term("questionnaireTemplateId", questionnaireContentQueryBean.getQuestionnaireTemplateId().toString()));
            builder.add(templateIdQuery, BooleanClause.Occur.MUST);
        }
        if (questionnaireContentQueryBean.getStatus() != null
                && questionnaireContentQueryBean.getStatus().compareTo(0) > 0) {
            TermQuery statusQuery = new TermQuery(new Term("status", questionnaireContentQueryBean.getStatus().toString()));
            builder.add(statusQuery, BooleanClause.Occur.MUST);
        }
        if (StringUtils.isNotBlank(questionnaireContentQueryBean.getProductionLine())) {
            FuzzyQuery productionLineQuery = new FuzzyQuery(new Term("productionLine", questionnaireContentQueryBean.getProductionLine()));
            builder.add(productionLineQuery, BooleanClause.Occur.MUST);
        }
        if (StringUtils.isNotBlank(questionnaireContentQueryBean.getJsonContent())) {
            FuzzyQuery jsonContentQuery = new FuzzyQuery(new Term("jsonContent", questionnaireContentQueryBean.getJsonContent()));
            builder.add(jsonContentQuery, BooleanClause.Occur.MUST);
        }
        return builder.build();
    }

    private IndexSearcher createSearcherInstance() throws IOException {
        FSDirectory directory = FSDirectory.open(Paths.get("indexFile"));
        IndexReader reader = DirectoryReader.open(directory);
        return new IndexSearcher(reader);
    }

    private List<String> getResultList(IndexSearcher searcher, BooleanQuery booleanQuery) throws IOException {
        TopDocs topDocs = searcher.search(booleanQuery, 100);
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < topDocs.totalHits; i++) {
            Document doc = searcher.doc(i);
            String content = doc.get("jsonContent");
            if (StringUtils.isNotBlank(content)) {
                resultList.add(content);
            }
        }
        return resultList;
    }
}
