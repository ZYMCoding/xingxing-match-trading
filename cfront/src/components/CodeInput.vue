<template>
    <!--
    state: 用户选择的内容
     fetch-suggestions: 通过给出queryString,callback来填充提示框内容
     trigger-on-focus: 是否获取焦点之后立即触发搜索
     debounce: 查询触发阈值，用户输入间隔大于某个值再开始搜索
     select: 自动提示框的消息(用户选中的item)
     -->

    <el-autocomplete
            style="width: 100%"
            size="small"
            placeholder="代码/简称"

            v-model="state"
            :fetch-suggestions="querySearchAsync"
            :trigger-on-focus="false"
            :debounce=100
            @select="updateInput"
    />

</template>

<script>

    import {queryCodeName} from '../api/orderApi'

    export default {
        name: "CodeInput",
        data() {
            return {
                state: '',
            }
        },
        methods: {
            //queryString 输入框的值 callback 回调函数
            querySearchAsync(queryString, callback) {
                //从后台服务查询数据
                let list = [{}];
                queryCodeName({
                    key: queryString
                }).then(res =>{
                   if(res.data.code != 0){
                       this.$route.replace({
                           path: "login",
                           query: {
                               msg: result.message
                           }
                       });
                   }else {
                       let resData = res.data.data;
                       for(let i of resData){
                           i.value = ('000000' + i.code).slice(-6)
                           + '--' + i.name;
                       }
                       list = resData;
                       //通知自动提示框显示哪几项
                       callback(list);
                   }
                });
            },
            updateInput(item) {
                // code[int]  000001
                // this.state = item.code;  1
                //  1  --> 0000001 --> 000001
                this.state = ('000000' + item.code).slice(-6);

                this.$bus.emit("codeinput-selected",item);
            }
        }
    }
</script>

<style lang="scss">
    .wide-dropdown {
        width: 600px !important;
    }

</style>