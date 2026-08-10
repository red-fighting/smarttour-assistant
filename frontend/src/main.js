import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import Vant from 'vant'
import router from './router'

const app = createApp(App)
app.use(Vant)
app.use(router)
app.mount('#app')
