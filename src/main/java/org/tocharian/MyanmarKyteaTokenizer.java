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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.logging.Logger;

public class MyanmarKyteaTokenizer extends Tokenizer {
    private static final Logger logger = Logger.getLogger(MyanmarKyteaTokenizer.class.getName());
    
    private final HttpSegmenter segmenter;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    
    // 分词结果和位置跟踪
    private char[] inputBuffer;
    private int inputLength;
    private int tokenStart;
    private int tokenEnd;
    private String[] tokens;
    private int currentToken;
    private int numTokens;
    
    public MyanmarKyteaTokenizer(HttpSegmenter segmenter) {
        this.segmenter = segmenter;
        this.inputBuffer = new char[8192]; // 初始缓冲区大小
    }
    
    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        
        if (currentToken >= numTokens) {
            return false;
        }
        
        // 获取当前词汇
        String token = tokens[currentToken];
        termAtt.append(token);
        
        // 计算偏移量 - 这部分逻辑可能需要针对缅甸文优化
        if (currentToken > 0) {
            tokenStart = tokenEnd + 1; // +1 表示空格的位置
        }
        tokenEnd = tokenStart + token.length();
        
        offsetAtt.setOffset(correctOffset(tokenStart), correctOffset(tokenEnd));
        posIncrAtt.setPositionIncrement(1);
        typeAtt.setType("word");
        
        currentToken++;
        return true;
    }
    
    @Override
    public void end() throws IOException {
        super.end();
        final int finalOffset = correctOffset(inputLength);
        offsetAtt.setOffset(finalOffset, finalOffset);
    }
    
    @Override
    public void reset() throws IOException {
        super.reset();
        
        // 读取输入
        inputLength = 0;
        int remaining = inputBuffer.length;
        int offset = 0;
        int n;
        
        while ((n = input.read(inputBuffer, offset, remaining)) != -1) {
            offset += n;
            inputLength += n;
            
            // 扩展缓冲区（如果需要）
            remaining -= n;
            if (remaining <= 0) {
                char[] newBuffer = new char[inputBuffer.length * 2];
                System.arraycopy(inputBuffer, 0, newBuffer, 0, inputLength);
                inputBuffer = newBuffer;
                remaining = inputBuffer.length - offset;
            }
        }
        
        // 处理输入的文本
        String inputText = new String(inputBuffer, 0, inputLength);
        try {
            // 调用HTTP服务进行分词
            String segmentedText = segmenter.segmentText(inputText);
            
            // 调试输出
            logger.fine("原始文本：" + inputText);
            logger.fine("分词结果：" + segmentedText);
            
            // 分割空格分隔的结果
            tokens = segmentedText.split("\\s+");
            numTokens = tokens.length;
        } catch (Exception e) {
            logger.warning("分词失败：" + e.getMessage());
            // 失败时简单分割（适合作为备选方案）
            tokens = inputText.split("\\s+");
            numTokens = tokens.length;
        }
        
        tokenStart = 0;
        tokenEnd = 0;
        currentToken = 0;
    }
} 