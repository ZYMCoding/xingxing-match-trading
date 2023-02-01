<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-pie-chart"></i> 资金股份
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <el-card class="container">
            <posi-list/>
        </el-card>
    </div>
</template>

<script>

    import PosiList from "../components/PosiList";

    export default {
        name: 'dashboard',
        components: {PosiList},
    };
</script>
