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

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.QueryPlugin;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.common.jdbc.JdbcDataModelUtil;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.common.jdbc.JdbcQuestionModelUtil;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.model.SqlExpression;

/**
 * mysql查询的插件
 * 
 * @author luowenlei
 *
 */
@Service("querySqlPlugin")
@Scope("prototype")
public class QuerySqlPlugin implements QueryPlugin {

    /**
     * JdbcDataModelUtil
     */
    @Resource(name = "jdbcDataModelUtil")
    private JdbcDataModelUtil jdbcDataModelUtil;
    
    /**
     * jdbcResultSet to DataModel
     */
    @Resource(name = "jdbcQuestionModelUtil")
    private JdbcQuestionModelUtil jdbcQuestionModelUtil;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.querybus.queryplugin.QueryPlugin#query(com
     * .baidu.rigel.biplatform.ac.query.model.QuestionModel)
     */
    @Override
    public DataModel query(QuestionModel questionModel) {
        SqlExpression sqlCause = jdbcQuestionModelUtil.convertQuestionModel2Sql(questionModel);
        DataModel dataModel = jdbcDataModelUtil.executeSql(questionModel, sqlCause);
        return dataModel;
    }
    
}
