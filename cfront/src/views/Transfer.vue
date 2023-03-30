<template>
    <div>
        <!-- 信息提示-->
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-baseball"></i> 银证转账
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <el-card shadow="hover" class="container">
            <el-row style="padding: 0 15%;">
                <el-form label-width="80px">
                    <!--转账方式-->
                    <el-form-item label="转账方式">
                        <el-select style="width: 100%" v-model="type">
                            <el-option v-for="item in form.type"
                                       :key="item.id"
                                       :label="item.name"
                                       :value="item.id"/>
                        </el-select>
                    </el-form-item>
                    <!--银行-->
                    <el-form-item label="选择银行">
                        <el-select style="width: 100%" v-model="bank">
                            <el-option v-for="item in form.bank"
                                       :key="item.id"
                                       :label="item.name"
                                       :value="item.name"/>
                        </el-select>
                    </el-form-item>
                    <!--银行密码-->
                    <el-form-item label="银行密码">
                        <el-input :type="'password'" v-model="form.password"/>
                    </el-form-item>
                    <!--选择币种-->
                    <el-form-item label="选择币种">
                        <el-select style="width: 100%" v-model="moneytype">
                            <el-option v-for="item in form.moneytype"
                                       :key="item.id"
                                       :label="item.name"
                                       :value="item.id"/>
                        </el-select>
                    </el-form-item>
                    <!--金额-->
                    <el-form-item label="金额">
                        <el-input v-model="form.money"/>
                    </el-form-item>
                    <el-form-item>
                        <el-button style="float: right;" size="small" type="primary">转账</el-button>
                    </el-form-item>


                </el-form>


            </el-row>

        </el-card>

    </div>
</template>

<script>
    export default {
        name: "Transfer",
        data() {
            return {
                form: {
                    type: [{id: 1, name: '银行转证券(转入)'}, {id: 0, name: '证券转银行(转出)'}],
                    bank: [{id: 0, name: '招商银行'}, {id: 1, name: '建设银行'}],
                    moneytype: [{id: 0, name: '人民币'}, {id: 1, name: '美元'}],
                    password: '',
                    money: 0,
                },

                type: 0,
                bank: '招商银行',
                moneytype: 0,
            }
        }
    }
</script>

<style scoped>

</style>