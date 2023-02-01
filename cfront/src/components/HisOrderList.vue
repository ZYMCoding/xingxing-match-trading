<template>
    <!--  委托列表  -->
    <div>

        <div class="handle-box">
            <el-row>
                <el-col :span="4">
                    <code-input/>
                </el-col>

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
                <el-button style="float: left" size="small" type="primary" icon="el-icon-search"
                           @click="handleSearch">
                    搜索
                </el-button>
            </el-row>
        </div>


        <el-table
                :data="tableData.slice( (query.currentPage - 1) * query.pageSize, query.currentPage * query.pageSize)"
                border
                :cell-style="cellStyle"
                @sort-change="changeTableSort">
            >
            <el-table-column prop="date" label="委托日期" align="center"
                             sortable :sort-orders="['ascending', 'descending']"/>
            <el-table-column prop="time" label="委托时间" align="center"/>
            <el-table-column prop="code" label="股票代码" align="center"/>
            <el-table-column prop="name" label="名称" align="center"/>
            <el-table-column prop="price" label="委托价格" align="center"/>
            <el-table-column prop="ocount" label="委托数量" align="center"/>
            <el-table-column prop="status" label="状态" align="center"/>
        </el-table>
        <div class="pagination">
            <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="query.currentPage"
                    :page-size="query.pageSize"
                    :total="pageTotal"
                    @current-change="handlePageChange"/>
        </div>
    </div>
</template>

<script>

    import CodeInput from "./CodeInput";
    import moment from 'moment';


    export default {
        name: "HisOrderList",
        components: {CodeInput},
        data() {
            return {
                tableData: [
                    {
                        date: '20200105',
                        time: '14:00:01',
                        code: 600000,
                        name: '浦发银行',
                        price: 10,
                        ocount: 100,
                        status: 1
                    },
                    {
                        date: '20200101',
                        time: '14:00:02',
                        code: 600000,
                        name: '浦发银行',
                        price: 11,
                        ocount: 100,
                        status: 1
                    },
                    {
                        date: '20200103',
                        time: '14:00:03',
                        code: 600000,
                        name: '浦发银行',
                        price: 12,
                        ocount: 100,
                        status: 1
                    },
                    {
                        date: '20200111',
                        time: '14:00:04',
                        code: 600000,
                        name: '浦发银行',
                        price: 13,
                        ocount: 100,
                        status: 1
                    },
                ],
                query: {
                    currentPage: 1, // 当前页码
                    pageSize: 3, // 每页的数据条数,
                    code: '',
                    startDate: '',
                    endDate: '',
                },
                pageTotal: 4,
            };
        },
        created() {
            let _today = moment();
            this.query.endDate = _today.subtract(1, 'days').format('YYYYMMDD');
            this.query.startDate = _today.subtract(8, 'days').format('YYYYMMDD');
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
            // 触发搜索按钮
            handleSearch() {
                this.$set(this.query, 'currentPage', 1);
                console.log("code=%s,startDate=%s,endDate=%s",
                    this.query.code,
                    this.query.startDate,
                    this.query.endDate,
                )
                // this.queryHisOrderPage();
                // code: this.query.code,
                //     startDate: this.query.startDate,
                //     endDate: this.query.endDate,
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
        },
    }
</script>