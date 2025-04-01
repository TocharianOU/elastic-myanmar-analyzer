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
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;

public class MyanmarKyteaAnalyzer extends Analyzer {
    private final HttpSegmenter segmenter;
    
    public MyanmarKyteaAnalyzer(HttpSegmenter segmenter) {
        this.segmenter = segmenter;
    }
    
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        // 创建分词器
        final Tokenizer tokenizer = new MyanmarKyteaTokenizer(segmenter);
        
        // 添加过滤器链
        TokenStream result = tokenizer;
        
        // 可选：添加小写过滤器，用于处理可能包含的拉丁字符
        // result = new LowerCaseFilter(result);
        
        return new TokenStreamComponents(tokenizer, result);
    }
} 