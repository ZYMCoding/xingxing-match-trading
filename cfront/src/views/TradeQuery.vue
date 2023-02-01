<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-s-order"></i> 查询
                </el-breadcrumb-item>
                <el-breadcrumb-item>当日成交</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <el-card shadow="hover" class="container">
            <trade-list/>
        </el-card>

    </div>
</template>

<script>

    import TradeList from "../components/TradeList";

    export default {
        name: 'TradeQuery',
        components: {
            TradeList,
        },
    };
</script>
