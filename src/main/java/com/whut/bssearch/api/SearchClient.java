package com.whut.bssearch.api;

import com.whut.cailiao.api.commons.ApiResponse;
import com.whut.cailiao.api.commons.ApiResponseCode;
import com.whut.cailiao.api.model.questionnaire.QuestionnaireContent;
import com.whut.cailiao.api.model.questionnaire.search.QuestionnaireContentQueryBean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 只提供搜索功能
 * 不提供创建索引的功能
 * Created by niuyang on 16/3/8.
 */
public class SearchClient {

    /**
     * 根据查询条件搜索
     * @param questionnaireContentQueryBean
     * @return
     */
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
     * 创建索引
     * @param questionnaireContentList
     * @param questionnaireTemplateId
     * @return
     */
    public ApiResponse createIndex(List<QuestionnaireContent> questionnaireContentList, int questionnaireTemplateId) {
        // 获取索引写入器
        ApiResponse response = ApiResponse.createDefaultApiResponse();
        try {
            IndexWriter indexWriter = getIndexWriter();
            // 创建索引
            buildIndex(indexWriter, questionnaireContentList, questionnaireTemplateId);
        } catch (IOException e) {
            response.setRetCode(ApiResponseCode.IO_EXCEPTION);
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

    /**
     * 创建索引
     * @param indexWriter
     * @param questionnaireContentList
     */
    private void buildIndex(IndexWriter indexWriter,
                            List<QuestionnaireContent> questionnaireContentList,
                            int questionnaireTemplateId) throws IOException {
        if (indexWriter == null || questionnaireTemplateId <= 0 || CollectionUtils.isEmpty(questionnaireContentList)) {
            return;
        }
        for (QuestionnaireContent questionnaireContent : questionnaireContentList) {
            if (questionnaireContent == null) {
                continue;
            }
            Document document = new Document();
            document.add(new IntField("questionnaireTemplateId", questionnaireTemplateId, Field.Store.YES));
            document.add(new TextField("productionLine", questionnaireContent.getProductionLine(), Field.Store.YES));
            document.add(new TextField("content", questionnaireContent.getJsonContent(), Field.Store.YES));
            indexWriter.addDocument(document);
        }
    }

    private IndexWriter getIndexWriter() throws IOException {
        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        // 创建索引写入工具
        FSDirectory directory = FSDirectory.open(Paths.get("indexFile"));
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        return new IndexWriter(directory, indexWriterConfig);
    }

}
