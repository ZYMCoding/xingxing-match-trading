<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-s-order"></i> 查询
                </el-breadcrumb-item>
                <el-breadcrumb-item>历史委托</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <el-card shadow="hover" class="container">
            <his-order-list/>
        </el-card>
    </div>
</template>

<script>

    import HisOrderList from "../components/HisOrderList";

    export default {
        name: 'HisOrderQuery',
        components: {
            HisOrderList,
        },
    };
</script>
