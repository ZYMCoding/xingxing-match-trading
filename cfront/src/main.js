import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

//bus
import VueBus from 'vue-bus'

Vue.use(VueBus);

//导入 element ui
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

Vue.use(ElementUI);


Vue.config.productionTip = false

let vue = new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app')

import {config} from "./api/frontConfig";
import VertxEventBus from 'vue-vertx3-eventbus-client';

Vue.use(VertxEventBus, {
    host: config.real_ws_remote.host,
    port: config.real_ws_remote.port,
    path: config.real_ws_remote.path
})
vue.$eventBus.enableReconnect(true);

export default vue