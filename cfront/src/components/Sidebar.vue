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
                        title: '????????????'
                    },
                    {
                        icon: 'el-icon-s-order',
                        index: 'buy',
                        title: '??????'
                    },
                    {
                        icon: 'el-icon-sell',
                        index: 'sell',
                        title: '??????'
                    },
                    {
                        icon: 'el-icon-search',
                        index: '3',
                        title: '??????',
                        subs: [
                            {
                                index: 'orderquery',
                                title: '????????????'
                            },
                            {
                                index: 'tradequery',
                                title: '????????????'
                            },
                            {
                                index: 'hisorderquery',
                                title: '????????????'
                            },
                            {
                                index: 'histradequery',
                                title: '????????????'
                            },
                        ]
                    },

                    {
                        icon: 'el-icon-bank-card',
                        index: '4',
                        title: '????????????',
                        subs: [
                            {
                                index: 'transfer',
                                title: '????????????'
                            },
                            {
                                index: 'transferquery',
                                title: '????????????'
                            },
                        ]
                    },
                    {
                        icon: 'el-icon-setting',
                        index: 'pwdsetting',
                        title: '????????????'
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
            // ?????? Event Bus ??????????????????????????????????????????
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

        /*????????????????????????1px*/
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
