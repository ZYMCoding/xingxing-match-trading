<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-s-order"></i> 查询
                </el-breadcrumb-item>
                <el-breadcrumb-item>历史成交</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <el-card shadow="hover" class="container">
            <his-trade-list/>
        </el-card>

    </div>
</template>

<script>

    import HisTradeList from "../components/HisTradeList";

    export default {
        name: 'HisTradeQuery',
        components: {
            HisTradeList,
        },
    };
</script>
