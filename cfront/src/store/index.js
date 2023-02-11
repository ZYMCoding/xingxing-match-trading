import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    posiData: [],
    orderData: [],
    tradeData: [],
    balance: 0
  },
  mutations: {
    updatePosi(state, posiInfo) {
      state.posiData = posiInfo;
    },
    updateOrder(state, orderInfo) {
      state.orderData = orderInfo;
    },
    updateTrade(state, tradeInfo) {
      state.tradeData = tradeInfo;
    },
    updateBalance(state, balance) {
      state.balance = balance;
    }
  },
  //异步操作
  // actions: {
  // },

  //类似state
  // modules: {
  // }
})
