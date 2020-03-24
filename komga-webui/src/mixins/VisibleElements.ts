import Vue from 'vue'

const visibleElements = Vue.extend({
  data: () => {
    return {
      visibleElements: [] as number[],
    }
  },
  methods: {
    async onElementIntersect (entries: any, observer: any, isIntersecting: boolean) {
      const elementIndex = Number(entries[0].target.dataset['index'])
      if (isIntersecting) {
        this.visibleElements.push(elementIndex)
      } else {
        this.$_.pull(this.visibleElements, elementIndex)
      }
    },
  },
})

export default visibleElements
