import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOrderStore = defineStore('order', () => {
    const orderId = ref(null)

    const finalPrice = ref(0)

    const itemName = ref('')

    function setOrder(id, price, name) {
        orderId.value = id
        finalPrice.value = price
        itemName.value = name
    }

    function clearOrder() {
        orderId.value = null
        finalPrice.value = 0
        itemName.value = ''
    }

    return{
        orderId,
        finalPrice,
        itemName,
        setOrder,
        clearOrder
    }
})