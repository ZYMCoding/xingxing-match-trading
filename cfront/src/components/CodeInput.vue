<template>
    <!--自动完成 输入框
    debounce 防抖-->
    <el-autocomplete
            v-model="state"
            style="width: 100%"
            size="small"
            placeholder="代码/简称"
            :fetch-suggestions="querySearchAsync"
            :trigger-on-focus="false"
            :debounce=100
            @select="updateInput"/>
</template>

<script>

    export default {
        name: "CodeInput",
        data() {
            return {
                state: '',
            };
        },
        methods: {
            //queryString 为在框中输入的值 ，callback 回调函数,将处理好的数据推回
            querySearchAsync(queryString, callback) {
                let list = [
                    {
                        code: 1,
                        name: '平安银行',
                        value: '000001-平安银行'
                    },
                    {
                        code: 600000,
                        name: '浦发银行',
                        value: '600000-浦发银行'
                    }
                ];
                callback(list);
                // let list = [{}];
                // queryCodeName({
                //     key: queryString
                // }).then(res => {
                //     if (res.data.code != 0) {
                //         this.$router.push({
                //             path: "login",
                //             query: {msg: res.data.message}
                //         })
                //     } else {
                //         //在这里为这个数组中每一个对象加一个value字段, 因为autocomplete只识别value字段并在下拉列中显示
                //         let resData = res.data.data;
                //         for (let i of resData) {
                //             i.value = ('000000' + i.code).slice(-6) + '--' + i.name;
                //         }
                //         list = resData;
                //         callback(list);
                //     }
            },
            updateInput(item) {
                //0000001
                this.state = ('000000' + item.code).slice(-6);
                this.$bus.emit("codeinput-selected", item);
            }
        },
    }
</script>

<style lang="scss">
    .wide-dropdown {
        width: 600px !important;
    }
</style>