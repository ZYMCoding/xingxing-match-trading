import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        name: 'Login',
        component: () => import('../views/Login.vue'),
    },
    {
        path: '/',
        component: () => import( '../views/Home.vue'),
        children: [
            // {
            //     path: '/dashboard',
            //     component: () => import('../components/HelloWorld.vue'),
            // },
            {
                path: '/dashboard',
                component: () => import('../views/Dashboard.vue'),
                meta: {requireAuth: false}
            },
            {
                //修改密码
                path: '/pwdsetting',
                component: () => import( '../views/PwdSetting.vue'),
                meta: {requireAuth: false}
            },
            {
                //银证转账
                path: '/transfer',
                component: () => import( '../views/Transfer.vue'),
                meta: {requireAuth: false}
            },
            {
                //银证转账查询
                path: '/transferquery',
                component: () => import( '../views/TransferQuery.vue'),
                meta: {requireAuth: false}
            },

            {
                //当日委托
                path: '/orderquery',
                component: () => import( '../views/OrderQuery.vue'),
                meta: {requireAuth: false}
            },
            {
                //当日成交
                path: '/tradequery',
                component: () => import( '../views/TradeQuery.vue'),
                meta: {requireAuth: false}
            },
            {
                //历史委托
                path: '/hisorderquery',
                component: () => import( '../views/HisOrderQuery.vue'),
                meta: {requireAuth: false}
            },
            {
                //历史成交
                path: '/histradequery',
                component: () => import( '../views/HisTradeQuery.vue'),
                meta: {requireAuth: false}
            },

            {
                //委买
                path: '/buy',
                component: () => import( '../views/Buy.vue'),
                meta: {requireAuth: false}
            },
            {
                //委卖
                path: '/sell',
                component: () => import('../views/Sell.vue'),
                meta: {requireAuth: false}
            },


        ]
    },
    {
        path: '/403',
        component: () => import('../views/403.vue'),
    },
    {
        path: '/404',
        component: () => import('../views/404.vue'),
    },
    {
        path: '*',
        redirect: '/404'
    },
]




const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

// 路由拦截器
router.beforeEach((to, from, next) => {
    if (to.meta.requireAuth) { // 判断该路由是否需要登录权限
        if (Boolean(sessionStorage.getItem("uid"))) { // 通过vuex state获取当前的user是否存在
            next();
        } else {
            next({
                path: '/',
            })
        }
    } else {
        next();
    }
})

export default router
