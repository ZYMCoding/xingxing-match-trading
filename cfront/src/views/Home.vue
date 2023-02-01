<template>
    <div class="wrapper">
        <!--        第一步-->
        <v-header/>

        <!--        第二步-->
        <v-sidebar/>

        <!--        第三步-->
        <!--        <div class="content-box">-->
        <!--            <div class="content">-->
        <!--                <transition name="move" mode="out-in">-->
        <!--                    <router-view></router-view>-->
        <!--                </transition>-->
        <!--            </div>-->
        <!--        </div>-->

        <!--        第四步-->
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
    //这么命名是为了防止和html本身的标签起冲突
    import vHeader from '../components/Header.vue';
    import vSidebar from '../components/Sidebar.vue';


    export default {
        data() {
            return {
                tagsList: [],
                collapse: false,
            };
        },
        components: {
            vHeader,
            vSidebar
        },
        created() {

            this.$bus.on('collapse-content', msg => {
                this.collapse = msg;
            });

        },
    }

</script>
