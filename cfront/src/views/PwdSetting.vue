<template>
    <div class="crumbs">
        <el-breadcrumb separator="/">
            <el-breadcrumb-item>
                <i class="el-icon-setting"></i> 修改密码
            </el-breadcrumb-item>
        </el-breadcrumb>


        <el-card shadow="hover" class="container">
            <el-row style="padding: 0 15%">
                <el-form :model="form">
                    <el-form-item label="旧密码" label-width="80px">
                        <el-input :type="'password'" v-model="form.oldpassword"/>
                    </el-form-item>
                    <el-form-item label="新密码" label-width="80px">
                        <el-input :type="'password'" v-model="form.newpassword"/>
                    </el-form-item>
                    <el-form-item label="确认密码" label-width="80px">
                        <el-input :type="'password'" v-model="form.newpasswordcfm"/>
                    </el-form-item>
                    <el-form-item>
                        <el-button style="float: right" size="small" type="primary"
                                   @click="onSubmit"
                        >确认修改
                        </el-button>
                    </el-form-item>

                </el-form>
            </el-row>

        </el-card>


    </div>
</template>

<script>

    import encryptMD5 from 'js-md5';
    import {pwdUpdate, logout} from '../api/loginApi'

    export default {
        name: "PwdSetting",
        data() {
            return {
                form: {
                    oldpassword: '',
                    newpassword: '',
                    newpasswordcfm: '',
                }
            }
        },
        methods: {
            logoutCallback(code,msg,data){
              if(code != 0){
                  this.$message.error(msg);
              }  else {
                  logout();
              }
            },
            onSubmit() {
                if (this.form.newpassword != this.form.newpasswordcfm) {
                    this.$message.warning("两次密码输入不一致，请重新输入");
                    return;
                }

                if (this.form.newpassword.length < 4) {
                    this.$message.warning("密码长度太短，请重新输入");
                    return;
                }

                pwdUpdate({
                        uid: sessionStorage.getItem("uid"),
                        oldpwd: encryptMD5(this.form.oldpassword),
                        newpwd: encryptMD5(this.form.newpassword),
                    },
                    this.logoutCallback
                )
            }
        }
    }
</script>

<style scoped>

</style>