<template>
    <!--  持仓列表  -->
    <div>
        <el-row>
            <el-col :span="5">
                可用资金:{{balance}}
            </el-col>
        </el-row>

        <el-table
                :data="
                    tableData.slice
                    (
                        (query.currentPage - 1) * query.pageSize,
                        query.currentPage * query.pageSize
                    )
                "
                border
                :cell-style="cellStyle"
                @sort-change="changeTableSort"
        >
            <el-table-column prop="code" label="代码" align="center"
                             sortable :sort-orders="['ascending', 'descending']"
            />
            <el-table-column prop="name" label="名称" align="center"/>
            <el-table-column prop="count" label="股票数量" align="center"/>
            <el-table-column prop="cost" label="总投入" align="center"/>
            <el-table-column label="成本" align="center"/>
        </el-table>

        <div class="pagination">
            <el-button round
                       type="primary" size="mini"
                       style="margin-top:2px;float: right"
                       icon="el-icon-refresh"
                       @click="">
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

    export default {
        name: "PosiList",
        data() {
            return {
                tableData: [
                    {code: '600025', name: '华能水电', count: 100, cost: 20},
                    {code: '600000', name: '浦发银行', count: 100, cost: 20},
                    {code: '000001', name: '平安银行', count: 100, cost: 20},
                    {code: '600886', name: '国投电力', count: 100, cost: 20},
                ],
                dataTotalCount: 4,

                balance: 10,

                query: {
                    currentPage: 1, // 当前页码
                    pageSize: 2 // 每页的数据条数
                }
            };
        },
        methods: {
            // 分页导航
            handlePageChange(val) {
                this.$set(this.query, 'currentPage', val);
            },

            //处理排序
            changeTableSort(column) {
                console.log('600886' - '000001');
                let fieldName = column.prop;
                if (column.order == "descending") {
                    this.tableData = this.tableData.sort((a, b) => b[fieldName] - a[fieldName]);
                } else {
                    this.tableData = this.tableData.sort((a, b) => a[fieldName] - b[fieldName]);
                }
            },

            cellStyle({row, column, rowIndex, columnIndex}) {
                    return "padding:2px";
            },
        },
        computed: {},
        watch: {},
        created() {
        }
    }
</script>

<style scoped>

</style>