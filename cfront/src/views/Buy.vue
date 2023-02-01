<template>
    <div>
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
                <el-col :span="12">
                    <order-widget :direction="0"/>
                </el-col>
                <el-col :span="12">
                    <order-book/>
                </el-col>
            </el-row>
            <posi-list/>
        </el-card>

    </div>
</template>

<script>

    import OrderWidget from "../components/OrderWidget";
    import OrderBook from "../components/OrderBook";

    import PosiList from "../components/PosiList";


    export default {
        name: "Buy",
        components: {
            OrderWidget,
            OrderBook,
            PosiList
        },
    }
</script>
