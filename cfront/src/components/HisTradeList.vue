<template>
    <!--  历史成交列表  -->
    <div>
        <div class="handle-box">
            <el-row>
                <el-col :span="5">
                    <code-input style="float: left"/>
                </el-col>

                <el-col :span="12">
                    <div style="float: left;margin-left: 10px">
                        <el-date-picker
                                size="small"
                                type="date"
                                placeholder="选择日期"
                                v-model="query.startDate"
                                value-format="yyyyMMdd"/>
                        -
                        <el-date-picker
                                size="small"
                                type="date"
                                style="margin-right: 5px"
                                placeholder="选择日期"
                                v-model="query.endDate"
                                value-format="yyyyMMdd"/>
                    </div>
                </el-col>
                <el-col :span="2">
                    <el-button style="float: left" size="small" type="primary" icon="el-icon-search"
                               @click="handleSearch">搜索
                    </el-button>
                </el-col>
            </el-row>
        </div>

        <el-table
                :data="tableData.slice( (query.currentPage - 1) * query.pageSize, query.currentPage * query.pageSize)"
                border
                :cell-style="cellStyle"
                style=" width: 100%;font-size: 14px;"
                @sort-change="changeTableSort">
            >
            <el-table-column prop="date" label="成交日期" align="center" />
            <el-table-column prop="time" label="成交时间" align="center"/>
            <el-table-column prop="code" label="股票代码" align="center"/>
            <el-table-column prop="name" label="名称" align="center"/>
            <el-table-column prop="price" label="成交价格(元)" align="center"/>
            <el-table-column prop="tcount" label="成交数量(股)" align="center"/>
            <el-table-column label="成交金额(元)" align="center"/>
            <el-table-column label="方向" align="center"/>
        </el-table>
        <div class="pagination">
            <div class="pagination">
                <el-pagination
                        background
                        layout="total, prev, pager, next"
                        :current-page="query.pageIndex"
                        :page-size="query.pageSize"
                        :total="pageTotal"
                        @current-change="handlePageChange"
                ></el-pagination>
            </div>
        </div>
    </div>
</template>

<script>

    export default {
        name: "HisTradeList",
        components: {CodeInput},
        data() {
            return {
                tableData: [],
                query: {
                    startDate: '',
                    endDate: '',
                    code: '',
                    currentPage: 1, // 当前页码
                    pageSize: 4 // 每页的数据条数
                },
                pageTotal: 0,
            };
        },
        created() {
            let _today = moment();
            this.query.endDate=  _today.subtract(1, 'days').format('YYYYMMDD');
            this.query.startDate  =  _today.subtract(8, 'days').format('YYYYMMDD');
            this.$bus.on("codeinput-selected", this.updateSelectCode);
        },
        beforeDestroy() {
            this.$bus.off("codeinput-selected", this.updateSelectCode);
        },
        methods: {
            cellStyle({row, column, rowIndex, columnIndex}) {
                return "padding:2px;";
            },
            updateSelectCode(item) {
                this.query.code = item.code;
            },
            // 分页导航
            handlePageChange(val) {
                this.$set(this.query, 'currentPage', val);
            },
            //处理排序
            changeTableSort(column) {
                let sortingType = column.order;
                let fieldName = column.prop;
                if (sortingType == "descending") {
                    this.tableData = this.tableData.sort((a, b) => {
                            if (b[fieldName] > a[fieldName]) {
                                return 1;
                            } else if (b[fieldName] === a[fieldName]) {
                                return 0;
                            } else {
                                return -1;
                            }
                        }
                    );
                } else {
                    this.tableData = this.tableData.sort((a, b) => {
                        if (b[fieldName] > a[fieldName]) {
                            return -1;
                        } else if (b[fieldName] === a[fieldName]) {
                            return 0;
                        } else {
                            return 1;
                        }
                    });
                }
            }
        }
    }
</script>