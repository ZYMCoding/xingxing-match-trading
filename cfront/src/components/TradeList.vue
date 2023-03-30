<template>
    <!--  成交列表  -->
    <div>
        <el-table
                :data="tableData.slice( (query.currentPage - 1) * query.pageSize, query.currentPage * query.pageSize)"
                border
                :cell-style="cellStyle"
                style=" width: 100%;font-size: 14px;"
                :default-sort ="{prop:'time',order:'descending'}"
                @sort-change="changeTableSort"
            >
            <el-table-column prop="time" label="成交时间" align="center"
                             sortable :sort-orders="['ascending', 'descending']"/>
            <el-table-column prop="code" label="股票代码" :formatter="codeFormatter" align="center"/>
            <el-table-column prop="name" label="名称" align="center"/>
            <el-table-column prop="price" label="成交价格(元)" :formatter="priceFormatter" align="center"/>
            <el-table-column prop="tcount" label="成交数量(股)" align="center"/>
            <el-table-column label="成交金额(元)" :formatter="tmoneyFormatter" align="center"/>
            <el-table-column label="方向" :formatter="directionFormatter" align="center"/>
        </el-table>
        <div class="pagination">
            <el-button round
                       type="primary" size="mini"
                       style="margin-top:2px;float: right"
                       icon="el-icon-refresh"
                       @click="queryTrade">
                刷新
            </el-button>
            <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="query.currentPage"
                    :page-size="query.pageSize"
                    :total="dataTotalCount"
                    @current-change="handlePageChange"/>
        </div>
    </div>
</template>

<script>

    import {queryTrade,queryBalance} from "../api/orderApi";
    import {codeFormat,moneyFormat,directionFormat} from "../api/formatter";

    export default {
        name: "TradeList",
        data() {
            return {
                tableData: [],
                query: {
                    currentPage: 1, // 当前页码
                    pageSize: 4 // 每页的数据条数
                },
            };
        },
        methods: {
            cellStyle({row, column, rowIndex, columnIndex}) {
                return "padding:2px;";
            },
            codeFormatter(row, column) {
                return codeFormat(row.code);
            },
            priceFormatter(row, column) {
                return moneyFormat(row.price);
            },
            tmoneyFormatter(row, column) {
                return moneyFormat(row.tcount * row.price);
            },
            directionFormatter(row, column) {
                return directionFormat(row.direction);
            },
            queryTrade() {
                queryTrade();

                queryBalance();
            },
            // 触发搜索按钮
            handleSearch() {
                this.$set(this.query, 'pageIndex', 1);
            },
            // 分页导航
            handlePageChange(val) {
                this.$set(this.query, 'currentPage', val);
            },
            //处理排序
            changeTableSort(column) {
                let sortingType = column.order;
                let fieldName = column.prop;
                if (fieldName === "time") {
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
        },
        computed: {
            tradeData() {
                return this.$store.state.tradeData;
            },
            dataTotalCount() {
                return this.$store.state.tradeData.length;
            }
        },
        watch: {
            tradeData: function (val) {
                this.tableData = val;
            }
        },
        created() {
            this.tableData = this.tradeData;
        }
    }
</script>

<style scoped>

</style>