/*
 * Licensed to Tocharian under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Tocharian licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.tocharian;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.plugin.analysis.AnalyzerFactory;
import org.elasticsearch.plugin.NamedComponent;

import java.util.logging.Logger;

@NamedComponent(value = "myanmar_kytea_analyzer")
public class MyanmarKyteaAnalyzerFactory implements AnalyzerFactory {
    private static final Logger logger = Logger.getLogger(MyanmarKyteaAnalyzerFactory.class.getName());
    private final HttpSegmenter segmenter;
    
    // 分词服务URL
    private static final String DEFAULT_SERVICE_URL = "http://gis.tocharian.eu:5000/segment";

    public MyanmarKyteaAnalyzerFactory() {
        // 初始化HTTP分词器
        logger.info("正在初始化MyanmarKyteaAnalyzerFactory...");
        try {
            String serviceUrl = System.getProperty("myanmar.segmenter.url", DEFAULT_SERVICE_URL);
            this.segmenter = new HttpSegmenter(serviceUrl);
            
            // 测试分词器并记录结果
            String testResult = segmenter.segmentText("အမှတ်၂၅၉");
            logger.info("分词器测试结果: " + testResult);
            
        } catch (Exception e) {
            logger.severe("初始化HTTP分词器失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("HTTP分词器初始化失败", e);
        }
        logger.info("MyanmarKyteaAnalyzerFactory初始化完成");
    }

    @Override
    public Analyzer create() {
        return new MyanmarKyteaAnalyzer(segmenter);
    }
} 