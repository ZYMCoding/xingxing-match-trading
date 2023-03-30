<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-bank-card"></i> 流水查询
                </el-breadcrumb-item>
            </el-breadcrumb>

            <el-card class="container">
                <div class="handle-box" style="float: left">
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

                    <el-button size="small" type="primary" icon="el-icon-search">
                        查询
                    </el-button>
                </div>
                <el-table
                        :data="tableData.slice( (query.currentPage - 1) * query.pageSize, query.currentPage * query.pageSize)"
                        border
                        style="font-size: 14px;"
                        :cell-style="cellStyle"
                        @sort-change="changeTableSort"
                >
                    <el-table-column prop="date" label="日期" align="center"
                                     sortable :sort-orders="['ascending', 'descending']"/>
                    <el-table-column prop="time" label="时间" align="center"/>
                    <el-table-column prop="type" :formatter="typeFormatter" label="业务类别" width="200" align="center"/>
                    <el-table-column prop="moneytype" :formatter="moneytypeFormatter" label="币种" align="center"/>
                    <el-table-column prop="money" label="金额" align="center"/>
                </el-table>
                <div class="pagination">
                    <el-pagination
                            background
                            layout="total, prev, pager, next"
                            :current-page="query.currentPage"
                            :page-size="query.pageSize"
                            :total="pageTotal"
                            @current-change="handlePageChange"
                    ></el-pagination>
                </div>
            </el-card>

        </div>
    </div>
</template>

<script>

    export default {
        name: 'TransferQuery',
        data() {
            return {
                query: {
                    startDate: '',
                    endDate: '',
                    currentPage: 1, // 当前页码
                    pageSize: 2 // 每页的数据条数
                },
                //第一步
                tableData: [
                    {
                        date: "2020202",
                        time: "13:03:00",
                        type: 0,
                        moneytype: 0,
                        money: '1000',
                    },
                    {
                        date: "2020202",
                        time: "13:04:00",
                        type: 1,
                        moneytype: 1,
                        money: '10000',
                    },
                    {
                        date: "2020201",
                        time: "13:05:00",
                        type: 1,
                        moneytype: 1,
                        money: '10000',
                    },
                ],
                pageTotal: 3
            };
        },
        methods: {
            //第二步 演示cellStyle
            cellStyle({row, column, rowIndex, columnIndex}) {
                return "padding:2px;";
            },
            moneytypeFormatter(row, column) {
                // 币种  0--人民币  1--美元 2--港币
                let moneyType = row.moneytype;
                switch (moneyType) {
                    case 0:
                        return '人民币';
                    case 1:
                        return '美元';
                    case 2:
                        return '港币';
                    default:
                        return '其他';
                }
            },
            typeFormatter(row, column) {
                // 转账类型 0--证券转银行 1--银行转证券
                let type = row.type;
                switch (type) {
                    case 0:
                        return '证券转银行';
                    case 1:
                        return '银行转证券';
                    default:
                        return '未知';
                }
            },
            // 分页导航
            handlePageChange(val) {
                this.$set(this.query, 'currentPage', val);
            },
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

<style scoped>

</style>
