package com.whut.bssearch.impl.web;

import com.alibaba.fastjson.JSON;
import com.whut.bssearch.api.SearchClient;
import com.whut.bssearch.impl.index.QuestionnaireIndexService;
import com.whut.cailiao.api.commons.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by niuyang on 16/3/8.
 */
@RestController
public class TestController {

    @Autowired
    private SearchClient client;

    @Autowired
    private QuestionnaireIndexService questionnaireIndexService;

    @RequestMapping("/bstest.html")
    public String bstest() throws Exception {

        questionnaireIndexService.createIndex();

        ApiResponse response = client.search(null);

        return JSON.toJSONString(response);
    }
}
