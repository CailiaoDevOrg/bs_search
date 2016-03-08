package com.whut.bssearch.impl.index.impl;

import com.whut.bssearch.impl.index.QuestionnaireIndexService;
import com.whut.cailiao.api.commons.ApiResponse;
import com.whut.cailiao.api.commons.ApiResponseCode;
import com.whut.cailiao.api.model.pagination.Page;
import com.whut.cailiao.api.model.questionnaire.QuestionnaireContent;
import com.whut.cailiao.api.model.questionnaire.QuestionnaireTemplate;
import com.whut.cailiao.api.service.questionnaire.QuestionnaireService;
import com.whut.cailiao.api.service.questionnaire.QuestionnaireTemplateService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by niuyang on 16/3/8.
 */
@Service("questionnaireIndexService")
public class QuestionnaireIndexServiceImpl implements QuestionnaireIndexService {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private QuestionnaireTemplateService questionnaireTemplateService;

    private Logger logger = LoggerFactory.getLogger(QuestionnaireIndexServiceImpl.class);

    /**
     * 创建索引
     */
    @Override
    public void createIndex() throws IOException {
        // 获取索引写入器
        IndexWriter indexWriter = getIndexWriter();
        // 创建索引
        long createIndexStartTime = System.currentTimeMillis();
        buildIndex(indexWriter);
        long createIndexEndTime = System.currentTimeMillis();
        logger.info("It takes " + (createIndexEndTime - createIndexStartTime) + " milliseconds to create index for the files");
    }

    /**
     * 获取索引写入工具
     * @return
     * @throws IOException
     */
    private IndexWriter getIndexWriter() throws IOException {
        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        // 创建索引写入工具
        FSDirectory directory = FSDirectory.open(Paths.get("/indexFile"));
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        return new IndexWriter(directory, indexWriterConfig);
    }

    /**
     * 获取问卷数据列表
     * @return
     */
    private List<QuestionnaireContent> getQuestionnaireContentList(int questionnaireTemplateId, int currentPage, int pageSize) {
        if (questionnaireTemplateId <= 0 || currentPage <= 0 || pageSize <= 0) {
            logger.error("getQuestionnaireContentList fail, input param error");
            return Collections.EMPTY_LIST;
        }
        ApiResponse response = this.questionnaireService.getQuestionnaireContentCommitList(questionnaireTemplateId, currentPage, pageSize);
        Map<String, Object> bodyMap;
        if (response == null
                || response.getRetCode() != ApiResponseCode.SUCCESS
                || MapUtils.isEmpty(bodyMap = response.getBody())
                || !bodyMap.containsKey("page")) {
            logger.error("rpc call fail");
            return Collections.EMPTY_LIST;
        }
        Page<QuestionnaireContent> page = (Page<QuestionnaireContent>) bodyMap.get("page");
        return page.getList();
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

    private void buildIndex(IndexWriter indexWriter) throws IOException {
        ApiResponse response = this.questionnaireTemplateService.getPublishedQuestionnaireTemplateList();
        Map<String, Object> bodyMap;
        if (response == null || response.getRetCode() != ApiResponseCode.SUCCESS
                || MapUtils.isEmpty(bodyMap = response.getBody())
                || !bodyMap.containsKey("questionnaireTemplateList")) {
            logger.error("rpc call fail");
            return;
        }
        List<QuestionnaireTemplate> questionnaireTemplateList = (List<QuestionnaireTemplate>) bodyMap.get("questionnaireTemplateList");
        if (CollectionUtils.isNotEmpty(questionnaireTemplateList)) {
            questionnaireTemplateList.stream().filter(questionnaireTemplate -> questionnaireTemplate != null && questionnaireTemplate.getId() != null && questionnaireTemplate.getId().compareTo(0) > 0).forEach(questionnaireTemplate -> {
                try {
                    buildIndex(indexWriter, questionnaireTemplate.getId());
                } catch (IOException e) {
                    logger.error("buildIndex error, ex = ", ex);
                }
            });
        }
    }

    private void buildIndex(IndexWriter indexWriter, int questionnaireTemplateId) throws IOException {
        ApiResponse response = this.questionnaireService.getQuestionnaireContentCount(questionnaireTemplateId);
        Map<String, Object> bodyMap;
        if (response == null
                || response.getRetCode() != ApiResponseCode.SUCCESS
                || MapUtils.isEmpty(bodyMap = response.getBody())
                || !bodyMap.containsKey("totalNum")) {
            logger.error("rpc call fail");
            return;
        }
        Integer totalNum = (Integer) bodyMap.get("totalNum");
        if (totalNum == null || totalNum.compareTo(0) < 0) {
            logger.error("totalNum is null or totalNum less than zero");
            return;
        }
        for (int i = 1; i <= (totalNum + 100 - 1) / 100; i++) {
            buildIndex(indexWriter, getQuestionnaireContentList(questionnaireTemplateId, i, 100), questionnaireTemplateId);
        }
    }
}
