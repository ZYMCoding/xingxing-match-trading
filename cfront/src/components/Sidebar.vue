<template>
    <div class="sidebar">
        <el-menu
                class="sidebar-el-menu"
                :default-active="onRoutes"
                :collapse="collapse"
                background-color="#324157"
                text-color="#bfcbd9"
                active-text-color="#20a0ff"
                unique-opened
                router
        >
            <template v-for="item in items">
                <template v-if="item.subs">
                    <el-submenu :index="item.index" :key="item.index">
                        <template slot="title">
                            <i :class="item.icon"></i>
                            <span slot="title">{{ item.title }}</span>
                        </template>
                        <template v-for="subItem in item.subs">
                            <el-submenu
                                    v-if="subItem.subs"
                                    :index="subItem.index"
                                    :key="subItem.index"
                            >
                                <template slot="title">{{ subItem.title }}</template>
                                <el-menu-item
                                        v-for="(threeItem,i) in subItem.subs"
                                        :key="i"
                                        :index="threeItem.index"
                                >{{ threeItem.title }}
                                </el-menu-item>
                            </el-submenu>
                            <el-menu-item
                                    v-else
                                    :index="subItem.index"
                                    :key="subItem.index"
                            >{{ subItem.title }}
                            </el-menu-item>
                        </template>
                    </el-submenu>
                </template>
                <template v-else>
                    <el-menu-item :index="item.index" :key="item.index">
                        <i :class="item.icon"></i>
                        <span slot="title">{{ item.title }}</span>
                    </el-menu-item>
                </template>
            </template>
        </el-menu>
    </div>
</template>

<script>

    export default {
        data() {
            return {
                collapse: false,
                items: [
                    {
                        icon: 'el-icon-pie-chart',
                        index: 'dashboard',
                        title: '资金股份'
                    },
                    {
                        icon: 'el-icon-s-order',
                        index: 'buy',
                        title: '买入'
                    },
                    {
                        icon: 'el-icon-sell',
                        index: 'sell',
                        title: '卖出'
                    },
                    {
                        icon: 'el-icon-search',
                        index: '3',
                        title: '查询',
                        subs: [
                            {
                                index: 'orderquery',
                                title: '当日委托'
                            },
                            {
                                index: 'tradequery',
                                title: '当日成交'
                            },
                            {
                                index: 'hisorderquery',
                                title: '历史委托'
                            },
                            {
                                index: 'histradequery',
                                title: '历史成交'
                            },
                        ]
                    },

                    {
                        icon: 'el-icon-bank-card',
                        index: '4',
                        title: '银证业务',
                        subs: [
                            {
                                index: 'transfer',
                                title: '银证转账'
                            },
                            {
                                index: 'transferquery',
                                title: '转账查询'
                            },
                        ]
                    },
                    {
                        icon: 'el-icon-setting',
                        index: 'pwdsetting',
                        title: '修改密码'
                    },


                ]
            };
        },
        computed: {
            onRoutes() {
                return this.$route.path.replace('/', '');
            }
        },
        created() {
            // 通过 Event Bus 进行组件间通信，来折叠侧边栏
            this.$bus.on('collapse', msg => {
                this.collapse = msg;
                this.$bus.emit('collapse-content', msg);
            });
        }
    };
</script>

<style lang="scss" >
    .sidebar {
        display: block;
        position: absolute;
        left: 0;
        top: 70px;
        bottom: 0;
        overflow-y: scroll;


        .el-menu-item {
            min-width: 150px;
        }

        li {
            text-align: left;
            .el-tooltip {
                width: auto ! important;
            }
        }

        /*下拉导航菜单多出1px*/
        .el-menu {
            border-right-width: 0;
        }

        .el-menu--collapse{
            width: auto ! important;
        }
    }

    .sidebar::-webkit-scrollbar {
        width: 0;
    }

    .sidebar-el-menu:not(.el-menu--collapse) {
        width: 150px;
    }

    .sidebar > ul {
        height: 100%;
    }




</style>
