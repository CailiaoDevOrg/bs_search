package com.whut.bssearch.war.index.impl;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.whut.bssearch.war.index.QuestionnaireIndexService;
import com.whut.cailiao.api.model.questionnaire.QuestionnaireContent;

/**
 * Created by niuyang on 16/3/8.
 */
@Service("questionnaireIndexService")
public class QuestionnaireIndexServiceImpl implements QuestionnaireIndexService {

    private Logger logger = LoggerFactory.getLogger(QuestionnaireIndexServiceImpl.class);
    /**
     * 创建索引
     */
    @Override
    public void createIndex() throws Exception {
        long createIndexStartTime = System.currentTimeMillis();
        File dataDir = new File("D:\\luceneData");

        File[] dataFiles  = dataDir.listFiles();

        // 创建分词器
        Analyzer analyzer = new StandardAnalyzer();
        // 创建索引写入工具
        FSDirectory directory = FSDirectory.open(Paths.get("/indexFile"));
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        // 批量创建索引

        List<QuestionnaireContent> questionnaireContentList = new ArrayList<>();
        // List<QUe>
        /*for(int i = 0; i < dataFiles.length; i++){
            if(dataFiles[i].isFile() && dataFiles[i].getName().endsWith(".txt")){
                System.out.println("Indexing file " + dataFiles[i].getCanonicalPath());
                Document document = new Document();
                Reader txtReader = new FileReader(dataFiles[i]);
                document.add(Field.Text("path",dataFiles[i].getCanonicalPath()));
                document.add(Field.Text("contents",txtReader));
                indexWriter.addDocument(document);
            }
        }

        indexWriter.optimize();
        indexWriter.close();*/

        long createIndexEndTime = System.currentTimeMillis();

        logger.info("It takes " + (createIndexEndTime - createIndexStartTime) + " milliseconds to create index for the files in directory "
                + dataDir.getPath());
    }
}
