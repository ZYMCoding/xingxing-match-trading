<template>
    <div>
        <!-- 引入头部导航栏-->
        <v-header></v-header>

        <!-- 引入侧边导航栏-->
        <v-sidebar></v-sidebar>

        <!--<router-view></router-view>-->
        <!-- 主页面业务-->
        <div class="content-box" :class="{'content-collapse':collapse}">
            <div class="content">
                <transition name="move" mode="out-in">
                    <router-view></router-view>
                </transition>
            </div>
        </div>

    </div>
</template>

<script>

    import vHeader from '../components/Header'
    import vSidebar from '../components/Sidebar'
    import {queryPosi, queryOrder, queryTrade, queryBalance} from '../api/orderApi'
    import vue from '../main'
    import {codeFormat} from "../api/formatter";

    export default {
        name: 'Home',
        data() {
            return {
                collapse: false,
            }
        },
        components: {
            vHeader,
            vSidebar
        },
        created() {
            console.log(sessionStorage.getItem("uid"));
            this.$bus.on("collapse-content", msg => {
                this.collapse = msg;
            });

            this.$bus.on("tradechange", res => {
                let jres = JSON.parse(res);
                let msg = "已成: " + (jres.direction == "BUY" ? "买入  " : "卖出  ")
                    + codeFormat(jres.code) + "  " + jres.volume + "股";
                this.$notify({
                    title: '新成交',
                    message: msg,
                    position: 'bottom-right',
                    type: 'success'
                })
            })
        },
        beforeDestroy() {
            this.$bus.off("collapse-content", msg => {
                this.collapse = msg;
            });
        },
        eventbus: {
            handlers: [
                {
                    address: 'orderchange-' + sessionStorage.getItem("uid"),
                    headers: {},
                    callback: function (err, msg) {

                        console.log("recv order change");

                        queryOrder();

                        queryTrade();

                        queryPosi();

                        queryBalance();
                    },

                },
                {
                    address: 'tradechange-' + sessionStorage.getItem("uid"),
                    headers: {},
                    callback: function (err, msg) {
                        vue.$bus.emit("tradechange", msg.body);
                    },
                }
            ]
        }
    }
</script>
