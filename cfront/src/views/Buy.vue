<template>
    <div>
        <!-- 信息提示-->
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-s-order"></i> 委托
                </el-breadcrumb-item>
                <el-breadcrumb-item>买入</el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <el-card shadow="hover" class="container">
            <el-row>
                <!-- 委托组件-->
                <el-col :span="12">
                    <order-widget :direction="0"/>
                </el-col>
                <!-- 订单簿组件-->
                <el-col :span="12">
                    <order-book/>
                </el-col>
            </el-row>

            <!-- 资金持仓的组件-->
            <posi-list/>


        </el-card>
    </div>

</template>

<script>

    import PosiList from '../components/PosiList'
    import OrderWidget from '../components/OrderWidget'
    import OrderBook from '../components/OrderBook'

    export default {
        name: "Buy",
        components: {
            PosiList,
            OrderWidget,
            OrderBook
        }
    }
</script>

<style scoped>

</style>