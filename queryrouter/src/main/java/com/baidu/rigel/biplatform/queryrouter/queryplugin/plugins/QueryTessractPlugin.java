/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.ConfigInfoUtils;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.JsonUnSeriallizableUtils;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.QueryPlugin;

/**
 * tessract查询插件
 * 
 * @author luowenlei
 *
 */
@Service("queryTessractPlugin")
@Scope("prototype")
public class QueryTessractPlugin implements QueryPlugin {

    /**
     * the logger object
     */
    private static Logger logger = LoggerFactory
            .getLogger(QueryTessractPlugin.class);

    /**
     * QUESTIONMODEL_PARAM_KEY
     */
    private static String QUESTIONMODEL_PARAM_KEY = "question";
    
    /**
     * TESSERACT_SERVER_PRO
     */
    private static String TESSERACT_SERVER_PRO = "server.tesseract.address";

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.querybus.queryplugin.QueryPlugin#query(com
     * .baidu.rigel.biplatform.ac.query.model.QuestionModel)
     */
    @Override
    public DataModel query(QuestionModel questionModel) {
        // TODO Auto-generated method stub
        long current = System.currentTimeMillis();
        Map<String, String> params = new HashMap<String, String>();

        params.put(QUESTIONMODEL_PARAM_KEY,
                AnswerCoreConstant.GSON.toJson(questionModel));
        long curr = System.currentTimeMillis();
        logger.info("begin execute query with tesseract ");
        String response = HttpRequest
                .sendPost(ConfigInfoUtils.getServerAddressByProperty(TESSERACT_SERVER_PRO)
                        + "/query", params);
        logger.info("execute query with tesseract cost {} ms",
                (System.currentTimeMillis() - curr));
        ResponseResult responseResult = AnswerCoreConstant.GSON.fromJson(
                response, ResponseResult.class);
        if (StringUtils.isNotBlank(responseResult.getData())) {
            String dataModelJson = responseResult.getData().replace("\\", "");
            dataModelJson = dataModelJson.substring(1,
                    dataModelJson.length() - 1);
            DataModel dataModel = JsonUnSeriallizableUtils
                    .dataModelFromJson(dataModelJson);
            StringBuilder sb = new StringBuilder();
            sb.append("execute query questionModel cost:")
                    .append(System.currentTimeMillis() - current).append("ms");
            logger.info(sb.toString());
            dataModel.setOthers(responseResult.getStatusInfo());
            return dataModel;
        }
        throw new MiniCubeQueryException("query occur error,msg:"
                + responseResult.getStatusInfo());

    }

}
