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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.common.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.TableData;
import com.baidu.rigel.biplatform.ac.query.data.TableData.Column;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.common.QuestionModel4TableDataUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.model.SqlExpression;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.model.SqlColumn;

/**
 * 
 * SQLDataQueryService的实现类
 * 
 * @author luowenlei
 *
 */
@Service("jdbcDataModelUtil")
public class JdbcDataModelUtil {
    
    /**
     * JdbcConnectionPool
     */
    @Resource(name = "jdbcHandler")
    private JdbcHandler jdbcHandler;
    
    /**
     * executeSql
     * 
     * @param questionModel
     *        questionModel
     * @param SqlExpression
     *        sqlExpression
     * @return DataModel DataModel
     */
    public DataModel executeSql(QuestionModel questionModel, SqlExpression sqlExpression) {
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel) questionModel;
        DataSourceInfo dataSourceInfo = configQuestionModel.getDataSourceInfo();
        questionModel.setUseIndex(false);
        
        List<Map<String, Object>> rowBasedList = jdbcHandler.queryForList(sqlExpression.getSql(),
            dataSourceInfo);
        // getAll columns from Cube
        HashMap<String, SqlColumn> allColums = QuestionModel4TableDataUtils
            .getAllCubeColumns(configQuestionModel.getCube());
        
        // get need columns from AxisMetas
        List<SqlColumn> needColums = QuestionModel4TableDataUtils.getNeedColumns(allColums,
            configQuestionModel.getAxisMetas(), (MiniCube) configQuestionModel.getCube());
        
        // init DataModel
        DataModel dataModel = this.initTableDataModel((MiniCube) configQuestionModel.getCube(),
            needColums);
        // 设置DataModel的ColBased Data
        this.setModelTableData(dataModel, needColums, rowBasedList);
        
        // 如果为getRecordSize为-1，那么需要搜索dataModel.getRecordSize() from database
        if (questionModel.getPageInfo().getTotalRecordCount() == -1) {
            dataModel.setRecordSize(jdbcHandler.queryForInt(sqlExpression.getCountSql(), dataSourceInfo));
        }
        return dataModel;
    }
    
    /**
     * setColumnHeadFields,列表头
     * 
     * @param ConfigQuestionModel
     *        questionModel
     * @return List<HeadField> 行表头
     */
    public DataModel initTableDataModel(MiniCube miniCube, List<SqlColumn> needColums) {
        DataModel dataModel = new DataModel();
        dataModel.setTableData(new TableData());
        dataModel.getTableData().setColumns(new ArrayList<Column>());
        dataModel.getTableData().setColBaseDatas(new HashMap<String, List<String>>());
        for (SqlColumn colDefine : needColums) {
            // String colDefineName = colDefine.getName();
            // if (colDefine.getDimension() != null
            // && colDefine.getDimension().getType() ==
            // DimensionType.TIME_DIMENSION) {
            // // 时间维度
            // colDefineName = colDefine.getLevel().getFactTableColumn();
            // }
            TableData.Column colum = new TableData.Column(colDefine.getTableFieldName(),
                colDefine.getCaption(), colDefine.getTableName());
            dataModel.getTableData().getColumns().add(colum);
            dataModel
                .getTableData()
                .getColBaseDatas()
                .put(colDefine.getTableName() + "." + colDefine.getTableFieldName(),
                    new ArrayList<String>());
        }
        return dataModel;
    }
    
    /**
     * 将Rowbased数据集转成colbased的数据集
     * 
     * @param dataModel
     *        dataModel
     * @param rowBasedList
     *        rowBasedList
     */
    public void setModelTableData(DataModel dataModel, List<SqlColumn> needColums,
        List<Map<String, Object>> rowBasedList) {
        Map<String, List<String>> rowBaseData = dataModel.getTableData().getColBaseDatas();
        for (Map<String, Object> row : rowBasedList) {
            for (int colIdx = 0; colIdx < needColums.size(); colIdx++) {
                SqlColumn column = needColums.get(colIdx);
                String tableDataColumnKey = column.getTableName() + "."
                    + column.getTableFieldName();
                
                if (rowBaseData.get(tableDataColumnKey) == null) {
                    // init TableData Column List
                    rowBaseData.put(tableDataColumnKey, new ArrayList<String>());
                }
                String cell = row.get(column.getSqlUniqueColumn()).toString();
                // get Data from
                List<String> oneColData = rowBaseData.get(tableDataColumnKey);
                oneColData.add(cell);
            }
        }
    }
    
}
